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

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.persistence.Entity;

import at.treedb.db.Base;
import at.treedb.db.ClassID;
import at.treedb.db.ClassSelector;
import at.treedb.db.DAO;
import at.treedb.db.DAOiface;
import at.treedb.db.DBkey;
import at.treedb.domain.Domain;
import at.treedb.user.User;

/**
 * A node represents a connection between a child entity and its parent entity.
 * 
 * @author Peter Sauer
 */
@SuppressWarnings("serial")
@Entity
public class Node extends Base implements Cloneable, ClassSelector {
    /**
     * node types
     */
    public enum NodeType {
        CITYPE, CI
    }

    /**
     * 
     * @author pezi
     *
     */
    public enum ConnectionType {
        IN_MEMORY, LAZY_CHILD, LAZY_NODES
    }

    // child ID
    @DBkey(ClassSelector.class)
    private int child;
    // parent ID
    @DBkey(ClassSelector.class)
    private int parent;
    // node type
    private NodeType nodeType;
    private ConnectionType connectionType;

    /**
     * This special constant indicates that the {@code Domain} is the root of
     * the tree, or in other words - this entity is connected with the domain.
     */
    public static final int PARENT_IS_A_DOMAIN = -1;

    protected Node() {
    }

    /**
     * Returns the connection type of the node.
     * 
     * @return connection type of the node
     */
    public ConnectionType getConnectionType() {
        return connectionType;
    }

    /**
     * Creates a node.
     * 
     * @param domain
     *            {@code Domain} of the node
     * @param user
     *            creator of the {@code Node}
     * @param nodeType
     *            type of the {@code Node}
     * @param child
     *            child ID
     * @param parent
     *            parent ID
     * @param connectionType
     *            type of the connection
     */
    private Node(Domain domain, User user, NodeType nodeType, @DBkey(ClassSelector.class) int child,
            @DBkey(ClassSelector.class) int parent, ConnectionType connectionType) {
        setDomain(domain.getHistId());
        this.child = child;
        this.parent = parent;
        this.nodeType = nodeType;
        this.connectionType = connectionType;
        if (user != null) {
            setCreatedBy(user.getHistId());
            setModifiedBy(user.getHistId());
        }
    }

    /**
     * Creates a node which represents the connection between two of the
     * following entities - CI or CItype.
     * 
     * @param domain
     *            domain of the {@code Node}
     * @param user
     *            creator if the {@code Node}
     * @param nodeType
     *            type of the node - CI or CItype
     * @param child
     *            child - CI or CItype
     * @param parent
     *            parent - CI or CItype
     * @return {@code Node} object
     * @throws Exception
     */
    public static Node create(DAOiface dao, Domain domain, User user, NodeType nodeType, Base child, Base parent)
            throws Exception {
        if (parent != null) {
            if (child.getHistId() == parent.getHistId()) {
                throw new Exception("Node.create(): Child and parent are identical!");
            }
            if (((Connectable) parent).getChildren().contains(child)) {
                throw new Exception("Node.create(): Child is already conncted with the parent!");
            }
        }
        if (((Connectable) child).getChildren().contains(parent)) {
            throw new Exception("Node.create(): Circular connection!");
        }
        // special handling for CItype: parent
        ConnectionType connectionType = ConnectionType.IN_MEMORY;
        if (nodeType == NodeType.CI) {
            CI c = (CI) child;
            CI p = (CI) parent;
            if (p.isInMemory() && !c.isInMemory()) {
                connectionType = ConnectionType.LAZY_CHILD;
            } else if (!p.isInMemory() && !c.isInMemory()) {
                connectionType = ConnectionType.LAZY_NODES;
            }
        }
        Node node = new Node(domain, user, nodeType, child.getHistId(),
                parent != null ? parent.getHistId() : Node.PARENT_IS_A_DOMAIN, connectionType);
        Base.save(dao, domain, user, node);
        return node;
    }

    /**
     * Returns the child ID.
     * 
     * @return child ID - CI or CItype
     */
    public @DBkey(ClassSelector.class) int getChild() {
        return child;
    }

    /**
     * Returns the parents ID.
     * 
     * @return parent ID - CI or CItype
     */
    public @DBkey(ClassSelector.class) int getParent() {
        return parent;
    }

    /**
     * Returns the node type.
     * 
     * @return {@code NodeType}, CI or CItype
     */
    public NodeType getType() {
        return nodeType;
    }

    /**
     * Loads a {@code Node}.
     * 
     * @param id
     *            node ID
     * @return {@code Node} object
     * @throws Exception
     */
    public static Node load(@DBkey(Node.class) int id) throws Exception {
        DAOiface dao = DAO.getDAO();
        Node node = null;
        try {
            dao.beginTransaction();
            node = dao.get(Node.class, id);
            dao.endTransaction();
        } catch (Exception e) {
            dao.rollback();
            throw e;
        }
        return node;
    }

    /**
     * Loads a {@code Node} by the pair child/parent item.
     * 
     * @param child
     *            child - CI or CItype ID
     * @param parent
     *            parent - CI or CItype ID
     * @param date
     *            temporal bound
     * @return {@code Node} object
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static Node load(@DBkey(ClassSelector.class) int child, @DBkey(ClassSelector.class) int parent, Date date)
            throws Exception {
        Node node = null;
        DAOiface dao = DAO.getDAO();
        try {
            dao.beginTransaction();
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("child", child);
            map.put("parent", parent);
            map.put("date", date);
            List<Node> list = null;
            list = (List<Node>) dao.query(
                    "select n from Node n where n.child = :child and n.parent = :parent and n.lastModified < :date and (n.deletionDate = null or n.deletionDate > :date) order by n.version desc",
                    0, 1, map);
            if (list.size() == 1) {
                node = list.get(0);
            }

            dao.endTransaction();
        } catch (Exception e) {
            dao.rollback();
            throw e;
        }
        return node;
    }

    /**
     * Deletes a {@code Node} defined by the pair child/parent item.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param domain
     *            {@code Domain} of the {@code Node}
     * @param user
     *            {@code User} who deletes a {@code Node}
     * @param child
     *            child - CI or CItype
     * @param parent
     *            parent - CI or CItype
     * @return {@code true} if the node exists and deletion was successful.
     *         {@code false} if the node doesn't exist.
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static boolean delete(DAOiface dao, Domain domain, User user, Base child, Base parent) throws Exception {
        boolean daoLocal = false;
        if (dao == null) {
            dao = DAO.getDAO();
            daoLocal = true;
        }
        boolean deleted = false;
        try {
            List<Node> list = null;
            if (daoLocal) {
                dao.beginTransaction();
            }
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("child", child.getHistId());
            map.put("parent", parent != null ? parent.getHistId() : Node.PARENT_IS_A_DOMAIN);
            map.put("domain", domain.getHistId());
            map.put("status", at.treedb.db.HistorizationIface.STATUS.ACTIVE);
            list = (List<Node>) dao.query(
                    "select n from Node n where n.domain = :domain and n.child = :child and n.parent = :parent and n.status = :status",
                    map);
            if (list.size() == 1) {

                Node n = list.get(0);
                n.setHistStatus(STATUS.DELETED);
                n.setModifiedBy(user.getHistId());
                n.setDeletionDate(new Date());
                dao.update(n);

                deleted = true;
            }
            if (daoLocal) {
                dao.endTransaction();
            }
        } catch (Exception e) {
            if (daoLocal) {
                dao.rollback();
            }
            throw e;
        }
        return deleted;
    }

    /**
     * Deletes a {@code Node} defined by the pair child/parent item.
     * 
     * 
     * @param domain
     *            {@code Domain} of the {@code Node}
     * @param user
     *            {@code User} who deletes a {@code Node}
     * @param child
     *            child - CI or CItype
     * @param parent
     *            parent - CI or CItype
     * @return {@code true} if the node exists and deletion was successful.
     *         {@code false} if the node doesn't exist.
     * @throws Exception
     */
    public static boolean delete(Domain domain, User user, Node child, Node parent) throws Exception {
        return delete(null, domain, user, child, parent);
    }

    @Override
    public ClassID getCID() {
        return ClassID.NODE;
    }

    @Override
    public Class<?> getClass(Field f) {
        if (nodeType == NodeType.CI) {
            return CI.class;
        }
        return CItype.class;
    }

}
