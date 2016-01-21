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
package at.treedb.domain;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;

import at.treedb.ci.Image;
import at.treedb.ci.ImageDummy;
import at.treedb.db.Base;
import at.treedb.db.ClassID;
import at.treedb.db.DAOiface;
import at.treedb.db.DBkey;
import at.treedb.i18n.Istring;
import at.treedb.i18n.Locale;
import at.treedb.user.User;

/**
 * User based categories for data bases (domains).
 * 
 * @author Peter Sauer
 *
 */
@SuppressWarnings("serial")
@Entity
public class DBcategory extends Base implements Comparable<Object>, Cloneable {
    private static DBcategory unknown;
    private static int CATEGORY_UNKNOWN_ID = -1;

    /**
     * Access fields for update and search operations
     */
    public enum Fields {
        name, alias, description, icon, index
    }

    private String name;
    @DBkey(Istring.class)
    private int alias;
    @DBkey(Istring.class)
    private int description;
    @DBkey(value = Image.class)
    private int icon;
    @Column(name = "m_index")
    private int index;

    @Override
    public ClassID getCID() {
        return ClassID.DBCATEGORY;
    }

    protected DBcategory() {

    }

    protected DBcategory(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public static DBcategory create(DAOiface dao, User user, Locale locale, String name, String alias,
            String description, ImageDummy icon, int order) throws Exception {
        DBcategory category = new DBcategory(name, order);
        if (alias != null) {
            category.alias = Istring.create(dao, null, user, category.getCID(), alias, locale.getLanguage())
                    .getHistId();
        }
        if (description != null) {
            category.description = Istring.create(dao, null, user, category.getCID(), description, locale.getLanguage())
                    .getHistId();
        }
        if (icon != null) {
            Image i = Image.create(dao, null, user,
                    "dbCategory_" + icon.getData().hashCode() + "_" + Base.getRandomLong(), icon.getData(),
                    icon.getMimeType(), icon.getLicense());
            category.icon = i.getHistId();
        }

        Base.save(dao, null, user, category);

        return category;
    }

    /**
     * Loads all {@code DBcategory} objects.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @throws Exception
     */
    public static List<? extends Base> loadAll(DAOiface dao) throws Exception {
        return Base.loadEntities(dao, DBcategory.class, null, false);
    }

    public static void delete(User user, DBcategory cat) throws Exception {
        Base.delete(user, cat, false);
    }

    public static void delete(DAOiface dao, User user, DBcategory cat) throws Exception {
        Base.delete(dao, user, cat, false);
    }

    public int getAlias() {
        return alias;
    }

    /**
     * Returns the description of the category;
     * 
     * @return
     */
    public int getDescription() {
        return description;
    }

    /**
     * 
     * @return
     */
    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public int compareTo(Object u) {
        return index - ((DBcategory) u).index;
    }

    static public DBcategory getDBcategoryUnkown() {
        return unknown;
    }

    static {
        unknown = new DBcategory("unkown", -1);
        unknown.setHistId(CATEGORY_UNKNOWN_ID);
    }
}
