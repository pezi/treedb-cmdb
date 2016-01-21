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
package at.treedb.util;

import java.util.ArrayList;
import at.treedb.ci.MimeType;

/**
 * <p>
 * Class for detecting the MIME type of a byte buffer/file.
 * </p>
 * 
 * @author Peter Sauer
 *
 */
public class ContentInfo {

    private static boolean contains(String headerStr, int start, byte[] data) {
        int[] header = hexStringToByteArray(headerStr);
        if (start + header.length > data.length) {
            return false;
        }
        for (int i = 0; i < header.length; ++i) {
            if (header[i] == -1) {
                continue;
            }
            if (header[i] != (data[start + i] & 0xff)) {
                return false;
            }
        }
        return true;
    }

    private static int[] hexStringToByteArray(String s) {
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < s.length(); ++i) {
            // convert wild card ? to -1
            if (s.charAt(i) == '?') {
                list.add(-1);
            } else {
                list.add(((byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16)))
                        & 0xff);
                ++i;
            }
        }
        int[] array = new int[list.size()];
        for (int i = 0; i < list.size(); ++i) {
            array[i] = list.get(i);
        }
        return array;
    }

    /**
     * Tries to detect the MIME type of a byte buffer.
     * 
     * @param data
     *            binary data
     * @return {@code MimeType} or {@code null}, if detection was not possible
     */
    public static MimeType getContentInfo(byte[] data) {
        for (MimeType type : MimeType.values()) {
            String[] headers = type.getHeaders();
            for (String h : headers) {
                if (h.equals("*")) {
                    continue;
                }
                int pos = h.indexOf("-");
                int start = 0;
                if (pos > 0) {
                    start = Integer.valueOf(h.substring(0, pos));
                    h = h.substring(pos + 1);
                }
                if (contains(h, start, data)) {
                    return type;
                }
            }
        }
        // MP3 test
        if (data.length > 4) {
            if ((data[0] & 0xff) == 0xff) {
                int value = data[0] & 0xf0;
                if (value == 0xe0 || value == 0xf0) {
                    return MimeType.MP3;
                }
            }
        }
        return null;
    }

    /**
     * Counts how often each byte value appears in a range of bytes.
     *
     * @param data
     *            input buffer.
     * @param start
     *            index into the buffer where the counting starts.
     * @param length
     *            number of bytes to count.
     *
     * @return array with 256 entries that say how often each byte value
     *         appeared in the requested input buffer range.
     **/
    private static int[] countByteDistribution(byte[] data, int start, int length) {
        final int[] countedData = new int[256];
        for (int i = start; i < start + length; i++) {
            countedData[data[i] & 0xFF]++;
        }
        return countedData;
    }

    /**
     * Calculates the log2 of a value.
     **/
    private static double log2(double d) {
        return Math.log(d) / Math.log(2.0);
    }

    /**
     * Calculates the entropy of a sub-array.
     *
     * @param data
     *            The input data.
     * @param start
     *            Index into the input data buffer where the entropy calculation
     *            begins.
     * @param length
     *            Number of bytes to consider during entropy calculation.
     *
     * @return Entropy of the sub-array.
     **/
    public static double calculateEntropy(byte[] data, int start, int length) {
        double entropy = 0;
        int[] countedData = countByteDistribution(data, start, length);
        for (int i = 0; i < 256; i++) {
            final double px = 1.0 * countedData[i] / length;

            if (px > 0) {
                entropy += -px * log2(px);
            }
        }
        return entropy;
    }
}
