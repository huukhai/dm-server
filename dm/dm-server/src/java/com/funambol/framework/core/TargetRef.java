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
 * This class represents the &lt;TargetRef&gt; element as defined by the SyncML
 * representation specifications
 *
 *
 *
 * @version $Id: TargetRef.java,v 1.2 2006/08/07 21:09:19 nichele Exp $
 */
public final class TargetRef
implements java.io.Serializable {

    // ------------------------------------------------------------ Private data

    private String value;
    private Target target;

    // ------------------------------------------------------------ Constructors

    /**
     * For serialization purposes
     */
    protected TargetRef() {}

    /**
     * Creates a new TargetRef object given the referenced value. A null value
     * is considered an empty string
     *
     * @param value the referenced value - NULL ALLOWED
     *
     * @throws IllegalArgumentException if value is null
     */
    public TargetRef(final String value) {
        setValue(value);
    }

    /**
     * Creates a new TargetRef object from an existing target.
     *
     * @param target the target to extract the reference from - NOT NULL
     *
     * @throws IllegalArgumentException if target is null
     *
     */
    public TargetRef(final Target target) {
        setTarget(target);
        value = target.getLocURI();
    }

    // ---------------------------------------------------------- Public methods

    /**
     * Returns the value
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the reference value. If value is null, the empty string is adopted.
     *
     * @param value the reference value - NULL
     */
    public void setValue(String value) {
        this.value = (value == null) ? "" : value;
    }

    /**
     * Gets the Target property
     *
     * @return target the Target property
     */
    public Target getTarget() {
        return this.target;
    }

    /**
     * Sets the Target property
     *
     * @param target the Target property
     */
    public void setTarget(Target target) {
        if (target == null) {
            throw new IllegalArgumentException("target cannot be null");
        }
        this.target = target;
    }
}
