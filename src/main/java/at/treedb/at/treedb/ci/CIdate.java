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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

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
 * {@code CI} data container for a Java {@code Date} value.
 * </p>
 * 
 * @author Peter Sauer
 * 
 */
@SuppressWarnings("serial")
// LDOR: 30.11.2013
@Entity
@Table(indexes = { @Index(columnList = "ci"), @Index(columnList = "uiElement"), @Index(columnList = "histId") })
public class CIdate extends CIdata {
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "m_date")
    private Date date;

    /**
     * Field access name for updates by Java reflection.
     */
    public enum Fields {
        /**
         * reflection field name
         */
        date;
    }

    protected CIdate() {
    }

    private CIdate(int ci, int ciType, long uiElement, Date date) {
        super(ci, ciType, uiElement);
        this.date = date;
    }

    /**
     * Creates a container for a {@code Date} value.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param domain
     *            {@code Domain} of the data element
     * @param user
     *            creator of the {@code CIdate}
     * @param ci
     *            ID of the {@code CI}
     * @param ciType
     *            ID of the {@code CItype}
     * @param uiElement
     *            ID of the {@code UIelement}
     * @param date
     *            {@code Date} date value
     * @return {@code CIdate} object
     * @throws Exception
     */
    public static CIdate create(DAOiface dao, Domain domain, User user, @DBkey(value = CI.class) int ci,
            @DBkey(value = CItype.class) int ciType, @DBkey(value = ClassSelector.class) long uiElement, Date date)
                    throws Exception {
        CIdate d = new CIdate(ci, ciType, uiElement, date);
        Base.save(dao, domain, user, d);
        return d;
    }

    /**
     * Loads a {@code CIdate}.
     * 
     * @param id
     *            ID of the {@code CIdate}
     * @return {@code CIdate} object
     * @throws Exception
     */
    public static CIdate load(@DBkey(value = CIdate.class) int id) throws Exception {
        return (CIdate) Base.load(null, CIdate.class, id, null);
    }

    /**
     * Loads a {@code CIdate}.
     * 
     * @param ci
     *            ID of the {@code CI}
     * @param uiElement
     *            ID of the {@code UIelement}
     * @param date
     *            temporal bound
     * @return {@code CIdate} object
     * @throws Exception
     */
    public static CIdate load(@DBkey(value = CI.class) int ci, @DBkey(value = ClassSelector.class) long uiElement,
            Date date) throws Exception {
        return (CIdate) CIdata.load(null, CIdate.class, ci, uiElement, null, date);
    }

    /**
     * Loads a {@code CIdate}.
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
    public static CIdate load(DAOiface dao, @DBkey(value = CI.class) int ci,
            @DBkey(value = ClassSelector.class) long uiElement, Date date) throws Exception {
        return (CIdate) CIdata.load(dao, CIdate.class, ci, uiElement, null, date);
    }

    /**
     * Creates or updates a {@code CIdate}.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param domain
     *            {@code Domain} of the data element
     * @param user
     *            user who performs the object update/creation
     * @param ci
     *            ID of the {@code CI}
     * @param ciType
     *            ID of the {@code CItype}
     * @param uiElement
     *            ID of the {@code UIelement}
     * @param value
     *            {@code Date} date value
     * @return {@code CIdate} object
     * @throws Exception
     */
    public static CIdate createOrUpdate(DAOiface dao, Domain domain, User user, @DBkey(value = CI.class) int ci,
            @DBkey(value = CItype.class) int ciType, @DBkey(value = ClassSelector.class) long uiElement, Date value)
                    throws Exception {
        CIdate d = (CIdate) load(dao, CIdate.class, ci, uiElement, null, null);
        if (d == null) {
            d = new CIdate(ci, ciType, uiElement, value);
            Base.save(dao, domain, user, d);
        } else {
            UpdateMap map = new UpdateMap(CIdate.Fields.class);
            map.addDate(CIdate.Fields.date, value);
            Base.update(dao, user, d, map);
        }
        return d;
    }

    /**
     * Returns a {@code Date} value.
     * 
     * @return {@code Date} object
     */
    public Date getData() {
        return date;
    }

    /**
     * Updates a {@code CIdate}.
     * 
     * @param user
     *            user who performs the object update
     * @param value
     *            {@code Date} update value
     * @throws Exception
     */
    public void update(User user, Date value) throws Exception {
        UpdateMap map = new UpdateMap(CIdate.Fields.class);
        map.addDate(CIdate.Fields.date, value);
        Base.update(user, this, map);
    }

    @Override
    public ClassID getCID() {
        return ClassID.CIDATE;
    }

}
