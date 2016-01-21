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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.Table;

import at.treedb.db.Base;
import at.treedb.db.ClassID;
import at.treedb.db.ClassSelector;
import at.treedb.db.DAOiface;
import at.treedb.db.DBkey;
import at.treedb.db.Detach;
import at.treedb.db.UpdateMap;
import at.treedb.domain.Domain;
import at.treedb.user.User;

/**
 * <p>
 * {@code CI} data container for binary data {@code byte[]}.
 * </p>
 * 
 * @author Peter Sauer
 * 
 */

@SuppressWarnings("serial")
@Entity
@Table(indexes = { @Index(columnList = "ci"), @Index(columnList = "uiElement"), @Index(columnList = "histId") })
public class CIblob extends CIdata {
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

    protected CIblob() {
    }

    private CIblob(int ci, int ciType, long uiElement, byte[] binValue) {
        super(ci, ciType, uiElement);
        binaryValue = binValue;
    }

    /**
     * Creates a container for binary data.
     * 
     * @param domain
     *            {@code Domain} of the data element
     * @param user
     *            creator of the {@code CIblob}
     * @param ci
     *            ID of the {@code CI}
     * @param ciType
     *            ID of the {@code CItype}
     * @param uiElement
     *            ID of the {@code UIelement}
     * @param value
     *            binary data
     * @return {@code CIblob} object
     * @throws Exception
     */
    public static CIblob create(DAOiface dao, Domain domain, User user, @DBkey(CI.class) int ci,
            @DBkey(CItype.class) int ciType, @DBkey(ClassSelector.class) long uiElement, byte[] value)
                    throws Exception {
        CIblob blob = new CIblob(ci, ciType, uiElement, value);
        Base.save(dao, domain, user, blob);
        return blob;
    }

    /**
     * Loads a {@code CIblob}.
     * 
     * @param id
     *            ID of the {@code CIblob}
     * @return {@code CIblob} object
     * @throws Exception
     */
    public static CIblob load(@DBkey(value = CIblob.class) int id) throws Exception {
        return (CIblob) Base.load(null, CIblob.class, id, null);
    }

    /**
     * Loads a {@code CIblob}.
     * 
     * @param ci
     *            ID of the {@code CI}
     * @param uiElement
     *            ID of the {@code UIelement}
     * @param date
     *            temporal bound
     * @return {@code CIbigDecimal} object
     * @throws Exception
     */
    public static CIblob load(@DBkey(value = CI.class) int ci, @DBkey(ClassSelector.class) long uiElement, Date date)
            throws Exception {
        return (CIblob) CIdata.load(null, CIblob.class, ci, uiElement, null, date);
    }

    /**
     * Loads a {@code CIblob}.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param ci
     *            ID of the {@code CI}
     * @param uiElement
     *            ID of the {@code UIelement}
     * @param date
     *            temporal bound
     * @return {@code CIbigDecimal} object
     * @throws Exception
     */
    public static CIblob load(DAOiface dao, @DBkey(value = CI.class) int ci, @DBkey(ClassSelector.class) long uiElement,
            Date date) throws Exception {
        return (CIblob) CIdata.load(dao, CIblob.class, ci, uiElement, null, date);
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
     * @param ci
     *            ID of the {@code CI}
     * @param ciType
     *            ID of the {@code CItype}
     * @param uiElement
     *            ID of the {@code UIelement}
     * @param value
     *            binary data
     * @return {@code CIblob} object
     * @throws Exception
     */
    public static CIblob createOrUpdate(DAOiface dao, Domain domain, User user, @DBkey(CI.class) int ci,
            @DBkey(CItype.class) int ciType, @DBkey(ClassSelector.class) long uiElement, byte[] value)
                    throws Exception {
        CIblob blob = (CIblob) load(dao, CIblob.class, ci, uiElement, null, null);
        if (blob == null) {
            blob = new CIblob(ci, ciType, uiElement, value);
            Base.save(dao, domain, user, blob);
        } else {
            UpdateMap map = new UpdateMap(CIblob.Fields.class);
            map.addBinary(CIblob.Fields.binaryValue, value);
            Base.update(dao, user, blob, map);
        }
        return blob;
    }

    /**
     * Returns the binary data.
     * 
     * @return binary data
     */
    public byte[] getData() {
        return binaryValue;
    }

    /**
     * Sets the binary data to zero.
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
        UpdateMap map = new UpdateMap(CIblob.Fields.class);
        map.addBinary(CIblob.Fields.binaryValue, binaryValue);
        Base.update(user, this, map);
    }

    @Override
    public ClassID getCID() {
        return ClassID.CIBLOB;
    }

}
