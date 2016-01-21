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
package at.treedb.db.hibernate;

import java.sql.Connection;
import java.sql.SQLException;
import org.hibernate.jdbc.Work;

public class ConnectionInfo implements Work {

    public String dataBaseUrl;
    public String dataBaseProductName;
    public String driverName;

    @Override
    public void execute(Connection connection) throws SQLException {
        dataBaseUrl = connection.getMetaData().getURL();
        dataBaseProductName = connection.getMetaData().getDatabaseProductName();
        driverName = connection.getMetaData().getDriverName();
    }

    public String getDataBaseProductName() {
        return dataBaseProductName;
    }

    public void setDataBaseProductName(String dataBaseProductName) {
        this.dataBaseProductName = dataBaseProductName;
    }

    public String getDataBaseUrl() {
        return dataBaseUrl;
    }

    public void setDataBaseUrl(String dataBaseUrl) {
        this.dataBaseUrl = dataBaseUrl;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }
}