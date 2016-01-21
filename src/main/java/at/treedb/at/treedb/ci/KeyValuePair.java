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

package at.treedb.ci;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;

import at.treedb.db.Base;
import at.treedb.db.ClassID;
import at.treedb.db.ClassSelector;
import at.treedb.db.DAO;
import at.treedb.db.DAOiface;
import at.treedb.db.DBkey;
import at.treedb.db.UpdateMap;
import at.treedb.domain.Domain;
import at.treedb.user.User;

/**
 * <p>
 * Class for storing key/value pairs in context of a {@code Domain}. e.g. init
 * parameter for specific {@Domain} program code.
 * </p>
 * 
 * 
 * @author Peter Sauer
 *
 */
@SuppressWarnings("serial")
@Entity
public class KeyValuePair extends Base implements ClassSelector {
    @Column(name = "m_key")
    private String key;
    private String description;
    private ClassID classId;
    @DBkey(ClassSelector.class)
    private int ciDataId;
    private transient Object data;

    private static HashMap<Integer, HashMap<String, KeyValuePair>> cache = new HashMap<Integer, HashMap<String, KeyValuePair>>();

    /**
     * Field access name for updates by Java reflection.
     */
    public enum Fields {
        /**
         * reflection field name
         */
        key
    }

    protected KeyValuePair() {
    }

    public static void cacheDomain(Domain domain) {
        cache.put(domain.getHistId(), new HashMap<String, KeyValuePair>());
    }

    static {
        cacheDomain(Domain.getDummyDomain());
    }

    @Override
    public ClassID getCID() {
        return ClassID.KEYVALUEPAIR;
    }

    private KeyValuePair(String key, String description) {
        this.setHistStatus(STATUS.ACTIVE);
        this.key = key;
        this.description = description;
    }

    /**
     * Returns the optional description of the key/value pair.
     * 
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the class ID of the data value.
     * 
     * @return ClassID
     */
    public ClassID getClassId() {
        return classId;
    }

    static private void storeCache(Domain domain, KeyValuePair key) {
        if (domain == null) {
            domain = Domain.getDummyDomain();
        }
        HashMap<String, KeyValuePair> map = cache.get(domain);
        if (map != null) {
            map.put(key.getKey(), key);
        }
    }

    /**
     * Creates a {@code KeyValuePair} for a string value.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param user
     *            creator of the {@code  KeyValuePair}
     * @param domain
     *            {@code DAOiface} (data access object)
     * @param keyValue
     *            key value
     * @param description
     *            description of the key/value pair
     * @param value
     *            string value
     * @return {@code KeyValuePair} object
     * @throws Exception
     */
    static public KeyValuePair createOrUpdateStringValue(DAOiface dao, User user, Domain domain, String keyValue,
            String description, String value) throws Exception {
        boolean localDAO = false;
        if (dao == null) {
            dao = DAO.getDAO();
            localDAO = true;
        }
        try {
            if (localDAO) {
                dao.beginTransaction();
            }
            KeyValuePair key = serarchValue(dao, domain, keyValue);
            if (key == null) {
                CIstring str = CIstring.create(dao, domain, user, 0, 0, 0, value);
                key = new KeyValuePair(keyValue, description);
                key.classId = ClassID.CISTRING;
                key.ciDataId = str.getHistId();
                Base.save(dao, domain, user, key);
                key.data = value;
            } else {
                UpdateMap map = new UpdateMap(CIstring.Fields.class);
                map.addString(CIstring.Fields.text, value);
                CIstring.update(dao, user, key.ciDataId, CIstring.class, map);
            }
            if (localDAO) {
                dao.endTransaction();
            }
            storeCache(domain, key);
            return key;
        } catch (Exception e) {
            if (localDAO) {
                dao.rollback();
            }
            throw e;
        }

    }

    public String getKey() {
        return key;
    }

    /**
     * Creates a {@code KeyValuePair} for a long value.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param user
     *            creator of the {@code  KeyValuePair}
     * @param domain
     *            {@code DAOiface} (data access object)
     * @param keyValue
     *            key value
     * @param description
     *            description of the key/value pair
     * @param value
     *            long value
     * @return {@code KeyValuePair} object
     * @throws Exception
     */
    static public KeyValuePair createOrUpdateLongValue(DAOiface dao, User user, Domain domain, String keyValue,
            String description, long longValue) throws Exception {
        boolean localDAO = false;
        if (dao == null) {
            dao = DAO.getDAO();
            localDAO = true;
        }
        try {
            if (localDAO) {
                dao.beginTransaction();
            }
            KeyValuePair key = serarchValue(dao, domain, keyValue);
            if (key == null) {
                CIlong l = CIlong.create(dao, domain, user, 0, 0, 0, longValue);
                key = new KeyValuePair(keyValue, description);
                key.classId = ClassID.CILONG;
                key.ciDataId = l.getHistId();
                Base.save(dao, domain, user, key);
                key.data = longValue;
            } else {
                UpdateMap map = new UpdateMap(CIlong.Fields.class);
                map.addLong(CIlong.Fields.longValue, longValue);
                Base.update(dao, user, key.ciDataId, CIlong.class, map);
            }
            if (localDAO) {
                dao.endTransaction();
            }
            storeCache(domain, key);
            return key;
        } catch (Exception e) {
            if (localDAO) {
                dao.rollback();
            }
            throw e;
        }
    }

    /**
     * Creates a {@code KeyValuePair} for a double value.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param user
     *            creator of the {@code  KeyValuePair}
     * @param domain
     *            {@code DAOiface} (data access object)
     * @param keyValue
     *            key value
     * @param description
     *            description of the key/value pair
     * @param value
     *            double value
     * @return {@code KeyValuePair} object
     * @throws Exception
     */
    static public KeyValuePair createDoubleValue(DAOiface dao, User user, Domain domain, String keyValue,
            String description, double doubleValue) throws Exception {
        boolean localDAO = false;
        if (dao == null) {
            dao = DAO.getDAO();
            localDAO = true;
        }
        try {
            if (localDAO) {
                dao.beginTransaction();
            }
            KeyValuePair key = serarchValue(dao, domain, keyValue);
            if (key == null) {
                CIdouble d = CIdouble.create(dao, domain, user, 0, 0, 0, doubleValue);
                key = new KeyValuePair(keyValue, description);
                key.classId = ClassID.CIDOUBLE;
                key.ciDataId = d.getHistId();
                Base.save(dao, domain, user, key);
                key.data = doubleValue;
            } else {
                UpdateMap map = new UpdateMap(CIdouble.Fields.class);
                map.addDouble(CIdouble.Fields.doubleValue, doubleValue);
                CIdouble.update(dao, user, key.ciDataId, CIdouble.class, map);
            }
            if (localDAO) {
                dao.endTransaction();
            }
            storeCache(domain, key);
            return key;

        } catch (Exception e) {
            if (localDAO) {
                dao.rollback();
            }
            throw e;
        }
    }

    /**
     * Creates a {@code KeyValuePair} for a binary value.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param user
     *            creator of the {@code  KeyValuePair}
     * @param domain
     *            {@code DAOiface} (data access object)
     * @param keyValue
     *            key value
     * @param description
     *            description of the key/value pair
     * @param value
     *            binary value
     * @return {@code KeyValuePair} object
     * @throws Exception
     */
    static public KeyValuePair createOrUpdateBinaryValue(DAOiface dao, User user, Domain domain, String keyValue,
            String description, byte[] binaryValue) throws Exception {
        boolean localDAO = false;
        if (dao == null) {
            dao = DAO.getDAO();
            localDAO = true;
        }
        try {
            if (localDAO) {
                dao.beginTransaction();
            }
            KeyValuePair key = serarchValue(dao, domain, keyValue);
            if (key == null) {
                CIblob b = CIblob.create(dao, domain, user, 0, 0, 0, binaryValue);
                key = new KeyValuePair(keyValue, description);
                key.classId = ClassID.BLOB;
                key.ciDataId = b.getHistId();
                Base.save(dao, domain, user, key);
                key.data = binaryValue;
            } else {
                UpdateMap map = new UpdateMap(CIblob.Fields.class);
                map.addBinary(CIblob.Fields.binaryValue, binaryValue);
                CIblob.update(dao, user, key.ciDataId, CIblob.class, map);
            }
            if (localDAO) {
                dao.endTransaction();
            }
            storeCache(domain, key);
            return key;
        } catch (Exception e) {
            if (localDAO) {
                dao.rollback();
            }
            throw e;
        }
    }

    /**
     * Creates a {@code KeyValuePair} for a boolean value.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param user
     *            creator of the {@code  KeyValuePair}
     * @param domain
     *            {@code DAOiface} (data access object)
     * @param keyValue
     *            key value
     * @param description
     *            description of the key/value pair
     * @param value
     *            boolean value
     * @return {@code KeyValuePair} object
     * @throws Exception
     */
    static public KeyValuePair createOrUpdateBooleanValue(DAOiface dao, User user, Domain domain, String keyValue,
            String description, boolean booleanValue) throws Exception {
        boolean localDAO = false;
        if (dao == null) {
            dao = DAO.getDAO();
            localDAO = true;
        }
        try {
            if (localDAO) {
                dao.beginTransaction();
            }
            KeyValuePair key = serarchValue(dao, domain, keyValue);
            if (key == null) {
                CIboolean b = CIboolean.create(dao, domain, user, 0, 0, 0, booleanValue);
                key = new KeyValuePair(keyValue, description);
                key.classId = ClassID.CIBOOLEAN;
                key.ciDataId = b.getHistId();
                Base.save(dao, domain, user, key);
                key.data = booleanValue;
            } else {
                UpdateMap map = new UpdateMap(CIboolean.Fields.class);
                map.addBoolean(CIboolean.Fields.booleanValue, booleanValue);
                CIboolean.update(dao, user, key.ciDataId, CIboolean.class, map);
            }
            if (localDAO) {
                dao.endTransaction();
            }
            storeCache(domain, key);
            return key;
        } catch (Exception e) {
            if (localDAO) {
                dao.rollback();
            }
            throw e;
        }
    }

    /**
     * Creates a {@code KeyValuePair} for a date value.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param user
     *            creator of the {@code  KeyValuePair}
     * @param domain
     *            {@code DAOiface} (data access object)
     * @param keyValue
     *            key value
     * @param description
     *            description of the key/value pair
     * @param value
     *            date value
     * @return {@code KeyValuePair} object
     * @throws Exception
     */
    static public KeyValuePair createOrUpdateDateValue(DAOiface dao, User user, Domain domain, String keyValue,
            String description, Date dateValue) throws Exception {
        boolean localDAO = false;
        if (dao == null) {
            dao = DAO.getDAO();
            localDAO = true;
        }
        try {
            if (localDAO) {
                dao.beginTransaction();
            }
            KeyValuePair key = serarchValue(dao, domain, keyValue);
            if (key == null) {
                CIdate date = CIdate.create(dao, domain, user, 0, 0, 0, dateValue);
                key = new KeyValuePair(keyValue, description);
                key.classId = ClassID.CIDATE;
                key.ciDataId = date.getHistId();
                Base.save(dao, domain, user, key);
                key.data = date;
            } else {
                UpdateMap map = new UpdateMap(CIdate.Fields.class);
                map.addDate(CIdate.Fields.date, dateValue);
                CIdate.update(dao, user, key.ciDataId, CIdate.class, map);
            }
            storeCache(domain, key);
            if (localDAO) {
                dao.endTransaction();
            }
            return key;
        } catch (Exception e) {
            if (localDAO) {
                dao.rollback();
            }
            throw e;
        }
    }

    private void checkReturnValue(ClassID cid) throws Exception {
        if (classId != cid) {
            throw new Exception("KeyValue.getXXX: Type mismatch " + cid.name() + " != " + classId.name());
        }
    }

    /**
     * Returns a string value.
     * 
     * @return string value
     * @throws Exception
     */
    public String getString() throws Exception {
        checkReturnValue(ClassID.CISTRING);
        return (String) data;
    }

    /**
     * Returns a long value.
     * 
     * @return long value
     * @throws Exception
     */
    public Long getLong() throws Exception {
        checkReturnValue(ClassID.CILONG);
        return (Long) data;
    }

    /**
     * Returns a binary value.
     * 
     * @return binary value
     * @throws Exception
     */
    public byte[] getBinary() throws Exception {
        checkReturnValue(ClassID.CIBLOB);
        return (byte[]) data;
    }

    /**
     * Returns a boolean value.
     * 
     * @return boolean value
     * @throws Exception
     */
    public Boolean getBoolean() throws Exception {
        checkReturnValue(ClassID.CIBOOLEAN);
        return (Boolean) data;
    }

    /**
     * Returns a data value.
     * 
     * @return boolean value
     * @throws Exception
     */
    public Date getDate() throws Exception {
        checkReturnValue(ClassID.CIDATE);
        return (Date) data;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void callbackAfterLoad(DAOiface dao) throws Exception {
        CIdata cidata = (CIdata) CIdata.load(dao, (Class<? extends Base>) this.classId.toClass(), this.ciDataId);
        data = cidata.getData();
    }

    @Override
    public boolean isCallbackAfterLoad() {
        return true;
    }

    /**
     * Searches a value per key.
     * 
     * @param domain
     *            {@code Domain} of the data element
     * @param key
     *            key value
     * @return {@code KeyValuePair}, or {@code null} if the key doesn't exist
     * @throws Exception
     */
    public static KeyValuePair serarchValue(Domain domain, String key) throws Exception {
        return serarchValue(null, domain, key);
    }

    /**
     * Searches a value per key.
     * 
     * @param domain
     *            {@code Domain} of the data element
     * @param key
     *            key value
     * @return {@code KeyValuePair}, or {@code null} if the key doesn't exist
     * @throws Exception
     */
    public static KeyValuePair serarchValue(DAOiface dao, Domain domain, String key) throws Exception {
        HashMap<String, KeyValuePair> map = cache.get(domain == null ? Domain.getDummyDomain() : domain);
        if (map != null) {
            KeyValuePair k = map.get(key);
            if (k != null) {
                return k;
            }
        }

        List<Base> list = Base.search(dao, domain, KeyValuePair.class, EnumSet.of(KeyValuePair.Fields.key), key, null,
                null, null, true);
        if (list.isEmpty()) {
            return null;
        }
        KeyValuePair k = (KeyValuePair) list.get(0);
        storeCache(domain, k);
        ;
        return k;
    }

    @Override
    public Class<?> getClass(Field f) {
        return classId.toClass();
    }

}
