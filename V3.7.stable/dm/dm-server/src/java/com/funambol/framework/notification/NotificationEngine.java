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

package com.funambol.framework.notification;

import com.funambol.framework.core.dm.bootstrap.BasicAccountInfo;
import com.funambol.framework.core.dm.bootstrap.BootStrap;

/**
 * Represent a engine for bootstrap and notification process
 *
 *
 * @version $Id: NotificationEngine.java,v 1.3 2006/11/15 14:01:08 nichele Exp $
 */
public interface NotificationEngine {

    // --------------------------------------------------------------- Constants
    public static final String CONTENT_TYPE_BOOTSTRAP = "application/vnd.syncml.dm+wbxml";


    // ---------------------------------------------------------- Public Methods

    /**
     * Sends a notification message.
     * @param messageType the type of the message. See {@link NotificationConstants} for details.
     * @param transportType the transport type used to send the message. See {@link NotificationConstants} for details.
     * @param sessionId the session id
     * @param phoneNumber the phone number of the target device
     * @param info application specific info
     * @param serverId the server identifier (necessary to calculate the digest of the message)
     * @param serverPassword the server password (necessary to calculate the digest of the message)
     * @param serverNonce the server nonce (necessary to calculate the digest of the message)
     * @throws NotificationException if a error occurs
     */
    public void sendNotificationMessage(int messageType,
                                        int transportType,
                                        int sessionId,
                                        String phoneNumber,
                                        String info,
                                        String serverId,
                                        String serverPassword,
                                        byte[] serverNonce) throws NotificationException;


    /**
     * Sends more notification messages.
     * @param messageType the type of the message. See {@link NotificationConstants} for details.
     * @param transportType the transport type used to send the message. See {@link NotificationConstants} for details.
     * @param sessionIds the session identifiers
     * @param phoneNumbers the phone numbers list of the target devices
     * @param info application specific info
     * @param serverId the server identifier (necessary to calculate the digest of the message)
     * @param serverPasswords the server passwords list (necessary to calculate the digest of the message)
     * @param serverNonces the server nonces list (necessary to calculate the digest of the message)
     * @throws NotificationException if a error occurs
     */
    public void sendNotificationMessages(int      messageType,
                                         int      transportType,
                                         int[]    sessionIds,
                                         String[] phoneNumbers,
                                         String   info,
                                         String   serverId,
                                         String[] serverPasswords,
                                         byte[][] serverNonces) throws NotificationException;



    /**
     * Builds and sends the bootstrap messages.
     * @param basicAccountsInfo the basic info about accounts to create bootstrapping the devices
     * @param bootstraps the list of bootstrap details
     * @throws NotificationException if a error occurs
     */
    public void bootstrap(BasicAccountInfo[] basicAccountsInfo,
                          BootStrap[]        bootstraps) throws  NotificationException;


}
