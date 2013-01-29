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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import com.funambol.framework.config.Configuration;
import com.funambol.framework.config.ConfigurationConstants;
import com.funambol.framework.core.AbstractCommand;
import com.funambol.framework.core.Add;
import com.funambol.framework.core.Alert;
import com.funambol.framework.core.AlertCode;
import com.funambol.framework.core.Authentication;
import com.funambol.framework.core.Chal;
import com.funambol.framework.core.ComplexData;
import com.funambol.framework.core.Constants;
import com.funambol.framework.core.Cred;
import com.funambol.framework.core.Data;
import com.funambol.framework.core.Delete;
import com.funambol.framework.core.Get;
import com.funambol.framework.core.Item;
import com.funambol.framework.core.ItemizedCommand;
import com.funambol.framework.core.Map;
import com.funambol.framework.core.MapItem;
import com.funambol.framework.core.Meta;
import com.funambol.framework.core.ModificationCommand;
import com.funambol.framework.core.NextNonce;
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
import com.funambol.framework.core.Util;
import com.funambol.framework.database.Database;
import com.funambol.framework.engine.SyncEngine;
import com.funambol.framework.engine.SyncItem;
import com.funambol.framework.engine.SyncItemImpl;
import com.funambol.framework.engine.SyncItemState;
import com.funambol.framework.engine.SyncOperation;
import com.funambol.framework.engine.source.MemorySyncSource;
import com.funambol.framework.engine.source.SyncSource;
import com.funambol.framework.protocol.ClientCompletion;
import com.funambol.framework.protocol.ClientModifications;
import com.funambol.framework.protocol.Flags;
import com.funambol.framework.protocol.ProtocolException;
import com.funambol.framework.protocol.ProtocolUtil;
import com.funambol.framework.protocol.SyncInitialization;
import com.funambol.framework.security.Officer;
import com.funambol.framework.security.SecurityConstants;
import com.funambol.framework.security.Sync4jPrincipal;
import com.funambol.framework.server.ClientMapping;
import com.funambol.framework.server.LastTimestamp;
import com.funambol.framework.server.Sync4jDevice;
import com.funambol.framework.server.SyncTimestamp;
import com.funambol.framework.server.error.InvalidCredentialsException;
import com.funambol.framework.server.error.ServerException;
import com.funambol.framework.server.session.SessionHandler;
import com.funambol.framework.server.session.SyncState;
import com.funambol.framework.server.store.NotFoundException;
import com.funambol.framework.server.store.PersistentStore;
import com.funambol.framework.server.store.PersistentStoreException;
import com.funambol.framework.tools.ArrayUtils;
import com.funambol.framework.tools.Base64;
import com.funambol.framework.tools.CommandIdGenerator;
import com.funambol.framework.tools.SimpleIdGenerator;
import com.funambol.framework.tools.SizeCalculator;

import com.funambol.server.engine.Sync4jSyncEngine;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;


/**
 * This class represents the handler for a SyncML session. It coordinates and
 * handles the packages and messages as dictated by the protocol.
 * <p>
 * The entry point is <i>processMessage()</i>, which determines which message is
 * expected and what processing has to be done (depending on the value of
 * <i>currentState</i>). If an error accours, the session goes to the state
 * <i>STATE_ERROR</i>; in this state no other messages but initialization can be
 * performed.
 * <p>
 * In the current implementation separate initialization is required.
 * <p>
 * <i>SessionHandler</i> makes use of a <i>SyncEngine</i> for all
 * tasks not related to the handling of the protocol.
 * See <i>com.funambol.framework.engine.SyncEngine</i> for more information.
 *
 * LOG NAME: funambol.handler
 *
 * @see com.funambol.framework.engine.SyncEngine
 *
 * @version $Id: SyncSessionHandler.java,v 1.3 2006/08/07 21:09:25 nichele Exp $
 *
 */
/**
 * @TODO: client challenge handling
 */
public class SyncSessionHandler
implements SessionHandler, java.io.Serializable, ConfigurationConstants, SecurityConstants {

    // --------------------------------------------------------------- Constants

    //
    // NOTE: the following states are in addition to the ones defined in
    //       SessionHandler
    //
    public static final int STATE_INITIALIZATION_PROCESSING  = 0x0010;
    public static final int STATE_INITIALIZATION_PROCESSED   = 0x0011;
    public static final int STATE_SYNCHRONIZATION_PROCESSING = 0x0012;
    public static final int STATE_SYNCHRONIZATION_PROCESSED  = 0x0013;
    public static final int STATE_SYNCHRONIZATION_COMPLETION = 0x0014;


    // ------------------------------------------------------------ Private data

    private int currentState = STATE_START;

    //
    // This data is true for slow sync
    //
    private boolean slow = false;

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

    private transient static final Logger log = Logger.getLogger(com.funambol.server.session.SyncSessionHandler.class.getName());

    /**
     * SyncTimestamp for the current synchronization
     */
    private SyncTimestamp nextTimestamp = null;

    private transient SyncInitialization syncInit = null;
    private transient ClientModifications modifications = null;

    /**
     * This Map contains all the mapping for the databases involved in the
     * synchronization for the current logged principal.<br>
     * The database names are used as keys and the corresponding mapping
     * contains the luig-guid mapping. The map is created and initialized
     * in <i>getClientMappings()</i>
     */
    private java.util.Map<String, ClientMapping> clientMappings = null;

    /**
     * Contain the client device Id for the current session
     */
    private String clientDeviceId = null;

    /**
     * The databases that have to be synchronized. It is set in the initialization
     * process.
     */
    Database[] dbs = null;

    /**
     * The server authentication type
     */
    private String serverAuthType = null;

    /**
     * List of command in order to create an answer with the maximum size
     * available (case of MultiMessage)
     */
    private ArrayList addStatus       = new ArrayList();
    private ArrayList addAlert        = new ArrayList();
    private ArrayList<AbstractCommand> addAbsCmd       = new ArrayList<AbstractCommand>();

    private long maxSizeAvailable = 0;

    // -------------------------------------------------------------- Properties

    /**
     * The session id - read only
     */
    private String sessionId = null;

    public String getSessionId() {
        return this.sessionId;
    }

    /**
     * Returns the device in session
     *
     * @return the device in session
     */
    public Sync4jDevice getDevice() {
        return syncState.device;
    }




    /**
     * The command id generator (it defaults ro a <i>CommandIdGenerator</i> instance)
     */
    private CommandIdGenerator cmdIdGenerator = new CommandIdGenerator();

    public void setCommandIdGenerator(CommandIdGenerator cmdIdGenerator) {
        this.cmdIdGenerator = cmdIdGenerator;
    }

    public CommandIdGenerator getCommandIdGenerator() {
        return this.cmdIdGenerator;
    }

    /**
     * The cmdIdGenerator must be reset each time the process
     * of a message is starting
     */
    private void resetIdGenerator() {
        this.cmdIdGenerator.reset();
        syncEngine.setCommandIdGenerator(this.cmdIdGenerator);
    }

    /**
     * The message id generator (it defaults ro a <i>SimpleIdGenerator</i> instance)
     */
    private SimpleIdGenerator msgIdGenerator = new SimpleIdGenerator();

    /**
     * The Last message Id from the client
     */
    private String lastMsgIdFromClient = null;

    /**
     * The <i>SyncEngine</i>
     */
    private Sync4jSyncEngine syncEngine = null;


    /**
      * Is guest enabled?
      */
    private boolean guestEnabled = false;

    /**
     * The SyncState for the syncronization process
     */
    private SyncState syncState = null;

    /**
      * Set message mime type
      */
    private String mimeType = null;

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    // ---------------------------------------------------------- Public methods

    public SyncEngine getSyncEngine() {
        return this.syncEngine;
    }

    /**
     * Indicates if the session is a new session
     */
    private boolean newSession = true;

    public void setNew(boolean newSession) {
        this.newSession = newSession;
    }

    public boolean isNew() {
        return this.newSession;
    }

    /**
     * Can I send Alert commands into this message?
     * If the server capabilities are required, check if
     * is possible send Capabilities and all Alert commands into only message
     */
    private boolean canSendAlerts = true;

    // ------------------------------------------------------------ Constructors

    /**
     * Creates a new instance of SyncSessionHandler
     */
    public SyncSessionHandler() {
        this.creationTimestamp = System.currentTimeMillis();
        this.syncEngine = new Sync4jSyncEngine(Configuration.getConfiguration());
    }

    /**
     * Creates a new instance of SyncSessionHandler with a given session id
     */
    public SyncSessionHandler(String sessionId) {
        this();
        this.sessionId = sessionId;
    }

    // ---------------------------------------------------------- Public methods

    /**
     * Returns true if the sessione has been authenticated.
     *
     * @return true if the sessione has been authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        return syncState.authenticationState == AUTH_AUTHENTICATED;
    }

    /**
     * Returns true if the session has not been authenticated because of an
     * expired account.
     *
     * @return true if the account is expired
     */
    public boolean isAccountExpired() {
        return syncEngine.getOfficer().isAccountExpired();
    }

    /**
     * Is this the last message?
     */
    private boolean finalMsg = false;

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
    throws ProtocolException, InvalidCredentialsException {
        boolean syncWithInit = false;

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
        lastMsgIdFromClient = message.getSyncHdr().getMsgID();

        //
        // Initialize the device ID from the client request
        //
        clientDeviceId = message.getSyncHdr().getSource().getLocURI();

        if (log.isEnabled(Level.TRACE)) {
            log.trace("current state: " + getStateName(currentState));
        }

        Chal chal = ProtocolUtil.getStatusChal(message);

        try {
            switch (currentState) {
                case STATE_ERROR: // in case of error you can start a new initialization
                case STATE_START:
                    nextTimestamp = new SyncTimestamp();
                    nextTimestamp.start = System.currentTimeMillis();
                    nextTimestamp.tagClient = String.valueOf(nextTimestamp.start);

                    syncState = new SyncState();

                    //
                    // Set maximum message size
                    //
                    Meta meta = message.getSyncHdr().getMeta();
                    if (meta != null) {
                        syncState.setMaxMsgSize(meta.getMaxMsgSize().longValue());
                    }

                    //
                    // read device information from the database. Use this
                    // information with care until the client has been
                    // authenticated.
                    //
                    syncState.device = new Sync4jDevice(clientDeviceId);
                    syncEngine.readDevice(syncState.device);

                    syncState.syncMLVerProto =
                                message.getSyncHdr().getVerProto().getVersion();
                    this.syncEngine.setSyncMLVerProto(syncState.syncMLVerProto);

                    moveTo(STATE_INITIALIZATION_PROCESSING);

                    //
                    // Check if the client has sent the credential with the same
                    // authentication type impose from the server
                    //
                    Cred cred = message.getSyncHdr().getCred();

                    if (!checkAuthType(cred)) {
                        //
                        // the client uses an authentication type different
                        // from authentication server type
                        //
                        syncState.loggedCredential = null;
                        syncState.authenticationState = AUTH_INVALID_CREDENTIALS;

                        if (cred == null && !isGuestEnabled()) {
                            syncState.authenticationState =
                                             syncState.AUTH_MISSING_CREDENTIALS;
                        }

                    } else {
                        login(message.getSyncHdr().getCred(), clientDeviceId);

                        if (isAuthenticated()) {
                            try {
                                syncEngine.readPrincipal(syncState.loggedPrincipal);
                            } catch (NotFoundException e) {
                                if (log.isEnabled(Level.INFO)) {
                                    log.info("Authenticated principal not found:" + syncState.loggedPrincipal);
                                }
                                syncState.authenticationState = AUTH_INVALID_CREDENTIALS;
                                syncState.loggedCredential = null;
                            }
                        }
                    }

                case STATE_INITIALIZATION_PROCESSING:
                    response = processInitMessage(message);

                    if (!isAuthenticated()) {
                        moveTo(STATE_START);
                        break;
                    }

                    moveTo(STATE_INITIALIZATION_PROCESSED);

                case STATE_INITIALIZATION_PROCESSED:

                    //
                    // If the client is authenticated and the authentication
                    // type is MD5, the given client next nonce can be stored
                    // to be used at the next session.
                    //
                    if (isAuthenticated() && chal != null) {
                        if (Constants.AUTH_TYPE_MD5.equals(chal.getType())) {
                            syncState.device.setServerNonce(Base64.decode(chal.getNextNonce().getValue()));
                            syncEngine.storeDevice(syncState.device);
                        }
                    }

                    //
                    //  Checking for message with Sync with Initialization
                    //  If yes, set CurrentState to STATE_SYNCHRONIZATION_PROCESSING
                    //  and proceed ahead for synchornization...
                    //
                    syncWithInit = checkSyncInit(message);
                    if (syncWithInit) {
                        moveTo(STATE_SYNCHRONIZATION_PROCESSING);
                        if (log.isEnabled(Level.TRACE)) {
                            log.trace("Sync message without separate initialization");
                        }
                    } else {
                        if (log.isEnabled(Level.TRACE)) {
                            log.trace("Sync with separate initalization");
                        }
                        if (message.getSyncBody().isFinalMsg()) {
                            moveTo(STATE_SYNCHRONIZATION_PROCESSING);
                        } else {
                            moveTo(STATE_INITIALIZATION_PROCESSING);
                        }
                        break;
                    }

                case STATE_SYNCHRONIZATION_PROCESSING:
                    syncWithInit = checkSyncInit(message);
                    if (syncWithInit) {
                        if (response == null) {
                            response = processInitMessage(message);
                        } else {
                            if (syncState.serverAuthenticationState == AUTH_UNAUTHENTICATED) {
                                Cred credential = checkServerAuthentication(message);
                                response.getSyncHdr().setCred(credential);
                            }
                        }
                    }

                    syncState.setSyncWithInit(syncWithInit);
                    syncState.setResponseInit(response);

                    //
                    // Check if there are Map commands because the client is
                    // always allowed to send the Map operations back to the
                    // server immediatly after adding the items to the client
                    // database.
                    AbstractCommand[] cmds =
                        (AbstractCommand[])message.getSyncBody().getCommands().toArray(new AbstractCommand[0]);
                    List list = ProtocolUtil.filterCommands(cmds, Map.class);
                    if (list.size() > 0) {
                    //
                        // Process message and cache status commands to send it
                        // in the next message.
                        //
                        processCompletionMessage(message);

                        storeClientMappings();
                        resetClientMappings();
                    }

                    response = processSyncMessage(message);

                    storeClientMappings();
                    resetClientMappings();

                    if (message.getSyncBody().isFinalMsg()) {
                        nextTimestamp.end = System.currentTimeMillis();
                        commit();
                        if (ProtocolUtil.noMoreResponse(message)) {
                            moveTo(STATE_SYNCHRONIZATION_PROCESSING);
                        } else {
                            //
                            // The server has not still finished to send the
                            // answer
                            //
                            if (response.getSyncBody().isFinalMsg()) {
                                moveTo(STATE_SYNCHRONIZATION_COMPLETION);
                            } else {
                                moveTo(STATE_SYNCHRONIZATION_PROCESSING);
                                syncState.setResponseFinal(false);
                                break;
                            }
                        }
                    } else {
                        if (syncState.getStatusCmdOut().isEmpty() &&
                            syncState.getAlertCmdOut().isEmpty() &&
                            syncState.getCmdOut().isEmpty() &&
                            response.getSyncBody().isFinalMsg()       ) {

                            if (!syncState.getMapStatusOut().isEmpty()) {
                                finalMsg = false;
                                response.getSyncBody().setFinalMsg(Boolean.FALSE);
                            } else {
                                finalMsg = true;
                                response.getSyncBody().setFinalMsg(Boolean.TRUE);
                            }

                            //
                            // The server changes state even if client message
                            // isn't final because in that message there is
                            // NEXT_MESSAGE request only.
                            //
                            moveTo(STATE_SYNCHRONIZATION_COMPLETION);

                            nextTimestamp.end = System.currentTimeMillis();
                            commit();

                        } else {
                            /**
                             * The Results and Get command must be send before
                             * send the server modifications even if the client
                             * is not final
                             */
                            if (addAbsCmd != null) {
                                for (int i=0; i< addAbsCmd.size(); i++) {
                                    AbstractCommand ac = addAbsCmd.get(i);
                                    if (ac instanceof Results || ac instanceof Get) {
                                        finalMsg = true;
                                        response.getSyncBody().setFinalMsg(Boolean.TRUE);
                                        break;
                                    } else {
                                        finalMsg = false;
                                        response.getSyncBody().setFinalMsg(Boolean.FALSE);
                                    }
                                }
                            } else {
                                finalMsg = false;
                                response.getSyncBody().setFinalMsg(Boolean.FALSE);
                            }
                        }
                    }
                    break;

                case STATE_SYNCHRONIZATION_COMPLETION:
                    response = processCompletionMessage(message);

                    storeClientMappings();
                    resetClientMappings();
                    if (message.getSyncBody().isFinalMsg()) {
                        if (response.getSyncBody().isFinalMsg()) {
                            finalMsg = true;
                            response.setLastMessage(true);
                            moveTo(STATE_END);
                        }
                    } else {
                        finalMsg = false;
                        response.getSyncBody().setFinalMsg(Boolean.FALSE);
                        moveTo(STATE_SYNCHRONIZATION_COMPLETION);
                    }
                    break;
                default:
                    logout();
                    throw new ProtocolException("Illegal state: " + currentState);
            }
        } catch (ProtocolException e) {
            log.debug( "processMessage", e);
            moveTo(STATE_ERROR);
            throw e;
        } catch (NotFoundException e) {
            log.debug( "processMessage", e);
            moveTo(STATE_ERROR);
            throw new InvalidCredentialsException("Invalid credential error", e);
        } catch (PersistentStoreException e) {
            log.debug( "processMessage", e);
            moveTo(STATE_ERROR);
            throw new ProtocolException("Persistent store error", e);
        } catch (Throwable t) {
            t.printStackTrace();
            log.debug( "processMessage", t);
            moveTo(STATE_ERROR);
        }

        if (log.isEnabled(Level.TRACE)) {
            log.trace("About returning message: " + Util.toXML(response));
        }

        return response;
    }

    /**
     * Check if the client authentication type is equals to the server
     * authetication type.
     *
     * @param cred the client credential
     *
     * @return true if client and server auth type match, false otherwise
     */
    private boolean checkAuthType(Cred cred) {

        Officer officer = syncEngine.getOfficer();

        serverAuthType = officer.getAuthType();

        if (cred == null) {
            if (officer.isGuestEnabled()) {
                syncState.authenticationState = syncState.AUTH_AUTHENTICATED;
            } else {
                syncState.authenticationState = syncState.AUTH_MISSING_CREDENTIALS;
            }
            return officer.isGuestEnabled();
        }

        String clientAuthType = cred.getType();

        if (serverAuthType.equalsIgnoreCase(clientAuthType)) {
            return true;
        }

        syncState.authenticationState = syncState.AUTH_AUTHENTICATED;

        return false;
    }

    /**
     * Merges Initalization and Synchronization responses into a single message.
     *
     * @param init Initialization Response Message
     * @param sync Synchronization Response Message
     *
     * @throws sync4.framework.core.RepresentationException in case of a
     *         representation error
     *
     *  @return the combined message
     */
    private SyncML mergeResponse(SyncML init, SyncML sync)
    throws RepresentationException {

        SyncHdr  header = null;
        SyncBody body     = null;

        //
        // The StatusCommand Objects must be sorted for cmdRef.
        //
        TreeMap<Integer, AbstractCommand> tmOtherCmd = new TreeMap<Integer, AbstractCommand>();
        ArrayList<AbstractCommand> alStatus = new ArrayList<AbstractCommand>();
        ArrayList<Object> al = new ArrayList<Object>();

        if (init != null) {
            AbstractCommand[] initCmd =
                (AbstractCommand[])init.getSyncBody().getCommands().toArray(
                                                        new AbstractCommand[0]);
            for(int i=0; i<initCmd.length; ++i) {

                if (initCmd[i] instanceof Status) {
                    alStatus.add(initCmd[i]);
                } else {
                    String key = initCmd[i].getCmdID().getCmdID();
                    tmOtherCmd.put(new Integer(key), initCmd[i]);
                }
            }
        }

        if (sync != null) {
            AbstractCommand[] syncCmd =
                (AbstractCommand[])sync.getSyncBody().getCommands().toArray(
                                                        new AbstractCommand[0]);

            for(int i=0; i<syncCmd.length; ++i){
                if (syncCmd[i] instanceof Status) {
                    boolean isSyncHdr = false;

                    if ("0".equals(((Status)syncCmd[i]).getCmdRef()) &&
                        "SyncHdr".equals(((Status)syncCmd[i]).getCmd())) {

                        for (int a=0;a<alStatus.size();a++) {
                            Status sc = (Status)alStatus.get(a);

                            if(sc.getCmdRef().equals("0") &&
                               sc.getCmd().equals("SyncHdr")) {
                                   isSyncHdr = true;
                                   break;
                            }
                        }
                        if(!isSyncHdr) {
                            alStatus.add(syncCmd[i]);
                        }
                        continue;
                    }

                    alStatus.add(syncCmd[i]);
                } else {
                    String key = syncCmd[i].getCmdID().getCmdID();
                    tmOtherCmd.put(new Integer(key), syncCmd[i]);
                }
            }
        }

        List<Object> list = Arrays.asList(
            ProtocolUtil.sortStatusCommand(alStatus.toArray(new AbstractCommand[0]))
        );

        al.addAll(list);

        java.util.Set<Integer> set1 = tmOtherCmd.keySet();
        java.util.Iterator<Integer> it1 = set1.iterator();
        while(it1.hasNext()) {
            Integer key = it1.next();
            al.add(tmOtherCmd.get(key));
        }

        AbstractCommand[] cmds = al.toArray(new AbstractCommand[0]);
        if (init != null) {
            header = init.getSyncHdr();
            body = new SyncBody(cmds, init.getSyncBody().isFinalMsg());
        } else {
            header = sync.getSyncHdr();
            body = new SyncBody(cmds, sync.getSyncBody().isFinalMsg());
        }

        return (new SyncML(header, body));
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

        Item[] items = new Item[0];
        int status = StatusCode.SERVER_FAILURE;

        if (syncEngine.isDebug()) {
            items = new Item[1];

            items[0] = new Item(
                           null, // target
                           null, // source
                           null, // meta
                           new ComplexData(error.getMessage()),
                           false //MoreData
                       );
        }

        if (error instanceof ServerException) {
            status = ((ServerException)error).getStatusCode();
        }

        Status statusCommand = new Status(
            cmdIdGenerator.next()               ,
            msgHeader.getMsgID()                ,
            "0" /* command ref */               ,
            "SyncHdr" /* see SyncML specs */    ,
            new TargetRef(msgHeader.getTarget()),
            new SourceRef(msgHeader.getSource()),
            null /* credential */               ,
            null /* challenge */                ,
            new Data(status)                    ,
            new Item[0]
        );

        String serverURI =
            syncEngine.getConfiguration().getStringValue(syncEngine.CFG_SERVER_URI);
        SyncHdr syncHeader = new SyncHdr (
            msgHeader.getVerDTD()                     ,
            msgHeader.getVerProto()                   ,
            msgHeader.getSessionID()                  ,
            msgHeader.getMsgID()                      ,
            new Target(msgHeader.getSource().getLocURI()),
            new Source(serverURI)                     ,
            null  /* response URI */                  ,
            false                                     ,
            null /* credentials */                    ,
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
     * extenal causes (i.e. the transport). The current implementation just move
     * the session state to the error state.
     * <p>
     * NOTE that the current implementation simply moves the state of the session
     * to <i>STATE_ERROR</i>.
     *
     * @param statusCode the error code
     *
     * @see com.funambol.framework.core.StatusCode for valid status codes
     *
     */
    public void abort(int statusCode) {
        moveTo(STATE_ERROR);
    }

    /**
     * Called to permanently commit the synchronization. It does the following:
     * <ul>
     *  <li>persists the <i>last</i> timestamp to the database for the sources
     *      successfully synchronized
     * </ul>
     */
    public void commit() {
        assert (syncState.loggedPrincipal != null);

        LastTimestamp last = null;

        PersistentStore ps = syncEngine.getStore();

        Database[] dbsGen = syncEngine.getDbs();

        for (int i = 0; (dbsGen != null) && (i < dbsGen.length); ++i) {
            if (dbsGen[i].getStatusCode() != StatusCode.OK) {
                //
                // This database is in error. Do not commit it.
                //
                continue;
            }
            last = new LastTimestamp(
                syncState.loggedPrincipal.getId(),
                dbsGen[i].getName(),
                dbsGen[i].getAnchor().getNext(),
                dbsGen[i].getServerAnchor().getNext(),
                nextTimestamp.start,
                nextTimestamp.end
            );

            if (log.isEnabled(Level.TRACE)) {
                log.trace("Commiting database "
                        + dbsGen[i].getName()
                        + " ( "
                        + last
                        + " )"
                );
            }

            try {
                boolean stored = ps.store(last);
                log.trace("LastTimeStamp stored: " + stored);
            } catch (Sync4jException e) {
                if(log.isEnabled(Level.FATAL)){
                    log.fatal("Error in saving persistent data");
                }
                log.debug( "commit", e);
            }
        }
    }

    // --------------------------------------------------------- Private methods

    /**
     * If property "isGuestEnabled" is true and credential is null use the
     * credential of user guest
     *
     * @return true if the property "isGuestEnabled" is true else false
     */
    private boolean isGuestEnabled() {
        return this.guestEnabled;
    }

    /**
     * Processes the given initialization message.
     *
     * @param message the message to be processed
     *
     * @return the response message
     *
     * @throws ProtocolException
     */
    private SyncML processInitMessage(SyncML message)
    throws ProtocolException {
        if (log.isEnabled(Level.TRACE)) {
            log.trace("Processing the given initialization message");
        }
        try {
            syncInit = new SyncInitialization(message.getSyncHdr() ,
                                              message.getSyncBody());

            //
            //Store the informations that will be used in the Syncronization process
            //
            syncState.addClientAlerts(syncInit.getClientAlerts());

            syncInit.setIdGenerator(cmdIdGenerator);
            syncInit.setFlag(Flags.FLAG_FINAL_MESSAGE);

            //
            // Sets the server authentication type so that the server will be able
            // to accordingly challenge the client
            //
            syncInit.setServerAuthType(serverAuthType);

            //
            // If authentication type is MD5 then generate a new NextNonce
            //
            if (serverAuthType.equalsIgnoreCase(Constants.AUTH_TYPE_MD5)) {
                NextNonce nonce = ProtocolUtil.generateNextNonce();
                syncInit.setNextNonce(nonce);
                syncState.device.setClientNonce(nonce.getValue());

                syncEngine.storeDevice(syncState.device);
            }

            if (isAuthenticated()) {
                syncInit.setAuthorizedStatusCode(StatusCode.AUTHENTICATION_ACCEPTED);

                syncInit.setClientCapabilitiesRequired(false);

                //
                // Gets the databases requested for synchronization and for each of
                // them checks if the database exists on the server and if the
                // credential is allowed to synchronize it
                //
                dbs = syncInit.getDatabasesToBeSynchronized();

                //
                // This will change the status code of the elements of clientDBs to
                // reflect the availability and accessibility of the given databases
                // on the server
                //
                syncEngine.prepareDatabases(syncState.loggedPrincipal, dbs, nextTimestamp);

                if (log.isEnabled(Level.TRACE)) {
                    log.trace("Requested databases: " + Arrays.asList(dbs));
                }

                boolean noDataSource = true; // there are no datasource to allowed to synchronize

                for (int i = 0; ((dbs != null) && (i < dbs.length)); ++i) {
                    syncInit.setStatusCodeForCommand(
                            dbs[i].getAlertCommand(),
                            dbs[i].getStatusCode()
                    );

                    if (dbs[i].getStatusCode() == StatusCode.OK) {
                        noDataSource = false;
                        syncEngine.addClientSource(
                                new MemorySyncSource(
                                    dbs[i].getName(),
                                    null,
                                    dbs[i].getSource().getLocURI())
                        );
                    }
                }

                syncEngine.setDbs(dbs);

                //
                // Setting the databases to synchronize. This will force the relative
                // <Alert>s to be inserted in the response.
                //
                syncInit.setDatabases(dbs);

                //
                // Setting server capabilities
                //
                syncInit.setServerCapabilities(
                    syncEngine.getServerCapabilities(syncInit.getDTDVersion())
                );

            } else {
                if (isAccountExpired()) {
                    syncInit.setAuthorizedStatusCode(StatusCode.PAYMENT_REQUIRED);
                    } else if (syncState.authenticationState == AUTH_MISSING_CREDENTIALS) {
                    syncInit.setAuthorizedStatusCode(StatusCode.MISSING_CREDENTIALS);
                    } else if (syncState.authenticationState == AUTH_INVALID_CREDENTIALS) {
                    syncInit.setAuthorizedStatusCode(StatusCode.INVALID_CREDENTIALS);
                } else {
                    syncInit.setAuthorizedStatusCode(StatusCode.FORBIDDEN);
                }
            }

            //
            // Now we set server credentials. If the client challenged the server,
            // we use the requested auth mechanism, otherwise, we use the default
            // server authentication scheme.
            // Not that if credentials are not set in server configuration,
            // getServerCredential returns null, which means that no credentials
            // will be returned to the client.
            //
            Chal chal = getChal(message);

            boolean credServerSend = !(syncEngine.getOfficer().getAuthType().equals(Constants.AUTH_TYPE_NONE));

            if (chal == null) {
                if (!credServerSend) {
                    syncInit.setServerCredentials(null);
                    syncState.serverAuthenticationState = AUTH_AUTHENTICATED;
                } else {
                    Meta meta = new Meta();
                    meta.setType(this.serverAuthType);
                    meta.setNextNonce(new NextNonce(Base64.encode(syncState.device.getServerNonce())));
                    chal = new Chal(meta);
                    syncInit.setServerCredentials(syncEngine.getServerCredentials(chal, syncState.device));
                }
            } else {
                syncInit.setServerCredentials(syncEngine.getServerCredentials(chal, syncState.device));

                Cred cred = checkServerAuthentication(message);
                if (cred != null) {
                    syncInit.setServerCredentials(cred);
                }
            }

            SyncML response = syncInit.getResponse(msgIdGenerator.current());
            response = mergeResponse(response, null);

            if (!message.getSyncBody().isFinalMsg()) {
                response.getSyncBody().setFinalMsg(Boolean.FALSE);
            }

            //
            // Calculate size of response message
            //
            SyncHdr  syncHdr  = response.getSyncHdr() ;
            SyncBody syncBody = response.getSyncBody();
            long sizeSyncHdr  = 0, sizeSyncBody = 0;

            if (Constants.MIMETYPE_SYNCMLDS_WBXML.equals(mimeType)) {
                sizeSyncHdr  = SizeCalculator.getWBXMLSize(syncHdr);
                sizeSyncBody = SizeCalculator.getWBXMLSize(syncBody);
            } else if (Constants.MIMETYPE_SYNCMLDS_XML.equals(mimeType)) {
                sizeSyncHdr  = SizeCalculator.getXMLSize(syncHdr);
                sizeSyncBody = SizeCalculator.getXMLSize(syncBody);
            }

            syncState.setOverheadHdr(sizeSyncHdr);
            maxSizeAvailable = syncState.getMaxMsgSize();
            //
            // Check if the client MaxMsgSize is greater then the server
            // minmsgsize
            //
            checkMaxMsgSize(response);

            if ((maxSizeAvailable >= sizeSyncHdr + sizeSyncBody ||
                 maxSizeAvailable == 0)                  &&
                 syncState.getStatusCmdOut().isEmpty() &&
                 syncState.getAlertCmdOut().isEmpty()  &&
                 syncState.getCmdOut().isEmpty()
               ) {
                return response;
            }

            //
            // Cache the commands to send in the next message
            //
            cacheCommands(response);

            //
            // The size of the answer is greater then the allowed MaxMsgSize
            // Calculate size of the single commands of the response.
            // Create one answer of the allowed dimension.
            //
            return createNextInitMsg(response);
        } catch (Sync4jException e) {
            throw new ProtocolException(e);
        }
    }

    /**
     * Create the next Initialization message with commands presents into queues
     *
     * @param syncML the hypothetical answer
     *
     * @return the server response
     */
    private SyncML createNextInitMsg(SyncML syncML) throws Sync4jException {
        if (log.isEnabled(Level.TRACE)) {
            log.trace("Create Initialization Message");
        }

        //
        // If the server capabilities are required check if they can be inserted
        // into the response composed from only SyncHdr and SyncHdr Status and
        // the capabilities. If the size capabilities is greater than that
        // size then set Get Status with StatusCode.REQUESTED_ENTITY_TOO_LARGE
        // and not send capabilities.
        //
        checkSizeCapabilities(syncML);

        maxSizeAvailable = calculateSizeAvailable();

        if (log.isEnabled(Level.TRACE)) {
            log.trace("How many Status can I include into the response?");
        }
        howManyStatus(syncState.getStatusCmdOut());
        //
        // Remove from queue the Status inserted into response
        //
        syncState.removeStatusCmdOut(addStatus);

        if (log.isEnabled(Level.TRACE)) {
            log.trace("Can I include server capabilities into the response?");
        }

        ArrayList commandList = new ArrayList();
        int size = addStatus.size();
        commandList.addAll(addStatus);

        if (canSendAlerts) {

            if (log.isEnabled(Level.TRACE)) {
                log.trace("How many Alert (if presents) can I include into the response?");
            }
            howManyAlert();

            //
            // Remove from queue the Alert inserted into response
            //
            syncState.removeAlertCmdOut(addAlert);

            if (log.isEnabled(Level.TRACE)) {
                log.trace("How many AbstractCommand can I include into the response?");
            }
            howManyAbstractCommand(true);
            //
            // Remove from queue the AbstractCommand inserted into response
            //
            syncState.removeCmdOut(addAbsCmd);
        }

        size = addAlert.size() + addAbsCmd.size();
        commandList.addAll(addAlert);
        commandList.addAll(addAbsCmd);

        AbstractCommand[] absCommands =
              (AbstractCommand[])commandList.toArray(new AbstractCommand[size]);

        SyncBody responseBody = new SyncBody(absCommands, true);

        if (!syncML.getSyncBody().isFinalMsg() ||
             !syncState.getStatusCmdOut().isEmpty() ||
             !syncState.getAlertCmdOut().isEmpty() ||
             !syncState.getCmdOut().isEmpty() ) {
            responseBody.setFinalMsg(Boolean.FALSE);
        }

        return new SyncML(syncML.getSyncHdr(), responseBody);
    }

    /**
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
        if (syncState.getMaxMsgSize() != 0) {
            if (Constants.MIMETYPE_SYNCMLDS_WBXML.equals(mimeType)) {
                minMsgSize = Long.parseLong(syncEngine.getConfiguration().getStringValue(syncEngine.CFG_MIN_MSGSIZE_WBXML));
            } else if (Constants.MIMETYPE_SYNCMLDS_XML.equals(mimeType)) {
                minMsgSize = Long.parseLong(syncEngine.getConfiguration().getStringValue(syncEngine.CFG_MIN_MSGSIZE_XML));
            }
            if (syncState.getMaxMsgSize() < minMsgSize) {
                Status statusHdr = (Status)response.getSyncBody().getCommands().get(0);
                statusHdr.setData(new Data(StatusCode.SYNCHRONIZATION_FAILED));

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
     * Check if are required the server capabilities and check their size.
     * If that size added at size of SyncHdr, added at SyncHdr Status and at all
     * Alert command is more than MaxMsgSize then set Get Status with
     * StatusCode.REQUESTED_ENTITY_TOO_LARGE
     *
     * @param response the SyncML response
     */
    private void checkSizeCapabilities(SyncML response) {
        if (log.isEnabled(Level.TRACE)) {
            log.trace("If server capabilities are required then check their " +
                       "size in order to establish if will be possible " +
                       "to send them");
        }

        long sizeFree = calculateSizeAvailable();

        Status statusGet = ProtocolUtil.filterStatus(
            (AbstractCommand[])response.getSyncBody().getCommands().toArray(new AbstractCommand[0]),
            Status.class,
            "Get"
        );

        if (statusGet != null) {
            ArrayList<AbstractCommand> results = ProtocolUtil.filterCommands(
                (AbstractCommand[])response.getSyncBody().getCommands().toArray(new AbstractCommand[0]),
                Results.class
            );

            if (results.size() == 1) {
                //
                // Calculate size Results
                //
                long sizeCap = 0;
                if (Constants.MIMETYPE_SYNCMLDS_WBXML.equals(mimeType)) {
                    sizeCap  = SizeCalculator.getWBXMLSize((Results)results.get(0));
                } else if (Constants.MIMETYPE_SYNCMLDS_XML.equals(mimeType)) {
                    sizeCap  = SizeCalculator.getXMLSize((Results)results.get(0));
                }

                if (sizeFree < sizeCap) {
                    if (log.isEnabled(Level.INFO)) {
                        log.info("Will not be NEVER possible to send " +
                                 "the server capabilities!");
                        log.info("This command will be removed from the " +
                                 "list of the commands to send to the client");
                    }
                    statusGet.setData(new Data(StatusCode.REQUESTED_ENTITY_TOO_LARGE));

                    syncState.removeCmdOut(results);

                    canSendAlerts = false;
                } else {
                    canSendAlerts = true;
                }
            }
        }
    }

    /**
     * Calculate the size available for the response message
     *
     * @return the size available for the response
     */
    private long calculateSizeAvailable() {
        long overhead = 0;
        if (Constants.MIMETYPE_SYNCMLDS_WBXML.equals(mimeType)) {
            overhead += SizeCalculator.getWBXMLOverheadSyncML()
                     +  SizeCalculator.getWBXMLOverheadSyncBody()
                     ;
        } else if (Constants.MIMETYPE_SYNCMLDS_XML.equals(mimeType)) {
            overhead += SizeCalculator.getXMLOverheadSyncML()
                     +  SizeCalculator.getXMLOverheadSyncBody()
                     ;
        }

        return syncState.getMaxMsgSize()
               -  overhead
               -  syncState.getOverheadHdr()
               -  syncState.getSizeStatusSyncHdr()
               ;
    }

    /**
     * Extracts or create a Chal object that represents how server credentials
     * should be created.<br>
     * If the client challenged the server, we must use the requested auth
     * mechanism, otherwise, we use the default server authentication scheme.
     *
     * @param msg the request message
     *
     * @return a Chal object reqpresenting how server credentials should be
     *         formatted.
     */
    private Chal getChal(final SyncML msg) {
        Chal chal = ProtocolUtil.getStatusChal(msg);
        if (chal != null) {
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
    private Cred checkServerAuthentication(SyncML msg)
    throws ProtocolException {
        int headerStatusCode = ProtocolUtil.getHeaderStatusCode(msg);
        if (headerStatusCode == -1 || headerStatusCode == StatusCode.OK) {
            return null;
        }

        if ((headerStatusCode == StatusCode.INVALID_CREDENTIALS) ||
            (headerStatusCode != StatusCode.AUTHENTICATION_ACCEPTED)
           ) {
            if (
                (serverAuthType.equalsIgnoreCase(Constants.AUTH_TYPE_MD5)   ) &&
                (syncState.serverAuthenticationState == AUTH_UNAUTHENTICATED)
               ) {
                   syncState.serverAuthenticationState = AUTH_RETRY_1;
                   return syncEngine.getServerCredentials(getChal(msg), syncState.device);
            } else {
                throw new ProtocolException("Unable to authenticate to the client");
            }
        }

        //
        // Authenticated!
        //
        syncState.serverAuthenticationState = AUTH_AUTHENTICATED;
        return null;
    }

    /**
     * Processes the given synchronization message.
     *
     * @param syncRequest the message to be processed
     *
     * @return the response message
     *
     * @throws ProtocolException
     */
    private SyncML processSyncMessage(SyncML syncRequest)
    throws ProtocolException {

        if (log.isEnabled(Level.TRACE)) {
            log.trace("Processing the given synchronization message");
            log.trace("client sources: " + syncEngine.getClientSources());
        }

        try {
            modifications =
                    new ClientModifications(syncRequest.getSyncHdr() ,
                                            syncRequest.getSyncBody(),
                                            syncEngine.getDbs());

            Sync[] syncCommands = modifications.getClientSyncCommands();
            if (syncCommands == null || syncCommands.length == 0) {
                throw new ProtocolException("Sync command expected in this package but no Sync commands were found");
            }

            List responseCommands = processModifications(modifications);

            if (log.isEnabled(Level.TRACE)) {
                log.trace("responseCommands: " + responseCommands);
            }

            modifications.setIdGenerator(cmdIdGenerator);
            modifications.setFlag(Flags.FLAG_ALL_RESPONSES_REQUIRED);

            Status[] statusSyncs = ProtocolUtil.filterCommands(
                responseCommands,
                new String[]{ Status.COMMAND_NAME }
            ).toArray(new Status[0]);

            Status[] statusMaps = (Status[])syncState.getMapStatusOut().toArray(new Status[0]);

            if (statusMaps != null && statusMaps.length > 0) {
                syncState.removeMapStatusOut(Arrays.asList(statusMaps));
                statusSyncs = (Status[])ArrayUtils.mergeArrays(statusSyncs, statusMaps, Status.class);
            }

            modifications.setClientModificationsStatus(statusSyncs);

            // .setServerModifications sets all the modification commands
            // required to be send to client from Server

            // cannot block this whole code,as this code may be used
            // by other DBS within same sync but differnt alert code.
            AbstractCommand[] serverModifications =
                    ProtocolUtil.filterCommands(
                        responseCommands,
                        new String[]{Sync.COMMAND_NAME}
                    ).toArray(new AbstractCommand[0]);

            syncState.setServerModifications(serverModifications);

            //
            //the server must be send all modification command only when the
            //client has sent the last message
            //
            if (syncRequest.getSyncBody().isFinalMsg()) {
                modifications.setServerModifications(syncState.getServerModifications());
                modifications.setFlag(Flags.FLAG_FINAL_MESSAGE);
            } else {
                //
                // Sort AbstractCommand for CmdID
                //
                List listCmd =
                    Arrays.asList(
                           ProtocolUtil.sortAbstractCommand(serverModifications)
                    );
                //
                // Cache Sync commands
                //
                syncState.addCmdOut(new LinkedList(listCmd));
            }

            SyncML response = modifications.getResponse(msgIdGenerator.current());

            if (syncState.isSyncWithInit()) {
                response = mergeResponse(syncState.getResponseInit(), response);
            } else {
                response = mergeResponse(null, response);
            }

            //
            // Calculate size of response message
            //
            SyncHdr  syncHdr  = response.getSyncHdr() ;
            SyncBody syncBody = response.getSyncBody();
            long sizeSyncHdr  = 0, sizeSyncBody = 0;
            if (Constants.MIMETYPE_SYNCMLDS_WBXML.equals(mimeType)) {
                sizeSyncHdr  = SizeCalculator.getWBXMLSize(syncHdr);
                sizeSyncBody = SizeCalculator.getWBXMLSize(syncBody);
            } else if (Constants.MIMETYPE_SYNCMLDS_XML.equals(mimeType)) {
                sizeSyncHdr  = SizeCalculator.getXMLSize(syncHdr);
                sizeSyncBody = SizeCalculator.getXMLSize(syncBody);
            }

            syncState.setOverheadHdr(sizeSyncHdr);
            maxSizeAvailable = syncState.getMaxMsgSize();

            if ( (maxSizeAvailable >= sizeSyncHdr + sizeSyncBody ||
                 maxSizeAvailable == 0) &&
                 syncState.getStatusCmdOut().isEmpty() &&
                 syncState.getAlertCmdOut().isEmpty() &&
                 syncState.getCmdOut().isEmpty()
               ) {

                return response;
            }

            //
            // Cache the commands to send in the next message
            //
            cacheCommands(response);

            //
            // The size of the answer is greater then the allowed MaxMsgSize
            // Calculate size of the single commands of the response.
            // Create one answer of the allowed dimension.
            // Cache the commands to send in the next message
            //
            if (log.isEnabled(Level.TRACE)) {
                log.trace("Cache Status and AbstractCommand to send in the next msg");
            }

            maxSizeAvailable = calculateSizeAvailable();

            if (log.isEnabled(Level.TRACE)) {
                log.trace("How many Status can I include into the response?");
            }
            howManyStatus(syncState.getStatusCmdOut());
            modifications.setClientModificationsStatus(
                                    (Status[])addStatus.toArray(new Status[0]));

            //
            // Remove from queue the Status inserted into response
            syncState.removeStatusCmdOut(addStatus);

            if (log.isEnabled(Level.TRACE)) {
                log.trace("How many Alert (if presents) can I include into the response?");
            }
            howManyAlert();
            modifications.setModificationsAlert(
                                    (Alert[])addAlert.toArray(new Alert[0]));

            //
            // Remove from queue the Alert inserted into response
            //
            syncState.removeAlertCmdOut(addAlert);

            if (log.isEnabled(Level.TRACE)) {
                log.trace("How many AbstractCommand can I include into the response?");
            }
            howManyAbstractCommand(false);
            //
            // Remove from queue the AbstractCommand inserted into response
            //
            syncState.removeCmdOut(addAbsCmd);

            if (log.isEnabled(Level.TRACE)) {
                log.trace("Add server modifications to response");
            }
            modifications.setServerModifications(
                 addAbsCmd.toArray(new AbstractCommand[0]));
            //
            // The server must be send all modification command only when the
            // client has sent the last message and when there aren't more
            // Status command into queue
            //
            if ( syncRequest.getSyncBody().isFinalMsg()  &&
                 syncState.getStatusCmdOut().isEmpty() &&
                 syncState.getAlertCmdOut().isEmpty()
               ) {
                howManyAbstractCommand(true);
                modifications.setServerModifications(
                     addAbsCmd.toArray(new AbstractCommand[0]));

                //
                // Remove from queue the AbstractCommand inserted into response
                //
                syncState.removeCmdOut(addAbsCmd);

                //
                // If there aren't more AbstractCommand into queue then the
                // message is set how Final
                //
                if (syncState.getCmdOut().isEmpty()) {
                    modifications.setFlag(Flags.FLAG_FINAL_MESSAGE);
                } else {
                    modifications.setFlagValue(Flags.FLAG_FINAL_MESSAGE, false);
                }
            } else {
                //
                // Check if the client send only Status and if there is not
                // a final into message
                //
                if (ProtocolUtil.noMoreResponse(syncRequest)) {
                    howManyAbstractCommand(true);
                    modifications.setServerModifications(
                         addAbsCmd.toArray(new AbstractCommand[0]));
                    //
                    // Remove from queue the AbstractCommand inserted into response
                    //
                    syncState.removeCmdOut(addAbsCmd);
                    modifications.setFlagValue(Flags.FLAG_FINAL_MESSAGE, true);
                } else {

                    /**
                     * The Results and Get command must be send before
                     * send the server modifications even if the client
                     * is not final
                     */
                    if (addAbsCmd != null) {
                        for (int i=0; i< addAbsCmd.size(); i++) {
                            AbstractCommand ac = addAbsCmd.get(i);
                            if (ac instanceof Results || ac instanceof Get) {
                                modifications.setFlagValue(Flags.FLAG_FINAL_MESSAGE, true);
                                //
                                // Remove from queue the AbstractCommand
                                // inserted into response
                                //
                                syncState.removeCmdOut(addAbsCmd);
                                break;
                            } else {
                                modifications.setFlagValue(Flags.FLAG_FINAL_MESSAGE, false);
                            }
                        }
                    } else {
                        modifications.setFlagValue(Flags.FLAG_FINAL_MESSAGE, false);
                    }
                }
            }

            return modifications.getResponse(msgIdGenerator.current());

        } catch (Sync4jException e) {

            //
            // Check if client required Next_Message and if latest server
            // response wasn't Final
            //
            checkAlertNextMessage(syncRequest);

            try {
                return createNextSyncMsg(syncRequest);
            } catch(Sync4jException ex) {
                throw new ProtocolException(ex);
            }
        }
    }

    /**
     * Check if client required Next_Message and if latest server
     * response wasn't Final
     */
    private void checkAlertNextMessage(SyncML request) {
        ArrayList commands = request.getSyncBody().getCommands();
        ArrayList alerts   =
            ProtocolUtil.filterCommands(
                (AbstractCommand[])commands.toArray(new AbstractCommand[0]),
                Alert.class
            );
        for (int i=0; alerts != null && i<alerts.size(); i++) {
            Alert alert = (Alert)alerts.get(i);
            if (alert.getData() == AlertCode.NEXT_MESSAGE) {

                if (log.isEnabled(Level.TRACE)) {
                    log.trace("Received Alert with code NEXT_MESSAGE");
                }
                Status statusAlert = new Status(
                    cmdIdGenerator.next()               ,
                    request.getSyncHdr().getMsgID()     ,
                    alert.getCmdID().getCmdID()         ,
                    Alert.COMMAND_NAME                  ,
                    (TargetRef[])null /* target refs */ ,
                    (SourceRef[])null /* source refs */ ,
                    null /* credential */               ,
                    null /* chal */                     ,
                    new Data(StatusCode.OK)             ,
                    null /* items */
                );

                addStatus = new ArrayList();
                addStatus.add(statusAlert);
                syncState.addStatusCmdOut(new LinkedList(addStatus));
                break;
            }
        }
    }

    /**
     * Create the next Synchronization message with Status and AbstractCommand
     * presents into queues
     *
     * @param syncML the hypothetical answer
     *
     * @return the server response
     */
    private SyncML createNextSyncMsg(SyncML syncML) throws Sync4jException {
        if (log.isEnabled(Level.TRACE)) {
            log.trace("Create Synchronization Message");
        }

        maxSizeAvailable = calculateSizeAvailable();

        Status[] allStatus = (Status[])syncState.getStatusCmdOut().toArray(new Status[0]);
        Status[] statusMaps = (Status[])syncState.getMapStatusOut().toArray(new Status[0]);
        if (statusMaps != null) {
            syncState.removeMapStatusOut(Arrays.asList(statusMaps));
            allStatus = (Status[])ArrayUtils.mergeArrays(allStatus, statusMaps, Status.class);
        }

        if (log.isEnabled(Level.TRACE)) {
            log.trace("How many Status can I include into the response?");
        }
        howManyStatus(Arrays.asList(allStatus));

        //
        // Remove from queue the Status inserted into response
        //
        syncState.removeStatusCmdOut(addStatus);

        if (log.isEnabled(Level.TRACE)) {
            log.trace("How many Alert (if presents) can I include into the response?");
        }
        howManyAlert();
        //
        // Remove from queue the Alert inserted into response
        //
        syncState.removeAlertCmdOut(addAlert);

        modifications = new ClientModifications(syncML.getSyncHdr(), syncML.getSyncBody());
        modifications.setIdGenerator(cmdIdGenerator);
        modifications.setFlag(Flags.FLAG_ALL_RESPONSES_REQUIRED);

        modifications.setClientModificationsStatus(
                    ProtocolUtil.filterCommands(
                            addStatus,
                            new String[]{ Status.COMMAND_NAME }
                    ).toArray(new Status[0])
            );

        modifications.setModificationsAlert(
                    ProtocolUtil.filterCommands(
                            addAlert,
                            new String[]{ Alert.COMMAND_NAME }
                    ).toArray(new Alert[0])
            );
        //
        // The server must be send all modification command only when the
        // client has sent the last message and when there aren't more
        // Status command into queue
        //
        if (syncState.getStatusCmdOut().isEmpty() &&
            syncState.getAlertCmdOut().isEmpty() ) {

            // .setServerModifications sets all the modification commands
            // required to be send to client from Server

            // cannot block this whole code,as this code may be used
            // by other DBS within same sync but differnt alert code.
            if (log.isEnabled(Level.TRACE)) {
                log.trace("How many AbstractCommand can I include into the response?");
            }
            howManyAbstractCommand(true);

            if (log.isEnabled(Level.TRACE)) {
                log.trace("Add server modifications to response");
            }
            modifications.setServerModifications(addAbsCmd.toArray(new AbstractCommand[0]));
            //
            // Remove from queue the AbstractCommand inserted into response
            //
            syncState.removeCmdOut(addAbsCmd);

            //
            // If there aren't more AbstractCommand into queue then the
            // message is set how Final
            //
            if (syncState.getCmdOut().isEmpty()) {
                modifications.setFlag(Flags.FLAG_FINAL_MESSAGE);
            }
        } else {
            modifications.setServerModifications(null);
            modifications.setFlagValue(Flags.FLAG_FINAL_MESSAGE, false);
        }

        return modifications.getResponse(msgIdGenerator.current());
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
                                           new String[]{ Status.COMMAND_NAME }
                            );
        //
        // Sort Status for CmdRef
        //
        List listStatus = Arrays.asList(ProtocolUtil.sortStatusCommand(
                                        statusCmdOut.toArray(new Status[0]))
                                       );
        //
        // Cache Status commands: no Status SyncHdr
        //
        syncState.addStatusCmdOut(new LinkedList(listStatus));
        Status statusSyncHdr = ProtocolUtil.filterStatus(
            (Status[])listStatus.toArray(new Status[0]),
            Status.class,
            "SyncHdr"
        );

        if (Constants.MIMETYPE_SYNCMLDS_WBXML.equals(mimeType)) {
            syncState.setSizeStatusSyncHdr(SizeCalculator.getWBXMLSize(statusSyncHdr));
        } else if (Constants.MIMETYPE_SYNCMLDS_XML.equals(mimeType)) {
            syncState.setSizeStatusSyncHdr(SizeCalculator.getXMLSize(statusSyncHdr));
        }

        //
        // Cache Status for SyncHdr to use (in order to create the asnwer) when
        // the server isn't in the state "STATE_SYNCHRONIZATION_PROCESSING"
        //
        syncState.setStatusSyncHdr(statusSyncHdr);
        ArrayList removeStatus = new ArrayList();
        removeStatus.add(statusSyncHdr);
        syncState.removeStatusCmdOut(removeStatus);

        //
        // Filter AbstractCommand commands
        // (in particular the Sync, Results and Get commands)
        //
        List<AbstractCommand> cmdOut = ProtocolUtil.filterCommands(
                                     response.getSyncBody().getCommands(),
                                     new String[]{ Results.COMMAND_NAME,
                                                   Get.COMMAND_NAME    ,
                                                   Sync.COMMAND_NAME
                                                 }
                                    );
        //
        // Sort AbstractCommand for CmdID
        //
        List listCmd = Arrays.asList(ProtocolUtil.sortAbstractCommand(
                                     cmdOut.toArray(new AbstractCommand[0]))
                                    );
        //
        // Cache Sync commands
        //
        syncState.addCmdOut(new LinkedList(listCmd));

        //
        // Cache Alert commands
        //
        List<AbstractCommand> listAlert = ProtocolUtil.filterCommands(
                                         response.getSyncBody().getCommands(),
                                         new String[] { Alert.COMMAND_NAME}
                                       );
        syncState.addAlertCmdOut(new LinkedList<AbstractCommand>(listAlert));
    }

    /**
     * Cache the status commands for Map to send in the next message
     */
    private void cacheMapCommands(SyncML response) {
        //
        // Filter Status commands
        //
        List<AbstractCommand> mapStatusOut = ProtocolUtil.filterCommands(
                                           response.getSyncBody().getCommands(),
                                           new String[]{ Status.COMMAND_NAME }
                            );
        //
        // Sort Status for CmdRef
        //
        List listStatus = Arrays.asList(ProtocolUtil.sortStatusCommand(
                                        mapStatusOut.toArray(new Status[0]))
                                       );
        //
        // Cache Status commands: no Status SyncHdr
        //
        syncState.addMapStatusOut(new LinkedList(listStatus));
        Status statusSyncHdr = ProtocolUtil.filterStatus(
            (Status[])listStatus.toArray(new Status[0]),
            Status.class,
            "SyncHdr"
        );

        if (Constants.MIMETYPE_SYNCMLDS_WBXML.equals(mimeType)) {
            syncState.setSizeStatusSyncHdr(SizeCalculator.getWBXMLSize(statusSyncHdr));
        } else if (Constants.MIMETYPE_SYNCMLDS_XML.equals(mimeType)) {
            syncState.setSizeStatusSyncHdr(SizeCalculator.getXMLSize(statusSyncHdr));
        }

        //
        // Cache Status for SyncHdr to use (in order to create the asnwer) when
        // the server isn't in the state "STATE_SYNCHRONIZATION_PROCESSING"
        //
        syncState.setStatusSyncHdr(statusSyncHdr);
        ArrayList removeStatus = new ArrayList();
        removeStatus.add(statusSyncHdr);
        syncState.removeMapStatusOut(removeStatus);
    }

    /**
     * Calculates how many Status can be sent
     */
    private void howManyStatus(List allStatus) {
        addStatus = new ArrayList();
        if (currentState != STATE_SYNCHRONIZATION_PROCESSING) {
            addStatus.add(syncState.getStatusSyncHdr());
        }

        //
        // Number of Status to insert into response
        //
        int x = 0;

        for (int i=0; allStatus != null && i<allStatus.size(); i++) {
            Status status = (Status)allStatus.get(i);

            //
            // Calculate size Status
            //
            long size = 0;
            if (Constants.MIMETYPE_SYNCMLDS_WBXML.equals(mimeType)) {
                size  = SizeCalculator.getWBXMLSize(status);
            } else if (Constants.MIMETYPE_SYNCMLDS_XML.equals(mimeType)) {
                size  = SizeCalculator.getXMLSize(status);
            }

            if (maxSizeAvailable - size >= 0) {
                addStatus.add((Status)allStatus.get(i));
                maxSizeAvailable -= size;
                x++;
                continue;
            }
            break;
        }

        if (log.isEnabled(Level.TRACE)) {
            log.trace("Number of Status inserted: " + x);
        }
    }

    /**
     * Calculates how many Alert can be sent
     */
    private void howManyAlert() {
        addAlert = new ArrayList();

        //
        // Number of Alert to insert into response
        //
        int x = 0;
        LinkedList<AbstractCommand> allAlert = syncState.getAlertCmdOut();

        if (maxSizeAvailable > 0 && syncState.getStatusCmdOut().isEmpty()) {
            for (int i=0; allAlert != null && i<allAlert.size(); i++) {
                Alert alert = (Alert)allAlert.get(i);

                //
                // Calculate size Alert
                //
                long size = 0;
                if (Constants.MIMETYPE_SYNCMLDS_WBXML.equals(mimeType)) {
                    size  = SizeCalculator.getWBXMLSize(alert);
                } else if (Constants.MIMETYPE_SYNCMLDS_XML.equals(mimeType)) {
                    size  = SizeCalculator.getXMLSize(alert);
                }

                if (maxSizeAvailable - size >= 0) {
                    addAlert.add((Alert)allAlert.get(i));
                    maxSizeAvailable -= size;
                    x++;
                    continue;
                }
                break;
            }
        }
        if (log.isEnabled(Level.TRACE)) {
            log.trace("Number of Alert inserted: " + x);
        }
    }

    /**
     * Calculates how many AbstractCommand can be sent
     *
     * @param checkSyncCmd true if the Sync commands must be checked
     *
     */
    private void howManyAbstractCommand(boolean checkSyncCmd) {
        addAbsCmd = new ArrayList<AbstractCommand>();

        //
        // Number of AbstractCommand to insert into response
        //
        int x = 0;
        long sizeSync = 0, size = 0;
        Sync syncCopy = null;

        LinkedList allCmd = syncState.getCmdOut();

        if (maxSizeAvailable > 0                    &&
            syncState.getStatusCmdOut().isEmpty() &&
            syncState.getAlertCmdOut().isEmpty() ) {

            ArrayList<AbstractCommand> results = new ArrayList<AbstractCommand>(ProtocolUtil.filterCommands(
                allCmd,
                new String[]{Results.COMMAND_NAME, Get.COMMAND_NAME}
            ));
            if (results != null && results.size() > 0) {
                for (int y=0; y<results.size(); y++) {
                    AbstractCommand cmd = results.get(y);
                    if (Constants.MIMETYPE_SYNCMLDS_WBXML.equals(mimeType)) {
                        size  = SizeCalculator.getCommandWBXMLSize(cmd);
                    } else if (Constants.MIMETYPE_SYNCMLDS_XML.equals(mimeType)) {
                        size  = SizeCalculator.getCommandXMLSize(cmd);
                    }

                    if (checkSizeCommands(cmd, size) == -1) {
                        allCmd.remove(cmd);
                        continue;
                    }

                    if (maxSizeAvailable - size >= 0) {
                        addAbsCmd.add(cmd);
                        maxSizeAvailable -= size;
                        x++;
                        results.remove(cmd);
                        continue;
                    }
                }
            }
            if (results != null && results.isEmpty()) {

                for (int i=0; allCmd != null && i<allCmd.size(); i++) {
                    size = 0;
                    AbstractCommand cmd = (AbstractCommand)allCmd.get(i);

                    if (cmd instanceof Sync && checkSyncCmd) {
                        sizeSync = 0;
                        Sync sync = (Sync)cmd;

                        syncCopy = new Sync(sync.getCmdID(),
                                            sync.isNoResp(),
                                            sync.getCred(),
                                            sync.getTarget(),
                                            sync.getSource(),
                                            sync.getMeta(),
                                            sync.getNumberOfChanges(),
                                            new AbstractCommand[0]
                                           );

                        if (Constants.MIMETYPE_SYNCMLDS_WBXML.equals(mimeType)) {
                            sizeSync  = SizeCalculator.getWBXMLSize(syncCopy);
                        } else if (Constants.MIMETYPE_SYNCMLDS_XML.equals(mimeType)) {
                            sizeSync  = SizeCalculator.getXMLSize(syncCopy);
                        }

                        if (maxSizeAvailable - sizeSync < 0) {
                            break;
                        }

                        maxSizeAvailable -= sizeSync;
                        ArrayList<AbstractCommand> cmdAddCopy = new ArrayList<AbstractCommand>();

                        if (sync.getCommands() != null) {
                            AbstractCommand[] ac = (AbstractCommand[])sync.getCommands().toArray(new AbstractCommand[0]);

                            for (int y=0; ac != null && y<ac.length; y++) {
                                if (Constants.MIMETYPE_SYNCMLDS_WBXML.equals(mimeType)) {
                                    size  = SizeCalculator.getCommandWBXMLSize(ac[y]);
                                } else if (Constants.MIMETYPE_SYNCMLDS_XML.equals(mimeType)) {
                                    size  = SizeCalculator.getCommandXMLSize(ac[y]);
                                }

                                if (checkSizeCommands(ac[y], sizeSync + size) == -1) {
                                    sync.getCommands().remove(ac[y]);
                                    continue;
                                }

                                if (maxSizeAvailable - size >= 0) {
                                    maxSizeAvailable -= size;
                                    x++;
                                    cmdAddCopy.add(ac[y]);
                                    continue;
                                } else {
                                    break;
                                }
                            }
                        }
                        syncCopy.setCommands(cmdAddCopy.toArray(new AbstractCommand[0]));
                        sync.getCommands().removeAll(cmdAddCopy);


                        int index = syncState.getCmdOut().indexOf(cmd);
                        syncState.getCmdOut().remove(cmd);
                        addAbsCmd.add(syncCopy);

                        if (!sync.getCommands().isEmpty()) {
                          syncState.getCmdOut().add(index, sync);
                        }

                    } else if (cmd instanceof Results || cmd instanceof Get) {
                        allCmd.remove(i);
                    } else if (!(cmd instanceof Sync)) {
                        if (Constants.MIMETYPE_SYNCMLDS_WBXML.equals(mimeType)) {
                            size  = SizeCalculator.getCommandWBXMLSize(cmd);
                        } else if (Constants.MIMETYPE_SYNCMLDS_XML.equals(mimeType)) {
                            size  = SizeCalculator.getCommandXMLSize(cmd);
                        }

                        if (checkSizeCommands(cmd, size) == -1) {
                            allCmd.remove(i);
                            continue;
                        }

                        if (maxSizeAvailable - size >= 0) {
                            addAbsCmd.add((AbstractCommand)allCmd.get(i));
                            maxSizeAvailable -= size;
                            x++;
                            continue;
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
     * Check the size of commands to send at the client.
     * If their size added at size of SyncHdr and SyncHdr Status is more than
     * MaxMsgSize then not send that command
     *
     * @param cmd the AbstractCommand to check
     */
    private int checkSizeCommands(AbstractCommand cmd, long size) {
        if (log.isEnabled(Level.TRACE)) {
            log.trace("Check the command size");
        }

        long sizeFree = calculateSizeAvailable();

        //
        // Calculate size
        //
        if (size == 0) {
            if (Constants.MIMETYPE_SYNCMLDS_WBXML.equals(mimeType)) {
                size = SizeCalculator.getWBXMLSize(cmd);
            } else if (Constants.MIMETYPE_SYNCMLDS_XML.equals(mimeType)) {
                size = SizeCalculator.getXMLSize(cmd);
            }
        }
        if (sizeFree < size) {
            if (log.isEnabled(Level.INFO)) {
                log.info("Will not be NEVER possible to send this command!");
                log.info("The " + cmd + " will be removed from the " +
                         "list of the commands to send to the client");
            }
            return -1;
        }
        return 0;
    }

    /**
     * Executes the given modifications and for each of them returns a status
     * command. It forwards the execution to the synchronization engine.
     *
     * @param modifications synchronization commands containing the modifications
     *                 that the client requires
     *
     * @return an array of command objects, each containig the result of
     *         a modification or a modification itself
     * @throws Sync4jException
     */
    private List processModifications(ClientModifications modifications)
    throws Sync4jException {
        SyncHdr    header           = modifications.getSyncHeader();
        String     msgId            = header.getMsgID()            ;
        boolean    headerNoResponse = header.isNoResp()            ;

        ArrayList responseCommands = new ArrayList();

        syncEngine.setCommandIdGenerator(cmdIdGenerator);

        Sync[] syncCommands = modifications.getClientSyncCommands();

        //
        // Retrieves existing LUID-GUID mapping
        //
        getClientMappings();

        //
        // First of all prepare the memory sources with the modification commands
        // receveid by the client. Sent the Status commands to check if there
        // is a "Refresh required": in this case is necessary to do a slow sync.
        //
        Status[] statusCommands =
            (Status[])ProtocolUtil.filterCommands(
                modifications.getClientCommands(),
                Status.class
            ).toArray(new Status[0]);

        prepareMemorySources(syncCommands, statusCommands);

        syncEngine.setClientMappings(clientMappings);

        try {
            syncEngine.sync(syncState.loggedPrincipal);
        } catch (Sync4jException e) {
            log.debug( "processModifications", e);
        }

        //
        // Status code for commands
        //
        if (headerNoResponse == false) {
            //
            // Sync commands
            //
            responseCommands.addAll(statusForSyncs(syncCommands));

            //
            // Status for server-side executed modification commands
            //
            Status[] operationStatus =
                syncEngine.getModificationsStatusCommands(msgId);

            for (int i=0; i<operationStatus.length; ++i) {
                responseCommands.add(operationStatus[i]);
            }
        }

        //
        // SyncCommands sent back to the client
        // No sync sent to client if ONE_WAY_FROM_CLIENT
        //

        Alert[] alertCommands = syncState.getClientAlerts();

        Item[] alertItems = null;
        ItemizedCommand[] commands = null;
        String uri = null;
        for (int k = 0; ((alertCommands != null) && (k < alertCommands.length)); ++k) {
            //
            // If the alert represents a client only action, just ignore it
            //
            if (AlertCode.isClientOnlyCode(alertCommands[k].getData())) {
                continue;
            }

            alertItems = alertCommands[k].getItems().toArray(new Item[0]);

            for (int i=0; ((alertItems != null) && (i < alertItems.length)); ++i) {
                uri = alertItems[i].getTarget().getLocURI();

                for (int s=0; ((syncCommands != null) && s<syncCommands.length); s++) {
                    if ((syncCommands[s].getTarget() != null) && !uri.equals(syncCommands[s].getTarget().getLocURI())) {
                        continue;
                    }
                    for (int j = 0; ((syncEngine.getDbs() != null) && (j < syncEngine.getDbs().length)); ++j) {
                        if ((syncEngine.getDbs())[j].getName().equals(uri)){
                            SyncOperation[] operations = syncEngine.getSyncOperations(uri);
                            commands = syncEngine.operationsToCommands(
                                           clientMappings.get(uri),
                                           operations,
                                           uri
                                       );
                            responseCommands.add(
                                    new Sync(
                                            cmdIdGenerator.next(),
                                            false,
                                            null,
                                            ProtocolUtil.source2Target(alertItems[i].getSource()),
                                            ProtocolUtil.target2Source(alertItems[i].getTarget()),
                                            null,
                                            0,
                                            commands
                                    )
                            );

                            //
                            // Now we can update the client-side LUID-GUID mappings
                            //
                            syncEngine.updateClientMappings(clientMappings, operations, slow);

                            //
                            // Resets client alert effectively processed
                            //
                            syncState.removeClientAlert((Alert)alertCommands[k]);
                        }
                    }  // next j
                }
            } // next i
        } // next k

        //
        // And here we can update the server-side LUID-GUID mappings
        //
        syncEngine.updateServerMappings(clientMappings, slow);

        return responseCommands;
    }

    /**
     * Process the completion message from the client check status for sync
     * command and update client mapping
     * @param message
     * @return the response message
     * @throws ProtocolException
     */
    private SyncML processCompletionMessage(SyncML message)
    throws ProtocolException {
        if (log.isEnabled(Level.TRACE)) {
            log.trace("Processing completion message");
        }
        SyncML response = null;

        try {
            ClientCompletion clientCompletion =
                    new ClientCompletion(message.getSyncHdr(),
                                         message.getSyncBody()
                                         );

            clientCompletion.setIdGenerator(cmdIdGenerator);

            if (message.getSyncBody().isFinalMsg()) {
                clientCompletion.setFlag(Flags.FLAG_FINAL_MESSAGE);
            }

            //
            // IF the client completion request contains MapCommand
            //
            if (clientCompletion.isMapCommandFind()) {
                String uri = null;

                ClientMapping mapping = null;
                Map[] mapCommands = clientCompletion.getMapCommands();
                MapItem[]    mapItems    = null                             ;
                for (int j=0; ((mapCommands != null) && (j<mapCommands.length)); ++j) {
                    uri = mapCommands[j].getTarget().getLocURI();
                    mapItems =
                        mapCommands[j].getMapItems().toArray(
                                                                new MapItem[0]);

                    mapping = clientMappings.get(uri);
                    if (mapping == null) {
                        mapping = new ClientMapping(syncState.loggedPrincipal, uri);
                        clientMappings.put(uri, mapping);
                    }

                    for (int i = 0; i < mapItems.length; i++) {
                        MapItem mapItem = mapItems[i];

                        //Adding item properties to the persistent mapping
                        String GUID = mapItem.getTarget().getLocURI();
                        String LUID = mapItem.getSource().getLocURI();
                        mapping.updateMapping(LUID, GUID);
                    }
                }
            }

            response = clientCompletion.getResponse(msgIdGenerator.current());

            //
            // To sort StatusCommand
            //
            response = mergeResponse(null, response);

            if (currentState != STATE_SYNCHRONIZATION_COMPLETION) {
                //
                // The server before ends to send its Items and then it sends
                // to client the status relative to the mapping
                //

                //
                // Cache the commands to send in the message
                //
                cacheMapCommands(response);

                return null;
            }

            //
            // Calculate size of response message
            //
            SyncHdr  syncHdr  = response.getSyncHdr() ;
            SyncBody syncBody = response.getSyncBody();
            long sizeSyncHdr  = 0, sizeSyncBody = 0;

            if (Constants.MIMETYPE_SYNCMLDS_WBXML.equals(mimeType)) {
                sizeSyncHdr  = SizeCalculator.getWBXMLSize(syncHdr);
                sizeSyncBody = SizeCalculator.getWBXMLSize(syncBody);
            } else if (Constants.MIMETYPE_SYNCMLDS_XML.equals(mimeType)) {
                sizeSyncHdr  = SizeCalculator.getXMLSize(syncHdr);
                sizeSyncBody = SizeCalculator.getXMLSize(syncBody);
            }

            syncState.setOverheadHdr(sizeSyncHdr);
            maxSizeAvailable = syncState.getMaxMsgSize();

            if ( (maxSizeAvailable >= sizeSyncHdr + sizeSyncBody ||
                  maxSizeAvailable == 0)                         &&
                  syncState.getMapStatusOut().isEmpty()
               ) {

                return response;
            }

            //
            // Cache the commands to send in the message
            //
            cacheMapCommands(response);

            //
            // The size of the answer is greater then the allowed MaxMsgSize
            // Calculate size of the single commands of the response.
            // Create one answer of the allowed dimension.
            //
            maxSizeAvailable = calculateSizeAvailable();

            if (log.isEnabled(Level.TRACE)) {
                log.trace("How many Status can I include into the response?");
            }
            howManyStatus(syncState.getMapStatusOut());

            SyncBody body = new SyncBody(
                   (AbstractCommand[])addStatus.toArray(new AbstractCommand[0]),
                   false
            );

            //
            // Remove from queue the Status inserted into response
            //
            syncState.removeMapStatusOut(addStatus);

            //
            // The server must be send all modification command only when the
            // client has sent the last message and when there aren't more
            // Status command into queue
            //
            if ( message.getSyncBody().isFinalMsg() &&
                 syncState.getMapStatusOut().isEmpty()
               ) {
               body.setFinalMsg(Boolean.TRUE);
            }

            return new SyncML(syncHdr, body);

        } catch (Sync4jException e) {
            if(log.isEnabled(Level.FATAL)){
                log.fatal("Error in process completion");
            }
            throw new ProtocolException(e);
        }
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
     * Prepares the memory sources with the modification commands receveid
     * by the client.
     * <p>
     * Note that if the requested synchronization is a slow sync, the items are
     * inserted as "existing" items, regardless the command they belong to.
     * (maybe this will change as soon as the specification becomes clearer)
     *
     * @param syncCommands the commands used to prepare the source
     * @param statusCommands the status commands sent by the client
     *
     */
    private void prepareMemorySources(Sync[] syncCommands, Status[] statusCommands) {

        List<SyncSource> sources = syncEngine.getClientSources();

        //
        // For efficiency: put the databases in a HashMap
        //
        HashMap<String, Database> dbMap = new HashMap<String, Database>();
        for (int i=0; ((syncEngine.getDbs() != null) && (i<syncEngine.getDbs().length)); ++i) {
            dbMap.put((syncEngine.getDbs())[i].getName(), (syncEngine.getDbs())[i]);
        }

        //
        // First of all prepare the memory sources with the modification commands
        // receveid by the client
        //
        int method;
        slow = false;
        Target target = null;
        MemorySyncSource mss = null;
        AbstractCommand[] modifications = null;
        for (int i = sources.size(); i > 0; --i) {
            if (log.isEnabled(Level.TRACE)) {
                log.trace("Preparing "
                        + syncEngine.getClientSources().get(i - 1)
                        + " with "
                        + Arrays.asList(syncCommands)
                );
            }

            mss = (MemorySyncSource) sources.get(i - 1);

            String  uri = mss.getSourceURI();

            modifications = new AbstractCommand[0];
            for (int j = 0; ((syncCommands != null) && (j < syncCommands.length)); ++j) {
                target = syncCommands[j].getTarget();
                if ((target != null) && (uri.equals(target.getLocURI()))) {
                    if (syncCommands[j].getCommands() != null) {
                       modifications =
                       (AbstractCommand[])syncCommands[j].getCommands().toArray(
                                                        new AbstractCommand[0]);
                    }

                    break;
                }
            }

            method = dbMap.get(uri).getMethod();
            slow = ((method == AlertCode.SLOW) || (method == AlertCode.REFRESH_FROM_SERVER));

            if (!slow) {
                String targetRef = null;
                String statusCode = null;
                for (int k=0; statusCommands!=null && k<statusCommands.length; k++) {
                    targetRef = statusCommands[k].getTargetRef().get(0).getValue();
                    if ((targetRef != null) && (uri.equals(targetRef))) {
                        statusCode = statusCommands[k].getData().getData();
                        if (statusCode.equals("" + StatusCode.REFRESH_REQUIRED)) {
                            slow = true;
                            dbMap.get(uri).setStatusCode(StatusCode.REFRESH_REQUIRED);
                        }
                        break;
                    }
                }
            }

            prepareMemorySource(mss, modifications, slow);
        }
    }

    /**
     * Prepares a source that represents the image of the client database. This
     * is done combining the existing client mapping with the modifications sent
     * by the client. Than this source can be compared with the server view of
     * the database.
     *
     * @param source the e source to prepare
     * @param commands the client modifications
     * @param slowSync true if the preparation is for a slow sync, false
     *                    otherwise
     *
     */
    private void prepareMemorySource(MemorySyncSource  source  ,
                                     AbstractCommand[] commands,
                                     boolean           slowSync) {
        ArrayList<SyncItem> existing = new ArrayList<SyncItem>();
        ArrayList<SyncItem> deleted  = new ArrayList<SyncItem>();
        ArrayList<SyncItem> created  = new ArrayList<SyncItem>();
        ArrayList<SyncItem> updated  = new ArrayList<SyncItem>();

        ClientMapping guidluid = null;

        //
        // First of all, in the case of fast sync, the items already mapped are
        // added as existing items. Note that server ids are used.
        // In the case of slow sync, the mappinngs are cleared so that they
        // won't generate conflicts.
        //
        guidluid = clientMappings.get(source.getSourceURI());
        if (slowSync) {
            if (guidluid != null && finalMsg) {
                guidluid.clearMappings();
            }
        } else {
            if (guidluid != null) {
                java.util.Map<String, String> map = guidluid.getMapping();

                Iterator<String> i = map.keySet().iterator();
                while (i.hasNext()) {
                    existing.add(
                        new SyncItemImpl(source, map.get(i.next()), SyncItemState.SYNCHRONIZED)
                    );
                }
            }
        }

        String name = null;
        for (int i = 0; ((commands != null) && (i < commands.length)); ++i) {
            name = commands[i].getName();
            if (slowSync && !Delete.COMMAND_NAME.equals(name)) {
                existing.addAll(
                    Arrays.asList(
                            syncEngine.itemsToSyncItems(
                                clientMappings.get(source.getSourceURI()),
                                source,
                                (ModificationCommand)commands[i],
                                SyncItemState.SYNCHRONIZED,
                                nextTimestamp.start
                            )
                    )
                );
                continue;
            }
            if (Add.COMMAND_NAME.equals(commands[i].getName())) {
                created.addAll(
                        Arrays.asList(
                                syncEngine.itemsToSyncItems(
                                    clientMappings.get(source.getSourceURI()),
                                    source,
                                    (ModificationCommand)commands[i],
                                    SyncItemState.NEW,
                                    nextTimestamp.start
                                )
                        )
                );
                continue;
            }

            if (Delete.COMMAND_NAME.equals(commands[i].getName())) {
                deleted.addAll(
                        Arrays.asList(
                                syncEngine.itemsToSyncItems(
                                    clientMappings.get(source.getSourceURI()),
                                    source,
                                    (ModificationCommand)commands[i],
                                    SyncItemState.DELETED,
                                    nextTimestamp.start
                                )
                        )
                );
                continue;
            }

            if (Replace.COMMAND_NAME.equals(commands[i].getName())) {
                updated.addAll(
                        Arrays.asList(
                                syncEngine.itemsToSyncItems(
                                    clientMappings.get(source.getSourceURI()),
                                    source,
                                    (ModificationCommand)commands[i],
                                    SyncItemState.UPDATED,
                                    nextTimestamp.start
                                )
                        )
                );
                continue;
            }
        }

        source.initialize(existing, deleted, created, updated);
    }

    /**
     * Create and return the status commands for the executed &lt;Sync&gt;s.
     *
     * @param syncCommands the Sync commands
     *
     * @return the status commands in a List collection
     */
    private List statusForSyncs(Sync[] syncCommands) {
        ArrayList ret = new ArrayList();

        String uri = null;
        Target target = null;
        Source source = null;
        TargetRef targetRef = null;
        SourceRef sourceRef = null;
        int statusCode = StatusCode.OK;
        for (int i = 0; (  (syncCommands     != null )
                        && (i < syncCommands.length)); ++i) {

            target = syncCommands[i].getTarget();
            source = syncCommands[i].getSource();

            //
            // A Sync command can be empty....
            //
            uri = (target==null) ? null : target.getLocURI();
            if ((uri == null) || (syncEngine.getClientSource(uri) != null)) {
                statusCode = StatusCode.OK;
            } else {
                statusCode = StatusCode.NOT_FOUND;
            }

            targetRef = (target == null) ? null : new TargetRef(uri);

            sourceRef = (source == null) ? null : new SourceRef(syncCommands[i].getSource());
            ret.add(
                new Status(
                        cmdIdGenerator.next(),
                        lastMsgIdFromClient,
                        syncCommands[i].getCmdID().getCmdID(),
                        syncCommands[i].COMMAND_NAME,
                        targetRef,
                        sourceRef,
                        null,
                        null,
                        new Data(statusCode),
                        new Item[0]
                )
            );
        } // next i

        return ret;
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

            if (syncEngine.isGuestEnabled()) {
                credential = Cred.getGuestCredential();
            } else {
                syncState.authenticationState = AUTH_MISSING_CREDENTIALS;
                return false;
            }
        }

        Sync4jPrincipal p = Sync4jPrincipal.fromCredential(credential.getData(),
                                                           credential.getType(),
                                                           deviceId            );
        //
        // If the authentication type server is MD5 then read the nonce for
        // the device and set this value into credential
        //
        if (serverAuthType.equalsIgnoreCase(Constants.AUTH_TYPE_MD5)) {

            NextNonce nn = new NextNonce(syncState.device.getClientNonce());

            Authentication auth = credential.getAuthentication();
            auth.setNextNonce(nn);
            auth.setDeviceId(deviceId);
            auth.setSyncMLVerProto(syncState.syncMLVerProto);
        }

        if (syncEngine.login(credential)) {
            if (serverAuthType.equalsIgnoreCase(Constants.AUTH_TYPE_MD5)) {
                p.setUsername(credential.getAuthentication().getUsername());
            }
            if (syncEngine.authorize(p, SecurityConstants.RESOURCE_SESSION)) {

                syncState.loggedCredential = credential;
                syncState.loggedPrincipal  = p         ;

                syncState.authenticationState = AUTH_AUTHENTICATED;

                return true;
            }
        }
        return false;
    }

    /**
     * Logs out the logged in credential
     */
    private void logout() {
        if (isAuthenticated()) {
            syncEngine.logout(syncState.loggedCredential);
        }
        syncState.authenticationState = AUTH_INVALID_CREDENTIALS;
        syncState.loggedCredential = null;
        syncState.loggedPrincipal  = null;
    }

    /**
     * Called by the <i>SyncBean</i> when the container release the session.
     * It commit the change to the DB, logs out the credential and
     * release aquired resources.
     */
    public void endSession() {
        commit();
        logout();
    }


    /**
     *  Checks if the message uses a separate initialization or not.
     *  If it contains &gt;Alert&lt; and &gt;Sync&lt; tag, it implies Sync with
     *  Initialization..
     *
     *  @param message Message to be checked for Sync with Initialization.
     *  @return TRUE (if syncWithInitialization) / FALSE (if not Sync with Init)
     *  @throws ProtocolException
     */
    private boolean checkSyncInit(SyncML message)
    throws ProtocolException{

        // check for the message of the SyncPackage
    // if it contains <alert> & <sync> tag, it implies Sync with Initialization..
    // As the Initialization process is completed, only <sync> package is needed
    // to be checked, but <alert> tag is also checked for Cross Verification.

        AbstractCommand[] clientCommands =
            (AbstractCommand[])message.getSyncBody().getCommands().toArray(
                                                        new AbstractCommand[0]);

        List syncs  = ProtocolUtil.filterCommands(clientCommands, Sync.class);
    List alerts = ProtocolUtil.filterCommands(clientCommands, Alert.class);

        return ((!syncs.isEmpty()) && (!alerts.isEmpty()));
    }

    /**
     * Resets the clientMappings map.
     */
    private void resetClientMappings() {
        clientMappings = new HashMap<String, ClientMapping>();
    }

    /**
     * Retrieves the existing LUID-GUID mappings for the logged in principal.
     * The mappings for all the databases involved in the synchronization are
     * loaded.
     *
     * @return a new <i>ClientMapping</i> object containing the mapping for the
     *         given client id
     *
     * @throws Sync4jException in case of error reading the mapping from the
     *         persistent store.
     */
    private void getClientMappings()
    throws Sync4jException {

        resetClientMappings();

        ClientMapping clientMapping = null;
        String uri = null;
        try {
            PersistentStore ps = syncEngine.getStore();

            for (int i = 0; ((syncEngine.getDbs() != null) && (i<syncEngine.getDbs().length)); ++i) {

                uri = (syncEngine.getDbs())[i].getName();

                clientMapping = new ClientMapping(syncState.loggedPrincipal, uri);

                ps.read(clientMapping);

                clientMappings.put(uri, clientMapping);
            }
        } catch (PersistentStoreException e) {
            if(log.isEnabled(Level.FATAL)){
                log.fatal("Unable to read clientMappings from the persistent store");
            }
            throw new Sync4jException(e);
        }
    }

    /**
     * Stores the LUIG-GUID mappings into the database
     *
     * @throws Sync4jException in case of database error
     */
    private void storeClientMappings() throws Sync4jException {
        if (clientMappings == null) {
            return;
        }

        try {
            PersistentStore ps = syncEngine.getStore();

            ClientMapping cm = null;
            Iterator<String> i = clientMappings.keySet().iterator();
            while (i.hasNext()) {
                cm = clientMappings.get(i.next());
                if (log.isEnabled(Level.TRACE)) {
                    log.trace("Saving client mapping: " + cm);
                }
                ps.store(cm);
            }
        } catch (PersistentStoreException e) {
            if(log.isEnabled(Level.FATAL)){
                log.fatal("Unable to save clientMappings to the persistent store");
            }
            throw new Sync4jException(e);
        }
    }

    /**
     * Read the given principal from the store. The principal must be
     * already configured with username and device. The current implementation
     * reads the following additional informatioin:
     * <ul>
     *  <li>principal id
     * </ul>
     *
     * @param principal the principal to be read
     *
     * @throws com.funambol.framework.server.store.PersistentStoreException
     */
    private void readPrincipal(Sync4jPrincipal principal)
    throws PersistentStoreException {
        assert(principal               != null);
        assert(principal.getUsername() != null);
        assert(principal.getDeviceId() != null);

        PersistentStore ps = syncEngine.getStore();

        ps.read(principal);
    }

    private void readObject(java.io.ObjectInputStream in)
    throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();

    }

    private String getStateName(int state) {
        String stateName = "STATE_UNKNOWN";

        switch (state) {
            case STATE_START                      : stateName = "STATE_START"                     ; break;
            case STATE_END                        : stateName = "STATE_END"                       ; break;
            case STATE_ERROR                      : stateName = "STATE_ERROR"                     ; break;
            case STATE_INITIALIZATION_PROCESSING  : stateName = "STATE_INITIALIZATION_PROCESSING" ; break;
            case STATE_INITIALIZATION_PROCESSED   : stateName = "STATE_INITIALIZATION_PROCESSED"  ; break;
            case STATE_SYNCHRONIZATION_PROCESSING : stateName = "STATE_SYNCHRONIZATION_PROCESSING"; break;
            case STATE_SYNCHRONIZATION_PROCESSED  : stateName = "STATE_SYNCHRONIZATION_PROCESSED" ; break;
            case STATE_SYNCHRONIZATION_COMPLETION : stateName = "STATE_SYNCHRONIZATION_COMPLETION"; break;
        }

        return stateName;
    }

    public SyncState getSyncState() {
        return this.syncState;
    }
}
