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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZMethod;
import org.apache.commons.compress.archivers.sevenz.SevenZMethodConfiguration;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;

import com.google.gson.Gson;

import com.thoughtworks.xstream.XStream;

import at.treedb.backup.DBexportInfo.BACKUP_TYPE;
import at.treedb.backup.Pseudonym.Gender;
import at.treedb.ci.CIblob;
import at.treedb.ci.MimeType;
import at.treedb.db.ClassID;
import at.treedb.db.DAO;
import at.treedb.db.DAOiface;
import at.treedb.db.DBentities;
import at.treedb.db.DBinfo;
import at.treedb.db.DBkey;
import at.treedb.db.Detach;
import at.treedb.db.Iterator;
import at.treedb.db.UpdateMap;
import at.treedb.db.HistorizationIface.STATUS;
import at.treedb.db.hibernate.DAOhibernate;
import at.treedb.dbfs.DBFSblock;
import at.treedb.dbfs.DBfile;
import at.treedb.dbfs.DBinputStream;
import at.treedb.domain.Domain;
import at.treedb.user.User;
import at.treedb.util.ContentInfo;

/**
 * <p>
 * Class to export a complete database or a domain to the DB and persistence
 * layer independent TDEF-format. </br>
 * TDEF = <b>T</b>ree <b>D</b>B <b>E</b>xchange <b>F</b>ormat
 * </p>
 * 
 * @author Peter Sauer
 * 
 */
public class Export {
    // internal buffer size for writing binary data
    final static public int BUFFER_SIZE = 4 * 1024 * 1024;

    public static String ROOT_DIR = "treeDB/";
    public static String FILES_DIR = "treeDB/_files/";

    private String path;
    private Serialization serialization;
    private boolean historicData;
    private SevenZMethod compressionMethod;
    private SevenZOutputFile sevenZOutput;
    private File archive;
    private Date date;
    private HashSet<Integer> userIDs;
    private STATUS status;
    private DBexportInfo dbInfo;
    private int entityFetchThreshold = 256;
    private DAOiface dao;
    private Gson gson = null;
    private XStream xstream = null;;

    private static HashSet<Class<?>> exclude;

    /**
     * Constructor
     * 
     * @param path
     *            path of the archive
     * @param serialization
     *            serialization method
     * @param compressionMethod
     *            compression method
     * @param historicData
     *            {@code true} if export includes historic data, {@code false}
     *            if not
     */
    public Export(String path, Serialization serialization, SevenZMethod compressionMethod, boolean historicData) {
        this.path = path;
        this.serialization = serialization;
        this.compressionMethod = compressionMethod;
        this.historicData = historicData;
        if (historicData) {
            status = STATUS.ACTIVE;
        } else {
            status = null;
        }
    }

    /**
     * Creates a 7z file archive entry.
     * 
     * @param path
     *            archive path
     * @param date
     *            file creation date
     * @return {@code SevenZArchiveEntry}
     */
    private SevenZArchiveEntry createFileEntry(String path, Date date) {
        SevenZArchiveEntry entry = new SevenZArchiveEntry();
        entry.setName(path);
        entry.setAccessDate(date);
        entry.setCreationDate(date);
        entry.setLastModifiedDate(date);
        entry.setDirectory(false);
        return entry;
    }

    /**
     * Creates a 7z directory archive entry.
     * 
     * @param directory
     *            archive directory path
     * @param date
     *            directory creation date
     * @throws IOException
     */
    private void createDirEntry(String directory, Date date) throws IOException {
        SevenZArchiveEntry entry = new SevenZArchiveEntry();
        entry.setName(directory);
        entry.setAccessDate(date);
        entry.setCreationDate(date);
        entry.setLastModifiedDate(date);
        entry.setDirectory(true);
        sevenZOutput.putArchiveEntry(entry);
        sevenZOutput.closeArchiveEntry();
    }

    /**
     * Writes an uncompressed binary archive data file.
     * 
     * @param path
     *            archive path
     * @param data
     *            binary data
     * @param date
     *            file creation date
     * @throws IOException
     */
    private void write(String path, byte data[], Date date) throws IOException {
        SevenZArchiveEntry entry = createFileEntry(path, date);
        entry.setContentMethods(Arrays.asList(new SevenZMethodConfiguration(SevenZMethod.COPY)));
        sevenZOutput.putArchiveEntry(entry);
        System.out.println(path);
        sevenZOutput.write(data);
        sevenZOutput.closeArchiveEntry();
    }

    /**
     * Writes a text archive file.
     * 
     * @param path
     *            archive path
     * @param data
     *            text fata
     * @param date
     *            file creation date
     * @throws IOException
     */
    private void write(String path, String text, Date date) throws IOException {
        SevenZArchiveEntry entry = createFileEntry(path, date);
        sevenZOutput.putArchiveEntry(entry);
        sevenZOutput.write(text.getBytes("UTF8"));
        sevenZOutput.closeArchiveEntry();
    }

    /**
     * <p>
     * Tries to find the best compression method for the file data. Actual the
     * code only sets for compressed binary formats (e.g. jpeg) the compression
     * method to COPY.
     * </p>
     *
     * 
     * @param data
     *            binary data
     * @return compression method
     */
    public static SevenZMethod findBestCompressionMethod(byte[] data, SevenZMethod compressionMethod) {
        try {
            MimeType mtype = ContentInfo.getContentInfo(data);
            if (mtype != null) {
                switch (mtype) {
                case JPG:
                case PNG:
                case MP4:
                case WEBM:
                case FLV:
                    return SevenZMethod.COPY;
                }
            }
        } catch (Exception e) {
            return compressionMethod;
        }
        return compressionMethod;
    }

    /**
     * Writes the {@code DBFSblock} objects of a {@code DBfile} to an archive
     * file.
     * 
     * @param directory
     *            archive directory
     * @param file
     *            {@code DBfile}
     * @param date
     *            file creation date
     * @throws IOException
     */
    private void write(String directory, DBfile file, Date date) throws IOException {
        SevenZArchiveEntry entry = createFileEntry(directory + file.getHistId(), date);
        sevenZOutput.putArchiveEntry(entry);
        DBinputStream is = new DBinputStream(file, dao);
        long size = file.getSize();
        byte[] buffer = new byte[BUFFER_SIZE];
        boolean setCompressMethod = false;
        while (size > 0) {
            long read = Math.min(buffer.length, size);
            is.read(buffer, 0, (int) read);
            if (!setCompressMethod) {
                setCompressMethod = true;
                entry.setContentMethods(Arrays
                        .asList(new SevenZMethodConfiguration(findBestCompressionMethod(buffer, compressionMethod))));
            }
            sevenZOutput.write(buffer, 0, (int) read);
            size -= read;
        }
        is.close();
        sevenZOutput.closeArchiveEntry();

    }

    /**
     * Creates an archive path for binary data.
     * 
     * @param index
     *            internal index of the binary field
     * @param export
     *            export interface
     * @return archive path
     */
    public static String createBinaryPath(int index, ExportIface export) {
        String indexStr = "";
        if (index > 0) {
            indexStr = "_" + index;
        }
        return FILES_DIR + export.getCID() + "_" + export.getDBid() + indexStr;
    }

    /**
     * Creates an archive path for binary data.
     * 
     * @param index
     *            internal index of the binary field
     * @param cid
     *            entity class ID
     * @param dbID
     *            entity DB ID
     * @return archive path
     */
    public static String createBinaryPath(int index, ClassID cid, int dbID) {
        String indexStr = "";
        if (index > 0) {
            indexStr = "_" + index;
        }
        return FILES_DIR + cid + "_" + dbID + indexStr;
    }

    /**
     * Detaches the binary data for every entity of a list.
     * 
     * @param clazz
     *            class of the entity
     * @param list
     *            list of entities
     * @return list of duplicated entities with removed binary data, or the
     *         original list, if removing binary data is not necessary.
     * @throws Exception
     */
    private List<Object> detachBinaryData(DAOiface dao, Class<?> clazz, List<Object> list) throws Exception {
        // handling for virtual files (data base files)
        if (clazz.equals(DBfile.class)) {
            for (Object o : list) {
                DBfile f = (DBfile) o;
                write(FILES_DIR, f, date);
            }
        } else {
            HashSet<Field> detachList = new HashSet<Field>();
            for (Field f : clazz.getDeclaredFields()) {
                if (f.getAnnotation(Detach.class) != null) {
                    f.setAccessible(true);
                    detachList.add(f);
                }
            }
            // detach binary data
            if (!detachList.isEmpty()) {
                ArrayList<Object> l = new ArrayList<Object>();
                for (Object o : list) {
                    // clone the original data!
                    ExportIface export = (ExportIface) ((ExportIface) o).clone();
                    if (dao.isJPA()) {
                        dao.detach(o);
                    }
                    // enumerate binary fields
                    for (Field f : detachList) {
                        // read optional index - necessary if more than one
                        // binary data element should be detached
                        int index = f.getAnnotation(Detach.class).index();
                        if ((byte[]) f.get(export) != null) {
                            write(createBinaryPath(index, export), (byte[]) f.get(export), date);
                        }
                        f.set(export, null);
                    }

                    l.add(export);
                }
                return l;
            }
        }
        return list;
    }

    /**
     * Collect all {@code DBkey} annotation with a reference to {@code User}.
     * 
     * @param clazz
     *            entity to be checked for {@code User} references
     * @param fieldList
     *            list of {@Field} objects
     * @return
     */
    private boolean collectUserFields(Class<?> clazz, ArrayList<Field> fieldList) {
        boolean userFields = false;
        for (Field f : clazz.getDeclaredFields()) {
            if (f.getAnnotation(DBkey.class) != null) {
                f.setAccessible(true);
                Class<?> c = f.getAnnotation(DBkey.class).value();
                if (c.equals(User.class)) {
                    fieldList.add(f);
                    userFields = true;
                }
            }
        }
        return userFields;
    }

    /**
     * Dumps all entities of the database, or just all entities of a domain to
     * the archive.
     * 
     * @param clazz
     *            entity to be dumped
     * @param domain
     *            optional domain, or {@code null} for dumping all entities of
     *            the database to the archive
     * @param backupType
     *            type of the backup
     * @throws Exception
     */
    private void dumpClass(DBexportInfo dbInfo, Class<?> clazz, Domain domain, DBexportInfo.BACKUP_TYPE backupType)
            throws Exception {
        String archivePath = ROOT_DIR + clazz.getSimpleName() + "/";
        createDirEntry(archivePath, date);

        Iterator iter = new Iterator(dao, clazz, domain, status, entityFetchThreshold);
        int blockCounter = 0;
        switch (serialization) {
        case JSON:
            gson = new Gson();
            break;
        case XML:
            xstream = new XStream();
            break;
        default:
            break;
        }
        dbInfo.addEntityCount(clazz.getSimpleName() + ":" + iter.getEntitiesNum());
        ArrayList<Field> fieldList = new ArrayList<Field>();
        boolean userFields = false;
        if (backupType == DBexportInfo.BACKUP_TYPE.DOMAIN) {
            userFields = collectUserFields(clazz, fieldList);
            if (clazz.getSuperclass() != null) {
                collectUserFields(clazz.getSuperclass(), fieldList);
            }
        }
        if (fieldList.size() > 0) {
            userFields = true;
        }

        boolean singleStep = false;
        if (clazz.equals(CIblob.class)) {
            singleStep = true;
        }

        while (iter.hasNext()) {
            List<Object> l = null;
            if (!singleStep) {
                l = iter.next();
                l = detachBinaryData(dao, clazz, l);
            } else {
                ArrayList<Object> list = new ArrayList<Object>();
                for (int i = 0; i < entityFetchThreshold; ++i) {
                    l = iter.nextObject();
                    if (l == null) {
                        break;
                    }
                    l = detachBinaryData(dao, clazz, l);
                    list.add(l.get(0));
                }
                if (list.size() == 0) {
                    continue;
                }
                l = list;
            }

            SevenZArchiveEntry entry = new SevenZArchiveEntry();
            entry = new SevenZArchiveEntry();
            entry.setName(archivePath + blockCounter++);
            entry.setAccessDate(date);
            entry.setCreationDate(date);
            entry.setLastModifiedDate(date);
            entry.setDirectory(false);
            sevenZOutput.putArchiveEntry(entry);
            byte[] byteStream = null;
            switch (serialization) {
            case JSON:
                byteStream = gson.toJson(l).getBytes();
                break;
            case XML:
                byteStream = xstream.toXML(l).getBytes();
                break;
            case BINARY:
                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(bs);
                out.writeObject(l);
                out.close();
                byteStream = bs.toByteArray();
                break;
            }
            // collect all user references
            if (userFields) {
                for (Object o : l) {
                    for (Field f : fieldList) {
                        f.setAccessible(true);
                        userIDs.add(f.getInt(o));
                    }
                }
            }
            sevenZOutput.write(byteStream);
            sevenZOutput.closeArchiveEntry();
            if (l instanceof CIblob) {
                if (dao.isJPA() && dao.getJPAimpl() == DAO.JPA_IMPL.ECLIPSELINK) {
                    dao.clear();
                } else {
                    dao.detach(l);
                }
                ((CIblob) l).resetBlob();
            }
            l = null;

        }
        iter.close();
    }

    /**
     * Dumps all users to the archive.
     * 
     * @param dbInfo
     *            database information
     * @param privacy
     *            privacy export filter
     * @throws Exception
     */
    private void dumpUser(DBexportInfo dbInfo, EnumSet<User.PRIVACY> privacy) throws Exception {
        String archivePath = ROOT_DIR + User.class.getSimpleName() + "/";
        createDirEntry(archivePath, date);
        Iterator iter = new Iterator(dao, User.class, null, null, entityFetchThreshold);
        int blockCounter = 0;
        switch (serialization) {
        case JSON:
            gson = new Gson();
            break;
        case XML:
            xstream = new XStream();
            break;
        default:
            break;
        }
        int counter = 0;

        while (iter.hasNext()) {
            List<Object> l = iter.next();
            ArrayList<Object> newList = new ArrayList<Object>();
            for (Object o : l) {
                User u = (User) o;
                // user must be member of the domain and ACTIVE or deleted
                if (userIDs.contains(u.getDBid())
                        && (u.getHistStatus() == STATUS.ACTIVE || u.getHistStatus() == STATUS.DELETED)) {
                    User clone = (User) u.clone();
                    UpdateMap map = new UpdateMap(User.Fields.class);
                    map.addString(User.Fields.password, null);
                    // apply privacy filter
                    if (privacy == null || !privacy.contains(User.PRIVACY.NAME)) {
                        Pseudonym fName = Pseudonym.generatePseudonym(Gender.RANDOM);
                        map.addString(User.Fields.firstName, fName.getFirstName());
                        map.addString(User.Fields.lastName, fName.getLastName());
                        map.addString(User.Fields.displayName, fName.getFirstName() + " " + fName.getLastName());
                        map.addString(User.Fields.nickName, null);
                        map.addString(User.Fields.lastName, null);
                        map.addString(User.Fields.email, fName.getEmail());
                    }
                    if (privacy == null || !privacy.contains(User.PRIVACY.PHONE)) {
                        map.addString(User.Fields.phone, null);
                    }
                    if (privacy == null || !privacy.contains(User.PRIVACY.MOBILE)) {
                        map.addString(User.Fields.mobile, null);
                    }
                    if (privacy == null || !privacy.contains(User.PRIVACY.USERID)) {
                        map.addString(User.Fields.userId, null);
                    }
                    clone.simpleUpdate(map, true);
                    // exported user is VIRTUAL
                    clone.setHistStatus(STATUS.VIRTUAL);
                    clone.setCreatedBy(0);
                    clone.setModifiedBy(0);
                    newList.add(clone);
                    ++counter;
                }
            }
            l = newList;
            SevenZArchiveEntry entry = new SevenZArchiveEntry();
            entry = new SevenZArchiveEntry();
            entry.setName(archivePath + blockCounter++);
            entry.setAccessDate(date);
            entry.setCreationDate(date);
            entry.setLastModifiedDate(date);
            entry.setDirectory(false);
            sevenZOutput.putArchiveEntry(entry);
            byte[] byteStream = null;
            switch (serialization) {
            case JSON:
                byteStream = gson.toJson(l).getBytes();
                break;
            case XML:
                byteStream = xstream.toXML(l).getBytes();
                break;
            case BINARY:
                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(bs);
                out.writeObject(l);
                out.close();
                byteStream = bs.toByteArray();
                break;
            }
            sevenZOutput.write(byteStream);
            sevenZOutput.closeArchiveEntry();
        }
        dbInfo.addEntityCount(User.class.getSimpleName() + ":" + counter);
    }

    private void initBackup(BACKUP_TYPE type, Domain d) throws Exception {
        dao = DAO.getDAO();
        archive = new File(path);
        sevenZOutput = new SevenZOutputFile(archive);
        sevenZOutput.setContentCompression(compressionMethod);
        dbInfo = new DBexportInfo(type, d, serialization, compressionMethod);
        date = new Date(System.currentTimeMillis());
        createDirEntry(ROOT_DIR, date);
        createDirEntry(FILES_DIR, date);
    }

    /**
     * Ignore abstract and some special class for default dump.
     * 
     * @param clazz
     * @return {@code
     */
    private static boolean isIgnoreClass(Class<?> clazz) {
        if (Modifier.isAbstract(clazz.getModifiers()) || clazz.equals(DBFSblock.class) || clazz.equals(DBinfo.class)) {
            return true;
        }
        return false;
    }

    /**
     * Full backup of the complete database.
     * 
     * @throws Exception
     */
    public void backup() throws Exception {
        initBackup(BACKUP_TYPE.FULL, null);
        try {
            dbInfo.setStartTime();
            if (dao.isHibernate()) {
                ((DAOhibernate) dao).beginStatelessTransaction();
            } else {
                dao.beginTransaction();
            }
            for (Class<?> c : DBentities.getClasses()) {
                if (isIgnoreClass(c)) {
                    continue;
                }
                System.out.println(c.getCanonicalName());
                dumpClass(dbInfo, c, null, BACKUP_TYPE.FULL);
            }
            dao.endTransaction();

            if (xstream == null) {
                xstream = new XStream();
            }
            dbInfo.setEndTime();
            write("dbinfo.xml", xstream.toXML(dbInfo), date);
        } catch (Exception e) {
            dao.rollback();
            sevenZOutput.close();
            archive.delete();
            throw e;
        }
        sevenZOutput.close();
    }

    /**
     * Backs up a domain.
     * 
     * @param domain
     *            domain to be backed up.
     * @param privacy
     *            privacy export filter
     * @throws Exception
     */
    public void backup(Domain domain, EnumSet<User.PRIVACY> privacy) throws Exception {
        userIDs = new HashSet<Integer>();
        initBackup(BACKUP_TYPE.DOMAIN, domain);
        try {
            dbInfo.setStartTime();
            if (dao.isHibernate()) {
                ((DAOhibernate) dao).beginStatelessTransaction();
            } else {
                dao.beginTransaction();
            }
            for (Class<?> c : DBentities.getClasses()) {
                if (isIgnoreClass(c) || exclude.contains(c)) {
                    continue;
                }
                dumpClass(dbInfo, c, domain, BACKUP_TYPE.DOMAIN);
            }
            dumpUser(dbInfo, privacy);
            dao.endTransaction();

            if (xstream == null) {
                xstream = new XStream();
            }
            dbInfo.setEndTime();
            write("dbinfo.xml", xstream.toXML(dbInfo), date);

        } catch (Exception e) {
            dao.rollback();
            sevenZOutput.close();
            archive.delete();
            throw e;
        }
        sevenZOutput.close();
    }

    static {
        exclude = new HashSet<Class<?>>();
        for (Class<?> c : new Class<?>[] { DBinfo.class, User.class }) {
            exclude.add(c);
        }
    }

}
