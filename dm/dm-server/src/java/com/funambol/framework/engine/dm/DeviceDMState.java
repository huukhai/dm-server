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

import java.io.Serializable;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * This class represents the device's DM state.
 *
 *
 *
 * @version $Id: DeviceDMState.java,v 1.4 2006/11/15 13:46:05 nichele Exp $
 *
 */
public class DeviceDMState implements Serializable {

    // --------------------------------------------------------------- Constants

    public static final byte STATE_UNKNOWN      = '-';
    public static final byte STATE_UNMANAGEABLE = 'U';
    public static final byte STATE_MANAGEABLE   = 'M';
    public static final byte STATE_NOTIFIED     = 'N';
    public static final byte STATE_IN_PROGRESS  = 'P';
    public static final byte STATE_COMPLETED    = 'C';
    public static final byte STATE_ERROR        = 'E';
    public static final byte STATE_ABORTED      = 'A';

    public static final String PROPERTY_STATE      = "state" ;
    public static final String PROPERTY_SESSION_ID = "mssid" ;
    public static final String PROPERTY_DEVICE_ID  = "device";

    // ------------------------------------------------------------- Public data

    public String id         ;
    public String deviceId   ;
    public String mssid      ;
    public byte   state      ;
    public Date   start      ;
    public Date   end        ;
    public String operation  ;
    public String info       ;

    // ------------------------------------------------------------- Costructors

    /** Creates a new instance of DeviceDMState */
    public DeviceDMState() {
        init(null, null, STATE_UNKNOWN, null, null, null, null);
    }

    /**
     * Creates a new instance of DeviceDMState for a give device
     *
     * @param deviceId the device id
     */
    public DeviceDMState(String deviceId) {
        init(deviceId, null, STATE_UNKNOWN, null, null, null, null);
    }

    // ---------------------------------------------------------- Public methods

    /**
     * Copies the given object into this.
     *
     * @param dms the DeviceDMState to copy from
     */
    public void copyFrom(DeviceDMState dms) {
        this.id        = dms.id       ;
        this.deviceId  = dms.deviceId ;
        this.mssid     = dms.mssid    ;
        this.state     = dms.state    ;
        this.start     = dms.start    ;
        this.end       = dms.end      ;
        this.operation = dms.operation;
        this.info      = dms.info     ;
    }
    // --------------------------------------------------------- Private methods

    /**
     * Init method.
     *
     * @param deviceId device id
     * @param mssid management server session id
     * @param state state
     * @param start session start
     * @param end session end
     * @param operation application specific management operation
     * @param info application specific details
     *
     */
    private void init(String deviceId ,
                      String mssid    ,
                      byte   state    ,
                      Date   start    ,
                      Date   end      ,
                      String operation,
                      String info     ) {
        this.deviceId  = deviceId ;
        this.mssid     = mssid    ;
        this.state     = state    ;
        this.start     = start    ;
        this.end       = end      ;
        this.operation = operation;
        this.info      = info     ;
    }


    // -------------------------------------------------------------- toString()
    public String toString() {
        ToStringBuilder sb = new ToStringBuilder(this);

        sb.append("deviceId",  deviceId   );
        sb.append("mssid",     mssid      );
        sb.append("state",     (char)state);
        sb.append("start",     start      );
        sb.append("end",       end        );
        sb.append("operation", operation  );
        sb.append("info",      info       );

        return sb.toString();
    }

}
