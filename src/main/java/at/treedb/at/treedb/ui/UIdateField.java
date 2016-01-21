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
import at.treedb.db.DBkey;
import at.treedb.domain.Domain;
import at.treedb.i18n.Istring;
import at.treedb.user.User;

/**
 * <p>
 * User interface element: date field
 * </p>
 * 
 * @author Peter Sauer
 *
 */
@SuppressWarnings("serial")
@Entity
public class UIdateField extends UIelement {
    @DBkey(Istring.class)
    private int validationError;

    protected UIdateField() {

    }

    protected UIdateField(UItab uiTab, String fieldName, Istring displayName, Istring description, boolean mandatory,
            Istring mandatoryError, Istring validationError) {
        super(uiTab, fieldName, displayName != null ? displayName.getHistId() : 0,
                description != null ? description.getHistId() : 0, mandatory,
                mandatoryError != null ? mandatoryError.getHistId() : 0);
        if (validationError != null) {
            this.validationError = validationError.getHistId();
        }
    }

    /**
     * Creates a {@code UIdateField} object.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param domain
     *            {@code Domain} of the data element
     * @param user
     *            creator of the element
     * @param uiTab
     *            {@code UIdateField} is an element of this {@code UItab}
     * @param fieldName
     *            internal field name
     * @param displayName
     *            display name
     * @param description
     *            description
     * @param mandatory
     *            {@code true} if this input element is mandatory, {@code false}
     *            if not
     * @param mandatoryError
     *            mandatory error message
     * @param validationError
     *            validation error message
     * @return {@code UIdateField} object
     * @throws Exception
     */
    public static UIdateField createUIdateField(DAOiface dao, Domain domain, User user, UItab uiTab, String fieldName,
            String displayName, String description, boolean mandatory, String mandatoryError, String validationError)
                    throws Exception {

        UIdateField date = null;
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
                iDescription = Istring.create(dao, domain, user, ClassID.UIDATEFIELD, description,
                        domain.getLanguage());
            }
            Istring iDisplay = null;
            if (displayName != null) {
                iDisplay = Istring.create(dao, domain, user, ClassID.UIDATEFIELD, displayName, domain.getLanguage());
            }
            Istring iMandatoryError = null;
            if (mandatoryError != null) {
                iMandatoryError = Istring.create(dao, domain, user, ClassID.UIDATEFIELD, mandatoryError,
                        domain.getLanguage());
            }

            Istring ivalidationError = null;
            if (validationError != null) {
                ivalidationError = Istring.create(dao, domain, user, ClassID.UIDATEFIELD, validationError,
                        domain.getLanguage());
            }

            date = new UIdateField(uiTab, fieldName, iDisplay, iDescription, mandatory, iMandatoryError,
                    ivalidationError);
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
        return ClassID.CIDATE;
    }

    @Override
    public ClassID getCID() {
        return ClassID.UIDATEFIELD;
    }

    public int getValidationError() {
        return validationError;
    }

}
