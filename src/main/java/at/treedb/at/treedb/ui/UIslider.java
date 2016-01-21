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

import javax.persistence.Column;
import javax.persistence.Entity;

import at.treedb.db.Base;
import at.treedb.db.ClassID;
import at.treedb.db.DAO;
import at.treedb.db.DAOiface;
import at.treedb.domain.Domain;
import at.treedb.i18n.Istring;
import at.treedb.user.User;

@SuppressWarnings("serial")
@Entity
public class UIslider extends UIelement {
    @Column(name = "m_min")
    private double min;
    @Column(name = "m_max")
    private double max;
    private int resolution;
    private int sliderWidth;

    protected UIslider() {

    }

    protected UIslider(UItab uiTab, String fieldName, Istring displayName, Istring description, double min, double max,
            int resolution, Istring width) {
        super(uiTab, fieldName, displayName != null ? displayName.getHistId() : 0,
                description != null ? description.getHistId() : 0, false, 0);
        this.min = min;
        this.max = max;
        this.resolution = resolution;
        if (width != null) {
            this.sliderWidth = width.getHistId();
        }
    }

    public static UIslider createUIslider(DAOiface dao, Domain domain, User user, UItab uiTab, String fieldName,
            String displayName, String description, double min, double max, int resolution, String width)
                    throws Exception {

        UIslider date = null;
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
                iDescription = Istring.create(dao, domain, user, ClassID.UISLIDER, description, domain.getLanguage());
            }
            Istring iDisplay = null;
            if (displayName != null) {
                iDisplay = Istring.create(dao, domain, user, ClassID.UISLIDER, displayName, domain.getLanguage());
            }
            Istring iwidth = null;
            if (width != null) {
                iwidth = Istring.create(dao, domain, user, ClassID.UISLIDER, width, domain.getLanguage());
            }
            date = new UIslider(uiTab, fieldName, iDisplay, iDescription, min, max, resolution, iwidth);
            Base.save(dao, domain, user, date);
            if (daoLocal) {
                dao.endTransaction();
            }
        } catch (Exception e) {
            if (daoLocal) {
                dao.rollback();
            }
            throw e;
        }
        return date;
    }

    @Override
    public ClassID getDataType() {
        return ClassID.CIDOUBLE;
    }

    @Override
    public ClassID getCID() {
        return ClassID.UISLIDER;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public int getResoltion() {
        return resolution;
    }

    public int getWidth() {
        return sliderWidth;
    }

}
