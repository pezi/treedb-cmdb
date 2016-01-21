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

import at.treedb.domain.Domain;
import at.treedb.domain.InstallationProgress;
import at.treedb.domain.InstallationProgress.Type;

/**
 * <p>
 * Interface for installing a domain.
 * </p>
 * 
 * @author Peter Sauer
 *
 */
public interface DomainInstallationIface {
    public String getName();

    /**
     * Returns the UUID of the domain.
     * 
     * @return UUID
     */
    public String getUUID();

    public Domain install(String dbName, boolean hasDemoData, InstallationProgress progress) throws Exception;

    /**
     * Returns a list of installation prerequisites.
     * 
     * @return installation prerequisites
     */
    public InstallationPrerequisite[] getInstallationPrerequisites();

    /**
     * Writes a message to the installation console
     * 
     * @param type
     *            message type
     * @param msg
     *            message
     */
    public void writeInternalMessage(Type type, String msg);

    /**
     * Returns {@code true} if test data are available, {@code false} if not.
     * 
     * @return test data available
     */
    public boolean hasTestData();

    /**
     * Returns {@code true} if REST support is available, {@code false} if not.
     * 
     * @return extended REST support available
     */
    public boolean hasRESTsupport();

    /**
     * Returns {@code true} if extended HTTP support is available, {@code false}
     * if not.
     * 
     * @return extended HTTP support available
     */
    public boolean hasHTTPsupport();

    /**
     * Return the number of installation stages.
     * 
     * @return installation stages
     */
    public int installationStages();
}
