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

import java.util.Date;

/**
 * Interface to handle entity historization.
 * 
 * @author Peter Sauer
 */
// LastReview: 17.02.2013
public interface HistorizationIface {
    /**
     * historization status of the entity
     */
    public enum STATUS {
        /**
         * actual, newest version of the entity
         */
        ACTIVE,
        /**
         * deleted entity
         */
        DELETED,
        /**
         * historic version of the entity
         */
        UPDATED,
        /**
         * special case: deleted from DB
         */
        DELETED_FROM_DB,
        /**
         * references from an imported DB, actual used only for (virtual)
         * {@code User}
         */
        VIRTUAL,
        /**
         * e.g. set a Domain to inactive
         */
        INACTIVE
    };

    /**
     * Returns the internal DB ID of the entity.
     * 
     * @return DB ID
     */
    public int getDBid();

    /**
     * Sets the DB ID.
     * 
     * @param id
     *            DB ID
     */
    public void setDBid(int id);

    /**
     * Returns the internal version counter of the entity.
     * 
     * @return version
     */
    public int getVersion();

    /**
     * Sets the internal version counter.
     * 
     * @param version
     */
    public void setVersion(int version);

    /**
     * Increments the internal version counter.
     */
    public void incVersion();

    /**
     * Sets the status of the entity.
     * 
     * @param status
     */
    public void setHistStatus(STATUS status);

    /**
     * Returns the status of the entity.
     * 
     * @return historization status
     */
    public STATUS getHistStatus();

    /**
     * Sets the logical ID of the entity. This ID represents an entity
     * independent of the status.
     * 
     * @param historizationId
     */
    public void setHistId(int historizationId);

    /**
     * Returns the logical ID.
     * 
     * @return logical ID
     */
    public int getHistId();

    /**
     * Returns the creation date of the entity.
     * 
     * @return creation date
     */
    public Date getCreationTime();

    /**
     * Sets the creation date of the entity.
     * 
     * @param date
     */
    void setCreationTime(Date date);

    /**
     * Returns the last modification date of the entity.
     * 
     * @return date of last modification
     */
    public Date getLastModified();

    /**
     * Sets the last modification date of the entity.
     * 
     * @param date
     *            modification date
     */
    void setLastModified(Date date);

    /**
     * Sets the deletion date of the entity.
     * 
     * @param delDate
     *            deletion date
     */
    public void setDeletionDate(Date delDate);

    /**
     * Returns the deletion date of the entity.
     * 
     * @return deletion date
     */
    public Date getDeletionDate();
}
