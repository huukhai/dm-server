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

package com.funambol.server.admin.ejb;


import com.funambol.framework.server.SyncUser;
import com.funambol.framework.server.Sync4jDevice;
import com.funambol.framework.server.store.Clause;
import com.funambol.framework.config.Configuration;
import com.funambol.framework.security.Sync4jPrincipal;
import com.funambol.framework.engine.source.SyncSource;
import com.funambol.framework.server.error.ServerException;
import com.funambol.server.admin.AdminException;
import com.funambol.framework.server.Sync4jModule;

import javax.ejb.Remove;


/**
 * This is the Bussiness interface for AdminBean.
 *
 *
 * @version $Id: AdminLocal.java,v 1.2 2006/08/07 21:09:24 nichele Exp $
 *
 */
public interface AdminBusiness {

    /**
     * CONFIGURATION
     */
    public Configuration getConfig();

    /**
     * USER
     */
    public String[] getRoles()
    throws ServerException, AdminException;

    public SyncUser[] getUsers(Clause clause)
    throws ServerException, AdminException;

    public void insertUser(SyncUser u)
    throws ServerException, AdminException;

    public void setUser(SyncUser u)
    throws ServerException, AdminException;

    public void deleteUser(String userName)
    throws ServerException, AdminException;

    public void importUser(SyncUser u)
    throws ServerException, AdminException;

    public int countUsers(Clause clause)
    throws ServerException, AdminException;


    /**
     * DEVICE
     */
    public Sync4jDevice[] getDevices(Clause clause)
    throws ServerException, AdminException;

    public String insertDevice(Sync4jDevice d)
    throws ServerException, AdminException;

    public void setDevice(Sync4jDevice d)
    throws ServerException, AdminException;

    public void deleteDevice(String deviceId)
    throws ServerException, AdminException;

    public int countDevices(Clause clause)
    throws ServerException, AdminException;


    /**
     * PRINCIPAL
     */
    public Sync4jPrincipal[] getPrincipals(Clause clause)
    throws ServerException, AdminException;

    public String insertPrincipal(Sync4jPrincipal p)
    throws ServerException, AdminException;

    public void deletePrincipal(String principalId)
    throws ServerException, AdminException;

    public int countPrincipals(Clause clause)
    throws ServerException, AdminException;


    /**
     * MODULE
     */
    public Sync4jModule[] getModulesName()
    throws ServerException, AdminException;

    public Sync4jModule getModule(String moduleId)
    throws ServerException, AdminException;

    public void insertModule(Sync4jModule syncModule, byte[] filess4j)
    throws ServerException, AdminException;

    public void deleteModule(String moduleId)
    throws ServerException, AdminException;

    /**
     * SOURCE
     */
    public void insertSource(String moduleId, String connectorId, String sourceTypeId, SyncSource source)
    throws ServerException, AdminException;

    public void setSource(String moduleId, String connectorId, String sourceTypeId, SyncSource source)
    throws ServerException, AdminException;

    public void deleteSource(String sourceUri)
    throws ServerException, AdminException;

     /**
      *  User call this method to initialize AdminBean
      * @exception ServerException
     */
    public void initBussiness() throws ServerException;

     /**
      * User call this method to remove AdminBean
      * @see javax.ejb.Remove
     */
    @Remove
    public void remove();
}
