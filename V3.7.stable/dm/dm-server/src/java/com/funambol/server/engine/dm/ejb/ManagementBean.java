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

package com.funambol.server.engine.dm.ejb;


import javax.naming.InitialContext;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Stateless;

import com.funambol.framework.config.Configuration;
import com.funambol.framework.engine.dm.ManagementException;
import com.funambol.framework.notification.NotificationException;
import com.funambol.framework.notification.NotificationConstants;
import com.funambol.framework.core.dm.bootstrap.BootStrap;

import com.funambol.server.engine.dm.ManagementEngine;
import com.funambol.server.admin.ejb.AdminLocal;

import org.jboss.logging.Logger;


/**
 * Management ejb - Bean class
 *
 * @version $Id: ManagementBean.java,v 1.4 2006/11/15 15:28:41 nichele Exp $
 */
@Stateless
public class ManagementBean
implements ManagementLocal, ManagementRemote {

    // ------------------------------------------------------- Private constants

    private static final String ADMINEJB_JNDI_NAME
        = "java:module/AdminBean!com.funambol.server.admin.ejb.AdminLocal";
    
    // ------------------------------------------------------------ Private data

    private transient static final Logger log = Logger.getLogger(com.funambol.server.engine.dm.ejb.ManagementBean.class.getName());

    // ------------------------------------------------------------- EJB methods

    /**
     *
     * @see javax.ejb.PostActivate
     */
    @PostActivate
    public void postActivate() {

    }

    /**
     *
     * @see javax.ejb.PrePassivate
     */
    @PrePassivate
    public void prePassivate() {

    }

    /**
     * Performs a bootstrap with the given bootstrap object
     *
     * @param bootstrap the bootstrap object with the neeed information to perfom
     *                  a bootstrap
     * @throws NotificationException if a error occurs
     */
    @Override
    public void bootstrap(BootStrap bootstrap)
    throws ManagementException, NotificationException {
        Configuration conf = loadConfiguration();

        ManagementEngine mngtEngine = new ManagementEngine(conf);

        mngtEngine.bootstrap(bootstrap);
    }

    /**
     * Performs a bootstrap with the given bootstrap objects
     *
     * @param bootstraps the bootstrap array with the neeed information to perfom
     *                  the bootstraps
     * @throws NotificationException if a error occurs
     */
    @Override
    public void bootstrap(BootStrap[] bootstraps)
    throws ManagementException, NotificationException {
        Configuration conf = loadConfiguration();

        ManagementEngine mngtEngine = new ManagementEngine(conf);

        mngtEngine.bootstrap(bootstraps);
    }

    /**
     * Sends a notification message to the device with the given phoneNumber
     * @param messageType the type of the notification message as define in <code>NotificationConstants<code>
     * @param transportType the type of the transport as define in <code>NotificationConstants<code>
     * @param phoneNumber the phone number
     * @param operation application specific operation to be performed
     * @param info application specific detail information
     *
     * @throws ManagementException if a error occurs in the management engine,
     *         NotificationException if an error occurs in the notification process
     */
    @Override
    public void sendNotification( int    messageType  ,
                                  int    transportType,
                                  String phoneNumber  ,
                                  String operation    ,
                                  String info         )
    throws ManagementException, NotificationException {

        Configuration conf = loadConfiguration();

        ManagementEngine mngtEngine = new ManagementEngine(conf);

        mngtEngine.sendNotification(
            messageType,
            transportType,
            phoneNumber,
            operation,
            info
        );
    }

    /**
     * Executes the management sequence identified by the given operation name.
     *
     * @param phoneNumber the phone number
     * @param operation the management operation name
     * @param info application specific detail information
     *
     * @throws ManagementException if a error occurs in the management engine,
     *         NotificationException if an error occurs in the notification process
     */
    @Override
    public void executeManagementOperation( String phoneNumber  ,
                                            String operation    ,
                                            String info         )
    throws ManagementException, NotificationException {

        sendNotification(
            NotificationConstants.NOTIFICATION_MESSAGE_TYPE_GENERIC,
            NotificationConstants.TRANSPORT_TYPE_WAP,
            phoneNumber,
            operation,
            info
        );
    }



    // --------------------------------------------------------- Private methods

    /**
     * Load the configuration through the management bean
     * @throws ManagementException if a error occurs
     * @return Configuration
     */
    private Configuration loadConfiguration()
    throws ManagementException {

        Configuration config = null;
        try {
            InitialContext ctx = new InitialContext();

            AdminLocal adminLocal = (AdminLocal)ctx.lookup(ADMINEJB_JNDI_NAME);

            config = adminLocal.getConfig();

        } catch (Exception e) {
            log.debug("loadConfiguration", e);

            throw new ManagementException("Error reading the configuration", e);
        }
        return config;
    }



}
