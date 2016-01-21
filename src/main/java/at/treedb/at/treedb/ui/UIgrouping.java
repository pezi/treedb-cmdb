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

import javax.persistence.Entity;

import at.treedb.ci.Image;
import at.treedb.ci.ImageDummy;
import at.treedb.db.Base;
import at.treedb.db.ClassID;
import at.treedb.db.DAO;
import at.treedb.db.DAOiface;
import at.treedb.db.DBkey;
import at.treedb.domain.Domain;
import at.treedb.i18n.Istring;
import at.treedb.ui.UItab.IconType;
import at.treedb.user.User;

/**
 * 
 * Helper class to arrange UI elements.
 * 
 * @author Peter Sauer
 *
 */
@SuppressWarnings("serial")
@Entity
public class UIgrouping extends UIelement {

    protected UIgrouping() {

    }

    public enum Grouping {
        PANEL, TABLE
    }

    @DBkey(Istring.class)
    private int groupingWidth;
    private IconType iconType;
    private int icon; // icon
    private String iconName;
    private int xPos;
    private int yPos;
    private int width;
    private int height;
    private Grouping grouping;

    protected UIgrouping(UItab uiTab, Grouping grouping, Istring displayName, Istring description, IconType type,
            String iconName, int icon, Istring groupingWidth, Rectangle rec) {
        super(uiTab, null, displayName != null ? displayName.getHistId() : 0,
                description != null ? description.getHistId() : 0, false, 0);
        if (groupingWidth != null) {
            this.groupingWidth = groupingWidth.getHistId();
        }
        this.iconType = type;
        this.iconName = iconName;
        this.icon = icon;
        this.grouping = grouping;
        if (rec != null) {
            this.xPos = rec.getX();
            this.yPos = rec.getY();
            this.width = rec.getWidth();
            this.height = rec.getHeight();
        }
    }

    /**
     * 
     * @param dao
     * @param domain
     * @param user
     * @param uiTab
     * @param grouping
     * @param displayName
     * @param description
     * @param itype
     * @param dicon
     * @param panelWidth
     * @param rec
     * @return
     * @throws Exception
     */
    public static UIgrouping createUIgroupingElement(DAOiface dao, Domain domain, User user, UItab uiTab,
            Grouping grouping, String displayName, String description, IconType itype, ImageDummy dicon,
            String panelWidth, Rectangle rec) throws Exception {

        UIgrouping panel = null;
        boolean daoLocal = false;
        if (dao == null) {
            dao = DAO.getDAO();
            daoLocal = true;
        }
        try {
            if (daoLocal) {
                dao.beginTransaction();
            }
            Istring iDescription = null;
            if (description != null) {
                iDescription = Istring.create(dao, domain, user, ClassID.UIGROUPING, description, domain.getLanguage());
            }
            Istring iDisplay = null;
            if (displayName != null) {
                iDisplay = Istring.create(dao, domain, user, ClassID.UIGROUPING, displayName, domain.getLanguage());

            }
            Istring ipanelWidth = null;
            if (panelWidth != null) {
                ipanelWidth = Istring.create(dao, domain, user, ClassID.UIGROUPING, panelWidth, domain.getLanguage());
            }
            int icon = 0;
            String iconName = null;
            String error = "UIgrouping.create(): Wrong ImageDummy.IconType enum!";
            if (itype != null) {
                switch (itype) {

                case ICON:
                    if (dicon.getDummyType() != ImageDummy.DummyType.CREATE) {
                        throw new Exception(error);
                    }

                    icon = Image.create(dao, domain, user, dicon.getName(), dicon.getData(), dicon.getMimeType(),
                            dicon.getLicense()).getHistId();
                    break;
                case SYSTEM:
                    if (dicon.getDummyType() != ImageDummy.DummyType.SYSTEM) {
                        throw new Exception(error);
                    }
                    iconName = dicon.getName();
                    break;
                }
            }

            panel = new UIgrouping(uiTab, grouping, iDisplay, iDescription, itype, iconName, icon, ipanelWidth, rec);
            Base.save(dao, domain, user, panel);
            if (daoLocal) {
                dao.endTransaction();
            }
        } catch (Exception e) {
            if (daoLocal) {
                dao.rollback();
            }
            throw e;
        }
        return panel;
    }

    @Override
    public ClassID getDataType() {
        return null;
    }

    @Override
    public ClassID getCID() {
        return ClassID.UIGROUPING;
    }

    public int getWidth() {
        return groupingWidth;
    }

    public int getIcon() {
        return icon;
    }

    public IconType getIconType() {
        return iconType;
    }

    public String getIconName() {
        return iconName;
    }

    public int getX() {
        return xPos;
    }

    public int getY() {
        return yPos;
    }

    public int getGridWidth() {
        return width;
    }

    public int getGridHeight() {
        return height;
    }

    public Grouping getGrouping() {
        return grouping;
    }

}
