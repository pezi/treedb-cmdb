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

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

import at.treedb.db.Base;
import at.treedb.db.ClassID;
import at.treedb.db.ClassSelector;
import at.treedb.db.DAOiface;
import at.treedb.db.DBkey;
import at.treedb.db.UpdateMap;
import at.treedb.domain.Domain;
import at.treedb.user.User;

/**
 * <p>
 * {@code CI} data container for a boolean value.
 * </p>
 * 
 * @author Peter Sauer
 * 
 */
// LDOR: 2013-11-29
@SuppressWarnings("serial")

@Entity
@Table(indexes = { @Index(columnList = "ci"), @Index(columnList = "uiElement"), @Index(columnList = "histId") })
public class CIboolean extends CIdata {
    private boolean booleanValue;

    /**
     * Field access name for updates by Java reflection.
     */
    public enum Fields {
        /**
         * reflection field name
         */
        booleanValue;
    }

    protected CIboolean() {
    }

    private CIboolean(int ci, int ciType, long uiElement, boolean b) {
        super(ci, ciType, uiElement);
        booleanValue = b;
    }

    /**
     * Creates a container for a boolean value.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param domain
     *            {@code Domain} of the data element
     * @param user
     *            creator of the {@code CIboolean}
     * @param ci
     *            ID of the {@code CI}
     * @param ciType
     *            ID of the {@code ciType}
     * @param uiElement
     *            ID of the {@code UIelement}
     * @param value
     *            boolean value
     * @return {@code CIboolean} object
     * @throws Exception
     */
    public static CIboolean create(DAOiface dao, Domain domain, User user, @DBkey(CI.class) int ci,
            @DBkey(CItype.class) int ciType, @DBkey(ClassSelector.class) long uiElement, boolean value)
                    throws Exception {
        CIboolean b = new CIboolean(ci, ciType, uiElement, value);
        Base.save(dao, domain, user, b);
        return b;
    }

    /**
     * Loads a {@code CIboolean}.
     * 
     * @param id
     *            ID of the {@code CIboolean}
     * @return {@code CIboolean} object
     * @throws Exception
     */
    public static CIboolean load(@DBkey(CIboolean.class) int id) throws Exception {
        return (CIboolean) Base.load(null, CIboolean.class, id, null);
    }

    /**
     * Loads a {@code CIboolean}.
     * 
     * @param ci
     *            ID of the {@code CI}
     * @param uiElement
     *            ID of the {@code UIelement}
     * @param date
     *            temporal bound
     * @return {@code CIbooelan} object
     * @throws Exception
     */
    public static CIboolean load(@DBkey(CI.class) int ci, @DBkey(ClassSelector.class) long uiElement, Date date)
            throws Exception {
        return (CIboolean) CIdata.load(null, CIboolean.class, ci, uiElement, null, date);
    }

    /**
     * Loads a {@code CIboolean}.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param ci
     *            ID of the {@code CI}
     * @param uiElement
     *            ID of the {@code UIelement}
     * @param date
     *            temporal bound
     * @return {@code CIboolean} object
     * @throws Exception
     */
    public static CIboolean load(DAOiface dao, @DBkey(CI.class) int ci, @DBkey(ClassSelector.class) long uiElement,
            Date date) throws Exception {
        return (CIboolean) CIdata.load(dao, CIboolean.class, ci, uiElement, null, date);
    }

    /**
     * Creates or updates a {@code CIbooelan}
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param domain
     *            {@code Domain} of the data element
     * @param user
     *            {@code User} who performs the object update/creation
     * @param ci
     *            ID of the {@code CI}
     * @param ciType
     *            ID of the {@code CItype}
     * @param uiElement
     *            ID of the {@code UIelement}
     * @param value
     *            boolean value
     * @return {@code CIboolean} object
     * @throws Exception
     */
    public static CIboolean createOrUpdate(DAOiface dao, Domain domain, User user, @DBkey(CI.class) int ci,
            @DBkey(CItype.class) int ciType, @DBkey(ClassSelector.class) long uiElement, boolean value)
                    throws Exception {
        CIboolean b = (CIboolean) load(dao, CIboolean.class, ci, uiElement, null, null);
        if (b == null) {
            b = new CIboolean(ci, ciType, uiElement, value);
            Base.save(dao, domain, user, b);
        } else {
            UpdateMap map = new UpdateMap(CIboolean.Fields.class);
            map.add(CIboolean.Fields.booleanValue, value);
            Base.update(dao, user, b, map);
        }
        return b;
    }

    /**
     * Returns a boolean value.
     * 
     * @return boolean value
     */
    public Boolean getData() {
        return booleanValue;
    }

    /**
     * Generic data access.
     * 
     * @return Object data value
     */
    // @Override
    // public Object getData() {
    // return booleanValue;
    // }

    /**
     * Updates a {@code CIbooelan}.
     * 
     * @param user
     *            {@code User} who performs the object update
     * @param b
     *            boolean value
     * @throws Exception
     */
    public void update(User user, boolean b) throws Exception {
        UpdateMap map = new UpdateMap(CIboolean.Fields.class);
        map.addBoolean(CIboolean.Fields.booleanValue, b);
        Base.update(user, this, map);
    }

    @Override
    public ClassID getCID() {
        return ClassID.CIBOOLEAN;
    }
}
