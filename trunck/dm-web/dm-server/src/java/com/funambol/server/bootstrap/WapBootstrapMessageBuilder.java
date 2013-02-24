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
 
package com.funambol.server.bootstrap;


import com.funambol.framework.core.dm.bootstrap.BasicAccountInfo;
import com.funambol.framework.core.dm.bootstrap.Characteristic;
import com.funambol.framework.core.dm.bootstrap.Parm;
import com.funambol.framework.core.dm.bootstrap.WapProvisioningDoc;
import com.funambol.framework.notification.NotificationException;
import com.funambol.framework.tools.Base64;

import com.funambol.server.notification.WBXMLTools;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.jibx.runtime.JiBXException;


/**
 * This is the builder to create a bootstrap message according to the WAP profile
 * @version $Id: WapBootstrapMessageBuilder.java,v 1.1 2006/11/15 14:04:15 nichele Exp $
 */
public class WapBootstrapMessageBuilder {

    // --------------------------------------------------------------- Constants
    private static final String VERSION_WAP_PROVISIONIG_DOC     = "1.0";
    private static final String APPLICATION_ID                  = "w7";
    private static final String AAUTH_LEVEL_SERVER              = "APPSRV";
    private static final String AAUTH_LEVEL_CLIENT              = "CLIENT";

    private static final String PROPERTY_NAME_ADDR              = "ADDR";
    private static final String PROPERTY_NAME_APPLICATION_ID    = "APPID";
    private static final String PROPERTY_NAME_NAME              = "NAME";
    private static final String PROPERTY_NAME_PROVIDER_ID       = "PROVIDER-ID";
    private static final String PROPERTY_NAME_AAUTHSECRET       = "AAUTHSECRET";
    private static final String PROPERTY_NAME_AAUTHDATA         = "AAUTHDATA";
    private static final String PROPERTY_NAME_AAUTHNAME         = "AAUTHNAME";
    private static final String PROPERTY_NAME_AAUTHLEVEL        = "AAUTHLEVEL";
    private static final String PROPERTY_NAME_PORTNBR           = "PORTNBR";

    // ------------------------------------------------------------ Private data

    private static final Logger log = Logger.getLogger(com.funambol.server.bootstrap.WapBootstrapMessageBuilder.class.getName());

    // ------------------------------------------------------------- Constructor

    /**
     * Creates a new WapBootstrapMessageBuilder
     */
    public WapBootstrapMessageBuilder() {
    }

    // ---------------------------------------------------------- Public methods

    /**
     * Creates the bootstrap message using the given information
     * @param basicAccountInfo the basic account info
     * @param wapDoc the base WapProvisioningDoc
     * @return the wap bootstrap message (encoded in wbxml)
     */
    public byte[] buildMessage(BasicAccountInfo  basicAccountInfo,
                               WapProvisioningDoc wapDoc)
    throws NotificationException {

        if (wapDoc == null) {
            wapDoc = new WapProvisioningDoc(VERSION_WAP_PROVISIONIG_DOC);
        }

        Characteristic w7 = getApplicationW7(wapDoc);
        if (w7 == null) {
            w7 = new Characteristic(Characteristic.TYPE_APPLICATION);
            Parm appId = new Parm(PROPERTY_NAME_APPLICATION_ID, APPLICATION_ID);
            w7.add(appId);
            wapDoc.addCharacteristic(w7);
        }

        setDMAccount(basicAccountInfo, w7);

        if (log.isEnabledFor(Level.TRACE)) {
            try {
                log.trace("WapProvisioninDoc to send: " + wapDoc.toXml());
            } catch (JiBXException ex) {
                //
                // Ignore it
                //
            }
        }

        //
        // Convert the message to WBXML
        //
        byte[] message = null;

        try {
            message = WBXMLTools.toWBXML(wapDoc);
        } catch (Exception e) {
            throw new NotificationException("Error during the conversion of the WAP bootstrap message in WBXML", e);
        }

        return message;
    }

    // --------------------------------------------------------- Private Methods

    /**
     * Search in the list of the characteristics of the given WapProvisioningDoc
     * a characterictic with type=APPLICATION and APPID=w7.
     * @param wapDoc the WapProvisioningDoc to search in
     * @return Characteristic
     */
    private Characteristic getApplicationW7(WapProvisioningDoc wapDoc) {
        List<Characteristic> characteristics = wapDoc.getCharacteristics();
        if (characteristics == null || characteristics.isEmpty()) {
            return null;
        }
        Iterator<Characteristic>       itCharacteristics = characteristics.iterator();
        Characteristic c          = null;
        Parm           parm       = null;

        while (itCharacteristics.hasNext()) {
            c = itCharacteristics.next();
            if (Characteristic.TYPE_APPLICATION.equalsIgnoreCase(c.getType())) {
                parm = c.getParm(PROPERTY_NAME_APPLICATION_ID);
                if (parm != null && APPLICATION_ID.equals(parm.getValue())) {
                    return c;
                }
            }
        }
        return null;
    }

    /**
     * Sets in the given Characteristic the info contained in the given BasicAccountInfo.
     * If the characteristic already contains some information, that are overwritten.
     * @param basicAccountInfo BasicAccountInfo
     * @param applicationCharacteristic Characteristic
     */
    private void setDMAccount(BasicAccountInfo basicAccountInfo,
                              Characteristic   applicationCharacteristic) {

        //
        // We remove the existing values
        //
        applicationCharacteristic.removeParm(PROPERTY_NAME_NAME);
        applicationCharacteristic.removeParm(PROPERTY_NAME_PROVIDER_ID);
        applicationCharacteristic.removeCharacteristic(Characteristic.TYPE_APPAUTH);
        applicationCharacteristic.removeCharacteristic(Characteristic.TYPE_APPADDR);

        int position = 1; // in the first position there is the APPIP parm

        String accountName = basicAccountInfo.getAccountName();
        if (accountName != null) {
            position = applicationCharacteristic.setParm(PROPERTY_NAME_NAME, accountName, position++);
        }

        applicationCharacteristic.setParm(PROPERTY_NAME_PROVIDER_ID,
                                          basicAccountInfo.getServerId(),
                                          position++);

        // Server address and port
        Characteristic appAddr = new Characteristic(Characteristic.TYPE_APPADDR);
        Parm addr = new Parm(PROPERTY_NAME_ADDR, basicAccountInfo.getServerAddress());
        appAddr.add(addr);

        // Port number
        Characteristic port = new Characteristic(Characteristic.TYPE_PORT);
        Parm portNbr = new Parm(PROPERTY_NAME_PORTNBR,
                                String.valueOf(basicAccountInfo.getServerPort()));
        port.add(portNbr);
        appAddr.add(port);

        // Server credential
        Characteristic appAuthServer = new Characteristic(Characteristic.
                TYPE_APPAUTH);
        Parm levelServer = new Parm(PROPERTY_NAME_AAUTHLEVEL, AAUTH_LEVEL_SERVER);
        Parm serverName = new Parm(PROPERTY_NAME_AAUTHNAME, basicAccountInfo.getServerId());
        Parm serverPw = new Parm(PROPERTY_NAME_AAUTHSECRET,
                                 basicAccountInfo.getServerPassword());

        String serverNonceB64 = new String(Base64.encode(basicAccountInfo.getServerNonce()));
        Parm serverNonce = new Parm(PROPERTY_NAME_AAUTHDATA, serverNonceB64);

        appAuthServer.add(levelServer);
        appAuthServer.add(serverName);
        appAuthServer.add(serverPw);
        appAuthServer.add(serverNonce);

        // Client credential
        Characteristic appAuthClient = new Characteristic(Characteristic.
                TYPE_APPAUTH);
        Parm levelClient = new Parm(PROPERTY_NAME_AAUTHLEVEL, AAUTH_LEVEL_CLIENT);
        Parm clientName = new Parm(PROPERTY_NAME_AAUTHNAME, basicAccountInfo.getClientUsername());
        Parm clientPwd = new Parm(PROPERTY_NAME_AAUTHSECRET,
                                  basicAccountInfo.getClientPassword());

        String clientNonceB64 = new String(Base64.encode(basicAccountInfo.getClientNonce()));
        Parm clientNonce = new Parm(PROPERTY_NAME_AAUTHDATA, clientNonceB64);

        appAuthClient.add(levelClient);
        appAuthClient.add(clientName);
        appAuthClient.add(clientPwd);
        appAuthClient.add(clientNonce);

        // Adds the previous characteristic to the 'Application' characteristic
        applicationCharacteristic.add(appAddr);
        applicationCharacteristic.add(appAuthServer);
        applicationCharacteristic.add(appAuthClient);
    }
}
