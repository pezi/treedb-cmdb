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
 */
package at.treedb.backup;

/**
 * <p>
 * Definition of a prerequisite installing a domain.
 * </p>
 * 
 * @author Peter Sauer
 *
 */
public class InstallationPrerequisite {
    /**
     * Message type
     */
    public enum MessageType {
        HINT, WARN, ERR
    };

    /**
     * Impact if a prerequisite is not meet.
     */
    public enum Impact {
        USER_DEFINED, DISABLE_TEST_DATA, DISABLE_INSTALLATION
    }

    private String description; // description of the prerequisite
    private MessageType messageType; // kind of message
    private String message; // prerequisite message
    private Impact impact; // kind of impact
    private String impactMessage; // impact message

    /**
     * Creates a prerequisite object.
     * 
     * @param description
     *            prerequisite description
     * @param messageType
     *            prerequisite type
     * @param message
     *            message in context of checking the prerequisite
     */
    public InstallationPrerequisite(String description, MessageType messageType, String message) {
        this.description = description;
        this.messageType = messageType;
        this.message = message;
    }

    /**
     * Returns the impact message if the prerequisite is not meet.
     * 
     * @return impact message
     */
    public String getImpactMessage() {
        return impactMessage;
    }

    /**
     * Creates a prerequisite object.
     * 
     * @param description
     *            prerequisite description
     * @param messageType
     *            prerequisite type
     * @param message
     *            message in context of checking the prerequisite
     * @param kind
     *            of impact
     * @param impactMessage
     *            message if the prerequisite is not meet
     */
    public InstallationPrerequisite(String description, MessageType messageType, String message, Impact impact,
            String impactMessage) {
        this.description = description;
        this.messageType = messageType;
        this.message = message;
        this.impactMessage = impactMessage;
        this.impact = impact;

    }

    /**
     * Returns the message in context of checking the prerequisite.
     * 
     * @return message text
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the message type
     * 
     * @return message type
     */
    public MessageType getMessageType() {
        return messageType;
    }

    /**
     * Returns a description of the prerequisite.
     * 
     * @return description of the prerequisite
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the kind of impact if a Prerequisite is missing.
     * 
     * @return kind of impact
     */
    public Impact getImpact() {
        return impact;
    }

}
