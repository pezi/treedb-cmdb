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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Index;

import at.treedb.db.Base;
import at.treedb.db.ClassID;
import at.treedb.db.DAO;
import at.treedb.db.DAOiface;
import at.treedb.db.DBkey;
import at.treedb.db.HistorizationIface;
import at.treedb.db.SearchLimit;
import at.treedb.db.UpdateMap;

/**
 * <p>
 * Group for TreeDB users.
 * </p>
 * 
 * @author Peter Sauer
 *
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "m_group", indexes = { @Index(columnList = "histId") })
public class Group extends Base implements Cloneable {
    static private HashMap<Integer, Group> buildInMap = new HashMap<Integer, Group>();
    static private HashSet<String> buildInSet = new HashSet<String>();

    /**
     * build-in group ADMIN
     */
    public static final int GROUP_ADMIN_ID = -1;
    /**
     * build-in group GUEST
     */

    public static final int GROUP_GUEST_ID = -2;
    private static Group admin, guest;

    /**
     * Access fields for update and search operations
     */
    public enum Fields {
        /**
         * name of the group
         */
        name, description, tenant
    }

    private String name;
    private String description;
    @DBkey(value = Tenant.class)
    private int tenant;

    protected Group() {
    }

    private Group(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Creates a {@code Group}
     * 
     * @param user
     *            user who creates a {@code Group}.
     * @param name
     *            name of the group
     * @param description
     *            description
     * @param language
     *            language of the description
     * @return {@code Group}
     * @throws Exception
     */
    public static Group create(User user, String name, String description) throws Exception {
        return create(null, user, name, description);
    }

    /**
     * Creates a {@code Group}
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param user
     *            user who creates a {@code Group}.
     * @param name
     *            name of the group
     * @param description
     *            description
     * @param language
     *            language of the description
     * @return {@code Group}
     * @throws Exception
     */
    public static Group create(DAOiface dao, User user, String name, String description) throws Exception {
        Group group = null;
        boolean localDAO = false;
        if (dao == null) {
            dao = DAO.getDAO();
            localDAO = true;
        }
        try {
            if (localDAO) {
                dao.beginTransaction();
            }
            group = new Group(name, description);
            Base.save(dao, null, user, group);
            if (localDAO) {
                dao.endTransaction();
            }
        } catch (Exception e) {
            if (localDAO) {
                dao.rollback();
            }
            throw e;
        }
        return group;
    }

    @Override
    public Object checkConstraints(DAOiface dao, UpdateMap update) throws Exception {
        // ensure that a group name is unique
        Base.checkConstraint(dao, update, getHistId(), Group.Fields.name, getName());
        return null;

    }

    /**
     * Returns the name of the group.
     * 
     * @return group name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the description of the group
     * 
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Deletes a group.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param user
     *            user who deletes a {@code Group}, can be {@code null}
     * @param group
     *            {@code Group} to be deleted
     * @throws Exception
     */
    public static void delete(DAOiface dao, User user, Group group) throws Exception {
        Base.delete(dao, user, group, false);
    }

    /**
     * Deletes a group.
     * 
     * @param user
     *            user who deletes a {@code Group}, can be {@code null}
     * @param group
     *            {@code Group} to be deleted
     * @throws Exception
     */
    public static void delete(User user, Group group) throws Exception {
        Base.delete(user, group, false);
    }

    /**
     * Deletes a {@code Group}.
     * 
     * @param user
     *            user who deletes a {@code Group}, can be null
     * @param id
     *            {@code Group} ID, which should be deleted
     * @throws Exception
     */
    public static void delete(User user, @DBkey(value = Group.class) int id) throws Exception {
        Base.delete(user, id, Group.class, false);
    }

    /**
     * Deletes a {@code Group} from DB explicit without historization.
     * 
     * @param user
     *            user who deletes a {@code Group}, can be null
     * @param group
     *            group to be deleted from DB
     * @throws Exception
     */
    public static void dbDelete(User user, Group group) throws Exception {
        Base.delete(user, group, true);
    }

    /**
     * Deletes a {@code Group} from DB explicit without historization.
     * 
     * @param user
     *            user who deletes a {@code Group}, can be null
     * @param id
     *            group DB ID
     * @throws Exception
     */
    public static void dbDelete(User user, @DBkey(value = Group.class) int id) throws Exception {
        Base.delete(user, id, Group.class, false);
    }

    /**
     * Loads a {@code Group}.
     * 
     * @param id
     *            {@code Group} ID
     * @return {@code Group}
     * @throws Exception
     */
    public static Group load(DAOiface dao, @DBkey(value = Group.class) int id) throws Exception {
        return (Group) Base.load(dao, Group.class, id, null);
    }

    /**
     * Loads a {@code Group}.
     * 
     * @param id
     *            {@code Group} ID
     * @return {@code Group}
     * @throws Exception
     */
    public static Group load(@DBkey(value = Group.class) int id) throws Exception {
        return (Group) Base.load(null, Group.class, id, null);
    }

    /**
     * Loads a {@code Group}.
     * 
     * @param id
     *            {@code Group} ID
     * @param date
     *            temporal bound
     * @return {@code Group}
     * @throws Exception
     */
    public static Group load(@DBkey(value = Group.class) int id, Date date) throws Exception {
        return (Group) Base.load(null, User.class, id, date);
    }

    /**
     * Loads a {@code Group}.
     * 
     * @param id
     *            {@code Group} ID
     * @param date
     *            temporal bound
     * @return {@code Group}
     * @throws Exception
     */
    public static Group load(DAOiface dao, @DBkey(value = Group.class) int id, Date date) throws Exception {
        return (Group) Base.load(dao, User.class, id, date);
    }

    /**
     * Loads a {@code Group} by its name.
     * 
     * @param group
     *            {@code Group} name
     * @return {@code Group}
     * @throws Exception
     */
    public static Group load(String group) throws Exception {
        List<Base> list = Group.search(EnumSet.of(Group.Fields.name), group, EnumSet.of(Base.Search.EQUALS), null);
        if (list.size() == 1) {
            return (Group) list.get(0);
        }
        return null;
    }

    /**
     * Counts the {@code Group} entities/table rows.
     * 
     * @param status
     *            search filter representing the historization status of the
     *            {@code Group} {@code null} counts all group DB entries.
     * @return number of groups
     * @throws Exception
     */
    public static long rowCount(HistorizationIface.STATUS status) throws Exception {
        return Base.countRow(null, Group.class, status, null);
    }

    /**
     * Updates a {@code Group}.
     * 
     * @param user
     *            {@code User} who updates a {@code Group}
     * @param map
     *            update map
     * @throws Exception
     */
    public void update(User user, UpdateMap map) throws Exception {
        Base.update(user, this, map);
    }

    /**
     * Updates a {@code Group}.
     * 
     * @param user
     *            {@code User} who updates a {@code Group}
     * @param group
     *            {@code Group} to be updated
     * @param map
     *            update map
     * @throws Exception
     */
    static public void update(User user, Group group, UpdateMap map) throws Exception {
        Base.update(user, group, map);
    }

    /**
     * Updates a {@code Group}.
     * 
     * @param user
     *            {@code User} who updates a {@code Group}
     * @param id
     *            {@code Group} ID
     * @param map
     *            update map
     * @throws Exception
     */
    static public void update(User user, @DBkey(value = Group.class) int id, UpdateMap map) throws Exception {
        Base.update(user, id, Group.class, map);
    }

    /**
     * Searches a {@code Group}.
     * 
     * @param fields
     *            search field
     * @param value
     *            search pattern
     * @param flags
     *            search flags
     * @param limit
     *            optional search limit, can be null
     * @return list of {@code Group}
     * @throws Exception
     */
    static public List<Base> search(EnumSet<Group.Fields> fields, String value, EnumSet<Base.Search> flags,
            SearchLimit limit) throws Exception {
        return Base.search(null, Group.class, fields, value, null, flags, limit, false);
    }

    /**
     * Searches a group per name.
     * 
     * @param groupName
     *            group name
     * @return {@code Group} object
     * @throws Exception
     */
    static public Group search(String groupName) throws Exception {
        List<Base> list = Group.search(EnumSet.of(Group.Fields.name), groupName, EnumSet.of(Search.EQUALS), null);
        if (list.size() == 1) {
            return (Group) list.get(0);
        }
        return null;
    }

    /**
     * Returns the build-in group GUEST.
     * 
     * @return build-in group GUEST.
     */
    public static Group getGuest() {
        return guest;
    }

    /**
     * Returns the build-in group ADMIN.
     * 
     * @return build-in group ADMIN
     */
    public static Group getAdmin() {
        return admin;
    }

    public static Group getBuildInGroup(int id) {
        return buildInMap.get(id);
    }

    /**
     * Checks if a group name is a build in group
     * 
     * @param name
     *            group name
     * @return {@code true} if the group is a build-in group, {@code false} if
     *         not
     */
    public static boolean isBuildInGroup(String name) {
        return buildInSet.contains(name);
    }

    @Override
    public ClassID getCID() {
        return ClassID.GROUP;
    }

    public static void addBuildInGroups(List<Base> list) {
        for (Group g : buildInMap.values()) {
            list.add(g);
        }
    }

    static {
        // build-in groups ADMIN & GUEST
        admin = new Group("ADMIN", "ADMIN_GROUP");
        admin.setHistId(GROUP_ADMIN_ID);
        buildInSet.add(admin.getName());
        guest = new Group("GUEST", "GUEST_GROUP");
        guest.setHistId(GROUP_GUEST_ID);
        buildInSet.add(guest.getName());
        buildInMap.put(admin.getHistId(), admin);
        buildInMap.put(guest.getHistId(), guest);

    }

}
