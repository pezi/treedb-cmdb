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

/**
 * Interface for tracking entity creation & modification.
 * 
 * @author Peter Sauer
 *
 */
public interface UserIface {
    /**
     * Sets the creator of the entity.
     * 
     * @param createdBy
     *            user id
     */
    public void setCreatedBy(int createdBy);

    /**
     * Returns the creator of the entity.
     * 
     * @return user id
     */
    public int getCreatedBy();

    /**
     * Sets the {@code User} who modifies the entity.
     * 
     * @param modifiedBy
     *            user id
     */
    public void setModifiedBy(int modifiedBy);

    /**
     * Returns the user who had modified the entity.
     * 
     * @return user id
     */
    public int getModifiedBy();
}
