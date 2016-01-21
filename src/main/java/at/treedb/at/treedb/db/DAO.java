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

import at.treedb.db.DAOiface;
import at.treedb.db.hibernate.DAOhibernate;
import at.treedb.db.jpa.DAOjpa;

/**
 * This Data Access Object (DAO) is the base for all DB related operations. In
 * this implementation this can be a JPA or a Hibernate instance.
 * 
 * @author Peter Sauer
 * 
 */
// LastReview: 17.02.2013
public class DAO {

    // supported JPA implementations
    public enum JPA_IMPL {
        HIBERNATEJPA, // http://www.eclipse.org/eclipselink
        ECLIPSELINK, // http://www.eclipse.org/eclipselink
        OBJECTDB, // http://objectdb.com
        OPENJPA, // openjpa.apache.org

    }

    // supported databases
    public enum DB {
        H2, // http://www.h2database.com
        DERBY, // http://db.apache.org/derby
        HSQLDB, // http://hsqldb.org
        MYSQL, // http://www.mysql.com
        MARIADB, // https://mariadb.org/
        POSTGRES, // http://www.postgresql.org
        SQLSERVER, // http://www.microsoft.com/de-de/download/details.aspx?id=29062
        ORACLE, // http://www.oracle.com/technetwork/products/express-edition/overview/index.html
        DB2, // http://db2express.com/de/
        FIREBIRD, // http://firebirdsql.org/
        SQLITE // https://bitbucket.org/xerial/sqlite-jdbc/downloads
    }

    public enum PERSISTENCE_CFG_CREATE_STRATEGY {
        NO_FILE_CREATION, DEFAULT_LOCATION, TEMPORARY
    }

    public enum DDL_STRATEGY {
        VALIDATE, UPDATE, CREATE
    };

    private static DAOiface daoIface;

    /**
     * Sets the DAO.
     * 
     * @param dao
     *            DAO instance
     */
    public static void setDAOiface(DAOiface dao) {
        daoIface = dao;
    }

    /**
     * Returns the DAO.
     * 
     * @return DAO object using JPA or Hibernate
     * @throws Exception
     */
    public static DAOiface getDAO() throws Exception {
        if (daoIface == null) {
            throw new Exception("DAOiface.getDAO(): DAOiface not set!");
        }
        return daoIface.getDAOiface();
    }

    /**
     * Creates a DAO object.
     * 
     * @param layer
     *            JPA or Hibernate
     * @param jpaImpl
     *            JPA implementation
     * @param database
     *            database
     * @param dbURL
     *            database URL
     * @param dbUser
     *            database user
     * @param dbPWD
     *            database password
     * @param creationStrategy
     * @param updateMap
     * @return DAO interface
     * @throws Exception
     */
    public synchronized static DAOiface createDAOiface(DAOiface.PERSISTENCE_LAYER layer, JPA_IMPL jpaImpl, DB database,
            DDL_STRATEGY dll, String dbURL, String dbUser, String dbPWD,
            PERSISTENCE_CFG_CREATE_STRATEGY creationStrategy, UpdateMap updateMap) throws Exception {
        if (creationStrategy == null) {
            creationStrategy = PERSISTENCE_CFG_CREATE_STRATEGY.DEFAULT_LOCATION;
        }
        if (database != null && database == DB.SQLITE) {
            if (layer == DAOiface.PERSISTENCE_LAYER.JPA && (jpaImpl == null || jpaImpl != JPA_IMPL.ECLIPSELINK)) {
                throw new Exception("For SQLITE only HIBERNATE or the combination JPA/ECLIPSELINK is valid due.");
            }
        }

        if (daoIface == null) {
            if (layer == DAOiface.PERSISTENCE_LAYER.HIBERNATE) {
                daoIface = DAOhibernate.getInstance(database, dll, dbURL, dbUser, dbPWD, creationStrategy);
            } else {
                daoIface = DAOjpa.getInstance(jpaImpl, database, dll, dbURL, dbUser, dbPWD, creationStrategy);
            }
        }
        // store internal DB informations in database
        daoIface.beginTransaction();
        DBinfo info = DBinfo.load(daoIface);
        if (info == null) {
            String user = null;
            String comment = null;
            if (updateMap != null) {
                Update u = updateMap.get(DBinfo.Fields.user);
                if (u != null) {
                    user = u.getString();
                }
                u = updateMap.get(DBinfo.Fields.comment);
                if (u != null) {
                    comment = u.getString();
                }
            }
            DBinfo.create(daoIface, user, comment);
        }

        daoIface.endTransaction();
        return daoIface;
    }

    /**
     * Resets the DAO.
     */
    public synchronized static void resetDAOiface() {
        if (daoIface != null) {
            daoIface.resetInstance();
            /*
             * if (daoIface.getPersistenceLayer() ==
             * DAOiface.PERSISTENCE_LAYER.HIBERNATE) { daoIface.resetInstance();
             * } else { daoIface.resetInstance(); }
             */
            daoIface = null;

        }
    }
}
