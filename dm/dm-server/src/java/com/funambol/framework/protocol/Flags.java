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

package com.funambol.framework.protocol;

/**
 * This interface grups flags used to drive the reading and the creations of
 * syncml packages.
 *
 *
 * @version $Id: Flags.java,v 1.2 2006/08/07 21:09:21 nichele Exp $
 */
public interface Flags {

    public static int HOW_MANY_FLAGS = 11;

    /**
     * Generic flags
     */
    public static int FLAG_ALL                    = -1;
    public static int FLAG_ALL_RESPONSES_REQUIRED = -2;

    /**
     * Specific flags
     */
    public static int FLAG_SYNC_RESPONSE_REQUIRED          =  0;
    public static int FLAG_MODIFICATIONS_RESPONSE_REQUIRED =  1;
    public static int FLAG_SYNC_STATUS_REQUIRED            =  2;
    public static int FLAG_FINAL_MESSAGE                   =  3;

    /**
     * should a deleted item be archived ?
     */
    public static int FLAG_ARCHIVE_DATA                    =  4;

    /**
     * are deletions soft deletions?
     */
    public static int FLAG_SOFT_DELETE                     =  5;

    /**
     * Is an Alert required?
     */
    public static int FLAG_ONE_ALERT_REQUIRED              =  6;

    /**
     * Is an Session abort required?
     */
    public static int FLAG_SESSION_ABORT_REQUIRED          =  7;

    /**
     * Are demands more messages ?
     */
    public static int FLAG_MORE_MSG_REQUIRED               =  8;

    /**
     * Server must send an abort alert ?
     */
    public static int FLAG_SERVER_ABORT_REQUIRED           =  9;

    /**
     * Server not must send a chal ?
     */
    public static int FLAG_CHAL_NOT_REQUIRED               =  10;
}
