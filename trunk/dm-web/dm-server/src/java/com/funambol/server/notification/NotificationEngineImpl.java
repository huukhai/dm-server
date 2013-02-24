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


import java.security.GeneralSecurityException;


import com.funambol.framework.config.Configuration;
import com.funambol.framework.core.dm.bootstrap.BasicAccountInfo;
import com.funambol.framework.core.dm.bootstrap.BootStrap;
import com.funambol.framework.notification.AbstractNotificationEngine;
import com.funambol.framework.notification.BootStrapSender;
import com.funambol.framework.notification.NotificationConstants;
import com.funambol.framework.notification.NotificationException;
import com.funambol.framework.notification.NotificationMessageBuilder;
import com.funambol.framework.notification.NotificationSender;
import com.funambol.framework.tools.DbgTools;
import com.funambol.framework.tools.SecurityTools;
import com.funambol.framework.tools.beans.BeanFactory;

import com.funambol.server.bootstrap.DMBootstrapMessageBuilder;
import com.funambol.server.bootstrap.PlainBootstrapMessageBuilder;
import com.funambol.server.bootstrap.WapBootstrapMessageBuilder;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/**
 * Concrete implementation of a <code>NotificationEngine</code>
 *
 * @version $Id: NotificationEngineImpl.java,v 1.5 2006/11/15 16:02:03 nichele Exp $
 */
public class NotificationEngineImpl extends AbstractNotificationEngine {

    // --------------------------------------------------------------- Constants

    private static final float SYNCML_DM_PROTOCOL_VERSION = 1.1f;

    public static final String NOTIFICATION_SENDER_BEAN =
        "com/funambol/server/engine/dm/NotificationSender.xml";

    public static final String BOOTSTRAP_SENDER_BEAN =
        "com/funambol/server/engine/dm/BootstrapSender.xml";


    // -------------------------------------------------------------- Properties

    // ------------------------------------------------------------ Private Data

    private transient static final Logger log = Logger.getLogger(com.funambol.server.notification.NotificationEngineImpl.class.getName());

    private Configuration config = null;


    // ------------------------------------------------------------- Constructor

    public NotificationEngineImpl(Configuration config) {
        super(config);
        this.config = config;
    }

    // ---------------------------------------------------------- Public Methods

    /**
     * Send a notification message.
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
    public void sendNotificationMessage( int    messageType   ,
                                         int    transportType ,
                                         int    sessionId     ,
                                         String phoneNumber   ,
                                         String info          ,
                                         String serverId      ,
                                         String serverPassword,
                                         byte[] serverNonce
                                       ) throws NotificationException {

        if (log.isEnabledFor(Level.INFO)) {
            log.info("sendNotificationMessage with: "+
                     "\n- messageType: "    + messageType    +
                     "\n- transportType: "  + transportType  +
                     "\n- sessionId: "      + sessionId      +
                     "\n- phoneNumber: "    + phoneNumber    +
                     "\n- info: "           + info           +
                     "\n- serverId: "       + serverId       +
                     "\n- serverPassword: " + serverPassword +
                     "\n- serverNonce: "    + serverNonce
                     );
        }

        NotificationMessageBuilder messageBuilder = null;
        NotificationSender messageSender = null;

        messageBuilder = getNotificationMessageBuilder(messageType);

        messageSender  = getNotificationSender();

        if (log.isEnabledFor(Level.TRACE)) {
            log.trace("NotificationMessageBuilder: " + messageBuilder);
            log.trace("Sender: "                     + messageSender );
        }


        // build message to send
        byte[] message = messageBuilder.buildMessage(sessionId, phoneNumber, serverId, serverPassword, serverNonce, SYNCML_DM_PROTOCOL_VERSION);


        if (log.isEnabledFor(Level.TRACE)) {
            log.trace("Notification message to send: " + DbgTools.bytesToHex(message));
        }


        // send message
        messageSender.sendMessages(messageType,
                                   new String[] {phoneNumber},
                                   new byte[][] {message},
                                   info);

        if (log.isEnabledFor(Level.INFO)) {
            log.info("Notification message to " + phoneNumber + " sent");
        }

    }

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
                                         byte[][] serverNonces) throws NotificationException {

        if (log.isEnabledFor(Level.INFO)) {
            log.info("sendNotificationMessages with: "+
                     "\n- messageType: "    + messageType    +
                     "\n- transportType: "  + transportType  +
                     "\n- info: "           + info           +
                     "\n- serverId: "       + serverId       );
        }

        NotificationMessageBuilder messageBuilder = null;
        NotificationSender         messageSender  = null;

        messageBuilder = getNotificationMessageBuilder(messageType);
        messageSender  = getNotificationSender();

        if (log.isEnabledFor(Level.TRACE)) {
            log.trace("NotificationMessageBuilder: " + messageBuilder);
            log.trace("Sender: "                     + messageSender );
        }

        int numDevices = phoneNumbers.length;
        byte[][] messages = new byte[numDevices][];
        for (int i=0; i<numDevices; i++) {

            // build message to send
            messages[i] = messageBuilder.buildMessage(sessionIds[i], phoneNumbers[i], serverId,
                serverPasswords[i], serverNonces[i], SYNCML_DM_PROTOCOL_VERSION);

            if (log.isEnabledFor(Level.TRACE)) {
                log.trace("Notification message to send to " + phoneNumbers[i] + ": " +
                         DbgTools.bytesToHex(messages[i]));
            }
        }

        // send message
        messageSender.sendMessages(messageType, phoneNumbers, messages, info);

        if (log.isEnabledFor(Level.INFO)) {
            log.info("Notification message sent to " + phoneNumbers.length + " devices");
        }

    }

    /**
     * Builds and sends bootstrap messages.
     * @param basicAccountsInfo the basic info about accounts to create bootstrapping the devices
     * @param bootstraps the bootstraps details.
     * @throws NotificationException if a error occurs
     */
    public void bootstrap(BasicAccountInfo[] basicAccountsInfo,
                          BootStrap[]        bootstraps) throws  NotificationException {


        if (basicAccountsInfo == null) {
            throw new NotificationException(
                "Unable to start the bootstrap process. The given basicAccountsInfo is null");
        }
        if (bootstraps == null) {
            throw new NotificationException(
                "Unable to start the bootstrap process. The given bootstraps is null");
        }

        int numDevices       = bootstraps.length;
        int numBasicAccounts = basicAccountsInfo.length;

        if (numDevices != numBasicAccounts) {
            throw new NotificationException(
                "Unable to start the bootstrap process. The number of the bootstrap " +
                "objects (" + numDevices + ") is different from the number of the basicAccountInfo objects (" +
                numBasicAccounts + ")");
        }

        BootStrapSender bootstrapSender = getBootStrapSender();

        byte[][] messages      = new byte[numDevices][];
        String[] macs          = new String[numDevices];
        int[]    authMethods   = new int[numDevices];
        String[] phoneNumbers  = new String[numDevices];
        int[]    messageTypes  = new int[numDevices];
        String[] info         = new String[numDevices];

        for (int i=0; i<numDevices; i++) {

            phoneNumbers[i] = bootstraps[i].getMsisdn();
            messageTypes[i]  = bootstraps[i].getMessageType();
            switch (messageTypes[i] ) {
                case NotificationConstants.BOOTSTRAP_MESSAGE_TYPE_PLAIN:

                    PlainBootstrapMessageBuilder plainBuilder =
                        new PlainBootstrapMessageBuilder();

                    messages[i] = plainBuilder.buildMessage(basicAccountsInfo[i],
                                                            bootstraps[i].getNodes());
                    break;

                case NotificationConstants.BOOTSTRAP_MESSAGE_TYPE_WAP:

                    WapBootstrapMessageBuilder wapBuilder =
                        new WapBootstrapMessageBuilder();

                        messages[i] =  wapBuilder.buildMessage(basicAccountsInfo[i],
                                                               bootstraps[i].getWapProvisioningDoc());
                    break;

                case NotificationConstants.BOOTSTRAP_MESSAGE_TYPE_DM:

                    DMBootstrapMessageBuilder dmBuilder =
                        new DMBootstrapMessageBuilder();

                    messages[i] = dmBuilder.buildMessage(basicAccountsInfo[i],
                                                         bootstraps[i].getMgmtTree());
                    break;

                default:
                    throw new NotificationException("Message type not supported");
            }

            info[i] = bootstraps[i].getInfo();

            authMethods[i] = bootstraps[i].getAuthMethod();

            macs[i] = bootstraps[i].getDigest();
            if (macs[i] == null) {
                macs[i] = computeMac(authMethods[i],
                                     messages[i],
                                     bootstraps[i].getImsi(),
                                     bootstraps[i].getUserPin(),
                                     bootstraps[i].isImsiInSemiOctet());
            }

            if (log.isEnabledFor(Level.TRACE)) {
                log.trace("Bootstrap message to send: " + messages[i] + " (length: " + messages[i].length +
                         ")");
            }
        }

        // send message
        bootstrapSender.sendMessages(messageTypes,
                                     macs,
                                     authMethods,
                                     phoneNumbers,
                                     messages,
                                     info);
    }


    // --------------------------------------------------------- Private Methods

    /**
     * Computes the mac according to the given parameters.
     * The algo used is:
     * <ui>
     * <li>If authMethod = AUTH_METHOD_NETWPIN then the imsi is used as key and f imsiInSemiOctet
     * is true, the imsi is converted in semi-octet (else the imsi is used as textplain)</li>
     * <li>If authMethod = AUTH_METHOD_USERPIN then the userPin is used as key (textplain) and
     * imsi/imsiInSemiOctet parameters are ignored</li>
     * <li>If authMethod = AUTH_METHOD_USERNETWPIN then the imsi and the userPin are used as key
     * (if imsiInSemiOctet is true the imsi is converted in semi-octet)</li>
     * </ui>
     * @param authMethod the authMethod to use. One between:
     * <ui>
     * <li>NotificationConstants.AUTH_METHOD_NETWPIN</li>
     * <li>NotificationConstants.AUTH_METHOD_USERPIN</li>
     * <li><NotificationConstants.AUTH_METHOD_USERNETWPIN/li>
     * </ui>
     * @param message the message
     * @param imsi the imsi
     * @param userPin the userPin
     * @param imsiInSemiOctet do we use the imsi in semi octet ?
     * @return the mac
     */
    private String computeMac(int     authMethod,
                              byte[]  message,
                              String  imsi,
                              String  userPin,
                              boolean imsiInSemiOctet) throws NotificationException {

        String mac = null;
        if (authMethod == NotificationConstants.AUTH_METHOD_NETWPIN) {

            if (imsiInSemiOctet) {
                if (log.isEnabledFor(Level.TRACE)) {
                    log.trace("Compute mac using imsi in semi-octect");
                }

                mac = computeMac(getKeyFromIMSI(imsi), message);
            } else {
                if (log.isEnabledFor(Level.TRACE)) {
                    log.trace("Compute mac using imsi in textplain");
                }

                mac = computeMac(imsi.getBytes(), message);
            }
        } else if (authMethod == NotificationConstants.AUTH_METHOD_USERPIN) {

            byte[] key = null;
            //
            // If userpin is null, I use the imsi also isn't properly correct
            //
            if (userPin == null) {

                if (log.isEnabledFor(Level.TRACE)) {
                    log.trace(
                            "Try using USERPIN but userPin is null. Uses imsi to calculate the MAC ");
                }

                if (imsi == null) {
                    if (log.isEnabledFor(Level.TRACE)) {
                        log.trace(
                                "Try using USERPIN but userPin and imsi are null");
                    }
                    throw new NotificationException(
                            "Userpin or imsi must be not null using USERPIN authentication method");
                }

                if (imsiInSemiOctet) {

                    if (log.isEnabledFor(Level.TRACE)) {
                        log.trace("Compute mac using imsi in semi-octect");
                    }

                    key = getKeyFromIMSI(imsi);
                } else {
                    if (log.isEnabledFor(Level.TRACE)) {
                        log.trace("Compute mac using imsi in textplain");
                    }

                    key = imsi.getBytes();
                }
            } else {
                if (log.isEnabledFor(Level.TRACE)) {
                    log.trace("Compute mac using user pin");
                }
                key = userPin.getBytes();
            }

            mac = computeMac(key, message);
        } else if (authMethod == NotificationConstants.AUTH_METHOD_USERNETWPIN) {
            byte[] imsiKey = null;
            if (imsiInSemiOctet) {
                if (log.isEnabledFor(Level.TRACE)) {
                    log.trace(
                            "Compute mac using imsi (in semi-octect) and user pin");
                }

                imsiKey = getKeyFromIMSI(imsi);
            } else {
                if (log.isEnabledFor(Level.TRACE)) {
                    log.trace(
                            "Compute mac using imsi (in text-plain) and user pin");
                }
                imsiKey = imsi.getBytes();
            }
            // Append imsiKey with the user pin
            int imsiKeyLength = imsiKey.length;
            byte[] userPinKey = userPin.getBytes();
            int userPinKeyLength = userPinKey.length;
            byte[] key = new byte[imsiKeyLength + userPinKeyLength];
            System.arraycopy(imsiKey, 0, key, 0, imsiKeyLength);
            System.arraycopy(userPinKey, 0, key, imsiKeyLength, userPinKeyLength);
            mac = computeMac(key, message);
        }

        return mac;
    }


    /**
     * Returns the configured notification sender.
     *
     * @throws NotificationException
     * @return NotificationSender
     */
    private NotificationSender getNotificationSender()
    throws NotificationException {
        NotificationSender messageSender = null;

        // read the sender object from the config path
        ClassLoader classLoader = config.getClassLoader();

        try {

            messageSender = (NotificationSender)BeanFactory.getBeanInstance(classLoader, NOTIFICATION_SENDER_BEAN);

        } catch (Exception ex) {
            log.debug("getSender", ex);

            throw new NotificationException("Error reading the sender", ex);
        }

        return messageSender;
            }

    /**
     * Returns the configured bootstrap sender.
     *
     * @throws NotificationException
     * @return BootStrapSender
     */
    private BootStrapSender getBootStrapSender()
    throws NotificationException {

        BootStrapSender messageSender = null;

        // read the sender object from the config path
        ClassLoader classLoader = config.getClassLoader();

        try {
            messageSender = (BootStrapSender)BeanFactory.getBeanInstance(classLoader, BOOTSTRAP_SENDER_BEAN);
        } catch (Exception ex) {
            log.debug("getSender", ex);

            throw new NotificationException("Error reading the sender", ex);
        }

        return messageSender;
    }

    /**
     * Returns the notification message builder for the given messageType
     * @param messageType int
     * @throws NotificationException
     * @return NotificationMessageBuilder
     */
    private NotificationMessageBuilder getNotificationMessageBuilder(int messageType) throws
        NotificationException {

        NotificationMessageBuilder messageBuilder = null;

        // get NotificationMessageBuilder
        switch (messageType) {
            case NotificationConstants.NOTIFICATION_MESSAGE_TYPE_GENERIC:
                messageBuilder = new GenericNotificationMessageBuilder();
                break;
            default:
                throw new NotificationException("Message type not supported");
        }

        return messageBuilder;
    }

    /**
     * Compute the mac of the message using the given key.
     *
     * @param key the key to use
     * @param message the message
     * @throws NotificationException if a error occurs
     * @return the mac of the message
     */
    private String computeMac(byte[] key, byte[] message) throws NotificationException {

        byte[] bMac = null;

        try {
            bMac = SecurityTools.computeHmacSha1(key, message);
        } catch (GeneralSecurityException e) {
            throw new NotificationException("Error computing the mac", e);
        }

        String mac  = bytesToHex(bMac);

        if (log.isEnabledFor(Level.TRACE)) {
            log.trace("Mac: " + mac);
        }
        return mac;
    }

    /**
     * Returns hex String representation of given byte[]
     * @param b byte[]
     * @return the hex representation of the byte[]
     */
    private static String bytesToHex(byte[] b) {
        StringBuffer buf = new StringBuffer("");
        for (int i = 0; i < b.length; i++) {
            buf.append(byteToHex(b[i]));
        }
        return buf.toString();
    }

    /**
     * Returns hex String representation of given byte
     * @param b byte
     * @return the hex representation of the byte
     */
    private static String byteToHex(byte b) {
        // Returns hex String representation of byte b
        char hexDigit[] = {
                          '0', '1', '2', '3', '4', '5', '6', '7',
                          '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
        };
        char[] array = {hexDigit[ (b >> 4) & 0x0f], hexDigit[b & 0x0f]};
        return new String(array);
    }

    /**
     * Computes the bytes array to use as key in the mac calculation
     * based on imsi. The IMSI MUST be on semi-octet representation as defined
     * in Digital cellular Telecommunications system (Phase 2+); Specification of the Subscriber
     * Identity Module - Mobile Equipment (SIM - ME) interface
     * (GSM 11.11 version 7.2.0 Release 1998)
     *
     * @param imsi String
     * @return byte[]
     */
    private static byte[] getKeyFromIMSI(String imsi) throws NotificationException {

        imsi = imsi.trim();

        if (log.isEnabledFor(Level.TRACE)) {
            log.trace("IMSI: " + imsi);
        }

        if ( (imsi.length() % 2) == 1 ) {
            imsi = "9" + imsi;
        } else {
            imsi = "1" + imsi;
            imsi = imsi + "F";

        }

        if (log.isEnabledFor(Level.TRACE)) {
            log.trace("IMSI updated: " + imsi);
        }

        int numDigit = imsi.length();
        String temp = null;
        char c1 = 0;
        char c2 = 0;
        byte[] key = new byte[numDigit / 2]; // always even
        int t = 0;
        for (int i = 0; i < numDigit; i++) {
            c1 = imsi.charAt(i);
            c2 = imsi.charAt(++i);
            temp = "" + c2 + c1;
            try {
                key[t] = (byte) (Integer.parseInt(temp, 16));
            } catch (NumberFormatException ex) {
                throw new NotificationException("IMSI isn't valid (only numbers are permitted)");
            }
            t++;
        }
        if (log.isEnabledFor(Level.TRACE)) {
            log.trace("Key from imsi: " + bytesToHex(key));
        }

        return key;
    }

}
