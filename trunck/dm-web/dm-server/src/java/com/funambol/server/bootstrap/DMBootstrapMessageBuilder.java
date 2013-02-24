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
import com.funambol.framework.core.dm.ddf.MgmtTree;
import com.funambol.framework.core.dm.ddf.Node;
import com.funambol.framework.core.dm.ddf.RTProperties;
import com.funambol.framework.engine.dm.Util;
import com.funambol.framework.notification.NotificationException;
import com.funambol.framework.tools.Base64;
import com.funambol.framework.tools.WBXMLTools;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;


/**
 * This is the builder to create a bootstrap message according to the DM profile
 * @version $Id: DMBootstrapMessageBuilder.java,v 1.1 2006/11/15 14:04:15 nichele Exp $
 */
public class DMBootstrapMessageBuilder {

    // --------------------------------------------------------------- Constants

    //
    // Device Management Account
    // Management Object
    //
    public static final String ACC_APP_ID            = "AppID";
    public static final String ACC_SERVER_ID         = "ServerID";
    public static final String ACC_NAME              = "Name";
    public static final String ACC_AUTH_PREF         = "AAuthPref";
    public static final String APP_ADDR              = "AppAddr";
    public static final String APP_AUTH              = "AppAuth";

    public static final String ACC_SERVER_ADDR       = "Addr";
    public static final String ACC_SERVER_ADDR_TYPE  = "AddrType";
    public static final String SERVER_PORT           = "Port";
    public static final String ACC_SERVER_PORT_NBR   = "PortNbr";

    public static final String ACC_AUTH_NAME         = "AAuthName";
    public static final String ACC_AUTH_LEVEL        = "AAuthLevel";
    public static final String ACC_AUTH_TYPE         = "AAuthType";
    public static final String ACC_AUTH_SECRET       = "AAuthSecret";
    public static final String ACC_AUTH_DATA         = "AAuthData";

    public static final String ACC_CLIENT            = "Client";
    public static final String ACC_SERVER            = "Server";

    //
    // Authentication type
    //
    public static final String AUTH_BASIC            = "BASIC";
    public static final String AUTH_MD5              = "DIGEST";
    public static final String AUTH_HMAC             = "HMAC";

    // -------------------------------------------------------------- Properties

    // ------------------------------------------------------------ Private data

    private static final Logger log = Logger.getLogger(com.funambol.server.bootstrap.DMBootstrapMessageBuilder.class.getName());

    // ------------------------------------------------------------- Constructor

    /**
     * Creates a new DMBootstrapMessageBuilder
     */
    public DMBootstrapMessageBuilder() {
 
    }

    // ---------------------------------------------------------- Public methods

    /**
     * Creates the bootstrap message using the given information
     *
     * @param basicAccountInfo the basic account info
     * @param bootstrapTree the MgmtTree to set in the DM bootstrap message
     * @throws NotificationException if an error occurs
     * @return the DM bootstrap message (encoded in wbxml)
     */
    public byte[] buildMessage(BasicAccountInfo basicAccountInfo,
                               MgmtTree bootstrapTree)
    throws NotificationException {

        //
        // Create a management tree using the basic account info
        //
        MgmtTree baiTree = createAccountMgmtTree(basicAccountInfo);

        bootstrapTree = DMBootstrapMessageBuilder.mergeMgmtTrees(bootstrapTree, baiTree);

        //
        // Create the SyncML message from the array of MgmtTree
        //

        SyncML syncMLPlainBootstrapMessage = getSyncMLBootstrapMessage(bootstrapTree, basicAccountInfo);

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
                log.trace("DMBootstrapMessage - xml: " + inputXml);
            }

            message = WBXMLTools.toWBXML(inputXml, Constants.DTD_1_2.getValue());

        } catch (Exception ex) {
            throw new NotificationException("Error during xml to wbxml transformation", ex);
    }

    return message;
}

    /**
     * Creates a new SyncML message parsing the given management trees.
     *
     * @param tree the managament tree
     * @param basicAccountInfo the basic account info
     * @return SyncML the new SyncML message
     * @throws NotificationException
     */
    private SyncML getSyncMLBootstrapMessage(MgmtTree tree, BasicAccountInfo basicAccountInfo) throws NotificationException {


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
                Constants.DTD_1_2,
                Constants.SYNCML_DM_1_2,
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

            Item item = Util.mgmtTree2item("./Inbox", tree, true, Constants.MIMETYPE_SYNCMLDM_WBXML);

            Add addCommand = new Add(
                    new CmdID(1),
                    false,
                    null ,
                    null ,
                    new Item[] {item});

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
     * Creates a new management tree using the basic account information
     *
     * @param basicAccountInfo the basic account info
     * @return MgmtTree the management tree with all the basic account info
     */
    private MgmtTree createAccountMgmtTree(BasicAccountInfo basicAccountInfo) {

        MgmtTree mgmtTree = new MgmtTree();
        mgmtTree.setVerDTD(Constants.DTD_1_2);

        //
        // Real time properties
        //
        String authentication = basicAccountInfo.getAuthPref();
        String authType       = "";
        if (Constants.AUTH_TYPE_BASIC.equalsIgnoreCase(authentication)){
            authType = AUTH_BASIC;
        } else if (Constants.AUTH_TYPE_MD5.equalsIgnoreCase(authentication)){
            authType = AUTH_MD5;
        } else if (Constants.AUTH_TYPE_HMAC.equalsIgnoreCase(authentication)){
            authType = AUTH_HMAC;
        }

        //
        // Convert the basic account info to a tree of Nodes
        //
        Node dmAcc = new Node(basicAccountInfo.getAccountName(),
                              null,
                              null,
                              RTProperties.FORMAT_NODE,
                              null);
        dmAcc.setType("org.openmobilealliance/1.0/w7");

        Node appId = new Node(ACC_APP_ID,
                              null,
                              null,
                              RTProperties.FORMAT_CHR,
                              "org.openmobilealliance/1.0/w7");

        dmAcc.addNode(appId);

        Node serverId = new Node(ACC_SERVER_ID,
                                 null,
                                 null,
                                 RTProperties.FORMAT_CHR,
                                 basicAccountInfo.getServerId());
        dmAcc.addNode(serverId);

        Node name = new Node(ACC_NAME,
                             null,
                             null,
                             RTProperties.FORMAT_CHR,
                             basicAccountInfo.getAccountName());
        dmAcc.addNode(name);

        Node appAddress = new Node(APP_ADDR,
                                   null,
                                   null,
                                   RTProperties.FORMAT_NODE,
                                   null);
        dmAcc.addNode(appAddress);

        Node mgmtServerName = new Node(basicAccountInfo.getAccountName(),
                                       null,
                                       null,
                                       RTProperties.FORMAT_NODE,
                                       null);
        appAddress.addNode(mgmtServerName);

        Node addr = new Node(ACC_SERVER_ADDR,
                             null,
                             null,
                             RTProperties.FORMAT_CHR,
                             basicAccountInfo.getServerAddress());
        mgmtServerName.addNode(addr);

        Node addressType = new Node(ACC_SERVER_ADDR_TYPE,
                                    null,
                                    null,
                                    RTProperties.FORMAT_CHR,
                                    String.valueOf(basicAccountInfo.getAddressType()));

        mgmtServerName.addNode(addressType);

        Node serverPort = new Node(SERVER_PORT,
                                   null,
                                   null,
                                   RTProperties.FORMAT_NODE,
                                   null);
        mgmtServerName.addNode(serverPort);

        Node portNbr = new Node(ACC_SERVER_PORT_NBR,
                                null,
                                null,
                                RTProperties.FORMAT_INT,
                                String.valueOf(basicAccountInfo.getServerPort()));
        serverPort.addNode(portNbr);

        Node authPref = new Node(ACC_AUTH_PREF, null, null, RTProperties.FORMAT_CHR, authType);
        dmAcc.addNode(authPref);

        Node appAuth = new Node(APP_AUTH, null, null, RTProperties.FORMAT_NODE, null);
        dmAcc.addNode(appAuth);

        Node authClient = new Node(ACC_CLIENT, null, null, RTProperties.FORMAT_NODE, null);
        dmAcc.addNode(authClient);

        Node cAuthType      = new Node(ACC_AUTH_TYPE, null, null, RTProperties.FORMAT_CHR, authType);
        authClient.addNode(cAuthType);

        Node cAuthName = new Node(ACC_AUTH_NAME,
                                  null,
                                  null,
                                  RTProperties.FORMAT_CHR,
                                  basicAccountInfo.getServerAddress());
        authClient.addNode(cAuthName);

        Node cAuthSecret = new Node(ACC_AUTH_SECRET,
                                    null,
                                    null,
                                    RTProperties.FORMAT_CHR,
                                    basicAccountInfo.getServerPassword());
        authClient.addNode(cAuthSecret);

        Node cAuthData = new Node(ACC_AUTH_DATA,
                                  null,
                                  null,
                                  RTProperties.FORMAT_CHR,
                                  new String(Base64.encode(basicAccountInfo.getServerNonce())));

        authClient.addNode(cAuthData);

        Node cAuthLevel = new Node(ACC_AUTH_LEVEL, null, null, RTProperties.FORMAT_CHR, "CLCRED");
        authClient.addNode(cAuthLevel);

        Node authServer = new Node(ACC_CLIENT, null, null, RTProperties.FORMAT_NODE, null);
        dmAcc.addNode(authServer);

        Node sAuthType = new Node(ACC_AUTH_TYPE, null, null, RTProperties.FORMAT_CHR, authType);
        authServer.addNode(sAuthType);

        Node sAuthName = new Node(ACC_AUTH_NAME,
                                  null,
                                  null,
                                  RTProperties.FORMAT_CHR,
                                  basicAccountInfo.getServerAddress());
        authServer.addNode(sAuthName);

        Node sAuthSecret = new Node(ACC_AUTH_SECRET,
                                    null,
                                    null,
                                    RTProperties.FORMAT_CHR,
                                    basicAccountInfo.getServerPassword());
        authServer.addNode(sAuthSecret);

        Node sAuthData = new Node(ACC_AUTH_DATA,
                                  null,
                                  null,
                                  RTProperties.FORMAT_CHR,
                                  new String(Base64.encode(basicAccountInfo.getServerNonce())));
        authServer.addNode(sAuthData);

        Node sAuthLevel = new Node(ACC_AUTH_LEVEL,
                                   null,
                                   null,
                                   RTProperties.FORMAT_CHR,
                                   "SRVCRED");
        authServer.addNode(sAuthLevel);

        mgmtTree.addNode(dmAcc);

        return mgmtTree;
    }

    /**
     * Merge the content of two management tree and return a new management tree. Nodes from the second
     * management tree update the correspondent nodes in the first one. New nodes from the second
     * management tree are added. Note that mergeMgmtTrees(a,b) != mergeMgmtTrees(b,a).
     *
     * @param firstTree the first managemente tree to be merged
     * @param secondTree the second managemente tree to be merged
     *
     * @return MgmtTree the merged MgmtTree
     */
    public static MgmtTree mergeMgmtTrees(MgmtTree firstTree, MgmtTree secondTree) {

        if(firstTree == null){
            return secondTree;
        }
        if(secondTree == null){
            return firstTree;
        }

        List<Node> nodes = Util.mergeSubNodes(firstTree.getTreeNodes(),secondTree.getTreeNodes());
        firstTree.setTreeNodes((ArrayList<Node>) nodes);

        return firstTree;
    }
}
