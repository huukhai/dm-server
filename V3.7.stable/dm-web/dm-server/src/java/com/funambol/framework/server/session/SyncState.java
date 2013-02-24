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

import com.funambol.framework.core.AbstractCommand;
import com.funambol.framework.core.Alert;
import com.funambol.framework.core.Cred;
import com.funambol.framework.core.Map;
import com.funambol.framework.core.Status;
import com.funambol.framework.core.Sync;
import com.funambol.framework.core.SyncML;
import com.funambol.framework.security.SecurityConstants;
import com.funambol.framework.security.Sync4jPrincipal;
import com.funambol.framework.server.Sync4jDevice;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;



/**
 * This class represents the state of the Syncronization process because cache
 * all informations that could be used in that process.
 * It is set during the Initialization process.
 *
 * @version $Id: SyncState.java,v 1.2 2006/08/07 21:09:22 nichele Exp $
 */
public class SyncState
implements SecurityConstants {

    // ------------------------------------------------------------- Public data

    /**
     * Cache the Sync4jDevice object (it contains Funambol information and nonces)
     */
    public Sync4jDevice device;

    /**
     * Authenticated credential. If null no credential has authenticated.
     */
    public Cred loggedCredential;

    /**
     * Authenticated principal. If null, no principal has authenticated
     */
    public Sync4jPrincipal loggedPrincipal;

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
     * Has the session been started ?
     */
    public boolean started;

    /**
     * Cache the SyncML Protocol version used by device
     */
    public String syncMLVerProto;

    // ---------------------------------------------------------- Public Methods
    public SyncState() {
        reset();
    }

    /**
     * Reset this object to an initial state
     */
    public void reset() {
        device                    = null;
        loggedCredential          = null;
        loggedPrincipal           = null;
        authenticationState       = AUTH_UNAUTHENTICATED;
        serverAuthenticationState = AUTH_UNAUTHENTICATED;
        started                   = false;
        syncMLVerProto            = null;
    }

    /**
     * Cache the AlertCommand
     */
    private ArrayList<Alert> listClientAlerts = new ArrayList<Alert>();

    public void addClientAlerts(Alert[] clientAlerts) {
        for (int i=0; ((clientAlerts != null) && (i<clientAlerts.length)); i++) {
            listClientAlerts.add(clientAlerts[i]);
        }
    }

    public void resetClientAlerts() {
        listClientAlerts = new ArrayList<Alert>();
    }

    public void removeClientAlert(Alert alert) {
        listClientAlerts.remove(alert);
    }

    public Alert[] getClientAlerts() {
        return listClientAlerts.toArray(new Alert[listClientAlerts.size()]);
    }

    /**
     * Cache the MapCommand
     */
    private ArrayList<Map> listMapCommands = new ArrayList<Map>();

    public void addMapCommands(Map[] mapCommands) {
        for (int i=0; ((mapCommands != null) && (i<mapCommands.length)); i++) {
            listMapCommands.add(mapCommands[i]);
        }
    }

    public Map[] getMapCommands() {
        return listMapCommands.toArray(new Map[listMapCommands.size()]);
    }

    /**
     * Cache the SyncCommand
     */
    private ArrayList<Sync> listSyncCommands = new ArrayList<Sync>();

    public void addSyncCommands(Sync[] syncCommands) {
        for (int i=0; ((syncCommands != null) && (i<syncCommands.length)); i++) {
            listSyncCommands.add(syncCommands[i]);
        }
    }

    public Sync[] getSyncCommands() {
        return listSyncCommands.toArray(new Sync[listSyncCommands.size()]);
    }

    /**
     * Cache the server modification commands: in the case of multimessage these
     * commands must be sent only when the client has sent its package
     */
    private ArrayList<AbstractCommand> listServerModifications = new ArrayList<AbstractCommand>();

    public void setServerModifications(AbstractCommand[] serverModifications) {
        for (int i=0; ((serverModifications != null) && (i<serverModifications.length)); i++) {
            listServerModifications.add(serverModifications[i]);
        }
    }

    public AbstractCommand[] getServerModifications() {
        return listServerModifications.toArray(new AbstractCommand[listServerModifications.size()]);
    }

    /**
     * Cache the Status command presents in the response message
     */
    private LinkedList listStatusCmdOut = new LinkedList();

    public void addStatusCmdOut(List statusList) {
        if (statusList != null) {
            listStatusCmdOut.addAll(statusList);
        }
    }

    public LinkedList getStatusCmdOut() {
        return listStatusCmdOut;
    }

    public void removeStatusCmdOut(List statusCommand) {
        if (statusCommand != null) {
            listStatusCmdOut.removeAll(statusCommand);
        }
    }

    /**
     * Cache the Status command for Map presents in the response message
     */
    private LinkedList listMapStatusOut = new LinkedList();

    public void addMapStatusOut(List mapStatusList) {
        if (mapStatusList != null) {
            listMapStatusOut.addAll(mapStatusList);
        }
    }

    public LinkedList getMapStatusOut() {
        return listMapStatusOut;
    }

    public void removeMapStatusOut(List mapStatus) {
        if (mapStatus != null) {
            listMapStatusOut.removeAll(mapStatus);
        }
    }

    /**
     * Cache the Alert command presents in the response message
     */
    private LinkedList<AbstractCommand> listAlertCmdOut = new LinkedList<AbstractCommand>();

    public void addAlertCmdOut(List<AbstractCommand> alertList) {
        if (alertList != null) {
            listAlertCmdOut.addAll(alertList);
        }
    }

    public LinkedList<AbstractCommand> getAlertCmdOut() {
        return listAlertCmdOut;
    }

    public void removeAlertCmdOut(List alertList) {
        if (alertList != null) {
            listAlertCmdOut.removeAll(alertList);
        }
    }

    /**
     * Cache the AbstractCommand presents in the response message
     */
    private LinkedList listCmdOut = new LinkedList();

    public void addCmdOut(List cmdList) {
        if (cmdList != null) {
            listCmdOut.addAll(cmdList);
        }
    }

    public LinkedList getCmdOut() {
        return listCmdOut;
    }

    public void removeCmdOut(List<AbstractCommand> abstractCommand) {
        if (abstractCommand != null) {
            listCmdOut.removeAll(abstractCommand);
        }
    }

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
     * Is Sync with initialization?
     */
    private boolean syncWithInit = false;
    public void setSyncWithInit(boolean syncWithInit) {
        this.syncWithInit = syncWithInit;
    }
    public boolean isSyncWithInit() {
        return this.syncWithInit;
    }

    /**
     * Cache the response with initialization
     */
    private SyncML responseInit = null;
    public void setResponseInit(SyncML responseInit) {
        this.responseInit = responseInit;
    }
    public SyncML getResponseInit() {
        return this.responseInit;
    }

    /**
     * Is the final response?
     */
    private boolean respFinal = true;
    public void setResponseFinal(boolean respFinal) {
        this.respFinal = respFinal;
    }
    public boolean isResponseFinal() {
        return this.respFinal;
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
}
