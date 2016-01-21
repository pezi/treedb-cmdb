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

package at.treedb.ci;

import java.util.HashSet;

import at.treedb.db.DBkey;

/**
 * Generic interface for a connecting items.
 * 
 * @author Peter Sauer
 * 
 */
public interface Connectable {
    /**
     * Possible connection types.
     * 
     */
    public enum TYPE {
        /**
         * Root item is the domain which is connected with the items.
         */
        DOMAIN,
        /**
         * Connected CIs
         */
        CI,
        /**
         * Connected CI type - reference tree
         */
        CITYPE,
        /**
         * Dummy connection for the helper class {@code ConnectionDummy}
         */
        DUMMY
    };

    /**
     * Returns all child items.
     * 
     * @return children of the item
     */
    HashSet<Connectable> getChildren();

    /**
     * Returns all parent items.
     * 
     * @return parents of the item
     */
    HashSet<Connectable> getParents();

    Connectable getParent() throws Exception;

    /**
     * Returns the canonical name of the item.
     * 
     * @param language
     *            (optional) language parameter
     * @return canonical name of the item
     * @throws Exception
     */
    String getCanonicalName();

    /**
     * Returns the connection type of the item
     * 
     * @return connection type
     */
    TYPE getConnectionType();

    /**
     * Returns the ID of the item,
     * 
     * @return DB ID of the item.
     */
    int getId();

    /**
     * Returns the icon of the item.
     * 
     * @return ID of the {@code Image}
     */
    @DBkey(value = Image.class)
    int getIcon();
}
