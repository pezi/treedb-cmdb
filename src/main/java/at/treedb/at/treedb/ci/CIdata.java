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
import java.util.HashMap;
import java.util.List;

import javax.persistence.MappedSuperclass;

import at.treedb.db.Base;
import at.treedb.db.ClassID;
import at.treedb.db.ClassSelector;
import at.treedb.db.DAO;
import at.treedb.db.DAOiface;
import at.treedb.db.DBindex;
import at.treedb.db.DBkey;
import at.treedb.db.SearchCriteria;

/**
 * <p>
 * Base class of a {@code CI} data element.
 * </p>
 * 
 * @author Peter Sauer
 * 
 */
@SuppressWarnings("serial")
// LDOR: 25.08.2013
@MappedSuperclass
@DBindex(columnList = { "ci", "uiElement" })
public abstract class CIdata extends Base implements Cloneable, ClassSelector {
    public enum BaseFields {
        ciType
    }

    @DBkey(CI.class)
    private int ci; // data is element of this CI
    @DBkey(ClassSelector.class)
    private long uiElement; // data is element of this UI element
    @DBkey(CItype.class)
    private int ciType; // data belongs to a CI of this CI type - used for query
                        // operations

    protected CIdata() {
    }

    protected CIdata(int ci, int ciType, long uiElement) {
        setHistStatus(STATUS.ACTIVE);
        this.ci = ci;
        this.ciType = ciType;
        this.uiElement = uiElement;
    }

    /**
     * Returns the {@code CI} ID which is owner of this data element.
     * 
     * @return {@code CI} ID
     */
    public @DBkey(CI.class) int getCi() {
        return ci;
    }

    /**
     * Returns the {@code UIelement} ID which is owner of this data element.
     * 
     * @return {@code UIelement} ID
     */
    public @DBkey(ClassSelector.class) long getUiElement() {
        return uiElement;
    }

    /**
     * Returns the {@code CItype} ID of the {@code CI} which is owner of this
     * data element.
     * 
     * @return {@code CItype} ID
     */
    public @DBkey(CItype.class) int getCiType() {
        return ciType;
    }

    protected static Base load(DAOiface dao, Class<? extends Base> clazz, int ci, long uiElement, SearchCriteria crit,
            Date date) throws Exception {
        List<Base> list = loadList(dao, clazz, ci, uiElement, crit, date, false);
        if (list.size() == 1) {
            return list.get(0);
        } else if (list.size() > 1) {
            // 'Houston, we have a problem'
            throw new Exception("CIdata.load(): result set size > 1");
        }
        return null;
    }

    protected static Base load(DAOiface dao, Class<? extends Base> clazz, int domain, SearchCriteria crit, Date date)
            throws Exception {
        List<? extends Base> list = loadEntities(dao, clazz, domain, crit, date);
        if (list.size() == 1) {
            return list.get(0);
        } else if (list.size() > 1) {
            // 'Houston, we have a problem'
            throw new Exception("CIdata.load(): result set size > 1");
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    protected static List<Base> loadList(DAOiface dao, Class<? extends Base> clazz, int ci, long uiElement,
            SearchCriteria crit, Date date, boolean lazy) throws Exception {
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
            map.put("ci", ci);
            map.put("uiElement", uiElement);
            String criteria = "";
            if (crit != null) {
                String critName = crit.getEnumValue().name();
                criteria += " and data." + critName + " " + crit.getOperator().toString() + " :" + critName;
                map.put(critName, crit.getData());
            }
            // load only active entities
            String className = clazz.getSimpleName();
            if (date == null) {
                map.put("status", at.treedb.db.HistorizationIface.STATUS.ACTIVE);
                list = (List<Base>) dao.query("select data from " + className
                        + " data where data.ci = :ci and data.uiElement = :uiElement and data.status = :status"
                        + criteria, map);

            } else {
                map.put("date", date);
                list = (List<Base>) dao.query("select data from " + className
                        + " data where data.ci = :ci and data.uiElement = :uiElement and data.lastModified < :date and (data.deletionDate = null or data.deletionDate > :date) "
                        + criteria + "order by data.version having max(data.version)", map);
            }
            if (!lazy && !list.isEmpty() && list.get(0).isCallbackAfterLoad()) {
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

    @SuppressWarnings("unchecked")
    protected static List<Base> loadList(DAOiface dao, Class<? extends Base> clazz, int domain, SearchCriteria crit,
            Date date, boolean lazy) throws Exception {
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
            map.put("ci", 0);
            map.put("uiElement", 0L);
            String criteria = "";
            if (crit != null) {
                String critName = crit.getEnumValue().name();
                criteria += " and data." + critName + " " + crit.getOperator().toString() + " :" + critName;
                map.put(critName, crit.getData());
            }
            // load only active entities
            String className = clazz.getSimpleName();
            if (date == null) {
                map.put("status", at.treedb.db.HistorizationIface.STATUS.ACTIVE);
                list = (List<Base>) dao.query("select data from " + className
                        + " data where data.domain = :domain and data.ci = :ci and data.uiElement = :uiElement and data.status = :status"
                        + criteria, map);

            } else {
                map.put("date", date);
                list = (List<Base>) dao.query("select data from " + className
                        + " data data.domain = :domain and where data.ci = :ci and data.uiElement = :uiElement and data.lastModified < :date and (data.deletionDate = null or data.deletionDate > :date) "
                        + criteria + "order by data.version having max(data.version)", map);
            }
            if (!lazy && !list.isEmpty() && list.get(0).isCallbackAfterLoad()) {
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
     * Generic data access.
     * 
     * @return Object data value
     */
    abstract public Object getData();

    @Override
    public Class<?> getClass(Field f) {
        if (uiElement > 0) {
            return ClassID.classIDtoClass((int) (uiElement >> 32));
        }
        return null;
    }

}
