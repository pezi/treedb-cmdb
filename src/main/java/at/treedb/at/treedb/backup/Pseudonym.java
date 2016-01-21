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
package at.treedb.backup;

import java.util.Random;

import de.beimax.janag.NameGenerator;

/**
 * <p>
 * Pseudonym generator to make real name anonymous for the DB export.<br>
 * e.g. Ernest Andrews (ernest.andrews@anonymous.local)
 * </p>
 * <p>
 * This class uses the
 * <a href="http://sourceforge.net/projects/janag.berlios/">JaNaG - Java Name
 * Generator</a>
 * </p>
 * 
 * @author Peter Sauer
 *
 */
public class Pseudonym {
    public enum Gender {
        RANDOM, MALE, FEMALE
    };

    private static NameGenerator nameGenerator;
    private static Random rnd;
    private String firstName;
    private String lastName;
    private String email;

    /**
     * Returns the first name.
     * 
     * @return first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Returns the last name.
     * 
     * @return last name.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Returns the email.
     * 
     * @return email address
     */
    public String getEmail() {
        return email;
    }

    private Pseudonym(String name) {
        String[] split = name.split(" ");
        firstName = split[0];
        lastName = split[1];
        email = firstName + "." + lastName + "@anonymous.local";
    }

    /**
     * Generates a pseudonym.
     * 
     * @param gender
     *            gender of the pseudonym
     * @return pseudonym
     */
    public static synchronized Pseudonym generatePseudonym(Gender gender) {
        if (nameGenerator == null) {
            nameGenerator = new NameGenerator("languages.txt", "semantics.txt");
            rnd = new Random();
        }
        String g = null;
        switch (gender) {
        case MALE:
            g = "Männlich";
            break;
        case FEMALE:
            g = "Weiblich";
            break;
        case RANDOM:
            g = rnd.nextBoolean() ? "Männlich" : "Weiblich";
            break;
        }
        return new Pseudonym(nameGenerator.getRandomName("US-Zensus", g + " Top 500+", 1)[0]);
    }

}
