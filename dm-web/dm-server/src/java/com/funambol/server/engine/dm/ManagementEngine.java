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
package com.funambol.server.engine.dm;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.funambol.framework.config.Configuration;
import com.funambol.framework.config.ConfigurationConstants;

import com.funambol.framework.core.dm.bootstrap.BasicAccountInfo;
import com.funambol.framework.core.dm.bootstrap.BootStrap;

import com.funambol.framework.engine.dm.DeviceDMState;
import com.funambol.framework.engine.dm.ManagementException;
import com.funambol.framework.notification.NotificationConstants;
import com.funambol.framework.notification.NotificationEngine;
import com.funambol.framework.notification.NotificationException;
import com.funambol.framework.security.Officer;
import com.funambol.framework.security.Sync4jPrincipal;

import com.funambol.framework.server.Sync4jDevice;
import com.funambol.framework.server.SyncUser;
import com.funambol.framework.server.store.Clause;
import com.funambol.framework.server.store.LogicalClause;
import com.funambol.framework.server.store.NotFoundException;
import com.funambol.framework.server.store.PersistentStoreException;
import com.funambol.framework.server.store.WhereClause;
import com.funambol.framework.tools.Base64;
import com.funambol.framework.tools.MD5;
import com.funambol.framework.tools.SecurityTools;

import com.funambol.server.admin.DBUserManager;

import com.funambol.server.engine.Sync4jEngine;

import com.funambol.server.notification.NotificationEngineImpl;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/**
 * This class implements the Management Engine component.
 *
 * @version $Id: ManagementEngine.java,v 1.7 2007-06-19 08:16:24 luigiafassina Exp $
 */
public class ManagementEngine extends Sync4jEngine {

    // --------------------------------------------------------------- Constants

    /**
     * HTTP: 1
     * WSP:  2
     * OBEX: 3
     */
    private static final int DEFAULT_ADDRESS_TYPE = 1;
    private static final int DEFAULT_PORT_NUMBER = 80;

    /**
     * The server bean to use to create a bootstrap object
     */
    public static final String SYNCML_DM_BOOTSTRAP_MESSAGE =
            "com/funambol/server/engine/dm/SyncMLDMbootstrapMessage.xml";

    // Spaceid used in getNextNotificationSessionIds
    private static final String NS_MSSID = "mssid";

    private static final String OPERATION_AFTER_BOOTSTRAP = "GetDeviceDetails";

    // ------------------------------------------------------------ Private Data
    private Configuration config = null;

    private static final Logger log = Logger.getLogger(com.funambol.server.engine.dm.ManagementEngine.class.getName());

    private DBUserManager dbUserManager = null;
    private Officer officer = null;

    // ------------------------------------------------------------- Constructor

    /**
     * Disable default constructor
     */
    private ManagementEngine() {}


    /**
     * Creates a new instance of ManagementEngine given a Configuration object.
     *
     * @param config the configuration object
     */
    public ManagementEngine(Configuration config) {
        super(config);
        this.config = config;

        dbUserManager = (DBUserManager) config.getBeanInstance(CFG_USER_MANAGER);
        officer = (Officer) config.getBeanInstance(CFG_SECURITY_OFFICER);
    }

    /**
     * Performs a bootstrap with the given parameter
     *
     * @param bootstrap the bootstrap object with the neeed information to perfom
     *                  a bootstrap
     * @throws NotificationException if a error occurs
     */
    public void bootstrap(BootStrap bootstrap) throws NotificationException {

        bootstrap(new BootStrap[] {bootstrap});
    }

    /**
     * Performs a bootstrap with the given bootstrap objects
     *
     * @param bootstraps the bootstrap object with the neeed information to perfom
     *                  a bootstrap
     * @throws NotificationException if a error occurs
     */
    public void bootstrap(BootStrap[] bootstraps) throws NotificationException {

        if (bootstraps == null) {
            throw new NotificationException(
                    "Unable to start the bootstrap process. The given BootStrap[] is null"
                    );
        }

        if (log.isEnabledFor(Level.INFO)) {
            log.info("Starting bootstrap process in ManagementEngine for " +
                     bootstraps.length + " devices");
        }

        int numBootStraps = bootstraps.length;

        //
        // Checks if the given bootstrap are valid
        //
        for (int i = 0; i < numBootStraps; i++) {
            if (bootstraps[i] == null) {
                throw new NotificationException(
                        "Error checking the given boostrap objects. The bootstrap n. " +
                        i + " is null"
                        );
            }

            try {
                verifyBootStrap(bootstraps[i]);
            } catch (Exception e) {
                throw new NotificationException(
                        "Error checking the boostrap n. " + i + " (" +
                        e.getMessage() + ")",
                        e
                        );
            }
        }

        BasicAccountInfo[] basicAccountsInfo = new BasicAccountInfo[
                                               numBootStraps];

        for (int i = 0; i < numBootStraps; i++) {
            if (log.isEnabledFor(Level.INFO)) {
                log.info("Start bootstrap process in ManagementEngine with: " +
                         bootstraps[i]);
            }
            basicAccountsInfo[i] = preBootstrapOperation(bootstraps[i]);
        }

        NotificationEngine notificationEngine = new NotificationEngineImpl(
                config);

        notificationEngine.bootstrap(basicAccountsInfo, bootstraps);
    }


    /**
     * Sends a notification message to the device with the given phoneNumber
     *
     * @param messageType the type of the notification message as define in <code>NotificationConstants<code>
     * @param transportType the type of the transport as define in <code>NotificationConstants<code>
     * @param phoneNumber the phone number
     * @param operation application specific management operation
     * @param info application specific info
     *
     * @throws NotificationException if an error occurs
     */
    public void sendNotification(int messageType,
                                 int transportType,
                                 String phoneNumber,
                                 String operation,
                                 String info) throws NotificationException {

        if (log.isEnabledFor(Level.INFO)) {
            log.info("Executing sendNotification with: " +
                     "\n\tmessageType: " + messageType +
                     "\n\ttransportType: " + transportType +
                     "\n\tphoneNumber: " + phoneNumber +
                     "\n\toperation: " + operation +
                     "\n\tinfo: " + info
                    );
        }

        int sessionId = getNextNotificationSessionIds(1)[0];

        String serverId = config.getStringValue(ConfigurationConstants.
                                                CFG_SERVER_ID);

        Sync4jDevice device = getSync4jDevice(phoneNumber);

        if (device == null) {
            throw new NotificationException("Device with phone number '" +
                                            phoneNumber + "' not found");
        }

        String serverPassword = device.getServerPassword();
        byte[] serverNonce = device.getServerNonce();

        NotificationEngine notificationEngine = new NotificationEngineImpl(
                config);

        notificationEngine.sendNotificationMessage(
                messageType, transportType, sessionId, phoneNumber,
                info, serverId, serverPassword, serverNonce
                );

        //
        // After notification, we have to initialize the device managemnent
        // state for this session. We do not have the device id, therefore we
        // set the session id as identifier. This will be matched by the
        // engine in resolveDMState(). See "Funambol Device Management Architecture"
        // for details.
        //
        DeviceDMState dms = new DeviceDMState();

        dms.mssid = String.valueOf(sessionId);
        dms.state = DeviceDMState.STATE_NOTIFIED;
        dms.start = new Date(System.currentTimeMillis());
        dms.operation = operation;
        dms.info = info;
        dms.deviceId = device.getDeviceId();

        storeDeviceDMState(dms);
    }

    /**
     * Sends a notification message to the devices with the given phoneNumbers
     *
     * @param messageType the type of the notification message as define in <code>NotificationConstants<code>
     * @param transportType the type of the transport as define in <code>NotificationConstants<code>
     * @param phoneNumbers the phone numbers list
     * @param operation application specific management operation
     * @param info application specific info
     *
     * @throws NotificationException if an error occurs
     */
    public void sendNotification(int messageType,
                                 int transportType,
                                 String[] phoneNumbers,
                                 String operation,
                                 String info) throws NotificationException {

        if (log.isEnabledFor(Level.INFO)) {
            log.info("Executing sendNotification to " + phoneNumbers.length +
                     " devices with: " +
                     "\n\tmessageType: " + messageType +
                     "\n\ttransportType: " + transportType +
                     "\n\toperation: " + operation +
                     "\n\tinfo: " + info
                    );
        }

        int numDevices = phoneNumbers.length;
        int[] sessionIds = getNextNotificationSessionIds(numDevices);

        String serverId = config.getStringValue(ConfigurationConstants.
                                                CFG_SERVER_ID);

        Sync4jDevice device = null;
        DeviceDMState dms = null;

        String[] serverPasswords = new String[numDevices];
        byte[][] serverNonces = new byte[numDevices][];
        for (int i = 0; i < numDevices; i++) {

            device = getSync4jDevice(phoneNumbers[i]);

            if (device == null) {
                throw new NotificationException("Device with phone number '" +
                                                phoneNumbers[i] + "' not found");
            }

            serverPasswords[i] = device.getServerPassword();
            serverNonces[i] = device.getServerNonce();

            //
            // Before notification, we have to initialize the device managemnent
            // state for this session. We do not have the device id, therefore we
            // set the session id as identifier. This will be matched by the
            // engine in resolveDMState().
            //
            dms = new DeviceDMState();

            dms.mssid = String.valueOf(sessionIds[i]);
            dms.state = DeviceDMState.STATE_NOTIFIED;
            dms.start = new Date(System.currentTimeMillis());
            dms.operation = operation;
            dms.info = info + ":" + phoneNumbers[i];
            dms.deviceId = device.getDeviceId();
            storeDeviceDMState(dms);
        }

        NotificationEngine notificationEngine = new NotificationEngineImpl(
                config);
        notificationEngine.sendNotificationMessages(messageType,
                transportType,
                sessionIds,
                phoneNumbers,
                info,
                serverId,
                serverPasswords,
                serverNonces);
    }


    /**
     * Stores the given DeviceDMState.
     *
     * @param d the DeviceDMState
     *
     */
    public void storeDeviceDMState(DeviceDMState d) {
        try {
            getStore().store(d);
        } catch (PersistentStoreException e) {
            if (log.isEnabledFor(Level.FATAL)) {
                log.fatal("Error storing the device DM state "
                           + d.toString()
                           + ": "
                           + e.getMessage()
                        );
            }

            log.debug("storeDeviceDMState", e);

        }
    }

    /**
     * Reads the given DeviceDMState.
     *
     * @param d the DeviceDMState
     *
     * @throws NotFoundException if the DeviceDMState is not found
     */
    public void readDeviceDMState(DeviceDMState d) throws NotFoundException {
        WhereClause where = new WhereClause(
                DeviceDMState.PROPERTY_DEVICE_ID,
                new String[] {d.deviceId},
                WhereClause.OPT_EQ,
                true
                            );

        try {
            DeviceDMState[] rows = (DeviceDMState[]) getStore().read(d, where);

            if (rows.length == 0) {
                throw new NotFoundException(
                        "No device DM state found with deviceId " + d.deviceId);
            }

            d.copyFrom(rows[0]);
        } catch (NotFoundException e) {
            throw e;
        } catch (PersistentStoreException e) {
            if (log.isEnabledFor(Level.FATAL)) {
                log.fatal("Error reading the device DM state "
                           + d.toString()
                           + ": "
                           + e.getMessage()
                        );
            }

            log.debug("readDeviceDMState", e);

        }
    }

    /**
     * Deletes the given DeviceDMState.
     *
     * @param d the DeviceDMState
     *
     */
    public void deleteDeviceDMState(DeviceDMState d) {
        try {
            getStore().delete(d);
        } catch (PersistentStoreException e) {
            if (log.isEnabledFor(Level.FATAL)) {
                log.fatal("Error deleting the device DM state "
                           + d.toString()
                           + ": "
                           + e.getMessage()
                        );
            }

            log.debug("deleteDeviceDMState", e);

        }
    }

    /**
     * This method tries to associate the given DeviceDMState instance to an
     * existing device management state. The algorithm is as follows:
     * <ul>
     *   <li>look for a <i>notified</i> device with session id equal to dms.mssid</li>
     *   <li>IF a device management session is found THEN fill dms with the
     *       retrieved values and return true</li>
     *   <li>ELSE look for any other state whose associated device is equal to dms.deviceId</li>
     *   <li>IF a device management session is found THEN fill dms with the
     *       retrieved values and return true.
     *   <li>return false in any other case
     * </ul>
     *
     * @param dms the DM session state
     * @param deviceConnected the device connected
     * @return true if the device management session was resolved, false otherwise.
     */
    public boolean resolveDMState(DeviceDMState dms,
                                  Sync4jDevice deviceConnected) {
        //
        // 1. Search for notified devices first
        //
        WhereClause wc1 = new WhereClause(
                DeviceDMState.PROPERTY_STATE,
                new String[] {String.valueOf((char) DeviceDMState.
                                             STATE_NOTIFIED)},
                WhereClause.OPT_EQ,
                true
                          );

        WhereClause wc2 = new WhereClause(
                DeviceDMState.PROPERTY_SESSION_ID,
                new String[] {dms.mssid},
                WhereClause.OPT_EQ,
                true
                          );

        LogicalClause select = new LogicalClause(
                LogicalClause.OPT_AND,
                new Clause[] {wc1, wc2}
                               );

        dms.state = DeviceDMState.STATE_NOTIFIED;

        try {
            DeviceDMState[] rows = (DeviceDMState[]) getStore().read(dms,
                    select);

            if (rows.length > 0) {
                //
                // The device id is null in the database, therefore we have to
                // overwrite it after copy
                //
                String deviceConnectedId = dms.deviceId;
                dms.copyFrom(rows[0]);

                if (!dms.deviceId.equalsIgnoreCase(deviceConnectedId)) {
                    //
                    // The device connected is different from the device notified
                    // So, we swap the device id (SIM Swap)
                    //
                    Sync4jDevice deviceNotified = new Sync4jDevice(dms.deviceId);

                    store.read(deviceNotified);
                    store.read(deviceConnected);

                    String descriptionDeviceNotified = deviceNotified.
                            getDescription();
                    String descriptionDeviceConnected = deviceConnected.
                            getDescription();

                    deviceNotified.setDescription(descriptionDeviceConnected);
                    deviceConnected.setDescription(descriptionDeviceNotified);

                    store.store(deviceNotified);
                    store.store(deviceConnected);
                }
                dms.deviceId = deviceConnectedId;

                return true;
            }
        } catch (PersistentStoreException e) {
            if (log.isEnabledFor(Level.FATAL)) {
                log.fatal("Error reading the device DM state "
                           + dms.toString()
                           + ": "
                           + e.getMessage()
                        );
            }

            log.debug("resolveDMState", e);

            return false;
        }

        //
        // 2. Search for any other association
        //
        wc1 = new WhereClause(
                DeviceDMState.PROPERTY_STATE,
                new String[] {String.valueOf((char) DeviceDMState.
                                             STATE_IN_PROGRESS)},
                WhereClause.OPT_EQ,
                true
              );

        wc2 = new WhereClause(
                DeviceDMState.PROPERTY_DEVICE_ID,
                new String[] {dms.deviceId},
                WhereClause.OPT_EQ,
                true
              );

        select = new LogicalClause(
                LogicalClause.OPT_AND,
                new Clause[] {wc1, wc2}
                 );

        try {
            DeviceDMState[] rows = (DeviceDMState[]) getStore().read(dms,
                    select);

            if (rows.length > 0) {
                dms.copyFrom(rows[0]);

                return true;
            }
        } catch (PersistentStoreException e) {
            if (log.isEnabledFor(Level.FATAL)) {
                log.fatal("Error reading the device DM state "
                           + dms.toString()
                           + ": "
                           + e.getMessage()
                        );
            }

            log.debug("resolveDMState", e);

        }

        return false;
    }

    // --------------------------------------------------------- Private Methods

    /**
     * Create the sync user, sync4j device and sync4j principal with the given data.
     * The username of the user is calculate as:<p/>b64(md5(user':'password))
     *
     * @param deviceId String
     * @param username String
     * @param password String
     * @param phoneNumber String
     * @throws PersistentStoreException
     * @return Sync4jDevice
     */
    private Sync4jDevice createSync4jUserDevicePrincipal(String deviceId,
            String username,
            String password,
            String phoneNumber) throws PersistentStoreException {

        // b64(md5(user':'password))
        String userDigest = calculateDigest(username, password);

        Sync4jDevice device = new Sync4jDevice(deviceId);
        boolean deviceFound = false;

        try {
            readDevice(device);
            deviceFound = true;
        } catch (NotFoundException ex) {
            deviceFound = false;
        }

        if (!deviceFound) {
            //
            // Checks if there is already a device with this phone number
            //
            Sync4jDevice[] devices = getSync4jDevices(phoneNumber);
            if (devices.length > 0) {
                //
                // We are in this case:
                //
                // 1) Bootstrap device A, with SIM 1
                // 2) Put SIM 1 in another non-Bootstrapped device B.
                // 3) try to Bootstrap device B, SIM 1
                //
                // For all devices with this phone number,
                // sets the phone number to empty string (so only one device
                // has this phone number)
                //
                for (int i = 0; i < devices.length; i++) {
                    getStore().read(devices[i]);
                    devices[i].setDescription("");
                    getStore().store(devices[i]);
                }
            }

            //
            // create and store new user
            //
            SyncUser user = new SyncUser();
            user.setUsername(userDigest);
            user.setPassword(" ");
            user.setFirstname("");
            user.setLastname(phoneNumber);
            String[] role = {"sync_user"};
            user.setRoles(role);
            dbUserManager.insertUser(user);
            //
            // create and store new device
            //
            device = new Sync4jDevice();
            device.setDeviceId(deviceId);
            device.setType("phone");
            device.setDescription(phoneNumber);
            device.setDigest(userDigest);

            byte[] serverNonce = MD5.getNextNonce();
            byte[] clientNonce = MD5.getNextNonce();
            device.setServerNonce(serverNonce);
            device.setClientNonce(clientNonce);

            String serverPassword = SecurityTools.getRandomPassword();
            device.setServerPassword(serverPassword);

            storeDevice(device);
            //
            // create and store new principal
            Sync4jPrincipal principal = new Sync4jPrincipal(userDigest,
                    deviceId);
            getStore().store(principal);
            return device;
        }

        //
        // Device found
        //

        //
        // Checks if there is already a device with this phone number
        //
        Sync4jDevice[] devices = getSync4jDevices(phoneNumber);
        if (devices.length == 1) {
            //
            // We are in this case:
            // 1) Bootstrap device A, with SIM 1
            // 2) Bootstrap device B, with SIM 2
            // 3) Put SIM 1 in device B
            // 4) Try to re-Bootstrap device B, SIM 1
            //
            // Swap the phone number
            // (so there isn't a phone number associated a more devices)
            // device A <--> SIM 2
            // device B <--> SIM 1
            //
            Sync4jDevice deviceA = null;
            Sync4jDevice deviceB = device;

            getStore().read(devices[0]);

            deviceA = devices[0];

            // Sets the phone number of the SIM 2 (device.getDescription())
            // (device == device B)
            deviceA.setDescription(deviceB.getDescription());
            getStore().store(deviceA);

        } else if (devices.length > 1) {
            //
            // This condition indicates a error because not more that one device
            // can have the same phone number
            //
            throw new IllegalStateException(
                    "There are more devices with phone number '" +
                    phoneNumber + "' then the swap of this is not possible");
        }

        byte[] serverNonce = MD5.getNextNonce();
        byte[] clientNonce = MD5.getNextNonce();
        device.setServerNonce(serverNonce);
        device.setClientNonce(clientNonce);

        String serverPassword = SecurityTools.getRandomPassword();
        device.setServerPassword(serverPassword);

        device.setDescription(phoneNumber);
        storeDevice(device);

        String deviceDigest = device.getDigest();

        //
        // Checks if the device has been bootstrapped with other username and password (digest)
        //
        if (StringUtils.equals(deviceDigest, userDigest)) {
            return device;
        }

        //
        // The device has been bootstrapped with other username and password (digest).
        // Then:
        // 1. update the device digest (and store the device)
        // 2. create new user with the new digest
        // 3. create new principal with the new user and the current device
        // 4. delete the previous principal
        // 5. delete the previous user (if not principal is associated to)
        //

        //
        // 1. Update the digest of the device with new digest
        //
        device.setDigest(userDigest);
        storeDevice(device);

        //
        // 2. Create new user with the new digest
        //
        SyncUser user = new SyncUser();
        user.setUsername(userDigest);
        user.setPassword(" ");
        user.setFirstname("");
        user.setLastname(phoneNumber);
        String[] role = {"sync_user"};
        user.setRoles(role);
        dbUserManager.insertUser(user);

        //
        // 3. Create new principal with the new user and the current device
        //    (the store method checks before if there is a principal with the same info)
        //
        Sync4jPrincipal principal = new Sync4jPrincipal(userDigest, deviceId);
        getStore().store(principal);

        //
        // 4. Delete the previous principal
        //
        Sync4jPrincipal previousPrincipal = new Sync4jPrincipal(deviceDigest,
                deviceId);
        getStore().read(previousPrincipal);
        getStore().delete(previousPrincipal);

        //
        // Checks if the previous user is associated to other principal
        //
        WhereClause wc1 = new WhereClause(
                Sync4jPrincipal.PROPERTY_USERNAME,
                new String[] {deviceDigest},
                WhereClause.OPT_EQ,
                true
                          );

        Sync4jPrincipal[] principals = (Sync4jPrincipal[]) (getStore().read(
                principal, wc1));

        if (principals.length == 0) {
            //
            // 5. Delete the previous user (no principal associated)
            //
            SyncUser previousUser = new SyncUser();
            previousUser.setUsername(deviceDigest);
            dbUserManager.deleteUser(previousUser);
        }
        return device;
    }

    /**
     * Returns a digest for the given username and password.<br/>
     * The digest is calculated as b64(md5(user':'password))
     *
     * @param username String
     * @param password String
     * @return String the digest
     */
    private String calculateDigest(String username, String password) {
        String cred = username + ":" + password;

        byte[] md5 = null;

        md5 = MD5.digest(cred.getBytes()); // md5(user:password)

        byte[] digest = Base64.encode(md5);

        return new String(digest);
    }

    /**
     * Reads the device with the given phoneNumber
     * @param phoneNumber String
     * @throws NotificationException if there are more devices with the same phoneNumber
     * @return Sync4jDevice the device with the given phoneNumber
     */
    private Sync4jDevice getSync4jDevice(String phoneNumber) throws
            NotificationException {

        // search device with description=phoneNumber (case insensitive)
        Clause clause = new WhereClause(Sync4jDevice.PROPERTY_DESCRIPTION,
                                        new String[] {phoneNumber},
                                        WhereClause.OPT_EQ,
                                        true);

        Sync4jDevice[] devices = null;

        try {
            // returns a Sync4jDevice[] with only id,description and type
            devices = (Sync4jDevice[]) getStore().read(new Sync4jDevice(),
                    clause);

        } catch (PersistentStoreException e) {
            throw new NotificationException("Unable to get device information",
                                            e);
        }

        if (devices == null) {
            return null;
        } else if (devices.length == 0) {
            throw new NotificationException("Device with phone number '" +
                                            phoneNumber + "' not found");
        } else if (devices.length > 1) {
            throw new NotificationException(
                    "There are more devices with the same phone number [" +
                    phoneNumber + "]");
        }

        try {
            // get all device info
            readDevice(devices[0]);

        } catch (PersistentStoreException e) {
            throw new NotificationException("Unable to get device information",
                                            e);
        }

        return devices[0];
    }

    /**
     * Reads the devices with the given phoneNumber
     * @param phoneNumber String
     * @return Sync4jDevice[] the devices with the given phoneNumber
     */
    private Sync4jDevice[] getSync4jDevices(String phoneNumber) throws
            PersistentStoreException {

        // search device with description=phoneNumber (case insensitive)
        Clause clause = new WhereClause(Sync4jDevice.PROPERTY_DESCRIPTION,
                                        new String[] {phoneNumber},
                                        WhereClause.OPT_EQ,
                                        true);

        Sync4jDevice[] devices = null;

        // returns a Sync4jDevice[] with only id,description and type
        devices = (Sync4jDevice[]) getStore().read(new Sync4jDevice(), clause);

        return devices;
    }

    /**
     * Performs the operations necessary to bootstrap a device:
     * <ui>
     * <li>Creates a new device with the given parameters
     * <li>Creates and returns the <code>BasicAccountInfo</code> with the
     *     basic account info
     * </ui>
     * @throws NotificationException if an error occurs
     * @return BasicAccountInfo
     */
    private BasicAccountInfo preBootstrapOperation(BootStrap bootstrap) throws
            NotificationException {

        //
        // creates user, device, principal
        //
        Sync4jDevice device = null;

        try {
            device = createSync4jUserDevicePrincipal(bootstrap.getDeviceId(),
                    bootstrap.getUsername(),
                    bootstrap.getPassword(),
                    bootstrap.getMsisdn());

        } catch (PersistentStoreException e) {
            log.debug("preBootstrapOperation", e);

            throw new NotificationException(
                    "Error creating the user/device/principal", e);
        }

        try {
            createDMStateToExecuteAfterBootstrap(bootstrap.getDeviceId(),
                                                 bootstrap.getOperation(),
                                                 bootstrap.getInfo());
        } catch (ManagementException e) {
            log.debug("preBootstrapOperation", e);

            throw new NotificationException("Error creating the dmstate", e);
        }

        byte[] serverNonce = device.getServerNonce();
        byte[] clientNonce = device.getClientNonce();

        String serverUri = config.getStringValue(ConfigurationConstants.
                                                 CFG_SERVER_URI);

        StringBuffer serverAddress = new StringBuffer();
        int serverPort = -1;

        URI uri = null;
        try {
            uri = new URI(serverUri);
            serverPort = uri.getPort();
            serverAddress.append(uri.getScheme()).append("://");
            serverAddress.append(uri.getHost()).append(uri.getPath());
            if (uri.getQuery() != null && uri.getQuery().length() != 0) {
                serverAddress.append('?').append(uri.getQuery());
            }
        } catch (URISyntaxException e) {
            throw new NotificationException("The server uri isn't a valid uri",
                                            e);
        }

        String serverId = config.getStringValue(ConfigurationConstants.
                                                CFG_SERVER_ID);
        String serverPassword = device.getServerPassword();

        BasicAccountInfo basicAccountInfo =
                new BasicAccountInfo(bootstrap.getDeviceId(),
                                     bootstrap.getAccountName(),
                                     serverAddress.toString(),
                                     serverPort,
                                     serverId,
                                     serverPassword,
                                     serverNonce,
                                     bootstrap.getUsername(),
                                     bootstrap.getPassword(),
                                     clientNonce,
                                     officer.getAuthType(),
                                     DEFAULT_ADDRESS_TYPE
                );

        return basicAccountInfo;
    }


    /**
     * Returns an array of the next session identifiers to use in notification message
     *
     * @throws NotificationException if error occurs reading the counter from
     *                               the persistent store
     * @return int[]
     */
    private synchronized int[] getNextNotificationSessionIds(int
            numOfSessionsId) throws NotificationException {

        int counter = 0;
        try {
            counter = getStore().readCounter(NS_MSSID, numOfSessionsId);
        } catch (PersistentStoreException ex) {
            throw new NotificationException("Error reading session ids");
        }

        int[] sessionIds = new int[numOfSessionsId];
        for (int i = 0; i < numOfSessionsId; i++) {
            sessionIds[i] = (counter + i) & 0xffff; // 16 bit integer
        }
        return sessionIds;
    }

    /**
     * Verify if the given bootstrap is correct (from the semantic point of view)
     * @param bootstrap BootStrap
     * @throws NotificationException if the bootstrap is not correct
     */
    private void verifyBootStrap(BootStrap bootstrap) throws
            NotificationException {

        if (bootstrap == null) {
            throw new NotificationException(
                    "Unable to send the bootstrap (is null)");
        }

        String imsi = bootstrap.getImsi();
        String userPin = bootstrap.getUserPin();
        int authMethod = bootstrap.getAuthMethod();
        String digest = bootstrap.getDigest();

        if (StringUtils.isNotEmpty(digest)) {
            return;
        }

        if (authMethod == NotificationConstants.AUTH_METHOD_NETWPIN) {
            if (StringUtils.isEmpty(imsi)) {
                throw new NotificationException(
                        "Imsi can't be null using NETWPIN authentication method");
            }
        } else if (authMethod == NotificationConstants.AUTH_METHOD_USERPIN) {
            if (StringUtils.isEmpty(userPin) && StringUtils.isEmpty(imsi)) {
                throw new NotificationException(
                        "Userpin or imsi must be not null using USERPIN authentication method");
            }
        } else if (authMethod == NotificationConstants.AUTH_METHOD_USERNETWPIN) {
            if (StringUtils.isEmpty(imsi)) {
                throw new NotificationException(
                        "Imsi can't be null using USERNETWPIN authentication method");
            }

            if (StringUtils.isEmpty(userPin)) {
                throw new NotificationException(
                        "Userpin can't be null using USERNETWPIN authentication method");
            }
        }
    }

    /**
     * Creates a new DMState with the operation to execute after a bootstrap
     * @param deviceId the device id
     * @param operation the operation to set in the dm state. If null (or empty)
     *                  <code>OPERATION_AFTER_BOOTSTRAP</code> is used.
     * @param info the application specific info
     */
    private void createDMStateToExecuteAfterBootstrap(String deviceId,
                                                      String operation,
                                                      String info)
    throws ManagementException {

        deleteAllPendingSessions(deviceId);

        DeviceDMState dmState = new DeviceDMState();
        dmState.deviceId = deviceId;
        dmState.state = DeviceDMState.STATE_IN_PROGRESS;
        if (StringUtils.isNotEmpty(operation)) {
            dmState.operation = operation;
        } else {
            dmState.operation = OPERATION_AFTER_BOOTSTRAP;

        }
        dmState.info = info;
        try {
            store.store(dmState);
        } catch (PersistentStoreException e) {
           if (log.isEnabledFor(Level.FATAL)) {
                log.fatal("Error storing the dm state to execute after the bootstrap: "
                           + e.getMessage()
                        );
            }

            log.debug("createDMStateToExecuteAfterBootstrap", e);

            throw new ManagementException("Error storing the dm state to execute after the bootstrap", e);
        }

    }

    /**
     * Deletes all pending sessions for the given deviceId
     * @param deviceId String
     * @throws ManagementException if an error occurs
     */
    private void deleteAllPendingSessions(String deviceId)
    throws ManagementException {

        //
        // 1. delete all dmstate with state 'P' for the given deviceId
        //
        WhereClause wc1 = new WhereClause(
                DeviceDMState.PROPERTY_STATE,
                new String[] {String.valueOf((char) DeviceDMState.
                                             STATE_IN_PROGRESS)},
                WhereClause.OPT_EQ,
                true
                          );

        WhereClause wc2 = new WhereClause(
                DeviceDMState.PROPERTY_DEVICE_ID,
                new String[] {deviceId},
                WhereClause.OPT_EQ,
                true
                          );

        LogicalClause select = new LogicalClause(
                LogicalClause.OPT_AND,
                new Clause[] {wc1, wc2}
                               );

        try {
            DeviceDMState[] rows = (DeviceDMState[]) getStore().read(new
                    DeviceDMState(), select);

            for (int i = 0; i < rows.length; i++) {
                deleteDeviceDMState(rows[i]);
            }

        } catch (PersistentStoreException e) {
            if (log.isEnabledFor(Level.FATAL)) {
                log.fatal("Error deleting the pending session for '" +
                           deviceId +
                           "': "
                           + e.getMessage()
                        );
            }

            log.debug("deleteAllPendingSessions", e);

            throw new ManagementException("Error deleting pending sessions", e);
        }
    }
}

