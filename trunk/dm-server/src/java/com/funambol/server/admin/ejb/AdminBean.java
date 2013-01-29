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

import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.ejb.Init;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Remove;
import javax.ejb.Stateful;

import javax.annotation.PostConstruct;

import com.funambol.framework.config.ConfigClassLoader;
import com.funambol.framework.config.Configuration;
import com.funambol.framework.config.ConfigurationConstants;
import com.funambol.framework.config.ConfigurationException;
import com.funambol.framework.engine.source.SyncSource;
import com.funambol.framework.engine.source.SyncSourceException;
import com.funambol.framework.security.Sync4jPrincipal;
import com.funambol.framework.server.Sync4jConnector;
import com.funambol.framework.server.Sync4jDevice;
import com.funambol.framework.server.Sync4jModule;
import com.funambol.framework.server.Sync4jSourceType;
import com.funambol.framework.server.SyncUser;
import com.funambol.framework.server.error.ServerException;
import com.funambol.framework.server.store.Clause;
import com.funambol.framework.server.store.ConfigPersistentStoreException;
import com.funambol.framework.server.store.LogicalClause;
import com.funambol.framework.server.store.PersistentStore;
import com.funambol.framework.server.store.PersistentStoreException;
import com.funambol.framework.server.store.WhereClause;
import com.funambol.framework.tools.beans.BeanException;
import com.funambol.framework.tools.beans.BeanFactory;

import com.funambol.server.admin.AdminException;
import com.funambol.server.admin.DBUserManager;
import com.funambol.server.engine.Sync4jSource;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;


/**
 *  This is the session enterprise java bean that handles the administration
 * console. It is designed to be a stateless session bean.
 *  <p>
 *  This server accepts the requests addressed to the hostname
 *  indicated by the configuration property pointed by {CFG_SERVER_URI} (see
 *  Funambol.properties).
 *  <p>
 *  AdminBean uses the system property funambol.dm.home to set the base of the
 *  config path. The complete config path will be $funambol.dm.home/config/com/funambol.
 *
 * @version $Id: AdminBean.java,v 1.3 2006/08/07 21:09:24 nichele Exp $
 *
 */
@Stateful
public class AdminBean
implements ConfigurationConstants, AdminLocal, AdminRemote {
    // ------------------------------------------------------- Private constants

    private static final String CONFIG_PATH_PREFIX = File.separator
                                                   + "config"
                                                   ;
    private static final String SERVER_CONFIG_FILE    = "Funambol.properties";

    private static final String OPT_INSERT     = "INSERT";
    private static final String OPT_UPDATE     = "UPDATE";

    private static final String SEARCH_COUNT_DEVICES    = "SCD";
    private static final String SEARCH_COUNT_PRINCIPALS = "SCP";

    private static final String PROPERTY_CONFIG_PATH = "funambol.dm.home";

    //This role is used only for authentication
    private static final String ROLE_SPECIAL = "special_sync_admin";

    // ------------------------------------------------------------ Private data

    private transient static final Logger log = Logger.getLogger(com.funambol.server.admin.ejb.AdminBean.class.getName());

    private Configuration config                = null;

    private PersistentStore ps                  = null;

    private DBUserManager dbUserManager         = null;

    private URL configPath                      = null;

    // ------------------------------------------------------------- EJB methods
    /**
     *
     * @see javax.ejb.Init
     */
    @Init
    public void init() {

    }

   /**
     *
     * @see javax.ejb.Init
     */
    @PostConstruct
    @Override
    public void initBussiness()
            throws ServerException {

        loadConfiguration();

        //
        // Set the underlying persistent store
        //
        HashMap<String, Object> psConfig = new HashMap<String, Object>(1);
        psConfig.put("class-loader", config.getClassLoader());
        ps = (PersistentStore)config.getBeanInstance(CFG_PERSISTENT_STORE);
        try {
            ps.configure(psConfig);
        } catch (ConfigPersistentStoreException e) {
            if(log.isEnabled(Level.FATAL)){
                log.fatal("Error configuring the persistent store: " + e.getMessage());
            }

            log.debug("<init>", e);

            String msg = "Error "    + e.getClass().getName()
                                     + " creating the AdminBean: "
                                     + e.getMessage();

            throw new ServerException( msg, e);
        }

        dbUserManager = (DBUserManager)config.getBeanInstance(CFG_USER_MANAGER);
    }

    @Override
    public Configuration getConfig() {
        return config;
    }

    /**
     *
     * @see com.funambol.server.admin.ejb.AdminBusiness
     */
    @Remove
    @Override
    public void remove() {

    }

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
     * Read the list of roles available.
     *
     * @return names of roles available
     *
     * @throws ServerException
     * @throws AdminException
     */
    @Override
    public String[] getRoles()
    throws ServerException, AdminException {
        if(log.isEnabled(Level.TRACE)){
            log.trace("AdminBean method getRoles");
        }

        String[] roles = null;

        try {

            roles = dbUserManager.getRoles();

            if (roles != null) {
                ArrayList<String> lst = new ArrayList<String>();
                int size = roles.length;
                for (int i=0; i<size; i++) {
                    String role = roles[i];
                    if(!role.startsWith(ROLE_SPECIAL)){
                        lst.add(role);
                    }
                }
                roles = lst.toArray(new String[0]);
            }

        } catch (PersistentStoreException e) {
            String msg = "Error reading roles: " + e.getMessage();

            if (log.isEnabled(Level.FATAL)) {
                log.fatal(msg);
            }
            log.debug("getRoles", e);

            throw new ServerException(msg, e);
        }
        return roles;
    }

    /**
     * Read all users that satisfy the parameter of search.
     *
     * @param clause array of conditions for the query
     *
     * @return array of SyncUser
     *
     * @throws ServerException
     * @throws AdminException
     */
    @Override
    public SyncUser[] getUsers(Clause clause)
    throws ServerException, AdminException {
        if(log.isEnabled(Level.TRACE)){
            log.trace("AdminBean method getUsers");
        }

        SyncUser[] users = null;
        try {

            users = dbUserManager.getUsers(clause);

            for (int i=0; (users != null) && i<users.length; i++) {
                dbUserManager.getUserRoles(users[i]);
            }

        } catch (PersistentStoreException e) {
            String msg = "Error reading Users: " + e.getMessage();

            if (log.isEnabled(Level.FATAL)) {
                log.fatal(msg);
            }

            log.debug("getUsers", e);

            throw new ServerException(msg, e);
        }

        return users;
    }

    /**
     * Insert a new user and the assigned role to it
     *
     * @param user the user to insert
     *
     * @throws ServerException
     * @throws AdminException
     */
    @Override
    public void insertUser(SyncUser user)
    throws ServerException, AdminException {
        if(log.isEnabled(Level.TRACE)){
            log.trace("AdminBean method insertUser");
        }

        try {

            dbUserManager.insertUser(user);

        } catch (PersistentStoreException e) {
            String msg = "Error inserting User: " + e.getMessage();

            if (log.isEnabled(Level.FATAL)) {
                log.fatal(msg);
            }

            log.debug("insertUser", e);

            throw new ServerException(msg, e);
        }
    }

    /**
     * Update the information of the specific user
     *
     * @param user the user with informations updated
     *
     * @throws ServerException
     * @throws AdminException
     */
    @Override
    public void setUser(SyncUser user)
    throws ServerException, AdminException {
        if(log.isEnabled(Level.TRACE)){
            log.trace("AdminBean method setUser");
        }

        try {

            dbUserManager.setUser(user);

        } catch (PersistentStoreException e) {
            String msg = "Error updating User: " + e.getMessage();

            if (log.isEnabled(Level.FATAL)) {
                log.fatal(msg);
            }

            log.debug("setUser", e);

            throw new ServerException(msg, e);
        }
    }

    /**
     * Delete the user
     *
     * @param userName the name of user to delete
     *
     * @throws ServerException
     * @throws AdminException
     */
    @Override
    public void deleteUser(String userName)
    throws ServerException, AdminException {
        if(log.isEnabled(Level.TRACE)){
            log.trace("AdminBean method deleteUser");
        }

        try {
            SyncUser user = new SyncUser(userName,null,null,null,null,null);
            dbUserManager.deleteUser(user);

        } catch (PersistentStoreException e) {
            String msg = "Error deleting User: " + e.getMessage();

            if (log.isEnabled(Level.FATAL)) {
                log.fatal(msg);
            }

            log.debug("deleteUser", e);

            throw new ServerException(msg, e);
        }
    }

    @Override
    public void importUser(SyncUser user)
    throws ServerException, AdminException {
        if(log.isEnabled(Level.TRACE)){
            log.trace("AdminBean method importUser");
        }

        throw new AdminException("Not implemented yet");
    }

    /**
     * Count the number of users that satisfy the specif clauses.
     *
     * @param clause the conditions of the search
     *
     * @return number of users
     *
     * @throws ServerException
     * @throws AdminException
     */
    @Override
    public int countUsers(Clause clause)
    throws ServerException, AdminException {
        if(log.isEnabled(Level.TRACE)){
            log.trace("AdminBean method countUsers");
        }

        int n = 0;

        try {

            n = dbUserManager.countUsers(clause);

        } catch (PersistentStoreException e) {
            String msg = "Error counting users: " + e.getMessage();

            if (log.isEnabled(Level.FATAL)) {
                log.fatal(msg);
            }

            log.debug("countUsers", e);

            throw new ServerException(msg, e);
        }
        return n;
    }

    /**
     * Read a list of device that satisfy the specific conditions.
     *
     * @param clauses the conditions foe search informations.
     *
     * @return array of device
     *
     * @throws ServerException
     * @throws AdminException
     */
    @Override
    public Sync4jDevice[] getDevices(Clause clauses)
    throws ServerException, AdminException {
        if(log.isEnabled(Level.TRACE)){
            log.trace("AdminBean method getDevices");
        }

        Sync4jDevice[] devices = null;

        try {

            devices = (Sync4jDevice[])ps.read(new Sync4jDevice(), clauses);

        } catch (PersistentStoreException e) {
            String msg = "Error reading devices: " + e.getMessage();

            if (log.isEnabled(Level.FATAL)) {
                log.fatal(msg);
            }

            log.debug("getDevices", e);

            throw new ServerException(msg, e);
        }
        return devices;
    }

    /**
     * Insert a new device.
     *
     * @param d the new device
     *
     * @return the device id
     *
     * @throws ServerException
     * @throws AdminException
     */
    @Override
    public String insertDevice(Sync4jDevice d)
    throws ServerException, AdminException {
        if(log.isEnabled(Level.TRACE)){
            log.trace("AdminBean method insertDevice");
        }

        try {

            ps.store(d);

        } catch (PersistentStoreException e) {
            String msg = "Error adding device: " + e.getMessage();

            if (log.isEnabled(Level.FATAL)) {
                log.fatal(msg);
            }

            log.debug("insertDevice", e);

            throw new ServerException(msg, e);
        }
        return d.getDeviceId();
    }

    /**
     * Update the information of specific device.
     *
     * @param d the device to update
     *
     * @throws ServerException
     * @throws AdminException
     */
    @Override
    public void setDevice(Sync4jDevice d)
    throws ServerException, AdminException {
        if(log.isEnabled(Level.TRACE)){
            log.trace("AdminBean method setDevice");
        }

        try {

            ps.store(d);

        } catch (PersistentStoreException e) {
            String msg = "Error updating device: " + e.getMessage();

            if (log.isEnabled(Level.FATAL)) {
                log.fatal(msg);
            }

            log.debug("setDevice", e);

            throw new ServerException(msg, e);
        }
    }

    /**
     * Delete the specific device.
     *
     * @param deviceId the device id that identifies the device
     *
     * @throws ServerException
     * @throws AdminException
     */
    @Override
    public void deleteDevice(String deviceId)
    throws ServerException, AdminException{
        if(log.isEnabled(Level.TRACE)){
            log.trace("AdminBean method deleteDevice");
        }

        try {
            Sync4jDevice sd = new Sync4jDevice(deviceId, null, null);
            ps.delete(sd);

        } catch (PersistentStoreException e) {
            String msg = "Error deleting device: " + e.getMessage();

            if (log.isEnabled(Level.FATAL)) {
                log.fatal(msg);
            }

            log.debug("deleteDevice", e);

            throw new ServerException(msg, e);
        }
    }

    /**
     * Count the number of device that satisfy the specific clauses.
     *
     * @param clauses the specific conditions for search device
     *
     * @return the number of device finds
     *
     * @throws ServerException
     * @throws AdminException
     */
    @Override
    public int countDevices(Clause clauses)
    throws ServerException, AdminException {
        if(log.isEnabled(Level.TRACE)){
            log.trace("AdminBean method countDevices");
        }

        int n = 0;

        try {

            String[] obj = (String[])ps.read(SEARCH_COUNT_DEVICES, clauses);
            n = Integer.parseInt(obj[0]);

        } catch (PersistentStoreException e) {
            String msg = "Error counting devices: " + e.getMessage();

            if (log.isEnabled(Level.FATAL)) {
                log.fatal(msg);
            }

            log.debug("countDevices", e);

            throw new ServerException(msg, e);
        }
        return n;
    }

    /**
     * Read all principal that satisfy the clauses.
     *
     * @param clauses the specific conditions for search principal
     *
     * @return array of principal
     *
     * @throws ServerException
     * @throws AdminException
     */
    @Override
    public Sync4jPrincipal[] getPrincipals(Clause clauses)
    throws ServerException, AdminException {
        if(log.isEnabled(Level.TRACE)){
            log.trace("AdminBean method getPrincipals");
        }

        Sync4jPrincipal[] principals = null;

        try {

            principals = (Sync4jPrincipal[])ps.read(new Sync4jPrincipal(null,null,null), clauses);

        } catch (PersistentStoreException e) {
            String msg = "Error reading principals: " + e.getMessage();

            if (log.isEnabled(Level.FATAL)) {
                log.fatal(msg);
            }

            log.debug("getPrincipals", e);

            throw new ServerException(msg, e);
        }
        return principals;
    }

    /**
     * Insert a new principal.
     *
     * @param p the new principal
     *
     * @return the principal id
     *
     * @throws ServerException
     * @throws AdminException
     */
    @Override
    public String insertPrincipal(Sync4jPrincipal p)
    throws ServerException, AdminException {
        if(log.isEnabled(Level.TRACE)){
            log.trace("AdminBean method insertPrincipal");
        }

        try {

            ps.store(p);

        } catch (PersistentStoreException e) {
            String msg = "Error adding rincipal: " + e.getMessage();

            if (log.isEnabled(Level.FATAL)) {
                log.fatal(msg);
            }

            log.debug("insertPrincipal", e);

            throw new ServerException(msg, e);
        }
        return p.getId();
    }

    /**
     * Delete the specific principal.
     *
     * @param principalId the principal id that identifies the principal
     *
     * @throws ServerException
     * @throws AdminException
     */
    @Override
    public void deletePrincipal(String principalId)
    throws ServerException, AdminException {
        if(log.isEnabled(Level.TRACE)){
            log.trace("AdminBean method deletePrincipal");
        }

        try {
            Sync4jPrincipal sp = new Sync4jPrincipal(principalId, null, null);
            ps.delete(sp);

        } catch (PersistentStoreException e) {
            String msg = "Error deleting principal: " + e.getMessage();

            if (log.isEnabled(Level.FATAL)) {
                log.fatal(msg);
            }

            log.debug("deletePrincipal", e);

            throw new ServerException(msg, e);
        }
    }

    /**
     * Count the number of principal that satisfy the specific clauses.
     *
     * @param clauses the specific conditions for search principal
     *
     * @return the number of principal finds
     *
     * @throws ServerException
     * @throws AdminException
     */
    @Override
    public int countPrincipals(Clause clauses)
    throws ServerException, AdminException {
        if(log.isEnabled(Level.TRACE)){
            log.trace("AdminBean method countPrincipals");
        }

        int n = 0;

        try {

            String[] obj = (String[])ps.read(SEARCH_COUNT_PRINCIPALS, clauses);
            n = Integer.parseInt(obj[0]);

        } catch (PersistentStoreException e) {
            String msg = "Error counting principals: " + e.getMessage();

            if (log.isEnabled(Level.FATAL)) {
                log.fatal(msg);
            }

            log.debug("countPrincipals", e);

            throw new ServerException(msg, e);
        }
        return n;
    }

    /**
     * Read all modules information
     *
     * @return an array with the modules information (empty if no objects are found)
     *
     * @throws ServerException
     * @throws AdminException
     */
    @Override
    public Sync4jModule[] getModulesName()
    throws ServerException, AdminException {
        if(log.isEnabled(Level.TRACE)){
            log.trace("AdminBean method getModulesName");
        }

        Sync4jModule[] module = null;

        try {

            module = (Sync4jModule[])ps.read(Sync4jModule.class);

        } catch (PersistentStoreException e) {
            String msg = "Error reading modules: " + e.getMessage();

            if (log.isEnabled(Level.TRACE)) {
                log.fatal(msg);
            }

            log.debug("getPrincipals", e);

            throw new ServerException(msg, e);
        }

        return module;
    }

    /**
     * Read the information of the specific module
     *
     * @param moduleId the module id that identifies the module to search
     *
     * @return the relative information to the module
     *
     * @throws ServerException
     * @throws AdminException
     */
    @Override
    public Sync4jModule getModule(String moduleId)
    throws ServerException, AdminException {
        if(log.isEnabled(Level.TRACE)){
            log.trace("AdminBean method getModule");
        }

        Sync4jModule module = null;

        try {

            module = new Sync4jModule(moduleId, null, null);
            ps.read(module);

            //for each SyncConnector read the SyncSourceType and
            //for each SyncSourceType read the SyncSource
            Sync4jConnector[] syncConnectors = module.getConnectors();
            for (int i=0; (syncConnectors != null) && i<syncConnectors.length; i++) {
                Sync4jConnector sc = syncConnectors[i];
                Sync4jSourceType[] syncSourceTypes = sc.getSourceTypes();

                for (int y=0; (syncSourceTypes != null) && y<syncSourceTypes.length; y++) {
                    Sync4jSource[] sync4jSources = (Sync4jSource[])ps.read(syncSourceTypes[y], null);

                    ArrayList<SyncSource> syncSources = new ArrayList<SyncSource>();
                    ArrayList<SyncSourceException> syncSourcesFailed = new ArrayList<SyncSourceException>();

                    for (int z=0; z<sync4jSources.length; z++) {

                        try {
                            SyncSource syncSource = (SyncSource)BeanFactory.getNoInitBeanInstance(
                                                 config.getClassLoader(),
                                                 sync4jSources[z].getConfig()
                                           );
                            syncSources.add(syncSource);

                        } catch (BeanException e) {
                            Throwable t = e.getCause();

                            String msg = "Error creating SyncSource "
                                       + module.getModuleName()
                                       + "/"
                                       + sc.getConnectorName()
                                       + "/"
                                       + syncSourceTypes[y].getDescription()
                                       + "/"
                                       + sync4jSources[z].getUri();

                            if ( t != null)
                                msg += ": " + t.getMessage();

                            log.debug("getModule", t);

                            syncSourcesFailed.add(
                                new SyncSourceException(
                                    sync4jSources[z].getUri(),
                                    sync4jSources[z].getConfig(),
                                    t)
                                );
                        }
                    }

                    SyncSource[] syncSourcesOK = syncSources.toArray(new SyncSource[syncSources.size()]);
                    syncSourceTypes[y].setSyncSources(syncSourcesOK);

                    SyncSourceException[] syncSourcesNO = syncSourcesFailed.toArray(new SyncSourceException[syncSourcesFailed.size()]);
                    syncSourceTypes[y].setSyncSourcesFailed(syncSourcesNO);
                }
            }

        } catch (PersistentStoreException e) {
            String msg = "Error getting module: " + e.getMessage();

            if (log.isEnabled(Level.FATAL)) {
                log.fatal("Error getting Module: " + e.getMessage());
            }

            log.debug("getModule", e);

            throw new ServerException(msg, e);
        }

        return module;
    }

    @Override
    public void insertModule(Sync4jModule module, byte[] filess4j)
    throws ServerException, AdminException {
        if(log.isEnabled(Level.TRACE)){
            log.trace("AdminBean method insertModule");
        }

        throw new AdminException("Not implemented yet");
    }

    @Override
    public void deleteModule(String moduleId)
    throws ServerException, AdminException {
        if(log.isEnabled(Level.TRACE)){
            log.trace("AdminBean method deleteModule");
        }

        throw new AdminException("Not implemented yet");
    }

    /**
     * Insert a new source into datastore and create a relative file xml with configuration.
     * The source must have a defined source type.
     * The source type must refer to a connector.
     * The connector must refer to a module.
     *
     * @param moduleId the module id
     * @param connectorId the connector id
     * @param sourceTypeId the source type id
     * @param source the information of the new source
     *
     * @throws ServerException
     * @throws AdminException
     */
    @Override
    public void insertSource(String moduleId, String connectorId, String sourceTypeId, SyncSource source)
    throws ServerException, AdminException {
        if(log.isEnabled(Level.TRACE)){
            log.trace("AdminBean method insertSource");
        }

        String uri = source.getSourceURI();
        String sourceName = source.getName();

        String config2 = moduleId+File.separator+connectorId+File.separator+sourceTypeId;

        Sync4jSource s4j = new Sync4jSource(uri,config2+File.separator+sourceName+".xml",sourceTypeId,sourceName);

        //
        //checking for the existance of the source before inserting
        //
        Sync4jSource existSource[] = null;
        try {

            WhereClause[] wc = new WhereClause[2];
            String value[] = new String[1];
            value[0] = uri;
            wc[0] = new WhereClause("uri",value, WhereClause.OPT_EQ, false);

            value[0] = sourceName;
            wc[1] = new WhereClause("name",value, WhereClause.OPT_EQ, false);
            LogicalClause lc = new LogicalClause(LogicalClause.OPT_OR, wc);

            existSource = (Sync4jSource[])ps.read(s4j, lc);

        } catch (PersistentStoreException e) {
            String msg = "Error reading sources existing: " + e.getMessage();

            if (log.isEnabled(Level.FATAL)) {
                log.fatal(msg);
            }

            log.debug("insertSource", e);

            throw new ServerException(msg, e);
        }

        if (existSource == null || existSource.length == 0) {
            try {

                ps.store(sourceTypeId, s4j, OPT_INSERT);

            } catch (PersistentStoreException e) {
                String msg = "Error adding the SyncSource: " + e.getMessage();

                if (log.isEnabled(Level.FATAL)) {
                    log.fatal(msg);
                }

                log.debug("insertSource", e);

                throw new ServerException(msg, e);
            }
        } else {
            String msg = "A SyncSource with URI "
                        + uri + " or with Name " + sourceName
                        + " is already present.";

            throw new AdminException(msg);
        }

        try {

            String path = configPath + config2;
            if (path.startsWith("file:")) path = path.substring(6);

            File f = new File(path);
            f.mkdirs();

            XMLEncoder encoder = null;
            encoder = new XMLEncoder(new FileOutputStream(path+File.separator+sourceName+".xml"));
            encoder.writeObject((Object)source);
            encoder.flush();
            encoder.close();

        } catch(FileNotFoundException e) {
            String msg = "Error storing the SyncSource on file system: " + e.getMessage();

            if (log.isEnabled(Level.FATAL)) {
                log.fatal(msg);
            }

            log.debug("insertSource", e);

            throw new ServerException(msg, e);
        }
    }

    /**
     * Update a specific source into datastore and create a relative file xml with configuration.
     * The source must have a defined source type.
     * The source type must refer to a connector.
     * The connector must refer to a module.
     *
     * @param moduleId the module id
     * @param connectorId the connector id
     * @param sourceTypeId the source type id
     * @param source the information of the new source
     *
     * @throws ServerException
     * @throws AdminException
     */
    @Override
    public void setSource(String moduleId, String connectorId, String sourceTypeId, SyncSource source)
    throws ServerException, AdminException{
        if(log.isEnabled(Level.TRACE)){
            log.trace("AdminBean method setSource");
        }

        String uri = source.getSourceURI();
        String sourceName = source.getName();

        String config2 = moduleId
                      + File.separator
                      + connectorId
                      + File.separator
                      + sourceTypeId
                      ;

        Sync4jSource s4j = new Sync4jSource(uri,null,sourceTypeId,null);

        try {

            ps.read(s4j);

        } catch (PersistentStoreException e) {
            String msg = "Error reading sources existing: " + e.getMessage();

            if (log.isEnabled(Level.FATAL)) {
                log.fatal(msg);
            }

            log.debug("insertSource", e);

            throw new ServerException(msg, e);
        }

        s4j.setSourceName(sourceName);

        String nameFileXml = s4j.getConfig().substring(config2.length() + 1);

        try {

            ps.store(sourceTypeId, s4j, OPT_UPDATE);

        } catch (PersistentStoreException e) {
            String msg = "Error storing SyncSource: " + e.getMessage();

            if (log.isEnabled(Level.FATAL)) {
                log.fatal(msg);
            }

            log.debug("insertSource", e);

            throw new ServerException(msg, e);
        }

        try {

            String path = configPath + config2;
            if (path.startsWith("file:")) path = path.substring(6);

            File f = new File(path);
            f.mkdirs();

            XMLEncoder encoder = null;
            encoder = new XMLEncoder(new FileOutputStream(path+File.separator+nameFileXml));
            encoder.writeObject((Object)source);
            encoder.flush();
            encoder.close();

        } catch(FileNotFoundException e) {
            String msg = "Error storing SyncSource: " + e.getMessage();

            if (log.isEnabled(Level.FATAL)) {
                log.fatal(msg);
            }

            log.debug("insertSource", e);

            throw new ServerException(msg, e);
        }
    }

    /**
     * Delete a specific source and the relative file of configuration.
     *
     * @param sourceUri the uri that identifies the source
     *
     * @throws ServerException
     * @throws AdminException
     */
    @Override
    public void deleteSource(String sourceUri)
    throws ServerException, AdminException{
        if(log.isEnabled(Level.TRACE)){
            log.trace("AdminBean method deleteSource");
        }

        Sync4jSource s4j = new Sync4jSource(sourceUri,null,null,null);
        try {

            ps.read(s4j);

        } catch (PersistentStoreException e) {
            String msg = "Error reading source: " + e.getMessage();

            if (log.isEnabled(Level.FATAL)) {
                log.fatal(msg);
            }

            log.debug("deleteSource", e);

            throw new ServerException(msg, e);
        }

        try {
            ps.delete(s4j);

        } catch (PersistentStoreException e) {
            String msg = "Error deleting SyncSource: " + e.getMessage();

            if (log.isEnabled(Level.FATAL)) {
                log.fatal(msg);
            }

            log.debug("deleteSource", e);

            throw new ServerException(msg, e);
        }

        String path = configPath + s4j.getConfig();
        if (path.startsWith("file:")) path = path.substring(6);

        File f = new File(path);
        f.delete();
    }

    // --------------------------------------------------------- private methods

    /**
     * Loads the configuration for the server. The URI from which the configuration
     * is must be set as the envirnoment property named as <i>ENV_SERVER_CONFIG_URI</i>.
     *
     * @throws ConfigurationException in case of errors.
     */
    private void loadConfiguration()
    throws ConfigurationException {

        config = Configuration.getConfiguration();

        String propertiesFile = null;
        try {
            //
            // The following two entries should be two URIs, but in the case
            // they are not (they do not start with http://, https:// or file://,
            // they are considered simple files
            //
            configPath = fixURI((String)System.getProperty(PROPERTY_CONFIG_PATH, ".") + CONFIG_PATH_PREFIX);

            config.setClassLoader(
                new ConfigClassLoader(
                    new URL[] { configPath },
                    getClass().getClassLoader()
                )
            );

            propertiesFile =
                configPath.toString() + File.separatorChar + SERVER_CONFIG_FILE;
            config.load(propertiesFile);

            if (log.isEnabled(Level.TRACE)) {
                log.trace("Configuration: " + config);
            }
        } catch (ConfigurationException e) {
            throw e;
        } catch (Exception e) {
            throw new ConfigurationException("Error loading configuration from "
                                             + propertiesFile, e);
        }
    }

    /**
     * Checks if the given string is an accepted URL (starting with http://,
     * https:// or file://). If yes, a new URL object representing the given
     * url is returned; otherwise, the given string is considered a file name
     * and a new URL is obtained calling File.toURL().
     *
     * @param s the string to check
     *
     * @return the corresponding URL if the string represents a URL or the
     *         fixed URL if the string is a pathname/filename
     *
     * @throws MalformedURLException
     */
    private URL fixURI(final String s)
    throws MalformedURLException {
//        String minS = s.toLowerCase();

        try {
            return new URL(s);
        } catch (MalformedURLException e) {
            //
            // This is not a URL, let's consider it just a file
        }

        try {
            return new File(new File(s).getCanonicalPath()).toURI().toURL();

        } catch (IOException e) {
            throw new MalformedURLException("Unable to convert" + s + " to a URL");
        }
    }

}
