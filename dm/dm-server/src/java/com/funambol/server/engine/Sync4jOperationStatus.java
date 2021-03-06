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
import com.funambol.framework.engine.SyncOperationStatusImpl;
import com.funambol.framework.engine.SyncOperation;
import com.funambol.framework.engine.source.SyncSource;

/**
 * This class represents a SyncOperation excecuted by Funambol. The main
 * difference between this and a normal <i>SyncOperationStatusImpl</i> is that
 * a <i>Sync4jOperationStatus</i> is associated to a SyncML command (such as
 * &lt;Add&gt;, &lt;Delete&gt;, ecc).
 *
 * @version $Id: Sync4jOperationStatus.java,v 1.4 2007-06-19 08:16:17 luigiafassina Exp $
 */
public abstract class Sync4jOperationStatus extends SyncOperationStatusImpl {

    // -------------------------------------------------------------- Properties

    /**
     * The command this status related to
     */
    protected ModificationCommand cmd = null;

    /** Getter for property cmd.
     * @return Value of property cmd.
     *
     */
    public ModificationCommand getCmd() {
        return cmd;
    }

    /**
     * Setter for property  cmd.
     *
     * @param cmd the cmd
     */
    public void setCommand(ModificationCommand cmd) {
        this.cmd = cmd;
    }

    /** Creates a new instance of Sync4jOperationStatus */
    public Sync4jOperationStatus(SyncOperation       operation ,
                                 SyncSource          syncSource,
                                 ModificationCommand cmd       ) {
        super(operation, syncSource);
        this.cmd = cmd;
    }

    /** Creates a new instance of Sync4jOperationStatus */
    public Sync4jOperationStatus(SyncOperation operation ,
                                 SyncSource    syncSource) {
        this(operation, syncSource, null);
    }
}
