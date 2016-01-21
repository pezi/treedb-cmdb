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
package at.treedb.backup;

import java.net.InetAddress;
import java.util.ArrayList;

import org.apache.commons.compress.archivers.sevenz.SevenZMethod;

import at.treedb.db.DAO;
import at.treedb.db.DAOiface;
import at.treedb.db.DBinfo;
import at.treedb.db.DAO.DB;
import at.treedb.db.DAO.JPA_IMPL;
import at.treedb.db.DAOiface.PERSISTENCE_LAYER;
import at.treedb.domain.Domain;
import at.treedb.i18n.Istring;
import at.treedb.i18n.IstringDummy;

/**
 * <p>
 * Class for storing informations for DB export.
 * </p>
 * 
 * @author Peter Sauer
 *
 */
// LDOR: 30.05.2014
public class DBexportInfo {
    public enum BACKUP_TYPE {
        FULL, DOMAIN
    };

    private String dbSchemaVersion;
    private BACKUP_TYPE backupType;
    private Serialization serialization;
    private PERSISTENCE_LAYER persistenceLayer;
    private JPA_IMPL jpaImplemantation;
    private DB database;
    private String hostName;
    private String operatingSystem;
    private SevenZMethod compressionMethod;
    private long startTime;
    private long endTime;
    private ArrayList<String> entityCount;
    private ArrayList<IstringDummy> domainDescription;

    /**
     * Creates a {@code  DBexportInfo} object.
     * 
     * @param backupType
     *            type of backup, e.g FULL
     * @param serialization
     *            type of object serialization
     * @param compressionMethod
     *            compression method
     * @throws Exception
     */
    public DBexportInfo(BACKUP_TYPE backupType, Domain d, Serialization serialization, SevenZMethod compressionMethod)
            throws Exception {
        this.backupType = backupType;
        this.compressionMethod = compressionMethod;
        this.serialization = serialization;
        dbSchemaVersion = DBinfo.DB_SCHEMA_VERSION;
        // export the domain descriptions (different languages)
        if (d != null && d.getDescription() != 0) {
            domainDescription = new ArrayList<IstringDummy>();
            for (Istring i : Istring.loadAllStrings(d.getDescription())) {
                domainDescription.add(new IstringDummy(i));
            }
        }
        DAOiface dao = DAO.getDAO();
        persistenceLayer = dao.getPersistenceLayer();
        if (dao.getJPAimpl() != null) {
            jpaImplemantation = dao.getJPAimpl();
        }
        if (DAO.getDAO().getDB() != null) {
            database = DAO.getDAO().getDB();
        }
        hostName = InetAddress.getLocalHost().getHostName();
        operatingSystem = System.getProperty("os.name");
        entityCount = new ArrayList<String>();
    }

    /**
     * Adds the count of an entity.
     * 
     * @param entity
     */
    public void addEntityCount(String entity) {
        entityCount.add(entity);
    }

    /**
     * Returns the {@code Serialization} method used for the backup.
     * 
     * @return {@code Serialization} method
     */
    public Serialization getSerialization() {
        return serialization;
    }

    /**
     * Returns the {@code PERSISTENCE_LAYER} of the original DB.
     * 
     * @return {@code PERSISTENCE_LAYER}
     */
    public PERSISTENCE_LAYER getPersistenceLayer() {
        return persistenceLayer;
    }

    /**
     * Returns the JPA implementation of the original DB.
     * 
     * @return {@code JPA_IMPL}
     */
    public JPA_IMPL jpaImplemantation() {
        return jpaImplemantation;
    }

    /**
     * Returns the original DB.
     * 
     * @return {@code DB}
     */
    public DB getDatabase() {
        return database;
    }

    /**
     * Returns the host name of the system which performed the DB backup.
     * 
     * @return host name
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * Returns the OS of the host which performed the DB backup.
     * 
     * @return host operating system
     */
    public String getOperatingSystem() {
        return operatingSystem;
    }

    /**
     * Returns the start time of the backup.
     * 
     * @return start time of the backup
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Sets the start time of the backup.
     */
    public void setStartTime() {
        this.startTime = System.currentTimeMillis();
    }

    /**
     * Returns the end time of the backup.
     * 
     * @return end time of the backup
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * Sets the end time of the backup.
     */
    public void setEndTime() {
        this.endTime = System.currentTimeMillis();
    }

    /**
     * Returns the compression method of the backup.
     * 
     * @return compression method
     */
    public SevenZMethod getCompressionMethod() {
        return compressionMethod;
    }

    /**
     * Returns the type of the backup.
     * 
     * @return {@code BACKUP_TYPE}
     */
    public BACKUP_TYPE getBackupType() {
        return backupType;
    }

    /**
     * Returns the DB schema version of the DB.
     * 
     * @return DB schema version
     */
    public String getDbSchemaVersion() {
        return dbSchemaVersion;
    }
}
