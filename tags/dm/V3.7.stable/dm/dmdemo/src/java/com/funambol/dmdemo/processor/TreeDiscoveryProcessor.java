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

package com.funambol.dmdemo.processor;

import java.beans.XMLEncoder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Serializable;

import java.security.Principal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.sql.DataSource;

import com.funambol.framework.core.Alert;
import com.funambol.framework.core.Util;
import com.funambol.framework.core.dm.ddf.DevInfo;
import com.funambol.framework.engine.dm.DeviceDMState;
import com.funambol.framework.engine.dm.GetManagementOperation;
import com.funambol.framework.engine.dm.LegacyManagementProcessorAdapter;
import com.funambol.framework.engine.dm.ManagementException;
import com.funambol.framework.engine.dm.ManagementOperation;
import com.funambol.framework.engine.dm.ManagementOperationResult;
import com.funambol.framework.engine.dm.TreeNode;
import com.funambol.framework.engine.dm.UserAlertManagementOperation;
import com.funambol.framework.tools.DBTools;
import com.funambol.framework.tools.IOTools;

import com.funambol.dmdemo.Constants;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;


/**
 * This processor discover the tree structure starting with a root node
 *
 *
 * @version $Id: TreeDiscoveryProcessor.java,v 1.6 2007-06-18 16:38:45 luigiafassina Exp $
 */
public class TreeDiscoveryProcessor
extends LegacyManagementProcessorAdapter implements java.io.Serializable {

    // --------------------------------------------------------------- Constants

    // ------------------------------------------------------- Private Constants

    private static final String SQL_UPDATE_DEVICE_TREE = "UPDATE fnbl_treediscovery_processor SET results=? WHERE id=?";
    private static final String SQL_SELECT_ROOT_NODE   = "SELECT root_node FROM fnbl_treediscovery_processor WHERE id=?";

    // ------------------------------------------------------------ Private data

    private DataSource datasource = null;
    private String sessionId      = null;
    private DeviceDMState dmState = null;

    private final Logger log = Logger.getLogger(com.funambol.dmdemo.processor.TreeDiscoveryProcessor.class.getName());

    private ArrayList<String> nodesList        = null;
    private java.util.Map<String, Object> nodesResults = null;
    private String rootNode            = null;

    private boolean dummyAlertAlreadySent = false;

    private String resultsDirectory = null;

    // -------------------------------------------------------------- Properties

    /**
     * Management processor name
     */
    private String name;

    /**
     * Sets management processor name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @see ManagementProcesessor
     */
    public String getName() {
        return name;
    }

    /**
      * The JNDI name of the datasource to be used
      */
     private String jndiDataSourceName = null;

     public String getJndiDataSourceName() {
         return this.jndiDataSourceName;
     }

     public void setJndiDataSourceName(String jndiDataSourceName) throws ManagementException {
         this.jndiDataSourceName = jndiDataSourceName;

         if (jndiDataSourceName == null) {
             datasource = null;
         }

         try {
             InitialContext ctx = new InitialContext();
             datasource = (DataSource) ctx.lookup(jndiDataSourceName);
         } catch (NamingException e) {
             throw new ManagementException("Data source "
             + jndiDataSourceName
             + " not found"
             , e
             );
         }
     }


    // ------------------------------------------------------------ Constructors

    /** Creates a new instance of TreeDiscoveryProcessor */
    public TreeDiscoveryProcessor() {
        resultsDirectory = System.getProperty(Constants.SYSTEM_PROPERTY_RESULTS_DIRECTORY);
        if (resultsDirectory == null) {
            resultsDirectory = ".";
        }
    }

    // ----------------------------------------------------- ManagementProcessor
    /**
     * Creates and initializes.
     *
     * @see ManagementProcesessor
     */
    public void beginSession(String        sessionId,
                             Principal     p        ,
                             int           type     ,
                             DevInfo       devInfo  ,
                             DeviceDMState dmState  )
    throws ManagementException {
        if (log.isEnabled(Level.DEBUG)) {
            log.debug("Starting a new DM management session");
            log.debug("sessionId: " + sessionId          );
            log.debug("principal: " + p                  );
            log.debug("type: "      + type               );
            log.debug("deviceId: "  + devInfo            );
        }

        this.nodesList    = new ArrayList<String>();
        this.nodesResults = new TreeMap<String, Object>();
        this.sessionId    = sessionId;
        this.dmState      = dmState;

        this.rootNode     = getRootNode(dmState.id);

        if (this.rootNode != null && !this.rootNode.equals("")) {
            this.nodesList.add(this.rootNode);
        }
    }

    /**
     * @see ManagementProcessor
     */
    void setGenericAlerts(Alert[] genericAlerts){
        int numAlerts = genericAlerts.length;
        log.info("SetGenericAlerts (num. alerts: " + numAlerts + ")");

        for (int i=0; i<numAlerts; i++) {
            log.info("Alert[" + i + "]: " + Util.toXML(genericAlerts[i]));
        }
    }

    /**
     * @see ManagementProcessor
     */
    public void endSession(int completionCode) throws ManagementException {
        saveResults();

        if (log.isEnabled(Level.DEBUG)) {
            log.debug("End a DM management session with sessionId: " + sessionId);
        }
    }

    /**
     * @see ManagementProcessor
     */
    public ManagementOperation[] getNextOperations() throws ManagementException {

        ArrayList<ManagementOperation> operationsList         = new ArrayList<ManagementOperation>();
        ManagementOperation[] operations = null;

        Map<String, Serializable> nodes = new HashMap<String, Serializable>();

        int numNodes = nodesList.size();

        if (numNodes == 0) {
            if (!dummyAlertAlreadySent) {
                // Dummy alert operation

                //
                // Some phone doesn't recognise the Alert command and a
                // TreeDiscovery session fails because the phone doesn't send
                // the last message.
                // As workaround, we add also a Get operation so the phone sends
                // the last message.
                //
                GetManagementOperation dummyGet = new GetManagementOperation();
                Map<String, Serializable> dummyNode = new HashMap<String, Serializable>();
                dummyNode.put("./DevInfo/Man", "");
                dummyGet.setNodes(dummyNode);
                operations = new ManagementOperation[] {
                             dummyGet,
                             UserAlertManagementOperation.getDisplay("Funambol DM Demo", 5, 15)
                };
                dummyAlertAlreadySent = true;
            } else {
                operations = new ManagementOperation[0];

            }
            return operations;
        }

        ManagementOperation o = null;

        for (int i = 0; i < numNodes; i++) {

            if ( ((i % 10) == 0) ) {
                if (i != 0) {
                    ((GetManagementOperation)o).setNodes(nodes);
                }
                o = new GetManagementOperation();
                operationsList.add(o);
                nodes = new HashMap<String, Serializable>();
            }

            nodes.put( nodesList.get(i), "");
        }

        ((GetManagementOperation)o).setNodes(nodes);

        operations = operationsList.toArray(new ManagementOperation[0]);

        return operations;
    }

    /**
     * @see ManagementProcesessor
     */
    public void setOperationResults(ManagementOperationResult[] results)
        throws ManagementException {

        String id = dmState.id;

        if (log.isEnabled(Level.TRACE)) {
            log.trace("setOperationResults for id: " + id);
        }

        int numResults = results.length;
        nodesList = new ArrayList<String>();

        if (!dummyAlertAlreadySent) {
            for (int i = 0; i < numResults; i++) {
                nodesList.addAll(getNodesList(results[i].getNodes()));
            }
        }

        if (nodesList.isEmpty()) {
            if (log.isEnabled(Level.TRACE)) {
                log.trace("No other nodes to get");
            }
            //
            // Session completed
            //
            dmState.state = DeviceDMState.STATE_COMPLETED;
        }

    }
    // --------------------------------------------------------- Private Methods

    private String getRootNode(String id) {

        if (log.isEnabled(Level.TRACE)) {
            log.trace("get root node for id: " + id);
        }

        Connection con          = null;
        PreparedStatement pStmt = null;
        ResultSet rs            = null;
        String rootNode         = null;
        try {
            con = datasource.getConnection();
            pStmt = con.prepareStatement(SQL_SELECT_ROOT_NODE);
            pStmt.setString(1, id);
            rs = pStmt.executeQuery();

            while (rs.next()) {
                rootNode = rs.getString(1);
            }
        } catch (SQLException ex) {
            if (log.isEnabled(Level.FATAL)) {
                log.fatal("Error reading root node: " + ex.getMessage());
            }
        } finally {
            DBTools.close(con, pStmt, rs);
        }

        if (log.isEnabled(Level.TRACE)) {
            log.trace("rootNode: " + rootNode);
        }

        return rootNode;
    }

    /**
     * Save node results on file system
     */
    private void saveResults() {

        String id = dmState.id;

        if (log.isEnabled(Level.TRACE)) {
            log.trace("save results for id: " + id);
        }

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        XMLEncoder encoder = new XMLEncoder(bout);
        encoder.writeObject(nodesResults);
        encoder.flush();
        encoder.close();

        //
        // Save results on filesystem
        //
        String directory = resultsDirectory + File.separator + id;
        File resultsFileDir = new File(directory);
        resultsFileDir.mkdirs();

        File resultsFile = new File(directory +
                                    File.separator +
                                    Constants.RESULTS_FILE_NAME);

        try {
            IOTools.writeFile(bout.toByteArray(), resultsFile);
        } catch (Exception e) {
            log.debug( "saveResults", e);
            if (log.isEnabled(Level.FATAL)) {
                log.fatal("Error saving device tree: " + e.getMessage());
            }
        }

        dmState.state = DeviceDMState.STATE_COMPLETED;
    }


    private ArrayList<String> getNodesList(Map nodes) {
        ArrayList<String> nodesList = new ArrayList<String>();
        Iterator it  = nodes.keySet().iterator();
        String key   = null;
        Object value = null;
        StringTokenizer st = null;
        String format = null;
        String temp   = null;
        while (it.hasNext()) {
            key = (String)it.next();
            value = nodes.get(key);
            if (value instanceof TreeNode) {
                format = ((TreeNode)value).getFormat();
                if (format.equalsIgnoreCase(TreeNode.FORMAT_NODE)) {
                    value = ((TreeNode)value).getValue();
                    st = new StringTokenizer((String)value, "/");
                    while (st.hasMoreTokens()) {
                        temp = key + "/" + st.nextToken();
                        nodesList.add(temp);
                        nodesResults.put(temp, "");
                    }
                } else {
                    nodesResults.put(key, value);
                }
            } else {
                nodesResults.put(key, value);
            }
        }
        return nodesList;
    }

}
