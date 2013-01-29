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
import java.util.Arrays;

import com.funambol.framework.core.AbstractCommand;
import com.funambol.framework.core.Alert;
import com.funambol.framework.core.AlertCode;
import com.funambol.framework.core.Chal;
import com.funambol.framework.core.Constants;
import com.funambol.framework.core.Cred;
import com.funambol.framework.core.Data;
import com.funambol.framework.core.Item;
import com.funambol.framework.core.NextNonce;
import com.funambol.framework.core.Replace;
import com.funambol.framework.core.RepresentationException;
import com.funambol.framework.core.Source;
import com.funambol.framework.core.SourceRef;
import com.funambol.framework.core.Status;
import com.funambol.framework.core.StatusCode;
import com.funambol.framework.core.SyncBody;
import com.funambol.framework.core.SyncHdr;
import com.funambol.framework.core.SyncML;
import com.funambol.framework.core.Target;
import com.funambol.framework.core.TargetRef;
import com.funambol.framework.core.dm.ddf.DevInfo;
import com.funambol.framework.protocol.v11.Errors;
import com.funambol.framework.protocol.v11.InitializationRequirements;
import com.funambol.framework.tools.ArrayUtils;


/**
 * Represents the Initialization package of the SyncML DM protocol.
 *
 * Example of use:
 * <pre>
 *   ManagementInitialization init = new ManagementInitialization(header, body);
 *   ... do something ...
 *   init.setManagementCommands(commands);
 *   init.setAuthorizedStatusCode(StatusCode.AUTHENTICATION_ACCEPTED);
 *   ... other initializations ...
 *   Message response = init.getResponse();
 * </pre>
 *
 *
 *
 * @version $Id: ManagementInitialization.java,v 1.3 2006/11/15 15:00:46 nichele Exp $
 *
 * @see SyncPackage
 */
/**
 * @TODO: extract the client Chal if given and store it in clientChal
 */
public class ManagementInitialization
extends SyncPackage
implements Errors {

    // ------------------------------------------------------------ Private data

    /**
     * Caches the commands sent by the client.It is set during the
     * checking of the requirements.
     */
    private AbstractCommand[] clientCommands = null;

    public AbstractCommand[] getClientCommands() {
        return clientCommands;
    }

    /**
     * Adds the given abstract command to clientCommands
     * @param command the command to add
     */
    public void addClientCommand(AbstractCommand[] command) {
        Object[] obj = (AbstractCommand[])ArrayUtils.mergeArrays(clientCommands, command,
            AbstractCommand.class);
        clientCommands = (AbstractCommand[])obj;
    }

    /**
     * Caches the alert command sent by the client. It is set during the
     * checking of the requirements.
     */
    private Alert clientAlert = null;

    public Alert getClientAlert() {
        return clientAlert;
    }


    /**
     * Authorized status code - used in building response message
     */
    private int authorizedStatusCode = -1;

    public void setAuthorizedStatusCode(int authorizedStatusCode) {
        this.authorizedStatusCode = authorizedStatusCode;
    }

    /**
     * Keeps the given DevInfo DM object given by the SyncML DM client
     */
    private DevInfo devInfo = null;

    /**
     * Returns the DevInfo DM object given by the SyncML DM client
     *
     * @return the DevInfo DM object given by the SyncML DM client
     */
    public DevInfo getDevInfo() {
        return devInfo;
    }

    /**
     * Management commands to be performed on the client
     */
    AbstractCommand[] managementCommands;

    /**
     * Sets managementCommands
     *
     * @param commands the management commands
     */
    public void setManagementCommands(AbstractCommand[] commands) {
       managementCommands = commands;
    }

    public AbstractCommand[] getManagementCommands() {
        return managementCommands;
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
     * Supported auth type
     */
    private String supportedAuthType = null;


    public void setSupportedAuthType(String supportedAuthType) {
        this.supportedAuthType = supportedAuthType;
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
    public ManagementInitialization(final SyncHdr  syncHeader,
                                    final SyncBody syncBody  )
    throws ProtocolException {
        super(syncHeader, syncBody);
        checkRequirements();

    }

    // ---------------------------------------------------------- Public methods

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

        AbstractCommand[] allClientCommands =
            (AbstractCommand[])syncBody.getCommands().toArray(
            new AbstractCommand[0]);

        //
        // Extracts  the Alert command
        //
        ArrayList<AbstractCommand> alertList = ProtocolUtil.filterCommands(allClientCommands,
            Alert.class);

        int size = alertList.size();

        // Checks if in the list of the alerts there is a alert for session abort.
        // If so, ignore other alert
        for (int i = 0; i < size; i++) {
            clientAlert = (Alert)alertList.get(i);

            if (clientAlert.getData() == AlertCode.SESSION_ABORT) {
                // session abort
                setFlag(FLAG_SESSION_ABORT_REQUIRED);
                break;
            } else {

            }
        }

        //
        // NOTE: we do not need to check for alert commands, since in DM we can
        // have Client Event Alerts (even if no processing is defined).
        //

        //
        // check if there is a Replace command with device info. If so,
        // caches device info. Plus, if the message is set as "final" check
        // that the it contains the Replace command.
        //
        if (isFinal()) {
            ArrayList<AbstractCommand> replaceCommands =
                ProtocolUtil.filterCommands(allClientCommands, Replace.class);

            if (replaceCommands.isEmpty()) {
                throw new ProtocolException(ERRMSG_MISSING_REPLACE);
            }
            InitializationRequirements.checkDeviceInfo( (Replace)replaceCommands.get(0));

            //
            // It's ok, cache the devInfo object and clientCommands
            //
            devInfo = ProtocolUtil.devInfoFromReplace( (Replace)replaceCommands.get(0));
        }

        clientCommands = allClientCommands;
    }



    /**
     * Constructs a proper response message.<br>
     * NOTES
     * <ul>
     *  <li> If server capabilities are not required, they are not sent (in
     *       the SyncML protocol the server MAY send not required capabilities)
     * </ul>
     *
     * @param msgId the msg id of the response
     * @return the response message
     *
     * @throws ProtocolException in case of error or inconsistency
     */
    public SyncML getResponse(String msgId) throws ProtocolException {
        ArrayList<AbstractCommand> responseCommandList = new ArrayList<AbstractCommand>();

        ArrayList<AbstractCommand> clientCommandList = new ArrayList<AbstractCommand>(Arrays.asList(clientCommands));

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

            if (!isFlag(Flags.FLAG_CHAL_NOT_REQUIRED)) {

                switch (authorizedStatusCode) {
                    case StatusCode.OK:

                        //
                        // Using HMAC the status code is 200 and the server must
                        // return a chal with new nonce
                        //
                        if (clientAuthType.equalsIgnoreCase(Constants.AUTH_TYPE_HMAC)) {
                            chal = Chal.getHMACChal();
                            chal.setNextNonce(nextNonce);
                        }

                        break;

                    case StatusCode.AUTHENTICATION_ACCEPTED:

                        //
                        // The MD5 and HMAC authentication always requires the chal element
                        //
                        if (clientAuthType.equalsIgnoreCase(Constants.AUTH_TYPE_MD5)) {
                            chal = Chal.getMD5Chal();
                            chal.setNextNonce(nextNonce);
                        } else if (clientAuthType.equalsIgnoreCase(Constants.AUTH_TYPE_HMAC)) {
                            chal = Chal.getHMACChal();
                            chal.setNextNonce(nextNonce);
                        }

                        break;
                    case StatusCode.INVALID_CREDENTIALS:

                        if (clientAuthType != null && supportedAuthType.indexOf(clientAuthType) != -1) {
                            if (clientAuthType.equalsIgnoreCase(Constants.AUTH_TYPE_BASIC)) {
                                chal = Chal.getBasicChal();
                            } else if (clientAuthType.equalsIgnoreCase(Constants.AUTH_TYPE_CLEAR)) {
                                chal = Chal.getClearChal();
                            } else if (clientAuthType.equalsIgnoreCase(Constants.AUTH_TYPE_MD5)) {
                                chal = Chal.getMD5Chal();
                                chal.setNextNonce(nextNonce);
                            } else if (clientAuthType.equalsIgnoreCase(Constants.AUTH_TYPE_HMAC)) {
                                chal = Chal.getHMACChal();
                                chal.setNextNonce(nextNonce);
                            }

                            break;
                        }

                    case StatusCode.MISSING_CREDENTIALS:

                        if (serverAuthType.equalsIgnoreCase(Constants.AUTH_TYPE_BASIC)) {
                            chal = Chal.getBasicChal();
                        } else if (serverAuthType.equalsIgnoreCase(Constants.AUTH_TYPE_CLEAR)) {
                            chal = Chal.getClearChal();
                        } else if (serverAuthType.equalsIgnoreCase(Constants.AUTH_TYPE_MD5)) {
                            chal = Chal.getMD5Chal();
                            chal.setNextNonce(nextNonce);
                        } else if (serverAuthType.equalsIgnoreCase(Constants.AUTH_TYPE_HMAC)) {
                            chal = Chal.getHMACChal();
                            chal.setNextNonce(nextNonce);
                        }

                        break;

                    default:

                }
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

            responseCommandList.add(statusCommand);

            //
            // Status for each command that requested it (it is supposed each
            // command has bean already checked).
            //
            int numClientCommand = clientCommandList.size();

            AbstractCommand command = null;

            for (int i=0; i<numClientCommand; i++) {

                command = clientCommandList.get(i);

                if (command.isNoResp() || (command instanceof Status)) {
                    continue;
                }

                //
                // NOTE: currently all items returns always the same status,
                // therefore no targetRefs/sourceRefs must be specified
                // (see syncml_represent_v11_20020215).
                //
                targetRefs = null;
                sourceRefs = null;

                String commandReference = command.getCmdID().getCmdID();
                int status = getStatusCodeForCommand(command, StatusCode.OK);

                Item[] items = new Item[0];

                statusCommand = new Status(
                                    idGenerator.next()          ,
                                    syncHeader.getMsgID()       ,
                                    commandReference            ,
                                    command.getName()           ,
                                    targetRefs                  ,
                                    sourceRefs                  ,
                                    null /* credential */       ,
                                    null /* challenge */        ,
                                    new Data(status)            ,
                                    items
                                );

                responseCommandList.add(statusCommand);

            }  // next i
        }  // end if syncHeader.getNoResponse() == false


        // in this implementation i add one status for all clientCommand, then
        // remove all clientCommand
        clientCommandList.clear();


        // re-set the clientCommands array with the commands again in the list
        clientCommands = clientCommandList.toArray(new AbstractCommand[] {});

        //
        // Add management commands
        //
        if (!isFlag(FLAG_SESSION_ABORT_REQUIRED)) {
            if (managementCommands != null) {
                for (int i = 0; i < managementCommands.length; ++i) {
                    responseCommandList.add(managementCommands[i]);
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

        int size = responseCommandList.size();
        AbstractCommand [] aCommands = new AbstractCommand[size];
        for (int i=0; i < size; i++) {
            aCommands[i] = responseCommandList.get(i);

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


    public void setRequest(SyncML request) {
        this.syncHeader = request.getSyncHdr();
        this.syncBody   = request.getSyncBody();
    }



    /**
     * Returns true if a session abort is required
     * @return true if a session abort is required, false otherwise
     */
    public boolean isSessionAbortRequired() {
        return isFlag(Flags.FLAG_SESSION_ABORT_REQUIRED);
    }

    // --------------------------------------------------------- Private methods


}
