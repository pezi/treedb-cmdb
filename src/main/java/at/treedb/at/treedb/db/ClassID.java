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

import at.treedb.ci.Blob;
import at.treedb.ci.CI;
import at.treedb.ci.CIbigDecimal;
import at.treedb.ci.CIblob;
import at.treedb.ci.CIboolean;
import at.treedb.ci.CIdate;
import at.treedb.ci.CIdouble;
import at.treedb.ci.CIfile;
import at.treedb.ci.CIgoogleMap;
import at.treedb.ci.CIi18nString;
import at.treedb.ci.CIimage;
import at.treedb.ci.CIlong;
import at.treedb.ci.CIstring;
import at.treedb.ci.CItype;
import at.treedb.ci.Image;
import at.treedb.ci.KeyValuePair;
import at.treedb.ci.Node;
import at.treedb.dbfs.DBfile;
import at.treedb.domain.DBcategory;
import at.treedb.domain.DBcategoryMembership;
import at.treedb.domain.Domain;
import at.treedb.domain.VersionInfo;
import at.treedb.i18n.Istring;
import at.treedb.i18n.SupportedLanguage;
import at.treedb.jslib.JsLib;
import at.treedb.ui.UIcheckbox;
import at.treedb.ui.UIdateField;
import at.treedb.ui.UIfile;
import at.treedb.ui.UIgoogleMap;
import at.treedb.ui.UIgrouping;
import at.treedb.ui.UIgroupingEnd;
import at.treedb.ui.UImacro;
import at.treedb.ui.UIoption;
import at.treedb.ui.UIselect;
import at.treedb.ui.UIslider;
import at.treedb.ui.UItab;
import at.treedb.ui.UItextArea;
import at.treedb.ui.UItextField;
import at.treedb.ui.UIwikiImage;
import at.treedb.ui.UIwikiTextArea;
import at.treedb.user.Group;
import at.treedb.user.Membership;
import at.treedb.user.Permissions;
import at.treedb.user.Tenant;
import at.treedb.user.User;

/**
 * <p>
 * Class ID
 * </p>
 * <p>
 * Remark: Do not change the order of this IDs.
 * </p>
 * 
 * @author Peter Sauer
 *
 */
public enum ClassID {
    NOT_DEFINED,

    USER,
    /** {@link at.treedb.user.User } */
    GROUP,
    /** {@link at.treedb.user.Group } */
    PERMISSIONS,
    /** {@link at.treedb.user.Permissions } */
    MEMBERSHIP,
    /** {@link at.treedb.user.Membership } */
    ISTRING,
    /** {@link at.treedb.i18n.Istring} */
    DOMAIN,
    /** {@link at.treedb.domain.Domain } */
    IMAGE,
    /** {@link at.treedb.ci.Image } */
    NODE,
    /** {@link at.treedb.ci.Node } */
    CI,
    /** {@link at.treedb.ci.CI} */
    CITYPE,
    /** {@link at.treedb.ci.CItype } */
    CIBOOLEAN,
    /** {@link at.treedb.ci.CIboolean} */
    CISTRING,
    /** {@link at.treedb.ci.CIstring} */
    CII18NSTRING,
    /** {@link at.treedb.ci.CIi18nString} */
    CILONG,
    /** {@link at.treedb.ci.CIlong} */
    CIDOUBLE,
    /** {@link at.treedb.ci.CIdouble} */
    CIBLOB,
    /** {@link at.treedb.ci.CIblob} */
    CIIMAGE,
    /** {@link at.treedb.ci.CIimage } */
    CIDATE,
    /** {@link at.treedb.ci.CIdate } */
    UITAB,
    /** {@link at.treedb.ui.UItab } */
    UITEXTFIELD,
    /** {@link at.treedb.ui.UItextField } */
    UITEXTAREA,
    /** {@link at.treedb.ui.UItextArea } */
    UIWIKITEXTAREA,
    /** {@link at.treedb.ui.UIwikiTextArea } */
    UIWIKIIMAGE,
    /** {@link at.treedb.ui.UIwikiImage } */
    UIDATEFIELD,
    /** {@link at.treedb.ui.UIdateField } */
    UICHECKBOX,
    /** {@link at.treedb.ui.UIcheckbox } */
    UISLIDER,
    /** {@link at.treedb.ui.UIslider } */
    UISELECT,
    /** {@link at.treedb.ui.UIselect } */
    UIOPTION,
    /** {@link at.treedb.ui.UIoption } */
    UIFILE,
    /** {@link at.treedb.ui.UIfile } */
    UIGROUPING,
    /** {@link at.treedb.ui.UIgrouping } */
    UIGROUPINGEND,
    /** {@link at.treedb.ui.UIgroupingEnd } */
    DBFILE, CIFILE, CIBIGDECIMAL, UIMACRO, CIGOOGLEMAP, UIGOOGLEMAP, SUPPORTED_LANGUAGE, CLAZZ, DBINFO, CACHEENTRY, DBCATEGORY, DBCATEGORYMEMBERSHIP, JSLIB, BLOB, KEYVALUEPAIR, VERSIONINFO, TENANT, EVENT, CIPASSWORD, UIBLOB;

    /**
     * Returns the corresponding {@code Class} of a class ID.
     * 
     * @return {@code Class}
     */
    public Class<?> toClass() {
        switch (this) {
        case USER:
            return User.class;
        case GROUP:
            return Group.class;
        case PERMISSIONS:
            return Permissions.class;
        case MEMBERSHIP:
            return Membership.class;
        case ISTRING:
            return Istring.class;
        case DOMAIN:
            return Domain.class;
        case IMAGE:
            return Image.class;
        case NODE:
            return Node.class;
        case CI:
            return CI.class;
        case CITYPE:
            return CItype.class;
        case CIBOOLEAN:
            return CIboolean.class;
        case CISTRING:
            return CIstring.class;
        case CII18NSTRING:
            return CIi18nString.class;
        case CILONG:
            return CIlong.class;
        case CIDOUBLE:
            return CIdouble.class;
        case CIBLOB:
            return CIblob.class;
        case CIIMAGE:
            return CIimage.class;
        case CIDATE:
            return CIdate.class;
        case UITAB:
            return UItab.class;
        case UITEXTFIELD:
            return UItextField.class;
        case UITEXTAREA:
            return UItextArea.class;
        case UIWIKITEXTAREA:
            return UIwikiTextArea.class;
        case UIWIKIIMAGE:
            return UIwikiImage.class;
        case UIDATEFIELD:
            return UIdateField.class;
        case UICHECKBOX:
            return UIcheckbox.class;
        case UISLIDER:
            return UIslider.class;
        case UISELECT:
            return UIselect.class;
        case UIOPTION:
            return UIoption.class;
        case UIFILE:
            return UIfile.class;
        case UIGROUPING:
            return UIgrouping.class;
        case UIGROUPINGEND:
            return UIgroupingEnd.class;
        case DBFILE:
            return DBfile.class;
        case CIFILE:
            return CIfile.class;
        case CIBIGDECIMAL:
            return CIbigDecimal.class;
        case UIMACRO:
            return UImacro.class;
        case CIGOOGLEMAP:
            return CIgoogleMap.class;
        case UIGOOGLEMAP:
            return UIgoogleMap.class;
        case SUPPORTED_LANGUAGE:
            return SupportedLanguage.class;
        case CLAZZ:
            return Clazz.class;
        case DBINFO:
            return DBinfo.class;
        case CACHEENTRY:
            return CacheEntry.class;
        case DBCATEGORY:
            return DBcategory.class;
        case DBCATEGORYMEMBERSHIP:
            return DBcategoryMembership.class;
        case JSLIB:
            return JsLib.class;
        case BLOB:
            return Blob.class;
        case KEYVALUEPAIR:
            return KeyValuePair.class;
        case VERSIONINFO:
            return VersionInfo.class;
        case TENANT:
            return Tenant.class;
        case EVENT:
            return Event.class;
        default:
            return null;
        }
    }

    private final static ClassID[] classIDlist = ClassID.values();

    /**
     * Returns the corresponding {@code Class} of a numeric class ID.
     * 
     * @return {@code Class}
     */
    static public Class<?> classIDtoClass(int id) {
        return classIDlist[id].toClass();
    }

};