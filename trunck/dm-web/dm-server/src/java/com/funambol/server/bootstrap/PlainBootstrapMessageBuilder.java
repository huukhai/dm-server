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


import com.funambol.framework.core.AbstractCommand;
import com.funambol.framework.core.Add;
import com.funambol.framework.core.CmdID;
import com.funambol.framework.core.Constants;
import com.funambol.framework.core.Item;
import com.funambol.framework.core.RepresentationException;
import com.funambol.framework.core.SessionID;
import com.funambol.framework.core.Source;
import com.funambol.framework.core.SyncBody;
import com.funambol.framework.core.SyncHdr;
import com.funambol.framework.core.SyncML;
import com.funambol.framework.core.Target;
import com.funambol.framework.core.dm.bootstrap.BasicAccountInfo;
import com.funambol.framework.engine.dm.TreeNode;
import com.funambol.framework.engine.dm.Util;
import com.funambol.framework.notification.NotificationException;
import com.funambol.framework.tools.Base64;
import com.funambol.framework.tools.WBXMLTools;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;


/**
 * This is the builder to create a bootstrap message according to the PLAIN profile
 * @version $Id: PlainBootstrapMessageBuilder.java,v 1.1 2006/11/15 14:04:15 nichele Exp $
 */
public class PlainBootstrapMessageBuilder {

    // --------------------------------------------------------------- Constants

    //Settings
    public static final String ACC_NAME             = "/Name";
    public static final String ACC_AUTH_PREF        = "/AuthPref";
    //Server
    public static final String ACC_SERVER_ADDR      = "/Addr";
    public static final String ACC_SERVER_PORT_NBR  = "/PortNbr";
    public static final String ACC_SERVER_ADDR_TYPE = "/AddrType";
    public static final String ACC_SERVER_ID        = "/ServerId";
    public static final String ACC_SERVER_PASSWORD  = "/ServerPW";
    public static final String ACC_SERVER_NONCE     = "/ServerNonce";
    //Client
    public static final String ACC_CLIENT_USERNAME  = "/UserName";
    public static final String ACC_CLIENT_PASSWORD  = "/ClientPW";
    public static final String ACC_CLIENT_NONCE     = "/ClientNonce";

    // -------------------------------------------------------------- Properties

    // ------------------------------------------------------------ Private data

    private static final Logger log = Logger.getLogger(com.funambol.server.bootstrap.PlainBootstrapMessageBuilder.class.getName());

    // ------------------------------------------------------------- Constructor

    /**
     * Creates a new PlainBootstrapMessageBuilder
     */
    public PlainBootstrapMessageBuilder() {
    }

    // ---------------------------------------------------------- Public methods

    /**
     * Creates the plain bootstrap message using the given information
     * @param basicAccountInfo the basic account info
     * @param nodes the nodes to set in the plain bootstrap message
     * @throws NotificationException if an error occurs
     * @return the plain bootstrap message (encoded in WBXML)
     */
    public byte[] buildMessage(BasicAccountInfo basicAccountInfo,
                               TreeNode[]       nodes)
    throws NotificationException {
        if (log.isEnabledFor(Level.TRACE)) {
            log.trace("Calling buildMessage in PlainBootstrapMessageBuilder");
        }

        Map<String, Object> bootstrapNodes = accountInfoNodes(basicAccountInfo);

        if (nodes != null) {
            for (int i = 0; i < nodes.length; i++) {
                if (!bootstrapNodes.containsKey(nodes[i].getName())) {
                    bootstrapNodes.put(nodes[i].getName(), nodes[i]);
                }
            }
        }

        SyncML syncMLPlainBootstrapMessage = getSyncMLBootstrapMessage(bootstrapNodes, basicAccountInfo);

        byte[] message = null;

        try {

            ByteArrayOutputStream bout = new ByteArrayOutputStream();

            IBindingFactory f = BindingDirectory.getFactory(SyncML.class);
            IMarshallingContext c = f.createMarshallingContext();
            c.setIndent(0);
            c.marshalDocument(syncMLPlainBootstrapMessage, "UTF-8", null, bout);

            String inputXml = new String(bout.toByteArray());
            inputXml = metInfNamespaceHandler(inputXml);

            if (log.isEnabledFor(Level.TRACE)) {
                log.trace("PlainBootstrapMessage - xml: " + inputXml);
            }

            message = WBXMLTools.toWBXML(inputXml, Constants.DTD_1_1.getValue());

        } catch (Exception ex) {
            throw new NotificationException("Error during xml to wbxml transformation", ex);
    }

    return message;
}

    /**
     * This is a solution in order to obviate to a JiBX bug: it does
     * not allow to declare the namespace to level of structure.
     *
     * @param msg the server response
     *
     * @return the response with namespace correctly added into MetInf element
     */
    private String metInfNamespaceHandler(String msg) {
        int s = 0;
        int e = 0;

        StringBuffer response = new StringBuffer();
        while (( e = msg.indexOf("<Meta", s)) >= 0) {

            response = response.append(msg.substring(s, e));

            int a = msg.indexOf("</Meta>", e);
            String meta = msg.substring(e, a);

            meta = meta.replaceAll("<Type>"   , "<Type xmlns='syncml:metinf'>");
            meta = meta.replaceAll("<Format>" , "<Format xmlns='syncml:metinf'>");
            meta = meta.replaceAll("<Mark>"   , "<Mark xmlns='syncml:metinf'>");
            meta = meta.replaceAll("<Size>"   , "<Size xmlns='syncml:metinf'>");
            meta = meta.replaceAll("<Anchor>" , "<Anchor xmlns='syncml:metinf'>");
            meta = meta.replaceAll("<Version>", "<Version xmlns='syncml:metinf'>");
            meta = meta.replaceAll("<NextNonce>" , "<NextNonce xmlns='syncml:metinf'>");
            meta = meta.replaceAll("<MaxMsgSize>", "<MaxMsgSize xmlns='syncml:metinf'>");
            meta = meta.replaceAll("<MaxObjSize>", "<MaxObjSize xmlns='syncml:metinf'>");
            meta = meta.replaceAll("<EMI>"    , "<EMI xmlns='syncml:metinf'>");
            meta = meta.replaceAll("<Mem>"    , "<Mem xmlns='syncml:metinf'>");

            s = a + 7;
            response.append(meta).append("</Meta>");
        }
        return response.append(msg.substring(s, msg.length())).toString();
    }


    /**
     * Returns a linked hash map of the bootstrap account properties
     *
     * @param basicAccountInfo
     * @return the linked hash map of the basic account info
     */
    private Map<String, Object> accountInfoNodes(BasicAccountInfo basicAccountInfo) {

        Map<String, Object> accountNodes = new LinkedHashMap<String, Object>();
        String accountURI = "./SyncML/DMAcc/" + basicAccountInfo.getAccountName();

        //Settings

        accountNodes.put(accountURI, new TreeNode(accountURI, null, TreeNode.FORMAT_NODE));

        accountNodes.put(accountURI + ACC_AUTH_PREF,
                         new TreeNode(accountURI + ACC_AUTH_PREF, basicAccountInfo.getAuthPref()));

        //Server
        accountNodes.put(accountURI + ACC_SERVER_ADDR,
                         new TreeNode(accountURI + ACC_SERVER_ADDR, basicAccountInfo.getServerAddress()));

        accountNodes.put(accountURI + ACC_SERVER_PORT_NBR,
                         new TreeNode(accountURI + ACC_SERVER_PORT_NBR,
                                      Integer.toString(basicAccountInfo.getServerPort()),
                                      TreeNode.FORMAT_INT)
                         );

        accountNodes.put(accountURI + ACC_SERVER_ADDR_TYPE,
                         new TreeNode(accountURI + ACC_SERVER_ADDR_TYPE,
                                      Integer.toString(basicAccountInfo.getAddressType()),
                                      TreeNode.FORMAT_INT)
                         );

        accountNodes.put(accountURI + ACC_SERVER_ID,
                         new TreeNode(accountURI + ACC_SERVER_ID, basicAccountInfo.getServerId()));

        accountNodes.put(accountURI + ACC_SERVER_NONCE,
                         new TreeNode(accountURI + ACC_SERVER_NONCE,
                                      Base64.encode(basicAccountInfo.getServerNonce()),
                                      TreeNode.FORMAT_BINARY)
                         );

        accountNodes.put(accountURI + ACC_SERVER_PASSWORD,
                         new TreeNode(accountURI + ACC_SERVER_PASSWORD, basicAccountInfo.getServerPassword()));


        //Client
        accountNodes.put(accountURI + ACC_CLIENT_NONCE,
                         new TreeNode(accountURI + ACC_CLIENT_NONCE,
                                      Base64.encode(basicAccountInfo.getClientNonce()))
                         );

        accountNodes.put(accountURI + ACC_CLIENT_PASSWORD,
                         new TreeNode(accountURI + ACC_CLIENT_PASSWORD, basicAccountInfo.getClientPassword()));

        accountNodes.put(accountURI + ACC_CLIENT_USERNAME,
                         new TreeNode(accountURI + ACC_CLIENT_USERNAME, basicAccountInfo.getClientUsername()));


        return accountNodes;
    }

    /**
     * Returns the <code>SyncML</code> object corresponds to the bootstrap message for the given <code>SycnMLDM</code> object.
     * @param bootstrapNodes the Map of Tree Nodes
     *
     * @throws NotificationException if a error occurs during message creation
     * @return SyncML the <code>SyncML</code> corresponds to the bootstrap message
     */
    private SyncML getSyncMLBootstrapMessage(Map<String, Object> bootstrapNodes, BasicAccountInfo basicAccountInfo)
    throws NotificationException {

        SyncML   syncMLBootstrapMessage = null;
        SyncHdr  syncHdrMessage         = null;
        SyncBody syncBodyMessage        = null;

        //
        // SyncML Header Message
        //

        try {

            URL url = null;
            URL newUrlWithPort = null;

            try {
                url = new URL(basicAccountInfo.getServerAddress());
                String hostName = url.getHost();
                String protocol = url.getProtocol();
                String file = url.getFile();

                newUrlWithPort = new URL(protocol, hostName,
                                         basicAccountInfo.getServerPort(), file);
            } catch (MalformedURLException e) {
                throw new NotificationException("Error creating the serverURI", e);
            }


            syncHdrMessage = new SyncHdr(
                Constants.DTD_1_1,
                Constants.SYNCML_DM_1_1,
                new SessionID("0"),
                "0", // MsgID
                new Target(basicAccountInfo.getDeviceId()),
                new Source(newUrlWithPort.toExternalForm()),
                null, /* response URI */
                false, /* no reponse   */
                null, /* credentials  */
                null
                /* metadata     */
                );


            //
            // SyncML Body Message
            //
            Item[] items = Util.nodes2Items(bootstrapNodes, true, false, null);
            Add addCommand = new Add(
                    new CmdID(1),
                    false,
                    null ,
                    null ,
                    items);

            syncBodyMessage = new SyncBody(
                new AbstractCommand[] {addCommand},
                true
                /* final */
                );


            syncMLBootstrapMessage = new SyncML(syncHdrMessage, syncBodyMessage);


        } catch (RepresentationException ex) {
            throw new NotificationException("Error during creation of the SyncML message", ex);
        }

        return syncMLBootstrapMessage;
    }

}
