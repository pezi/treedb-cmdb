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
import java.util.Date;
import java.util.HashMap;

import at.treedb.ci.ImageDummy;
import at.treedb.db.Update.Type;
import at.treedb.i18n.IstringDummy;
import at.treedb.i18n.Locale;

/**
 * <p>
 * Map containing updates for entity based on {@code CIdata}.
 * </p>
 * 
 * @author Peter Sauer
 * 
 */
public class UpdateMap {
    private HashMap<Enum<?>, Update> map = new HashMap<Enum<?>, Update>();
    private Class<?> clazz;

    /**
     * Creates an update map
     * 
     * @param fields
     */
    public UpdateMap(Class<? extends Enum<?>> fields) {
        this.clazz = fields;
    }

    /**
     * Removes an {@code Update} entry
     * 
     * @param field
     *            enum value
     * @return removed {@code Update}
     */
    public Update remove(Enum<?> field) {
        return map.remove(field);
    }

    /**
     * Returns an {@code Update}.
     * 
     * @param field
     *            enum value
     * @return {@code Update}
     */
    public Update get(Enum<?> field) {
        return map.get(field);
    }

    /**
     * Returns an update string value with removing the update.
     * 
     * @param field
     *            field name
     * @return string value
     */
    public String getStringAndRemoveUpdate(Enum<?> field) {
        Update u = map.get(field);
        String s = null;
        if (u != null) {
            s = u.getString();
            map.remove(field);
        }
        return s;
    }

    /**
     * Returns a update map.
     * 
     * @return update map
     */
    public HashMap<Enum<?>, Update> getMap() {
        return map;
    }

    /**
     * Returns the size of the update map.
     * 
     * @return update map size
     */
    public int size() {
        return map.size();
    }

    /**
     * Checks if this update map is empty.
     * 
     * @return {@code true} if this map is empty
     */
    public boolean isEmpty() {
        return map.size() == 0 ? true : false;
    }

    /**
     * Adds a {@code int} update.
     * 
     * @param field
     *            field name
     * @param i
     *            int update value
     * @return {@code Update} object
     * @throws Exception
     */
    public Update addInt(Enum<?> field, int i) throws Exception {
        if (!field.getClass().equals(clazz)) {
            throw new Exception("UpdateMap.addInt(): Class missmatch!");
        }
        Update u = new Update(Type.INT, new Integer(i));
        map.put(field, u);
        return u;
    }

    /*
     * public Update addLocale(Enum<?> field,Locale.LOCALE locale) throws
     * Exception { if (!field.getClass().equals(clazz)) { throw new Exception(
     * "UpdateMap.addLocale(): Class missmatch!"); } Update u = new
     * Update(Type.LOCALE,locale); map.put(field, u); return u; }
     */

    /**
     * Adds an {@code long} update.
     * 
     * @param field
     *            field name
     * @param l
     *            long update value
     * @return {@code Update} object
     * @throws Exception
     */
    public Update addLong(Enum<?> field, long l) throws Exception {
        if (!field.getClass().equals(clazz)) {
            throw new Exception("UpdateMap.addLong(): Class missmatch!");
        }
        Update u = new Update(Type.LONG, new Long(l));
        map.put(field, u);
        return u;
    }

    /**
     * Adds a {@code flat} update.
     * 
     * @param field
     *            field name
     * @param f
     *            float update value
     * @return {@code Update} object
     * @throws Exception
     */
    public Update addFloat(Enum<?> field, long f) throws Exception {
        if (!field.getClass().equals(clazz)) {
            throw new Exception("UpdateMap.addFloat(): Class missmatch!");
        }
        Update u = new Update(Type.FLOAT, new Float(f));
        map.put(field, u);
        return u;
    }

    /**
     * Adds a {@code double} update.
     * 
     * @param field
     *            field name
     * @param d
     *            double update value
     * @return {@code Update} object
     * @throws Exception
     */
    public Update addDouble(Enum<?> field, double d) throws Exception {
        if (!field.getClass().equals(clazz)) {
            throw new Exception("UpdateMap.addDouble(): Class missmatch!");
        }
        Update u = new Update(Type.DOUBLE, new Double(d));
        map.put(field, u);
        return u;
    }

    /**
     * Adds a {@code String} update.
     * 
     * @param field
     *            field name
     * @param str
     *            string update value
     * @return {@code Update} object
     * @throws Exception
     */
    public Update addString(Enum<?> field, String str) throws Exception {
        if (!field.getClass().equals(clazz)) {
            throw new Exception("UpdateMap.addString(): Class missmatch!");
        }
        Update u = new Update(Type.STRING, str);
        map.put(field, u);
        return u;
    }

    /**
     * Adds an {@code BigDecimal} update.
     * 
     * @param field
     *            field name
     * @param big
     *            {@code BigDecimal} update value
     * @return {@code Update} object
     * @throws Exception
     */
    public Update addBigDecimal(Enum<?> field, BigDecimal big) throws Exception {
        if (!field.getClass().equals(clazz)) {
            throw new Exception("UpdateMap.addBigDecimal(): Class missmatch!");
        }
        Update u = new Update(Type.BIGDECIMAL, big);
        map.put(field, u);
        return u;
    }

    /**
     * Adds a binary data update.
     * 
     * @param field
     *            field name
     * @param bin
     *            binary data
     * @return {@code Update} object
     * @throws Exception
     */
    public Update addBinary(Enum<?> field, byte[] bin) throws Exception {
        if (!field.getClass().equals(clazz)) {
            throw new Exception("UpdateMap.addBinary(): Class missmatch!");
        }
        Update u = new Update(Type.BINARY, bin);
        map.put(field, u);
        return u;
    }

    public Update addLazyBinary(Enum<?> field, byte[] bin) throws Exception {
        if (!field.getClass().equals(clazz)) {
            throw new Exception("UpdateMap.addBinary(): Class missmatch!");
        }
        Update u = new Update(Type.LAZY_BINARY, bin);
        map.put(field, u);
        return u;
    }

    /**
     * Adds a {@code boolean} update.
     * 
     * @param field
     *            field name
     * @param bool
     *            boolean update value
     * @return {@code Update} object
     * @throws Exception
     */
    public Update addBoolean(Enum<?> field, boolean bool) throws Exception {
        if (!field.getClass().equals(clazz)) {
            throw new Exception("UpdateMap.addBoolean(): Class missmatch!");
        }
        Update u = new Update(Type.BOOLEAN, new Boolean(bool));
        map.put(field, u);
        return u;
    }

    /**
     * Adds an {@code enum} update.
     * 
     * @param field
     *            field name
     * @param e
     *            enum update value
     * @return {@code Update} object
     * @throws Exception
     */
    public Update addEnum(Enum<?> field, Enum<?> e) throws Exception {
        if (!field.getClass().equals(clazz)) {
            throw new Exception("UpdateMap.addEnum(): Class missmatch!");
        }
        Update u = new Update(Type.ENUM, e);
        map.put(field, u);
        return u;
    }

    /**
     * Adds an {@code Date} update.
     * 
     * @param field
     *            field name
     * @param date
     *            date update value
     * @return {@code Update} object
     * @throws Exception
     */
    public Update addDate(Enum<?> field, Date date) throws Exception {
        if (!field.getClass().equals(clazz)) {
            throw new Exception("UpdateMap.addDate(): Class missmatch!");
        }
        Update u = new Update(Type.DATE, date);
        map.put(field, u);
        return u;
    }

    /**
     * Adds an {@code Istring} update.
     * 
     * @param field
     *            field name
     * @param text
     *            text update
     * @param language
     *            language, 2 digit code
     * @param country
     *            country, 2 digit code
     * @return {@code Update} object
     * @throws Exception
     */
    public Update addIstring(Enum<?> field, String text, Locale.LANGUAGE language, Locale.COUNTRY country)
            throws Exception {
        if (!field.getClass().equals(clazz)) {
            throw new Exception("UpdateMap.addIstring(): Class missmatch!");
        }
        Update u = map.get(field);
        IstringDummy d = new IstringDummy(text, language, country);
        if (u == null) {
            u = new Update(Type.ISTRING, d);
        } else {
            u.addDummyIstring(d);
        }
        map.put(field, u);
        return u;
    }

    /**
     * Adds an {@code Istring} removal update.
     * 
     * @param field
     *            field name
     * @param language
     *            language code
     * @param country
     *            country code
     * @return {@code Update} object
     * @throws Exception
     */
    public Update deleteIstring(Enum<?> field, Locale.LANGUAGE language, Locale.COUNTRY country) throws Exception {
        if (!field.getClass().equals(clazz)) {
            throw new Exception("UpdateMap.deleteIstring(): Class missmatch!");
        }
        Update u = new Update(Type.ISTRING_DELETE, new IstringDummy(null, language, country));
        map.put(field, u);
        return u;
    }

    /**
     * Adds an {@code Image} update.
     * 
     * @param field
     *            field name
     * @param imap
     *            map containing the changes of an {@code Image} object. @see
     *            {@link at.treedb.ci.Image}
     * @return {@code Update} object
     * @throws Exception
     */
    public Update addImage(Enum<?> field, UpdateMap imap) throws Exception {
        if (!field.getClass().equals(clazz)) {
            throw new Exception("UpdateMap.addImage(): Class missmatch!");
        }
        Update u = new Update(Type.IMAGE, imap);
        map.put(field, u);
        return u;
    }

    /**
     * Adds an dummy image
     * 
     * @param field
     *            field name
     * @param imageDummy
     *            dummyImage
     * @return {@code Update} object
     * @throws Exception
     */
    public Update addImageDummy(Enum<?> field, ImageDummy imageDummy) throws Exception {
        if (!field.getClass().equals(clazz)) {
            throw new Exception("UpdateMap.addImage(): Class missmatch!");
        }
        Update u = new Update(Type.IMAGE_DUMMY, imageDummy);
        map.put(field, u);
        return u;
    }

    /**
     * Checks if the update affects member variables, or only referenced data
     * are updated.
     * 
     * @return {@code true} update of the entity, {@code false} only referenced
     *         data are updated
     */
    public boolean isEntityUpdate() {
        for (Update u : map.values()) {
            switch (u.getType()) {
            case ISTRING:
            case IMAGE:
            case LAZY_BINARY:
                continue;
            default:
                return true;
            }
        }
        return false;
    }

    /**
     * Adds an {@code Image} removal update.
     * 
     * @param field
     *            file name
     * @return {@code Update} object
     * @throws Exception
     */
    public Update deleteImage(Enum<?> field) throws Exception {
        if (!field.getClass().equals(clazz)) {
            throw new Exception("UpdateMap.deleteImage(): Class missmatch!");
        }
        Update u = new Update(Type.IMAGE_DELETE, null);
        map.put(field, u);
        return u;
    }

    /**
     * Add a generic update
     * 
     * @param field
     *            field name
     * @param obj
     *            update value
     * @return {@code Update} object
     * @throws Exception
     */
    public Update add(Enum<?> field, Object obj) throws Exception {
        if (!field.getClass().equals(clazz)) {
            throw new Exception("UpdateMap.add(): Class missmatch!");
        }
        Update u = null;
        if (obj instanceof String) {
            u = new Update(Type.STRING, (String) obj);
        } else if (obj instanceof Integer) {
            u = new Update(Type.INT, (Integer) obj);
        } else if (obj instanceof Long) {
            u = new Update(Type.LONG, (Long) obj);
        } else if (obj instanceof Float) {
            u = new Update(Type.FLOAT, (Float) obj);
        } else if (obj instanceof Double) {
            u = new Update(Type.DOUBLE, (Double) obj);
        } else if (obj instanceof Boolean) {
            u = new Update(Type.BOOLEAN, (Boolean) obj);
        } else if (obj instanceof Date) {
            u = new Update(Type.DATE, (Date) obj);
        } else if (obj instanceof byte[]) {
            u = new Update(Type.BINARY, (byte[]) obj);
        } else if (obj instanceof Enum) {
            u = new Update(Type.ENUM, (Enum<?>) obj);
        } else if (obj instanceof BigDecimal) {
            u = new Update(Type.BIGDECIMAL, (BigDecimal) obj);
        } else {
            throw new Exception("Not supported type " + obj.getClass().getName());
        }
        map.put(field, u);
        return u;
    }

}
