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
import java.util.HashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import at.treedb.ci.Image;
import at.treedb.ci.ImageDummy;
import at.treedb.db.Base;
import at.treedb.db.ClassID;
import at.treedb.db.DAO;
import at.treedb.db.DAOiface;
import at.treedb.domain.Domain;
import at.treedb.i18n.Istring;
import at.treedb.ui.UIoption.UIoptionDummy;
import at.treedb.user.User;

@SuppressWarnings("serial")
@Entity
public class UIselect extends UIelement {

    public enum SelectType {
        NATIVE, COMBOMOX, LIST, OPTIONS
    };

    private boolean nullSelectionAllowed;
    private SelectType selectType;
    private boolean multiSelect;
    @Column(name = "m_rows")
    private int rows;

    @Transient
    private UIoption[] options;

    @Transient
    private HashMap<String, Integer> valueMap;

    protected UIselect() {

    }

    public void initOptions(DAOiface dao) throws Exception {
        options = UIoption.loadList(dao, this.getHistId(), null);
    }

    protected UIselect(UItab uiTab, String fieldName, Istring displayName, Istring description, boolean mandatory,
            Istring mandatoryError, SelectType selectType, boolean multiSelect, int rows,
            boolean nullSelectionAllowed) {
        super(uiTab, fieldName, displayName != null ? displayName.getHistId() : 0,
                description != null ? description.getHistId() : 0, mandatory,
                mandatoryError != null ? mandatoryError.getHistId() : 0);
        this.selectType = selectType;
        this.multiSelect = multiSelect;
        this.rows = rows;
        this.nullSelectionAllowed = nullSelectionAllowed;
    }

    public int getOption(String value) {
        if (valueMap == null) {
            valueMap = new HashMap<String, Integer>();
            if (options != null) {
                for (UIoption o : options) {
                    valueMap.put(o.getValue(), o.getOption());
                }
            }
        }
        Integer i = valueMap.get(value);
        if (i == null) {
            return 0;
        }
        return i;
    }

    public static UIselect createUIselect(DAOiface dao, Domain domain, User user, UItab uiTab, String fieldName,
            String displayName, String description, boolean mandatory, String mandatoryError, SelectType selectType,
            boolean multiSelect, int rows, boolean nullSelectionAllowed, UIoptionDummy[] selection,
            ArrayList<UIoption> optionList) throws Exception {

        UIselect sel = null;
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
                iDescription = Istring.create(dao, domain, user, ClassID.UISELECT, description, domain.getLanguage());
            }
            Istring iDisplay = null;
            if (displayName != null) {
                iDisplay = Istring.create(dao, domain, user, ClassID.UISELECT, displayName, domain.getLanguage());
            }
            Istring iMandatoryError = null;
            if (mandatoryError != null) {
                iMandatoryError = Istring.create(dao, domain, user, ClassID.UIDATEFIELD, mandatoryError,
                        domain.getLanguage());
            }

            sel = new UIselect(uiTab, fieldName, iDisplay, iDescription, mandatory, iMandatoryError, selectType,
                    multiSelect, rows, nullSelectionAllowed);

            Base.save(dao, domain, user, sel);

            int index = 0;
            for (UIoptionDummy s : selection) {
                Istring i = Istring.create(dao, domain, user, ClassID.UISELECT, s.getOption(), domain.getLanguage());
                int imageId = 0;
                if (s.getImageDummy() != null) {
                    ImageDummy dicon = s.getImageDummy();
                    imageId = Image.create(dao, domain, user, dicon.getName(), dicon.getData(), dicon.getMimeType(),
                            dicon.getLicense()).getHistId();
                }
                UIoption option = UIoption.create(dao, domain, user, sel.getHistId(), s.getValue(), i.getHistId(),
                        index, imageId);
                if (optionList != null) {
                    optionList.add(option);
                }
                index += 10000;
            }

            if (daoLocal) {
                dao.endTransaction();
            }
        } catch (Exception e) {
            if (daoLocal) {
                dao.rollback();
            }
            throw e;
        }
        return sel;
    }

    @Override
    public ClassID getDataType() {
        return ClassID.CISTRING;
    }

    @Override
    public ClassID getCID() {
        return ClassID.UISELECT;
    }

    public boolean isNullSelectionAllowed() {
        return nullSelectionAllowed;
    }

    public UIoption[] getOptions() {
        return options;
    }

    public SelectType getSelectType() {
        return selectType;
    }

    public int getRows() {
        return rows;
    }

    public boolean isMultiSelect() {
        return multiSelect;
    }

}
