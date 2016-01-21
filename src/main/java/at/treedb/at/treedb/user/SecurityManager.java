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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import at.treedb.db.Base;
import at.treedb.db.DAOiface;
import at.treedb.domain.Domain;
import at.treedb.user.Permission;
import at.treedb.user.Permissions;

/**
 * <p>
 * Security manager
 * </p>
 * 
 * @author Peter Sauer
 * 
 */
public class SecurityManager {

    static HashMap<Integer, HashMap<Integer, EnumSet<Permission>>> domainMap = new HashMap<Integer, HashMap<Integer, EnumSet<Permission>>>();
    static HashMap<Integer, HashSet<Integer>> userMap = new HashMap<Integer, HashSet<Integer>>();

    public static void removePermissions(int domain) {
        domainMap.remove(domain);
    }

    public static HashMap<Integer, EnumSet<Permission>> getRights(DAOiface dao, Domain domain) throws Exception {
        return getRights(dao, domain.getHistId());
    }

    private static HashMap<Integer, EnumSet<Permission>> getRights(DAOiface dao, int domainId) throws Exception {
        HashMap<Integer, EnumSet<Permission>> rights = null;
        if (!domainMap.containsKey(domainId)) {
            rights = new HashMap<Integer, EnumSet<Permission>>();
            List<Permissions> permissions = Permissions.loadAll(dao, domainId);
            for (Permissions p : permissions) {
                rights.put(p.getGroup(), p.getPermissions());
            }
            domainMap.put(domainId, rights);
        } else {
            rights = domainMap.get(domainId);
        }
        return rights;
    }

    private static HashSet<Integer> getGroups(DAOiface dao, User user) throws Exception {
        HashSet<Integer> groups = null;
        int id = user.getHistId();
        if (!userMap.containsKey(id)) {
            groups = new HashSet<Integer>();
            List<Base> membership = Membership.load(dao, user);
            for (Base b : membership) {
                Membership m = (Membership) b;
                groups.add(m.getGroup());
            }
            userMap.put(id, groups);
        } else {
            groups = userMap.get(id);
        }
        return groups;
    }

    /**
     * Checks is a {@code User} has for a given {@code Domain} read access.
     * 
     * @param domain
     *            {@code Domain} ID
     * @param user
     *            {@code User}
     * @return boolean {@code true} if the user has read access, {@code false}
     *         if not
     * @throws Exception
     */
    static public boolean hasReadRight(DAOiface dao, Domain domain, User user) throws Exception {
        Objects.requireNonNull(domain, "SecurityManager.hasReadRight(): parameter domain can't be null");
        return hasReadRight(dao, domain.getHistId(), user);
    }

    /**
     * Checks is a {@code User} has for a given {@code Domain} read access.
     * 
     * @param domain
     *            {@code Domain}
     * @param user
     *            {@code User}
     * @return boolean {@code true} if the user has read access, {@code false}
     *         if not
     * @throws Exception
     */
    static public boolean hasReadRight(DAOiface dao, int domainId, User user) throws Exception {
        HashMap<Integer, EnumSet<Permission>> rights = getRights(dao, domainId);
        if (rights.containsKey(Group.GROUP_GUEST_ID)) {
            return true;
        }

        if (user == null) {
            return false;
        }

        if (user.getHistId() == 0) {
            return true;
        }
        HashSet<Integer> groups = getGroups(dao, user);
        if (groups.contains(Group.GROUP_ADMIN_ID)) {
            return true;
        }

        for (Integer g : groups) {
            EnumSet<Permission> p = rights.get(g);
            if (p != null) {
                if (p.contains(Permission.READ)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks is a {@code User} has for a given {@code Domain} write access.
     * 
     * @param domain
     *            {@code Domain}
     * @param user
     *            {@code User}
     * @return boolean {@code true} if the user has write access, {@code false}
     *         if not
     * @throws Exception
     */
    static public boolean hasWriteRight(DAOiface dao, Domain domain, User user) throws Exception {
        if (user == null) {
            return false;
        }
        if (user.getHistId() == 0) {
            return true;
        }
        Objects.requireNonNull(domain, "SecurityManager.hasWriteRight(): parameter domain can't be null");
        return hasWriteRight(dao, domain.getHistId(), user);
    }

    /**
     * Checks is a {@code User} has for a given {@code Domain} write access.
     * 
     * @param domainID
     *            {@code Domain} ID
     * @param user
     *            {@code User}
     * @return boolean {@code true} if the user has write access, {@code false}
     *         if not
     * @throws Exception
     */
    static public boolean hasWriteRight(DAOiface dao, int domainID, User user) throws Exception {
        if (user == null) {
            return false;
        }
        if (user.getHistId() == 0) {
            return true;
        }
        HashSet<Integer> groups = getGroups(dao, user);
        HashMap<Integer, EnumSet<Permission>> rights = getRights(dao, domainID);
        if (groups.contains(Group.GROUP_ADMIN_ID)) {
            return true;
        }
        for (Integer g : groups) {
            EnumSet<Permission> p = rights.get(g);
            if (p != null) {
                if (p.contains(Permission.WRITE)) {
                    return true;
                }
            }
        }
        return false;
    }

}
