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

package at.treedb.ci;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Index;

import at.treedb.db.Base;
import at.treedb.db.ClassID;
import at.treedb.db.DAOiface;
import at.treedb.db.UpdateMap;
import at.treedb.domain.Domain;
import at.treedb.user.User;

/**
 * Represents a Google map section.
 * 
 * @author Peter Sauer
 * 
 */
@SuppressWarnings("serial")
@Table(indexes = { @Index(columnList = "ci"), @Index(columnList = "uiElement"), @Index(columnList = "histId") })
@Entity
public class CIgoogleMap extends CIdata {

    // Google map type
    public enum MapType {
        Hybrid, Roadmap, Satellite, Terrain
    };

    private double longitude; // decimal degrees
    private double latitude; // decimal degrees
    private MapType mapType; // map type
    private int width; // size of the map area in pixels
    private int height; // height of the map area in pixels

    public enum Fields {
        longitude, latitude, mapType, width, height;
    }

    protected CIgoogleMap() {
    }

    private CIgoogleMap(int ci, int ciType, long uiElement, double longitude, double latitude, MapType mapType,
            int width, int height) {
        super(ci, ciType, uiElement);
        this.longitude = longitude;
        this.latitude = latitude;
        this.mapType = mapType;
        this.width = width;
        this.height = height;
    }

    public static CIgoogleMap create(DAOiface dao, Domain domain, User user, int ci, int ciType, long uiElement,
            double longitude, double latitude, MapType mapType, int width, int height) throws Exception {
        CIgoogleMap map = new CIgoogleMap(ci, ciType, uiElement, longitude, latitude, mapType, width, height);
        Base.save(dao, domain, user, map);
        return map;
    }

    public static CIgoogleMap load(int id) throws Exception {
        return (CIgoogleMap) Base.load(null, CIgoogleMap.class, id, null);
    }

    public static CIgoogleMap load(int ci, long uiElement, Date date) throws Exception {
        return (CIgoogleMap) CIdata.load(null, CIgoogleMap.class, ci, uiElement, null, date);
    }

    public static CIgoogleMap load(DAOiface dao, int ci, long uiElement, Date date) throws Exception {
        return (CIgoogleMap) CIdata.load(dao, CIgoogleMap.class, ci, uiElement, null, date);
    }

    public static CIgoogleMap createOrUpdate(DAOiface dao, Domain domain, User user, int ci, int ciType, long uiElement,
            double longitude, double latitude, MapType mapType, int width, int height) throws Exception {
        CIgoogleMap d = (CIgoogleMap) load(dao, CIgoogleMap.class, ci, uiElement, null, null);
        if (d == null) {
            d = new CIgoogleMap(ci, ciType, uiElement, longitude, latitude, mapType, width, height);
            Base.save(dao, domain, user, d);
        } else {
            UpdateMap map = new UpdateMap(CIgoogleMap.Fields.class);
            map.addDouble(CIgoogleMap.Fields.longitude, longitude);
            map.addDouble(CIgoogleMap.Fields.latitude, latitude);
            map.addEnum(CIgoogleMap.Fields.mapType, mapType);
            map.addLong(CIgoogleMap.Fields.width, width);
            map.addLong(CIgoogleMap.Fields.height, height);
            Base.update(dao, user, d, map);
        }
        return d;
    }

    public void update(User user, double longitude, double latitude, MapType mapType, int width, int height)
            throws Exception {
        UpdateMap map = new UpdateMap(CIgoogleMap.Fields.class);
        map.addDouble(CIgoogleMap.Fields.longitude, longitude);
        map.addDouble(CIgoogleMap.Fields.latitude, latitude);
        map.addEnum(CIgoogleMap.Fields.mapType, mapType);
        map.addLong(CIgoogleMap.Fields.width, width);
        map.addLong(CIgoogleMap.Fields.height, height);
        Base.update(user, this, map);
    }

    @Override
    public ClassID getCID() {
        return ClassID.CIGOOGLEMAP;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public MapType getMapType() {
        return mapType;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public CIgoogleMap getData() {
        return this;
    }
}
