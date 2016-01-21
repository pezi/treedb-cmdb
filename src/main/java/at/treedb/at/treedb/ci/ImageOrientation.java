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
package at.treedb.ci;

public enum ImageOrientation {
    NOT_AVAILABLE(0), TOP(1), BOTTOM(3), RIGHT_SIDE(6), LEFT_SIDE(8);
    private int orientation;

    ImageOrientation(int orientation) {
        this.orientation = orientation;
    }

    public int getOrientation() {
        return orientation;
    }

    public boolean isSideLying() {
        if (orientation == 6 || orientation == 8) {
            return true;
        }
        return false;
    }

    public int getRotationAngle() {
        switch (orientation) {
        case 0:
        case 1:
            return 0;
        case 3:
            return 180;
        case 8:
            return 270;
        case 6:
            return 90;
        }
        return 0;
    }
}
