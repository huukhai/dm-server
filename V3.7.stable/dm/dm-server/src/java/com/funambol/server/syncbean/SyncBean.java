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

package com.funambol.server.syncbean;


import javax.ejb.Init;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.funambol.framework.server.SyncResponse;
import com.funambol.framework.config.Configuration;
import com.funambol.framework.server.error.ServerException;

import com.funambol.server.admin.ejb.AdminLocal;
import com.funambol.server.engine.SyncAdapter;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;


/**
 * This is the session enterprise java bean that handles a synchronization
 * request. It is designed to be a stateful session bean so that the state
 * and the session management are on charge of the application server.
 *
 * SyncBean uses two environment properties:
 * <ul>
 *   <li><i>{ENV_ENGINE_FACTORY_NAME}</i> points to the bean to be used as a
 *       factory for the synchronization engine</li>
 *   <li><i>{ENV_ADMIN_NAME}</i> points to the bean to be used as a
 *       manager of administration's part
 * </ul>
 *
 * LOG NAME: funambol.server
 *
 * @version $Id: SyncBean.java,v 1.3 2006/11/15 16:08:43 nichele Exp $
 *
 */
@Stateful
public class SyncBean
implements SyncLocal, SyncRemote {

    // ------------------------------------------------------- Private constants

    private static final String ADMINEJB_JNDI_NAME
        = "java:module/AdminBean!com.funambol.server.admin.ejb.AdminLocal";

    // ------------------------------------------------------------ Private data

    private static final Logger log = Logger.getLogger(com.funambol.server.syncbean.SyncBean.class.getName());

    private String sessionId                    = null;

    private SyncAdapter syncAdapter             = null;

    // ------------------------------------------------------------- EJB methods
    /**
     *
     * @see javax.ejb.Init
     */
    @Init
    public void init() {

    }

    @Override
    public void initBussiness(String sessionId) throws ServerException {
        if(log.isEnabled(Level.TRACE)){
            log.trace("initBussiness(sessionId) is called");
        }

        try {
        syncAdapter = new SyncAdapter(loadConfiguration());
        syncAdapter.setSessionId(sessionId);
        this.sessionId = sessionId;
        } catch(Exception e) {
            if(log.isEnabled(Level.FATAL)){
                log.fatal("Error creating the SyncAdapter: " + e);
    }
            log.debug("init", e);
            String msg = "Error "    + e.getClass().getName()
                                     + " creating the SyncBean: "
                                     + e.getMessage();
            throw new ServerException( msg, e);
        }
    }

    @Override
    public void initBussiness() throws ServerException {
        if(log.isEnabled(Level.TRACE)){
            log.trace("initBussiness() is called");
        }
        try {
        syncAdapter = new SyncAdapter(loadConfiguration());
        } catch (Exception e) {
            if(log.isEnabled(Level.FATAL)){
                log.fatal("Error creating the SyncAdapter: " + e);
            }
            log.debug("init", e);

            String msg = "Error "    + e.getClass().getName()
                                     + " creating the SyncBean: "
                                     + e.getMessage();
            throw new ServerException( msg, e);
        }
    }
    /**
     * Creates an instance of AdminBean
     */
    private Configuration loadConfiguration() throws ServerException {

        try {
            Configuration config = null;
            InitialContext ctx = new InitialContext();
            AdminLocal adminLocal = (AdminLocal)ctx.lookup(ADMINEJB_JNDI_NAME);
            config = adminLocal.getConfig();

            return config;

        } catch (NamingException e) {
            log.debug("loadConfiguration", e);
            throw new ServerException( "Error "
                                     + e.getClass().getName()
                                     + " creating the AdminBean: "
                                     + e.getMessage()
                                     );
        } catch (Exception e) {
            log.debug("loadConfiguration", e);
            throw new ServerException( "Error "
                                     + e.getClass().getName()
                                     + " creating the AdminBean: "
                                     + e.getMessage()
                                     );
        }
    }


    /**
     * When the session terminates this object is removed. This can happen
     * because the synchronization process has been completed or because the
     * session has expired. If the session completes its job, the synchro-
     * nization can be committed.
     * @see javax.ejb.Remove
     */
    @Remove
    @Override
    public void remove() {
        if(log.isEnabled(Level.TRACE)){
            log.trace("remove() is called");
        }
        syncAdapter.endSync();
    }

    /**
     *
     * @see javax.ejb.PostActivate
     */
    @PostActivate
    public void postActivate() {
        if(log.isEnabled(Level.TRACE)){
            log.trace("postActive() is called");
    }
    }

    /**
     *
     * @see javax.ejb.PrePassivate
     */
    @PrePassivate
    public void prePassivate() {
        if(log.isEnabled(Level.TRACE)){
            log.trace("prePassive is called");
        }
    }

    /**
     *
     * process the incoming sync message
     *
     * @param msg must be non-null
     * @param mimeType may be null.
     * @param hmacHeader may be null.
     */
    @Override
    public SyncResponse processMessage(final byte[] msg,
                                       final String mimeType,
                                       final String hmacHeader) throws ServerException {
        if(log.isEnabled(Level.TRACE)){
            log.trace("processMessage() is called");
        }
        return syncAdapter.processMessage(msg, mimeType, hmacHeader);
    }

    /**
     * Used to process a status information as needed by the client object (i.e.
     * errors or success).
     *
     * @param statusCode the status code
     * @param info additional descriptive message
     *
     * @see com.funambol.framework.core.StatusCode for valid status codes.
     */
    @Override
    public SyncResponse processStatusCode(int statusCode, String info){
        if(log.isEnabled(Level.TRACE)){
            log.trace("processStatus() is called");
        }
        return syncAdapter.processStatusCode(statusCode, info);
    }

    /**
     *
     * @see com.funambol.server.syncbean.SyncBusiness
     */
    @Override
    public void setSessionId(String sessionId) {
        if(log.isEnabled(Level.TRACE)){
            log.trace("setSessionId(sessionId) is called");
        }
        this.sessionId = sessionId;
        syncAdapter.setSessionId(sessionId);
    }

    /**
     *
     * @see com.funambol.server.syncbean.SyncBusiness
     */
    @Override
    public String getSessionId() {
         if(log.isEnabled(Level.TRACE)){
            log.trace("getSessionId() is called");
        }
        return sessionId;
    }

}
