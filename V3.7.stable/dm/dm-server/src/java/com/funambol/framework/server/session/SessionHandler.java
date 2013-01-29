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

package com.funambol.framework.server.session;


import com.funambol.framework.core.Sync4jException;
import com.funambol.framework.core.SyncML;
import com.funambol.framework.server.error.ServerFailureException;
import com.funambol.framework.server.error.InvalidCredentialsException;
import com.funambol.framework.engine.SyncEngine;
import com.funambol.framework.protocol.ProtocolException;
import com.funambol.framework.server.Sync4jDevice;


/**
 * This interface represent the functionality a session object must provide.
 * Different implementations can provide their own policy, but they have to be
 * compliant at least to this contract.
 *
 * The entry point is <i>processMessage()</i>, which determines which message is
 * expected and what processing has to be done (depending on the value of
 * <i>currentState</i>).
 * <p>
 * In the current implementation separate initialization is required.
 * <p>
 * <i>SessionHandler</i> makes use of a <i>SyncEngine</i> for all
 * tasks not related to the handling of the protocol.
 * See <i>com.funambol.framework.engine.SyncEngine</i> for more information.
 *
 * @see com.funambol.framework.engine.SyncEngine
 *
 * @version $Id: SessionHandler.java,v 1.3 2006/11/15 15:19:55 nichele Exp $
 */
public interface SessionHandler {

    // --------------------------------------------------------------- Constants

    public static final int STATE_START            = 0x0000;
    public static final int STATE_END              = 0x0001;
    public static final int STATE_SESSION_ABORTED  = 0x0002;
    public static final int STATE_ERROR            = 0xFFFF;


    // ---------------------------------------------------------- Public methods


    public String getSessionId();

    /**
     * Sets the mime type
     *
     */
    public void setMimeType(String mimeType);


    /**
     * The session a new session?
     *
     * @return true if the session is new, false otherwise
     */
    public boolean isNew();

    public void setNew(boolean newSession);

    /**
     * Returns true if the sessione has been authenticated.
     *
     * @return true if the sessione has been authenticated, false otherwise
     */
    public boolean isAuthenticated();

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
    throws ProtocolException, InvalidCredentialsException, ServerFailureException;

    /**
     * Processes an error condition. This method is called when the error is
     * is not fatal and is manageable at a protocol/session level so that a
     * well formed SyncML message can be returned.
     * <p>
     * Note that the offending message <i>msg</i> cannot be null, meaning that
     * at least the incoming message was a SyncML message. In this context,
     * <i>RepresentationException</i>s are excluded.
     *
     * @param msg the offending message - NOT NULL
     * @param error the exception representing the error condition - NOT NULL
     *
     * @throws com.funambol.framework.core.Sync4jException in case of unexpected errors
     *
     * @return the response message
     */
    public SyncML processError(SyncML msg, Throwable error)
    throws Sync4jException;

    /**
     * Called by the <i>SessionManager</i> when the session is expired.
     * It logs out the credential and release aquired resources.
     */
    public void expire();

    /**
     * Gets the creation timestamp of the session
     */
    public long getCreationTimestamp();

    /**
     * Indicates that the current session successfully ended. The synch-
     * ronization can be finalized and committed.
     */
    public void commit();

    /**
     * Indicates that the current session must be forced to end without
     * commiting the work (note that this does not automatically mean that the
     * synchronization is rolled back).
     *
     * @param statusCode the reason for the aborting
     */
    public void abort(int statusCode);

    /**
     * Gets the current state
     *
     * @return the current state
     */
    public int getCurrentState();


    /**
     * Gets the device in session
     *
     * @return the device in session
     */
    public Sync4jDevice getDevice();


}
