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

package com.funambol.framework.core.dm.bootstrap;

import com.funambol.framework.tools.Base64;

/**
 * Corresponds to the single dm account node in the management tree
 *
 * @version $Id: DMAccount.java,v 1.1 2006/11/15 13:43:15 nichele Exp $
 */
public class DMAccount  implements java.io.Serializable {

    // --------------------------------------------------------------- Constants

   public static final String DMACC_ADDR             = "/Addr";
   public static final String DMACC_ADDR_TYPE        = "/AddrType"  ;
   public static final String DMACC_PORT_NBR         = "/PortNbr"  ;
   public static final String DMACC_CON_REF          = "/ConRef";
   public static final String DMACC_SERVER_ID        = "/ServerId";
   public static final String DMACC_SERVER_PASSWORD  = "/ServerPW";
   public static final String DMACC_SERVER_NONCE     = "/ServerNonce";
   public static final String DMACC_USERNAME         = "/UserName";
   public static final String DMACC_CLIENT_PASSWORD  = "/ClientPW";
   public static final String DMACC_CLIENT_NONCE     = "/ClientNonce";
   public static final String DMACC_AUTH_PREF        = "/AuthPref";
   public static final String DMACC_NAME             = "/Name";


   // ------------------------------------------------------------ Private data
   private String address;
   private int    addressType;
   private int    portNumber;
   private String conRef;
   private String serverId;
   private String serverPassword;
   private byte[] serverNonce;
   private String userName;
   private String clientPassword;
   private byte[] clientNonce;
   private String authPref;
   private String name;



   // ------------------------------------------------------------ Constructors
   /** For serialization purposes */
   public DMAccount() {}

    /**
     * Creates a new dm account object with the given parameters
     *
     * @param address the server address
     * @param addressType the type of the server address
     * @param portNumber the port number to use
     * @param conRef the reference to connectivity information
     * @param serverId the server identifier
     * @param serverPassword the server password
     * @param serverNonce the next nonce that the server will use
     * @param clientPassword the password of the client
     * @param clientNonce the next nonce that the client will use
     * @param authPref the syncml authentication type
     * @param name the user displayable name
     */
    public DMAccount(final String address,
                     final int    addressType,
                     final int    portNumber,
                     final String conRef,
                     final String serverId,
                     final String serverPassword,
                     final byte[] serverNonce,
                     final String userName,
                     final String clientPassword,
                     final byte[] clientNonce,
                     final String authPref,
                     final String name) {
        this.address         = address;
        this.addressType     = addressType;
        this.portNumber      = portNumber;
        this.conRef          = conRef;
        this.serverId        = serverId;
        this.serverPassword  = serverPassword;
        this.serverNonce     = serverNonce;
        this.userName        = userName;
        this.clientPassword  = clientPassword;
        this.clientNonce     = clientNonce;
        this.authPref        = authPref;
        this.name            = name;
    }


    // ---------------------------------------------------------- Public methods

    /**
     * Gets the userName
     *
     * @return  the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the userName
     *
     * @param  userName the userName
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Gets the clientNonce
     *
     * @return  the clientNonce
     */
    public byte[] getClientNonce() {
        return clientNonce;
    }

    /**
     * Sets the clientNonce
     *
     * @param  clientNonce the clientNonce
     */
    public void setClientNonce(byte[] clientNonce) {
        this.clientNonce = clientNonce;
    }

    /**
     * Sets the clientNonce
     *
     * @param  b64ClientNonce the client nonce
     */
    public void setClientNonce(String b64ClientNonce) {
        this.clientNonce = Base64.decode(b64ClientNonce);
    }

    /**
     * Gets the address
     *
     * @return  the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the address
     *
     * @param  address the address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Gets the serverId
     *
     * @return  the serverId
     */
    public String getServerId() {
        return serverId;
    }

    /**
     * Sets the serverId
     *
     * @param  serverId the serverId
     */
    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    /**
     * Gets the addressType
     *
     * @return  the addressType
     */
    public int getAddressType() {
        return addressType;
    }

    /**
     * Sets the addressType
     *
     * @param  addressType the addressType
     */
    public void setAddressType(int addressType) {
        this.addressType = addressType;
    }

    /**
     * Gets the serverPassword
     *
     * @return  the serverPassword
     */
    public String getServerPassword() {
        return serverPassword;
    }

    /**
     * Sets the serverPassword
     *
     * @param  serverPassword the serverPassword
     */
    public void setServerPassword(String serverPassword) {
        this.serverPassword = serverPassword;
    }

    /**
     * Gets the portNumber
     *
     * @return  the portNumber
     */
    public int getPortNumber() {
        return portNumber;
    }

    /**
     * Sets the portNumber
     *
     * @param  portNumber the portNumber
     */
    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

    /**
     * Gets the conRef
     *
     * @return  the conRef
     */
    public String getConRef() {
        return conRef;
    }

    /**
     * Sets the conRef
     *
     * @param  conRef the conRef
     */
    public void setConRef(String conRef) {
        this.conRef = conRef;
    }

    /**
     * Gets the name
     *
     * @return  the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name
     *
     * @param  name the name
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * Gets the serverNonce
     *
     * @return  the serverNonce
     */
    public byte[] getServerNonce() {
        return serverNonce;
    }

    /**
     * Sets the serverNonce
     *
     * @param  serverNonce the serverNonce
     */
    public void setServerNonce(byte[] serverNonce) {
        this.serverNonce = serverNonce;
    }

    /**
     * Sets the serverNonce
     *
     * @param  b64ServerNonce the serverNonce
     */
    public void setServerNonce(String b64ServerNonce) {
        this.serverNonce = Base64.decode(b64ServerNonce);
    }

    /**
     * Gets the clientPassword
     *
     * @return  the clientPassword
     */
    public String getClientPassword() {
        return clientPassword;
    }

    /**
     * Sets the clientPassword
     *
     * @param  clientPassword the clientPassword
     */
    public void setClientPassword(String clientPassword) {
        this.clientPassword = clientPassword;
    }

    /**
     * Gets the authPref
     *
     * @return  the authPref
     */
    public String getAuthPref() {
        return authPref;
    }

    /**
     * Sets the authPref
     *
     * @param  authPref the authPref
     */
    public void setAuthPref(String authPref) {
        this.authPref = authPref;
    }
}
