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
package at.treedb.db;

/**
 * <p>
 * Helper class for a DAO transaction.
 * </p>
 * 
 * @author Peter Sauer
 *
 */
public abstract class DAOtransaction {
    public DAOtransaction() throws Exception {
        doTransaction();
    }

    public abstract void doWork(DAOiface dao) throws Exception;

    private void doTransaction() throws Exception {
        DAOiface dao = DAO.getDAO();
        try {
            dao.beginTransaction();
            doWork(dao);

        } catch (Exception e) {
            dao.rollback();
            throw e;
        } finally {
            dao.endTransaction();
        }
    }
}
