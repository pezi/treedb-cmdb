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
import at.treedb.ui.UIgrouping.Grouping;
import at.treedb.user.User;

@Entity
public class UIgroupingEnd extends UIelement {

    protected UIgroupingEnd() {

    }

    private Grouping grouping;

    protected UIgroupingEnd(UItab uiTab, Grouping grouping) {
        super(uiTab, null, 0, 0, false, 0);
        this.grouping = grouping;
    }

    public static UIgroupingEnd createUIgroupingEnd(DAOiface dao, Domain domain, User user, UItab uiTab,
            Grouping grouping) throws Exception {
        UIgroupingEnd groupingEnd = null;
        boolean daoLocal = false;
        if (dao == null) {
            dao = DAO.getDAO();
            daoLocal = true;
        }
        try {
            if (daoLocal) {
                dao.beginTransaction();
            }
            groupingEnd = new UIgroupingEnd(uiTab, grouping);
            Base.save(dao, domain, user, groupingEnd);
            if (daoLocal) {
                dao.endTransaction();
            }
        } catch (Exception e) {
            if (daoLocal) {
                dao.rollback();
            }
            throw e;
        }
        return groupingEnd;
    }

    public Grouping getGrouping() {
        return grouping;
    }

    @Override
    public ClassID getDataType() {
        return null;
    }

    @Override
    public ClassID getCID() {
        return ClassID.UIGROUPINGEND;
    }

}
