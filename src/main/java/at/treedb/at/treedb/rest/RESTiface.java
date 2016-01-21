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
package at.treedb.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * REST interface for a {@code Domain}
 * </p>
 * 
 * @author Peter Sauer
 *
 */

public interface RESTiface {
    public boolean isRESTifaceAvailable();
    public boolean isRESTifaceEnabled();
    public void doGET(String method, HttpServletRequest request, HttpServletResponse response);
    public void doPOST(String method, HttpServletRequest request, HttpServletResponse response);
    public void doPUT(String method, HttpServletRequest request, HttpServletResponse response);
    public void doDELETE(String method, HttpServletRequest request, HttpServletResponse response);
}
