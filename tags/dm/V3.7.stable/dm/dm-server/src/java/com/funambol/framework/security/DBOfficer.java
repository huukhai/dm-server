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

package com.funambol.framework.security;


import java.security.Principal;

import com.funambol.framework.core.Cred;
import com.funambol.framework.core.Authentication;
import com.funambol.framework.config.Configuration;
import com.funambol.framework.config.ConfigurationConstants;
import com.funambol.framework.core.HMACAuthentication;
import com.funambol.framework.core.Constants;
import com.funambol.framework.tools.MD5;
import com.funambol.framework.tools.Base64;
import com.funambol.framework.server.Sync4jDevice;
import com.funambol.framework.server.SyncUser;
import com.funambol.framework.server.store.NotFoundException;
import com.funambol.framework.server.store.PersistentStore;
import com.funambol.framework.server.store.PersistentStoreException;
import com.funambol.framework.server.store.WhereClause;

import com.funambol.server.admin.UserManager;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;


/**
 * This implementation of <i>Officier</i>
 *
 *.com
 */
public class DBOfficer implements Officer, java.io.Serializable {

    // ---------------------------------------------------------- Protected data

    protected transient static final Logger log = Logger.getLogger(com.funambol.framework.security.DBOfficer.class.getName());
    protected PersistentStore ps = null;

    // ------------------------------------------------------------- Public data
    // -------------------------------------------------------------- Properties

    /**
     * Has the last login failed for incorrect login/password?
     */
    private boolean loginFailed = false;

    public boolean isLoginFailed() {
        return loginFailed;
    }

    /**
     * Has the last login failed for expired temporary login?
     */
    private boolean loginExpired = false;

    /**
     * Can use a guest credential if there is not credential in the message?
     */
    private boolean guestEnabled = true;

    public boolean isGuestEnabled() {
        return this.guestEnabled;
    }

    public void setGuestEnabled(boolean guestEnabled) {
        this.guestEnabled = guestEnabled;
    }

    /**
     * Which type of authetication does impose the server?
     */
    private String authType = "syncml:auth-md5";

    public String getAuthType() {
        return this.authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;

        // the authType is supported!!!!
        this.supportedAuthType = this.supportedAuthType + this.authType;
    }


    private String supportedAuthType = null;

    public String getSupportedAuthType() {
        return this.supportedAuthType;
    }

    public void setSupportedAuthType(String supportedAuthType) {

        // the authType is supported!!!!
        this.supportedAuthType = this.authType + supportedAuthType;
    }


    // ---------------------------------------------------------- Public methods

    /**
     * Authenticates a credential
     *
     * @param credential the credential to be authenticated
     *
     * @return true if the credential is autenticated, false otherwise
     */
    public boolean authenticate(Cred credential) {

        String type = credential.getType();

        if ( (Constants.AUTH_TYPE_BASIC).equals(type)  ) {
            return authenticateBasicCredential(credential);
        } else if ( (Constants.AUTH_TYPE_MD5).equals(type)  ) {
            return authenticateMD5Credential(credential);
        } else if ( (Constants.AUTH_TYPE_HMAC).equals(type)  ) {
            return authenticateHMACCredential(credential);
        } else {
            return false;
        }
    }

    /**
     * Authorizes a resource.
     *
     * @param principal the requesting entity
     * @param resource the name (or the identifier) of the resource to be authorized
     *
     * @return true if the credential is authorized to access the resource, false
     *         otherwise
     */
    public boolean authorize(Principal principal, String resource) {
        return true;
    }

    /** Un-authenticates a credential.
     *
     * @param credential the credential to be unauthenticated
     */
    public void unAuthenticate(Cred credential) {
        //
        // Do nothing. In the current implementation, the authentication is
        // discarde as soon as the LoginContext is garbage collected.
        //
    }

    /**
     * @see com.funambol.framework.security.Officer
     */
    public boolean isAccountExpired() {
        return loginExpired;
    }


    // --------------------------------------------------------- Private Methods
    private void readObject(java.io.ObjectInputStream in)
    throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
    }


    private boolean authenticateBasicCredential(Cred credential) {
        SyncUser       user   = null                          ;
        Authentication auth   = credential.getAuthentication();

        String username = null;
        String password = null;

        String s = new String(Base64.decode(credential.getData()));

        int p = s.indexOf(':');

        if (p == -1) {
            username = s;
            password = "";
        } else {
            username = (p>0) ? s.substring(0, p) : "";
            password = (p == (s.length()-1)) ? "" : s.substring(p+1);
        }

        if (log.isEnabled(Level.TRACE)) {
            log.trace("Username: " + username + "; password: " + password);
        }

        try {
            user = readUser(username);
        } catch (NotFoundException e) {
            if (log.isEnabled(Level.TRACE)) {
                log.trace("User " + username + " not found!");
            }
        } catch (PersistentStoreException e) {
            log.fatal( "Error reading the user "
                      + username
                      + ": "
                      + e.getMessage()
                      );
            log.debug("authenticateBasicCredential", e);

            return false;
        }

        return password.equals(user.getPassword());
    }

    private boolean authenticateMD5Credential(Cred credential) {

        Sync4jDevice   device = null                          ;
        Authentication auth   = credential.getAuthentication();

        try {
            device = readDevice(auth.getDeviceId());
        } catch (PersistentStoreException e) {
            log.fatal("Error configuring the persistent store: " + e.getMessage(),e);
            log.debug("authenticateMD5Credential", e);

            return false;
        }

        //
        // NOTE: the digest read from the store is formatted as
        // b64(H(username:password))
        //
        String userDigest  = device.getDigest();
        byte[] clientNonce = auth.getNextNonce().getValue();

        if (log.isEnabled(Level.TRACE)) {
            log.trace("userDigest: " + userDigest);
        }

        byte[] userDigestBytes = userDigest.getBytes();

        //
        // computation of the MD5 digest
        //
        // Creates a unique buffer containing the bytes to digest
        //
        byte[] buf = new byte[userDigestBytes.length + 1 + clientNonce.length];

        System.arraycopy(userDigestBytes, 0, buf, 0, userDigestBytes.length);
        buf[userDigestBytes.length] = (byte)':';
        System.arraycopy(clientNonce, 0, buf, userDigestBytes.length+1, clientNonce.length);

        byte[] digest = MD5.digest(buf);

        //
        // encoding digest in Base64 for comparation with digest sent by client
        //
        String serverDigestNonceB64 = new String(Base64.encode(digest));

        //
        // digest sent by client in format b64
        //
        String msgDigestNonceB64 = auth.getData();

        if (log.isEnabled(Level.TRACE)) {
            log.trace("serverDigestNonceB64: " + serverDigestNonceB64);
            log.trace("msgDigestNonceB64: "    + msgDigestNonceB64   );
        }

        if (!msgDigestNonceB64.equals(serverDigestNonceB64)) {
            return false;
        }

        auth.setUsername(userDigest);

        return true;

    }

    private boolean authenticateHMACCredential(Cred credential) {
        Sync4jDevice   device = null                          ;
        Authentication auth   = credential.getAuthentication();

        if ( !(auth instanceof HMACAuthentication) ) {
            throw new IllegalStateException("Authentication not valid!");
        }

        try {
            device = readDevice(auth.getDeviceId());
        } catch (PersistentStoreException e) {
            log.fatal("Error configuring the persistent store: " + e.getMessage(),e);
            log.debug("authenticateMD5Credential", e);

            return false;
        }

        String userMac       = ((HMACAuthentication)auth).getUserMac()      ;
        String calculatedMac = ((HMACAuthentication)auth).getCalculatedMac();

        if ((userMac != null) && (userMac.equals(calculatedMac))) {
            auth.setUsername(device.getDigest());
            return true;
        }

        return false;
    }

    /**
     * Reads a device from the persistent store from the device id.
     *
     * @param deviceId the device id
     *
     * @return a Sync4jDevice representing the device
     *
     * @throws PersistentStoreException in case of errors reading from the db
     */
    private Sync4jDevice readDevice(String deviceId)
    throws PersistentStoreException {
        Configuration config = Configuration.getConfiguration();

        PersistentStore ps =
            (PersistentStore)config.getBeanInstance(ConfigurationConstants.CFG_PERSISTENT_STORE);

        Sync4jDevice device = new Sync4jDevice(deviceId, null, null);
        ps.read(device);

        return device;
    }

    /**
     * Reads a user from the database store given his/her usrname
     *
     * @param username the user name
     *
     * @throws NotFoundException if no users are found
     *         PersistentStoreException in case of persistent store errors
     */
    private SyncUser readUser(String username)
    throws NotFoundException, PersistentStoreException {
        Configuration config = Configuration.getConfiguration();

        UserManager um =
            (UserManager)config.getBeanInstance(ConfigurationConstants.CFG_USER_MANAGER);

        SyncUser[] results = um.getUsers(
            new WhereClause("username", new String[] { username }, WhereClause.OPT_EQ, false)
        );

        if ((results == null) || (results.length == 0)) {
            throw new NotFoundException(username);
        }

        return results[0];
    }


}
