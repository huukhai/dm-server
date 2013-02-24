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

package com.funambol.framework.engine.dm;
import com.funambol.framework.core.Alert;

/**
 * The ManagementProcessor interface defines the structure of an object able
 * to handle management sessions.
 *
 * @version $Id: ManagementProcessor.java,v 1.3 2006/11/15 13:47:31 nichele Exp $
 */
public interface ManagementProcessor {

    /**
     *
     * Called when a management session is started for the given principal.
     *
     * @param context is a DM Session Context and contains:
     * <ul>
     *  <li>protocol version</li>
     *  <li>sessionId is the content of the SessionID element of the OTA DM message</li>
     *  <li>type is the management session type (as specified in the message Alert)</li>
     *  <li>info is the device info of the device under management</li>
     *  <li>dmstate is the device management state, which represents a row of the fnbl_dm_state table.</li>
     * </ul>
     * @throws ManagementException in case of errors
     */
     public void beginSession(SessionContext context) throws ManagementException;

    /**
     * Called when the management session is closed. CompletionCode can be one
     * of:
     * <ul>
     *  <li>DM_SESSION_SUCCESS</li>
     *  <li>DM_SESSION_ABORT</li>
     *  <li>DM_SESSION_FAILED</li>
     *
     * @param completionCode the management session competion code
     *
     * @throws ManagementException in case of errors
     */
    public void endSession(int completionCode) throws ManagementException;

    /**
     * Called to retrieve the next management operations to be performed.
     *
     * @return an array of ManagementOperation representing the management
     *          operations to be performed.
     *
     * @throws ManagementException in case of errors
     */
    public ManagementOperation[] getNextOperations() throws ManagementException;

    /**
     * Called to notify the processor for received generic alerts
     *
     * @param genericAlerts is an array of one or more generic alerts
     *
     * @throws ManagementException in case of errors
     */
    public void setGenericAlert(Alert[] genericAlerts) throws ManagementException;

    /**
     * Called to set the results of a set of management operations.
     *
     * @param results the results of a set of management operations.
     *
     * @throws ManagementException in case of errors
     */
    public void setOperationResults(ManagementOperationResult[] results)
    throws ManagementException;

    /**
     * Name to uniquely identify the management processor instance (each
     * installed management processor should have a different name in its
     * configuration file).
     *
     * @return the management processor name
     */
    public String getName();
}
