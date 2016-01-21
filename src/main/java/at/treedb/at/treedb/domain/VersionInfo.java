/*
 * (C) Copyright 2014,215 Peter Sauer (http://treedb.at/).
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

import javax.persistence.Entity;

import at.treedb.db.Base;
import at.treedb.db.ClassID;
import at.treedb.db.DAOiface;
import at.treedb.user.User;

/**
 * <p>
 * Version info of a {@code Domain}/DB.
 * </p>
 * 
 * @author Peter Sauer
 *
 */
@SuppressWarnings("serial")
@Entity
public class VersionInfo extends Base {
    public enum CodeMaturity {
        PRODUCTION, INTEGRATION, TEST, DEVELOPMENT, ALPHA_VERSION, BETA_VERSION
    }

    private String versionString;
    // code level maturity
    private CodeMaturity codeMaturity;
    // code changes of this version
    private String changes;

    protected VersionInfo() {
    }

    /**
     * Creates a {@code VersionInfo}.
     * 
     * @param versionString
     *            version string, e.g. 0.1.2
     * @param codeMaturity
     *            additional info about the code
     * @param changes
     *            code changes
     */
    public VersionInfo(String versionString, CodeMaturity codeMaturity, String changes) {
        this.versionString = versionString;
        this.codeMaturity = codeMaturity;
        this.changes = changes;
    }

    /**
     * Returns the code maturity level.
     * 
     * @return code maturity level
     */
    public CodeMaturity getCodeMaturityLevel() {
        return codeMaturity;
    }

    /**
     * Returns a description of the code changes of this version.
     * 
     * @return code changes
     */
    public String getChanges() {
        return changes;
    }

    /**
     * Creates a {@code VersionInfo} object.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param domain
     *            {@code Domain} which is associated with this version
     * @param user
     *            user who performs the {@code VersionInfo} creation
     * @param versionString
     *            version string, e.g. 0.1.2
     * @param codeMaturity
     *            additional info about the code
     * @param changes
     *            code changes
     * @return {@code VersionInfo} object
     * @throws Exception
     */
    public static VersionInfo create(DAOiface dao, Domain domain, User user, String versionString,
            CodeMaturity codeMaturity, String changes) throws Exception {
        VersionInfo version = new VersionInfo(versionString, codeMaturity, changes);
        Base.save(dao, domain, user, version);
        return version;
    }

    @Override
    public ClassID getCID() {
        return ClassID.VERSIONINFO;
    }

}
