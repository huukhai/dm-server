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

import java.io.Serializable;


/**
 * Corresponds to the &lt;parm&gt; tag in a Wap provisioning doc.
 *
 * @version $Id: Parm.java,v 1.1 2006/11/15 13:43:15 nichele Exp $
 */
public final class Parm implements Serializable {

    private static final long serialVersionUID = 6158602889561087131L;

    // -------------------------------------------------------------- Properties

    private String name;
    private String value;

    // ------------------------------------------------------------ Constructors
    public Parm() {
    }

    public Parm(String name, String value) {
        this.name  = name;
        setValue(value);
    }


    /**
     * Gets the name
     * @return the name of this parm
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this parm
     * @param name the name to sets
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the value
     * @return the value of this parm.
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of this parm
     * @param value the value to sets
     */
    public void setValue(String value) {
        this.value = ( (value == null) ? "" : value );
    }

    /**
     * Two Parm are equals if and only if:
     * <ul>
     *   <li>o is an instance of Parm and the name
     *  is the same</li>
     * </ul>
     *
     * @param o the reference object with which to compare.
     *
     * @return true if this object is the same as the obj argument; false otherwise.
     */
    public boolean equals(Object o) {
        if ((o == null) || !(o instanceof Parm)) {
            return false;
        }

        return (getName().equals(((Parm)o).getName()));
    }

    /**
     * Return the hashcode
     * @return int
     */
    public int hashCode() {
        return getName().hashCode();
    }
}
