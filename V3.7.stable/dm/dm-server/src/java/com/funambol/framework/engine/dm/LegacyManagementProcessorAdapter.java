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

import java.security.Principal;

import com.funambol.framework.core.Alert;
import com.funambol.framework.core.dm.ddf.DevInfo;

/**
 * 
 * This adapter permits to use old processors just extending this class instead 
 * of implementing ManagementProcessor. In order to make easy the use of already developed 
 * processors for previous version of the Funambol DM Server an adapter class is provided.
 *
 */
public abstract class LegacyManagementProcessorAdapter implements
        ManagementProcessor {

    /* (non-Javadoc)
     * @see com.funambol.framework.engine.dm.ManagementProcessor#beginSession(javax.ejb.SessionContext)
     * 
     * Calls the old contract beginSession(sessionId, principal, type, devInfo, dmState)
     * used by already developed processors for previous version of the Funambol DM Server
     * 
     */
    public void beginSession(SessionContext context) throws ManagementException {
        
        beginSession(context.getSessionId(), 
                context.getPrincipal(), 
                context.getType(), 
                context.getDevInfo(),
                context.getDmstate());
        
    }

    /**
     * Called to notify the processor for received generic alerts
     *
     * Provides an empty implementation of the method setGenericAlert for old processors 
     * that don't provide this functionality. No need to change those processor anymore.
     * 
     * @param genericAlerts the array of one or more generic alerts
     *
     * @throws ManagementException in case of errors
     */
    public void setGenericAlert(Alert[] genericAlerts)
            throws ManagementException {
    }

    /**
     * Called by old SyncML DM 1.1 processors when a management session is started for the given principal.
     * sessionId is the content of the SessionID element of the OTA DM message.
     *
     * @param sessionId the content of the SessionID element of the OTA DM message
     * @param p         the principal who started the management session
     * @param type      the management session type (as specified in the message Alert)
     * @param devInfo   the device info of the device under management
     * @param dmstate   the device management state
     *
     * @throws ManagementException in case of errors
     */
    public abstract void beginSession(String sessionId,
            Principal     p        ,
            int           type     ,
            DevInfo       devInfo  ,
            DeviceDMState dmstate  )
    throws ManagementException;
    
}
