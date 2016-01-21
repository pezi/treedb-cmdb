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
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.Table;

import at.treedb.db.Base;
import at.treedb.db.ClassID;
import at.treedb.db.ClassSelector;
import at.treedb.db.DAOiface;
import at.treedb.db.DBkey;
import at.treedb.db.SearchCriteria;
import at.treedb.db.SearchLimit;
import at.treedb.db.UpdateMap;
import at.treedb.domain.Domain;
import at.treedb.i18n.Locale;
import at.treedb.user.User;

/**
 * <p>
 * {@code CI} data container for a i18n string.
 * </p>
 * 
 * @author Peter Sauer
 * 
 */
@SuppressWarnings("serial")
// LDR: 12.12.2013
@Entity
@Table(indexes = { @Index(columnList = "ci"), @Index(columnList = "uiElement"), @Index(columnList = "histId") })
public class CIi18nString extends CIdata {
    // http://stackoverflow.com/questions/25885992/derby-a-truncation-error-was-encountered-trying-to-shrink-clob-stream-value
    // @Column(columnDefinition="clob")
    // solves the problem for Hibernate/Derby - but conflicts with other DBs
    @Lob
    @Column(length = 10485760) // 10MB
    private String text;
    private Locale.LANGUAGE language;

    /**
     * Field access name for updates by Java reflection.
     */
    public enum Fields {
        /**
         * reflection field name
         */
        text, language;
    }

    protected CIi18nString() {
    }

    private CIi18nString(int ci, int ciType, long uiElement, String text, Locale.LANGUAGE language) {
        super(ci, ciType, uiElement);
        this.text = text;
        this.language = language;
    }

    public Locale.LANGUAGE getLanguage() {
        return language;
    }

    /**
     * Creates a container for an i18n string.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param domain
     *            {@code Domain}
     * @param user
     *            creator of the {@code CIi18nString}
     * @param ci
     *            ID of the {@code CI}
     * @param ciType
     *            ID of the {@code CItype}
     * @param uiElement
     *            ID of the {@code UIelement}
     * @param text
     *            text string
     * @param language
     *            language code
     * @return {@code CIi18nString} object
     * @throws Exception
     */
    public static CIi18nString createOrUpdate(DAOiface dao, Domain domain, User user, @DBkey(CI.class) int ci,
            @DBkey(CItype.class) int ciType, @DBkey(ClassSelector.class) long uiElement, String text,
            Locale.LANGUAGE language) throws Exception {
        CIi18nString s = (CIi18nString) load(dao, CIi18nString.class, ci, uiElement,
                new SearchCriteria(Fields.language, language), null);
        if (s == null) {
            s = new CIi18nString(ci, ciType, uiElement, text, language);
            Base.save(dao, domain, user, s);
        } else {
            UpdateMap map = new UpdateMap(CIi18nString.Fields.class);
            map.add(CIi18nString.Fields.text, text);
            Base.update(dao, user, s, map);
        }
        return s;
    }

    /**
     * Loads a {@code CIi18nString}.
     * 
     * @param id
     *            ID of the {@code CIi18nString}
     * @return {@code CIi18nString} object
     * @throws Exception
     */
    public static CIi18nString load(@DBkey(CIi18nString.class) int id) throws Exception {
        return (CIi18nString) Base.load(null, CIi18nString.class, id, null);

    }

    /**
     * Loads a {@code CIi18nString}.
     * 
     * @param ci
     *            ID of the {@code CI}
     * @param uiElement
     *            ID of the {@code UIelement}
     * @param language
     *            language
     * @param date
     *            temporal bound
     * @return {@code CIi18nString} object
     * @throws Exception
     */
    public static CIi18nString load(@DBkey(CI.class) int ci, @DBkey(ClassSelector.class) long uiElement,
            Locale.LANGUAGE language, Date date) throws Exception {
        return (CIi18nString) CIdata.load(null, CIi18nString.class, ci, uiElement,
                new SearchCriteria(Fields.language, language), date);
    }

    /**
     * Loads a {@code CIi18nString}.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param ci
     *            ID of the {@code CI}
     * @param uiElement
     *            ID of the {@code UIelement}
     * @param language
     *            language code
     * @param date
     *            temporal bound
     * @return {@code CIi18nString} object
     * @throws Exception
     */
    public static CIi18nString load(DAOiface dao, @DBkey(CI.class) int ci, @DBkey(ClassSelector.class) long uiElement,
            Locale.LANGUAGE language, Date date) throws Exception {
        return (CIi18nString) CIdata.load(dao, CIi18nString.class, ci, uiElement,
                new SearchCriteria(Fields.language, language), date);
    }

    /**
     * Returns an i18n string.
     * 
     * @return i18n string
     */
    public String getData() {
        return text;
    }

    /**
     * Updates a {@code CIi18nString}.
     * 
     * @param user
     *            user
     * @param text
     *            text value
     * @throws Exception
     */
    public void update(User user, String text) throws Exception {
        UpdateMap map = new UpdateMap(CIi18nString.Fields.class);
        map.addString(CIi18nString.Fields.text, text);
        Base.update(user, this, map);
    }

    /**
     * Searches an i18n string.
     * 
     * @param domain
     *            {@code Domain}
     * @param value
     *            search pattern
     * @param flags
     *            search flags
     * @param limit
     *            result set limitation in dependency of the flag
     *            {@link at.treedb.db.Base.Search}
     * @return result set
     * @throws Exception
     */
    static public List<Base> search(Domain domain, String value, Locale.LANGUAGE language, EnumSet<Base.Search> flags,
            SearchLimit limit) throws Exception {
        return Base.search(domain, CIi18nString.class, EnumSet.of(Fields.text), value,
                new SearchCriteria[] { new SearchCriteria(Fields.language, language) }, flags, limit, false);
    }

    @Override
    public ClassID getCID() {
        return ClassID.CII18NSTRING;
    }

}
