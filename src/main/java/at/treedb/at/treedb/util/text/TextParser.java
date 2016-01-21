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

import javax.xml.parsers.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import at.treedb.i18n.Locale;
import java.io.*;

/**
 * <p>
 * XML Parser for reading the UI language strings.
 * </p>
 */
public class TextParser extends DefaultHandler {
    private String lang;
    private String country;
    private String id;
    private boolean inText = false;
    private TextGroup actualGroup;
    private Text text;
    private StringBuffer textBuffer;
    private SAXParserFactory factory;
    private SAXParser saxParser;

    public class DummyEntityResolver implements EntityResolver {
        public String dtd;

        public DummyEntityResolver(String dtd) {
            this.dtd = dtd;
        }

        public InputSource resolveEntity(String publicID, String systemID) throws SAXException {

            return new InputSource(new StringReader(dtd));
        }
    }

    /**
     * Reads and parses the XML file.
     * 
     * @throws Exception
     */
    public void parse() throws Exception {

        if (text.getSource() == Text.SOURCE.URI) {
            saxParser.parse(text.getURI(), this);
        } else if (text.getSource() == Text.SOURCE.FILE) {
            saxParser.parse(text.getFilePath(), this);
        } else {
            saxParser.parse(new InputSource(new StringReader(text.getXML())), this);
        }
    }

    /**
     * Reads and parses the XML file.
     * 
     * @throws Exception
     */
    public void parse(String dtd) throws Exception {
        XMLReader xmlReader = saxParser.getXMLReader();
        xmlReader.setEntityResolver(new DummyEntityResolver(dtd));
        xmlReader.setContentHandler(this);
        if (text.getSource() == Text.SOURCE.URI) {
            xmlReader.parse(text.getURI());
        } else if (text.getSource() == Text.SOURCE.FILE) {
            xmlReader.parse(text.getFilePath());
        } else {
            xmlReader.parse(new InputSource(new StringReader(text.getXML())));
        }
    }

    /**
     * Constructor
     * 
     * @param text
     * @throws Exception
     */
    public TextParser(Text text) throws Exception {
        factory = SAXParserFactory.newInstance();
        factory.setValidating(true);
        saxParser = factory.newSAXParser();
        this.text = text;
    }

    /**
     * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
     */
    @Override
    public void error(SAXParseException e) throws SAXParseException {
        throw e;
    }

    /**
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    @Override
    public void characters(char[] buf, int offset, int len) {
        if (inText) {
            textBuffer.append(buf, offset, len);
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String namespaceURI, String sName, String qName, Attributes attrs) throws SAXException {
        if (qName.equals("group")) {
            if (attrs != null) {
                for (int i = 0; i < attrs.getLength(); i++) {
                    String tmp = attrs.getQName(i);
                    String value = attrs.getValue(i);
                    if ("name".equals(tmp)) {
                        if (text != null) {
                            actualGroup = text.getGroupWithoutCheck(value);
                        }
                        if (actualGroup == null) {
                            actualGroup = new TextGroup(value);
                        }
                    }
                }
            }
        }
        if (qName.equals("text")) {
            if (attrs != null) {
                for (int i = 0; i < attrs.getLength(); i++) {
                    String tmp = attrs.getQName(i);
                    String value = attrs.getValue(i);
                    if ("id".equals(tmp)) {
                        inText = true;
                        id = value;
                    }
                }
            }
        }
        if (qName.equals("lang")) {
            if (attrs != null) {
                country = null;
                for (int i = 0; i < attrs.getLength(); i++) {
                    String tmp = attrs.getQName(i);
                    String value = attrs.getValue(i);
                    if ("id".equals(tmp)) {
                        lang = value;
                        textBuffer = new StringBuffer();
                    }
                    if (tmp.equals("country")) {
                        country = value;
                    }
                }
            }

        }

    }

    /**
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String namespaceURI, String sName, String qName) throws SAXException {
        if (qName.equals("group")) {
            text.putGroup(actualGroup);
        } else if (qName.equals("lang")) {
            Locale.COUNTRY c = null;
            if (country != null) {
                c = Locale.COUNTRY.valueOf(country.toUpperCase());
            }
            actualGroup.put(Locale.LANGUAGE.valueOf(lang), c, id, textBuffer.toString());
        } else if (qName.equals("text")) {
            inText = false;
        }
    }

    /**
     * Returns a {@code Text} object.
     * 
     * @return {@code Text} object
     */
    public Text getText() {
        return text;
    }

}
