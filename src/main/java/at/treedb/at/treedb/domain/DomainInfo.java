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

import java.util.HashMap;

import com.thoughtworks.xstream.XStream;

import at.treedb.i18n.Locale;

/**
 * <p>
 * Helper class to store
 * </p>
 * 
 * @author Peter Sauer
 *
 */
public class DomainInfo {
    private String dbName;
    private String installationClass;
    private HashMap<Locale.LANGUAGE, String> descriptionMap;
    private boolean hasDemoData;
    private String versionNumber;

    public DomainInfo() {

    }

    public DomainInfo(String dbName, String versionNumber, String installationClass, boolean hasDemoData,
            HashMap<Locale.LANGUAGE, String> descriptionMap) {
        this.dbName = dbName;
        this.installationClass = installationClass;
        this.descriptionMap = descriptionMap;
        this.hasDemoData = hasDemoData;
    }

    public String getDBname() {
        return dbName;
    }

    public String getVersionNumer() {
        return versionNumber;
    }

    public static DomainInfo deserialize(String xml) {
        XStream xstream = new XStream();
        return (DomainInfo) xstream.fromXML(xml);
    }

    public String getDescription(Locale.LANGUAGE lang) {
        return descriptionMap.get(lang);
    }

    public String getInstallationClass() {
        return installationClass;
    }

    public boolean hasDemoData() {
        return hasDemoData;
    }
}
