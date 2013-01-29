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
 * Corresponds to the &lt;DevDetail&gt; element in the SyncML devinf DTD
 *
 * @version $Id: DevDetail.java,v 1.3 2006/11/15 14:28:49 nichele Exp $
 */

public class DevDetail {

    // --------------------------------------------------------------- Constants

   public static final String DEVDETAIL_URI_MAX_DEPTH      = "/URI/MaxDepth";
   public static final String DEVDETAIL_URI_MAX_TOT_LEN    = "/URI/MaxToLen"  ;
   public static final String DEVDETAIL_URI_MAX_SEG_LEN    = "/URI/MaxSegLen"  ;
   public static final String DEVDETAIL_DEV_TYP            = "/DevTyp";
   public static final String DEVDETAIL_OEM                = "/OEM";
   public static final String DEVDETAIL_FWV                = "/FwV";
   public static final String DEVDETAIL_SWV                = "/SwV";
   public static final String DEVDETAIL_HWV                = "/HwV";
   public static final String DEVDETAIL_LRG_OBJ            = "/LrgObj";


   // ------------------------------------------------------------ Private data
   private String uriMaxDepth;
   private String uriMaxLen;
   private String uriMaxSegLen;
   private String devTyp;
   private String oem;
   private String fwVersion;
   private String swVersion;
   private String hwVersion;
   private String lrgObj;


   // ------------------------------------------------------------ Constructors
   /** For serialization purposes */
   protected DevDetail() {}

    /**
     * Creates a new DevDetail object with the given parameters
     *
     * @param uriMaxDepth the maximum depth of the management tree
     * @param uriMaxLen the maximum total length of any URI
     * @param uriMaxSegLen the maximum total length of any uri segment
     * @param devTyp the device type, e.g. PDA, pager, phone
     * @param oem the original equipment manufacturer
     * @param fwVersion the firmware version
     * @param swVersion the software version
     * @param hwVersion the hardware version
     * @param lrgObj indicates whether the device supports large object
     */
    public DevDetail(final String uriMaxDepth,
                     final String uriMaxLen,
                     final String uriMaxSegLen,
                     final String devTyp,
                     final String oem,
                     final String fwVersion,
                     final String swVersion,
                     final String hwVersion,
                     final String lrgObj) {
        this.uriMaxDepth  = uriMaxDepth;
        this.uriMaxLen    = uriMaxLen;
        this.uriMaxSegLen = uriMaxSegLen;
        this.devTyp       = devTyp;
        this.oem          = oem;
        this.fwVersion    = fwVersion;
        this.swVersion    = swVersion;
        this.hwVersion    = hwVersion;
        this.lrgObj       = lrgObj;
    }


    // ---------------------------------------------------------- Public methods


    /**
     * Returns the maximum depth of the management tree
     *
     * @return the uriMaxDepth property
     */
    public String getUriMaxDepth() {
        return uriMaxDepth;
    }

    /**
     * Sets the maximum depth of the management tree
     *
     * @param  uriMaxDepth the maximum depth of the management tree
     */
    public void setUriMaxDepth(String uriMaxDepth) {
        this.uriMaxDepth = uriMaxDepth;
    }

    /**
     * Returns the maximum total length of any URI
     *
     * @return  the uriMaxLen property
     */
    public String getUriMaxLen() {
        return uriMaxLen;
    }

    /**
     * Sets the maximum total length of any URI
     *
     * @param  uriMaxLen the maximum total length of any URI
     */
    public void setUriMaxLen(String uriMaxLen) {
        this.uriMaxLen = uriMaxLen;
    }

    /**
     * Returns the maximum total length of any uri segment
     *
     * @return  the uriMaxSegLen property
     */
    public String getUriMaxSegLen() {
        return uriMaxSegLen;
    }

    /**
     * Sets the maximum total length of any uri segment
     *
     * @param  uriMaxSegLen the maximum total length of any uri segment
     */
    public void setUriMaxSegLen(String uriMaxSegLen) {
        this.uriMaxSegLen = uriMaxSegLen;
    }

    /**
     * Returns the device type
     *
     * @return  the devTyp property
     */
    public String getDevTyp() {
        return devTyp;
    }

    /**
     * Sets the device type
     *
     * @param devTyp the device type
     */
    public void setDevTyp(String devTyp) {
        this.devTyp = devTyp;
    }

    /**
     * Returns the original equipment manufacturer
     *
     * @return  the oem property
     */
    public String getOem() {
        return oem;
    }

    /**
     * Sets the original equipment manufacturer
     *
     * @param  oem the original equipment manufacturer
     */
    public void setOem(String oem) {
        this.oem = oem;
    }

    /**
     * Returns the firmware version
     *
     * @return  the fwVersion property
     */
    public String getFwVersion() {
        return fwVersion;
    }

    /**
     * Sets the firmware version
     *
     * @param  fwVersion the firmware version
     */
    public void setFwVersion(String fwVersion) {
        this.fwVersion = fwVersion;
    }

    /**
     * Returns the software version
     *
     * @return  the swVersion property
     */
    public String getSwVersion() {
        return swVersion;
    }

    /**
     * Sets the software version
     *
     * @param  swVersion the software version
     */
    public void setSwVersion(String swVersion) {
        this.swVersion = swVersion;
    }

    /**
     * Returns the hardware version
     *
     * @return  the hwVersion property
     */
    public String getHwVersion() {
        return hwVersion;
    }

    /**
     * Sets the hardware version
     *
     * @param hwVersion the hardware version
     */
    public void setHwVersion(String hwVersion) {
        this.hwVersion = hwVersion;
    }

    /**
     * Returns the lrgObj property. Indicates whether the device supports large object.
     *
     * @return  the lrgObj property
     */
    public String getLrgObj() {
        return lrgObj;
    }

    /**
     * Sets the lrgObj property. Indicates whether the device supports large object.
     *
     * @param lrgObj the lrgObj
     */
    public void setLrgObj(String lrgObj) {
        this.lrgObj = lrgObj;
    }

}
