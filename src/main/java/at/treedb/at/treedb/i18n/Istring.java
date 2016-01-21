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
package at.treedb.i18n;

import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;

import at.treedb.ci.CI;
import at.treedb.db.Base;
import at.treedb.db.ClassID;
import at.treedb.db.DAO;
import at.treedb.db.DAOiface;
import at.treedb.db.DBkey;
import at.treedb.db.HistorizationIface;
import at.treedb.db.SearchCriteria;
import at.treedb.db.SearchLimit;
import at.treedb.domain.Domain;
import at.treedb.user.User;

/**
 * Internationalization (i18n) support for storing language and country variants
 * of a text string. The class name is a short form of <i>i18n string</i>. In
 * this documentation this kind of string is called <i>language string</i>.
 * 
 * @author Peter Sauer
 */

@SuppressWarnings("serial")
@Entity
public class Istring extends Base implements Cloneable {
    public enum Fields {
        /**
         * reflection field name
         */
        text, language, ownerCID;
    }

    // two digit language code
    private Locale.LANGUAGE language;
    // two digit country
    private Locale.COUNTRY country;
    @DBkey(CI.class)
    public int ci; // data is element of this CI

    // text string
    // http://stackoverflow.com/questions/25885992/derby-a-truncation-error-was-encountered-trying-to-shrink-clob-stream-value
    // @Column(columnDefinition="clob")
    @Lob
    @Column(length = 10485760) // 10MB
    private String text;
    // optional class ID of the class which owns this Istring
    private ClassID ownerCID;

    protected Istring() {
        setHistStatus(STATUS.ACTIVE);
    }

    /**
     * Sets the language.
     * 
     * @param language
     *            language code
     */
    private void setLanguage(Locale.LANGUAGE language) {
        this.language = language;
    }

    /**
     * Returns the language.
     * 
     * @return language code
     */
    public Locale.LANGUAGE getLanguage() {
        return language;
    }

    /**
     * Sets the text.
     * 
     * @param text
     *            text string
     */
    private void setText(String text) {
        this.text = text;
    }

    /**
     * Returns the text.
     * 
     * @return text string
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the country.
     * 
     * @param country
     *            country code
     */
    private void setCountry(Locale.COUNTRY country) {
        this.country = country;
    }

    /**
     * Returns the country.
     * 
     * @return country code
     */
    public Locale.COUNTRY getCountry() {
        return country;
    }

    /**
     * Returns the group ID which sums up all languages and country variants of
     * a language string.
     * 
     * @return group ID
     */
    public int getGroupId() {
        return getHistId();
    }

    /**
     * Creates and saves a language string.
     * 
     * @param domain
     *            domain {@code Domain} of the data element
     * @param user
     *            {@code User} who creates the {@code Istring}
     * @param text
     *            language string
     * @param lang
     *            language code
     * @return {@code Istring}
     * @throws Exception
     */
    public static Istring create(Domain domain, User user, ClassID ownerID, String text, Locale.LANGUAGE lang)
            throws Exception {
        return create(null, domain != null ? domain.getHistId() : 0, user, ownerID, text, lang);
    }

    /**
     * Creates and saves a language string.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param domain
     *            domain {@code Domain} of the data element
     * @param user
     *            {@code User} who creates the {@code Istring}
     * @param text
     *            language string
     * @param lang
     *            language code
     * @return {@code Istring}
     * @throws Exception
     */
    public static Istring create(DAOiface dao, Domain domain, User user, ClassID ownerCID, String text,
            Locale.LANGUAGE lang) throws Exception {
        return create(dao, domain != null ? domain.getHistId() : 0, user, ownerCID, text, lang);
    }

    /**
     * Creates and saves a language string.
     * 
     * @param dao
     *            dao {@code DAOiface} (data access object)
     * @param domain
     *            {@code Domain} of the data element
     * @param text
     *            language string
     * @param lang
     *            language code
     * @return {@code Istring}
     * @throws Exception
     */
    public static Istring create(DAOiface dao, int domain, User user, ClassID ownerCID, String text,
            Locale.LANGUAGE lang) throws Exception {
        boolean isDAOlocal = false;
        // get DAO object?
        if (dao == null) {
            dao = DAO.getDAO();
            isDAOlocal = true;
        }
        Istring u = null;
        try {
            // if DAO is local, start transaction
            if (isDAOlocal) {
                dao.beginTransaction();
            }
            u = new Istring();
            Date d = new Date();
            u.setCreationTime(d);
            u.setLastModified(d);
            u.setText(text);
            if (ownerCID != null) {
                u.setOwnerCID(ownerCID);
            }
            u.setLanguage(lang);
            u.setCountry(null);
            u.setHistStatus(STATUS.ACTIVE);

            if (user != null) {
                u.setCreatedBy(user.getHistId());
                u.setModifiedBy(user.getHistId());
            }
            if (domain != 0) {
                u.setDomain(domain);
            }
            dao.saveAndFlushIfJPA(u);
            // dao.update(u);
            u.setHistId(u.getDBid());

            // if DAO is local - end transaction
            if (isDAOlocal) {
                dao.endTransaction();
            }
        } catch (Exception e) {
            if (isDAOlocal) {
                dao.rollback();
            }
            throw e;
        }
        return u;
    }

    /**
     * Updates a language string.
     * 
     * @param user
     *            {@code User} who updates the language string
     * @param text
     *            update text
     * @throws Exception
     */
    public void update(User user, String text) throws Exception {
        if (this.text.equals(text)) {
            return;
        }
        DAOiface dao = DAO.getDAO();
        try {
            dao.beginTransaction();

            Istring copy = this.clone();
            if (copy.getHistStatus() == STATUS.ACTIVE) {
                copy.setHistStatus(STATUS.UPDATED);

            }
            dao.save(copy);

            this.setHistStatus(STATUS.ACTIVE);
            this.setLastModified(new Date());
            this.setText(text);
            if (user != null) {
                this.setModifiedBy(user.getHistId());
            }
            this.incVersion();
            dao.update(this);
            dao.endTransaction();
        } catch (Exception e) {
            dao.rollback();
            throw e;
        }
    }

    /**
     * Saves, or updates a language string.
     * 
     * @param user
     *            {@code User} who updates the language string
     * @param text
     *            text
     * @param language
     *            language code
     * @param country
     *            country code
     * 
     * @return {@code Istring}
     * @throws Exception
     */
    public Istring saveOrUpdate(User user, String text, Locale.LANGUAGE language, Locale.COUNTRY country)
            throws Exception {
        DAOiface dao = DAO.getDAO();
        try {
            Istring istring;
            dao.beginTransaction();
            istring = Istring.saveOrUpdate(dao, this.getDomain(), user != null ? user.getHistId() : 0, this.getHistId(),
                    text, language, country, null);
            dao.endTransaction();
            return istring;
        } catch (Exception e) {
            dao.rollback();
            throw e;
        }
    }

    @Override
    public Istring clone() throws CloneNotSupportedException {
        Istring copy;
        copy = (Istring) super.clone();
        copy.setDBid(0);
        copy.resetTransactionVersion();
        return copy;

    }

    @SuppressWarnings("unchecked")
    public static Istring saveOrUpdate(DAOiface dao, int domain, int user, int id, String text,
            Locale.LANGUAGE language, Locale.COUNTRY country, ClassID cid) throws Exception {
        Istring u = null;
        List<Istring> list = null;
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("id", id);
        map.put("lang", language);
        String histStat = "";

        histStat = "and s.status <> :status";
        map.put("status", HistorizationIface.STATUS.UPDATED);

        if (country != null) {
            map.put("country", country);
            list = (List<Istring>) dao
                    .query("select s from Istring s where s.histId = :id and s.language = :lang and s.country = :country "
                            + histStat, map);
        } else {
            list = (List<Istring>) dao
                    .query("select s from Istring s where s.histId = :id and s.language = :lang and s.country is null "
                            + histStat, map);
        }
        if (list.size() > 0) {
            u = list.get(0);
            if (!u.text.equals(text)) {

                Istring copy = u.clone();
                if (copy.getHistStatus() == STATUS.ACTIVE) {
                    copy.setHistStatus(STATUS.UPDATED);
                }
                dao.save(copy);

                u.setHistStatus(STATUS.ACTIVE);
                u.setDeletionDate(null);
                u.setLastModified(new Date());
                u.setText(text);
                u.setModifiedBy(user);
                u.incVersion();
                dao.update(u);

            }
        } else {
            u = new Istring();
            Date d = new Date();
            u.setCreationTime(d);
            u.setLastModified(d);
            u.setCreatedBy(user);
            u.setModifiedBy(user);
            u.setText(text);
            u.setLanguage(language);
            u.setCountry(country);
            if (cid != null) {
                u.setOwnerCID(cid);
            }
            if (domain != 0) {
                u.setDomain(domain);
            }
            if (id != 0) {
                u.setHistId(id);
            }
            dao.save(u);
            if (id == 0) {
                dao.saveAndFlushIfJPA(u);
                u.setHistId(u.getDBid());
            }
        }

        return u;
    }

    /**
     * Loads a language/country specific string.
     * 
     * @param id
     *            language string ID
     * @param language
     *            language code
     * @param country
     *            country code
     * @return {@code Istring}
     * @throws Exception
     */
    public static Istring load(int id, Locale.LANGUAGE language, Locale.COUNTRY country) throws Exception {
        return load(null, id, language, country, null);
    }

    /**
     * Loads a language string.
     * 
     * @param id
     *            language string ID
     * @param language
     *            languager code
     * @return {@code Istring}
     * @throws Exception
     */
    public static Istring load(int id, Locale.LANGUAGE language) throws Exception {
        return load(null, id, language, null, null);
    }

    public static Istring load(DAOiface dao, int id, Locale.LANGUAGE language) throws Exception {
        return load(dao, id, language, null, null);
    }

    /**
     * Loads a language string.
     * 
     * @param id
     *            language string ID
     * @param language
     *            language code
     * @return {@code Istring} object
     * @throws Exception
     */
    public static Istring load(int id, Locale.LANGUAGE language, Date date) throws Exception {
        return load(null, id, language, null, date);
    }

    /**
     * Loads a language string.
     * 
     * @param id
     *            language string ID
     * @param language
     *            language code
     * @param country
     *            country code
     * @param date
     *            temporal bound
     * @return {@code Istring} object
     * @throws Exception
     */
    public static Istring load(int id, Locale.LANGUAGE language, Locale.COUNTRY country, Date date) throws Exception {
        return load(null, id, language, country, date);
    }

    /**
     * Loads a language string.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param id
     *            language string ID
     * @param language
     *            language, two character code
     * @param country
     *            country, two character code country code
     * @param date
     *            temporal bound
     * @return {code IString}
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static Istring load(DAOiface dao, int id, Locale.LANGUAGE language, Locale.COUNTRY country, Date date)
            throws Exception {
        if (language == null) {
            throw new Exception("Istring.load(): Parameter language can't be null!");
        }
        boolean isDAOlocal = false;
        if (dao == null) {
            dao = DAO.getDAO();
            isDAOlocal = true;
        }
        try {
            List<Istring> list = null;
            Istring istring = null;
            if (isDAOlocal) {
                dao.beginTransaction();
            }
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("id", id);
            map.put("status", at.treedb.db.HistorizationIface.STATUS.ACTIVE);
            map.put("lang", language);

            // load the string using the primary id
            String cstmnt;
            int limit = 1;
            if (country == null) {
                cstmnt = "s.country is null";
            } else {
                cstmnt = "(s.country = :country or s.country is null)";
                map.put("country", country);
                limit = 2;
            }

            if (date == null) {
                list = (List<Istring>) dao
                        .query("select s from Istring s where s.histId = :id and s.language = :lang and " + cstmnt
                                + " and s.status = :status", map);
                // one or two strings a possible
                if (list.size() == 1) {
                    istring = list.get(0);
                } else if (list.size() != 0) {
                    if (list.size() != 2) {
                        throw new Exception(
                                "Istring.load(): Internal error, DB result missmatch! Result size: " + list.size());
                    }
                    if (list.get(0).getCountry() != null) {
                        istring = list.get(0);
                    } else {
                        istring = list.get(1);
                    }

                }
            } else {
                map.put("date", date);
                list = (List<Istring>) dao.query(
                        "select s from Istring s where s.histId = :id and s.language = :lang and " + cstmnt
                                + " and s.lastModified < :date and (s.deletionDate = null or s.deletionDate > :date) order by s.version desc",
                        0, limit, map);
                // one or two strings are possible
                if (list.size() > 0) {
                    if (list.size() == 1) {
                        istring = list.get(0);
                    } else {
                        if (list.get(0).getCountry() != null) {
                            istring = list.get(0);
                        } else {
                            istring = list.get(1);
                        }
                    }

                }
            }

            if (isDAOlocal) {
                dao.endTransaction();
            }
            return istring;
        } catch (Exception e) {
            e.printStackTrace();
            if (isDAOlocal) {
                dao.endTransaction();
                dao.rollback();
            }
            throw e;
        }
    }

    /**
     * Loads all language and country variants of a string.
     * 
     * @param id
     *            language string ID
     * @return list of all language and country variants
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static List<Istring> loadAllStrings(int id) throws Exception {
        DAOiface dao = DAO.getDAO();
        List<Istring> list = null;
        try {
            dao.beginTransaction();
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("id", id);
            map.put("status", at.treedb.db.HistorizationIface.STATUS.ACTIVE);
            list = (List<Istring>) dao.query("select s from Istring s where s.histId = :id and s.status = :status",
                    map);
            list.size();
            dao.endTransaction();
        } catch (Exception e) {
            dao.endTransaction();
            dao.rollback();
            throw e;
        }
        if (list.size() == 0) {
            return null;
        }
        return list;
    }

    /**
     * Deletes all language and country variants of a language string.
     * 
     * @param user
     *            {@code User} who deletes the language string
     * @param id
     *            language string ID
     * @return number of deleted language strings
     * @throws Exception
     */
    public static int delete(User user, int id) throws Exception {
        DAOiface dao = DAO.getDAO();
        int count = 0;
        try {
            dao.beginTransaction();
            count = delete(dao, user, id);
            dao.endTransaction();
        } catch (Exception e) {
            dao.rollback();
            throw e;
        }
        return count;
    }

    /**
     * Deletes all language and country variants of a language string.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param user
     *            {@code User} who deletes the language string
     * @param id
     *            language string ID
     * @return number of deleted strings
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static int delete(DAOiface dao, User user, int id) throws Exception {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("id", id);
        map.put("status", at.treedb.db.HistorizationIface.STATUS.ACTIVE);
        List<Istring> list = (List<Istring>) dao
                .query("select s from Istring s where s.histId = :id and s.status = :status", map);
        int count = list.size();
        for (Istring s : list) {

            s.setHistStatus(STATUS.DELETED);
            s.setDeletionDate(new Date());
            if (user != null) {
                s.setModifiedBy(user.getHistId());
            }
            dao.update(s);

        }
        return count;
    }

    /**
     * Deletes a language with all country variant of a language string.
     * 
     * @param user
     *            {@code User} who deletes the language string
     * @param id
     *            language string ID
     * @param language
     *            language code e.g. de
     * @return number of deleted strings
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static int delete(User user, int id, Locale.LANGUAGE language) throws Exception {
        DAOiface dao = DAO.getDAO();
        List<Istring> list = null;
        int count = 0;
        try {
            dao.beginTransaction();
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("id", id);
            map.put("status", at.treedb.db.HistorizationIface.STATUS.ACTIVE);
            map.put("lang", language);
            list = (List<Istring>) dao.query(
                    "select s from Istring s where s.histId = :id and s.language = :lang and s.status = :status", map);
            count = list.size();
            for (Istring s : list) {

                s.setHistStatus(STATUS.DELETED);
                s.setDeletionDate(new Date());
                if (user != null) {
                    s.setModifiedBy(user.getHistId());
                }
                dao.update(s);

            }
            dao.endTransaction();
        } catch (Exception e) {
            dao.rollback();
            throw e;
        }
        return count;
    }

    /**
     * Deletes the country variant of a language of a language string.
     * 
     * @param user
     *            {@code User} who deletes the language string
     * @param id
     *            language string ID
     * @param language
     *            language code e.g. de
     * @param country
     *            country code e.g. at
     * @return number of deleted strings
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static int delete(User user, int id, Locale.LANGUAGE language, Locale.COUNTRY country) throws Exception {
        DAOiface dao = DAO.getDAO();
        List<Istring> list = null;
        int deleted = 0;
        try {
            dao.beginTransaction();
            if (country != null) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("id", id);
                map.put("status", at.treedb.db.HistorizationIface.STATUS.ACTIVE);
                map.put("lang", language);
                map.put("country", country);
                list = (List<Istring>) dao.query(
                        "select s from Istring s where s.histId = :id and s.language = :lang and s.country = :country and s.status = :status",
                        map);
            }
            deleted = list.size();
            for (Istring s : list) {

                s.setHistStatus(STATUS.DELETED);
                s.setDeletionDate(new Date());
                if (user != null) {
                    s.setModifiedBy(user.getHistId());
                }
                dao.update(s);

            }
        } catch (Exception e) {
            dao.rollback();
            throw e;
        }
        return deleted;
    }

    /**
     * Deletes the language variant of a language string.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param user
     *            {@code User} who deletes the language string
     * @param id
     *            language string ID
     * @param language
     *            language code e.g. de
     * @return number of deleted strings
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static int delete(DAOiface dao, User user, int id, Locale.LANGUAGE language) throws Exception {
        List<Istring> list = null;
        int count = 0;

        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("id", id);
        map.put("status", at.treedb.db.HistorizationIface.STATUS.ACTIVE);
        map.put("lang", language);
        list = (List<Istring>) dao.query(
                "select s from Istring s where s.histId = :id and s.language = :lang and s.status = :status", map);
        count = list.size();
        for (Istring s : list) {

            dao.update(s);
            s.setHistStatus(STATUS.DELETED);
            s.setDeletionDate(new Date());
            if (user != null) {
                s.setModifiedBy(user.getHistId());
            }
        }
        return count;
    }

    /**
     * Deletes the language variant of a language string.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param user
     *            {@code User} who deletes the language string
     * @param id
     *            language string ID
     * @param language
     *            language code e.g. de
     * @param country
     *            country code e.g. at
     * @return number of deleted strings
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static int delete(DAOiface dao, User user, int id, Locale.LANGUAGE language, Locale.COUNTRY country)
            throws Exception {
        List<Istring> list = null;
        int deleted = 0;
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("id", id);
        map.put("status", at.treedb.db.HistorizationIface.STATUS.ACTIVE);
        map.put("lang", language);
        map.put("country", country);
        if (country != null) {
            list = (List<Istring>) dao.query(
                    "select s from Istring s where s.histId = :id and s.language = :lang and s.country = :country and s.status = :status",
                    map);
        }
        deleted = list.size();
        for (Istring s : list) {
            s.setHistStatus(STATUS.DELETED);
            s.setDeletionDate(new Date());
            if (user != null) {
                s.setModifiedBy(user.getHistId());
            }
            dao.update(s);
        }
        return deleted;
    }

    /**
     * Deletes a language string from DB explicit without historization.
     * 
     * @param user
     *            {@code User} who deletes the language string, can be null
     * @param istring
     *            language string
     * @throws Exception
     */
    public static void dbDelete(User user, Istring istring) throws Exception {
        Base.delete(user, istring, true);
    }

    /**
     * Deletes a language string from DB explicit without historization.
     * 
     * @param user
     *            {@code User} who deletes the language string, can be null
     * @param id
     *            language string ID
     * @throws Exception
     */
    public static void dbDelete(User user, int id) throws Exception {
        Base.delete(user, null, false);
    }

    /**
     * Counts all language & country variants of a string.
     * 
     * @param id
     *            language string ID
     * @return language & country variants of a string
     * @throws Exception
     */
    public long count(int id) throws Exception {
        long count = 0;
        DAOiface dao = DAO.getDAO();
        try {
            dao.beginTransaction();
            count = count(dao, id);
            dao.endTransaction();
        } catch (Exception e) {
            dao.rollback();
            throw e;
        }
        return count;
    }

    /**
     * Counts all language & country variants of a string.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param id
     *            language string ID
     * @return language & country variants of a string
     * @throws Exception
     */
    public long count(DAOiface dao, int id) throws Exception {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("id", id);
        @SuppressWarnings("unchecked")
        List<Long> remaining = (List<Long>) dao.query("select count(*) from Istring where histID = :id", map);
        return remaining.get(0).intValue();

    }

    /**
     * Counts the {@code Istring} entities/table rows.
     * 
     * @param status
     *            search filter representing the user's historization status.
     *            <code>null<null> counts all {@code Istring} DB entries.
     * @return count of {@code Istring} entities
     * @throws Exception
     */
    public static long rowCount(HistorizationIface.STATUS status) throws Exception {
        return Base.countRow(null, Istring.class, status, null);
    }

    /**
     * Sets the owner class of the {@code Istring}
     * 
     * @param clazz
     *            owner class
     */
    public void setOwnerCID(ClassID clazz) {
        ownerCID = clazz;
    }

    static public List<Base> search(Domain domain, String value, Locale.LANGUAGE language, ClassID ownerCID,
            EnumSet<Base.Search> flags, SearchLimit limit) throws Exception {
        return Base.search(domain,
                Istring.class, EnumSet.of(Fields.text), value, new SearchCriteria[] {
                        new SearchCriteria(Fields.language, language), new SearchCriteria(Fields.ownerCID, ownerCID) },
                flags, limit, false);
    }

    /**
     * Returns the owner class of the {@code Istring}
     * 
     * @return owner class
     */
    public ClassID getOwnerCID(ClassID id) {
        return ownerCID;
    }

    @Override
    public ClassID getCID() {
        return ClassID.ISTRING;
    }

    public int getCI() {
        return ci;
    }

    public void setCI(int ciID) {
        ci = ciID;
    }
}
