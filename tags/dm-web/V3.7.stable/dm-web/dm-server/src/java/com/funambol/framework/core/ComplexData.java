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

import com.funambol.framework.core.dm.ddf.MgmtTree;

/**
 * Corresponds to the &lt;Data&gt; tag in the SyncML represent DTD when delivering
 * an Anchor
 *
 * @version $Id: ComplexData.java,v 1.3 2006/11/15 14:23:10 nichele Exp $
 */
public final class ComplexData
extends Data
implements java.io.Serializable {

    // ------------------------------------------------------------ Private data
    private Anchor anchor;
    private DevInf devInf;

    private MgmtTree mgmtTree;

    // ------------------------------------------------------------ Constructors
    /** For serialization purposes */
    public ComplexData() {}

    /**
     * Creates a Data object from the given anchors string.
     *
     * @param data the anchor
     *
     */
    public ComplexData(String data) {
        super(data);
    }


    // ---------------------------------------------------------- Public methods

    /**
     * Gets the Anchor object property
     *
     * @return anchor the Anchor object
     */
    public Anchor getAnchor() {
        return anchor;
    }

    /**
     * Sets the Anchor object property
     *
     * @param anchor the Anchor object
     */
    public void setAnchor(Anchor anchor) {
        if (null == anchor) {
            throw new IllegalArgumentException("anchor cannot be null");
        }
        this.anchor = anchor;
    }

    /**
     * Gets the DevInf object property
     *
     * @return devInf the DevInf object property
     */
    public DevInf getDevInf() {
        return devInf;
    }

    /**
     * Sets the DevInf object property
     *
     * @param devInf the DevInf object property
     *
     */
    public void setDevInf(DevInf devInf) {
        if (null == devInf) {
            throw new IllegalArgumentException("devInf cannot be null");
        }
        this.devInf = devInf;
    }

    public MgmtTree getMgmtTree() {
        return mgmtTree;
    }
    
    public void setMgmtTree(MgmtTree mgmtTree) {
        if (null == mgmtTree) {
            throw new IllegalArgumentException("mgmtTree cannot be null");
        }
        this.mgmtTree = mgmtTree;
    }

}
