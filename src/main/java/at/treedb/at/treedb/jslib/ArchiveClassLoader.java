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

package at.treedb.jslib;

import java.io.File;
import java.net.URL;
import java.util.HashMap;

import at.treedb.db.DAO;
import at.treedb.db.DAOiface;
import at.treedb.util.FileStorage;
import at.treedb.util.Stream;

/**
 * <p>
 * Custom class loader for loading Java code. from a <code>JSlib</code> archive.
 * This Java classes serve as a bridge between Vaadin and the external
 * JavaScript library.
 * </p>
 * 
 * @author Peter Sauer
 * 
 */
public class ArchiveClassLoader extends ClassLoader {
    private String libName; // l
    private String version;
    private String libID;
    private DAOiface dao;
    private static HashMap<String, HashMap<String, Class<?>>> cache = new HashMap<String, HashMap<String, Class<?>>>();;
    private JsLib lib;

    public ArchiveClassLoader(DAOiface dao, String jsLib, String version) {
        this.dao = dao;
        this.libName = jsLib;
    }

    public static void clearCache(String name, String version) {
        cache.remove(name + version);
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        if (lib == null) {
            try {
                lib = JsLib.load(dao, libName, version);
                libID = lib.getName() + lib.getVersion();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        if (lib == null) {
            throw new ClassNotFoundException();
        }
        HashMap<String, Class<?>> subCache = cache.get(libID);
        if (subCache != null) {
            if (subCache.containsKey(name)) {
                return subCache.get(name);
            }
        }
        byte[] clazzData = null;
        try {
            String path = libName + "/java/classes/" + name.replace(".", "/") + ".class";
            clazzData = lib.getArchiveEntry(path);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ClassNotFoundException();
        }
        if (clazzData == null) {
            return Class.forName(name);
        } else {
            Class<?> c = defineClass(name, clazzData, 0, clazzData.length);
            if (subCache == null) {
                subCache = new HashMap<String, Class<?>>();
                cache.put(libID, subCache);
            }
            subCache.put(name, c);
            return c;
        }
    }

    @Override
    public URL findResource(String name) {
        try {
            String path = libName + "/java/classes/" + name;
            byte[] resource = lib.getArchiveEntry(path);
            if (resource == null) {
                return null;
            }
            // shorten the path
            int pos = name.lastIndexOf("/");
            if (pos > 0) {
                name = name.substring(pos + 1);
            }
            String prefix;
            String postfix;
            pos = name.lastIndexOf(".");
            if (pos > 0) {
                prefix = name.substring(0, pos);
                postfix = name.substring(pos);
            } else {
                prefix = name;
                postfix = "";
            }
            File tempFile = FileStorage.getInstance().createTempFile(prefix, postfix);
            Stream.writeByteStream(tempFile, resource);
            return new URL("file:///" + tempFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

}
