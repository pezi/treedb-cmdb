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

import java.lang.ref.WeakReference;
import java.util.EnumSet;

import at.treedb.user.User;

/**
 * <p>
 * Lock object for data access synchronization.
 * </p>
 * 
 * @author Peter Sauer
 *
 * @param <LOCKED>
 *            object, which should be locked
 * @param <LOCKING>
 *            object, which locks the {@code LOCKED} object
 */
public class Lock {

    public enum LockType {
        SPECIAL, GROUP, OBJECT
    }

    // special locks
    public enum SpecialLock {
        DATABASE
    }

    // lock level
    public enum LockLevel {
        WRITE
    }

    private LockType lockType;
    private User user;
    private ClassID classID;
    private LockLevel level;
    private Base lockedObject;
    private EnumSet<ClassID> lockedGroup;
    private SpecialLock specialLock;
    private long lockDate;
    private WeakReference<LockingIface> lockingObject;
    private String reason;
    private boolean locked;

    /**
     * Creates a {@code Lock} object.
     * 
     * @param user
     *            {@code User}, who creates the {@code Lock} object
     * @param level
     *            locking level
     * @param lockedObject
     *            {@code LOCKED} object
     * @param lockingObject
     *            {@code LOCKING} object
     */
    public Lock(User user, LockLevel level, Base lockedObject, LockingIface lockingObject) {
        this.lockType = LockType.OBJECT;
        this.user = user;
        this.classID = ((Base) lockedObject).getCID();
        this.level = level;
        this.lockedObject = lockedObject;
        this.lockingObject = new WeakReference<LockingIface>(lockingObject);
    }

    public Lock(User user, LockLevel level, SpecialLock specialLock, LockingIface lockingObject) {
        this.lockType = LockType.SPECIAL;
        this.user = user;
        this.level = level;
        this.specialLock = specialLock;
        this.lockingObject = new WeakReference<LockingIface>(lockingObject);
    }

    public Lock(User user, LockLevel level, EnumSet<ClassID> lockedGroup, LockingIface lockingObject) {
        this.lockType = LockType.GROUP;
        this.user = user;
        this.level = level;
        this.lockedGroup = lockedGroup;
        this.lockingObject = new WeakReference<LockingIface>(lockingObject);
    }

    /**
     * Returns the {@code LOCKED} object.
     * 
     * @return {@code LOCKED} object
     */
    public Base getLockedObject() {
        return lockedObject;
    }

    /**
     * {@code User} who locks the object.
     * 
     * @return {@code User}
     */
    public User getUser() {
        return user;
    }

    /**
     * Returns the locking object
     * 
     * @return {@code LOCKING} object
     */
    public LockingIface getLockingObject() {
        return lockingObject.get();
    }

    /**
     * Tries to acquire the lock.
     * 
     * @throws Exception
     */
    public void lock() throws Exception {
        LockingManager.acquireLock(this);
    }

    /**
     * Releases a lock.
     * 
     * @throws Exception
     */
    public void unlock() throws Exception {
        LockingManager.releaseLock(this);
    }

    /**
     * Returns the {@code ClassID} of the locked object.
     * 
     * @return {@code ClassID}, or {@code null} if not available
     */
    public ClassID getClassID() {
        return classID;
    }

    /**
     * Returns the lock date.
     * 
     * @return lock date
     */
    public long getLockDate() {
        return lockDate;
    }

    /**
     * Sets the lock date.
     */
    public void setLockDate() {
        this.lockDate = System.currentTimeMillis();
    }

    public SpecialLock getSpecialLock() {
        return specialLock;
    }

    public LockType getLockType() {
        return lockType;
    }
}
