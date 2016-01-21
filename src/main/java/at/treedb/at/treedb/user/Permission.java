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

/**
 * <p>
 * Permissions to handle access to the entities (CI, CItype, etc of a
 * {@code Domain}.
 * </p>
 * 
 * @author Peter Sauer
 *
 */
public enum Permission {
    /**
     * Read permission, necessary to access a {@code Domain}.
     */
    READ(1),
    /**
     * Write permission to change data fields.
     * 
     */
    /**
     * Extended read permissions, access to the data fields (read only)
     */
    READ_FIELDS(2),
    /**
     * Add or remove a CI
     */
    WRITE(4),

    DOMAIN_ADMIN(8),
    /**
     * Read permission to see the reference tree
     */
    DATABASE_ADMIN(16);

    private final long right;

    private Permission(long right) {
        this.right = right;
    }

    /**
     * Returns/converts the enum value to a bit mask.
     * 
     * @return single access right as bit mask
     */
    public long getRight() {
        return right;
    }

    public static EnumSet<Permission> getAvailablePermissions() {
        return AVAILABLE;
    }

    public static EnumSet<Permission> getAdminPermissions() {
        return ADMIN;
    }

    public static EnumSet<Permission> getGuestPermissions() {
        return GUEST;
    }

    static private EnumSet<Permission> AVAILABLE = EnumSet.of(READ, READ_FIELDS, WRITE, DOMAIN_ADMIN, DATABASE_ADMIN);

    static private EnumSet<Permission> ADMIN = EnumSet.of(READ, READ_FIELDS, WRITE, DOMAIN_ADMIN, DATABASE_ADMIN);

    static private EnumSet<Permission> GUEST = EnumSet.of(READ, READ_FIELDS);

}
