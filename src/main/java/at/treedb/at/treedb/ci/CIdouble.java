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
 * {@code CI} data container for a double value.
 * </p>
 * 
 * @author Peter Sauer
 * 
 */
@SuppressWarnings("serial")
// LDOR: 30.11.2013
@Entity
@Table(indexes = { @Index(columnList = "ci"), @Index(columnList = "uiElement"), @Index(columnList = "histId") })
public class CIdouble extends CIdata {
    private double doubleValue;

    /**
     * Field access name for updates by Java reflection.
     */
    public enum Fields {
        /**
         * reflection field name
         */
        doubleValue;
    }

    protected CIdouble() {
    }

    private CIdouble(int ci, int ciType, long uiElement, double value) {
        super(ci, ciType, uiElement);
        this.doubleValue = value;
    }

    /**
     * Creates a container for a double value.
     * 
     * @param domain
     *            {@code Domain} of the data element
     * @param user
     *            creator of the {@code CIdouble}
     * @param ci
     *            ID of the {@code CI}
     * @param ciType
     *            ID of the {@code CItype}
     * @param uiElement
     *            ID of the {@code UIelement}
     * @param value
     *            double value
     * @return {@code CIdouble} object
     * @throws Exception
     */
    public static CIdouble create(DAOiface dao, Domain domain, User user, @DBkey(CI.class) int ci,
            @DBkey(CItype.class) int ciType, @DBkey(ClassSelector.class) long uiElement, double value)
                    throws Exception {
        CIdouble d = new CIdouble(ci, ciType, uiElement, value);
        Base.save(dao, domain, user, d);
        return d;
    }

    /**
     * Loads a {@code CIdouble}.
     * 
     * @param id
     *            ID of the {@code CIdouble}
     * @return {@code CIdouble} object
     * @throws Exception
     */
    public static CIdouble load(@DBkey(CIdouble.class) int id) throws Exception {
        return (CIdouble) Base.load(null, CIdouble.class, id, null);
    }

    /**
     * Loads a {@code CIdouble}.
     * 
     * @param ci
     *            ID of the {@code CI}
     * @param uiElement
     *            ID of the {@code UIelement}
     * @param date
     *            temporal bound
     * @return {@code CIdouble} object
     * @throws Exception
     */
    public static CIdouble load(@DBkey(CI.class) int ci, @DBkey(ClassSelector.class) long uiElement, Date date)
            throws Exception {
        return (CIdouble) CIdata.load(null, CIdouble.class, ci, uiElement, null, date);
    }

    /**
     * Loads a {@code CIdouble}.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param ci
     *            ID of the {@code CI}
     * @param uiElement
     *            ID of the {@code UIelement}
     * @param date
     *            temporal bound
     * @return {@code CIdoublw} object
     * @throws Exception
     */
    public static CIdouble load(DAOiface dao, @DBkey(CI.class) int ci, @DBkey(ClassSelector.class) long uiElement,
            Date date) throws Exception {
        return (CIdouble) CIdata.load(dao, CIdouble.class, ci, uiElement, null, date);
    }

    /**
     * Creates or updates a {@code CIdouble}
     * 
     * @param dao
     * @code DAOiface} (data access object)
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
     *            double value
     * @return {@code CIdouble} object
     * @throws Exception
     */
    public static CIdouble createOrUpdate(DAOiface dao, Domain domain, User user, @DBkey(CI.class) int ci,
            @DBkey(CItype.class) int ciType, @DBkey(ClassSelector.class) long uiElement, double value)
                    throws Exception {
        CIdouble d = (CIdouble) load(dao, CIdouble.class, ci, uiElement, null, null);
        if (d == null) {
            d = new CIdouble(ci, ciType, uiElement, value);
            Base.save(dao, domain, user, d);
        } else {
            UpdateMap map = new UpdateMap(CIdouble.Fields.class);
            map.addDouble(CIdouble.Fields.doubleValue, value);
            Base.update(dao, user, d, map);
        }
        return d;
    }

    /**
     * Returns a double value.
     * 
     * @return double value
     */
    public Double getData() {
        return doubleValue;
    }

    /**
     * Updates a {@code CIdouble}.
     * 
     * @param user
     *            user who performs the update operation
     * @param d
     *            double value
     * @throws Exception
     */
    public void update(User user, double d) throws Exception {
        UpdateMap map = new UpdateMap(CIdouble.Fields.class);
        map.addDouble(CIdouble.Fields.doubleValue, d);
        Base.update(user, this, map);
    }

    @Override
    public ClassID getCID() {
        return ClassID.CIDOUBLE;
    }
}
