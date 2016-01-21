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

/**
 * ITIS Unit taxonomic type mapping from the SQL tables 'taxon_unit_types' and
 * 'kingdoms" to Java enums.
 * 
 * @author Peter Sauer
 *
 */
public class UnitType {
    private Kingdom kingdom;
    private Rank rank;

    /**
     * Unit type
     * 
     * @param k
     *            kingkdom (http://en.wikipedia.org/wiki/Kingdom_%28biology%29)
     * @param r
     *            taxonomic rank (http://en.wikipedia.org/wiki/Taxonomic_rank)
     */
    public UnitType(Kingdom k, Rank r) {
        this.kingdom = k;
        this.rank = r;
    }

    public enum Kingdom {
        Bacteria, Protozoa, Plantae, Fungi, Animalia, Chromista, Archaea
    }

    public enum MajorRank {
        Kingdom, Phylum, Class, Order, Family, Genus, Species, Unspecified
    }

    public enum Rank {
        Kingdom, Subkingdom, Infrakingdom, Superdivision, Superphylum, Phylum, Division, Subphylum, Subdivision, Infraphylum, Infradivision, Parvphylum, Parvdivision, Superclass, Class, Subclass, Infraclass, Superorder, Order, Suborder, Infraorder, Superfamily, Family, Subfamily, Tribe, Subtribe, Genus, Subgenus, Section, Subsection, Species, Subspecies, Variety, Subvariety, Race, Stirp, Form, Morph, Aberration, Subform, Unspecified
    }

    /**
     * Return the taxonomic rank.
     * 
     * @return taxonomic rank
     */
    public Rank getRank() {
        return rank;
    }

    /**
     * Returns the kingdom.
     * 
     * @return kingdom
     */
    public Kingdom getKingdom() {
        return kingdom;
    }

    /**
     * Returns the major rank of a given rank. e.g 'Subclass' would be mapped to
     * 'Class'
     * 
     * @return major rank
     */
    public MajorRank getMajorRank() {
        return getMajorRank(rank);
    }

    /**
     * Returns the major rank of a given rank. e.g 'Subclass' would be mapped to
     * 'Class'
     * 
     * @return major rank
     */
    public static MajorRank getMajorRank(Rank r) {
        int ordinal = r.ordinal();
        if (ordinal >= Rank.Kingdom.ordinal() && ordinal <= Rank.Infrakingdom.ordinal()) {
            return MajorRank.Kingdom;
        }
        if (ordinal >= Rank.Superphylum.ordinal() && ordinal <= Rank.Infraphylum.ordinal()) {
            return MajorRank.Phylum;
        }
        if (ordinal >= Rank.Superclass.ordinal() && ordinal <= Rank.Infraclass.ordinal()) {
            return MajorRank.Class;
        }
        if (ordinal >= Rank.Superorder.ordinal() && ordinal <= Rank.Infraorder.ordinal()) {
            return MajorRank.Order;
        }
        if (ordinal >= Rank.Superfamily.ordinal() && ordinal <= Rank.Subtribe.ordinal()) {
            return MajorRank.Family;
        }
        if (ordinal >= Rank.Genus.ordinal() && ordinal <= Rank.Subsection.ordinal()) {
            return MajorRank.Genus;
        }
        if (ordinal >= Rank.Species.ordinal() && ordinal <= Rank.Subform.ordinal()) {
            return MajorRank.Species;
        }
        return MajorRank.Unspecified;
    }
}
