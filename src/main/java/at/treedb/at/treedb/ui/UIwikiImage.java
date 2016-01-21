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

import at.treedb.db.Base;
import at.treedb.db.ClassID;
import at.treedb.db.DAO;
import at.treedb.db.DAOiface;
import at.treedb.domain.Domain;
import at.treedb.i18n.Istring;
import at.treedb.user.User;

@SuppressWarnings("serial")
@Entity
public class UIwikiImage extends UIelement {

    protected UIwikiImage() {

    }

    protected UIwikiImage(UItab uiTab, String fieldName, Istring displayName, Istring description) {
        super(uiTab, fieldName, displayName != null ? displayName.getHistId() : 0,
                description != null ? description.getHistId() : 0, false, 0);

    }

    public static UIwikiImage createUIimage(DAOiface dao, Domain domain, User user, UItab uiTab, String fieldName,
            String displayName, String description) throws Exception {

        UIwikiImage image = null;
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
                iDescription = Istring.create(dao, domain, user, ClassID.UITEXTFIELD, description,
                        domain.getLanguage());
            }
            Istring iDisplay = null;
            if (displayName != null) {
                iDisplay = Istring.create(dao, domain, user, ClassID.UITEXTFIELD, displayName, domain.getLanguage());
            }
            image = new UIwikiImage(uiTab, fieldName, iDisplay, iDescription);
            Base.save(dao, domain, user, image);
            if (daoLocal) {
                dao.endTransaction();
            }
        } catch (Exception e) {
            if (daoLocal) {
                dao.rollback();
            }
            throw e;
        }
        return image;
    }

    @Override
    public ClassID getDataType() {
        return ClassID.CIIMAGE;
    }

    @Override
    public ClassID getCID() {
        return ClassID.UIWIKIIMAGE;
    }

}
