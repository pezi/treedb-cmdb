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
package at.treedb.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import at.treedb.ci.CI;
import at.treedb.ci.CIblob;
import at.treedb.ci.CIboolean;
import at.treedb.ci.CIdate;
import at.treedb.ci.CIdouble;
import at.treedb.ci.CIfile;
import at.treedb.ci.CIi18nString;
import at.treedb.ci.CIimage;
import at.treedb.ci.CIlong;
import at.treedb.ci.CIstring;
import at.treedb.ci.CItype;
import at.treedb.ci.Connectable;
import at.treedb.ci.ConnectionDummy;
import at.treedb.ci.FileDummy;
import at.treedb.ci.Image;
import at.treedb.ci.ImageDummy;
import at.treedb.ci.MimeType;
import at.treedb.ci.Node;
import at.treedb.ci.NodeDummy;
import at.treedb.domain.Domain;
import at.treedb.i18n.IstringDummy;
import at.treedb.i18n.Locale;
import at.treedb.i18n.SupportedLanguage;
import at.treedb.i18n.Locale.LOCALE;
import at.treedb.ui.Import;
import at.treedb.ui.Rectangle;
import at.treedb.ui.UIblob;
import at.treedb.ui.UIcheckbox;
import at.treedb.ui.UIdateField;
import at.treedb.ui.UIelement;
import at.treedb.ui.UIfile;
import at.treedb.ui.UIgrouping;
import at.treedb.ui.UIgroupingEnd;
import at.treedb.ui.UImacro;
import at.treedb.ui.UIoption;
import at.treedb.ui.UIselect;
import at.treedb.ui.UIslider;
import at.treedb.ui.UItab;
import at.treedb.ui.UItextArea;
import at.treedb.ui.UItextField;
import at.treedb.ui.UIwikiImage;
import at.treedb.ui.UIwikiTextArea;
import at.treedb.ui.UIoption.UIoptionDummy;
import at.treedb.ui.UIselect.SelectType;
import at.treedb.ui.UItab.ContentMode;
import at.treedb.ui.UItab.Layout;
import at.treedb.ui.UItab.TabType;
import at.treedb.ui.UItab.TextProcessor;
import at.treedb.ui.UItextArea.TextArea;
import at.treedb.user.User;
import at.treedb.util.FileAccess;

/**
 * <p>
 * Helper class to centralize/simplify DAO operations.
 * </p>
 * 
 * @author Peter Sauer
 * 
 */
// TODO: rework fallback paths
public class DAOhelper {
    private Domain domain;
    private User user;
    private DAOiface dao = null;

    private ArrayList<NodeDummy> ciConnectList = new ArrayList<NodeDummy>();
    private ArrayList<NodeDummy> ciTypeConnectList = new ArrayList<NodeDummy>();

    private ArrayList<NodeDummy> ciDisconnectList = new ArrayList<NodeDummy>();
    private ArrayList<NodeDummy> ciTypeDisconnectList = new ArrayList<NodeDummy>();
    private HashSet<Integer> deletedCIs = new HashSet<Integer>();
    private HashSet<Integer> deletedCItypes = new HashSet<Integer>();
    private HashMap<Integer, Connectable> ciTypeMap;
    private ArrayList<TabInfo> uiTabList = new ArrayList<TabInfo>();
    private ArrayList<ElementInfo> uiElementList = new ArrayList<ElementInfo>();

    private void reset() {
        ciConnectList = new ArrayList<NodeDummy>();
        ciTypeConnectList = new ArrayList<NodeDummy>();
        ciDisconnectList = new ArrayList<NodeDummy>();
        ciTypeDisconnectList = new ArrayList<NodeDummy>();
        uiTabList = new ArrayList<TabInfo>();
        uiElementList = new ArrayList<ElementInfo>();
    }

    private class TabInfo {
        private CItype ciType;
        private UItab uiTab;

        public TabInfo(CItype ciType, UItab uiTab) {
            this.ciType = ciType;
            this.uiTab = uiTab;
        }

        public CItype getCItype() {
            return ciType;
        }

        public UItab getUiTab() {
            return uiTab;
        }

    }

    private class ElementInfo {
        private UItab uiTab;
        private UIelement uiElement;

        public ElementInfo(UItab uiTab, UIelement uiElement) {
            this.uiTab = uiTab;
            this.uiElement = uiElement;
        }

        public UItab getUiTab() {
            return uiTab;
        }

        public UIelement getUIelement() {
            return uiElement;
        }

    }

    /**
     * Creates a DAO helper/context for the given pair domain/user.
     * 
     * @param domain
     *            domain
     * @param user
     *            user
     */
    public DAOhelper(Domain domain, User user) {
        this.domain = domain;
        this.user = user;
    }

    public DAOhelper(Domain domain, User user, DAOiface dao) {
        this.domain = domain;
        this.user = user;
        this.dao = dao;
    }

    /**
     * Returns the DAO interface.
     * 
     * @return DAO interface
     */
    public DAOiface getDAOiface() {
        return dao;
    }

    /**
     * Starts a DAO transaction.
     * 
     * @throws Exception
     */
    public void beginTransaction() throws Exception {
        dao = DAO.getDAO();
        dao.beginTransaction();
    }

    public ArrayList<ClazzDummy> writeTreeDBifaceClasses(FileAccess fileAccess, String classDir, String srcDir,
            String packageName) throws Exception {
        ArrayList<ClazzDummy> clazzList = Clazz.readClasses(fileAccess, classDir, srcDir, packageName);
        for (ClazzDummy d : clazzList) {
            Clazz.create(dao, domain, user, d.getName(), d.getSource(), d.getData());
        }
        return clazzList;
    }

    public ArrayList<ClazzDummy> writeTreeDBifaceClasses(DAOiface dao, FileAccess fileAccess, String classDir,
            String srcDir, String packageName) throws Exception {
        ArrayList<ClazzDummy> clazzList = Clazz.readClasses(fileAccess, classDir, srcDir, packageName);
        for (ClazzDummy d : clazzList) {
            Clazz.create(dao, domain, user, d.getName(), d.getSource(), d.getData());
        }
        return clazzList;
    }

    private void check(Connectable child, Connectable parent) throws Exception {
        for (Connectable c : parent.getParents()) {
            if (c.getId() == child.getId()) {
                throw new Exception("check() Up error!");
            }
            check(child, c);
        }
    }

    public void buildConnections() throws Exception {

        if (ciConnectList.size() > 0) {
            // connect all CIs
            synchronized (domain.getCImap()) {
                for (NodeDummy node : ciConnectList) {
                    node.getChild().getParents().add(node.getParent());
                    node.getParent().getChildren().add(node.getChild());
                }
            }
            ciConnectList.clear();
        }
        if (ciTypeConnectList.size() > 0) {

            if (ciTypeMap == null) {
                ciTypeMap = new HashMap<Integer, Connectable>();
                for (CItype t : domain.getCItypeMap().values()) {
                    ConnectionDummy d = new ConnectionDummy(t);
                    ciTypeMap.put(d.getId(), d);
                }
                for (CItype t : domain.getCItypeMap().values()) {
                    ConnectionDummy d = new ConnectionDummy(t);
                    for (Connectable c : d.getParents()) {
                        Connectable parent = ciTypeMap.get(c.getId());
                        parent.getChildren().add(c);
                        c.getParents().add(parent);
                    }
                }
            }
            for (NodeDummy node : ciTypeConnectList) {
                int parentIDnode = node.getParent().getId();
                if (parentIDnode == Node.PARENT_IS_A_DOMAIN) {
                    continue;
                }
                Connectable child = ciTypeMap.get(node.getChild().getId());
                Connectable parent = ciTypeMap.get(node.getParent().getId());
                check(child, parent);
                child.getParents().add(parent);
                parent.getChildren().add(child);
            }

            // connect all CItypes
            synchronized (domain.getCItypeMap()) {
                for (NodeDummy node : ciTypeConnectList) {
                    node.getChild().getParents().add(node.getParent());
                    node.getParent().getChildren().add(node.getChild());
                    // ((Base) node.getChild()).unlock();
                    // ((Base) node.getParent()).unlock();
                }
            }
            ciTypeConnectList.clear();
        }
        if (uiElementList.size() > 0) {
            for (ElementInfo i : uiElementList) {
                i.getUiTab().addUIelement(i.getUIelement());
            }
            uiElementList.clear();
        }
        if (uiTabList.size() > 0) {
            for (TabInfo i : uiTabList) {
                i.getCItype().addUItab(i.getUiTab());
            }
            uiTabList.clear();
        }
        if (ciTypeDisconnectList.size() > 0) {
            for (NodeDummy d : ciTypeDisconnectList) {
                Connectable c = d.getChild();
                Connectable p = d.getParent();
                c.getParents().remove(p);
                p.getChildren().remove(c);
            }
        }
        if (ciDisconnectList.size() > 0) {
            for (NodeDummy d : ciDisconnectList) {
                Connectable c = d.getChild();
                Connectable p = d.getParent();
                c.getParents().remove(p);
                p.getChildren().remove(c);
            }
        }
    }

    /**
     * Ends a DAO transaction.
     * 
     * @throws Exception
     */
    public void endTransaction() throws Exception {
        if (dao == null) {
            throw new Exception("DAOhelper.endTransaction(): Missing beginTransaction() call!");
        }
        buildConnections();
        dao.endTransaction();
    }

    public void flush() throws Exception {
        if (dao == null) {
            throw new Exception("DAOhelper.flush(): Missing beginTransaction() call!");
        }
        buildConnections();
        dao.flush();
    }

    /**
     * Rollbacks a DAO transaction.
     */
    public void rollback() {
        if (dao != null) {
            dao.rollback();
        }
    }

    /**
     * Creates a CI type
     * 
     * @param name
     *            name of the CI type
     * @param alias
     *            alias name of the CI type
     * @param description
     *            description of the CI type
     * @param image
     *            image of the CI type
     * @param internalId
     *            user defined internal ID
     * @return {@code CItype}
     */
    public CItype createCItype(String name, String alias, String description, ImageDummy image, int internalId)
            throws Exception {
        synchronized (domain.getCItypeMap()) {
            CItype t = CItype.create(dao, domain, user, name, alias, description, image, internalId);
            domain.putCItype(t);
            return t;
        }
    }

    /**
     * 
     * @param type
     * @param map
     * @throws Exception
     */
    public void updateCItype(CItype type, UpdateMap map) throws Exception {
        CItype.update(dao, user, type, map);
    }

    /**
     * 
     * @param type
     * @throws Exception
     */
    public void removeCItype(CItype type) throws Exception {
        if (type.getParents().size() > 0) {
            throw new Exception(
                    "removeCItype(): Unable to delete CItype. CItype has " + type.getParents().size() + " parent(s)!");
        }
        if (type.getChildren().size() > 0) {
            throw new Exception("removeCItype(): Unable to delete CItype. CItype has " + type.getParents().size()
                    + " child/children!");
        }
        long count = Base.countRow(dao, CI.class, HistorizationIface.STATUS.ACTIVE, "type =" + type.getHistId());
        if (count > 0) {
            throw new Exception("removeCItype(): Unable to delete CItype. CItype is used by " + count + " CI(s)!");
        }
        CItype.delete(dao, domain, user, type);
        domain.removeCItype(type);
    }

    /**
     * 
     * @param type
     * @param name
     * @param alias
     * @return
     * @throws Exception
     */
    public CI createCI(CItype type, String name, String alias) throws Exception {
        return createCI(type, name, alias, null);
    }

    /**
     * 
     * @param type
     * @param name
     * @param alias
     * @return
     * @throws Exception
     */
    public CI createCI(CItype type, String name, String alias, UpdateMap umap) throws Exception {
        synchronized (domain.getCImap()) {
            if (domain.isUniqueCInames() && domain.getCI(name) != null) {
                throw new Exception("DAOhelper.createCI(): CI name is not unique: " + name);
            }
            CI c = CI.create(dao, domain, user, type, name, alias);
            if (umap != null) {
                updateCI(c, umap);
            }
            domain.putCI(c);
            return c;
        }
    }

    /**
     * 
     * @param type
     * @param name
     * @param alias
     * @param inMemory
     * @param ciContext
     * @return
     * @throws Exception
     */
    public CI createCI(CItype type, String name, String alias, boolean inMemory, CI ciContext) throws Exception {
        synchronized (domain.getCImap()) {
            if (name != null) {
                if (domain.getCI(name) != null) {
                    throw new Exception("DAOhelper.createCI(): CI name is not unique: " + name);
                }
            }
            CI c = CI.create(dao, domain, user, type, name, alias, inMemory, ciContext);
            if (inMemory) {
                domain.putCI(c);
            }
            return c;
        }
    }

    /**
     * Updates a {@code CI}.
     * 
     * @param ci
     *            CI
     * @param map
     *            update map
     * @throws Exception
     */
    public void updateCI(CI ci, UpdateMap map) throws Exception {
        CI.update(dao, user, ci, map);
    }

    /**
     * 
     * @param tab
     * @param map
     * @throws Exception
     */
    public void updateUItab(UItab tab, UpdateMap map) throws Exception {
        UItab.update(dao, user, tab, map);
    }

    public void updateUIelement(UIelement element, UpdateMap map) throws Exception {
        UItab.update(dao, user, element, map);
    }

    public void updateUIoption(UIoption element, UpdateMap map) throws Exception {
        UIoption.update(dao, user, element, map);
    }

    @SuppressWarnings("unchecked")
    public void saveOrUpdateCIdata(CI ci, UpdateCIdata data) throws Exception {
        HashMap<String, Object> map = data.getUpdates();
        for (String name : map.keySet()) {
            UIelement ui = ci.getCItypeObj().getUIelement(name);
            switch (ui.getDataType()) {
            case CISTRING:
                CIstring.createOrUpdate(dao, domain, user, ci.getHistId(), ci.getCIType(), ui.getComposedId(),
                        (String) map.get(name));
                break;
            case CILONG:
                CIlong.createOrUpdate(dao, domain, user, ci.getHistId(), ci.getCIType(), ui.getComposedId(),
                        (Long) map.get(name));
                break;
            case CIDOUBLE:
                CIdouble.createOrUpdate(dao, domain, user, ci.getHistId(), ci.getCIType(), ui.getComposedId(),
                        (Double) map.get(name));
                break;

            case CII18NSTRING: {
                ArrayList<IstringDummy> list = (ArrayList<IstringDummy>) map.get(name);
                for (IstringDummy d : list) {
                    CIi18nString.createOrUpdate(dao, domain, user, ci.getHistId(), ci.getCIType(), ui.getComposedId(),
                            d.getText(), d.getLanguage());
                }
                break;
            }
            case CIIMAGE: {
                ArrayList<ImageDummy> list = (ArrayList<ImageDummy>) map.get(name);
                for (ImageDummy d : list) {
                    if (!d.isThumbnail()) {
                        CIimage.createOrUpdate(dao, domain, user, ci.getHistId(), ci.getCIType(), ui.getComposedId(),
                                d.getName(), d.getDescription(), d.getLicense(), d.getMimeType(), d.getData());
                    } else {
                        CIimage.createOrUpdate(dao, domain, user, ci.getHistId(), ci.getCIType(), ui.getComposedId(),
                                d.getName(), d.getDescription(), d.getLicense(), d.getMimeType(), d.getData(),
                                d.getMaxWidth(), d.getMaxHeight(), d.isDetachImage());
                    }
                }
                break;
            }
            case CIFILE: {
                ArrayList<FileDummy> list = (ArrayList<FileDummy>) map.get(name);
                for (FileDummy d : list) {
                    CIfile.createOrUpdate(dao, domain, user, ci.getHistId(), ci.getCIType(), ui.getComposedId(),
                            d.getName(), d.getDescription(), d.getLicense(), d.getFile(), d.getArchivData(),
                            d.getMimeType(), d.getFilePreview(), d.getMimeTypePreview());
                }
                break;
            }
            case CIDATE: {
                CIdate.createOrUpdate(dao, domain, user, ci.getHistId(), ci.getCIType(), ui.getComposedId(),
                        (Date) map.get(name));
                break;
            }
            case CIBOOLEAN: {
                CIboolean.createOrUpdate(dao, domain, user, ci.getHistId(), ci.getCIType(), ui.getComposedId(),
                        (Boolean) map.get(name));
                break;
            }
            case CIBLOB: {
                CIblob.createOrUpdate(dao, domain, user, ci.getHistId(), ci.getCIType(), ui.getComposedId(),
                        (byte[]) map.get(name));
                break;
            }
            default: {
                throw new Exception("DAOhelper.saveOrUpdateCIdata(): Not supported CIdata type!");
            }
            }

        }

    }

    public void deleteCI(CI ci) throws Exception {
        if (ci.getChildren().size() > 1) {
            for (Connectable c : ci.getChildren()) {
                if (!deletedCIs.contains(c.getId())) {
                    throw new Exception("DAOhelper.delete(): CI has children");
                }
            }

        }
        deletedCIs.add(ci.getHistId());
        for (Connectable parent : ci.getParents()) {
            disconnect(ci, (CI) parent);
        }
        Base.delete(dao, user, ci, false);
        domain.getCImap().remove(ci.getHistId());
    }

    public void deleteCIimage(int histID, boolean deleteCache) throws Exception {
        CIimage.delete(dao, user, histID);
        if (deleteCache) {
            CacheEntry.dbDelete(dao, domain, ClassID.CIIMAGE, histID);
        }
    }

    /**
     * 
     * @param child
     * @param parent
     * @return
     * @throws Exception
     */
    public Node connect(CI child, CI parent) throws Exception {
        if (child == null || parent == null) {
            throw new Exception("DAOhelper.connect(): child or parent is null!");
        }
        // check connections
        /*
         * HashSet<Connectable> con = domain.getCItype(parent.getCIType())
         * .getChildren(); boolean possible = false; for (Connectable c : con) {
         * if (c.getEntityId() == child.getCIType()) { possible = true; } } if
         * (!possible) { throw new Exception(
         * "Node.connect(): This connection isn' possible according the reference tree!"
         * ); }
         */
        Node node = Node.create(dao, domain, user, Node.NodeType.CI, child, parent);
        ciConnectList.add(new NodeDummy(child, parent, Node.NodeType.CI));
        return node;
    }

    /**
     * 
     * @param child
     * @param parent
     * @return
     * @throws Exception
     */
    public Node connect(CItype child, CItype parent) throws Exception {
        Connectable cparent = null;
        Node node = Node.create(dao, domain, user, Node.NodeType.CITYPE, child, parent);

        if (parent != null) {
            cparent = parent;
        } else {
            cparent = domain;
        }

        ciTypeConnectList.add(new NodeDummy(child, cparent, Node.NodeType.CITYPE));
        return node;
    }

    public void disconnect(CItype child, CItype parent) throws Exception {
        if (!child.getParents().contains(parent) || (parent != null && !parent.getChildren().contains(child))) {
            throw new Exception("Node.disconnect(): No child/parent relationship for this CItypes!");
        }
        Node.delete(dao, domain, user, child, parent);
        ciTypeDisconnectList.add(new NodeDummy(child, parent, Node.NodeType.CITYPE));
    }

    public void disconnect(CI child, CI parent) throws Exception {
        if (!child.getParents().contains(parent) || (parent != null && !parent.getChildren().contains(child))) {
            throw new Exception("Node.disconnect(): No child/parent relationship for this CItypes!");
        }
        Node.delete(dao, domain, user, child, parent);
        ciDisconnectList.add(new NodeDummy(child, parent, Node.NodeType.CI));
    }

    /**
     * 
     * @param name
     * @param data
     * @param mimeType
     * @return
     * @throws Exception
     */
    public Image createImage(String name, byte[] data, MimeType mimeType, String license) throws Exception {
        return Image.create(dao, domain, user, name, data, mimeType, license);
    }

    public CIimage createCIimage(int ci, int ciType, long uiElement, String name, byte[] data, MimeType mimeType,
            String description, String license) throws Exception {
        return CIimage.create(dao, domain, user, ci, ciType, uiElement, name, description, license, mimeType, data);
    }

    /**
     * 
     * @param displayTab
     * @param ciType
     * @param internalName
     * @param name
     * @param iconType
     * @param icon
     * @param tabType
     * @param template
     * @param processor
     * @param contentMode
     * @param dataImport
     * @param layout
     * @param update
     * @return
     * @throws Exception
     */
    public UItab createUItab(boolean displayTab, CItype ciType, String internalName, String name,
            UItab.IconType iconType, ImageDummy icon, TabType tabType, String template, TextProcessor processor,
            ContentMode contentMode, Import dataImport, Layout layout, UpdateMap update) throws Exception {
        UItab tab = UItab.create(dao, domain, user, displayTab, ciType, internalName, name, iconType, icon, tabType,
                template, processor, contentMode, dataImport, layout);
        if (update != null) {
            updateUItab(tab, update);
        }
        uiTabList.add(new TabInfo(ciType, tab));
        return tab;

    }

    /**
     * Creates Wiki tab.
     * 
     * @param ciType
     *            {@code CItype} which owns the Wiki tab
     * @param name
     *            tab name
     * @param processor
     *            optional text processor, can be {@code null}
     * @param dataImport
     *            optional data import directive, can be {@code null}
     * @return {@code UItab}
     * @throws Exception
     */
    public UItab createWikiUItab(CItype ciType, String name, TextProcessor processor, Import dataImport)
            throws Exception {
        UItab tab = UItab.create(dao, domain, user, true, ciType, null, name, UItab.IconType.CITYPE, null, TabType.WIKI,
                null, processor, UItab.ContentMode.HTML, dataImport, null);
        uiTabList.add(new TabInfo(ciType, tab));
        createUIrichTextField(tab, "content", null, null, 0);
        createUIimage(tab, "images", null, null);
        createUIfile(tab, "files", null, null);
        return tab;

    }

    /*
     * public UItab createBrowserUItab(CItype ciType, String name) throws
     * Exception { UItab tab = UItab.create(dao, domain, user, ciType,null,
     * name, UItab.IconType.CITYPE,null,TabType.BROWSER, null, null,
     * null,null,null); uiTabList.add(new TabInfo(ciType,tab));
     * createUIrichTextField(tab, "comment",null, null, 0); this.cre return tab;
     * 
     * }
     */

    /**
     * 
     * @param uiTab
     * @param dataType
     * @param fieldName
     * @param displayName
     * @param description
     * @param columns
     * @param lowerLimit
     * @param upperLimit
     * @param mandatory
     * @param mandatoryError
     * @param validator
     * @param validationRegExpr
     * @param validationError
     * @param update
     * @return
     * @throws Exception
     */
    public UItextField createUItextField(UItab uiTab, ClassID dataType, String fieldName, String displayName,
            String description, int columns, double lowerLimit, double upperLimit, boolean mandatory,
            String mandatoryError, UItextField.VALIDATOR validator, String validationRegExpr, String validationError,
            UpdateMap update) throws Exception {
        UItextField uif = UItextField.createUItextField(dao, domain, user, uiTab, dataType, fieldName, displayName,
                description, columns, lowerLimit, upperLimit, mandatory, mandatoryError, validator, validationRegExpr,
                validationError);
        if (update != null) {
            updateUIelement(uif, update);
        }
        uiElementList.add(new ElementInfo(uiTab, uif));
        return uif;

    }

    /**
     * 
     * @param uiTab
     * @param fieldName
     * @param displayName
     * @param description
     * @param mandatory
     * @param mandatoryError
     * @param selectType
     * @param multiSelect
     * @param rows
     * @param nullSelectionAllowed
     * @param selection
     * @param updates
     * @return
     * @throws Exception
     */
    public UIselect createUIselect(UItab uiTab, String fieldName, String displayName, String description,
            boolean mandatory, String mandatoryError, SelectType selectType, boolean multiSelect, int rows,
            boolean nullSelectionAllowed, UIoptionDummy[] selection, LanguageUpdate[] updates) throws Exception {
        ArrayList<UIoption> optionList = new ArrayList<UIoption>();
        UIselect us = UIselect.createUIselect(dao, domain, user, uiTab, fieldName, displayName, description, mandatory,
                mandatoryError, selectType, multiSelect, rows, nullSelectionAllowed, selection, optionList);
        if (updates != null) {
            int index = 0;
            updateUIelement(us, updates[index++].getUpdateMap());
            if (updates.length > 1) {
                for (UIoption option : optionList) {
                    updateUIoption(option, updates[index++].getUpdateMap());
                }
            }
        }
        uiElementList.add(new ElementInfo(uiTab, us));
        return us;
    }

    /**
     * Creates a {@code UItextArea}
     * 
     * @param uiTab
     * @param fieldName
     *            internal field name of the text area
     * @param displayName
     *            displayed name of the text area
     * @param description
     *            description of the text area
     * @param maxLength
     *            maximum text length
     * @param columns
     *            width of the text ares
     * @param rows
     *            numer
     * @param wordWrap
     * @param mandatory
     * @param mandatoryError
     * @param validationRegExpr
     * @param validationError
     * @param textArea
     * @param aceMode
     * @param update
     * @return {@code UItextArea} object
     * @throws Exception
     */
    public UItextArea createUItextArea(UItab uiTab, String fieldName, String displayName, String description,
            int maxLength, int columns, int rows, boolean wordWrap, boolean mandatory, String mandatoryError,
            String validationRegExpr, String validationError, TextArea textArea, String aceMode, UpdateMap update)
                    throws Exception {
        UItextArea uia = UItextArea.createUItextArea(dao, domain, user, uiTab, fieldName, displayName, description,
                maxLength, columns, rows, wordWrap, mandatory, mandatoryError, validationRegExpr, validationError,
                textArea, aceMode);
        if (update != null) {
            updateUIelement(uia, update);
        }
        uiElementList.add(new ElementInfo(uiTab, uia));
        return uia;
    }

    /**
     * 
     * @param ciType
     * @param uiElement
     * @param name
     * @param locale
     * @param macro
     * @param description
     * @return
     * @throws Exception
     */
    public UImacro createUImacro(int ciType, long uiElement, String name, LOCALE locale, String macro,
            String description) throws Exception {
        return UImacro.create(dao, domain, user, 0, ciType, uiElement, name, locale, macro, description);
    }

    public UIdateField createUIdateField(UItab uiTab, String fieldName, String displayName, String description,
            boolean mandatory, String mandatoryError, String validationError, UpdateMap update) throws Exception {
        UIdateField uid = UIdateField.createUIdateField(dao, domain, user, uiTab, fieldName, displayName, description,
                mandatory, mandatoryError, validationError);
        if (update != null) {
            updateUIelement(uid, update);
        }
        uiElementList.add(new ElementInfo(uiTab, uid));
        return uid;
    }

    public UIcheckbox createUIcheckbox(UItab uiTab, String fieldName, String displayName, String description)
            throws Exception {
        UIcheckbox uic = UIcheckbox.createUICheckbox(dao, domain, user, uiTab, fieldName, displayName, description);
        uiElementList.add(new ElementInfo(uiTab, uic));
        return uic;
    }

    public UIblob createUIblob(UItab uiTab, String fieldName, String displayName, String description) throws Exception {
        UIblob uic = UIblob.createUIblob(dao, domain, user, uiTab, fieldName, displayName, description);
        uiElementList.add(new ElementInfo(uiTab, uic));
        return uic;
    }

    public UIslider createUIslider(UItab uiTab, String fieldName, String displayName, String description, double min,
            double max, int resolution, String width) throws Exception {
        UIslider uis = UIslider.createUIslider(dao, domain, user, uiTab, fieldName, displayName, description, min, max,
                resolution, width);
        uiElementList.add(new ElementInfo(uiTab, uis));
        return uis;
    }

    public UIgrouping createUIpanel(UItab uiTab, String displayName, String description, UItab.IconType iconType,
            ImageDummy icon, String panelWidth, Rectangle rec, UpdateMap update) throws Exception {
        UIgrouping uig = UIgrouping.createUIgroupingElement(dao, domain, user, uiTab, UIgrouping.Grouping.PANEL,
                displayName, description, iconType, icon, panelWidth, rec);
        if (update != null) {
            updateUIelement(uig, update);
        }
        uiElementList.add(new ElementInfo(uiTab, uig));
        return uig;
    }

    public UIgrouping createUItable(UItab uiTab, String displayName, String description, UItab.IconType iconType,
            ImageDummy icon, String panelWidth, Rectangle rec) throws Exception {
        UIgrouping uig = UIgrouping.createUIgroupingElement(dao, domain, user, uiTab, UIgrouping.Grouping.TABLE,
                displayName, description, iconType, icon, panelWidth, rec);
        uiElementList.add(new ElementInfo(uiTab, uig));
        return uig;
    }

    public UIgroupingEnd createUIpanelEnd(UItab uiTab) throws Exception {
        UIgroupingEnd groupEnd = UIgroupingEnd.createUIgroupingEnd(dao, domain, user, uiTab, UIgrouping.Grouping.PANEL);
        uiElementList.add(new ElementInfo(uiTab, groupEnd));
        return groupEnd;
    }

    public UIgroupingEnd createUItableEnd(UItab uiTab) throws Exception {
        UIgroupingEnd groupEnd = UIgroupingEnd.createUIgroupingEnd(dao, domain, user, uiTab, UIgrouping.Grouping.TABLE);
        uiElementList.add(new ElementInfo(uiTab, groupEnd));
        return groupEnd;
    }

    public UIwikiTextArea createUIrichTextField(UItab uiTab, String fieldName, String displayName, String description,
            int maxLength) throws Exception {
        UIwikiTextArea uif = UIwikiTextArea.createUIrichTextField(dao, domain, user, uiTab, fieldName, displayName,
                description, maxLength);
        uiElementList.add(new ElementInfo(uiTab, uif));
        return uif;
    }

    public UIwikiImage createUIimage(UItab uiTab, String fieldName, String displayName, String description)
            throws Exception {
        UIwikiImage uif = UIwikiImage.createUIimage(dao, domain, user, uiTab, fieldName, displayName, description);
        uiElementList.add(new ElementInfo(uiTab, uif));
        return uif;
    }

    public UIfile createUIfile(UItab uiTab, String fieldName, String displayName, String description) throws Exception {
        UIfile uif = UIfile.createUIfile(dao, domain, user, uiTab, fieldName, displayName, description);
        uiElementList.add(new ElementInfo(uiTab, uif));
        return uif;
    }

    public SupportedLanguage addSupporedLanuage(Locale locale, int priority) throws Exception {
        return SupportedLanguage.create(dao, domain, user, locale, priority);
    }

}
