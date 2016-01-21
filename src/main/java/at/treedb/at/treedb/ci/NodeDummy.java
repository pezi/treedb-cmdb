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

/**
 * Dummy node for connecting CIs or CI types.
 * 
 * @author Peter Sauer
 */
public class NodeDummy {
    private Connectable child;
    private Connectable parent;
    private Node.NodeType type;

    /**
     * Creates a dummy node for connecting a child node with a parent node.
     * 
     * @param child
     *            child entity (CI or CItype)
     * @param parent
     *            parent entity (CI or CItype)
     * @param type
     *            entity type (CI or CItype)
     */
    public NodeDummy(Connectable child, Connectable parent, Node.NodeType type) {
        this.child = child;
        this.parent = parent;
        this.type = type;
    }

    /**
     * Returns the parent entity (CI or CItype).
     * 
     * @return {@code Connectable} parent object
     */
    public Connectable getParent() {
        return parent;
    }

    /**
     * Returns the child entity (CI or CItype).
     * 
     * @return {@code Connectable} child object
     */
    public Connectable getChild() {
        return child;
    }

    /**
     * Returns the entity type.
     * 
     * @return CI or CItype
     */
    public Node.NodeType getNodeType() {
        return type;
    }
}
