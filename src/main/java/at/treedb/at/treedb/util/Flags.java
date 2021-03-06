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

import java.util.EnumSet;

public class Flags {
    public static long toBitMask(EnumSet<?> set) {
        long mask = 0L;
        if (set != null) {
            for (Enum<?> e : set) {
                mask |= (1 << e.ordinal());
            }
        }
        return mask;
    }
}
