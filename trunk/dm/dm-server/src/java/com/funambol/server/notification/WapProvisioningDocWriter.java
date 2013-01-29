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
 
package com.funambol.server.notification;


import java.io.IOException;
import java.io.OutputStream;

import org.kxml.wap.Wbxml;
import org.kxml.wap.WbxmlWriter;



/**
 * A parser for WapProvisioningDoc built on top of the WbxmlWriter
 *
 *
 * @version $Id: WapProvisioningDocWriter.java,v 1.2 2006/08/07 21:09:25 nichele Exp $
 */
public class WapProvisioningDocWriter extends WbxmlWriter {
    // --------------------------------------------------------------- Constants

    // ------------------------------------------------------------ Private data
    private OutputStream out = null;

    /**
     * Calls constructor of WbxmlWriter, sets then the appropriate tag table
     * @exception IOException if an error occurs
     */
    public WapProvisioningDocWriter(OutputStream out) throws IOException {
        super(out);

        this.out = out;

        setTagTable(0, WapProvisioningDocWBXML.tagTable[0]);
        setTagTable(1, WapProvisioningDocWBXML.tagTable[1]);

        setAttrStartTable(0, WapProvisioningDocWBXML.attributeStartTable[0]);
        setAttrStartTable(1, WapProvisioningDocWBXML.attributeStartTable[1]);

        setAttrValueTable(0, WapProvisioningDocWBXML.paramValueTable[0]);
        setAttrValueTable(1, WapProvisioningDocWBXML.paramValueTable[1]);
    }


    /**
     * Writes out WBXML headers, override for other behaviour / values
     *
     * @exception IOException if an error occurs while writing
     */
    protected void writeHeader() throws IOException {
        out.write(Wbxml.WBXML_VERSION_12  ); // version
        out.write(0x0B); // public identifier
        out.write(Wbxml.WBXML_CHARSET_UTF8); // UTF-8
    }


}