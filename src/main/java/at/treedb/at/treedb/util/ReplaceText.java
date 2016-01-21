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

/**
 * <p>
 * Utility for simple text replacement.
 * </p>
 * 
 *@author Peter Sauer
 */

import java.util.*;
import java.io.*;

/**
 * Helper class for simple text replacement operations. Variables inside a text
 * like <code>$test$</code> will be replaced by a given string. The decoration
 * <code>$</code> symbol can be used inside the text by code stuffing:
 * <code>$$</code> stands for <code>$</code>
 * 
 * @author Peter Sauer
 */
public class ReplaceText {
    private final String property; // name of the replacement variable
    private final String valueString;
    private final char[] valueArray;
    /**
     * Decorating symbol <code>$</code> - e.g <code>$replace$</code>
     */
    public static final char SUBST_SYMBOL = '$';
    /**
     * Maximum length of the substitution variable: 30 characters
     */
    public static final int MAX_SUBSTVAR_LEN = 30;

    /**
     * Creates an empty replacement container.
     * 
     * @param property
     *            name of the variable
     * 
     */
    public ReplaceText(String property) {
        this.property = property;
        valueString = "";
        valueArray = null;
    }

    /**
     * Creates a replacement container for character array insertion.
     * 
     * @param property
     *            name of the variable
     * 
     * @param valueArray
     *            replacement char array
     * 
     */
    public ReplaceText(String property, char[] valueArray) {
        this.property = property;
        this.valueString = null;
        this.valueArray = valueArray;
    }

    /**
     * Creates a replacement container for a number insertion.
     * 
     * @param property
     *            name of the variable
     * 
     * @param value
     *            numeric value
     * 
     */
    public ReplaceText(String property, long value) {
        this.property = property;
        valueString = "" + value;
        valueArray = null;
    }

    /**
     * Creates a replacement container for a string insertion.
     * 
     * @param property
     *            name of the variable
     * 
     * @param value
     *            replacement string
     */
    public ReplaceText(String property, String value) {
        this.property = property;
        this.valueString = value;
        valueArray = null;
    }

    /**
     * Replaces the text variables.
     * 
     * @param source
     *            character array
     * @param replace
     *            replacement containers
     * @return replaced text
     */
    public static String replace(char[] source, ReplaceText[] replace) {
        int i, j, max, start;
        HashMap<String, ReplaceText> map = new HashMap<String, ReplaceText>(replace.length * 3);
        for (i = 0; i < replace.length; ++i) {
            map.put(replace[i].property, replace[i]);
        }
        // initial buffer size with 10% plus
        int bufferSize = source.length + source.length / 10;
        StringBuffer buffer = new StringBuffer(bufferSize);
        main_loop: for (i = 0; i < source.length; ++i) {
            if (source[i] == SUBST_SYMBOL) {
                // code stuffing?
                if (i < source.length - 1) {
                    if (source[i + 1] == SUBST_SYMBOL) {
                        buffer.append(SUBST_SYMBOL);
                        ++i;
                        continue;
                    }
                } else {
                    // last character is a SUBST_SYMBOL - ???
                    buffer.append(SUBST_SYMBOL);
                    break;
                }
                start = i + 1;
                max = Math.min(start + MAX_SUBSTVAR_LEN, source.length);
                for (j = start; j < max; ++j) {
                    if (source[j] == SUBST_SYMBOL) {
                        int len = j - start;
                        if (len > 0) {
                            ReplaceText r = map.get(new String(source, start, len));
                            if (r != null) {
                                if (r.valueString != null) {
                                    buffer.append(r.valueString);
                                } else {
                                    buffer.append(r.valueArray);
                                }
                                i = j;
                                continue main_loop;
                            }
                        }
                    }
                }
            }
            buffer.append(source[i]);
        }
        return buffer.toString();
    }

    /**
     * Replaces the text variables.
     * 
     * @param source
     *            source text
     * @param replace
     *            replacement containers
     * @return replaced text
     */
    public static String replaceText(String source, ReplaceText[] replace) {
        return replace(source.toCharArray(), replace);
    }

    /**
     * Replaces the text variables.
     * 
     * @param file
     *            source file
     * @param replace
     *            replacement containers
     * @return replaced text
     * @throws IOException
     */
    public static String replaceText(File file, ReplaceText[] replace) throws IOException {
        FileReader reader = new FileReader(file);
        int fileLength = (int) file.length();
        char[] stream = new char[fileLength];
        reader.read(stream, 0, fileLength);
        reader.close();
        return replace(stream, replace);
    }

    /**
     * Test
     * 
     * @param test
     */
    public static void main(String test[]) {
        ReplaceText[] rt = new ReplaceText[] { new ReplaceText("text", "is"), new ReplaceText("num", 12) };
        System.out.println(replaceText("This $text$ a simple test: $num$$$: $$", rt));
    }
}
