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

import at.treedb.ci.CI;

/**
 * Taxonomic unit.
 * 
 * @author Peter Sauer
 * 
 */
public class Unit {
    private int tsn;
    private int parentTsn;
    private String name;
    private boolean isInvalid;
    private InvalidName invalidName;
    private UnitType type;
    private static UnitType.Rank[] rank = UnitType.Rank.values();

    public Unit(int tsn, int parentTsn, String name, UnitType type) {
        this.isInvalid = false;
        this.tsn = tsn;
        this.parentTsn = parentTsn;
        this.name = name;
        this.type = type;
    }

    /**
     * Returns a unique CI name with following coding: 'CIname xx' xx is the
     * taxonomic rank as a number.<br>
     * e.g. Animalia 00 stands for Animalia Kingdom<br>
     * This handling is necessary due the fact, that some taxonomic names are in
     * multiple use for different ranks.
     * 
     * @return unique CI name
     */
    public String getCIname() {
        String rank = "";
        if (getType().getRank().ordinal() < 10) {
            rank = "0";
        }
        rank += getType().getRank().ordinal();
        rank = name + " " + rank;
        return rank;
    }

    /**
     * Returns the display name of the taxonomic CI containing name and rank.
     * 
     * @param ci
     * @return display name - e.g. Animalia (Kingdom)
     */
    public static String getDisplayName(CI ci) {
        StringBuffer buf = new StringBuffer();
        buf.append(ci.getName().substring(0, ci.getName().length() - 2));
        buf.append("(");
        buf.append(rank[ci.getCItypeObj().getInternId()]);
        buf.append(")");
        return buf.toString();
    }

    /**
     * Returns the display name of the taxonomic CI containing name and rank.
     * 
     * @return display name - e.g. Animalia (Kingdom)
     */
    public String getDisplayName() {
        StringBuffer buf = new StringBuffer();
        buf.append(name);
        buf.append("(");
        buf.append(rank[getType().getRank().ordinal()]);
        buf.append(")");
        return buf.toString();
    }

    public Unit(int tsn, int parentTsn, String name, UnitType type, InvalidName invalidName) {
        this.isInvalid = true;
        this.tsn = tsn;
        this.parentTsn = parentTsn;
        this.name = name;
        this.type = type;
        this.invalidName = invalidName;
    }

    public String getName() {
        return name;
    }

    public UnitType getType() {
        return type;
    }

    public UnitType.MajorRank getSimpleRank() {
        return type.getMajorRank();
    }

    public int getTSN() {
        return tsn;
    }

    public int getParentTSN() {
        return parentTsn;
    }

    public boolean isInvalid() {
        return isInvalid;
    }

    public InvalidName getInvalidName() {
        return invalidName;
    }

}
