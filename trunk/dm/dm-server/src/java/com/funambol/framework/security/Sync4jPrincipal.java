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

package com.funambol.framework.security;

import java.security.Principal;

import com.funambol.framework.core.Constants;
import com.funambol.framework.tools.Base64;


/**
 * This class implements the <i>Pricipal</i> interface and represents a Sync4j
 * principal. In Sync4j a principal is represented by a couple
 * <i>(username, device id)</i> and is associated to a unique id.
 *
 *.com
 *
 * @version $Id: Sync4jPrincipal.java,v 1.3 2006/11/15 15:16:04 nichele Exp $
 */
public class Sync4jPrincipal implements Principal, java.io.Serializable {
    // --------------------------------------------------------------- Constants
    public final static String PROPERTY_ID       = "id";
    public final static String PROPERTY_USERNAME = "username";
    public final static String PROPERTY_DEVICE   = "device";

    // ------------------------------------------------------------ Private data

    private String id       = null; // principalid
    private String username = null;
    private String deviceId = null;
    private String email    = null;
    private String firstName= null;
    private String lastName = null;


    // ------------------------------------------------------------ Constructors
    /**
     * Disable the default constructor.
     */
    protected Sync4jPrincipal() {}

    /**
     * Creates a new instance of UsernamePrincipal
     *
     * @param id principal id
     * @param username the username element of the principal
     * @param deviceId the deviceId element of the principal
     */
    public Sync4jPrincipal(String id, String username, String deviceId) {
        this.id       = id      ;
        this.username = username;
        this.deviceId = deviceId;
    }

    /**
     * Creates a new instance of UsernamePrincipal
     *
     * @param username the username element of the principal
     * @param deviceId the deviceId element of the principal
     *
     */
    public Sync4jPrincipal(String username, String deviceId) {
        this(null, username, deviceId);
    }


    /**
     * Creates a new instance of UsernamePrincipal
     *
     * @param id principal id
     */
    public Sync4jPrincipal(String id) {
        this(id, null, null);
    }

    /**
     * Creates a new instance of CredentialPrincipal from the given Credential.
     *
     * <p>Note: Don't use this method. Use <code>fromCredential(Cred, String)</code> instead.
     *
     * @param userpwd user and password in the form <user>:<password>
     * @param type the username coding as expressed in the a Credential object
     * @param deviceId the deviceId element of the principal. Can be NULL
     *
     * @see com.funambol.framework.core.Credential
     *
     * @throws IllegalArgumentException
     */
    public static Sync4jPrincipal fromCredential( String userpwd  ,
                                                  String type     ,
                                                  String deviceId ) throws IllegalArgumentException {

        Sync4jPrincipal ret = new Sync4jPrincipal();

        if (Constants.AUTH_SUPPORTED_TYPES.indexOf(type) < 0) {
            throw new IllegalArgumentException( "Authorization type '"
                                              + type
                                              + "' not supported"
                                              );
        }

        if (Constants.AUTH_TYPE_BASIC.equals(type)) {
            String s = new String(Base64.decode(userpwd.getBytes()));

            int p = s.indexOf(':');

            if (p == -1) {
                ret.setUsername(s);
            } else {
                ret.setUsername((p>0) ? s.substring(0, p) : "");
            }
        } else if (Constants.AUTH_TYPE_CLEAR.equals(type)) {
            String s = userpwd;

            int p = s.indexOf(':');

            if (p == -1) {
                ret.setUsername(s);
            } else {
                ret.setUsername((p>0) ? s.substring(0, p) : "");
            }
        } else {
            ret.setUsername(userpwd);
        }

        ret.setId(null);
        ret.setDeviceId(deviceId);

        return ret;
    }



    // ---------------------------------------------------------- Public methods

    /**
     * The name of this principal is in the form:
     *   {username}.{deviceId}
     *
     * @return the principal's name
     */
    public String getName() {

        return (deviceId == null)
             ? username
             : (username + '.' + deviceId);
    }

    /**
     * The username part of the principal name.
     *
     * @return the username part of the principal name.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set the username part of the principal name.
     *
     * @param username the username principal's username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * The device id part of the principal name.
     *
     * @return the device id part of the principal name.
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Set the device id part of the principal name.
     *
     * @param deviceId the device id principal's username
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * The principal unique id.
     *
     * @return the unique principal id.
     */
    public String getId() {
        return id;
    }

    /**
     * Set the principal unique id.
     *
     * @param id the principal unique id.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * The user's email of the principal.
     *
     * @return the user's email of the principal.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Set the user's email of the principal.
     *
     * @param email the user's email of the principal
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * The user's first name of the principal.
     *
     * @return the user's first name of the principal.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Set the user's first name of the principal.
     *
     * @param firstName the user's first name of the principal
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * The user's last name of the principal.
     *
     * @return the user's last name of the principal.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Set the user's last name of the principal.
     *
     * @param lastName the user's last name of the principal
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Two Sync4jPrincipals are equals if and only if:
     * <ul>
     *   <li>o is null or
     *   <li>o is not an instance of Sync4jPrincipal or
     *   <li>the two getName()s do not match
     * </ul>
     *
     * @param o the reference object with which to compare.
     *
     * @return true if this object is the same as the obj argument; false otherwise.
     */
    public boolean equals(Object o) {
        if ((o == null) || !(o instanceof Sync4jPrincipal)) {
            return false;
        }

        return (getName().equals(((Sync4jPrincipal)o).getName()));
    }

    public int hashCode() {
        return getName().hashCode();
    }

    public String toString() {
        return getName();
    }

}
