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

import java.util.EnumSet;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.Table;

import at.treedb.db.Base;
import at.treedb.db.ClassID;
import at.treedb.db.DAOiface;
import at.treedb.db.DBkey;
import at.treedb.db.Detach;
import at.treedb.db.HistorizationIface;
import at.treedb.db.SearchLimit;
import at.treedb.db.UpdateMap;
import at.treedb.domain.Domain;
import at.treedb.user.User;

/**
 * <p>
 * Class for UI images and icons.
 * </p>
 * 
 * @author Peter Sauer
 */
@SuppressWarnings("serial")
// LDR: 19.12.2013
@Entity
@Table(indexes = { @Index(columnList = "name"), @Index(columnList = "histId") })
public class Image extends Base implements Cloneable {

    public enum Fields {
        name, data, mimeType, license
    }

    @Column(nullable = false)
    private String name;
    // binary data
    @Detach
    @Lob
    @Column(nullable = false, length = 10485760) // 10MB
    private byte[] data;
    // MIME (Multipurpose Internet Mail Extensions) type
    @Column(nullable = false)
    private MimeType mimeType;
    // license
    private String license;

    protected Image() {
    }

    private Image(String name, byte[] data, MimeType mimeType, String license) {
        this.setHistStatus(STATUS.ACTIVE);
        this.name = name;
        this.data = data;
        this.mimeType = mimeType;
        this.license = license;
    }

    /**
     * Creates an image.
     * 
     * @param domain
     *            {@code Domain} of the data element
     * @param user
     *            creator of the {@code Image}
     * @param name
     *            name of the icon
     * @param data
     *            binary data
     * @param mimeType
     *            MIME type of the icon
     * @return {@code Image} object
     * @throws Exception
     */
    static public Image create(Domain domain, User user, String name, byte[] data, MimeType mimeType, String licence)
            throws Exception {
        Image image = new Image(name, data, mimeType, licence);
        Base.save(null, domain, user, image);
        return image;
    }

    @Override
    public Object checkConstraints(DAOiface dao, UpdateMap update) throws Exception {
        Base.checkConstraintPerDomain(dao, update, getDomain(), getHistId(), Image.Fields.name, getName());
        return null;

    }

    /**
     * Creates an {@code Image}.
     * 
     * @param domain
     *            {@code Domain} of the image
     * @param user
     *            creator of the {@code Image}
     * @param name
     *            name of the image
     * @param data
     *            binary image data
     * @param mimeType
     *            MIME type of the image
     * @return {@code Image} object
     * @throws Exception
     */
    static public Image create(DAOiface dao, Domain domain, User user, String name, byte[] data, MimeType mimeType,
            String license) throws Exception {
        Image image = new Image(name, data, mimeType, license);
        Base.save(dao, domain, user, image);
        return image;
    }

    /**
     * Loads an {@code Image}.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param id
     *            ID of the {@code Image}
     * @return {@code Image} object
     * @throws Exception
     */
    public static Image load(DAOiface dao, @DBkey(Image.class) int id) throws Exception {
        return (Image) Base.load(dao, Image.class, id, null);
    }

    /**
     * Deletes an {@code Image} from DB explicit without historization.
     * 
     * @param user
     *            user who deletes the {@code Image}
     * @param image
     *            {@code Image} to be deleted
     * 
     * @throws Exception
     */
    public static void dbDelete(User user, Image image) throws Exception {
        Base.delete(user, image, true);
    }

    /**
     * Deletes an {@code Image} from DB explicit without historization.
     * 
     * @param user
     *            user who deletes the {@code Image}
     * @param image
     *            ID of the {@code Image}
     * @throws Exception
     */
    public static void dbDelete(User user, @DBkey(Image.class) int image) throws Exception {
        Base.delete(user, image, Image.class, true);
    }

    /**
     * Deletes an {@code Image}.
     * 
     * @param user
     *            {@code User} who deletes the {@code Image}
     * @param image
     *            {@code Image} to be deleted
     * @throws Exception
     */
    public static void delete(User user, Image image) throws Exception {
        Base.delete(user, image, false);
    }

    /**
     * Deletes an {@code Image}.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param user
     *            user who deletes the {@code Image}
     * @param id
     *            ID of the {@code Image}
     * @throws Exception
     */
    public static void delete(DAOiface dao, User user, @DBkey(Image.class) int id) throws Exception {
        Base.delete(dao, user, id, Image.class, false);
    }

    /**
     * Deletes an {@code Image}.
     * 
     * @param user
     *            user who deletes the {@code Image}
     * @param id
     *            ID of the {@code Image}
     * @throws Exception
     */
    public static void delete(User user, int id) throws Exception {
        Base.delete(user, id, Image.class, false);
    }

    /**
     * Updates an {@code Image}.
     * 
     * @param user
     *            {@code User} who performs the update
     * 
     * @param map
     *            map of changes
     * @throws Exception
     */
    public void update(User user, UpdateMap map) throws Exception {
        Base.update(user, this, map);
    }

    /**
     * Updates an {@code Image}.
     * 
     * @param user
     *            {@code User} who performs the update
     * @param image
     *            {@code Image} to be updated
     * @param map
     *            map of changes
     * @throws Exception
     */
    public static void update(User user, Image image, UpdateMap map) throws Exception {
        Base.update(user, image, map);
    }

    /**
     * Updates an {@code Image}.
     * 
     * @param user
     *            user {@code User} who performs the update
     * @param id
     *            {@code Image} ID
     * @param map
     *            map of changes
     * @throws Exception
     */
    public static void update(User user, @DBkey(Image.class) int id, UpdateMap map) throws Exception {
        Base.update(user, id, Image.class, map);
    }

    /**
     * Updates an {@code Image}.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param user
     *            {@code User} who performs the update
     * @param id
     *            image ID
     * @param map
     *            map of changes
     * @throws Exception
     */
    public static void update(DAOiface dao, User user, @DBkey(Image.class) int id, UpdateMap map) throws Exception {
        Base.update(dao, user, id, Image.class, map);
    }

    /**
     * Returns the name of the image.
     * 
     * @return image name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the binary image data.
     * 
     * @return binary data of the image
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Returns the MIME type of the image.
     * 
     * @return MIME type of the image
     */
    public MimeType getMIMEtype() {
        return mimeType;
    }

    /**
     * Return the license of the image.
     * 
     * @return license of the image
     */
    public String getLicense() {
        return license;
    }

    /**
     * Counts the {@code Image} entities/DB table rows.
     * 
     * @param status
     *            filter representing the user's historization status.
     *            <code>null<null> counts all user DB entries.
     * @return number if images
     * @throws Exception
     */
    public static long rowCount(HistorizationIface.STATUS status) throws Exception {
        return Base.countRow(null, Image.class, status, null);
    }

    /**
     * Returns a unique text ID to ensure that a web bowser notice images
     * changes.
     * 
     * @return synthetic text ID
     */
    public String getCacheId() {
        return "" + domain + getHistId() + data.hashCode();
    }

    /**
     * Searches an image.
     * 
     * @param fields
     *            image property to be searched
     * @param value
     *            property/search value
     * @param flags
     *            search flags
     * @return list of {@code Image} objects
     * @throws Exception
     */
    static public List<Base> search(Domain domain, EnumSet<Image.Fields> fields, String value,
            EnumSet<Base.Search> flags, SearchLimit limit) throws Exception {
        return Base.search(domain, Image.class, fields, value, null, flags, limit, false);
    }

    /**
     * Loads an image by its name
     * 
     * @param domain
     *            {@code Domain} of the image
     * @param name
     *            name of the image
     * @return {@code Image} object
     * @throws Exception
     */
    static public Image load(Domain domain, String name) throws Exception {
        return load(null, domain, name);
    }

    /**
     * Loads an image by its name
     * 
     * @param domain
     *            {@code Domain} of the image
     * @param name
     *            name of the image
     * @return {@code Image} object
     * @throws Exception
     */
    static public Image load(DAOiface dao, Domain domain, String name) throws Exception {
        List<Base> list = Base.search(dao, domain, Image.class, EnumSet.of(Image.Fields.name), name, null, null, null,
                false);
        if (list == null || list.size() != 1) {
            return null;
        }
        return (Image) list.get(0);
    }

    @Override
    public ClassID getCID() {
        return ClassID.IMAGE;
    }

}
