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

/**
 * List of supported serialization methods.
 * 
 * @author Peter Sauer
 *
 */
public enum Serialization {
    /**
     * GSON (http://code.google.com/p/google-gson/)
     */
    JSON,
    /**
     * Xstream (http://xstream.codehaus.org/)
     */
    XML,
    /**
     * Java object serialization - do no use it! Any change of a Java class can
     * break backward compatibility to older serialized objects!
     */
    BINARY
}
