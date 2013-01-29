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

package com.funambol.dmdemo;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.funambol.framework.core.AlertCode;
import com.funambol.framework.engine.dm.ManagementException;
import com.funambol.framework.tools.DBTools;
import com.funambol.framework.engine.dm.UserAlertManagementOperation;
import com.funambol.framework.engine.dm.ManagementOperation;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;


/**
 * It's the bean used in jsp to keep the session information and to performe the business
 * operation.
 *
 * @version $Id: DMDemoBean.java,v 1.5 2007-06-18 16:38:44 luigiafassina Exp $
 */
public class DMDemoBean {

    // --------------------------------------------------------------- Constants
    public static final String PROCESSOR_GENERIC            = "GenericProcessor";
    public static final String PROCESSOR_TREE_DISCOVERY     = "TreeDiscoveryProcessor";
    public static final String PROCESSOR_GET_DEVICE_DETAILS = "GetDeviceDetails";

    private static final String JNDI_NAME = "java:jboss/datasources/FunambolDMDS";

    private static final String SQL_INSERT_DM_STATE =
        "INSERT INTO fnbl_dm_state (id,device,mssid,state,operation) values (?,?,?,'P',?)";

    private static final String SQL_UPDATE_DM_STATE =
        "UPDATE fnbl_dm_state SET device=?, operation=? WHERE id =?";

    private static final String SQL_SELECT_DM_SESSIONS_LIST =
        "SELECT id, device, state, operation, start_ts, end_ts FROM fnbl_dm_state ";

    private static final String SQL_SELECT_DM_STATE =
        "SELECT id, device, state, operation, start_ts, end_ts FROM fnbl_dm_state WHERE mssid=?";

    private static final String SQL_DELETE_DM_STATE =
        "DELETE FROM fnbl_dm_state WHERE id=?";

    private static final String MESSAGE_DUMMY_ALERT_OPERATION = "MESSAGE_DUMMY_ALERT_OPERATION";

    private static final ResourceBundle resourceBundle =
        ResourceBundle.getBundle("DMDemo", new Locale("en", "EN"));

    public static final UserAlertManagementOperation DUMMY_ALERT_OPERATION =
        UserAlertManagementOperation.getDisplay(resourceBundle.getString(MESSAGE_DUMMY_ALERT_OPERATION), 5, 15);


    // ------------------------------------------------------------ Private data

    private static final Logger log = Logger.getLogger(com.funambol.dmdemo.DMDemoBean.class.getName());

    private DataSource datasource = null;

    private String sessionId = null;
    private String state     = null;
    private String deviceId  = null;
    private String operation = null;

    // The password used during login
    private String password      = null;
    private boolean userIsLogged = false;

    public GenericProcessorTools genericProcessorTools = null;
    public TreeDiscoveryProcessorTools treeDiscoveryProcessorTools = null;
    public RegistrationTools registrationTools = null;

    // ------------------------------------------------------------ Constructors
    public DMDemoBean() {
        if (log.isEnabled(Level.TRACE)) {
            log.trace("New DMDemoBean");
        }
        datasource                  = lookupDataSource();
        genericProcessorTools       = new GenericProcessorTools(datasource);
        treeDiscoveryProcessorTools = new TreeDiscoveryProcessorTools(datasource);
        registrationTools           = new RegistrationTools(datasource);
    }

    // ---------------------------------------------------------- Public Methods


    /**
     * Checks if the given operation is the Dummy Alert Operation
     * @param operation ManagementOperation
     * @return true if the given operation is the Dummy Alert Operation,
     *         false otherwise
     */
    public static boolean isDummyOperation(ManagementOperation operation) {
        if (operation == null) {
            return false;
        }
        if (!(operation instanceof UserAlertManagementOperation)) {
            return false;
        }
        UserAlertManagementOperation alertOperation =
            (UserAlertManagementOperation)operation;

        if (alertOperation.getAlertCode() != AlertCode.DISPLAY)  {
            return false;
        }

        String[] alerts = alertOperation.getAlerts();
        if (alerts == null || alerts.length != 1) {
            return false;
        }
        if (alerts[0] == null ||
            !(resourceBundle.getString(MESSAGE_DUMMY_ALERT_OPERATION).equals(alerts[0]))) {
            return false;
        }

        return true;
    }

    /**
     * Returns the dataSource
     * @return DataSource
     */
    public DataSource getDataSource() {
        if (datasource == null) {
            datasource = lookupDataSource();
        }
        return datasource;
    }

    /**
     * Returns the sessions list of the given device. If the device is null, all
     * sessions are returned.
     * @param deviceId String
     * @return List
     * @throws ManagementException
     */
    public List<Map> getSessionsList(String deviceId) throws ManagementException {

        String whereDeviceId = "";
        if (deviceId != null && !deviceId.equals("")) {
            whereDeviceId = " WHERE device=?";
        } else {
            deviceId = "";
        }

        Connection con          = null;
        PreparedStatement pStmt = null;
        ResultSet rs            = null;

        String query = SQL_SELECT_DM_SESSIONS_LIST + " " +
                       whereDeviceId + " order by id, end_ts";

        List<Map> sessionsList = null;
        try {
            con   = datasource.getConnection();
            pStmt = con.prepareStatement(query);
            if (!deviceId.equals("")) {
                pStmt.setObject(1, deviceId);
            }
            rs = pStmt.executeQuery();

            //
            // Gets the sessions list
            //
            sessionsList = new ArrayList<Map>();
            Map<String, Comparable> row = null;
            while (rs.next()) {
                row = new HashMap<String, Comparable>();
                row.put("id",        rs.getString("id"));
                row.put("device",    rs.getString("device"));
                row.put("operation", rs.getString("operation"));
                row.put("state",     rs.getString("state"));
                row.put("start_ts",  rs.getTimestamp("start_ts"));
                row.put("end_ts",    rs.getTimestamp("end_ts"));
                sessionsList.add(row);
            }
        } catch (SQLException e) {
            throw new ManagementException("Error reading the sessions list", e);
        } finally {
            DBTools.close(con, pStmt, rs);
        }
        return sessionsList;
    }

    /**
     * Start a new management session for the given deviceId and with the given processor
     *
     * @param deviceId the device id
     * @param processor the processor to use
     * @throws ManagementException if the processor is unknow
     */
    public void startNewManagementSession(String deviceId, String processor) throws
        ManagementException {

        String sessionId = String.valueOf(System.currentTimeMillis());

        setSessionId(sessionId);
        this.deviceId = deviceId;
        this.state    = "P";

        if (PROCESSOR_GENERIC.equals(processor)) {
            this.operation = PROCESSOR_GENERIC;
            genericProcessorTools.startNewManagementSession(deviceId, sessionId, processor);
        } else if (PROCESSOR_TREE_DISCOVERY.equals(processor)) {
            this.operation = PROCESSOR_TREE_DISCOVERY;
            treeDiscoveryProcessorTools.startNewManagementSession(deviceId, sessionId);
        } else if (PROCESSOR_GET_DEVICE_DETAILS.equals(processor)) {
            this.operation = PROCESSOR_GET_DEVICE_DETAILS;
            genericProcessorTools.startNewManagementSession(deviceId, sessionId, processor);
        } else {
            throw new ManagementException("Unknow Processor");
        }
    }

    /**
     * Load the session with the given sessionId
     * @param sessionId String
     * @throws ManagementException
     */
    public void loadSession(String sessionId) throws ManagementException {
        if (log.isEnabled(Level.TRACE)) {
            log.trace("loadSession with sessionId: " + sessionId);
        }

        Connection con = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;

        try {
            con = datasource.getConnection();

            pStmt = con.prepareStatement(SQL_SELECT_DM_STATE);
            pStmt.setObject(1, sessionId);

            rs = pStmt.executeQuery();

            while (rs.next()) {
                this.deviceId  = rs.getString("device");
                this.state     = rs.getString("state");
                this.sessionId = sessionId;
                this.operation = rs.getString("operation");
            }

            if (PROCESSOR_GENERIC.equals(this.operation) || PROCESSOR_GET_DEVICE_DETAILS.equals(this.operation)) {
                genericProcessorTools.loadGenericSession(sessionId);
            } else if (PROCESSOR_TREE_DISCOVERY.equals(this.operation)) {
                treeDiscoveryProcessorTools.loadTreeSession(sessionId);
            } else {
                throw new ManagementException("Unknow processor");
            }

        } catch (SQLException e) {
            throw new ManagementException("Error reading the session operations", e);
        } finally {
            DBTools.close(con, pStmt, rs);
        }
    }

    /**
     * Register a new dm demo user using <code>RegistrationTools</code>
     * @param name String
     * @param company String
     * @param job String
     * @param address String
     * @param city String
     * @param zip String
     * @param state String
     * @param country String
     * @param email String
     * @param phone String
     * @param deviceId String
     * @param password String
     * @throws ManagementException
     */
    public void registerNewUser(String name,
                                String company,
                                String job,
                                String address,
                                String city,
                                String zip,
                                String state,
                                String country,
                                String email,
                                String phone,
                                String deviceId) throws ManagementException {
        registrationTools.registerNewUser(name,
                                          company,
                                          job,
                                          address,
                                          city,
                                          zip,
                                          state,
                                          country,
                                          email,
                                          phone,
                                          deviceId);
    }

    /**
     * Performe login
     * @param deviceId the device id
     * @param password the password
     * @return boolean true if the user is authenticated, false otherwise
     * @throws ManagementException
     */
    public boolean loginUser(String deviceId, String password) throws ManagementException {
        if (registrationTools.loginUser(deviceId, password)) {
            this.deviceId     = deviceId;
            this.password     = password;
            this.userIsLogged = true;
            return true;
        }
        this.userIsLogged = false;
        return false;
    }

    /**
     * Checks if the given device already exists
     * @param deviceId the device id
     * @return boolean true if the device already exists in fnbl_user_dm_demo
     * @throws ManagementException
     */
    public boolean checkIfUserAlreadyExists(String deviceId) throws ManagementException {
        return registrationTools.checkIfUserAlreadyExists(deviceId);
    }

    /**
     * Returns true if the user is logged
     * @return boolean
     */
    public boolean isUserLogged() {
        return userIsLogged;
    }

    /**
     * Save the session
     * @throws ManagementException
     */
    public void saveSession() throws ManagementException {

        if (sessionId == null) {
            return;
        }
        int numUpdated = updateDMState();
        if (numUpdated == 0) {
            insertDMState();
        }

        if (PROCESSOR_GENERIC.equals(this.operation)) {
            genericProcessorTools.saveSession();
        } else if (PROCESSOR_TREE_DISCOVERY.equals(this.operation)) {
            treeDiscoveryProcessorTools.saveSession();
        } else if (PROCESSOR_GET_DEVICE_DETAILS.equals(this.operation)) {
            genericProcessorTools.saveSession();
        } else {
            throw new ManagementException("Unknow processor");
        }
    }

    /**
     * Remove the session that currently is in state "P"
     * @throws ManagementException
     */
    public void deleteSessionToProcess() throws ManagementException {

        if (log.isEnabled(Level.TRACE)) {
            log.trace("deleteSession in state 'P'");
        }

        Connection con          = null;
        PreparedStatement pStmt = null;
        ResultSet rs            = null;
        String sessionId        = null;
        String operation        = null;
        try {
            //
            // delete dm_state
            //
            con   = datasource.getConnection();
            pStmt = con.prepareStatement(SQL_SELECT_DM_SESSIONS_LIST + " WHERE state='P'");

            rs = pStmt.executeQuery();

            while (rs.next()) {
                sessionId = rs.getString("id");
                operation = rs.getString("operation");

                setSessionId(sessionId);
                setOperation(operation);

                if (log.isEnabled(Level.TRACE)) {
                    log.trace("Delete session with id: " + sessionId);
                }
                deleteSession();

                if (PROCESSOR_GENERIC.equals(operation) || PROCESSOR_GET_DEVICE_DETAILS.equals(operation)) {
                    genericProcessorTools.deleteSession();
                } else if (PROCESSOR_TREE_DISCOVERY.equals(operation)) {
                    treeDiscoveryProcessorTools.deleteSession();
                } else {
                    throw new ManagementException("Unknow processor");
                }
            }


        } catch (SQLException e) {
            throw new ManagementException("Error deleting the session", e);
        } finally {
            DBTools.close(con, pStmt, rs);
        }
    }


    /**
     * Delete the session
     * @throws ManagementException
     */
    public void deleteSession() throws ManagementException {
        if (log.isEnabled(Level.TRACE)) {
            log.trace("deleteSession with sessionId: " + sessionId);
        }

        if (sessionId == null) {
            return;
        }

        Connection con          = null;
        PreparedStatement pStmt = null;
        ResultSet rs            = null;

        try {
            //
            // delete dm_state
            //
            con   = datasource.getConnection();
            pStmt = con.prepareStatement(SQL_DELETE_DM_STATE);

            pStmt.setObject(1, sessionId);

            pStmt.executeUpdate();

            if (PROCESSOR_GENERIC.equals(this.operation) || PROCESSOR_GET_DEVICE_DETAILS.equals(this.operation)) {
                genericProcessorTools.deleteSession();
            } else if (PROCESSOR_TREE_DISCOVERY.equals(this.operation)) {
                treeDiscoveryProcessorTools.deleteSession();
            } else {
                throw new ManagementException("Unknow processor");
            }

        } catch (SQLException e) {
            throw new ManagementException("Error deleting the session", e);
        } finally {
            DBTools.close(con, pStmt, rs);
        }
    }

    /**
     * Returns the device id
     * @return the device id
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Sets the device id
     * @param deviceId the device id to set
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Returns the operation (processor)
     * @return the operation (processor)
     */
    public String getOperation() {
        return operation;
    }

    /**
     * Sets the operation (processor)
     * @param operation the operation (processor) to sets
     */
    public void setOperation(String operation) {
        this.operation = operation;
    }

    /**
     * Returns the session id
     * @return the session id
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Sets the session id
     * @param sessionId the session id to set
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
        genericProcessorTools.setSessionId(sessionId);
        treeDiscoveryProcessorTools.setSessionId(sessionId);
    }

    /**
     * Returns the session state
     * @return the session state
     */
    public String getSessionState() {
        return state;
    }

    /**
     * Returns a description for state of the session
     * @return a description for state of the session
     */
    public String getStateDescription() {
        if (state == null || state.equals("")) {
            return "Unknow";
        } else if (state.equalsIgnoreCase("P")) {
            return "To process";
        }
        if (state.equalsIgnoreCase("C")) {
            return "Completed";
        }
        return "Unknow";
    }

    /**
     * Returns a String representation of this object
     * @return String
     */
    public String toString() {
        ToStringBuilder sb = new ToStringBuilder(this);
        sb.append("sessionId", sessionId);
        sb.append("deviceId", deviceId);
        sb.append("state", state);
        sb.append("operation", operation);
        sb.append("genericProcessorTools", genericProcessorTools);
        sb.append("treeDiscoveryProcessorTools", treeDiscoveryProcessorTools);
        return sb.toString();
    }

    // --------------------------------------------------------- Private Methods

    /**
     * Lookup the datasource and returns it
     * @return the datasource
     */
    private DataSource lookupDataSource() {
        if (log.isEnabled(Level.TRACE)) {
            log.trace("getDataSource: " + JNDI_NAME);
        }
        DataSource ds = null;
        try {
            InitialContext namingContext = new InitialContext();
            Object obj = (DataSource)namingContext.lookup(JNDI_NAME);
            ds = (DataSource)obj;
            if (log.isEnabled(Level.TRACE)) {
                log.trace("datasource: " + ds);
            }
        } catch (Exception e) {
            log.debug( "lookupDataSource", e);
            if (log.isEnabled(Level.FATAL)) {
                log.fatal("Error getting the datasource: " + e.getMessage());
            }
        }
        return ds;
    }

    /**
     * Update the session in DMState table
     * @return int
     * @throws ManagementException
     */
    private int updateDMState() throws ManagementException {
        if (log.isEnabled(Level.TRACE)) {
            log.trace("updateDMState with sessionId: " + sessionId);
        }

        Connection con = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;

        int numUpdated = 0;
        try {
            con = datasource.getConnection();

            pStmt = con.prepareStatement(SQL_UPDATE_DM_STATE);
            pStmt.setObject(1, deviceId);
            pStmt.setObject(2, operation);
            pStmt.setObject(3, sessionId);

            numUpdated = pStmt.executeUpdate();

        } catch (SQLException e) {
            throw new ManagementException("Error updating the session", e);
        } finally {
            DBTools.close(con, pStmt, rs);
        }
        return numUpdated;
    }

    /**
     * Insert the session in DMState table
     * @throws ManagementException
     */
    private void insertDMState() throws ManagementException {
        if (log.isEnabled(Level.TRACE)) {
            log.trace("insertDMState with sessionId: " + sessionId);
        }

        if (sessionId == null) {
            return;
        }

        Connection con = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;

        try {
            //
            // insert new dm_state
            //
            con = datasource.getConnection();
            pStmt = con.prepareStatement(SQL_INSERT_DM_STATE);

            pStmt.setObject(1, sessionId);
            pStmt.setObject(2, deviceId);
            pStmt.setObject(3, sessionId);
            pStmt.setObject(4, operation);

            pStmt.executeUpdate();

        } catch (SQLException e) {
            throw new ManagementException("Error inserting the session", e);
        } finally {
            DBTools.close(con, pStmt, rs);
        }
    }
}
