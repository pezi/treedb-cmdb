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

import java.util.HashMap;
import java.io.File;

/**
 * <p>
 * Class
 * </p>
 */
public class TextLib {
    private static HashMap<String, Text> libMap = new HashMap<String, Text>();

    /**
     * Load a XML language file
     * 
     * @param contentName
     *            inten
     * @param path
     *            Pfad der XML-Sprachdatei
     * @return
     * @throws Exception
     */
    public static synchronized Text loadFile(String contentName, String path) throws Exception {

        Text text = (Text) libMap.get(contentName);
        long dolm = new File(path).lastModified();
        if (text == null || dolm != text.getLastModfied()) {
            path = "file:///" + path.replace('\\', '/');
            text = new Text(path, Text.SOURCE.FILE);
            libMap.put(contentName, text);
        }
        return text;
    }

    /**
     * 
     * @param contentName
     * @param url
     * @return
     * @throws Exception
     */
    public static synchronized Text loadURL(String contentName, String url) throws Exception {
        Text text = (Text) libMap.get(contentName);
        if (text == null) {
            text = new Text(url, Text.SOURCE.URI);
            libMap.put(contentName, text);
        }
        return text;
    }

    /**
     * 
     * @param textName
     * @return
     */
    public static Text getText(String textName) {
        return libMap.get(textName);
    }

}
