/*
* (C) Copyright 2014-2016 Peter Sauer (http://treedb.at/).
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 */
package at.treedb.jslib;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import javax.persistence.Entity;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import at.treedb.ci.Blob;
import at.treedb.ci.MimeType;
import at.treedb.db.Base;
import at.treedb.db.ClassID;
import at.treedb.db.DAO;
import at.treedb.db.DAOiface;
import at.treedb.db.DBkey;
import at.treedb.user.User;
import at.treedb.util.ArchiveEntry;
import at.treedb.util.ContentInfo;
import at.treedb.util.FileStorage;
import at.treedb.util.Stream;

/**
 * <p>
 * Helper class to include external JavaScript libraries.
 * </p>
 * 
 * @author Peter Sauer
 *
 */
@SuppressWarnings("serial")
@Entity
public class JsLib extends Base implements Comparable<Object> {
    public static final String HEADER = "JSLIB";
    private String name; // name of the JavaScript library
    private String libVersion; // library version string - must satisfy the
                               // scheme x.y[.z]
    private String homepage; // homepage of the library
    private String description; // library description
    @DBkey(value = Blob.class)
    private int jsLibArchive; // binary data
    transient private byte[] data; // binary archive data
    transient private boolean isExtracted; // indicates if the archive is
                                           // extracted
    transient private ZipArchiveInputStream zipInput; // ZIP archive
    transient private SevenZFile sevenZFile; // 7-zip archive
    transient private HashMap<String, ArchiveEntry> archiveMap = new HashMap<String, ArchiveEntry>();
    transient private HashMap<String, byte[]> fileCache = new HashMap<String, byte[]>();
    transient private ArrayList<String> javaFiles = new ArrayList<String>();
    transient private FileChannel inChannel; //
    transient private File dumpFile; // dump file containing the archive as a
                                     // single file
    static final Object lockObj = new Object();

    public enum Fields {
        name, libVersion, homepage, description, jsLibArchive;
    }

    private static HashMap<String, HashMap<Version, JsLib>> jsLibs = null;

    @Override
    public ClassID getCID() {
        return ClassID.JSLIB;
    }

    protected JsLib() {
    }

    protected JsLib(String name, String version, String homepage, String description, Blob blob) {
        this.name = name;
        this.libVersion = version;
        this.homepage = homepage;
        this.description = description;
        jsLibArchive = blob.getHistId();
        data = blob.getBinaryData();
    }

    /**
     * Creates a JavaScript library.
     * 
     * @param dao
     *            {@code DAOiface} data access object
     * @param user
     *            user creator of the {@code JsLib}
     * @param name
     *            library name
     * @param version
     *            library version - library version string - must satisfy the
     *            scheme x.y[.z]
     * @param homepage
     *            homepage of the JavaScript library
     * @param description
     *            library description
     * @param libData
     *            library archive - must be a ZIP or a 7z archive
     * @return {@code JavaScript} object
     * @throws Exception
     */
    public static JsLib create(DAOiface dao, User user, String name, String version, String homepage,
            String description, byte[] libData) throws Exception {
        Objects.requireNonNull(name, "JsLib.JsLib(): parameter name can't be null");
        Objects.requireNonNull(libData, "JsLib.JsLib(): parameter libData can't be null");
        Objects.requireNonNull(version, "JsLib.JsLib(): parameter version can't be null");
        MimeType mtype = ContentInfo.getContentInfo(libData);
        if (mtype == null) {
            throw new Exception("JsLib.JsLib(): Unknown binary format");
        }
        if (!(mtype.equals(MimeType.ZIP) || mtype.equals(MimeType._7Z))) {
            throw new Exception("JsLib.JsLib(): library archive must be zip or 7zip");
        }
        Blob blob = Blob.create(dao, null, user, ClassID.JSLIB, libData);
        JsLib lib = new JsLib(name, version, homepage, description, blob);
        Base.save(dao, null, user, lib);
        return lib;
    }

    @Override
    public void callbackAfterLoad(DAOiface dao) throws Exception {
        Blob blob = (Blob) Base.load(dao, Blob.class, this.jsLibArchive, null);
        this.data = blob.getBinaryData();
    }

    public static void delete(User user, JsLib lib) throws Exception {
        Base.delete(user, lib, false);
    }

    public static void delete(DAOiface dao, User user, JsLib lib) throws Exception {
        Base.delete(dao, user, lib, false);
    }

    @Override
    public boolean isCallbackAfterLoad() {
        return true;
    }

    /**
     * Returns the name of the library.
     * 
     * @return libray name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the version of the library.
     * 
     * @return library version
     */
    public String getLibVersion() {
        return libVersion;
    }

    /**
     * Returns a description of the library.
     * 
     * @return library description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the homepage of the library
     * 
     * @return homepage of the library
     */
    public String getHomeapge() {
        return homepage;
    }

    /**
     * Returns the binary data of an archive entry.
     * 
     * @param path
     *            archive path
     * @return binary data
     * @throws IOException
     */
    public byte[] getArchiveEntry(String path) throws IOException {
        if (!archiveMap.containsKey(path)) {
            return null;
        }
        byte[] data = fileCache.get(path);
        if (data == null) {
            ArchiveEntry e = archiveMap.get(path);
            ByteBuffer buf = ByteBuffer.allocate(e.getLength());
            inChannel.read(buf, e.getOffset());
            data = buf.array();
            fileCache.put(path, data);
        }
        return data;
    }

    public static boolean exists(DAOiface dao, String name) throws Exception {
        initJsLib(dao);
        if (jsLibs == null) {
            return false;
        }
        return jsLibs.containsKey(name);
    }

    // TODO - update code for uploading/changing libs
    public static boolean initJsLib(DAOiface dao) throws Exception {
        synchronized (lockObj) {
            if (jsLibs == null) {
                @SuppressWarnings("unchecked")
                List<JsLib> list = (List<JsLib>) loadEntities(dao, JsLib.class, null, false);
                if (list.size() > 0) {
                    jsLibs = new HashMap<String, HashMap<Version, JsLib>>();
                    for (JsLib lib : list) {
                        String n = lib.getName();
                        HashMap<Version, JsLib> v = jsLibs.get(n);
                        if (v == null) {
                            v = new HashMap<Version, JsLib>();
                            jsLibs.put(n, v);
                        }
                        v.put(new Version(lib.getLibVersion()), lib);
                    }
                }
                return true;
            }
            return false;
        }
    }

    /**
     * Returns a list of available javascript libraries without loading the
     * binary archive data.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @return java script list
     * @throws Exception
     */
    public static synchronized List<JsLib> loadAllEntities(DAOiface dao) throws Exception {
        @SuppressWarnings("unchecked")
        List<JsLib> list = (List<JsLib>) loadEntities(dao, JsLib.class, null, false);
        return list;
    }

    /**
     * Unloads a <code>JsLib</code> object.
     * 
     * @param name
     *            name of the
     * @param version
     * @throws IOException
     */
    public static void unloadLib(String name, String version) throws IOException {
        if (jsLibs == null) {
            return;
        }
        synchronized (lockObj) {
            HashMap<Version, JsLib> v = jsLibs.get(name);
            if (v != null) {
                JsLib lib = v.remove(new Version(version));
                if (lib != null) {
                    if (lib.inChannel != null) {
                        lib.inChannel.close();
                        lib.dumpFile.delete();
                    }
                    // free file cache for the GC
                    lib.fileCache = null;
                    if (v.isEmpty()) {
                        jsLibs.remove(name);
                    }
                }
                ArchiveClassLoader.clearCache(name, version);
            }
        }
    }

    public static void addLib(String name, String version, JsLib lib) {
        synchronized (lockObj) {
            HashMap<Version, JsLib> v = jsLibs.get(name);
            if (v == null) {
                v = new HashMap<Version, JsLib>();
                jsLibs.put(name, v);
            }
            v.put(new Version(version), lib);
        }
    }

    /**
     * Loads an external JavaScript library.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param name
     *            library name
     * @param version
     *            optional library version. If this parameter is null the
     *            library with the highest version number will be loaded
     * @return {@code JsLib} object
     * @throws Exception
     */
    @SuppressWarnings("resource")
    public static synchronized JsLib load(DAOiface dao, String name, String version) throws Exception {
        initJsLib(dao);
        if (jsLibs == null) {
            return null;
        }
        JsLib lib = null;
        synchronized (lockObj) {
            HashMap<Version, JsLib> v = jsLibs.get(name);
            if (v == null) {
                return null;
            }

            if (version != null) {
                lib = v.get(new Version(version));
            } else {
                Version[] array = v.keySet().toArray(new Version[v.size()]);
                Arrays.sort(array);
                // return the library with the highest version number
                lib = v.get(array[array.length - 1]);
            }
        }
        if (lib != null) {
            if (!lib.isExtracted) {
                // load binary archive data
                lib.callbackAfterLoad(dao);
                // detect zip of 7z archive
                MimeType mtype = ContentInfo.getContentInfo(lib.data);
                int totalSize = 0;
                HashMap<String, byte[]> dataMap = null;
                String libName = "jsLib" + lib.getHistId();
                String classPath = lib.getName() + "/java/classes/";
                if (mtype != null) {
                    // ZIP archive
                    if (mtype.equals(MimeType.ZIP)) {
                        dataMap = new HashMap<String, byte[]>();
                        lib.zipInput = new ZipArchiveInputStream(new ByteArrayInputStream(lib.data));
                        do {
                            ZipArchiveEntry entry = lib.zipInput.getNextZipEntry();
                            if (entry == null) {
                                break;
                            }
                            if (entry.isDirectory()) {
                                continue;
                            }
                            int size = (int) entry.getSize();
                            totalSize += size;
                            byte[] data = new byte[size];
                            lib.zipInput.read(data, 0, size);
                            dataMap.put(entry.getName(), data);
                            if (entry.getName().contains(classPath)) {
                                lib.javaFiles.add(entry.getName());
                            }
                        } while (true);
                        lib.zipInput.close();
                        lib.isExtracted = true;
                        // 7-zip archive
                    } else if (mtype.equals(MimeType._7Z)) {
                        dataMap = new HashMap<String, byte[]>();
                        File tempFile = FileStorage.getInstance().createTempFile(libName, ".7z");
                        tempFile.deleteOnExit();
                        Stream.writeByteStream(tempFile, lib.data);
                        lib.sevenZFile = new SevenZFile(tempFile);
                        do {
                            SevenZArchiveEntry entry = lib.sevenZFile.getNextEntry();
                            if (entry == null) {
                                break;
                            }
                            if (entry.isDirectory()) {
                                continue;
                            }
                            int size = (int) entry.getSize();
                            totalSize += size;
                            byte[] data = new byte[size];
                            lib.sevenZFile.read(data, 0, size);
                            dataMap.put(entry.getName(), data);
                            if (entry.getName().contains(classPath)) {
                                lib.javaFiles.add(entry.getName());
                            }

                        } while (true);
                        lib.sevenZFile.close();
                        lib.isExtracted = true;
                    }
                }
                if (!lib.isExtracted) {
                    throw new Exception("JsLib.load(): No JavaScript archive extracted!");
                }
                // create a buffer for the archive
                byte[] buf = new byte[totalSize];
                int offset = 0;
                // enumerate the archive entries
                for (String n : dataMap.keySet()) {
                    byte[] d = dataMap.get(n);
                    System.arraycopy(d, 0, buf, offset, d.length);
                    lib.archiveMap.put(n, new ArchiveEntry(offset, d.length));
                    offset += d.length;
                }
                // create a temporary file containing the extracted archive
                File tempFile = FileStorage.getInstance().createTempFile(libName, ".dump");
                lib.dumpFile = tempFile;
                tempFile.deleteOnExit();
                Stream.writeByteStream(tempFile, buf);
                FileInputStream inFile = new FileInputStream(tempFile);
                // closed by the GC
                lib.inChannel = inFile.getChannel();
                // discard the archive data - free the memory
                lib.data = null;
                dataMap = null;
            }
        }
        return lib;
    }

    @Override
    public int compareTo(Object lib) {
        return getName().compareTo(((JsLib) lib).getName());
    }

}
