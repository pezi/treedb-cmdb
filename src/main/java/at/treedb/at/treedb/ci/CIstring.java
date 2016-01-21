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

import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.Table;

import at.treedb.db.Base;
import at.treedb.db.ClassID;
import at.treedb.db.ClassSelector;
import at.treedb.db.DAO;
import at.treedb.db.DAOiface;
import at.treedb.db.DBkey;
import at.treedb.db.SearchCriteria;
import at.treedb.db.SearchLimit;
import at.treedb.db.UpdateMap;
import at.treedb.domain.Domain;
import at.treedb.user.User;

/**
 * <p>
 * {@code CI} data container for a text.
 * </p>
 * 
 * @author Peter Sauer
 * 
 */
@SuppressWarnings("serial")
// LDR: 12.12.2013
@Entity
@Table(indexes = { @Index(columnList = "ci"), @Index(columnList = "uiElement"), @Index(columnList = "histId") })
public class CIstring extends CIdata {
    @Lob
    @Column(length = 10485760) // 10MB
    private String text;

    /**
     * Field access name for updates by Java reflection.
     */
    public enum Fields {
        /**
         * reflection field name
         */
        text;
    }

    protected CIstring() {
    }

    private CIstring(int ci, int ciType, long uiElement, String text) {
        super(ci, ciType, uiElement);
        this.text = text;
    }

    /**
     * Creates a container for a string value.
     * 
     * @param dao
     *            {@code DAOiface} data access object
     * @param domain
     *            {@code Domain} of the data element
     * @param user
     *            creator of the {@code CI}
     * @param ci
     *            ID of the {@code CI}
     * @param uiElement
     *            ID of the {@code UIelement}
     * @param text
     *            string value
     * @return {@code CIstring} object
     * @throws Exception
     */
    public static CIstring create(DAOiface dao, Domain domain, User user, @DBkey(CI.class) int ci,
            @DBkey(CItype.class) int ciType, @DBkey(ClassSelector.class) long uiElement, String text) throws Exception {
        CIstring s = new CIstring(ci, ciType, uiElement, text);
        Base.save(dao, domain, user, s);
        return s;
    }

    /**
     * Creates or updates a {@code CIstring}
     * 
     * @param dao
     *            {@code DAOiface} data access object
     * @param domain
     *            {@code Domain} of the data element
     * @param user
     *            user {@code User} who performs the object update/creation
     * 
     * @param ci
     *            ID of the {@code CI}
     * @param ciType
     *            ID of the {@code CItype}
     * @param uiElement
     *            ID of the {@code UIelement}
     * @param text
     *            text value
     * @return {@code CIstring} object
     * @throws Exception
     */
    public static CIstring createOrUpdate(DAOiface dao, Domain domain, User user, @DBkey(CI.class) int ci,
            @DBkey(CItype.class) int ciType, @DBkey(ClassSelector.class) long uiElement, String text) throws Exception {
        CIstring s = (CIstring) load(dao, CIstring.class, ci, uiElement, null, null);
        if (s == null) {
            s = new CIstring(ci, ciType, uiElement, text);
            Base.save(dao, domain, user, s);
        } else {
            UpdateMap map = new UpdateMap(CIstring.Fields.class);
            map.addString(CIstring.Fields.text, text);
            Base.update(dao, user, s, map);
        }
        return s;
    }

    /**
     * Loads a {@code CIstring}.
     * 
     * @param id
     *            ID of the {@code CIstring}
     * @return {@code CIstring} object
     * @throws Exception
     */
    public static CIstring load(@DBkey(CIstring.class) int id) throws Exception {
        return (CIstring) Base.load(null, CIstring.class, id, null);

    }

    /**
     * Loads a {@code CIstring}.
     * 
     * @param ci
     *            ID of the {@code CI}
     * @param uiElement
     *            ID of the {@code UIelement}
     * @param date
     *            temporal bound
     * @return {@code CIstring} object
     * @throws Exception
     */
    public static CIstring load(@DBkey(CI.class) int ci, @DBkey(ClassSelector.class) long uiElement, Date date)
            throws Exception {
        return (CIstring) CIdata.load(null, CIstring.class, ci, uiElement, null, date);
    }

    /**
     * Loads a {@code CIstring}.
     * 
     * @param dao
     *            {@code DAOiface} data access object
     * @param ci
     *            ID of the {@code CI}
     * @param ui
     *            ID of the {@code UIelement}
     * @param date
     *            temporal bound
     * @return {@code CIstring} object
     * @throws Exception
     */
    public static CIstring load(DAOiface dao, @DBkey(CI.class) int ci, @DBkey(ClassSelector.class) long uiElement,
            Date date) throws Exception {
        return (CIstring) CIdata.load(dao, CIstring.class, ci, uiElement, null, date);
    }

    /**
     * Returns a text string.
     * 
     * @return text
     */
    public String getData() {
        return text;
    }

    /**
     * Updates a {@code CIstring}.
     * 
     * @param user
     *            user {@code User} which performs the {@code CIstring} update
     * @param text
     *            text
     * @throws Exception
     */
    public void update(User user, String text) throws Exception {
        UpdateMap map = new UpdateMap(CIstring.Fields.class);
        map.addString(CIstring.Fields.text, text);
        Base.update(user, this, map);
    }

    /**
     * Searches a text string.
     * 
     * @param domain
     *            {@code Domain} of the data element
     * @param pattern
     *            search pattern
     * @param criteria
     *            list of search criteria
     * @param flags
     *            search flags
     * @param limit
     *            search limit
     * @return result list of strings matching the search criteria
     * @throws Exception
     */
    static public List<Base> search(Domain domain, String pattern, SearchCriteria[] criteria,
            EnumSet<Base.Search> flags, SearchLimit limit) throws Exception {
        return Base.search(domain, CIstring.class, EnumSet.of(CIstring.Fields.text), pattern, criteria, flags, limit,
                false);
    }

    /**
     * Searches a text string.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param ciType
     *            ID of the {@code CItype}
     * @param search
     *            search pattern
     * @param date
     *            temporal bound
     * @return result list
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static List<Base> search(DAOiface dao, @DBkey(CItype.class) int ciType, String search, Date date)
            throws Exception {
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
            map.put("ciType", ciType);
            map.put("search", search);
            // load only active entities
            if (date == null) {
                map.put("status", at.treedb.db.HistorizationIface.STATUS.ACTIVE);
                list = (List<Base>) dao.query(
                        "select data from " + CIstring.class.getSimpleName()
                                + " data where data.ciType = :ciType and data.status = :status and data.text like :search",
                        map);
            } else {
                map.put("date", date);
                list = (List<Base>) dao.query("select data from " + CIstring.class.getSimpleName()
                        + " data where data.ci = :ci and data.uiElement = :uiElement and data.lastModified < :date and (data.deletionDate = null or data.deletionDate > :date) "
                        + "and data.text like :search order by data.version having max(data.version)", map);
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

    @Override
    public ClassID getCID() {
        return ClassID.CISTRING;
    }

}
