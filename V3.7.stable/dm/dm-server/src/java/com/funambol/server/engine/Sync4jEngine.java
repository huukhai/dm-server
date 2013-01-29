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

package com.funambol.server.engine;


import java.security.Principal;
import java.util.HashMap;

import com.funambol.framework.config.Configuration;
import com.funambol.framework.config.ConfigurationConstants;
import com.funambol.framework.core.Authentication;
import com.funambol.framework.core.Chal;
import com.funambol.framework.core.Constants;
import com.funambol.framework.core.Cred;
import com.funambol.framework.core.Meta;
import com.funambol.framework.security.Officer;
import com.funambol.framework.security.Sync4jPrincipal;
import com.funambol.framework.server.Sync4jDevice;
import com.funambol.framework.server.store.ConfigPersistentStoreException;
import com.funambol.framework.server.store.PersistentStore;
import com.funambol.framework.server.store.PersistentStoreException;
import com.funambol.framework.tools.Base64;
import com.funambol.framework.tools.CommandIdGenerator;
import com.funambol.framework.tools.MD5;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;


/**
 * This is the Funambol implementation of the synchronization engine.
 *
 * LOG NAME: funambol.engine
 *
 * @version $Id: Sync4jEngine.java,v 1.2 2006/08/07 21:09:24 nichele Exp $
 */
public class Sync4jEngine
implements java.io.Serializable, ConfigurationConstants {

    // --------------------------------------------------------------- Constants

    public static final String LOG_NAME = "engine";

    // ---------------------------------------------------------- Protected data

    /**
     * The configuration properties
     */
    protected Configuration configuration = null;

    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * The security officer
     */
    protected Officer officer = null;

    public Officer getOfficer() {
        return this.officer;
    }

    /**
     * The underlying persistent store
     */
    protected PersistentStore store = null;

    /** Getter for property store.
     * @return Value of property store.
     *
     */
    public PersistentStore getStore() {
        return store;
    }

    /** Setter for property store.
     * @param store New value of property store.
     *
     */
    public void setStore(PersistentStore store) {
        this.store = store;
    }

    /**
     * Logger
     */
    protected transient static final Logger log = Logger.getLogger(com.funambol.server.engine.Sync4jEngine.class.getName());
    /**
     * Sets the SyncML Protocol version. It is used to create the server
     * credential with MD5 authentication.
     */
    private String syncMLVerProto = null;
    public void setSyncMLVerProto(String syncMLVerProto) {
        this.syncMLVerProto = syncMLVerProto;
    }

    // ------------------------------------------------------------ Constructors

    /**
     * To allow deserializazion of subclasses.
     */
    protected  Sync4jEngine() {
    }

    /**
     * Creates a new instance of Sync4jEngine. <p>
     * NOTE: This is a sample implementation that deals with a single source on
     * the file system.
     *
     * @throws ConfigurationException a runtime exception in case of misconfiguration
     */
    public Sync4jEngine(Configuration configuration) {

        this.configuration = configuration;

        //
        // Set the underlying persistent store
        //
        HashMap<String, Object> psConfig = new HashMap<String, Object>(1);
        psConfig.put("class-loader", configuration.getClassLoader());
        store = (PersistentStore)configuration.getBeanInstance(CFG_PERSISTENT_STORE);
        try {
            store.configure(psConfig);
        } catch (ConfigPersistentStoreException e) {
            if(log.isEnabled(Level.FATAL)){
                log.fatal("Error configuring the persistent store: " + e);
            }
            log.debug("<init>", e);
        }

        //
        // Set the security officer
        //
        officer = (Officer)configuration.getBeanInstance(CFG_SECURITY_OFFICER);
        cmdIdGenerator = new CommandIdGenerator();
    }

    // ------------------------------------------------ Configuration properties

     /**
     * The id generator
     */
    protected CommandIdGenerator cmdIdGenerator = null;

    /**
     * Set the id generator for commands
     *
     * @param cmdIdGenerator the id generator
     */
    public void setCommandIdGenerator(CommandIdGenerator cmdIdGenerator) {
        this.cmdIdGenerator = cmdIdGenerator;
    }


    /**
     * Return the id generator
     *
     * @return the id generator
     */
    public CommandIdGenerator getCommandIdGenerator() {
        return cmdIdGenerator;
    }

    // ------------------------------------------------------ Runtime properties



    /**
     * Authenticates the given credential using <i>officer</i>.
     *
     * @param credential the credential to be authenticated
     *
     * @return true if the credential is authenticated, false otherwise
     */
    public boolean login(Cred credential)  {
        return officer.authenticate(credential);
    }

    public boolean isGuestEnabled() {
        return officer.isGuestEnabled();
    }

    /**
     * Logs out the given credential using <i>officer</i>.
     *
     * @param credential the credential to be authenticated
     *
     * @return true if the credential is authenticated, false otherwise
     */
    public void logout(Cred credential) {
        officer.unAuthenticate(credential);
    }

    /**
     * Authorizes the given principal to access the given resource using
     * <i>officer</i>.
     *
     * @param principal the entity to be authorized
     * @param resource the name of the resource
     *
     * @return true if the principal is authorized, false otherwise
     */
    public boolean authorize(Principal principal, String resource) {
        return officer.authorize(principal, resource);
    }

    /**
     * Is the server in debug mode?
     *
     * @return the value of the configuration property CFG_SERVER_DEBUG
     */
    public boolean isDebug() {
        return configuration.getBoolValue(CFG_SERVER_DEBUG, false);
    }

    /**
     * Returns the server credentials in the format specified by the given Chal.
     *
     * @param chal the client Chal
     * @param device the device
     *
     * @return the server credentials. <code>null</code> if the given chal is null or
     *         if the server id is null.
     */
    public Cred getServerCredentials(Chal chal, Sync4jDevice device) {
        String login = configuration.getStringValue(CFG_SERVER_ID);

        if (login == null || login.equals("")) {
            return null;
        }

        if (chal == null) {
          return null;
        }

        String pwd    = device.getServerPassword();

        String type   = chal.getType();
        String data   = login + ':' + pwd;

        if (Constants.AUTH_TYPE_BASIC.equals(type)) {
            data = new String(Base64.encode(data.getBytes()));
        } else if (Constants.AUTH_TYPE_CLEAR.equals(type)) {
            data = login + ':' + pwd;
        } else if (Constants.AUTH_TYPE_MD5.equals(type)) {

            if (this.syncMLVerProto.indexOf("1.0") != -1) {
                //
                //Steps to follow:
                //1) decode nonce sent by client
                //2) b64(H(username:password:nonce))
                //
                byte[] serverNonce = chal.getNextNonce().getValue();
                String serverNonceDecode = null;
                if ((serverNonce == null) || (serverNonce.length == 0)) {
                    serverNonceDecode = "";
                } else {
                    serverNonceDecode = Constants.FORMAT_B64.equals(chal.getFormat())
                                      ? new String(Base64.decode(serverNonce))
                                      : new String(serverNonce)
                                      ;
                }
                String withNonce = new String(data.getBytes()) + ':' + serverNonceDecode;
                byte[] digestNonce = MD5.digest(withNonce.getBytes());
                data =  new String(Base64.encode(digestNonce));

            } else {
                //
                //Steps to follow:
                //1) H(username:password)
                //2) b64(H(username:password))
                //3) decode nonce sent by client
                //4) b64(H(username:password)):nonce
                //5) H(b64(H(username:password)):nonce)
                //6) b64(H(b64(H(username:password)):nonce))
                //

               byte[] serverNonce = chal.getNextNonce().getValue();
               byte[] serverNonceDecode = null;
               if ( (serverNonce == null) || (serverNonce.length == 0)) {
                   serverNonceDecode = new byte[0];
               } else {
                   serverNonceDecode = Constants.FORMAT_B64.equals(chal.getFormat())
                                        ? Base64.decode(serverNonce)
                                        : serverNonce
                                        ;
               }

               byte[] digest = MD5.digest(data.getBytes()); // data = username:password
               byte[] b64    = Base64.encode(digest);

               byte[] buf = new byte[b64.length + 1 + serverNonceDecode.length];

               System.arraycopy(b64, 0, buf, 0, b64.length);
               buf[b64.length] = (byte)':';
               System.arraycopy(serverNonceDecode, 0, buf, b64.length+1, serverNonceDecode.length);

               data = new String(Base64.encode(MD5.digest(buf)));

            }
        } else if (Constants.AUTH_TYPE_HMAC.equals(type)) {
            /**
             * Not required because the credential HMAC
             * is management and calculated from SyncAdapter using
             * data in SyncResponse (the credential HMAC is removed from the message
             * in SyncAdapter)
             */
        }

        Meta m = new Meta();
        m.setType(type);
        m.setNextNonce(null);
        return new Cred(new Authentication(m, data));
    }

    /**
     * Read the given principal from the store. The principal must be
     * already configured with username and device. The current implementation
     * reads the following additional informatioin:
     * <ul>
     *  <li>principal id
     * </ul>
     *
     * @param principal the principal to be read
     *
     * @throws com.funambol.framework.server.store.PersistentStoreException
     */
    public void readPrincipal(Sync4jPrincipal principal)
    throws PersistentStoreException {
        assert(principal               != null);
        assert(principal.getUsername() != null);
        assert(principal.getDeviceId() != null);

        getStore().read(principal);
    }

    /**
     * Read the Sync4jDevice identified by the given id from the store.
     *
     * @param deviceId the device of which reading the information
     *
     * @throws com.funambol.framework.server.store.PersistentStoreException
     */
    public void readDevice(Sync4jDevice device)
    throws PersistentStoreException {
        assert(device               != null);
        assert(device.getDeviceId() != null);

        getStore().read(device);
    }

    /**
     * Stores the given Sync4jDevice.
     *
     * @param device the device
     *
     */
    public void storeDevice(Sync4jDevice device) {
        try{
            getStore().store(device);
        } catch (PersistentStoreException e) {
            if (log.isEnabled(Level.FATAL)) {
                log.fatal("Error storing the device: " + e.getMessage());
            }

            log.debug("storeDevice", e);
        }
    }
}
