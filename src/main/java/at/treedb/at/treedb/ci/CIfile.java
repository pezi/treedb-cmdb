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
package at.treedb.ci;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.Table;

import at.treedb.db.Base;
import at.treedb.db.ClassID;
import at.treedb.db.ClassSelector;
import at.treedb.db.DAO;
import at.treedb.db.DAOiface;
import at.treedb.db.DBkey;
import at.treedb.db.Detach;
import at.treedb.db.SearchCriteria;
import at.treedb.db.UpdateMap;
import at.treedb.dbfs.DBFSblock;
import at.treedb.dbfs.DBfile;
import at.treedb.dbfs.DBoutputStream;
import at.treedb.domain.Domain;
import at.treedb.i18n.Istring;
import at.treedb.user.User;
import at.treedb.util.ArchiveFileAccess;

/**
 * <p>
 * {@code CI} data container for a large (binary) file.
 * </p>
 * 
 * @author Peter Sauer
 * 
 */
@SuppressWarnings("serial")
@Entity
@Table(indexes = { @Index(columnList = "name"), @Index(columnList = "ci"), @Index(columnList = "uiElement"),
        @Index(columnList = "histId") })
// LDR: 01.12.2013
public class CIfile extends CIdata {
    // create an index
    @Column(nullable = false)
    // @org.eclipse.persistence.annotations.Index
    // @javax.jdo.annotations.Index
    private String name; // file name
    // description
    @Column(length = 8192)
    private int description;
    // license
    @Column(length = 8192)
    private String license;
    // mime type of the file
    private MimeType mimeType;
    @DBkey(value = DBfile.class)
    private int dbFile;
    @Detach
    @Lob
    @Column(nullable = true, length = 5242880) // 5MB
    // file preview
    private byte[] preview;
    // MIME type of the file preview
    private MimeType previewMimeType;

    /**
     * Field access name for updates by Java reflection.
     */
    public enum Fields {
        name, description, license, mimeType, previewMimeTyp, preview;
    }

    protected CIfile() {
    }

    private CIfile(int ci, int ciType, long uiElement, String name, String license, MimeType mimeType, int dbFile,
            MimeType previewMimeType, byte[] preview) {
        super(ci, ciType, uiElement);
        this.name = name;
        this.license = license;
        this.mimeType = mimeType;
        this.dbFile = dbFile;
        this.previewMimeType = previewMimeType;
        this.preview = preview;
    }

    /**
     * <p>
     * Creates a container for a (binary) file.
     * </p>
     * <p>
     * Following virtual path will be used:<br>
     * {@code /files/ciId/uiElementId/fileName}
     * </p>
     * 
     * @param dao
     *            {@code DAOiface} data access object
     * @param domain
     *            {@code Domain} of the data element
     * @param user
     *            creator of the {@code CIfile}
     * @param ci
     *            ID of the {@code CI}
     * @param ciType
     *            ID of the {@code CItype}
     * @param uiElement
     *            ID of the {@code UIelement}
     * @param name
     *            file name
     * @param description
     *            file description
     * @param license
     *            license information
     * @param file
     *            {@code File} object
     * @param mimeType
     *            MIME type of the file
     * @param preview
     *            {@code File} object of the preview
     * @param previewMimeType
     *            {@code MimeType} type of preview
     * @return {@code CIfile} object
     * @throws Exception
     */
    public static CIfile create(DAOiface dao, Domain domain, User user, @DBkey(CI.class) int ci,
            @DBkey(CItype.class) int ciType, @DBkey(ClassSelector.class) long uiElement, String name,
            String description, String license, File file, ArchiveFileAccess ablock, MimeType mimeType, byte[] preview,
            MimeType previewMimeType) throws Exception {
        CIfile ciFile = null;
        boolean localDAO = false;
        if (dao == null) {
            dao = DAO.getDAO();
            localDAO = true;
        }
        try {
            // virtual file path
            DBfile f = DBfile.create(dao, domain, user, "/files/" + ci + "/" + uiElement + "/" + name);
            DBoutputStream out = new DBoutputStream(f, dao);

            if (file != null) {
                byte[] buffer = new byte[DBFSblock.BLOCK_SIZE];
                FileInputStream reader = new FileInputStream(file);
                long fileLength = file.length();
                // main loop for reading binary data
                while (true) {
                    int read = reader.read(buffer, 0, DBFSblock.BLOCK_SIZE);
                    if (read == -1) {
                        break;
                    }
                    out.write(buffer, 0, read);
                    fileLength -= read;
                    if (fileLength == 0) {
                        break;
                    }
                }
                reader.close();
                out.close();
            } else if (ablock != null) {
                int fileLength = ablock.getSize();
                FileChannel fchannel = ablock.getFileChannel();
                long offset = ablock.getOffset();
                while (fileLength > 0) {
                    int read = Math.min(DBFSblock.BLOCK_SIZE, fileLength);
                    ByteBuffer buf = ByteBuffer.allocate(read);
                    int z = fchannel.read(buf, offset);
                    out.write(buf.array(), 0, read);
                    offset += read;
                    fileLength -= read;
                }
                out.close();
            }
            // store the preview data
            MimeType previewMIME = null;
            byte[] previewData = null;

            if (preview != null) {
                previewData = preview;
                previewMIME = previewMimeType;
            }
            ciFile = new CIfile(ci, ciType, uiElement, name, license, mimeType, f.getHistId(), previewMIME,
                    previewData);
            if (description != null) {
                ciFile.description = Istring
                        .create(dao, domain, user, ciFile.getCID(), description, domain.getLanguage()).getHistId();
            }
            Base.save(dao, domain, user, ciFile);
            if (localDAO) {
                dao.endTransaction();
            }
        } catch (Exception e) {
            if (localDAO) {
                dao.rollback();
            }
            throw e;
        }
        return ciFile;
    }

    /**
     * Loads a {@code CIfile}.
     * 
     * @param id
     *            ID of the {@code CIfile}
     * @return {@code CIfile} object
     * @throws Exception
     */
    public static CIfile load(@DBkey(CIfile.class) int id) throws Exception {
        return (CIfile) Base.load(null, CIfile.class, id, null);
    }

    /**
     * Loads a {@code CIfile}.
     * 
     * @param ci
     *            ID of the {@code CI}
     * @param ui
     *            ID of the {@code UIelement}
     * @param name
     *            file name
     * @return {@code CIfile} object
     * @throws Exception
     */
    public static CIfile load(@DBkey(CI.class) int ci, @DBkey(ClassSelector.class) long uiElement, String name)
            throws Exception {
        return (CIfile) load(null, CIfile.class, ci, uiElement, new SearchCriteria(Fields.name, name), null);
    }

    /**
     * Loads a {@code CIfile}.
     * 
     * @param dao
     *            {@code DAOiface} data access object
     * @param ci
     *            ID of the {@code CI}
     * @param ui
     *            ID of the {@code UIelement}
     * @param name
     *            file name
     * @return {@code CIfile} object
     * @throws Exception
     */
    public static CIfile load(DAOiface dao, @DBkey(CI.class) int ci, @DBkey(ClassSelector.class) long uiElement,
            String name) throws Exception {
        return (CIfile) load(dao, CIfile.class, ci, uiElement, new SearchCriteria(Fields.name, name), null);
    }

    /**
     * Loads a {@code CIfile} list.
     * 
     * @param ci
     *            {@code CI} ID
     * @param uiElement
     *            {@code UIlement} ID
     * @return {@code CIfile} list
     * @throws Exception
     */
    public static List<Base> loadAll(@DBkey(CI.class) int ci, @DBkey(ClassSelector.class) long uiElement)
            throws Exception {
        return loadList(null, CIfile.class, ci, uiElement, null, null, false);
    }

    /**
     * Creates or updates a {@code CIfile}
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param domain
     *            {@code Domain} of the data element
     * @param user
     *            {@code User} who performs the update/creation
     * @param ci
     *            ID of the {@code CI}
     * @param ciType
     *            ID of the {@code ciType}
     * @param uiElement
     *            ID of the {@code UIelement}
     * @param name
     *            file name
     * @param description
     *            file description
     * @param license
     *            license
     * @param file
     *            {@code File} object
     * @param mimeType
     *            MIME type of the file data
     * @param preview
     *            {@code File} object
     * @param mimeTypePreview
     *            MIME type of the preview data
     * @return {@code CIfile}
     * @throws Exception
     */
    public static CIfile createOrUpdate(DAOiface dao, Domain domain, User user, @DBkey(value = CI.class) int ci,
            @DBkey(value = CItype.class) int ciType, @DBkey(value = ClassSelector.class) long uiElement, String name,
            String description, String license, File file, ArchiveFileAccess ablock, MimeType mimeType, byte[] preview,
            MimeType mimeTypePreview) throws Exception {
        CIfile ciFile = (CIfile) load(dao, CIfile.class, ci, uiElement, new SearchCriteria(Fields.name, name), null);
        if (ciFile == null) {
            ciFile = create(dao, domain, user, ci, ciType, uiElement, name, description, license, file, ablock,
                    mimeType, preview, mimeTypePreview);

        } else {
            UpdateMap map = new UpdateMap(CIfile.Fields.class);
            map.addString(CIfile.Fields.name, name);
            map.addString(CIfile.Fields.license, license);
            map.addIstring(CIfile.Fields.description, description, domain.getLanguage(), null);
            map.addEnum(CIfile.Fields.mimeType, (Enum<?>) mimeType);
        }
        return ciFile;
    }

    /**
     * Deletes a binary file.
     * 
     * @param user
     * @param id
     * @throws Exception
     */
    // TODO: missing code
    public static void delete(User user, int id) throws Exception {

    }

    /**
     * Returns the name of the binary file.
     * 
     * @return file name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the ID of the {@code DBfile}.
     * 
     * @return {@code DBfile} ID
     */
    public @DBkey(DBfile.class) int getDBfile() {
        return dbFile;
    }

    /**
     * Returns the license of the file.
     * 
     * @return license info
     */
    public String getLicense() {
        return license;
    }

    /**
     * Returns the description of the file.
     * 
     * @return ID of the {@code Istring}
     */
    public @DBkey(value = Istring.class) int getDescription() {
        return description;
    }

    /**
     * Returns the MIME type of the file.
     * 
     * @return MIME of the file
     */
    public MimeType getMimeType() {
        return mimeType;
    }

    /**
     * Returns the preview binary data. e.g the first picture frame of a video.
     * 
     * @return preview binary data
     */
    public byte[] getPreview() {
        return preview;
    }

    /**
     * Returns the MIME type of the file preview.
     * 
     * @return MIME type of the preview
     */
    public MimeType getMimeTypePreview() {
        return previewMimeType;
    }

    @Override
    public Object checkConstraints(DAOiface dao, UpdateMap update) throws Exception {
        // ensure that a filename is unique for a CI
        Base.checkConstraintsPerCI(dao, update, getDomain(), getCi(), getHistId(), CIfile.Fields.name, getName());
        return null;
    }

    @Override
    public ClassID getCID() {
        return ClassID.CIFILE;
    }

    public void resetData() {
        preview = null;
    }

    public CIfile getData() {
        return this;
    }
}
