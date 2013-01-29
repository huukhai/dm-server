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

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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

import javax.sql.DataSource;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.funambol.framework.core.AlertCode;
import com.funambol.framework.engine.dm.AddManagementOperation;
import com.funambol.framework.engine.dm.CopyManagementOperation;
import com.funambol.framework.engine.dm.DeleteManagementOperation;
import com.funambol.framework.engine.dm.ExecManagementOperation;
import com.funambol.framework.engine.dm.GetManagementOperation;
import com.funambol.framework.engine.dm.ManagementException;
import com.funambol.framework.engine.dm.ManagementOperation;
import com.funambol.framework.engine.dm.ManagementOperationResult;
import com.funambol.framework.engine.dm.ReplaceManagementOperation;
import com.funambol.framework.engine.dm.TreeManagementOperation;
import com.funambol.framework.engine.dm.TreeNode;
import com.funambol.framework.engine.dm.UserAlertManagementOperation;
import com.funambol.framework.tools.Base64;
import com.funambol.framework.tools.DBTools;
import com.funambol.framework.tools.IOTools;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;


/**
 * Contains the supported methods for generic management processor.
 *
 * @version $Id: GenericProcessorTools.java,v 1.5 2007-06-18 16:38:44 luigiafassina Exp $
 */
public class GenericProcessorTools {

    // --------------------------------------------------------------- Constants

    public static final String PRECONFIGURED_GET_DEVICE_DETAILS = "GetDeviceDetails";

    private static final String COMMAND_WITHOUT_VALUE_IN_RESPONSE =
        "Add, Replace";

    private static final String SQL_SELECT_DM_STATE =
        "SELECT id, device, state, start_ts, end_ts FROM fnbl_dm_state WHERE mssid=?";

    // ------------------------------------------------------------ Private data

    private static final Logger log = Logger.getLogger(com.funambol.dmdemo.GenericProcessorTools.class.getName());

    private DataSource datasource = null;

    private List<ManagementOperation> operationsList = null;
    private List<ManagementOperationResult> operationsResults = null;
    private String sessionId = null;

    private String sessionsDirectory = null;


    // ------------------------------------------------------------ Constructors
    private GenericProcessorTools() {
        sessionsDirectory = System.getProperty(Constants.SYSTEM_PROPERTY_RESULTS_DIRECTORY);
        if (sessionsDirectory == null) {
            sessionsDirectory = ".";
        }
    }

    public GenericProcessorTools(DataSource ds) {
        this();
        this.datasource = ds;
    }

    // ---------------------------------------------------------- Public Methods

    /**
     * Start a new session.
     * @param deviceId String
     * @param sessionId String
     * @throws ManagementException
     */
    public void startNewManagementSession(String deviceId, String sessionId, String processor) throws
        ManagementException {

        if (DMDemoBean.PROCESSOR_GET_DEVICE_DETAILS.equals(processor)) {
            this.operationsList    = getPreconfiguredOperationsForGetDeviceDetail();

            if (this.operationsList == null) {
                this.operationsList = new ArrayList<ManagementOperation>();
            }
            int numOperations = this.operationsList.size();
            if (numOperations > 0) {
                if (!DMDemoBean.isDummyOperation(
                        this.operationsList.get(numOperations - 1))
                    ) {
                    this.operationsList.add(DMDemoBean.DUMMY_ALERT_OPERATION);
                }
            } else {
                this.operationsList.add(DMDemoBean.DUMMY_ALERT_OPERATION);
            }
        } else {
            this.operationsList = new ArrayList<ManagementOperation>();
        }
        this.operationsResults = null;
        this.sessionId         = sessionId;
    }

    /**
     * Save the session.
     * @throws ManagementException
     */
    public void saveSession() throws ManagementException {

        if (sessionId == null) {
            return;
        }
        saveOperations(operationsList, sessionId);
    }

    /**
     * Delete the session.
     * @throws ManagementException
     */
    public void deleteSession() throws ManagementException {
        if (sessionId == null) {
            return;
        }


        try {

            //
            // We must delete the operations and the results on the filesystem
            //
            deleteSessions(sessionId);

        } catch (Exception e) {
            throw new ManagementException("Error deleting the session", e);
        }
    }

    /**
     * Adds a new operation to the operations list
     * @param operationName the operation name
     * @param nodeUri the node uri
     * @param value the value
     * @param newNode is a new node ?
     * @throws ManagementException
     */
    public void addManagementOperation(String operationName,
                                       String nodeUri,
                                       String value,
                                       String format,
                                       String option1,
                                       String option2,
                                       String option3,
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

        if (currentState == null || currentState.equals("P")) {

            //
            // Checks if the last operations is the dummy operation
            //
            int numOperations = operationsList.size();
            if (numOperations > 0) {
                if (DMDemoBean.isDummyOperation(
                        operationsList.get(numOperations - 1))
                    ) {
                    // remove dummy operation
                    operationsList.remove(numOperations - 1);
                }
            }

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
                        TreeNode newNodeToAdd = new TreeNode(nodeUri);
                        newNodeToAdd.setFormat(format);
                        newNodeToAdd.setValue(fixType(value, format));
                        nodes.put(nodeUri, newNodeToAdd);
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


                    if (newNode) {
                        TreeNode newNodeToAdd = new TreeNode(nodeUri);
                        newNodeToAdd.setFormat(TreeNode.FORMAT_NODE);
                        nodes.put(nodeUri, newNodeToAdd);
                    } else {
                        TreeNode newNodeToAdd = new TreeNode(nodeUri);
                        newNodeToAdd.setFormat(format);
                        newNodeToAdd.setValue(fixType(value, format));
                        nodes.put(nodeUri, newNodeToAdd);
                    }

                    ( (ReplaceManagementOperation)operationToAdd).setNodes(nodes);

                } else if (operationName.equalsIgnoreCase("Copy")) {
                    operationToAdd = new CopyManagementOperation();
                    Map nodes = new HashMap();
                    nodes.put(nodeUri, value);
                    ((CopyManagementOperation)operationToAdd).setNodes(nodes);

                } else if (operationName.equalsIgnoreCase("Exec")) {
                    operationToAdd = new ExecManagementOperation();
                    Map nodes = new HashMap();
                    ((ExecManagementOperation)operationToAdd).setCorrelator(option1);
                    nodes.put(nodeUri, value);
                    ((ExecManagementOperation)operationToAdd).setNodes(nodes);

                } else if (operationName.equalsIgnoreCase("ShowAlertMessage")) {
                    operationToAdd = UserAlertManagementOperation.getDisplay(value, 10, 15);
                } else if (operationName.equalsIgnoreCase("ShowConfirmMessage")) {
                    operationToAdd = UserAlertManagementOperation.getConfirm(value, 10, 15, "No");
                }else if (operationName.equalsIgnoreCase("ShowInputMessage")) {
                    operationToAdd = UserAlertManagementOperation.getInput(value, 10, 15, "", 32,'A', 'T');
                }else if (operationName.equalsIgnoreCase("ShowChoiceMessage")) {                    String[] Options = new String[3];
                    Options[0] = option1;
                    Options[1] = option2;
                    Options[2] = option3;
                    operationToAdd = UserAlertManagementOperation.getChoice(value, Options, 10, 15, option1);
                }

            }

            if (operationToAdd != null) {
                operationsList.add(operationToAdd);
                //updateOperationsList();
            }

            numOperations = operationsList.size();
            if (numOperations > 0) {
                if (!DMDemoBean.isDummyOperation(operationsList.get(numOperations - 1)
                    )) {
                    operationsList.add(DMDemoBean.DUMMY_ALERT_OPERATION);
                }
            } else {
                operationsList.add(DMDemoBean.DUMMY_ALERT_OPERATION);
            }

        }
    }
    /**
     * Returns the operations results setting in the nodes the value get from the
     * operations list.
     * @return List
     */
    public List<ManagementOperationResult> getOperationsResultsWithValue() throws ManagementException {
        if (operationsResults == null) {
            return null;
        }

        int numResults = operationsResults.size();

        ManagementOperationResult operationResult = null;
        ManagementOperation       operation       = null;
        String commandName = null;
        for (int i=0; i<numResults; i++) {
            operationResult = (ManagementOperationResult)operationsResults.get(i);

            //
            // Get the operation
            //
            operation = operationsList.get(i);
            if (!(operation instanceof TreeManagementOperation)) {
                continue;
            }
            commandName = operationResult.getCommand();
            if (COMMAND_WITHOUT_VALUE_IN_RESPONSE.indexOf(commandName) != -1) {
               operationResult.setNodes(((TreeManagementOperation)(operation)).getNodes());
            }

            if (commandName.equalsIgnoreCase("Alert")) {
                if (! (operation instanceof UserAlertManagementOperation) ) {
                    throw new ManagementException("Error matching the results with the operation");
                }

                int alertCode = ((UserAlertManagementOperation)operation).getAlertCode();
                if (alertCode == AlertCode.DISPLAY) {
                    operationResult.setCommand("Display");
                } else if (alertCode == AlertCode.CONFIRM_OR_REJECT) {
                    operationResult.setCommand("Confirmation");
                } else if (alertCode == AlertCode.INPUT) {
                    operationResult.setCommand("Input");
                } else if (alertCode == AlertCode.SINGLE_CHOICE) {
                    operationResult.setCommand("Choice");
                } else {
                   operationResult.setCommand("Alert");
                }
            }

        }

        return operationsResults;
    }

    /**
     * Move the operation identify with the given index up in the list
     * @param index int
     */
    public void moveOperationUp(int index) {
        ManagementOperation opToMove = null;
        if (index > 0) {
            opToMove = operationsList.remove(index);
            operationsList.add(index - 1, opToMove);
        }

    }

    /**
     * Move the operation identify with the given index down in the list
     * @param index int
     */
    public void moveOperationDown(int index) {
        ManagementOperation opToMove = null;

        //
        // I sottract 2 because the last operations is the dummy alert
        //
        if (index < (operationsList.size() - 2)) {
            opToMove = operationsList.remove(index);
            operationsList.add(index + 1, opToMove);
        }
    }

    /**
     * Remove the operation identify with the given index
     * @param index int
     */
    public void removeOperation(int index) {
        if (operationsList == null) {
            return;
        }
        if (index != -1) {
            operationsList.remove(index);
        }
    }

    /**
     * Load a session
     * @param sessionId String
     * @throws ManagementException
     */
    public void loadGenericSession(String sessionId) throws ManagementException {

        if (log.isEnabled(Level.TRACE)) {
            log.trace("loadGenericSession with sessionId: " + sessionId);
        }

        String xmlOperationsResults = null;

        try {
            this.operationsList = readOperations(sessionId);

            this.operationsResults = readResults(sessionId);
        } catch (IOException e) {
            throw new ManagementException("Error loading the session", e);
        }

        if (log.isEnabled(Level.TRACE)) {
            log.trace("number of operations: " + operationsList.size());
        }

        if (log.isEnabled(Level.TRACE)) {
            log.trace("number of results: " + operationsResults.size());
        }
    }

    /**
     * Returns the sessionId
     * @return the sessionId
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Sets the sessionId
     * @param sessionId the sessionId to set
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Returns the operationsList
     * @return the operationsList. If it is null, returns a empty list
     */
    public List<ManagementOperation> getOperationsList() {
        if (operationsList == null) {
            return new ArrayList<ManagementOperation>();
        }
        return operationsList;
    }

    /**
     * Returns the operationsResults
     * @return the operationsResults. If this is null, returns a empty list
     */
    public List<ManagementOperationResult> getOperationsResults() {
        if (operationsResults == null) {
            return new ArrayList<ManagementOperationResult>();
        }
        return operationsResults;
    }

    /**
     * Preconfigures the operations list with a set of the operations based on the
     * given preconfiguredOperations.
     * @param preconfiguredOperations String
     */
    public void setPreconfiguredOperations(String preconfiguredOperations)

        throws ManagementException  {
        List<ManagementOperation> list = null;
        if (PRECONFIGURED_GET_DEVICE_DETAILS.equals(preconfiguredOperations)) {
            list = getPreconfiguredOperationsForGetDeviceDetail();
        } else {
            throw new ManagementException("Unknow preconfigured operations (" + preconfiguredOperations + ")");
        }
        this.operationsList = list;
    }

    /**
     * Returns a String representation of this class
     * @return String
     */
    public String toString() {
        ToStringBuilder sb = new ToStringBuilder(this);
        sb.append("sessionId", sessionId);
        sb.append("num.operations",
                  (operationsList == null) ? "null" : String.valueOf(operationsList.size()));
        sb.append("num.results",
                  (operationsResults == null) ? "null" : String.valueOf(operationsResults.size()));
        return sb.toString();
    }

    // --------------------------------------------------------- Private Methods

    /**
     * Search in the operationList the operation correspondent to the given
     * <code>ManagementOperationResult</code>. If found it, sets the nodes in the ManagementOperationResult
     * with the nodes in the operation because the nodes in operation contains also the
     * value.
     * @param operationResult ManagementOperationResult
     */
    private void setValuesInManagementOperationResult(ManagementOperationResult operationResult) {

        String commandName = operationResult.getCommand();
        Map resultNodes    = operationResult.getNodes();

        Iterator<ManagementOperation> itOperationsList     = operationsList.iterator();
        String operationCommandName   = null;
        Map<String, Object> operationNodes            = null;
        ManagementOperation operation = null;

        while (itOperationsList.hasNext()) {

            operation            = itOperationsList.next();
            operationCommandName = operation.getDescription();

            if (! (operation instanceof TreeManagementOperation)) {
                continue;
            }
            if (!operationCommandName.equals(commandName)) {
                continue;
            }

            if (!(operation instanceof UserAlertManagementOperation)) {
                operationNodes = ( (TreeManagementOperation)operation).getNodes();
                if (operationNodes.size() != resultNodes.size()) {
                    continue;
                }
                if (compareKeys(resultNodes, operationNodes)) {
                    //
                    // Found operation
                    //
                    operationResult.setNodes(operationNodes);
                }
            } else {

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
     * Load the session state.
     * @return String
     * @throws ManagementException
     */
    private String loadSessionState() throws ManagementException {
        if (log.isEnabled(Level.TRACE)) {
            log.trace("loadSessionState with sessionId: " + sessionId);
        }

        Connection con          = null;
        PreparedStatement pStmt = null;
        ResultSet rs            = null;

        String state = null;

        try {
            con  = datasource.getConnection();
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

    /**
     * Returns a list of the operations to get the following nodes:
     * <ui>
     * <li>./DevDetail/Hwv</li>
     * <li>./DevDetail/SwV</li>
     * <li>./DevDetail/FwV</li>
     * <li>./DevDetail/OEM</li>
     * <li>./DevDetail/DevTyp</li>
     * <li>./DevInfo/Lang</li>
     * <li>./DevInfo/DmV</li>
     * <li>./DevInfo/Mod</li>
     * <li>./DevInfo/Man</li>
     * </ui>
     * @return ArrayList
     */
    private List<ManagementOperation> getPreconfiguredOperationsForGetDeviceDetail() {
        List<ManagementOperation> operations = new ArrayList<ManagementOperation>();
        List<String> nodesList = new ArrayList<String>();

        nodesList.add("./DevInfo/Man");
        nodesList.add("./DevInfo/Mod");
        nodesList.add("./DevDetail/DevTyp");
        nodesList.add("./DevDetail/OEM");
        nodesList.add("./DevInfo/Lang");
        nodesList.add("./DevDetail/FwV");
        nodesList.add("./DevDetail/SwV");
        nodesList.add("./DevDetail/HwV");
        nodesList.add("./DevInfo/DmV");

        int numNodes = nodesList.size();
        GetManagementOperation getOp = null;
        Map<String, Serializable> nodes = null;
        for (int i=0; i<numNodes; i++) {
            getOp = new GetManagementOperation();
            nodes = new HashMap<String, Serializable>();
            nodes.put(nodesList.get(i), "");
            getOp.setNodes(nodes);
            operations.add(getOp);
        }
        return operations;
    }

    /**
     * Read the results file
     * @param dmStateId String
     * @return List
     * @throws IOException
     */
    private List<ManagementOperationResult> readResults(String dmStateId) throws IOException {
        File resultsFile = new File(sessionsDirectory +
                                    File.separator +
                                    dmStateId +
                                    File.separator +
                                    Constants.RESULTS_FILE_NAME);
        if (!resultsFile.exists()) {
            return new ArrayList<ManagementOperationResult>();
        }

        String xmlResults = IOTools.readFileString(resultsFile);
        if (xmlResults == null) {
            return new ArrayList<ManagementOperationResult>(); //  empty arraylist
        }
        XMLDecoder decoder = new XMLDecoder(
            new ByteArrayInputStream(xmlResults.getBytes())
                             );
        return (ArrayList<ManagementOperationResult>)decoder.readObject();
    }


    /**
     * Delete the directory where there are the operations and
     * the results files for the given dmStateId
     * @param dmStateId String
     * @throws IOException
     */
    private void deleteSessions(String dmStateId) throws IOException {
        File directory = new File(sessionsDirectory +
                                  File.separator +
                                  dmStateId);

        File[] files = directory.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (!files[i].delete()) {
                    throw new IOException("Error deleting file '"
                        + files[i].getAbsolutePath() + "'");
                }
            }
        }
        directory.delete();
    }

    /**
     * Save the operations on file system
     * @param resultsList List
     */
    private void saveOperations(List<ManagementOperation> operationsList, String id) {

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        XMLEncoder encoder = new XMLEncoder(bout);
        encoder.writeObject(operationsList);
        encoder.flush();
        encoder.close();

        //
        // Save results on filesystem
        //
        String directory = sessionsDirectory + File.separator + id;
        File resultsFileDir = new File(directory);
        resultsFileDir.mkdirs();

        File operationsFile = new File(directory +
                                    File.separator +
                                    Constants.OPERATIONS_FILE_NAME);

        try {
            IOTools.writeFile(bout.toByteArray(), operationsFile);
        } catch (Exception e) {
            log.debug( "saveOperations", e);
            if (log.isEnabled(Level.FATAL)) {
                log.fatal("Error saving device tree: " + e.getMessage());
            }
        }

    }

    /**
     * Read the operations file and returns the operations list.
     * @param dmStateId String
     * @return List
     * @throws IOException
     */
    private List<ManagementOperation> readOperations(String dmStateId) throws IOException {
        File operationsFile = new File(sessionsDirectory +
                                       File.separator +
                                       dmStateId +
                                       File.separator +
                                       Constants.OPERATIONS_FILE_NAME);

        if (log.isEnabled(Level.TRACE)) {
            log.trace("get operations for id: " + dmStateId);
        }

        if (!operationsFile.exists()) {
            return null;
        }

        String xmlOperations = IOTools.readFileString(operationsFile);

        if (xmlOperations == null) {
            return new ArrayList<ManagementOperation>();  // returns a empty arraylist
        }

        XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(xmlOperations.getBytes()));
        ArrayList<ManagementOperation> operationsList = (ArrayList<ManagementOperation>)decoder.readObject();

        return operationsList;
    }

    /**
     * Convert the given value in the right object according to the given type
     * @param value String
     * @param type String
     * @return Object
     */
    private Object fixType(String value, String type) {
        Object objValue = null;
        if (TreeNode.FORMAT_INT.equalsIgnoreCase(type)) {
            try {
                objValue = new Integer(value);
            } catch (NumberFormatException ex) {
                return value;
            }
            return objValue;
        } else if (TreeNode.FORMAT_BOOL.equalsIgnoreCase(type)) {
            objValue = new Boolean(value);
            return objValue;
        } else if (TreeNode.FORMAT_BINARY.equalsIgnoreCase(type)) {
            objValue = Base64.decode(value);
            return objValue;
        }

        return value;
    }

}
