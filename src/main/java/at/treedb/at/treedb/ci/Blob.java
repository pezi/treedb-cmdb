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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.Table;

import at.treedb.db.Base;
import at.treedb.db.ClassID;
import at.treedb.db.DAOiface;
import at.treedb.db.DBkey;
import at.treedb.db.Detach;
import at.treedb.db.UpdateMap;
import at.treedb.domain.Domain;
import at.treedb.user.User;

/**
 * <p>
 * {@code CI} data container for binary data {@code byte[]}. <br>
 * </p>
 * 
 * @author Peter Sauer
 * 
 */
// LDOR: 2013-11-29

@SuppressWarnings("serial")
@Entity
@Table(name = "m_blob", indexes = { @Index(columnList = "histId") })
public class Blob extends Base implements Cloneable {
    private ClassID classId;
    @Detach
    @Lob
    @Column(length = 524288000) // 0.5GB
    private byte[] binaryValue;

    /**
     * Field access name for updates by Java reflection.
     */
    public enum Fields {
        /**
         * reflection field name
         */
        binaryValue;
    }

    protected Blob() {
    }

    private Blob(ClassID classId, byte[] binValue) {
        this.classId = classId;
        this.binaryValue = binValue;
    }

    /**
     * Creates a container for binary data.
     * 
     * @param domain
     *            {@code Domain} of the data element
     * @param user
     *            creator of the {@code Blob}
     * @param classId
     *            owner class of the binary data
     * @param value
     *            binary data
     * @return {@code Blob} object
     * @throws Exception
     */
    public static Blob create(DAOiface dao, Domain domain, User user, ClassID classId, byte[] value) throws Exception {
        Blob blob = new Blob(classId, value);
        Base.save(dao, domain, user, blob);
        return blob;
    }

    /**
     * Loads a {@code Blob}.
     * 
     * @param id
     *            ID of the {@code Blob}
     * @return {@code CIblob} object
     * @throws Exception
     */
    public static Blob load(@DBkey(value = Blob.class) int id) throws Exception {
        return (Blob) Base.load(null, Blob.class, id, null);
    }

    /**
     * Creates or updates a {@code CIblob}
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param domain
     *            {@code Domain} of the data element
     * @param user
     *            user {@code User} who performs the {@code CIblob}
     *            update/creation
     * @param value
     *            binary data
     * @return {@code CIblob} object
     * @throws Exception
     */
    public static Blob createOrUpdate(DAOiface dao, Domain domain, User user, ClassID classId,
            @DBkey(Blob.class) int id, byte[] value) throws Exception {
        Blob blob = (Blob) Base.load(null, Blob.class, id, null);
        if (blob == null) {
            blob = new Blob(classId, value);
            Base.save(dao, domain, user, blob);
        } else {
            UpdateMap map = new UpdateMap(Blob.Fields.class);
            map.addBinary(Blob.Fields.binaryValue, value);
            Base.update(dao, user, blob, map);
        }
        return blob;
    }

    /**
     * Returns the binary data.
     * 
     * @return binary data
     */
    public byte[] getBinaryData() {
        return binaryValue;
    }

    /**
     * Generic data access.
     * 
     * @return Object data value
     */

    /**
     * Sets the binary data to null.
     */
    public void resetBlob() {
        binaryValue = null;
    }

    /**
     * Updates a {@code CIblob}.
     * 
     * @param user
     *            {@code User} who performs the {@code CIblob} update
     * @param binaryValue
     *            update data
     * @throws Exception
     */
    public void update(User user, byte[] binaryValue) throws Exception {
        UpdateMap map = new UpdateMap(Blob.Fields.class);
        map.addBinary(Blob.Fields.binaryValue, binaryValue);
        Base.update(user, this, map);
    }

    /**
     * Returns the {@code ClassID} of the owner class
     * 
     * @return {@code ClassID}
     */
    public ClassID getOwnerCID() {
        return classId;
    }

    @Override
    public ClassID getCID() {
        return ClassID.BLOB;
    }
}
