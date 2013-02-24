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

package com.funambol.framework.engine;


import com.funambol.framework.engine.source.SyncSource;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * This is a base implementation of a <i>SyncOperationStatus</i>
 *
 *
 * @version $Id: SyncOperationStatusImpl.java,v 1.2 2006/08/07 21:09:19 nichele Exp $
 *
 */
public abstract class SyncOperationStatusImpl implements SyncOperationStatus {

    // -------------------------------------------------------------- Properties

    /**
     * The operation this object represents the execution for
     */
    private SyncOperation operation = null;

    /** Getter for property operation.
     * @return Value of property operation.
     *
     */
    public SyncOperation getOperation() {
        return operation;
    }

    /**
     * The source the operation was executed on
     */
    private SyncSource syncSource = null;

    /** Getter for property syncSource.
     * @return Value of property syncSource.
     *
     */
    public SyncSource getSyncSource() {
        return syncSource;
    }


    // ------------------------------------------------------------- Contructors

    /**
     * Creates a new instance of SyncOperationStatus
     *
     * @param operation the operation - NOT NULL
     * @param syncSource the source - NOT NULL
     *
     * @throws IllegalArgumentException in case operation is null
     */
    public SyncOperationStatusImpl(SyncOperation operation, SyncSource syncSource) {
        if (operation == null) {
            throw new IllegalArgumentException("operation cannnot be null");
        }
        if (syncSource == null) {
            throw new IllegalArgumentException("syncSource cannnot be null");
        }

        this.operation  = operation ;
        this.syncSource = syncSource;
    }

    // ---------------------------------------------------------- Public methods

    public String toString() {
        return new ToStringBuilder(this).
            append("operation",  operation.toString() ).
            append("syncSource", syncSource.toString()).
            toString();
    }

    /** The operation status code
     * @return the operation status code
     *
     */
    abstract public int getStatusCode();

}
