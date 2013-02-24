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

import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.List;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;

import com.funambol.framework.tools.Base64;


/**
 *
 * This class contains methods for serialize and deserialize
 *
 *
 *
 * @version $Id: Util.java,v 1.3 2006/11/15 14:27:59 nichele Exp $
 *
 */
public final class Util {
    // ------------------------------------------------------------ Constructors

    private Util() {
    }

    // ---------------------------------------------------------- Public methods

    /**
     * Serialize Long value to string.
     *
     * @param value Long value to be serialized
     *
     * @return the representation of value
     */
    public static String serializeWrapLong(Long value) {
        return String.valueOf(value);
    }

    /**
     * Deserialize Long from string
     *
     * @param value string to be parsed
     *
     * @return the representation of value
     */
    public static Long deserializeWrapLong(String value) {
        if (value != null) {
            return Long.valueOf(value.trim());
        }
        return null;
    }

    public static Boolean deserializeBoolean(String value) {
        if (value != null &&
            (value.equals("") || value.equalsIgnoreCase("true"))) {
            return Boolean.TRUE;
        }
        return null;
    }

    public static String serializeBoolean(Boolean value) {
        return value.booleanValue() ? "" : null;
    }

    /**
     * Use marshall to create the representation XML of the object SyncML
     *
     * @param syncML the object SyncML
     *
     * @return the representation XML of the message
     */
    public static String toXML(SyncML syncML) {
        String message = null;
        try {

            ByteArrayOutputStream bout = new ByteArrayOutputStream();

            IBindingFactory f = BindingDirectory.getFactory(SyncML.class);
            IMarshallingContext c = f.createMarshallingContext();
            c.setIndent(0);
            c.marshalDocument(syncML, "UTF-8", null, bout);

            message = new String(bout.toByteArray());

        } catch(Exception e) {
            e.printStackTrace();
        }
        return message;
    }


    /**
     * Use marshall to create the representation XML of the object,
     * therefore must exist the mapping in the binding file.
     *
     * @param obj the object
     *
     * @return the representation XML of the object
     */
    public static String toXML(Object obj) {
        return toXML(obj, false);
    }

    /**
     * Use marshall to create the representation XML of the object,
     * therefore must exist the mapping in the binding file.
     *
     * @param obj the object
     * @param indent must the result be indent ?
     *
     * @return the representation XML of the object
     */
    public static String toXML(Object obj, boolean indent) {
        String message = null;
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();

            IBindingFactory f = BindingDirectory.getFactory(obj.getClass());
            IMarshallingContext c = f.createMarshallingContext();
            c.setIndent((indent ? 1 : 0));
            c.marshalDocument(obj, "UTF-8", null, bout);

            message = new String(bout.toByteArray());
        } catch(Exception e) {
            e.printStackTrace();
        }
        return message;
    }

    /**
     * Use marshall to create the representation XML of the given objects,
     * therefore must exist the mapping in the binding file.
     *
     * @param obj the objects
     *
     * @return the representation XML of the objects
     */
    public static String toXML(Object[] obj) {
        if (obj == null) return null;
        int numObj = obj.length;
        StringBuffer s = new StringBuffer();
        for (int i=0; i<numObj; i++) {
            s.append("Object: " + i + "\n");
            s.append(toXML(obj[i]));
        }
        return s.toString();
    }

    /**
     * Use marshall to create the representation XML of the given objects,
     * therefore must exist the mapping in the binding file.
     *
     * @param objList the list of objects
     *
     * @return the representation XML of the objects
     */
    public static String toXML(List objList) {
        if ( objList  == null) return null;
        StringBuffer s = new StringBuffer();
        Iterator i = objList.iterator();
        int num = 0;
        while (i.hasNext()) {
            s.append("Object: " + (num++) + "\n");
            s.append(toXML(i.next()));
        }
        return s.toString();
    }


    /**
     * Checks if the given item contains the data in b64 format
     * @param item the item to check
     * @return true if the data in the item is in b64, false otherwise
     */
    public static boolean isItemWithBinaryData(Item item) {
        Meta meta = item.getMeta();
        if (meta == null) {
            return false;
        }
        String format = meta.getFormat();

        if (format != null && format.equalsIgnoreCase("b64")) {
            return true;
        }
        return false;
    }

    /**
     * Returns the size of the given Item, null if no size is specified
     * @param item the item to check
     * @return the size of the given Item, null if no size is specified
     */
    public static Long getItemSize(Item item) {
        Meta meta = item.getMeta();
        if (meta == null) {
            return null;
        }
        Long size = meta.getSize();

        return size;
    }

    /**
     * Returns the real dimension of the data in the given Item
     * @param item the item to check
     * @return the real dimension of the data in the given Item,
     *         null if the item not contains data
     */
    public static Long getRealItemSize(Item item, String mimeType) {

        Long size = null;
        Data data = item.getData();
        if (data == null) {
            return null;
        }
        String sData = data.getData();
        if (sData == null) {
            return null;
        }
        boolean isBinary = isItemWithBinaryData(item);
        //
        // If mime type is xml, binary data are managed as string
        //
        if (Constants.MIMETYPE_SYNCMLDM_XML.equals(mimeType) ||
            Constants.MIMETYPE_SYNCMLDS_XML.equals(mimeType)) {
            isBinary = false;
        }

        if (isBinary) {
           byte[] bValue = Base64.decode(sData.getBytes());
           size = Long.valueOf(bValue.length);
        } else {
            size = Long.valueOf(sData.length());
        }

        return size;
    }
}
