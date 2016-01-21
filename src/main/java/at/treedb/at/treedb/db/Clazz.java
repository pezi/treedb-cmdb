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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.Table;

import at.treedb.domain.Domain;
import at.treedb.user.User;
import at.treedb.util.FileAccess;

/**
 * <p>
 * Container to store Java classes.
 * </p>
 * 
 * @author Peter Sauer
 */

@Entity
@Table(indexes = { @Index(columnList = "name"), @Index(columnList = "histId") })
public class Clazz extends Base implements Cloneable {
    private static final long serialVersionUID = 1L;

    public enum Fields {
        name, data, source
    }

    @Column(nullable = false)
    private String name; // full qualified class name
    // binary data
    @Lob
    @Detach
    @Column(nullable = false, length = 5242880)
    private byte[] data; // binary class data

    @Lob
    private String source; // Java source

    protected Clazz() {
    }

    private Clazz(String name, String source, byte[] data) {
        this.setHistStatus(STATUS.ACTIVE);
        this.name = name;
        this.source = source;
        this.data = data;
    }

    /**
     * Reads all classes/(optional) sources of a Java package.
     * 
     * @param classdir
     *            path of the directory containing the binary classes
     * @param sourceDir
     *            path of the directory containing the Java sources, optional
     *            parameter, can be {@code null}
     * @param packageName
     *            full qualified package name
     * @return
     * @throws Exception
     */
    public static ArrayList<ClazzDummy> readClasses(FileAccess fileAccess, String classDir, String sourceDir,
            String packageName) throws Exception {
        ArrayList<ClazzDummy> list = new ArrayList<ClazzDummy>();
        readClasses(fileAccess, list, classDir, sourceDir, packageName);
        return list;
    }

    private static void readClasses(FileAccess fileAccess, ArrayList<ClazzDummy> list, String classDir,
            String sourceDir, String packageName) throws Exception {
        if (!classDir.endsWith("/")) {
            classDir += "/";
        }
        if (sourceDir != null && !sourceDir.endsWith("/")) {
            sourceDir += "/";
        }
        classDir += packageName.replace('.', '/');
        String[] flist = fileAccess.list(classDir);
        if (flist == null) {
            return;
        }
        for (String f : flist) {
            if (f.endsWith(".class")) {
                String clazzName = f.substring(f.lastIndexOf("/") + 1, f.lastIndexOf('.'));

                String source = null;
                if (sourceDir != null && !clazzName.contains("$")) {
                    String srcPath = sourceDir + classDir + "/" + clazzName + ".java";
                    if (fileAccess.exists(srcPath)) {
                        source = fileAccess.getText(srcPath);
                    }
                }
                list.add(new ClazzDummy(packageName + "." + clazzName, source, fileAccess.getData(f)));
            }
        }
    }

    /**
     * Creates a Java class object.
     * 
     * @param domain
     *            {@code Domain} of the data element
     * @param user
     *            creator of the {@code Clazz}
     * @param name
     *            full qualified class name
     * @param source
     *            source file
     * @param data
     *            binary class data
     * @return {@code Clazz} object
     * @throws Exception
     */
    static public Clazz create(Domain domain, User user, String name, String source, byte[] data) throws Exception {
        Clazz clazz = new Clazz(name, source, data);
        Base.save(null, domain, user, clazz);
        return clazz;
    }

    @Override
    public Object checkConstraints(DAOiface dao, UpdateMap update) throws Exception {
        Base.checkConstraintPerDomain(dao, update, getDomain(), getHistId(), Clazz.Fields.name, getName());
        return null;
    }

    /**
     * Creates a {@code Clazz}.
     * 
     * @param domain
     *            {@code Domain} of the image
     * @param user
     *            creator of the {@code Class}
     * @param name
     *            full qualified class name
     * @param data
     *            binary class data
     * @return {@code Clazz} object
     * @throws Exception
     */
    static public Clazz create(DAOiface dao, Domain domain, User user, String name, String source, byte[] data)
            throws Exception {
        Clazz clazz = new Clazz(name, source, data);
        Base.save(dao, domain, user, clazz);
        return clazz;
    }

    /**
     * Loads a {@code Clazz}.
     * 
     * @param id
     *            ID of the {@code Clazz}
     * @return {@code Clazz} object
     * @throws Exception
     */
    public static Clazz load(DAOiface dao, @DBkey(Clazz.class) int id) throws Exception {
        return (Clazz) Base.load(dao, Clazz.class, id, null);
    }

    /**
     * Deletes an {@code Clazz} from DB explicit without historization.
     * 
     * @param user
     *            user who deletes the {@code Clazz}
     * @param image
     *            {@code Clazz} to be deleted
     * 
     * @throws Exception
     */
    public static void dbDelete(User user, Clazz clazz) throws Exception {
        Base.delete(user, clazz, true);
    }

    /**
     * Deletes an {@code Clazz} from DB explicit without historization.
     * 
     * @param user
     *            user who deletes the {@code Clazz}
     * @param clazz
     *            ID of the {@code Clazz}
     * @throws Exception
     */
    public static void dbDelete(User user, @DBkey(Clazz.class) int clazz) throws Exception {
        Base.delete(user, clazz, Clazz.class, true);
    }

    /**
     * Deletes an {@code Clazz}.
     * 
     * @param user
     *            user who deletes the {@code Clazz}
     * @param image
     *            {@code Clazz} to be deleted
     * @throws Exception
     */
    public static void delete(User user, Clazz clazz) throws Exception {
        Base.delete(user, clazz, false);
    }

    /**
     * Deletes an {@code Clazz}.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param user
     *            user who deletes the {@code Clazz}
     * @param id
     *            ID of the {@code Clazz}
     * @throws Exception
     */
    public static void delete(DAOiface dao, User user, @DBkey(Clazz.class) int id) throws Exception {
        Base.delete(dao, user, id, Clazz.class, false);
    }

    /**
     * Deletes an {@code Clazz}.
     * 
     * @param user
     *            user who deletes the {@code Clazz}
     * @param id
     *            ID of the {@code Clazz}
     * @throws Exception
     */
    public static void delete(User user, int id) throws Exception {
        Base.delete(user, id, Clazz.class, false);
    }

    /**
     * Updates an {@code Clazz}.
     * 
     * @param user
     *            {@code User} who performs the update
     * 
     * @param map
     *            map of changes
     * @throws Exception
     */
    public void update(User user, UpdateMap map) throws Exception {
        Base.update(user, this, map);
    }

    /**
     * Updates an {@code Clazz}.
     * 
     * @param user
     *            {@code User} who performs the update
     * @param clazz
     *            {@code Clazz} to be updated
     * @param map
     *            map of changes
     * @throws Exception
     */
    public static void update(User user, Clazz clazz, UpdateMap map) throws Exception {
        Base.update(user, clazz, map);
    }

    /**
     * Updates an {@code Clazz}.
     * 
     * @param user
     *            user {@code User} who performs the update
     * @param id
     *            {@code Clazz} ID
     * @param map
     *            map of changes
     * @throws Exception
     */
    public static void update(User user, @DBkey(Clazz.class) int id, UpdateMap map) throws Exception {
        Base.update(user, id, Clazz.class, map);
    }

    /**
     * Updates an {@code Clazz}.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param user
     *            {@code User} who performs the update
     * @param id
     *            class ID
     * @param map
     *            map of changes
     * @throws Exception
     */
    public static void update(DAOiface dao, User user, @DBkey(Clazz.class) int id, UpdateMap map) throws Exception {
        Base.update(dao, user, id, Clazz.class, map);
    }

    /**
     * Returns the full qualified class name.
     * 
     * @return image name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the binary class data.
     * 
     * @return binary data of the class
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Returns the optional Java source.
     * 
     * @return Java source
     */
    public String getSource() {
        return source;
    }

    /**
     * Counts the {@code Clazz} entities/DB table rows.
     * 
     * @param status
     *            filter representing the user's historization status.
     *            <code>null<null> counts all user DB entries.
     * @return number of images
     * @throws Exception
     */
    public static long rowCount(HistorizationIface.STATUS status) throws Exception {
        return Base.countRow(null, Clazz.class, status, null);
    }

    /**
     * Searches a class.
     * 
     * @param fields
     *            image property to be searched
     * @param value
     *            property/search value
     * @param flags
     *            search flags
     * @return list of {@code Image} objects
     * @throws Exception
     */
    static public List<Base> search(Domain domain, EnumSet<Clazz.Fields> fields, String value,
            EnumSet<Base.Search> flags, SearchLimit limit) throws Exception {
        return Base.search(domain, Clazz.class, fields, value, null, flags, limit, false);
    }

    /**
     * Loads an class by its full qualified name
     * 
     * @param domain
     *            {@code Domain} of the class
     * @param name
     *            full qualified name
     * @return {@code Clazz} object
     * @throws Exception
     */
    static public Clazz load(Domain domain, String name) throws Exception {
        List<Base> list = Base.search(domain, Clazz.class, EnumSet.of(Clazz.Fields.name), name, null, null, null,
                false);
        if (list == null || list.size() != 1) {
            return null;
        }
        return (Clazz) list.get(0);
    }

    static public Clazz load(DAOiface dao, Domain domain, String name) throws Exception {
        List<Base> list = Base.search(dao, domain, Clazz.class, EnumSet.of(Clazz.Fields.name), name, null, null, null,
                false);
        if (list == null || list.size() != 1) {
            return null;
        }
        return (Clazz) list.get(0);
    }

    @Override
    public ClassID getCID() {
        return ClassID.CLAZZ;
    }

}
