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
package at.treedb.db;

/**
 * <p Helper class for creating a {@code Clazz} object.
 * </p>
 * 
 * @author Peter Sauer
 *
 */
public class ClazzDummy {
    private String name;
    private String source;
    private byte[] data;

    /**
     * Constructor
     * 
     * @param name
     *            full qualified class name
     * @param source
     *            optional Java source
     * @param data
     *            binary class data
     */
    public ClazzDummy(String name, String source, byte[] data) {
        this.name = name;
        this.source = source;
        this.data = data;
    }

    /**
     * Returns the full qualified class name.
     * 
     * @return full qualified class name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the Java source.
     * 
     * @return Java source
     */
    public String getSource() {
        return source;
    }

    /**
     * Returns the binary class data.
     * 
     * @return binary class data
     */
    public byte[] getData() {
        return data;
    }
}
