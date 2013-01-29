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

import org.apache.commons.lang.builder.ToStringBuilder;

import com.funambol.framework.core.Constants;
import com.funambol.framework.core.VerProto;
import com.funambol.framework.core.dm.ddf.DevInfo;
import com.funambol.framework.security.Sync4jPrincipal;

/**
 * Contains the session context used in the DM process. The SessionContext has:
 * <ul>
 * <li>protocol version</li>
 * <li>sessionId is the content of the SessionID element of the OTA DM message</li>
 * <li>type is the management session type (as specified in the message Alert)</li>
 * <li>info is the device info of the device under management</li> 
 * <li>dmstate is the device management state, which represents a row of the fnbl_dm_state table</li>
 * </ul>
 * 
 */
public class SessionContext implements java.io.Serializable {

    // -------------------------------------------------------------- Constants
    
       public static final VerProto SYNCML_DM_1_1   = Constants.SYNCML_DM_1_1 ;
       public static final VerProto SYNCML_DM_1_2   = Constants.SYNCML_DM_1_2 ;
    
    // -------------------------------------------------------------- Properties

    private String          sessionId          = null;
    private Sync4jPrincipal principal          = null;
    private int             type;
    private DevInfo         devInfo            = null;
    private DeviceDMState   dmstate            = null; 
    private VerProto        verPro             = null;
    
    // ------------------------------------------------------------- Constructor

    /**
     * Creates a new SyncContext
     * @param sessionId     the content of the SessionID element of the DM message
     * @param principal     the principal
     * @param type          the management session type
     * @param devInfo       the device info of the device under management
     * @param dmstate       the device management state
     * @param verPro        the version protocol
     * 
     */
    public SessionContext(String sessionId        ,
                       Sync4jPrincipal principal  ,
                       int             type       ,
                       DevInfo         devInfo    ,
                       DeviceDMState   dmstate    ,
                       VerProto        verPro   ) {

        this.sessionId          = sessionId;
        this.principal          = principal;
        this.type               = type     ;
        this.devInfo            = devInfo  ;
        this.dmstate            = dmstate  ;
        this.verPro             = verPro   ;
        
    }
    
    // ---------------------------------------------------------- Public Methods            

    /**
     * Returns a String representation of this Session Context
     * @return String
     */
    public String toString() {
        ToStringBuilder sb = new ToStringBuilder(this).append("\r\n");

        sb.append("sessionId", sessionId);
        sb.append("principal", principal);
        sb.append("type"     , type);
        sb.append("devInfo"  , devInfo.getDevId());
        sb.append("dmstate"  , dmstate);
        sb.append("verPro"   , verPro.getVersion());
        
        return sb.toString();
    }
    
    /**
     * Returns the principal
     * @return the principal property 
     */
    public Sync4jPrincipal getPrincipal() {
        return principal;
    }

    /**
     * Returns the device info of the device under management
     * @return the devinfo property 
     */    
    public DevInfo getDevInfo() {
        return this.devInfo;
    }

    /**
     * Returns the device management state
     * @return the dmstate property 
     */    
    public DeviceDMState getDmstate() {
        return this.dmstate;
    }
    
    /**
     * Returns the content of the SessionID element of the DM message
     * @return the sessionId property 
     */
    public String getSessionId() {
        return this.sessionId;
    }

    /**
     * Returns the management session type
     * @return the type property 
     */    
    public int getType() {
        return this.type;
    }

    /**
     * Returns the version protocol
     * @return the verPro property 
     */    
    public VerProto getVerPro() {
        return this.verPro;
    }
    
}
