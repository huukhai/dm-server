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
import java.util.List;


/**
 * This class represents the &lt;Meta&gt; tag as defined by the SyncML
 * representation specifications.
 *
 *
 *
 * @version $Id: Meta.java,v 1.3 2006/11/15 14:21:53 nichele Exp $
 *
 */
public final class Meta implements java.io.Serializable {

    // ------------------------------------------------------------ Private data
    private String     format    ;
    private String     type      ;
    private String     mark      ;
    private Anchor     anchor    ;
    private String     version   ;
    private NextNonce  nextNonce ;
    private Long       maxMsgSize;
    private Long       maxObjSize;
    private Long       size      ;
    private ArrayList<EMI>  emi  ;
    private Mem        mem       ;
    private MetInf     metInf    ;

    // ------------------------------------------------------------ Constructors

    /**
     * For serialization purposes
     */
    public Meta() {
        set(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );
    }

    // ---------------------------------------------------------- Public methods

    /**
     * Returns the <i>metInf</i> object. If null, a new instance is created and
     * stored in <i>metInf</i>
     *
     * @return the value of <i>metInf</i> or a new instance if <i>metInf</i> is null
     */
    public MetInf getMetInf() {
        if (metInf == null) {
            return (metInf = new MetInf());
        }

        return metInf;
    }

    /**
     * Sets <i>metInf</i> to the given value.
     *
     * @param metInf the new <i>metInf</i> value
     */
    public void setMetInf(MetInf metInf) {
        this.metInf = metInf;
    }

    /**
     * This get method always returns null. This is a used in the JiBX mapping
     * in order to do not output the MetInf element.
     *
     * @return always null
     */
    public MetInf getNullMetInf() {
        return null;
    }

    /**
     * Returns dateSize (in bytes)
     *
     * @return size
     */
    public Long getSize() {
        return getMetInf().getSize();
    }

    /**
     * Sets size
     *
     * @param size the new size value
     */
    public void setSize(Long size) {
        getMetInf().setSize(size);
    }

    /**
     * Returns format
     *
     * @return format
     */
    public String getFormat() {
        return getMetInf().getFormat();
    }

    /**
     * Sets format
     *
     * @param format the new format value
     */
    public void setFormat(String format) {
        getMetInf().setFormat(format);
    }

    /**
     * Returns type
     *
     * @return type
     */
    public String getType() {
        return getMetInf().getType();
    }

    /**
     * Sets type
     *
     * @param type the new type value
     */
    public void setType(String type) {
        getMetInf().setType(type);
    }

    /**
     * Returns mark
     *
     * @return mark
     */
    public String getMark() {
        return getMetInf().getMark();
    }

    /**
     * Sets mark
     *
     * @param mark the new mark value
     */
    public void setMark(String mark) {
        getMetInf().setMark(mark);
    }

    /**
     * Returns anchor
     *
     * @return anchor
     */
    public Anchor getAnchor() {
        return getMetInf().getAnchor();
    }

    /**
     * Sets anchor
     *
     * @param anchor the new anchor value
     */
    public void setAnchor(Anchor anchor) {
        getMetInf().setAnchor(anchor);
    }

    /**
     * Returns version
     *
     * @return version
     */
    public String getVersion() {
        return getMetInf().getVersion();
    }

    /**
     * Sets version
     *
     * @param version the new version value
     */
    public void setVersion(String version) {
        getMetInf().setVersion(version);
    }

    /**
     * Returns nextNonce
     *
     * @return nextNonce
     */
    public NextNonce getNextNonce() {
        return getMetInf().getNextNonce();
    }

    /**
     * Sets nextNonce
     *
     * @param nextNonce the new nextNonce value
     */
    public void setNextNonce(NextNonce nextNonce) {
        getMetInf().setNextNonce(nextNonce);
    }

    /**
     * Returns maxMsgSize
     *
     * @return maxMsgSize
     */
    public Long getMaxMsgSize() {
        return getMetInf().getMaxMsgSize();
    }

    /**
     * Sets maxMsgSize
     *
     * @param maxMsgSize the new maxMsgSize value
     */
    public void setMaxMsgSize(Long maxMsgSize) {
        getMetInf().setMaxMsgSize(maxMsgSize);
    }

    /**
     * Returns maxObjSize
     *
     * @return maxObjSize
     */
    public Long getMaxObjSize() {
        return getMetInf().getMaxObjSize();
    }

    /**
     * Sets maObjSize
     *
     * @param maxObjSize the new maxObjSize value
     */
    public void setMaxObjSize(Long maxObjSize) {
        getMetInf().setMaxObjSize(maxObjSize);
    }

    /**
     * Returns emi
     *
     * @return emi
     */
    public ArrayList<EMI> getEMI() {
        return getMetInf().getEMI();
    }

    /**
     *
     * This property is binding with set-method and there is a
     * bug into JiBx: it uses the first method with the specified
     * name without checking the parameter type.
     * This method must be written before all other setEMI() methods
     * to have a right marshalling.
     *
     * @param emi ArrayList of EMI object
     */
    public void setEMI(ArrayList<EMI> emi) {
        if (emi != null) {
            getMetInf().getEMI().addAll(emi);
        }
    }

    /**
     * Sets emi
     *
     * @param emi the new emi value
     */
    public void setEMI(EMI[] emi) {
        if (emi != null) {
            List<EMI> c = Arrays.asList(emi);
            getMetInf().getEMI().addAll(c);
        }
    }

    /**
     * Returns mem
     *
     * @return mem
     */
    public Mem getMem() {
        return getMetInf().getMem();
    }

    /**
     * Sets mem
     *
     * @param mem the new mem value
     */
    public void setMem(Mem mem) {
        getMetInf().setMem(mem);
    }

    // --------------------------------------------------------- Private methods

    /**
     * Sets all properties in once.
     *
     * @param format the encoding format
     * @param type usually a MIME type
     * @param mark the mark element
     * @param size the data size in bytes
     * @param anchor the Anchor
     * @param version the data version
     * @param nonce the next nonce value
     * @param maxMsgSize the maximum message size in bytes
     * @param emi experimental meta info
     * @param mem memory information
     *
     */
    private void set(final String    format    ,
                     final String    type      ,
                     final String    mark      ,
                     final Long      size      ,
                     final Anchor    anchor    ,
                     final String    version   ,
                     final NextNonce nonce     ,
                     final Long      maxMsgSize,
                     final Long      maxObjSize,
                     final EMI[]     emi       ,
                     final Mem       mem       ) {
        getMetInf(); // if still null, a new instance will be created

        metInf.setFormat     (format    );
        metInf.setType       (type      );
        metInf.setMark       (mark      );
        metInf.setAnchor     (anchor    );
        metInf.setSize       (size      );
        metInf.setVersion    (version   );
        metInf.setNextNonce  (nonce     );
        metInf.setMaxMsgSize (maxMsgSize);
        metInf.setMaxObjSize (maxObjSize);
        metInf.setMem        (mem       );
        metInf.setEMI        (emi       );
    }

}
