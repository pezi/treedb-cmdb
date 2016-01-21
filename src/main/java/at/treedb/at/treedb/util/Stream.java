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

import java.io.*;
import java.nio.charset.Charset;

/**
 * <p>
 * Class for reading, writing and copying streams.
 * </p>
 * 
 * @author Peter Sauer
 */
public class Stream {

    /**
     * Size of the internal copy buffer
     */
    public static final int COPY_BUF_SIZE = 64 * 1024;
    private static Charset charset = Charset.defaultCharset();

    public static Charset getCharset() {
        return charset;
    }

    public static void setCharset(Charset cset) {
        charset = cset;
    }

    /**
     * Reads a byte stream.
     * 
     * @param file
     *            file path
     * @return byte array
     */
    public static byte[] readByteStream(File file) throws Exception {
        FileInputStream reader = new FileInputStream(file);
        int fileLength = (int) file.length();
        byte[] stream = new byte[fileLength];
        reader.read(stream, 0, fileLength);
        reader.close();
        return stream;
    }

    /**
     * Reads a byte stream.
     * 
     * @param fileName
     *            file path
     * @return byte array
     */
    public static byte[] readByteStream(String fileName) throws Exception {
        return readByteStream(new File(fileName));
    }

    /**
     * Reads a stream and returns a character array.
     * 
     * @param file
     *            {@code File} object
     * @return character array
     */
    public static char[] readStreamAsCharArray(File file) throws Exception {
        byte[] buffer = readByteStream(file);
        String s = new String(buffer, 0, buffer.length, charset.name());
        return s.toCharArray();
    }

    public static char[] readStreamAsCharArray(String filePath) throws Exception {
        return readStreamAsCharArray(new File(filePath));
    }

    /**
     * Returns a character stream as a string.
     * 
     * @param file
     *            {@code File} object
     * @return String
     * @throws Exception
     */
    public static String readStreamAsString(File file) throws Exception {
        byte[] buffer = readByteStream(file);
        return new String(buffer, 0, buffer.length, charset.name());
    }

    public static String readStreamAsString(String filePath) throws Exception {
        return readStreamAsString(new File(filePath));
    }

    /**
     * Writes a character array.
     * 
     * @param fileName
     *            file path
     * @param carray
     *            character array
     * @throws Exception
     */
    public static void writeCharStream(String fileName, char[] carray) throws Exception {
        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(new File(fileName)), charset.name());
        osw.write(carray, 0, carray.length);
        osw.close();
    }

    /**
     * Writes a byte array.
     * 
     * @param fileName
     *            file path
     * @param barray
     *            byte array
     * @throws Exception
     */
    public static void writeByteStream(String fileName, byte[] barray) throws Exception {
        File file = new File(fileName);
        FileOutputStream writer = new FileOutputStream(file);
        writer.write(barray, 0, barray.length);
        writer.close();
    }

    /**
     * Writes a byte array.
     * 
     * @param file
     *            file {@code File} object
     * @param barray
     *            byte array
     * @throws Exception
     */
    public static void writeByteStream(File file, byte[] barray) throws Exception {
        FileOutputStream writer = new FileOutputStream(file);
        writer.write(barray, 0, barray.length);
        writer.close();
    }

    /**
     * Writes a string as stream.
     * 
     * @param fileName
     *            file path
     * @param str
     *            string
     * @throws Exception
     */
    public static void writeString(String fileName, String str) throws Exception {
        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(new File(fileName)), charset.name());
        osw.write(str);
        osw.close();
    }

    /**
     * Writes a string as stream.
     * 
     * @param file
     *            file path
     * @param str
     *            string
     * @throws Exception
     */
    public static void writeString(File file, String str) throws Exception {
        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file), charset.name());
        osw.write(str);
        osw.close();
    }

    /**
     * Copies a file.
     * 
     * @param src
     *            source file path
     * @param dst
     *            destination file path
     * @throws IOException
     */
    public static void copy(String src, String dst) throws IOException {
        copy(src, dst, Stream.COPY_BUF_SIZE);
    }

    /**
     * Copies a file.
     * 
     * @param src
     *            source file path
     * @param dst
     *            destination file path
     * @param bufSize
     *            size of the internal copy buffer in bytes
     * @throws IOException
     */
    public static void copy(String src, String dst, int bufSize) throws IOException {
        InputStream in = new FileInputStream(new File(src));
        OutputStream out = new FileOutputStream(new File(dst));
        // Transfer bytes from in to out
        byte[] buf = new byte[bufSize];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    /**
     * Deletes a directory with content.
     * 
     * @param dir
     *            directory
     * @throws IOException
     */
    public static void deleteDir(String dir) throws IOException {
        deleteDir(new File(dir));
    }

    /**
     * Deletes a directory with content.
     * 
     * @param dir
     *            directory
     * @throws IOException
     */
    public static void deleteDir(File dir) throws IOException {
        if (dir.isDirectory()) {
            for (File f : dir.listFiles())
                deleteDir(f);
        }
        if (!dir.delete()) {
            throw new FileNotFoundException("Stream.deleteDir(): Failed to delete: " + dir.getAbsolutePath());
        }
    }

    /**
     * Reads an input stream.
     * 
     * @param is
     *            input stream
     * @return byte array
     * @throws IOException
     */
    public static byte[] readInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 16];
        while (true) {
            int read = is.read(buffer, 0, buffer.length);
            if (read == -1) {
                break;
            }
            bos.write(buffer, 0, read);
        }
        bos.flush();
        return bos.toByteArray();
    }

    public static boolean isFilenameValid(String file) {
        File f = new File(file);
        try {
            f.getCanonicalPath();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
