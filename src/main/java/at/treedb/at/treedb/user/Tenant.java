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
package at.treedb.user;

import javax.persistence.Column;
import javax.persistence.Entity;

import at.treedb.ci.Image;
import at.treedb.db.Base;
import at.treedb.db.ClassID;
import at.treedb.db.DAO;
import at.treedb.db.DAOiface;
import at.treedb.db.DBkey;

/**
 * <p>
 * No in use due principal problems of tenant concept.
 * </p>
 * 
 * @author Peter Sauer
 *
 */
@SuppressWarnings("serial")
@Entity
public class Tenant extends Base implements Cloneable {

    @Override
    public ClassID getCID() {
        return ClassID.TENANT;
    }

    /**
     * Access fields for update and search operations
     */
    public enum Fields {
        name, prefix, displayName, comment
    }

    private String name;
    private String prefix;
    private String displayName;
    @Column(name = "m_comment") // Oracle
    private String comment;
    @DBkey(value = Image.class)
    private int logo;

    protected Tenant() {

    }

    public Tenant(String name, String prefix, String displayName, String comment) {
        this.name = name;
        this.prefix = prefix;
        this.displayName = displayName;
        this.comment = comment;
    }

    /**
     * 
     * @param user
     * @param name
     * @param prefix
     * @param displayName
     * @param comment
     * @return
     * @throws Exception
     */
    public static Tenant create(User user, String name, String prefix, String displayName, String comment)
            throws Exception {
        Tenant t = new Tenant(name, prefix, displayName, comment);
        DAOiface dao = DAO.getDAO();
        try {
            dao.beginTransaction();
            Base.save(dao, null, user, t);
            dao.endTransaction();
        } catch (Exception e) {
            dao.rollback();
            throw e;
        }
        return t;
    }

    /**
     * 
     * @param admin
     * @param tenant
     * @throws Exception
     */
    public static void delete(User admin, Tenant tenant) throws Exception {
        Base.delete(admin, tenant, false);
    }

    /**
     * Returns the tenant name.
     * 
     * @return tenant name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the prefix of the tenant.
     * 
     * @return prefix of the tenant
     */
    public String getPrefix() {
        return prefix;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getLogo() {
        return logo;
    }

    public String getComment() {
        return comment;
    }
}
