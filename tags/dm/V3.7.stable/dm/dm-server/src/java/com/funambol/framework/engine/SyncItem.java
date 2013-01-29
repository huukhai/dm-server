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

package com.funambol.framework.engine;

import java.util.Map;

import com.funambol.framework.engine.source.SyncSource;


/**
 * <i>SyncItem</i> is the indivisible entity that can be exchanged in a
 * synchronization process. It is similar to a 
 * <i>com.funambol.framework.core.Item</i>,
 * but this one is more generic, not related to any protocol.<br>
 * A <i>SyncItem</i> is uniquely identified by its <i>SyncItemKey</i>, whilst
 * item data are stored in properties, which can be retrieved calling
 * <i>getProperty()</i>, <i>getProperties()</i> and <i>getPropertyValue()</i>.
 * Properties can be set by calling <i>setProperties()</i>, <i>setProperty()</i>
 * and <i>setPropertyValue()</i>.<br>
 * A <i>SyncItem</i> is also associated with a state, which can be one of the
 * values defined in <i>SyncItemState</i>.<p>
 * A <i>SyncItem</i> can be <i>mapped</i>, that is assiciated to an item belonging
 * to another source, meaning that the item represents the same entity in either
 * sources.
 * <p>
 * The following properties are considered standard:
 * <table>
 * <tr>
 * <td>BINARY_CONTENT</td><td>A row bynary representation of the item content</td>
 * </tr>
 * </table>
 *
 * @version $Id: SyncItem.java,v 1.2 2006/08/07 21:09:19 nichele Exp $
 *
 */
public interface SyncItem {

    // --------------------------------------------------------------- Constants

    public static final String PROPERTY_BINARY_CONTENT = "BINARY_CONTENT";
    public static final String PROPERTY_TIMESTAMP      = "TIMESTAMP"     ;

    // -------------------------------------------------------------------------

    /**
     * @return the SyncItem's unique identifier
     */
    public SyncItemKey getKey();
    public SyncItemKey getMappedKey();

    public char getState();

    public void setState(char state);

    /**
     * Returns the <i>properties</i> property. A cloned copy of the internal map
     * is returned.
     *
     * @return the <i>properties</i> property.
     */
    public Map<String, Object> getProperties();

    /**
     * Sets the <i>properties</i> property. All items in the given map are added
     * to the internal map.
     *
     * @param properties the new values
     */
    public void setProperties(Map<String, Object> properties);

    /** Sets/adds the given property to this <i>SyncItem</i>
     *
     * @param property The property to set/add
     */
    public void setProperty(SyncProperty property);

    /** Returns the property with the given name
     *
     * @param propertyName The property name
     *
     * @return the property with the given name if exists or null if not
     */
    public SyncProperty getProperty(String propertyName);


    /** Sets the value of the property with the given name.
     *
     * @param propertyName The property's name
     * @param propertyValue The new value
     */
    public void setPropertyValue(String propertyName, String propertyValue);

    /** Returns the value of the property with the given name.
     *
     * @param propertyName The property's name
     *
     * @return the property value if this <i>SyncItem</i> has the given
     *         property or null otherwise.
     */
    public Object getPropertyValue(String propertyName);

    /** Getter for property syncSource.
     * @return Value of property syncSource.
     *
     */
    public SyncSource getSyncSource();

    /**
     * Is this item mapped?
     *
     * @return <i>true</i> if the item is mapped to another source's item,
     *         <i>false</i> otherwise
     */
    public boolean isMapped();


}
