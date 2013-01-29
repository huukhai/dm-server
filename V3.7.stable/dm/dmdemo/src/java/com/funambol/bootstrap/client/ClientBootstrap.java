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

package com.funambol.bootstrap.client;


import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;

import com.funambol.framework.server.error.ServerFailureException;
import com.funambol.framework.core.dm.bootstrap.WapProvisioningDoc;
import com.funambol.framework.core.dm.bootstrap.Characteristic;
import com.funambol.framework.core.dm.bootstrap.Parm;
import com.funambol.framework.core.dm.bootstrap.BootStrap;
import com.funambol.framework.notification.NotificationConstants;
import com.funambol.framework.engine.dm.TreeNode;
import com.funambol.framework.core.dm.ddf.MgmtTree;

import com.funambol.server.engine.dm.ejb.ManagementRemote;


/**
 *
 * @version $Id: ClientBootstrap.java,v 1.3 2007-06-18 16:38:44 luigiafassina Exp $
 */
public class ClientBootstrap {

    public static final String MANAGEMENT_EJB_JNDI_NAME
        = "ejb:FunambolDMServer/funambol-server.jar//ManagementBean!com.funambol.server.engine.dm.ejb.ManagementRemote";

    public static final String CONTEXT_URL_PREFIXES
        = "org.jboss.ejb.client.naming";


    public static void main(String[] args) throws Exception {
        if (args.length != 6) {
            System.out.println("Use com.funambol.bootstrap.client.Bootstrap " +
                               "<messageType> <deviceId> <phoneNumber> <accountName> <userName> <password>");
            System.exit(-1);
        }

        System.out.println("Lookup management bean");

        int messageType;
        
        try {
            messageType   = Integer.parseInt(args[0]);
        } catch (NumberFormatException ex) {
            messageType = -1;
        }
        
        String deviceId      = args[1];
        String phoneNumber   = args[2];
        String accountName   = args[3];
        String username      = args[4];
        String password      = args[5];

        BootStrap bootstrap = new BootStrap(phoneNumber,
                                            messageType,
                                            null,  // sender
                                            null,  // wapDoc
                                            null,  // TreeNode[]
                                            null,  // MgmtTree
                                            accountName,
                                            username,
                                            password,
                                            deviceId,
                                            null,
                                            null,
                                            null,
                                            -1,
                                            false,
                                            "12345",
                                            "GetDeviceDetails");

        if (messageType == NotificationConstants.BOOTSTRAP_MESSAGE_TYPE_WAP) {
            bootstrap.setWapProvisioningDoc(getWapProvisioninDoc());
        } else if (messageType == NotificationConstants.BOOTSTRAP_MESSAGE_TYPE_PLAIN) {
            bootstrap.setNodes(getNodes());
        } else if (messageType == NotificationConstants.BOOTSTRAP_MESSAGE_TYPE_DM) {
            bootstrap.setMgmtTree(getMgmtTree());
        } else {
            System.out.println( args[0] + " is not an allowed message type. Valid options are:");
            System.out.println(NotificationConstants.BOOTSTRAP_MESSAGE_TYPE_WAP   + " - WAP profile");
            System.out.println(NotificationConstants.BOOTSTRAP_MESSAGE_TYPE_PLAIN + " - PLAIN profile");
            System.out.println(NotificationConstants.BOOTSTRAP_MESSAGE_TYPE_DM    + " - DM profile");
            System.exit(-1);   
        }

        ManagementRemote remote = getManagementBeanInstance();
        System.out.println("Bootstrapping");

        remote.bootstrap(bootstrap);

        System.out.println("Bootstrap sent");
    }

    private static TreeNode[] getNodes() {
        TreeNode serverPassword = new TreeNode("./SyncML/DMAcc/funambolAccount/ServerPW", "testCon");
        TreeNode conRef = new TreeNode("./SyncML/DMAcc/funambolAccount/ConRef", "testCon");
        TreeNode con    = new TreeNode("./SyncML/DMAcc/Con/TestCon", null, TreeNode.FORMAT_NODE);
        TreeNode conId  = new TreeNode("./SyncML/DMAcc/Con/TestCon/Id", "testCon");

        TreeNode[] nodes = new TreeNode[] {serverPassword, conRef, con, conId};
        return nodes;
    }

    private static MgmtTree getMgmtTree() {
        MgmtTree tree = new MgmtTree();
        tree.setVerDTD(com.funambol.framework.core.Constants.DTD_1_2);
        return tree;
        
    }

    private static WapProvisioningDoc getWapProvisioninDoc() {
        WapProvisioningDoc wapProvisioningDoc = new WapProvisioningDoc("1.0");

        // Application
        Characteristic application = new Characteristic("APPLICATION");
        Parm applicationId         = new Parm("APPID", "w7");
        Parm name                  = new Parm("NAME", "account name");
        Parm providerId            = new Parm("PROVIDER-ID", "provider name");
        Parm toNap                 = new Parm("TO-NAPID", "TEST");

        application.add(applicationId);
        application.add(name);
        application.add(providerId);
        application.add(toNap);


        // NAPDEF
        Characteristic napDef = new Characteristic("NAPDEF");
        Parm napIp            = new Parm("NAPID", "TEST");
        Parm bearer           = new Parm("BEARER", "22");
        Parm napAddress       = new Parm("NAP-ADDRESS", "nap-address");

        napDef.add(napIp);
        napDef.add(bearer);
        napDef.add(napAddress);

        // adds characteristic to the main document
        wapProvisioningDoc.addCharacteristic(application);
        wapProvisioningDoc.addCharacteristic(napDef);

        return wapProvisioningDoc;
    }

    private static ManagementRemote getManagementBeanInstance()
    throws ServerFailureException {

        ManagementRemote remote;

        InitialContext ctx;

         try {
             Hashtable props = new Hashtable();
             
             props.put(Context.URL_PKG_PREFIXES,        CONTEXT_URL_PREFIXES);

             ctx        = new InitialContext(props);
             remote = (ManagementRemote)ctx.lookup(MANAGEMENT_EJB_JNDI_NAME);

        } catch (Exception e) {
            e.printStackTrace();
            throw new ServerFailureException(e);
        }

        return remote;
    }
}
