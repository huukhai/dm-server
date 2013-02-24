/*
 * Funambol is a mobile platform developed by Funambol, Inc.
 * Copyright (C) 2011 Funambol, Inc.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
 * WORK IN WHICH THE COPYRIGHT IS OWNED BY FUNAMBOL, FUNAMBOL DISCLAIMS THE
 * WARRANTY OF NON INFRINGEMENT  OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Funambol, Inc. headquarters at 643 Bair Island Road, Suite
 * 305, Redwood City, CA 94063, USA, or at email address info@funambol.com.
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * "Powered by Funambol" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by Funambol".
 */

package com.funambol.server.notification;


import com.funambol.framework.notification.NotificationSender;
import com.funambol.framework.notification.BootStrapSender;
import com.funambol.framework.notification.NotificationException;
import com.funambol.framework.tools.DbgTools;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/**
 * This is a simple implementation of the Sender interface that simply logs its
 * activity.
 *
 * @version $Id: SimpleSender.java,v 1.5 2006/11/15 16:02:43 nichele Exp $
 */
public class SimpleSender implements NotificationSender, BootStrapSender {
    // ---------------------------------------------------------- Private Data

    private transient static final Logger log = Logger.getLogger(com.funambol.server.notification.SimpleSender.class.getName());

    // ---------------------------------------------------------- Public Methods

    /**
     * Sends a message
     * @param messageType the type of the message. See {@link com.funambol.framework.notification.NotificationConstants}
     * @param phoneNumber the phone number of the target device
     * @param message the messagge to send
     * @param info application specific info
     * @throws NotificationException
     */
    public void sendMessage(int    messageType,
                            String phoneNumber,
                            byte[] message    ,
                            String info       )
    throws NotificationException {

        if (log.isEnabledFor(Level.INFO)) {
            log.info( "Send message \n\tmessageType: "
                    + messageType
                    + "\n\tphoneNumber: "
                    + phoneNumber
                    + "\n\tmessage length: "
                    + ((message != null) ? String.valueOf(message.length) : "unknown")
                    + "\n\tinfo: "
                    + info
            );
            log.info("Message: " + DbgTools.bytesToHex(message));
        }

    }

    /**
     * Sends messages
     * @param messageType the type of the message. See {@link com.funambol.framework.notification.NotificationConstants}
     * @param phoneNumbers the phone numbers list of the target device
     * @param message the messages to send
     * @param info application specific info
     * @throws NotificationException
     */
    public void sendMessages(int      messageType,
                             String[] phoneNumbers,
                             byte[][] message,
                             String   info) throws NotificationException {

        if (log.isEnabledFor(Level.INFO)) {
            log.info( "Send messages \n\tmessageType: "
                      + messageType
                      + "\n\tnum. devices: "
                      + phoneNumbers.length
                      + "\n\tinfo: "
                      + info
                );
            for (int i=0; i<message.length; i++) {
                log.info("Message " + i + ": " + DbgTools.bytesToHex(message[i]));
            }
        }
    }

    /**
     * Sends more messages
     * @param messageType the type of the messages. See {@link com.funambol.framework.notification.NotificationConstants}
     * @param digest the digest list
     * @param authMethods the authentication methods
     * @param phoneNumbers the phone number list
     * @param messages the messages to send
     * @param info application specific info
     * @throws NotificationException
     */
    public void sendMessages(int[]    messageType,
                             String[] digest,
                             int[]    authMethods,
                             String[] phoneNumbers,
                             byte[][] messages,
                             String[] info) throws NotificationException {

        if (log.isEnabledFor(Level.INFO)) {
            log.info( "Send messages: \n\tnum. devices: "
                      + phoneNumbers.length
                );
            for (int i=0; i<messages.length; i++) {
                log.info("Phone number " + i + ": " + phoneNumbers[i]);
                log.info("Message " + i + ": " + DbgTools.bytesToHex(messages[i]));
                log.info("Digest " + i + ": " + digest[i]);
                log.info("AuthMethod " + i + ": " + authMethods[i]);
                log.info("Info " + i + ": " + digest[i]);
            }
        }
    }



}
