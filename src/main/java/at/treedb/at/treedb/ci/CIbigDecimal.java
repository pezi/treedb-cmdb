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

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Index;

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
 * {@code CI} data container for a Java {@code BigDecimal} value. Used for
 * finance/money calculations.
 * </p>
 * 
 * @author Peter Sauer
 * 
 */
// LDOR: 2013-11-29
@Entity
@Table(indexes = { @Index(columnList = "ci"), @Index(columnList = "uiElement"), @Index(columnList = "histId") })
public class CIbigDecimal extends CIdata {
    private static final long serialVersionUID = 1L;
    // TODO: discussion of these values
    @Column(precision = 24, scale = 8)
    private BigDecimal bigDecimalValue;

    /**
     * Field access name for updates by Java reflection.
     */
    public enum Fields {
        /**
         * reflection field name
         */
        bigDecimalValue;
    }

    protected CIbigDecimal() {
    }

    private CIbigDecimal(@DBkey(CI.class) int ci, @DBkey(CItype.class) int ciType,
            @DBkey(ClassSelector.class) long uiElement, BigDecimal value) {
        super(ci, ciType, uiElement);
        this.bigDecimalValue = value;
    }

    /**
     * Creates a container for a {@code BigDecimal} value.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param domain
     *            {@code Domain} of the data element
     * @param user
     *            creator of the {@code CIbigDecimal}
     * @param ci
     *            ID of the {@code CI}
     * @param ciType
     *            ID of the {@code CItype}
     * @param uiElement
     *            ID of the {@code UIelement}
     * @param value
     *            {@code BigDecimal} value
     * @return {@code CIbigDecimal} object
     * @throws Exception
     */
    public static CIbigDecimal create(DAOiface dao, Domain domain, User user, @DBkey(CI.class) int ci,
            @DBkey(CItype.class) int ciType, @DBkey(ClassSelector.class) long uiElement, BigDecimal value)
                    throws Exception {
        Objects.requireNonNull(domain, "CIbigDecimal.create(): parameter domain can't be null");
        CIbigDecimal big = new CIbigDecimal(ci, ciType, uiElement, value);
        Base.save(dao, domain, user, big);
        return big;
    }

    /**
     * Loads a {@code CIbigDecimal}.
     * 
     * @param id
     *            ID of the {@code CIbigDecimal}
     * @return {@code CIbigDecimal} object
     * @throws Exception
     */
    public static CIbigDecimal load(@DBkey(CIbigDecimal.class) int id) throws Exception {
        return (CIbigDecimal) Base.load(null, CIbigDecimal.class, id, null);
    }

    /**
     * Loads a {@code CIbigDecimal}.
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
    public static CIbigDecimal load(@DBkey(value = CI.class) int ci, @DBkey(value = ClassSelector.class) long uiElement,
            Date date) throws Exception {
        return (CIbigDecimal) CIdata.load(null, CIbigDecimal.class, ci, uiElement, null, date);
    }

    /**
     * Loads a {@code CIbigDecimal}.
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
    public static CIbigDecimal load(DAOiface dao, @DBkey(value = CI.class) int ci,
            @DBkey(value = ClassSelector.class) long uiElement, Date date) throws Exception {
        return (CIbigDecimal) CIdata.load(dao, CIbigDecimal.class, ci, uiElement, null, date);
    }

    /**
     * Creates or updates a {@code CIbigDecimal}.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * 
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
     *            {@code BigDecimal} value
     * @return {@code CIbigDecimal} object
     * @throws Exception
     */
    public static CIbigDecimal createOrUpdate(DAOiface dao, Domain domain, User user, @DBkey(CI.class) int ci,
            @DBkey(CItype.class) int ciType, @DBkey(ClassSelector.class) long uiElement, BigDecimal value)
                    throws Exception {
        CIbigDecimal d = (CIbigDecimal) load(dao, CIbigDecimal.class, ci, uiElement, null, null);
        if (d == null) {
            d = new CIbigDecimal(ci, ciType, uiElement, value);
            Base.save(dao, domain, user, d);
        } else {
            UpdateMap map = new UpdateMap(CIbigDecimal.Fields.class);
            map.addBigDecimal(CIbigDecimal.Fields.bigDecimalValue, value);
            Base.update(dao, user, d, map);
        }
        return d;
    }

    /**
     * Returns the {@code BigDecimal} value.
     * 
     * @return {@code BigDecimal} value
     */
    public BigDecimal getData() {
        return bigDecimalValue;
    }

    /**
     * Updates a {@code CIbigDecimal}.
     * 
     * @param user
     *            user who performs the update operation
     * @param big
     *            {@code BigDecimal} update value
     * @throws Exception
     */
    public void update(User user, BigDecimal big) throws Exception {
        UpdateMap map = new UpdateMap(CIbigDecimal.Fields.class);
        map.addBigDecimal(CIbigDecimal.Fields.bigDecimalValue, big);
        Base.update(user, this, map);
    }

    @Override
    public ClassID getCID() {
        return ClassID.CIBIGDECIMAL;
    }
}
