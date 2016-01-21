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

import java.util.ArrayList;
import java.util.EnumSet;

/**
 * <p>
 * Supported MIME types with additional bytes signatures for header
 * identification.<br>
 * File signatures table: http://www.garykessler.net/library/file_sigs.html
 * </p>
 * 
 * @author Peter Sauer
 * 
 */
public enum MimeType {
    // additional logic coded into the hex string header
    // ? ... wild card
    // x- ... skip x bytes of the header
    // * ... no unique header available
    GIF("474946383761", "474946383961") {
        public String toString() {
            return "image/gif";
        }
    },
    JPG("FFD8FFE0??4A46494600", "FFD8FFE1??4578696600", "FFD8FFE8??535049464600") {
        public String toString() {
            return "image/jpg";
        }

    },
    PNG("89504E470D0A1A0A") {
        public String toString() {
            return "image/png";
        }
    },

    SWF("5A5753") {
        public String toString() {
            return "application/x-shockwave-flash";
        }
    },

    FLV("464C5601") {
        public String toString() {
            return "video/x-flv";
        }
    },
    OGG("4F67675300020000000000000000") {
        public String toString() {
            return "video/ogg";
        }
    },
    MP4("000000146674797069736F6D", "000000186674797033677035", "00000018667479706D703432",
            "0000001C667479704D534E56012900464D534E566D703432", "4-6674797033677035", "4-667479704D534E56",
            "4-6674797069736F6D") {
        public String toString() {
            return "video/mp4";
        }
    },
    WEBM("1A45DFA3") {
        public String toString() {
            return "video/webm";
        }
    },
    ZIP("504B0304") {
        public String toString() {
            return "application/zip";
        }
    },
    _7Z("377ABCAF271C") {
        public String toString() {
            return "application/x-7z-compressed";
        }
    },
    RAR("526172211A0700", "526172211A070100") {
        public String toString() {
            return "application/x-rar-compressed";
        }
    },
    MP3("*") {
        public String toString() {
            return "audio/mpeg3";
        }
    },
    PDF("25504446") {
        public String toString() {
            return "application/pdf";
        }
    },
    MKV("1A45DFA3934282886D6174726F736B61") {
        public String toString() {
            return "video/x-matroska";
        }
    },
    WAV("52494646????57415645666D7420") {
        public String toString() {
            return "audio/x-wav";
        }
    };

    private MimeType(String... headers) {
        ArrayList<String> list = new ArrayList<String>();
        for (String h : headers) {
            list.add(h);
        }
        header = list.toArray(new String[list.size()]);
    }

    private String[] header;

    private String removeUnderscore(String name) {
        if (name.startsWith("_")) {
            return name.substring(1);
        }
        return name;
    }

    /**
     * Returns a list of headers
     * 
     * @return header list
     */
    public String[] getHeaders() {
        return header;
    }

    static EnumSet<MimeType> webImages;

    /**
     * Returns file extension including the leading point.
     * 
     * @return file extension - e.g. .jpg
     */
    public String getExtensionWithPoint() {
        return "." + removeUnderscore(name()).toLowerCase();
    }

    /**
     * Returns the file extension.
     * 
     * @return file extension
     */
    public String getExtension() {
        return removeUnderscore(name()).toLowerCase();
    }

    /**
     * Returns the MIME type of a file.
     * 
     * @param path
     *            file path
     * @return MIME type, {@code null} if the MIME type can not matched with the
     *         supported MIME types
     */
    public static MimeType getMimeType(String path) {
        MimeType mimeType = null;
        int pos = path.lastIndexOf(".");
        if (pos == -1) {
            return null;
        }
        try {
            String tmp = path.substring(pos + 1).toUpperCase();
            if (tmp.equals("7Z")) {
                tmp = "_7Z";
            }
            mimeType = MimeType.valueOf(tmp);
        } catch (Exception e) {
            return null;
        }
        return mimeType;
    }

    static {
        webImages = EnumSet.of(MimeType.GIF, MimeType.PNG, MimeType.JPG);
    }

    /**
     * Returns a set of all available MIME types.
     * 
     * @return set of all available MIME types
     */
    public static EnumSet<MimeType> getWebImageMimeTypes() {
        return webImages;
    }

    /**
     * Returns the MIME type of a given file extension.
     * 
     * @param ext
     *            file extension
     * @return {@code MimeType} if available, or {@code null} if not
     */
    public static MimeType extToMimeType(String ext) {
        ext = ext.toUpperCase();
        if (ext.equals("7Z")) {
            ext = "_7Z";
        }
        try {
            return MimeType.valueOf(ext);
        } catch (Exception e) {
            return null;
        }
    }
}
