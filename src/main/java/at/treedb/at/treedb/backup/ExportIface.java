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

package at.treedb.backup;

import at.treedb.db.ClassID;

/**
 * <p>
 * Interface for the (TDEF) Tree DB Exchange Format. This interface is a mix of
 * other interfaces/methods to provide a minimal method set. for exporting data.
 * </p>
 * 
 * @author Peter Sauer
 *
 */

public interface ExportIface {
    /**
     * {@link at.treedb.dbHistorizationIface#getDBid }
     */
    public int getDBid();

    /**
     * {@link at.treedb.ClassID#getCID }
     */
    public ClassID getCID();

    /**
     * original JAVA clone signature
     */
    public Object clone() throws CloneNotSupportedException;
}
