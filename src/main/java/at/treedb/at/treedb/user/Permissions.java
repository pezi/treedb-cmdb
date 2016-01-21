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

import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;

import at.treedb.db.Base;
import at.treedb.db.ClassID;
import at.treedb.db.DAO;
import at.treedb.db.DAOiface;
import at.treedb.db.DBkey;
import at.treedb.db.HistorizationIface;
import at.treedb.db.LazyLoad;
import at.treedb.db.Update;
import at.treedb.db.UpdateMap;
import at.treedb.domain.Domain;

/**
 * <p>
 * Permission handling for CRUD operations. For a {@code Domain} each
 * {@code Group} has given permissions. Special handling for build-in groups
 * ADMIN and GUEST
 * </p>
 * 
 * @author Peter Sauer
 * 
 */
@Entity
public class Permissions extends Base implements Cloneable {
    private static final long serialVersionUID = 1L;

    /**
     * Access fields for update and search operations
     */
    public enum Fields {
        /**
         * permissionsMask
         */
        permissionsMask
    }

    @DBkey(Group.class)
    @Column(name = "m_group")
    // group is part of the SQL syntax
    private int group;
    @LazyLoad
    // this binary permission mask is a binary mirror of the enum field
    private long permissionsMask; // <----------------------|
    // permission to access a given Domain |
    transient private EnumSet<Permission> permissions; // <-|

    protected Permissions() {

    }

    private Permissions(Domain domain, Group group, EnumSet<Permission> perm) {
        this.domain = domain.getHistId();
        this.group = group.getHistId();
        if (perm == null) {
            perm = EnumSet.noneOf(Permission.class);
        }
        this.permissions = perm;
        permissionsMask = 0;
        // EnumSet to binary value
        for (Enum<Permission> p : perm) {
            permissionsMask |= ((Permission) p).getRight();
        }

    }

    protected void callbackUpdate(DAOiface dao, User user, UpdateMap map) throws Exception {
        checkForBuildInGroups(group);
        Update u = map.get(Permissions.Fields.permissionsMask);
        if (u != null) {
            // lazy update by getPermissions()
            permissions = null;
        }
    }

    /**
     * Create a right access mask for a group in context with a domain.
     * 
     * @param domain
     *            {@code Domain}
     * @param group
     *            {@code Group}
     * @param permissions
     *            set of permissions
     * @return {@code Permissions}
     */
    public static Permissions createDummy(Domain domain, Group group, EnumSet<Permission> permissions) {
        return new Permissions(domain, group, permissions);
    }

    /**
     * Create a right access mask for a group in context with a domain.
     * 
     * @param user
     *            {@code User} who creates a {@code Permission}
     * @param domain
     *            {@code Domain}
     * @param group
     *            {@code Group}
     * @param permissions
     *            set of permissions
     * @return {@code Permissions}
     * @throws Exception
     */
    public static Permissions create(User user, Domain domain, Group group, EnumSet<Permission> permissions)
            throws Exception {
        return create(null, user, domain, group, permissions);
    }

    /**
     * Create a right access mask for a group in context with a domain.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param user
     *            {@code User} who creates a {@code Permission}
     * @param domain
     *            {@code Domain}
     * @param group
     *            {@code Group}
     * @param permissions
     *            set of permissions
     * @return {@code Permissions}
     * @throws Exception
     */
    public static Permissions create(DAOiface dao, User user, Domain domain, Group group,
            EnumSet<Permission> permissions) throws Exception {
        Permissions permission = null;
        // overwrite permissions for
        if (group.getHistId() == Group.GROUP_ADMIN_ID) {
            permissions = Permission.getAdminPermissions();
        }
        if (group.getHistId() == Group.GROUP_GUEST_ID) {
            permissions = Permission.getGuestPermissions();
        }
        boolean localDAO = false;
        if (dao == null) {
            dao = DAO.getDAO();
            localDAO = true;
        }
        try {
            if (localDAO) {
                dao.beginTransaction();
            }
            permission = new Permissions(domain, group, permissions);
            Base.save(dao, null, user, permission);
            if (localDAO) {
                dao.endTransaction();
            }
        } catch (Exception e) {
            if (localDAO) {
                dao.rollback();
            }
            throw e;
        }
        return permission;
    }

    /**
     * Returns the {@code Group} ID.
     * 
     * @return {@code Group} ID.
     */
    public int getGroup() {
        return group;
    }

    /**
     * Returns a (binary) permission mask.
     * 
     * @return (binary) permission mask.
     */
    public long getPermissionsBinaryMask() {
        return permissionsMask;
    }

    /**
     * Returns set of permissions.
     * 
     * @return permissions set
     */
    public synchronized EnumSet<Permission> getPermissions() {
        if (permissions == null) {
            permissions = EnumSet.noneOf(Permission.class);
            for (Permission p : Permission.values()) {
                if ((permissionsMask & p.getRight()) != 0) {
                    permissions.add(p);
                }
            }
        }
        return permissions;
    }

    /**
     * Returns the {@code Domain} ID.
     * 
     * @return {@code Domain} ID.
     */
    public int getDomain() {
        return domain;
    }

    /**
     * Deletes a {@code Permission}.
     * 
     * @param user
     *            {@code User} who deletes the {@code Permission}.
     * @param permission
     *            permission to be deleted
     * @throws Exception
     */
    public static void delete(User user, Permissions permission) throws Exception {
        Base.delete(user, permission, false);
    }

    public static void delete(DAOiface dao, User user, Permissions permission) throws Exception {
        Base.delete(dao, user, permission, false);
    }

    /**
     * Deletes a permission.
     * 
     * @param user
     *            {@code User} which deletes the {@code Permission}.
     * @throws Exception
     */
    public static void delete(User user, int id) throws Exception {
        Base.delete(user, id, Permissions.class, false);
    }

    /**
     * Deletes a {@code Permission} from DB explicit without historization.
     * 
     * @param user
     *            {@code User} which deletes the {@code Permission}.
     * @param permission
     *            permission to be deleted permanently
     * @throws Exception
     */
    public static void dbDelete(User user, Permissions permission) throws Exception {
        Base.delete(user, permission, true);
    }

    /**
     * Deletes a {@code Permission} from DB explicit without historization.
     * 
     * @param user
     *            {@code User} which deletes the {@code Permission}.
     * @param id
     *            permission ID to be deleted permanently
     * @throws Exception
     */
    public static void dbDelete(User user, int id) throws Exception {
        Base.delete(user, id, Permissions.class, false);
    }

    /**
     * Loads a {@code Permission} object.
     * 
     * @param id
     *            {@code Permission} ID
     * @return {@code Permission} object
     * @throws Exception
     */
    public static Permissions load(int id) throws Exception {
        return (Permissions) Base.load(null, Permissions.class, id, null);
    }

    /**
     * Loads a {@code Permission}.
     * 
     * @param id
     *            permission ID
     * @param date
     *            temporal bound
     * @return {@code Permission} object
     * @throws Exception
     */
    public static Permissions load(int id, Date date) throws Exception {
        return (Permissions) Base.load(null, User.class, id, date);
    }

    /**
     * Counts the {@code Permission} entities/table rows.
     * 
     * @param status
     *            search filter representing the historization status of the
     *            {@code Permission}. {@code null} counts all {@code Permission}
     *            DB entries.
     * @return number of {@code Permission}
     * @throws Exception
     */
    public static long rowCount(HistorizationIface.STATUS status) throws Exception {
        return Base.countRow(null, Group.class, status, null);
    }

    private static void checkForBuildInGroups(int group) throws Exception {
        if (group < 0) {
            throw new Exception("Permission update of a build-in group isn't possible.");
        }
    }

    /**
     * Updates a {@code Permission}.
     * 
     * @param admin
     *            {@code User} who update a {@code Permission}
     * @param map
     *            update map
     * @throws Exception
     */
    public void update(User admin, UpdateMap map) throws Exception {
        checkForBuildInGroups(group);
        Base.update(admin, this, map);
    }

    /**
     * Updates a permission.
     * 
     * @param user
     *            {@code User} which updates the {@code Permissions}
     * @param permission
     *            permission to be updated
     * @param map
     *            update map
     * @throws Exception
     */
    static public void update(User user, Permissions permission, UpdateMap map) throws Exception {
        checkForBuildInGroups(permission.getGroup());
        Base.update(user, permission, map);
    }

    /**
     * Updates a permission.
     * 
     * @param admin
     *            administration user
     * @param id
     *            permission ID, to be updated
     * @param map
     *            update map
     * @throws Exception
     */
    static public void update(User admin, int id, UpdateMap map) throws Exception {
        Base.update(admin, id, Permissions.class, map);
    }

    /**
     * Load all permissions of a {@code Domain}
     * 
     * @param domainId
     *            {@code Domain} ID
     * @return list of {@code Permissions} object
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    static public List<Permissions> loadAll(DAOiface dao, int domainId) throws Exception {
        return (List<Permissions>) Base.loadEntities(dao, Permissions.class, domainId, null, null);
    }

    @Override
    public ClassID getCID() {
        return ClassID.PERMISSIONS;
    }

}
