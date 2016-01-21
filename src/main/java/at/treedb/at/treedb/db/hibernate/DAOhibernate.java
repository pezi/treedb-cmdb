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
package at.treedb.db.hibernate;

import java.io.File;
import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.jdbc.Work;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.SessionFactory;

import at.treedb.db.DAO;
import at.treedb.db.DAOiface;
import at.treedb.db.DBentities;
import at.treedb.db.HistorizationIface;
import at.treedb.db.DAO.DB;
import at.treedb.db.DAO.DDL_STRATEGY;
import at.treedb.db.DAO.JPA_IMPL;
import at.treedb.db.DAO.PERSISTENCE_CFG_CREATE_STRATEGY;
import at.treedb.util.ReplaceText;
import at.treedb.util.Stream;

/**
 * <p>
 * Hibernate implementation of the DAOiface.
 * </p>
 * 
 * @author Peter Sauer
 * 
 */
// LDOR: 17.02.2013
public class DAOhibernate implements DAOiface, Cloneable {
    private boolean isSubclassing = false;

    private static Configuration configuration;
    @SuppressWarnings("rawtypes")
    private static ArrayList<Class> annotatedClasses;
    private Session session;
    private StatelessSession statelessSession;
    private boolean isStatelessSession;
    private Transaction tx;

    private static final String NOT_SUPPORTED = "not supported for a StatelessSession";
    private static ServiceRegistry serviceRegistry;
    private static DAOhibernate instance;
    private static SessionFactory sessionFactory;
    private static DAO.DB database;
    private static String databaseName;
    private static String databaseVersion;

    private DAOhibernate(DAO.DB database) {
        DAOhibernate.database = database;
    }

    /**
     * Returns an instance of a Hibernate DAO.
     * 
     * @return DAOiface
     * @throws Exception
     */
    public static synchronized DAOhibernate getInstance() throws Exception {
        return instance;
    }

    /**
     * Resets the DAO interface.
     */
    @Override
    public synchronized void resetInstance() {
        if (instance != null) {
            instance.close();
        }
        instance = null;
    }

    // http://stackoverflow.com/questions/33262/how-do-i-load-an-org-w3c-dom-document-from-xml-in-a-string
    public static org.w3c.dom.Document loadXMLFrom(String xml) throws org.xml.sax.SAXException, java.io.IOException {
        return loadXMLFrom(new java.io.ByteArrayInputStream(xml.getBytes()));
    }

    public static org.w3c.dom.Document loadXMLFrom(java.io.InputStream is)
            throws org.xml.sax.SAXException, java.io.IOException {
        javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        // avoid validating
        factory.setValidating(false);
        javax.xml.parsers.DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (javax.xml.parsers.ParserConfigurationException ex) {
        }
        org.w3c.dom.Document doc = builder.parse(is);
        is.close();
        return doc;
    }

    /**
     * Creates an instance of a Hibernate DAO.
     * 
     * @param database
     *            database
     * @param dbURL
     *            database URL
     * @param dbUser
     *            database User
     * @param dbPWD
     *            database password
     * @return {@code DAOhibernate} object
     * @throws Exception
     */
    public static synchronized DAOhibernate getInstance(DAO.DB database, DDL_STRATEGY dll, String dbURL, String dbUser,
            String dbPWD, PERSISTENCE_CFG_CREATE_STRATEGY creationStrategy) throws Exception {
        if (instance == null) {
            if (dbPWD == null) {
                dbPWD = "";
            }
            String hibernateDialect = null;
            String dbDriver = null;
            if (database == null) {
                throw new Exception("DAOhibernate.getInstance(): Database must be set!");
            }
            switch (database) {
            case H2:
                dbDriver = "org.h2.Driver";
                hibernateDialect = "H2Dialect";
                break;
            case DERBY:
                if (dbURL.contains("jdbc:derby://")) {
                    dbDriver = "org.apache.derby.jdbc.ClientDriver";
                } else {
                    dbDriver = "org.apache.derby.jdbc.EmbeddedDriver";
                }
                hibernateDialect = "DerbyTenSevenDialect";
                break;
            case HSQLDB:
                dbDriver = "org.hsqldb.jdbc.JDBCDriver";
                hibernateDialect = "HSQLDialect";
                break;
            case MYSQL:
                dbDriver = "com.mysql.jdbc.Driver";
                hibernateDialect = "MySQLDialect";
                break;
            case MARIADB:
                dbDriver = "org.mariadb.jdbc.Driver";
                hibernateDialect = "MySQLDialect";
                break;
            case POSTGRES:
                dbDriver = "org.postgresql.Driver";
                hibernateDialect = "PostgreSQLDialect";
                break;
            case SQLSERVER:
                dbDriver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
                hibernateDialect = "SQLServerDialect";
                break;
            case ORACLE:
                dbDriver = "oracle.jdbc.OracleDriver";
                hibernateDialect = "Oracle10gDialect";
                break;
            case FIREBIRD:
                dbDriver = "org.firebirdsql.jdbc.FBDriver";
                hibernateDialect = "FirebirdDialect";
                break;
            case DB2:
                dbDriver = "com.ibm.db2.jcc.DB2Driver";
                hibernateDialect = "DB2Dialect";
                break;
            case SQLITE:
                dbDriver = "org.sqlite.JDBC";
                hibernateDialect = "SQLiteDialect";
                break;
            default:
                throw new Exception("Missing DB implementation for" + database);
            }
            String hbm2ddl = "none";
            switch (dll) {
            case VALIDATE:
                hbm2ddl = "validate";
                break;
            case UPDATE:
                hbm2ddl = "update";
                break;
            case CREATE:
                hbm2ddl = "create";
                break;

            }
            ReplaceText[] rt = new ReplaceText[] { new ReplaceText("creationDate", (new Date()).toString()),
                    new ReplaceText("hbm2ddl", hbm2ddl), new ReplaceText("dbDriver", dbDriver),
                    new ReplaceText("dbURL", dbURL), new ReplaceText("dialect", hibernateDialect),
                    new ReplaceText("dbUser", dbUser), new ReplaceText("dbPwd", dbPWD), };

            URI hibernateCfg = DAOhibernate.class.getResource("/hibernateTemplate.cfg.xml").toURI();

            String cfgStr = new String(
                    Stream.readInputStream(DAOhibernate.class.getResourceAsStream("/hibernateTemplate.cfg.xml")));

            String xml = ReplaceText.replaceText(cfgStr, rt);
            String path = hibernateCfg.toString();
            int skip = 0;
            boolean jar = false;
            if (path.startsWith("file:")) {
                skip = "file:".length();
            } else if (path.startsWith("jar:file:")) {
                skip = "jar:file:".length();
                jar = true;
            }
            path = path.replace("%20", " ");
            path = path.substring(skip);
            path = path.substring(0, path.lastIndexOf("/")) + "/hibernateTemplate.cfg";

            if (jar) {
                path = path.substring(0, path.indexOf("WEB-INF"));

                String tomcat = path + "WEB-INF/classes/META-INF/";
                new File(tomcat).mkdirs();
                Stream.writeString(new File(tomcat + "hibernateTemplate.cfg"), xml);
                path += "hibernateTemplate.cfg";
            }

            Configuration cfg = new Configuration();
            if (creationStrategy == PERSISTENCE_CFG_CREATE_STRATEGY.NO_FILE_CREATION) {
                cfg.configure();
            } else if (creationStrategy == PERSISTENCE_CFG_CREATE_STRATEGY.DEFAULT_LOCATION) {
                File file = new File(path);
                Stream.writeString(file, xml);
                cfg.configure(file);
            } else {
                File tmpFile = File.createTempFile("hibernateTemplate", ".cfg");
                Stream.writeString(tmpFile, xml);
                cfg.configure(tmpFile);
            }

            @SuppressWarnings("rawtypes")
            ArrayList<Class> annotations = null;
            annotations = loadEnitiyClasses(DBentities.getClassesAsList(), cfg);
            serviceRegistry = new StandardServiceRegistryBuilder().applySettings(cfg.getProperties()).build();
            sessionFactory = cfg.buildSessionFactory(serviceRegistry);

            instance = new DAOhibernate(database);
            instance.setConfiguration(cfg);
            DAOhibernate.annotatedClasses = annotations;

            instance.beginTransaction();
            instance.getActualSession().doWork(new Work() {
                @Override
                public void execute(Connection connection) throws SQLException {
                    databaseName = connection.getMetaData().getDatabaseProductName();
                    databaseVersion = connection.getMetaData().getDatabaseProductVersion();
                }
            });
            instance.endTransaction();

        }
        return instance;
    }

    /**
     * Sets the Hibernate configuration.
     * 
     * @param configuration
     */
    public void setConfiguration(Configuration configuration) {
        DAOhibernate.configuration = configuration;
    }

    /**
     * Gets the Hibernate configuration.
     * 
     * @return Hibernate configuration
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    @SuppressWarnings({ "rawtypes", "unused" })
    private void setAnnotatedClasses(ArrayList<Class> annotatedClasses) {
        DAOhibernate.annotatedClasses = annotatedClasses;
    }

    /**
     * Returns the annotated classes.
     * 
     * @return list of annotated classes
     */
    @SuppressWarnings("rawtypes")
    public ArrayList<Class> getAnnotatedClasses() {
        return annotatedClasses;
    }

    @SuppressWarnings("rawtypes")
    private static ArrayList<Class> loadEnitiyClasses(String[] list, Configuration cfg) throws Exception {
        HashSet<String> set = new HashSet<String>();
        ArrayList<Class> annotations = new ArrayList<Class>();
        for (int i = 0; i < list.length; ++i) {
            int pos = list[i].lastIndexOf(".");
            if (pos != -1) {
                String p = list[i].substring(0, pos);
                if (!set.contains(p)) {
                    set.add(p);
                    cfg.addPackage(p);
                }
            }
            Class c = Class.forName(list[i]);
            cfg.addAnnotatedClass(c);
            annotations.add(c);
        }
        return annotations;
    }

    @Override
    public <T extends HistorizationIface> void update(T entity) {
        if (isStatelessSession) {
            statelessSession.update(entity);
        } else {
            session.update(entity);
        }
    }

    private static String buildErrMsg(String method) {
        return "DAOhibernate." + method + "()" + NOT_SUPPORTED;
    }

    @Override
    public <T> void save(T entity) throws Exception {
        if (isStatelessSession) {
            throw new Exception(buildErrMsg("save"));
        } else {
            session.save(entity);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Class<T> clazz, long primKey) {
        if (isStatelessSession) {
            return (T) statelessSession.get(clazz, primKey);
        } else {
            return (T) session.get(clazz, primKey);
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Class<T> clazz, int primKey) {
        if (isStatelessSession) {
            return (T) statelessSession.get(clazz, primKey);
        } else {
            return (T) session.get(clazz, primKey);
        }
    }

    @Override
    public List<?> query(String query, HashMap<String, Object> map) {
        Query q;
        if (isStatelessSession) {
            q = statelessSession.createQuery(query);
        } else {
            q = session.createQuery(query);
        }

        if (map != null) {
            Iterator<String> iter = map.keySet().iterator();
            while (iter.hasNext()) {
                String key = iter.next();
                Object o = map.get(key);
                q.setParameter(key, o);
            }
        }
        return q.list();
    }

    /**
     * Creates a Hibernate query object.
     * 
     * @param query
     *            query string
     * @param map
     *            query parameter
     * @return Hibernate query object
     */
    public Query createQuery(String query, HashMap<String, Object> map) {
        Query q;
        if (isStatelessSession) {
            q = statelessSession.createQuery(query);
        } else {
            q = session.createQuery(query);
        }
        if (map != null) {
            Iterator<String> iter = map.keySet().iterator();
            while (iter.hasNext()) {
                String key = iter.next();
                Object o = map.get(key);
                q.setParameter(key, o);
            }
        }
        return q;
    }

    @Override
    public int queryAndExecute(String query, HashMap<String, Object> map) {
        Query q;
        if (isStatelessSession) {
            q = statelessSession.createQuery(query);
        } else {
            q = session.createQuery(query);
        }
        if (map != null) {
            Iterator<String> iter = map.keySet().iterator();
            while (iter.hasNext()) {
                String key = iter.next();
                Object o = map.get(key);
                q.setParameter(key, o);
            }
        }
        return q.executeUpdate();
    }

    @Override
    public List<?> query(String query, int start, int length, HashMap<String, Object> map) {
        Query q;
        if (isStatelessSession) {
            q = statelessSession.createQuery(query);
        } else {
            q = session.createQuery(query);
        }
        if (map != null) {
            Iterator<String> iter = map.keySet().iterator();
            while (iter.hasNext()) {
                String key = iter.next();
                Object o = map.get(key);
                q.setParameter(key, o);
            }
        }
        q.setFirstResult(start);
        q.setMaxResults(length);
        return q.list();
    }

    @Override
    public List<?> nativeQuery(String query) throws Exception {
        if (isStatelessSession) {
            return statelessSession.createSQLQuery(query).list();
        } else {
            return session.createSQLQuery(query).list();
        }
    }

    @Override
    public int nativeQueryAndExecute(String query) throws Exception {
        SQLQuery sqlQuery = session.createSQLQuery(query);
        if (isStatelessSession) {
            sqlQuery = statelessSession.createSQLQuery(query);
        } else {
            sqlQuery = session.createSQLQuery(query);
        }
        return sqlQuery.executeUpdate();
    }

    @Override
    public void beginTransaction() {
        session = sessionFactory.getCurrentSession();
        tx = session.beginTransaction();
        isStatelessSession = false;
    }

    /**
     * Starts a stateless session.
     */
    public void beginStatelessTransaction() {
        statelessSession = sessionFactory.openStatelessSession();
        tx = statelessSession.beginTransaction();
        isStatelessSession = true;
    }

    /**
     * Returns a stateless session.
     * 
     * @return stateless session
     */
    public StatelessSession getStatelessSession() {
        return statelessSession;
    }

    @Override
    public void endTransaction() {
        if (tx != null && tx.getStatus() == TransactionStatus.ACTIVE) {
            tx.commit();
        }
    }

    @Override
    public <T extends HistorizationIface> void delete(T entity) {
        if (isStatelessSession) {
            statelessSession.delete(entity);
        } else {
            session.delete(entity);
        }
    }

    @Override
    public DAOiface getDAOiface() throws CloneNotSupportedException {
        return (DAOiface) instance.clone();
    }

    @Override
    public void flush() throws Exception {
        if (isStatelessSession) {
            throw new Exception(buildErrMsg("flush"));
        } else {
            session.flush();
        }
    }

    @Override
    public PERSISTENCE_LAYER getPersistenceLayer() {
        return DAOiface.PERSISTENCE_LAYER.HIBERNATE;
    }

    @Override
    public void rollback() {
        if (tx != null && tx.getStatus() == TransactionStatus.ACTIVE) {
            tx.rollback();
        }
    }

    @Override
    public <T> void saveAndFlushIfJPA(T entity) throws Exception {
        if (isStatelessSession) {
            throw new Exception(buildErrMsg("saveAndFlushIfJPA"));
        } else {
            session.save(entity);
        }
    }

    @Override
    public JPA_IMPL getJPAimpl() {
        return null;
    }

    @Override
    public DB getDB() {
        return database;
    }

    @Override
    public void close() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @Override
    public <T> void detach(T entity) throws Exception {
        if (isStatelessSession) {
            throw new Exception(buildErrMsg("evict"));
        } else {
            session.evict(entity);
        }
    }

    @Override
    public void clear() {
        session.clear();
    }

    /**
     * Returns the actual Hibernate session
     * 
     * @return Hibernate session
     */
    public Session getActualSession() {
        return session;
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
        return true;
    }

    @Override
    public boolean isJPA() {
        return false;
    }

}
