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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.sql.DataSource;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.funambol.framework.engine.dm.ManagementException;
import com.funambol.framework.tools.DBTools;
import com.funambol.framework.tools.IOTools;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;


/**
 * Contains the supported methods for tree discovery management.
 *
 *
 * @version $Id: TreeDiscoveryProcessorTools.java,v 1.5 2007-06-18 16:38:44 luigiafassina Exp $
 */
public class TreeDiscoveryProcessorTools {

    // ---------------------------------------------------------------- Contants

    private static final String SQL_INSERT_TREE_DISCOVERY_SESSION =
        "INSERT INTO fnbl_treediscovery_processor (id,root_node) values (?,?)";

    private static final String SQL_UPDATE_TREE_DISCOVERY_SESSION =
        "UPDATE fnbl_treediscovery_processor set root_node=? WHERE id=?";

    private static final String SQL_SELECT_TREE_DISCOVERY_SESSION =
        "SELECT id, root_node FROM fnbl_treediscovery_processor WHERE id=? ";

    private static final String SQL_DELETE_TREE_DISCOVERY =
        "DELETE FROM fnbl_treediscovery_processor WHERE id=?";

    // ------------------------------------------------------------ Private data
    private static final Logger log = Logger.getLogger(com.funambol.dmdemo.TreeDiscoveryProcessorTools.class.getName());

    private DataSource datasource = null;

    private String rootNode = null;
    private Map treeDiscoveryResults = null;
    private String sessionId = null;

    private String resultsDirectory = null;

    // ------------------------------------------------------------ Constructors
    private TreeDiscoveryProcessorTools() {
        resultsDirectory = System.getProperty(Constants.SYSTEM_PROPERTY_RESULTS_DIRECTORY);
        if (resultsDirectory == null) {
            resultsDirectory = ".";
        }
    }

    public TreeDiscoveryProcessorTools(DataSource ds) {
        this();
        this.datasource = ds;
    }

    // ---------------------------------------------------------- Public Methods

    /**
     * Returns the map of the results
     * @return the map of the results
     */
    public Map getTreeDiscoveryResults() {
        if (treeDiscoveryResults == null) {
            return new HashMap();
        }
        return treeDiscoveryResults;
    }

    /**
     * Start a new management session
     * @param deviceId String
     * @param sessionId String
     * @throws ManagementException
     */
    public void startNewManagementSession(String deviceId, String sessionId) throws
        ManagementException {

        this.sessionId            = sessionId;
        this.rootNode             = null;
        this.treeDiscoveryResults = null;
    }

    /**
     * Delete the session
     * @throws ManagementException
     */
    public void deleteSession() throws ManagementException {
        if (sessionId == null) {
            return;
        }

        Connection con          = null;
        PreparedStatement pStmt = null;
        ResultSet rs            = null;

        try {
            con   = datasource.getConnection();
            pStmt = con.prepareStatement(SQL_DELETE_TREE_DISCOVERY);

            pStmt.setObject(1, sessionId);

            pStmt.executeUpdate();

            //
            // We must delete also the results on thfe filesystem
            //
            deleteResults(sessionId);

        } catch (Exception e) {
            throw new ManagementException("Error deleting the session", e);
        } finally {
            DBTools.close(con, pStmt, rs);
        }


    }

    /**
     * Save the session
     * @throws ManagementException
     */
    public void saveSession() throws ManagementException {
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

    /**
     * Load the session with the given sessionId
     * @param sessionId String
     * @throws ManagementException
     */
    public void loadTreeSession(String sessionId) throws ManagementException {
        if (log.isEnabled(Level.TRACE)) {
            log.trace("loadTreeSession with sessionId: " + sessionId);
        }

        Connection con           = null;
        PreparedStatement pStmt  = null;
        ResultSet rs             = null;

        String xmlOperationsResults = null;
        try {
            con   = datasource.getConnection();
            pStmt = con.prepareStatement(SQL_SELECT_TREE_DISCOVERY_SESSION);
            pStmt.setObject(1, sessionId);

            rs = pStmt.executeQuery();

            //
            // Gets the sessions list
            //
            while (rs.next()) {
                this.rootNode = rs.getString("root_node");
            }

            //
            // Read the results from filesystem
            //
            xmlOperationsResults = readResults(sessionId);

        } catch (Exception e) {
            throw new ManagementException("Error reading the session operations", e);
        } finally {
            DBTools.close(con, pStmt, rs);
        }

        if (xmlOperationsResults == null) {
            this.treeDiscoveryResults = new TreeMap();
        } else {
            XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(xmlOperationsResults.
                getBytes()));
            this.treeDiscoveryResults = (Map)decoder.readObject();
        }

        if (log.isEnabled(Level.TRACE)) {
            log.trace("number of results: " + treeDiscoveryResults.size());
        }
    }


    /**
      * Returns the root node
      * @return the root node
      */
     public String getRootNode() {
         return rootNode;
     }

     /**
      * Sets the root node
      * @param rootNode the root node to set
      */
     public void setRootNode(String rootNode) {
         if (rootNode != null) {
             //
             // Removing the '/' at the end of the root node
             //
             while (rootNode.endsWith("/")) {
                 rootNode = rootNode.substring(0, rootNode.length() - 1);
             }
         }
         this.rootNode = rootNode;
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


    public String toString() {
        ToStringBuilder sb = new ToStringBuilder(this);
        sb.append("sessionId", sessionId);
        sb.append("rootNode", rootNode);
        sb.append("num.results treeDiscovery",
                  (treeDiscoveryResults == null) ? "null" :
                  String.valueOf(treeDiscoveryResults.size()));
        return sb.toString();
    }

    // --------------------------------------------------------- Private Methods
    private int updateTreeDiscoverySession(String rootNode) throws ManagementException {

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
            throw new ManagementException("Error updating the treeDiscoverySession with session: " +
                                          sessionId, e);
        } finally {
            DBTools.close(con, pStmt, rs);
        }
        return numUpdated;
    }

    private void insertTreeDiscoverySession(String rootNode) throws ManagementException {

        if (sessionId == null) {
            return;
        }

        Connection con = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;

        try {
            //
            // insert new tree discovery session
            //
            con = datasource.getConnection();
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

    /**
     * Delete the directory where there is the results file for the given dmStateId
     * @param dmStateId String
     * @throws IOException
     */
    private void deleteResults(String dmStateId) throws IOException {
        File directory = new File(resultsDirectory +
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
     * Read the results file. Returns <code>null</code> if the file doesn't exist
     * @param dmStateId String
     * @return String
     * @throws IOException
     */
    private String readResults(String dmStateId) throws IOException {
        File resultsFile = new File(resultsDirectory +
                                    File.separator +
                                    dmStateId +
                                    File.separator +
                                    Constants.RESULTS_FILE_NAME);
        if (!resultsFile.exists()) {
            return null;
        }


        return IOTools.readFileString(resultsFile);
    }
}
