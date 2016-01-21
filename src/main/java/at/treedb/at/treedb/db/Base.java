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

import java.io.Serializable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.openjpa.enhance.Reflection;
import org.apache.openjpa.persistence.OpenJPAPersistence;

import at.treedb.backup.ExportIface;
import at.treedb.ci.CI;
import at.treedb.ci.Image;
import at.treedb.ci.ImageDummy;
import at.treedb.domain.DBcategory;
import at.treedb.domain.Domain;
import at.treedb.i18n.Istring;
import at.treedb.i18n.IstringDummy;
import at.treedb.user.User;

/**
 * Abstract base class for persiting entities.
 * 
 * @author Peter Sauer
 * 
 */
@SuppressWarnings("serial")
// DOLR: 29.11.2015
@MappedSuperclass
@DBindex(columnList = "histId")
public abstract class Base implements Serializable, HistorizationIface, ExportIface {
    // internal list for tracking callbacks
    private static HashMap<Class<? extends Base>, HashSet<String>> callbackUpdateFields = new HashMap<Class<? extends Base>, HashSet<String>>();
    // data base ID
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    // domain ID
    @DBkey(value = Domain.class)
    protected int domain;
    // timestamps for creation, modification and deletion
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModified;
    @Temporal(TemporalType.TIMESTAMP)
    private Date deletionDate;
    // historization status
    @Enumerated(EnumType.ORDINAL)
    private STATUS status = STATUS.ACTIVE;
    // DB ID of the first created entity
    private int histId;
    private int version;
    // internal entity version counter - managed by the persistence layer
    // transactions and concurrency tracking
    @Version
    private int dbVersion;
    // user tracking for creation & modification of entities
    @DBkey(value = User.class)
    private int createdBy;
    @DBkey(value = User.class)
    private int modifiedBy;

    private static Random random = new Random();;

    /**
     * Stores a pair class/field for tracking callbacks for fields.
     * 
     * @param clazz
     *            class to be tracked for updates
     * @param field
     *            name of the field
     */
    static public void addCallbackUpdateField(Class<? extends Base> clazz, String field) {
        HashSet<String> set = callbackUpdateFields.get(clazz);
        if (set == null) {
            set = new HashSet<String>();
            callbackUpdateFields.put(clazz, set);
        }
        set.add(field);
    }

    /**
     * Invokes a callback before updating the entity.
     * 
     * @param dao
     *            {@code DAOiface} data access object
     * @param user
     *            user {@code User} who updates the entity
     * @param map
     *            map containing the data
     * @param info
     *            optional context object
     * @return {@code true} if a update takes place, {@code false} if not
     * @throws Exception
     */
    private boolean invokeCallbackUpdate(DAOiface dao, User user, UpdateMap map, Object context) throws Exception {
        HashSet<String> set = callbackUpdateFields.get(this.getClass());
        if (set == null) {
            return false;
        }
        for (Enum<?> e : map.getMap().keySet()) {
            if (set.contains(e.name())) {
                this.callbackUpdate(dao, user, map, context);
                return true;
            }
        }
        return false;
    }

    /**
     * Checks data against optional constraints.
     * 
     * @param dao
     *            {@code DAOiface} data access object
     * @param map
     *            map containing the data
     * @return optional context object
     * @throws Exception
     */
    public Object checkConstraints(DAOiface dao, UpdateMap map) throws Exception {
        return null;
    }

    /**
     * Optional method/callback before persisting data e.g. used for setting the
     * internal ID according a rule.
     */
    protected void callbackBeforeSave() {
    }

    /**
     * Optional method/callback before updating data.
     */
    protected void callbackUpdate(DAOiface dao, User user, UpdateMap map, Object context) throws Exception {
    }

    /**
     * Optional callback after loading the entity. e.g. used for setting
     * transient data fields
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @throws Exception
     */
    public void callbackAfterLoad(DAOiface dao) throws Exception {
    }

    /**
     * Checks if a callback after loading entity is necessary.
     * 
     * @return {@code true} if {@code callbackAfterLoad()} is invoked,
     *         {@code false} if not
     */
    public boolean isCallbackAfterLoad() {
        return false;
    }

    /**
     * Returns the proprietary DB ID of the entity.
     * 
     * @return DB ID
     */
    @Override
    public int getDBid() {
        return id;
    }

    /**
     * Returns a composed unique DB id consisting of the proprietary DB ID and
     * the class ID.
     * 
     * @return composed unique DB id
     */
    public long getUniqueDBid() {
        return ((long) getCID().ordinal() << 32) | (long) histId;
    }

    /*
     * histId should be unique and can be used as a hash value
     */
    @Override
    public int hashCode() {
        return histId;
    }

    /*
     * see above comment
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        // ensure that only objects of the same class are compared
        // the histId is only unique over the entities of a class (Hibernate vs.
        // JPA)
        if (!(this.getClass().equals(o.getClass()))) {
            return false;
        }
        return ((Base) o).histId == histId;
    }

    /**
     * Sets the proprietary DB ID of the entity.
     * 
     * @param id
     *            DB ID
     */
    @Override
    public void setDBid(int id) {
        this.id = id;
    }

    /**
     * Returns the version of the entity. Each historization (=update)
     * increments the version counter.
     * 
     * @return version counter
     */
    @Override
    public int getVersion() {
        return version;
    }

    /**
     * Increments the version counter.
     */
    @Override
    public void incVersion() {
        ++version;
    }

    /**
     * Sets the historization version counter.
     * 
     * @param version
     *            version counter
     */
    @Override
    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * Sets the creation time stamp of the entity.
     * 
     * @param timestamp
     *            creation time
     */
    public void setCreationTime(Date timestamp) {
        this.creationTime = timestamp;
    }

    /**
     * Returns the creation time stamp of the entity.
     * 
     * @return creation time
     */
    public Date getCreationTime() {
        return creationTime;
    }

    /**
     * Sets the last modification time stamp of the entity.
     * 
     * @param timestamp
     *            modification time
     */
    public void setLastModified(Date timestamp) {
        lastModified = timestamp;
    }

    /**
     * Returns the last modification time stamp of the entity.
     * 
     * @return last modification time
     */
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * Sets the deletion time stamp of the entity.
     * 
     * @param delDate
     *            deletion time
     */
    public void setDeletionDate(Date delDate) {
        deletionDate = delDate;

    }

    /**
     * Returns the deletion time stamp of the entity.
     * 
     * @return deletion time
     */
    public Date getDeletionDate() {
        return deletionDate;
    }

    /**
     * Sets the historization status of the entity.
     * 
     * @param status
     *            historization status
     */
    public void setHistStatus(STATUS status) {
        this.status = status;
    }

    /**
     * Returns the historization status of the entity.
     * 
     * @return historization status
     */
    public STATUS getHistStatus() {
        return status;
    }

    /**
     * Sets the historization ID of the entity. This value is equal to the DB ID
     * of the first version of the entity.
     * 
     * @param histId
     *            historization ID
     */
    public void setHistId(int histId) {
        this.histId = histId;
    }

    /**
     * Returns the historization ID of the entity.
     * 
     * @return historization ID
     */
    public int getHistId() {
        return histId;
    }

    /**
     * Sets the creator of this entity.
     * 
     * @param createdBy
     *            {@code User} ID
     */
    public void setCreatedBy(@DBkey(value = User.class) int createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * Returns the creator of this entity.
     * 
     * @return {@code User} ID
     */
    public @DBkey(value = User.class) int getCreatedBy() {
        return createdBy;
    }

    /**
     * Sets the user who made the last modification of this entity.
     * 
     * @param modifiedBy
     *            {@code User} ID
     */
    public void setModifiedBy(@DBkey(value = User.class) int modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    /**
     * Returns the user who has make the last modification of this entity.
     * 
     * @return {@code User} ID
     */
    public @DBkey(value = User.class) int getModifiedBy() {
        return modifiedBy;
    }

    /**
     * Returns the transaction version of this entity - managed by the
     * transaction layer.
     * 
     * @return transaction version
     */
    public int getTransactionVersion() {
        return dbVersion;
    }

    /**
     * Sets the transaction version of the entity to 0. Used internally for
     * operations like cloning objects.
     */
    public void resetTransactionVersion() {
        this.dbVersion = 0;
    }

    /**
     * Sets the {@code Domain}.
     * 
     * @param domain
     *            {@code Domain} of the entity
     */
    public void setDomain(@DBkey(value = Domain.class) int domain) {
        this.domain = domain;
    }

    /**
     * Returns the {@code Domain} ID.
     * 
     * @return {@code Domain} of the entity
     */
    public @DBkey(value = Domain.class) int getDomain() {
        return domain;
    }

    /**
     * Loads an entity.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param clazz
     *            entity class to be loaded
     * @param id
     *            base class ID
     * @param date
     *            optional temporal bound
     * @return {@code Base} entity
     * @throws Exception
     */
    protected static Base load(DAOiface dao, Class<? extends Base> clazz, @DBkey(value = Base.class) int id, Date date)
            throws Exception {
        return load(dao, clazz, id, date, false);
    }

    /**
     * Loads an entity.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param clazz
     *            entity class to be loaded
     * @param id
     *            base class ID
     * @return {@code Base} entity
     * @throws Exception
     */
    public static Base load(DAOiface dao, Class<? extends Base> clazz, @DBkey(value = Base.class) int id)
            throws Exception {
        return load(dao, clazz, id, null, false);
    }

    /**
     * Loads an entity without binary data.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param clazz
     *            entity class to be loaded
     * @param id
     *            base class ID
     * @param date
     *            optional temporal bound
     * @return {@code Base} entity
     * @throws Exception
     */
    protected static Base loadLazy(DAOiface dao, Class<? extends Base> clazz, @DBkey(value = Base.class) int id,
            Date date) throws Exception {
        return load(dao, clazz, id, date, true);
    }

    /**
     * Loads an entity.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param clazz
     *            entity class to be loaded
     * @param id
     *            base class ID
     * @param date
     *            optional temporal bound
     * @param lazy
     *            {@code true} for lazy loading
     * @return {@code Base} entity
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    protected static Base load(DAOiface dao, Class<? extends Base> clazz, @DBkey(value = Base.class) int id, Date date,
            boolean lazy) throws Exception {
        Base base = null;
        boolean localDAO = false;
        if (dao == null) {
            dao = DAO.getDAO();
            localDAO = true;
        }
        try {
            if (localDAO) {
                dao.beginTransaction();
            }
            List<Base> list = null;
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("id", id);
            String className = clazz.getSimpleName();
            if (date == null) {
                map.put("status", at.treedb.db.HistorizationIface.STATUS.ACTIVE);
                list = (List<Base>) dao
                        .query("select i from " + className + " i where i.histId = :id and i.status = :status", map);
            } else {
                // load all entities
                map.put("date", date);
                list = (List<Base>) dao.query(
                        "select i from " + className
                                + " i where i.histId = :id and i.lastModified < :date and (i.deletionDate is null or i.deletionDate > :date) order by i.version desc",
                        0, 1, map);
            }
            if (list.size() == 1) {
                base = list.get(0);
                if (lazy) {
                    // callback for loading the binary data
                    base.callbackAfterLoad(dao);
                }
            } else {
                if (list.size() > 1) {
                    throw new Exception("Base.load(): Entity/DB ID is not unique!");
                }
            }
            if (localDAO) {
                dao.endTransaction();
            }
        } catch (Exception e) {
            if (localDAO) {
                dao.rollback();
            }
            throw e;
        }
        return base;
    }

    /**
     * Loads all entities of a class.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param clazz
     *            class name
     * @param date
     *            optional temporal bound
     * @param callbackAfterLoad
     *            calls the overwritten method {@code callbackAfterLoad}
     * @return list entity list
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static List<? extends Base> loadEntities(DAOiface dao, Class<? extends Base> clazz, Date date,
            boolean callbackAfterLoad) throws Exception {
        List<? extends Base> list = null;
        boolean localDAO = false;
        if (dao == null) {
            dao = DAO.getDAO();
            localDAO = true;
        }
        try {
            if (localDAO) {
                dao.beginTransaction();
            }

            String className = clazz.getSimpleName();
            HashMap<String, Object> map = new HashMap<String, Object>();
            if (date == null) {
                // load all active entities
                map.put("status", at.treedb.db.HistorizationIface.STATUS.ACTIVE);
                list = (List<? extends Base>) dao.query("select i from " + className + " i where i.status = :status",
                        map);
            } else {
                // load all entities with a temporal bound
                map.put("date", date);
                list = (List<? extends Base>) dao.query(
                        "select i from " + className
                                + " i where i.lastModified < :date and (i.deletionDate is null or i.deletionDate > :date) order by i.version desc",
                        0, 1, map);
            }
            if (callbackAfterLoad && !list.isEmpty()) {
                for (Base b : list) {
                    b.callbackAfterLoad(dao);
                }
            }
            if (localDAO) {
                dao.endTransaction();
            }
        } catch (Exception e) {
            dao.rollback();
            throw e;
        }
        return list;
    }

    /**
     * Loads entities of a class.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param clazz
     *            class name
     * @param domain
     *            {@code Domain} of the entity
     * @param crit
     *            search criteria
     * @param date
     *            temporal bound
     * @return list entity list
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static List<? extends Base> loadEntities(DAOiface dao, Class<? extends Base> clazz, int domain,
            SearchCriteria crit, Date date) throws Exception {
        List<Base> list = null;
        boolean localDAO = false;
        if (dao == null) {
            dao = DAO.getDAO();
            localDAO = true;
        }
        try {
            if (localDAO) {
                dao.beginTransaction();
            }
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("domain", domain);
            String criteria = "";
            if (crit != null) {
                String critName = crit.getEnumValue().name();
                criteria += " and data." + critName + " " + crit.getOperator().toString() + " :" + critName;
                map.put(critName, crit.getData());
            }
            String className = clazz.getSimpleName();
            // load only active entities
            if (date == null) {
                map.put("status", at.treedb.db.HistorizationIface.STATUS.ACTIVE);
                list = (List<Base>) dao.query("select data from " + className
                        + " data where data.domain = :domain and data.status = :status" + criteria, map);
            } else {
                map.put("date", date);
                list = (List<Base>) dao.query("select data from " + className
                        + " data where data.domain = :domain and data.lastModified < :date and (data.deletionDate = null or data.deletionDate > :date)"
                        + criteria + " order by data.version having max(data.version)", map);
            }
            if (!list.isEmpty() && list.get(0).isCallbackAfterLoad()) {
                for (Base b : list) {
                    b.callbackAfterLoad(dao);
                }
            }
            if (localDAO) {
                dao.endTransaction();
            }
        } catch (Exception e) {
            if (localDAO) {
                dao.rollback();
            }
            throw e;
        }
        return list;
    }

    /**
     * Loads entities of a class.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param clazz
     *            class name
     * @param domain
     *            {@code Domain} of the entity
     * @param crit
     *            search criteria
     * @param date
     *            temporal bound
     * @return list entity list
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static List<? extends Base> loadEntities(DAOiface dao, Class<? extends Base> clazz, SearchCriteria crit,
            Date date) throws Exception {
        List<Base> list = null;
        boolean localDAO = false;
        if (dao == null) {
            dao = DAO.getDAO();
            localDAO = true;
        }
        try {
            if (localDAO) {
                dao.beginTransaction();
            }
            HashMap<String, Object> map = new HashMap<String, Object>();
            String criteria = "";
            if (crit != null) {
                String critName = crit.getEnumValue().name();
                criteria += " and data." + critName + " " + crit.getOperator().toString() + " :" + critName;
                map.put(critName, crit.getData());
            }
            String className = clazz.getSimpleName();
            // load only active entities
            if (date == null) {
                map.put("status", at.treedb.db.HistorizationIface.STATUS.ACTIVE);
                list = (List<Base>) dao
                        .query("select data from " + className + " data where data.status = :status" + criteria, map);
            } else {
                map.put("date", date);
                list = (List<Base>) dao.query("select data from " + className
                        + " data where data.lastModified < :date and (data.deletionDate = null or data.deletionDate > :date)"
                        + criteria + " order by data.version having max(data.version)", map);
            }
            if (!list.isEmpty() && list.get(0).isCallbackAfterLoad()) {
                for (Base b : list) {
                    b.callbackAfterLoad(dao);
                }
            }
            if (localDAO) {
                dao.endTransaction();
            }
        } catch (Exception e) {
            if (localDAO) {
                dao.rollback();
            }
            throw e;
        }
        return list;
    }

    /**
     * Loads entities of a class.
     * 
     * @param dao
     * @param domain
     * @param clazz
     * @param crit
     * @param limit
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static List<? extends Base> loadEntities(DAOiface dao, Domain domain, Class<? extends Base> clazz,
            SearchCriteria crit, SearchLimit limit) throws Exception {
        List<Base> list = null;
        boolean localDAO = false;
        if (dao == null) {
            dao = DAO.getDAO();
            localDAO = true;
        }
        try {
            if (localDAO) {
                dao.beginTransaction();
            }
            HashMap<String, Object> map = new HashMap<String, Object>();

            String criteria = "";
            if (crit != null) {
                String critName = crit.getEnumValue().name();
                criteria += " and data." + critName + " " + crit.getOperator().toString() + " :" + critName;
                map.put(critName, crit.getData());
            }
            String dom = "";
            if (domain != null) {
                dom = " and data.domain = :domain";
                map.put("domain", domain.getHistId());
            }
            String className = clazz.getSimpleName();
            // load only active entities
            map.put("status", at.treedb.db.HistorizationIface.STATUS.ACTIVE);
            list = (List<Base>) dao.query("select data from " + className + " data where data.status = :status" + dom
                    + criteria + " order by data.creationTime DESC", limit.getFirstResult(), limit.getMaxResults(),
                    map);

            if (!list.isEmpty() && list.get(0).isCallbackAfterLoad()) {
                for (Base b : list) {
                    b.callbackAfterLoad(dao);
                }
            }
            if (localDAO) {
                dao.endTransaction();
            }
        } catch (Exception e) {
            if (localDAO) {
                dao.rollback();
            }
            throw e;
        }
        return list;
    }

    /**
     * 
     * @param dao
     * @param domain
     * @param clazz
     * @param crit
     * @param start
     * @param end
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static List<? extends Base> loadEntities(DAOiface dao, Domain domain, Class<? extends Base> clazz,
            SearchCriteria crit, Date start, Date end) throws Exception {
        List<Base> list = null;
        if (start == null || end == null) {
            throw new Exception("Base.loadEntities(): temporal bound can not be null");
        }
        boolean localDAO = false;
        if (dao == null) {
            dao = DAO.getDAO();
            localDAO = true;
        }
        try {
            if (localDAO) {
                dao.beginTransaction();
            }
            HashMap<String, Object> map = new HashMap<String, Object>();

            String criteria = "";
            if (crit != null) {
                String critName = crit.getEnumValue().name();
                criteria += " and data." + critName + " " + crit.getOperator().toString() + " :" + critName;
                map.put(critName, crit.getData());
            }
            String dom = "";
            if (domain != null) {
                dom = " and data.domain = :domain";
                map.put("domain", domain.getHistId());
            }
            String className = clazz.getSimpleName();
            // load only active entities
            map.put("start", start);
            map.put("end", end);
            map.put("status", at.treedb.db.HistorizationIface.STATUS.ACTIVE);
            String time = " and (data.creationTime >= :start and data.creationTime <= :end)";
            list = (List<Base>) dao.query(
                    "select data from " + className + " data where data.status = :status" + dom + criteria + time, map);

            if (!list.isEmpty() && list.get(0).isCallbackAfterLoad()) {
                for (Base b : list) {
                    b.callbackAfterLoad(dao);
                }
            }
            if (localDAO) {
                dao.endTransaction();
            }
        } catch (Exception e) {
            if (localDAO) {
                dao.rollback();
            }
            throw e;
        }
        return list;
    }

    /**
     * 
     * @param dao
     * @param clazz
     * @param crit
     * @param date
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static List<? extends Base> loadEntities(DAOiface dao, Class<? extends Base> clazz, SearchCriteria[] crit,
            Date date) throws Exception {
        List<Base> list = null;
        boolean localDAO = false;
        if (dao == null) {
            dao = DAO.getDAO();
            localDAO = true;
        }
        try {
            if (localDAO) {
                dao.beginTransaction();
            }
            HashMap<String, Object> map = new HashMap<String, Object>();
            String criteria = "";
            if (crit != null) {
                for (SearchCriteria c : crit) {
                    String critName = c.getEnumValue().name();
                    criteria += " and data." + critName + " " + c.getOperator().toString() + " :" + critName;
                    map.put(critName, c.getData());
                }

            }
            String className = clazz.getSimpleName();
            // load only active entities
            if (date == null) {
                map.put("status", at.treedb.db.HistorizationIface.STATUS.ACTIVE);
                list = (List<Base>) dao
                        .query("select data from " + className + " data where data.status = :status" + criteria, map);
            } else {
                map.put("date", date);
                list = (List<Base>) dao.query("select data from " + className
                        + " data where data.lastModified < :date and (data.deletionDate = null or data.deletionDate > :date)"
                        + criteria + " order by data.version having max(data.version)", map);
            }
            if (!list.isEmpty() && list.get(0).isCallbackAfterLoad()) {
                for (Base b : list) {
                    b.callbackAfterLoad(dao);
                }
            }
            if (localDAO) {
                dao.endTransaction();
            }
        } catch (Exception e) {
            if (localDAO) {
                dao.rollback();
            }
            throw e;
        }
        return list;
    }

    /**
     * Saves an entity.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param domain
     *            domain {@Domain) of the entity.
     * @param user
     *            user {@code User} who deletes the entity
     * @param base
     *            entity to be saved
     * @throws Exception
     */
    protected static void save(DAOiface dao, Domain domain, User user, Base base) throws Exception {
        boolean localDAO = false;
        if (dao == null) {
            dao = DAO.getDAO();
            localDAO = true;
        }
        try {
            if (localDAO) {
                dao.beginTransaction();
            }
            // perform some checks
            base.checkConstraints(dao, null);

            Date d = new Date();
            base.setCreationTime(d);
            base.setLastModified(d);
            if (domain != null) {
                base.setDomain(domain.getHistId());
            }

            if (user != null) {
                base.setCreatedBy(user.getHistId());
                base.setModifiedBy(user.getHistId());
            }
            // save entity to get an ID
            dao.saveAndFlushIfJPA(base);
            // optional callback for persisting data
            base.callbackBeforeSave();

            // historization ID = DB ID
            base.setHistId(base.getDBid());
            if (localDAO) {
                dao.endTransaction();
            }
        } catch (Exception e) {
            if (localDAO) {
                dao.rollback();
            }
            throw e;
        }
    }

    /**
     * Help method for restoring an entity from the backup. This method resets
     * all internal object IDs.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param base
     *            restored object
     * @throws Exception
     */
    public static void restore(DAOiface dao, Base base) throws Exception {
        // reset internal values
        base.id = base.dbVersion = 0;
        // save entity to get an ID
        dao.saveAndFlushIfJPA(base);
    }

    /**
     * Deletes an entity.
     * 
     * @param user
     *            {@code User} who deletes the entity
     * 
     * @param base
     *            base entity to be deleted
     * @param dbDelete
     *            {@code true} for deleting the entity form DB, {@code false}
     *            historization delete
     * 
     * @throws Exception
     */
    static protected void delete(User user, Base base, boolean dbDelete) throws Exception {
        delete(null, user, base, 0, null, dbDelete);
    }

    /**
     * Deletes an entity.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * 
     * @param user
     *            {@code User} who deletes the entity.
     * 
     * @param base
     *            base entity to be deleted
     * @param dbDelete
     *            {@code true} for deleting the entity form DB, {@code false}
     *            historization deletion
     * 
     * @throws Exception
     */
    static protected void delete(DAOiface dao, User user, Base base, boolean dbDelete) throws Exception {
        delete(dao, user, base, 0, null, dbDelete);
    }

    /**
     * Deletes an entity.
     * 
     * @param user
     *            {@code User} who deletes the entity.
     * @param id
     *            ID of the entity
     * @param clazz
     *            class of the entity
     * @param dbDelete
     *            {@code true} for deleting the entity form DB, {@code false}
     *            historization deletion
     * @return {@code true} if the entity exits and deletion was successful,
     *         {@code false} if the entity ID doesn't exist
     * @throws Exception
     */
    static protected boolean delete(User user, @DBkey(value = Base.class) int id, Class<? extends Base> clazz,
            boolean dbDelete) throws Exception {
        return delete(null, user, null, id, clazz, dbDelete);
    }

    /**
     * Deletes an entity.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param user
     *            {@code User} who deletes the entity.
     * @param id
     *            ID of the entity
     * @param clazz
     *            class of the entity
     * @param dbDelete
     *            {@code true} for deleting the entity form DB, {@code false}
     *            historization deletion
     * @return {@code true} if the entity exits and deletion was successful,
     *         {@code false} if the entity ID doesn't exist
     * @throws Exception
     */
    static protected boolean delete(DAOiface dao, User user, @DBkey(value = Base.class) int id,
            Class<? extends Base> clazz, boolean dbDelete) throws Exception {
        return delete(dao, user, null, id, clazz, dbDelete);
    }

    /**
     * Deletes an entity.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * 
     * @param user
     *            user {@code User} who deletes an entity.
     * 
     * @param base
     *            entity to be deleted, if {@code null} the parameter entity ID
     *            will be used for the entity deletion
     * 
     * @param id
     *            ID of the entity, if the entity is {@code null}
     * 
     * @param clazz
     *            class information, if the entity is given by its id
     * @return {@code true} if the entity exits and deletion was successful,
     *         {@code false} if the entity ID doesn't exist
     * @throws Exception
     */
    static private boolean delete(DAOiface dao, User user, Base base, @DBkey(value = Base.class) int id,
            Class<? extends Base> clazz, boolean dbDelete) throws Exception {
        if (!dbDelete && base != null && base.getHistStatus() != HistorizationIface.STATUS.ACTIVE) {
            throw new Exception("User.delete(): Deleting historic entities isn't allowed!");
        }
        boolean localDAO = false;
        if (dao == null) {
            dao = DAO.getDAO();
            localDAO = true;
        }
        Date d = new Date();
        boolean deleted = false;
        try {
            if (localDAO) {
                dao.beginTransaction();
            }
            if (base == null) {
                if (clazz == null) {
                    throw new Exception("Base.delete(): parameter clazz is null!");
                }
                base = dao.get(clazz, id);
            }
            if (base != null) {

                base.setHistStatus(STATUS.DELETED);
                base.setDeletionDate(d);
                if (user != null) {
                    base.setModifiedBy(user.getHistId());
                }
                dao.update(base);

                deleted = true;
            }
            if (localDAO) {
                dao.endTransaction();
            }
        } catch (Exception e) {
            if (localDAO) {
                dao.rollback();
            }
            throw e;
        }

        return deleted;
    }

    /**
     * Updates an entity.
     * 
     * @param user
     *            user who updates an entity
     * 
     * @param base
     *            entity which should be updated
     * @param map
     *            map containing all changes
     * @throws Exception
     */
    public static void update(User user, @DBkey(value = Base.class) Base base, UpdateMap map) throws Exception {
        update(null, user, base, 0, null, map);
    }

    /**
     * Updates an entity.
     * 
     * @param user
     *            {@code User} who updates an entity
     * 
     * @param id
     *            entity ID which should be updated
     * @param clazz
     *            class information, if the entity is given by its ID
     * @param map
     *            map containing all changes
     * @throws Exception
     */
    public static void update(User user, @DBkey(value = Base.class) int id, Class<? extends Base> clazz, UpdateMap map)
            throws Exception {
        update(null, user, null, id, clazz, map);
    }

    /**
     * Updates an entity.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * 
     * @param user
     *            {@code User} who updates an entity
     * 
     * @param base
     *            entity which should be updated
     * 
     * @param map
     *            map containing all changes
     * @throws Exception
     */
    public static void update(DAOiface dao, User user, Base base, UpdateMap map) throws Exception {
        update(dao, user, base, 0, null, map);
    }

    /**
     * Updates an entity.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * 
     * @param user
     *            {@code User} who updates an entity
     * 
     * @param id
     *            entity ID which should be updated
     * @param clazz
     *            class information, if the entity is given by its ID
     * @param map
     *            map containing all changes
     * @throws Exception
     */
    public static void update(DAOiface dao, User user, @DBkey(value = Base.class) int id, Class<? extends Base> clazz,
            UpdateMap map) throws Exception {
        update(dao, user, null, id, clazz, map);
    }

    /**
     * Updates an entity.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * 
     * @param user
     *            {@code User} who updates an entity
     * 
     * @param base
     *            entity which should be updated
     * @param id
     *            entity ID which should be updated
     * @param clazz
     *            class information, if the entity is given by its id
     * @param map
     *            map containing the changes
     * @throws Exception
     */
    private static void update(DAOiface dao, User user, Base base, @DBkey(value = Base.class) int id,
            Class<? extends Base> clazz, UpdateMap map) throws Exception {
        if (base != null && base.getHistStatus() != HistorizationIface.STATUS.ACTIVE) {
            throw new Exception("User.update(): Updating historic entities isn't allowed!");
        }
        if (base != null) {
            base.check(map);
        }
        if (map.size() == 0) {
            return;
        }
        boolean localDAO = false;
        if (dao == null) {
            dao = DAO.getDAO();
            localDAO = true;
        }
        try {
            if (localDAO) {
                dao.beginTransaction();
            }
            if (base == null) {
                if (clazz == null) {
                    throw new Exception("Base.update(): parameter clazz is null!");
                }
                base = dao.get(clazz, id);
                base.check(map);
            }
            Object info = null;
            if (map.size() > 0) {
                // update the entity?
                if (map.isEntityUpdate()) {
                    info = base.checkConstraints(dao, map);
                    synchronized (base) {
                        Base copy = (Base) base.clone();
                        if (copy.getHistStatus() == STATUS.ACTIVE) {
                            copy.setHistStatus(STATUS.UPDATED);
                        }
                        copy.setDBid(0);
                        copy.resetTransactionVersion();

                        dao.save(copy);

                        if (user != null) {
                            base.setModifiedBy(user.getHistId());
                        }
                        if (dao.getPersistenceLayer() == at.treedb.db.DAOiface.PERSISTENCE_LAYER.JPA) {
                            // the JPA way
                            // 1.) update the entity in memory
                            base.update(dao, user, map);
                            // 2.) re-load the entity
                            base = dao.get(base.getClass(), base.getDBid());
                        }
                        // 3.) update the entity
                        base.update(dao, user, map);
                        base.incVersion();
                        base.setLastModified(new Date());
                        dao.update(base);
                    }
                } else {
                    // update contains only referenced data types (e.g. Istring
                    // or
                    // Image)
                    base.update(dao, user, map);
                }
                base.invokeCallbackUpdate(dao, user, map, info);

            }
            if (localDAO) {
                dao.endTransaction();
            }
        } catch (Exception e) {
            if (localDAO) {
                dao.rollback();
            }
            throw e;
        }
    }

    /**
     * Search filter
     * 
     * @author Peter Sauer
     * 
     */
    public enum Search {

        /**
         * case sensitive search
         */
        CASE_SENSITIVE,
        /**
         * include historic data
         */
        HISTORIC,
        /**
         * exact search
         */
        EQUALS,
        /**
         * result set limitation
         * 
         */
        LIMIT;

    }

    /**
     * Searches a value inside the fields of an entity.
     * 
     * @param domain
     *            {@code Domain} of the entity
     * @param clazz
     *            class to be searched
     * @param fields
     *            fields to be searched
     * @param value
     *            search pattern
     * @param criteria
     *            additional search criteria
     * @param flags
     *            search filter
     * @param callbackAfterLoad
     *            calls the overwritten method {@code callbackAfterLoad}
     * @return list of {@code Base} entities
     * @throws Exception
     */
    protected static List<Base> search(Domain domain, Class<? extends Base> clazz, EnumSet<?> fields, String value,
            SearchCriteria[] criteria, EnumSet<Search> flags, SearchLimit limit, boolean callbackAfterLoad)
                    throws Exception {
        return search(null, domain, clazz, fields, value, criteria, flags, limit, callbackAfterLoad);
    }

    /**
     * Searches a value inside the fields of an entity.
     * 
     * @param domain
     *            {@code Domain} of the entity
     * @param clazz
     *            class to be searched
     * @param fields
     *            fields to be searched
     * @param value
     *            search pattern
     * @param criteria
     *            additional search criteria
     * @param flags
     *            search filter
     * @param callbackAfterLoad
     *            calls the overwritten method {@code callbackAfterLoad}
     * @return list of {@code Base} entities
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static List<Base> search(DAOiface dao, Domain domain, Class<? extends Base> clazz, EnumSet<?> fields,
            String value, SearchCriteria[] criteria, EnumSet<Search> flags, SearchLimit limit,
            boolean callbackAfterLoad) throws Exception {
        List<Base> list = null;
        boolean isDAOlocale = false;
        try {
            if (dao == null) {
                dao = DAO.getDAO();
                isDAOlocale = true;
                dao.beginTransaction();
            }
            StringBuffer buf = new StringBuffer();

            HashMap<String, Object> map = new HashMap<String, Object>();
            boolean caseSenstive = false;
            if (flags != null && flags.contains(Search.CASE_SENSITIVE)) {
                caseSenstive = true;
            } else {
                value = value.toUpperCase();
            }
            map.put("value", value);
            buf.append("select i from ");
            buf.append(clazz.getSimpleName());
            buf.append(" i where ");

            if (flags == null || !flags.contains(Search.HISTORIC)) {
                buf.append("i.status = :status and ");
                map.put("status", at.treedb.db.HistorizationIface.STATUS.ACTIVE);
            }

            if (domain != null) {
                buf.append("i.domain = :domain and ");
                map.put("domain", domain.getHistId());
            }

            if (criteria != null) {
                for (SearchCriteria crit : criteria) {
                    String critName = crit.getEnumValue().name();
                    buf.append(" i.");
                    buf.append(critName);
                    buf.append(" ");
                    buf.append(crit.getOperator().toString());
                    buf.append(" :");
                    buf.append(critName);
                    buf.append(" and");
                    map.put(critName, crit.getData());
                }
            }

            buf.append(" (");
            Iterator<?> iter = fields.iterator();

            int index = 0;
            while (iter.hasNext()) {
                if (index > 0) {
                    buf.append(" OR ");
                }
                if (!caseSenstive) {
                    buf.append("UPPER(");
                }
                buf.append("i.");
                buf.append(((Enum<?>) iter.next()).name());
                if (!caseSenstive) {
                    buf.append(")");
                }
                if (flags != null && flags.contains(Search.EQUALS)) {
                    buf.append(" = ");
                } else {
                    buf.append(" like ");
                }
                buf.append(":value");
                ++index;
            }

            buf.append(")");

            if (flags != null && flags.contains(Search.HISTORIC)) {
                buf.append(" ORDER BY i.version DESC");
            }
            if (flags != null && flags.contains(Search.LIMIT)) {
                list = (List<Base>) dao.query(buf.toString(), limit.getFirstResult(), limit.getMaxResults(), map);
            } else {
                list = (List<Base>) dao.query(buf.toString(), map);
            }
            if (callbackAfterLoad && !list.isEmpty()) {
                for (Base b : list) {
                    b.callbackAfterLoad(dao);
                }
            }
            if (isDAOlocale) {
                dao.endTransaction();
            }
        } catch (Exception e) {
            dao.rollback();
            throw e;
        }
        return list;
    }

    /**
     * Checks the map containing the changes. Redundant update entries will be
     * removed.
     * 
     * @param map
     *            map containing the changes
     * @throws Exception
     */
    protected void check(UpdateMap map) throws Exception {
        Class<?> c = this.getClass();
        Enum<?>[] list = map.getMap().keySet().toArray(new Enum[map.getMap().keySet().size()]);
        for (Enum<?> field : list) {
            Update u = map.get(field);
            Field f;
            try {
                f = c.getDeclaredField(field.name());
            } catch (java.lang.NoSuchFieldException e) {
                f = c.getSuperclass().getDeclaredField(field.name());
            }
            f.setAccessible(true);
            switch (u.getType()) {
            case STRING:
                String value = u.getString();
                String s = (String) f.get(this);
                if ((value == null && s == null) || (s != null && value != null && s.equals(value))) {
                    map.remove(field);
                }
                break;
            case DOUBLE:
                if (u.getDouble() == (Double) f.get(this)) {
                    map.remove(field);
                }
                break;
            case LONG:
                if (u.getLong() == (Long) f.get(this)) {
                    map.remove(field);
                }
                break;
            case INT:
                if (u.getInt() == (Integer) f.get(this)) {
                    map.remove(field);
                }
                break;
            case BINARY:
                byte[] a = (byte[]) f.get(this);
                byte[] b = u.getBinary();
                if ((a == null && b == null) || (a != null && b != null && Arrays.equals(a, b))) {
                    map.remove(field);
                }
                break;
            case BOOLEAN:
                if (u.getBoolean() == (Boolean) f.get(this)) {
                    map.remove(field);
                }
                break;
            case DATE:
                Date dvalue = u.getDate();
                Date d = (Date) f.get(this);
                if ((dvalue == null && d == null) || (d != null && dvalue != null && d.equals(dvalue))) {
                    map.remove(field);
                }
                break;
            case ENUM:
                if (u.getEnum() == (Enum<?>) f.get(this)) {
                    map.remove(field);
                }
                break;
            case BIGDECIMAL:
                if (u.getBigDecimal().equals((BigDecimal) f.get(this))) {
                    map.remove(field);
                }
                // no check for these types
            case IMAGE:
            case ISTRING_DELETE:
            case ISTRING:
            case IMAGE_DELETE:
            case IMAGE_DUMMY:
                break;
            default:
                throw new Exception("Base.check(): Type not implemented: " + u.getType().toString());
            }
        }
    }

    /**
     * Returns the class ID.
     */
    public abstract ClassID getCID();

    /**
     * Updates an entity.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param user
     *            user who updates the entity
     * @param map
     *            map containing the changes
     * @throws Exception
     */
    protected void update(DAOiface dao, User user, UpdateMap map) throws Exception {
        Class<?> c = this.getClass();

        for (Enum<?> field : map.getMap().keySet()) {
            Update u = map.get(field);
            Field f;
            try {
                f = c.getDeclaredField(field.name());
            } catch (java.lang.NoSuchFieldException e) {
                f = c.getSuperclass().getDeclaredField(field.name());
            }
            f.setAccessible(true);
            switch (u.getType()) {
            case STRING:
                f.set(this, u.getString());
                break;
            case DOUBLE:
                f.set(this, u.getDouble());
                break;
            case FLOAT:
                f.set(this, u.getFloat());
                break;
            case LONG:
                f.set(this, u.getLong());
                break;
            case INT:
                f.set(this, u.getInt());
                break;
            case LAZY_BINARY:
            case BINARY:
                f.set(this, u.getBinary());
                break;
            case BOOLEAN:
                f.set(this, u.getBoolean());
                break;
            case DATE:
                f.set(this, u.getDate());
                break;
            case ENUM:
                f.set(this, u.getEnum());
                break;
            case BIGDECIMAL:
                f.set(this, u.getBigDecimal());
                break;
            case ISTRING: {
                DBkey a = f.getAnnotation(DBkey.class);
                if (a == null || !a.value().equals(Istring.class)) {
                    throw new Exception("Base.update(): Field type mismatch for an IString");
                }
                ArrayList<IstringDummy> list = u.getIstringDummy();
                // dao.flush();
                for (IstringDummy i : list) {
                    Istring istr = null;
                    int userId = user != null ? user.getHistId() : 0;
                    if (f.getInt(this) == 0) {
                        istr = Istring.create(dao, domain, user, this.getCID(), i.getText(), i.getLanguage());
                    } else {
                        istr = Istring.saveOrUpdate(dao, domain, userId, f.getInt(this), i.getText(), i.getLanguage(),
                                i.getCountry(), this.getCID());
                    }
                    // only for CI make a reference form IString to the owner
                    if (this instanceof CI) {
                        istr.setCI(this.getHistId());
                    }
                    if (f.getInt(this) == 0) {
                        f.setInt(this, istr.getHistId());
                        dao.update(this);
                    }
                }
                break;
            }
            case ISTRING_DELETE: {
                DBkey a = f.getAnnotation(DBkey.class);
                if (a == null || !a.value().equals(Istring.class)) {
                    throw new Exception("Base.update(): Field type mismatch for an IString");
                }
                ArrayList<IstringDummy> list = u.getIstringDummy();
                for (IstringDummy i : list) {
                    if (i.getCountry() == null && i.getLanguage() == null) {
                        Istring.delete(dao, user, f.getInt(this));
                    } else if (i.getCountry() == null && i.getLanguage() != null) {
                        Istring.delete(dao, user, f.getInt(this), i.getLanguage());
                    } else if (i.getCountry() != null && i.getLanguage() != null) {
                        Istring.delete(dao, user, f.getInt(this), i.getLanguage(), i.getCountry());
                    }
                }
                break;
            }
            case IMAGE: {
                DBkey a = f.getAnnotation(DBkey.class);
                if (a == null || !a.value().equals(Image.class)) {
                    throw new Exception("Base.update(): Field type mismatch for an Image");
                }
                Image.update(dao, user, f.getInt(this), u.getUpdateMap());
                break;
            }
            case IMAGE_DUMMY: {
                DBkey a = f.getAnnotation(DBkey.class);
                if (a == null || !a.value().equals(Image.class)) {
                    throw new Exception("Base.update(): Field type mismatch for an Image");
                }
                int id = f.getInt(this);
                if (id == 0) {
                    ImageDummy idummy = u.getImageDummy();
                    if (this instanceof User) {
                        User uuser = (User) this;
                        Image i = Image.create(dao, null, user, "userImage_" + Base.getRandomLong(), idummy.getData(),
                                idummy.getMimeType(), idummy.getLicense());
                        uuser.setImage(i.getHistId());
                    } else if (this instanceof DBcategory) {
                        DBcategory cat = (DBcategory) this;
                        Image i = Image.create(dao, null, user, "catImage_" + Base.getRandomLong(), idummy.getData(),
                                idummy.getMimeType(), idummy.getLicense());
                        cat.setIcon(i.getHistId());
                    } else {
                        throw new Exception("Base.update(): ImageDummy for this relationship is't defined");
                    }
                } else {
                    Image.update(dao, user, id, u.getImageDummy().getImageUpdateMap());
                }
                break;
            }
            case IMAGE_DELETE: {
                DBkey a = f.getAnnotation(DBkey.class);
                if (a == null || !a.value().equals(Image.class)) {
                    throw new Exception("Base.update(): Field type mismatch for an Image");
                }
                Image.delete(dao, user, f.getInt(this));
                f.setInt(this, 0);
                break;
            }
            default:
                throw new Exception("Base.update(): Type not implemented!");
            }
        }
    }

    /**
     * Updates only embedded data types of an entity.
     * 
     * @param map
     *            map containing the changes
     * @param
     * @throws Exception
     */
    public void simpleUpdate(UpdateMap map, boolean strict) throws Exception {
        Class<?> c = this.getClass();

        for (Enum<?> field : map.getMap().keySet()) {
            Update u = map.get(field);
            Field f = c.getDeclaredField(field.name());
            f.setAccessible(true);
            switch (u.getType()) {
            case STRING:
                f.set(this, u.getString());
                break;
            case DOUBLE:
                f.set(this, u.getDouble());
                break;
            case FLOAT:
                f.set(this, u.getFloat());
                break;
            case LONG:
                f.set(this, u.getLong());
                break;
            case INT:
                f.set(this, u.getInt());
                break;
            case LAZY_BINARY:
            case BINARY:
                f.set(this, u.getBinary());
                break;
            case BOOLEAN:
                f.set(this, u.getBoolean());
                break;
            case DATE:
                f.set(this, u.getDate());
                break;
            case ENUM:
                f.set(this, u.getEnum());
                break;
            case BIGDECIMAL:
                f.set(this, u.getBigDecimal());
                break;
            default:
                if (strict) {
                    throw new Exception("Base.update(): Type not implemented!");
                }
            }
        }
    }

    /**
     * Counts the entries/table rows - DB related operation.
     * 
     * @param clazz
     *            class of the entity
     * @param status
     *            historisation status of the entity
     * @param where
     *            optional where clause (single statement)
     * @return entity count
     * @throws Exception
     */
    // TODO: Fix ugly where clause
    public static long countRow(DAOiface dao, Class<?> clazz, HistorizationIface.STATUS stat, String where)
            throws Exception {
        long size = 0;
        boolean localDAO = false;
        if (dao == null) {
            dao = DAO.getDAO();
            localDAO = true;
        }
        try {
            if (localDAO) {
                dao.beginTransaction();
            }
            StringBuffer buf = new StringBuffer("SELECT count(c) FROM ");
            buf.append(clazz.getSimpleName());
            buf.append(" c");
            HashMap<String, Object> map = null;
            if (stat != null) {
                map = new HashMap<String, Object>();
                map.put("status", stat);
                buf.append(" where c.status = :status");
            }
            if (where != null) {
                buf.append(" AND c." + where);
            }

            Object s = dao.query(buf.toString(), map).get(0);
            if (s instanceof BigInteger) {
                BigInteger bi = (BigInteger) s;
                size = bi.longValue();
            } else {
                size = (Long) s;
            }
            if (localDAO) {
                dao.endTransaction();
            }
        } catch (Exception e) {
            if (localDAO) {
                dao.rollback();
            }
            throw e;
        }
        return size;
    }

    /**
     * Field constraint - given text field of an entity must be unique for a
     * domain.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param update
     *            update data
     * @param domain
     *            {@code Domain} of the entity
     * @param dbID
     *            ID of the entity
     * @param fieldName
     *            text field name
     * @param value
     *            field value
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static void checkConstraintPerDomain(DAOiface dao, UpdateMap update, int domain, int dbID, Enum<?> fieldName,
            String value) throws Exception {
        List<Base> list = null;
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("domain", domain);
        map.put("status", at.treedb.db.HistorizationIface.STATUS.ACTIVE);
        String entry = fieldName.name();
        Class<?> clazz = fieldName.getDeclaringClass().getDeclaringClass();
        if (update == null) {
            map.put(entry, value);
            list = (List<Base>) dao.query("select i from " + clazz.getSimpleName()
                    + " i where i.domain = :domain and i." + entry + " = :" + entry + " and i.status = :status", map);
        } else {
            Update m = update.get(fieldName);
            if (m != null) {
                map.put("histId", dbID);
                map.put("name", m.getString());
                list = (List<Base>) dao
                        .query("select i from " + clazz.getSimpleName() + " i where i.domain = :domain and i." + entry
                                + " = :" + entry + " and i.status = :status and i.histId <> :histId", map);
            }
        }
        if (list != null && list.size() > 0) {
            throw new Exception(
                    clazz.getSimpleName() + ".checkConstraints(): Property " + entry + " isn't unique:" + value);
        }
    }

    /**
     * Field constraint - given text field of an entity must be unique over all
     * domains.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param update
     *            update data
     * @param dbID
     *            DB ID of the entity
     * @param fieldName
     *            text field name
     * @param value
     *            field value
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static void checkConstraint(DAOiface dao, UpdateMap update, int dbID, Enum<?> fieldName, String value)
            throws Exception {
        List<Base> list = null;
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("status", at.treedb.db.HistorizationIface.STATUS.ACTIVE);
        String entry = fieldName.name();
        Class<?> clazz = fieldName.getDeclaringClass().getDeclaringClass();
        if (update == null) {
            map.put(entry, value);
            list = (List<Base>) dao.query("select i from " + clazz.getSimpleName() + " i where i." + entry + " = :"
                    + entry + " and i.status = :status", map);
        } else {
            Update m = update.get(fieldName);
            if (m != null) {
                map.put("histId", dbID);
                map.put(entry, m.getString());
                list = (List<Base>) dao.query("select i from " + clazz.getSimpleName() + " i where i." + entry + " = :"
                        + entry + " and i.status = :status and i.histId <> :histId", map);
            }
        }
        if (list != null && list.size() > 0) {
            throw new Exception(
                    clazz.getSimpleName() + ".checkConstraints(): Property " + entry + " isn't unique:" + value);
        }
    }

    /**
     * Field constraint - given text field of a entity must be unique for a CI.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param update
     *            update data
     * @param domain
     *            {@code Domain} of the entity
     * @param ciID
     *            ID of the CI
     * @param dbID
     *            DB ID of the entity
     * @param fieldName
     *            text field name
     * @param value
     *            field value
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static void checkConstraintsPerCI(DAOiface dao, UpdateMap update, int domain, int ciID, int dbID,
            Enum<?> fieldName, String value) throws Exception {
        List<Base> list = null;
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("domain", domain);
        map.put("status", at.treedb.db.HistorizationIface.STATUS.ACTIVE);
        Class<?> clazz = fieldName.getDeclaringClass().getDeclaringClass();
        String entry = fieldName.name();
        if (update == null) {
            map.put(entry, value);
            map.put("ciId", ciID);
            list = (List<Base>) dao
                    .query("select i from " + clazz.getSimpleName() + " i where i.domain = :domain and i." + entry
                            + " = :" + entry + " and i.status = :status and i.ci = :ciId", map);
        } else {
            Update m = update.get(fieldName);
            if (m != null) {
                map.put("histId", dbID);
                map.put("ciId", ciID);
                map.put("name", m.getString());
                list = (List<Base>) dao.query(
                        "select i from " + clazz.getSimpleName() + " i where i.domain = :domain and i." + entry + " = :"
                                + entry + " and i.ci =:ciId and i.status = :status and i.histId <> :histId",
                        map);
            }
        }
        if (list != null && list.size() > 0) {
            throw new Exception(
                    clazz.getSimpleName() + ".checkConstraints(): Property " + entry + " isn't unique:" + value);
        }
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    protected static int queryAndExecute(DAOiface dao, String query, HashMap<String, Object> map) throws Exception {
        int count = 0;
        boolean localDAO = false;
        if (dao == null) {
            dao = DAO.getDAO();
            localDAO = true;
        }
        try {
            if (localDAO) {
                dao.beginTransaction();
            }
            count = dao.queryAndExecute(query, map);
            if (localDAO) {
                dao.endTransaction();
            }
        } catch (Exception e) {
            if (localDAO) {
                dao.rollback();
            }
            throw e;
        }
        return count;
    }

    @SuppressWarnings("unchecked")
    protected static List<Base> query(DAOiface dao, String query, HashMap<String, Object> map) throws Exception {
        List<Base> base = null;
        boolean localDAO = false;
        if (dao == null) {
            dao = DAO.getDAO();
            localDAO = true;
        }
        try {
            if (localDAO) {
                dao.beginTransaction();
            }
            base = (List<Base>) dao.query(query, map);
            if (localDAO) {
                dao.endTransaction();
            }
        } catch (Exception e) {
            if (localDAO) {
                dao.rollback();
            }
            throw e;
        }
        return base;
    }

    public static long getRandomLong() {
        long value = random.nextLong();
        if (value < 0) {
            value = -value;
        }
        return value;
    }

}
