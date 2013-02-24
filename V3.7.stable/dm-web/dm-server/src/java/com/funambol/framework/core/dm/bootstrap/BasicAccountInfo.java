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

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Contains the basic account information to use bootstrapping a device.
 *
 * @version $Id: BasicAccountInfo.java,v 1.1 2006/11/15 13:43:15 nichele Exp $
 */
public class BasicAccountInfo {

    // -------------------------------------------------------------- Properties
    private String deviceId;
    private String accountName;
    private String serverAddress;
    private int    serverPort;
    private String serverId;
    private String serverPassword;
    private byte[] serverNonce;
    private String clientUsername;
    private String clientPassword;
    private byte[] clientNonce;
    private String authPref;
    private int    addressType;

    /**
     * Gets the deviceId
     * @return Returns the deviceId property
     */
    public String getDeviceId() {
        return this.deviceId;
    }

    /**
     * Sets the deviceId
     * @param deviceId the deviceId property to set
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Gets the accountName
     * @return the accountName
     */
    public String getAccountName() {
        return accountName;
    }

    /**
     * Sets the accountName
     * @param accountName the accountName to sets
     */
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    /**
     * Gets the serverAddress
     * @return the serverAddress
     */
    public String getServerAddress() {
        return serverAddress;
    }

    /**
     * Sets the serverAddress
     * @param serverAddress the serverAddress to sets
     */
    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    /**
     * Gets the serverPort
     * @return the serverPort
     */
    public int getServerPort() {
        return serverPort;
    }

    /**
     * Sets the serverPort
     * @param serverPort the serverPort to sets
     */
    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    /**
     * Gets the serverId
     * @return the serverId
     */
    public String getServerId() {
        return serverId;
    }

    /**
     * Sets the serverId
     * @param serverId the serverId to sets
     */
    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    /**
     * Gets the serverPassword
     * @return the serverPassword
     */
    public String getServerPassword() {
        return serverPassword;
    }

    /**
     * Sets the serverPassword
     * @param serverPassword the serverPassword to sets
     */
    public void setServerPassword(String serverPassword) {
        this.serverPassword = serverPassword;
    }

    /**
     * Gets the serverNonce
     * @return the serverNonce
     */
    public byte[] getServerNonce() {
        return serverNonce;
    }

    /**
     * Sets the serverNonce
     * @param serverNonce the serverNonce to sets
     */
    public void setServerNonce(byte[] serverNonce) {
        this.serverNonce = serverNonce;
    }

    /**
     * Gets the clientUsername
     * @return the clientUsername
     */
    public String getClientUsername() {
        return clientUsername;
    }

    /**
     * Sets the clientUsername
     * @param clientUsername the clientUsername to sets
     */
    public void setClientUsername(String clientUsername) {
        this.clientUsername = clientUsername;
    }

    /**
     * Gets the clientPassword
     * @return the clientPassword
     */
    public String getClientPassword() {
        return clientPassword;
    }

    /**
     * Sets the clientPassword
     * @param clientPassword the clientPassword to sets
     */
    public void setClientPassword(String clientPassword) {
        this.clientPassword = clientPassword;
    }

    /**
     * Gets the clientNonce
     * @return the clientNonce
     */
    public byte[] getClientNonce() {
        return clientNonce;
    }

    /**
     * Sets the clientNonce
     * @param clientNonce the clientNonce to sets
     */
    public void setClientNonce(byte[] clientNonce) {
        this.clientNonce = clientNonce;
    }

    /**
     * Gets the authPref
     * @return the authPref
     */
    public String getAuthPref() {
        return authPref;
    }

    /**
     * Sets the authPref
     * @param authPref the authPref to sets
     */
    public void setAuthPref(String authPref) {
        this.authPref = authPref;
    }

    /**
     * Gets the addressType
     * @return Returns the addressType property.
     */
    public int getAddressType() {
        return this.addressType;
    }

    /**
     * Sets the addressType
     * @param addressType the addressType property to set.
     */
    public void setAddressType(int addressType) {
        this.addressType = addressType;
    }

    // ------------------------------------------------------------- Constructor

    /**
     * Creates a new BasicAccountInfo with the given parameters
     */
    public BasicAccountInfo(String deviceId,
                            String accountName,
                            String serverAddress,
                            int    serverPort,
                            String serverId,
                            String serverPassword,
                            byte[] serverNonce,
                            String clientUsername,
                            String clientPassword,
                            byte[] clientNonce,
                            String authPref,
                            int    addressType) {

        this.deviceId       = deviceId;
        this.accountName    = accountName;
        this.serverAddress  = serverAddress;
        this.serverPort     = serverPort;
        this.serverId       = serverId;
        this.serverPassword = serverPassword;
        this.serverNonce    = serverNonce;
        this.clientUsername = clientUsername;
        this.clientPassword = clientPassword;
        this.clientNonce    = clientNonce;
        this.authPref       = authPref;
        this.addressType    = addressType;
    }

    /**
     * Returns a String representation of this BasicAccountInfo
     * @return String
     */
    public String toString() {
        ToStringBuilder sb = new ToStringBuilder(this);

        sb.append("deviceId"      , deviceId);        
        sb.append("accountName"   , accountName);
        sb.append("serverAddress" , serverAddress);
        sb.append("serverPort"    , serverPort);
        sb.append("serverId"      , serverId);
        sb.append("serverPassword", serverPassword);
        sb.append("serverNonce"   , serverNonce);
        sb.append("clientUsername", clientUsername);
        sb.append("clientPassword", clientPassword);
        sb.append("clientNonce"   , clientNonce);
        sb.append("authPref"      , authPref);
        sb.append("addressType"   , addressType);        

        return sb.toString();
    }

}
