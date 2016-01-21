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

import javax.persistence.Column;

/**
 * Base class 
 * 
 * @author Peter Sauer
 */
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import at.treedb.db.Base;
import at.treedb.db.ClassID;
import at.treedb.db.DBkey;
import at.treedb.i18n.Istring;

/**
 * <p>
 * Base class of an UI element.
 * </p>
 * 
 * @author Peter Sauer
 *
 */

@SuppressWarnings("serial")
@MappedSuperclass
abstract public class UIelement extends Base implements Comparable<Object> {

    public enum BaseFields {
        index, displayName, description, mandatory, mandatoryError
    }

    // index - display order of the elements
    @Column(name = "m_index")
    private int index;
    // field name
    private String fieldName;
    @DBkey(Istring.class)
    // display name
    private int displayName;
    @DBkey(Istring.class)
    protected int description;
    @DBkey(UItab.class)
    private int uiTab;
    private boolean mandatory;
    @DBkey(Istring.class)
    private int mandatoryError;
    @Transient
    private UItab uiTabObj;

    protected UIelement() {

    }

    /**
     * Returns the composed ID of the {@code UIelement}.<br>
     * <------- long value 64 bit --------><br>
     * <--32 bit value--><--32 bit value--><br>
     * <- ClassID -><- hist. ID -><br>
     * 
     * @return
     */
    public long getComposedId() {
        return (((long) getCID().ordinal()) << 32) | (long) getHistId();
    }

    /**
     * Extracts the historization ID from the composed ID.
     * 
     * @param composed
     *            ID
     * @return historization ID
     */
    public static int extractHistIdFromComposedId(long composed) {
        return (int) (composed & 0xffffffffL);
    }

    /**
     * Extracts the {@code ClassID} ID from the composed ID.
     * 
     * @param composed
     *            ID
     * @return {@code ClassID} ID
     */
    public static int extractClassIdFromComposedId(long composed) {
        return (int) (composed >> 32);
    }

    /**
     * Extracts the {@code ClassID} from the composed ID.
     * 
     * @param composed
     *            ID
     * @return {@code ClassID} ID
     */
    public static Class<?> getClassIdFromComposedId(long composed) {
        return ClassID.classIDtoClass((int) (composed >> 32));
    }

    /**
     * Creates a composed ID for a {@code UIelement}.
     * 
     * @param id
     *            DB ID
     * @return composed ID
     */
    public long createComposedId(long id) {
        return (((long) getCID().ordinal()) << 32) | id;
    }

    /**
     * Constructor
     * 
     * @param uiTab
     *            associated {@code UItab}
     * @param fieldName
     *            internal field name
     * @param displayName
     *            display name
     * @param description
     *            description
     * @param mandatory
     *            {@code true} if this input element is mandatory,{@code false}
     *            if not
     * @param mandatoryError
     *            error message if a mandatory input field remains empty
     */
    protected UIelement(UItab uiTab, String fieldName, int displayName, int description, boolean mandatory,
            int mandatoryError) {
        uiTabObj = uiTab;
        this.fieldName = fieldName;
        this.displayName = displayName;
        this.uiTab = uiTab.getHistId();
        this.description = description;
        this.mandatory = mandatory;
        this.mandatoryError = mandatoryError;
    }

    /**
     * Returns the order index of the element.
     * 
     * @return index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Return the associated {@code UItab} ID
     * 
     * @return {@code UItab} ID
     */
    public @DBkey(UItab.class) int getUItab() {
        return uiTab;
    }

    /**
     * Sets the index to order elements.
     * 
     * @param index
     *            order index
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Returns the internal name of the {@code UIelement}.
     * 
     * @return internal name of the element
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Returns a description of the {@code UIelement}.
     * 
     * @return description of the element
     */
    public @DBkey(Istring.class) int getDescription() {
        return description;
    }

    /**
     * Returns the display name of the {@code UIelement}.
     * 
     * @return display name
     */
    public @DBkey(Istring.class) int getDisplayName() {
        return displayName;
    }

    abstract public ClassID getDataType();

    @Override
    public int compareTo(Object u) {
        return index - ((UIelement) u).index;
    }

    /**
     * Gets a boolean value that indicates whether the {@code UIelement} is
     * mandatory.
     * 
     * @return {@code true} if the {@code UIelement} is mandatory,{@code false}
     *         if not
     */
    public boolean isMandatory() {
        return mandatory;
    }

    /**
     * Returns the mandatory error message.
     * 
     * @return mandatory error message
     */
    public @DBkey(Istring.class) int getMandatoryError() {
        return mandatoryError;
    }

    @Override
    public void callbackBeforeSave() {
        this.setIndex(uiTabObj.getNextIndex());
    }

}
