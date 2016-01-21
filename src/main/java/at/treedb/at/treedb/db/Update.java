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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

import at.treedb.ci.ImageDummy;
import at.treedb.i18n.IstringDummy;

/**
 * <p>
 * Data container for storing updates/changes for a single data element.
 * </p>
 * 
 * @author Peter Sauer
 * 
 */
public class Update {
    /**
     * Known update values
     */
    public enum Type {
        INT,
        /** integer value update */
        LONG,
        /** updates a long value */
        FLOAT,
        /** updates a float value */
        DOUBLE,
        /** float value update */
        STRING,
        /** string value update */
        BINARY,
        /** binary value update */
        BOOLEAN,
        /** boolean value update */
        ENUM,
        /** enum value update */
        DATE,
        /** date value update */
        ISTRING,
        /** string value update */
        ISTRING_DELETE,
        /** language string deletion */
        IMAGE,
        /** image update */
        IMAGE_DELETE,
        /** image deletion */
        BIGDECIMAL,
        /** big decimal value update */
        LAZY_BINARY, IMAGE_DUMMY,
    }

    private Type type;
    private Object object;
    private ArrayList<IstringDummy> ilist;

    /**
     * Update container
     * 
     * @param type
     *            type of data
     * @param object
     *            update data
     */
    public Update(Type type, Object object) {
        this.type = type;
        if (type == Type.ISTRING || type == Type.ISTRING_DELETE) {
            ilist = new ArrayList<IstringDummy>();
            ilist.add((IstringDummy) object);
            object = ilist;
        }
        this.object = object;
    }

    /**
     * Returns the data type of the update.
     * 
     * @return data type
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the update data.
     * 
     * @return update data
     */
    public Object getObject() {
        return object;
    }

    /**
     * Returns an {@code int}.
     * 
     * @return {@code int} update
     */
    public int getInt() {
        return (Integer) object;
    }

    /**
     * Returns an {@code int}.
     * 
     * @return {@code int} update
     */
    public long getLong() {
        return (Long) object;
    }

    /**
     * Returns a {@code float}.
     * 
     * @return {@code float} update
     */
    public float getFloat() {
        return (Float) object;
    }

    /**
     * Returns a {@code double}.
     * 
     * @return {@code double} update
     */
    public double getDouble() {
        return (Double) object;
    }

    /**
     * Returns a {@code boolean}.
     * 
     * @return {@code boolean} update
     */
    public boolean getBoolean() {
        return (Boolean) object;
    }

    /**
     * Returns a {@code BigDecimal}
     * 
     * @return {@code BigDecimal} update
     */
    public BigDecimal getBigDecimal() {
        return (BigDecimal) object;
    }

    /**
     * Return a generic {@code Enum}.
     * 
     * @return generic {@code Enum} update
     */
    public Enum<?> getEnum() {
        return (Enum<?>) object;
    }

    /**
     * Returns a string.
     * 
     * @return string update
     */
    public String getString() {
        return (String) object;
    }

    /**
     * Returns a {@code Date}.
     * 
     * @return date update
     */
    public Date getDate() {
        return (Date) object;
    }

    /**
     * Returns binary data.
     * 
     * @return binary data update
     */
    public byte[] getBinary() {
        return (byte[]) object;
    }

    /**
     * Returns a list of {@code IstringDummy}
     * 
     * @return list of {@code IstringDummy}
     */
    @SuppressWarnings("unchecked")
    public ArrayList<IstringDummy> getIstringDummy() {
        return (ArrayList<IstringDummy>) object;
    }

    /**
     * Adds a {@code IstringDummy} for updating an {@code Istring}.
     * 
     * @param dstr
     *            {@code IstringDummy}
     */
    public void addDummyIstring(IstringDummy dstr) {
        ilist.add(dstr);
    }

    /**
     * Returns a {@code ImageDummy}.
     * 
     * @return {@code ImageDummy}
     */
    public ImageDummy getImageDummy() {
        return (ImageDummy) object;
    }

    /**
     * Returns a {@code UpdateMap}
     * 
     * @return {@code UpdateMap}
     */
    public UpdateMap getUpdateMap() {
        return (UpdateMap) object;
    }

}
