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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.persistence.Entity;
import javax.persistence.Transient;

import at.treedb.db.Base;
import at.treedb.db.ClassID;
import at.treedb.db.DAOiface;
import at.treedb.db.DBkey;
import at.treedb.db.UpdateMap;
import at.treedb.domain.Domain;
import at.treedb.i18n.Istring;
import at.treedb.ui.UIelement;
import at.treedb.ui.UItab;
import at.treedb.user.User;

/**
 * 
 * Represents the type of a CI.
 * 
 * @author Peter Sauer
 */
@Entity
// LDOR: 12.12.2013
public class CItype extends Base implements Cloneable, Connectable {
    private static final long serialVersionUID = 1L;

    // index (order) generator for the CItype related objects UItab and
    // UIelement.
    private final int INDEX_INCREMENT = 1000;

    private int indexGenerator;

    public enum Fields {
        name, alias, description, icon
    }

    // optional internal number
    private int internalId;
    // name of the CI type
    // @DBkey(Istring.class)
    private String name;
    // alias name of the CI type
    @DBkey(Istring.class)
    private int alias;
    // description of the CI type
    @DBkey(Istring.class)
    private int description;
    // icon of the CI type
    @DBkey(Image.class)
    private int icon;

    // helper list for all UI tabs
    @Transient
    private ArrayList<UItab> uiTabs = new ArrayList<UItab>();
    // helper map for all UI elements of a CI type
    @Transient
    private HashMap<String, UIelement> fieldMap = new HashMap<String, UIelement>();

    // connections/dependency between CI types for representing a (valid)
    // reference tree (optional)
    @Transient
    private HashSet<Connectable> children = new HashSet<Connectable>();
    @Transient
    private HashSet<Connectable> parents = new HashSet<Connectable>();

    /**
     * Returns the next sequence index for a {@code UItab}/{@code UIelement}
     * object.
     * 
     * @return index
     */
    public int getNextIndex() {
        indexGenerator += INDEX_INCREMENT;
        return indexGenerator;
    }

    protected CItype() {

    }

    private CItype(String name, Istring alias, Istring description, int icon, int internalId) {
        indexGenerator = -INDEX_INCREMENT;
        this.name = name;
        if (alias != null) {
            this.alias = alias.getHistId();
        }
        if (description != null) {
            this.description = description.getHistId();
        }
        this.icon = icon;
        this.internalId = internalId;
    }

    /**
     * Creates a {@code CItype}.
     * 
     * @param domain
     *            {@code Domain} of the data element
     * @param name
     *            name of the CI type
     * @param alias
     *            alias name of the CI type
     * @param description
     *            description of the CI type
     * @param image
     *            image of the CI type
     * @return {@code CItype}
     * @throws Exception
     */
    public static CItype create(Domain domain, User user, String name, String alias, String description,
            ImageDummy image, int internalId) throws Exception {
        return create(null, domain, user, name, alias, description, image, internalId);
    }

    /**
     * Creates a {@code CItype}.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param domain
     *            {@code Domain} of the data element
     * @param name
     *            name of the CI type
     * @param alias
     *            alias name of the CI type
     * @param description
     *            description of the CI type
     * @param dimage
     *            {@code ImageDummy} holding the image of the CI type
     * @return {@code CItype}
     * @throws Exception
     */
    public static CItype create(DAOiface dao, Domain domain, User user, String name, String alias, String description,
            ImageDummy dimage, int internalId) throws Exception {
        Istring ialias = null;
        if (alias != null) {
            ialias = Istring.create(dao, domain, user, ClassID.CITYPE, alias, domain.getLanguage());
        }
        Istring idescription = null;
        if (description != null) {
            idescription = Istring.create(dao, domain, user, ClassID.CITYPE, description, domain.getLanguage());
        }
        int icon = 0;
        if (dimage != null) {
            icon = Image.create(dao, domain, user, dimage.getName(), dimage.getData(), dimage.getMimeType(),
                    dimage.getLicense()).getHistId();
        }
        CItype type = new CItype(name, ialias, idescription, icon, internalId);
        Base.save(dao, domain, user, type);
        return type;
    }

    /**
     * Deletes a {@code CItype}.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param domain
     *            {@code Domain} domain of the data element
     * @param user
     *            user who deletes a CI type
     * @param type
     *            {@code CItype} to be deleted
     * @throws Exception
     */
    public static void delete(DAOiface dao, Domain domain, User user, CItype type) throws Exception {
        if (type.alias != 0) {
            Istring.delete(dao, user, type.alias);
        }
        if (type.description != 0) {
            Istring.delete(dao, user, type.description);
        }
        if (type.icon != 0) {
            Image.delete(dao, user, type.icon);
        }
    }

    /**
     * Returns the name of the {@code CItype}.
     * 
     * @return ID of the {@code Istring}
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the description of the {@code CItype}.
     * 
     * @return ID of the {@code Istring}
     */
    public @DBkey(Istring.class) int getDescription() {
        return description;
    }

    /**
     * Returns the alias of the {@code CItype}.
     * 
     * @return ID of the {@code Istring}
     */
    public @DBkey(Istring.class) int getAlias() {
        return alias;
    }

    /**
     * Returns the icon of the CI type .
     * 
     * @return ID of the {@code Image}
     */
    public @DBkey(Image.class) int getIcon() {
        return icon;
    }

    /**
     * Loads a {@code CItype}
     * 
     * @param id
     *            ID of the {@code CItype}
     * @return {@code CItype} object
     * @throws Exception
     */
    public static CItype load(@DBkey(CItype.class) int id) throws Exception {
        return (CItype) Base.load(null, CItype.class, id, null);
    }

    /**
     * Updates a {@code CItype}
     * 
     * @param user
     *            {@code User} who modifies the {@code CItype}
     * @param map
     *            {@code UpdateMap} map containing the changes
     * @throws Exception
     */
    public void update(User user, UpdateMap map) throws Exception {
        Base.update(user, this, map);
    }

    /**
     * Updates a {@code CItype}
     * 
     * @param user
     *            {@code User} who modifies the {@code CItype}
     * @param type
     *            {@code CItype} to be updated
     * @param map
     *            {@code UpdateMap} map containing the changes
     * @throws Exception
     */
    public static void update(User user, CItype type, UpdateMap map) throws Exception {
        Base.update(user, type, map);
    }

    /**
     * Updates a {@code CItype}
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param user
     *            {@code User} who modifies the {@code CItype}
     * @param type
     *            {@code CItype} to be updated
     * @param map
     *            {@code UpdateMap} map containing the changes
     * @throws Exception
     */
    public static void update(DAOiface dao, User user, CItype type, UpdateMap map) throws Exception {
        Base.update(dao, user, type, map);
    }

    /**
     * Updates a {@code CItype}
     * 
     * @param user
     *            {@code User} who modifies the {@code CItype}
     * @param id
     *            ID of the {@code CItype}
     * @param map
     *            {@code UpdateMap} map containing the changes
     * @throws Exception
     */
    public static void update(User user, @DBkey(CItype.class) int id, UpdateMap map) throws Exception {
        update(user, id, CItype.class, map);
    }

    /**
     * Sets the {@code UItab}.
     * 
     * @param tabs
     *            list of {@code UItab}
     */
    public void setUItab(ArrayList<UItab> tabs) {
        uiTabs = tabs;
        for (UItab t : tabs) {
            for (UIelement u : t.getUIelements()) {
                fieldMap.put(u.getFieldName(), u);
            }
        }
    }

    /**
     * Returns the optional internal ID. This ID is given by the user to
     * simplify the {@code CItype} management.
     * 
     * @return internal ID
     */
    public int getInternId() {
        return internalId;
    }

    /**
     * Adds a {@code UItab}.
     * 
     * @param tab
     *            {@code UItab}, User interface tab
     */
    public void addUItab(UItab tab) {
        uiTabs.add(tab);
        for (UIelement u : tab.getUIelements()) {
            fieldMap.put(u.getFieldName(), u);
        }
    }

    /**
     * Returns a {@code UIelement} by internal name.
     * 
     * @param name
     *            name of the {@code UIelement}
     * @return {@code UIelement}
     */
    public UIelement getUIelement(String name) {
        return fieldMap.get(name);
    }

    /**
     * Returns a list of {@code UItab}.
     * 
     * @return list of {@code UItab}
     */
    public ArrayList<UItab> getUItab() {
        return uiTabs;
    }

    @Override
    public ClassID getCID() {
        return ClassID.CITYPE;
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
            throw new Exception("CItype.getParent(): No parent available");
        }
        if (parents.size() > 1) {
            throw new Exception("CItype.getParent(): Multiple parents available");
        }
        return parents.toArray(new Connectable[1])[0];
    }

    // TODO: fix problem returning null
    @Override
    public String getCanonicalName() {
        // return Istring.load(name, lang).getText();
        return null;
    }

    @Override
    public TYPE getConnectionType() {
        return TYPE.CITYPE;
    }

    @Override
    public int getId() {
        return getHistId();
    }

}
