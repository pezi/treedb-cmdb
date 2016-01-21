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
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.Transient;

import at.treedb.db.Base;
import at.treedb.db.ClassID;
import at.treedb.db.DAO;
import at.treedb.db.DAOiface;
import at.treedb.db.DBkey;
import at.treedb.db.Update;
import at.treedb.db.UpdateMap;
import at.treedb.domain.Domain;
import at.treedb.i18n.Istring;
import at.treedb.user.User;

/**
 * <p>
 * The {@code CI} (<i>configuration item</i>) is the base item of the CMDB
 * (Change Management Data Base). Later this concept of a CMDB<sup>1</sup> was
 * extended to a more general approach/database - named TreeDB. In spite of of
 * this fact - the base classes use a CMDB related naming convention.
 * <p>
 * <sup>1</sup> <a href="http://en.wikipedia.org/wiki/CMDB">CMDB - Wikipedia</a>
 * 
 * @author Peter Sauer
 */

// LDOR: 2014-03-27
// Hint LDOR: Last date of review
@SuppressWarnings("serial")
@Entity
public class CI extends Base implements Cloneable, Connectable {

    public enum Fields {
        alias, name, contextCI
    }

    // @Callback
    private String name; // CI's name - must be unique
    @DBkey(Istring.class)
    private int alias; // alternative name of the CI
    @DBkey(CItype.class)
    private int type; // type of the CI - e.g. server, data base, etc.
    @DBkey(CI.class)
    private int contextCI;
    @Transient
    private HashSet<Connectable> children = new HashSet<Connectable>();
    @Transient
    private HashSet<Connectable> parents = new HashSet<Connectable>();
    // CI is in memory
    private boolean inMemory;

    /**
     * Creates a {@code CI} object.
     * 
     * @param domain
     *            {@code Domain} of the {@code CI} object
     * @param user
     *            creator of the {@code CI} object, can be null
     * @param type
     *            type of the {@code CI } object, e.g. server item
     * @param name
     *            name of the {@code CI} object.
     * @param alias
     *            alias name (I18N string) of the {@code CI} object, can be null
     * @return {@code CI} object
     * @throws Exception
     */
    public static CI create(Domain domain, User user, CItype type, String name, String alias) throws Exception {
        CI ci = new CI(domain, type, name);
        save(domain, user, ci, alias);
        return ci;
    }

    /**
     * Creates a {@code CI} object.
     * 
     * @param domain
     *            {@code Domain} of the {@code CI} object
     * @param user
     *            creator of the {@code CI} object, can be null
     * @param type
     *            type of the {@code CI } object, e.g. server item
     * @param name
     *            name of the {@code CI} object.
     * @param alias
     *            alias name (I18N string) of the {@code CI} object, can be null
     * @param inMemory
     *            {@code true} if the CI resides in memory, {@code false} if not
     * @param ciContext
     *            This optional parameter is used for logical connection between
     *            the {@code ciContext} CI and the new created CI
     * @return {@code CI} object
     * @throws Exception
     */
    public static CI create(Domain domain, User user, CItype type, String name, String alias, boolean inMemory,
            CI ciContext) throws Exception {
        CI ci = new CI(domain, type, name, inMemory, ciContext);
        save(domain, user, ci, alias);
        return ci;
    }

    /**
     * Created a dummy CI.
     * 
     * @return
     */
    public static CI createDummy() {
        return new CI();
    }

    /**
     * Creates a {@code CI} object.
     * 
     * @param dao
     *            {@code DAOiface} (data access object), can be null
     * @param domain
     *            {@code Domain} of the {@code CI} object.
     * @param user
     *            creator of the {@code CI} object, can be null
     * @param type
     *            type of the {@code CI} object , e.g. server, IP address
     * @param name
     *            name of the {@code CI} object
     * @param alias
     *            alias name (I18N string) of the {@code CI} object
     * @return {@code CI} object
     * @throws Exception
     */
    public static CI create(DAOiface dao, Domain domain, User user, CItype type, String name, String alias)
            throws Exception {
        CI ci = new CI(domain, type, name);
        save(dao, domain, user, ci, alias);
        return ci;
    }

    /**
     * Creates a {@code CI} object.
     * 
     * @param dao
     *            {@code DAOiface} (data access object), can be null
     * @param domain
     *            {@code Domain} of the {@code CI} object.
     * @param user
     *            creator of the {@code CI} object, can be null
     * @param type
     *            type of the {@code CI} object , e.g. server, IP address
     * @param name
     *            name of the {@code CI} object
     * @param alias
     *            alias name (I18N string) of the {@code CI} object
     * @param inMemory
     *            {@code true} if this CI is an in memory object, {@code false}
     *            if not
     * @return {@code CI} object
     * @throws Exception
     */
    public static CI create(DAOiface dao, Domain domain, User user, CItype type, String name, String alias,
            boolean inMemory) throws Exception {
        CI ci = new CI(domain, type, name, inMemory, null);
        save(dao, domain, user, ci, alias);
        return ci;
    }

    /**
     * Creates a {@code CI} object.
     * 
     * @param dao
     *            {@code DAOiface} (data access object), can be null
     * @param domain
     *            {@code Domain} of the {@code CI} object
     * @param user
     *            creator of the {@code CI} object, can be null
     * @param type
     *            type of the {@code CI} object , e.g. server, IP address
     * @param name
     *            name of the {@code CI} object
     * @param alias
     *            alias name (I18N string) of the {@code CI} object
     * @param inMemory
     *            {@code true} if this CI is an in memory object, {@code false}
     *            if not
     * @param ciContext
     *            this optional CI describes a user defined context/relationship
     *            with the created CI
     * @return {@code CI} object
     * @throws Exception
     */
    public static CI create(DAOiface dao, Domain domain, User user, CItype type, String name, String alias,
            boolean inMemory, CI ciContext) throws Exception {
        CI ci = new CI(domain, type, name, inMemory, ciContext);
        save(dao, domain, user, ci, alias);
        return ci;
    }

    // protected, and not private to satisfy OpenJPA
    protected CI() {
    }

    /**
     * Constructor
     * 
     * @param domain
     *            {@code Domain} of the {@code CI} object
     * @param type
     *            type of the {@code CI} object , e.g. server, IP address
     * @param name
     *            name of the {@code CI} object
     * @throws NullPointerException
     */
    private CI(Domain domain, CItype type, String name) throws NullPointerException {
        Objects.requireNonNull(domain, "CI.CI(): parameter domain can't be null");
        Objects.requireNonNull(type, "CI.CI(): parameter type can't be null");
        Objects.requireNonNull(name, "CI.CI(): parameter name can't be null");
        this.name = name;
        this.inMemory = true;
        this.type = type.getHistId();
        this.setDomain(domain.getDomain());
    }

    /**
     * Constructor
     * 
     * @param domain
     *            {@code Domain} of the {@code CI} object
     * @param type
     *            type of the {@code CI} object , e.g. server, IP address
     * @param name
     *            name of the {@code CI} object
     * @param inMemory
     *            inMemory {@code true} if this CI is an in memory object,
     *            {@code false} if not
     * @param contextCI
     *            ciContext this optional CI describes a user defined
     *            context/relationship with the created CI
     * @throws NullPointerException
     */
    private CI(Domain domain, CItype type, String name, boolean inMemory, CI contextCI) throws NullPointerException {
        Objects.requireNonNull(domain, "CI.CI(): parameter domain can't be null");
        Objects.requireNonNull(type, "CI.CI(): parameter type can't be null");
        this.name = name;
        this.type = type.getHistId();
        if (contextCI != null) {
            this.contextCI = contextCI.getHistId();
        }
        this.inMemory = inMemory;
        this.setDomain(domain.getDomain());
    }

    /**
     * Returns the name of the {@code CI} object.
     * 
     * @return name of the {@code CI}
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Returns the alternative name of the {@code CI}.
     * 
     * @return ID of the {@code Istring}
     */
    public @DBkey(Istring.class) int getAlias() {
        return alias;
    }

    /**
     * Sets the type of the {@code CI}.
     * 
     * @param type
     *            type of the {@code CI}
     */
    public void setCIType(CItype type) {
        this.type = type.getHistId();
    }

    /**
     * Returns the type of the {@code CI}.
     * 
     * @return ID of the {@code CItype}
     */
    public @DBkey(CItype.class) int getCIType() {
        return type;
    }

    /**
     * Returns the type of the {@code CI}.
     * 
     * @return {@code CItype}
     */
    public CItype getCItypeObj() {
        return Domain.get(domain).getCItypeMap().get(type);
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
            throw new Exception("CI.getParent(): No parent available");
        }
        if (parents.size() > 1) {
            throw new Exception("CI.getParent(): Multiple parents available");
        }
        return parents.toArray(new Connectable[1])[0];
    }

    private static void save(Domain domain, User user, CI ci, String alias) throws Exception {
        save(null, domain, user, ci, alias);
    }

    private static void save(DAOiface dao, Domain domain, User user, CI ci, String alias) throws Exception {
        Objects.requireNonNull(domain, "CI.save(): parameter domain can't be null");
        Objects.requireNonNull(domain, "CI.save(): parameter ci can't be null");

        boolean localDAO = false;
        if (dao == null) {
            dao = DAO.getDAO();
            localDAO = true;
        }
        try {
            if (localDAO) {
                dao.beginTransaction();
            }
            Istring istr = null;
            if (alias != null) {
                istr = Istring.create(dao, domain, user, ci.getCID(), alias, domain.getLanguage());
                ci.alias = istr.getHistId();
            }
            Base.save(dao, domain, user, ci);
            if (istr != null) {
                istr.setCI(ci.getHistId());
            }
            if (localDAO) {
                dao.endTransaction();
            }
        } catch (Exception e) {
            if (localDAO) {
                dao.rollback();
            }
            throw e;
        }
    }

    @Override
    public CI clone() throws CloneNotSupportedException {
        CI c = (CI) super.clone();
        c.setDBid(0);
        c.resetTransactionVersion();
        return c;
    }

    /**
     * Updates a {@code CI}.
     * 
     * @param user
     *            {@code User} who modifies the {@code CI} object
     * @param map
     *            {@code UpdateMap} map containing the changes
     * @throws Exception
     */
    public void update(User user, UpdateMap map) throws Exception {
        Base.update(user, this, map);
    }

    @Override
    public Object checkConstraints(DAOiface dao, UpdateMap map) throws Exception {
        String oldName = null;
        Domain d = Domain.get(domain);
        if (d.isUniqueCInames() && map != null) {
            Update up = map.get(CI.Fields.name);
            if (up != null) {
                CI ci = Domain.get(domain).getCI(up.getString());
                if (ci != null) {
                    if (ci.getHistId() != this.getHistId()) {
                        throw new Exception("CI.checkConstraints(): CI name not unique");
                    }
                }
                oldName = this.name;
            }
        }
        return oldName;
    }

    @Override
    protected void callbackUpdate(DAOiface dao, User user, UpdateMap map, Object info) throws Exception {
        Domain d = Domain.get(domain);
        if (d.isUniqueCInames() && info != null) {
            String oldName = (String) info;
            d.updateCIname(this, oldName);
        }
    }

    /**
     * Updates a {@code CI}.
     * 
     * @param user
     *            {@code User} who modifies the CI
     * @param ci
     *            {@code CI}
     * @param map
     *            {@code UpdateMap} containing the changes
     * @throws Exception
     */
    public static void update(User user, CI ci, UpdateMap map) throws Exception {
        Objects.requireNonNull(map, "CI.update(): parameter map can't be null");
        update(null, user, ci, map);
    }

    /**
     * Updates a {@code CI}.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param user
     *            {@code User} who modifies the {@code CI}
     * @param ci
     *            {@code CI}
     * @param map
     *            {@code UpdateMap} map containing the changes
     * @throws Exception
     */
    public static void update(DAOiface dao, User user, CI ci, UpdateMap map) throws Exception {
        Objects.requireNonNull(ci, "CI.update(): parameter ci can't be null");
        Base.update(dao, user, ci, map);
    }

    /**
     * Updates a {@code CI}.
     * 
     * @param user
     *            {@code User} who modifies the {@code CI}
     * @param id
     *            ID of the {@code CI}
     * @param map
     *            {@code UpdateMap} map containing the changes
     * @throws Exception
     */
    public static void update(User user, int id, UpdateMap map) throws Exception {
        if (id < 1) {
            throw new Exception("CI.update(): paramter id must be a postive number");
        }
        update(user, id, CI.class, map);
    }

    @Override
    public ClassID getCID() {
        return ClassID.CI;
    }

    @Override
    public String getCanonicalName() {
        return name;
    }

    @Override
    public TYPE getConnectionType() {
        return TYPE.CI;
    }

    @Override
    public int getId() {
        return getHistId();
    }

    @Override
    public int getIcon() {
        return getCItypeObj().getIcon();
    }

    /**
     * Returns the optional context CI.
     * 
     * @return
     */
    public int getContextCI() {
        return contextCI;
    }

    /**
     * Returns if the CI is an in-memory object.
     * 
     * @return {@code true} if the CI is an in-memory object, {@code false} if
     *         not
     */
    public boolean isInMemory() {
        return inMemory;
    }

}