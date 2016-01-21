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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

import at.treedb.db.DAOiface;

/**
 * <p>
 * DB representation of a data block. All blocks in sequence represents a file.
 * </p>
 * DBFS = Database file system
 * 
 * @author Peter Sauer
 *
 */
@Entity
public class DBFSblock implements Serializable {
    private static final long serialVersionUID = 1L;
    // TODO: discussion of this value - the actual value
    // is empirical doing some speed tests over different
    // persistence layer/DB combinations.
    final public static int BLOCK_SIZE = 512 * 1024;
    // the aggregated block ID consists of two int values
    // <------- long value 64 bit -------->
    // <--32 bit value--><--32 bit value-->
    // <--- file ID ----><- data block ID->
    @Id
    private long id;
    // binary data
    @Lob
    @Column(length = 5242880) // 5MB
    private byte[] data;
    // block size in bytes
    @Column(name = "m_size") // Oracle
    private int size;

    protected DBFSblock() {

    }

    /**
     * Constructs a {@Code DBFSblock}.
     * 
     * @param fileId
     *            ID of the {@code DBfile} object
     * @param index
     *            block index
     * @param data
     *            binary data
     */
    private DBFSblock(int fileId, int index, byte[] data) {
        this.id = ((long) fileId << 32) | index;
        this.data = data;
        this.size = data.length;
    }

    /**
     * Writes a {@code DBFSblock}.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param fileId
     *            filed ID
     * @param index
     *            block index
     * @param data
     *            binary data
     * @param size
     *            data size in bytes
     * @throws Exception
     */
    public static void write(DAOiface dao, int fileId, int index, byte[] data, int size) throws Exception {
        if (size == 0) {
            return;
        }
        if (data.length != size) {
            // create a smaller block
            byte block[] = new byte[size];
            System.arraycopy(data, 0, block, 0, size);
            data = block;
        }
        DBFSblock b = new DBFSblock(fileId, index, data);
        dao.save(b);
    }

    /**
     * Reads a {@code DBFSblock}.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param fileId
     *            file ID
     * @param index
     *            block index
     * @return {@code DBFSblock}
     */
    public static DBFSblock read(DAOiface dao, int fileId, int index) {
        return dao.get(DBFSblock.class, ((long) fileId << 32) | index);
    }

    /**
     * Returns the binary data of the block.
     * 
     * @return binary data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Returns the size of the data block
     * 
     * @return size of the data block in bytes
     */
    public int getSize() {
        return size;
    }

}
