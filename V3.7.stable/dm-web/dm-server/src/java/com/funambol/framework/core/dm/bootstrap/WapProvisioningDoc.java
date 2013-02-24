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

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.JiBXException;


/**
 * Corresponds to the &lt;wap-provisioningdoc&gt; tag in a Wap provisioning doc.
 *
 *
 *
 * @version $Id: WapProvisioningDoc.java,v 1.1 2006/11/15 13:43:15 nichele Exp $
 *
 */
public final class WapProvisioningDoc implements Serializable {

    private static final long serialVersionUID = 994146838047570571L;

    // -------------------------------------------------------------- Properties
    private ArrayList<Characteristic> characteristics = null;

    private String version = null;

    // ------------------------------------------------------------ Constructors

    /**
     * Creates a new WapProvisioningDoc
     */
    public WapProvisioningDoc() {
        characteristics = new ArrayList<Characteristic>();
    }


    /**
     * Creates a new WapProvisioningDoc with the given versione
     * @param version the version of this WapProvisioningDoc
     */
    public WapProvisioningDoc(String version) {
        this.version = version;
        characteristics = new ArrayList<Characteristic>();
    }


    /**
      * Gets the version of this WapProvisioningDoc
      * @return the version of this WapProvisioningDoc
      */
     public String getVersion() {
         return version;
     }

     /**
      * Sets the version of this WapProvisioningDoc
      * @param version the version to set
      */
     public void setVersion(String version) {
         this.version = version;
     }

    /**
     * Adds new <code>Characteristic</code> to this WapProvisioningDoc
     *
     * @param characteristicToAdd the characteristic to add
     */
    public void addCharacteristic(Characteristic characteristicToAdd) {
        characteristics.add(characteristicToAdd);
    }

    /**
     * Adds a list of<code>Characteristic</code> to this WapProvisioningDoc
     *
     * @param characteristicsToAdd the characteristics to add
     */
    public void addCharacteristicList(List<Characteristic> characteristicsToAdd) {
            characteristics.addAll(characteristicsToAdd);
    }

    /**
     * Returns the characteristcs list
     *
     */
    public ArrayList<Characteristic> getCharacteristics() {
        return characteristics;
    }

    /**
     * Returns a xml representation of this WapProvisioningDoc
     * @return String
     */
    public String toXml() throws JiBXException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        IBindingFactory f = BindingDirectory.getFactory(WapProvisioningDoc.class);
        IMarshallingContext c = f.createMarshallingContext();
        c.setIndent(0);
        c.marshalDocument(this, "UTF-8", null, bout);

        String inputXml = new String(bout.toByteArray());

        return inputXml;

    }

}
