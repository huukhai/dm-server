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


import java.beans.XMLDecoder;
import java.beans.XMLEncoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import java.security.Principal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.sql.DataSource;

import com.funambol.dmdemo.Constants;

import com.funambol.framework.core.Alert;
import com.funambol.framework.core.Util;
import com.funambol.framework.core.dm.ddf.DevInfo;
import com.funambol.framework.engine.dm.DeviceDMState;
import com.funambol.framework.engine.dm.LegacyManagementProcessorAdapter;
import com.funambol.framework.engine.dm.ManagementException;
import com.funambol.framework.engine.dm.ManagementOperation;
import com.funambol.framework.engine.dm.ManagementOperationResult;
import com.funambol.framework.tools.IOTools;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;


/**
 *
 *
 * @version $Id: GenericProcessor.java,v 1.5 2007-06-18 16:38:45 luigiafassina Exp $
 */
public class GenericProcessor
extends LegacyManagementProcessorAdapter implements java.io.Serializable {

    // --------------------------------------------------------------- Constants

    // ------------------------------------------------------- Private Constants

    // ---------------------------------------------------------- Protected data

    protected DataSource datasource = null;
    protected String sessionId      = null;
    protected DeviceDMState dmState = null;

    protected String sessionsDirectory = null;

    protected final Logger log = Logger.getLogger(com.funambol.dmdemo.processor.GenericProcessor.class.getName());

    protected int step = 0;

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

    /** Creates a new instance of GenericProcessor */
    public GenericProcessor() {
        sessionsDirectory = System.getProperty(Constants.SYSTEM_PROPERTY_RESULTS_DIRECTORY);
        if (sessionsDirectory == null) {
            sessionsDirectory = ".";
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
        this.sessionId = sessionId;
        this.dmState   = dmState;
    }

    /**
     * @see ManagementProcessor
     */
    public void endSession(int completionCode) throws ManagementException {
        if (log.isEnabled(Level.DEBUG)) {
            log.debug("End a DM management session with sessionId: " + sessionId);
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
    public ManagementOperation[] getNextOperations() throws ManagementException {
        ManagementOperation[] operations = null;

        if (step == 0) {
            try {
                operations =
                    (ManagementOperation[])readOperations(dmState.id).toArray(new
                    ManagementOperation[0]);
            } catch (IOException ex) {
                throw new ManagementException("Error reading the operations", ex);
            }
        } else {
            operations = new ManagementOperation[0];
        }
        return operations;
    }

    /**
     * @see ManagementProcesessor
     */
    public void setOperationResults(ManagementOperationResult[] results)
        throws ManagementException {

        step++;

        String id = dmState.id;

        if (log.isEnabled(Level.TRACE)) {
            log.trace("update results for id: " + id);
        }

        List<ManagementOperationResult> resultsList = new ArrayList<ManagementOperationResult>(Arrays.asList(results));

        saveResults(resultsList, id);

        dmState.state = DeviceDMState.STATE_COMPLETED;
    }

    // --------------------------------------------------------- Private Methods

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
     * Save the results on file system
     * @param resultsList List
     */
    private void saveResults(List<ManagementOperationResult> resultsList, String id) {

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        XMLEncoder encoder = new XMLEncoder(bout);
        encoder.writeObject(resultsList);
        encoder.flush();
        encoder.close();

        //
        // Save results on filesystem
        //
        String directory = sessionsDirectory + File.separator + id;
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
                log.fatal("Error saving results: " + e.getMessage());
            }
        }
    }
}
