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
package at.treedb.backup;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

import at.treedb.ci.CI;
import at.treedb.ci.CIblob;
import at.treedb.ci.CIfile;
import at.treedb.db.Base;
import at.treedb.db.ClassSelector;
import at.treedb.db.DAO;
import at.treedb.db.DAOiface;
import at.treedb.db.DBentities;
import at.treedb.db.DBinfo;
import at.treedb.db.DBkey;
import at.treedb.db.Detach;
import at.treedb.db.Iterator;
import at.treedb.dbfs.DBFSblock;
import at.treedb.dbfs.DBfile;
import at.treedb.dbfs.DBoutputStream;
import at.treedb.ui.UIelement;

import com.google.gson.Gson;
import com.thoughtworks.xstream.XStream;

/**
 * <p>
 * Class to import a database or a domain from the DB and persistence layer
 * independent TDEF-format. </br>
 * TDEF - Tree DB Exchange Format
 * </p>
 * <p>
 * Used libraries:
 * <ul>
 * <li>http://commons.apache.org/proper/commons-compress/ for base compressed
 * methods</li>
 * <li>http://tukaani.org/xz/java.html for</li>
 * </ul>
 * <p>
 * 
 * @author Peter Sauer
 * 
 */
public class Import {
    private String archivePath;
    private HashMap<String, SevenZArchiveEntry> archiveMap;
    private SevenZFile sevenZFile;
    private String archiveInfo;
    private Serialization serialization;
    private Gson gson = null;
    private XStream xstream = null;
    private int entityFetchThreshold = 1000;
    private Field dBFilePathField;
    // hash maps for set the new DB IDs
    private HashMap<Class<?>, HashMap<Integer, Integer>> classIdMap;
    private HashMap<Class<?>, HashMap<Integer, Integer>> detachIdMap;
    private HashMap<Class<?>, HashMap<Integer, Integer>> historicIdMap;
    private HashMap<Integer, Integer> fileIdMap;
    private HashSet<Integer> ciFileHashSet;

    /**
     * Constructor
     * 
     * @param path
     *            archive path
     */
    public Import(String path) {
        this.archivePath = path;
        classIdMap = new HashMap<Class<?>, HashMap<Integer, Integer>>();
        detachIdMap = new HashMap<Class<?>, HashMap<Integer, Integer>>();
        historicIdMap = new HashMap<Class<?>, HashMap<Integer, Integer>>();
        fileIdMap = new HashMap<Integer, Integer>();
        ciFileHashSet = new HashSet<Integer>();
        // we have no setter for this field, do it with reflection
        for (Field f : DBfile.class.getDeclaredFields()) {
            if (f.getName().equals("filePath")) {
                f.setAccessible(true);
                dBFilePathField = f;
                break;
            }
        }
    }

    /**
     * Read all archive entries.
     * 
     * @return archive entries
     * @throws IOException
     */
    private HashMap<String, SevenZArchiveEntry> readEntries() throws IOException {
        HashMap<String, SevenZArchiveEntry> archiveMap = new HashMap<String, SevenZArchiveEntry>();
        sevenZFile = new SevenZFile(new File(archivePath));
        while (true) {
            SevenZArchiveEntry entry = sevenZFile.getNextEntry();
            if (entry == null) {
                break;
            }
            if (entry.isDirectory()) {
                continue;
            }
            archiveMap.put(entry.getName(), entry);
        }
        sevenZFile.close();
        return archiveMap;
    }

    /**
     * Reads a archive entry.
     * 
     * @param path
     *            archive path
     * @return archive entry data
     * @throws IOException
     */
    private byte[] readData(String path) throws IOException {
        if (!archiveMap.containsKey(path)) {
            return null;
        }
        byte[] content = null;
        sevenZFile = new SevenZFile(new File(archivePath));
        while (true) {
            SevenZArchiveEntry entry = sevenZFile.getNextEntry();
            if (entry == null) {
                break;
            }
            if (entry.isDirectory()) {
                continue;
            }
            if (entry.getName().equals(path)) {
                int size = (int) entry.getSize();
                content = new byte[size];
                int index = 0;
                while (size > 0) {
                    int read = sevenZFile.read(content, index, size);
                    size -= read;
                    index += read;
                }
                break;
            }
        }
        sevenZFile.close();
        return content;
    }

    private static boolean ignoreClass(Class<?> c) {
        if (Modifier.isAbstract(c.getModifiers()) || c.equals(DBFSblock.class) || c.equals(DBinfo.class)) {
            return true;
        }
        return false;
    }

    /**
     * Full restore of a database
     * 
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public void fullRestore() throws Exception {
        archiveMap = readEntries();
        archiveInfo = new String(readData("dbinfo.xml"), "UTF8");
        xstream = new XStream();
        DBexportInfo dbInfo = (DBexportInfo) xstream.fromXML(archiveInfo);
        serialization = dbInfo.getSerialization();
        switch (serialization) {
        case JSON:
            gson = new Gson();
            break;
        case XML:
            if (xstream == null) {
                xstream = new XStream();
            }
            break;
        default:
            break;
        }

        DAOiface dao = DAO.getDAO();
        // dummy array for binary data
        byte[] dummyArray = new byte[1];
        // first loop - restore data without detached data
        for (Class<?> c : DBentities.getClasses()) {
            if (ignoreClass(c)) {
                continue;
            }
            HashMap<Integer, Integer> idMap = new HashMap<Integer, Integer>();
            classIdMap.put(c, idMap);
            HashMap<Integer, Integer> detachMap = new HashMap<Integer, Integer>();
            detachIdMap.put(c, detachMap);
            HashMap<Integer, Integer> historicMap = new HashMap<Integer, Integer>();
            historicIdMap.put(c, historicMap);
            int dataBlockCounter = 0;
            while (true) {
                String path = "treeDB/" + c.getSimpleName() + "/" + dataBlockCounter;
                if (!archiveMap.containsKey(path)) {
                    break; // not entities available of this class
                }
                byte[] data = readData(path);
                // detached binary data available?
                ArrayList<Field> fieldList = new ArrayList<Field>();
                for (Field f : c.getDeclaredFields()) {
                    if (f.getAnnotation(Detach.class) != null) {
                        f.setAccessible(true);
                        fieldList.add(f);
                    }

                }
                // data de-serialization
                List<Base> list = null;
                switch (serialization) {
                case JSON:
                    ParameterizedTypeImpl pti = new ParameterizedTypeImpl(List.class, new Type[] { c }, null);
                    list = gson.fromJson(new String(data), pti);
                    break;
                case XML:
                    list = (List<Base>) xstream.fromXML(new String(data));
                    break;
                case BINARY:
                    ByteArrayInputStream is = new ByteArrayInputStream(data);
                    ObjectInputStream input = new ObjectInputStream(is);
                    list = (List<Base>) input.readObject();
                    input.close();
                    break;
                default:
                    break;
                }
                // traverse entities
                for (Base b : list) {
                    if (!fieldList.isEmpty()) {
                        for (Field f : fieldList) {
                            // set a dummy binary array to avoid
                            // @Column(nullable = false) conflicts
                            f.set(b, dummyArray);
                        }
                    }
                    int oldDBid = b.getDBid();
                    dao.beginTransaction();
                    Base.restore(dao, b);
                    dao.endTransaction();
                    // store the pair old DB ID / new DB ID
                    idMap.put(oldDBid, b.getDBid());
                    if (historicMap.get(b.getHistId()) == null) {
                        historicMap.put(b.getHistId(), b.getDBid());
                    }
                    if (!fieldList.isEmpty()) {
                        // store the pair new DB ID / old DB ID
                        detachMap.put(b.getDBid(), oldDBid);
                    }
                    if (c.equals(DBfile.class)) {
                        fileIdMap.put(b.getDBid(), oldDBid);
                    }
                }
                ++dataBlockCounter;
            }
        }
        // second loop - adjust DB IDs
        for (Class<?> c : DBentities.getClasses()) {
            if (ignoreClass(c)) {
                continue;
            }
            dao.beginTransaction();
            Iterator iter = new Iterator(dao, c, null, null, entityFetchThreshold);
            adjustFields(dao, c, iter);
            dao.endTransaction();
        }
    }

    private void adjustFields(DAOiface dao, Class<?> c, Iterator iter) throws Exception {
        ArrayList<Field> list = ClassDependency.getAllFields(c);
        ArrayList<Field> dbKeys = new ArrayList<Field>();
        ArrayList<Field> detached = new ArrayList<Field>();
        // System.out.println("adjust class:" + c.getSimpleName() + ":");
        for (Field f : list) {
            f.setAccessible(true);
            if (f.getAnnotation(Detach.class) != null) {
                detached.add(f);
            } else if (f.getAnnotation(DBkey.class) != null) {
                dbKeys.add(f);
            }
        }
        HashMap<Integer, Integer> detachMap = detachIdMap.get(c);
        HashMap<Integer, Integer> historicMap = historicIdMap.get(c);

        while (iter.hasNext()) {
            List<Object> l = iter.next();
            for (Object o : l) {
                Base b = (Base) o;
                for (Field f : dbKeys) {
                    f.setAccessible(true);
                    Class<?> clazz = f.getAnnotation(DBkey.class).value();
                    HashMap<Integer, Integer> idMap;
                    Class<?> sel = null;
                    // ClassSelector necessary for ID re-mapping?
                    if (clazz.equals(ClassSelector.class)) {
                        sel = ((ClassSelector) b).getClass(f);
                        if (sel == null) {
                            continue;
                        }
                        idMap = classIdMap.get(sel);
                    } else {
                        idMap = classIdMap.get(clazz);
                    }

                    if (sel != null && f.getType().equals(Long.TYPE)) {
                        long oldKey = f.getLong(b);
                        if (oldKey > 0) {
                            int id = UIelement.extractHistIdFromComposedId(oldKey);
                            if (idMap.get(id) != null) {
                                long newKey = (oldKey & 0xffffffff00000000L) + idMap.get(id);
                                f.setLong(b, newKey);
                            }
                        }
                    } else {
                        int oldKey = f.getInt(b);
                        if (oldKey > 0) {
                            if (c.getName().contains("CIimage")) {
                                System.out.println(f.getName() + ":" + idMap.get(oldKey));
                            }
                            f.setInt(b, idMap.get(oldKey));
                        }
                    }
                }
                // re-attach detached binary data
                for (Field f : detached) {
                    int index = f.getAnnotation(Detach.class).index();
                    String path = Export.createBinaryPath(index, b.getCID(), detachMap.get(b.getDBid()));
                    f.set(b, readData(path));

                }
                // set new historic ID

                b.setHistId(historicMap.get(b.getHistId()));

                if (c.equals(CIfile.class)) {
                    ciFileHashSet.add(((CIfile) b).getDBfile());
                }

                dao.update(b);
                // re-import DBfile data
                if (c.equals(DBfile.class)) {
                    DBfile file = (DBfile) b;
                    if (ciFileHashSet.contains(file.getHistId())) {
                        // adapt CIfile virtual path:
                        // /files/ciId/uiElementId/fileName
                        String[] split = file.getPath().split("/");
                        split[2] = "" + classIdMap.get(CI.class).get(Integer.parseInt(split[2]));
                        long composed = Long.parseLong(split[3]);
                        int id = UIelement.extractHistIdFromComposedId(composed);
                        HashMap<Integer, Integer> idMap = classIdMap.get(UIelement.getClassIdFromComposedId(composed));
                        split[3] = "" + ((composed & 0xffffffff00000000L) + idMap.get(id));
                        StringBuffer buf = new StringBuffer();
                        for (String s : split) {
                            if (s.equals("")) {
                                continue;
                            }
                            buf.append("/");
                            buf.append(s);
                        }
                        dBFilePathField.set(file, buf.toString());
                    }
                    writeFile(dao, file, Export.FILES_DIR + fileIdMap.get(file.getDBid()));
                }
                dao.flush();
                // try to free memory
                if (b instanceof CIblob) {
                    // for EclipseLink a session clearing necessary! only
                    // detaching isn't working really -
                    // memory consumption is increasing in spite of detaching
                    // objects!
                    if (dao.isJPA() && dao.getJPAimpl() == DAO.JPA_IMPL.ECLIPSELINK) {
                        dao.clear();
                    } else {
                        dao.detach(b);
                    }
                    // clear binary data
                    ((CIblob) b).resetBlob();
                    b = null;
                }
            }
        }
    }

    public void writeFile(DAOiface dao, DBfile file, String path) throws Exception {
        if (!archiveMap.containsKey(path)) {
            throw new Exception("Import.writeFile(): path not found - " + path);
        }
        byte[] content = null;
        sevenZFile = new SevenZFile(new File(archivePath));
        while (true) {
            SevenZArchiveEntry entry = sevenZFile.getNextEntry();
            if (entry == null) {
                break;
            }
            if (entry.isDirectory()) {
                continue;
            }
            if (entry.getName().equals(path)) {
                int size = (int) entry.getSize();
                content = new byte[Math.min(Export.BUFFER_SIZE, size)];
                int read = content.length;
                DBoutputStream os = new DBoutputStream(file, dao);
                while (size > 0) {
                    int readCount = sevenZFile.read(content, 0, read);
                    size -= readCount;
                    read = Math.min(size, read);
                    os.write(content, 0, readCount);
                }
                os.close();
                break;
            }
        }
        sevenZFile.close();
    }

}
