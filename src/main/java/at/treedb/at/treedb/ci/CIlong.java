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
 * {@code CI} data container for a long value.
 * </p>
 * 
 * @author Peter Sauer
 * 
 */
@SuppressWarnings("serial")
// LDR: 25.08.2013
@Entity
@Table(indexes = { @Index(columnList = "ci"), @Index(columnList = "uiElement"), @Index(columnList = "histId") })
public class CIlong extends CIdata {
    private long longValue;

    /**
     * Field access name for updates by Java reflection.
     */
    public enum Fields {
        /**
         * reflection field name
         */
        longValue;
    }

    protected CIlong() {
    }

    public CIlong(int ci, int ciType, long uiElement, long l) {
        super(ci, ciType, uiElement);
        longValue = l;
    }

    /**
     * Creates a container for a long value.
     * 
     * @param dao
     *            {@code DAOiface} data access object
     * @param domain
     *            {@code Domain} of the data element
     * @param user
     *            creator of the {@code CIlong}
     * @param ci
     *            ID of the {@code CI}
     * @param uiElement
     *            ID of the {@code UIelement}
     * @param value
     *            long value
     * @return {@code CIlong} object
     * @throws Exception
     */
    public static CIlong create(DAOiface dao, Domain domain, User user, @DBkey(CI.class) int ci,
            @DBkey(CItype.class) int ciType, @DBkey(ClassSelector.class) long uiElement, long value) throws Exception {
        CIlong l = new CIlong(ci, ciType, uiElement, value);
        Base.save(dao, domain, user, l);
        return l;
    }

    /**
     * Loads a {@code CIlong}.
     * 
     * @param id
     *            ID of the {@code CIlong}
     * @return {@code CIlong} object
     * @throws Exception
     */
    public static CIlong load(@DBkey(value = CIlong.class) int id) throws Exception {
        return (CIlong) Base.load(null, CIlong.class, id, null);
    }

    /**
     * Loads a {@code CIlong}.
     * 
     * @param ci
     *            ID of the {@code CI}
     * @param uiElement
     *            ID of the {@code UIelement}
     * @param date
     *            temporal bound
     * @return {@code CIlong} object
     * @throws Exception
     */
    public static CIlong load(@DBkey(value = CI.class) int ci, @DBkey(ClassSelector.class) long uiElement, Date date)
            throws Exception {
        return (CIlong) CIdata.load(null, CIlong.class, ci, uiElement, null, date);
    }

    /**
     * Loads a {@code CIlong}.
     * 
     * @param dao
     *            {@code DAOiface} data access object
     * @param ci
     *            ID of the {@code CI}
     * @param uiElement
     *            ID of the {@code UIelement}
     * @param date
     *            temporal bound
     * @return {@code CIlong} object
     * @throws Exception
     */
    public static CIlong load(DAOiface dao, @DBkey(value = CI.class) int ci, @DBkey(ClassSelector.class) long uiElement,
            Date date) throws Exception {
        return (CIlong) CIdata.load(dao, CIlong.class, ci, uiElement, null, date);
    }

    /**
     * Creates or updates a {@code CIlong}
     * 
     * @param dao
     *            data access object
     * @param domain
     *            {@code Domain} of the data element
     * @param user
     *            {@code User} who performs the {@code CIlong} update/creation
     * @param ci
     *            ID of the {@code CI}
     * @param ciType
     *            ID of the {@code CItype}
     * @param uiElement
     *            ID of the {@code UIelement}
     * @param value
     *            long value
     * @return {@code CIlong} object
     * @throws Exception
     */
    public static CIlong createOrUpdate(DAOiface dao, Domain domain, User user, @DBkey(CI.class) int ci,
            @DBkey(CItype.class) int ciType, @DBkey(ClassSelector.class) long uiElement, long value) throws Exception {
        CIlong l = (CIlong) load(dao, CIlong.class, ci, uiElement, null, null);
        if (l == null) {
            l = new CIlong(ci, ciType, uiElement, value);
            Base.save(dao, domain, user, l);
        } else {
            UpdateMap map = new UpdateMap(CIlong.Fields.class);
            map.add(CIlong.Fields.longValue, value);
            Base.update(dao, user, l, map);
        }
        return l;
    }

    /**
     * Returns a long value.
     * 
     * @return long
     */
    public Long getData() {
        return longValue;
    }

    /**
     * Updates a {@code CIlong}.
     * 
     * @param user
     *            user {@code User} who performs the {@code CIlong} update
     * @param l
     *            long value
     * @throws Exception
     */
    public void update(User user, long l) throws Exception {
        UpdateMap map = new UpdateMap(CIlong.Fields.class);
        map.addLong(CIlong.Fields.longValue, l);
        Base.update(user, this, map);
    }

    @Override
    public ClassID getCID() {
        return ClassID.CILONG;
    }
}
