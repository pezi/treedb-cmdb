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
package at.treedb.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import at.treedb.ci.FileDummy;
import at.treedb.ci.MimeType;

/**
 * <p>
 * Helper class to encapsulate the data access to a file collection. The file
 * collection can be a directory or a 7z archive.
 * </p>
 * 
 * @author Peter Sauer
 *
 */
public class FileAccess {
    private String baseDir;
    private SevenZip sevenZip;
    private boolean isArchive = false;

    /**
     * Constructor for file access inside a directory.
     * 
     * @param baseDir
     *            base directory
     * @throws IOException
     */
    public FileAccess(String baseDir) throws IOException {
        this.baseDir = baseDir;
    }

    /**
     * Constructor for file access inside a 7z archive
     * 
     * @param szip
     *            path of the 7z archive
     * @param baseDir
     *            base directory inside the archive
     */
    public FileAccess(SevenZip szip, String baseDir) {
        this.sevenZip = szip;
        this.baseDir = baseDir;
        this.isArchive = true;
    }

    /**
     * Checks the existence of a file.
     * 
     * @param path
     *            file path
     * @return {@code true} if the file exists
     */
    public boolean exists(String path) {
        if (!isArchive) {
            return (new File(baseDir + path)).exists();
        }
        return sevenZip.exists(baseDir + path);
    }

    /**
     * Creates a {@code FileDummy} object.
     * 
     * @param name
     *            file name
     * @param license
     * @param description
     *            description
     * @param filePath
     *            file path
     * @param mimeType
     *            MIME type
     * @param preview
     *            preview
     * @param mimeTypePreview
     *            MIME type of the preview
     * @return {@code FileDummy} object
     * @throws Exception
     */
    public FileDummy createFileDummy(String name, String license, String description, String filePath,
            MimeType mimeType, byte[] preview, MimeType mimeTypePreview) throws Exception {
        if (isArchive) {
            return new FileDummy(name, license, description, sevenZip.getArchiveAccessEntry(baseDir + filePath),
                    mimeType, preview, mimeTypePreview);
        }
        return new FileDummy(name, license, description, new File(baseDir + filePath), mimeType, preview,
                mimeTypePreview);
    }

    /**
     * Returns the binary data of file
     * 
     * @param path
     *            virtual file path
     * @return binary data
     * @throws Exception
     */
    public byte[] getData(String path) throws Exception {
        if (!isArchive) {
            return Stream.readByteStream(baseDir + path);
        }
        return sevenZip.getData(baseDir + path);
    }

    /**
     * Returns the text from a file.
     * 
     * @param path
     *            virtual path
     * @return
     * @throws Exception
     */
    public String getText(String path) throws Exception {
        if (!isArchive) {
            return new String(Stream.readByteStream(baseDir + path), "UTF8");
        }
        return new String(sevenZip.getData(baseDir + path), "UTF8");
    }

    /**
     * Searches a directory
     * 
     * @param path
     *            virtual path
     * @return {@code true} if the directory exists, {@code false} if not
     */
    public boolean searchDir(String path) {
        if (!isArchive) {
            File f = new File(baseDir + path);
            if (f.exists() && f.isDirectory()) {
                return true;
            }
            return false;
        }
        return sevenZip.searchDir(baseDir + path);
    }

    /**
     * Returns all files by traversing recursively a directory.
     * 
     * @param list
     *            array for storing the files
     * @param path
     *            virtual path
     */
    private void list(ArrayList<String> list, String path) {
        String[] flist = new File(baseDir + path).list();
        if (flist != null) {
            for (String s : flist) {
                if (!path.isEmpty() && !path.endsWith("/")) {
                    path = path + "/";
                }
                File f = new File(baseDir + path + s);
                if (f.isDirectory()) {
                    list(list, path + s);
                } else {
                    list.add(path + s);
                }
            }
        }
    }

    /**
     * Returns all files by traversing recursively a directory.
     * 
     * @param path
     *            virtual path
     * @return file file list
     */
    public String[] list(String path) {
        if (!isArchive) {
            ArrayList<String> list = new ArrayList<String>();
            list(list, path);
            if (list.size() > 0) {
                return list.toArray(new String[list.size()]);
            }
            return null;
        }

        String[] list = sevenZip.list(baseDir + path);
        // remove the base directory from the absolute path!
        if (list != null) {
            for (int i = 0; i < list.length; ++i) {
                list[i] = list[i].substring(baseDir.length());
            }
        }
        return list;
    }

    /**
     * Closes all file handles.
     * 
     * @throws IOException
     */
    public void close() throws IOException {
        if (isArchive) {
            sevenZip.close();
        }
    }
}
