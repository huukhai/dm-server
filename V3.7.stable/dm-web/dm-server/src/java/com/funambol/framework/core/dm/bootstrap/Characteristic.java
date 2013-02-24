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

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 * Corresponds to the &lt;characteristic&gt; tag in a Wap provisioning doc.
 *
 * @version $Id: Characteristic.java,v 1.1 2006/11/15 13:43:15 nichele Exp $
 */
public final class Characteristic implements Serializable {

    private static final long serialVersionUID = 4113564488529304650L;

    // --------------------------------------------------------------- Constants
    public static final String TYPE_PXLOGICAL          = "PXLOGICAL";
    public static final String TYPE_PXPHYSICAL         = "PXPHYSICAL";
    public static final String TYPE_PXAUTHINFO         = "PXAUTHINFO";
    public static final String TYPE_PORT               = "PORT";
    public static final String TYPE_NAPDEF             = "NAPDEF";
    public static final String TYPE_NAPAUTHINFO        = "NAPAUTHINFO";
    public static final String TYPE_VALIDITY           = "VALIDITY";
    public static final String TYPE_BOOTSTRAP          = "BOOTSTRAP";
    public static final String TYPE_CLIENTIDENTITY     = "CLIENTIDENTITY";
    public static final String TYPE_VENDORCONFIG       = "VENDORCONFIG";
    public static final String TYPE_APPLICATION        = "APPLICATION";
    public static final String TYPE_APPADDR            = "APPADDR";
    public static final String TYPE_APPAUTH            = "APPAUTH";
    public static final String TYPE_RESOURCE           = "RESOURCE";
    public static final String TYPE_ACCESS             = "ACCESS";


    // -------------------------------------------------------------- Properties

    private ArrayList<Serializable> children;

    private String type;

    // ------------------------------------------------------------ Constructors

    public Characteristic() {
        children = new ArrayList<Serializable>();
    }

    public Characteristic(String type) {
        this();
        this.type = type;
    }


    /**
     * Adds new <code>Characteristic</code> to this characteristic
     * @param characteristicToAdd the characteristic to add
     */
    public void add(Characteristic characteristicToAdd) {
        children.add(characteristicToAdd);
    }

    /**
     * Adds new <code>Parm</code> to this characteristic
     * @param parmToAdd the parm to add
     */
    public void add(Parm parmToAdd) {
        children.add(parmToAdd);
    }

    /**
     * Gets the type
     * @return the type of this characteristic
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of this characteristic
     * @param type the type to sets
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the children list
     * @return the children
     */
    public ArrayList<Serializable> getChildren() {
        return children;
    }


    /**
     * Removes for the children list the parm with the given name
     * @param name String
     */
    public void removeParm(String name) {
        Iterator<Serializable> itChildren = null;
        Object   child      = null;
        Parm     parm       = null;
        if (children != null) {
            itChildren = children.iterator();
            while (itChildren.hasNext()) {
                child = itChildren.next();
                if (child instanceof Parm) {
                    parm = (Parm)child;
                    if (name.equalsIgnoreCase(parm.getName())) {
                        children.remove(parm);
                        return;
                    }
                }
            }
        }
        return ;
    }

    /**
     * Removes for the children list the characteristics with the given type
     * (Maybe there are more characteristics with the same name)
     * @param type String
     */
    public void removeCharacteristic(String type) {
        Iterator<Serializable>       itChildren   = null;
        Object         child        = null;
        Characteristic c            = null;
        List<Characteristic>           charToRemove = new ArrayList<Characteristic>();
        if (children != null) {
            itChildren = children.iterator();
            while (itChildren.hasNext()) {
                child = itChildren.next();
                if (child instanceof Characteristic) {
                    c = (Characteristic)child;
                    if (type.equalsIgnoreCase(c.getType())) {
                        charToRemove.add(c);
                    }
                }
            }
        }
        children.removeAll(charToRemove);
        return ;
    }

    /**
     * Returns the parm with the given name searching it in the children list.
     * @param name the name to search
     * @return the parm with the given name searching it in the children list.
     *         <code>null</code> if a parm with the specified name doesn't exist.
     */
    public Parm getParm(String name) {
        Iterator<Serializable> itChildren = null;
        Object   child      = null;
        Parm     parm       = null;
        if (children != null) {
            itChildren = children.iterator();
            while (itChildren.hasNext()) {
                child = itChildren.next();
                if (child instanceof Parm) {
                    parm = (Parm)child;
                    if (name.equalsIgnoreCase(parm.getName())) {
                        return parm;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Sets a parm with the given name and value.
     * If a parm with the given name is found in the children list, his value is
     * overwritten, otherwise a new Parm is created and add at the given position
     * @param name String
     * @param value String
     * @param position the position of the parm.
     */
    public int setParm(String name, String value, int position) {
        Iterator<Serializable> itChildren = null;
        Object   child      = null;
        Parm     parm       = null;
        int      pos        = 0;
        if (children != null) {
            itChildren = children.iterator();
            while (itChildren.hasNext()) {
                child = itChildren.next();
                if (child instanceof Parm) {
                    parm = (Parm)child;
                    if (name.equalsIgnoreCase(parm.getName())) {
                        parm.setValue(value);
                        return pos;
                    }
                }
                pos++;
            }
        }
        parm = new Parm(name, value);
        children.add(position, parm);
        return position;
    }

    /**
     * Returns the characteristic with the given type searching it in the children list.
     * @param type the type to search
     * @return the characteristic with the given type searching it in the children list.
     *         <code>null</code> if a characteristic with the specified type doesn't exist.
     */
    public Characteristic getCharacteristic(String type) {
        Iterator<Serializable> itChildren = null;
        Object   child      = null;
        Characteristic characteristic = null;

        if (children != null) {
            itChildren = children.iterator();
            while (itChildren.hasNext()) {
                child = itChildren.next();
                if (child instanceof Characteristic) {
                    characteristic = (Characteristic)child;
                    if (type.equalsIgnoreCase(characteristic.getType())) {
                        return characteristic;
                    }
                }
            }
        }
        return null;
    }
}
