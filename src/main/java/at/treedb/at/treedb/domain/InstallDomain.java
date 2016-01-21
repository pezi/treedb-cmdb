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

import java.io.File;
import java.io.IOException;

import at.treedb.backup.DomainInstallationIface;

import at.treedb.ci.CI;
import at.treedb.ci.ImageDummy;
import at.treedb.ci.MimeType;
import at.treedb.db.DAOhelper;
import at.treedb.db.UpdateCIdata;
import at.treedb.i18n.IstringDummy;
import at.treedb.i18n.Locale;
import at.treedb.user.User;
import at.treedb.util.FileAccess;
import at.treedb.util.SevenZip;

/**
 * Helper class to install a {@code Domain).
 * 
 * @author Peter Sauer
 *
 */
public class InstallDomain {
    private User user;
    private String baseDir;
    private String archivePath;
    private SevenZip sevenZip;
    private FileAccess fileAccess;
    private InstallationProgress progress;
    private boolean isArchive;

    /**
     * Constructor
     * 
     * @param user
     *            {@code User} who installs a domain
     * @param path
     */
    public InstallDomain(User user, String path, boolean isArchvie) {
        this.user = user;
        this.isArchive = isArchvie;
        if (isArchvie) {
            this.baseDir = null;
            this.archivePath = path;
        } else {
            this.baseDir = path;
            this.archivePath = null;
        }

    }

    /**
     * I
     * 
     * @param archiveDir
     * @param iface
     *            interface to display the extraction progress
     * @param progress
     * @throws IOException
     */
    public void initDataAccess(String archiveDir, DomainInstallationIface iface, InstallationProgress progress)
            throws Exception {
        if (getArchivePath() != null) {
            this.progress = progress;
            sevenZip = new SevenZip(new File(getArchivePath()), iface);
            if (!archiveDir.endsWith("/")) {
                archiveDir += "/";
            }
            fileAccess = new FileAccess(sevenZip, archiveDir);
        } else {
            fileAccess = new FileAccess(getBaseDir());
        }
    }

    /**
     * Returns the {@code FileAccess} object.
     * 
     * @return {@code FileAccess} object
     */
    public FileAccess getFileAccess() {
        return fileAccess;
    }

    /**
     * Returns the {@code User}.
     * 
     * @return {@code User}
     */
    public User getUser() {
        return user;
    }

    /**
     * Returns the base directory.
     * 
     * @return base directory
     */
    public String getBaseDir() {
        return baseDir;
    }

    /**
     * Returns the archive path.
     * 
     * @return archive path
     */
    public String getArchivePath() {
        return archivePath;
    }

    /**
     * Writes an internal message.
     * 
     * @param type
     *            message type
     * @param message
     *            text message
     */
    public void writeInternalMessage(InstallationProgress.Type type, String message) {
        if (progress != null) {
            progress.write(type, message);
        } else {
            System.out.println(message);
        }

    }

    protected void writeInstallMsg(String message) {
        if (progress != null) {
            progress.writeln(InstallationProgress.Type.MSG, message);
        } else {
            System.out.println(message);
        }
    }

    protected void writeInstallLine(InstallationProgress.Type type, String message) {
        if (progress != null) {
            progress.write(type, message);
        } else {
            System.out.println(message);
        }
    }

    protected void updateProgress(int actual, int total) {
        if (progress != null) {
            progress.updateProgress(actual, total);
        }
    }

    protected void createWIKInode(Domain domain, String ciName, DAOhelper daoHelper, Locale.LANGUAGE[] otherLang)
            throws Exception {
        CI ci = domain.getCI(ciName);
        if (ci != null) {
            String path = ci.getName() + ".txt";
            if (fileAccess.exists(path)) {
                UpdateCIdata up = new UpdateCIdata(ci, domain.getLanguage());
                up.addIString("content", new IstringDummy(fileAccess.getText(path), Locale.LANGUAGE.de));
                for (Locale.LANGUAGE l : otherLang) {
                    path = ci.getName() + "_" + l.name() + ".txt";
                    if (fileAccess.exists(path)) {
                        up.addIString("content", new IstringDummy(fileAccess.getText(path), l));
                    }
                }
                path = ci.getName() + ".png";
                if (getFileAccess().exists(path)) {
                    up.addImage("images",
                            new ImageDummy(ci.getName(), fileAccess.getData(path), MimeType.PNG, null, null));
                }
                daoHelper.saveOrUpdateCIdata(ci, up);
            }
        }
    }
}
