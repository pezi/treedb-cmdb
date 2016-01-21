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
 * Container for an invalid taxonomic name. e.g. old, outdated names
 * @author Peter Sauer
 *
 */
public class InvalidName {
	private int tsn;
	private String invalidName;
	private String invalidReason;
	
	/**
	 * Creates an InvalidNam object.
	 * @param tsn TSN
	 * @param invalidName invalid name
	 * @param invalidReason reason for invalidity 
	 */
	public InvalidName(int tsn,String invalidName,String invalidReason) {
		this.tsn = tsn;
		this.invalidName = invalidName;
		this.invalidReason = invalidReason;
	}
	
	/**
	 * Returns the TSN.
	 * @return TSN name
	 */
	public int getTSN() {
		return tsn;
	}
	
	/**
	 * Returns the invalid name;
	 * @return invalid name
	 */
	public String getInvalidName() {
		return invalidName;
	}
	
	/**
	 * Returns the reason for invalidity. 
	 * @return reason for invalidity
	 */
	public String getInvalidReason() {
		return invalidReason;
	}
		
}
