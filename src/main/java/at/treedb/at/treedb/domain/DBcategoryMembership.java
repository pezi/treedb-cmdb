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

import at.treedb.db.Base;
import at.treedb.db.ClassID;
import at.treedb.db.DAOiface;
import at.treedb.db.DBkey;
import at.treedb.user.User;

/**
 * <p>
 * Helper class for assigning a {@code Domain} to an user defined category.
 * </p>
 * 
 * @author Peter Sauer
 *
 */
@SuppressWarnings("serial")
@Entity
public class DBcategoryMembership extends Base implements Comparable<Object>, Cloneable {
    /**
     * Access fields for update and search operations
     */
    public enum Fields {
        dbCategory, index
    }

    @DBkey(DBcategory.class)
    private int dbCategory;
    @Column(name = "m_index")
    private int index;

    @Override
    public ClassID getCID() {
        return ClassID.DBCATEGORYMEMBERSHIP;
    }

    protected DBcategoryMembership() {
    }

    protected DBcategoryMembership(DBcategory category, int index) {
        dbCategory = category.getHistId();
        this.index = index;
    }

    /**
     * Creates a {@code DBcategoryMembership} object.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param domain
     *            {@code Domain} which is associated with this membership
     * @param user
     *            user who performs the {@code DBcategoryMembership} creation
     * @param category
     *            {@code DBcategory} which is associated with the {@code Domain}
     * @return {@code DBcategoryMembership} object
     * @throws Exception
     */
    public static DBcategoryMembership create(DAOiface dao, Domain domain, User user, DBcategory category, int order)
            throws Exception {
        DBcategoryMembership membership = new DBcategoryMembership(category, order);
        Base.save(dao, domain, user, membership);
        return membership;
    }

    /**
     * Loads all {@code DBcategoryMembership} objects.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @throws Exception
     */
    public static List<? extends Base> loadAll(DAOiface dao) throws Exception {
        return Base.loadEntities(dao, DBcategoryMembership.class, null, false);
    }

    @SuppressWarnings("unchecked")
    public static DBcategoryMembership load(DAOiface dao, Domain domain) throws Exception {
        List<Base> list = (List<Base>) Base.loadEntities(dao, DBcategoryMembership.class, domain.getHistId(), null,
                null);
        if (list.size() == 0) {
            return null;
        } else {
            return (DBcategoryMembership) list.get(0);
        }
    }

    /**
     * Return the ID of the category.
     * 
     * @return category ID
     */
    public int getDBcategory() {
        return dbCategory;
    }

    /**
     * Returns the ordinal numeral of this membership.
     * 
     * @return ordinal numeral
     */
    public int getIndex() {
        return index;
    }

    @Override
    public int compareTo(Object u) {
        return index - ((DBcategoryMembership) u).index;
    }

    public static void delete(User user, DBcategoryMembership cat) throws Exception {
        Base.delete(user, cat, false);
    }

    public static void delete(DAOiface dao, User user, DBcategoryMembership cat) throws Exception {
        Base.delete(dao, user, cat, false);
    }
}
