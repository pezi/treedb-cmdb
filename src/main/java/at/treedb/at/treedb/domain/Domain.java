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
package at.treedb.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import at.treedb.ci.CI;
import at.treedb.ci.CItype;
import at.treedb.ci.Connectable;
import at.treedb.ci.Image;
import at.treedb.ci.ImageDummy;
import at.treedb.ci.Node;
import at.treedb.db.Base;
import at.treedb.db.ClassID;
import at.treedb.db.DAO;
import at.treedb.db.DAOiface;
import at.treedb.db.DBentities;
import at.treedb.db.DBkey;
import at.treedb.db.SearchLimit;
import at.treedb.db.UpdateMap;
import at.treedb.i18n.Istring;
import at.treedb.i18n.Locale;
import at.treedb.i18n.SupportedLanguage;
import at.treedb.rest.HTTPiface;
import at.treedb.rest.RESTiface;
import at.treedb.i18n.Locale.LANGUAGE;
import at.treedb.i18n.Locale.LOCALE;
import at.treedb.ui.UIelement;
import at.treedb.ui.UImacro;
import at.treedb.ui.UIselect;
import at.treedb.ui.UItab;
import at.treedb.user.Tenant;
import at.treedb.user.User;
import at.treedb.util.Flags;
import at.treedb.util.text.Text;
import at.treedb.util.text.TextGroup;

/**
 * A domain helps to separate different trees/logical groups of CIs.
 * 
 * @author Peter Sauer
 */
@SuppressWarnings("serial")
@Entity
public class Domain extends Base implements Cloneable, Connectable {

    public enum Properties {
        CI_NO_UNIQUE_NAMES, //
        CI_LAZY_LOADING, //
    }

    public enum Fields {
        alias, description, bigIcon, smallIcon, codeMaturity, version, tenant, uiText
    }

    private enum privateFields {
        name
    }

    final public static int SMALL_ICON_WIDTH = 16;
    final public static int SMALL_ICON_HEIGHT = 16;

    final public static int BIG_ICON_WIDTH = 100;
    final public static int BIG_ICON_HEIGHT = 100;

    private static Domain dummyDomain;

    @DBkey(value = Tenant.class)
    private int tenant;
    // UUID
    private String uuid;
    // clone counter for tracking
    private int cloneCounter;
    // each copy provides a separate clone UUUID
    private String cloneUUID;

    @DBkey(value = VersionInfo.class)
    private int versionInfo;
    // domain name
    @Column(nullable = false)
    private String name;
    // alternative name of the domain
    @DBkey(value = Istring.class)
    private int alias;
    // description of the domain
    @DBkey(value = Istring.class)
    private int description;
    // graphical representation of a domain
    // (big) icon - should be 100x100 points
    @DBkey(value = Image.class)
    private int bigIcon;
    // small icon - should be 16x16
    @DBkey(value = Image.class)
    private int smallIcon;
    // primary language
    private Locale.LANGUAGE language;
    // primary country
    private Locale.COUNTRY country;

    transient private Locale locale;
    transient private List<SupportedLanguage> supportedLanguages;
    // HTML color code
    @Column(length = 6)
    private String webColor;
    // class which implements the
    // at.treedb.vaadin.TreeDBiface interface
    private String treeDBifaceClass;
    private String RESTifaceClass;
    private String HTTPifaceClass;
    // properties
    private long flags;
    // optional UI text - see at.treedb.util.text.Text
    @DBkey(value = Istring.class)
    private int uiText;
    @Transient
    private String uiXMLtext;
    @Transient
    // CI map with the DB id as key
    private HashMap<Integer, CI> ciIntMap = new HashMap<Integer, CI>();
    // CI map with the CI name as key
    @Transient
    private HashMap<String, CI> ciStrMap = new HashMap<String, CI>();
    @Transient
    // CI type map with the DB id as key
    private HashMap<Integer, CItype> ciTypeMap = new HashMap<Integer, CItype>();
    @Transient
    private HashMap<String, UImacro> macroMap = new HashMap<String, UImacro>();
    @Transient
    private HashMap<LOCALE, HashMap<String, UImacro>> localeMacroMap = new HashMap<Locale.LOCALE, HashMap<String, UImacro>>();
    // the root element of all CItype trees is the domain
    @Transient
    private HashSet<Connectable> children = new HashSet<Connectable>();
    @Transient
    private HashSet<Connectable> parents = new HashSet<Connectable>();
    @Transient
    private boolean uniqueCInames;
    @Transient
    private boolean isInitialized;
    // optional REST interface
    @Transient
    private RESTiface restIface;
    // optional HTTP interface for delivering content
    @Transient
    private HTTPiface httpIface;
    // GUI text as XML
    @Transient
    private Text guiText;
    // maps containing all domains
    // domain by Id map
    private static HashMap<Integer, Domain> domainIntMap = new HashMap<Integer, Domain>();
    // domain by name map
    private static HashMap<String, Domain> domainStrMap = new HashMap<String, Domain>();

    public void setRESTiface(RESTiface restIface) {
        this.restIface = restIface;
    }

    public RESTiface getRESTiface() {
        return restIface;
    }

    public void setHTTPiface(HTTPiface httpIface) {
        this.httpIface = httpIface;
    }

    public HTTPiface getHTTPiface() {
        return httpIface;
    }

    protected Domain() {
    }

    public boolean isUniqueCInames() {
        return uniqueCInames;
    }

    public String getCloneUUID() {
        return cloneUUID;
    }

    /**
     * Returns a dummy {@code Domain}. This special {@code Domain} is used for
     * search operations histId 0.
     * 
     * @return dummy {@code Domain}
     */
    public static synchronized Domain getDummyDomain() {
        if (dummyDomain == null) {
            dummyDomain = new Domain();
        }
        return dummyDomain;
    }

    /**
     * Clears all static internal data.
     */
    public static void clearInternalData() {
        domainIntMap = new HashMap<Integer, Domain>();
        domainStrMap = new HashMap<String, Domain>();
    }

    @Override
    public ClassID getCID() {
        return ClassID.DOMAIN;
    }

    private Domain(User user, String name, Locale locale, String webColor, String treeDBifaceClass,
            String RESTifaceClass, String HTTPifaceClass, String uuid) throws Exception {
        Objects.requireNonNull(locale, "Domain(): locale can not be null!");
        Objects.requireNonNull(name, "Domain(): name can not be null!");
        setHistStatus(STATUS.ACTIVE);
        this.uuid = uuid;
        cloneUUID = UUID.randomUUID().toString();
        if (user != null) {
            setCreatedBy(user.getDBid());
        }
        this.locale = locale;
        this.language = locale.getLanguage();
        this.country = locale.getCountry();
        this.name = name;
        this.webColor = webColor;
        this.treeDBifaceClass = treeDBifaceClass;
        this.RESTifaceClass = RESTifaceClass;
        this.HTTPifaceClass = HTTPifaceClass;
        this.isInitialized = false;
    }

    /**
     * Creates a {@code Domain}, which presents a logical group of CIs.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param user
     *            {@code User} who creates the {@code Domain}
     * @param name
     *            name of the {@code Domain}
     * @param locale
     *            primary language
     * @param alias
     *            alias name of the {@code Domain}
     * @param description
     *            description of the {@code Domain}
     * @param version
     *            user defined DB version string
     * @param changes
     *            change log
     * @param codeMaturity
     * @param smallIcon
     *            icon representation of the {@code Domain} - 16x16 pixels
     * @param bigIcon
     *            image representation of the {@code Domain} - 100x100 pixels
     * @param webColor
     *            color, which represents the domain
     * @param treeDBifaceClass
     *            FQCN of the class which defines the domain
     * @param RESTifaceClass
     *            FQCN of the optional REST interface
     * @param HTTPifaceClass
     *            FQCN of the optional HTTP interface
     * @param flags
     *            flags for controlling the behavior of the domain
     * @param uuid
     *            UUID of the domain
     * @return {@code Domain} object
     * @throws Exception
     */
    public static Domain create(DAOiface dao, User user, String name, Locale locale, String alias, String description,
            String version, String changes, VersionInfo.CodeMaturity codeMaturity, ImageDummy smallIcon,
            ImageDummy bigIcon, String webColor, String treeDBifaceClass, String RESTifaceClass, String HTTPifaceClass,
            EnumSet<Properties> flags, String uuid) throws Exception {
        Domain domain = null;
        synchronized (domainIntMap) {
            // check to prevent duplicate domain names
            List<Base> dList = Base.search(null, Domain.class, EnumSet.of(Domain.privateFields.name), name, null, null,
                    null, false);
            if (dList.size() > 0) {
                throw new DuplicateName("Domain.create(): Duplicate domain name!");
            }
            domain = new Domain(user, name, locale, webColor, treeDBifaceClass, RESTifaceClass, HTTPifaceClass, uuid);
            save(dao, domain, user, alias, description, version, changes, codeMaturity, smallIcon, bigIcon,
                    Flags.toBitMask(flags));
            domainIntMap.put(domain.getHistId(), domain);
            domainStrMap.put(name, domain);
            domain.uniqueCInames = !domain.isProperty(Domain.Properties.CI_NO_UNIQUE_NAMES);
        }
        return domain;
    }

    /**
     * Initializes the internal structures of a {@code Domain}.
     * 
     * @param name
     *            name of the {@code Domain}
     * @throws Exception
     */
    public static void initializeDomain(String name) throws Exception {
        synchronized (domainIntMap) {
            Domain domain = domainStrMap.get(name);
            if (domain == null) {
                throw new Exception("Domain.initializeDomain(): Domain not found");
            }
            if (!domain.isInitialized) {
                domain = load(name, -1);
                domainIntMap.put(domain.getHistId(), domain);
                domainStrMap.put(name, domain);
            }
        }
    }

    /**
     * Returns the initialization status of the {@code Domain}.
     * 
     * @return {@code true} if the {@code Domain} is initialized, {@code false}
     *         if not
     */
    public boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Loads a {@code Domain}.
     * 
     * @param name
     *            name of the {@code Domain}
     * @return {@code Domain}
     * @throws Exception
     */
    public static Domain load(String name) throws Exception {
        Domain domain = null;
        synchronized (domainIntMap) {
            domain = domainStrMap.get(name);

            if (domain == null) {
                // load per name
                domain = load(name, -1);
                if (domain == null) {
                    return null;
                }
                domainIntMap.put(domain.getHistId(), domain);
                domainStrMap.put(name, domain);

            }
        }
        return domain;
    }

    @Override
    public void callbackAfterLoad(DAOiface dao) throws Exception {
        uniqueCInames = !isProperty(Domain.Properties.CI_NO_UNIQUE_NAMES);
    }

    @Override
    public boolean isCallbackAfterLoad() {
        return true;
    }

    /**
     * Gets a {@code Domain} by its ID.
     * 
     * @param domainID
     *            ID of the {@code Domain}
     * @return {@code Domain}
     * @throws Exception
     */
    public static Domain get(@DBkey(value = Domain.class) int domainId) {
        synchronized (domainIntMap) {
            Domain domain = domainIntMap.get(domainId);
            if (domain == null) {
                try {
                    domain = load(domainId);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return null;
                }
            }
            return domain;
        }
    }

    /**
     * Loads a {@code Domain}
     * 
     * @param id
     *            ID of the {@code Domain}
     * @return {@code Domain}
     * @throws Exception
     */
    public static Domain load(@DBkey(value = Domain.class) int id) throws Exception {
        Domain domain = null;
        synchronized (domainIntMap) {
            domain = domainIntMap.get(id);
            if (domain == null) {
                // load per id
                domain = load(null, id);
                if (domain == null) {
                    return null;
                }
                domainIntMap.put(domain.getHistId(), domain);
                domainStrMap.put(domain.getName(), domain);
            }
        }
        return domain;
    }

    /**
     * Renames a {@code Domain}.
     * 
     * @param user
     *            user who renames the domain
     * @param newName
     *            new name of the {@code Domain}
     * @throws Exception
     */
    public void rename(User user, String newName) throws Exception {
        Objects.requireNonNull(newName, "Domain.rename(): parameter name can't be null");
        newName = newName.trim();
        if (newName.isEmpty()) {
            throw new Exception("Domain.rename(): parameter name can't be empty");
        }
        if (name.equals(newName)) {
            return;
        }
        synchronized (domainIntMap) {
            List<Base> dList = Base.search(null, Domain.class, EnumSet.of(Domain.privateFields.name), newName, null,
                    null, null, false);
            if (dList.size() > 0) {
                throw new DuplicateName();
            }
            String oldName = getName();
            UpdateMap map = new UpdateMap(Domain.privateFields.class);
            map.addString(Domain.privateFields.name, newName);
            this.update(user, map);
            domainStrMap.remove(oldName);
            domainStrMap.put(newName, this);
        }
    }

    /**
     * 
     * @param name
     * @return
     * @throws Exception
     */
    public static Domain getDomain(String name) throws Exception {
        Domain d = domainStrMap.get(name);
        if (d == null) {
            d = Domain.load(name);
        }
        return d;
    }

    /**
     * 
     * @param name
     * @return
     * @throws Exception
     */
    public static boolean exists(String name) throws Exception {
        return domainStrMap.containsKey(name);
    }

    /**
     * Unloads a domain.
     * 
     * @param domain
     */
    public void unloadDomain(Domain domain) {
        synchronized (domainIntMap) {
            domainIntMap.remove(domain.getHistId());
            domainStrMap.remove(domain.getName());
        }
    }

    /**
     * Returns name of the {@code Domain}.
     * 
     * @return {@code Domain} name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the domain description.
     * 
     * @return {@code Istring} ID of the description
     */
    public @DBkey(Istring.class) int getDescription() {
        return description;
    }

    /**
     * Returns the alias domain.
     * 
     * @return {@code Istring} ID of the alias name
     */
    public @DBkey(value = String.class) int getAlias() {
        return alias;
    }

    /**
     * Saves a domain.
     * 
     * @param domain
     *            {@code Domain} to be saved
     * @param user
     *            {@code User} who saves the {@code Domain}
     * @param alias
     *            alias name of the {@code Domain}
     * @param description
     *            description of the {@code Domain}
     * @param smallIcon
     *            small icon of the {@code Domain}
     * @param icon
     *            icon of the {@code Domain}
     * @param flags
     *            flags
     * @throws Exception
     */
    private static void save(DAOiface dao, Domain domain, User user, String alias, String description, String version,
            String changes, VersionInfo.CodeMaturity codeMaturity, ImageDummy smallIcon, ImageDummy icon, long flags)
                    throws Exception {
        boolean localDAO = false;
        if (dao == null) {
            dao = DAO.getDAO();
            localDAO = true;
        }
        Date d = new Date();
        try {
            if (localDAO) {
                dao.beginTransaction();
            }
            domain.setCreationTime(d);
            domain.setLastModified(d);

            dao.saveAndFlushIfJPA(domain);
            domain.setHistId(domain.getDBid());
            domain.setDomain(domain.getDBid());
            domain.flags = flags;
            if (alias != null) {
                domain.alias = Istring.create(dao, domain, user, domain.getCID(), alias, domain.getLanguage())
                        .getHistId();
            }
            if (description != null) {
                domain.description = Istring
                        .create(dao, domain, user, domain.getCID(), description, domain.getLanguage()).getHistId();
            }
            if (version != null) {
                domain.versionInfo = VersionInfo.create(dao, domain, user, version, codeMaturity, changes).getHistId();
            }

            if (icon != null) {
                Image i = Image.create(dao, domain, user, icon.getName(), icon.getData(), icon.getMimeType(),
                        icon.getLicense());
                domain.bigIcon = i.getHistId();
            }
            if (smallIcon != null) {
                Image i = Image.create(dao, domain, user, smallIcon.getName(), smallIcon.getData(),
                        smallIcon.getMimeType(), smallIcon.getLicense());
                domain.smallIcon = i.getHistId();
            }
            dao.update(domain);
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

    /**
     * Loads a {@code Domain} by its name or id.
     * 
     * @param name
     *            name of the {@code Domain}
     * @param id
     *            id of the {@code Domain}
     * @return {@code Domain
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private static Domain load(String name, int id) throws Exception {

        DAOiface dao = DAO.getDAO();
        Domain domain = null;
        try {
            dao.beginTransaction();
            List<Domain> list = null;
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("status", at.treedb.db.HistorizationIface.STATUS.ACTIVE);

            if (name != null) {
                map.put("name", name);
                list = (List<Domain>) dao.query("select d from Domain d where d.name = :name and d.status = :status",
                        map);
            } else {
                map.put("id", id);
                list = (List<Domain>) dao.query("select d from Domain d where d.histId = :id and d.status = :status",
                        map);
            }
            if (list.size() == 1) {
                domain = list.get(0);
            } else {
                dao.endTransaction();
                return null;
            }
            domain.uniqueCInames = !domain.isProperty(Domain.Properties.CI_NO_UNIQUE_NAMES);
            domain.locale = new Locale(domain.language, domain.country);
            // load all supported languages
            domain.supportedLanguages = SupportedLanguage.loadAll(dao, domain.getHistId());

            map = new HashMap<String, Object>();
            map.put("id", domain.getHistId());
            map.put("status", at.treedb.db.HistorizationIface.STATUS.ACTIVE);
            // load all CIs which resides in memory
            List<CI> ciList = (List<CI>) dao.query(
                    "select ci from CI ci where ci.domain = :id and ci.status = :status and ci.inMemory = true", map);
            // build all hash maps
            for (CI c : ciList) {
                domain.ciIntMap.put(c.getHistId(), c);
                if (domain.uniqueCInames) {
                    domain.ciStrMap.put(c.getName(), c);
                }
            }

            // load all UI elements
            HashMap<Integer, ArrayList<UIelement>> uiMap = new HashMap<Integer, ArrayList<UIelement>>();
            for (Class<?> c : DBentities.getClasses()) {
                if (c.getSuperclass() == UIelement.class) {
                    List<UIelement> ulist = (List<UIelement>) dao.query(
                            "select t from " + c.getCanonicalName() + " t where t.domain = :id and t.status = :status",
                            map);
                    for (UIelement u : ulist) {
                        ArrayList<UIelement> ul = uiMap.get(u.getUItab());
                        if (ul == null) {
                            ul = new ArrayList<UIelement>();
                            uiMap.put(u.getUItab(), ul);
                        }
                        ul.add(u);
                        if (u instanceof UIselect) {
                            ((UIselect) u).initOptions(dao);
                        }
                    }
                }
            }

            // load UI tabs
            List<UItab> uiTab = (List<UItab>) dao
                    .query("select t from UItab t where t.domain = :id and t.status = :status order by t.index", map);
            HashMap<Integer, ArrayList<UItab>> tabMap = new HashMap<Integer, ArrayList<UItab>>();
            for (UItab tab : uiTab) {
                ArrayList<UItab> t = tabMap.get(tab.getCiType());
                if (t == null) {
                    t = new ArrayList<UItab>();
                    tabMap.put(tab.getCiType(), t);
                }
                ArrayList<UIelement> ul = uiMap.get(tab.getHistId());
                if (ul != null) {
                    Collections.sort(ul);
                    tab.setUIelements(ul);
                }
                t.add(tab);
            }

            // load all CI types
            List<CItype> types = (List<CItype>) dao
                    .query("select t from CItype t where t.domain = :id and t.status = :status", map);

            for (CItype t : types) {
                ArrayList<UItab> l = tabMap.get(t.getHistId());
                if (l != null) {
                    t.setUItab(l);
                }
                domain.ciTypeMap.put(t.getHistId(), t);
            }

            HashMap<Integer, CI> ciMap = domain.ciIntMap;
            HashMap<Integer, CItype> typeMap = domain.ciTypeMap;
            map.put("ctype", at.treedb.ci.Node.ConnectionType.LAZY_NODES);
            // load all nodes
            List<Node> nodes = (List<Node>) dao.query(
                    "select n from Node n where n.domain = :id and n.status = :status and n.connectionType < :ctype",
                    map);
            // connect all CIs
            for (Node n : nodes) {
                if (n.getType() == Node.NodeType.CI) {
                    CI child = ciMap.get(n.getChild());
                    if (child == null) {
                        throw new Exception("Domain.load(): Missing CI - Node<->CI mismatch!");
                    }
                    CI parent = ciMap.get(n.getParent());
                    if (parent == null) {
                        throw new Exception("Domain.load(): Missing CI - Node<->CI mismatch!");
                    }
                    child.getParents().add(parent);
                    parent.getChildren().add(child);
                } else {
                    CItype child = typeMap.get(n.getChild());
                    if (child == null) {
                        throw new Exception("Domain.load(): Missing CItype - Node<->CItype mismatch!");
                    }
                    Connectable cparent;
                    if (n.getParent() != Node.PARENT_IS_A_DOMAIN) {
                        cparent = typeMap.get(n.getParent());
                        if (cparent == null) {
                            throw new Exception("Domain.load(): Missing CItype - Node<->CItype mismatch!");
                        }
                    } else {
                        cparent = domain;
                    }
                    child.getParents().add(cparent);
                    cparent.getChildren().add(child);
                }
            }

            // load all macros
            List<? extends Base> macros = UImacro.loadAll(dao, domain, null);
            for (Base b : macros) {
                UImacro m = (UImacro) b;
                addMacro(domain, m);
            }
            // load UI text
            if (domain.uiText != 0) {
                domain.uiXMLtext = Istring.load(dao, domain.uiText, LANGUAGE.sys).getText();

            }
            dao.endTransaction();
        } catch (Exception e) {
            dao.rollback();
            throw e;
        }
        domain.isInitialized = true;
        return domain;
    }

    /**
     * Returns the {@code TextGroup} for a given language.
     * 
     * @param groupName
     *            name the text group
     * @param lang
     *            language
     * @return {@code TextGroup}
     * @throws Exception
     */
    public TextGroup getGroup(String groupName, LANGUAGE lang) throws Exception {
        if (uiXMLtext == null) {
            return null;
        }
        synchronized (uiXMLtext) {
            if (guiText == null) {
                guiText = new Text(uiXMLtext, Text.SOURCE.STRING, Text.DTD);
            }
        }
        return guiText.getGroup(groupName, lang, null);
    }

    /**
     * Returns a {@code CItype} by its user defined internal id.
     * 
     * @param internal
     *            id
     * @return {@code CItype} object
     */
    public CItype getCItypeByInternalId(int internal) {
        for (CItype t : ciTypeMap.values()) {
            if (t.getInternId() == internal) {
                return t;
            }
        }
        return null;
    }

    /**
     * Returns a {@code CItype} by its user defined internal enum value.
     * 
     * @param e
     *            {@code Enum} value
     * @return {@code CItype} object
     */
    public CItype getCItypeByInternalId(Enum<?> e) {
        return getCItypeByInternalId(e.ordinal());
    }

    /**
     * Adds a macro to a {@code Domain}.
     * 
     * @param domain
     *            {@code Domain}
     * @param macro
     *            macro UI macro
     */
    public static void addMacro(Domain domain, UImacro macro) {
        if (macro.getLocale() == null) {
            domain.macroMap.put(macro.getName(), macro);
        } else {
            // special handling for existing macros, which are changed to a a
            // language macro
            domain.macroMap.remove(macro.getName());
            HashMap<String, UImacro> mmap = domain.localeMacroMap.get(macro.getLocale());
            if (mmap == null) {
                mmap = new HashMap<String, UImacro>();
                domain.localeMacroMap.put(macro.getLocale(), mmap);
            }
            mmap.put(macro.getName(), macro);
        }
    }

    /**
     * Removes a macro form a domain.
     * 
     * @param domain
     *            {@code Domain} of the macro
     * @param macro
     *            macro
     */
    public static void removeMacro(Domain domain, UImacro macro) {
        if (macro.getLocale() == null) {
            domain.getMacroMap().remove(macro.getName());
        } else {
            domain.getLMacroMap().get(macro.getLocale()).remove(macro.getName());
        }
    }

    /**
     * Renames a macro.
     * 
     * @param domain
     *            {@code Domain} of the macro
     * @param oldName
     *            old name name of the macro
     * @param macro
     *            renamed macro
     */
    public static void renameMacro(Domain domain, String oldName, UImacro macro) {
        if (macro.getLocale() == null) {
            domain.getMacroMap().remove(oldName);
            domain.getMacroMap().put(macro.getName(), macro);
        } else {
            HashMap<String, UImacro> map = domain.getLMacroMap().get(macro.getLocale());
            map.remove(oldName);
            map.put(macro.getName(), macro);
        }
    }

    /**
     * Returns map containing the text macros.
     * 
     * @return macro map
     */
    public HashMap<String, UImacro> getMacroMap() {
        return macroMap;
    }

    /**
     * Returns map containing the language text macros.
     * 
     * @return macro map
     */
    public HashMap<Locale.LOCALE, HashMap<String, UImacro>> getLMacroMap() {
        return localeMacroMap;
    }

    /**
     * Returns a macro by its name.
     * 
     * @param name
     *            name of the macro
     * @return {@code UImacro}
     */
    public UImacro getMacro(String name) {
        return macroMap.get(name);
    }

    /**
     * Returns a language macro by its name.
     * 
     * @param locale
     *            language
     * @param name
     *            name of the macro
     * @return {@code UImacro}
     */
    public UImacro getLMacro(Locale.LOCALE locale, String name) {
        HashMap<String, UImacro> map = localeMacroMap.get(locale);
        if (map != null) {
            return map.get(name);
        }
        return null;
    }

    /**
     * Deletes a {@code Domain}.
     * 
     * @param user
     *            {@code User} who deletes a {@code Domain}
     * @param domain
     *            {@code Domain} to be deleted
     * 
     * @throws Exception
     */
    public static void delete(DAOiface dao, User user, Domain domain) throws Exception {
        synchronized (domainIntMap) {
            Base.delete(dao, user, domain, false);
            domainIntMap.remove(domain.getHistId());
            domainStrMap.remove(domain.getName());
        }
    }

    /*
     * private static void deleteDomain(Domain domain, User user) throws
     * Exception { domain.setLastModified(new Date()); DAOiface dao =
     * DAO.getDAO(); try { dao.beginTransaction();
     * 
     * // dao.delete(domain); if (domain.alias != 0) { Istring.delete(dao, user,
     * domain.alias); } if (domain.description != 0) { Istring.delete(dao, user,
     * domain.description); } if (domain.bigIcon != 0) { Image.delete(dao, user,
     * domain.bigIcon); } dao.endTransaction(); } catch (Exception e) {
     * dao.rollback(); throw e; } finally {
     * 
     * } }
     */

    /**
     * Returns the 100x100 big icon.
     * 
     * @return big icon
     */
    public @DBkey(value = Image.class) int getBigIcon() {
        return bigIcon;
    }

    /**
     * Returns the small 16x16 icon.
     * 
     * @return small icon
     */
    public @DBkey(value = Image.class) int getSmallIcon() {
        return smallIcon;
    }

    /**
     * Returns a HTML color string.
     * 
     * @return HTML color
     */
    public String getWebColor() {
        return webColor;
    }

    /**
     * Returns a CI.
     * 
     * @param id
     *            CI id
     * @return {@code CI}
     */
    public CI getCI(@DBkey(value = Image.class) int id) {
        return ciIntMap.get(id);
    }

    /**
     * Returns a CI per name.
     * 
     * @param name
     *            exact CI name
     * @return {@code CI} object
     */
    public CI getCI(String name) throws Exception {
        if (!uniqueCInames) {
            throw new Exception("Domain.getCI(): Not allowed for domains with the property CI_NO_UNIQUE_NAMES");
        }
        return ciStrMap.get(name);
    }

    /**
     * Searches a {@code CI}
     * 
     * @param name
     *            exact name of the {@code CI}
     * @param ctype
     *            type of the {@code CI}
     * @return {@code CI} object . Warning: For domains with not unique
     *         {@code CI) names
     * only the first occurrence will be returned.
     * 
     */
    public CI searchCI(String name, CItype ctype) {
        for (CI c : getCImap().values()) {
            if (c.getCIType() == ctype.getHistId() && c.getName() != null && c.getName().equals(name)) {
                return c;
            }
        }
        return null;
    }

    private CI traverseCI(CI c, String name, CItype ctype) {
        if (c.getCItypeObj().getHistId() == ctype.getHistId() && c.getName().equals(name)) {
            return c;
        }
        HashSet<Connectable> hset = c.getChildren();
        if (hset.size() == 0) {
            return null;
        }
        for (Connectable n : hset) {
            CI result = traverseCI((CI) n, name, ctype);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * Searches recursively a CI of a certain type starting at a given CI
     * 
     * @param start
     *            CI starting the search
     * @param name
     *            name of CI
     * @param ctype
     *            type of the CI
     * @return CI which match the requirements
     */
    public CI searchRecursively(CI start, String name, CItype ctype) {
        return traverseCI(start, name, ctype);
    }

    /**
     * Searches multiple CIs by name
     * 
     * @param ctype
     *            type of the CI
     * @param args
     *            CI names
     * @return map containing the search result CI name/CI
     */
    public HashMap<String, CI> searchCI(CItype ctype, String... args) {
        HashMap<String, CI> map = new HashMap<String, CI>();
        HashSet<String> search = new HashSet<String>();
        for (String s : args) {
            search.add(s);
        }
        for (CI c : getCImap().values()) {
            if (c.getCIType() == ctype.getHistId() && c.getName() != null) {
                if (search.contains(c.getName())) {
                    map.put(c.getName(), c);
                }
            }
        }
        return map;
    }

    private void traverse(Connectable c, ArrayList<Connectable> al) {
        HashSet<Connectable> hset = c.getChildren();
        if (hset.size() == 0) {
            al.add(c);
            return;
        }
        for (Connectable n : hset) {
            traverse(n, al);
        }
        al.add(c);
    }

    /**
     * Traverses a tree collecting recursive the {@code Connectable} elements.
     * 
     * @param c
     *            starting node
     * @return list of {@code Connectable} elements
     */
    public ArrayList<Connectable> traverseElemements(Connectable c) {
        ArrayList<Connectable> al = new ArrayList<Connectable>();
        traverse(c, al);
        return al;
    }

    /**
     * Updates the name of a {@code CI}
     * 
     * @param ci
     *            {@code CI}
     * @param oldName
     *            old name of the {@code CI}
     * @throws Exception
     */
    public void updateCIname(CI ci, String oldName) throws Exception {
        if (!uniqueCInames) {
            throw new Exception("Domain.getCI(): Not allowed for domains with the property CI_NO_UNIQUE_NAMES");
        }
        ciStrMap.remove(oldName);
        ciStrMap.put(ci.getName(), ci);
    }

    /**
     * Stores a CI.
     * 
     * @param ci
     *            {@code CI} object
     */
    public void putCI(CI ci) throws Exception {
        if (!ci.isInMemory()) {
            throw new Exception("Domain.putCI(): Not allowed for lazy loading CIs");
        }
        ciIntMap.put(ci.getHistId(), ci);
        if (uniqueCInames) {
            ciStrMap.put(ci.getName(), ci);
        }
    }

    /**
     * Returns a {@code CItype} from the internal map.
     * 
     * @param id
     *            CItype ID
     * @return {@code CItype} object
     */
    public CItype getCItype(@DBkey(value = CItype.class) int id) {
        return ciTypeMap.get(id);
    }

    /**
     * Puts a {@code CItype} into the internal map.
     * 
     * @param type
     *            {@code CItype}
     */
    public void putCItype(CItype type) {
        ciTypeMap.put(type.getHistId(), type);
    }

    /**
     * Removes a {@code CItype} from the internal map.
     * 
     * @param type
     *            {@code CItype}
     * @return {@code CItype} object
     */
    public CItype removeCItype(CItype type) {
        return ciTypeMap.remove(type.getHistId());
    }

    /**
     * Returns the primary language code of the domain.
     * 
     * @return primary language code, lower case 2-digit code
     */

    public Locale.LANGUAGE getLanguage() {
        return language;
    }

    /**
     * Returns the country code (language) of the domain.
     * 
     * @return primary country, lower case 2-digit code, can be null
     */
    public Locale.COUNTRY getCountry() {
        return country;
    }

    /**
     * Returns the locale of the domain.
     * 
     * @return {@code Locale}
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Determines if the GUI language is supported by the data base.
     * 
     * @param lang
     *            GUI language
     * @return {@code true} if the GUI language is supported by the data base
     */
    public Locale getNearestLocale(Locale locale) {
        HashMap<Locale.LANGUAGE, SupportedLanguage> map = new HashMap<Locale.LANGUAGE, SupportedLanguage>();
        for (SupportedLanguage s : supportedLanguages) {
            if (s.getLocale().equals(locale.getLocale())) {
                return locale;
            }
            Locale.LANGUAGE l = s.getLocale().getLanguage();
            SupportedLanguage sl = map.get(l);
            if (sl == null) {
                map.put(l, s);
            } else {
                if (s.getPriority() > sl.getPriority()) {
                    map.put(l, s);
                }
            }
        }
        SupportedLanguage sl = map.get(locale.getLocale().getLanguage());
        if (sl != null) {
            return new Locale(sl.getLocale());
        }

        return new Locale(language, country);
    }

    /**
     * 
     * @return
     */
    public List<SupportedLanguage> getSupportedLanguages() {
        return supportedLanguages;
    }

    /**
     * Updates a domain.
     * 
     * @param user
     *            {@code User} who updates the {@code Domain}
     * @param map
     *            update map
     * @throws Exception
     */
    public void update(User user, UpdateMap map) throws Exception {
        Base.update(user, this, map);
    }

    /**
     * Returns the GUI class, which is associated with this domain.
     * 
     * @return class name
     */
    public String getTeeDBifaceClass() {
        return treeDBifaceClass;
    }

    /**
     * Returns the FQCN of optional REST interface.
     * 
     * @return FQCN
     */
    public String getRESTifaceClass() {
        return RESTifaceClass;
    }

    /**
     * Returns the FQCN of optional HTTP interface.
     * 
     * @return FQCN
     */
    public String getHTTPifaceClass() {
        return HTTPifaceClass;
    }

    /**
     * Updates a domain.
     * 
     * @param user
     *            {@code User} who updates the {@code Domain}
     * @param domain
     *            {@code Domain}, to be updated
     * @param map
     *            update map
     * @throws Exception
     */
    static public void update(User user, Domain domain, UpdateMap map) throws Exception {
        Base.update(user, domain, map);
    }

    /**
     * Updates a domain.
     * 
     * @param user
     *            {@code User} who updates the {@code Domain}
     * @param domain
     *            {@code Domain}, to be updated
     * @param map
     *            update map
     * @throws Exception
     */
    static public void update(DAOiface dao, User user, Domain domain, UpdateMap map) throws Exception {
        Base.update(dao, user, domain, map);
    }

    /**
     * Updates a domain.
     * 
     * @param admin
     *            administration user
     * @param id
     *            {@code Domain}, to be updated
     * @param map
     *            update map
     * @throws Exception
     */
    static public void update(User admin, @DBkey(value = Domain.class) int id, UpdateMap map) throws Exception {
        Base.update(admin, id, Domain.class, map);
    }

    /**
     * Searches a {@code Domain}.
     * 
     * @param fields
     *            search field
     * @param value
     *            fields to be searched
     * @param flags
     *            search flags
     * @param limit
     *            search limit
     * @return list of {@code domains}
     * @throws Exception
     */
    static public List<Base> search(EnumSet<Fields> fields, String value, int maxResults, EnumSet<Base.Search> flags,
            SearchLimit limit) throws Exception {
        return Base.search(null, Domain.class, fields, value, null, flags, limit, false);
    }

    /**
     * Returns a hash map containing all CIs.
     * 
     * @return {@code HashMap<Integer, CI>}
     */
    public HashMap<Integer, CI> getCImap() {
        return ciIntMap;
    }

    /**
     * Returns a hash map containing all CItypes.
     * 
     * @return {@code HashMap<Integer, CItype>}
     */
    public HashMap<Integer, CItype> getCItypeMap() {
        return ciTypeMap;
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
    public String getCanonicalName() {
        return name;
    }

    @Override
    public TYPE getConnectionType() {
        return TYPE.DOMAIN;
    }

    @Override
    public int getId() {
        return Node.PARENT_IS_A_DOMAIN;
    }

    @Override
    public int getIcon() {
        return smallIcon;
    }

    /**
     * Returns the UUID (universally unique identifier).
     * 
     * @return UUID
     */
    public String getUUID() {
        return uuid;
    }

    /**
     * Returns the UI text as XML file.
     * 
     * @return
     */
    public String getUItext() {
        return uiXMLtext;
    }

    /**
     * Returns is a property is set
     * 
     * @param prop
     *            property
     * @return {@code true} if a property is set, {@code false} if not
     */
    public boolean isProperty(Properties prop) {
        if ((flags & (1L << prop.ordinal())) != 0) {
            return true;
        }
        return false;
    }

    @Override
    public Connectable getParent() throws Exception {
        return null;
    }
}
