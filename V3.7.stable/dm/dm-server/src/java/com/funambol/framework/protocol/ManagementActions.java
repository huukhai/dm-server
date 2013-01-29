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
import com.funambol.framework.core.Alert;
import com.funambol.framework.core.AlertCode;
import com.funambol.framework.core.Chal;
import com.funambol.framework.core.Constants;
import com.funambol.framework.core.Cred;
import com.funambol.framework.core.Data;
import com.funambol.framework.core.Item;
import com.funambol.framework.core.NextNonce;
import com.funambol.framework.core.RepresentationException;
import com.funambol.framework.core.Results;
import com.funambol.framework.core.Source;
import com.funambol.framework.core.SourceRef;
import com.funambol.framework.core.Status;
import com.funambol.framework.core.StatusCode;
import com.funambol.framework.core.SyncBody;
import com.funambol.framework.core.SyncHdr;
import com.funambol.framework.core.SyncML;
import com.funambol.framework.core.Target;
import com.funambol.framework.core.TargetRef;
import com.funambol.framework.core.Util;
import com.funambol.framework.protocol.v11.BasicRequirements;
import com.funambol.framework.protocol.v11.ManagementActionsRequirements;
import com.funambol.framework.protocol.v11.Errors;

/**
 * Represents a Management Actions package of the SyncML DM protocol.
 *
 *  The class is designed to be used in two times. First a <i>ManagementActions</i>
 *  is created and checked for validity and compliancy with the protocol. Than
 *  <i>getResponse()</i> can be used to get a response message for the given
 *  request. During the request validation process some information about the
 *  request message are cached into instance variables and used in <i>getResponse()</i>.<br>
 *
 *
 *
 * @version $Id: ManagementActions.java,v 1.6 2007-06-19 08:16:12 luigiafassina Exp $
 */
public class ManagementActions extends SyncPackage implements Flags, Errors {

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


    // ------------------------------------------------------------ Constructors

    /** Constructor. It creates a new instance from the message header and body.
     * It also checks if the requirements specified by the SyncML DM protocol
     * are met; if not a Sync4jException is thrown.
     *
     * @param syncHeader the header of the syncronization packet
     * @param syncBody the body of the syncronization packet
     *
     * @throws ProtocolException in case SyncML requiremets are not respected
     */
    public ManagementActions(final SyncHdr syncHeader, final SyncBody syncBody)
    throws ProtocolException {
        super(syncHeader, syncBody);
        checkRequirements();

        checkSessionAbortRequired(syncBody);
    }

    // -------------------------------------------------------------- Properties

    /**
     * Caches the alert for session abort. It is set during the
     * checking of the requirements.
     */
    private Alert sessionAbortAlert = null;

    public boolean isSessionAbortRequired() {
        return !(sessionAbortAlert == null);
    }

    /**
     * Authorized status code - used in building response message
     */
    private int authorizedStatusCode = StatusCode.OK;

    public void setAuthorizedStatusCode(int authorizedStatusCode) {
        this.authorizedStatusCode = authorizedStatusCode;
    }

    /**
     * The message mime type
     */
    private String mimeType = null;

    /**
     * Set message mime type
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
      * Client authentication type
      */
     private String clientAuthType = null;

     /**
      * Sets clientAuthType
      *
      * @param authType client authentication type
      */
     public void setClientAuthType(String authType) {
         this.clientAuthType = authType;
     }


     /**
      * Next nonce for HMAC/MD5 authentication
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
     * The management commands the server wants to sent to the client.
     */
    private AbstractCommand[] managementCommands = null;

    /**
     * Returns the serverModifications property.
     *
     * @return the serverModifications property.
     */
    public AbstractCommand[] getManagementCommands() {
        return this.managementCommands;
    }

    /**
     * Sets the managementCommands property.
     *
     * @param managementCommands new value
     */
    public void setManagementCommands(AbstractCommand[] managementCommands) {
        this.managementCommands = managementCommands;
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
      * Sets the clientCommands property.
      *
      * @param commands the commands to set
      */
     public void setClientCommands(AbstractCommand[] commands) {
         this.clientCommands = commands;
     }


    // ---------------------------------------------------------- Public methods

    /**
     * Checks that all requirements regarding the header of the initialization
     * packet are respected.
     *
     * @throws ProtocolException if header requirements are not respected
     */
    public void checkHeaderRequirements() throws ProtocolException {
        BasicRequirements.checkDTDVersion     (syncHeader.getVerDTD()   );
        BasicRequirements.checkProtocolVersion(syncHeader.getVerProto() );
        BasicRequirements.checkSessionId      (syncHeader.getSessionID());
        BasicRequirements.checkMessageId      (syncHeader.getMsgID()    );
        BasicRequirements.checkTarget         (syncHeader.getTarget()   );
        BasicRequirements.checkSource         (syncHeader.getSource()   );
    }

    /**
     * Checks that all requirements regarding the body of the initialization
     * packet are respected.
     *
     * @throws ProtocolException if body requirements are not respected
     */
    public void checkBodyRequirements() throws ProtocolException {
        // NOTE: initializes the clientCommands property
        clientCommands = (AbstractCommand[])syncBody.getCommands().toArray(new AbstractCommand[0]);

        //
        // If there is a Results command, check it
        //
        List results = ProtocolUtil.filterCommands(clientCommands, Results.class);

        if (results.size() > 0) {
            ManagementActionsRequirements.checkResults(results);
        }
    }

    /**
     * Filters client commands for Results and return them
     *
     * @return an array of Results commands
     */
    public Results[] getResults() {
        ArrayList<AbstractCommand> ret =
            ProtocolUtil.filterCommands(clientCommands, Results.class);

        return (Results[])ret.toArray(new Results[ret.size()]);
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
        ArrayList<AbstractCommand> responseCommandList = new ArrayList<AbstractCommand>();

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


            //
            // If the client uses HMAC, a Chal element must be returned
            //
            Chal chal = null;

            if (Constants.AUTH_TYPE_HMAC.equalsIgnoreCase(clientAuthType)) {
                //
                // We must return a chal with the next nonce
                //
                chal = Chal.getHMACChal();
                chal.setNextNonce(nextNonce);
            }

            Status statusCommand = new Status(
                                              idGenerator.next()               ,
                                              syncHeader.getMsgID()            ,
                                              "0" /* command ref */            ,
                                              "SyncHdr" /* see SyncML specs */ ,
                                              targetRefs                       ,
                                              sourceRefs                       ,
                                              null /* credential */            ,
                                              chal /* challenge */             ,
                                              new Data(authorizedStatusCode)   ,
                                              new Item[0]
                                          );

            responseCommandList.add(statusCommand);
        }

        if (isFlag(Flags.FLAG_SERVER_ABORT_REQUIRED)) {

            //
            // Add only Alert for abort the session
            //
            Alert abortAlert = new Alert(idGenerator.next(),
                                         false,
                                         null,
                                         AlertCode.SESSION_ABORT,
                                         new Item[0]);

            responseCommandList.add(abortAlert);

        } else {

            //
            // Adds status for all client command
            //
            int numClientCommand = clientCommands.length;
            AbstractCommand command = null;
            TargetRef[] targetRefs = null;
            SourceRef[] sourceRefs = null;
            Status statusCommand = null;
            boolean hasLargeObject = false;
            Long itemSize = null;
            for (int i = 0; i < numClientCommand; i++) {

                command = clientCommands[i];

                if (command.isNoResp() || (command instanceof Status)) {
                    continue;
                }

                int status = StatusCode.OK;
                hasLargeObject = ProtocolUtil.hasLargeObject(command);

                //
                // For Results:
                // - if this not has a large object:
                //   if the item has a size specified this must match with the real dimension
                //     of the data
                //     otherwise
                //   no response is necessary.
                // - if Results has a large object the server must return a status code (213)
                //
                if (command instanceof Results) {
                    ArrayList<Item> itemsList = ( (Results)command).getItems();
                    if (!itemsList.isEmpty()) {
                        //
                        // Getting the size from the last item.
                        //
                        itemSize = Util.getItemSize( (itemsList.get(itemsList.size() - 1)));
                    }

                    if (!hasLargeObject) {
                        if (itemSize != null) {
                            Long realSize = Util.getRealItemSize( (itemsList.get(0)), mimeType);
                            if (itemSize.equals(realSize)) {
                                // no status necessary
                                continue;
                            } else {
                                status = StatusCode.OBJECT_SIZE_MISMATCH;
                            }
                        } else {
                            continue;
                        }
                    } else {
                        if (itemSize == null) {
                            // A large object is sent from the client
                            // without the meta size
                            status = StatusCode.INCOMPLETE_COMMAND;
                        } else {
                            status = StatusCode.CHUNKED_ITEM_ACCEPTED;
                        }
                    }
                }

                //
                // NOTE: currently all items returns always the same status,
                // therefore no targetRefs/sourceRefs must be specified
                // (see syncml_represent_v11_20020215).
                //
                targetRefs = null;
                sourceRefs = null;

                String commandReference = command.getCmdID().getCmdID();

                Item[] items = new Item[0];

                statusCommand = new Status(
                    idGenerator.next(),
                    syncHeader.getMsgID(),
                    commandReference,
                    command.getName(),
                    targetRefs,
                    sourceRefs,
                    null
                    /* credential */
                    ,
                    null
                    /* challenge */
                    ,
                    new Data(status),
                    items
                                );

                responseCommandList.add(statusCommand);

            } // next i

            if (sessionAbortAlert == null) {

                if (!isFlag(FLAG_MORE_MSG_REQUIRED)) {
                    //
                    // Include management action commands
                    //
                    for (int i = 0; ( (managementCommands != null) && (i < managementCommands.length));
                                 ++i) {
                        responseCommandList.add(managementCommands[i]);
                    }
                } else {
                    //
                    // waiting for more msg from the client.
                    // Send alert code 1222
                    //
                    Alert moreMsg = new Alert(
                        idGenerator.next(), // cmdId
                        false, // noResp
                        null, // cred
                        AlertCode.MORE_DATA, // data
                        null // item[]
                                    );
                    responseCommandList.add(moreMsg);
                    unsetFlag(FLAG_FINAL_MESSAGE);
                }
            }
        }

        //
        // Constructs return message
        //
        Target target = new Target(syncHeader.getSource().getLocURI(),
                                   syncHeader.getSource().getLocName());
        Source source = new Source(syncHeader.getTarget().getLocURI(),
                                   syncHeader.getTarget().getLocName());

        SyncHdr responseHeader = new SyncHdr (
                                        getDTDVersion()                      ,
                                        getProtocolVersion()                 ,
                                        syncHeader.getSessionID()            ,
                                        msgId                                ,
                                        target                               ,
                                        source                               ,
                                        null  /* response URI */             ,
                                        false                                ,
                                        serverCredentials                    ,
                                        null /* meta data */
                                    );

        AbstractCommand[] commands 	= null;
        int numCommand = responseCommandList.size();
        if (numCommand == 0) {
            commands = new AbstractCommand[1];
        } else {
            commands = new AbstractCommand[numCommand];
        }
        for (int i=0; i < numCommand; i++) {
            commands[i] = responseCommandList.get(i);
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

    // --------------------------------------------------------- Private methods
    /**
     * Checks if in the request there is a alert code for session abort. If present,
     * cache it in sessionAbortAlert instance variable.
     * @param syncBody the SyncBody to check
     */
    private void checkSessionAbortRequired(SyncBody syncBody) {

        AbstractCommand[] allClientCommands =
            (AbstractCommand[])syncBody.getCommands().toArray(
            new AbstractCommand[0]);


        //
        // Extracts the Alert command
        //
        ArrayList<AbstractCommand> alertList = (ArrayList<AbstractCommand>)ProtocolUtil.filterCommands(allClientCommands,
            Alert.class);
        int size = alertList.size();
        Alert[] alerts = new Alert[size];
        for (int i = 0; i < size; i++) {
            alerts[i] = (Alert)alertList.get(i);

            if (alerts[i].getData() == AlertCode.SESSION_ABORT) {
                // session abort required
                sessionAbortAlert = alerts[i];
                return ;
            }
        }
        sessionAbortAlert = null;
    }

}
