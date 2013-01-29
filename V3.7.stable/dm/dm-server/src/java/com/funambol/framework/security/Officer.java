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

import com.funambol.framework.core.Cred;

/**
 * This the interface that defines how credentials are authenticated to a device
 * and how resources are authorized for a given credential.
 *
 *.com
 */
public interface Officer {

    /**
     * Authenticates a credential.
     *
     * @param credential the credential to be authenticated
     *
     * @return true if the credential is autenticated, false otherwise
     */
    public boolean authenticate(Cred credential);

    /**
     * Un-authenticates a credential.
     *
     * @param credential the credential to be unauthenticated
     */
    public void unAuthenticate(Cred credential);

    /**
     * Authorizes a resource.
     *
     * @param principal the requesting entity
     * @param resource the name (or the identifier) of the resource to be authorized
     *
     * @return true if the credential is authorized to access the resource, false
     *         otherwise
     */
    public boolean authorize(Principal credential, String resource);

    /**
     * Read property guestEnabled
     */
    public boolean isGuestEnabled();

    /**
     * Read property authType
     *
     * @return the type of authentication set up from the server
     */
    public String getAuthType();

    /**
     * Set the type of server authentication
     *
     * @param authType the type of server authentication
     *
     */
    public void setAuthType(String authType);


    /**
     * Read property supportedAuthType
     *
     * @return the supported type of server authentication
     */
    public String getSupportedAuthType();


    /**
     * Set the supported type of server authentication
     *
     * @param supportedAuthType the supported type of server authentication
     *
     */
    public void setSupportedAuthType(String supportedAuthType);


    /**
     * Is the account authenticated by this officer expired?
     *
     * @return true if the account is expired, false otherwise
     */
    public boolean isAccountExpired();

}
