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

package com.funambol.framework.logging;

/**
 * Contains the name of the logger used in Funambol
 *
 * @version $Id: FunambolLoggerName.java,v 1.3 2006/08/07 21:09:21 nichele Exp $
 */
public interface FunambolLoggerName {

    public static final String TRANSPORT_HTTP              = "transport.http";
    public static final String DM                          = "dm";
    public static final String SERVER_DM_BOOTSTRAP         = "dm.bootstrap";
    public static final String SERVER_DM_NOTIFICATION      = "dm.notification";
    public static final String HANDLER                     = "handler";
    public static final String ENGINE                      = "engine";
    public static final String FRAMEWORK_ENGINE            = "framework.engine";
    public static final String SERVER_ADMIN                = "server.admin";
    public static final String TEST_TOOLS_POST_SYNCML      = "test.tools.PostSyncML";
    public static final String LOG_SQL                     = "log.sql";
    public static final String SOURCE                      = "source";

}
