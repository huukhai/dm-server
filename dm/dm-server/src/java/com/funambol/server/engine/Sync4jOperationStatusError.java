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

package com.funambol.server.engine;

import com.funambol.framework.core.ModificationCommand;
import com.funambol.framework.core.StatusCode;
import com.funambol.framework.engine.SyncOperation;
import com.funambol.framework.engine.source.SyncSource;
import com.funambol.framework.server.error.ServerException;

import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * This class represents the status of the execution of a <i>SyncOperation</i>
 * that has failed.
 *
 *
 * @version $Id: Sync4jOperationStatusError.java,v 1.4 2007-06-19 08:16:17 luigiafassina Exp $

 * @see com.funambol.framework.engine.SyncOperation
 *
 */
public class Sync4jOperationStatusError extends Sync4jOperationStatus{

    // -------------------------------------------------------------- Properties

    /**
     * The error condition if caused by an exception
     */
    private Throwable error = null;

    /** Getter for property error.
     * @return Value of property error.
     *
     */
    public Throwable getError() {
        return error;
    }

    // ------------------------------------------------------------- Contructors

    /**
     * Creates a new instance of SyncOperationStatusError
     *
     * @param operation the operation - NOT NULL
     * @param syncSource the source the operation was performed on - NOT NULL
     * @param cmd the command this status relates to - NULL
     * @param error the error condition - NULL
     *
     * @throws IllegalArgumentException in case operation is null
     */
    public Sync4jOperationStatusError(SyncOperation       operation ,
                                      SyncSource          syncSource,
                                      ModificationCommand cmd       ,
                                      Throwable           error     ) {
        super(operation, syncSource, cmd);

        this.error = error;
    }

    // ---------------------------------------------------------- Public methods

    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);

        builder.append("operation" , getOperation().toString() );
        builder.append("syncSource", getSyncSource().toString());
        if (error != null) {
            builder.append("error", error.getMessage());
        } else {
            builder.append("error", (String)null);
        }

        return builder.toString();
    }

    /** Which SyncML status code this condition corresponds to?
     *
     */
    public int getStatusCode() {
        Throwable cause = error.getCause();

        if (cause instanceof ServerException) {
            return ((ServerException)cause).getStatusCode();
        } else {
            return StatusCode.COMMAND_FAILED;
        }
    }

}
