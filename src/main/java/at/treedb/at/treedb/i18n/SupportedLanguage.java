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

package at.treedb.i18n;

import java.util.List;

import javax.persistence.Entity;

import at.treedb.db.Base;
import at.treedb.db.ClassID;
import at.treedb.db.DAOiface;
import at.treedb.db.DBkey;
import at.treedb.domain.Domain;
import at.treedb.user.User;

/**
 * <p>
 * This class indicates that a
 * {@code Domain) of the base class supports a given language. 
 * </p>
 * 
 * @author Peter Sauer
 */
@Entity
public class SupportedLanguage extends Base implements Cloneable {

    private static final long serialVersionUID = 1L;
    private Locale.LOCALE locale;
    private int priority;

    protected SupportedLanguage() {
    }

    /**
     * Constructor
     * 
     * @param domain
     *            {@code Domain} of this entity
     * @param user
     *            creator of the {@code SupportedLanguage}
     * @param locale
     *            {@code Locale}
     * @param priority
     *            priority for locales with the same language e.g. de_DE, de_AT,
     *            de_CH
     * 
     */
    private SupportedLanguage(Domain domain, User user, Locale locale, int priority) {
        setDomain(domain.getHistId());
        this.locale = locale.getLocale();
        this.priority = priority;
        if (user != null) {
            setCreatedBy(user.getHistId());
            setModifiedBy(user.getHistId());
        }
    }

    /**
     * Creates a {@code SupportedLanguage}
     * 
     * @param domain
     *            {@code Domain} of this entity
     * @param user
     *            creator of the {@code SupportedLanguage}
     * @param loclae
     *            {@code Locale}
     * @return {@code SupportedLanguage} object
     * @throws Exception
     */
    public static SupportedLanguage create(DAOiface dao, Domain domain, User user, Locale locale, int priority)
            throws Exception {
        SupportedLanguage lang = new SupportedLanguage(domain, user, locale, priority);
        Base.save(dao, domain, user, lang);
        return lang;
    }

    /**
     * Loads a {@code SupportedLanguage}.
     * 
     * @param id
     *            entity ID
     * @return {@code SupportedLanguage} object
     * @throws Exception
     */
    public static SupportedLanguage load(@DBkey(value = SupportedLanguage.class) int id) throws Exception {
        return (SupportedLanguage) Base.load(null, SupportedLanguage.class, id, null);
    }

    /**
     * Deletes a {@code SupportedLanguage}.
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param domain
     *            {@code Domain} of the {@code SupportedLanguage}
     * @param user
     *            {@code User} who deletes a {@code SupportedLanguage}
     * @param id
     *            ID of the {@code SupportedLanguage}
     * @return {@code true} if the node exists and deletion was successful.
     *         {@code false} if the node doesn't exist.
     * @throws Exception
     */
    public static boolean delete(DAOiface dao, Domain domain, User user, @DBkey(value = SupportedLanguage.class) int id)
            throws Exception {
        return Base.delete(user, id, SupportedLanguage.class, false);
    }

    /**
     * Loads all supported languages for a given {@code Domain}
     * 
     * @param dao
     *            {@code DAOiface} (data access object)
     * @param domainId
     *            ID of the {@code Domain}
     * @return list of {@code SupportedLanguage}
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    static public List<SupportedLanguage> loadAll(DAOiface dao, @DBkey(value = Domain.class) int domainId)
            throws Exception {
        return (List<SupportedLanguage>) Base.loadEntities(dao, SupportedLanguage.class, domainId, null, null);
    }

    /**
     * Returns the {@code LOCALE}
     * 
     * @return {@code LOCALE}
     */
    public Locale.LOCALE getLocale() {
        return locale;
    }

    /**
     * Returns the priority of the {@code LOCALE}.<br>
     * e.g support for de_AT (priority 0) and de_DE (priority 1) is available,
     * and the client web browser language is de_CH. de_CH will will be matched
     * with de_DE which has a higher priority than de_AT
     * 
     * @return priority
     */
    public int getPriority() {
        return priority;
    }

    @Override
    public ClassID getCID() {
        return ClassID.SUPPORTED_LANGUAGE;
    }

}
