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

/**
 * <p>
 * User interface element for a binary value.
 * </p>
 * 
 * @author Peter Sauer
 *
 */
@SuppressWarnings("serial")
@Entity
public class UIblob extends UIelement {

    protected UIblob() {

    }

    protected UIblob(UItab uiTab, String fieldName, Istring displayName, Istring description) {
        super(uiTab, fieldName, displayName != null ? displayName.getHistId() : 0,
                description != null ? description.getHistId() : 0, false, 0);

    }

    /**
     * Creates an {@code UIblob}.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param domain
     *            {@code Domain} of the data element
     * @param user
     *            creator of the element
     * @param uiTab
     *            {@code UIblob} is an element of this {@code UItab}
     * @param fieldName
     *            internal field name
     * @param displayName
     *            display name
     * @param description
     *            description of the element
     * @return {@code UIcheckbox} object
     * @throws Exception
     */
    public static UIblob createUIblob(DAOiface dao, Domain domain, User user, UItab uiTab, String fieldName,
            String displayName, String description) throws Exception {

        UIblob date = null;
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
                iDescription = Istring.create(dao, domain, user, ClassID.UIBLOB, description, domain.getLanguage());
            }
            Istring iDisplay = null;
            if (displayName != null) {
                iDisplay = Istring.create(dao, domain, user, ClassID.UIBLOB, displayName, domain.getLanguage());
            }

            date = new UIblob(uiTab, fieldName, iDisplay, iDescription);
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
        return ClassID.CIBLOB;
    }

    @Override
    public ClassID getCID() {
        return ClassID.UIBLOB;
    }

}
