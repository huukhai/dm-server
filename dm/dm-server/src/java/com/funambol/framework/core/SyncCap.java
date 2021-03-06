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


import java.util.ArrayList;
import java.util.Arrays;



/**
 * This class represents the synchronization capabilities of a datastore and
 * corresponds to the &lt;SyncCap&gt; tag in the SyncML devinf DTD
 *
 *  @see DataStore
 * 
 *
 *  @version $Id: SyncCap.java,v 1.2 2006/08/07 21:05:28 nichele Exp $
 */
public final class SyncCap
implements java.io.Serializable {

    // --------------------------------------------------------------- Constants
    private ArrayList<SyncType> syncTypes = new ArrayList<SyncType>();

    // ------------------------------------------------------------ Constructors

    /** For serialization purposes */
    protected SyncCap(){}

    /**
     * Creates a new SyncCap object that specifies the synchronization
     * capabilities of the given datastore
     *
     * @param syncTypes an array of type of the supported
     *                  synchronization - NOT NULL
     *
     */
    public SyncCap(final SyncType[] syncTypes) {
        setSyncType(syncTypes);
    }

    // ---------------------------------------------------------- Public methods

    /**
     *
     * @return The return value is guaranteed to be non-null.
     *      Also, the array's elements are guaranteed to
     *      be non-null.
     *
     */
    public ArrayList<SyncType> getSyncType() {
        return syncTypes;
    }

    public void setSyncType(SyncType[] syncTypes) {
        if (syncTypes == null || syncTypes.length == 0) {
            throw new IllegalArgumentException(
                                           "syncTypes cannot be null or empty");
        }
        this.syncTypes.clear();
        this.syncTypes.addAll(Arrays.asList(syncTypes));
    }
}
