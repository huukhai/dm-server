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

import com.funambol.framework.engine.dm.AddManagementOperation;
import com.funambol.framework.engine.dm.DeleteManagementOperation;
import com.funambol.framework.engine.dm.GetManagementOperation;
import com.funambol.framework.engine.dm.ManagementException;
import com.funambol.framework.engine.dm.ManagementOperation;
import com.funambol.framework.engine.dm.ManagementOperationResult;
import com.funambol.framework.engine.dm.ReplaceManagementOperation;
import com.funambol.framework.engine.dm.TreeManagementOperation;
import com.funambol.framework.engine.dm.TreeNode;
import com.funambol.framework.tools.Base64;
import com.funambol.framework.tools.DBTools;
import com.funambol.framework.tools.MD5;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.lang.builder.ToStringBuilder;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;


/**
 *
 *@version $Id: ManagementSessionTools.java,v 1.4 2007-06-18 16:38:44 luigiafassina Exp $
 */
public class ManagementSessionTools {

    // --------------------------------------------------------------- Constants
    private static final String JNDI_NAME = "java:jboss/datasources/FunambolDMDS";

    private static final String DEFAULT_USER_NAME     = "funambol";
    private static final String DEFAULT_USER_PASSWORD = "funambol";

    private static final String DEFAULT_DEVICE_DESCRIPTION     = "phone for DM demo";
    private static final String DEFAULT_DEVICE_SERVER_PASSWORD = "srvpwd";
    private static final String DEFAULT_DEVICE_TYPE            = "phone";


    private static final String COMMAND_WITHOUT_VALUE_IN_RESPONSE =
        "Add, Replace";

    private static final String SQL_INSERT_DM_STATE =
        "INSERT INTO fnbl_dm_state (id,device,mssid,state,operation) values (?,?,?,'P',?)";

    private static final String SQL_INSERT_DM_DEMO_OPERATIONS =
        "INSERT INTO fnbl_dmdemo_operations (id,operations) values (?,?)";

    private static final String SQL_SELECT_DM_DEMO_OPERATIONS =
        "SELECT id,operations,results FROM fnbl_dmdemo_operations WHERE id=?";

    private static final String SQL_SELECT_DM_SESSIONS_LIST =
        "SELECT id, device, state, operation, start_ts, end_ts FROM fnbl_dm_state ";

    private static final String SQL_SELECT_DM_STATE =
        "SELECT id, device, state, start_ts, end_ts FROM fnbl_dm_state WHERE mssid=?";

    private static final String SQL_UPDATE_DM_DEMO_OPERATIONS =
        "UPDATE fnbl_dmdemo_operations SET operations=? WHERE id=?";

    private static final String SQL_INSERT_TREE_DISCOVERY_SESSION =
        "INSERT INTO fnbl_device_tree (id,root_node) values (?,?)";

    private static final String SQL_UPDATE_TREE_DISCOVERY_SESSION =
        "UPDATE fnbl_device_tree set root_node=? WHERE id=?";

    private static final String SQL_SELECT_TREE_DISCOVERY_SESSION =
        "SELECT id, root_node, results FROM fnbl_device_tree WHERE id=? ";

    private static final String SQL_INSERT_NEW_USER_DM_DEMO =
        "INSERT INTO fnbl_user_dm_demo " +
        " (name, company, job, address, city, zip, state, country, email, phone, device_id, password) " +
        " VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";

    private static final String SQL_SELECT_USER_DM_DEMO = "SELECT password FROM fnbl_user_dm_demo WHERE device_id=?";

    private static final String SQL_INSERT_USER =
        "INSERT INTO fnbl_user (username) values (?)";

    private static final String SQL_UPDATE_USER =
        "UPDATE fnbl_user SET password=?, email=?, last_name=? WHERE username = ?";

    private static final String SQL_INSERT_DEVICE =
        "INSERT INTO fnbl_device (id, description, type, digest, server_password) " +
        " values (?,?,?,?,?)";

    private static final String SQL_UPDATE_DEVICE =
        "UPDATE fnbl_device SET description=?, type=?, digest=?, client_nonce=?, server_nonce=?, server_password=? " +
        " WHERE id= ?";

    private static final String SQL_SELECT_USER = "SELECT * FROM fnbl_user WHERE username=?";

    private static final String SQL_SELECT_DEVICE = "SELECT * FROM fnbl_device WHERE id=?";

    private static final String SQL_SELECT_ID_PRINCIPAL =
        "SELECT counter FROM fnbl_id WHERE idspace=?";

    private static final String SQL_UPDATE_ID_PRINCIPAL =
        "UPDATE fnbl_id SET counter=? WHERE idspace=?";

    private static final String SQL_SELECT_PRINCIPAL =
        "SELECT * FROM fnbl_principal WHERE username=? AND device=?";

    private static final String SQL_INSERT_PRINCIPAL =
        "INSERT INTO fnbl_principal (id, username, device) values (?,?,?)";

    // ------------------------------------------------------------ Private data

    private static final Logger log = Logger.getLogger(com.funambol.dmdemo.ManagementSessionTools.class.getName());

    private DataSource datasource = null;

    private String sessionId = null;
    private String state     = null;
    private String deviceId  = null;

    private List<ManagementOperation> operationsList    = null;
    private List<ManagementOperationResult> operationsResults = null;

    private String rootNode = null;
    private Map treeDiscoveryResults = null;

    // ------------------------------------------------------------ Constructors
    public ManagementSessionTools() {
        if (log.isEnabled(Level.TRACE)) {
            log.trace("New ManagementSessionTools");
        }
        datasource = lookupDataSource();
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
        if (deviceId != null && !deviceId.equals("")){
            whereDeviceId = " WHERE device=?";
        } else {
            deviceId = "";
        }

        Connection con = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;

        String query = SQL_SELECT_DM_SESSIONS_LIST + " " +
                       whereDeviceId + " order by id, end_ts";

        List<Map> sessionsList = null;
        try {
            con = datasource.getConnection();
            pStmt = con.prepareStatement(query);
            if (deviceId != null && !deviceId.equals("")) {
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
                row.put("id", rs.getString("id"));
                row.put("device", rs.getString("device"));
                row.put("operation", rs.getString("operation"));
                row.put("state", rs.getString("state"));
                row.put("start_ts", rs.getTimestamp("start_ts"));
                row.put("end_ts", rs.getTimestamp("end_ts"));
                sessionsList.add(row);
            }
        } catch (SQLException e) {
            throw new ManagementException("Error reading the sessions list", e);
        } finally {
            DBTools.close(con, pStmt, rs);
        }
        return sessionsList;
    }

    public int updateOperationsList() throws ManagementException {

        if (sessionId == null) {
            return 0;
        }

        Connection con = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;

        int numUpdated = 0;

        try {
            //
            // update dm_demo
            //
            con = datasource.getConnection();
            pStmt = con.prepareStatement(SQL_UPDATE_DM_DEMO_OPERATIONS);

            pStmt.setObject(1, getXmlOperationsList());
            pStmt.setObject(2, sessionId);

            numUpdated = pStmt.executeUpdate();

        } catch (SQLException e) {
            throw new ManagementException("Error updating the operations list for session: " + sessionId, e);
        } finally {
            DBTools.close(con, pStmt, rs);
        }
        return numUpdated;
    }

    public List<ManagementOperation> getOperationsList() {
        if (operationsList == null) {
            return new ArrayList<ManagementOperation>();
        }
        return operationsList;
    }

    public Map getTreeDiscoveryResults() {
        if (treeDiscoveryResults == null) {
            return new HashMap();
        }
        return treeDiscoveryResults;
    }

    public List<ManagementOperationResult> getOperationsResults() {
        if (operationsResults == null) {
            return new ArrayList<ManagementOperationResult>();
        }
        return operationsResults;
    }

    public void startNewManagementSession(String deviceId) throws ManagementException {

        String sessionId = String.valueOf(System.currentTimeMillis());

        this.operationsList = new ArrayList<ManagementOperation>();
        this.operationsResults = null;
        this.sessionId = sessionId;
        this.deviceId = deviceId;
        this.state = "P";
        this.rootNode = null;
        this.treeDiscoveryResults = null;
    }

    public void saveOperationsList()  throws ManagementException {
        if (sessionId == null) {
            return ;
        }
        int numUpdated = updateOperationsList();
        if (numUpdated == 0) {
            insertOperationsList();
        }
    }

    public boolean loginUser(String deviceId, String password) throws ManagementException {

        Connection con = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;


        String dbPassword = null;

        try {
            con = datasource.getConnection();
            pStmt = con.prepareStatement(SQL_SELECT_USER_DM_DEMO);

            pStmt.setObject(1, deviceId);

            rs = pStmt.executeQuery();

            while (rs.next()) {
                dbPassword = rs.getString(1);
            }
        } catch (SQLException e) {
            throw new ManagementException("Error reading the user", e);
        } finally {
            DBTools.close(con, pStmt, rs);
        }

        if (dbPassword != null && dbPassword.equalsIgnoreCase(password)) {
            this.deviceId = deviceId;
            return true;
        }

        return false;
    }


    public void insertOperationsList() throws ManagementException {

        if (sessionId == null) {
            return ;
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
            pStmt.setObject(4, "DMDemoProcessor");


            int temp = pStmt.executeUpdate();

            //
            // insert new dm_demo_operation
            //
            pStmt = con.prepareStatement(SQL_INSERT_DM_DEMO_OPERATIONS);

            pStmt.setObject(1, sessionId);
            pStmt.setObject(2, getXmlOperationsList());

            pStmt.executeUpdate();

        } catch (SQLException e) {
            throw new ManagementException("Error inserting the session", e);
        } finally {
            DBTools.close(con, pStmt, rs);
        }

    }

    public void addManagementOperation(String operationName,
                                       String nodeUri,
                                       String value,
                                       boolean newNode) throws ManagementException {

        ManagementOperation operationToAdd = null;

        if (operationsList == null) {
            if (log.isEnabled(Level.TRACE)) {
                log.trace("Try to add a new operation with operationsList null");
            }
            return;
        }

        //
        // Before to add a new operation, checks if the session is again in state 'P'
        //
        String currentState = loadSessionState();
        if (currentState == null) {
            //
            // The session isn't in the db
            //
            this.state = "P";
        } else {
            this.state = currentState;
        }

        if (state != null && state.equals("P")) {
            //
            // Add operation
            //
            if (operationName != null) {
                if (operationName.equalsIgnoreCase("Add")) {
                    operationToAdd = new AddManagementOperation();

                    Map<String, Serializable> nodes = new HashMap<String, Serializable>();

                    if (newNode) {
                        TreeNode newNodeToAdd = new TreeNode(nodeUri);
                        newNodeToAdd.setFormat(TreeNode.FORMAT_NODE);
                        nodes.put(nodeUri, newNodeToAdd);
                    } else {
                        nodes.put(nodeUri, value);
                    }

                    ( (AddManagementOperation)operationToAdd).setNodes(nodes);
                } else if (operationName.equalsIgnoreCase("Get")) {
                    operationToAdd = new GetManagementOperation();
                    Map<String, Serializable> nodes = new HashMap<String, Serializable>();
                    nodes.put(nodeUri, "");
                    ( (GetManagementOperation)operationToAdd).setNodes(nodes);
                } else if (operationName.equalsIgnoreCase("Delete")) {
                    operationToAdd = new DeleteManagementOperation();
                    Map<String, Serializable> nodes = new HashMap<String, Serializable>();
                    nodes.put(nodeUri, "");
                    ( (DeleteManagementOperation)operationToAdd).setNodes(nodes);
                } else if (operationName.equalsIgnoreCase("Replace")) {
                    operationToAdd = new ReplaceManagementOperation();
                    Map<String, Serializable> nodes = new HashMap<String, Serializable>();
                    nodes.put(nodeUri, value);
                    ( (ReplaceManagementOperation)operationToAdd).setNodes(nodes);
                }
            }

            if (operationToAdd != null) {
                operationsList.add(operationToAdd);
                //updateOperationsList();
            }
        }
    }


    public List<ManagementOperationResult> getOperationsResultsWithValue() {
        if (operationsResults == null) {
            return null;
        }
        Iterator<ManagementOperationResult> itResults = operationsResults.iterator();
        ManagementOperationResult operationResult = null;

        String commandName = null;
        while (itResults.hasNext()) {
            operationResult = itResults.next();
            commandName = operationResult.getCommand();
            if (COMMAND_WITHOUT_VALUE_IN_RESPONSE.indexOf(commandName) != -1) {
                setValuesInManagementOperationResult(operationResult);
            }
        }
        return operationsResults;
    }


    public String getRootNode() {
        return rootNode;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }


    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void moveOperationUp(int index) {
        ManagementOperation opToMove = null;
        if (index > 0) {
            opToMove = operationsList.remove(index);
            operationsList.add(index - 1, opToMove);
        }

    }

    public void moveOperationDown(int index) {
        ManagementOperation opToMove = null;
        if (index < (operationsList.size() - 1)) {
            opToMove = operationsList.remove(index);
            operationsList.add(index + 1, opToMove);
        }
    }


    public void removeOperation(int index) {
        if (operationsList == null) {
            return;
        }
        if (index != -1) {
            operationsList.remove(index);
        }
    }

    public String getSessionState() {
        return state;
    }

    public String getStateDescription() {
        if (state == null || state.equals("")) {
            return "Unknow";
        } else if (state.equalsIgnoreCase("P")) {
            return "To process";
        } if (state.equalsIgnoreCase("C")) {
            return "Completed";
        }
        return "Unknow";
    }

    public void loadDMSession(String sessionId) throws ManagementException {

        if (log.isEnabled(Level.TRACE)) {
            log.trace("loadDMSession with sessionId: " + sessionId);
        }

        Connection con = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;

        String xmlOperations = null;
        String xmlOperationsResults = null;
        try {
            con = datasource.getConnection();

            pStmt = con.prepareStatement(SQL_SELECT_DM_STATE);
            pStmt.setObject(1, sessionId);

            rs = pStmt.executeQuery();

            while (rs.next()) {
                this.deviceId  = rs.getString("device");
                this.state     = rs.getString("state");
                this.sessionId = sessionId;
            }

            pStmt = con.prepareStatement(SQL_SELECT_DM_DEMO_OPERATIONS);
            pStmt.setObject(1, sessionId);

            rs = pStmt.executeQuery();

            //
            // Gets the sessions list
            //
            while (rs.next()) {
                xmlOperations = rs.getString("operations");
                xmlOperationsResults = rs.getString("results");
            }

        } catch (SQLException e) {
            throw new ManagementException("Error reading the session operations", e);
        } finally {
            DBTools.close(con, pStmt, rs);
        }

        if (xmlOperations == null) {
            this.operationsList = new ArrayList<ManagementOperation>(); //  empty arraylist
        } else {
            XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(xmlOperations.getBytes()));
            this.operationsList = (ArrayList<ManagementOperation>)decoder.readObject();
        }

        if (log.isEnabled(Level.TRACE)) {
            log.trace("number of operations: " + operationsList.size());
        }

        if (xmlOperationsResults == null) {
            this.operationsResults = new ArrayList<ManagementOperationResult>(); //  empty arraylist
        } else {
            XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(xmlOperationsResults.getBytes()));
            this.operationsResults = (ArrayList<ManagementOperationResult>)decoder.readObject();
        }

        if (log.isEnabled(Level.TRACE)) {
            log.trace("number of results: " + operationsResults.size());
        }

    }



    public void cloneSession() {
        //this.deviceId = null;
        this.state    = "P";
        this.sessionId = String.valueOf(System.currentTimeMillis());
        this.operationsResults = null;
    }


    // ---------------------------------------------------------- Private methods

    /**
     * Search in the operationList the operation correspondent to the given
     * <code>ManagementOperationResult</code>. If found it, sets the nodes in the ManagementOperationResult
     * with the nodes in the operation because the nodes in operation contains also the
     * value.
     * @param operationResult ManagementOperationResult
     */
    private void setValuesInManagementOperationResult(ManagementOperationResult operationResult) {

        String commandName = operationResult.getCommand();
        Map<String, Object> resultNodes    = operationResult.getNodes();

        Iterator<ManagementOperation> itOperationsList = operationsList.iterator();
        String operationCommandName = null;
        Map<String, Object> operationNodes          = null;
        ManagementOperation operation = null;

        while (itOperationsList.hasNext()) {
            operation            = itOperationsList.next();
            operationCommandName = operation.getDescription();
            if (!(operation instanceof TreeManagementOperation)) {
                continue;
            }
            if (!operationCommandName.equals(commandName)) {
                continue;
            }
            operationNodes = ((TreeManagementOperation)operation).getNodes();
            if (operationNodes.size() != resultNodes.size()) {
                continue;
            }
            if (compareKeys(resultNodes, operationNodes)) {
                //
                // Found operation
                //
                operationResult.setNodes(operationNodes);
            }
        }
    }


    /**
     * Checks if the map2 contains all keys that are in map1.
     * @param map1 Map
     * @param map2 Map
     * @return boolean
     */
    private boolean compareKeys(Map map1, Map<String, Object> map2) {
        Iterator itMap1 = map1.keySet().iterator();
        String key1 = null;
        while (itMap1.hasNext()) {
            key1 = (String)itMap1.next();
            if (!map2.containsKey(key1)) {
                //
                // The map2 not contains the key1
                //
                return false;
            }
        }
        return true;
    }


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
        }catch(Exception e){
            log.debug( "lookupDataSource", e);
            if (log.isEnabled(Level.FATAL)) {
                log.fatal("Error getting the datasource: " + e.getMessage());
            }
        }
        return ds;
    }


    private String getXmlOperationsList() {
        if (operationsList == null) {
            return null;
        }
        String xml = null;
        byte[] bytes = null;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        XMLEncoder encoder = new XMLEncoder(bout);
        encoder.writeObject(operationsList);
        encoder.flush();
        encoder.close();
        bytes = bout.toByteArray();
        xml = new String(bytes);
        return xml;
    }

    private String getXmlForEmptyListOfOperations() {
        String xml;
        byte[] bytes = null;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        XMLEncoder encoder = new XMLEncoder(bout);
        encoder.writeObject(new ArrayList());
        encoder.flush();
        encoder.close();
        bytes = bout.toByteArray();
        xml = new String(bytes);
        return xml;
    }

    public String toString() {
        ToStringBuilder sb = new ToStringBuilder(this);
        sb.append("sessionId", sessionId);
        sb.append("deviceId", deviceId);
        sb.append("state", state);
        sb.append("num.operations", (operationsList == null) ? "null" : String.valueOf(operationsList.size()));
        sb.append("num.results", (operationsResults == null) ? "null" : String.valueOf(operationsResults.size()));
        sb.append("rootNode", rootNode);
        sb.append("num.results treeDiscovery", (treeDiscoveryResults == null) ? "null" : String.valueOf(treeDiscoveryResults.size()));
        return sb.toString();
    }

    private String loadSessionState() throws ManagementException {
        if (log.isEnabled(Level.TRACE)) {
            log.trace("loadSessionState with sessionId: " + sessionId);
        }

        Connection con = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;

        String state = null;

        try {
            con = datasource.getConnection();

            pStmt = con.prepareStatement(SQL_SELECT_DM_STATE);
            pStmt.setObject(1, sessionId);

            rs = pStmt.executeQuery();

            while (rs.next()) {
                state = rs.getString("state");
            }

        } catch (SQLException e) {
            throw new ManagementException("Error reading the session operations", e);
        } finally {
            DBTools.close(con, pStmt, rs);
        }


        if (log.isEnabled(Level.TRACE)) {
            log.trace("State of the session '" + sessionId + "':  " + state);
        }

        return state;
    }

    public void insertTreeDiscoverySession(String rootNode) throws ManagementException {

        if (sessionId == null) {
            return ;
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
            pStmt.setObject(4, "TreeDiscovery");

            int temp = pStmt.executeUpdate();

            //
            // insert new dm_demo_operation
            //
            pStmt = con.prepareStatement(SQL_INSERT_TREE_DISCOVERY_SESSION);

            pStmt.setObject(1, sessionId);
            pStmt.setObject(2, rootNode);

            pStmt.executeUpdate();

        } catch (SQLException e) {
            throw new ManagementException("Error inserting the session", e);
        } finally {
            DBTools.close(con, pStmt, rs);
        }

    }

    public int updateTreeDiscoverySession(String rootNode) throws ManagementException {

        if (sessionId == null) {
            return 0;
        }

        Connection con = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;

        int numUpdated = 0;

        try {
            //
            // update dm_demo
            //
            con = datasource.getConnection();
            pStmt = con.prepareStatement(SQL_UPDATE_TREE_DISCOVERY_SESSION);

            pStmt.setObject(1, rootNode);
            pStmt.setObject(2, sessionId);

            numUpdated = pStmt.executeUpdate();

        } catch (SQLException e) {
            throw new ManagementException("Error updating the treeDiscoverySession with session: " + sessionId, e);
        } finally {
            DBTools.close(con, pStmt, rs);
        }
        return numUpdated;
    }

    public void saveTreeDiscoverySession(String rootNode) throws ManagementException {
        if (sessionId == null) {
            throw new ManagementException("The sessionId must be != null");
        }
        if (rootNode == null) {
            throw new ManagementException("The root node must be != null");
        }
        int numUpdated = updateTreeDiscoverySession(rootNode);
        if (numUpdated == 0) {
            insertTreeDiscoverySession(rootNode);
        }
    }

    public void loadTreeSession(String sessionId) throws ManagementException {
        if (log.isEnabled(Level.TRACE)) {
            log.trace("loadTreeSession with sessionId: " + sessionId);
        }

        Connection con = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;


        String xmlOperationsResults = null;
        try {
            con = datasource.getConnection();

            pStmt = con.prepareStatement(SQL_SELECT_DM_STATE);
            pStmt.setObject(1, sessionId);

            rs = pStmt.executeQuery();

            while (rs.next()) {
                this.deviceId  = rs.getString("device");
                this.state     = rs.getString("state");
                this.sessionId = sessionId;
            }

            pStmt = con.prepareStatement(SQL_SELECT_TREE_DISCOVERY_SESSION);
            pStmt.setObject(1, sessionId);

            rs = pStmt.executeQuery();

            //
            // Gets the sessions list
            //
            while (rs.next()) {
                this.rootNode = rs.getString("root_node");
                xmlOperationsResults = rs.getString("results");
            }

        } catch (SQLException e) {
            throw new ManagementException("Error reading the session operations", e);
        } finally {
            DBTools.close(con, pStmt, rs);
        }


        if (xmlOperationsResults == null) {
            this.treeDiscoveryResults = new TreeMap();
        } else {
            XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(xmlOperationsResults.getBytes()));
            this.treeDiscoveryResults = (Map)decoder.readObject();
        }

        if (log.isEnabled(Level.TRACE)) {
            log.trace("number of results: " + treeDiscoveryResults.size());
        }
    }

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
                              String deviceId,
                              String password) throws ManagementException {

      insertNewUser(name, company, job, address, city, zip, state, country, email, phone, deviceId,
                    password);

      String digest = setSync4jUser();

      setSync4jDevice(deviceId, digest);

      setSync4jPrincipal(digest, deviceId);

  }



    public void insertNewUser(String name,
                              String company,
                              String job,
                              String address,
                              String city,
                              String zip,
                              String state,
                              String country,
                              String email,
                              String phone,
                              String deviceId,
                              String password) throws ManagementException {

            Connection con = null;
            PreparedStatement pStmt = null;
            ResultSet rs = null;

            if (name == null || name.equals("") ||
                deviceId == null || deviceId.equals("") ||
                password == null || password.equals("") ||
                email == null || email.equals("")) {

                throw new ManagementException("Name, Email, Device Id and Password are required");
            }

            try {
                //
                // insert new dm_state
                //
                con = datasource.getConnection();
                pStmt = con.prepareStatement(SQL_INSERT_NEW_USER_DM_DEMO);

                pStmt.setObject(1, name);
                pStmt.setObject(2, company);
                pStmt.setObject(3, job);
                pStmt.setObject(4, address);
                pStmt.setObject(5, city);
                pStmt.setObject(6, zip);
                pStmt.setObject(7, state);
                pStmt.setObject(8, country);
                pStmt.setObject(9, email);
                pStmt.setObject(10, phone);
                pStmt.setObject(11, deviceId);
                pStmt.setObject(12, password);

                int temp = pStmt.executeUpdate();


            } catch (SQLException e) {
                throw new ManagementException("Error inserting the new user", e);
            } finally {
                DBTools.close(con, pStmt, rs);
            }
    }

    private String setSync4jUser() throws ManagementException {

        String name = DEFAULT_USER_NAME;
        String password = DEFAULT_USER_PASSWORD;

        // b64(H(name:password)
        String digest = new String(Base64.encode(MD5.digest( (name + ":" + password).getBytes() )));

        Connection con = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;

        boolean userFound = false;

        try {
            //
            // checks if the user already exist
            //
            con = datasource.getConnection();
            pStmt = con.prepareStatement(SQL_SELECT_USER);

            pStmt.setObject(1, digest);

            rs = pStmt.executeQuery();

            while (rs.next()) {
                userFound = true;
            }

            if (!userFound) {
                pStmt = con.prepareStatement(SQL_INSERT_USER);
                pStmt.setObject(1, digest);

                pStmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw new ManagementException("Error inserting the user", e);
        } finally {
            DBTools.close(con, pStmt, rs);
        }
        return digest;
    }

    private void setSync4jDevice(String deviceId, String digest) throws ManagementException {

        Connection con = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;

        boolean deviceFound = false;

        try {
            //
            // checks if the device already exist
            //
            con = datasource.getConnection();
            pStmt = con.prepareStatement(SQL_SELECT_DEVICE);

            pStmt.setObject(1, deviceId);

            rs = pStmt.executeQuery();

            while (rs.next()) {
                deviceFound = true;
            }

            if (!deviceFound) {
                pStmt = con.prepareStatement(SQL_INSERT_DEVICE);
                pStmt.setObject(1, deviceId);
                pStmt.setObject(2, DEFAULT_DEVICE_DESCRIPTION);
                pStmt.setObject(3, DEFAULT_DEVICE_TYPE);
                pStmt.setObject(4, digest);
                pStmt.setObject(5, DEFAULT_DEVICE_SERVER_PASSWORD);

                pStmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw new ManagementException("Error inserting the device", e);
        } finally {
            DBTools.close(con, pStmt, rs);
        }
    }

    private void setSync4jPrincipal(String digest, String deviceId) throws ManagementException {

        Connection con = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;

        boolean principalFound = false;

        try {
            //
            // checks if the device already exist
            //
            con = datasource.getConnection();
            pStmt = con.prepareStatement(SQL_SELECT_PRINCIPAL);

            pStmt.setObject(1, digest);
            pStmt.setObject(2, deviceId);

            rs = pStmt.executeQuery();

            while (rs.next()) {
                principalFound = true;
            }

            if (!principalFound) {
                pStmt = con.prepareStatement(SQL_INSERT_PRINCIPAL);
                pStmt.setObject(1, getSync4jPrincipalId());
                pStmt.setObject(2, digest);
                pStmt.setObject(3, deviceId);

                pStmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw new ManagementException("Error inserting the principal", e);
        } finally {
            DBTools.close(con, pStmt, rs);
        }
    }

    private String getSync4jPrincipalId() throws ManagementException {
        Connection con = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;

        String sId = null;
        int id     = -1;

        try {

            con = datasource.getConnection();
            pStmt = con.prepareStatement(SQL_SELECT_ID_PRINCIPAL);

            pStmt.setObject(1, "principal");

            rs = pStmt.executeQuery();
            while (rs.next()) {
                sId = rs.getString(1);
            }

            id = Integer.parseInt(sId);

            pStmt = con.prepareStatement(SQL_UPDATE_ID_PRINCIPAL);
            pStmt.setObject(1, String.valueOf( (id +1) ));
            pStmt.setObject(2, "principal");

            pStmt.executeUpdate();


        } catch (SQLException e) {
            throw new ManagementException("Error reading id for the principal", e);
        } finally {
            DBTools.close(con, pStmt, rs);
        }
        return sId;
    }
}
