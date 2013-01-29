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


import javax.ejb.Remove;

import com.funambol.framework.server.SyncResponse;
import com.funambol.framework.server.error.ServerException;

/**
 * This is the Bussiness interface for SyncBean
 */
public interface SyncBusiness {
    /**
     * Used to process a SyncML message as translated by the client object.
     *
     * @param messageData the message as a stream of byes. NOT NULL
     * @param mimeType the mime type of the stream.
     * @param hmacHeader the hmac value associated with the request
     *
     * @throws com.funambol.framework.server.error.ServerException in case 
     *         of an error in the server that the client should be aware of.
     *
     * @return the response message
     */
    public SyncResponse processMessage(byte[] messageData,
                           String mimeType, String hmacHeader) throws ServerException;

    /**
     * Used to process a status information as needed by the client object (i.e.
     * errors or success).
     *
     * @param statusCode the status code
     * @param info additional descriptive message
     *
     * @return the response message
     *
     * @see com.funambol.framework.core.StatusCode for valid status codes.
     */
    public SyncResponse processStatusCode(int statusCode, String info);

    /**
     * Setter of SessionId
     * @param sessionId SessionId
     */
    public void setSessionId(String sessionId);

    /**
     * Getter of SessionId
     * @return SessionId
     */
    public String getSessionId();

    /**
     * User call this method to initialize SyncBean
     * @param sessionId SessionId
     * @exception ServerException
     */
    public void initBussiness(String sessionId) throws ServerException;

    /**
     * User call this method to initialize SyncBean
     * @exception ServerException
     */
    public void initBussiness() throws ServerException;

    /**
     * User call this method to remove SyncBean
     * @see javax.ejb.Remove
     */
    @Remove
    public void remove();
}
