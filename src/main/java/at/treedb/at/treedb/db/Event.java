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

import javax.persistence.Entity;

import at.treedb.user.User;

/**
 * <p>
 * 
 * </p>
 * 
 * @author Peter Sauer
 *
 */
@SuppressWarnings("serial")
@Entity

public class Event extends Base implements Cloneable {
    public enum EventClass {
        MESSAGE, WARNING, ERROR
    };

    public enum EventType {
        USER_LOGON, USER_LOGOUT, EXCEPTION, USER_DEFINED
    };

    @Override
    public ClassID getCID() {
        return ClassID.EVENT;
    }

    /**
     * Access fields for update and search operations
     */
    public enum Fields {
        /**
         * first name of the user
         */
        longValue, message,
    }

    private EventClass eventClass;
    private EventType eventType;
    private long longValue;
    private String message;

    protected Event() {
    }

    private Event(EventClass eventClass, EventType eventType, long longValue, String message) {
        this.eventClass = eventClass;
        this.eventType = eventType;
        this.longValue = longValue;
        this.message = message;
    }

    public static Event logMessage(User user, EventType eventType) {
        try {
            return create(null, user, EventClass.MESSAGE, eventType, 0, null);
        } catch (Exception e) {

        }
        return null;
    }

    public static Event create(User user, EventClass eventClass, EventType eventType, long longValue, String message)
            throws Exception {
        return create(null, user, eventClass, eventType, longValue, message);
    }

    public static Event create(DAOiface dao, User user, EventClass eventClass, EventType eventType, long longValue,
            String message) throws Exception {
        Event event = null;
        boolean localDAO = false;
        if (dao == null) {
            dao = DAO.getDAO();
            localDAO = true;
        }
        try {
            if (localDAO) {
                dao.beginTransaction();
            }
            event = new Event(eventClass, eventType, longValue, message);
            Base.save(dao, null, user, event);
            if (localDAO) {
                dao.endTransaction();
            }
        } catch (Exception e) {
            if (localDAO) {
                dao.rollback();
            }
            throw e;
        }
        return event;
    }
}
