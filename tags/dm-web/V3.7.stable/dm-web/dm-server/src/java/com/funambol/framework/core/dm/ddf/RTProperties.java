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
 
package com.funambol.framework.core.dm.ddf;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Represents Run Time Properties that exists at run-time in a device.
 * 
 * Each node may have a different set of RTProperties. A node that only supports 
 * the mandatory properties and does not need any default values for any property 
 * and may omit the RTProperties
 * 
 * @version $Id: RTProperties.java,v 1.1 2006/11/15 14:29:17 nichele Exp $
 */
public class RTProperties implements Serializable {

    // --------------------------------------------------------------- Constants
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
    private String acl;
    private String format; 
    private String name; 
    private String size;
    private String title;
    private String tStamp; 
    private String type; 
    private String verNo;
    
    // ------------------------------------------------------------- Constructor
    public RTProperties() {
        this.type = TYPE_DEFAULT_VALUE;
        this.format = FORMAT_DEFAULT_VALUE;
    }

    /**
     * Creates a new RTProperties
     * @param acl        the Access Control List
     * @param format     the format
     * @param tStamp     the time stamp of the last modification
     * @param type       the MIME type 
     * @param verNo      the version number
     */
    public RTProperties(String acl,
                        String format,
                        String tStamp,
                        String type,
                        String verNo) {
        this();
        this.acl    = acl   ;
        this.tStamp = tStamp;
        this.type   = type  ;
        this.verNo  = verNo ;
        setFormat(format);
    }
    
    /**
     * Creates a new RTProperties
     * @param acl        the Access Control List
     * @param format     the format
     * @param name       the name
     * @param size       the size in bytes
     * @param title      the title 
     * @param tStamp     the time stamp of the last modification
     * @param type       the MIME type 
     * @param verNo      the version number
     */
    public RTProperties(String acl,
                        String format,
                        String name,
                        String size,
                        String title,
                        String tStamp,
                        String type,
                        String verNo) {
        
        this(acl, format, tStamp, type, verNo);
        this.name   = name  ;
        this.size   = size  ;
        this.title  = title ;
    }
    
    // ---------------------------------------------------------- Public Methods
    /**
     * Returns the Access Control List
     *
     * @return the acl property
     */
    public String getAcl() {
        return acl;
    }

    /**
     * Set the Access Control List: this is a REQUIRED property that 
     * defines who can manipulate the underlying object.
     * 
     * @param acl the acl property
     */    
    public void setAcl(String acl) {
        this.acl = acl;
    }
    
    /**
     * Set the format property. The format must be one of the allowed formats: 
     * b64, bin, bool, chr, int, node, null, xml, date, time, float. 
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
     * Returns the format property
     *
     * @return the format property
     */
    public String getFormat() {
        return this.format;
    }
    
    /**
     * Tests if the format is b64
     * 
     * @return the Boolean object corresponding to the primitive value true 
     * if the format is b64; null otherwise.
     */
    public Boolean getFormatB64() {
        if (FORMAT_BINARY.equals(format)) {
            return Boolean.TRUE;
    }
        return null;
    }
    
    /**
     * Sets the format as b64 if the the Boolean parameter corresponds to 
     * the primitive value true
     * 
     * @param formatB64 the format is b64
     */
    public void setFormatB64(Boolean formatB64) {
        if (formatB64 != null && formatB64.booleanValue()) {
            this.format = FORMAT_BINARY;
        }
    }

    /**
     * Tests if the format is b64
     * 
     * @return the Boolean object corresponding to the primitive value true 
     * if the format is b64; null otherwise.
     */
    public Boolean getFormatBIN() {
        if (FORMAT_BINARY.equals(format)) {
            return Boolean.TRUE;
        }
        return null;
    }

    /**
     * Sets the format as b64 if the the Boolean parameter corresponds to 
     * the primitive value true
     * 
     * @param formatBIN the format is b64
     */
    public void setFormatBIN(Boolean formatBIN) {
        if (formatBIN != null && formatBIN.booleanValue()) {
            this.format = FORMAT_BINARY;
        }
    }

    /**
     * Tests if the format is bool
     * 
     * @return the Boolean object corresponding to the primitive value true 
     * if the format is bool; null otherwise.
     */
    public Boolean getFormatBOOL() {
        if (FORMAT_BOOL.equals(format)) {
            return Boolean.TRUE;
        }
        return null;
    }

    /**
     * Sets the format as bool if the the Boolean parameter corresponds to 
     * the primitive value true
     * 
     * @param formatBOOL the format is bool
     */
    public void setFormatBOOL(Boolean formatBOOL) {
        if (formatBOOL != null && formatBOOL.booleanValue()) {
            this.format = FORMAT_BOOL;
        }
    }
    
    /**
     * Tests if the format is chr
     * 
     * @return the Boolean object corresponding to the primitive value true 
     * if the format is chr; null otherwise.
     */
    public Boolean getFormatCHR() {
        if (FORMAT_CHR.equals(format)) {
            return Boolean.TRUE;
        }
        return null;
    }

    /**
     * Sets the format as chr if the the Boolean parameter corresponds to 
     * the primitive value true
     * 
     * @param formatCHR the format is chr
     */
    public void setFormatCHR(Boolean formatCHR) {
        if (formatCHR != null && formatCHR.booleanValue()) {
            this.format = FORMAT_CHR;
        }
    }
    
    /**
     * Tests if the format is int
     * 
     * @return the Boolean object corresponding to the primitive value true 
     * if the format is inr; null otherwise.
     */
    public Boolean getFormatINT() {
        if (FORMAT_INT.equals(format)) {
            return Boolean.TRUE;
        }
        return null;
    }

    /**
     * Sets the format as int if the the Boolean parameter corresponds to 
     * the primitive value true
     * 
     * @param formatINT the format is int
     */
    public void setFormatINT(Boolean formatINT) {
        if (formatINT != null && formatINT.booleanValue()) {
            this.format = FORMAT_INT;
        }
    }

    /**
     * Tests if the format is node
     * 
     * @return the Boolean object corresponding to the primitive value true 
     * if the format is node; null otherwise.
     */
    public Boolean getFormatNODE() {
        if (FORMAT_NODE.equals(format)) {
            return Boolean.TRUE;
        }
        return null;
    }

    /**
     * Sets the format as node if the the Boolean parameter corresponds to 
     * the primitive value true
     * 
     * @param formatNODE the format is node
     */
    public void setFormatNODE(Boolean formatNODE) {
        if (formatNODE != null && formatNODE.booleanValue()) {
            this.format = FORMAT_NODE;
        }
    }
    
    /**
     * Tests if the format is null
     * 
     * @return the Boolean object corresponding to the primitive value true 
     * if the format is null; null otherwise.
     */
    public Boolean getFormatNULL() {
        if (FORMAT_NULL.equals(format)) {
            return Boolean.TRUE;
        }
        return null;
    }
    
    /**
     * Sets the format as null if the the Boolean parameter corresponds to 
     * the primitive value true
     * 
     * @param formatNULL the format is null
     */    
    public void setFormatNULL(Boolean formatNULL) {
        if (formatNULL != null && formatNULL.booleanValue()) {
            this.format = FORMAT_NULL;
        }
    }

    /**
     * Tests if the format is xml
     * 
     * @return the Boolean object corresponding to the primitive value true 
     * if the format is xml; null otherwise.
     */
    public Boolean getFormatXML() {
        if (FORMAT_XML.equals(format)) {
            return Boolean.TRUE;
        }
        return null;
    }

    /**
     * Sets the format as xml if the the Boolean parameter corresponds to 
     * the primitive value true
     * 
     * @param formatXML the format is xml
     */    
    public void setFormatXML(Boolean formatXML) {
        if (formatXML != null && formatXML.booleanValue()) {
            this.format = FORMAT_XML;
        }
    }

    /**
     * Tests if the format is date
     * 
     * @return the Boolean object corresponding to the primitive value true 
     * if the format is date; null otherwise.
     */
    public Boolean getFormatDATE() {
        if (FORMAT_DATE.equals(format)) {
            return Boolean.TRUE;
        }
        return null;
    }

    /**
     * Sets the format as date if the the Boolean parameter corresponds to 
     * the primitive value true
     * 
     * @param formatDATE the format is date
     */ 
    public void setFormatDATE(Boolean formatDATE) {
        if (formatDATE != null && formatDATE.booleanValue()) {
            this.format = FORMAT_DATE;
        }
    }

    /**
     * Tests if the format is time
     * 
     * @return the Boolean object corresponding to the primitive value true 
     * if the format is time; null otherwise.
     */
    public Boolean getFormatTIME() {
        if (FORMAT_TIME.equals(format)) {
            return Boolean.TRUE;
        }
        return null;
    }

    /**
     * Sets the format as time if the the Boolean parameter corresponds to 
     * the primitive value true
     * 
     * @param formatTIME the format is time
     */ 
    public void setFormatTIME(Boolean formatTIME) {
        if (formatTIME != null && formatTIME.booleanValue()) {
            this.format = FORMAT_TIME;
        }
    }

    /**
     * Tests if the format is float
     * 
     * @return the Boolean object corresponding to the primitive value true 
     * if the format is float; null otherwise.
     */
    public Boolean getFormatFLOAT() {
        if (FORMAT_FLOAT.equals(format)) {
            return Boolean.TRUE;
        }
        return null;
    }

    /**
     * Sets the format as float if the the Boolean parameter corresponds to 
     * the primitive value true
     * 
     * @param formatFLOAT the format is float
     */ 
    public void setFormatFLOAT(Boolean formatFLOAT) {
        if (formatFLOAT != null && formatFLOAT.booleanValue()) {
            this.format = FORMAT_FLOAT;
        }
    }

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
     * Returns the size of the object in bytes
     *
     * @return the size property
     */
    public String getSize() {
        return size;
    }
    
    /**
     * Set the size of the object in bytes. Required for Leaf objects, 
     * size is not applicable for interior Nodes. 
     *
     * @param size the new size
     */
    public void setSize(String size) {
        this.size = size;
    }
    
    /**
     * Returns the title property
     *
     * @return the title property
     */    
    public String getTitle() {
        return title;
    }
    
    /**
     * Set the title user-friendly property
     *
     * @param title the new user-friendly name
     */
    public void setTitle(String title) {
        this.title = title;
    }
    
    /**
     * Returns the time stamp of the last modification.
     *
     * @return the tStamp
     */ 
    public String getTStamp() {
        return tStamp;
    }
    
    /**
     * Set the time stamp of the last modification
     *
     * @param stamp the new time stamp
     */
    public void setTStamp(String stamp) {
        tStamp = stamp;
    }
    
    /**
     * Returns the MIME type of the object
     *
     * @return the type property
     */   
    public String getType() {
        return type;
    }
    
    /**
     * Set the MIME type of the object
     *
     * @param type the new MIME type
     */
    public void setType(String type) {
        this.type = type;
    }
    
    /**
     * Returns the version Number of the object
     *
     * @return the verNo property
     */
    public String getVerNo() {
        return verNo;
    }
    
    /**
     * Set the version Number of the object
     *
     * @param verNo the new version number
     */
    public void setVerNo(String verNo) {
        this.verNo = verNo;
    }
    
    /**
     * Returns a String representation of the Run Time Properties
     * 
     * @return String
     */
    public String toString() {

        ToStringBuilder sb = new ToStringBuilder(this);
        
        sb.append("acl"    , acl);
        sb.append("format" , format); 
        sb.append("name"   , name); 
        sb.append("size"   , size);
        sb.append("title"  , title);
        sb.append("tStamp" , tStamp); 
        sb.append("type"   , type); 
        sb.append("verNo"  , verNo);
        
        return sb.toString();
    }
    
}
