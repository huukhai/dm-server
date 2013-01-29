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

package com.funambol.server.session;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.funambol.framework.config.Configuration;
import com.funambol.framework.config.ConfigurationConstants;
import com.funambol.framework.core.AbstractCommand;
import com.funambol.framework.core.Add;
import com.funambol.framework.core.Alert;
import com.funambol.framework.core.AlertCode;
import com.funambol.framework.core.Atomic;
import com.funambol.framework.core.Authentication;
import com.funambol.framework.core.Chal;
import com.funambol.framework.core.ComplexData;
import com.funambol.framework.core.Constants;
import com.funambol.framework.core.Copy;
import com.funambol.framework.core.Cred;
import com.funambol.framework.core.Data;
import com.funambol.framework.core.Delete;
import com.funambol.framework.core.Exec;
import com.funambol.framework.core.Get;
import com.funambol.framework.core.Item;
import com.funambol.framework.core.ItemizedCommand;
import com.funambol.framework.core.Meta;
import com.funambol.framework.core.NextNonce;
import com.funambol.framework.core.Replace;
import com.funambol.framework.core.Results;
import com.funambol.framework.core.Sequence;
import com.funambol.framework.core.Source;
import com.funambol.framework.core.SourceRef;
import com.funambol.framework.core.Status;
import com.funambol.framework.core.StatusCode;
import com.funambol.framework.core.Sync4jException;
import com.funambol.framework.core.SyncBody;
import com.funambol.framework.core.SyncHdr;
import com.funambol.framework.core.SyncML;
import com.funambol.framework.core.Target;
import com.funambol.framework.core.TargetRef;
import com.funambol.framework.engine.dm.DeviceDMState;
import com.funambol.framework.engine.dm.ManagementCommandDescriptor;
import com.funambol.framework.engine.dm.ManagementException;
import com.funambol.framework.engine.dm.ManagementProcessor;
import com.funambol.framework.engine.dm.SessionContext;
import com.funambol.framework.engine.dm.Util;
import com.funambol.framework.protocol.Flags;
import com.funambol.framework.protocol.ManagementActions;
import com.funambol.framework.protocol.ManagementInitialization;
import com.funambol.framework.protocol.ProtocolException;
import com.funambol.framework.protocol.ProtocolUtil;
import com.funambol.framework.security.Officer;
import com.funambol.framework.security.SecurityConstants;
import com.funambol.framework.security.Sync4jPrincipal;
import com.funambol.framework.server.Sync4jDevice;
import com.funambol.framework.server.SyncTimestamp;
import com.funambol.framework.server.dm.ProcessorSelector;
import com.funambol.framework.server.error.InvalidCredentialsException;
import com.funambol.framework.server.error.ServerException;
import com.funambol.framework.server.error.ServerFailureException;
import com.funambol.framework.server.session.ManagementState;
import com.funambol.framework.server.session.SessionHandler;
import com.funambol.framework.server.store.NotFoundException;
import com.funambol.framework.server.store.PersistentStoreException;
import com.funambol.framework.tools.Base64;
import com.funambol.framework.tools.SimpleIdGenerator;
import com.funambol.framework.tools.SizeCalculator;

import com.funambol.server.engine.dm.ManagementEngine;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;


/**
 * This class represents the handler for a SyncML DM session. It coordinates and
 * handles the packages and messages as dictated by the protocol.
 * <p>
 * The entry point is <i>processMessage()</i>, which determines which message is
 * expected and what processing has to be done (depending on the value of
 * <i>currentState</i>). If an error accours, the session goes to the state
 * <i>STATE_ERROR</i>; in this state no other messages but initialization can be
 * performed.
 * <p>
 *
 * LOG NAME: funambol.handler
 *
 * @see com.funambol.framework.server.session.SessionHandler
 *
 * @version $Id: ManagementSessionHandler.java,v 1.10 2006/11/15 16:06:55 nichele Exp $
 *
 */
public class ManagementSessionHandler
implements SessionHandler, java.io.Serializable,
           SecurityConstants, ConfigurationConstants {

    // --------------------------------------------------------------- Constants

    //
    // NOTE: the following states are in addition to the ones defined in
    //       SessionHandler
    //
    public static final int STATE_INITIALIZATION_PROCESSING    = 0x0010;
    public static final int STATE_INITIALIZATION_PROCESSED     = 0x0011;
    public static final int STATE_MANAGEMENT_PROCESSING        = 0x0012;
    public static final int STATE_MANAGEMENT_PROCESSED         = 0x0013;
    public static final int STATE_MANAGEMENT_COMPLETION        = 0x0014;
    public static final int STATE_CLIENT_UNAUTHORIZED          = 0x0016;


    //
    // The server has sent a complete package. It is awainting status from the
    // client on the commands sent in the package. Because the status and results
    //  may be large, such as the result of Get commands, the client may send
    // multiple message back to the server before completing its response
    //
    public static final int STATE_WAITING_MORE_MSG             = 0x0015;

    // ------------------------------------------------------- Private Constants
    private static final String[] MANAGEMENT_COMMANDS
         = {"Add", "Alert", "Copy", "Delete", "Exec" ,"Get", "Replace", "Atomic", "Sequence"};

     /**
      * Given a maxMsgSize, only the 85% is used, so in the pipeline manager,
      * the server has the 15% available
      */
     private static final double TOLLERANCE_MAX_MSG_SIZE = 0.85d;


    // ------------------------------------------------------------ Private data

    private int currentState = STATE_START;

    /**
     * Gets the current state
     *
     * @return the current state
     */
    public int getCurrentState() {
        return currentState;
    }

    private long creationTimestamp = -1;

    /**
     * Gets the creation timestamp of the session
     *
     * @return the creation timestamp
     */
    public long getCreationTimestamp() {
        return creationTimestamp;
    }

    private transient static final Logger log =
        Logger.getLogger(com.funambol.server.session.ManagementSessionHandler.class.getName());

    private transient ManagementInitialization init       = null;
    private transient ManagementActions        actions    = null;

    /**
     * Keeps the state information of this management session
     */
    private ManagementState sessionState = null;

    /**
     * The management processor to be used for this management session
     */
    private ManagementProcessor processor = null;

    /**
     * The server authentication type
     */
    private String serverAuthType = null;

    /**
     * The supported server authentication type
     */
    private String supportedAuthType = null;

    /**
     * The client authentication type
     */
    private String clientAuthType = null;

    /**
     * Sync4j engine object
     */
    private ManagementEngine engine = null;

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
     * Indicates if the session is a new session
     */
    //
    // @TODO is this still used
    //
    private boolean newSession = true;

    /**
     * List of command in order to create an answer with the maximum size
     * available (case of MultiMessage)
     */
    private ArrayList<Status> addStatus  = null;
    private ArrayList<Object> addAbsCmd  = null;

    /**
     * The max msg size
     */
    private long maxSizeAvailable = 0;


    // -------------------------------------------------------------- Properties

    /**
     * Returns the management session id
     *
     * @return the management session id
     */
    public String getSessionId() {
        return sessionState.getSessionId();
    }

    /**
     * Returns the management device
     *
     * @return the management device
     */
    public Sync4jDevice getDevice() {
        return sessionState.device;
    }


    /**
     * The cmdIdGenerator must be reset each time the process
     * of a message is starting
     */
    private void resetIdGenerator() {
        engine.getCommandIdGenerator().reset();
    }

    /**
     * The message id generator (it defaults ro a <i>SimpleIdGenerator</i> instance)
     */
    private SimpleIdGenerator msgIdGenerator = new SimpleIdGenerator();

    // ------------------------------------------------------------ Constructors

    /**
     * Creates a new instance of SimpleSessionHandler
     */
    public ManagementSessionHandler() {
        creationTimestamp = System.currentTimeMillis();
        init();
    }

    /**
     * Creates a new instance of SimpleSessionHandler with a given session id
     *
     * @param sessionId session id
     */
    public ManagementSessionHandler(String sessionId) {
        this();
        sessionState.sessionId = sessionId;
    }

    // ---------------------------------------------------------- Public methods

    /**
     * Sets newSession
     *
     * @param newSession the new value
     */
    public void setNew(boolean newSession) {
        this.newSession = newSession;
    }

    /**
     * Is newSession true?
     *
     * @return the newSession value
     */
    public boolean isNew() {
        return this.newSession;
    }

    /**
     * Returns true if the sessione has been authenticated.
     *
     * @return true if the sessione has been authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        return sessionState.authenticationState == AUTH_AUTHENTICATED;
    }

    /**
     * Processes the given message. See the class description for more
     * information.
     *
     * @param message the message to be processed
     *
     * @return the response message
     *
     * @throws ProtocolException in case of a protocol error
     * @throws InvalidCredentialsException in case of invalid credentials
     * @throws ServerFailureException in case of an internal server error
     */
    public SyncML processMessage(SyncML message)
    throws ProtocolException, InvalidCredentialsException, ServerFailureException {
        String deviceId     = null;
        SyncML response     = null;

        //
        // Reset the cmdIdGenerator has specified in the spec
        //
        resetIdGenerator();

        //
        // Each time a message is received for a particular session adjust the message ID
        //
        msgIdGenerator.next();

        //
        //  We maintain the message Id from client
        //
        sessionState.lastMsgIdFromClient = message.getSyncHdr().getMsgID();

        //
        // Initialize the device ID from the client request.
        //
        deviceId = message.getSyncHdr().getSource().getLocURI();

        if (log.isEnabled(Level.TRACE)) {
            log.trace("device id: " + deviceId);
            log.trace("current state: " + getStateName(currentState));
        }
        //
        // Set maximum message size
        //
        Meta meta = message.getSyncHdr().getMeta();
        if (meta != null) {
            Long maxMsgSize = meta.getMaxMsgSize();
            if (maxMsgSize != null) {
                sessionState.setMaxMsgSize(maxMsgSize.longValue());
            }

            Long maxObjSize = meta.getMaxObjSize();
            if (maxObjSize != null) {
                sessionState.setMaxObjSize(maxObjSize.longValue());
            }
        }

        try {
            switch (currentState) {
                case STATE_ERROR: // in case of error you can start a new initialization
                case STATE_END:
                case STATE_START:
                case STATE_CLIENT_UNAUTHORIZED:
                    sessionState.reset();

                    sessionState.nextTimestamp = new SyncTimestamp(
                        System.currentTimeMillis()
                        );

                    //
                    // Read device information. Use this information with care
                    // until the client has been authenticated.
                    //
                    sessionState.device = new Sync4jDevice(deviceId);
                    engine.readDevice(sessionState.device);

                    sessionState.syncMLVerProto =
                                message.getSyncHdr().getVerProto();
                    this.engine.setSyncMLVerProto(sessionState.syncMLVerProto.getVersion());

                    if (currentState != STATE_CLIENT_UNAUTHORIZED) {
                        moveTo(STATE_INITIALIZATION_PROCESSING);
                    }
                case STATE_INITIALIZATION_PROCESSING:
                    //
                    // Skip authentication if already authenticated
                    //
                    if (!isAuthenticated()) {

                        //
                        // Check if the client has sent the credentials with a
                        // authentication type supported by the server
                        //
                        Cred cred = message.getSyncHdr().getCred();
                        if (!checkAuthType(cred)) {
                            //
                            // the client uses an authentication type different
                            // from the server authentication type
                            //
                            sessionState.loggedCredential = null;
                        } else {
                            login(cred, deviceId);

                            if (isAuthenticated()) {
                                try {
                                    engine.readPrincipal(sessionState.loggedPrincipal);
                                } catch (NotFoundException e) {
                                    if (log.isEnabled(Level.INFO)) {
                                        log.info("Authenticated principal not found: " + sessionState.loggedPrincipal);
                                    }
                                    sessionState.authenticationState = AUTH_INVALID_CREDENTIALS;
                                    sessionState.loggedCredential = null;
                                }
                            }
                        }
                    }

                    boolean clientAuthenticated = isAuthenticated();
                    boolean chalNotRequired     = false;

                    if (!clientAuthenticated && (currentState == STATE_CLIENT_UNAUTHORIZED)) {
                        //
                        // If the client isn't authenticated for the second time,
                        // no chal must be sent from the server
                        //
                        chalNotRequired = true;
                    }

                    response = processInitMessage(message, chalNotRequired);

                    if (!clientAuthenticated) {
                        if (currentState == STATE_CLIENT_UNAUTHORIZED) {
                            response.setLastMessage(true);
                            moveTo(STATE_ERROR);
                        } else {
                            moveTo(STATE_CLIENT_UNAUTHORIZED);
                        }
                        break;
                    }

                    if (message.getSyncBody().isFinalMsg()) {
                        moveTo(STATE_INITIALIZATION_PROCESSED);
                    } else {
                        break;
                    }

                case STATE_INITIALIZATION_PROCESSED:
                    //
                    // If the client did not authenticate the server, we have to
                    // set the credentials once more if the authentication type
                    // is MD5 or stop the processing otherwise
                    //
                    checkServerAuthentication(message);

                    if ( (sessionState.started == false) ||
                         (
                           (sessionState.serverAuthenticationState != AUTH_AUTHENTICATED) &&
                           (sessionState.serverAuthenticationState != AUTH_ACCEPTED)
                         )
                       ) {
                        init.setFlag(Flags.FLAG_FINAL_MESSAGE);

                        // if addAbsCmd != null means that
                        // the server have already sent to the client the command,
                        // in previous message
                        // but the client not have authenticate the server. So,
                        // re-add the addAbsCmd to the cache. Before re-add the commands,
                        // we checks if there is one command splitted. If  happens,
                        // we merge data command with data splitted (splittedData in
                        // sessionState)
                        //
                        if (addAbsCmd != null) {
                            if (sessionState.getSplittedCommand() != null) {
                                if (addAbsCmd.contains(sessionState.getSplittedCommand())) {
                                    mergeData();
                                    sessionState.setSplittedCommand(null);
                                    sessionState.setNextDataToSend(null);
                                }
                            }
                            sessionState.addCmdOut(0, addAbsCmd);
                        }

                        // re-set the ManagementInitialization with the clientCommand
                        AbstractCommand[] allClientCommands =
                            (AbstractCommand[])(message.getSyncBody()).getCommands().toArray(
                            new AbstractCommand[0]);

                        init.addClientCommand(allClientCommands);

                        response = startManagementSession(message);

                        if (init.isSessionAbortRequired()) {
                            // session abort

                            if (log.isEnabled(Level.INFO)) {
                                log.info("Session aborted by client");
                            }

                            response.setLastMessage(true);
                            sessionState.nextTimestamp.end = System.currentTimeMillis();
                            sessionState.dmstate.state = DeviceDMState.STATE_ABORTED;
                            endSession();
                            moveTo(STATE_SESSION_ABORTED);
                            break;
                        }

                        sessionState.started = true;

                        if (ProtocolUtil.noMoreResponse(response)) {
                            moveTo(STATE_MANAGEMENT_COMPLETION);
                            sessionState.nextTimestamp.end = System.currentTimeMillis();
                            endSession();
                            moveTo(STATE_END);
                        }
                        break;
                    } else {
                        moveTo(STATE_MANAGEMENT_PROCESSING);
                    }

                case STATE_MANAGEMENT_PROCESSING:

                    response = processManagementMessage(message);

                    if (actions.isSessionAbortRequired()) {
                        // session abort

                        if (log.isEnabled(Level.INFO)) {
                            log.info("Session aborted by client");
                        }

                        response.setLastMessage(true);
                        sessionState.nextTimestamp.end = System.currentTimeMillis();
                        sessionState.dmstate.state = DeviceDMState.STATE_ABORTED;
                        endSession();
                        moveTo(STATE_SESSION_ABORTED);

                        break;
                    }

                    if (ProtocolUtil.noMoreResponse(response)) {
                        moveTo(STATE_MANAGEMENT_PROCESSED);
                        moveTo(STATE_MANAGEMENT_COMPLETION);
                    } else {
                        break;
                    }

                case STATE_MANAGEMENT_COMPLETION:
                    sessionState.nextTimestamp.end = System.currentTimeMillis();

                    response.setLastMessage(true);

                    endSession();

                    moveTo(STATE_END);
                    break;

                default:
                    endSession();
                    throw new ProtocolException( "Illegal state: "
                                               + getStateName(currentState)
                                               + '('
                                               + currentState
                                               + ')'
                                               );
            }
        } catch (ProtocolException e) {
            if (sessionState.dmstate != null) {
                sessionState.dmstate.state = DeviceDMState.STATE_ERROR;
            }
            endSession();
            log.debug( "processMessage", e);
            moveTo(STATE_ERROR);
            throw e;
        } catch (NotFoundException e) {
            if (sessionState.dmstate != null) {
                sessionState.dmstate.state = DeviceDMState.STATE_ERROR;
            }
            endSession();
            log.debug( "processMessage", e);
            moveTo(STATE_ERROR);
            throw new InvalidCredentialsException("Invalid credential error", e);
        } catch (PersistentStoreException e) {
            if (sessionState.dmstate != null) {
                sessionState.dmstate.state = DeviceDMState.STATE_ERROR;
            }
            endSession();
            log.debug( "processMessage", e);
            moveTo(STATE_ERROR);
            throw new ServerFailureException("Persistent store error", e);
        } catch (ManagementException e) {
            if (sessionState.dmstate != null) {
                sessionState.dmstate.state = DeviceDMState.STATE_ERROR;
            }
            endSession();
            log.debug( "processMessage", e);
            moveTo(STATE_ERROR);
            throw new ServerFailureException("Management error", e);
        } catch (Throwable t) {
            if (sessionState.dmstate != null) {
                sessionState.dmstate.state = DeviceDMState.STATE_ERROR;
            }
            endSession();
            log.debug( "processMessage", t);
            moveTo(STATE_ERROR);
        }

        List commands = response.getSyncBody().getCommands();
        ProtocolUtil.updateCmdId(commands);

        //
        // In order to handle the Status sent by the client as result of the
        // commands, we have to store the ManagementCommandDescriptors for the
        // sent management commands
        //
        storeManagementCommandDescriptorForManagementCommands(msgIdGenerator.current(), commands);

        return response;
    }

    /**
     * Processes an error condition. This method is called when the error is
     * is not fatal and is manageable at a protocol/session level. This results
     * in a well formed SyncML message with an appropriete error code.
     * <p>
     * Note that the offending message <i>msg</i> cannot be null, meaning that
     * at least the incoming message was a SyncML message. In this context,
     * <i>RepresentationException</i>s are excluded.
     *
     * @param the offending message - NOT NULL
     * @param the exception representing the error condition - NOT NULL
     *
     * @throws com.funambol.framework.core.Sync4jException in case of unexpected errors
     *
     * @return the response message
     */
    public SyncML processError(SyncML msg, Throwable error)
    throws Sync4jException {
        SyncHdr msgHeader = msg.getSyncHdr();

        int status = StatusCode.SERVER_FAILURE;

        if (error instanceof ServerException) {
            status = ((ServerException)error).getStatusCode();
        }

        Item item = new Item(
            new Target(msgHeader.getSource().getLocURI()),
            new Source(msgHeader.getTarget().getLocURI()),
            null                                         ,
            new ComplexData(error.getMessage())          ,
            false   /*  MoreData  */
        );

        Status statusCommand = new Status(
                engine.getCommandIdGenerator().next(),
                msgHeader.getMsgID()                 ,
                "0" /* command ref */                ,
                "SyncHdr" /* see SyncML specs */     ,
                new TargetRef(msgHeader.getTarget()) ,
                new SourceRef(msgHeader.getSource()) ,
                null /* credential */                ,
                null /* challenge */                 ,
                new Data(status)                     ,
                new Item[] { item }
            );

        String serverURI =
            Configuration.getConfiguration().getStringValue(CFG_SERVER_URI);
        SyncHdr syncHeader = new SyncHdr (
                                    msgHeader.getVerDTD()                        ,
                                    msgHeader.getVerProto()                      ,
                                    msgHeader.getSessionID()                     ,
                                    msgIdGenerator.current()                     ,
                                    new Target(msgHeader.getSource().getLocURI()),
                                    new Source(serverURI)                        ,
                                    null  /* response URI */                     ,
                                    false                                        ,
                                    null /* credentials */                       ,
                                    null /* metadata */
                               );

        SyncBody syncBody = new SyncBody(
                                new AbstractCommand[] { statusCommand },
                                true /* final */
                            );

        moveTo(STATE_ERROR);

        return new SyncML(syncHeader, syncBody);
    }

    /**
     * Called by the <i>SessionManager</i> when the session is expired.
     * It logs out the credential and release aquired resources.
     */

    public void expire() {
        logout();
    }

    /**
     * Called to interrupt the processing in case of errors depending on
     * extenal causes (i.e. the transport) or in case of session abort.
     * The current implementation just move the session state to the error state.
     * <p>
     *
     * @param statusCode the error code
     *
     * @see com.funambol.framework.core.StatusCode for valid status codes
     *
     */
    public void abort(int statusCode) {
        if (statusCode != StatusCode.SESSION_ABORTED) {
            moveTo(STATE_ERROR);
        }
    }

   /**
    * Called by the <i>SyncBean</i> when the container release the session.
    * It commit the change to the DB, logs out the credential and
    * release aquired resources.
    */
    public void endSession() {
        logout();

        if (sessionState.dmstate != null) {

            switch (sessionState.dmstate.state) {

                case DeviceDMState.STATE_COMPLETED:

                    engine.deleteDeviceDMState(sessionState.dmstate);
                    try {
                        processor.endSession(DeviceDMState.STATE_COMPLETED);
                    } catch (ManagementException e) {
                        if (log.isEnabled(Level.FATAL)) {
                            log.fatal("Error calling endSession: " + e.getMessage());
                        }
                        log.debug( "endSession", e);
                    }
                    break;

                case DeviceDMState.STATE_ABORTED:

                    try {
                        processor.endSession(DeviceDMState.STATE_ABORTED);
                    } catch (ManagementException e) {
                        if (log.isEnabled(Level.FATAL)) {
                            log.fatal("Error calling endSession: " + e.getMessage());
                        }
                        log.debug( "endSession", e);
                    }

                default:
                    sessionState.dmstate.end = new Date(System.currentTimeMillis());
                    engine.storeDeviceDMState(sessionState.dmstate);
            }

        }
    }

    /**
     * Called to permanently commit the synchronization. It does the following:
     * <ul>
     *  <li>persists the <i>last</i> timestamp to the database for the sources
     *      successfully synchronized
     * </ul>
     */
    public void commit() {
        sessionState.dmstate.end   = new Date(System.currentTimeMillis());

        if (log.isEnabled(Level.TRACE)) {
            log.trace("Committing state: " + sessionState.dmstate);
        }

        engine.storeDeviceDMState(sessionState.dmstate);
    }

    // --------------------------------------------------------- Private methods

    /**
     * Initializes internal handler state
     */
    private void init() {
        sessionState = new ManagementState();

        engine = new ManagementEngine(Configuration.getConfiguration());
    }

    /**
     * Processes the given initialization message.
     *
     *
     * @param message the message to be processed
     * @param chalNotRequired if a chal with a new next noce is not required
     * @return the response message
     *
     * @throws ProtocolException, ManagementException
     */
    private SyncML processInitMessage(SyncML request, boolean chalNotRequired)
        throws ProtocolException, ManagementException {
        //
        // In some circumstances, the device doesn't send the replace command (devInf)
        // if the server has answered 401 to the first message. The device sends
        // only the new credential.
        // If there is the replace command, the SessionHandler caches it.
        // If there isn't the replace command in the message, but there is a replace command in
        // cache, the SessionHandler adds it to the message because the ManagementInitialization
        // wants the replace command.
        //

        SyncBody syncBody = request.getSyncBody();
        ArrayList<AbstractCommand> allClientCommands = syncBody.getCommands();
        List<AbstractCommand> replaceCommands = ProtocolUtil.filterCommands(allClientCommands, new String[] {"Replace"});

        if (replaceCommands.isEmpty()) {
            if (sessionState.devInfReplaceCommand != null) {
                allClientCommands.add(sessionState.devInfReplaceCommand);
                syncBody.setCommands(allClientCommands.toArray(new AbstractCommand[0]));
            }
        } else {
            sessionState.devInfReplaceCommand = (Replace)replaceCommands.get(0);
        }

        init = new ManagementInitialization(request.getSyncHdr(),
                                            request.getSyncBody());

        init.setIdGenerator(engine.getCommandIdGenerator());

        //
        // Sets the server authentication type so that the server will be able
        // to accordingly challenge the client
        //
        init.setServerAuthType(serverAuthType);

        init.setSupportedAuthType(supportedAuthType);

        init.setClientAuthType(clientAuthType);

        if (request.getSyncBody().isFinalMsg()) {
            init.setFlag(Flags.FLAG_FINAL_MESSAGE);
        } else {
            init.unsetFlag(Flags.FLAG_FINAL_MESSAGE);
        }

        //
        // If authentication type is MD5 or HMAC then generate a new NextNonce and store
        // it into the database
        //
        if (clientAuthType != null && !chalNotRequired) {
            if (clientAuthType.equalsIgnoreCase(Constants.AUTH_TYPE_MD5) ||
                clientAuthType.equalsIgnoreCase(Constants.AUTH_TYPE_HMAC)) {

                NextNonce nonce = ProtocolUtil.generateNextNonce();
                sessionState.device.setClientNonce(nonce.getValue());
                init.setNextNonce(nonce);
                engine.storeDevice(sessionState.device);

            }

        }

        if (!isAuthenticated()) {

            switch (sessionState.authenticationState) {
                case AUTH_MISSING_CREDENTIALS:
                    init.setAuthorizedStatusCode(StatusCode.MISSING_CREDENTIALS);

                    // if missing credential then server returns a chal with the
                    // server auth type. If the server auth type is md5 or hmac then set next nonce

                    if (serverAuthType.equalsIgnoreCase(Constants.AUTH_TYPE_MD5) ||
                        serverAuthType.equalsIgnoreCase(Constants.AUTH_TYPE_HMAC)) {
                        if (!chalNotRequired) {
                            NextNonce nonce = ProtocolUtil.generateNextNonce();
                            sessionState.device.setClientNonce(nonce.getValue());
                            init.setNextNonce(nonce);
                            engine.storeDevice(sessionState.device);
                        }
                    }

                    break;
                case AUTH_INVALID_CREDENTIALS:
                    init.setAuthorizedStatusCode(StatusCode.INVALID_CREDENTIALS);

                    // if missing credential then server returns a chal with the
                    // server auth type. If the server auth type is md5 or hmac then set next nonce

                    if (serverAuthType.equalsIgnoreCase(Constants.AUTH_TYPE_MD5) ||
                        serverAuthType.equalsIgnoreCase(Constants.AUTH_TYPE_HMAC)) {
                        if  (!chalNotRequired) {
                            NextNonce nonce = ProtocolUtil.generateNextNonce();
                            sessionState.device.setClientNonce(nonce.getValue());
                            init.setNextNonce(nonce);
                            engine.storeDevice(sessionState.device);
                        }
                    }

                    break;
                default:
                    init.setAuthorizedStatusCode(StatusCode.FORBIDDEN);
            }
        } else {
            if (Constants.AUTH_TYPE_HMAC.equalsIgnoreCase(clientAuthType)) {
                //
                // Using HMAC the server must return 200
                //
                init.setAuthorizedStatusCode(StatusCode.OK);
            } else {
                init.setAuthorizedStatusCode(StatusCode.AUTHENTICATION_ACCEPTED);
            }
            //
            // The alert could be issued in any of the initialization messages
            //
            Alert alert = init.getClientAlert();

            if (alert != null) {
                sessionState.type = alert.getData();
            }
        }

        //
        // Set server credentials if required
        //
        if (sessionState.serverAuthenticationState != AUTH_ACCEPTED) {
            init.setServerCredentials(
                engine.getServerCredentials(getChal(request), sessionState.device)
                );
        }
        if (chalNotRequired) {
            init.setFlag(Flags.FLAG_CHAL_NOT_REQUIRED);
        } else {
            init.unsetFlag(Flags.FLAG_CHAL_NOT_REQUIRED);
        }
        SyncML response = init.getResponse(msgIdGenerator.current());

        return response;
    }

    /**
     * Processes the given management message.
     *
     * @param syncRequest the message to be processed
     *
     * @return the response message
     *
     * @throws ProtocolException
     */
    private SyncML processManagementMessage(SyncML request) throws ProtocolException,
        ManagementException {

        //
        // If the client sent a MD5/HMAC Chal, store the server next nonce
        //
        storeServerNonce(ProtocolUtil.getStatusChal(request));

        actions = new ManagementActions(request.getSyncHdr(),
                                        request.getSyncBody());

        actions.setClientAuthType(clientAuthType);
        actions.setIdGenerator(engine.getCommandIdGenerator());

        //
        // If the server uses the HMAC and the client not sends the chal with the next nonce or
        //  if the client uses the HMAC but the value received is not correct, the server abort the session
        //
        boolean serverAbortRequired = false;

        if (Constants.AUTH_TYPE_HMAC.equalsIgnoreCase(serverAuthType)) {
            //
            // Checks if the client has sent the next nonce
            //
            Chal chal = getMessageChal(request);
            if (chal == null) {
                //
                // The server must abort the session (see OMA-SyncML-DMSecurity)
                //
                if (log.isEnabled(Level.TRACE)) {
                    log.trace("Server abort the session because the client has not sent the next nonce");
                }

                serverAbortRequired = true;
            }
        }

        //
        // If the client uses the HMAC the server must check again the credential
        // and generate new nonce
        //
        Cred clientCred = null;
        if (Constants.AUTH_TYPE_HMAC.equalsIgnoreCase(clientAuthType)) {
            //
            // Generate new nonce
            //
            NextNonce nonce = ProtocolUtil.generateNextNonce();
            sessionState.device.setClientNonce(nonce.getValue());
            actions.setNextNonce(nonce);
            engine.storeDevice(sessionState.device);

            //
            // Checks if the credential is valid
            //
            clientCred = request.getSyncHdr().getCred();
            Officer officer = engine.getOfficer();

            if (!officer.authenticate(clientCred)) {

                if (log.isEnabled(Level.TRACE)) {
                    log.trace("Server abort the session because the credential sent by the client is not valid");
                }

                serverAbortRequired = true;
            }
        }


        if (serverAbortRequired) {
            //
            // We must abort the session
            //
            //
            // Set server credentials if required
            //
            if (sessionState.serverAuthenticationState != AUTH_ACCEPTED) {
                actions.setServerCredentials(
                    engine.getServerCredentials(getChal(request), sessionState.device)
                    );
            }
            actions.setFlag(Flags.FLAG_SERVER_ABORT_REQUIRED);
            actions.setFlag(Flags.FLAG_FINAL_MESSAGE);

            sessionState.nextTimestamp.end = System.currentTimeMillis();
            sessionState.dmstate.state = DeviceDMState.STATE_ABORTED;
            endSession();
            moveTo(STATE_SESSION_ABORTED);
            SyncML response = actions.getResponse(msgIdGenerator.current());
            response.setLastMessage(true);
            return response;

        }

        //
        // Add Generic alerts to Cache
        //
        Alert[] alertsToCache = ProtocolUtil.searchGenericAlertCommands(request.getSyncBody());
        sessionState.addGenericAlert(alertsToCache);

        boolean alertCode1222sentFromTheClient = false;
        boolean alertCode1222requiredFromTheClient = false;

        //
        // Caches the status/results of the client
        //
        List<AbstractCommand> commandToCache = ProtocolUtil.filterCommands(
                                   request.getSyncBody().getCommands(),
                                   new String[]{ Status.COMMAND_NAME, Results.COMMAND_NAME, Alert.COMMAND_NAME }
                                   );

        Alert[] removedAlerts = removeAlerts(commandToCache);

        checkForReceivedLargeObject(commandToCache);

        sessionState.addClientCommands(commandToCache.toArray(new AbstractCommand[0]));

        //
        // If the request is not final and there isn't an alert with code 1222, the server must answer
        // with a 1222 alert code without new commands and caches, if there is, large object
        //
        if (!request.getSyncBody().isFinalMsg()) {

            //
            // Check if in the request there is a 1222 alert code. If there is it
            // then we check if there is a large object to send
            //
            //
            Alert alert = ProtocolUtil.searchAlertCommand(request.getSyncBody(), AlertCode.MORE_DATA);
            if (alert != null) {
                //
                // The client wants more data
                //
                alertCode1222sentFromTheClient = true;
            } else {
                //
                // The client has not sent all status/results
                //
                actions.setFlag(Flags.FLAG_MORE_MSG_REQUIRED);
                alertCode1222requiredFromTheClient = true;

                Item itemWithLargeObject = ProtocolUtil.getLargeObject(commandToCache);

                if (itemWithLargeObject != null) {
                    sessionState.setReceivedLargeObject(itemWithLargeObject.getData().getData());
                    Long size = com.funambol.framework.core.Util.getItemSize(itemWithLargeObject);
                    if (size != null) {
                        sessionState.setSizeOfReceivedLargeObject(size);
                    } else {
                        if (sessionState.getSizeOfReceivedLargeObject() == null) {
                            // Sets size to -1 so the ManagementActions knows when
                            // a large object is sent from the client without the meta size
                            sessionState.setSizeOfReceivedLargeObject(Long.valueOf(-1));
                        }
                    }
                } else {
                    //
                    // If the request is final, the large object in cache not must
                    // be more used
                    //
                    sessionState.setReceivedLargeObject(null);
                    sessionState.setSizeOfReceivedLargeObject(null);
                }
            }

        } else {
            actions.setFlag(Flags.FLAG_FINAL_MESSAGE);
            //
            // Set the clientCommands with the client commands in cache
            //
            if (removedAlerts == null || removedAlerts.length == 0) {
                actions.setClientCommands(sessionState.getClientCommands());
            } else {
                //
                // We should add the removed alerts because otherwise the server
                // doesn't return the Status for that
                //
                AbstractCommand[] commands =
                    new AbstractCommand[sessionState.getClientCommands().length + removedAlerts.length];

                System.arraycopy(sessionState.getClientCommands(),
                                 0,
                                 commands,
                                 0,
                                 sessionState.getClientCommands().length);

                System.arraycopy(removedAlerts,
                                 0,
                                 commands,
                                 sessionState.getClientCommands().length,
                                 removedAlerts.length);

                actions.setClientCommands(commands);
            }

            //
            // If the cache is empty we call the setOperationResults with all results
            //
            if (sessionState.getCmdOut() == null || sessionState.getCmdOut().isEmpty()) {
                //
                // Gets the client Status and Results commands and merge them in
                // in a corresponding array of ManagementOperationResult objects.
                // The so returned array can then be passed to the management
                // processor.
                //
                processor.setOperationResults(
                    Util.operationResults(
                    actions.getClientCommands(), String.valueOf(StatusCode.CHUNKED_ITEM_ACCEPTED))
                );
                sessionState.clearClientCommands();

                //
                // Set Generic alerts and clear cache
                //
                Alert[] genericAlerts = sessionState.getGenericAlert();
                if (genericAlerts != null || genericAlerts.length > 0){
                    if (log.isEnabled(Level.DEBUG)) {
                        log.debug("Call setGenericAlert with " + genericAlerts.length + " generic alerts");
                    }
                    processor.setGenericAlert(genericAlerts);
                    sessionState.clearGenericAlert();
                }

            }

            // If the client sent a final message, the previous data is removed from the
            // sessionState because it is already merge with new data
            sessionState.setReceivedLargeObject(null);
        }

        actions.setFlag(Flags.FLAG_ALL_RESPONSES_REQUIRED);

        //
        // If the session is aborted or the server is awaiting for more msg or the
        // client is awaiting for more data, no commands must be added to the response
        //
        if (!actions.isSessionAbortRequired() && !alertCode1222requiredFromTheClient && !alertCode1222sentFromTheClient) {
            //
            // Checks if there are command in cache
            //
            List<Object> commandInCache = sessionState.getCmdOut();
            if (log.isEnabled(Level.DEBUG)) {
                log.debug("command in cache: " + commandInCache.size());
            }
            if (!commandInCache.isEmpty()) {
                AbstractCommand[] newCommands = commandInCache.toArray(new
                    AbstractCommand[] {});
                actions.setManagementCommands(newCommands);
                //
                // remove the commands from the cache
                //
                sessionState.removeCmdOut(commandInCache);
                if (log.isEnabled(Level.DEBUG)) {
                    log.debug("Num. of managementCommands in actions: " +
                              actions.getManagementCommands().length);
                }
            } else {
                //
                // Gets the next available operations from the processor
                //
                if (log.isEnabled(Level.DEBUG)) {
                    log.debug("Call getNextOperations for new operation");
                }

                actions.setManagementCommands(
                    Util.managementOperations2commands(
                    processor.getNextOperations(),
                                engine.getCommandIdGenerator(),
                                mimeType
                    )
                    );
            }
        } else if (alertCode1222sentFromTheClient) {
            //
            // We must check if there is previous command splitted on more
            // message (large object)
            //
            AbstractCommand previousCommand = sessionState.getSplittedCommand();

            if (previousCommand == null) {
                throw new ProtocolException("No more data to send");
            }
            //
            // If previousCmd is not null then it is a ItemizedCommand
            // (only ItemizedCommand are splittabled)
            // with only one item
            //
            Item item = ((ItemizedCommand)previousCommand).getItems().get(0);
            item.getData().setData(sessionState.getNextDataToSend());

            //
            // The dimension of the data is already sent to the client in the previous message.
            // Then remove meta size information from the item
            //
            Meta meta = item.getMeta();
            if (meta != null) {
                meta.setSize(null);
            }

            // Sets more data to false
            item.setMoreData(Boolean.FALSE);

            List<Object> commandInCache = sessionState.getCmdOut();

            //
            // Put the command in cache so tha actions manage all command
            //
            commandInCache.add(0, previousCommand);

            AbstractCommand[] newCommands = commandInCache.toArray(new
                AbstractCommand[] {});
            actions.setManagementCommands(newCommands);
        }

        //
        // Set server credentials if required
        //
        if (sessionState.serverAuthenticationState != AUTH_ACCEPTED) {
            actions.setServerCredentials(
                engine.getServerCredentials(getChal(request), sessionState.device)
                );
        }

        actions.setMimeType(mimeType);
        SyncML response = actions.getResponse(msgIdGenerator.current());

        if (alertCode1222requiredFromTheClient) {
            //
            // Returns the response without checks the dimension because the
            // response must contain only status for header and alert code 1222
            //
            return response;
        }

        //
        // Cache the commands to send in the next message
        //
        clearCache();
        cacheCommands(response);

        //
        // Calculate size of response message
        //
        SyncHdr syncHdr = response.getSyncHdr();
        SyncBody syncBody = response.getSyncBody();
        long sizeSyncHdr = 0, sizeSyncBody = 0;

        /**
         * The test case suite currently send only ds mime type, then we must check
         * also MIMETYPE_SYNCMLDS_WBXML and MIMETYPE_SYNCMLDS_XML
         */
        if (Constants.MIMETYPE_SYNCMLDM_WBXML.equals(mimeType) ||
            Constants.MIMETYPE_SYNCMLDS_WBXML.equals(mimeType)) {
            sizeSyncHdr = SizeCalculator.getWBXMLSize(syncHdr);
            sizeSyncBody = SizeCalculator.getWBXMLSize(syncBody);
        } else if (Constants.MIMETYPE_SYNCMLDM_XML.equals(mimeType) ||
                   Constants.MIMETYPE_SYNCMLDS_XML.equals(mimeType)) {
            sizeSyncHdr = SizeCalculator.getXMLSize(syncHdr);
            sizeSyncBody = SizeCalculator.getXMLSize(syncBody);
        }

        if (log.isEnabled(Level.DEBUG)) {
            log.debug("maxMsgSize: " + sessionState.getMaxMsgSize());
            log.debug("sizeSyncHdr: " + sizeSyncHdr);
            log.debug("sizeSyncBody: " + sizeSyncBody);
        }

        sessionState.setOverheadHdr(sizeSyncHdr);
        maxSizeAvailable = (int)(sessionState.getMaxMsgSize() * TOLLERANCE_MAX_MSG_SIZE);

        //
        // If the dimension of the response is < maxSizeAvailable or maxSizeAvailable is = 0
        // and there aren't the status/alerts/cmds cached then returns the response.
        // Else caches the commands of the response and re-create it.
        //
        if ( (maxSizeAvailable >= sizeSyncHdr + sizeSyncBody ||
              maxSizeAvailable == 0) &&
            sessionState.getStatusCmdOut().isEmpty() &&
            sessionState.getAlertCmdOut().isEmpty() &&
            sessionState.getCmdOut().isEmpty()
            ) {
            return response;
        }

        //
        // The size of the answer is greater then the allowed MaxMsgSize
        // Calculate size of the single commands of the response.
        // Create one answer of the allowed dimension.
        //
        try {
            response = createNextMsg(response);
        } catch (Sync4jException e) {
            throw new ProtocolException(e);
        }

        return response;
    }

    /**
     * Makes a state transition. Very simple implementation at the moment: it
     * changes the value of <i>currentState</i> to the given value.
     *
     * @param state the new state
     */
    private void moveTo(int state) {
        if (log.isEnabled(Level.TRACE)) {
            log.trace("moving to state " + getStateName(state));
        }
        currentState = state;
    }


    /**
     * Checks that the credentials of the given message are allowed to start a
     * session.
     *
     * @param credential the message
     * @param deviceId the deviceId
     */
    private boolean login(Cred credential, String deviceId) {
        //
        // May be the credential is already logged in...
        //
        logout();

        //
        // If the credential is not specified, create a new "guest" credential
        // but only if the property isGuestEnabled is true
        //
        if (credential == null) {
            sessionState.authenticationState = AUTH_MISSING_CREDENTIALS;

            return false;
        }

        clientAuthType = credential.getType();
        Sync4jPrincipal p = Sync4jPrincipal.fromCredential(credential.getData(),
                                                           credential.getType(),
                                                           deviceId            );
        //
        // If the client authentication type is MD5 or HMAC then set the server nonce
        // value into credential
        //
        if (clientAuthType.equalsIgnoreCase(Constants.AUTH_TYPE_MD5) ||
            clientAuthType.equalsIgnoreCase(Constants.AUTH_TYPE_HMAC)) {

            NextNonce nonce = new NextNonce(sessionState.device.getClientNonce());

            Authentication auth = credential.getAuthentication();
            auth.setNextNonce(nonce);
            auth.setDeviceId(deviceId);
        }

        try {
            Officer officer = engine.getOfficer();
            if (officer.authenticate(credential)) {
                //
                // if authType == MD5, the officer sets in the Authentication the
                // correct username, then sets it in the principal
                //
                if (clientAuthType.equalsIgnoreCase(Constants.AUTH_TYPE_MD5) ||
                    clientAuthType.equalsIgnoreCase(Constants.AUTH_TYPE_HMAC)) {
                    p.setUsername(credential.getAuthentication().getUsername());
                }

                if (officer.authorize(p, RESOURCE_MANAGEMENT)) {

                    sessionState.loggedCredential = credential;
                    sessionState.loggedPrincipal  = p         ;

                    sessionState.authenticationState = AUTH_AUTHENTICATED;

                    return true;
                }
            }
        } catch (Exception e) {
            if (log.isEnabled(Level.FATAL)) {
                log.fatal("Unable to login due to the error: " + e.getMessage());
            }
            log.debug("login", e);
        }

        return false;
    }

    /**
     * Logs out the logged in credential
     */
    private void logout() {
        sessionState.authenticationState = AUTH_INVALID_CREDENTIALS;
        sessionState.loggedCredential = null;
        sessionState.loggedPrincipal  = null;
    }

    /**
     * Starts a new management session, choosing the right managment processor.
     *
     * @param requestMessage the request
     *
     * @return the SyncML message to send back to the client containing the
     *         management commands (if there is any) as obtained by the
     *         management processor
     *
     * @throws ProtocolException in the case the message could not meet SyncML specs
     * @throws ManagementException if any other error occurs
     */
    private SyncML startManagementSession(SyncML requestMessage)
        throws ProtocolException, ManagementException {

        //
        // If the client sent a MD5/HMAC Chal, store the server next nonce
        //
        storeServerNonce(ProtocolUtil.getStatusChal(requestMessage));

        String sessionId = requestMessage.getSyncHdr().getSessionID().getSessionID();

        //
        // Gets the processor selector and retrieve the processor to be used
        // for this management session
        //
        Configuration c = Configuration.getConfiguration();

        //
        // Try to associate this session to an existing state (by session id or
        // device id). See the design document for details.
        //
        sessionState.dmstate = new DeviceDMState(sessionState.device.getDeviceId());
        sessionState.dmstate.mssid = init.getSessionId();
        if (!engine.resolveDMState(sessionState.dmstate, sessionState.device)) {
            //
            // This is new DM session for which the server has no information
            //
            sessionState.dmstate.id    = null;
            sessionState.dmstate.mssid = init.getSessionId();
        }

        //
        // If addAbsCmd is != null means that the processor already has been called.
        // Then cache contains the AbstractCommand to send
        //
        if (addAbsCmd == null) {
            ProcessorSelector selector =
                (ProcessorSelector)c.getBeanInstance(CFG_SERVER_DM_SELECTOR);

            processor = selector.getProcessor(sessionState.dmstate, init.getDevInfo());

            if (processor == null) {
                throw new ManagementException("No management processor could be selected!");
            }

            if (sessionState.dmstate.state == DeviceDMState.STATE_NOTIFIED) {
                //
                // Change the state to IN_PROGRESS
                //
                sessionState.dmstate.state = DeviceDMState.STATE_IN_PROGRESS;
            }
        }

        //
        // The alert could be issued in any of the initialization messages
        //
        Alert alert = init.getClientAlert();

        if (alert != null) {
            sessionState.type = alert.getData();
        }

        //
        // If addAbsCmd is != null means that the processor already has been called.
        // Then cache contains the AbstractCommand to send
        //
        if (addAbsCmd == null) {
            sessionState.dmstate.start = new Date(System.currentTimeMillis());
            //
            // Perform session initialization
            //

            SessionContext context = new SessionContext(sessionId,
                sessionState.loggedPrincipal,
                sessionState.type,
                init.getDevInfo(),
                    sessionState.dmstate,
                    sessionState.syncMLVerProto);

            processor.beginSession(context);

            //
            // Set Generic alerts
            //
            Alert[] genericAlerts = ProtocolUtil.searchGenericAlertCommands(requestMessage.getSyncBody());
            if (genericAlerts != null || genericAlerts.length > 0){
                if (log.isEnabled(Level.DEBUG)) {
                    log.debug("Start session calls setGenericAlert with " + genericAlerts.length + " generic alerts");
                }
                processor.setGenericAlert(genericAlerts);
            }

            if (!init.isSessionAbortRequired()) {
                //
                // Get the next available operations
                //
                init.setManagementCommands(
                    Util.managementOperations2commands(
                    processor.getNextOperations(),
                        engine.getCommandIdGenerator(),
                        mimeType
                    )
                    );
            }
        }

        // re-set the request because the init put in the response a reference to the request (MsgRef)
        init.setRequest(requestMessage);
        if (requestMessage.getSyncBody().isFinalMsg()) {
            init.setFlag(Flags.FLAG_FINAL_MESSAGE);
        } else {
            init.unsetFlag(Flags.FLAG_FINAL_MESSAGE);
        }

        SyncML response = init.getResponse(msgIdGenerator.current());

        //
        // Calculate size of response message
        //
        SyncHdr syncHdr = response.getSyncHdr();
        SyncBody syncBody = response.getSyncBody();
        long sizeSyncHdr = 0, sizeSyncBody = 0;

        /**
         * The test case suite currently send only ds mime type, then we must check
         * also MIMETYPE_SYNCMLDS_WBXML and MIMETYPE_SYNCMLDS_XML
         */
        if (Constants.MIMETYPE_SYNCMLDM_WBXML.equals(mimeType) ||
            Constants.MIMETYPE_SYNCMLDS_WBXML.equals(mimeType)) {
            sizeSyncHdr = SizeCalculator.getWBXMLSize(syncHdr);
            sizeSyncBody = SizeCalculator.getWBXMLSize(syncBody);
        } else if (Constants.MIMETYPE_SYNCMLDM_XML.equals(mimeType) ||
                   Constants.MIMETYPE_SYNCMLDS_XML.equals(mimeType)) {
            sizeSyncHdr = SizeCalculator.getXMLSize(syncHdr);
            sizeSyncBody = SizeCalculator.getXMLSize(syncBody);
        }

        if (log.isEnabled(Level.DEBUG)) {
            log.debug("maxMsgSize: " + sessionState.getMaxMsgSize());
            log.debug("sizeSyncHdr: " + sizeSyncHdr);
            log.debug("sizeSyncBody: " + sizeSyncBody);
        }

        sessionState.setOverheadHdr(sizeSyncHdr);
        maxSizeAvailable = sessionState.getMaxMsgSize();

        //
        // Check if the client MaxMsgSize is greater then the server
        // minmsgsize
        //
        checkMaxMsgSize(response);

        //
        // If the dimension of the response is < maxSizeAvailable or maxSizeAvailable is = 0
        // and there aren't the status/alerts/cmds cached then returns the response.
        // Else caches the commands of the response and re-create it.
        //
        if ( (maxSizeAvailable >= sizeSyncHdr + sizeSyncBody ||
              maxSizeAvailable == 0) &&
            sessionState.getStatusCmdOut().isEmpty() &&
            sessionState.getAlertCmdOut().isEmpty() &&
            sessionState.getCmdOut().isEmpty()
            ) {
           return response;
        }

        clearCache();
        //
        // Cache the alert/status/commands to send in the next package
        //
        cacheCommands(response);

        //
        // The size of the answer is greater then the allowed MaxMsgSize
        // Calculate size of the single commands of the response.
        // Create one answer of the allowed dimension.
        //
        try {
            response = createNextMsg(response);
        } catch (Sync4jException e) {
            throw new ProtocolException(e);
        }

        return response;

    }

    /**
     * Check if the client authentication type is supported from the officer
     *
     * @param cred the client credential
     *
     * @return true if the client authentication type is supported, false otherwise
     */
    private boolean checkAuthType(Cred cred) {
        Officer officer   = engine.getOfficer();

        serverAuthType    = officer.getAuthType();
        supportedAuthType = officer.getSupportedAuthType();

        if (cred == null) {
            if (officer.isGuestEnabled()) {
                sessionState.authenticationState = ManagementState.AUTH_AUTHENTICATED;
            } else {
                sessionState.authenticationState = ManagementState.AUTH_MISSING_CREDENTIALS;
            }
            return officer.isGuestEnabled();
        }

        String clientAuthType = cred.getType();

        if (supportedAuthType.indexOf(clientAuthType) != -1) {
            return true;
        }

        sessionState.authenticationState = ManagementState.AUTH_INVALID_CREDENTIALS;

        return false;
    }

    /**
     * Checks if the client expects the server to send its credentials. If so,
     * checkServerAuth returns the client Chal command.
     *
     * @param message the SyncML object
     *
     * @return the client Chal command or null if the client did not
     *         challenge the server
     */
    private Chal getMessageChal(SyncML message) {
        Status[] statusCmds =
            ProtocolUtil.filterCommands(
                                        message.getSyncBody().getCommands(),
                                        new String[]{ Status.COMMAND_NAME }
                                        ).toArray(new Status[0]);

        for (int i = 0; statusCmds != null && i < statusCmds.length; i++) {
            if ("0".equals(statusCmds[i].getCmdRef()) &&
                "SyncHdr".equals(statusCmds[i].getCmd())) {

                return statusCmds[i].getChal();
            }
        }

        return null;
    }

    /**
     * Extracts or create a Chal object that represents how server credentials
     * should be created.<br>
     * If the client challenged the server, we must use the requested auth
     * mechanism, otherwise, if the server is not logged, we use the default server authentication scheme.
     *
     * @param msg the request message
     *
     * @return a Chal object representing how server credentials should be
     *         formatted. <code>null</code> if the client not requires the
     *         authentication and if serverAuthType is set to Constants.AUTH_TYPE_NONE
     */
    private Chal getChal(final SyncML msg) {
        Chal chal = getMessageChal(msg);


        if (chal == null) {
          //
          // client not challenged the server
          //
          if (sessionState.serverAuthenticationState != AUTH_ACCEPTED) {
            //
            // the server is not logged
            //
            if (serverAuthType.equalsIgnoreCase(Constants.AUTH_TYPE_NONE)) {
              return null;
            }

            Meta meta = new Meta();
            meta.setType(serverAuthType);
            meta.setFormat(Constants.FORMAT_CLEAR);
            meta.setNextNonce(new NextNonce(sessionState.device.getServerNonce()));
            chal = new Chal(meta);

          }
        } else {
            if (log.isEnabled(Level.TRACE)) {
                log.trace("Challenged server authentication with scheme " + chal.getType());
            }
        }

        return chal;
    }

    /**
     * Checks if the server authentication succedeed. It throws a
     * ProtocolException if not.
     *
     * @param msg the SyncML message to check
     *
     * @return true if the server should retry authentication, false otherwise
     *
     * @throw ProcotolException if server authentication did not succeed
     */
    private void checkServerAuthentication(SyncML msg) throws ProtocolException {
        int headerStatusCode = ProtocolUtil.getHeaderStatusCode(msg);
        if (headerStatusCode == -1) {
            return;
        }

        if ( (headerStatusCode == StatusCode.INVALID_CREDENTIALS ||
              headerStatusCode == StatusCode.MISSING_CREDENTIALS)) {

            /**
             * The server isn't authorized.
             * If server has used basic authentication and the client has required
             * basic authentication means that the server is unable to authenticate
             * to the client.
             * If the client has required a authentication type different from that used
             * from the server, then set the credential for the server to the new authentication
             * type.
             * If the authentication type used from the server is equal to that required
             * from the client (different from basic), the server retries with the same
             * authentication type (the client could have sent a new nonce)
             */

            String authRequiredFromClient = null;
            Chal chal = getChal(msg);

            if (chal != null) {
                authRequiredFromClient = chal.getType();
            }

            if (serverAuthType.equalsIgnoreCase(Constants.AUTH_TYPE_BASIC) &&
                authRequiredFromClient.equalsIgnoreCase(Constants.AUTH_TYPE_BASIC)) {

                throw new ProtocolException("Unable to authenticate to the client");

            } else if (!authRequiredFromClient.equalsIgnoreCase(serverAuthType)) {
                serverAuthType = authRequiredFromClient;

                init.setServerCredentials(
                    engine.getServerCredentials(chal, sessionState.device)
                    );
                sessionState.serverAuthenticationState = AUTH_RETRY_1;

            } else if (authRequiredFromClient.equalsIgnoreCase(serverAuthType) &&
                       sessionState.serverAuthenticationState == AUTH_UNAUTHENTICATED) {
                init.setServerCredentials(
                    engine.getServerCredentials(chal, sessionState.device)
                );
                sessionState.serverAuthenticationState = AUTH_RETRY_1;
            } else {
                throw new ProtocolException("Unable to authenticate to the client");
            }

            return;
        } else if (headerStatusCode == StatusCode.AUTHENTICATION_ACCEPTED) {
            //
            // Authenticated with code 212
            //
            if (log.isEnabled(Level.TRACE)) {
                log.trace("Server logged (code 212)");
            }
            sessionState.serverAuthenticationState = AUTH_ACCEPTED;
        } else {
            //
            // Authenticated with code 200
            //
            if (log.isEnabled(Level.TRACE)) {
                log.trace("Server auhenticated (code 200)");
            }
            sessionState.serverAuthenticationState = AUTH_AUTHENTICATED;
        }
    }

    /**
     * Stores the server next nonce in both the database and the managementState
     * device member if the chal type is MD5/HMAC. If chal is null, nothing is saved.
     *
     * @parameter chal the Chal object; if null, nothing is saved
     */
    private void storeServerNonce(Chal chal) {
        if (chal == null) {
            return;
        }

        if (Constants.AUTH_TYPE_MD5.equals(chal.getType()) ||
            Constants.AUTH_TYPE_HMAC.equals(chal.getType())) {

            NextNonce nonce = chal.getNextNonce();
            if (nonce != null) {
                sessionState.device.setServerNonce(Base64.decode(nonce.getValue()));
                engine.storeDevice(sessionState.device);
            }
        }
    }

    /**
     * Returns a String representation of the given state
     *
     * @param state the state code
     *
     * @return a String representation of the given state
     */
    private String getStateName(int state) {
        String stateName = "STATE_UNKNOWN";

        switch (state) {
            case STATE_START                     : stateName = "STATE_START"                     ; break;
            case STATE_END                       : stateName = "STATE_END"                       ; break;
            case STATE_ERROR                     : stateName = "STATE_ERROR"                     ; break;
            case STATE_INITIALIZATION_PROCESSING : stateName = "STATE_INITIALIZATION_PROCESSING" ; break;
            case STATE_INITIALIZATION_PROCESSED  : stateName = "STATE_INITIALIZATION_PROCESSED"  ; break;
            case STATE_MANAGEMENT_PROCESSING     : stateName = "STATE_MANAGEMENT_PROCESSING"     ; break;
            case STATE_MANAGEMENT_PROCESSED      : stateName = "STATE_MANAGEMENT_PROCESSED"      ; break;
            case STATE_MANAGEMENT_COMPLETION     : stateName = "STATE_MANAGEMENT_COMPLETION"     ; break;
            case STATE_SESSION_ABORTED           : stateName = "STATE_SESSION_ABORTED"           ; break;

            default                              : stateName = "STATE_UNKNOWN"                   ; break;
        }

        return stateName;
    }

    /**
     * Clear the cache
     */
    private void clearCache() {
        sessionState.getAlertCmdOut().clear();
        sessionState.getStatusCmdOut().clear();
        sessionState.getCmdOut().clear();
    }

    /**
     * Cache the commands to send in the next message
     */
    private void cacheCommands(SyncML response) {
        //
        // Filter Status commands
        //
        List<AbstractCommand> statusCmdOut = ProtocolUtil.filterCommands(
            response.getSyncBody().getCommands(),
            new String[] {Status.COMMAND_NAME}
            );
        //
        // Sort Status for CmdRef
        //
        List<Object> listStatus = Arrays.asList(ProtocolUtil.sortStatusCommand(
            statusCmdOut.toArray(new Status[0]))
                                      );
        //
        // Cache Status commands
        //
        sessionState.addStatusCmdOut(listStatus);

        Status statusSyncHdr = ProtocolUtil.filterStatus(
            listStatus.toArray(new Status[0]),
            Status.class,
            "SyncHdr"
        );

        /**
         * The test case suite currently send only ds mime type, then we must check
         * also MIMETYPE_SYNCMLDS_WBXML and MIMETYPE_SYNCMLDS_XML
         */
        if (Constants.MIMETYPE_SYNCMLDM_WBXML.equals(mimeType) ||
            Constants.MIMETYPE_SYNCMLDS_WBXML.equals(mimeType)) {
            sessionState.setSizeStatusSyncHdr(SizeCalculator.getWBXMLSize(statusSyncHdr));
        } else if (Constants.MIMETYPE_SYNCMLDM_XML.equals(mimeType) ||
                   Constants.MIMETYPE_SYNCMLDS_XML.equals(mimeType)) {
            sessionState.setSizeStatusSyncHdr(SizeCalculator.getXMLSize(statusSyncHdr));
        }

        //
        // Cache Status for SyncHdr to use (in order to create the asnwer) when
        // the server isn't in the state "STATE_SYNCHRONIZATION_PROCESSING"
        //
        sessionState.setStatusSyncHdr(statusSyncHdr);

        //
        // Remove status of the SyncHdr
        //
        ArrayList<Status> removeStatus = new ArrayList<Status>();
        removeStatus.add(statusSyncHdr);
        sessionState.removeStatusCmdOut(removeStatus);

        //
        // Filter AbstractCommand commands
        // (in particular the Sync, Results and Get commands)
        //
        List<AbstractCommand> cmdOut = ProtocolUtil.filterCommands(
            response.getSyncBody().getCommands(),
            MANAGEMENT_COMMANDS
            );

        //
        // Sort AbstractCommand for CmdID
        //
        List<Object> listCmd = Arrays.asList(ProtocolUtil.sortAbstractCommand(
            cmdOut.toArray(new AbstractCommand[0]))
                                     );
        //
        // Cache commands
        //
        sessionState.addCmdOut(new LinkedList<Object>(listCmd));
    }

    /**
     * Create the next Initialization message with commands presents into queues
     *
     * @param syncML the hypothetical answer
     *
     * @return the server response
     */
    private SyncML createNextMsg(SyncML syncML) throws Sync4jException {

        if (log.isEnabled(Level.TRACE)) {
            log.trace("Create Next Message");
        }

        long sizeSyncHdr       = sessionState.getOverheadHdr();
        long sizeStatusSyncHdr = sessionState.getSizeStatusSyncHdr();

        maxSizeAvailable = sessionState.getMaxMsgSize() -
                           sizeSyncHdr               -
                           sizeStatusSyncHdr         ;


        if (Constants.MIMETYPE_SYNCMLDM_WBXML.equals(mimeType) ||
            Constants.MIMETYPE_SYNCMLDS_WBXML.equals(mimeType)) {
            maxSizeAvailable -= SizeCalculator.getWBXMLOverheadSyncML();
            maxSizeAvailable -= SizeCalculator.getWBXMLOverheadSyncBody();
        } else if (Constants.MIMETYPE_SYNCMLDM_XML.equals(mimeType) ||
                   Constants.MIMETYPE_SYNCMLDS_XML.equals(mimeType)) {
            maxSizeAvailable -= SizeCalculator.getXMLOverheadSyncML();
            maxSizeAvailable -= SizeCalculator.getXMLOverheadSyncBody();
        }

        //
        // Checks status
        //
        if (log.isEnabled(Level.TRACE)) {
            log.trace("How many Status can I include into the response?");
        }
        howManyStatus(sessionState.getStatusCmdOut());

        sessionState.removeStatusCmdOut(addStatus);

        //
        // Checks AbstractCommand
        //
        if (log.isEnabled(Level.TRACE)) {
            log.trace("How many AbstractCommand can I include into the response?");
        }
        howManyAbstractCommand();

        sessionState.removeCmdOut(addAbsCmd);

        int size = addStatus.size() + addAbsCmd.size();
        ArrayList<Object> commandList = new ArrayList<Object>(size);
        commandList.addAll(addStatus);
        commandList.addAll(addAbsCmd);

        AbstractCommand[] absCommands =
            commandList.toArray(new AbstractCommand[size]);

        // if there is a data splitted in session, the message is not final
        boolean isFinal = (sessionState.getNextDataToSend() == null);
        SyncBody responseBody = new SyncBody(absCommands, isFinal);

        if (log.isEnabled(Level.TRACE)) {
            log.trace("status in cache after creation: " + sessionState.getStatusCmdOut().size());
            log.trace("alert in cache after creation: " + sessionState.getAlertCmdOut().size());
            log.trace("command in cache after creation: " + sessionState.getCmdOut().size());
        }

        SyncML newResponse = new SyncML(syncML.getSyncHdr(), responseBody);

        if (log.isEnabled(Level.TRACE)) {
            if (Constants.MIMETYPE_SYNCMLDM_WBXML.equals(mimeType) ||
                Constants.MIMETYPE_SYNCMLDS_WBXML.equals(mimeType)) {
                size = (int) SizeCalculator.getWBXMLSize(newResponse);
            } else if (Constants.MIMETYPE_SYNCMLDM_XML.equals(mimeType) ||
                       Constants.MIMETYPE_SYNCMLDS_XML.equals(mimeType)) {
                size = (int) SizeCalculator.getXMLSize(newResponse);
            }
            log.trace("Response size: " + size);
        }

        return newResponse;
    }

    /**
     * Calculates how many Status can be sent
     */
    private void howManyStatus(List<Object> allStatus) {
        addStatus = new ArrayList<Status>();
        addStatus.add(sessionState.getStatusSyncHdr());

        //
        // Number of Status to insert into response
        //
        int x = 0;
        Status status = null;
        long size = 0;
        for (int i = 0; allStatus != null && i < allStatus.size(); i++) {
            status = (Status)allStatus.get(i);
            //
            // Calculate size Status
            //
            size = 0;
            if (Constants.MIMETYPE_SYNCMLDM_WBXML.equals(mimeType) ||
                Constants.MIMETYPE_SYNCMLDS_WBXML.equals(mimeType)) {
                size = SizeCalculator.getWBXMLSize(status);
            } else if (Constants.MIMETYPE_SYNCMLDM_XML.equals(mimeType) ||
                       Constants.MIMETYPE_SYNCMLDS_XML.equals(mimeType)) {
                size = SizeCalculator.getXMLSize(status);
            }

            // If there is space, we add the state
            if (maxSizeAvailable - size >= 0) {
                addStatus.add( (Status)allStatus.get(i));
                maxSizeAvailable -= size;
                x++;
            } else {
                break;
            }
        }

        if (log.isEnabled(Level.TRACE)) {
            log.trace("Number of Status inserted: " + x);
        }
    }


    /**
     * Calculates how many AbstractCommand can be sent.
     */
    private void howManyAbstractCommand() {
        addAbsCmd = new ArrayList<Object>();
        boolean isCmdWithLargeObject =false;

        //
        // Number of AbstractCommand to insert into response
        //
        int x = 0;
        List<Object> allCmd = sessionState.getCmdOut();

        if (maxSizeAvailable > 0 &&
            sessionState.getStatusCmdOut().isEmpty() &&
            sessionState.getAlertCmdOut().isEmpty()) {

            long size = 0;
            AbstractCommand cmd = null;
            for (int i = 0; allCmd != null && i < allCmd.size(); i++) {
                size = 0;
                cmd = (AbstractCommand)allCmd.get(i);

                if (Constants.MIMETYPE_SYNCMLDM_WBXML.equals(mimeType) ||
                    Constants.MIMETYPE_SYNCMLDS_WBXML.equals(mimeType)) {
                    size = SizeCalculator.getCommandWBXMLSize(cmd);
                } else if (Constants.MIMETYPE_SYNCMLDM_XML.equals(mimeType) ||
                           Constants.MIMETYPE_SYNCMLDS_XML.equals(mimeType)) {
                    size = SizeCalculator.getCommandXMLSize(cmd);
                }

                // If there is space, we add the abstract command
                if (maxSizeAvailable - size >= 0) {
                    addAbsCmd.add( cmd );
                    maxSizeAvailable -= size;
                    if (cmd == sessionState.getSplittedCommand()) {
                        //
                        // I have added a previous splitted cmd.
                        //
                        sessionState.setSplittedCommand(null);
                        sessionState.setNextDataToSend(null);
                    }
                    x++;
                } else {

                    isCmdWithLargeObject = checkForSplitData(cmd, maxSizeAvailable);

                    //
                    // If cmd is the first command that we try to add and there isn't
                    // sufficiently space then we try to split the data in more message (large object).
                    // If large object isn't permitted (because the dimension is greater of the maxObjectSize)
                    // we log the info and remove the command from the list
                    // because this command not will be never in the list
                    //
                    if (i == 0) {

                        if (!isCmdWithLargeObject) {
                            if (log.isEnabled(Level.INFO)) {
                                log.info("The command " + cmd + " is too large (" + size +
                                         " bytes)");
                            }
                            sessionState.removeCmdOut(cmd);
                        } else {
                            addAbsCmd.add(cmd);
                            x++;
                            if (sessionState.getNextDataToSend() == null) {
                                //
                                // All data is sent. Try to add more commands
                                //
                                continue;
                            } else {
                                break;
                            }
                        }
                    } else {
                        if (!isCmdWithLargeObject) {
                            //
                            // This isn't first command and isn't a command with
                            // large object, then break the process
                            //
                            break;
                        } else {
                            //
                            // This isn't first command but is a command with
                            // large object, then add the command and break the process
                            //
                            addAbsCmd.add(cmd);
                            x++;
                            if (sessionState.getNextDataToSend() == null) {
                                //
                                // All data is sent. Try to add more commands
                                //
                                continue;
                            } else {
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        }
        if (log.isEnabled(Level.TRACE)) {
            log.trace("Number of AbstractCommand inserted: " + x);
        }

    }

    /**
     * Checks if the given command is splittable with the given sizeAvailable.
     *
     * @param cmd AbstractCommand
     * @param sizeAvailable long
     * @return boolean
     */
    private boolean checkForSplitData(AbstractCommand cmd, long sizeAvailable) {
        Item itemToSplit     = null;
        //
        // If cmd contains no items or more than 1 items, no large object are permitted
        //
        if (cmd instanceof ItemizedCommand) {
            if (((ItemizedCommand)cmd).getItems().size() != 1) {
                if (log.isEnabled(Level.DEBUG)) {
                    log.debug("Command with more items isn't splittable");
                }
                return false;
            } else {
                itemToSplit = ((ItemizedCommand)cmd).getItems().get(0);
            }
        } else {
            if (log.isEnabled(Level.DEBUG)) {
                log.debug("Command isn't a ItemizedCommand then it isn't splittable");
            }
            return false;
        }

        Data data = itemToSplit.getData();
        if (data == null) {
            return false;
        }
        String dataValue = data.getData();
        if (dataValue == null) {
            return false;
        }

        Object dataToSplit = dataValue;
        boolean isBinaryData = false;
        int lengthDataToSplit = 0;

        Meta meta = itemToSplit.getMeta();
        if (Constants.MIMETYPE_SYNCMLDM_WBXML.equals(mimeType) ||
            Constants.MIMETYPE_SYNCMLDS_WBXML.equals(mimeType)) {

            if (meta != null) {
                String format = meta.getFormat();
                if (format != null && format.equalsIgnoreCase("b64")) {
                    isBinaryData = true;
                    dataToSplit = Base64.decode( ( (String)dataToSplit).getBytes());
                    lengthDataToSplit = ( (byte[])dataToSplit).length;
                }

            }
        }

        if (!isBinaryData) {
            lengthDataToSplit = ((String)dataToSplit).length();
        }

        long maxObjSize = sessionState.getMaxObjSize();
        if (maxObjSize != 0 &&  lengthDataToSplit > maxObjSize) {
            if (log.isEnabled(Level.DEBUG)) {
                log.debug("The dimension of the data is greater of the maxObjSize");
            }
            // data too large
            return false;
        }
        //
        // try to split the cmd
        //
        if (sessionState.getNextDataToSend().equals(dataValue)) {
            //
            // the item already has been splitted,
            // then no meta size is set
            //
        } else {
            //
            // We add data lenght information on the item
            //
            if (meta == null) {
                meta = new Meta();
                itemToSplit.setMeta(meta);
            }

            meta.setSize(Long.valueOf(lengthDataToSplit));
        }

        int sizeAvailableForData = calculateDataSizeAvailable(sizeAvailable, cmd, itemToSplit);

        if (sizeAvailableForData <= 0) {
            //
            // No space for data
            //
            return false;
        }

        sessionState.setSplittedCommand(cmd);

        Object newData = null;
        int lengthNewData = 0;

        if (isBinaryData) {
            if (sizeAvailableForData > lengthDataToSplit) {
                // send all
                sizeAvailableForData = lengthDataToSplit;
                newData = dataToSplit;
            } else {
                newData = new byte[sizeAvailableForData];
                System.arraycopy(dataToSplit, 0, newData, 0, sizeAvailableForData);
            }
            lengthNewData = sizeAvailableForData;
        } else {
            newData = ((String)dataToSplit).substring(0, sizeAvailableForData);
            lengthNewData = ((String)newData).length();
        }

        if (isBinaryData) {
            newData = new String(Base64.encode((byte[])newData));
        }
        itemToSplit.getData().setData((String)newData);

        if (sizeAvailableForData >= lengthDataToSplit) {
            // all data is sent
            sessionState.setNextDataToSend(null);
            itemToSplit.setMoreData(Boolean.FALSE);
        } else {
            //
            // We must save the data not sent
            //
            String dataNotSent = null;
            if (isBinaryData) {
                byte[] byteNotSent = new byte[lengthDataToSplit - sizeAvailableForData];
                System.arraycopy(dataToSplit, sizeAvailableForData, byteNotSent, 0, lengthDataToSplit - sizeAvailableForData);
                dataNotSent = new String(Base64.encode(byteNotSent));
            } else {
                dataNotSent = ((String)dataToSplit).substring(sizeAvailableForData);

            }
            itemToSplit.setMoreData(Boolean.TRUE);
            sessionState.setNextDataToSend(dataNotSent);
        }

        return true;
    }

    /**
     * Returns the space available for data.
     *
     * @param commandSizeAvailable long
     * @param cmd AbstractCommand
     * @param item Item
     * @return int
     */
    private int calculateDataSizeAvailable(long commandSizeAvailable, AbstractCommand cmd, Item item) {
        //
        // Caches the original data
        //
        ComplexData itemData = item.getData();
        item.setData(new ComplexData(""));

        long commandSize = -1;

        if (Constants.MIMETYPE_SYNCMLDM_WBXML.equals(mimeType) ||
            Constants.MIMETYPE_SYNCMLDS_WBXML.equals(mimeType)) {
            commandSize = SizeCalculator.getCommandWBXMLSize(cmd);
        } else if (Constants.MIMETYPE_SYNCMLDM_XML.equals(mimeType) ||
                   Constants.MIMETYPE_SYNCMLDS_XML.equals(mimeType)) {
            commandSize = SizeCalculator.getCommandXMLSize(cmd);
        }

        int sizeAvailableForMoreData = (int)(commandSizeAvailable - commandSize);
        //
        // Re-set the original data
        //
        item.setData(itemData);
        return sizeAvailableForMoreData;
    }

    /**
     * Checks if in the given list of commands there is data to add with
     * the previous data. The command with the data to add is the first in the list.
     * If there is a command, merges the previous data with the data contained in
     * the command.
     * @param clientCommands the command list to check
     * @throws ProtocolException if the list not contains a valid Results
     */
    private void checkForReceivedLargeObject(List<AbstractCommand> clientCommands) throws ProtocolException  {
        String previousReceivedLargeObject = sessionState.getReceivedLargeObject();

        if (previousReceivedLargeObject == null) {
            return;
        }

        //
        // There is a large object in cache. The first Results command
        // contains data to add to previousReceivedLargeObject
        //
        ArrayList<AbstractCommand> results = ProtocolUtil.filterCommands((clientCommands.toArray(new AbstractCommand[0])), Results.class);
        if (results.isEmpty()) {
            throw new ProtocolException("Error awaiting more data from the client - No Results in the request");
        }

        //
        // If there is previousReceivedLargeObject, the first command in the next
        // message contains the data to add with the previous data
        //
        Results result = (Results)results.get(0);
        Item item      = result.getItems().get(0);

        boolean isReceivedItemWithBinaryData = com.funambol.framework.core.Util.isItemWithBinaryData(item);

        String receivedData = null;

        if (item.getData() != null) {
            receivedData = item.getData().getData();
        } else {
            throw new ProtocolException("Error awaiting more data from the client - No Data in the first item");
        }
        if (isReceivedItemWithBinaryData) {
            //
            // If mime type is xml, binary data are managed as string
            //
            if (Constants.MIMETYPE_SYNCMLDM_XML.equals(mimeType) ||
                Constants.MIMETYPE_SYNCMLDS_XML.equals(mimeType)) {
                isReceivedItemWithBinaryData = false;
            }
        }

        if (isReceivedItemWithBinaryData) {
            byte[] previousData = Base64.decode(previousReceivedLargeObject.getBytes());
            byte[] bReceivedData = Base64.decode(receivedData.getBytes());
            byte[] newData = new byte[previousData.length + bReceivedData.length];
            System.arraycopy(previousData, 0, newData, 0, previousData.length);
            System.arraycopy(bReceivedData, 0, newData, previousData.length, bReceivedData.length);
            item.getData().setData(new String(Base64.encode(newData)));
        } else {
            String newData = previousReceivedLargeObject + receivedData;
            item.getData().setData(newData);
        }
        //
        // re-add the size of the data in the item because the client sends the size
        // only in the first message. So, the ManagementAction can checks the correct
        // dimension
        //
        Meta meta = item.getMeta();
        if (meta == null) {
            meta = new Meta();
            item.setMeta(meta);
        }
        meta.setSize(sessionState.getSizeOfReceivedLargeObject());
    }

    /**
     * Merge the data contained in the command splitted with the data still
     * to send. It is use for rebuild the data in case in which the client
     * sends a 401 or 407 code
     */
    private void mergeData() {
        AbstractCommand cmd = sessionState.getSplittedCommand();
        //
        // Only itemizedCommand with only one item are splittable
        //
        Item itemSplitted = (((ItemizedCommand)cmd).getItems()).get(0);
        boolean isBinary = com.funambol.framework.core.Util.isItemWithBinaryData(itemSplitted);

        if (isBinary) {
            //
            // If mime type is xml, binary data are managed as string
            //
            if (Constants.MIMETYPE_SYNCMLDM_XML.equals(mimeType) ||
                Constants.MIMETYPE_SYNCMLDS_XML.equals(mimeType)) {
                isBinary = false;
            }
        }

        if (isBinary) {
            byte[] previousData = Base64.decode(itemSplitted.getData().getData().getBytes());
            byte[] nextData     = Base64.decode(sessionState.getNextDataToSend().getBytes());
            byte[] newData      = new byte[previousData.length + nextData.length];
            System.arraycopy(previousData, 0, newData, 0, previousData.length);
            System.arraycopy(nextData, 0, newData, previousData.length, nextData.length);
            itemSplitted.getData().setData(new String(Base64.encode(newData)));
        } else {
            String previousData = itemSplitted.getData().getData();
            String nextData     = sessionState.getNextDataToSend();
            String newData      = previousData + nextData;
            itemSplitted.getData().setData(newData);
        }
    }

    /*
     * Check if the client MaxMsgSize is greater than server minmsgsize.
     * If the client MaxMsgSize is smaller than minmsgsize then change
     * the status code of SyncHdr into StatusCode.SYNCHRONIZATION_FAILED.
     *
     * @param response the SyncML response message
     */
    private void checkMaxMsgSize(SyncML response) {
        if (log.isEnabled(Level.TRACE)) {
            log.trace("Check if the MaxMsgSize is larger of the minimal " +
                       "size of the messages of the server");
        }
        long minMsgSize = 0;
        if (sessionState.getMaxMsgSize() != 0) {
            if (Constants.MIMETYPE_SYNCMLDS_WBXML.equals(mimeType) ||
                Constants.MIMETYPE_SYNCMLDM_WBXML.equals(mimeType)) {
                minMsgSize = Long.parseLong(engine.getConfiguration().getStringValue(ManagementEngine.CFG_MIN_MSGSIZE_WBXML));
            } else if (Constants.MIMETYPE_SYNCMLDS_XML.equals(mimeType) ||
                       Constants.MIMETYPE_SYNCMLDM_XML.equals(mimeType)) {
                minMsgSize = Long.parseLong(engine.getConfiguration().getStringValue(ManagementEngine.CFG_MIN_MSGSIZE_XML));
            }
            if (sessionState.getMaxMsgSize() < minMsgSize) {
                Status statusHdr = (Status)response.getSyncBody().getCommands().get(0);
                statusHdr.setData(new Data(StatusCode.COMMAND_FAILED));

                if (log.isEnabled(Level.INFO)) {
                    log.info("The MaxMsgSize is smaller than minimum size " +
                             "that the server response could have!");
                    log.info("The server will not answer to some message " +
                             "of the client.");
                }
            }
        }
    }

    /**
     * Removes Alert commands from the list of the given commands
     * @param commands List
     *
     * @return an array with the removed alerts
     */
    private Alert[] removeAlerts(List<AbstractCommand> commands) {
        if (commands == null || commands.isEmpty()) {
            return new Alert[0];
        }
        List<AbstractCommand> commandsToRemove = new ArrayList<AbstractCommand>();

        Iterator<AbstractCommand> itCommands = commands.iterator();
        AbstractCommand command = null;
        while (itCommands.hasNext()) {
            command = itCommands.next();
            if (command instanceof Alert) {
                commandsToRemove.add(command);
            }
        }
        commands.removeAll(commandsToRemove);
        return (Alert[])commandsToRemove.toArray(new Alert[0]);
    }


    /**
     * Stores for each commands in the given list, a new entry in the
     * commandsDescriptors map with:
     * key: msgId|cmdId
     * value: commandDescriptor
     * @param msgId String
     * @param commands List
     */
    private void storeManagementCommandDescriptorForManagementCommands(String msgId,
                                                                       List commands) {
        if (commands == null) {
            return;
        }

        Iterator        itCommands = commands.iterator();
        String          cmdId      = null;
        AbstractCommand command    = null;

        ManagementCommandDescriptor
            commandDescriptor = null;

        while (itCommands.hasNext()) {

            command = (AbstractCommand)(itCommands.next());
            if (command instanceof Atomic) {
                storeManagementCommandDescriptorForManagementCommands(msgId,
                                                                      ((Atomic)command).getCommands());
                continue;
            }
            if (command instanceof Sequence) {
                storeManagementCommandDescriptorForManagementCommands(msgId,
                                                                      ((Sequence)command).getCommands());
                continue;
            }
            cmdId = command.getCmdID().getCmdID();
            String key = getCommandDescriptorKey(msgId, cmdId);
            commandDescriptor = createManagementCommandDescriptor(msgId, command);

            sessionState.managementCommandDescriptors.put(key.toString(), commandDescriptor);
        }
    }


    /**
     * Creates a ManagementCommandDescriptor with the given data.
     * @param msgId String
     * @param command AbstractCommand
     * @return ManagementCommandDescriptor
     */
    private ManagementCommandDescriptor createManagementCommandDescriptor(String msgId,
                                                                          AbstractCommand command) {

        if (command instanceof Alert) {
            if (!isAManagementAlert((Alert)command)) {
                //
                // The alert command is not a management alert
                // (it can be a 1222 or a 1223)
                //
                return null;
            }
        }
        ManagementCommandDescriptor commandDescriptor = null;

        if (command instanceof Add    ||
            command instanceof Alert  ||
            command instanceof Copy   ||
            command instanceof Delete ||
            command instanceof Exec   ||
            command instanceof Get    ||
            command instanceof Replace)  {

            commandDescriptor = new ManagementCommandDescriptor(msgId,
                                                                command.getCmdID().getCmdID(),
                                                                command.getName()
                                );
        }


        return commandDescriptor;
    }

    /**
     * Is the given alert a management alert ? A management alert must have code:
     * <br/>
     * <ui>
     *    <li>DISPLAY: 1100</li>
     *    <li>CONFIRM_OR_REJECT: 1101</li>
     *    <li>INPUT: 1102</li>
     *    <li>SINGLE_CHOICE: 1103</li>
     *    <li>MULTIPLE_CHOICE: 1104</li>
     * </ui>
     * @param alert the alert to check
     * @return true if the given alert is a management alert, false otherwise
     */
    private boolean isAManagementAlert(Alert alert) {
        if (alert == null) {
            return false;
        }
        int code = alert.getData();
        switch (code) {
            case AlertCode.DISPLAY:
            case AlertCode.CONFIRM_OR_REJECT:
            case AlertCode.INPUT:
            case AlertCode.SINGLE_CHOICE:
            case AlertCode.MULTIPLE_CHOICE:
                return true;
            default:
                return false;
        }
    }

    /**
     * Removed the status from the given abstract commands that don't have a command descriptor
     * @param commands the commands
     * @param commandDescriptors the command descriptors
     * @return the given commands without status without command descriptor
     */
    private AbstractCommand[] removeStatusWithoutCommandDescriptor(
            AbstractCommand[] commands,
            java.util.Map     commandDescriptors) {

        if (commands == null || commands.length == 0) {
            return commands;
        }

        List<AbstractCommand> commandsToReturn = new ArrayList<AbstractCommand>(commands.length);
        String msgRef = null;
        String cmdRef = null;
        String cmd    = null;
        String key    = null;
        ManagementCommandDescriptor cmdDescriptor = null;
        for (int i=0; i<commands.length; i++) {

            if (!(commands[i] instanceof Status)) {
                commandsToReturn.add(commands[i]);
                continue;
            }
            msgRef = ((Status)commands[i]).getMsgRef();
            cmdRef = ((Status)commands[i]).getCmdRef();
            cmd    = ((Status)commands[i]).getCmd();
            key    = getCommandDescriptorKey(msgRef, cmdRef);
            cmdDescriptor =
                    sessionState.managementCommandDescriptors.get(key);
            if (cmdDescriptor == null) {
                //
                // The status doesn't have a command descriptor
                //
                continue;
            }
            if (!cmdDescriptor.getCommandName().equalsIgnoreCase(cmd)) {
                //
                // The command is different !
                //
                continue;
            }
            commandsToReturn.add(commands[i]);
        }
        return commandsToReturn.toArray(
                new AbstractCommand[commandsToReturn.size()]
        );
    }

    /**
     * Returns the key to use handling command descriptors from the given
     * msgId and cmdId
     * @param msgId the message id
     * @param cmdId the command id
     * @return msgId + "|" + cmdId
     */
    private String getCommandDescriptorKey(String msgId, String cmdId) {
        StringBuffer sb = new StringBuffer(msgId);
        sb.append('|').append(cmdId);
        return sb.toString();
    }
}
