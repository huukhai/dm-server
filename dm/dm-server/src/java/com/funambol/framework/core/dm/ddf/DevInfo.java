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

package com.funambol.framework.core.dm.ddf;

/**
 * Corresponds to the &lt;DevInf&gt; element in the SyncML devinf DTD
 *
 * @version $Id: DevInfo.java,v 1.4 2006/11/15 14:28:49 nichele Exp $
 */
public final class DevInfo implements java.io.Serializable {

    // --------------------------------------------------------------- Constants

    public static final String DEVINFO_DEV_ID = "/DevId";
    public static final String DEVINFO_MAN    = "/Man"  ;
    public static final String DEVINFO_MOD    = "/Mod"  ;
    public static final String DEVINFO_DMV    = "/DmV"  ;
    public static final String DEVINFO_LANG   = "/Lang" ;

    // ------------------------------------------------------------ Private data
    private String devId;
    private String man;
    private String mod;
    private String dmV;
    private String lang;

    // ------------------------------------------------------------ Constructors
    /** For serialization purposes */
    protected DevInfo() {}

    /**
     * Creates a new DevInfo object with the given parameters
     *
     * @param devId the device identifier
     * @param man the device manufacturer
     * @param mod the device model name
     * @param dmV a SyncML DM client version identifier
     * @param lang the device software version
     */
    public DevInfo(final String devId,
                   final String man  ,
                   final String mod  ,
                   final String dmV  ,
                   final String lang ) {
        this.devId = devId;
        this.man   = man  ;
        this.mod   = mod  ;
        this.dmV   = dmV  ;
        this.lang  = lang ;
    }

    // ---------------------------------------------------------- Public methods
    /**
     * Gets the device identifier
     *
     * @return  the device identifier
     */
    public String getDevId() {
        return devId;
    }

    /**
     * Sets the device identifier
     *
     * @param  devId the device identifier
     */
    public void setDevId(String devId) {
        this.devId = devId;
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
     * Gets the SyncML DM client version
     *
     * @return the SyncML DM client version
     */
    public String getFwV() {
        return dmV;
    }

    /**
     * Sets the SyncML DM client version
     *
     * @param dmV the SyncML DM client version
     */
    public void setDmV(String dmV) {
        this.dmV =dmV;
    }

    /**
     * Gets the device current language setting
     *
     * @return the device current language setting
     */
    public String getLang() {
        return lang;
    }

    /**
     * Sets the device current language setting
     *
     * @param lang the device current language setting
     */
    public void setLang(String lang) {
        this.lang = lang;
    }
}
