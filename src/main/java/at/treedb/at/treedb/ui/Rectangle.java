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
package at.treedb.ui;

/**
 * <p>
 * Rectangle helper class for grouping input elements in a grid layout.
 * </p>
 * 
 * @author Peter Sauer
 *
 */
public class Rectangle {
    private int xPos;
    private int yPos;
    private int width;
    private int height;

    /**
     * Creates a {@code Rectangle}.
     * 
     * @param x
     *            x position
     * @param y
     *            y position
     * @param width
     *            rectangle width
     * @param height
     *            rectangle height
     */
    public Rectangle(int x, int y, int width, int height) {
        this.xPos = x;
        this.yPos = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Creates a {@code Rectangle} with dimension 1 x 1.
     * 
     * @param x
     *            x position
     * @param y
     *            y position
     */
    public Rectangle(int x, int y) {
        this.xPos = x;
        this.yPos = y;
        this.width = 1;
        this.height = 1;
    }

    /**
     * Returns the x position.
     * 
     * @return x position of the rectangle
     */
    public int getX() {
        return xPos;
    }

    /**
     * Returns the y position.
     * 
     * @return y position of the rectangle
     */
    public int getY() {
        return yPos;
    }

    /**
     * Returns the width of the rectangle.
     * 
     * @return rectangle width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height of the rectangle.
     * 
     * @return rectangle height
     */
    public int getHeight() {
        return height;
    }
}
