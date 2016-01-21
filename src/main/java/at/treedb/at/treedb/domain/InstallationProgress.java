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
package at.treedb.domain;

/**
 * <p>
 * Interface for attending the installation of a domain.
 * </p>
 * 
 * @author Peter Sauer
 *
 */
public interface InstallationProgress {
    /**
     * Message level
     */
    enum Type {
        MSG, WRN, ERR
    };

    /**
     * Writes an installation message including a new line.
     * 
     * @param type
     *            message type
     * @param message
     *            message text
     */
    public void writeln(Type type, String message);

    /**
     * Writes an installation message including a new line.
     * 
     * @param type
     *            message type
     * @param message
     *            message text
     */
    public void write(Type type, String message);

    /**
     * Updates the installation progress.
     * 
     * @param stage
     *            installation stage
     * @param total
     *            number of installation stages
     */
    public void updateProgress(int stage, int total);
}
