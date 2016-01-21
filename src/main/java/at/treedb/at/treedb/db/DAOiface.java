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

import java.util.HashMap;
import java.util.List;

import at.treedb.db.DAOiface;
import at.treedb.db.DAO.DB;
import at.treedb.db.DAO.JPA_IMPL;

/**
 * Generic Data Access Object(DAO) interface for accessing the DB.
 * 
 * @author Peter Sauer
 * 
 */
// LDOR: 20.12.2013
public interface DAOiface {
    /**
     * Supported persistence layer
     * 
     */
    public enum PERSISTENCE_LAYER {
        /**
         * Java Persistence API
         */
        JPA,
        /**
         * HIBERNATE
         */
        HIBERNATE
    }

    /**
     * Returns the DAO interface.
     * 
     * @return {@code DAOiface}
     */
    public DAOiface getDAOiface() throws CloneNotSupportedException;

    /**
     * Starts the DB based transaction.
     */
    public void beginTransaction();

    /**
     * Ends the DB based transaction.
     */
    public void endTransaction();

    /**
     * Updates an entity.
     * 
     * @param entity
     */
    public <T extends HistorizationIface> void update(T entity);

    /**
     * Persists an entity.
     * 
     * @param entity
     * @throws Exception
     */
    public <T> void save(T entity) throws Exception;

    /**
     * Saves an entity and invokes a DB flush, if the underlying persistence
     * layer is JPA. This special handling is necessary according the JPA
     * behavior that only after a flush the entity is fully persisted and all
     * fields, like the auto generated values, are available. e.g. the DB
     * generated primary key is used as historization ID.
     * 
     * @param entity
     * @throws Exception
     */
    public <T> void saveAndFlushIfJPA(T entity) throws Exception;

    /**
     * Performs a DB related flush to update the DB.
     * 
     * @throws Exception
     */
    public void flush() throws Exception;

    public <T> void detach(T entity) throws Exception;

    /**
     * Deletes an entity.
     * 
     * @param entity
     */
    public <T extends HistorizationIface> void delete(T entity);

    /**
     * Gets an entity with a 64-bit primary key. A 64-bit key is used for
     * composed keys. e.g. {@link at.treedb.dbfs.DBFSblock} (virtual file
     * system)
     * 
     * @param clazz
     *            class of the entity
     * @param primKey
     *            DB ID of the entity - PK is a long (64 bit) value
     * @return persisted entity
     */
    public <T> T get(Class<T> clazz, long primKey);

    /**
     * Gets an entity.
     * 
     * @param clazz
     *            class of the entity
     * @param primKey
     *            DB ID of the entity - PK is an int (32 bit) value
     * @return persisted entity
     */
    public <T> T get(Class<T> clazz, int primKey);

    /**
     * Query with named parameter.
     * 
     * @param query
     *            query string
     * @param map
     *            parameter map with named parameter as key and the query object
     *            as value
     * @return query result
     */
    public List<?> query(String query, HashMap<String, Object> map);

    /**
     * Query with named parameter.
     * 
     * @param start
     *            start position of the first result, numbered from 0
     * @param maxResults
     *            maximum number of results
     * @param query
     *            query string
     * @param map
     *            parameter map with named parameter as key and the object as
     *            value
     * @return query result
     */
    public List<?> query(String query, int start, int maxResults, HashMap<String, Object> map) throws Exception;

    /**
     * Query with named parameter including execution - e.g. bulk delete/update
     * 
     * @param query
     *            query string
     * @param map
     *            parameter map with named parameter as key and the object as
     *            value
     * @return number of affected DB objects
     */
    public int queryAndExecute(String query, HashMap<String, Object> map);

    /**
     * Native SQL query.
     * 
     * @param query
     *            native SQL query statement
     * @return SQL result list
     * @throws Exception
     */
    public List<?> nativeQuery(String query) throws Exception;

    /**
     * Native SQL query and statement execution.
     * 
     * @param query
     *            native SQL query statement
     * @return SQL result list
     * @throws Exception
     */
    public int nativeQueryAndExecute(String query) throws Exception;

    /**
     * Invokes a DB related transaction rollback.
     */
    public void rollback();

    /**
     * Closes the instance of the interface including DB connection closing.
     */
    public void close();

    /**
     * Returns the actual persistence layer.
     * 
     * @return persistence layer
     */
    public PERSISTENCE_LAYER getPersistenceLayer();

    /**
     * Returns the JPA implementation.
     * 
     * @return JPA implementation
     */
    public JPA_IMPL getJPAimpl();

    /**
     * Returns the type of the data base.
     * 
     * @return {@code DB} data base type
     */
    public DB getDB();

    /**
     * Resets the DAO interface - resets the underlying persistence layer.
     */
    public void resetInstance();

    /**
     * Clears the actual JPA/Hibenate session.
     */
    public void clear();

    /**
     * Returns the database name
     * 
     * @return name of the data base
     */
    public String getDatabaseName();

    /**
     * Returns the version of the data base
     * 
     * @return data base version
     */
    public String getDatabaseVersion();

    /**
     * Checks if Hibernate is the persistence layer.
     * 
     * @return {@code true} for Hibernate, {@code false} if not
     */
    public boolean isHibernate();

    /**
     * Checks if JPA is the persistence layer.
     * 
     * @return {@code true} for a JPA, {@code false} if not
     */
    public boolean isJPA();
}
