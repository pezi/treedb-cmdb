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
public class UItextArea extends UIelement {

    public enum TextArea {
        NATIVE, ACEEEDITOR
    };

    private TextArea textArea;
    private String aceMode;
    private int maxLength;
    private int icolumns;
    private int irows;
    private boolean wordWrap;
    private int validationRegExpr;
    private int validationError;

    protected UItextArea() {

    }

    public TextArea getTextArea() {
        return textArea;
    }

    public String getAceMode() {
        return aceMode;
    }

    protected UItextArea(UItab uiTab, String fieldName, Istring displayName, Istring description, int maxLength,
            int columns, int rows, boolean wordWrap, boolean mandatory, Istring mandatoryError,
            Istring validationRegExpr, Istring validationError, TextArea textArea, String aceMode) {
        super(uiTab, fieldName, displayName != null ? displayName.getHistId() : 0,
                description != null ? description.getHistId() : 0, mandatory,
                mandatoryError != null ? mandatoryError.getHistId() : 0);
        if (textArea == null) {
            textArea = TextArea.NATIVE;
        } else {
            this.textArea = textArea;
        }
        this.aceMode = aceMode;
        this.maxLength = maxLength;
        this.icolumns = columns;
        this.irows = rows;
        this.wordWrap = wordWrap;
        if (validationRegExpr != null) {
            this.validationRegExpr = validationRegExpr.getHistId();
        }
        if (validationError != null) {
            this.validationError = validationError.getHistId();
        }
    }

    public static UItextArea createUItextArea(DAOiface dao, Domain domain, User user, UItab uiTab, String fieldName,
            String displayName, String description, int maxLength, int columns, int rows, boolean wordWrap,
            boolean mandatory, String mandatoryError, String validationRegExpr, String validationError,
            TextArea textArea, String aceMode) throws Exception {
        UItextArea input = null;
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
                iDescription = Istring.create(dao, domain, user, ClassID.UITEXTAREA, description, domain.getLanguage());
            }
            Istring iDisplay = null;
            if (displayName != null) {
                iDisplay = Istring.create(dao, domain, user, ClassID.UITEXTAREA, displayName, domain.getLanguage());
            }
            Istring iMandatoryError = null;
            if (mandatoryError != null) {
                iMandatoryError = Istring.create(dao, domain, user, ClassID.UITEXTAREA, mandatoryError,
                        domain.getLanguage());
            }
            Istring ivalidationRegExpr = null;
            if (validationRegExpr != null) {
                ivalidationRegExpr = Istring.create(dao, domain, user, ClassID.UITEXTAREA, validationRegExpr,
                        domain.getLanguage());
            }
            Istring ivalidationError = null;
            if (validationError != null) {
                ivalidationError = Istring.create(dao, domain, user, ClassID.UITEXTAREA, validationError,
                        domain.getLanguage());
            }

            input = new UItextArea(uiTab, fieldName, iDisplay, iDescription, maxLength, columns, rows, wordWrap,
                    mandatory, iMandatoryError, ivalidationRegExpr, ivalidationError, textArea, aceMode);
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

    public int getMaxLength() {
        return maxLength;
    }

    public int getColumns() {
        return icolumns;
    }

    public int getRows() {
        return irows;
    }

    public boolean isWordWrap() {
        return wordWrap;
    }

    public int getValidationRegExpr() {
        return validationRegExpr;
    }

    public int getValidationError() {
        return validationError;
    }

    @Override
    public ClassID getCID() {
        return ClassID.UITEXTAREA;
    }

    @Override
    public ClassID getDataType() {
        return ClassID.CISTRING;
    }

}
