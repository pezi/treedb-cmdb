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
package at.treedb.db.jpa;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.Persistence;
import javax.persistence.Query;

import at.treedb.db.Base;
import at.treedb.db.DAO;
import at.treedb.db.DAOiface;
import at.treedb.db.DBentities;
import at.treedb.db.HistorizationIface;
import at.treedb.db.DAO.DB;
import at.treedb.db.DAO.DDL_STRATEGY;
import at.treedb.db.DAO.JPA_IMPL;
import at.treedb.db.DAO.PERSISTENCE_CFG_CREATE_STRATEGY;
import at.treedb.db.hibernate.DAOhibernate;
import at.treedb.util.ReplaceText;
import at.treedb.util.Stream;

/**
 * JPA implementation of the DAOiface.
 * 
 * @author Peter Sauer
 * 
 */
public class DAOjpa implements DAOiface, Cloneable {

    // private static String enhancingClass;
    // private static boolean isSubclassing = false;
    private EntityManager entityManager;
    private EntityTransaction transaction;
    private static EntityManagerFactory entityManagerFactory;
    private static JPA_IMPL jpaImpl;
    private static DAO.DB database;
    private static String databaseName;
    private static String databaseVersion;

    private DAOjpa(JPA_IMPL jpaImpl, DAO.DB database, boolean syncWriteOperations) {
        DAOjpa.jpaImpl = jpaImpl;
        DAOjpa.database = database;
    }

    private static DAOjpa instance;

    /**
     * Resets the DAO interface
     */
    @Override
    public synchronized void resetInstance() {
        if (instance != null) {
            instance.close();
        }
        instance = null;
    }

    /**
     * Creates an instance of a JPA DAO.
     * 
     * @param jpaImpl
     *            JAP implementation
     * @param database
     *            database
     * @param dbURL
     *            database URL
     * @param dbUser
     *            database User
     * @param dbPWD
     *            database password
     * @param creationStrategy
     *            strategy creating the persistence cfg file
     * @return {@code DAOjpa} JAP representation of the {@code DAO}
     * @throws Exception
     */
    public static synchronized DAOjpa getInstance(JPA_IMPL jpaImpl, DAO.DB database, DDL_STRATEGY dll, String dbURL,
            String dbUser, String dbPWD, PERSISTENCE_CFG_CREATE_STRATEGY creationStrategy) throws Exception {
        if (instance == null) {

            String dbDriver = "";
            String platform = "";
            String provider = "";
            String unitName = "";
            String enhancingClass = "";
            String hibernateDialect = "";
            boolean isSubclassing = false;
            boolean syncWriteOperations = false;
            if (jpaImpl == null) {
                throw new Exception("JPA implementation must be set!");
            }
            switch (jpaImpl) {
            case HIBERNATEJPA:
                provider = "org.hibernate.jpa.HibernatePersistenceProvider";
                unitName = "hibernateJPA";
                break;
            case ECLIPSELINK:
                provider = "org.eclipse.persistence.jpa.PersistenceProvider";
                unitName = "eclipseLink";
                break;
            case OBJECTDB:
                provider = "com.objectdb.jpa.Provider";
                unitName = "objectDB";
                break;
            case OPENJPA:
                provider = "org.apache.openjpa.persistence.PersistenceProviderImpl";
                unitName = "openJPA";
                break;
            }
            if (dbPWD == null) {
                dbPWD = "";
            }
            if (database != null) {
                switch (database) {
                case H2:
                    dbDriver = "org.h2.Driver";
                    platform = "H2Platform";
                    hibernateDialect = "H2Dialect";
                    break;
                case DERBY:
                    // different drivers for client/server
                    if (dbURL.contains("jdbc:derby://")) {
                        dbDriver = "org.apache.derby.jdbc.ClientDriver";
                    } else {
                        dbDriver = "org.apache.derby.jdbc.EmbeddedDriver";
                    }
                    platform = "DerbyPlatform";
                    hibernateDialect = "DerbyDialect";
                    break;
                case HSQLDB:
                    dbDriver = "org.hsqldb.jdbc.JDBCDriver";
                    platform = "HSQLPlatform";
                    hibernateDialect = "HSQLDialect";
                    break;
                case MYSQL:
                    dbDriver = "com.mysql.jdbc.Driver";
                    platform = "MySQLPlatform";
                    hibernateDialect = "MySQLDialect";
                    break;
                case MARIADB:
                    dbDriver = "org.mariadb.jdbc.Driver";
                    platform = "MySQLPlatform";
                    hibernateDialect = "MySQLDialect";
                    break;
                case POSTGRES:
                    dbDriver = "org.postgresql.Driver";
                    platform = "PostgreSQLPlatform";
                    hibernateDialect = "PostgreSQLDialect";
                    break;
                case SQLSERVER:
                    dbDriver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
                    platform = "SQLServerPlatform";
                    hibernateDialect = "SQLServerDialect";
                    break;
                case ORACLE:
                    dbDriver = "oracle.jdbc.OracleDriver";
                    platform = "OraclePlatform";
                    hibernateDialect = "Oracle10gDialect";
                    break;
                case DB2:
                    dbDriver = "com.ibm.db2.jcc.DB2Driver";
                    platform = "DB2Platform";
                    hibernateDialect = "DB2Dialect";
                    break;
                case FIREBIRD:
                    dbDriver = "org.firebirdsql.jdbc.FBDriver";
                    platform = "FirebirdPlatform";
                    hibernateDialect = "FirebirdDialect";
                    break;
                case SQLITE:
                    dbDriver = "org.sqlite.JDBC";
                    platform = "DatabasePlatform";
                    hibernateDialect = "SQLiteDialect";
                    syncWriteOperations = true;
                    break;
                default:
                    throw new Exception("Missing DB impl for" + database);
                }
            } else {
                // ObjectDB
                dbDriver = "";
                platform = "";
            }
            String hbm2ddl = "";
            String elddl = "";
            String openjpaddl = "buildSchema";
            switch (dll) {
            case VALIDATE:
                hbm2ddl = "validate";
                elddl = "none";
                break;
            case UPDATE:
                hbm2ddl = "update";
                elddl = "create-or-extend-tables";
                break;
            case CREATE:
                // ObjectDB
                if (jpaImpl == JPA_IMPL.OBJECTDB) {
                    // delete data files
                    File of = new File(dbURL);
                    if (of.exists()) {
                        of.delete();
                    }
                    of = new File(dbURL + "$");
                    if (of.exists()) {
                        of.delete();
                    }
                }
                hbm2ddl = "create";
                elddl = "drop-and-create-tables";
                openjpaddl = "buildSchema(SchemaAction=&apos;add,deleteTableContents&apos;,ForeignKeys=true)";
                break;

            }

            StringBuffer buf = new StringBuffer();
            for (String entity : DBentities.getClassesAsList()) {
                buf.append("<class>");
                buf.append(entity);
                buf.append("</class>\n");
            }
            ReplaceText[] rt = new ReplaceText[] { new ReplaceText("creationDate", (new Date()).toString()),
                    new ReplaceText("hbm2ddl", hbm2ddl), new ReplaceText("elddl", elddl),
                    new ReplaceText("openjpaddl", openjpaddl), new ReplaceText("dbDriver", dbDriver),
                    new ReplaceText("dbURL", dbURL), new ReplaceText("platform", platform),
                    new ReplaceText("dialect", hibernateDialect), new ReplaceText("dbUser", dbUser),
                    new ReplaceText("dbPwd", dbPWD), new ReplaceText("provider", provider),
                    new ReplaceText("unitName", unitName),

                    new ReplaceText("mappings", buf.toString()) };
            // read & set up the persistence.xml template
            URI persistenceCfg = DAOhibernate.class.getResource("/META-INF/persistenceTemplate.xml").toURI();

            String cfg = new String(
                    Stream.readInputStream(DAOjpa.class.getResourceAsStream("/META-INF/persistenceTemplate.xml")));

            String xml = ReplaceText.replaceText(cfg, rt);
            String path = null;

            switch (creationStrategy) {
            case NO_FILE_CREATION:
                break;
            case DEFAULT_LOCATION: {
                path = persistenceCfg.toString();
                int skip = 0;
                boolean jar = false;
                if (path.startsWith("file:")) {
                    skip = "file:".length();
                } else if (path.startsWith("jar:file:")) {
                    skip = "jar:file:".length();
                    jar = true;
                }

                // for debuging
                // System.out.println(xml);
                path = path.replace("%20", " ");
                path = path.substring(skip);

                path = path.substring(0, path.lastIndexOf("/")) + "/persistence.xml";

                // saves the persistence.xml file
                if (jar) {
                    path = path.substring(0, path.indexOf("WEB-INF"));
                    String tomcat = path + "WEB-INF/classes/META-INF/";
                    new File(tomcat).mkdirs();
                    Stream.writeString(new File(tomcat + "persistence.xml"), xml);

                } else {
                    Stream.writeString(path, xml);
                }
                break;
            }
            case TEMPORARY: {
                throw new Exception("Creating a temporary persistence.xml file isn't possible.");
                /*
                 * String tmpDir = System.getProperty("java.io.tmpdir"); if
                 * (!tmpDir.endsWith(File.separator)) { tmpDir +=
                 * File.separator; } tmpDir = tmpDir.toLowerCase(); tmpDir +=
                 * "META-INF" + File.separator; File fDir = new File(tmpDir); if
                 * (!fDir.exists() && !fDir.mkdirs()) { throw new Exception(
                 * "DAOjpa getInstance(): Unable to create the directory " +
                 * tmpDir); } tmpDir += "persistence.xml";
                 * Stream.writeString(tmpDir, xml);
                 */
            }
            }

            entityManagerFactory = Persistence.createEntityManagerFactory(unitName);
            if (path != null) {
                // delete this file, otherwise this file will be deployed inside
                // the CMDB.jar. Multiple persistence.xml files
                // causes a erratic behavior due the fact by chance one of the
                // persistence.xml is loaded by the class loader!
                System.out.println("Try to delete the persistence.xml file: " + new File(path).delete());
            }
            // Fix for search entities in a empty ObjectDB data base.
            // See: http://www.objectdb.com/database/forum/597
            if (jpaImpl == JPA_IMPL.OBJECTDB) {
                EntityManager em = entityManagerFactory.createEntityManager();
                em.getMetamodel().getManagedTypes();
                em.close();
            }

            instance = new DAOjpa(jpaImpl, database, syncWriteOperations);

            if (jpaImpl == JPA_IMPL.ECLIPSELINK || jpaImpl == JPA_IMPL.OBJECTDB) {
                try {
                    instance.beginTransaction();
                    java.sql.Connection connection = instance.entityManager.unwrap(java.sql.Connection.class);
                    databaseName = connection.getMetaData().getDatabaseProductName();
                    databaseVersion = connection.getMetaData().getDatabaseProductVersion();
                } catch (Exception e) {
                    // ignore exception
                } finally {
                    instance.endTransaction();
                }
            } else if (jpaImpl == JPA_IMPL.OBJECTDB) {

                try {
                    databaseName = "ObjectDB";
                    com.objectdb.jdo.PMF pmf = new com.objectdb.jdo.PMF();
                    databaseVersion = pmf.getProperties().getProperty("VersionNumber");
                    pmf.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return instance;
    }

    @Override
    public DAOiface getDAOiface() throws CloneNotSupportedException {
        return (DAOiface) instance.clone();
    }

    @Override
    public void beginTransaction() {
        entityManager = entityManagerFactory.createEntityManager();
        transaction = entityManager.getTransaction();
        transaction.begin();
    }

    @Override
    public void endTransaction() {
        if (transaction != null && transaction.isActive()) {
            transaction.commit();
        }
        if (entityManager != null) {
            entityManager.close();
        }
    }

    @Override
    public <T extends HistorizationIface> void update(T entity) {
        entityManager.merge(entity);

    }

    @Override
    public <T> void save(T entity) {
        entityManager.persist(entity);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends HistorizationIface> void delete(T entity) {
        T e = (T) entityManager.find(entity.getClass(), entity.getDBid());
        entityManager.remove(e);
    }

    @Override
    public <T> T get(Class<T> clazz, long primKey) {
        return entityManager.find(clazz, primKey);
    }

    @Override
    public <T> T get(Class<T> clazz, int primKey) {
        return entityManager.find(clazz, primKey);
    }

    @Override
    public <T> void detach(T entity) {
        entityManager.detach(entity);
        Class<?> clazz = ((Base) entity).getCID().toClass();
        entityManager.getEntityManagerFactory().getCache().evict(clazz, ((Base) entity).getDBid());
    }

    @Override
    public void flush() {
        entityManager.flush();
    }

    @Override
    public PERSISTENCE_LAYER getPersistenceLayer() {
        return DAOiface.PERSISTENCE_LAYER.JPA;
    }

    @Override
    public void rollback() {
        if (transaction != null && transaction.isActive()) {
            transaction.rollback();
        }
    }

    @Override
    public <T> void saveAndFlushIfJPA(T entity) {
        entityManager.persist(entity);
        entityManager.flush();
        entityManager.refresh(entity);
    }

    public Query createQuery(String query, HashMap<String, Object> map) {
        Query q = entityManager.createQuery(query);
        q.setFlushMode(FlushModeType.COMMIT);
        if (map != null) {
            Iterator<String> iter = map.keySet().iterator();
            while (iter.hasNext()) {
                String key = iter.next();
                q.setParameter(key, map.get(key));
            }
        }
        return q;
    }

    @Override
    public List<?> query(String query, HashMap<String, Object> map) {
        Query q = entityManager.createQuery(query);
        q.setFlushMode(FlushModeType.COMMIT);
        if (map != null) {
            Iterator<String> iter = map.keySet().iterator();
            while (iter.hasNext()) {
                String key = iter.next();
                q.setParameter(key, map.get(key));
            }
        }
        return q.getResultList();
    }

    @Override
    public int queryAndExecute(String query, HashMap<String, Object> map) {
        Query q = entityManager.createQuery(query);
        q.setFlushMode(FlushModeType.COMMIT);
        if (map != null) {
            Iterator<String> iter = map.keySet().iterator();
            while (iter.hasNext()) {
                String key = iter.next();
                q.setParameter(key, map.get(key));
            }
        }
        return q.executeUpdate();
    }

    /**
     * Returns the entity manager.
     * 
     * @return entity manager
     */
    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public List<?> query(String query, int start, int length, HashMap<String, Object> map) {
        Query q = entityManager.createQuery(query);
        q.setFlushMode(FlushModeType.COMMIT);
        if (map != null) {
            Iterator<String> iter = map.keySet().iterator();
            while (iter.hasNext()) {
                String key = iter.next();
                q.setParameter(key, map.get(key));
            }
        }
        q.setFirstResult(start);
        q.setMaxResults(length);
        return q.getResultList();
    }

    @Override
    public List<?> nativeQuery(String query) throws Exception {
        return entityManager.createNativeQuery(query).getResultList();
    }

    @Override
    public int nativeQueryAndExecute(String query) throws Exception {
        Query q = entityManager.createNativeQuery(query);
        return q.executeUpdate();
    }

    @Override
    public void close() {
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
        }
    }

    @Override
    public JPA_IMPL getJPAimpl() {
        return jpaImpl;
    }

    @Override
    public DB getDB() {
        return database;
    }

    @Override
    public void clear() {
        entityManager.clear();
    }

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    @Override
    public String getDatabaseVersion() {
        return databaseVersion;
    }

    @Override
    public boolean isHibernate() {
        return false;
    }

    @Override
    public boolean isJPA() {
        return true;
    }

}
