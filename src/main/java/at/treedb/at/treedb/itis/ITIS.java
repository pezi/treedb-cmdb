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
package at.treedb.itis;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import at.treedb.itis.UnitType.Kingdom;
import at.treedb.itis.UnitType.Rank;

/**
 * Helper class to access the Integrated Taxonomic Information System (ITIS).
 * 
 * Homepage : http://www.itis.gov Data model:
 * http://www.itis.gov/pdf/phys_mod.pdf
 * http://www.itis.gov/pdf/references_online_model.pdf
 * 
 * @author Peter Sauer
 * 
 */
public class ITIS {
    private static ITIS instance;
    private Connection conn;
    private String dbURL;
    private String user;
    private String password;

    // complete List
    private static ArrayList<Unit> completeList = null;
    private static HashMap<Integer, Unit> completeMap = null;
    private static HashMap<String, UnitType> map;

    private ITIS(String dbURL, String user, String pwd) throws Exception {
        String tmp = dbURL.toLowerCase();
        String dbClass = null;
        if (tmp.startsWith("jdbc:mysql:")) {
            dbClass = "com.mysql.jdbc.Driver";
        } else if (tmp.startsWith("jdbc:sqlite:")) {
            dbClass = "org.sqlite.JDBC";
        } else if (tmp.startsWith("jdbc:postgres:")) {
            dbClass = "org.postgresql.Driver";
        }
        if (dbClass != null) {
            Class.forName(dbClass);
        }

        this.dbURL = dbURL;
        this.user = user;
        this.password = pwd;
    }

    public static boolean testConnection(String dbURL, String user, String pwd) throws Exception {
        String tmp = dbURL.toLowerCase();
        String dbClass = null;
        if (tmp.startsWith("jdbc:mysql:")) {
            dbClass = "com.mysql.jdbc.Driver";
        } else if (tmp.startsWith("jdbc:sqlite:")) {
            dbClass = "org.sqlite.JDBC";
        } else if (tmp.startsWith("jdbc:postgres:")) {
            dbClass = "org.postgresql.Driver";
        } else {
            throw new Exception("Unsupported data base driver!");
        }
        if (dbClass != null) {
            Class.forName(dbClass);
        }
        DriverManager.setLoginTimeout(3);
        Connection con = DriverManager.getConnection(dbURL, user, pwd);
        con.close();
        return true;

    }

    public void open() throws SQLException {
        instance.conn = DriverManager.getConnection(dbURL, user, password);
    }

    /**
     * Close SQL connection.
     * 
     * @throws SQLException
     */
    public void close() throws SQLException {
        if (conn != null) {
            conn.close();
        }
        // instance = null;
    }

    /**
     * Returns the SQL connection.
     * 
     * @return {@code Connection} SQL connection
     */
    public Connection getConnection() {
        return conn;
    }

    /**
     * Creates an ITIS instance.
     * 
     * @param dbURL
     * @param user
     * @param pwd
     * @return
     * @throws Exception
     */
    public static synchronized ITIS createInstance(String dbURL, String user, String pwd) throws Exception {
        if (instance == null) {
            return instance = new ITIS(dbURL, user, pwd);
        }
        return instance;
    }

    public static synchronized ITIS getInstance() throws Exception {

        return instance;
    }

    public String getDBurl() {
        return dbURL;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public ArrayList<Unit> search(String name) throws Exception {
        Statement stmt = conn.createStatement();
        ArrayList<Unit> list = new ArrayList<Unit>();
        ResultSet rs = stmt.executeQuery(
                "select tsn,parent_tsn,complete_name,kingdom_id,rank_id from taxonomic_units where complete_name = '"
                        + name + "'");

        while (rs.next()) {
            int k = rs.getInt("kingdom_id");
            int r = rs.getInt("rank_id");
            list.add(new Unit(rs.getInt("tsn"), rs.getInt("parent_tsn"), rs.getString("complete_name"), getType(k, r)));
        }
        stmt.close();
        return list;
    }

    public ArrayList<Unit> searchSpecies(String name) throws Exception {
        Statement stmt = conn.createStatement();
        ArrayList<Unit> list = new ArrayList<Unit>();
        ResultSet rs = stmt.executeQuery(
                "select tsn,parent_tsn,complete_name,kingdom_id,rank_id,n_usage,unaccept_reason from taxonomic_units where LOWER(complete_name) like LOWER('"
                        + name + "') and rank_id = 220");

        ArrayList<InvalidName> invalid = new ArrayList<InvalidName>();
        HashSet<Integer> tnsSet = new HashSet<Integer>();
        while (rs.next()) {
            String usage = rs.getString("n_usage");
            if (usage == null) {
                continue;
            }
            int tsn = rs.getInt("tsn");
            if (!(usage.equals("accepted") || usage.equals("valid"))) {
                invalid.add(new InvalidName(tsn, rs.getString("complete_name"), rs.getString("unaccept_reason")));
                continue;
            }
            int k = rs.getInt("kingdom_id");
            int r = rs.getInt("rank_id");
            if (tnsSet.contains(tsn)) {
                continue;
            }
            tnsSet.add(tsn);
            list.add(new Unit(rs.getInt("tsn"), rs.getInt("parent_tsn"), rs.getString("complete_name"), getType(k, r)));
        }
        for (InvalidName i : invalid) {
            rs = stmt.executeQuery("select tsn_accepted from synonym_links where tsn =" + i.getTSN());
            int tsn = -1;
            while (rs.next()) {
                tsn = rs.getInt("tsn_accepted");
            }
            if (tsn == -1) {
                continue;
            }
            if (tnsSet.contains(tsn)) {
                continue;
            }
            rs = stmt.executeQuery(
                    "select tsn,parent_tsn,complete_name,kingdom_id,rank_id from taxonomic_units where tsn =" + tsn);
            while (rs.next()) {
                int k = rs.getInt("kingdom_id");
                int r = rs.getInt("rank_id");
                tnsSet.add(tsn);
                list.add(new Unit(rs.getInt("tsn"), rs.getInt("parent_tsn"), rs.getString("complete_name"),
                        getType(k, r), i));
            }
        }

        stmt.close();
        return list;
    }

    public ArrayList<Unit> getAllSpecies() throws Exception {
        Statement stmt = conn.createStatement();
        ArrayList<Unit> list = new ArrayList<Unit>();
        ResultSet rs = stmt.executeQuery(
                "select tsn,parent_tsn,complete_name,kingdom_id,rank_id from taxonomic_units where rank_id = 220");

        while (rs.next()) {
            int k = rs.getInt("kingdom_id");
            int r = rs.getInt("rank_id");
            list.add(new Unit(rs.getInt("tsn"), rs.getInt("parent_tsn"), rs.getString("complete_name"), getType(k, r)));
        }
        stmt.close();
        return list;
    }

    public HashMap<String, Unit> getAllSpeciesAsMap() throws Exception {
        Statement stmt = conn.createStatement();
        HashMap<String, Unit> map = new HashMap<String, Unit>();
        ResultSet rs = stmt.executeQuery(
                "select tsn,parent_tsn,complete_name,kingdom_id,rank_id from taxonomic_units where rank_id = 220");

        while (rs.next()) {
            int k = rs.getInt("kingdom_id");
            int r = rs.getInt("rank_id");
            Unit u = new Unit(rs.getInt("tsn"), rs.getInt("parent_tsn"), rs.getString("complete_name"), getType(k, r));
            map.put(u.getName().toLowerCase(), u);
        }
        stmt.close();

        return map;
    }

    public ArrayList<Unit> getAll() throws Exception {
        if (completeList == null) {
            completeList = new ArrayList<Unit>();
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt
                    .executeQuery("select tsn,parent_tsn,complete_name,kingdom_id,rank_id from taxonomic_units");

            while (rs.next()) {
                int k = rs.getInt("kingdom_id");
                int r = rs.getInt("rank_id");
                completeList.add(new Unit(rs.getInt("tsn"), rs.getInt("parent_tsn"), rs.getString("complete_name"),
                        getType(k, r)));
            }
            stmt.close();
        }
        return completeList;
    }

    public HashMap<Integer, Unit> getAllAsMap() throws Exception {
        if (completeList == null) {
            getAll();
        }
        if (completeMap == null) {
            completeMap = new HashMap<Integer, Unit>();
            for (Unit u : completeList) {
                completeMap.put(u.getTSN(), u);
            }

        }
        return completeMap;
    }

    public Unit get(int tsn) throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
                "select tsn,parent_tsn,complete_name,kingdom_id,rank_id from taxonomic_units where tsn = " + tsn);
        Unit u = null;
        while (rs.next()) {
            int k = rs.getInt("kingdom_id");
            int r = rs.getInt("rank_id");
            u = new Unit(rs.getInt("tsn"), rs.getInt("parent_tsn"), rs.getString("complete_name"), getType(k, r));
        }
        stmt.close();
        return u;
    }

    public ArrayList<Unit> traverse(HashMap<Integer, Unit> map, Unit u) throws ClassNotFoundException, SQLException {
        ArrayList<Unit> list = new ArrayList<Unit>();
        list.add(u);
        while (true) {
            u = map.get(u.getParentTSN());
            if (u != null) {
                list.add(u);
            } else {
                break;
            }
        }
        Collections.reverse(list);
        return list;
    }

    public ArrayList<Unit> traverse(Unit u) throws Exception {
        ArrayList<Unit> list = new ArrayList<Unit>();
        list.add(u);
        while (true) {
            u = get(u.getParentTSN());
            if (u != null) {
                list.add(u);
            } else {
                break;
            }
        }
        Collections.reverse(list);
        return list;
    }

    public UnitType getType(int kingdomId, int unitId) throws Exception {
        if (map == null) {
            map = new HashMap<String, UnitType>();
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery("select kingdom_id,rank_id,rank_name from taxon_unit_types");

            while (rs.next()) {
                int kingdom = rs.getInt("kingdom_id");
                map.put(kingdom + "_" + rs.getInt("rank_id"), new UnitType(Kingdom.values()[kingdom - 1],
                        Enum.valueOf(Rank.class, rs.getString("rank_name"))));
            }
            stmt.close();

        }
        return map.get(kingdomId + "_" + unitId);
    }

}
