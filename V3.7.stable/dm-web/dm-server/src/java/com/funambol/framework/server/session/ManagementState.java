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
package com.funambol.framework.server.session;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;

import com.funambol.framework.core.AbstractCommand;
import com.funambol.framework.core.Alert;
import com.funambol.framework.core.AlertCode;
import com.funambol.framework.core.Cred;
import com.funambol.framework.core.DevInf;
import com.funambol.framework.core.Replace;
import com.funambol.framework.core.Status;
import com.funambol.framework.core.VerProto;
import com.funambol.framework.engine.dm.DeviceDMState;
import com.funambol.framework.engine.dm.ManagementCommandDescriptor;
import com.funambol.framework.security.SecurityConstants;
import com.funambol.framework.security.Sync4jPrincipal;
import com.funambol.framework.server.SyncTimestamp;
import com.funambol.framework.server.Sync4jDevice;
import com.funambol.framework.protocol.ProtocolUtil;

/**
 * This class keeps state information of a device management session.
 *
 * @version $Id: ManagementState.java,v 1.6 2007-06-19 08:16:16 luigiafassina Exp $
 */
public class ManagementState
implements SecurityConstants {

    // --------------------------------------------------------------- Constants

    // ------------------------------------------------------------ Private data

    /**
     * Cache AbstractCommands (Alert, Put, Get)
     */
    private ArrayList<AbstractCommand> clientCommands;

    // ------------------------------------------------------------- Public data

    /**
     * Cache the devInf replace command
     */
    public Replace devInfReplaceCommand;

    /**
     * Cache the device info
     */
    public DevInf deviceInfo;

    /**
     * Cache the Sync4jDevice object (it contains Funambol information and nonces)
     */
    public Sync4jDevice device;

    /**
     * Cache the device DM state
     */
    public DeviceDMState dmstate;

    /**
     * The management session id
     */
    public String sessionId;

    /**
     * Authenticated credential. If null no credential has authenticated.
     */
    public Cred loggedCredential;

    /**
     * Authenticated principal. If null, no principal has authenticated
     */
    public Sync4jPrincipal loggedPrincipal;

    /**
     * The Last message Id from the client
     */
    public String lastMsgIdFromClient;

    /**
     * Timestamps of the last management session
     */
    public SyncTimestamp lastTimestamp;

    /**
     * Timestamps of the current management session
     */
    public SyncTimestamp nextTimestamp;

    /**
     * Authentication state. One of the following values:
     * <ul>
     *   <li>AUTH_UNAUTHENTICATED</li>
     *   <li>AUTH_MISSING_CREDENTIALS</li>
     *   <li>AUTH_INVALID_CREDENTIALS</li>
     *   <li>AUTH_AUTHENTICATED</li>
     * </ul>
     */
    public int authenticationState;

    /**
     * Server authentication state. One of the following values:
     * <ul>
     *   <li>AUTH_UNAUTHENTICATED</li>
     *   <li>AUTH_RETRY_1</li>
     *   <li>AUTH_RETRY_2</li>
     * </ul>
     */
    public int serverAuthenticationState;

    /**
     * Management session type (@see AlertCode)
     */
    public int type;

    /**
     * Has the session been started ?
     */
    public boolean started;


    /**
     * Cache the SyncML Protocol version used by device
     */
    public VerProto syncMLVerProto;

    /**
     * Cache of ManagementCommandDescriptor
     * The key of this map is msgId|cmdId. The value is a commandDescriptor.
     * So for example, if the command 2 in the messege 3 is an Add for the
     * source uri 'contact', the map will contain:
     * key: 3|2
     * value: the command descriptor
     */
    public java.util.Map<String, ManagementCommandDescriptor> managementCommandDescriptors = new HashMap<String, ManagementCommandDescriptor>();

    /**
     * Cache the maximum message size
     */
    private long maxMsgSize = 0;
    public void setMaxMsgSize(long value) {
        maxMsgSize = value;
    }

    public long getMaxMsgSize() {
        return maxMsgSize;
    }

    /**
     * Cache the maximum object size
     */
    private long maxObjSize = 0;
    public long getMaxObjSize() {
        return this.maxObjSize;
    }
    public void setMaxObjSize(long maxObjSize) {
        this.maxObjSize = maxObjSize;
    }


    /**
     * Cache overhead for SyncHdr object
     */
    private long overheadHdr = 0;
    public long getOverheadHdr() {
        return this.overheadHdr;
    }
    public void setOverheadHdr(long overheadHdr) {
        this.overheadHdr = overheadHdr;
    }


    /**
     * Cache the Status command presents in the response message
     */
    private List<Object> listStatusCmdOut = new ArrayList<Object>();

    public void addStatusCmdOut(List<Object> statusList) {
        listStatusCmdOut.addAll(statusList);
    }

    public List<Object> getStatusCmdOut() {
        return listStatusCmdOut;
    }

    public void removeStatusCmdOut(List<Status> statusCommand) {
        listStatusCmdOut.removeAll(statusCommand);
    }


    /**
     * Cache the Alert command presents in the response message
     */
    private List<Alert> listAlertCmdOut = new ArrayList<Alert>();

    public void addAlertCmdOut(List<Alert> alertList) {
        listAlertCmdOut.addAll(alertList);
    }

    public List<Alert> getAlertCmdOut() {
        return listAlertCmdOut;
    }

    public void removeAlertCmdOut(List<Alert> alertList) {
        listAlertCmdOut.removeAll(alertList);
    }

    /**
     * Cache the Generic Alert commands
     */
    private List<Alert> listGenericAlert = new ArrayList<Alert>();

    public void addGenericAlert(Alert[] alertsToCache) {
        if (alertsToCache != null) {
            listGenericAlert.addAll(Arrays.asList(alertsToCache));
        }
    }

    public Alert[] getGenericAlert() {
        return listGenericAlert.toArray(new Alert[listGenericAlert.size()]);
    }

    public void clearGenericAlert() {
        listGenericAlert.clear();
    }

    /**
     * Cache cmd splitted in more message (Large Object)
     */
    private AbstractCommand splittedCommand = null;

    public AbstractCommand getSplittedCommand() {
        return splittedCommand;
    }

    public void setSplittedCommand(AbstractCommand splittedCommand) {
        this.splittedCommand = splittedCommand;
    }


    /**
     * Cache received large object
     */
    private String receivedLargeObject = null;

    public String getReceivedLargeObject() {
        return receivedLargeObject;
    }

    public void setReceivedLargeObject(String receivedLargeObject) {
        this.receivedLargeObject = receivedLargeObject;
    }

    /**
     * Cache size of received large object
     */
    private Long sizeOfReceivedLargeObject = null;

    public void setSizeOfReceivedLargeObject(Long size) {
        this.sizeOfReceivedLargeObject = size;
    }

    public Long getSizeOfReceivedLargeObject() {
        return sizeOfReceivedLargeObject;
    }


    /**
     * Cache data to send in next messages (Large Object)
     */
    private String nextDataToSend = null;

    public String getNextDataToSend() {
        return nextDataToSend;
    }

    public void setNextDataToSend(String nextDataToSend) {
        this.nextDataToSend = nextDataToSend;
    }



    /**
     * Cache the AbstractCommand presents in the response message
     */
    private List<Object> listCmdOut = new ArrayList<Object>();

    public void addCmdOut(List<Object> cmdList) {
        listCmdOut.addAll(cmdList);
    }

    public void addCmdOut(int index, List<Object> cmdList) {
         listCmdOut.addAll(index, cmdList);
     }

    public List<Object> getCmdOut() {
        return listCmdOut;
    }

    public void removeCmdOut(List<Object> abstractCommandsList) {
        listCmdOut.removeAll(abstractCommandsList);
    }

    public void removeCmdOut(AbstractCommand abstractCommand) {
        listCmdOut.remove(abstractCommand);
    }


    /**
     * Cache overhead of Status for SyncHdr
     */
    private long sizeStatusSyncHdr = 0;
    public long getSizeStatusSyncHdr() {
        return this.sizeStatusSyncHdr;
    }

    public void setSizeStatusSyncHdr(long sizeStatusSyncHdr) {
         this.sizeStatusSyncHdr = sizeStatusSyncHdr;
     }

     /**
      * Cache Status for SyncHdr
      */
     private Status statusSyncHdr;
     public void setStatusSyncHdr(Status statusSyncHdr) {
         this.statusSyncHdr = statusSyncHdr;
     }

     public Status getStatusSyncHdr() {
         return this.statusSyncHdr;
     }


    // ------------------------------------------------------------ Constructors

    /**
     * Creates a new management state object associated to the given session id
     *
     * @param sessionId the sessionId
     */
    public ManagementState(String sessionId) {
        this();
        this.sessionId = sessionId;
    }

    /**
     * Creates a new management state object with empty state and not associated
     * to any session id.
     */
    public ManagementState() {
        reset();
    }



    // ---------------------------------------------------------- Public Methods

    public void addClientCommands(AbstractCommand[] commands) {
        for (int i=0; ((commands != null) && (i<commands.length)); i++) {
            if (ProtocolUtil.hasLargeObject(commands[i])) {
                // commands with large object don't come cached
                continue;
            }
            clientCommands.add(commands[i]);
        }
    }

    public AbstractCommand[] getClientCommands() {
        return clientCommands.toArray(new AbstractCommand[clientCommands.size()]);
    }

    public void clearClientCommands() {
        clientCommands.clear();
    }


    /**
     * Sets sessionId
     *
     * @param sessionId the management session id
     */
    protected void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Returns the management session id
     *
     * @return the management session id
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Reset this object to an initial state
     */
    public void reset() {
        loggedCredential    = null;
        loggedPrincipal     = null;
        lastMsgIdFromClient = null;
        sessionId           = null;
        lastTimestamp       = null;
        nextTimestamp       = null;

        started             = false;

        type                = AlertCode.UNKNOWN;

        authenticationState = AUTH_UNAUTHENTICATED;

        serverAuthenticationState = AUTH_UNAUTHENTICATED;

        clientCommands = new ArrayList<AbstractCommand>();
    }
}
