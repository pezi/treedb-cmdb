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
package at.treedb.util.text;

import java.util.*;

import at.treedb.i18n.Locale;

/**
 * <p>
 * Group of language strings.
 * </p>
 */
public class TextGroup {
    private Locale.LANGUAGE language;
    private Locale.COUNTRY country;
    private HashMap<String, String> map;
    // group name
    private String name;

    /**
     * Copy constructor
     * 
     * @param group
     *            group to be copied
     * @param language
     *            language of the group
     * @param country
     *            country of the group
     */
    public TextGroup(TextGroup group, Locale.LANGUAGE language, Locale.COUNTRY country) {
        this.language = language;
        this.country = country;
        this.name = group.name;
        this.map = group.map;
    }

    /**
     * Sets the language and the country.
     * 
     * @param language
     *            language of the group
     * @param country
     *            country of the group
     */
    public void setLang(Locale.LANGUAGE language, Locale.COUNTRY country) {
        this.language = language;
        this.country = country;
    }

    /**
     * Returns the text for a given text ID.
     * 
     * @param id
     *            text ID
     * @return text
     */
    public String get(String id) {
        String t = null;
        // 1.) get country variant
        if (country != null) {
            t = (String) map.get(id + "_" + language.name() + "_" + country.name());
        }
        // 2.) if null, get language variant
        if (t == null) {
            t = (String) map.get(id + "_" + language.name());
        }
        // system message
        if (t == null) {
            t = (String) map.get(id);
        }
        return t;
    }

    /**
     * Returns the language of the group
     * 
     * @return {@code Locale.LANGUAGE}
     */
    public Locale.LANGUAGE getLanguage() {
        return language;
    }

    /**
     * Stores a language string.
     * 
     * @param language
     *            language of the string
     * @param country
     *            optional country, used for store a country variant, e.g.
     *            Tomate (de_DE) vs. Paradeiser (de_AT) [=tomato]
     * @param id
     *            text ID string
     * @param text
     *            text
     */
    public void put(Locale.LANGUAGE language, Locale.COUNTRY country, String id, String text) {
        if (country == null) {
            map.put(id + "_" + language.name(), text);
        } else {
            map.put(id + "_" + language.name() + "_" + country.name(), text);
        }
    }

    /**
     * Stores a language independent (system) string.
     * 
     * @param id
     *            text ID string
     * @param text
     *            text
     */
    public void putSYS(String id, String text) {
        map.put(id, text);
    }

    /**
     * Constructor
     * 
     * @param name
     *            name of the group
     */
    public TextGroup(String name) {
        map = new HashMap<String, String>();
        this.name = name;
    }

    /**
     * Returns the group name.
     * 
     * @return group name of the group.
     */
    public String getName() {
        return name;
    }

}