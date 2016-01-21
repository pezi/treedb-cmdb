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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import at.treedb.backup.Export;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZMethod;
import org.apache.commons.compress.archivers.sevenz.SevenZMethodConfiguration;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;

/**
 * <p>
 * Helper class to create an 7-zip archive.
 * </p>
 * 
 * @author Peter Sauer
 *
 */
public class Compress {
    final static public int BUFFER_SIZE = 4 * 1024 * 1024;
    private String[] fileList;
    private String baseDir;
    private String archive;
    private HashSet<String> exclude;

    private SevenZOutputFile sevenZOutput;
    private SevenZMethod compressionMehtod = SevenZMethod.LZMA2;

    /**
     * Constructor
     * 
     * @param archive
     *            path of the archive file
     * @param baseDir
     *            base directory
     * @param fileList
     *            list of files and directories
     */
    public Compress(String archive, String baseDir, String excludeExt, String[] fileList) {
        this.archive = archive;
        if (!baseDir.endsWith("/")) {
            baseDir += "/";
        }
        this.baseDir = baseDir;
        this.fileList = fileList;
        this.exclude = new HashSet<String>();
        if (excludeExt != null && !excludeExt.isEmpty()) {
            for (String e : excludeExt.split("\\|")) {
                exclude.add(e.toLowerCase());
            }
        }
    }

    /**
     * Creates the archive.
     * 
     * @throws IOException
     */
    public void compress() throws IOException {
        sevenZOutput = new SevenZOutputFile(new File(archive));
        sevenZOutput.setContentCompression(compressionMehtod);
        for (String f : fileList) {
            compressFile(new File(baseDir + f));
        }
        sevenZOutput.close();
    }

    /**
     * Compresses a file or a directory.
     * 
     * @param f
     *            file or directory to be compressed
     * @throws IOException
     */
    private void compressFile(File f) throws IOException {
        if (f.isDirectory()) {
            File[] fileList = f.listFiles();
            if (fileList.length > 0) {
                for (File file : f.listFiles()) {
                    compressFile(file);
                }
            } else {
                addEmptyDir(f);
            }
        } else {
            addFile(f);
        }
    }

    /**
     * Adds an empty directory to the archive.
     * 
     * @param dir
     *            empty directory
     * @throws IOException
     */
    private void addEmptyDir(File dir) throws IOException {
        SevenZArchiveEntry entry = new SevenZArchiveEntry();
        entry.setName(dir.getAbsolutePath().substring(baseDir.length()).replace('\\', '/'));
        entry.setAccessDate(dir.lastModified());
        entry.setCreationDate(dir.lastModified());
        entry.setLastModifiedDate(dir.lastModified());
        entry.setDirectory(true);
        sevenZOutput.putArchiveEntry(entry);
        sevenZOutput.closeArchiveEntry();

    }

    private String getFileExtension(File file) {
        String name = file.getName();
        try {
            return name.substring(name.lastIndexOf(".") + 1);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Adds a file to the archive
     * 
     * @param file
     *            file to be compressed
     * @throws IOException
     */
    private void addFile(File file) throws IOException {
        if (!exclude.isEmpty()) {
            String extension = getFileExtension(file);
            if (!extension.isEmpty() && exclude.contains(extension.toLowerCase())) {
                return;
            }
        }

        SevenZArchiveEntry entry = new SevenZArchiveEntry();
        entry.setName(file.getAbsolutePath().substring(baseDir.length()).replace('\\', '/'));
        entry.setAccessDate(file.lastModified());
        entry.setCreationDate(file.lastModified());
        entry.setLastModifiedDate(file.lastModified());
        entry.setDirectory(false);

        sevenZOutput.putArchiveEntry(entry);

        FileInputStream is = new FileInputStream(file);
        long size = file.length();
        byte[] buffer = new byte[BUFFER_SIZE];
        boolean setCompressMethod = false;
        while (size > 0) {
            long read = Math.min(buffer.length, size);
            is.read(buffer, 0, (int) read);
            if (!setCompressMethod) {
                setCompressMethod = true;
                entry.setContentMethods(Arrays.asList(
                        new SevenZMethodConfiguration(Export.findBestCompressionMethod(buffer, compressionMehtod))));
            }
            sevenZOutput.write(buffer, 0, (int) read);
            size -= read;
        }
        is.close();
        sevenZOutput.closeArchiveEntry();
    }

    /**
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        StringBuffer buf = new StringBuffer();
        buf.append("7z-compression with following parameters: ");
        for (String s : args) {
            buf.append(s);
            buf.append(' ');
        }
        System.out.println(buf.toString());
        Compress c = new Compress(args[0], args[1], args[2], Arrays.copyOfRange(args, 3, args.length));
        c.compress();
    }

}
