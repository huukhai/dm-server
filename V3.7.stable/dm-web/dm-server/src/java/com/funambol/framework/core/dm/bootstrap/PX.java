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

package com.funambol.framework.core.dm.bootstrap;

import java.util.HashMap;
import java.util.Map;


/**
 * Corresponds to the &lt;PX&gt; interior node in the management tree
 *
 * @version $Id: PX.java,v 1.1 2006/11/15 13:43:15 nichele Exp $
 */
public class PX {

    // --------------------------------------------------------------- Constants

    public static final String PX_PORT_NBR  = "/PortNbr";
    public static final String PX_ADDR_TYPE = "/AddrType";
    public static final String PX_ADDR      = "/Addr";
    public static final String PX_AUTH      = "/Auth";

    // ------------------------------------------------------------ Private data
    private String portNbr;
    private String address;
    private String addressType;

    // contains all the pairs id/secret for each authentication methods
    private Map<String, Auth> auths;

    // ------------------------------------------------------------ Constructors
    /** For serialization purposes */
    public PX() {
        this.auths = new HashMap<String, Auth>();
    }

    /**
     * Creates a new ConPX object with the given parameters
     * @param portNbr the port number to use
     * @param address the server address
     * @param addressType the type of the server address
     *
     */
    public PX(final String portNbr,
              final String address,
              final String addressType) {
        this();
        this.portNbr     = portNbr;
        this.address     = address;
        this.addressType = addressType;

    }

    // ---------------------------------------------------------- Public methods

    /**
     * Gets the portNbr
     *
     * @return  the portNbr
     */
    public String getPortNbr() {
        return portNbr;
    }

    /**
     * Sets the portNbr
     *
     * @param  portNbr the portNbr
     */
    public void setPortNbr(String portNbr) {
        this.portNbr = portNbr;
    }

    /**
     * Gets the address
     *
     * @return  the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the address
     *
     * @param  address the address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Gets the addressType
     *
     * @return  the addressType
     */
    public String getAddressType() {
        return addressType;
    }

    /**
     * Sets the addressType
     *
     * @param  addressType the addressType
     */
    public void setAddressType(String addressType) {
        this.addressType = addressType;
    }

    /**
     * Gets the <code>ConAuth</code> with the given name
     *
     * @return  the auth with the given name
     */
    public Auth getAuth(String authenticationName) {
        return auths.get(authenticationName);
    }

    /*
     * Gets the authentications available
     *
     * @return  the authentications available
     */
    public Map<String, Auth> getAuths() {
        return auths;
    }

    /*
     * Sets the auth
     *
     * @param  the auth
     */
    public void setAuths(Map<String, Auth> auths) {
        this.auths = auths;
    }


    /**
     * Adds the auth
     *
     * @param  auth the auth to add
     * @param  authenticationName the authentication name
     */
    public void addAuth(Auth auth, String authenticationName) {
        auths.put(authenticationName, auth);
    }

}
