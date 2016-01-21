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
import java.io.OutputStream;
import java.util.zip.CRC32;

import at.treedb.db.DAOiface;

/**
 * <p>
 * Class extension of {@code OutputStream} to access the DBFS (data base file
 * system) for write access.
 * </p>
 * 
 * @author Peter Sauer
 *
 */
public class DBoutputStream extends OutputStream {
    private DBfile dbFile;
    private int index = 0;
    private byte[] buffer = new byte[DBFSblock.BLOCK_SIZE];
    private int blockIndex = 0;
    private DAOiface dao;
    private CRC32 crc = new CRC32();

    /**
     * Constructor
     * 
     * @param dbFile
     *            open {@code DBfile} for writing
     * @param dao
     *            {@code DAOiface} (data access object)
     */
    public DBoutputStream(DBfile dbFile, DAOiface dao) {
        this.dbFile = dbFile;
        this.dao = dao;
    }

    /**
     * @see java.io.OutputStream#write(int b)
     */
    @Override
    public void write(int b) throws IOException {
        crc.update(b);
        buffer[index++] = (byte) b;
        dbFile.incSize();
        if (index == DBFSblock.BLOCK_SIZE) {
            try {
                DBFSblock.write(dao, dbFile.getHistId(), blockIndex++, buffer, index);
            } catch (Exception e) {
                throw new IOException(e.getMessage());
            }
            index = 0;
            buffer = new byte[DBFSblock.BLOCK_SIZE];
        }

    }

    @Override
    public void flush() {

    }

    @Override
    public void close() {
        dbFile.setCRC32(crc.getValue());
        dao.update(dbFile);
        try {
            DBFSblock.write(dao, dbFile.getHistId(), blockIndex, buffer, index);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        for (int i = 0; i < b.length; ++i) {
            write(b[i]);
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        for (int i = off; i < off + len; ++i) {
            write(b[i]);
        }
    }

}
