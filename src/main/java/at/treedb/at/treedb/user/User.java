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
package at.treedb.user;

import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Index;

import at.treedb.ci.Image;
import at.treedb.ci.ImageDummy;
import at.treedb.db.Base;
import at.treedb.db.ClassID;
import at.treedb.db.DAO;
import at.treedb.db.DAOiface;
import at.treedb.db.DBkey;
import at.treedb.db.HistorizationIface;
import at.treedb.db.SearchCriteria;
import at.treedb.db.SearchLimit;
import at.treedb.db.Update;
import at.treedb.db.UpdateMap;
import at.treedb.util.BCrypt;

/**
 * <p>
 * Each CRUD<sup>1</sup> operations of CMDB entities take place in the
 * context/ownership of an user.<br>
 * Warning: User creation is very slow due the hashing & salting the password
 * </p>
 * <sup>1</sup> <a href="http://en.wikipedia.org/wiki/CRUD">CRUD - Wikipedia</a>
 * <
 * 
 * @author Peter Sauer
 */

@Entity
@Table(name = "m_user", indexes = { @Index(columnList = "email"), @Index(columnList = "histId") })
// some DBs have a problem with a table named user - e.g. Derby DB
public class User extends Base implements Cloneable {
    final public static int USER_SYSTEM_ID = 0;
    final public static int USET_REST_SERVICE_ID = -1;
    final public static int USER_IMAGE_WIDTH = 640;
    final public static int USER_IMAGE_HEIGHT = 480;

    private static User adminUser = new User("TreeDB", "Admin", "TreeDB Admin");;

    /**
     * Privacy filter for DB export
     */
    public enum PRIVACY {
        NAME, PHONE, MOBILE, USERID
    };

    private static final long serialVersionUID = 1L;

    /**
     * Access fields for update and search operations
     */
    public enum Fields {
        /**
         * first name of the user
         */
        firstName,
        /**
         * last name of the user
         */
        lastName,
        /**
         * middle name of the user
         */
        middleName,
        /**
         * nickname of the user
         */
        nickName,
        /**
         * display name of the user
         */
        displayName,
        /**
         * email of the user
         */
        email,
        /**
         * password of the user
         */
        password,
        /**
         * phone number of the user
         */
        phone,
        /**
         * mobile number of the user
         */
        mobile,
        /**
         * external user id
         */
        userId,
        /**
         * image of the user
         */
        image,
        /**
         * tenant
         */
        tenant
    }

    // UUID - needed for export/import
    private String uuid;
    private String firstName;
    private String lastName;
    private String middleName;
    private String nickName;
    private String displayName;
    private String email;
    private String password;
    private String phone;
    private String mobile;
    @DBkey(value = Image.class)
    private int image;
    @DBkey(value = Tenant.class)
    private int tenant;

    // external user ID - e.g. login name
    private String userId;

    protected User() {
    }

    private User(String firstName, String lastName, String displayName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.displayName = displayName;
    }

    private User(String firstName, String lastName, String middleName, String displayName, String email,
            String password, String userId) {
        Objects.requireNonNull(firstName, "User.User(): parameter firstName can't be null");
        Objects.requireNonNull(lastName, "User.User(): parameter lastName can't be null");
        Objects.requireNonNull(displayName, "User.User(): parameter displayName can't be null");
        Objects.requireNonNull(email, "User.User(): parameter email can't be null");
        Objects.requireNonNull(password, "User.User(): parameter password can't be null");
        this.setHistStatus(STATUS.ACTIVE);
        uuid = UUID.randomUUID().toString();
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
        this.displayName = displayName;
        this.email = email;
        // very slow
        this.password = BCrypt.hashpw(password, BCrypt.gensalt());
        this.userId = userId;
    }

    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    /**
     * Creates an {@code User}.
     * 
     * @param user
     *            creator of the new user, can be null
     * @param firstName
     *            first name of the user
     * @param lastName
     *            last name of the user
     * @param middleName
     *            middle name of the user
     * @param displayName
     *            display name
     * @param email
     *            email of the user
     * @param userId
     *            external user ID, e.g. login name
     * @param map
     *            update map for setting additional parameter like phone, etc.
     * @return {@code User} object
     * @throws Exception
     */
    public static User create(User user, String firstName, String lastName, String middleName, String displayName,
            String email, String password, String userId, UpdateMap map) throws Exception {

        User u = new User(firstName, lastName, middleName, displayName, email, password, userId);
        DAOiface dao = DAO.getDAO();
        try {
            dao.beginTransaction();

            if (map != null) {
                u.simpleUpdate(map, false);
                Update up = map.get(User.Fields.image);
                if (up != null) {
                    ImageDummy dummy = up.getImageDummy();
                    Image img = Image.create(dao, null, user, "userImage_" + System.currentTimeMillis(),
                            dummy.getData(), dummy.getMimeType(), dummy.getLicense());
                    u.image = img.getHistId();
                }
            }

            Base.save(dao, null, user, u);

            dao.endTransaction();
        } catch (Exception e) {
            dao.rollback();
            throw e;
        }
        return u;
    }

    /**
     * Creates an {@code User}.
     * 
     * @param user
     *            creator of the new user, can be null
     * @param map
     *            map containing the user data fields
     * @return {@code User} object
     * @throws Exception
     */
    public static User create(User user, UpdateMap map) throws Exception {
        String firstName = map.getStringAndRemoveUpdate(User.Fields.firstName);
        String lastName = map.getStringAndRemoveUpdate(User.Fields.lastName);
        String middleName = map.getStringAndRemoveUpdate(User.Fields.middleName);
        String displayName = map.getStringAndRemoveUpdate(User.Fields.displayName);
        String email = map.getStringAndRemoveUpdate(User.Fields.email);
        String password = map.getStringAndRemoveUpdate(User.Fields.password);
        String userId = map.getStringAndRemoveUpdate(User.Fields.userId);
        return create(user, firstName, lastName, middleName, displayName, email, password, userId, map);
    }

    @Override
    public Object checkConstraints(DAOiface dao, UpdateMap update) throws Exception {

        if (update == null) {
            checkConstraint(dao, update, 0, User.Fields.email, getEmail());
        } else {
            Update m = update.get(User.Fields.email);
            if (m != null) {
                checkConstraint(dao, update, getHistId(), User.Fields.email, m.getString());
            }
        }
        return null;
    }

    /**
     * Deletes a {@code User}.
     * 
     * @param admin
     *            admin {@code User} who deletes the user, can be null
     * @param user
     *            user to be deleted
     * @throws Exception
     */
    public static void delete(User admin, User user) throws Exception {
        Base.delete(admin, user, false);
    }

    /**
     * Deletes a {@code User}.
     * 
     * @param admin
     *            admin {@code User} who deletes the user, can be null
     * @param user
     *            user to be deleted
     * @throws Exception
     */
    public static void delete(DAOiface dao, User admin, User user) throws Exception {
        Base.delete(dao, admin, user, false);
    }

    /**
     * Deletes a user.
     * 
     * @param admin
     *            admin {@code User} who deletes the user, can be null
     * @param id
     *            user (historization) id, which should be deleted
     * @throws Exception
     */
    public static void delete(User admin, int id) throws Exception {
        Base.delete(admin, id, User.class, false);
    }

    /**
     * Deletes an user from DB explicit without historization.
     * 
     * @param admin
     *            admin {@code User} who deletes the user, can be null
     * @param user
     *            user DB id
     * @throws Exception
     */
    public static void dbDelete(User admin, User user) throws Exception {
        Base.delete(admin, user, true);
    }

    /**
     * Deletes an user from DB explicit without historization.
     * 
     * @param admin
     *            admin {@code User} who deletes the user, can be null
     * @param id
     *            user DB id
     * @throws Exception
     */
    public static void dbDelete(User admin, int id) throws Exception {
        Base.delete(admin, id, User.class, false);
    }

    /**
     * Loads an user.
     * 
     * @param id
     *            user ID
     * @return {@code User} object
     * @throws Exception
     */
    public static User load(int id) throws Exception {
        return (User) Base.load(null, User.class, id, null);
    }

    /**
     * Loads an user.
     * 
     * @param id
     *            user ID
     * @param date
     *            temporal bound
     * @return {@code User} object
     * @throws Exception
     */
    public static User load(int id, Date date) throws Exception {
        return (User) Base.load(null, User.class, id, date);
    }

    public static User load(String email) throws Exception {
        List<Base> list = User.search(EnumSet.of(User.Fields.email), email, EnumSet.of(Base.Search.EQUALS), null);

        if (list.size() == 1) {
            return (User) list.get(0);
        }
        return null;
    }

    /**
     * Counts the user entities/table rows.
     * 
     * @param status
     *            search filter representing the user's historization status.
     *            {@code null} counts all user DB entries.
     * @return number of users
     * @throws Exception
     */
    public static long rowCount(HistorizationIface.STATUS status) throws Exception {
        return Base.countRow(null, User.class, status, null);
    }

    /**
     * Updates an user.
     * 
     * @param admin
     *            admin {@code User} who deletes the user, can be null
     * @param map
     *            update map
     * @throws Exception
     */
    public void update(User admin, UpdateMap map) throws Exception {
        Base.update(admin, this, map);
    }

    /**
     * Updates an user.
     * 
     * @param admin
     *            administration user
     * @param user
     *            user to be updated
     * @param map
     *            update map
     * @throws Exception
     */
    static public void update(User admin, User user, UpdateMap map) throws Exception {
        Base.update(admin, user, map);
    }

    /**
     * Updates an user.
     * 
     * @param admin
     *            admin {@code User} who deletes the user, can be null
     * @param id
     *            user ID, to be updated
     * @param map
     *            update map
     * @throws Exception
     */
    static public void update(User admin, int id, UpdateMap map) throws Exception {
        Base.update(admin, id, User.class, map);
    }

    /**
     * Searches an user.
     * 
     * @param fields
     *            field to be searched through
     * @param value
     *            search pattern
     * @param flags
     *            search flags
     * @return list of {@code User} object
     * @throws Exception
     */
    static public List<Base> search(EnumSet<User.Fields> fields, String value, EnumSet<Base.Search> flags,
            SearchLimit limit) throws Exception {
        return Base.search(null, User.class, fields, value, null, flags, limit, false);
    }

    /**
     * Searches an user.
     * 
     * @param email
     *            user's email address
     * @return {@code User} object
     * @throws Exception
     */
    static public User search(String email) throws Exception {
        List<Base> list = User.search(EnumSet.of(Fields.email), email, EnumSet.of(Search.EQUALS), null);
        if (list.size() == 1) {
            return (User) list.get(0);
        }
        return null;
    }

    /**
     * Returns the email address.
     * 
     * @return email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns the password of the user.
     * 
     * @return password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns the first name.
     * 
     * @return first name;
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Returns the last name.
     * 
     * @return last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Returns the middle name.
     * 
     * @return middle name
     */
    public String getMiddleName() {
        return middleName;
    }

    /**
     * Returns the display name.
     * 
     * @return display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the nickname.
     * 
     * @return nickname;
     */
    public String getNickName() {
        return nickName;
    }

    /**
     * Returns the phone number.
     * 
     * @return phone number
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Returns the mobile number.
     * 
     * @return mobile number
     */
    public String getMobile() {
        return mobile;
    }

    /**
     * Returns the (external) user ID - e.g. login name
     * 
     * @return user ID
     */
    public String getExternalUserId() {
        return userId;
    }

    @Override
    public ClassID getCID() {
        return ClassID.USER;
    }

    /**
     * Returns the UUID (universally unique identifier).
     * 
     * @return UUID
     */
    public String getUUID() {
        return uuid;
    }

    /**
     * Returns the image of the user.
     * 
     * @return image user ID
     */
    public @DBkey(value = Image.class) int getImage() {
        return image;
    }

    /**
     * Checks if a user has admin rights.
     * 
     * @param dao
     *            {@code DAOiface} (data access object), can be null
     * @param user
     *            user to be checked for admin rights
     * @return {@code true} if the user as admin right, {@code false} if not
     * @throws Exception
     */
    public static boolean isAdmin(DAOiface dao, User user) throws Exception {
        if (user != null) {
            // check for build-in admin user
            if (user.getHistId() == 0) {
                return true;
            }
            List<? extends Base> list = Base.loadEntities(dao, Membership.class,
                    new SearchCriteria[] { new SearchCriteria(Membership.Fields.user, user.getHistId()),
                            new SearchCriteria(Membership.Fields.group, Group.GROUP_ADMIN_ID) },
                    null);
            if (list.size() == 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets user image.
     * 
     * @param image
     *            user image ID
     */
    public void setImage(int image) {
        this.image = image;
    }

    /**
     * Return the build-in admin user.
     * 
     * @return build-in admin user
     */
    public static User getAdminUser() {
        return adminUser;
    }
}
