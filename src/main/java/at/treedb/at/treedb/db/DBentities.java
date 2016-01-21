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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

/**
 * <p>
 * List of persisted entities.
 * </p>
 * 
 * @author Peter Sauer
 * 
 */
// LastReview: 17.02.2014
public class DBentities {
    // Hint: CIfile MUST be before DBfile to meet the correct data import order
    private static final Class<?>[] entities = { at.treedb.i18n.Istring.class, at.treedb.user.User.class,
            at.treedb.user.Group.class, at.treedb.user.Permissions.class, at.treedb.user.Membership.class,
            at.treedb.user.Tenant.class, at.treedb.db.Event.class, at.treedb.domain.Domain.class,
            at.treedb.domain.DBcategory.class, at.treedb.domain.DBcategoryMembership.class,
            at.treedb.domain.VersionInfo.class, at.treedb.i18n.SupportedLanguage.class, at.treedb.jslib.JsLib.class,
            at.treedb.db.Base.class, at.treedb.db.DBinfo.class, at.treedb.db.CacheEntry.class,
            at.treedb.ci.CItype.class, at.treedb.ci.CI.class, at.treedb.ci.Node.class, at.treedb.ci.Image.class,
            at.treedb.db.Clazz.class, at.treedb.ci.CIstring.class, at.treedb.ci.CIdata.class,
            at.treedb.ci.CIboolean.class, at.treedb.ci.CIdouble.class, at.treedb.ci.CIblob.class,
            at.treedb.ci.Blob.class, at.treedb.ci.CIdate.class, at.treedb.ci.CIi18nString.class,
            at.treedb.ci.CIimage.class, at.treedb.ci.CIfile.class, at.treedb.ci.CIlong.class,
            at.treedb.ci.CIbigDecimal.class, at.treedb.ci.KeyValuePair.class,

            at.treedb.ui.UIwikiImage.class, at.treedb.ui.UIfile.class, at.treedb.ui.UImacro.class,
            at.treedb.ui.UItab.class, at.treedb.ui.UItextField.class, at.treedb.ui.UItextArea.class,
            at.treedb.ui.UIelement.class, at.treedb.ui.UIwikiTextArea.class, at.treedb.ui.UIgrouping.class,
            at.treedb.ui.UIgroupingEnd.class, at.treedb.ui.UIdateField.class, at.treedb.ui.UIcheckbox.class,
            at.treedb.ui.UIslider.class, at.treedb.ui.UIselect.class, at.treedb.ui.UIoption.class,
            at.treedb.ui.UIblob.class, at.treedb.dbfs.DBfile.class, at.treedb.dbfs.DBFSblock.class

    };

    private static String[] entitiesList = null;

    /**
     * Returns a list of Hibernate/JPA annotated classes as string.
     * 
     * @return class name list
     * @throws Exception
     */
    public static synchronized String[] getClassesAsList() throws Exception {
        if (entitiesList == null) {
            ArrayList<String> clist = new ArrayList<String>();
            for (Class<?> c : entities) {
                clist.add(c.getName());
            }
            entitiesList = clist.toArray(new String[clist.size()]);
        }
        return entitiesList;
    }

    /**
     * Returns a list of Hibernate/JPA annotated classes.
     * 
     * @return class list
     */
    public static Class<?>[] getClasses() {
        return entities;
    }

    static {
        try {
            loop: for (Class<?> c : entities) {

                if (!Modifier.isAbstract(c.getModifiers())) {
                    Class<?> s = c;
                    while (true) {
                        s = s.getSuperclass();
                        if (s == null) {
                            continue loop;
                        }
                        if (s.equals(Base.class)) {
                            break;
                        }
                    }

                    for (Field f : c.getDeclaredFields()) {
                        if (f.getAnnotation(LazyLoad.class) != null || f.getAnnotation(Callback.class) != null) {
                            Method method = c.getMethod("addCallbackUpdateField", Class.class, String.class);
                            Object o = method.invoke(null, c, f.getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
