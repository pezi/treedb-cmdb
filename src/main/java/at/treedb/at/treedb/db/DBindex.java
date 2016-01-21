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

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;

/**
 * <p>
 * Annotation for defining DB indexes in an abstract class which is annotated
 * with {@code @javax.persistence.MappedSuperclass}. This annotation should help
 * to solve following problems.
 * </p>
 * <p>
 * <ul>
 * <li>There is no standard JPA way for propagate index definitions from the
 * abstract class to subclasses</li>
 * <li>This annotation is used by the {@code ChangeAnnotation} class.</li>
 * <li>For an abstract class annotated with
 * {@code @javax.persistence.MappedSuperclass} a combination with
 * {@code @javax.persistence.Table} is not possible for some JPA implementation.
 * e.g. OpenJPA</li>
 * </ul>
 * </p>
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface DBindex {
    String[] columnList();
}
