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
 * This class represents the content type information such as content type and
 * version of content type
 *
 * 
 *
 *  @version $Id: ContentTypeInfo.java,v 1.2 2006/08/07 21:05:28 nichele Exp $
 */
public class ContentTypeInfo
implements java.io.Serializable {

    // ------------------------------------------------------------ Private data
    private String ctType;
    private String verCT;

    // ------------------------------------------------------------ Constructors
    /** For serialization purposes */
    protected ContentTypeInfo() {}

    /**
     * Creates a new ContentTypeCapability object with the given content type
     * and versione
     *
     * @param ctType corresponds to &lt;CTType&gt; element in the SyncML
     *                    specification - NOT NULL
     * @param verCT corresponds to &lt;VerCT&gt; element in the SyncML
     *                specification - NOT NULL
     *
     */
    public ContentTypeInfo(final String ctType, final String verCT) {
        if (ctType == null || ctType.length() == 0){
            throw new IllegalArgumentException("ctType cannot be null");
        }

        if (verCT == null || verCT.length() == 0){
            throw new IllegalArgumentException("verCT cannot be null");
        }
        this.ctType = ctType;
        this.verCT = verCT;
    }

    // ---------------------------------------------------------- Public methods

    /**
     * Gets the content type properties
     *
     * @return the content type properties
     */
    public String getCTType() {
        return ctType;
    }

    /**
     * Sets the content type properties
     *
     * @param ctType the content type properties
     */
    public void setCTType(String ctType) {
        this.ctType = ctType;
    }

    /**
     * Gets the version of the content type
     *
     * @return the version of the content type
     */
    public String getVerCT() {
        return verCT;
    }

    /**
     * Sets the version of the content type
     *
     * @param verCT the version of the content type
     */
    public void setVerCT(String verCT) {
        this.verCT = verCT;
    }
}
