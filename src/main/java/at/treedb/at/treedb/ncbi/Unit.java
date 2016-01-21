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

import at.treedb.ci.CI;

public class Unit {

    private String name;
    private Rank rank;
    private static Rank[] rankArray = Rank.values();

    public Unit(String name, String rank) {
        this.name = name;
        this.rank = Rank.valueOf(Character.toUpperCase(rank.charAt(0)) + rank.substring(1));
    }

    public String getName() {
        return name;
    }

    public Rank getRank() {
        return rank;
    }

    public enum Kingdom {
        Animalia, Fungi,
        // Metazoa,
        // Viridiplantae,
        Chlorobionta
    }

    public enum MajorRank {
        Kingdom, Phylum, Class, Order, Family, Genus, Species, Unspecified
    }

    public enum Rank {
        // kingdom
        Superkingdom, Kingdom, Subkingdom,
        // phylum
        Superphylum, Phylum, Subphylum,
        // class
        Superclass, Class, Subclass, Infraclass,
        // order
        Superorder, Order, Suborder, Infraorder, Parvorder,
        // family
        Superfamily, Family, Subfamily, Tribe, Subtribe,
        // genus
        Genus, Subgenus,
        // species
        Speciesgroup, Speciessubgroup, Species, Subspecies, Varietas, Forma,
        // undefined
        Norank
    }

    /**
     * Returns the major rank of a given rank. e.g 'Subclass' would be mapped to
     * 'Class'
     * 
     * @return major rank
     */
    public static MajorRank getMajorRank(Rank r) {
        int ordinal = r.ordinal();
        if (ordinal >= Rank.Kingdom.ordinal() && ordinal <= Rank.Subkingdom.ordinal()) {
            return MajorRank.Kingdom;
        }
        if (ordinal >= Rank.Superphylum.ordinal() && ordinal <= Rank.Subphylum.ordinal()) {
            return MajorRank.Phylum;
        }
        if (ordinal >= Rank.Superclass.ordinal() && ordinal <= Rank.Infraclass.ordinal()) {
            return MajorRank.Class;
        }
        if (ordinal >= Rank.Superorder.ordinal() && ordinal <= Rank.Parvorder.ordinal()) {
            return MajorRank.Order;
        }
        if (ordinal >= Rank.Superfamily.ordinal() && ordinal <= Rank.Subtribe.ordinal()) {
            return MajorRank.Family;
        }
        if (ordinal >= Rank.Genus.ordinal() && ordinal <= Rank.Subgenus.ordinal()) {
            return MajorRank.Genus;
        }
        if (ordinal >= Rank.Species.ordinal() && ordinal <= Rank.Forma.ordinal()) {
            return MajorRank.Species;
        }
        return MajorRank.Unspecified;
    }

    public static String getDisplayName(CI ci) {
        StringBuffer buf = new StringBuffer();
        buf.append(ci.getName().substring(0, ci.getName().length() - 2));
        buf.append("(");
        buf.append(rankArray[ci.getCItypeObj().getInternId()]);
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
        buf.append(rankArray[getRank().ordinal()]);
        buf.append(")");
        return buf.toString();
    }

    public String getCIname() {
        String rank = "";
        if (getRank().ordinal() < 10) {
            rank = "0";
        }
        rank += getRank().ordinal();
        rank = name + " " + rank;
        return rank;
    }

}
