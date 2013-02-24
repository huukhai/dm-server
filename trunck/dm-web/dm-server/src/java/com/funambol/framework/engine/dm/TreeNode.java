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

package com.funambol.framework.engine.dm;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Represents a node in dm tree.
 *
 * @version $Id: TreeNode.java,v 1.5 2006/11/15 13:46:28 nichele Exp $
 */
public class TreeNode implements Serializable {

    // --------------------------------------------------------------- Constants
    private static final long serialVersionUID = 7399068704049081543L;

    public static final String FORMAT_BINARY  = "b64";
    public static final String FORMAT_BOOL    = "bool";
    public static final String FORMAT_CHR     = "chr";
    public static final String FORMAT_FLOAT   = "float";
    public static final String FORMAT_INT     = "int";
    public static final String FORMAT_NODE    = "node";
    public static final String FORMAT_NULL    = "null";
    public static final String FORMAT_XML     = "xml";
    public static final String FORMAT_DATE    = "date";
    public static final String FORMAT_TIME    = "time";
    public static final String FORMAT_DEFAULT_VALUE = FORMAT_CHR;

    public static final String[] VALID_FORMATS = {
        FORMAT_BINARY,
        FORMAT_BOOL,
        FORMAT_CHR,
        FORMAT_FLOAT,
        FORMAT_INT,
        FORMAT_NODE,
        FORMAT_NULL,
        FORMAT_XML,
        FORMAT_DATE,
        FORMAT_TIME
    };
    
    public static final String TYPE_DEFAULT_VALUE = "text/plain";


    // -------------------------------------------------------------- Properties
    private String name   = null;
    private String format = null;
    private String type   = null;
    private Object value  = null;

    // ------------------------------------------------------------- Constructor

    public TreeNode() {
        this.type = TYPE_DEFAULT_VALUE;
    }

    public TreeNode(String name) {
        this();
        this.name = name;
    }

    public TreeNode(String name, Object value) {
        this(name);
        setValue(value);
    }

    public TreeNode(String name, Object value, String format) {
        this(name, value);
        setFormat(format);
        if (FORMAT_NODE.equals(format)){
            this.type = null;
        }
    }

    public TreeNode(String name, Object value, String format, String type) {
        this(name, value, format);
        this.type = type;
    }

    // ----------------------------------------------------------- Public Methods

    /**
     * Returns the name property
     *
     * @return the name property
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name property
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the value property
     *
     * @return the value property
     */
    public Object getValue() {
        return value;
    }

    /**
     * Set the value property
     *
     * @param value the new value
     */
    public void setValue(Object value) {
        this.value = value;

        if (value != null) {
            if (value instanceof byte[]) {
                this.format = FORMAT_BINARY;
            } else if (value instanceof Boolean) {
                this.format = FORMAT_BOOL;
            } else if (value instanceof Integer) {
                this.format = FORMAT_INT;
            } else {
                this.format = FORMAT_DEFAULT_VALUE;
            }
        }
    }

    /**
     * Returns the format property
     *
     * @return the format property
     */
    public String getFormat() {
        return format;
    }

    /**
     * Set the format property. The format must be one of the allowed formats: 
     * b64, bin, bool, chr, int, node, null, xml. 
     * 
     * @param format the format property
     * @throws IllegalArgumentException if the format is not valid
     */
    public void setFormat(String format) {
        for (int i = 0; i < VALID_FORMATS.length; i++) {
            if (VALID_FORMATS[i].equalsIgnoreCase(format)){
                this.format = format.toLowerCase();
                return;
            }
        }
        throw new IllegalArgumentException("Format '" + format + "' is not valid");
    }

    /**
     * Returns the type property
     *
     * @return the type property
     */
    public String getType() {
        return type;
    }

    /**
     * Set the type property
     *
     * @param type the new type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns a String representation of this TreeNode
     * @return String
     */
    public String toString() {
        ToStringBuilder sb = new ToStringBuilder(this);

        sb.append("name"  , name);
        sb.append("value" , value);
        sb.append("format", format);
        sb.append("type"  , type);

        return sb.toString();
    }
}
