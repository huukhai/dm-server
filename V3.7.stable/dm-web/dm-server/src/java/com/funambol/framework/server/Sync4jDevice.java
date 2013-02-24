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

package com.funambol.framework.server;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;
import com.funambol.framework.tools.Base64;

/**
 * This class represents a Sync4j device.
 *
 *
 *
 * @version $Id: Sync4jDevice.java,v 1.5 2007-06-19 08:16:16 luigiafassina Exp $
 *
 */
public class Sync4jDevice implements Serializable {

    // --------------------------------------------------------------- Constants
    public static final String PROPERTY_ID              = "id";
    public static final String PROPERTY_DESCRIPTION     = "description";
    public static final String PROPERTY_TYPE            = "type";
    public static final String PROPERTY_DIGEST          = "digest";
    public static final String PROPERTY_CLIENT_NONCE    = "client_nonce";
    public static final String PROPERTY_SERVER_NONCE    = "server_nonce";
    public static final String PROPERTY_SERVER_PASSWORD = "server_password";


    // ------------------------------------------------------------ Private data
    private String deviceId      ;
    private String description   ;
    private String type          ;
    private String digest        ;
    private byte[] clientNonce   ;
    private byte[] serverNonce   ;
    private String serverPassword;

    // ------------------------------------------------------------- Costructors
    /** Creates a new instance of Sync4jDevice */
    public Sync4jDevice() {
        this(null, null, null, null, null, null);
    }

    /**
     * Creates a new instance of Sync4jDevice.
     *
     * @param deviceId the device identification
     */
    public Sync4jDevice(String deviceId) {
        this(deviceId, null, null, null, null, null);
    }

    /**
     * Creates a new instance of Sync4jDevice.
     *
     * @param deviceId the device identification
     * @param description the device description
     * @param type the device type
     *
     */
    public Sync4jDevice(String deviceId, String description, String type) {
        this(deviceId, description, type, null, null, null);

        this.deviceId    = deviceId;
        this.description = description;
        this.type        = type;
    }

    /**
     * Creates a new instance of Sync4jDevice.
     *
     * @param deviceId the device identification
     * @param description the device description
     * @param type the device type
     * @param digest the digest for MD5 authentication
     * @param clientNonce the next nonce sent by server for MD5 authentication
     * @param serverNonce the next nonce sent by client for MD5 authentication
     */
    public Sync4jDevice(final String deviceId   ,
                        final String description,
                        final String type       ,
                        final String digest     ,
                        final byte[] clientNonce ,
                        final byte[] serverNonce) {
        this(deviceId, description, type, digest, clientNonce, serverNonce, null);

    }

    /**
     * Creates a new instance of Sync4jDevice.
     *
     * @param deviceId the device identification
     * @param description the device description
     * @param type the device type
     * @param digest the digest for MD5 authentication
     * @param clientNonce    the next nonce sent by server for MD5 authentication
     * @param serverNonce    the next nonce sent by client for MD5 authentication
     * @param serverPassword the server password for this device
     */
    public Sync4jDevice(final String deviceId   ,
                        final String description,
                        final String type       ,
                        final String digest     ,
                        final byte[] clientNonce,
                        final byte[] serverNonce,
                        final String serverPassword) {
        this.deviceId       = deviceId;
        this.description    = description;
        this.type           = type;
        this.digest         = digest;
        this.clientNonce    = clientNonce;
        this.serverNonce    = serverNonce;
        this.serverPassword = serverPassword;
    }


    /** Getter for property deviceId.
     * @return Value of property deviceId.
     *
     */
    public String getDeviceId() {
        return deviceId;
    }

    /** Setter for property deviceId.
     * @param deviceId New value of property deviceId.
     *
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /** Getter for property description.
     * @return Value of property description.
     *
     */
    public String getDescription() {
        return description;
    }

    /** Setter for property description.
     * @param description New value of property description.
     *
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /** Getter for property type.
     * @return Value of property type.
     *
     */
    public String getType() {
        return type;
    }

    /** Setter for property type.
     * @param type New value of property type.
     *
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the digest property
     *
     * @return digest the digest value
     */
    public String getDigest() {
        return this.digest;
    }

    /**
     * Sets the digest property
     *
     * @param digest the digest property
     *
     */
    public void setDigest(String digest) {
        this.digest = digest;
    }

    /**
     * Gets the client_nonce property
     *
     * @return clientNonce the nonce that the server uses in order to authenticate
     *                     iteself with the client
     */
    public byte[] getClientNonce() {
        return this.clientNonce;
    }

    /**
     * Sets the client_nonce property
     *
     * @param clientNonce the nonce that the server uses in order to authenticate
     *                    iteself with the client
     */
    public void setClientNonce(byte[] clientNonce) {
        this.clientNonce = clientNonce;
    }

    /**
     * Gets the server_nonce property
     *
     * @return serverNonce the nonce that the client uses in order to authenticate
     *                     iteself with the server
     */
    public byte[] getServerNonce() {
        return this.serverNonce;
    }

    /**
     * Sets the server_nonce property
     *
     * @param serverNonce the nonce that the client uses in order to authenticate
     *                    iteself with the server
     */
    public void setServerNonce(byte[] serverNonce) {
        this.serverNonce = serverNonce;
    }

    /**
     * Gets the server password property
     *
     * @return serverPassword the server password for this device
     */
    public String getServerPassword() {
        return this.serverPassword;
    }

    /**
     * Sets the server password property
     *
     * @param serverPassword the server password for this device
     */
    public void setServerPassword(String serverPassword) {
        this.serverPassword = serverPassword;
    }



    public String toString() {
        ToStringBuilder sb = new ToStringBuilder(this);

         sb.append("deviceId",         deviceId);
         sb.append("description",      description);
         sb.append("type",             type);
         if (digest != null) {
             sb.append("digest",       digest);
         }
         if (clientNonce != null) {
             sb.append("client_nonce", new String(Base64.encode(clientNonce)));
         } else {
             sb.append("client_nonce", "is null");
         }
         if (serverNonce != null) {
             sb.append("server_nonce", new String(Base64.encode(serverNonce)));
         } else {
             sb.append("server_nonce", "is null");
         }

         sb.append("serverPassword",   serverPassword);

         return sb.toString();
    }

}
