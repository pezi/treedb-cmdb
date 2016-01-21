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

/**
 * <p>
 * Helper class for access the dump file which contains all the files of a
 * JavaScript library.
 * </p>
 * 
 * @author Peter Sauer
 *
 */
public class ArchiveEntry {
    private int offset; // file offset
    private int length; // data length block

    /**
     * Constructor
     * 
     * @param offset
     *            start position of the archive entry
     * @param length
     *            length of the archive entry
     */
    public ArchiveEntry(int offset, int length) {
        this.offset = offset;
        this.length = length;
    }

    /**
     * Returns the start position of the archive entry.
     * 
     * @return start position of the archive entry
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Returns the length of the archive entry
     * 
     * @return file length of the archive entry
     */
    public int getLength() {
        return length;
    }
}