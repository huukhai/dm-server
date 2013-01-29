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

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.funambol.framework.engine.dm.TreeNode;

import com.funambol.framework.core.dm.ddf.MgmtTree;

/**
 * Contains the info to bootstrap a device.
 *
 * @version $Id: BootStrap.java,v 1.1 2006/11/15 13:43:15 nichele Exp $
 */
public class BootStrap implements Serializable {

    private static final long serialVersionUID = -5837833278316924293L;

    // -------------------------------------------------------------- Properties
    private String             msisdn;
    private int                messageType;
    private String             sender;
    private WapProvisioningDoc wapProvisioningDoc;
    private MgmtTree           mgmtTree;
    private TreeNode[]         nodes;
    private String             accountName;
    private String             username;
    private String             password;
    private String             deviceId;
    private String             imsi;
    private String             userPin;
    private String             digest;
    private int                authMethod;
    private boolean            imsiInSemiOctet;
    private String             info;
    private String             operation;

    /**
     * Gets the msisdn
     * @return the msisdn
     */
    public String getMsisdn() {
        return msisdn;
    }

    /**
     * Sets the msisdn
     * @param msisdn the msisdn to sets
     */
    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
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
     * Gets the username
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username
     * @param username the username to sets
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the password
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password
     * @param password the password to sets
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the deviceId
     * @return the deviceId
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Sets the deviceId
     * @param deviceId the deviceId to sets
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Gets the imsi
     * @return the imsi
     */
    public String getImsi() {
        return imsi;
    }

    /**
     * Sets the imsi
     * @param imsi the imsi to sets
     */
    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    /**
     * Gets the digest
     * @return the digest
     */
    public String getDigest() {
        return digest;
    }

    /**
     * Sets the digest
     * @param digest the digest to sets
     */
    public void setDigest(String digest) {
        this.digest = digest;
    }

    /**
     * Gets the authMethod
     * @return the authMethod
     */
    public int getAuthMethod() {
        return authMethod;
    }

    /**
     * Sets the authMethod
     * @param authMethod the authMethod to sets
     */
    public void setAuthMethod(int authMethod) {
        this.authMethod = authMethod;
    }

    /**
     * Gets the imsiInSemiOctet
     * @return the imsiInSemiOctet
     */
    public boolean isImsiInSemiOctet() {
        return imsiInSemiOctet;
    }

    /**
     * Sets if the imsi must be codified in semi-octet
     * @param imsiInSemiOctet the imsi must be codified in semi-octet ?
     */
    public void setImsiInSemiOctet(boolean imsiInSemiOctet) {
        this.imsiInSemiOctet = imsiInSemiOctet;
    }

    /**
     * Gets the userPin
     * @return the userPin
     */
    public String getUserPin() {
        return userPin;
    }

    /**
     * Sets the userPin
     * @param userPin the userPin to sets
     */
    public void setUserPin(String userPin) {
        this.userPin = userPin;
    }


    /**
     * Gets the info
     * @return the info
     */
    public String getInfo() {
        return info;
    }

    /**
     * Sets the info
     * @param info the info to sets
     */
    public void setInfo(String info) {
        this.info = info;
    }

    /**
     * Gets the messageType
     * @return the userPin
     */
    public int getMessageType() {
        return messageType;
    }

    /**
     * Sets the messageType
     * @param messageType the messageType to sets
     */
    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    /**
     * Gets the sender
     * @return the sender
     */
    public String getSender() {
        return sender;
    }

    /**
     * Sets the sender
     * @param sender the sender to sets
     */
    public void setSender(String sender) {
        this.sender = sender;
    }

    /**
     * Gets the operation
     * @return the operation
     */
    public String getOperation() {
        return operation;
    }

    /**
     * Sets the operation
     * @param operation the operation to sets
     */
    public void setOperation(String operation) {
        this.operation = operation;
    }

    /**
     * Gets the wapProvisioningDoc
     * @return the wapProvisioningDoc
     */
    public WapProvisioningDoc getWapProvisioningDoc() {
        return wapProvisioningDoc;
    }

    /**
     * Sets the wapProvisioningDoc
     * @param wapDoc the wapProvisioningDoc to sets
     */
    public void setWapProvisioningDoc(WapProvisioningDoc wapDoc) {
        this.wapProvisioningDoc = wapDoc;
    }

    /**
     * Gets the nodes
     * @return the nodes
     */
    public TreeNode[] getNodes() {
        return nodes;
    }

    /**
     * Sets the nodes
     * @param nodes the nodes to sets
     */
    public void setNodes(TreeNode[] nodes) {
        this.nodes = nodes;
    }

    /**
     * Gets the array of mgmtTree
     * @return the mgmtTree
     */
    public MgmtTree getMgmtTree() {
        return mgmtTree;
    }

    /**
     * Sets the array of mgmtTree
     * @param mgmtTree the mgmtTree to sets
     */
    public void setMgmtTree(MgmtTree mgmtTree) {
        this.mgmtTree = mgmtTree;
    }

    // ------------------------------------------------------------ Constructors

    /**
     * Creates a new Bootstrap
     */
    public BootStrap() {
    }

    /**
     * Creates a new Bootstrap with the given parameters
     */
    public BootStrap(String             msisdn,
                     int                messageType,
                     String             sender,
                     WapProvisioningDoc wapDoc,
                     TreeNode[]         nodes,
                     MgmtTree           mgtmTree,
                     String             accountName,
                     String             username,
                     String             password,
                     String             deviceId,
                     String             imsi,
                     String             userPin,
                     String             digest,
                     int                authMethod,
                     boolean            imsiInSemiOctet,
                     String             info,
                     String             operation) {

        this.msisdn             = msisdn;
        this.messageType        = messageType;
        this.sender             = sender;
        this.wapProvisioningDoc = wapDoc;
        this.nodes              = nodes;
        this.mgmtTree           = mgtmTree;
        this.accountName        = accountName;
        this.username           = username;
        this.password           = password;
        this.deviceId           = deviceId;
        this.imsi               = imsi;
        this.userPin            = userPin;
        this.digest             = digest;
        this.authMethod         = authMethod;
        this.imsiInSemiOctet    = imsiInSemiOctet;
        this.info               = info;
        this.operation          = operation;
    }

    // --------------------------------------------------------- Pubblic Methods

    /**
     * Returns a String representation of this Bootstrap
     * @return String
     */
    public String toString() {
        ToStringBuilder sb = new ToStringBuilder(this);

        sb.append("msisdn"            , msisdn);
        sb.append("messageType"       , messageType);
        sb.append("sender"            , sender);
        sb.append("wapProvisioningDoc", wapProvisioningDoc);
        sb.append("nodes"             , nodes);
        sb.append("mgmtTree"          , mgmtTree);
        sb.append("accountName"       , accountName);
        sb.append("username"          , username);
        sb.append("password"          , password);
        sb.append("deviceId"          , deviceId);
        sb.append("imsi"              , imsi);
        sb.append("userPin"           , userPin);
        sb.append("authMethod"        , authMethod);
        sb.append("digest"            , digest);
        sb.append("imsiInSemiOctet"   , imsiInSemiOctet);
        sb.append("info"              , info);
        sb.append("operation"         , operation);

        return sb.toString();
    }


}
