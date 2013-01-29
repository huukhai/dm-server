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

package com.funambol.transport.http.server;

import javax.naming.InitialContext;

import com.funambol.framework.transport.http.SyncHolder;
import com.funambol.framework.server.SyncResponse;
import com.funambol.server.syncbean.SyncLocal;
import com.funambol.framework.core.Sync4jException;
import com.funambol.framework.server.error.ServerException;
import com.funambol.framework.server.error.ServerFailureException;



/**
 * Implementes a <i>SyncHolder</i> wrapping a local EJB.
 *
 *
 * @version $Id: LocalEJBSyncHolder.java,v 1.2 2006/08/07 21:09:26 nichele Exp $
 */
public class LocalEJBSyncHolder implements SyncHolder {

    // --------------------------------------------------------------- Constants

    public static final String SYNCEJB_JNDI_NAME
        = "java:app/funambol-server.jar/SyncBean!com.funambol.server.syncbean.SyncLocal";

    // ------------------------------------------------------------ Private data

    private SyncLocal     syncLocal         = null;
    private String        sessionId         = null;
    private long          creationTimestamp       ;

    // ------------------------------------------------------------ Constructors

    public LocalEJBSyncHolder() {

        creationTimestamp = System.currentTimeMillis();
    }

    // ---------------------------------------------------------- Public methods

    /** Processes an incoming message.
     *
     * @param requestData the SyncML request as stream of bytes
     * @param contentType the content type associated with the request
     * @param hmacHeader the hmac value associated with the request
     *
     * @return the SyncML response as a <i>ISyncResponse</i> object
     *
     * @throws ServerException in case of a server error
     *
     */
    @Override
    public SyncResponse processMessage(byte[] requestData, String contentType, String hmacHeader)
    throws ServerException {
        return syncLocal.processMessage(requestData, contentType, hmacHeader);
    }

    @Override
    public void setSessionId(String sessionId) throws Sync4jException {

        try {
            InitialContext ctx = new InitialContext();
            syncLocal = (SyncLocal)ctx.lookup(SYNCEJB_JNDI_NAME);
            syncLocal.initBussiness();
        } catch(Exception e){
            throw new ServerFailureException(e);
        }

        syncLocal.setSessionId(sessionId);
        this.sessionId = sessionId;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    /** Called when the SyncHolder is not required any more. It gives the holder
     * an opportunity to do clean up and releaseing of resources.
     *
     * @throws java.lang.Exception in case of error. The real exception is stored
     * in the cause.
     *
     */
    @Override
    public void close() {
            syncLocal.remove();
    }

    /**
     * Returns the creation timestamp (in milliseconds since midnight, January
     * 1, 1970 UTC).
     *
     * @see com.funambol.framework.transport.http.SyncHolder
     */
    @Override
    public long getCreationTimestamp() {
        return creationTimestamp;
    }
}
