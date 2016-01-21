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

import java.nio.channels.FileChannel;

/**
 * <p>
 * Helper class to read data from the archive dump file.
 * </p>
 * 
 * @author Peter Sauer
 *
 */
public class ArchiveFileAccess {
    private int offset; // offset of the data
    private int size; // size of the data
    private FileChannel fileChannel;

    /**
     * Constructor
     * 
     * @param offset
     *            start position of the data block
     * @param size
     *            data size
     * @param fileChannel
     *            file object to access the data
     */
    public ArchiveFileAccess(int offset, int size, FileChannel fileChannel) {
        this.offset = offset;
        this.size = size;
        this.fileChannel = fileChannel;
    }

    /**
     * Returns the offset of the data.
     * 
     * @return start position of the data
     */
    public int getOffset() {
        return offset;
    }

    /***
     * Returns the size of the data.
     * 
     * @return data block size
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns a Java NIO object.
     * 
     * @return Java NIO object
     */
    public FileChannel getFileChannel() {
        return fileChannel;
    }
}
