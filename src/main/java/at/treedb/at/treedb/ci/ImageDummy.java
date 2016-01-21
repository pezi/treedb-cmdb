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

import at.treedb.db.UpdateMap;

/**
 * Placeholder for creating/updating an image.
 * 
 * @author Peter Sauer
 */

public class ImageDummy {
    // internal name of the image
    private String name;
    // binary data
    private byte[] data;
    // MIME (Multipurpose Internet Mail Extensions) type
    private MimeType mimeType;
    // image description
    private String description;
    // license information
    private String license;
    // dummy type
    private DummyType type;
    // create thumbnail
    boolean thumbnail;
    // maximum image width
    private int maxWidth;
    // maximum image height
    private int maxHeight;

    private boolean detachImage;

    /**
     * Type of the dummy - new versus build-in image
     * 
     * @author Peter Sauer
     * 
     */
    public enum DummyType {
        /**
         * build-in, system (JAR) located image
         */
        SYSTEM,
        /**
         * new image
         */
        CREATE
    }

    /**
     * Creates a dummy for creating a new image.
     * 
     * @param name
     *            name of the image
     * @param data
     *            image data
     * @param mimeType
     *            MIME type of the image
     * @param description
     *            image descriptions
     * @param license
     *            license of the image
     * @throws Exception
     */
    public ImageDummy(String name, byte[] data, MimeType mimeType, String description, String license)
            throws Exception {
        if (name == null || data == null || mimeType == null) {
            throw new Exception("ImageDummy(): Input parameter is null!");
        }
        this.name = name;
        this.data = data;
        this.mimeType = mimeType;
        this.description = description;
        this.license = license;
        this.type = DummyType.CREATE;
    }

    public ImageDummy(String name, byte[] data, MimeType mimeType, String description, String license, int width,
            int height) throws Exception {
        if (name == null || data == null || mimeType == null) {
            throw new Exception("ImageDummy(): Input parameter is null!");
        }
        this.name = name;
        this.data = data;
        this.mimeType = mimeType;
        this.license = license;
        this.description = description;
        type = DummyType.CREATE;
        thumbnail = true;
        this.maxWidth = width;
        this.maxHeight = height;
    }

    /**
     * Creates a dummy for a build-in image.
     * 
     * @param name
     *            name of the image
     */
    public ImageDummy(String name) {
        this.name = name;
        type = DummyType.SYSTEM;
    }

    /**
     * Returns the dummy type
     * 
     * @return dummy type - new or build-in image
     */
    public DummyType getDummyType() {
        return type;
    }

    /**
     * Returns the name of the icon.
     * 
     * @return name of the icon
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the image data.
     * 
     * @return image data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Returns the MIME type of the image.
     * 
     * @return mime type of the image
     */
    public MimeType getMimeType() {
        return mimeType;
    }

    /**
     * Returns the license of the image.
     * 
     * @return license of the image
     */
    public String getLicense() {
        return license;
    }

    /**
     * Returns the license of the image.
     * 
     * @return description of the image
     */
    public String getDescription() {
        return description;
    }

    public boolean isThumbnail() {
        return thumbnail;
    }

    /**
     * Returns the maximum width of the image.
     * 
     * @return maximum width of the image
     */
    public int getMaxWidth() {
        return maxWidth;
    }

    /**
     * Returns the maximum height of the image.
     * 
     * @return maximum height of the image
     */
    public int getMaxHeight() {
        return maxHeight;
    }

    /**
     * If this flag is {@code true} the new created image will be be detached
     * from the context of the persistence layer. This special flag is used
     * during the process of uploading (big) images. This flag should try to
     * avoid running into the {@code OutOfMemoryError} Exception.
     * 
     * @param detach
     *            {@code true} if the new created image should be be detached
     *            from the context of the persistence layer
     */
    public void setDeatchImage(boolean detach) {
        detachImage = detach;
    }

    /**
     * Returns {@code true} if the new created image should be be detached from
     * the context of the persistence layer.
     * 
     * @return {@code true} detach image, {@code false} if not
     */
    public boolean isDetachImage() {
        return detachImage;
    }

    /**
     * Returns a dummy image as a update map
     * 
     * @return
     * @throws Exception
     */
    public UpdateMap getImageUpdateMap() throws Exception {
        UpdateMap map = new UpdateMap(Image.Fields.class);
        if (name != null) {
            map.addString(Image.Fields.name, name);
        }
        if (data != null) {
            map.addBinary(Image.Fields.data, data);
        }
        if (license != null) {
            map.addString(Image.Fields.license, license);
        }
        if (mimeType != null) {
            map.addEnum(Image.Fields.mimeType, mimeType);
        }
        return map;
    }
}
