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
 * Corresponds to the &l;tDevInf&gt; element in the SyncML devinf DTD
 *
 *
 *
 * @version $Id: DevInf.java,v 1.3 2006/11/15 14:21:53 nichele Exp $
 */
public final class DevInf
implements java.io.Serializable {

    // ------------------------------------------------------------ Private data
    private VerDTD verDTD;
    private String man;
    private String mod;
    private String oem;
    private String fwV;
    private String swV;
    private String hwV;
    private String devID;
    private String devTyp;
    private ArrayList<DataStore> datastores = new ArrayList<DataStore>();
    private ArrayList<CTCap> ctCap = new ArrayList<CTCap>();
    private ArrayList<Ext> ext = new ArrayList<Ext>();
    private Boolean utc;
    private Boolean supportLargeObjs;
    private Boolean supportNumberOfChanges;

    // ------------------------------------------------------------ Constructors
    /** For serialization purposes */
    protected DevInf() {}

    /**
     * Creates a new DevInf object with the given parameter
     *
     * @param verDTD the DTD version - NOT NULL
     * @param man the device manufacturer
     * @param mod the device model name
     * @param oem the device OEM
     * @param fwV the device firmware version
     * @param swV the device software version
     * @param hwV the device hardware version
     * @param devID the device ID - NOT NULL
     * @param devTyp the device type - NOT NULL
     * @param dataStores the array of datastore - NOT NULL
     * @param ctCap the array of content type capability - NOT NULL
     * @param ext the array of extension element name - NOT NULL
     * @param utc is true if the device supports UTC based time
     * @param supportLargeObjs is true if the device supports handling of large objects
     * @param supportNumberOfChanges is true if the device supports number of changes
     *
     */
    public DevInf(final VerDTD verDTD,
                  final String man,
                  final String mod,
                  final String oem,
                  final String fwV,
                  final String swV,
                  final String hwV,
                  final String devID,
                  final String devTyp,
                  final DataStore[] dataStores,
                  final CTCap[] ctCap,
                  final Ext[] ext,
                  final boolean utc,
                  final boolean supportLargeObjs,
                  final boolean supportNumberOfChanges) {

        setVerDTD(verDTD);
        setDevID(devID);
        setDevTyp(devTyp);
        setDataStore(dataStores);
        setCTCap(ctCap);
        setExt(ext);

        this.man = man;
        this.mod = mod;
        this.oem = oem;
        this.fwV = fwV;
        this.swV = swV;
        this.hwV = hwV;

        this.utc  = (utc) ? Boolean.valueOf(utc) : null;
        this.supportLargeObjs  =
                      (supportLargeObjs) ? Boolean.valueOf(supportLargeObjs) : null;
        this.supportNumberOfChanges  =
          (supportNumberOfChanges) ? Boolean.valueOf(supportNumberOfChanges) : null;
    }

    // ---------------------------------------------------------- Public methods
    /**
     * Gets the DTD version property
     *
     * @return the DTD version property
     */
    public VerDTD getVerDTD() {
        return verDTD;
    }

    /**
     * Sets the DTD version property
     *
     * @param verDTD the DTD version
     */
    public void setVerDTD(VerDTD verDTD) {
        if (verDTD == null) {
            throw new IllegalArgumentException("verDTD cannot be null");
        }
        this.verDTD = verDTD;
    }

    /**
     * Gets the device manufacturer property
     *
     * @return the device manufacturer property
     */
    public String getMan() {
        return man;
    }

    /**
     * Sets the device manufacturer property
     *
     * @param man the device manufacturer property
     *
     */
    public void setMan(String man) {
        this.man = man;
    }

    /**
     * Gets the model name of device
     *
     * @return the model name of device
     */
    public String getMod() {
        return mod;
    }

    /**
     * Sets the device model property
     *
     * @param mod the device model property
     *
     */
    public void setMod(String mod) {
        this.mod = mod;
    }

    /**
     * Gets the Original Equipment Manufacturer of the device
     *
     * @return the OEM property
     */
    public String getOEM() {
        return oem;
    }

    /**
     * Sets the Original Equipment Manufacturer of the device
     *
     * @param oem the Original Equipment Manufacturer of the device
     *
     */
    public void setOEM(String oem) {
        this.oem = oem;
    }

    /**
     * Gets the firmware version property
     *
     * @return the firmware version property
     */
    public String getFwV() {
        return fwV;
    }

    /**
     * Sets the firmware version property
     *
     * @param fwV the firmware version property
     *
     */
    public void setFwV(String fwV) {
        this.fwV =fwV;
    }

    /**
     * Gets the software version property
     *
     * @return the software version property
     */
    public String getSwV() {
        return swV;
    }

    /**
     * Sets the software version property
     *
     * @param swV the software version property
     *
     */
    public void setSwV(String swV) {
        this.swV =swV;
    }

    /**
     * Gets the hardware version property
     *
     * @return the hardware version property
     */
    public String getHwV() {
        return hwV;
    }

    /**
     * Sets the hardware version property
     *
     * @param hwV the hardware version property
     *
     */
    public void setHwV(String hwV) {
        this.hwV =hwV;
    }

    /**
     * Gets the device identifier
     *
     * @return the device identifier
     */
    public String getDevID() {
        return devID;
    }

    /**
     * Sets the device identifier
     *
     * @param devID the device identifier
     *
     */
    public void setDevID(String devID) {
        if (devID == null) {
            throw new IllegalArgumentException("devID cannot be null");
        }
        this.devID = devID;
    }

    /**
     * Gets the device type
     *
     * @return the device type
     */
    public String getDevTyp() {
        return devTyp;
    }

    /**
     * Sets the device type
     *
     * @param devTyp the device type
     *
     */
    public void setDevTyp(String devTyp) {
        if (devTyp == null) {
            throw new IllegalArgumentException("devTyp cannot be null");
        }
        this.devTyp = devTyp;
    }

    /**
     * Gets the array of datastore
     *
     * @return the array of datastore
     */
    public ArrayList<DataStore> getDataStore() {
        return this.datastores;
    }

    /**
     * Sets an array of datastore
     *
     * @param dataStores an array of datastore
     *
     */
    public void setDataStore(DataStore[] dataStores) {
        if (dataStores == null ) {
            throw new IllegalArgumentException("datastores cannot be null");
        }
        this.datastores.clear();
        this.datastores.addAll(Arrays.asList(dataStores));
    }

    /**
     * Gets the array of content type capability
     *
     * @return the array of content type capability
     */
    public ArrayList<CTCap> getCTCap() {
        return this.ctCap;
    }

    /**
     * Sets an array of content type capability
     *
     * @param ctCap an array of content type capability
     *
     */
    public void setCTCap(CTCap[] ctCap) {
        if (ctCap != null) {
            this.ctCap.clear();
            this.ctCap.addAll(Arrays.asList(ctCap));
        } else {
            this.ctCap = null;
        }
    }

    /**
     * Gets the array of extension
     *
     * @return the array of extension
     */
    public ArrayList<Ext> getExt() {
        return this.ext;
    }

    /**
     * Sets an array of extensions
     *
     * @param ext an array of extensions
     *
     */
    public void setExt(Ext[] ext) {
        if (ext != null) {
            this.ext.clear();
            this.ext.addAll(Arrays.asList(ext));
        } else {
            this.ext = null;
        }
    }

    /**
     * Gets true if the device supports UTC based time
     *
     * @return true if the device supports UTC based time
     */
    public boolean isUTC() {
        return (utc != null);
    }

    /**
     * Sets the UTC property
     *
     * @param utc is true if the device supports UTC based time
     */
    public void setUTC(Boolean utc) {
        this.utc = (utc.booleanValue()) ? utc : null;
    }

    /**
     * Gets the Boolean value of utc
     *
     * @return true if the device supports UTC based time
     */
    public Boolean getUTC() {
        if (!utc.booleanValue()) {
            return null;
        }
        return utc;
    }

    /**
     * Gets true if the device supports handling of large objects
     *
     * @return true if the device supports handling of large objects
     */
    public boolean isSupportLargeObjs() {
        return (supportLargeObjs != null);
    }

    /**
     * Sets the supportLargeObjs property
     *
     * @param supportLargeObjs is true if the device supports handling of large objects
     *
     */
    public void setSupportLargeObjs(Boolean supportLargeObjs) {
        this.supportLargeObjs = (supportLargeObjs.booleanValue()) ?
                                supportLargeObjs :
                                null
                                ;
    }

    /**
     * Gets the Boolean value of supportLargeObjs
     *
     * @return true if the device supports handling of large objects
     */
    public Boolean getSupportLargeObjs() {
        if (!supportLargeObjs.booleanValue()) {
            return null;
        }
        return supportLargeObjs;
    }

    /**
     * Gets true if the device supports number of changes
     *
     * @return true if the device supports number of changes
     */
    public boolean isSupportNumberOfChanges() {
        return (supportNumberOfChanges != null);
    }

    /**
     * Sets the supportNumberOfChanges property
     *
     * @param supportNumberOfChanges is true if the device supports number of changes
     *
     */
    public void setSupportNumberOfChanges(Boolean supportNumberOfChanges) {
        this.supportNumberOfChanges = (supportNumberOfChanges.booleanValue()) ?
                                      supportNumberOfChanges :
                                      null
                                      ;
    }

    /**
     * Gets the Boolean value of supportNumberOfChanges
     *
     * @return true if the device supports number of changes
     */
    public Boolean getSupportNumberOfChanges() {
        if (!supportNumberOfChanges.booleanValue()) {
            return null;
        }
        return supportNumberOfChanges;
    }
}
