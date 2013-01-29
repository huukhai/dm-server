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

package com.funambol.framework.engine.source;

import java.security.Principal;
import java.sql.Timestamp;

import com.funambol.framework.engine.SyncItem;
import com.funambol.framework.engine.SyncItemKey;


/**
 * Implements the basic functionalities of a <i>SyncSource</i> like naming.
 *
 *
 *
 * @version $Id: AbstractSyncSource.java,v 1.2 2006/08/07 21:09:20 nichele Exp $
 */
public abstract class AbstractSyncSource implements SyncSource, java.io.Serializable {
    // --------------------------------------------------------------- Constants

    public static final String LOG_NAME = "source";

    // ---------------------------------------------------------- Protected data

    protected String name       = null;
    protected String type       = null;
    protected String sourceURI  = null;
    protected SyncSourceInfo info      = null;

    // ------------------------------------------------------------ Constructors

    /** Creates a new instance of AbstractSyncSource */
    protected AbstractSyncSource() {
    }

    public AbstractSyncSource(String name, String type, String sourceURI) {
        if (name == null) {
            throw new NullPointerException("name is null!");
        }

        this.name      = name;
        this.type      = (type == null) ? "unknown" : type;
        this.sourceURI = sourceURI;
    }

    public AbstractSyncSource(String name) {
        this(name, null, null);
    }

    // ---------------------------------------------------------- Public methods

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /** Getter for property uri.
     * @return Value of property uri.
     */
    public String getSourceURI() {
        return sourceURI;
    }

    /** Setter for property uri.
     * @param sourceURI New value of property uri.
     */
    public void setSourceURI(String sourceURI) {
        this.sourceURI = sourceURI;
    }

    /** Returns the type info of the content handled by this source
     *
     * @return the type info of the content handled by this source
     *
     */
    public SyncSourceInfo getInfo() {
        return this.info;
    }

    /**
     * Setter for the property <i>info</i>
     */
    public void setInfo(SyncSourceInfo info) {
        this.info = info;
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer(super.toString());

        sb.append(" - {name: ").append(getName()     );
        sb.append(" type: "   ).append(getType()     );
        sb.append(" uri: "    ).append(getSourceURI());
        sb.append("}"         );

        return sb.toString();
    }

    public void beginSync(Principal principal, int syncMode) throws SyncSourceException {
    }

    public void endSync(Principal principal) throws SyncSourceException {
    }

    // -------------------------------------------------------- Abstract methods

    public abstract SyncItem[] getAllSyncItems(Principal principal) throws SyncSourceException;

    public abstract SyncItemKey[] getDeletedSyncItemKeys(Principal principal,
                                                         Timestamp since    ) throws SyncSourceException;

    public abstract SyncItem[] getDeletedSyncItems(Principal principal,
                                                   Timestamp since    ) throws SyncSourceException;

    public abstract SyncItemKey[] getNewSyncItemKeys(Principal principal,
                                                     Timestamp since    ) throws SyncSourceException;

    public abstract SyncItem[] getNewSyncItems(Principal principal,
                                               Timestamp since    ) throws SyncSourceException;

    public abstract SyncItem getSyncItemFromId(Principal principal, SyncItemKey syncItemKey) throws SyncSourceException;

    public abstract SyncItem[] getSyncItemsFromIds(Principal principal, SyncItemKey[] syncItemKeys) throws SyncSourceException;

    public abstract SyncItem[] getUpdatedSyncItems(Principal principal,
                                                   Timestamp since    ) throws SyncSourceException;

    public abstract void removeSyncItem(Principal principal, SyncItem syncItem) throws SyncSourceException;

    public abstract void removeSyncItems(Principal principal, SyncItem[] syncItems) throws SyncSourceException;

    public abstract SyncItem setSyncItem(Principal principal, SyncItem syncInstance) throws SyncSourceException;

    public abstract SyncItem[] setSyncItems(Principal principal, SyncItem[] syncItems) throws SyncSourceException;

    public abstract SyncItem getSyncItemFromTwin(Principal principal, SyncItem syncItem) throws SyncSourceException;

    public abstract SyncItem[] getSyncItemsFromTwins(Principal principal, SyncItem[] syncItems) throws SyncSourceException;
}
