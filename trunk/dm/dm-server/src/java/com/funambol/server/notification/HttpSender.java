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

import java.io.BufferedOutputStream;
import java.net.Socket;
import java.net.URL;

import com.funambol.framework.notification.NotificationConstants;
import com.funambol.framework.notification.NotificationException;
import com.funambol.framework.notification.NotificationSender;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;


/**
 * This sender sends notification message via http (to use with SCTS DM)
 * <p/>
 * To obtain the same result of the SCTS server you must to set:
 * <ui>SessionId: 12345</ui>
 * <ui>ServerId: Scts</ui>
 *  <ui>ServerPwd: SimpleClient</ui>
 * <ui>ServerNonce: AOVuAA==   (this is already in base 64 format. Corresponds to 00E56E00)</ui>
 * <br/>and in <code>com.funambol.server.notification.NotificationEngineImpl</code> you must to set:
 *  <ui>SYNCML_DM_PROTOCOL_VERSION = 1.1f</ui>
 *
 * @version $Id: HttpSender.java,v 1.7 2007-06-19 08:16:25 luigiafassina Exp $
 */
public class HttpSender implements NotificationSender {

    // -------------------------------------------------------------- Properties
    private String deviceAddress = null;

    // ------------------------------------------------------------ Private data

    private transient static final Logger log = Logger.getLogger(com.funambol.server.notification.HttpSender.class.getName());

    // ------------------------------------------------------------- Constructor
    public HttpSender() {

    }

    // ---------------------------------------------------------- Public Methods

    /**
     * Gets the address device
     *
     * @return  the address device
     */
    public String getDeviceAddress() {
        return deviceAddress;
    }

    /**
     * Sets the address device
     *
     * @param  deviceAddress the address device
     */
    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    /**
     * Sends messages
     * @param messageType the type of the message. See {@link NotificationConstants}
     * @param phoneNumbers the phone numbers list of the target device
     * @param messages the messages to send
     * @param info application specific info
     * @throws NotificationException
     */
    public void sendMessages(int      messageType,
                             String[] phoneNumbers,
                             byte[][] messages,
                             String   info) throws NotificationException {

        for (int i=0; i<phoneNumbers.length; i++) {

            //
            // For the first message not wait
            //
            if (i != 0) {
                System.out.println("Wait 20 s for test with SCTS");
                try {
                    Thread.sleep(20000);
                } catch (InterruptedException ex) {
                }

            }
            sendMessage(messageType, phoneNumbers[i], messages[i], info);
        }
    }

    // --------------------------------------------------------- Private methods
    /**
     * Send the given message via http
     * @param messageType int
     * @param phoneNumber String
     * @param message byte[]
     * @param info String
     */
    private void sendMessage(int messageType, String phoneNumber, byte[] message, String info)
        throws NotificationException {

        if (log.isEnabled(Level.INFO)) {
            log.info("sendMessage: " + message + " (length: " + message.length + ")");
        }

        if (messageType != NotificationConstants.NOTIFICATION_MESSAGE_TYPE_GENERIC) {
            throw new NotificationException("This sender is usable only for notification message");
        }

        byte[] nestedRequest = buildNestedRequest(message);
        byte[] request = buildRequest(nestedRequest);

        try {
            sendHttpRequest(request);
        } catch (Exception e) {
            throw new NotificationException("Error sending the message via http", e);
        }

    }

    private void sendHttpRequest(byte[] message) throws Exception {
        if (log.isEnabled(Level.INFO)) {
            log.info("Send message to: " + deviceAddress);
        }
        URL url = new URL(deviceAddress);
        String host = url.getHost();
        int port = url.getPort();

        Socket conn = new Socket(host, port);

        // Send data
        BufferedOutputStream wr = new BufferedOutputStream(conn.getOutputStream());

        wr.write(message);
        wr.flush();

        wr.close();
    }

    private byte[] buildRequest(byte[] message) {
        StringBuffer sb = new StringBuffer("POST /wappush HTTP/1.1\r\n");
        sb.append("Host: 127.0.0.1\r\n");
        sb.append("Content-Type: application/http\r\n");
        sb.append("Content-Length: ").append(message.length).append("\r\n");
        sb.append("X-Wap-Push-OTA-Version: 1.0\r\n\r\n");

        byte[] header = sb.toString().getBytes();

        byte[] request = new byte[header.length + message.length];

        System.arraycopy(header, 0, request, 0, header.length);
        System.arraycopy(message, 0, request, header.length, message.length);

        return request;

    }

    private byte[] buildNestedRequest(byte[] message) {
        StringBuffer sb = new StringBuffer("HTTP/1.1 200 OK\r\n");
        sb.append("Content-Language: en\r\n");
        sb.append("Content-Length: ").append(message.length).append("\r\n");
        sb.append("Content-Type: application/vnd.syncml.notification\r\n\r\n");

        byte[] header = sb.toString().getBytes();

        byte[] nestedRequest = new byte[header.length + message.length];

        System.arraycopy(header, 0, nestedRequest, 0, header.length);
        System.arraycopy(message, 0, nestedRequest, header.length, message.length);

        return nestedRequest;


    }

}
