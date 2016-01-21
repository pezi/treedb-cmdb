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
 * </p>
 * 
 * @author Peter Sauer
 *
 */
@Entity
public class UItextField extends UIelement {
    private static final long serialVersionUID = 1L;
    private ClassID dataType;
    private double lowerLimit;
    private double upperLimit;
    private int icolumns;
    private boolean isPassword;
    // private boolean wordWrap;
    @DBkey(Istring.class)
    private int validationRegExpr;
    @DBkey(Istring.class)
    private int validationError;
    private VALIDATOR validator;

    public enum Fields {
        validationError
    }

    public enum VALIDATOR {
        LENGTH, INTEGER, DOUBLE, EMAIL, REGEX, BEANSHELL, IPADRESS
    };

    protected UItextField() {

    }

    protected UItextField(UItab uiTab, ClassID dataType, String fieldName, Istring displayName, Istring description,
            int columns, double lowerLimit, double upperLimit, boolean mandatory, Istring mandatoryError,
            VALIDATOR validator, Istring validationRegExpr, Istring validationError) {
        super(uiTab, fieldName, displayName != null ? displayName.getHistId() : 0,
                description != null ? description.getHistId() : 0, mandatory,
                mandatoryError != null ? mandatoryError.getHistId() : 0);
        if (dataType == ClassID.CIPASSWORD) {
            this.dataType = ClassID.CISTRING;
            this.isPassword = true;
        } else {
            this.dataType = dataType;
        }
        this.upperLimit = upperLimit;
        this.lowerLimit = lowerLimit;
        this.icolumns = columns;

        this.validator = validator;
        if (validationRegExpr != null) {
            this.validationRegExpr = validationRegExpr.getHistId();
        }
        if (validationError != null) {
            this.validationError = validationError.getHistId();
        }
    }

    /**
     * 
     * @param dao
     * @param domain
     * @param user
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
     * @return
     * @throws Exception
     */
    public static UItextField createUItextField(DAOiface dao, Domain domain, User user, UItab uiTab, ClassID dataType,
            String fieldName, String displayName, String description, int columns, double lowerLimit, double upperLimit,
            boolean mandatory, String mandatoryError, VALIDATOR validator, String validationRegExpr,
            String validationError) throws Exception {
        switch (dataType) {
        case CILONG:
        case CIDOUBLE:
        case CISTRING:
        case CIPASSWORD:
            break;
        default:
            throw new Exception("UItextField.createUItextField(): not supported ClassID: " + dataType.name());
        }

        UItextField input = null;
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
            Istring iMandatoryError = null;
            if (mandatoryError != null) {
                iMandatoryError = Istring.create(dao, domain, user, ClassID.UITEXTFIELD, mandatoryError,
                        domain.getLanguage());
            }
            Istring ivalidationRegExpr = null;
            if (validationRegExpr != null) {
                ivalidationRegExpr = Istring.create(dao, domain, user, ClassID.UITEXTFIELD, validationRegExpr,
                        domain.getLanguage());
            }
            Istring ivalidationError = null;
            if (validationError != null) {
                ivalidationError = Istring.create(dao, domain, user, ClassID.UITEXTFIELD, validationError,
                        domain.getLanguage());
            }

            input = new UItextField(uiTab, dataType, fieldName, iDisplay, iDescription, columns, lowerLimit, upperLimit,
                    mandatory, iMandatoryError, validator, ivalidationRegExpr, ivalidationError);
            Base.save(dao, domain, user, input);
            if (daoLocal) {
                dao.endTransaction();
            }
        } catch (Exception e) {
            if (daoLocal) {
                dao.rollback();
            }
            throw e;
        }
        return input;
    }

    public int getDescription() {
        return description;
    }

    public double getLowerLimit() {
        return lowerLimit;
    }

    public double getUpperLimit() {
        return upperLimit;
    }

    public int getColumns() {
        return icolumns;
    }

    public VALIDATOR getValidator() {
        return validator;
    }

    public int getValidationRegExpr() {
        return validationRegExpr;
    }

    public int getValidationError() {
        return validationError;
    }

    public boolean isPassword() {
        return isPassword;
    }

    @Override
    public ClassID getCID() {
        return ClassID.UITEXTFIELD;
    }

    @Override
    public ClassID getDataType() {
        return dataType;
    }

}
