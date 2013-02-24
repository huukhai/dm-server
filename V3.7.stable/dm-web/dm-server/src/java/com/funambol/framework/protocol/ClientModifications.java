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

package com.funambol.framework.protocol;

import java.util.List;
import java.util.ArrayList;

import com.funambol.framework.core.AbstractCommand;
import com.funambol.framework.core.Add;
import com.funambol.framework.core.Alert;
import com.funambol.framework.core.AlertCode;
import com.funambol.framework.core.CmdID;
import com.funambol.framework.core.Copy;
import com.funambol.framework.core.Data;
import com.funambol.framework.core.Delete;
import com.funambol.framework.core.Exec;
import com.funambol.framework.core.Item;
import com.funambol.framework.core.Meta;
import com.funambol.framework.core.ModificationCommand;
import com.funambol.framework.core.Replace;
import com.funambol.framework.core.RepresentationException;
import com.funambol.framework.core.Results;
import com.funambol.framework.core.Source;
import com.funambol.framework.core.SourceRef;
import com.funambol.framework.core.Status;
import com.funambol.framework.core.StatusCode;
import com.funambol.framework.core.Sync;
import com.funambol.framework.core.Sync4jException;
import com.funambol.framework.core.SyncBody;
import com.funambol.framework.core.SyncHdr;
import com.funambol.framework.core.SyncML;
import com.funambol.framework.core.Target;
import com.funambol.framework.core.TargetRef;
import com.funambol.framework.database.Database;
import com.funambol.framework.protocol.v11.ClientModificationsRequirements;

/**
 * Represents a Client Modification package of the SyncML protocol.
 *
 *  The class is designed to be used in two times. First a <i>ClientModification</i>
 *  is created and checked for validity and compliancy with the protocol. Than
 *  <i>getResponse()</i> can be used to get a response message for the given
 *  request. During the request validation process some information about the
 *  request message are cached into instance variables and used in <i>getResponse()</i>.<br>
 *
 *
 *
 * @version $Id: ClientModifications.java,v 1.5 2007-06-19 08:16:12 luigiafassina Exp $
 */
public class ClientModifications
extends SyncPackage
implements Flags {

    // ------------------------------------------------------------ Constructors

    /** Constructors. It creates a new instance from the message header and body
     * plus the databases to synchronize.
     * It also checks if the requirements specified by the SyncML protocol are
     * met; if not a Sync4jException is thrown.
     * @param syncHeader the header of the syncronization packet
     * @param syncBody the body of the syncronization packet
     * @param syncDb the array of databases to be synchronized
     * @throws ProtocolException in case SyncML requiremets are not respected
     */
    public ClientModifications(final      SyncHdr  syncHeader,
    						   final      SyncBody   syncBody  ,
    						   Database[] syncDb               )
    throws ProtocolException {
        super(syncHeader, syncBody);
        checkRequirements();
        databases = syncDb;
    }

    public ClientModifications(final SyncHdr  syncHeader,
                               final SyncBody syncBody  )
    throws Sync4jException {
        super(syncHeader, syncBody);
    }

    // -------------------------------------------------------------- Properties

    /**
     * Has the server sent its capabilities and is expecting a response?
     * If yes, <i>serverCapabilitiesCmdId</i> is set to the id of the Put command
     * sent by the server. If not, <i>serverCapabilitiesCmdId</i> is empty.
     */
    private CmdID serverCapabilitiesCmdId = null;

    /**
     * Returns the serverCapabilitiesCmdId property.
     *
     * @return the serverCapabilitiesCmdId property.
     */
    public CmdID getServerCapabilitiesCmdId() {
        return this.serverCapabilitiesCmdId;
    }

    /**
     * Sets the serverCapabilitiesCmdId property.
     *
     * @param serverCapabilitiesCmdId new value
     */
    public void setServerCapabilitiesCmdId(CmdID serverCapabilitiesCmdId) {
        this.serverCapabilitiesCmdId = serverCapabilitiesCmdId;
    }

    /**
     * Has the server requested client capabilities?
     * If yes, <i>clientCapabilitiesCmdId</i> is set to the id of the Get command
     * sent by the server. If not, <i>clientCapabilitiesCmdId</i> is empty.
     */
    private CmdID clientCapabilitiesCmdId = null;

    /**
     * Returns the clientCapabilitiesCmdId property.
     *
     * @return the clientCapabilitiesCmdId property.
     */
    public CmdID getClientCapabilitiesCmdId() {
        return this.clientCapabilitiesCmdId;
    }

    /**
     * Sets the serverCapabilitiesCmdId property.
     * @param clientCapabilitiesCmdId new value
     */
    public void setClientCapabilitiesCmdId(CmdID clientCapabilitiesCmdId) {
        this.clientCapabilitiesCmdId = clientCapabilitiesCmdId;
    }

    /**
     * The results command in response to the request of client capabilities
     */
    private Results clientCapabilitiesResults = null;

    /**
     * Returns the clientCapabilitiesResults property.
     *
     * @return the clientCapabilitiesResults property.
     */
    public Results getClientCapabilitiesResults() {
        return this.clientCapabilitiesResults;
    }

    /**
     * The status command in response to the sending of server capabilities
     */
    private Status serverCapabilitiesStatus = null;

    /**
     * Returns the serverCapabilitiesStatus property.
     *
     * @return the serverCapabilitiesStatus property.
     */
    public Status getServerCapabilitiesStatus() {
        return this.serverCapabilitiesStatus;
    }

    /**
     * The client Sync command identifier. It is used when a response is required.
     */
    private CmdID clientSyncCmdId = null;

    /**
     * Returns the clientSyncCmdId property.
     *
     * @return the clientSyncCmdId property.
     */
    public CmdID getClientSyncCmdId() {
        return this.clientSyncCmdId;
    }

    /**
     * Sets the clientSyncCmdId property.
     *
     * @param clientSyncCmdId new value
     */
    public void setClientSyncCmdId(CmdID clientSyncCmdId) {
        this.clientSyncCmdId = clientSyncCmdId;
    }

    /**
     * The modification commands the server wants to sent to the client.
     */
    private AbstractCommand[] serverModifications = null;

    /**
     * Returns the serverModifications property.
     *
     * @return the serverModifications property.
     */
    public AbstractCommand[] getServerModifications() {
        return this.serverModifications;
    }

    /**
     * Sets the serverModifications property.
     *
     * @param serverModifications new value
     */
    public void setServerModifications(AbstractCommand[] serverModifications) {
        this.serverModifications = serverModifications;
    }

    /**
     * The status to be returned for the client sync command.
     */
    private Status[] clientModificationsStatus = null;

    /**
     * Returns the clientModificationsStatus property.
     *
     * @return the clientModificationsStatus property.
     */
    public Status[] getClientModificationsStatus() {
        return this.clientModificationsStatus;
    }

    /**
     * Sets the clientModificationsStatus property.
     *
     * @param clientModificationsStatus new value
     */
    public void setClientModificationsStatus(Status[] clientModificationsStatus) {
        this.clientModificationsStatus = clientModificationsStatus;
    }

    /**
     * Caches the commands sent by the client. It is set during the
     * checking of the requirements.
     */
    private AbstractCommand[] clientCommands = null;

    /**
     * Returns the clientCommands property.
     *
     * @return the clientCommands property.
     */
    public AbstractCommand[] getClientCommands() {
        return clientCommands;
    }

    /**
     * Caches the SyncCommand sent by the client. It is set during the checking
     * of requirements.
     */
    private Sync[] clientSyncCommands = null;

    /**
     * Returns the clientSyncCommands property.
     *
     * @return the clientSyncCommands property.
     */
    public Sync[] getClientSyncCommands() {
        return this.clientSyncCommands;
    }

    /**
     * Databases that the server wants to synchronize.
     */
    private Database[] databases = null;

    /**
     * Sets the databases property.
     *
     * @param databases new value
     */
    public void setDatabases(Database[] databases) {
        this.databases = databases;
    }

    /**
     * Returns the databases property.
     *
     * @return the databases property.
     */
    public Database[] getDatabases() {
        return this.databases;
    }

    /**
     * The alert commands the server wants to sent to the client.
     */
    private Alert[] modificationsAlert = null;

    /**
     * Returns the alertModifications property.
     *
     * @return the alertModifications property.
     */
    public Alert[] getModificationsAlert() {
        return this.modificationsAlert;
    }

    /**
     * Sets the modificationsAlert property.
     *
     * @param modificationsAlert new value
     */
    public void setModificationsAlert(Alert[] modificationsAlert) {
        this.modificationsAlert = modificationsAlert;
    }

    // ---------------------------------------------------------- Public methods

    /** Checks that all requirements regarding the header of the initialization
     * packet are respected.
     * @throws ProtocolException if header requirements are not respected
     */
    public void checkHeaderRequirements() throws ProtocolException {
        ClientModificationsRequirements.checkDTDVersion     (syncHeader.getVerDTD()   );
        ClientModificationsRequirements.checkProtocolVersion(syncHeader.getVerProto() );
        ClientModificationsRequirements.checkSessionId      (syncHeader.getSessionID());
        ClientModificationsRequirements.checkMessageId      (syncHeader.getMsgID()    );
        ClientModificationsRequirements.checkTarget         (syncHeader.getTarget()               );
        ClientModificationsRequirements.checkSource         (syncHeader.getSource()               );
    }

    /** Checks that all requirements regarding the body of the initialization
     * packet are respected.
     *
     * NOTE: bullet 2 pag 34 is not clear. Ignored for now.
     * @throws ProtocolException if body requirements are not respected
     */
    public void checkBodyRequirements() throws ProtocolException {
        // NOTE: initializes the clientCommands property
        clientCommands = (AbstractCommand[])syncBody.getCommands().toArray(new AbstractCommand[0]);

        //
        // If the server sent the device information to the client and requested
        // a response, serverCapabilitiesCmdId contains the command id of the
        // request command. A Status command with the same cmd id reference
        // must exist.
        //
        checkServerCapabilitiesStatus();

        //
        // If the server requested the device capabilities of the client,
        // clientCapabilitiesCmdId contains the command id of the Get command.
        // A Results command with the same cmd id reference must exist.
        //
        checkClientCapabilitiesResult();

        //
        // The Sync command must exists
        //
        checkSyncCommand();
    }

    // ----------------------------------------------------------- getResponse()

    /**
     * Constructs a proper response message.<p>
     * The sync package to the client has the following purposes:
     * <ul>
     *  <li>To inform the client about the results of sync analysis.
     *  <li>To inform about all data modifications, which have happened in the
     *      server since the previous time when the server has sent the
     *      modifications to the client.
     * </ul>
     *
     * @param msgId the msg id of the response
     * @return the response message
     *
     * @throws ProtocolException in case of error or inconsistency
     */
    public SyncML getResponse(String msgId) throws ProtocolException {
        ArrayList<AbstractCommand> commandList = new ArrayList<AbstractCommand>();

        if (idGenerator == null) {
            throw new NullPointerException("The id generator is null. Please set a value for idGenerator");
        }

        //
        // Constructs all required response commands.
        //
        // NOTE: if NoResp is specified in the header element, than no
        //       response commands must be returned regardless NoResp is
        //       specified or not in subsequent commands
        //
        if (syncHeader.isNoResp() == false) {

            TargetRef[] targetRefs = new TargetRef[] { new TargetRef(syncHeader.getTarget().getLocURI()) };
            SourceRef[] sourceRefs = new SourceRef[] { new SourceRef(syncHeader.getSource().getLocURI()) };

            Status statusCommand = new Status(
                idGenerator.next()               ,
                syncHeader.getMsgID()            ,
                "0" /* command ref */            ,
                "SyncHdr" /* see SyncML specs */ ,
                targetRefs                       ,
                sourceRefs                       ,
                null /* credential */            ,
                null /* challenge */             ,
                new Data(StatusCode.OK)          ,
                new Item[0]
            );

            commandList.add(statusCommand);

            //
            // 2. The Status element MUST be included in SyncBody if requested by
            //    the client. It is now used to indicate the general status of
            //    the sync analysis and the status information related to data
            //    items sent by the client (e.g., a conflict has happened.).
            //
            for (int i=0; (  isFlag(FLAG_SYNC_STATUS_REQUIRED)
                          && (clientModificationsStatus != null)
                          && (i<clientModificationsStatus.length) ); ++i) {
                commandList.add(clientModificationsStatus[i]);
            }
        }

        //
        // The Alert element, if present, MUST be included in SyncBody.
        //
        for (int i=0; ((modificationsAlert != null) && (i<modificationsAlert.length)); ++i) {
            commandList.add(modificationsAlert[i]);
        }

        //
        // 3. The Sync element MUST be included in SyncBody, if earlier there
        // were no occurred errors, which could prevent the server to process
        // the sync analysis and to send its modifications back to the client.
        //
        for (int i=0; ((serverModifications != null) && (i<serverModifications.length)); ++i) {
            commandList.add(serverModifications[i]);
        }

        //
        // Constructs return message
        //
        Target target = new Target(syncHeader.getSource().getLocURI(),
                                   syncHeader.getSource().getLocName());
        Source source = new Source(syncHeader.getTarget().getLocURI(),
                                   syncHeader.getTarget().getLocName());
        SyncHdr responseHeader = new SyncHdr (
            getDTDVersion()          ,
            getProtocolVersion()     ,
            syncHeader.getSessionID(),
            msgId                    ,
            target                   ,
            source                   ,
            null  /* response URI */ ,
            false                    ,
            null /* credentials */   ,
            null /* meta data */
        );

        AbstractCommand[] commands = null;
        int size = commandList.size();
        if (size == 0) {
            commands = new AbstractCommand[1];
        } else {
            commands = new AbstractCommand[size];
        }
        for (int i=0; i < size; i++) {
            commands[i] = commandList.get(i);
        }

        SyncBody responseBody = new SyncBody(
            commands,
            isFlag(FLAG_FINAL_MESSAGE) /* final */
        );

        try {
            return new SyncML(responseHeader, responseBody);
        } catch (RepresentationException e) {
            //
            // It should never happen !!!!
            //
            throw new ProtocolException("Unexpected error", e);
        }
    }

    /**
     * Create a Status command for the Sync sent by the client.
     *
     * <b>NOTE</b>: the protocol does not specify any information about the format
     * and the content of this message. By now a dummy status command is created
     * and returned.
     *
     * @return a StatusCommand object
     */
    public Status createSyncStatusCommand() {
        return new Status(
            idGenerator.next()                 ,
            "0" /* message id; TO DO */        ,
            clientSyncCmdId.getCmdID()         ,
            Sync.COMMAND_NAME                  ,
            (TargetRef[])null /* target refs */,
            (SourceRef[])null /* source refs */,
            null /* credential */              ,
            null /* chal */                    ,
            null /* Data */                    ,
            null /* items */
        );

    }

    /** For the Sync element, there are the following requirements.
     * <ul>
     *   <li> CmdID is required.
     *   <li> The response can be required for the Sync command. (See the Caching of Map Item,
     *        Chapter 2.3.1)
     *   <li> Target is used to specify the target database.
     *   <li> Source is used to specify the source database.
     * </ul>
     *
     * 5. If there is any modification in the server after the previous sync,
     * there are following requirements for the operational elements (e.g.,
     * Replace, Delete, and Add 4 ) within the Sync element.
     * <ul>
     *   <li> CmdID is required.
     *   <li> The response can be required for these operations.
     *   <li> Source MUST be used to define the temporary GUID (See Definitions)
     *        of the data item in the server if the operation is an addition.
     *        If the operation is not an addition, Source MUST NOT be included.
     *   <li> Target MUST be used to define the LUID (See Definitions) of the
     *        data item if the operation is not an addition. If the operation is
     *        an addition, Target MUST NOT be included.
     *   <li> The Data element inside Item is used to include the data itself if
     *        the operation is not a seletion.
     *   <li> The Type element of the MetaInf DTD MUST be included in the Meta
     *        element to indicate the type of the data item (E.g., MIME type).
     *        The Meta element inside an operation or inside an item can be used.
     * </ul>
     * @param db the database to be synchronized
     * @return a Sync command
     * @throws ProtocolException if any protocol requirement is not respected
     */
    public Sync createSyncCommand(Database db)
    throws ProtocolException {
        CmdID syncId = idGenerator.next();

        AbstractCommand[] commands = null;

        // if db.getMethod is One_Way_Sync_CLIENT
        // no synccommand from Server to Client

        if(db.getMethod() != AlertCode.ONE_WAY_FROM_CLIENT){
            commands = prepareCommands(db);
        }

        return new Sync(
            syncId                             ,
            isFlag(FLAG_SYNC_RESPONSE_REQUIRED),
            null                               , /* credentials */
            db.getTarget()                     ,
            db.getSource()                     ,
            null                               , /* Meta        */
            0,
            commands
        );

    }

    /**
     * Returns an array of synchronization commands (Add, Copy, Delete, Exec,
     * Replace) based on the content of the given database.
     *
     * @param db the database to be synchronized
     *
     * @return an array of AbstractCommand
     */
    public AbstractCommand[] prepareCommands(Database db) {
        ArrayList<ModificationCommand> commands = new ArrayList<ModificationCommand>();

        Meta meta = new Meta();
        meta.setType(db.getType());

        Item[] items = null;  // reused many times

        //
        // Add
        //
        items = db.getAddItems();
        if (items != null) {
            commands.add(
                new Add(
                    idGenerator.next()                          ,
                    isFlag(FLAG_MODIFICATIONS_RESPONSE_REQUIRED),
                    null /* credentials */                      ,
                    meta                                        ,
                    items                                       )
            );
        }

        //
        // Copy
        //
        items = db.getCopyItems();
        if (items != null) {
            commands.add(
                new Copy(
                    idGenerator.next()                          ,
                    isFlag(FLAG_MODIFICATIONS_RESPONSE_REQUIRED),
                    null /* credentials */                      ,
                    meta                                        ,
                    items                                       )
            );
        }

        //
        // Delete
        //
        items = db.getDeleteItems();
        if (items != null) {
            commands.add(
                new Delete(
                    idGenerator.next()                          ,
                    isFlag(FLAG_MODIFICATIONS_RESPONSE_REQUIRED),
                    isFlag(FLAG_ARCHIVE_DATA)                   ,
                    isFlag(FLAG_SOFT_DELETE)                    ,
                    null /* credentials */                      ,
                    meta                                        ,
                    items                                       )
            );
        }

        //
        // Exec
        //
        items = db.getExecItems();

        for (int i=0; ((items != null) && (i<items.length)); ++i) {
            commands.add(
                new Exec(
                    idGenerator.next()                          ,
                    isFlag(FLAG_MODIFICATIONS_RESPONSE_REQUIRED),
                    null  /* credentials */                     ,
                    null  /* correlator  */                     ,
                    items[i]                                    )
            );
        }

        //
        // Replace
        //
        items = db.getReplaceItems();
        if (items != null) {
            commands.add(
                new Replace(
                    idGenerator.next()                          ,
                    isFlag(FLAG_MODIFICATIONS_RESPONSE_REQUIRED),
                    null /* credentials */                      ,
                    meta                                        ,
                    items                                       )
            );
        }

        int size = commands.size();
        AbstractCommand [] aCommands = new AbstractCommand[size];
        for (int i=0; i < size; i++) {
            aCommands[i] = commands.get(i);
        }
        return aCommands;
    }

    // --------------------------------------------------------- Private methods

    /**
     * Checks if the requested status for server capabilities has been specified.
     * <p>
     *
     * @throws ProtocolException
     */
    private void checkServerCapabilitiesStatus()
    throws ProtocolException {
        //
        // If serverCapabilitiesCmdId is null no serverCapabilities status is required
        //
        if (serverCapabilitiesCmdId == null) return;

        List list = ProtocolUtil.filterCommands(clientCommands         ,
                                                Status.class    ,
                                                serverCapabilitiesCmdId);

        if (list.isEmpty()) {
            Object[] args = new Object[] { serverCapabilitiesCmdId.getCmdID() };
            throw new ProtocolException(ClientModificationsRequirements.ERRMSG_MISSING_STATUS_COMMAND, args);
        }

        serverCapabilitiesStatus = (Status)list.get(0);
    }

    /**
     * Checks if the result for client capabilities has been given.
     * <p>
     *
     * @throws ProtocolException
     */
    private void checkClientCapabilitiesResult()
    throws ProtocolException {
        //
        // If clientCapabilitiesCmdId is null no client capabilities were required
        //
        if (clientCapabilitiesCmdId == null) return;

        List list = ProtocolUtil.filterCommands(clientCommands         ,
                                                Results.class   ,
                                                clientCapabilitiesCmdId);

        if (list.isEmpty()) {
            Object[] args = new Object[] { clientCapabilitiesCmdId.getCmdID() };
            throw new ProtocolException(ClientModificationsRequirements.ERRMSG_MISSING_RESULTS_COMMAND, args);
        }

        Results results = (Results)list.get(0);

        ClientModificationsRequirements.checkCapabilities(
            results,
            ClientModificationsRequirements.CLIENT_CAPABILITIES
        );

        clientCapabilitiesResults = results;
    }

    /**
     * Checks the Sync command.
     * <p>Filters out the Sync Messages from client to Server with
     * ONE_WAY_SYNC_SERVER
     *
     * @throws ProtocolException
     */
    private void checkSyncCommand()
    throws ProtocolException {
        List list = ProtocolUtil.filterCommands(clientCommands   ,
                                                Sync.class);

        if (list.isEmpty()) {
            clientSyncCommands = new Sync[0];
            return;
        }

        clientSyncCommands = (Sync[])list.toArray(new Sync[list.size()]);
    }

}
