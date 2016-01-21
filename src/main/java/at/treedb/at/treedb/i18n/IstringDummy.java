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

/**
 * Dummy string for update operations.
 * 
 * @author Peter Sauer
 * 
 */
public class IstringDummy {
    // language limited to two characters - e.g. de
    Locale.LANGUAGE language;
    // country code limited to two characters, e.g. at
    Locale.COUNTRY country;
    // text string
    private String text;
    // private CID.ClassID cid;

    public IstringDummy(String text, Locale.LANGUAGE language, Locale.COUNTRY country) {
        this.setText(text);
        this.setLanguage(language);
        this.setCountry(country);
    }

    public IstringDummy(Istring i) {
        this.setText(i.getText());
        this.setLanguage(i.getLanguage());
        this.setCountry(i.getCountry());
    }

    public IstringDummy(String text, Locale.LANGUAGE language) {
        this.setText(text);
        this.setLanguage(language);
    }

    public Locale.LANGUAGE getLanguage() {
        return language;
    }

    public void setLanguage(Locale.LANGUAGE language) {
        this.language = language;
    }

    public Locale.COUNTRY getCountry() {
        return country;
    }

    public void setCountry(Locale.COUNTRY country) {
        this.country = country;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
