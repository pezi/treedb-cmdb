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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;

import at.treedb.db.hibernate.DAOhibernate;
import at.treedb.domain.Domain;

/**
 * Iterator for reading/traverse DB entities.
 * 
 * @author Peter Sauer
 * 
 */
public class Iterator {
    private int pageSize;
    private long entitiesNum;
    private int index;
    private int toRead;
    private DAOiface dao;
    private boolean hasNext;
    private HashMap<String, Object> map;
    private String queryString;
    private String nativeQueryString;
    private ScrollableResults sresult;
    private List<Integer> idList;
    private int listIndex;
    private Class<?> clazz;

    /**
     * Constructor
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param clazz
     *            persisted class
     * @param domain
     *            optional {@code Domain}, can be {@code null}
     * @param status
     *            optional historization status, can be {@code null}
     * @param pageSize
     *            entities page size (=iteration step size)
     * @throws Exception
     */
    public Iterator(DAOiface dao, Class<?> clazz, Domain domain, HistorizationIface.STATUS status, int pageSize)
            throws Exception {
        this.dao = dao;
        this.pageSize = pageSize;
        this.clazz = clazz;
        String where = null;
        if (domain != null) {
            where = "domain = " + domain.getHistId();
        }
        long size = Base.countRow(dao, clazz, status, where);
        entitiesNum = size;
        toRead = (int) size;
        if (toRead > 0) {
            hasNext = true;
            map = new HashMap<String, Object>();
            queryString = "select i from " + clazz.getSimpleName() + " i";
            nativeQueryString = "select id from " + clazz.getSimpleName() + " ";
            boolean whereClause = false;
            if (domain != null) {
                map.put("domain", domain.getHistId());
                queryString += " where i.domain = :domain";
                nativeQueryString += "where domain=" + domain.getHistId();
                whereClause = true;
            }
            if (status != null) {
                map.put("status", status);
                if (whereClause) {
                    queryString += " and i.status = :status order by i.id";
                    nativeQueryString += " and status=" + status.ordinal();
                } else {
                    queryString += " where i.status = :status order by i.id";
                    nativeQueryString += "where status=" + status.ordinal();
                }
            }
        }
    }

    /**
     * Entities available?
     * 
     * @return {@code true} if entities are available {@code false} if not
     */
    public boolean hasNext() {
        return hasNext;
    }

    /**
     * Reads the entities during the iteration step.
     * 
     * @return List<Object> entities list
     * @throws Exception
     */
    public List<Object> next() throws Exception {
        if (!hasNext) {
            return null;
        }
        int size = Math.min(pageSize, toRead);
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) dao.query(queryString, index, size, map);
        index += size;
        toRead -= size;
        if (toRead == 0) {
            hasNext = false;
        }
        return list;
    }

    public void close() {
        if (sresult != null) {
            sresult.close();
        }
    }

    /**
     * <p>
     * Returns the next single object of an entity. This method is used to
     * enumerate large (binary) objects of a entity set. Single object fetching
     * should avoid running into OutOfMemory exceptions.
     * </p>
     * <p>
     * <b>Implementation details:</b>
     * <ol>
     * <li>Hibernate: Statless session<br>
     * </li>
     * <li>JPA/EclipseLink: <a href=
     * "http://wiki.eclipse.org/Using_Advanced_Query_API_%28ELUG%29#Example_107-12">
     * ReadAllQuery/CursoredStream</a> (streaming data) wasn't really working -
     * every time the whole entity data set was loaded by the first access!
     * Actual a native SQL statement is used to pre-load all object IDs. This
     * list is used to retrieve all objects.</li>
     * <li>JPA/ObjectDB: Slow query with setting first position/max data set
     * size.</li>
     * </ol>
     * 
     * @return entity object
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public List<Object> nextObject() throws Exception {
        if (!hasNext) {
            return null;
        }
        int size = 1;
        List<Object> list = null;
        // Hibernate environment
        if (dao.isHibernate() || dao.getJPAimpl() == DAO.JPA_IMPL.HIBERNATEJPA) {
            if (sresult == null) {
                Query query = ((DAOhibernate) dao).createQuery(queryString, map);
                query.setReadOnly(true);
                // MIN_VALUE gives hint to JDBC driver to stream results - but
                // this magic
                // is not working for every DB!
                if (dao.getDB() != DAO.DB.H2) {
                    query.setFetchSize(Integer.MIN_VALUE);
                }
                sresult = query.scroll(ScrollMode.FORWARD_ONLY);
            }
            if (sresult.next()) {
                list = new ArrayList<Object>();
                list.add(sresult.get(0));
            }
        } else {
            if (dao.getJPAimpl() != DAO.JPA_IMPL.OBJECTDB) {
                if (idList == null) {
                    idList = (List<Integer>) dao.nativeQuery(nativeQueryString);
                    if (idList.size() == 0) {
                        return null;
                    }
                }
                if (listIndex < idList.size()) {
                    list = new ArrayList<Object>();
                    Object o = Base.load(dao, (Class<? extends Base>) clazz, idList.get(listIndex));
                    if (o == null) {
                        throw new Exception("Iterator.nextObject(): loading JPA object for ID " + idList.get(listIndex)
                                + " failed");
                    }
                    list.add(o);
                    ++listIndex;
                }
            } else {
                // TODO: fallback for ObjectDB - working, but slow, very slow
                list = (List<Object>) dao.query(queryString, index, size, map);
            }
        }
        index += size;
        toRead -= size;
        if (toRead == 0) {
            hasNext = false;
        }
        return list;
    }

    /**
     * Returns the internal page size (=iteration step size)
     * 
     * @return
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Returns the number of DB entities.
     * 
     * @return number of DB entities
     */
    public long getEntitiesNum() {
        return entitiesNum;
    }

}
