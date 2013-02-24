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

package com.funambol.framework.core;


/**
 * This class represents the possible alerts code.
 *
 * @see Alert
 * 
 * @version $Id: AlertCode.java,v 1.3 2006/11/15 14:19:22 nichele Exp $
 *
 */
public final class AlertCode {
    public final static int UNKNOWN                       =  -1;

    public final static int TWO_WAY                       = 200;
    public final static int SLOW                          = 201;
    public final static int ONE_WAY_FROM_CLIENT           = 202;
    public final static int REFRESH_FROM_CLIENT           = 203;
    public final static int ONE_WAY_FROM_SERVER           = 204;
    public final static int REFRESH_FROM_SERVER           = 205;
    public final static int TWO_WAY_BY_SERVER             = 206;
    public final static int ONE_WAY_FROM_CLIENT_BY_SERVER = 207;
    public final static int REFRESH_FROM_CLIENT_BY_SERVER = 208;
    public final static int ONE_WAY_FROM_SERVER_BY_SERVER = 209;
    public final static int REFRESH_FROM_SERVER_BY_SERVER = 210;
    public final static int RESULT_ALERT                  = 221;
    public final static int NEXT_MESSAGE                  = 222;

    public final static int DISPLAY                       = 1100;
    public final static int CONFIRM_OR_REJECT             = 1101;
    public final static int INPUT                         = 1102;
    public final static int SINGLE_CHOICE                 = 1103;
    public final static int MULTIPLE_CHOICE               = 1104;

    public final static int SERVER_INITIATED_MANAGEMENT   = 1200;
    public final static int CLIENT_INITIATED_MANAGEMENT   = 1201;

    public final static int MORE_DATA                     = 1222;
    public final static int SESSION_ABORT                 = 1223;
    public final static int CLIENT_EVENT                  = 1224;
    public final static int NO_END_OF_DATA                = 1225;
    public final static int GENERIC_ALERT                 = 1226;
    
    private AlertCode() {}

    /**
     * Determines if the given code is an initialization code, such as one of:
     * <ul>
     *   <li> TWO_WAY
     *   <li> SLOW
     *   <li> ONE_WAY_FROM_CLIENT
     *   <li> REFRESH_FROM_CLIENT
     *   <li> ONE_WAY_FROM_SERVER
     *   <li> REFRESH_FROM_SERVER
     *   <li> TWO_WAY_BY_SERVER
     *   <li> ONE_WAY_FROM_CLIENT_BY_SERVE
     *   <li> REFRESH_FROM_CLIENT_BY_SERVER
     *   <li> ONE_WAY_FROM_SERVER_BY_SERVER
     *   <li> REFRESH_FROM_SERVER_BY_SERVER
     *   <li> SERVER_INITIATED_MANAGEMENT
     *   <li> CLIENT_INITIATED_MANAGEMENT
     *   <li> MORE_DATA
     * </ul>
     *
     * @param code the code to be checked
     *
     * @return true if the code is an initialization code, false otherwise
     */
    static public boolean isInitializationCode(int code) {
        return (  (code == TWO_WAY                      )
               || (code == SLOW                         )
               || (code == ONE_WAY_FROM_CLIENT          )
               || (code == REFRESH_FROM_CLIENT          )
               || (code == ONE_WAY_FROM_SERVER          )
               || (code == REFRESH_FROM_SERVER          )
               || (code == TWO_WAY_BY_SERVER            )
               || (code == ONE_WAY_FROM_CLIENT_BY_SERVER)
               || (code == REFRESH_FROM_CLIENT_BY_SERVER)
               || (code == ONE_WAY_FROM_SERVER_BY_SERVER)
               || (code == REFRESH_FROM_SERVER_BY_SERVER)
               || (code == SERVER_INITIATED_MANAGEMENT  )
               || (code == CLIENT_INITIATED_MANAGEMENT  )
               || (code == MORE_DATA                    )
               );
    }

    /**
     * Determines if the given code represents a client only action, such as is
     * one of:
     * <ul>
     *   <li>ONE_WAY_FROM_CLIENT</li>
     *   <li>REFRESH_FROM_CLIENT</li>
     * </ul>
     *
     * @param code the code to be checked
     *
     * @return true if the code represents a client only action, false otherwise
     */
    static public boolean isClientOnlyCode(int code) {
        return (  (code == ONE_WAY_FROM_CLIENT)
               || (code == REFRESH_FROM_CLIENT)
               );
    }

    /**
     * Determines if the given code represents a user alert action, such as
     * one of:
     * <ul>
     *   <li>DISPLAY</li>
     *   <li>CONFIRM_OR_REJECT</li>
     *   <li>INPUT</li>
     *   <li>SINGLE_CHOICE</li>
     *   <li>MULTIPLE_CHOICE</li>
     * </ul>
     *
     * @param code the code to be checked
     *
     * @return true if the code represents a client only action, false otherwise
     */
    static public boolean isUserAlertCode(int code) {
        return (  (code == DISPLAY          )
               || (code == CONFIRM_OR_REJECT)
               || (code == INPUT            )
               || (code == SINGLE_CHOICE    )
               || (code == MULTIPLE_CHOICE  )
               );
    }

}
