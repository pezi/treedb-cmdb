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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import at.treedb.ci.CI;
import at.treedb.ci.CItype;
import at.treedb.ci.FileDummy;
import at.treedb.ci.ImageDummy;
import at.treedb.i18n.IstringDummy;
import at.treedb.i18n.Locale;
import at.treedb.ui.UIelement;
import at.treedb.ui.UItab;

/**
 * Container for collecting updates of a {@code CI}.
 * 
 * @author Peter Sauer
 * 
 */
public class UpdateCIdata {
    private HashMap<String, Object> data = new HashMap<String, Object>();
    private CItype ciType;
    private CI ci;
    private Locale.LANGUAGE language;
    // update effects a new CI
    private boolean newCI;
    private UItab uiTab;
    private Object dataTabSheetComponent;

    /**
     * Creates an {@code UpdateCIdata} container
     * 
     * @param ci
     *            {@code CI} to be updated
     * @param tab
     *            {@code UItab} associated with this update
     * @param language
     *            actual language
     */
    public UpdateCIdata(CI ci, UItab tab, Locale.LANGUAGE language) {
        this.ci = ci;
        this.uiTab = tab;
        this.ciType = ci.getCItypeObj();
        this.language = language;
    }

    /**
     * Creates an {@code UpdateCIdata} container
     * 
     * @param ci
     *            {@code CI} to be updated
     * 
     * @param language
     *            actual language
     */
    public UpdateCIdata(CI ci, Locale.LANGUAGE language) {
        this(ci, null, language);
    }

    /**
     * Creates an {@code UpdateCIdata} container for a new {@code CI}-
     * 
     * @param ci
     *            {@code CI} to be updated
     * @param language
     *            actual language
     */
    public UpdateCIdata(CItype ciType, UItab tab, Locale.LANGUAGE language) {
        this.ci = null;
        this.ciType = ciType;
        this.uiTab = tab;
        this.language = language;
    }

    /**
     * Sets the {@code CI}
     * 
     * @param ci
     *            {@code CI} object
     */
    public void setCI(CI ci) {
        this.ci = ci;
    }

    public UItab getUItab() {
        return uiTab;
    }

    /**
     * Returns if the update effects a new {@code CI}.
     * 
     * @return {@code true} if the update effects a new {@code CI}, {@code true}
     *         if the update effects a existing {@code CI}
     */
    public boolean isNewCI() {
        return newCI;
    }

    /**
     * Determines if the update effects a new {@code CI}.
     * 
     * @param isNewCI
     *            {@code true} if the update effects a new {@code CI},
     *            {@code true} if the update effects a existing {@code CI}
     */
    public void setIsNewCI(boolean isNewCI) {
        this.newCI = isNewCI;
    }

    /**
     * Returns the {@code CItype} of the new {@code CI}.
     * 
     * @return {@code CItype}
     */
    public CItype getCItype() {
        return ciType;
    }

    /**
     * Returns the {@code CI} which data should be updated.
     * 
     * @return {@code CI}
     */
    public CI getCI() {
        return ci;
    }

    /**
     * Adds a long update.
     * 
     * @param fieldName
     *            field name
     * @param l
     *            long update
     * @throws Exception
     */
    public void addLong(String fieldName, Long l) throws Exception {
        UIelement ui = ciType.getUIelement(fieldName);
        if (ui == null) {
            throw new Exception(
                    "UpdateCIdata.addLong(): Unknown data field '" + fieldName + "' for CI " + ci.getName());
        }
        if (ui.getDataType() != ClassID.CILONG) {
            throw new Exception(
                    "UpdateCIdata.addLong(): Data type missmatch for field '" + fieldName + "' for CI " + ci.getName());
        }
        data.put(fieldName, l);
    }

    /**
     * Adds a string update.
     * 
     * @param fieldName
     *            field name
     * @param s
     *            string update
     * @throws Exception
     */
    public void addString(String fieldName, String s) throws Exception {
        UIelement ui = ciType.getUIelement(fieldName);
        if (ui == null) {
            throw new Exception(
                    "UpdateCIdata.addString(): Unknown data field '" + fieldName + "' for CI " + ci.getName());
        }
        if (ui.getDataType() != ClassID.CISTRING) {
            throw new Exception("UpdateCIdata.addString(): Data type missmatch for field '" + fieldName + "' for CI "
                    + ci.getName());
        }
        data.put(fieldName, s);
    }

    public void removeString(String fieldName) throws Exception {
        UIelement ui = ciType.getUIelement(fieldName);
        if (ui == null) {
            throw new Exception(
                    "UpdateCIdata.addString(): Unknown data field '" + fieldName + "' for CI " + ci.getName());
        }
        data.remove(fieldName);
    }

    /**
     * Adds a date update.
     * 
     * @param fieldName
     *            field name
     * @param d
     *            date update
     * @throws Exception
     */
    public void addDate(String fieldName, Date d) throws Exception {
        UIelement ui = ciType.getUIelement(fieldName);
        if (ui == null) {
            throw new Exception(
                    "UpdateCIdata.addDate(): Unknown data field '" + fieldName + "' for CI " + ci.getName());
        }
        data.put(fieldName, d);
    }

    /**
     * Adds a double update.
     * 
     * @param fieldName
     *            field name
     * @param d
     *            double update
     * @throws Exception
     */
    public void addDouble(String fieldName, double d) throws Exception {
        UIelement ui = ciType.getUIelement(fieldName);
        if (ui == null) {
            throw new Exception(
                    "UpdateCIdata.addDouble(): Unknown data field '" + fieldName + "' for CI " + ci.getName());
        }
        if (ui.getDataType() != ClassID.CIDOUBLE) {
            throw new Exception("UpdateCIdata.addDouble(): Data type missmatch for field '" + fieldName + "' for CI "
                    + ci.getName());
        }
        data.put(fieldName, d);
    }

    /**
     * Adds a boolean update.
     * 
     * @param fieldName
     *            field name
     * @param b
     *            boolean update
     * @throws Exception
     */
    public void addBoolean(String fieldName, boolean b) throws Exception {
        UIelement ui = ciType.getUIelement(fieldName);
        if (ui == null) {
            throw new Exception(
                    "UpdateCIdata.addBoolean(): Unknown data field '" + fieldName + "' for CI " + ci.getName());
        }
        if (ui.getDataType() != ClassID.CIBOOLEAN) {
            throw new Exception("UpdateCIdata.addBoolean(): Data type missmatch for field '" + fieldName + "' for CI "
                    + ci.getName());
        }
        data.put(fieldName, b);
    }

    /**
     * Adds a binary update.
     * 
     * @param fieldName
     *            field name
     * @param binData
     *            binary data
     * @throws Exception
     */
    public void addBlob(String fieldName, byte[] binData) throws Exception {
        UIelement ui = ciType.getUIelement(fieldName);
        if (ui == null) {
            throw new Exception(
                    "UpdateCIdata.addBlob(): Unknown data field '" + fieldName + "' for CI " + ci.getName());
        }
        if (ui.getDataType() != ClassID.CIBLOB) {
            throw new Exception(
                    "UpdateCIdata.addBlob(): Data type missmatch for field '" + fieldName + "' for CI " + ci.getName());
        }
        data.put(fieldName, binData);
    }

    /**
     * Adds a {@code IstringDummy} for a {@code CIi18nString} update.
     * 
     * @param fieldName
     *            field name
     * @param dummy
     *            {@code IstringDummy}
     * @throws Exception
     */
    public void addIString(String fieldName, IstringDummy dummy) throws Exception {
        UIelement ui = ciType.getUIelement(fieldName);
        if (ui == null) {
            throw new Exception(
                    "UpdateCIdata.addIString(): Unknown data field '" + fieldName + "' for CI " + ci.getName());
        }
        if (ui.getDataType() != ClassID.CII18NSTRING) {
            throw new Exception("UpdateCIdata.addIString(): Data type missmatch for field '" + fieldName + "' for CI "
                    + ci.getName());
        }
        @SuppressWarnings("unchecked")
        ArrayList<IstringDummy> list = (ArrayList<IstringDummy>) data.get(fieldName);
        if (list == null) {
            list = new ArrayList<IstringDummy>();
            data.put(fieldName, list);
        }

        list.add(dummy);
    }

    /**
     * Adds a {@code ImageDummy} for a {@code CIimage} update.
     * 
     * @param fieldName
     *            field name
     * @param dummy
     *            {@code ImageDummy}
     * @throws Exception
     */
    public void addImage(String fieldName, ImageDummy dummy) throws Exception {
        UIelement ui = ciType.getUIelement(fieldName);
        if (ui == null) {
            throw new Exception(
                    "UpdateCIdata.addImage(): Unknown data field '" + fieldName + "' for CI " + ci.getName());
        }
        if (ui.getDataType() != ClassID.CIIMAGE) {
            throw new Exception("UpdateCIdata.addImage(): Data type missmatch for field '" + fieldName + "' for CI "
                    + ci.getName());
        }
        @SuppressWarnings("unchecked")
        ArrayList<ImageDummy> list = (ArrayList<ImageDummy>) data.get(fieldName);
        if (list == null) {
            list = new ArrayList<ImageDummy>();
            data.put(fieldName, list);
        }
        list.add(dummy);

    }

    /**
     * Adds a {@code FileDummy} for a {@code CIfile} update.
     * 
     * @param fieldName
     *            field name
     * @param dummy
     *            {@code FileDummy}
     * @throws Exception
     */
    public void addFile(String fieldName, FileDummy dummy) throws Exception {
        UIelement ui = ciType.getUIelement(fieldName);
        if (ui == null) {
            throw new Exception(
                    "UpdateCIdata.addFile(): Unknown data field '" + fieldName + "' for CI " + ci.getName());
        }
        if (ui.getDataType() != ClassID.CIFILE) {
            throw new Exception(
                    "UpdateCIdata.addFile(): Data type missmatch for field '" + fieldName + "' for CI " + ci.getName());
        }
        @SuppressWarnings("unchecked")
        ArrayList<FileDummy> list = (ArrayList<FileDummy>) data.get(fieldName);
        if (list == null) {
            list = new ArrayList<FileDummy>();
            data.put(fieldName, list);
        }
        list.add(dummy);

    }

    /**
     * Returns the {@code UIelement}
     * 
     * @param fieldName
     *            field name
     * @return {@code UIelement} {@code null}, if the the field name doesn't
     *         exist.
     */
    public UIelement getUIelement(String fieldName) {
        return ciType.getUIelement(fieldName);
    }

    /**
     * Return all available updates.
     * 
     * @return update map
     */
    public HashMap<String, Object> getUpdates() {
        return data;
    }

    /**
     * Returns the language context of the update map.
     * 
     * @return language.
     */
    public Locale.LANGUAGE getLanguage() {
        return language;
    }

    public Object getDataTabSheetComponent() {
        return dataTabSheetComponent;
    }

    public void setDataTabSheetComponent(Object dataTabSheetComponent) {
        this.dataTabSheetComponent = dataTabSheetComponent;
    }
}
