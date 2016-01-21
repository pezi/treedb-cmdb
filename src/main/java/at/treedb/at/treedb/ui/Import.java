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
package at.treedb.ui;

import java.util.HashMap;
import java.util.HashSet;

/**
 * <p>
 * Class/directive for<br>
 * ... explicit tab update. e.g {@code Tab 2} imports data from {@code Tab 1} to
 * indicate that after a data update of {@code Tab 1} the content of
 * {@code Tab 2} must be updated.<br>
 * ... import CI data from other nodes.<br>
 * <p>
 * <p>
 * Update data tabs of the same CI:<br>
 * <code>*</code> ... updates all tabs of the CI</br>
 * <code>test1,test2</code> ... updates the tabs with this name of the CI</br>
 * <br>
 * Data Import form other CIs:<br>
 * Example: <code>parent.[*]|child1.[test1,test2]</code> ... import form parent
 * CI all tab data<br>
 * ... import form child level 2 (self.child.child) the data of the tabs
 * <code>test1</code> and <code>test2</code><br>
 * </p>
 * 
 * @author Peter Sauer
 * 
 */
public class Import {
    private HashSet<String> tabUpdate;
    private HashMap<Integer, HashSet<String>> parentImport;
    private HashMap<Integer, HashSet<String>> childImport;

    /**
     * Constructor
     */
    public Import() {
        tabUpdate = new HashSet<String>();
        parentImport = new HashMap<Integer, HashSet<String>>();
        childImport = new HashMap<Integer, HashSet<String>>();
    }

    /**
     * Constructor
     * 
     * @param updates
     *            list of tab updates
     * @param imports
     *            list of import commands
     */
    public Import(String updates, String imports) {
        this();
        if (updates != null) {
            String[] tabs = updates.split(",");
            for (String s : tabs) {
                tabUpdate.add(s.trim());
            }
        }
        if (imports != null) {
            String[] split = imports.split("\\|");
            for (int i = 0; i < split.length; ++i) {
                String[] parts = split[i].split("\\.");
                HashSet<String> t = new HashSet<String>();
                int level = 1;
                if (parts[0].startsWith("parent")) {
                    String tmp = parts[0].substring("parent".length());
                    if (!tmp.isEmpty()) {
                        level = Integer.parseInt(tmp);
                    }
                    checkLevel(level);
                    String s = parts[1].trim();
                    s = s.substring(1, s.length() - 1);
                    for (String imp : s.split(",")) {
                        t.add(imp);
                    }
                    parentImport.put(level, t);
                } else if (parts[0].startsWith("child")) {
                    String tmp = parts[0].substring("child".length());
                    if (!tmp.isEmpty()) {
                        level = Integer.parseInt(tmp);
                    }
                    checkLevel(level);
                    String s = parts[1].trim();
                    s = s.substring(1, s.length() - 1);
                    for (String imp : s.split(",")) {
                        t.add(imp);
                    }
                    childImport.put(level, t);
                } else {
                    throw new IllegalArgumentException("Invalid relative: " + parts[0]);
                }
            }
        }
    }

    /**
     * Adds a tab for a explicit update by its name
     * 
     * @param name
     *            internal tab name
     */
    public void addTab(String name) {
        tabUpdate.add(name);
    }

    /**
     * Adds a tab for a explicit update
     */
    public void addAllTabs() {
        tabUpdate.clear();
        tabUpdate.add("*");
    }

    private void checkLevel(int level) throws IllegalArgumentException {
        if (level < 1) {
            throw new IllegalArgumentException("Import: parameter level is invalid");
        }
    }

    /**
     * Add a tab of a 'parent' {@code CI} for data import by its name.
     * 
     * @param level
     *            parent level, value must be > 0
     * @param name
     *            name of the tab
     */
    public void addTabParent(int level, String name) {
        checkLevel(level);
        HashSet<String> list = parentImport.get(level);
        if (list == null) {
            list = new HashSet<String>();
            parentImport.put(level, list);
        }
        list.add(name);
    }

    /**
     * Add all tabs of a 'parent' {@code CI} for data import.
     * 
     * @param level
     *            parent level, value must be > 0
     */
    public void addAllTabsParent(int level) {
        checkLevel(level);
        HashSet<String> list = parentImport.get(level);
        if (list == null) {
            list = new HashSet<String>();
            parentImport.put(level, list);
        }
        list.clear();
        list.add("*");
    }

    /**
     * Add a tab of a 'child' {@code CI} for data import by its name.
     * 
     * @param level
     *            parent level, value must be > 0
     * @param name
     *            name of the tab
     */
    public void addTabChild(int level, String name) {
        checkLevel(level);
        HashSet<String> list = childImport.get(level);
        if (list == null) {
            list = new HashSet<String>();
            childImport.put(level, list);
        }
        list.add(name);
    }

    /**
     * Add all tabs of a 'child' {@code CI} for data import.
     * 
     * @param level
     *            parent level, value must be > 0
     */
    public void addChildAllTabs(int level) {
        checkLevel(level);
        HashSet<String> list = childImport.get(level);
        if (list == null) {
            list = new HashSet<String>();
            childImport.put(level, list);
        }
        list.clear();
        list.add("*");
    }

    /**
     * Returns the tab updates as a string.
     * 
     * @return tab updates
     */
    public String getUpdates() {
        StringBuffer buf = new StringBuffer();
        int index = 0;
        for (String i : tabUpdate) {
            if (index > 0) {
                buf.append(",");
            }
            buf.append(i);
            ++index;
        }
        if (buf.length() == 0) {
            return null;
        }
        return buf.toString();
    }

    /**
     * Returns the tab imports from other CIs as a string.
     * 
     * @return tab tab imports
     */
    public String getImports() {
        StringBuffer buf = new StringBuffer();
        int index = 0;
        HashMap<Integer, HashSet<String>> relative;
        String relativeName;
        for (int i = 0; i < 2; ++i) {
            if (i == 0) {
                relative = parentImport;
                relativeName = "parent";
            } else {
                relative = childImport;
                relativeName = "child";
                if (childImport.size() > 0 && parentImport.size() > 0) {
                    buf.append("|");
                }
            }
            if (relative.size() > 0) {
                index = 0;
                for (Integer level : relative.keySet()) {
                    if (index > 0) {
                        buf.append("|");
                    }
                    HashSet<String> list = relative.get(level);
                    buf.append(relativeName);
                    buf.append(level);
                    buf.append(".[");
                    int p = 0;
                    for (String tab : list) {
                        if (p > 0) {
                            buf.append(",");
                        }
                        buf.append(tab);
                        ++p;
                    }
                    buf.append("]");
                    ++index;
                }
            }
        }
        if (buf.length() == 0) {
            return null;
        }
        return buf.toString();
    }

    /**
     * Checks if a tab should be updated.
     * 
     * @param update
     *            internal tab name
     * @return
     */
    public boolean checkUpdate(String update) {
        return tabUpdate.contains(update);
    }

    /**
     * Returns the 'parent' import list.
     * 
     * @return 'parent' import list
     */
    public HashMap<Integer, HashSet<String>> getParentImports() {
        return parentImport;
    }

    /**
     * Returns the 'child' import list.
     * 
     * @return
     */
    public HashMap<Integer, HashSet<String>> getChildImports() {
        return childImport;
    }

    public static void main(String args[]) {
        Import i = new Import();
        i.addTab("test");
        i.addTab("test12");
        i.addTabChild(1, "test1");
        i.addTabChild(1, "test2");
        i.addTabChild(3, "test2");
        i.addAllTabsParent(3);
        System.out.println(i.getImports());
        Import p = new Import("*", i.getImports());
        System.out.println(p.getImports());
    }
}
