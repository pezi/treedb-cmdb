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
package at.treedb.db;

/**
 * <p>
 * Limits for search operations.
 * </p>
 * 
 * @author Peter Sauer
 * 
 */
// LDOR: 22.12.2013
public class SearchLimit {
    private int firstResult; // index of the first result
    private int maxResults;

    /**
     * Constructor
     * 
     * @param firstResult
     *            index of the first result
     * @param maxResults
     *            maximum of results
     */
    public SearchLimit(int firstResult, int maxResults) {
        this.firstResult = firstResult;
        this.maxResults = maxResults;
    }

    /**
     * Constructor
     * 
     * @param maxResults
     *            maximum of results
     */
    public SearchLimit(int maxResults) {
        this.firstResult = 0;
        this.maxResults = maxResults;
    }

    /**
     * Returns the index of the first result
     * 
     * @return index of the first result
     */
    public int getFirstResult() {
        return firstResult;
    }

    /**
     * Returns the maximum of results
     * 
     * @return maximum
     */
    public int getMaxResults() {
        return maxResults;
    }
}