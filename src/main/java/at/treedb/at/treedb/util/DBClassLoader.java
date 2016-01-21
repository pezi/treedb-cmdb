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

import at.treedb.db.Clazz;
import at.treedb.domain.Domain;

/**
 * <p>
 * Custom class loader for
 * </p>
 * 
 * @author Peter Sauer
 * 
 */
public class DBClassLoader extends ClassLoader {

    private Domain domain;
    private HashMap<String, Class<?>> cache;

    public DBClassLoader(Domain domain) {
        this.domain = domain;
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        Clazz cl = null;
        if (cache != null && cache.containsKey(name)) {
            return cache.get(name);
        }
        try {
            return Class.forName(name);
        } catch (Exception e) {
        }
        try {
            cl = Clazz.load(domain, name);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ClassNotFoundException();
        }
        if (cl == null) {
            throw new ClassNotFoundException();
        }
        Class<?> c = defineClass(name, cl.getData(), 0, cl.getData().length);
        if (cache == null) {
            cache = new HashMap<String, Class<?>>();
        }
        cache.put(name, c);
        return c;

    }

}
