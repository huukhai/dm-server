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

import java.io.Serializable;
import java.util.Hashtable;

import javax.security.auth.login.LoginContext;
import javax.naming.Context;
import javax.naming.InitialContext;

import com.funambol.server.syncbean.SyncRemote;
import com.funambol.framework.transport.http.SyncHolder;
import com.funambol.framework.server.SyncResponse;
import com.funambol.framework.server.error.ServerException;
import com.funambol.framework.server.error.ServerFailureException;
import com.funambol.commons.security.ClientLoginCallbackHandler;


/**
 * Implementes a <i>SyncHolder</i> wrapping a remote EJB.
 * <p>
 * This holder must be used in a clustered environment.
 *
 * @version $Id: RemoteEJBSyncHolder.java,v 1.3 2006/11/15 16:12:03 nichele Exp $
 */
public class RemoteEJBSyncHolder
implements SyncHolder, Serializable {

    // --------------------------------------------------------------- Constants

    public static final String SYNCEJB_JNDI_NAME
        = "ejb:FunambolDMServer/funambol-server.jar//SyncBean!com.funambol.server.syncbean.SyncLocal?stateful";

    public static final String CONTEXT_URL_PREFIXES
        = "org.jboss.ejb.client.naming";

    // ------------------------------------------------------------ Private data

    private transient SyncRemote syncRemote          = null ;
    private transient boolean    newHolder         = false;
    private           String     sessionId                ;
    private           long       creationTimestamp        ;
    private String jndiAddress;

    // ------------------------------------------------------------ Constructors

    /**
     * For serialization purposes
     */
    protected RemoteEJBSyncHolder() {}

    /**
     * Creates a new SyncServerEJBSyncHolder setting the <i>newHolder</i>
     * properties to </i>newHolderFlag</i>. Please note that when the default
     * constructore is used (i.e.during deserialization), the default value
     * <i>false</i> is taken. This is wanted, because if the object is
     * deserialized, it is not new by construction. The same is true for
     * <i>creationTimestamp</i> but in this case no default value is provided
     * as it is not transient (the serialized value must be taken).
     *
     * @param newHolderFlag is this holder a new one?
     */
    public RemoteEJBSyncHolder(boolean newHolderFlag) {
        newHolder = newHolderFlag;
        if (newHolder) {
            creationTimestamp = System.currentTimeMillis();
        }
    }

    // ---------------------------------------------------------- Public methods

    /**
     * Processes an incoming message.<br>
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
        try {
            String username = "supersa";
            char[] password = "ass194amb".toCharArray();

            ClientLoginCallbackHandler handler =
                 new ClientLoginCallbackHandler(username, password);
            LoginContext lc = new LoginContext("client-login", handler);

            lc.login();

            return getSyncServerBeanInstance()
                   .processMessage(requestData, contentType, hmacHeader);
        } catch (Exception e) {
            throw new ServerException(e);
        }
    }

    @Override
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    public void setJndiAddress(String jndiAddress){
        this.jndiAddress = jndiAddress;
    }

    public String getJndiAddress() {
        return jndiAddress;
    }

    /** Called when the SyncHolder is not required any more. It gives the holder
     * an opportunity to do clean up and releaseing of resources.
     *
     * @throws java.lang.Exception in case of error. The real exception is stored
     * in the cause.
     *
     */
    @Override
    public void close() throws Exception {
        try {
            if (syncRemote != null) {
                syncRemote.remove();
            }
        } catch (Exception e) {
            throw new Exception("Error in closing the SyncHolder", e);
        }
    }

    /**
     * Returns the creation timestamp
     *
     * @return the creation timestamp
     */
    @Override
    public long getCreationTimestamp() {
        return creationTimestamp;
    }

    /**
     * Returns the newHolder flag
     *
     * @return the newHolder flag
     */
    public boolean isNew() {
        return newHolder;
    }

    // --------------------------------------------------------- Private methods

    /**
     * Returns the SyncBean instance to use for remote calls. Note that field
     * <i>syncBean</i> can be null if the bean has never been used or this
     * instance comes from a deserialization process.<br>
     * If <i>syncBean</i> is null then if <i>syncHandle</i> is not null, it is
     * used to get the EJB instance. If also </i>syncHandle</i> is null, the
     * bean is created.
     *
     * @return the SyncServerBean EJB instance
     *
     * @throws ServerFailureException in case of error
     */
    private SyncRemote getSyncServerBeanInstance()
    throws ServerFailureException {
        if (syncRemote != null) {
            return syncRemote;
        }

        //
        // Never used before.... create a new EJB instance from scratch
        // NOTE: session Id cannot be empty
        //
        if ((sessionId == null) || (sessionId.length() == 0)) {
            throw new ServerFailureException("No session id is associated to this handler");
        }

        InitialContext ctx;

         try {

             Hashtable props = new Hashtable();

             props.put(Context.URL_PKG_PREFIXES,        CONTEXT_URL_PREFIXES);

             ctx        = new InitialContext(props);
             syncRemote       = (SyncRemote)ctx.lookup(SYNCEJB_JNDI_NAME);
             syncRemote.setSessionId(sessionId);

        } catch (Exception e) {
            throw new ServerFailureException(e);
        }

        return syncRemote;
    }

}
