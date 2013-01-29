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

import java.util.ArrayList;
import java.util.Arrays;


/**
 * This class represents the content type property such as property name, value,
 * display name, size and the content type parameters
 *
 * 
 *
 *  @version $Id: CTPropParam.java,v 1.3 2006/11/15 14:21:53 nichele Exp $
 */
public final class CTPropParam
implements java.io.Serializable {

    // ------------------------------------------------------------ Private data
    private String propName;
    private ArrayList<String> valEnum = new ArrayList<String>();
    private String displayName;
    private String dataType;
    private int size;
    private ArrayList<ContentTypeParameter> ctParameters = new ArrayList<ContentTypeParameter>();

    // ------------------------------------------------------------ Constructors

    /** For serialization purposes */
    protected CTPropParam() {}

    /**
     * Creates a new ContentTypeProperty object with the given name, value and
     * display name
     *
     * @param propName corresponds to &lt;PropName&gt; element in the SyncML
     *                  specification - NOT NULL
     * @param valEnum   corresponds to &lt;ValEnum&gt; element in the SyncML
     *                  specification
     * @param displayName corresponds to &lt;DisplayName&gt; element in the SyncML
     *                  specification
     * @param ctParameters the array of content type parameters - NOT NULL
     *
     */
    public CTPropParam(final String propName,
                       final String[] valEnum,
                       final String displayName,
                       final ContentTypeParameter[] ctParameters) {
        setPropName(propName);
        setValEnum(valEnum);
        setContentTypeParameters(ctParameters);

        this.displayName  = displayName;
    }


    /**
     * Creates a new ContentTypeProperty object with the given name, type, size and
     * display name
     *
     * @param propName corresponds to &lt;PropName&gt; element in the SyncML
     *                  specification - NOT NULL
     * @param dataType   corresponds to &lt;CTType&gt; element in the SyncML
     *                  specification            
     * @param size   corresponds to &lt;MaxObjSize&gt; element in the SyncML
     *                  specification
     * @param displayName corresponds to &lt;DisplayName&gt; element in the SyncML
     *                  specification
     * @param ctParameters the array of content type parameters - NOT NULL
     *
     */
    public CTPropParam(final String propName,
                       final String dataType,
                       final int size,
                       final String displayName,
                       final ContentTypeParameter[] ctParameters) {

        setPropName(propName);
        setContentTypeParameters(ctParameters);

        this.dataType     = dataType;
        this.size         = size;
        this.displayName  = displayName;
    }

    // ---------------------------------------------------------- Public methods

    /**
     * Gets the property name
     *
     * @return the property name
     */
    public String getPropName() {
        return propName;
    }

    /**
     * Sets the property name
     *
     * @param propName the property name
     */
    public void setPropName(String propName) {
        if (propName == null){
            throw new IllegalArgumentException("propName cannot be null");
        }
        this.propName = propName;
    }

    /**
     * Gets the array of value for the property
     *
     * @return the array of value for the property
     */
    public ArrayList<String> getValEnum() {
        return this.valEnum;
    }

    /**
     * Sets the array of enumerated value property
     *
     * @param valEnum the array of enumerated value property
     */
    public void setValEnum(ArrayList<String> valEnum) {
        if (valEnum != null) {
            this.valEnum.clear();
            this.valEnum.addAll(valEnum);
        } else {
            this.valEnum = null;
        }
    }

    /**
     * Sets the array of enumerated value property
     *
     * @param valEnum the array of enumerated value property
     */
    public void setValEnum(String[] valEnum) {
        if (valEnum != null) {
            this.valEnum.clear();
            this.valEnum.addAll(Arrays.asList(valEnum));
        } else {
            this.valEnum = null;
        }
    }

    /**
     * Gets the display name property
     *
     * @return the display name property
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the display name of a given content type property
     *
     * @param displayName the display name of a given content type property
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the data type propeties
     *
     * @return the data type propeties
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * Sets the data type of a given content type property
     *
     * @param dataType the data type of a given content type property
     */
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    /**
     * Gets the size propeties
     *
     * @return the size propeties
     */
    public int getSize() {
        return size;
    }

    /**
     * Sets the size of a given content type property
     *
     * @param size the size of a given content type property
     *
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * Gets the array of ContentTypeParameter
     *
     * @return the size propeties
     */
    public ArrayList<ContentTypeParameter> getContentTypeParameters() {
        return this.ctParameters;
    }

    /**
     * Sets an array of content type properties
     *
     * @param ctParameters an array of content type properties
     *
     */
    public void setContentTypeParameters(ContentTypeParameter[] ctParameters) {
        if (ctParameters != null) {
            this.ctParameters.clear();
            this.ctParameters.addAll(Arrays.asList(ctParameters));
        } else {
            this.ctParameters = null;
        }
    }
}
