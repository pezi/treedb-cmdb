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
package at.treedb.db;

import java.util.HashMap;

/**
 * <p>
 * Manager for handling object locking for write access.
 * </p>
 * 
 * @author Peter Sauer
 *
 */
public class LockingManager {
    private static String RELEASE_ERROR = "LockingManager.releaseLock(): Try to unlock a non locked ";
    // private static boolean globalDatabaseLock;
    // locked objects
    private static HashMap<Long, Lock> baseMap = new HashMap<Long, Lock>();
    private static final Object sync = new Object();

    /**
     * Tries to acquire a lock.
     * 
     * @param lock
     * @throws Exception
     */
    public static void acquireLock(Lock lock) throws Exception {
        // TODO: check user rights
        synchronized (sync) {
            switch (lock.getLockType()) {
            case OBJECT:
                Base base = lock.getLockedObject();
                long composedId = base.getUniqueDBid();
                Lock existingLock = baseMap.get(composedId);
                // check lock - check weak reference to the locking object
                if (existingLock != null && existingLock.getLockingObject() == null) {
                    // there exits a lock, but the locking object was
                    // destroyed by the GC
                    baseMap.remove(composedId);
                    existingLock = null;
                }
                if (existingLock != null) {
                    throw new LockException(lock);
                }
                baseMap.put(composedId, lock);
                lock.setLockDate();
                break;
            }

        }
    }

    /**
     * Releases a lock.
     * 
     * @param lock
     *            lock to be released
     * @throws Exception
     */
    public static void releaseLock(Lock lock) throws Exception {
        synchronized (sync) {
            baseMap.remove(lock.getLockedObject().getUniqueDBid());
        }
    }
}
