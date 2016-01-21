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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;

import javax.persistence.Index;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;

import at.treedb.ci.CIimage;
import at.treedb.db.DBindex;

/**
 * <p>
 * Experimental class to change the {@code Index} annotation at runtime. This
 * code is working only for Hibernate. EclipseLink ignores the annotations
 * generated at runtime.
 * </p>
 * <p>
 * The code collects all {@code Index} annotations over the class inheritance to
 * create a new combined {@code Index} annotation
 * </p>
 * 
 * @author Peter Sauer
 *
 */
public class ChangeDBannotation {

    /**
     * Creates an {@code Index} annotation.
     * 
     * @param columnName
     *            column name
     * @return {@code Index} annotation
     */
    public static Index createIndex(final String columnName) {
        return (Index) Proxy.newProxyInstance(Index.class.getClassLoader(), new Class[] { Index.class },
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        String name = method.getName();
                        if (name.equals("columnList")) {
                            return columnName;
                        } else if (name.equals("unique")) {
                            return new Boolean(false);
                        } else {
                            return "";
                        }
                    }
                });
    }

    /**
     * 
     * @param clazz
     * @param list
     */
    private static void collectIndexes(final Class<?> clazz, ArrayList<String> list) {
        Class<?> s = clazz.getSuperclass();
        Annotation mapped = s.getAnnotation(MappedSuperclass.class);
        if (mapped != null) {
            Annotation a = s.getAnnotation(DBindex.class);
            if (a == null) {
                return;
            }
            DBindex ilist = (DBindex) a;
            for (String index : ilist.columnList()) {
                list.add(index);
            }
            collectIndexes(s, list);
        }
    }

    /**
     * 
     * @param clazz
     * @return
     * @throws ClassNotFoundException
     */
    private static ArrayList<Index> getIndexes(Class<?> clazz) throws ClassNotFoundException {
        Annotation a = Class.forName(clazz.getName()).getAnnotation(Table.class);
        if (a == null) {
            return null;
        }
        ArrayList<String> list = new ArrayList<String>();
        collectIndexes(clazz, list);
        Table t = (Table) a;
        ArrayList<Index> ilist = new ArrayList<Index>();
        HashSet<String> indexHash = new HashSet<String>();
        for (Index i : t.indexes()) {
            if (indexHash.contains(i.columnList())) {
                continue;
            }
            ilist.add(i);
            indexHash.add(i.columnList());
        }
        for (String istr : list) {
            if (indexHash.contains(istr)) {
                continue;
            }
            ilist.add(createIndex(istr));
            indexHash.add(istr);
        }
        return ilist;
    }

    /**
     * 
     * @param clazz
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static void rebuildIndexAnnotations(Class<?> clazz) throws Exception {
        Annotation a = Class.forName(clazz.getName()).getAnnotation(Table.class);
        if (a == null) {
            return;
        }
        ArrayList<Index> ilist = getIndexes(clazz);
        if (!ilist.isEmpty()) {
            Object invocationHandler = Proxy.getInvocationHandler(a);
            Field field = invocationHandler.getClass().getDeclaredField("memberValues");
            field.setAccessible(true);
            LinkedHashMap<String, Object[]> map = (LinkedHashMap<String, Object[]>) field.get(invocationHandler);
            map.put("indexes", ilist.toArray(new Index[ilist.size()]));
            field.set(invocationHandler, map);
        }

    }

    /**
     * 
     * @param clazz
     * @return
     * @throws Exception
     */
    public static String getTableAnnotation(final Class<?> clazz) throws Exception {
        ArrayList<Index> ilist = getIndexes(clazz);
        if (ilist == null) {
            return null;
        }
        if (!ilist.isEmpty()) {
            StringBuffer b = new StringBuffer("@Table(indexes = {");
            int index = 0;
            for (Index i : ilist) {
                if (index > 0) {
                    b.append(",");
                }
                b.append("@Index(columnList = \"");
                b.append(i.columnList());
                b.append("\")");
                ++index;
            }
            b.append("})");
            return b.toString();
        }
        return null;
    }

    public static void test(Class<?> clazz) {
        Annotation a = clazz.getAnnotation(Table.class);

        Table t = (Table) a;
        for (Index i : t.indexes()) {
            System.out.println(i.columnList());
        }
    }

    public static void main(String args[]) throws Exception {
        // test(CIimage.class);
        rebuildIndexAnnotations(CIimage.class);
        test(CIimage.class);
    }

}
