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
package at.treedb.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

import at.treedb.backup.DomainInstallationIface;
import at.treedb.domain.InstallationProgress;

/**
 * <p>
 * Helper class to acccess an 7-zip archive.
 * </p>
 * 
 * @author Peter Sauer
 *
 */
public class SevenZip extends ClassLoader {
    // size of the read buffer
    private static final int READ_BUFFER = 4 * 1024 * 1024;
    private HashMap<String, ArchiveEntry> entryMap;
    private SevenZFile sevenZFile;
    private File dumpFile; // dump file containing the extracted archive data
    private FileChannel channel;
    private FileInputStream dumpReader;
    private String classPath;

    /**
     * Constructor
     * 
     * @param file
     *            7-zip archive
     * @throws IOException
     */
    public SevenZip(File file) throws Exception {
        this(file, null);
    }

    /**
     * Constructor
     * 
     * @param file
     *            7-zip archive
     * @throws IOException
     */
    public SevenZip(String file) throws Exception {
        this(new File(file), null);
    }

    /**
     * Constructor
     * 
     * @param file
     *            7-zip archive file
     * @param iface
     *            interface to display the extraction progress
     * @throws IOException
     */
    public SevenZip(File file, DomainInstallationIface iface) throws Exception {
        sevenZFile = new SevenZFile(file);
        entryMap = new HashMap<String, ArchiveEntry>();
        // create a temporary file containing the extracted archive
        dumpFile = FileStorage.getInstance().createTempFile("sevenZip", ".dump");
        FileOutputStream writer = new FileOutputStream(dumpFile);
        byte[] readBuffer = new byte[READ_BUFFER];
        int offset = 0;
        if (iface != null) {
            iface.writeInternalMessage(InstallationProgress.Type.MSG,
                    "Extracting installation package: '.' stands for " + READ_BUFFER + " MB data");
            iface.writeInternalMessage(null, "<br>");
        }
        int count = 0;
        int bytesWritten = 0;
        // extract and dump the files
        do {
            SevenZArchiveEntry entry = sevenZFile.getNextEntry();
            if (entry == null) {
                break;
            }
            if (!entry.isDirectory()) {
                int readBytes = (int) entry.getSize();
                int size = readBytes;
                int index = 0;
                while (readBytes > 0) {
                    int read = Math.min(readBytes, READ_BUFFER);
                    bytesWritten += read;
                    if (iface != null) {
                        iface.writeInternalMessage(null, "<b>.</b>");
                        ++count;
                        if (count == 80) {
                            iface.writeInternalMessage(null, "<br>");
                            count = 0;
                        }
                    }
                    sevenZFile.read(readBuffer, index, read);
                    writer.write(readBuffer, 0, read);
                    readBytes -= read;
                }
                entryMap.put(entry.getName().replace('\\', '/'), new ArchiveEntry(offset, size));
                offset += size;
            }
        } while (true);
        if (iface != null) {
            iface.writeInternalMessage(null, "<br>");
            iface.writeInternalMessage(InstallationProgress.Type.MSG,
                    "Extraction finished: " + bytesWritten + " bytes written");
        }
        writer.close();
        dumpReader = new FileInputStream(dumpFile);
        channel = dumpReader.getChannel();
    }

    /**
     * Extracts some data (files and directories) from the archive without
     * extracting the whole archive.
     * 
     * @param archive
     *            7-zip archive
     * @param fileList
     *            file extraction list, a path with an ending '/' denotes a
     *            directory
     * @return file list as a map file name/file data
     * @throws IOException
     */
    public static HashMap<String, byte[]> exctact(File archive, String... fileList) throws IOException {
        HashSet<String> fileSet = new HashSet<String>();
        ArrayList<String> dirList = new ArrayList<String>();
        for (String f : fileList) {
            if (!f.endsWith("/")) {
                fileSet.add(f);
            } else {
                dirList.add(f);
            }
        }
        HashMap<String, byte[]> resultMap = new HashMap<String, byte[]>();
        SevenZFile sevenZFile = new SevenZFile(archive);
        do {
            SevenZArchiveEntry entry = sevenZFile.getNextEntry();
            if (entry == null) {
                break;
            }
            // convert window path to unix style
            String name = entry.getName().replace('\\', '/');
            if (!entry.isDirectory()) {
                boolean storeFile = false;
                if (fileSet.contains(name)) {
                    storeFile = true;
                } else {
                    // search directories
                    for (String s : dirList) {
                        if (name.startsWith(s)) {
                            storeFile = true;
                            break;
                        }
                    }
                }
                // store the file
                if (storeFile) {
                    int size = (int) entry.getSize();
                    byte[] data = new byte[size];
                    sevenZFile.read(data, 0, size);
                    resultMap.put(name, data);
                    // in this case we can finish the extraction loop
                    if (dirList.isEmpty() && resultMap.size() == fileSet.size()) {
                        break;
                    }
                }
            }
        } while (true);
        sevenZFile.close();
        return resultMap;
    }

    /**
     * Checks if a file exists.
     * 
     * @param path
     *            file path
     * @return {@code true} if the file exists, {@code false} if not
     */
    public boolean exists(String path) {
        ArchiveEntry entry = entryMap.get(path);
        if (entry == null) {
            return false;
        }
        return true;
    }

    /**
     * Checks if a directory exists.
     * 
     * @param path
     *            directory path
     * @return {@code true} if the directory exists, {@code false} if not
     */
    public boolean searchDir(String path) {
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        for (String p : entryMap.keySet()) {
            if (p.startsWith(path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a the files of a directory including all subdirectories.
     * 
     * @param directory
     * @return file list
     */
    public String[] list(String directory) {
        ArrayList<String> list = new ArrayList<String>();
        if (!directory.endsWith("/")) {
            directory = directory + "/";
        }
        for (String p : entryMap.keySet()) {
            if (p.startsWith(directory)) {
                list.add(p);
            }
        }
        if (list.size() == 0) {
            return null;
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * Returns the data of an archive entry
     * 
     * @param path
     *            archive entry path
     * @return binary archive data
     * @throws Exception
     */
    public byte[] getData(String path) throws Exception {
        ArchiveEntry entry = entryMap.get(path);
        if (entry == null) {
            throw new Exception("SevenZipAccess.getData(): Path not found: " + path);
        }
        ByteBuffer buf = ByteBuffer.allocate(entry.getLength());
        channel.read(buf, entry.getOffset());
        return buf.array();
    }

    /**
     * Returns an object to access an entry of the archive inside the dump file.
     * This method is used for large (binary) objects.
     * 
     * @param path
     *            archive path
     * @return object to directly access an archive entry.
     * @throws Exception
     */
    public ArchiveFileAccess getArchiveAccessEntry(String path) throws Exception {
        ArchiveEntry entry = entryMap.get(path);
        if (entry == null) {
            throw new Exception("SevenZipAccess.getData(): Path not found: " + path);
        }
        return new ArchiveFileAccess(entry.getOffset(), entry.getLength(), channel);
    }

    /**
     * Closes the archive file.
     * 
     * @throws IOException
     */
    public void close() throws IOException {
        if (sevenZFile != null) {
            sevenZFile.close();
        }
        // close the file channel
        if (dumpReader != null) {
            dumpReader.close();
        }
        // delete the dump file
        if (dumpFile != null) {
            dumpFile.delete();
        }
    }

    /**
     * Set the directory path which contains the Java .class files.
     * 
     * @param classPath
     *            directory path
     */
    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    /**
     * Retruns the directory path which contains the Java .class files.
     * 
     * @return class path (=directory path)
     */
    public String getClassPath() {
        return classPath;
    }

    /**
     * Modified class cloader to load Java classes from the archive.
     */
    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        byte clazzData[] = null;
        try {
            String path = classPath + name.replace(".", "/") + ".class";
            clazzData = getData(path);
            if (clazzData != null) {
                return defineClass(name, clazzData, 0, clazzData.length);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ClassNotFoundException();
        }
        return Class.forName(name);
    }

    public static void main(String args[]) throws IOException {
        System.out.println("Extract");
        HashMap<String, byte[]> map = exctact(new File("c:/TreeDBdata/domains/ZooDB.7z"), "info.xml", "classes/");
        for (String s : map.keySet()) {
            if (s.startsWith("classes/")) {
                String className = s.substring("classes/".length(), s.lastIndexOf(".class")).replace("/", ".");
                System.out.println(className);
            }
        }
    }
}
