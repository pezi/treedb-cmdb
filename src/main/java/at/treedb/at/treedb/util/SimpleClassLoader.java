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

import java.util.HashMap;

/**
 * <p>
 * Simple class loader.
 * </p>
 * 
 * @author Peter Sauer
 *
 */
public class SimpleClassLoader extends ClassLoader {
    // class map
    private HashMap<String, Class<?>> classMap;

    /**
     * Constructor
     */
    public SimpleClassLoader() {
        classMap = new HashMap<String, Class<?>>();
    }

    /**
     * Stores a class.
     * 
     * @param name
     *            full qualified class name
     * @param data
     *            binary class data
     */
    public void putClass(String name, byte[] data) {
        classMap.put(name, defineClass(name, data, 0, data.length));
    }

    /**
     * Removes a class form the internal map.
     * 
     * @param name
     *            full qualified class name
     */
    public void removeClass(String name) {
        classMap.remove(name);
    }

    /**
     * If the class is not available in the internal storage
     * <code>Class.forName()</code> will be called.
     */
    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> clazz = classMap.get(name);
        if (clazz != null) {
            return clazz;
        }
        return Class.forName(name);
    }
}
