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

import at.treedb.util.ArchiveFileAccess;

/**
 * Dummy container for a {@code CIfile} object.
 * 
 * @author Peter Sauer
 * 
 */
public class FileDummy {
    private String name; // file name
    private String license;
    private String description;
    private MimeType mimeType;
    private File file;
    private ArchiveFileAccess archiveData;
    private MimeType mimeTypePreview;
    private byte[] preview; // optional preview image

    /**
     * Creates a dummy container for a {@code CIfile}.
     * 
     * @param name
     *            file name
     * @param license
     *            license informations
     * @param description
     *            file description
     * @param file
     *            file data
     * @param mimeType
     *            MIME type of the binary file
     * @param preview
     *            optional preview image
     * @param mimeTypePreview
     *            mimeType MIME type of preview
     */
    public FileDummy(String name, String license, String description, File file, MimeType mimeType, byte[] preview,
            MimeType mimeTypePreview) {
        this.name = name;
        this.license = license;
        this.description = description;
        this.mimeType = mimeType;
        this.file = file;
        this.mimeTypePreview = mimeTypePreview;
        this.preview = preview;

    }

    /**
     * Creates a dummy container for a {@code CIfile}.
     * 
     * @param name
     *            file name
     * @param license
     *            license informations
     * @param description
     *            file description
     * @param archiveData
     *            archiveData
     * @param mimeType
     *            MIME type of the binary file
     * @param preview
     *            optional preview image
     * @param mimeTypePreview
     *            mimeType MIME type of preview
     */
    public FileDummy(String name, String license, String description, ArchiveFileAccess archiveData, MimeType mimeType,
            byte[] preview, MimeType mimeTypePreview) {
        this.name = name;
        this.license = license;
        this.description = description;
        this.mimeType = mimeType;
        this.archiveData = archiveData;
        this.mimeTypePreview = mimeTypePreview;
        this.preview = preview;
    }

    /**
     * Returns the name of the (binary) file.
     * 
     * @return file name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the license of the (binary) file.
     * 
     * @return license
     */
    public String getLicense() {
        return license;
    }

    /**
     * Returns the description of the file.
     * 
     * @return file description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the MIME type of the file.
     * 
     * @return MIME of the binary file
     */
    public MimeType getMimeType() {
        return mimeType;
    }

    /**
     * Returns the {@code File} object.
     * 
     * @return {@code File} object
     */
    public File getFile() {
        return file;
    }

    /**
     * Returns the archive data.
     * 
     * @return archive data
     */
    public ArchiveFileAccess getArchivData() {
        return archiveData;
    }

    /**
     * Returns the MIME type of the file preview.
     * 
     * @return MIME type of the preview
     */
    public MimeType getMimeTypePreview() {
        return mimeTypePreview;
    }

    /**
     * Returns the preview {@code File}.
     * 
     * @return preview {@code File}
     */
    public byte[] getFilePreview() {
        return preview;
    }
}
