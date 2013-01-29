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

package com.funambol.notification.client;


import java.util.Hashtable;

import javax.naming.InitialContext;
import javax.naming.Context;

import com.funambol.framework.server.error.ServerFailureException;

import com.funambol.server.engine.dm.ejb.ManagementRemote;



/**
 *
 * @version $Id: Notification.java,v 1.4 2007-06-18 16:38:45 luigiafassina Exp $
 */
public class Notification {

    public static final String MANAGEMENT_EJB_JNDI_NAME
    = "ejb:FunambolDMServer/funambol-server.jar//ManagementBean!com.funambol.server.engine.dm.ejb.ManagementRemote";
    
    public static final String CONTEXT_URL_PREFIXES
         = "org.jboss.ejb.client.naming";

    public static void main(String[] args) throws Exception {
        System.out.println("Lookup management bean");

        if (args.length != 2) {
            System.out.println("Use com.funambol.notification.client.Notification <phoneNumber> <operation>");
            System.exit(-1);
        }

        String phoneNumber = args[0];
        String operation = args[1];

        ManagementRemote remote = getManagementBeanInstance();
        System.out.println("Sending notification");
        remote.executeManagementOperation(phoneNumber, operation, "1");
        System.out.println("Notification sent");
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
            throw new ServerFailureException(e);
        }

        return remote;
    }
}
