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
package at.treedb.backup;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import at.treedb.ci.CI;
import at.treedb.db.ClassSelector;
import at.treedb.db.DBentities;
import at.treedb.db.DBkey;

/**
 * <p>
 * Helper class for building an entity relationship tree by analyzing the
 * {@code DBkey} annotations.
 * </p>
 * 
 * @author Peter Sauer
 *
 */
// LDOR: 17.08.2014
public class ClassDependency {
    private Class<?> clazz;
    // children = this class references other classes
    private HashSet<Class<?>> children;
    // parents = classes which reference this class
    private HashSet<Class<?>> parents;
    private boolean isAbstract;
    private boolean selfReference;

    /**
     * Constructor
     * 
     * @param clazz
     *            entity class
     */
    public ClassDependency(Class<?> clazz) {
        this.clazz = clazz;
        children = new HashSet<Class<?>>();
        parents = new HashSet<Class<?>>();
        isAbstract = Modifier.isAbstract(clazz.getModifiers());
    }

    /**
     * Returns the class of the entity.
     * 
     * @return class of the entity
     */
    public Class<?> getEntity() {
        return clazz;
    }

    /**
     * Returns if the class is abstract.
     * 
     * @return {@code true} if the class is abstract, {@code false} if not
     */
    public boolean isAbstract() {
        return isAbstract;
    }

    /**
     * Sets the self-reference flag.
     * 
     * @param self
     *            {@code true} if the class has a self-reference, {@code false}
     *            if not
     */
    public void setSelfReference(boolean self) {
        selfReference = self;
    }

    /**
     * Returns the self-reference flag.
     * 
     * @return {@code true} if the class has a self-reference, {@code false} if
     *         not
     */
    public boolean isSelfReference() {
        return selfReference;
    }

    /**
     * Adds a child class.
     * 
     * @param child
     *            child class
     */
    public void addChild(Class<?> child) {
        children.add(child);
    }

    /**
     * Adds a parent class.
     * 
     * @param parent
     *            parent class
     */
    public void addParent(Class<?> parent) {
        parents.add(parent);
    }

    /**
     * Returns the child classes.
     * 
     * @return child classes
     */
    public HashSet<Class<?>> getChildren() {
        return children;
    }

    /**
     * Returns the parent classes.
     * 
     * @return parent classes
     */
    public HashSet<Class<?>> getParents() {
        return parents;
    }

    /**
     * Returns an entity relationship tree.
     * 
     * @return entity relationship tree
     */
    public static HashMap<Class<?>, ClassDependency> getDependencyTree() {
        HashMap<Class<?>, ClassDependency> map = new HashMap<Class<?>, ClassDependency>();
        ArrayList<Class<?>> classList = new ArrayList<Class<?>>();
        for (Class<?> c : DBentities.getClasses()) {
            classList.add(c);
        }
        classList.add(ClassSelector.class);

        for (Class<?> c : classList) {
            map.put(c, new ClassDependency(c));
        }
        for (Class<?> c : classList) {
            ClassDependency cd = map.get(c);
            ArrayList<Field> list = getAllFields(c);
            for (Field f : list) {
                Annotation a = f.getAnnotation(DBkey.class);
                if (a != null) {
                    Class<?> refClazz = f.getAnnotation(DBkey.class).value();
                    if (refClazz.equals(c)) {
                        cd.setSelfReference(true);
                    } else {
                        cd.addParent(refClazz);
                        map.get(refClazz).addChild(c);
                    }
                }
            }
        }
        return map;
    }

    /**
     * Creates a list of all fields, including fields of super classes.
     * 
     * @param clazz
     *            class
     * @param list
     *            field list
     */
    private static void getAllFields(Class<?> clazz, ArrayList<Field> list) {
        for (Field f : clazz.getDeclaredFields()) {
            f.setAccessible(true);
            list.add(f);
        }
        if (clazz.getSuperclass() != null && !clazz.getSuperclass().equals(Object.class)) {
            getAllFields(clazz.getSuperclass(), list);
        }
    }

    /**
     * Returns all fields, including fields of super classes.
     * 
     * @param clazz
     *            class
     */
    public static ArrayList<Field> getAllFields(Class<?> clazz) {
        ArrayList<Field> list = new ArrayList<Field>();
        getAllFields(clazz, list);
        return list;
    }

    /**
     * Helper method to print the relationship of a class.
     * 
     * @param clazz
     *            class
     */
    public static void printRelationship(Class<?> clazz) {
        HashMap<Class<?>, ClassDependency> map = getDependencyTree();
        ClassDependency cd = map.get(clazz);
        System.out.println(clazz.getSimpleName() + (cd.isSelfReference() ? " [self]" : ""));
        System.out.println("Children:");
        for (Class<?> c : cd.getChildren()) {
            System.out.println(" " + c.getSimpleName() + (map.get(c).isAbstract() ? " [abstract]" : ""));
        }

        System.out.println("Parents:");
        for (Class<?> p : cd.getParents()) {
            System.out.println(" " + p.getSimpleName() + (map.get(p).isAbstract() ? " [abstract]" : ""));
        }
    }

}
