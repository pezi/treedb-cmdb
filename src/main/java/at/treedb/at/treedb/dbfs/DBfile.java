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
package at.treedb.dbfs;

import java.util.HashMap;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;

import at.treedb.db.Base;
import at.treedb.db.ClassID;
import at.treedb.db.DAOiface;
import at.treedb.domain.Domain;
import at.treedb.user.User;

/**
 * <p>
 * Simple DB based file system to store binary data in a DB. To avoid storing
 * large binary data in one DB blob a data unit is divided into blocks. This
 * layer should avoid problems with the combination DB/blobs/InputStream.
 * </p>
 * 
 * @author Peter Sauer
 *
 */
// LDOR: 22.12.2013
@Entity
public class DBfile extends Base {
    private static final long serialVersionUID = 1L;
    // virtual file path - can be any string
    // Hint: the variable name is referenced by at.treedb.backup.Import.java
    private String filePath;
    // file size
    @Column(name = "m_size") // Oracle
    private long size;
    // CRC32
    private long crc32;

    protected DBfile() {

    }

    private DBfile(String filePath) {
        this.setHistStatus(STATUS.ACTIVE);
        this.filePath = filePath;
    }

    /**
     * Creates a {@code DBfile}
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param domain
     *            {@code Domain} of the file
     * @param user
     *            creator of the {@code DBfile}
     * @param path
     *            virtual file path
     * @return {@code DBfile} object
     * @throws Exception
     */
    public static DBfile create(DAOiface dao, Domain domain, User user, String path) throws Exception {
        DBfile dbf = new DBfile(path);
        Base.save(dao, domain, user, dbf);
        return dbf;
    }

    /**
     * Loads a {@code DBfile}.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param domain
     *            domain of the {@code DBfile}
     * @param path
     *            virtual path of the {@code DBfile}
     * @return {@code DBfile} object
     * @throws Exception
     */
    public static DBfile load(DAOiface dao, Domain domain, String path) throws Exception {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("status", at.treedb.db.HistorizationIface.STATUS.ACTIVE);
        map.put("path", path);
        map.put("domain", domain.getHistId());
        @SuppressWarnings("unchecked")
        List<Base> list = (List<Base>) dao.query("select f from " + DBfile.class.getSimpleName()
                + " f where  f.domain = :domain and f.status = :status and f.filePath = :path", map);
        if (list.size() == 1) {
            DBfile file = (DBfile) list.get(0);
            if (!file.checkIntegrity()) {
                throw new Exception("DBfile.load(): DBfile object wasnt'closed correctly");
            }
            return file;
        }
        return null;
    }

    public boolean checkIntegrity() {
        if (size > 0 && crc32 == 0) {
            return false;
        }
        return true;
    }

    /**
     * Deletes a {@code DBfile}.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param domain
     *            domain of the {@code DBfile}
     * @param user
     *            user who deletes the {@code DBfile}.
     * @param path
     *            virtual path of the {@code DBfile}
     * @return {@code true} if the deletion was successful, {@code false} if not
     * @throws Exception
     */
    public static boolean delete(DAOiface dao, Domain domain, User user, String path) throws Exception {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("status", at.treedb.db.HistorizationIface.STATUS.ACTIVE);
        map.put("path", path);
        map.put("domain", domain.getHistId());
        @SuppressWarnings("unchecked")
        List<Base> list = (List<Base>) dao.query("select f from " + DBfile.class.getSimpleName()
                + " f where f.domain = :domain and f.status = :status and f.filePath = :path", map);
        if (list.size() == 1) {
            Base.delete(dao, user, (DBfile) list.get(0), false);
            return true;

        }
        return false;
    }

    /**
     * Deletes a {@code DBfile} permanent from DB.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param domain
     *            domain of the {@code DBfile}
     * @param user
     *            user who deletes the {@code DBfile}.
     * @param path
     *            virtual path of the {@code DBfile}
     * @return {@code true} if the deletion was successful, {@code false} if not
     * @throws Exception
     */
    public static boolean dbDelete(DAOiface dao, Domain domain, User user, String path) throws Exception {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("status", at.treedb.db.HistorizationIface.STATUS.ACTIVE);
        map.put("path", path);
        map.put("domain", domain.getHistId());
        @SuppressWarnings("unchecked")
        List<Base> list = (List<Base>) dao.query("select f from " + DBfile.class.getSimpleName()
                + " f where f.domain = :domain and f.status = :status and f.filePath = :path", map);
        if (list.size() == 1) {
            DBfile f = (DBfile) list.get(0);
            Base.delete(dao, user, f, true);
            map = new HashMap<String, Object>();
            map.put("histId", (long) f.getHistId());
            // see also DBFSblock
            dao.queryAndExecute(
                    "delete from " + DBFSblock.class.getSimpleName() + " b where (b.id / 4294967296L) = :histId", map);
            return true;

        }
        return false;
    }

    /**
     * Increments the {@code DBfile} size.
     */
    public void incSize() {
        ++size;
    }

    /**
     * Sets the CRC32 of {@code DBfile}.
     * 
     * @param crc32
     */
    public void setCRC32(long crc32) {
        this.crc32 = crc32;
    }

    /**
     * Returns the CRC32 of the {@code DBfile} .
     * 
     * @return CRC32
     */
    public long getCRC32() {
        return crc32;
    }

    /**
     * Returns the last modification of the {@code DBfile}
     * 
     * @return date of last modification
     */
    public long lastModified() {
        return this.getLastModified().getTime();
    }

    /**
     * Returns the file size.
     * 
     * @return file size
     */
    public long getSize() {
        return size;
    }

    /**
     * Returns the virtual file path
     * 
     * @return virtual file path
     */
    public String getPath() {
        return filePath;
    }

    @Override
    public ClassID getCID() {
        return ClassID.DBFILE;
    }
}
