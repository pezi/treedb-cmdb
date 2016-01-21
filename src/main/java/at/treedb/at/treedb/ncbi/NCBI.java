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
package at.treedb.ncbi;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Helper class for the National Center for Biotechnology Information (NCBI)
 * Taxonomy database http://www.ncbi.nlm.nih.gov/taxonomy Data source:
 * ftp://ftp.ncbi.nih.gov/pub/taxonomy/
 * 
 * @author Peter Sauer
 * 
 */
public class NCBI {
    private static NCBI instance;

    private HashMap<Integer, String> revNameMap = new HashMap<Integer, String>();
    private HashMap<String, Integer> nameMap = new HashMap<String, Integer>();
    private HashMap<Integer, Integer> nodesMap = new HashMap<Integer, Integer>();
    private HashMap<Integer, String> rankMap = new HashMap<Integer, String>();

    private NCBI(String path) throws Exception {
        init(path);
    }

    public static NCBI createInstance(String path) throws Exception {
        if (instance == null) {
            return instance = new NCBI(path);
        }
        return instance;
    }

    public static synchronized NCBI getInstance() {
        return instance;
    }

    private String extractFile(ZipInputStream zis, ZipEntry ze) throws IOException {
        int size = (int) ze.getSize();
        byte[] data = new byte[(int) size];
        int index = 0;

        while (true) {
            int read = zis.read(data, index, size);
            if (read == -1) {
                break;
            }
            index += read;
            size -= read;
            if (size == 0) {
                break;
            }
        }
        return new String(data);
    }

    private void init(String path) throws Exception {
        ZipInputStream zis = new ZipInputStream(new FileInputStream(path));
        ZipEntry ze;
        while ((ze = zis.getNextEntry()) != null) {
            if (ze.getName().equals("names.dmp")) {
                String names = extractFile(zis, ze);
                String[] lines = names.split("\n");
                for (String l : lines) {
                    String[] col = l.split("\\|");
                    String name = col[1].trim();
                    int tmp = Integer.parseInt(col[0].trim());
                    if (!revNameMap.containsKey(tmp)) {
                        revNameMap.put(tmp, name);
                    }
                    nameMap.put(name, tmp);
                }
            }
            if (ze.getName().equals("nodes.dmp")) {
                String nodes = extractFile(zis, ze);
                String[] lines = nodes.split("\n");
                for (String l : lines) {
                    String[] col = l.split("\\|");
                    int tmp = Integer.parseInt(col[0].trim());
                    nodesMap.put(tmp, Integer.parseInt(col[1].trim()));
                    rankMap.put(tmp, col[2].trim());
                }
            }

        }
        System.gc();
    }

    public Unit get(String name) {
        Integer start = nameMap.get(name);
        if (start != null) {
            return new Unit(revNameMap.get(start), rankMap.get(start));
        }
        return null;
    }

    public ArrayList<Unit> search(String name) {
        ArrayList<Unit> list = new ArrayList<Unit>();
        for (String s : nameMap.keySet()) {
            if (s.contains(name)) {
                list.add(new Unit(s, rankMap.get(nameMap.get(s))));
            }
        }
        return list;
    }

    public ArrayList<Unit> searchSpecies(String name, int limit) {
        ArrayList<Unit> list = new ArrayList<Unit>();
        int index = 0;
        name = name.toLowerCase();
        for (String s : nameMap.keySet()) {
            if (rankMap.get(nameMap.get(s)).equals("species")) {
                if (s.toLowerCase().contains(name)) {
                    list.add(new Unit(s, rankMap.get(nameMap.get(s))));
                    ++index;
                    if (index == limit) {
                        break;
                    }
                }
            }
        }
        return list;
    }

    public ArrayList<Unit> traverse(Unit unit) {
        ArrayList<Unit> list = new ArrayList<Unit>();

        Integer start = nameMap.get(unit.getName());
        int last;
        if (start != null) {

            while (true) {
                String rank = rankMap.get(start);
                if (!rank.startsWith("no rank")) {
                    list.add(new Unit(revNameMap.get(start), rank));
                }
                last = start;
                start = nodesMap.get(start);
                if (last == start) {
                    break;
                }
            }
        }
        Collections.reverse(list);
        return list;

    }

    public HashMap<Integer, String> getRankMap() {
        return rankMap;
    }

    public HashMap<String, Integer> getNameMap() {
        return nameMap;
    }

    public static void main(String args[]) throws Exception {
        NCBI n = NCBI.createInstance("c:/tmp/taxdmp.zip");
        ArrayList<Unit> list = n.searchSpecies("Bufo b", 200);
        for (Unit u : list) {
            System.out.println(u.getName());
        }
        /*
         * ArrayList<Unit> list = n.traverse(n.get("Cleome spinosa")); for(Unit
         * u:list) { System.out.println(u.getName() + " " +
         * u.getRank().toString()); }
         */
        /*
         * for (String s : n.getNameMap().keySet()) { String rank =
         * n.getRankMap().get(n.getNameMap().get(s)); if
         * (rank.equals("kingdom")) { System.out.println(s); } }
         */

    }

}
