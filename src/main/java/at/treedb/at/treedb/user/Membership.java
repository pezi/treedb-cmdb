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
import java.util.HashMap;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;

import at.treedb.db.Base;
import at.treedb.db.ClassID;
import at.treedb.db.DAO;
import at.treedb.db.DAOiface;
import at.treedb.db.DBkey;
import at.treedb.db.HistorizationIface;
import at.treedb.db.SearchCriteria;

/**
 * <p>
 * Membership defines the affiliation of user with a group.
 * </p>
 * 
 * @author Peter Sauer
 *
 */
@SuppressWarnings("serial")
@Entity
public class Membership extends Base {

    public enum Fields {
        group, user
    }

    @DBkey(Group.class)
    @Column(name = "m_group") // some DBs have a problem with 'group'
    private int group;
    @DBkey(User.class)
    @Column(name = "m_user") // some DBs have a problem with 'user'
    private int user;

    //
    protected Membership() {
    }

    private Membership(User user, Group group) {
        this.user = user.getHistId();
        this.group = group.getHistId();
    }

    /**
     * Creates a {@code Membership}.
     * 
     * @param user
     *            {@code User} who is member of the parameter {@code group}
     * @param group
     *            {@code Group}
     * @return {@code Membership}.
     * @throws Exception
     */
    public static Membership create(User creator, User user, Group group) throws Exception {
        return create(null, creator, user, group);
    }

    /**
     * Creates a {@code Membership}.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param user
     *            {@code User} who is member of the parameter {@code group}
     * @param group
     *            {@code Group}
     * @return {@code Membership}.
     * @throws Exception
     */
    public static Membership create(DAOiface dao, User creator, User user, Group group) throws Exception {
        Membership membership = null;
        if (user == null || group == null) {
            throw new NullPointerException("Membership.create(): parameter user/group is null!");
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
            List<? extends Base> list = Base.loadEntities(dao, Membership.class,
                    new SearchCriteria[] { new SearchCriteria(Membership.Fields.user, user.getHistId()),
                            new SearchCriteria(Membership.Fields.group, Group.GROUP_ADMIN_ID) },
                    null);
            if (list.size() > 0) {
                throw new Exception("Membership create(): Group membership exists for this user!");
            }
            membership = new Membership(user, group);
            Base.save(dao, null, creator, membership);
            if (localDAO) {
                dao.endTransaction();
            }
        } catch (Exception e) {
            if (localDAO) {
                dao.rollback();
            }
            throw e;
        }
        return membership;
    }

    /**
     * Returns the group ID.
     * 
     * @return group ID
     */
    public int getGroup() {
        return group;
    }

    /**
     * Returns the user ID
     * 
     * @return user ID
     */
    public int getUser() {
        return user;
    }

    /**
     * Deletes a {@code Membership}.
     * 
     * @param user
     *            user who deletes a {@code Membership}
     * @param membership
     *            {@code Membership} to be deleted
     * @throws Exception
     */
    public static void delete(User user, Membership membership) throws Exception {
        Base.delete(user, membership, false);
    }

    public static void delete(DAOiface dao, User user, Membership membership) throws Exception {
        Base.delete(dao, user, membership, false);
    }

    /**
     * Deletes a {@code Membership}.
     * 
     * @param user
     *            user who deletes a {@code Membership}
     * @param id
     *            {@code Membership} ID which should be deleted
     * @throws Exception
     */
    public static void delete(User user, int id) throws Exception {
        Base.delete(user, id, Membership.class, false);
    }

    /**
     * Deletes a {@code Membership}.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param user
     *            user who deletes a {@code Membership}
     * @param id
     *            {@code Membership} ID which should be deleted
     * @throws Exception
     */
    public static void delete(DAOiface dao, User user, int id) throws Exception {
        Base.delete(dao, user, id, Membership.class, false);
    }

    /**
     * Deletes a {@code Membership} from DB explicit without historization.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param user
     *            user who deletes a {@code Membership}
     * @param id
     *            {@code Membership} ID
     * @throws Exception
     */
    public static void dbDelete(DAOiface dao, User user, int id) throws Exception {
        Base.delete(dao, user, id, Membership.class, false);
    }

    /**
     * Deletes a {@code Membership} from DB explicit without historization.
     * 
     * @param user
     *            user who deletes a {@code Membership}
     * @param id
     *            {@code Membership} ID
     * @throws Exception
     */
    public static void dbDelete(User user, int id) throws Exception {
        Base.delete(user, id, Membership.class, false);
    }

    /**
     * Loads a {@code Membership}.
     * 
     * @param id
     *            {@code Membership} ID
     * @return {@code Membership} object
     * @throws Exception
     */
    public static Membership load(int id) throws Exception {
        return (Membership) Base.load(null, Membership.class, id, null);
    }

    /**
     * Loads a {@code Membership}.
     * 
     * @param id
     *            {@code Membership} ID
     * @param date
     *            temporal bound
     * @return {@code Membership} object
     * @throws Exception
     */
    public static Membership load(int id, Date date) throws Exception {
        return (Membership) Base.load(null, User.class, id, date);
    }

    /**
     * Counts the {@code Membership} entities/table rows.
     * 
     * @param status
     *            search filter representing the historization status of the
     *            {@code Membership}. {@code null} counts all user DB entries.
     * @return {@code Membership} number
     * @throws Exception
     */
    public static long rowCount(HistorizationIface.STATUS status) throws Exception {
        return Base.countRow(null, Group.class, status, null);
    }

    /**
     * Loads all memberships of a {@code User}
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param user
     *            {@code User}
     * @return list of {@code Membership}
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    static public List<Base> load(DAOiface dao, User user) throws Exception {
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
            map.put("status", at.treedb.db.HistorizationIface.STATUS.ACTIVE);
            map.put("user", user.getHistId());
            StringBuffer buf = new StringBuffer();
            buf.append("select p from ");
            buf.append(Membership.class.getSimpleName());
            buf.append(" p where p.user = :user and p.status = :status");
            list = (List<Base>) dao.query(buf.toString(), map);
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
        return ClassID.MEMBERSHIP;
    }

}
