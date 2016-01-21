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

import java.io.File;
import java.io.IOException;

public class FileStorage {
    private static FileStorage instance;
    boolean hasFileStorage;
    boolean isCleanUp;
    private String storageDir;
    private String tmpDir;
    private String binaryDir;
    private long counter = System.currentTimeMillis();

    private FileStorage(String storageDir, boolean tmpCleanUp) throws Exception {

        if (storageDir != null) {
            this.hasFileStorage = true;
            isCleanUp = tmpCleanUp;
            File file = new File(storageDir);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    throw new Exception("Unable to create file storage directory: " + storageDir);
                }
            }
            storageDir = file.getCanonicalPath();
            this.storageDir = storageDir;
            tmpDir = storageDir + "/tmp/";
            file = new File(tmpDir);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    throw new Exception("Unable to create temp directory: " + tmpDir);
                }
            }
        }
    }

    public static synchronized FileStorage getInstance() throws Exception {
        if (instance == null) {
            throw new Exception("FileStorage not initalized");
        }
        return instance;
    }

    public static synchronized FileStorage init(String storageDir, boolean tmpCleanUp) throws Exception {
        instance = new FileStorage(storageDir, tmpCleanUp);
        return instance;
    }

    public File createTempFile(String prefix, String suffix) throws IOException {
        if (hasFileStorage) {
            if (prefix == null) {
                prefix = "";
            }
            if (suffix == null) {
                suffix = ".tmp";
            } else {
                if (!suffix.startsWith(".")) {
                    suffix = "." + suffix;
                }
            }
            File f = new File(tmpDir + prefix + "_" + counter++ + suffix);
            System.out.println(f.getCanonicalPath());
            return f;
        } else {
            return File.createTempFile(prefix, suffix);
        }
    }
}
