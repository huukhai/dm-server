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
import java.util.HashMap;
import java.util.Iterator;

import com.funambol.framework.engine.source.SyncSource;

import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * <i>SyncItem</i> is the indivisible entity that can be exchanged in a
 * synchronization process. It is similar to an Item, but it doesn't contain
 * any data, only status and addressing information. The idea is that a
 * <i>SyncItem</i> represents status information about an item. Only if an item
 * must be synchronized it needs also the real data.
 * <p>
 * The <i>SyncItemKey</i> uniquely identifies the item into the server. Client
 * keys must be translated into server keys before create a <i>SyncItem</i>.
 *
 *
 *
 * @version $Id: SyncItemImpl.java,v 1.2 2006/08/07 21:09:19 nichele Exp $
 *
 */
public class SyncItemImpl implements java.io.Serializable, SyncItem {

    // -------------------------------------------------------------- Properties

    /**
     * The SyncItem's unique identifier - read only
     */
    protected SyncItemKey key = null;
    public SyncItemKey getKey() {
        return this.key;
    }

    /**
     * The SyncItem's mapped identifier - read/write
     */
    protected SyncItemKey mappedKey = null;
    public SyncItemKey getMappedKey() {
        return mappedKey;
    }

    /**
     *  A <i>SyncItem</i> is mapped if mappedKey is not null.
     *
     * @return <i>true</i> if the item is mapped to another source's item,
     *         <i>false</i> otherwise
     *
     */
    public boolean isMapped() {
        return (mappedKey != null);
    }

    /**
     * The state of this <i>SyncItem</i>
     *
     * @see com.funambol.framework.engine.SyncItemState
     */
    protected char state = SyncItemState.UNKNOWN;

    public char getState(){
        return state;
    }

    public void setState(char state){
        this.state = state;
    }

    /**
     * The <i>SyncItem</i>'s properties - read and write
     */
    protected HashMap<String, Object> properties = new HashMap<String, Object>();

    /**
     * Returns the <i>properties</i> property. A cloned copy of the internal map
     * is returned.
     *
     * @return the <i>properties</i> property.
     */
    public Map<String, Object> getProperties() {
        return (Map<String, Object>)this.properties.clone();
    }

    /**
     * Sets the <i>properties</i> property. All items in the given map are added
     * to the internal map. <i>propertis</i> can contain String values or
     * <i>SyncPorperty</i> values. In the former case, <i>new SyncProperty<i/>s are
     * created.
     *
     * @param properties the new values
     */
    public void setProperties(Map<String, Object> properties){
        this.properties.clear();

        Object value = null;
        String name  = null;

        Iterator<String> i = properties.keySet().iterator();
        while(i.hasNext()) {
            name  = i.next();
            value = properties.get(name);

            if (!(value instanceof SyncProperty)) {
                value = new SyncProperty(name, value.toString());
            }
            this.properties.put(name, value);
        }
    }

    /** Sets/adds the given property to this <i>SyncItem</i>
     *
     * @param property The property to set/add
     */
    public void setProperty(SyncProperty property) {
        properties.put(property.getName(), property);
    }

    /** Returns the property with the given name
     *
     * @param propertyName The property name
     *
     * @return the property with the given name if exists or null if not
     */
    public SyncProperty getProperty(String propertyName) {
        return (SyncProperty)properties.get(propertyName);
    }

        /**
     * The SyncSource this item belongs to
     */
    protected SyncSource syncSource = null;

    /** Getter for property syncSource.
     * @return Value of property syncSource.
     *
     */
    public SyncSource getSyncSource() {
        return syncSource;
    }

    /** Setter for property syncSource.
     * @param syncSource New value of property syncSource. NOT NULL
     *
     */
    public void setSyncSource(SyncSource syncSource) {
        if (syncSource == null) {
            throw new NullPointerException("syncSource cannot be null");
        }

        this.syncSource = syncSource;
    }

    // ------------------------------------------------------------ Constructors

    protected SyncItemImpl() {}

    public SyncItemImpl(SyncSource syncSource, Object key) {
        this(syncSource, key, SyncItemState.UNKNOWN);
    }

    /**
     * Creates a new <i>SyncItem</i> belonging to the given source. After
     * creating a new item, usually <i>setProperties()</i> should be invoked.
     *
     * @param syncSource the source this item belongs to
     * @param key the item identifier
     * @param state one of the state value defined in <i>SyncItemState</i>
     */
    public SyncItemImpl(SyncSource syncSource, Object key, char state) {
        this(syncSource, key, null, state);
    }

    /**
     * Creates a new <i>SyncItem</i> belonging to the given source but mapped to
     * another source. After creating a new item, usually <i>setProperties()</i>
     * should be invoked.
     *
     * @param syncSource the source this item belongs to
     * @param key the item identifier
     * @param mappedKey the mapped key
     * @param state one of the state value defined in <i>SyncItemState</i>
     */
    public SyncItemImpl(SyncSource syncSource, Object key, Object mappedKey, char state) {
        this.syncSource = syncSource          ;
        this.key        = new SyncItemKey(key);
        this.state      = state               ;

        this.mappedKey = (mappedKey != null)
                       ? new SyncItemKey(mappedKey)
                       : null;
    }

    // ---------------------------------------------------------- Public methods


    /** Sets the value of the property with the given name.
     *
     * @param propertyName The property's name
     * @param propertyValue The new value
     */
    public void setPropertyValue(String propertyName, String propertyValue) {
        SyncProperty property = (SyncProperty)properties.get(propertyName);

        if (property != null) {
            property.setValue(propertyValue);
        }
    }

    /** Returns the value of the property with the given name.
     *
     * @param propertyName The property's name
     *
     * @return the property value if this <i>SyncItem</i> has the given
     *         property or null otherwise.
     */
    public Object getPropertyValue(String propertyName) {
        SyncProperty property = (SyncProperty)properties.get(propertyName);

        return (property == null) ? null
                                  : property.getValue()
                                  ;
    }

    /**
     * Two <i>SyncItem</i>s are equal if their keys are equal.
     *
     * @param o the object this instance must be compared to.
     *
     * @return the given object is equal to this object
     *
     */
    public boolean equals(Object o) {
        if (!(o instanceof SyncItem)) return false;

        return ((SyncItem)o).getKey().equals(key);
    }

    /**
     * Creates and returns a "not-existing" <i>SyncItem</i>. It is used internally to
     * represent an item which has not a physical correspondance in a source.
     *
     * @param syncSource the <i>SyncSource</i> the not existing item belongs to
     * @return the a "not-exisiting" <i>SyncItem</i>
     */
    public static SyncItem getNotExistingSyncItem(SyncSource syncSource) {
        SyncItem notExisting = new SyncItemImpl(syncSource, "NOT_EXISTING");

        notExisting.setState(SyncItemState.NOT_EXISTING);

        return notExisting;
    }

    /**
     * @return a string representation of this SyncItem for debugging purposes
     */
    public String toString() {
        return new ToStringBuilder(this).
                   append("key"       , key.toString()        ).
                   append("state"     , state                 ).
                   append("properties",  properties.toString()).
                   toString();
    }
}
