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
 * Corresponds to &lt;DataStore&gt; element in SyncML devinf DTD
 *
 *
 *
 * @version $Id: DataStore.java,v 1.3 2006/11/15 14:21:53 nichele Exp $
 */
public final class DataStore
implements java.io.Serializable {

    // ------------------------------------------------------------ Private data
    private SourceRef sourceRef;
    private String displayName;
    private long maxGUIDSize;
    private ContentTypeInfo rxPref;
    private ArrayList<ContentTypeInfo> rx = new ArrayList<ContentTypeInfo>();
    private ContentTypeInfo txPref;
    private ArrayList<ContentTypeInfo> tx = new ArrayList<ContentTypeInfo>();
    private DSMem dsMem;
    private SyncCap syncCap;

    // ------------------------------------------------------------ Constructors

    /** For serialization purposes */
    protected DataStore() {}

    /**
     * Creates a new DataStore object with the given input information
     *
     * @param sourceRef specifies the source address from the associated
     *                  command - NOT NULL
     * @param displayName the display name
     * @param maxGUIDSize the maximum GUID size. Set to -1 if the Maximum GUID
     *                  size is unknown or unspecified. Otherwise, this
     *                  parameter should be a positive number.
     * @param rxPref the relative information received to the content type
     *               preferred - NOT NULL
     * @param rx an array of the relative info received to the content type
     *           supported - NOT NULL
     * @param txPref the relative information trasmitted
     *                  to the content type preferred - NOT NULL
     * @param tx an array of the relative info trasmitted to the content type
     *           supported - NOT NULL
     * @param dsMem the datastore memory info
     * @param syncCap the synchronization capabilities - NOT NULL
     *
     */
    public DataStore(final SourceRef sourceRef,
                     final String displayName,
                     final long maxGUIDSize,
                     final ContentTypeInfo rxPref,
                     final ContentTypeInfo[] rx,
                     final ContentTypeInfo txPref,
                     final ContentTypeInfo[] tx,
                     final DSMem dsMem,
                     final SyncCap syncCap) {

        setSourceRef(sourceRef);
        setMaxGUIDSize(maxGUIDSize);
        setRxPref(rxPref);
        setRx(rx);
        setTxPref(txPref);
        setTx(tx);
        setSyncCap(syncCap);

        this.displayName = displayName;
        this.dsMem = dsMem;
    }

    // ---------------------------------------------------------- Public methods

    /**
     * Gets the sourceRef properties
     *
     * @return the sourceRef properties
     */
    public SourceRef getSourceRef() {
        return sourceRef;
    }

    /**
     * Sets the reference URI
     *
     * @param sourceRef the reference URI
     *
     */
    public void setSourceRef(SourceRef sourceRef) {
        if (sourceRef == null) {
            throw new IllegalArgumentException("sourceRef cannot be null");
        }
        this.sourceRef = sourceRef;
    }

    /**
     * Gets the displayName properties
     *
     * @return the displayName properties
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the displayName property
     *
     * @param displayName the displauName property
     *
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the maxGUIDSize properties
     *
     * @return the maxGUIDSize properties
     */
    public long getMaxGUIDSize() {
        return maxGUIDSize;
    }

    public void setMaxGUIDSize(long maxGUIDSize) {
        if ((maxGUIDSize == 0) || (maxGUIDSize < -1)) {
            throw new IllegalArgumentException("illegal maxGUIDSize value");
        }
        this.maxGUIDSize = maxGUIDSize;
    }

    /**
     * Gets the ContentTypeInfo corresponds to &lt;Rx-Pref&gt; element
     *
     * @return the ContentTypeInfo corresponds to &l;tRx-Pref&gt; element
     */
    public ContentTypeInfo getRxPref() {
        return rxPref;
    }

    /**
     * Sets the preferred type and version of a content type received by the device
     *
     * @param rxPref the preferred type and version of a content type
     */
    public void setRxPref(ContentTypeInfo rxPref) {
        if (rxPref == null) {
            throw new IllegalArgumentException("rxPref cannot be null");
        }
        this.rxPref = rxPref;
    }

    /**
     * Gets the ContentTypeInfo corresponds to &lt;Rx&gt; element
     *
     * @return the ContentTypeInfo corresponds to &lt;Rx&gt; element
     */
    public ArrayList<ContentTypeInfo> getRx() {
        return rx;
    }

    /**
     * Sets the supported type and version of a content type received by the device
     *
     * @param rxCTI an array of supported type and version of a content type
     */
    public void setRx(ContentTypeInfo[] rxCTI) {
        if (rxCTI == null) {
            throw new IllegalArgumentException("rx cannot be null");
        }
        this.rx.clear();
        this.rx.addAll(Arrays.asList(rxCTI));
    }


    /**
     * Gets the ContentTypeInfo corresponds to &lt;Tx-Pref&gt; element
     *
     * @return the ContentTypeInfo corresponds to &lt;Tx-Pref&gt; element
     */
    public ContentTypeInfo getTxPref() {
        return txPref;
    }

    /**
     * Sets the preferred type and version of a content type trasmitted by the device
     *
     * @param txPref the preferred type and version of a content type
     */
    public void setTxPref(ContentTypeInfo txPref) {
        if (txPref == null) {
            throw new IllegalArgumentException("txPref cannot be null");
        }
        this.txPref = txPref;
    }

    /**
     * Gets an array of ContentTypeInfo corresponds to &lt;Tx&gt; element
     *
     * @return an array of ContentTypeInfo corresponds to &lt;Tx&gt; element
     */
    public ArrayList<ContentTypeInfo> getTx() {
        return tx;
    }

    /**
     * Sets the supported type and version of a content type trasmitted by the device
     *
     * @param txCTI an array of supported type and version of a content type
     */
    public void setTx(ContentTypeInfo[] txCTI) {
        if (txCTI == null) {
            throw new IllegalArgumentException("tx cannot be null");
        }
        this.tx.clear();
        this.tx.addAll(Arrays.asList(txCTI));
    }

    /**
     * Gets the datastore memory information.
     *
     * @return the datastore memory information.
     */
    public DSMem getDSMem() {
        return dsMem;
    }

    /**
     * Sets the datastore memory information
     *
     * @param dsMem the datastore memory information
     */
    public void setDSMem(DSMem dsMem) {
        this.dsMem = dsMem;
    }

    /**
     * Gets the synchronization capabilities of a datastore.
     *
     * @return the synchronization capabilities of a datastore.
     */
    public SyncCap getSyncCap() {
        return syncCap;
    }

    /**
     * Sets the synchronization capabilities of a datastore.
     *
     * @param syncCap the synchronization capabilities of a datastore
     *
     */
    public void setSyncCap(SyncCap syncCap) {
        if (syncCap == null) {
            throw new IllegalArgumentException("syncCap cannot be null");
        }
        this.syncCap = syncCap;
    }
}
