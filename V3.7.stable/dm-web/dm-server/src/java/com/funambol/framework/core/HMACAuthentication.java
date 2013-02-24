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

/**
 * Contains the information for HMAC autenthication
 *
 *
 *
 * @version $Id: HMACAuthentication.java,v 1.3 2006/11/15 14:24:31 nichele Exp $
 */

public class HMACAuthentication extends Authentication {

    // --------------------------------------------------------------- Constants
    private final String DEFAULT_ALGORITHM = "MD5";


    // ------------------------------------------------------------ Private data
    private String userMac;
    private String algorithm;
    private String calculatedMac;


    // ------------------------------------------------------------ Constructors

    /** For serialization purposes */
    protected HMACAuthentication() {}



    public HMACAuthentication(final String data) {
        super(Constants.AUTH_TYPE_HMAC, Constants.FORMAT_B64, data);
    }


    // ---------------------------------------------------------- Public Methods

    /**
     * Gets the userMac property
     *
     * @return the userMac property
     */
    public String getUserMac() {
        return this.userMac;
    }


    /**
     * Sets the userMac property
     * @param userMac the userMac property
     */
    public void setUserMac(String userMac) {
        this.userMac = userMac;
    }

    /**
     * Gets the calculatedMac property
     *
     * @return the calculatedMac property
     */
    public String getCalculatedMac() {
        return this.calculatedMac;
    }


    /**
     * Sets the calculatedMac property
     * @param calculatedMac the calculatedMac property
     */
    public void setCalculatedMac(String calculatedMac) {
        this.calculatedMac = calculatedMac;
    }

    /**
     * Gets the algorithm property
     *
     * @return the algorithm property
     */
    public String getAlgorithm() {
        return this.algorithm;
    }


    /**
     * Sets the data property
     *
     * @param data the data property
     *
     */
    public void setData(String data) {
        if (data == null) {
            throw new IllegalArgumentException("data cannot be null");
        }


        // example
        // hmacHeader: algorithm=MD5, username="Robert Jordan", mac=Dgpoz8Pbs4XC0ecp6kLw4Q==


        // parse hmac header for algorithm

        int indexAlgorithm = data.indexOf("algorithm");
        int indexEndAlgorithm = -1;

        if (indexAlgorithm == -1) {
            algorithm = DEFAULT_ALGORITHM;
        } else {
            indexEndAlgorithm = data.indexOf(",", indexAlgorithm);
            algorithm = data.substring(indexAlgorithm + 10, indexEndAlgorithm);
        }

        // parse hmac header for username

        int indexUsername = data.indexOf("username", indexEndAlgorithm + 1);

        if (indexUsername == -1) {
            throw new IllegalArgumentException("Username missing in hmac header");
        }

        int indexEndUsername = data.indexOf("\"", indexUsername + 10);

        if (indexEndUsername == -1) {
            throw new IllegalArgumentException("Unable to get username from hmac header [" + data +
                                               "]");
        }

        // checks if the previous char is '\'
        while (data.charAt(indexEndUsername - 1) == '\\') {
            indexEndUsername = data.indexOf("\"", indexEndUsername + 1);
        }

        if (indexEndUsername == -1) {
            throw new IllegalArgumentException("Unable to get username from hmac header [" + data +
                                               "]");
        }

        setUsername(data.substring(indexUsername + 10, indexEndUsername));

        // parse hmac header for mac

        int indexMac = data.indexOf("mac", indexEndUsername);

        if (indexMac == -1) {
            throw new IllegalArgumentException("Mac value missing in hmac header");
        } else {
            userMac = data.substring(indexMac + 4);
        }

    }

}
