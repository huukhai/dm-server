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

package com.funambol.framework.server;


import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * This class specializes a <i>SyncTimestamp</i> to represent a last sync
 * timestamp as retrieved from the database. It associates a sync timestamp with
 * a particular principal and database.
 *
 * @version $Id: LastTimestamp.java,v 1.2 2006/08/07 21:09:22 nichele Exp $
 */
public class LastTimestamp extends SyncTimestamp {

    // ------------------------------------------------------------- Public data

    public String principal = null;  // the id of the principal
    public String database  = null;

    // ------------------------------------------------------------ Constructors

    /** Creates a new instance of LastTimestamp */
    public LastTimestamp(final String principal, final String database) {
        super();
        this.principal = principal ;
        this.database  = database  ;
    }

    /**
     * Creates a new instance of LastTimestamp.
     * It is need to cache the next anchor of the server and the next anchor
     * of the client.
     *
     * @param principal the principal id
     * @param database the source for sync
     * @param tagServer the last/next tag of the server
     * @param tagClient the last/next tag of the client
     * @param start the timestamp of when sync started
     * @param end the timestamo of when sync ended
     *
     */
    public LastTimestamp(final String principal,
                         final String database,
                         final String tagServer,
                         final String tagClient,
                         final long start,
                         final long end) {
        this(principal, database);
        this.tagServer = tagServer;
        this.tagClient = tagClient;
        this.start    = start    ;
        this.end      = end      ;
    }

    // ---------------------------------------------------------- Public methods

    public String toString() {
        ToStringBuilder sb = new ToStringBuilder(this);

        sb.append("principal", principal).
           append("database" , database).
           append("tagServer", tagServer).
           append("tagClient", tagClient).
           append("start"    , new java.sql.Timestamp(start)).
           append("end"      , new java.sql.Timestamp(end)  );

        return sb.toString();
    }
}
