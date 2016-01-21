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

/**
 * <p>
 * Dummy/helper class for connecting items.
 * </p>
 * 
 * @author Peter Sauer
 * 
 */
public class ConnectionDummy implements Connectable {
    private int id;
    private HashSet<Connectable> parents;
    private HashSet<Connectable> children;

    public ConnectionDummy(Connectable c) {
        id = c.getId();
        parents = new HashSet<Connectable>();
        children = new HashSet<Connectable>();
    }

    @Override
    public HashSet<Connectable> getChildren() {
        return children;
    }

    @Override
    public HashSet<Connectable> getParents() {
        return parents;
    }

    @Override
    public Connectable getParent() throws Exception {
        if (parents.isEmpty()) {
            throw new Exception("ConnectionDummy.getParent(): No parent available");
        }
        if (parents.size() > 1) {
            throw new Exception("ConnectionDummy.getParent(): Multiple parents available");
        }
        return parents.toArray(new Connectable[1])[0];
    }

    @Override
    public String getCanonicalName() {
        return "" + id;
    }

    @Override
    public TYPE getConnectionType() {
        return TYPE.DUMMY;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getIcon() {
        return 0;
    }

}
