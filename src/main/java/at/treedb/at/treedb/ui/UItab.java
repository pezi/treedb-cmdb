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
package at.treedb.ui;

import java.util.ArrayList;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import at.treedb.ci.CItype;
import at.treedb.ci.Image;
import at.treedb.ci.ImageDummy;
import at.treedb.db.Base;
import at.treedb.db.ClassID;
import at.treedb.db.DAO;
import at.treedb.db.DAOiface;
import at.treedb.db.DBkey;
import at.treedb.db.UpdateMap;
import at.treedb.domain.Domain;
import at.treedb.i18n.Istring;
import at.treedb.user.User;

/**
 * <p>
 * Container for a UI tab.
 * </p>
 * 
 * @author Peter Sauer
 * 
 */
@SuppressWarnings("serial")
// TODO: Update for complex fields
@Entity
public class UItab extends Base implements Cloneable {

    public enum Fields {
        caption, template, textProcessor, contentMode
    }

    /**
     * Different text processors for creating the tab content.
     */
    public enum TextProcessor {
        NONE,
        /**
         * Simple text substitution.
         */
        SIMPLE,
        /**
         * http://freemarker.sourceforge.net/
         */
        FREEMARKER,
        /**
         * http://velocity.apache.org/
         */
        VELOCITY,
        /**
         * Java based PHP interpreter http://quercus.caucho.com
         * http://www.caucho.com/resin-3.1/doc/quercus.xtp
         */
        RESIN_QUERCUS,
        /**
         * Beanshell http://www.beanshell.org/ Interpreter which provides the
         * content
         */
        BEANSHELL,
        /**
         * Java class which provides the content
         */
        CLASS,
        /**
         * Shows only the data edit component in the read only mode
         */
        READONLY
    };

    /**
     * Source of the tab icon.
     */
    public enum IconType {
        /**
         * Tab has no icon.
         */
        NONE,
        /**
         * Tab uses the same icon like the CI type.
         */
        CITYPE,
        /**
         * Tab uses a system icon by its name.
         */
        SYSTEM,
        /**
         * Tab uses an own icon.
         */
        ICON
    };

    /**
     * Content mode
     */
    public enum ContentMode {
        /**
         * plain text
         */
        TEXT,
        /**
         * preformatted text
         */
        PREFORMATTED,
        /**
         * HTML
         */
        HTML,
        /**
         * LaTeX/MathML support http://www.mathjax.org/
         */
        MATHJAX, HIGHLIGHT

    }

    /**
     * Tab types
     */
    public enum TabType {
        /**
         * WIKI based tab
         */
        WIKI,
        /**
         * UI composed tab with an optional template
         */
        UIELEMENTS,
        /**
         * embedded browser
         */
        BROWSER,
    }

    /**
     * Tab layout
     */
    public enum Layout {
        VERTICAL, GRID
    }

    // display the tab/template, if false the tab is a data only tab
    private boolean isDisplayed;
    @DBkey(CItype.class)
    private int ciType; // CItype
    private String name; // internal tab name
    // tab update list
    private String dataUpdate;
    // data import from other CIs
    private String dataImport;
    @DBkey(Istring.class)
    private int caption; // tab caption
    private IconType iconType;
    @DBkey(Image.class)
    private int icon; // icon
    private String iconName;
    @Column(name = "m_index")
    private int index; // internal ordinal number for tabs
    private TabType tabType;
    @DBkey(Istring.class)
    private int template; // template
    private TextProcessor textProcessor; // text processor
    private ContentMode contentMode;
    private Layout layout;
    @Transient
    private ArrayList<UIelement> uiElements = new ArrayList<UIelement>();
    @Transient
    private CItype ciTypeObj;

    protected UItab() {
    }

    /**
     * Constructor
     * 
     * @param displayTab
     *            {@code true} if the tab/template will be displayed,
     *            {@code false} if the tab is a data only tab
     * @param ciType
     *            associated {@code ciType}
     * @param tabName
     *            internal tab name
     * @param caption
     *            tab caption (Istring)
     * @param iconType
     *            icon type
     * @param iconName
     *            iconNmae
     * @param icon
     *            icon ID
     * @param tabType
     *            tab type
     * @param template
     *            content template
     * @param processor
     *            text processor
     * @param contentMode
     *            content mode
     * @param dataImport
     *            data import
     * @param layout
     *            content layout
     */
    private UItab(boolean displayTab, CItype ciType, String tabName, Istring caption, IconType iconType,
            String iconName, int icon, TabType tabType, Istring template, TextProcessor processor,
            ContentMode contentMode, Import dataImport, Layout layout) {
        this.isDisplayed = displayTab;
        this.ciTypeObj = ciType;
        this.ciType = ciType.getHistId();
        if (caption != null) {
            this.caption = caption.getHistId();
        }
        this.iconType = iconType;
        this.iconName = iconName;
        this.name = tabName;
        if (dataImport != null) {
            this.dataUpdate = dataImport.getUpdates();
            this.dataImport = dataImport.getImports();
        }
        this.icon = icon;
        this.tabType = tabType;
        if (template != null) {
            this.template = template.getHistId();
        }
        this.textProcessor = processor;
        this.contentMode = contentMode;
        this.layout = layout;
    }

    /**
     * Returns the internal name of the tab.
     * 
     * @return internal name
     */
    public String getInternalName() {
        return name;
    }

    /**
     * Returns the import directive.
     * 
     * @return import directive
     */
    public Import getDataImport() {
        if (dataUpdate == null && dataImport == null) {
            return null;
        }
        return new Import(dataUpdate, dataImport);
    }

    /**
     * Creates a {@code UItab}.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param domain
     *            {@code Domain} of the data element
     * @param user
     *            creator of the {@code UItab}
     * @param ciType
     *            ID of the {@code CItype}
     * @param name
     *            tab name
     * @param iconType
     *            icon type
     * @param dummyIcon
     *            dummy icon
     * @param template
     *            text template
     * @param processor
     *            text processor
     * @param contentMode
     *            content mode
     * @return {@code UItab}
     * @throws Exception
     */
    public static UItab create(DAOiface dao, Domain domain, User user, boolean displayTab, CItype ciType,
            String internalName, String name, IconType iconType, ImageDummy dummyIcon, TabType tabType, String template,
            TextProcessor processor, ContentMode contentMode, Import dataImport, Layout layout) throws Exception {
        UItab tab = null;
        boolean daoLocal = false;
        if (dao == null) {
            dao = DAO.getDAO();
            daoLocal = true;
        }
        try {
            if (daoLocal) {
                dao.beginTransaction();
            }
            Istring iName = null;
            if (name != null) {
                iName = Istring.create(dao, domain, user, ClassID.UITAB, name, domain.getLanguage());
            }
            Istring iTemplate = null;
            if (template != null) {
                iTemplate = Istring.create(dao, domain, user, ClassID.UITAB, template, domain.getLanguage());
            }
            int icon = 0;
            String iconName = null;
            String error = "UItab.create(): Wrong ImageDummy.IconType enum";
            if (iconType != null) {
                switch (iconType) {
                case ICON:
                    if (dummyIcon.getDummyType() != ImageDummy.DummyType.CREATE) {
                        throw new Exception(error);
                    }
                    icon = Image.create(dao, domain, user, dummyIcon.getName(), dummyIcon.getData(),
                            dummyIcon.getMimeType(), dummyIcon.getLicense()).getHistId();
                    break;
                case SYSTEM:
                    if (dummyIcon.getDummyType() != ImageDummy.DummyType.SYSTEM) {
                        throw new Exception(error);
                    }
                    iconName = dummyIcon.getName();
                    break;
                default:
                    break;
                }
            }
            tab = new UItab(displayTab, ciType, internalName, iName, iconType, iconName, icon, tabType, iTemplate,
                    processor, contentMode, dataImport, layout);
            Base.save(dao, domain, user, tab);
            if (daoLocal) {
                dao.endTransaction();
            }
        } catch (Exception e) {
            if (daoLocal) {
                dao.rollback();
            }
            throw e;
        }
        return tab;
    }

    /**
     * Returns {@code true} if the tab/template will be displayed, {@code false}
     * if the tab is a data only tab.
     * 
     * @return
     */
    public boolean isDisplayed() {
        return isDisplayed;
    }

    /**
     * Returns the tab caption.
     * 
     * @return tab caption
     */
    public @DBkey(Istring.class) int getCaption() {
        return caption;
    }

    /**
     * Returns the content mode.
     * 
     * @return content mode
     */
    public ContentMode getContentMode() {
        return contentMode;
    }

    /**
     * Returns the text template.
     * 
     * @return text template
     */
    public @DBkey(Istring.class) int getTemplate() {
        return template;
    }

    /**
     * Returns the {@code CItype} of the tab
     * 
     * @return CItype ID
     */
    public @DBkey(CItype.class) int getCiType() {
        return ciType;
    }

    /**
     * Returns the icon of the tab.
     * 
     * @return image ID
     */
    public @DBkey(Image.class) int getIcon() {
        return icon;
    }

    /**
     * Returns the tab type.
     * 
     * @return {@code TabType}
     */
    public TabType getTabType() {
        return tabType;
    }

    /**
     * Returns the name of the system icon, if the tab icon is a system icon.
     * 
     * @return system icon name, {@code null for the icon types
     */
    public String getIconName() {
        return iconName;
    }

    /**
     * Returns the text processor.
     * 
     * @return {@code TextProcessor}
     */
    public TextProcessor getTextProcessor() {
        return textProcessor;
    }

    /**
     * Returns a list of {@code UIelement} associated with this tab.
     * 
     * @return list of {@code UIelement}
     */
    public ArrayList<UIelement> getUIelements() {
        return uiElements;
    }

    /**
     * Sets a list of {@code UIelement} associated with this tab.
     * 
     * @param uiElements
     *            list of {@code UIelement}
     */
    public void setUIelements(ArrayList<UIelement> uiElements) {
        this.uiElements = uiElements;
    }

    /**
     * Adds a {@code UIelement}.
     * 
     * @param element
     *            {@code UIelement}
     */
    public void addUIelement(UIelement element) {
        uiElements.add(element);
    }

    /**
     * Returns the icon type of the tab.
     * 
     * @return {@code IconType}
     */
    public IconType getIconType() {
        return iconType;
    }

    /**
     * Returns the tab layout.
     * 
     * @return {@code Layout}
     */
    public Layout getLayout() {
        return layout;
    }

    /**
     * Returns the ordinal number of the tab index.
     * 
     * @return tab index
     */
    public int getIndex() {
        return index;
    }

    @Override
    public ClassID getCID() {
        return ClassID.UITAB;
    }

    @Override
    public void callbackBeforeSave() {
        this.index = ciTypeObj.getNextIndex();
    }

    /**
     * Updates a {@code UItab}
     * 
     * @param user
     *            {@code User} who performs the update operation
     * @param tab
     *            tab to be updated
     * @param map
     *            update map
     * @throws Exception
     */
    static public void update(User user, UItab tab, UpdateMap map) throws Exception {
        Base.update(user, tab, map);
    }

    /**
     * Updates a {@code UItab}
     * 
     * @param user
     *            {@code User} who performs the update operation
     * @param tab
     *            tab ID
     * @param map
     *            update map
     * @throws Exception
     */
    static public void update(User admin, int id, UpdateMap map) throws Exception {
        Base.update(admin, id, UItab.class, map);
    }

    /**
     * Returns the next index for a {@code UItab}/{@code UIelement} object.
     * 
     * @return index index for order elements
     */
    public int getNextIndex() {
        return ciTypeObj.getNextIndex();
    }

}
