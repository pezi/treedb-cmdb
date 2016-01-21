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
 * Annotation to show the relationship between a DB ID and the referenced
 * persisted class. This annotation is used for exporting data as a TDEF (TreeDB
 * Exchange Format) file.
 * </p>
 * Example - {@code createdBy} is the DB ID of an {@code User} entity <code>
 * 	&#64;DBkey(value = User.class)
	private int createdBy;
 * </code>
 * 
 * @author Peter Sauer
 * 
 */
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface DBkey {
    Class<?> value();
}
