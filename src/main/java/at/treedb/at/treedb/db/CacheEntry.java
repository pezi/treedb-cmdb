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

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import at.treedb.domain.Domain;

/**
 * <p>
 * Container to store pre-cached data like thumbnails.
 * </p>
 * 
 * @author Peter Sauer
 */

@SuppressWarnings("serial")
@Entity
@Table(indexes = { @Index(columnList = "cacheId"), @Index(columnList = "refHistId"), @Index(columnList = "histId") })

public class CacheEntry extends Base implements Cloneable, ClassSelector {
    private final static long CACHE_LIFETIME = 24 * 60 * 60 * 1000L;

    public enum Fields {
        cacheId
    }

    private ClassID classId;
    @DBkey(ClassSelector.class)
    private int refHistId; // historization ID
    // create an DB index for the cacheId
    @Column(nullable = false)
    private String cacheId; // cache identifier
    // binary data
    @Detach
    @Lob
    @Column(nullable = false)

    private byte[] data; // binary class data
    private String info; // additional internal information
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUsed;

    protected CacheEntry() {
    }

    private CacheEntry(ClassID classId, int histId, String cacheId, byte[] data, String info) {
        this.setHistStatus(STATUS.ACTIVE);
        this.classId = classId;
        this.refHistId = histId;
        this.cacheId = cacheId;
        this.data = data;
        this.info = info;
        this.lastUsed = new Date();
    }

    /**
     * Creates a cache entry object.
     *
     * @param domain
     *            {@code Domain} of the cache entry
     * @param classId
     *            {@ClassID} of the cache entry
     * @param histID
     *            historiztion ID of the {@code Base} class associated with the
     *            cached data
     * @param cacheId
     *            unique cache data ID
     * @param data
     *            binary data
     * @return {@code CacheEntry} object
     * @throws Exception
     */
    static public CacheEntry create(Domain domain, ClassID classId, int histID, String cacheId, byte[] data,
            String info) throws Exception {
        CacheEntry entry = new CacheEntry(classId, histID, cacheId, data, info);
        Base.save(null, domain, null, entry);
        return entry;
    }

    /**
     * Creates a cache entry object.
     * 
     * @param dao
     *            {@code DAOiface} (data access object), can be null
     * @param domain
     *            {@code Domain} of the cache entry
     * @param classId
     *            {@ClassID} of the cache entry
     * @param histId
     *            historiztion ID of the {@code Base} class associated with the
     *            cached data
     * @param cacheId
     *            unique cache data ID
     * @param data
     *            binary data
     * @param info
     *            optional cache info
     * @param cacheLifeTime
     *            cache lifetime
     * @return {@code CacheEntry} object
     * @throws Exception
     */
    static public CacheEntry create(DAOiface dao, Domain domain, ClassID classId, int histId, String cacheId,
            byte[] data, String info) throws Exception {
        CacheEntry clazz = new CacheEntry(classId, histId, cacheId, data, info);
        Base.save(dao, domain, null, clazz);
        return clazz;
    }

    /**
     * Loads a cache entry.
     * 
     * @param dao
     *            {@code DAOiface} (data access object), can be {@code null}
     * @param domain
     *            {@code Domain} of the cache entry
     * @param classId
     *            {@ClassID} of the cache entry
     * @param histId
     *            historiztion ID of the {@code Base} class associated with the
     *            cached data
     * @param cacheId
     *            unique cache data ID
     * @return {@code CacheEntry} object
     * @throws Exception
     */
    public static CacheEntry load(DAOiface dao, Domain domain, ClassID classId, int histId, String cacheId)
            throws Exception {
        HashMap<String, Object> map = new HashMap<String, Object>();
        ;
        map.put("classId", classId);
        map.put("histId", histId);
        map.put("cacheId", cacheId);
        StringBuffer buf = new StringBuffer();
        buf.append("select c from ");
        buf.append(CacheEntry.class.getName());
        buf.append(" c where c.refHistId = :histId and c.classId = :classId and c.cacheId = :cacheId");
        List<Base> list = Base.query(dao, buf.toString(), map);
        if (list.size() == 1) {
            CacheEntry ce = (CacheEntry) list.get(0);
            if (ce.getLastModified().getTime() + CACHE_LIFETIME < System.currentTimeMillis()) {
                ce.updateLastUsed(dao);
            }
            return (CacheEntry) ce;
        }
        return null;
    }

    /**
     * Deletes cache entries.
     * 
     * @param dao
     *            {@code DAOiface} (data access object), can be null
     * @param domain
     *            {@code Domain} of the cache entry
     * @param classId
     *            {@ClassID} of the cache entry
     * @param histId
     *            historiztion ID of the {@code Base} class associated with the
     *            cached data
     * @param cacheId
     *            unique cache data ID
     * @param exactSearch
     *            {@code true} for an exact search, {@code false} for SQL LIKE
     *            search - e.g. delete different scaled versions of an image
     * @return count of deleted cache entries
     * @throws Exception
     */
    public static int dbDelete(DAOiface dao, Domain domain, ClassID classId, int histId, String cacheId,
            boolean exactSearch) throws Exception {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("domain", domain.getHistId());
        map.put("cacheId", cacheId);
        map.put("classId", classId);
        map.put("histId", histId);
        map.put("cacheId", cacheId);
        StringBuffer buf = new StringBuffer();
        buf.append("delete from ");
        buf.append(CacheEntry.class.getName());
        buf.append(" c where c.domain = :domain and c.classId = :classId and c.refHistId = :histId and c.cacheId ");
        if (exactSearch) {
            buf.append("=");
        } else {
            buf.append("like");
        }
        buf.append(" :cacheId");
        return Base.queryAndExecute(dao, buf.toString(), map);
    }

    public void updateLastUsed(DAOiface dao) throws Exception {
        this.setLastUsed();
        dao.update(this);
    }

    /**
     * Deletes all cache entries for a given {@code Base} historization ID
     * 
     * @param dao
     *            {@code DAOiface} (data access object), can be null
     * @param domain
     *            {@code Domain} of the cache entry
     * @param classId
     *            {@ClassID} of the cache entry
     * @param histId
     *            historiztion ID of the {@code Base} class associated with the
     *            cached data
     * @return count of deleted cache entries
     * @throws Exception
     */
    public static int dbDelete(DAOiface dao, Domain domain, ClassID classId, int histId) throws Exception {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("domain", domain.getHistId());
        ;
        map.put("classId", classId);
        map.put("histId", histId);
        StringBuffer buf = new StringBuffer();
        buf.append("delete from ");
        buf.append(CacheEntry.class.getName());
        buf.append(" c where c.domain = :domain and c.classId = :classId and c.refHistId = :histId");
        return Base.queryAndExecute(dao, buf.toString(), map);
    }

    /**
     * Returns the unique cache data ID.
     * 
     * @return cache data ID
     */
    public String getCacheId() {
        return cacheId;
    }

    /**
     * Returns the binary cache data.
     * 
     * @return binary cache data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Returns the additional cache info - e.g dimension of the thumbnail
     * 
     * @return cache info
     */
    public String getInfo() {
        return info;
    }

    /**
     * Creates a image dimension info string.
     * 
     * @param width
     *            image width
     * @param height
     *            image height
     * @return dimension info string
     */
    public static String createImageDimension(int width, int height) {
        return width + ":" + height;
    }

    /**
     * Returns the dimension of the image.
     * 
     * @return int[2] containg width and height
     */
    public int[] getImageDimension() {
        String[] dim = info.split(":");
        return new int[] { Integer.parseInt(dim[0]), Integer.parseInt(dim[1]) };
    }

    /**
     * Counts the {@code CacheEntry} entities per DB table row.
     * 
     * @return number of cache entries
     * @throws Exception
     */
    public static long rowCount() throws Exception {
        return Base.countRow(null, CacheEntry.class, HistorizationIface.STATUS.ACTIVE, null);
    }

    @Override
    public ClassID getCID() {
        return ClassID.CACHEENTRY;
    }

    @Override
    public Class<?> getClass(Field f) {
        return classId.toClass();
    }

    /**
     * Returns the last usage of the cache entry.
     * 
     * @return
     */
    public Date getLastUsed() {
        return lastUsed;
    }

    /**
     * Sets the last usage of the cache entry.
     */
    public void setLastUsed() {
        lastUsed = new Date();
    }

}
