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
import java.io.*;

import at.treedb.i18n.Locale;
import at.treedb.util.Stream;

/**
 * <p>
 * XML (with DTD) based i18n text strings
 * </p>
 * 
 * @author Peter Sauer
 */
public class Text {

    public static String DTD = "<?xml version='1.0' encoding='utf-8'?>" + "<!ELEMENT content (group)+>"
            + "<!ELEMENT group (text)+>" + "<!ATTLIST group  name CDATA  #REQUIRED>" + "<!ELEMENT text (lang)+>"
            + "<!ATTLIST text" + "  id CDATA  #REQUIRED" + "  country CDATA #IMPLIED" + ">"
            + "<!ELEMENT lang (#PCDATA) >" + "<!ATTLIST lang"
            + "  id (de | en | fr | it | es | pl | nl | ru | sys ) #REQUIRED"
            + "  country (at | de | ch | uk | us | ru) #IMPLIED" + ">";

    public enum SOURCE {
        FILE, URI, STRING
    }

    private HashMap<String, TextGroup> groups = new HashMap<String, TextGroup>();

    private String data;
    private SOURCE source;
    private TextParser parser;
    private long lastModified = 0;

    /**
     * Constructor
     * 
     * @param path
     *            path of the XML
     */
    public Text(String data, SOURCE source) throws Exception {
        this.data = data;
        this.source = source;
        parser = new TextParser(this);
        parser.parse();
    }

    /**
     * Constructor
     * 
     * @param path
     *            path of the XML
     */
    public Text(String data, SOURCE source, String dtd) throws Exception {
        this.data = data;
        this.source = source;
        parser = new TextParser(this);
        parser.parse(dtd);
    }

    /**
     * Saves the last date of the file modification.
     * 
     * @param time
     *            date of last change
     */
    public void setLastModified(long time) {
        lastModified = time;
    }

    /**
     * Returns the date of the last modification.
     * 
     * @return date of the last modification
     */
    public long getLastModfied() {
        return lastModified;
    }

    /**
     * Stores a {@code Group}.
     * 
     * @param group
     *            {@code Group}
     */
    public void putGroup(TextGroup group) {
        groups.put(group.getName(), group);
    }

    /**
     * Returns a {@code Group}.
     * 
     * @param language
     *            language of the group
     * @param country
     *            optional country of the group
     * @param name
     *            name of the group
     * @return group {@code Group}
     */
    public TextGroup getGroup(String name, Locale.LANGUAGE language, Locale.COUNTRY country) throws Exception {
        if (source == Text.SOURCE.FILE) {
            long lm = new File(getFilePath()).lastModified();
            if (lm != lastModified) {
                // reload & parse XML file!
                lastModified = lm;
                parser.parse();
            }
        }

        TextGroup g = groups.get(name);
        if (g != null) {
            // clone group and set the language/country
            g = new TextGroup(g, language, country);
        }
        return g;
    }

    /**
     * Returns a {@code Group} without checking if the XML file was changed.
     * 
     * @param name
     *            name of the group
     * @return group {@code Group}
     */
    public TextGroup getGroupWithoutCheck(String name) {
        return (TextGroup) groups.get(name);
    }

    /**
     * Returns the path of the XML file.
     * 
     * @return path of the XML file
     * @throws Exception
     */
    public String getURI() throws Exception {
        if (this.source != Text.SOURCE.URI) {
            throw new Exception("Text.getURI(): wrong XML source");
        }
        return data;
    }

    /**
     * Returns the file path of the XML file.
     * 
     * @return path of the XML file
     */
    public String getFilePath() throws Exception {
        if (this.source != Text.SOURCE.FILE) {
            throw new Exception("Text.getFilePath(): wrong XML source");
        }
        if (data.startsWith("file:///")) {
            return data.substring("file:///".length());
        }
        return data;
    }

    public String getXML() throws Exception {
        if (this.source != Text.SOURCE.STRING) {
            throw new Exception("Text.getXML(): wrong XML source");
        }
        return data;
    }

    /**
     * 
     * @return
     */
    public SOURCE getSource() {
        return source;
    }

    public static void main(String[] args) throws Exception {

        Text text = new Text(
                Stream.readStreamAsString("c:/develop/eclipse/CMDB/TreeDB/WebContent/WEB-INF/text/content.xml"),
                Text.SOURCE.STRING,
                Stream.readStreamAsString("c:/develop/eclipse/CMDB/TreeDB/WebContent/WEB-INF/text/text.dtd"));
        TextGroup g = text.getGroup("gui", Locale.LANGUAGE.en, null);
        System.out.println(g.get("search"));
    }

    public TextParser getTextParser() {
        return parser;
    }

}
