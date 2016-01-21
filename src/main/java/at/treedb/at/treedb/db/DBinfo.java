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

package at.treedb.db;

import java.net.InetAddress;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;

import at.treedb.user.User;

/**
 * <p>
 * Container to store DB informations inside the DB.
 * </p>
 * 
 * @author Peter Sauer
 */

@Entity
public class DBinfo extends Base implements Cloneable {
    private static final long serialVersionUID = 1L;

    public enum Fields {
        user, comment
    }

    public static final String DB_SCHEMA_VERSION = "0.1";

    private String dbSchemaVersion;

    private String uuid;
    @Column(name = "m_user")
    private String user;
    @Column(name = "m_comment") // I love oralce ;)
    private String comment;
    private String persistenceLayer;
    private String jpaImplemantation;
    @Column(name = "m_database")
    private String database;
    private String hostName;
    private String operatingSystem;
    private String javaVersion;

    protected DBinfo() {

    }

    protected DBinfo(String user, String comment) throws Exception {
        this.setHistStatus(STATUS.ACTIVE);
        dbSchemaVersion = DB_SCHEMA_VERSION;
        this.user = user;
        this.comment = comment;
        uuid = UUID.randomUUID().toString();
        DAOiface dao = DAO.getDAO();
        persistenceLayer = dao.getPersistenceLayer().name();
        if (dao.getJPAimpl() != null) {
            jpaImplemantation = dao.getJPAimpl().name();
        }
        if (dao.getDB() != null) {
            database = dao.getDB().name();
        }
        hostName = InetAddress.getLocalHost().getHostName();
        operatingSystem = System.getProperty("os.name");
        javaVersion = System.getProperty("java.vendor") + " " + System.getProperty("java.version");
    }

    @Override
    public Object checkConstraints(DAOiface dao, UpdateMap update) throws Exception {
        if (update != null) {
            long count = DBinfo.rowCount(STATUS.ACTIVE);
            if (count != 0) {
                throw new Exception("DBinfo: Only one entity DBinfo is allowed.");
            }
        }
        return null;
    }

    /**
     * Creates a {@code DBinfo}.
     * 
     * @param user
     *            creator of the {@code DBinfo}
     * @param comment
     *            comment
     * @return {@code DBinfo} object
     * @throws Exception
     */
    static public DBinfo create(DAOiface dao, String user, String comment) throws Exception {
        DBinfo info = new DBinfo(user, comment);
        Base.save(dao, null, null, info);
        return info;
    }

    /**
     * Loads a {@code Clazz}.
     * 
     * @param id
     *            ID of the {@code Clazz}
     * @return {@code Clazz} object
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static DBinfo load(DAOiface dao) throws Exception {
        List<DBinfo> list = (List<DBinfo>) Base.loadEntities(dao, DBinfo.class, null, false);
        if (list.size() > 1) {
            throw new Exception("DBinfo.load()");
        }
        if (list.size() == 0) {
            return null;
        }
        return list.get(0);
    }

    /**
     * Updates an {@code Clazz}.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param user
     *            {@code User} who performs the update
     * @param id
     *            class ID
     * @param map
     *            map of changes
     * @throws Exception
     */
    public static void update(DAOiface dao, User user, @DBkey(DBinfo.class) int id, UpdateMap map) throws Exception {
        Base.update(dao, user, id, DBinfo.class, map);
    }

    /**
     * Counts the {@code Clazz} entities/DB table rows.
     * 
     * @param status
     *            filter representing the user's historization status.
     *            <code>null<null> counts all user DB entries.
     * @return number if images
     * @throws Exception
     */
    public static long rowCount(HistorizationIface.STATUS status) throws Exception {
        return Base.countRow(null, DBinfo.class, status, null);
    }

    @Override
    public ClassID getCID() {
        return ClassID.DBINFO;
    }

    /**
     * Returns the UUID (universally unique identifier) of the TreeDB.
     * 
     * @return UUID
     */
    public String getUUID() {
        return uuid;
    }

    /**
     * Returns the user, who creates the database.
     * 
     * @return user user information
     */
    public String getUser() {
        return user;
    }

    /**
     * Returns a comment.
     * 
     * @return comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * Returns the persistence layer.
     * 
     * @return persistence layer
     */
    public String getPersistenceLayer() {
        return persistenceLayer;
    }

    /**
     * Return the JPA implementation, if available.
     * 
     * @return PA implementation
     */
    public String getJpaImplemaentation() {
        return jpaImplemantation;
    }

    /**
     * Returns the database.
     * 
     * @return database
     */
    public String getDatabase() {
        return database;
    }

    /**
     * Returns the host name of the host where the database was created.
     * 
     * @return host name
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * Returns the OS of the host where the database was created.
     * 
     * @return host name
     */
    public String getOperatingSystem() {
        return operatingSystem;
    }

    /**
     * Returns the version of the internal (TreeDB) DB schema.
     * 
     * @return internal (TreeDB) DB schema.
     */
    public String getDbSchemaVersion() {
        return dbSchemaVersion;
    }

    /**
     * Returns the exact Java Version of the environment where the database was
     * created.
     * 
     * @return Java version
     */
    public String getJavaVersion() {
        return javaVersion;
    }

}
