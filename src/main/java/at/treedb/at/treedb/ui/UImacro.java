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
package at.treedb.ui;

import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;

import at.treedb.ci.CIdata;
import at.treedb.db.Base;
import at.treedb.db.ClassID;
import at.treedb.db.DAOiface;
import at.treedb.db.SearchCriteria;
import at.treedb.db.SearchLimit;
import at.treedb.db.UpdateMap;
import at.treedb.domain.Domain;
import at.treedb.i18n.Locale;
import at.treedb.user.User;

/**
 * 
 * <p>
 * Text macro
 * </p>
 * 
 * @author Peter Sauer
 *
 */
@SuppressWarnings("serial")
@Entity
public class UImacro extends CIdata {

    private Locale.LOCALE locale;
    private String name;
    private String description;
    @Lob
    @Column(nullable = false, length = 5242880) // 5MB
    private String macro;

    public enum Fields {
        name, macro, locale, description;
    }

    protected UImacro() {
    }

    private UImacro(int ci, int ciType, long uiElement, String name, Locale.LOCALE locale, String macro,
            String description) {
        super(ci, ciType, uiElement);
        this.locale = locale;
        ;
        this.name = name;
        this.macro = macro;
        this.description = description;
    }

    /**
     * Creates a text macro.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param domain
     *            {@code Domain} of the data element
     * @param user
     *            creator of the {@code UImacro}
     * @param ci
     *            ID of the {@code CI}
     * @param ciType
     *            ID of the {@code CItype}
     * @param uiElement
     *            ID of the {@code UIelement}
     * @param name
     *            name of the macro
     * @param macro
     *            macro text
     * @return {@code UImacro}
     * @throws Exception
     */
    public static UImacro create(DAOiface dao, Domain domain, User user, int ci, int ciType, long uiElement,
            String name, Locale.LOCALE locale, String macro, String description) throws Exception {
        UImacro s = new UImacro(ci, ciType, uiElement, name, locale, macro, description);
        Base.save(dao, domain, user, s);
        return s;
    }

    /**
     * Creates or updates a text macro.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param domain
     *            {@code Domain} of the data element
     * @param user
     *            creator of the {@code UImacro}
     * @param ci
     *            ID of the {@code CI}
     * @param ciType
     *            ID of the {@code CItype}
     * @param uiElement
     *            ID of the {@code UIelement}
     * @param name
     *            name of the macro
     * @param macro
     *            macro text
     * @return {@code UImacro}
     * @throws Exception
     */
    public static UImacro createOrUpdate(DAOiface dao, Domain domain, User user, int ci, int ciType, long uiElement,
            String name, Locale.LOCALE locale, String macro, String description) throws Exception {
        UImacro s = (UImacro) load(dao, UImacro.class, ci, uiElement, new SearchCriteria(UImacro.Fields.locale, locale),
                null);
        if (s == null) {
            s = new UImacro(ci, ciType, uiElement, name, locale, macro, description);
            Base.save(dao, domain, user, s);
        } else {
            UpdateMap map = new UpdateMap(UImacro.Fields.class);
            map.addString(UImacro.Fields.name, name);
            map.addString(UImacro.Fields.macro, macro);
            map.addString(UImacro.Fields.description, description);
            map.addEnum(UImacro.Fields.locale, locale);
            Base.update(dao, user, s, map);
        }
        return s;
    }

    /**
     * Loads a list of language specific macros.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param domain
     *            {@code Domain} of the data element
     * @param locle
     *            load macros of this {@Locale.LOCALE}
     * @param date
     *            temporal bound
     * @return macro {@code UImacro} list
     * @throws Exception
     */
    public static List<? extends Base> loadAll(DAOiface dao, Domain domain, Locale.LOCALE locale, Date date)
            throws Exception {
        return Base.loadEntities(dao, UImacro.class, domain.getHistId(),
                new SearchCriteria(UImacro.Fields.locale, locale), date);
    }

    /**
     * Loads a list of macros.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param domain
     *            {@code Domain} of the data element
     * @param date
     *            temporal bound
     * @return {@code UImacro} list
     * @throws Exception
     */
    public static List<? extends Base> loadAll(DAOiface dao, Domain domain, Date date) throws Exception {
        return Base.loadEntities(dao, UImacro.class, domain.getHistId(), null, date);
    }

    /**
     * Loads a macro.
     * 
     * @param id
     *            ID of the {@code UImacro}
     * @return {@code UImacro}
     * @throws Exception
     */
    public static UImacro load(int id) throws Exception {
        return (UImacro) Base.load(null, UImacro.class, id, null);
    }

    /**
     * Loads a macro
     * 
     * @param ci
     *            ID of the {@code CI}
     * @param ui
     *            ID of the {@code UIelement}
     * @param date
     *            temporal bound
     * @return {@code UImacro}
     * @throws Exception
     */
    public static UImacro load(int ci, long ui, Date date) throws Exception {
        return (UImacro) CIdata.load(null, UImacro.class, ci, ui, null, date);
    }

    /**
     * Loads a macro.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param ci
     *            ID of the {@code CI}
     * @param ui
     *            ID of the {@code UIelement}
     * @param date
     *            temporal bound
     * @return {@code UImacro}
     * @throws Exception
     */
    public static UImacro load(DAOiface dao, int ci, long ui, Date date) throws Exception {
        return (UImacro) CIdata.load(null, UImacro.class, ci, ui, null, date);
    }

    /**
     * Returns the macro text.
     * 
     * @return macro text
     */
    public String getMacro() {
        return macro;
    }

    /**
     * Returns the name of the macro.
     * 
     * @return macro name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the {@code Locale.LOCALE} of the macro.
     * 
     * @return {@code Locale.LOCALE}
     */
    public Locale.LOCALE getLocale() {
        return locale;
    }

    /**
     * Returns the description of the macro.
     * 
     * @return macro description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Updates a {@code UImacro}
     * 
     * @param user
     *            {@code User} who performs the object update
     * @param macro
     *            macro text
     * @throws Exception
     */
    public void update(User user, String macro) throws Exception {
        UpdateMap map = new UpdateMap(UImacro.Fields.class);
        map.addString(UImacro.Fields.macro, macro);
        Base.update(user, this, map);
    }

    /**
     * Renames a {@code UImacro}
     * 
     * @param user
     *            {@code User} who performs the object update
     * @param macro
     *            macro text
     * @throws Exception
     */
    public void rename(User user, String newName) throws Exception {
        UpdateMap map = new UpdateMap(UImacro.Fields.class);
        map.addString(UImacro.Fields.name, newName);
        Base.update(user, this, map);
    }

    /**
     * Deletes a macro
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param user{@code User} who performs the delete operation
     * @param macroId
     *            macro ID
     * @throws Exception
     */
    public static void delete(DAOiface dao, User user, int macroId) throws Exception {
        Base.delete(dao, user, macroId, UImacro.class, false);
    }

    /**
     * Searches a macro.
     * 
     * @param domain
     *            {@code Domain} of the data element
     * @param fields
     *            search fields
     * @param value
     *            search value
     * @param flags
     *            search flags
     * @param limit
     *            search limits
     * @return list of macros
     * @throws Exception
     */
    static public List<Base> search(Domain domain, EnumSet<UImacro.Fields> fields, String value,
            EnumSet<Base.Search> flags, SearchLimit limit) throws Exception {
        return Base.search(domain, UImacro.class, fields, value, null, flags, limit, false);
    }

    @Override
    public ClassID getCID() {
        return ClassID.UIMACRO;
    }

    @Override
    public UImacro getData() {
        return this;
    }
}
