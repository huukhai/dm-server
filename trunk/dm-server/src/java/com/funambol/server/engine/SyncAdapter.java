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
package com.funambol.server.engine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;

import com.funambol.framework.config.Configuration;
import com.funambol.framework.config.ConfigurationConstants;
import com.funambol.framework.core.Constants;
import com.funambol.framework.core.Cred;
import com.funambol.framework.core.HMACAuthentication;
import com.funambol.framework.core.StatusCode;
import com.funambol.framework.core.Sync4jException;
import com.funambol.framework.core.SyncML;
import com.funambol.framework.core.Util;
import com.funambol.framework.engine.pipeline.MessageProcessingContext;
import com.funambol.framework.engine.pipeline.PipelineManager;
import com.funambol.framework.protocol.ProtocolException;
import com.funambol.framework.server.Sync4jDevice;
import com.funambol.framework.server.SyncResponse;
import com.funambol.framework.server.error.BadRequestException;
import com.funambol.framework.server.error.InvalidCredentialsException;
import com.funambol.framework.server.error.NotImplementedException;
import com.funambol.framework.server.error.ServerException;
import com.funambol.framework.server.session.SessionHandler;
import com.funambol.framework.server.store.PersistentStore;
import com.funambol.framework.server.store.PersistentStoreException;
import com.funambol.framework.tools.SecurityTools;
import com.funambol.framework.tools.WBXMLTools;
import com.funambol.framework.tools.beans.BeanFactory;

import com.funambol.server.SyncMLCanonizer;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;


/**
 * This class handles a synchronization request.
 * <p>
 * This server accepts synchronization requests addressed to the hostname
 * indicated by the configuration property pointed by {CFG_SERVER_URI} (see
 * Funambol.properties).
 * <p>
 * LOG NAME: funambol.server
 *
 * @version $Id: SyncAdapter.java,v 1.8 2007-06-19 08:16:17 luigiafassina Exp $
 *
 */
public class SyncAdapter
implements Constants, ConfigurationConstants, Serializable{

    // ----------------------------------------------------- Protected constants
    protected static final String CONFIG_SYNCML_CANONIZER
        = "com/funambol/server/SyncMLCanonizer.xml";

    protected static final String HMAC_ALGORITHM = "MD5";

    // ---------------------------------------------------------- Protected data

    protected transient static final Logger log            = Logger.getLogger(com.funambol.server.engine.SyncAdapter.class.getName());

    protected SessionHandler sessionHandler   = null;

    protected Configuration config            = null;

    protected String sessionId                = null;

    protected PipelineManager pipelineManager = null;

    protected MessageProcessingContext mpc    = null;

    protected SyncML inputMessage             = null;

    protected SyncMLCanonizer syncMLCanonizer = null;

    protected String mimeType                 = null;

    // ------------------------------------------------------------ Constructors

    /**
     * Creates a new SyncAdapter with the given configuration object.
     *
     * @param sessionId the serve session id
     * @param config the configuration object
     */
    public SyncAdapter(Configuration config) {
        super();

        this.config = config;

        sessionHandler = (SessionHandler)config.getBeanInstance(CFG_SESSION_HANDLER);
        pipelineManager = (PipelineManager)config.getBeanInstance(CFG_ENGINE_PIPELINE);

        try {
            syncMLCanonizer = (SyncMLCanonizer)BeanFactory.getBeanInstance(
                config.getClassLoader(), CONFIG_SYNCML_CANONIZER);
        } catch (Exception e) {
            log.debug("constructor", e);

            new Sync4jException("Error "
                                + e.getClass().getName()
                                + " creating the syncMLCanonizer: "
                                + e.getMessage()
                                ).printStackTrace();
        }

        mpc = new MessageProcessingContext();
    }

    // ----------------------------------------------------------- Public methods

    /**
     * Sets sessionId
     *
     * @param sessionId the server session id
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Gets sessionId
     *
     * @return the server session id
     */
    public String getSessionId() {
       return sessionId;
    }

    /**
     * Finalizes the sync session
     */
    public void endSync() {
        int currentState = sessionHandler.getCurrentState();

        switch (currentState) {
            case SessionHandler.STATE_END:
                sessionHandler.commit();
                break;

            case SessionHandler.STATE_ERROR:
                sessionHandler.abort(StatusCode.PROCESSING_ERROR);
                break;

            case SessionHandler.STATE_SESSION_ABORTED:
                sessionHandler.abort(StatusCode.SESSION_ABORTED);
                break;

            default:
                sessionHandler.abort(StatusCode.SESSION_EXPIRED);
                break;
        }
    }

    /**
     * process the incoming sync message
     *
     * @param msg must be non-null
     * @param mimeType may be null.
     * @param hmacHeader may be null.
     */
    public SyncResponse processMessage(final byte[] msg,
                                       final String mimeType,
                                       final String hmacHeader) throws ServerException {

        SyncResponse response = null;
        String inMessage = null;
        SyncML outMessage = null;

        if (msg == null) {
            String err = "msg is null!";

            if(log.isEnabled(Level.FATAL)){
                log.fatal(err);
            }

            throw new ServerException(err);
        }

        if (mimeType == null) {
            String err = "mimeType is null!";

            if(log.isEnabled(Level.FATAL)){
                log.fatal(err);
            }

            throw new ServerException(err);
        }

        if (log.isEnabled(Level.TRACE)) {
            log.trace("mimeType: " + mimeType);
        }

        this.mimeType = mimeType;

        try {
            if (MIMETYPE_SYNCMLDS_WBXML.equals(mimeType) ||
                MIMETYPE_SYNCMLDM_WBXML.equals(mimeType)) {
                //
                // convert WBXML to XML, and then pass to processXMLMessage
                //
                if (log.isEnabled(Level.TRACE)) {
                    log.trace("Convert message from wbxml to xml");
                }
                inMessage = WBXMLTools.wbxmlToXml(msg);

            } else if (MIMETYPE_SYNCMLDS_XML.equals(mimeType) ||
                       MIMETYPE_SYNCMLDM_XML.equals(mimeType)) {

                inMessage = new String(msg);

            } else {
                //
                // its an unsupported MIME type
                //
                String err = "Unknown mime type:  " + mimeType;

                if(log.isEnabled(Level.FATAL)){
                    log.fatal(err);
                }

                throw new NotImplementedException(err);
            }

        } catch (Sync4jException e) {
            String err = "Error processing message: " + e.getMessage();

            if(log.isEnabled(Level.FATAL)){
                log.fatal(err);
            }

            throw new ServerException(err, e);
        }

        inMessage = syncMLCanonizer.canonizeInputMessage(inMessage);

        try {
            if (log.isEnabled(Level.TRACE)) {
                log.trace("Message to translate into the SyncML object:\n" + inMessage);
            }

            IBindingFactory f = BindingDirectory.getFactory(SyncML.class);
            IUnmarshallingContext c = f.createUnmarshallingContext();

            Object syncML = c.unmarshalDocument(
                new ByteArrayInputStream(inMessage.getBytes()), "UTF-8"
                );

            inputMessage = (SyncML)syncML;

        } catch(JiBXException e) {
            e.printStackTrace();

            if(log.isEnabled(Level.FATAL)){
                log.fatal("Error unmarshalling message: " + e.getMessage());
            }

            throw new ServerException(e);
        }

        mpc.setProperty(mpc.PROPERTY_SESSIONID, sessionId);

        try {

            if (pipelineManager != null) {
                if (log.isEnabled(Level.TRACE)) {
                    log.trace("Calling input pipeline");
                }
                pipelineManager.preProcessMessage(mpc, inputMessage);
            } else {
                if (log.isEnabled(Level.TRACE)) {
                    log.trace("Pipeline manager is null...preprocess message not performed");
                }
            }

            outMessage = processInputMessage(hmacHeader, msg);

            final String resultMimeType = mimeType;

            if (pipelineManager != null) {
                if (log.isEnabled(Level.TRACE)) {
                    log.trace("Calling output pipeline");
                }
                pipelineManager.postProcessMessage(mpc, outMessage);
            } else {
                  if (log.isEnabled(Level.TRACE)) {
                    log.trace("Pipeline manager is null...postprocess message not performed");
                }
            }

            Cred credOut = outMessage.getSyncHdr().getCred();

            if (log.isEnabled(Level.TRACE)) {
                if (credOut != null) {
                    log.trace("credOut.getFormat: " + credOut.getFormat());
                    log.trace("credOut.getType: " + credOut.getType());
                    log.trace("credOut.getData: " + credOut.getData());
                } else {
                    log.trace("credOut is null");
                }
            }

            // Used for hmac
            boolean hmacRequired  = false;
            String serverId       = null;
            String serverPassword = null;
            byte[] serverNonce    = null;

            if (credOut != null && credOut.getType().equalsIgnoreCase(Constants.AUTH_TYPE_HMAC)) {

                hmacRequired = true;

                // remove the cred, calculate data for hmac add it to the SyncResponse
                outMessage.getSyncHdr().setCred(null);
            }

            if (credOut != null) {
                Sync4jDevice device = sessionHandler.getDevice();
                serverId = config.getStringValue(ConfigurationConstants.CFG_SERVER_ID);
                if (device != null) {
                    serverPassword = device.getServerPassword();
                    serverNonce = device.getServerNonce();
                }
            }

            if (log.isEnabled(Level.TRACE)) {
                log.trace("Outgoing message: " + Util.toXML(outMessage));
            }

            if (log.isEnabled(Level.TRACE)) {
                log.trace("The process is "
                         + ( (outMessage.isLastMessage()) ? "" : "not ")
                         + "complete"
                         );
            }

            response = getSyncResponse(outMessage, resultMimeType, hmacRequired, serverId, serverPassword, serverNonce);

        } catch (Sync4jException e) {
            String err = "Error processing return message: " + e.getMessage();

            if(log.isEnabled(Level.FATAL)){
                log.fatal(err);
            }

            throw new ServerException(err);
        }

        return (SyncResponse)response;
    }

    /**
     * Used to process a status information as needed by the client object (i.e.
     * errors or success).
     *
     * @param statusCode the status code
     * @param statusMessage additional descriptive message
     *
     * @see com.funambol.framework.core.StatusCode for valid status codes.
     */
    public SyncResponse processStatusCode(int statusCode, String info) {
        if (statusCode != StatusCode.OK) {
            sessionHandler.abort(statusCode);
        }
        return null;
    }

    // --------------------------------------------------------- Private methods

    /**
     * Processes the input SyncML message after convertion to objects. See class
     * description for more information.
     *
     * @return the response message
     *
     * @throws ServerException in case of errors
     */
    private SyncML processInputMessage(String hmacHeader, byte[] data) throws ServerException {
        try {
            try {
                sessionHandler.setMimeType(mimeType);
                checkHmacHeader(hmacHeader, data);
                return sessionHandler.processMessage(inputMessage);
            } catch (InvalidCredentialsException e) {
                return sessionHandler.processError(inputMessage, e);
            } catch (ServerException e) {
                return sessionHandler.processError(inputMessage, e);
            } catch (ProtocolException e) {
                return sessionHandler.processError(inputMessage,
                    new BadRequestException(e.getMessage()));
            }
        } catch (Sync4jException e1) {
            //
            // This can be due only to processError
            //
            throw new ServerException(e1);
        }
    }

    /**
     * Parses the hmac header and adds credential info to the input message if the
     * given header contains a valid hmac header.
     *
     * <p>Examples:<br/>
     * algorithm=MD5, username="Robert Jordan", mac=Dgpoz8Pbs4XC0ecp6kLw4Q==
     *
     * @param hmacHeader String
     * @throws ServerException
     */
    private void checkHmacHeader(String hmacHeader, byte[] data) throws ServerException {
        if (hmacHeader == null) {
            return;
        }

        HMACAuthentication auth = new HMACAuthentication(hmacHeader);

        String algorithm = auth.getAlgorithm();

        if (!"MD5".equals(algorithm)) {
            throw new IllegalArgumentException("Algorithm '" + algorithm + "' isn't supported using hmac");
        }

        //
        // Initialize the device ID from the client request
        //
        String deviceId = inputMessage.getSyncHdr().getSource().getLocURI();

        // Calculates mac
        String calculatedMac = calculateMac(algorithm, deviceId, data);

        if (log.isEnabled(Level.TRACE)) {
            log.trace("calculatedMac: " + calculatedMac);
        }

        // Creates credential and add it to the inputMessage
        auth.setCalculatedMac(calculatedMac);
        auth.setDeviceId(deviceId);

        Cred cred = new Cred(auth);

        inputMessage.getSyncHdr().setCred(cred);
    }


    private String calculateMac(String algorithm, String deviceId, byte[] data) {

        //
        // read device
        //
        PersistentStore ps = (PersistentStore)config.getBeanInstance(ConfigurationConstants.CFG_PERSISTENT_STORE);

        Sync4jDevice device = new Sync4jDevice(deviceId);
        try {
            boolean tmp = ps.read(device);
        } catch (PersistentStoreException e) {
            if (log.isEnabled(Level.FATAL)) {
                log.fatal("Error reading device with id: '" + deviceId + "': " + e);
            }
            log.debug("calculateMac", e);

            return null;
        }

        if (log.isEnabled(Level.TRACE)) {
            log.trace("Compute hmac credential for device: " + device);
        }

        String hmacValue = null;

        try {
            hmacValue = SecurityTools.getHMACValue(
                            algorithm,
                            data,
                            device.getDigest(),
                            device.getClientNonce(),
                            log
            );
        } catch (NoSuchAlgorithmException e) {
            if (log.isEnabled(Level.TRACE)) {
                log.fatal(e.getMessage());
            }
            log.debug("calculateMac", e);
        }

        return hmacValue;
    }

    /**
     * Returns a <code>SyncResponse</code> in accordance with the given parameters.
     * <p>If mimeType is equals to <code>Constants.MIMETYPE_SYNCMLDS_WBXML</code> or
     * <code>Constants.MIMETYPE_SYNCMLDM_WBXML</code> converts the SyncML message in wbxml, else
     * if mimeType is equals to <code>Constants.MIMETYPE_SYNCMLDS_XML</code> or
     * <code>Constants.MIMETYPE_SYNCMLDM_XML</code> converts the SyncML message in xml.
     * <p>If hmacRequired if <code>true</code>, uses the serverId, serverPassword, serverNonce
     * to calculate the value of the hmac header.</p>
     *
     * @param outMessage SyncML the message
     * @param mimeType the mime type of the response
     * @param hmacRequired indicates if hmac header is required
     * @param serverId the server id
     * @param serverPassword the server password
     * @param serverNonce the server nonce
     *
     * @throws ServerException
     *
     * @return the <code>SyncResponse</code> in accordance with the given parameters.
     *
     */
    private SyncResponse getSyncResponse(SyncML outMessage, String mimeType, boolean hmacRequired,
                                         String serverId, String serverPassword, byte[] serverNonce)
    throws ServerException {

        byte[] msg = null;

        if (Constants.MIMETYPE_SYNCMLDS_XML.equals(mimeType) ||
            Constants.MIMETYPE_SYNCMLDM_XML.equals(mimeType)) {

            if (log.isEnabled(Level.TRACE)) {
                log.trace("Sending back XML");
            }

            try {
                if (log.isEnabled(Level.TRACE)) {
                    log.trace("Marshalling response message SyncML");
                }

                ByteArrayOutputStream bout = new ByteArrayOutputStream();

                IBindingFactory f = BindingDirectory.getFactory(SyncML.class);
                IMarshallingContext c = f.createMarshallingContext();
                c.setIndent(0);
                c.marshalDocument(outMessage, "UTF-8", null, bout);

                msg = bout.toByteArray();

                // temporary solution to handle namespace
                msg = (syncMLCanonizer.canonizeOutputMessage(new String(msg))).getBytes();

            } catch (Exception e) {
                throw new ServerException("Error marshalling the syncml response", e);
            }

        } else if (Constants.MIMETYPE_SYNCMLDS_WBXML.equals(mimeType) ||
                   Constants.MIMETYPE_SYNCMLDM_WBXML.equals(mimeType)){

            if (log.isEnabled(Level.TRACE)) {
                log.trace("Sending back WBXML");
            }
            String verDTD = outMessage.getSyncHdr().getVerDTD().getValue();

            try {

                ByteArrayOutputStream bout = new ByteArrayOutputStream();

                IBindingFactory f = BindingDirectory.getFactory(SyncML.class);
                IMarshallingContext c = f.createMarshallingContext();
                c.setIndent(0);
                c.marshalDocument(outMessage, "UTF-8", null, bout);

                msg = bout.toByteArray();

                // temporary solution to handle namespace
                String sResponse = (syncMLCanonizer.canonizeOutputMessage(new String(msg)));

                msg = WBXMLTools.toWBXML(sResponse, verDTD);

            } catch(Exception e) {
                throw new ServerException("Error converting the syncml response in wbxml", e);
            }

        }

        StringBuffer hmacHeaderResponse = null;

        if (hmacRequired) {
            try {
                String mac = SecurityTools.getHMACValue(HMAC_ALGORITHM,
                                                        msg,
                                                        serverId,
                                                        serverPassword,
                                                        serverNonce,
                                                        log);

                if (mac == null) {
                    mac = "";
                }

                // e.g.: algorithm=MD5, username="funambol", mac=4kLzmrm4Vu6MK8mC7VSE5w==
                hmacHeaderResponse = new StringBuffer("algorithm=");
                hmacHeaderResponse.append(HMAC_ALGORITHM).append(", username=\"");
                hmacHeaderResponse.append(serverId).append("\", mac=").append(mac);

            } catch (NoSuchAlgorithmException e) {
                if (log.isEnabled(Level.FATAL)) {
                    log.fatal("Error calculating hmac: " + e);
                }
                log.debug("getSyncResponse", e);
            }
        }

        SyncResponse syncResponse = new Sync4jResponse(msg, mimeType,
            (hmacHeaderResponse == null ? null : hmacHeaderResponse.toString()),
            outMessage.isLastMessage());

        return syncResponse;
    }

    // --------------------------------------------------------- Private classes

    private static class Sync4jResponse implements SyncResponse {

        // -------------------------------------------------------- Private data
        private byte[] msg;
        private String resultMimeType;
        private String hmacHeader;
        private boolean lastMessage;

        // --------------------------------------------------------- Constructor
        private Sync4jResponse(final byte[] msg,
                               final String resultMimeType,
                               final String hmacHeader,
                               final boolean lastMessage) {

            this.msg = msg;
            this.resultMimeType = resultMimeType;
            this.hmacHeader     = hmacHeader;
            this.lastMessage    = lastMessage;
        }

        // ------------------------------------------------------ Public Methods

        /**
         * Returns the response content.
         *
         * @return the response content.
         */
        public byte[] getMessage() {
            return this.msg;
        }

        /**
         * Returns the mime type of the response.
         *
         * @return the mime type of the response.
         */
        public String getMimeType() {
            return resultMimeType;
        }

        /**
         * Returns the hmac header to add to the trasport header.
         *
         * @return the hamc header. <code>null</code> if hmac header is not required.
         */
        public String getHMACHeader() {
            return this.hmacHeader;
        }

        /**
         * Is this message the last message allowed for the current session?
         *
         * @return true if yes, false otherwise
         */
        public boolean isCompleted() {
            return lastMessage;
        }
    }

}
