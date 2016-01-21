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
package at.treedb.dbfs;

import java.io.IOException;
import java.io.InputStream;

import at.treedb.db.DAOiface;

/**
 * <p>
 * Class extension of {@code InputStream} to access the DBFS (data base file
 * system) for read access.
 * </p>
 * 
 * @author Peter Sauer
 *
 */
public class DBinputStream extends InputStream {
    private DBfile dbFile;
    private int index = 0;
    private byte[] buffer;
    private int blockIndex = -1;
    private DAOiface dao;
    private boolean eof = false;

    /**
     * Constructor
     * 
     * @param dbFile
     *            open {@code DBfile} for reading
     * @param dao
     *            {@code DAOiface} (data access object)
     */
    public DBinputStream(DBfile dbFile, DAOiface dao) {
        this.dbFile = dbFile;
        this.dao = dao;
    }

    /**
     * @see java.io.InputStream#read
     */
    @Override
    public int read() throws IOException {

        if (eof) {
            return -1;
        }
        if (index == 0) {
            if (blockIndex == -1) {
                blockIndex = 0;
            }
            DBFSblock block = DBFSblock.read(dao, dbFile.getHistId(), blockIndex);
            if (block == null) {
                eof = true;
                return -1;
            }
            buffer = block.getData();
        }
        byte b = buffer[index++];
        if (index == DBFSblock.BLOCK_SIZE) {
            index = 0;
            ++blockIndex;
        } else if (index == buffer.length) {
            eof = true;
        }
        return b & 0xff;
    }

    /**
     * @see java.io.InputStream#read(byte[] data)
     */
    @Override
    public int read(byte[] data) throws IOException {
        for (int i = 0; i < data.length; ++i) {
            int b = read();
            if (b == -1) {
                if (i == 0) {
                    return -1;
                } else {
                    return i;
                }
            }
            data[i] = (byte) b;
        }
        return data.length;
    }

    /**
     * @see java.io.InputStream#read(byte[] data,int off,int len)
     */
    @Override
    public int read(byte[] data, int off, int len) throws IOException {
        int count = 0;
        for (int i = off; i < off + len; ++i) {
            int b = read();
            if (b == -1) {
                if (i == 0) {
                    return -1;
                } else {
                    return count;
                }
            }
            data[i] = (byte) b;
            ++count;
        }
        return count;
    }

    /**
     * Skips bytes relative from the actual file position.
     * 
     * @see java.io.InputStream#skip(long n)
     */
    @Override
    public long skip(long n) throws IOException {
        long skipIndex = index + n;
        if (skipIndex > dbFile.getSize()) {
            return 0;
        }
        int skipBlockIndex = (int) (skipIndex / DBFSblock.BLOCK_SIZE);
        if (blockIndex == skipBlockIndex) {
            index = (int) skipIndex;
            return n;
        }
        DBFSblock block = DBFSblock.read(dao, dbFile.getHistId(), skipBlockIndex);
        if (block == null) {
            return 0;
        }
        buffer = block.getData();
        blockIndex = skipBlockIndex;
        index = skipBlockIndex;
        return n;
    }

    /**
     * Sets the file position relative to the beginning.
     * 
     * @param n
     *            number of bytes
     * @throws Exception
     */
    public void seek(long n) throws Exception {

        if (n < 0) {
            throw new IOException("DBinputStream.seek(): position has a negative value");
        }
        if (n > dbFile.getSize()) {
            throw new IOException("DBinputStream.seek(): end of file reached");
        }
        int seekBlockIndex = (int) (n / DBFSblock.BLOCK_SIZE);
        index = (int) (n % DBFSblock.BLOCK_SIZE);
        if (seekBlockIndex != blockIndex) {
            DBFSblock block = DBFSblock.read(dao, dbFile.getHistId(), seekBlockIndex);
            if (block != null) {
                buffer = block.getData();

            } else {
                throw new IOException("DBinputStream.seek(): Unable to read block ID:" + dbFile.getHistId() + " index: "
                        + index + "!");
            }
            blockIndex = seekBlockIndex;
        }
    }

    @Override
    public int available() {
        return buffer.length - index;
    }

    @Override
    public void close() {

    }

    /**
     * Returns the file size.
     * 
     * @return file size
     */
    public long length() {
        return dbFile.getSize();
    }

    public DBfile getDbFile() {
        return dbFile;
    }

    public boolean markSupported() {
        return false;
    }
}
