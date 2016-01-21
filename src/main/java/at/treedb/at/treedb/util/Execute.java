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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;

/**
 * <p>
 * 
 * </p>
 * 
 * @author Peter Sauer
 *
 */
public class Execute {

    public static class ExecResult {
        private int exitCode;
        private String outputStream;
        private Exception exception;

        public ExecResult(int exitCode, String outputStream, Exception exception) {
            this.exitCode = exitCode;
            this.outputStream = outputStream;
            this.exception = exception;
        }

        public int getExitCode() {
            return exitCode;
        }

        public String getOutputStream() {
            return outputStream;
        }

        public Exception getException() {
            return exception;
        }

    }

    public static void convertVideo2FLV(String source) throws Exception {
        String FFMPEG = "ffmpeg";
        if (System.getProperty("os.name").toLowerCase().toLowerCase().startsWith("win")) {
            FFMPEG += ".exe";
        }
        String destination = source.replace(".mts", ".flv");
        String command = "-i ${source} -vcodec flv -f flv -r 25 -s 800x450 -aspect 16:9  -b 2000k -g 160 -cmp 2 -subcmp 2 -mbd 2 -trellis 2 -acodec libmp3lame -ac 2 -ar 44100 -ab 256k ${destination}";
        String command2 = "-i ${source} -ss 0 -vframes 1 -vcodec mjpeg -f image2 ${destination}";
        ExecResult r = null;
        Map<String, File> map = new HashMap<String, File>();
        if (!new File(destination).exists()) {
            map.put("source", new File(source));
            map.put("destination", new File(destination));
            System.out.println("call exec");
            r = execute(FFMPEG, command.split(" "), map);

            if (r.getExitCode() != 0) {

                throw new Exception(r.getOutputStream());
            }
        }
        source = destination;
        destination = source.replace(".flv", ".jpg");
        if (!new File(destination).exists()) {
            map = new HashMap<String, File>();
            map.put("source", new File(source));
            map.put("destination", new File(destination));

            r = execute(FFMPEG, command2.split(" "), map);

            if (r.getExitCode() != 0) {
                throw new Exception(r.getOutputStream());
            }
        }
    }

    public static void main(String args[]) throws ExecuteException, IOException {
        String command = "-i ${source} -vcodec flv -f flv -r 25 -s 800x450 -aspect 16:9  -b 2000k -g 160 -cmp 2 -subcmp 2 -mbd 2 -trellis 2 -acodec libmp3lame -ac 2 -ar 44100 -ab 256k ${destination}";
        String command2 = "-i ${source} -ss 0 -vframes 1 -vcodec mjpeg -f image2 ${destination}";

        Map<String, File> map = new HashMap<String, File>();
        map.put("source", new File("c:/tmp/bilder/elefant/00009.MTS"));
        map.put("destination", new File("c:/tmp/bilder/elefant/00009.flv"));
        execute("ffmpeg.exe", command.split(" "), map);

        map = new HashMap<String, File>();
        map.put("source", new File("c:/tmp/bilder/elefant/00009.flv"));
        map.put("destination", new File("c:/tmp/bilder/elefant/00009.jpg"));
        ExecResult r = execute("ffmpeg.exe", command2.split(" "), map);
        System.out.println(r.exitCode);

    }

    public static ExecResult execute(String command, String[] param, Map<String, File> map) {
        DefaultExecutor executor = new DefaultExecutor();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CommandLine cmdLine = null;
        int exitValue = 0;
        try {
            PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
            cmdLine = new CommandLine(command);
            if (param != null) {
                for (String s : param) {
                    s = s.trim();
                    if (s.isEmpty()) {
                        continue;
                    }
                    cmdLine.addArgument(s);
                }
            }
            cmdLine.setSubstitutionMap(map);
            executor.setStreamHandler(streamHandler);
            exitValue = executor.execute(cmdLine);

            return new ExecResult(exitValue, outputStream.toString(), null);
        } catch (Exception e) {
            return new ExecResult(-1, outputStream.toString(), e);
        }

    }

}
