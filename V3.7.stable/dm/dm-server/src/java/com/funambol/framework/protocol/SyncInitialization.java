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


import java.util.ArrayList;
import java.util.HashMap;
import java.security.Principal;

import com.funambol.framework.core.AbstractCommand;
import com.funambol.framework.core.Alert;
import com.funambol.framework.core.AlertCode;
import com.funambol.framework.core.Anchor;
import com.funambol.framework.core.Chal;
import com.funambol.framework.core.ComplexData;
import com.funambol.framework.core.Constants;
import com.funambol.framework.core.Cred;
import com.funambol.framework.core.Data;
import com.funambol.framework.core.DevInf;
import com.funambol.framework.core.DevInfItem;
import com.funambol.framework.core.Get;
import com.funambol.framework.core.Item;
import com.funambol.framework.core.ItemizedCommand;
import com.funambol.framework.core.Meta;
import com.funambol.framework.core.NextNonce;
import com.funambol.framework.core.Put;
import com.funambol.framework.core.RepresentationException;
import com.funambol.framework.core.Results;
import com.funambol.framework.core.Source;
import com.funambol.framework.core.SourceRef;
import com.funambol.framework.core.Status;
import com.funambol.framework.core.StatusCode;
import com.funambol.framework.core.Sync;
import com.funambol.framework.core.SyncBody;
import com.funambol.framework.core.SyncHdr;
import com.funambol.framework.core.SyncML;
import com.funambol.framework.core.Target;
import com.funambol.framework.core.TargetRef;
import com.funambol.framework.database.Database;
import com.funambol.framework.protocol.v11.InitializationRequirements;
import com.funambol.framework.security.Sync4jPrincipal;


/**
 * Represents the Initialization package of the SyncML protocol.
 *
 * Example:
 * <pre>
 *   SyncInitialization syncInit = new SyncInitialization(header, body);
 *   ... do something ...
 *   syncInit.setServerCapabilities(serverCapabilities);
 *   syncInit.setAuthorizedStatusCode(StatusCode.AUTHENTICATION_ACCEPTED);
 *   syncInit.setClientCapabilitiesStatusCode(StatusCode.OK);
 *   ... other initializations ...
 *   Message response = syncInit.getResponse();
 * </pre>
 *
 *
 *
 * @version $Id: SyncInitialization.java,v 1.3 2006/11/15 14:59:14 nichele Exp $
 *
 * @see SyncPackage
 */
public class SyncInitialization
extends SyncPackage
{
    /**
     * Contains the request for server capabilities sent by the client
     * (null means capabilities not required)
     */
    private Get serverCapabilitiesRequest = null;

    /**
     * The device capabilities sent by the client.
     */
    private Put clientCapabilities = null;

    public DevInf getClientDeviceInfo() throws ProtocolException {
        DevInfItem item = (DevInfItem)this.clientCapabilities.getItems().get(0);
        return item.getDevInfData().getDevInf();
    }

    /**
     * Caches the commands sent by the client.It is set during the
     * checking of the requirements.
     */
    private AbstractCommand[] clientCommands = null;

    public AbstractCommand[] getClientCommands() {
        return clientCommands;
    }

    /**
     * Caches the alert command sent by the client. It is set during the
     * checking of the requirements.
     */
    private Alert[] clientAlerts = null;

    public Alert[] getClientAlerts() {
        return clientAlerts;
    }

    /**
     * Caches the sync command sent by the client to create Status in the case
     * of client is not authenticated
     */
    private Sync[] clientSyncs = null;

    /**
     * Client Capabilities status code
     */
    private int clientCapabilitiesStatusCode = -1;

    public int getClientCapabilitiesStatusCode() {
        return this.clientCapabilitiesStatusCode;
    }

    public void setClientCapabilitiesStatusCode(int clientCapabilitiesStatusCode) {
        this.clientCapabilitiesStatusCode = clientCapabilitiesStatusCode;
    }

    /**
     * Server capabilities
     */
    private DevInf serverCapabilities = null;

    public void setServerCapabilities(DevInf capabilities) {
        this.serverCapabilities = capabilities;
    }

    public DevInf getServerCapabilities() {
        return this.serverCapabilities;
    }

    /**
     * Databases that the server wants to synchronized. They can be differnt
     * from the databases the client has requested to be synchronized.
     */
    private Database[] databases = null;

    public void setDatabases(Database[] databases) {
        this.databases = databases;
    }

    public Database[] getDatabases() {
        return this.databases;
    }

    /**
     * Does the server require client capabilities?
     */
    private boolean clientCapabilitiesRequired = false;

    public void setClientCapabilitiesRequired(boolean clientCapabilitiesRequired) {
        this.clientCapabilitiesRequired = clientCapabilitiesRequired;
    }

    public boolean isClientCapabilitiesRequired() {
        return this.clientCapabilitiesRequired;
    }

    /**
     * Authorized status code - used in building response message
     */
    private int authorizedStatusCode = -1;

    public void setAuthorizedStatusCode(int authorizedStatusCode) {
        this.authorizedStatusCode = authorizedStatusCode;
    }

    /**
     * Did the client challenge for authentication? If so, this property contains
     * client Chal
     */
    private Chal clientChal = null;

    /**
     * Gets clientChal
     *
     * @return the given client Chal
     */
    public Chal getClientChal() {
        return clientChal;
    }

    /**
     * Server authentication type
     */
    private String serverAuthType = null;

    /**
     * Sets serverAuthType
     *
     * @param authType server authentication type
     */
    public void setServerAuthType(String authType) {
        this.serverAuthType = authType;
    }

    /**
     * Next nonce for MD5 authentication
     */
    private NextNonce nextNonce = null;

    /**
     * Sets nextNonce
     *
     * @param nextNonce the next nonce
     */
    public void setNextNonce(NextNonce nextNonce) {
        this.nextNonce = nextNonce;
    }

    /**
     * Did the client challenge for authentication? If so, this property must
     * contain server credentials.
     */
    private Cred serverCredentials;

    /**
     * Sets serverCredentials
     *
     * @param serverCredentials the new server credentials
     */
    public void setServerCredentials(Cred serverCredentials) {
        this.serverCredentials = serverCredentials;
    }

    /**
     * Gets serverCredentials
     */
    public Cred getServerCredentials() {
        return serverCredentials;
    }

    // ---------------------------------------------------------- Command status

    /**
     * The map containing the status of the commands
     */
    private HashMap<String, Integer> commandStatus = new HashMap<String, Integer>();

    /**
     * Sets the status code for the given command.
     *
     * @param cmd the command
     * @param statusCode the status code
     */
    public void setStatusCodeForCommand(AbstractCommand cmd, int statusCode) {
        setStatusCodeForCommand(cmd.getCmdID().getCmdID(), statusCode);
    }

    /**
     * Sets the status code for the command identified by the given id.
     *
     * @param cmdId the command id
     * @param statusCode the status code
     */
    public void setStatusCodeForCommand(String cmdId, int statusCode) {
        commandStatus.put(cmdId, Integer.valueOf(statusCode));
    }

    /**
     * Returns the status code for the given command. The status code must be
     * previously set with <i>setStatusCodeForCommand()</i>. If no status code
     * is associated to the given command, the default status code is returned.
     *
     * @param cmd the command
     * @param defaultCode the default status code
     *
     * @return the status code for the command if previously set, the default
     *         status code otherwise
     */
    public int getStatusCodeForCommand(AbstractCommand cmd, int defaultCode) {
        String cmdId = cmd.getCmdID().getCmdID();

        return getStatusCodeForCommand(cmdId, defaultCode);

    }

    /**
     * The same as <i>getStatusCodeForCommand(AbstractCommand, int) but passing
     * in the command id instead of the command.
     *
     * @param cmdId the command id
     * @param defaultCode
     *
     * @return the status code for the command if previously set, the default
     *         status code otherwise
     */
    public int getStatusCodeForCommand(String cmdId, int defaultCode) {
        Integer statusCode = commandStatus.get(cmdId);

        return (statusCode == null) ? defaultCode : statusCode.intValue();
    }


    // ------------------------------------------------------------ Constructors

    /**
      *
      *  @param syncHeader the header of the syncronization packet
      *  @param syncBody   the body of the syncronization packet
      *
      *  @throws ProtocolException
      */
    public SyncInitialization(final SyncHdr  syncHeader,
                              final SyncBody syncBody  )
    throws ProtocolException
    {
        super(syncHeader, syncBody);
        checkRequirements();
    }

    // ------------------------------------------------------ Public methods

    /**
     * Alerts specifying the database to be synchronized could be more
     * then one. Each can contains more than one item, which specifies
     * a single database. This method selects the items containing the
     * databases regardless in what alert command they where included.
     *
     * @return an array of Database objects
     */
    public Database[] getDatabasesToBeSynchronized() {
        ArrayList<Database> dbList = new ArrayList<Database>();

        Cred c = null;

        Database db    = null;
        Item[]   items = null;
        Meta     meta  = null;
        for (int i=0; ((clientAlerts != null) && (i < clientAlerts.length)); ++i) {
            //
            // Only database synchronization alerts are selected
            //
            if (!AlertCode.isInitializationCode(clientAlerts[i].getData())) {
                continue;
            }

            items = clientAlerts[i].getItems().toArray(new Item[0]);
            for (int j=0; ((items != null) && (j<items.length)); ++j) {
                meta = items[j].getMeta();
                Anchor anchor = meta.getAnchor();

                //
                // If the anchor does not exists, the alert does not represent
                // a database to be synchronized.
                //
                if (anchor == null) {
                    continue;
                }

                c = syncHeader.getCred();
                if (c == null) {
                    c = Cred.getGuestCredential();
                }
                Principal p = Sync4jPrincipal.fromCredential (
                                  c.getData(),
                                  c.getType(),
                                  syncHeader.getSource().getLocURI());

                //
                // NOTE: the target becomes the database source and vice-versa
                //
                db = new Database(
                    items[j].getTarget().getLocURI()                ,
                    null /* type */                                 ,
                    ProtocolUtil.source2Target(items[j].getSource()),
                    ProtocolUtil.target2Source(items[j].getTarget()),
                    anchor                                          ,
                    p
                );
                db.setMethod(clientAlerts[i].getData());
                db.setAlertCommand(clientAlerts[i]);

                dbList.add(db);
            }  // next j
        }  // next i

        int dbSize = dbList.size();
        Database[] dbArray = new Database[dbSize];
        for (int i=0; i<dbSize; i++) {
            dbArray[i] = dbList.get(i);
        }
        return dbArray;
    }

    // -------------------------------------------------------------------------

    /**
     * Checks that all requirements regarding the header of the initialization
     * packet are respected.
     *
     * @throws ProtocolException
     */
    public void checkHeaderRequirements()
    throws ProtocolException {
        InitializationRequirements.checkDTDVersion     (syncHeader.getVerDTD()   );
        InitializationRequirements.checkProtocolVersion(syncHeader.getVerProto());
        InitializationRequirements.checkSessionId      (syncHeader.getSessionID());
        InitializationRequirements.checkMessageId      (syncHeader.getMsgID()    );
        InitializationRequirements.checkTarget         (syncHeader.getTarget()   );
        InitializationRequirements.checkSource         (syncHeader.getSource()   );
    }

    /**
     * Checks that all requirements regarding the body of the initialization
     * packet are respected.
     *
     * @throws ProtocolException
     */
    public void checkBodyRequirements()
    throws ProtocolException {
        ArrayList<Alert> listAlerts = new ArrayList<Alert>();
        ArrayList<Alert> mergedClientCommands = new ArrayList<Alert>();

        AbstractCommand[] allClientCommands =
            (AbstractCommand[])syncBody.getCommands().toArray(
                                                        new AbstractCommand[0]);

        //
        // Extracts and checks alert commands
        //
        ArrayList<AbstractCommand> alertList = ProtocolUtil.filterCommands(allClientCommands    ,
                                                          Alert.class);
        int size = alertList.size();
        Alert[] alerts = new Alert[size];
        for (int i=0; i < size; i++) {
            alerts[i] = (Alert)alertList.get(i);

            listAlerts.add(alerts[i]);
         }

        for (int i=0; (alerts != null) && (i < alerts.length); ++i) {
             InitializationRequirements.checkAlertCommand(alerts[i]);
        }  // next i

        //
        // All alerts are OK => they can be cached
        //
        clientAlerts = listAlerts.toArray(new Alert[0]);
        mergedClientCommands.addAll(listAlerts);

        ArrayList clientCapabilitiesList =
            ProtocolUtil.filterCommands(allClientCommands, Put.class);

        if ((clientCapabilities != null) && (clientCapabilitiesList.size()>0)) {
            InitializationRequirements.checkCapabilities((Put)clientCapabilitiesList.get(0)     ,
                                                         InitializationRequirements.CLIENT_CAPABILITIES);
            clientCapabilities = (Put)clientCapabilitiesList.get(0);
        }
        mergedClientCommands.addAll(clientCapabilitiesList);

        ArrayList capabilitiesRequest =
            ProtocolUtil.filterCommands(allClientCommands, Get.class);

        if ((capabilitiesRequest != null) && (capabilitiesRequest.size()>0)) {
            InitializationRequirements.checkCapabilitiesRequest((Get)capabilitiesRequest.get(0));
            serverCapabilitiesRequest = (Get)capabilitiesRequest.get(0);
        }
        mergedClientCommands.addAll(capabilitiesRequest);

        //
        // Extracts Sync commands
        //
        ArrayList listSync = ProtocolUtil.filterCommands(allClientCommands, Sync.class);
        clientSyncs = (Sync[])listSync.toArray(new Sync[0]);

        clientCommands =
            mergedClientCommands.toArray(new AbstractCommand[0]);

    }


    /**
     * Constructs a proper response message.<br>
     * NOTES
     * <ul>
     *  <li> If server capabilities are not required, they are not sent (in
     *       the SyncML protocol the server MAY send not required capabilities)
     * </ul>
     *
     *
     * @param msgId the msg id of the response
     *
     * @return the response message
     *
     * @throws ProtocolException in case of error or inconsistency
     */
    public SyncML getResponse(String msgId) throws ProtocolException {
        ArrayList<ItemizedCommand> commandList = new ArrayList<ItemizedCommand>();

        //
        // Constructs all required response commands.
        //
        // NOTE: if NoResp is specified in the header element, than no
        //       response commands must be returned regardless NoResp is
        //       specified or not in subsequent commands
        //

        if (syncHeader.isNoResp() == false) {
            //
            // Session authorization
            //
            TargetRef[] targetRefs = new TargetRef[] { new TargetRef(syncHeader.getTarget().getLocURI()) };
            SourceRef[] sourceRefs = new SourceRef[] { new SourceRef(syncHeader.getSource().getLocURI()) };

            //
            // If the session is not authenticated, a Chal element must be returned
            //
            Chal chal = null;
            if (authorizedStatusCode != StatusCode.AUTHENTICATION_ACCEPTED) {
                if (serverAuthType.equalsIgnoreCase(Constants.AUTH_TYPE_BASIC)) {
                    chal = Chal.getBasicChal();
                } else if (serverAuthType.equalsIgnoreCase(Constants.AUTH_TYPE_CLEAR)) {
                    chal = Chal.getClearChal();
                }
            }

            //
            // The MD5 authentication always requires the chal element
            //
            if (serverAuthType.equalsIgnoreCase(Constants.AUTH_TYPE_MD5)) {
                chal = Chal.getMD5Chal();
                chal.setNextNonce(nextNonce);
            }

            Status statusCommand = new Status(
                idGenerator.next()                            ,
                syncHeader.getMsgID()                         ,
                "0" /* command ref */                         ,
                "SyncHdr" /* see SyncML specs */              ,
                targetRefs                                    ,
                sourceRefs                                    ,
                null /* credential */                         ,
                chal                                          ,
                new Data(String.valueOf(authorizedStatusCode)),
                new Item[0]
            );

            commandList.add(statusCommand);

            //
            // Status for each command that requested it (it is supposed each
            // command has bean already checked).
            //
            for (int i=0; ((clientCommands != null) && (i < clientCommands.length)); ++i) {
                if (clientCommands[i].isNoResp()) continue;

                targetRefs = null;
                sourceRefs = null;
                if (clientCommands[i] instanceof ItemizedCommand) {
                    Item[] items = ((ItemizedCommand)clientCommands[i]).getItems().toArray(new Item[0]);

                    ArrayList<TargetRef> trefs = new ArrayList<TargetRef>();
                    ArrayList<SourceRef> srefs = new ArrayList<SourceRef>();
                    Target t;
                    Source s;
                    for (int j=0; j<items.length; ++j) {
                        t = items[j].getTarget();
                        s = items[j].getSource();

                        if (t != null) {
                            trefs.add(new TargetRef(t));
                        }
                        if (s != null) {
                            srefs.add(new SourceRef(s));
                        }
                    }  // next j

                    if (trefs.size() > 0) {
                        targetRefs = trefs.toArray(new TargetRef[trefs.size()]);
                    }
                    if (srefs.size() > 0) {
                        sourceRefs = srefs.toArray(new SourceRef[srefs.size()]);
                    }
                }

                String commandReference = clientCommands[i].getCmdID().getCmdID();
                int status = getStatusCodeForCommand(clientCommands[i], StatusCode.OK);

                Item[] items = new Item[0];

		//
		// Within Response of Alert, Item must contain the NEXT Anchor.
                // NOTE: a database represents the server point of view so that
                //       the target is the client database and the source the
                //       server database.
		//
                if (clientCommands[i] instanceof Alert) {
                    for(int j=0; (databases != null) && (j<databases.length) ; ++j) {
                        if((databases[j].getSource().getLocURI()).equals(targetRefs[0].getValue())){
                            items = new Item[1];

                            Anchor alertAnchor =
                                       new Anchor(null, databases[j].getNext());

                            ComplexData data = new ComplexData();
                            data.setAnchor(alertAnchor);

                            items[0] = new Item(
                               null,  // target
                               null,  // source
                               null, // meta
                               data,
                               false //MoreData
                            );

                            break;
                        }
                    }
                }

                statusCommand = new Status(
                    idGenerator.next()          ,
                    syncHeader.getMsgID()       ,
                    commandReference            ,
                    clientCommands[i].getName() ,
                    targetRefs                  ,
                    sourceRefs                  ,
                    null /* credential */       ,
                    null /* challenge */        ,
                    new Data(status)            ,
                    items
                );

                commandList.add(statusCommand);
            }  // next i

            //
            // Status for client capabilities
            //
            if (clientCapabilities != null) {
                String commandReference = clientCapabilities.getCmdID().getCmdID();
                targetRefs = new TargetRef[] {};
                sourceRefs = new SourceRef[] {
                    new SourceRef(InitializationRequirements.CAPABILITIES_SOURCE)
                };

                Data data = new Data(String.valueOf(clientCapabilitiesStatusCode));
                statusCommand = new Status(
                    idGenerator.next()          ,
                    syncHeader.getMsgID()       ,
                    commandReference            ,
                    clientCapabilities.getName(),
                    targetRefs                  ,
                    sourceRefs                  ,
                    null /* credential */       ,
                    null /* challenge */        ,
                    data                        ,
                    null /* items */
                );

                commandList.add(statusCommand);
            }
            //
            // If status is not Authorized then create status for all commands
            // even if Sync command
            //
            if (authorizedStatusCode != StatusCode.AUTHENTICATION_ACCEPTED) {
                if (clientSyncs != null && clientSyncs.length > 0) {
                    for (int y=0; y<clientSyncs.length; y++) {
                        Sync sync = clientSyncs[y];
                        ArrayList<AbstractCommand> al = sync.getCommands();

                        String cmdRef = clientSyncs[y].getCmdID().getCmdID();
                        TargetRef[] tRefs = null;
                        if (clientSyncs[y].getTarget() != null) {
                            tRefs = new TargetRef[] { new TargetRef(clientSyncs[y].getTarget().getLocURI()) };
                        }

                        SourceRef[] sRefs = null;
                        if (clientSyncs[y].getSource() != null) {
                            sRefs = new SourceRef[] { new SourceRef(clientSyncs[y].getSource().getLocURI()) };
                        }

                        statusCommand = new Status(
                            idGenerator.next()              ,
                            syncHeader.getMsgID()           ,
                            cmdRef                          ,
                            clientSyncs[y].getName()        ,
                            tRefs                           ,
                            sRefs                           ,
                            null /* credential */           ,
                            null /* challenge */            ,
                            new Data(authorizedStatusCode)  ,
                            new Item[0]
                        );

                        commandList.add(statusCommand);

                        if (al != null) {
                            AbstractCommand[] absCmd = (AbstractCommand[])sync.getCommands().toArray(new AbstractCommand[0]);

                            for (int z=0; absCmd != null && z<absCmd.length; z++) {
                                cmdRef = absCmd[z].getCmdID().getCmdID();

                                statusCommand = new Status(
                                    idGenerator.next()            ,
                                    syncHeader.getMsgID()         ,
                                    cmdRef                        ,
                                    absCmd[z].getName()           ,
                                    tRefs                         ,
                                    sRefs                         ,
                                    null /* credential */         ,
                                    null /* challenge */          ,
                                    new Data(authorizedStatusCode),
                                    new Item[0]
                                );

                                commandList.add(statusCommand);
                            }
                        }
                    }
                }
            }
        }  // end if syncHeader.getNoResponse() == false

        //
        // Server capabilities
        //
        if ((serverCapabilitiesRequest != null) && (authorizedStatusCode == StatusCode.AUTHENTICATION_ACCEPTED)) {
            if (serverCapabilities == null) {
                throw new ProtocolException("Error in creating a response: server capabilities not set (use setServerCapabilities())");
            }

            String commandReference =
                serverCapabilitiesRequest.getCmdID().getCmdID();

            Meta meta = serverCapabilitiesRequest.getMeta();
            if (meta == null) {
                meta = new Meta();
                meta.setType(Constants.MIMETYPE_SYNCML_DEVICEINFO_XML);
            }

            ComplexData data = new ComplexData();
            data.setDevInf(serverCapabilities);

            Source source  = ProtocolUtil.target2Source(
                                  (serverCapabilitiesRequest.getItems().get(0)).getTarget()
                              );
            Item[] capabilities = new Item[] { new Item(null, source, null, data, false) };

            Results resultsCommand = new Results(
                idGenerator.next()   ,
                syncHeader.getMsgID(),
                commandReference     ,
                meta /* meta */      ,
                null /* target ref */,
                null /* source ref */,
                capabilities
            );
            commandList.add(resultsCommand);
        }

        //
        // Alerts for each database to be synchronized
        //
        for (int i=0; (databases != null) &&
                      (i<databases.length); ++i ) {

            if (databases[i].getStatusCode() == StatusCode.OK) {

                Alert alertCommand =
                ProtocolUtil.createAlertCommand(idGenerator.next(),
                                                false             ,
                                                null              ,
                                                databases[i]      );
                commandList.add(alertCommand);
            }
        }

        //
        // If client capabilities are required but not provided, a get command
        // must be added.
        //
        if (clientCapabilitiesRequired && (clientCapabilities == null)) {
            Meta meta = new Meta();
            meta.setType(Constants.MIMETYPE_SYNCML_DEVICEINFO_XML);

            Target target = new Target( InitializationRequirements.CAPABILITIES_TARGET,
                                        InitializationRequirements.CAPABILITIES_TARGET);
            Item[] items = new Item[1];

            items[0] = new Item(
                           target,
                           null  , /* source */
                           null  , /* meta   */
                           null  , /* data   */
                           false   /* moreData*/
                       );
            Get getCommand = new Get(
                idGenerator.next()     ,
                false /* no response */,
                null  /* language    */,
                null  /* credentials */,
                meta                   ,
                items
            );
            commandList.add(getCommand);
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
            serverCredentials        ,
            syncHeader.getMeta()
        );

        int size = commandList.size();
        AbstractCommand [] aCommands = new AbstractCommand[size];
        for (int i=0; i < size; i++) {
            aCommands[i] = commandList.get(i);

            if (authorizedStatusCode != StatusCode.AUTHENTICATION_ACCEPTED) {
                if (aCommands[i] instanceof Status) {
                    Status sc = (Status)aCommands[i];
                    sc.setData(new Data(String.valueOf(authorizedStatusCode)));
                }
            }

        }
        SyncBody responseBody = new SyncBody(
                                   aCommands,
                                   isFlag(Flags.FLAG_FINAL_MESSAGE)
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

    // --------------------------------------------------------- Private methods


}
