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
package at.treedb.util;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

import at.treedb.ci.KeyValuePair;
import at.treedb.domain.Domain;

/**
 * <p>
 * Class for sending Email.
 * </p>
 * 
 * @author Peter Sauer
 *
 */
public class Mail {
    public enum TransportSecurity {
        NONE, STARTTLS, SSL
    }

    private String smtpHost;
    private int smtpPort;
    private String errorMsg = "Mail.init(): missing value ";
    private String smtpUser;
    private String smtpPassword;
    // private String smtpFrom;
    private TransportSecurity transportSecurity;

    public static final String EMAIL_REGEX = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    /**
     * Constructor
     * 
     * @throws Exception
     */
    public Mail() throws Exception {
        KeyValuePair kp = KeyValuePair.serarchValue(Domain.getDummyDomain(), "mail.smtp.host");
        if (kp == null || kp.getString() == null) {
            throw new Exception(errorMsg + "mail.smtp.host");
        }
        // smtp host
        smtpHost = kp.getString();
        kp = KeyValuePair.serarchValue(Domain.getDummyDomain(), "mail.smtp.port");
        if (kp == null || kp.getLong() == null) {
            smtpPort = 25;
        } else {
            smtpPort = (int) ((long) kp.getLong());
        }
        // SMTP user
        kp = KeyValuePair.serarchValue(Domain.getDummyDomain(), "mail.smtp.user");
        if (kp == null || kp.getString() == null) {
            throw new Exception(errorMsg + "mail.smtp.user");
        }
        smtpUser = kp.getString();
        // SMTP password
        kp = KeyValuePair.serarchValue(Domain.getDummyDomain(), "mail.smtp.password");
        if (kp == null || kp.getString() == null) {
            throw new Exception(errorMsg + "mail.smtp.password");
        }
        smtpPassword = kp.getString();
        // smtp from
        /*
         * kp = KeyValuePair.serarchValue(Domain.getDummyDomain(),
         * "mail.smtp.from"); if (kp == null || kp.getString() == null) {
         * smtpFrom = smtpUser; } else { smtpFrom = kp.getString(); }
         */
        // transport security
        kp = KeyValuePair.serarchValue(Domain.getDummyDomain(), "mail.smtp.security");
        if (kp == null) {
            transportSecurity = TransportSecurity.NONE;
        } else {
            transportSecurity = TransportSecurity.valueOf(kp.getString());
        }
    }

    public static boolean checkEmail(String mail) {
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        Matcher matcher = pattern.matcher(mail);
        return matcher.matches();
    }

    /**
     * 
     * @param sendTo
     * @param subject
     * @param message
     * @throws EmailException
     */
    public void sendMail(String mailTo, String mailFrom, String subject, String message) throws Exception {
        Objects.requireNonNull(mailTo, "Mail.sendMail(): mailTo can not be null!");
        Objects.requireNonNull(mailFrom, "Mail.sendMail(): mailFrom can not be null!");
        Objects.requireNonNull(subject, "Mail.sendMail(): subject can not be null!");
        Objects.requireNonNull(message, "Mail.sendMail(): message can not be null!");
        Email email = new SimpleEmail();
        email.setHostName(smtpHost);

        email.setAuthenticator(new DefaultAuthenticator(smtpUser, smtpPassword));
        if (transportSecurity == TransportSecurity.SSL) {
            email.setSSLOnConnect(true);
            email.setSSLCheckServerIdentity(false);
        } else if (transportSecurity == TransportSecurity.STARTTLS) {
            email.setStartTLSRequired(true);
        }
        email.setSmtpPort(smtpPort);
        email.setFrom(mailFrom);
        email.setSubject(subject);
        email.setMsg(message);
        email.addTo(mailTo);
        email.send();
    }

    public static void sendMail(String smtpHost, int smtpPort, TransportSecurity transportSecurity, String smtpUser,
            String smtpPassword, String mailTo, String mailFrom, String subject, String message) throws Exception {
        Objects.requireNonNull(mailTo, "Mail.sendMail(): mailTo can not be null!");
        Objects.requireNonNull(subject, "Mail.sendMail(): subject can not be null!");
        Objects.requireNonNull(message, "Mail.sendMail(): message can not be null!");
        Email email = new SimpleEmail();
        email.setHostName(smtpHost);

        email.setAuthenticator(new DefaultAuthenticator(smtpUser, smtpPassword));
        if (transportSecurity == TransportSecurity.SSL) {
            email.setSSLOnConnect(true);
            email.setSSLCheckServerIdentity(false);
        } else if (transportSecurity == TransportSecurity.STARTTLS) {
            email.setStartTLSRequired(true);
        }
        email.setSmtpPort(smtpPort);
        if (mailFrom != null && !mailFrom.isEmpty()) {
            email.setFrom(smtpUser);
        }
        email.setSubject(subject);
        email.setMsg(message);
        email.addTo(mailTo);
        email.send();
    }

}
