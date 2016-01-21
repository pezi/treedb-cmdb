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
import java.util.HashSet;

/**
 * <p>
 * Manager for locking objects for exclusive use.
 * </p>
 * 
 * @author Peter Sauer
 *
 */
public class LockingManager {
    static private HashSet<LockLevel> hashSet = new HashSet<LockLevel>();

    public enum LockLevel {
        TEST
    };

    public static void getLock(EnumSet<LockLevel> locks) throws Exception {
        synchronized (hashSet) {
            for (LockLevel l : locks) {
                if (hashSet.contains(l)) {
                    throw new Exception("Lock level " + l.name() + " is locked.");
                } else {
                    hashSet.add(l);
                }
            }
        }
    }

    public static void releaseLock(EnumSet<LockLevel> locks) throws Exception {
        synchronized (hashSet) {
            for (LockLevel l : locks) {
                if (!hashSet.contains(l)) {
                    throw new Exception("Unlocking non locked level: " + l.name());
                } else {
                    hashSet.remove(l);
                }
            }
        }
    }

}
