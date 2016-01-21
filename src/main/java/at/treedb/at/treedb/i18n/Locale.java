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
package at.treedb.i18n;

import java.util.HashMap;

/**
 * <p>
 * Own implementation of a {@code Locale} for handling language and country
 * settings.
 * </p>
 * 
 * @author Peter Sauer
 *
 */
public class Locale {

    public enum LOCALE {
        en_UK, en_US, de_DE, de_AT, de_CH, pl_PL, ru_RU;

        public LANGUAGE getLanguage() {
            return LANGUAGE.valueOf(this.name().substring(0, 2));
        }

        public COUNTRY getCountry() {
            return COUNTRY.valueOf(this.name().substring(3));
        }
    };

    public enum LANGUAGE {
        en, de, pl, ru, it, es, fr, hu, sys
    }

    public enum COUNTRY {
        UK, US, AT, DE, CH, PL, RU, HU, ES, IT, FR
    }

    private LOCALE locale;
    private LANGUAGE language;
    private COUNTRY country;
    private java.util.Locale javaLocale;
    private static LOCALE defaultLocale = LOCALE.de_DE;
    private static HashMap<LANGUAGE, LOCALE> preferedLocale = new HashMap<LANGUAGE, LOCALE>();;

    static {
        preferedLocale.put(LANGUAGE.en, LOCALE.en_UK);
    }

    /**
     * Sets the default locale.
     * 
     * @param locale
     */
    public static void setDefaultLocale(LOCALE locale) {
        defaultLocale = locale;
    }

    /**
     * Sets the preferred locale for a language. e.g. for 'en' is 'en_UK' the
     * preferred locale.
     * 
     * @param lang
     *            language
     * @param locale
     *            preferred locale
     */
    public static void setPreferredLocale(LANGUAGE lang, LOCALE locale) {
        preferedLocale.put(lang, locale);
    }

    /**
     * Returns the preferred locale for a language.
     * 
     * @param language
     *            language. e.g. for 'en' is 'en_UK' the preferred locale.
     * @return preferred {@code Locale}
     */
    public static LOCALE getPreferedLocale(LANGUAGE language) {
        LOCALE l = preferedLocale.get(language);
        if (l == null) {
            return LOCALE.valueOf(language.name() + "_" + language.name().toUpperCase());
        }
        return l;
    }

    /**
     * Returns the best fitting locale.
     * 
     * @param locale
     *            locale e.g. from the client
     * @return {@code Locale}
     */
    public static Locale getNearestLocale(String locale) {
        Locale l = new Locale(defaultLocale);
        ;
        try {
            l = new Locale(LOCALE.valueOf(locale));
        } catch (Exception e) {
            try {
                String[] split = locale.split("_");
                String lang = split[0];
                if (lang.equals("en")) {
                    return new Locale(preferedLocale.get(LANGUAGE.en));
                }
                return new Locale(LOCALE.valueOf(lang + "_" + lang.toUpperCase()));
            } catch (Exception e2) {
            }
        }
        return l;
    }

    /**
     * Constructor
     * 
     * @param locale
     *            locale string e.g de_DE
     */
    public Locale(String locale) {
        this(LOCALE.valueOf(locale));
    }

    /**
     * Constructor
     * 
     * @param locale
     *            {@code Locale}
     */
    public Locale(LOCALE locale) {
        this.locale = locale;
        String[] split = locale.name().split("_");
        this.language = LANGUAGE.valueOf(split[0]);
        // set country for language == country to null!
        if (!split[0].equals(split[1].toLowerCase())) {
            this.country = COUNTRY.valueOf(split[1]);
        } else {
            this.country = null;
        }
        javaLocale = new java.util.Locale(split[0], split[1]);
    }

    /**
     * Constructor
     * 
     * @param language
     *            {@code LANGUGAE}
     */
    public Locale(LANGUAGE language) {
        this.language = language;
        String l = language.name();
        this.country = null;
        locale = LOCALE.valueOf(l + "_" + l.toUpperCase());
        javaLocale = new java.util.Locale(l, l.toLowerCase());
    }

    /**
     * Constructor
     * 
     * @param lang
     *            language code
     * @param country
     *            country code
     */
    public Locale(LANGUAGE language, COUNTRY country) {
        this.language = language;
        String l = language.name();
        String c = null;
        if (country == null) {
            this.country = null;
            c = l.toUpperCase();
        } else {
            this.country = country;
            c = country.name();
        }
        locale = LOCALE.valueOf(l + "_" + c);
        javaLocale = new java.util.Locale(l, c.toLowerCase());
    }

    /**
     * Constructor
     * 
     * @param lang
     *            language, two digit code
     * @param country
     *            country, two digit code
     */
    public Locale(String lang, String country) {
        if (country == null) {
            this.language = LANGUAGE.valueOf(lang);
            String l = language.name();
            this.locale = LOCALE.valueOf(l + "_" + l.toUpperCase());
            this.country = null;
        } else {
            this.language = LANGUAGE.valueOf(lang);
            String l = language.name();
            this.country = COUNTRY.valueOf(country);
            String c = this.country.name();
            this.locale = LOCALE.valueOf(l + "_" + c.toUpperCase());
        }
        javaLocale = new java.util.Locale(lang, country);
    }

    /**
     * Returns the {@code LOCALE}
     * 
     * @return {@code LOCALE}, e.g. de_AT
     */
    public LOCALE getLocale() {
        return locale;
    }

    /**
     * Returns the Java locale.
     * 
     * @return java.util.Locale
     */
    public java.util.Locale getJavaLocale() {
        return javaLocale;
    }

    /**
     * Returns the {@code LANGUAGE}
     * 
     * @return {@code LANGUAGE}, e.g de (<i>de</i>utsch = german)
     */
    public LANGUAGE getLanguage() {
        return language;
    }

    /**
     * Returns the {@code COUNTRY}
     * 
     * @return {@code COUNTRY}, e.g AT (=Austria)
     */
    public COUNTRY getCountry() {
        return country;
    }

}
