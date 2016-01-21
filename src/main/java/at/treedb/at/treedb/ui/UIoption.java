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
package at.treedb.ui;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;

import at.treedb.ci.Image;
import at.treedb.ci.ImageDummy;
import at.treedb.db.Base;
import at.treedb.db.ClassID;
import at.treedb.db.DAO;
import at.treedb.db.DAOiface;
import at.treedb.db.DBkey;
import at.treedb.domain.Domain;
import at.treedb.i18n.Istring;
import at.treedb.user.User;

@SuppressWarnings("serial")
@Entity
public class UIoption extends Base implements Cloneable {

    public static class UIoptionDummy {
        private String value;
        private String option;
        private ImageDummy imageDummy;

        public UIoptionDummy(String value, String option, ImageDummy iDummy) {
            this.option = option;
            this.value = value;
            this.imageDummy = iDummy;
        }

        public UIoptionDummy(String value, String option) {
            this.option = option;
            this.value = value;
            this.imageDummy = null;
        }

        public String getValue() {
            return value;
        }

        public String getOption() {
            return option;
        }

        public ImageDummy getImageDummy() {
            return imageDummy;
        }
    }

    @DBkey(value = UIselect.class)
    private int selectId;
    @Column(name = "m_value") // firebird problem
    private String value;
    @Column(name = "m_option")
    @DBkey(Istring.class)
    private int option;
    @Column(name = "m_index")
    private int index;
    @DBkey(value = Image.class)
    private int image;

    public String getValue() {
        return value;
    }

    public int getOption() {
        return option;
    }

    public int getSelectId() {
        return selectId;
    }

    public int getIndex() {
        return index;
    }

    public int getIcon() {
        return image;
    }

    public enum Fields {
        value, option, index
    }

    protected UIoption() {
    }

    protected UIoption(int selectId, String value, int option, int index, int image) {
        this.selectId = selectId;
        this.value = value;
        this.option = option;
        this.index = index;
        this.image = image;
    }

    public static UIoption create(DAOiface dao, Domain domain, User user, int selectId, String value, int option,
            int index, int image) throws Exception {

        UIoption opt = new UIoption(selectId, value, option, index, image);
        Base.save(dao, domain, user, opt);
        return opt;
    }

    public static UIoption load(int id) throws Exception {
        return (UIoption) Base.load(null, UIoption.class, id, null);
    }

    @SuppressWarnings("unchecked")
    public static UIoption[] loadList(DAOiface dao, int selectId, Date date) throws Exception {
        List<Base> list = null;
        boolean localDAO = false;
        if (dao == null) {
            dao = DAO.getDAO();
            localDAO = true;
        }
        try {
            if (localDAO) {
                dao.beginTransaction();
            }
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("selectid", selectId);
            // load only active entities
            if (date == null) {
                map.put("status", at.treedb.db.HistorizationIface.STATUS.ACTIVE);
                list = (List<Base>) dao.query(
                        "select data from " + UIoption.class.getSimpleName()
                                + " data where data.selectId= :selectid  and data.status = :status order by data.index",
                        map);
            } else {
                map.put("date", date);
                list = (List<Base>) dao.query("select data from " + UIoption.class.getSimpleName()
                        + " data where data.selectID = :selectid and data.lastModified < :date and (data.deletionDate = null or data.deletionDate > :date) "
                        + " order by data.version having max(data.version)", map);

            }

            if (localDAO) {
                dao.endTransaction();
            }
        } catch (Exception e) {
            if (localDAO) {
                dao.rollback();
            }
            throw e;
        }
        if (list.size() == 0) {
            return null;
        }
        UIoption[] options = new UIoption[list.size()];
        int index = 0;
        for (Base b : list) {
            options[index++] = (UIoption) b;
        }

        return options;

    }

    /*
     * public void update(User user, long l) throws Exception { UpdateMap map =
     * new UpdateMap(UIoption.Fields.class);
     * map.addLong(UIoption.Fields.longValue, l); Base.update(user, this, 0,
     * null, map); }
     */

    @Override
    public ClassID getCID() {
        return ClassID.UIOPTION;
    }
}
