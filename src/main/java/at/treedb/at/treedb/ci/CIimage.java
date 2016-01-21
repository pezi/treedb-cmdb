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

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.drew.imaging.ImageMetadataReader;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;

import at.treedb.db.Base;
import at.treedb.db.CacheEntry;
import at.treedb.db.ClassID;
import at.treedb.db.ClassSelector;
import at.treedb.db.DAOiface;
import at.treedb.db.DBkey;
import at.treedb.db.LazyLoad;
import at.treedb.db.SearchCriteria;
import at.treedb.db.UpdateMap;
import at.treedb.domain.Domain;
import at.treedb.user.User;

/**
 * <p>
 * {@code CIimage} data container for an image.
 * </p>
 * 
 * @author Peter Sauer
 * 
 */
// LDOR: 24.06.2014
@Entity
@Table(indexes = { @Index(columnList = "name"), @Index(columnList = "ci"), @Index(columnList = "uiElement"),
        @Index(columnList = "histId") })
public class CIimage extends CIdata {

    private static final long serialVersionUID = 1L;
    private String name; // image name
    @Column(length = 8192)
    private String description; // imgae
    @Column(length = 8192)
    private String license;
    private MimeType mimeType;

    transient private boolean lazyLoad;
    private int hashCode; // hash code of the binary data
    @DBkey(value = CIblob.class)
    private int blobId;
    @LazyLoad
    transient private byte[] data; // binary image data
    private int width; // image width
    private int height; // image height
    @Column(name = "m_size") // Oracle
    private int size; // image size in bytes
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateTimeOriginal; // image creation date (EXIF)
    private boolean autoRotate; // support auto image rotation
    @Enumerated(EnumType.ORDINAL)
    private ImageOrientation orientation; // image orientation (EXIF)

    public enum GeoLocationSupport {
        NOT_AVAILABLE, AVAILABLE, IGNORE
    }

    // geo location support
    @Enumerated(EnumType.ORDINAL)
    private GeoLocationSupport geoLocation = GeoLocationSupport.NOT_AVAILABLE;
    private double latitude; // geographic coordinates
    private double longitude;

    public enum Fields {
        name, data, license, mimeType, hashCode, description, geoLocation, autoRotate, latitude, longitude
    }

    protected CIimage() {
    }

    /**
     * Updates the internal image data.
     * 
     * @param data
     *            binary image data.
     * @throws Exception
     */
    private void updateImageData(byte[] data) throws Exception {
        ByteArrayInputStream bstream = new ByteArrayInputStream(data);
        BufferedImage image = ImageIO.read(bstream);
        width = image.getWidth();
        height = image.getHeight();
        size = data.length;
        hashCode = Arrays.hashCode(data);
        bstream.reset();
        // read different EXIF data from the original image
        if (mimeType == MimeType.JPG) {
            Metadata metadata = ImageMetadataReader.readMetadata(new BufferedInputStream(bstream));

            Directory direction = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            // get image orientation
            // see: http://www.impulseadventure.com/photo/exif-orientation.html
            if (direction != null && direction.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
                switch (direction.getInt(ExifIFD0Directory.TAG_ORIENTATION)) {
                case 1:
                case 2:
                    orientation = ImageOrientation.TOP;
                    break;
                case 3:
                case 4:
                    orientation = ImageOrientation.BOTTOM;
                    break;
                case 5:
                case 6:
                    orientation = ImageOrientation.RIGHT_SIDE;
                    break;
                case 7:
                case 8:
                    orientation = ImageOrientation.LEFT_SIDE;
                    break;
                }
                autoRotate = true;
            } else {
                orientation = ImageOrientation.NOT_AVAILABLE;
                autoRotate = false;
            }
            // get geo location information
            GpsDirectory gps = metadata.getFirstDirectoryOfType(GpsDirectory.class);
            if (gps != null) {
                GeoLocation location = gps.getGeoLocation();
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    geoLocation = GeoLocationSupport.AVAILABLE;
                } else {
                    geoLocation = GeoLocationSupport.NOT_AVAILABLE;
                }
            } else {
                geoLocation = GeoLocationSupport.NOT_AVAILABLE;
            }
            // get image date time
            ExifSubIFDDirectory date = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (date != null && date.containsTag(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)) {
                dateTimeOriginal = date.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
            }
        } else {
            orientation = ImageOrientation.NOT_AVAILABLE;
            autoRotate = false;
            geoLocation = GeoLocationSupport.NOT_AVAILABLE;
        }

    }

    private CIimage(int ci, int ciType, long uiElement, String name, String description, String license,
            MimeType mimeType, byte[] data, int blobId) throws Exception {
        super(ci, ciType, uiElement);
        this.name = name;
        this.description = description;
        this.license = license;
        this.mimeType = mimeType;
        this.data = data;
        this.blobId = blobId;
        lazyLoad = false;
        updateImageData(data);
    }

    /**
     * Helper method to convert a {@code Image} to a {@code CIimage} object.
     * 
     * @param image
     *            {code Image object}
     * @return converted {@code CIimage} object
     * @throws IOException
     */
    public static CIimage convert(Image image) throws IOException {
        CIimage ciImg = new CIimage();
        ciImg.setHistId(image.getHistId());
        ciImg.setDomain(image.getDomain());
        ciImg.data = image.getData();
        ciImg.size = ciImg.data.length;
        ciImg.mimeType = image.getMIMEtype();
        ciImg.name = image.getName();
        ciImg.license = image.getLicense();
        ciImg.orientation = ImageOrientation.NOT_AVAILABLE;
        BufferedImage bsrc = ImageIO.read(new ByteArrayInputStream(ciImg.data));
        ciImg.width = bsrc.getWidth();
        ciImg.height = bsrc.getHeight();
        return ciImg;
    }

    /**
     * Returns the hash code of the binary image data.
     * 
     * @return hashCode of the image data
     * 
     */
    public int getImgDataHashCode() {
        return hashCode;
    }

    /**
     * Creates a container for an image.
     * 
     * @param dao
     *            {@code DAOiface} data access object
     * @param domain
     *            {@code Domain} of the data element
     * @param user
     *            creator of the {@code CIlong}
     * @param ciType
     *            ID of the {@code CItype}
     * @param uiElement
     *            ID of the {@code UIelement}
     * @param name
     *            name of the image
     * @param license
     *            license information
     * @param mimeType
     *            MIME type of the image
     * @param imgData
     *            binary image data
     * @return {@code CIimage} object
     * @throws Exception
     */
    public static CIimage create(DAOiface dao, Domain domain, User user, @DBkey(CI.class) int ci,
            @DBkey(CItype.class) int ciType, @DBkey(ClassSelector.class) long uiElement, String name,
            String description, String license, MimeType mimeType, byte[] imgData) throws Exception {
        CIblob blob = CIblob.create(dao, domain, user, ci, ciType, uiElement, imgData);
        CIimage cimage = new CIimage(ci, ciType, uiElement, name, description, license, mimeType, imgData,
                blob.getHistId());
        Base.save(dao, domain, user, cimage);
        return cimage;
    }

    /**
     * Loads a {@code CIimage}.
     * 
     * @param id
     *            ID of the {@code CIimage}
     * @return {@code CIimage} object
     * @throws Exception
     */
    public static CIimage load(@DBkey(CIimage.class) int id) throws Exception {
        return (CIimage) Base.load(null, CIimage.class, id, null);
    }

    /**
     * Loads a {@code CIimage}.
     * 
     * @param dao
     *            {@code DAOiface} data access object
     * @param id
     *            ID of the {@code CIimage}
     * @return {@code CIimage} object
     * @throws Exception
     */
    public static CIimage load(DAOiface dao, @DBkey(CIimage.class) int id) throws Exception {
        return (CIimage) Base.load(dao, CIimage.class, id, null);
    }

    /**
     * Loads a {@code CIimage} without binary data.
     * 
     * @param dao
     *            {@code DAOiface} data access object
     * @param id
     *            ID of the {@code CIimage}
     * @return {@code CIimage} object
     * @throws Exception
     */
    public static CIimage loadLazy(DAOiface dao, @DBkey(CIimage.class) int id) throws Exception {
        CIimage img = (CIimage) Base.loadLazy(dao, CIimage.class, id, null);
        img.lazyLoad = true;
        return img;
    }

    /**
     * Loads the binary image data for a lazy {@code CIimage} object.
     * 
     * @throws Exception
     */
    public void loadImageDataIfIsLazy(DAOiface dao) throws Exception {
        synchronized (this) {
            if (lazyLoad) {
                lazyLoad = false;
                CIblob blob = (CIblob) Base.load(dao, CIblob.class, this.blobId, null);
                data = blob.getData();
            }
        }
    }

    /**
     * Loads a {@code CIimage}.
     * 
     * @param ci
     *            ID of the {@code CI}
     * @param uiElement
     *            ID of the {@code UIelement}
     * @param name
     *            image name
     * @return {@code CIimage} object
     * @throws Exception
     */
    public static CIimage load(@DBkey(CI.class) int ci, @DBkey(ClassSelector.class) long uiElement, String name)
            throws Exception {
        return (CIimage) load(null, CIimage.class, ci, uiElement, new SearchCriteria(Fields.name, name), null);
    }

    @Override
    public void callbackAfterLoad(DAOiface dao) throws Exception {
        CIblob blob = (CIblob) Base.load(dao, CIblob.class, this.blobId, null);
        this.data = blob.getData();
    }

    @Override
    public boolean isCallbackAfterLoad() {
        return true;
    }

    /**
     * Loads a {@code CIimage} per name.
     * 
     * @param domain
     *            ID of the {@code Domain}
     * @param name
     *            image name
     * @return {@code CIimage} object
     * @throws Exception
     */
    public static CIimage load(@DBkey(Domain.class) int domain, String name) throws Exception {
        return (CIimage) load(null, CIimage.class, domain, new SearchCriteria(Fields.name, name), null);
    }

    /**
     * Loads a {@code CIimage} per name.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param domain
     *            ID of the {@code Domain}
     * @param name
     *            image name
     * @return {@code CIimage} object
     * @throws Exception
     */
    public static CIimage load(DAOiface dao, @DBkey(Domain.class) int domain, String name) throws Exception {
        return (CIimage) load(dao, CIimage.class, domain, new SearchCriteria(Fields.name, name), null);
    }

    /**
     * Loads a {@code CIimage}.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param ci
     *            ID of the {@code CI}
     * @param uiElement
     *            ID of the {@code UIelement}
     * @param name
     *            image name
     * @return {@code CIimage} object
     * @throws Exception
     */
    public static CIimage load(DAOiface dao, @DBkey(CI.class) int ci, @DBkey(ClassSelector.class) long uiElement,
            String name) throws Exception {
        return (CIimage) load(dao, CIimage.class, ci, uiElement, new SearchCriteria(Fields.name, name), null);
    }

    /**
     * Load all images of a {@code UIelement} in combination of a CI.
     * 
     * @param ci
     *            ID of the {@code CI}
     * @param uiElement
     *            ID of the {@code UIelement}
     * @return {@code CIimage} object
     * @throws Exception
     */
    public static List<Base> loadAll(@DBkey(CI.class) int ci, @DBkey(ClassSelector.class) long uiElement, boolean lazy)
            throws Exception {
        return loadAll(null, ci, uiElement, lazy);
    }

    /**
     * 
     * @param dao
     * @param ci
     * @param uiElement
     * @param lazy
     * @return
     * @throws Exception
     */
    public static List<Base> loadAll(DAOiface dao, @DBkey(CI.class) int ci, @DBkey(ClassSelector.class) long uiElement,
            boolean lazy) throws Exception {
        List<Base> list = loadList(dao, CIimage.class, ci, uiElement, null, null, lazy);
        if (lazy) {
            for (Base b : list) {
                ((CIimage) b).lazyLoad = true;
            }
        }
        return list;
    }

    /**
     * 
     * @param dao
     * @param domain
     * @param lazy
     * @return
     * @throws Exception
     */
    public static List<Base> loadAll(DAOiface dao, @DBkey(Domain.class) int domain, boolean lazy) throws Exception {
        List<Base> list = loadList(dao, CIimage.class, domain, null, null, lazy);
        if (lazy) {
            for (Base b : list) {
                ((CIimage) b).lazyLoad = true;
            }
        }
        return list;
    }

    /**
     * Creates or updates a {@code CIimage}
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param domain
     *            {@code Domain} of the data element
     * @param user
     *            user {@code User} who performs the update/creation
     * @param ci
     *            ID of the {@code CI}
     * @param ciType
     *            ID of the {@code CItype}
     * @param uiElement
     *            ID of the {@code UIelement}
     * @param license
     *            image license information
     * @param mimeType
     *            image MIME type
     * @param data
     *            binary image data
     * @return {@code CIimage} object
     * @throws Exception
     */
    public static CIimage createOrUpdate(DAOiface dao, Domain domain, User user, @DBkey(CI.class) int ci,
            @DBkey(CItype.class) int ciType, @DBkey(ClassSelector.class) long uiElement, String name,
            String description, String license, MimeType mimeType, byte[] data) throws Exception {
        CIimage image = (CIimage) load(dao, CIimage.class, ci, uiElement, new SearchCriteria(Fields.name, name), null);
        if (image == null) {
            CIblob blob = CIblob.create(dao, domain, user, ci, ciType, uiElement, data);
            image = new CIimage(ci, ciType, uiElement, name, description, license, mimeType, data, blob.getHistId());
            Base.save(dao, domain, user, image);

        } else {
            UpdateMap map = new UpdateMap(CIimage.Fields.class);
            map.addBinary(CIimage.Fields.data, data);
            map.addString(CIimage.Fields.name, name);
            map.addString(CIimage.Fields.description, description);
            map.addString(CIimage.Fields.license, license);
            map.addEnum(CIimage.Fields.mimeType, (Enum<?>) mimeType);
            Base.update(dao, user, image, map);
            if (map.get(CIimage.Fields.data) != null) {
                image.hashCode = Arrays.hashCode(data);
            }

        }
        return image;
    }

    /**
     * 
     * @param maxWidth
     * @param maxHeight
     * @return
     */
    public String cacheStringID(int maxWidth, int maxHeight) {
        return "" + hashCode + "_" + maxWidth + "_" + maxHeight + (isAutoRotate() ? "1" : "0");

    }

    /**
     * Creates a cache entry for a minimized image - e.g. thumbnail.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param image
     *            {@code CIimage} object
     * @param maxWidth
     *            maximum width of the minimized image
     * @param maxHeight
     *            maximum height of the minimized image
     * @throws Exception
     */
    public static CacheEntry createCacheEntry(DAOiface dao, CIimage image, int maxWidth, int maxHeight)
            throws Exception {
        ByteArrayInputStream bs = new ByteArrayInputStream(image.getData());
        BufferedImage sourceImage = ImageIO.read(bs);
        int swidth = sourceImage.getWidth();
        int sheight = sourceImage.getHeight();
        int tmp;
        if (image.getOrientation().isSideLying()) {
            tmp = swidth;
            swidth = sheight;
            sheight = tmp;
        }
        // image re-scaling necessary?
        if (swidth > maxWidth || sheight > maxHeight) {
            // calculate the new dimensions
            int w, h;
            if (swidth > sheight) {
                h = maxWidth * sheight / swidth;
                w = maxWidth;
            } else {
                h = maxHeight;
                w = maxHeight * swidth / sheight;
            }

            final String imgKeyDB = image.cacheStringID(maxWidth, maxHeight);

            if (image.getOrientation().isSideLying()) {
                tmp = w;
                w = h;
                h = tmp;
            }

            BufferedImage destinationImage = ImageManipulation.resize(sourceImage, w, h);
            destinationImage = ImageManipulation.rotateBufferedImage(image, destinationImage);

            ByteArrayOutputStream imagebuffer = new ByteArrayOutputStream();
            ImageIO.write(destinationImage, image.getMimeType().getExtension(), imagebuffer);
            byte[] imageData = imagebuffer.toByteArray();

            return CacheEntry.create(dao, Domain.get(image.getDomain()), ClassID.CIIMAGE, image.getHistId(), imgKeyDB,
                    imageData, CacheEntry.createImageDimension(w, h));

        }
        return null;
    }

    /**
     * Creates or updates a {@code CIimage} object.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param domain
     * @param user
     * @param ci
     * @param ciType
     * @param uiElement
     * @param name
     *            image name
     * @param license
     *            license
     * @param mimeType
     *            MIME type
     * @param data
     *            binary data
     * @param maxWidth
     *            wit
     * @param maxHeight
     * @return
     * @throws Exception
     */
    public static CIimage createOrUpdate(DAOiface dao, Domain domain, User user, @DBkey(CI.class) int ci,
            @DBkey(CItype.class) int ciType, @DBkey(ClassSelector.class) long uiElement, String name,
            String description, String license, MimeType mimeType, byte[] data, int maxWidth, int maxHeight,
            boolean detach) throws Exception {
        CIimage image = (CIimage) load(dao, CIimage.class, ci, uiElement, new SearchCriteria(Fields.name, name), null);
        if (image == null) {
            CIblob blob = CIblob.create(dao, domain, user, ci, ciType, uiElement, data);
            image = new CIimage(ci, ciType, uiElement, name, description, license, mimeType, data, blob.getHistId());
            Base.save(dao, domain, user, image);
            CacheEntry ce = createCacheEntry(dao, image, maxWidth, maxHeight);
            if (detach) {
                dao.flush();
                dao.detach(blob);
                dao.detach(image);
                if (ce != null) {
                    dao.detach(ce);
                }
            }

        } else {
            UpdateMap map = new UpdateMap(CIimage.Fields.class);
            map.addBinary(CIimage.Fields.data, data);
            map.addString(CIimage.Fields.name, name);
            map.addString(CIimage.Fields.description, description);
            map.addString(CIimage.Fields.license, license);
            map.addEnum(CIimage.Fields.mimeType, (Enum<?>) mimeType);
            Base.update(dao, user, image, map);
            if (map.get(CIimage.Fields.data) != null) {
                image.updateImageData(data);
                CacheEntry.dbDelete(dao, domain, ClassID.CIIMAGE, image.getHistId());
                createCacheEntry(dao, image, maxWidth, maxHeight);
            }

        }
        return image;
    }

    /**
     * Deletes a {code CIimage}.
     * 
     * @param user
     *            user who deletes the {code CIimage}
     * @param id
     *            image ID
     * @throws Exception
     */
    public static void delete(DAOiface dao, User user, @DBkey(CIimage.class) int id) throws Exception {
        Base.delete(dao, user, id, CIimage.class, false);
    }

    /**
     * Deletes a {code CIimage} object.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param user
     *            user who deletes the {code CIimage}
     * @param id
     *            image ID
     * @throws Exception
     */
    public static void delete(User user, @DBkey(CIimage.class) int id) throws Exception {
        Base.delete(user, id, CIimage.class, false);
    }

    /**
     * Returns the binary data of the image.
     * 
     * @return binary image data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Returns the name of the image.
     * 
     * @return image name
     */
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Returns the license information of the image.
     * 
     * @return image
     */
    public String getLicense() {
        return license;
    }

    /**
     * Returns the MIME type of the image.
     * 
     * @return {@code MimeType} of the image
     */
    public MimeType getMimeType() {
        return mimeType;
    }

    @Override
    public ClassID getCID() {
        return ClassID.CIIMAGE;
    }

    @Override
    public Object checkConstraints(DAOiface dao, UpdateMap update) throws Exception {
        // constrain unique image name
        Base.checkConstraintsPerCI(dao, update, getDomain(), getCi(), getHistId(), CIimage.Fields.name, getName());
        return null;
    }

    /**
     * Returns the image width in pixels.
     * 
     * @return image width
     */
    public int getWidth() {
        if (orientation.isSideLying()) {
            return height;
        }
        return width;
    }

    /**
     * Returns the image height in pixels.
     * 
     * @return image height
     */
    public int getHeight() {
        if (orientation.isSideLying()) {
            return width;
        }
        return height;
    }

    /**
     * Indicates whether the image data are available.
     * 
     * @return {@code true} if the image data are available, otherwise
     *         {@code false}
     */
    public boolean isLazyLoad() {
        return lazyLoad;
    }

    /**
     * Returns the size of the image in bytes.
     * 
     * @return size of the image in bytes
     */
    public int getSize() {
        return size;
    }

    public boolean isAutoRotate() {
        return autoRotate;
    }

    /**
     * Returns the EXIF date stamp
     * 
     * @return
     */
    public Date getDateTimeOriginal() {
        return dateTimeOriginal;
    }

    public GeoLocationSupport getGetLocation() {
        return geoLocation;
    }

    /**
     * Returns the latitude value, if EXIF geo location
     * 
     * @return latitude value
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Returns the longitude value, if EXIF geo location
     * 
     * @return longitude value
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Returns the EXIF orientation of the image.
     * 
     * @return EXIF orientation of the image
     */
    public ImageOrientation getOrientation() {
        return orientation;
    }

    @Override
    protected void callbackUpdate(DAOiface dao, User user, UpdateMap map, Object object) throws Exception {
        UpdateMap m = new UpdateMap(CIblob.Fields.class);
        m.addBinary(CIblob.Fields.binaryValue, this.data);
        Base.update(dao, user, this.blobId, CIblob.class, m);
    }
}
