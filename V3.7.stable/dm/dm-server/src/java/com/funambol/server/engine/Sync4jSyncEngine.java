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
package com.funambol.server.engine;


import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.funambol.framework.engine.source.SyncSource;
import com.funambol.framework.security.Sync4jPrincipal;
import com.funambol.framework.tools.beans.BeanFactory;
import com.funambol.framework.database.Database;
import com.funambol.framework.config.Configuration;
import com.funambol.framework.config.ConfigurationConstants;
import com.funambol.framework.core.AlertCode;
import com.funambol.framework.core.Anchor;
import com.funambol.framework.core.CTCap;
import com.funambol.framework.core.ContentTypeInfo;
import com.funambol.framework.core.DSMem;
import com.funambol.framework.core.DataStore;
import com.funambol.framework.core.DevInf;
import com.funambol.framework.core.Ext;
import com.funambol.framework.core.ItemizedCommand;
import com.funambol.framework.core.ModificationCommand;
import com.funambol.framework.core.SourceRef;
import com.funambol.framework.core.Status;
import com.funambol.framework.core.StatusCode;
import com.funambol.framework.core.Sync4jException;
import com.funambol.framework.core.SyncCap;
import com.funambol.framework.core.SyncType;
import com.funambol.framework.core.VerDTD;
import com.funambol.framework.engine.SyncEngine;
import com.funambol.framework.engine.SyncException;
import com.funambol.framework.engine.SyncItem;
import com.funambol.framework.engine.SyncOperation;
import com.funambol.framework.engine.SyncOperationStatus;
import com.funambol.framework.engine.SyncStrategy;
import com.funambol.framework.server.ClientMapping;
import com.funambol.framework.server.LastTimestamp;
import com.funambol.framework.server.SyncTimestamp;
import com.funambol.framework.server.store.NotFoundException;
import com.funambol.framework.server.store.PersistentStoreException;

import org.jboss.logging.Logger.Level;


/**
 * This is the Funambol implementation of the synchronization engine.
 *
 * LOG NAME: funambol.engine
 *
 * @version $Id: Sync4jSyncEngine.java,v 1.5 2007-06-19 08:16:17 luigiafassina Exp $
 */
public class Sync4jSyncEngine
extends Sync4jEngine
implements java.io.Serializable, SyncEngine, ConfigurationConstants {

    // --------------------------------------------------------------- Constants

    // ------------------------------------------------------------ Private data

    /**
     * The sources this engine deals with
     */
    private ArrayList<Object> serverSources = new ArrayList<Object>();
    private ArrayList<SyncSource> clientSources = new ArrayList<SyncSource>();

    /**
     * The modification objects of the last synchronization
     */
    private java.util.Map operations = null;

    /**
     * The modification objects of the last synchronization
     */
    private SyncOperationStatus[] operationStatus = null;

    // ------------------------------------------------------------ Constructors

    /**
     * To allow deserializazion of subclasses.
     */
    protected Sync4jSyncEngine() {
    }

    /**
     * Creates a new instance of Sync4jEngine. <p>
     * NOTE: This is a sample implementation that deals with a single source on
     * the file system.
     *
     * @throws ConfigurationException a runtime exception in case of misconfiguration
     */
    public Sync4jSyncEngine(Configuration configuration) {
        super(configuration);

        //
        // Set SyncSources
        //
        Sync4jSource[] sources = null;

        try {
            sources = (Sync4jSource[])store.read(Sync4jSource.class);
        } catch (PersistentStoreException e) {
            if (log.isEnabled(Level.FATAL)) {
                log.fatal("Error reading registered sources: " + e.getMessage());
            }

            log.debug("<init>", e);
        }

        for (int i=0; ((sources != null) && (i<sources.length)); ++i) {
            if (log.isEnabled(Level.TRACE)) {
                log.trace("sources[" + i + "]: " + sources[i]);
            }

            try {
                serverSources.add(
                    BeanFactory.getBeanInstance(configuration.getClassLoader(),  sources[i].getConfig())
                );
            } catch (Exception e){
                String msg = "Unable to create sync source "
                           + sources[i]
                           + ": "
                           + e.getMessage();

                if(log.isEnabled(Level.FATAL)){
                    log.fatal(msg);
                }

                log.debug("<init>", e);
            }
        }


        //
        // Set the SyncStrategy object to use for comparisons
        //
        SyncStrategy strategy =
           (SyncStrategy)configuration.getClassInstance(CFG_STRATEGY_CLASS);

        setStrategy(strategy);

        if (log.isEnabled(Level.INFO)) {
            log.info("Engine configuration:");
            log.info("store: "    + store   );
            log.info("officer: "  + officer );
            log.info("strategy: " + strategy);
        }
    }

    // ------------------------------------------------------ Runtime properties

    /**
     * The underlying strategy.
     */
    private SyncStrategy strategy = null;

    /**
     * Get the underlying strategy
     *
     * @return the underlying strategy
     */
    public SyncStrategy getStrategy() {
        return this.strategy;
    }

    /**
     * Set the synchronization strategy to be used
     */
    public void setStrategy(SyncStrategy strategy) {
        this.strategy = strategy;
    }


    /**
     * The database to be synchronized
     */
    private java.util.Map<String, Database> dbs = new HashMap<String, Database>();

    public void setDbs(java.util.Map<String, Database> dbs) {
        if (this.dbs != null) {
            this.dbs.clear();
        } else {
            this.dbs = new HashMap<String, Database>(dbs.size());
        }
        this.dbs.putAll(dbs);
    }

    public void setDbs(Database[] dbs) {
        if (this.dbs == null) {
            this.dbs = new HashMap<String, Database>(dbs.length);
        }

        for (int i=0; ((dbs != null) && (i<dbs.length)); ++i) {
            this.dbs.put(dbs[i].getName(), dbs[i]);
        }
    }

    public Database[] getDbs() {
        if (this.dbs == null) {
            return new Database[0];
        }

        Iterator<Database> i = this.dbs.values().iterator();

        Database[] ret = new Database[this.dbs.size()];

        int j = 0;
        while(i.hasNext()) {
            ret[j++] = i.next();
        }

        return ret;
    }

    /**
     * The existing LUID-GUID mapping. It is used and modify by sync()
     */
    private java.util.Map<String, ClientMapping> clientMappings = new HashMap<String, ClientMapping>();

    public void setClientMappings(java.util.Map<String, ClientMapping> clientMappings) {
        this.clientMappings = clientMappings;
    }

    // ---------------------------------------------------------- Public methods

    /**
     * Fires and manages the synchronization process.
     *
     * @param principal the principal who is requesting the sync
     *
     * @throws Sync4jException in case of error
     */
    public void sync(Sync4jPrincipal principal)
    throws Sync4jException {
        log.info("Starting synchronization ...");

        SyncStrategy syncStrategy = getStrategy();

        SyncSource clientSource = null, serverSource = null;
        Database   db           = null;

        ArrayList<SyncOperationStatus> status = new ArrayList<SyncOperationStatus>();

        //
        // Create maps for server sources so that they can be accessed throught
        // their name
        //
        HashMap<String, SyncSource> sourceMap = new HashMap<String, SyncSource>(serverSources.size());

        Iterator<Object> s = serverSources.iterator();
        while(s.hasNext()) {
            serverSource = (SyncSource)s.next();
            sourceMap.put(serverSource.getSourceURI(), serverSource);
        }

        //
        // Now process clientSources
        //

        operations = new HashMap();

        String   uri = null;
        Iterator<SyncSource> c = clientSources.iterator();
        while(c.hasNext()) {
            clientSource = c.next();
            uri          = clientSource.getSourceURI();

            serverSource = sourceMap.get(uri);
            db           = dbs.get(clientSource.getSourceURI());

            SyncSource[] sources = new SyncSource[] {
                serverSource, clientSource
            };

            try {
                Sync4jPrincipal p = (Sync4jPrincipal)db.getPrincipal();
                p.setId(principal.getId());

                //
                // Call beginSync()
                //
                serverSource.beginSync(p, db.getMethod());

                if (   (db.getMethod() == AlertCode.SLOW)
                    || (db.getMethod() == AlertCode.REFRESH_FROM_SERVER)
                    || (db.getStatusCode() == StatusCode.REFRESH_REQUIRED)) {
                    operations.put(uri, syncStrategy.prepareSlowSync(sources, p));
                } else {
                    //
                    // Read the last timestamp from the persistent store, than
                    // prepare for fast synchronization
                    //
                    LastTimestamp last = new LastTimestamp(
                                             p.getId()    ,
                                             db.getName()
                                         );
                    try {
                        store.read(last);
                    } catch (PersistentStoreException e) {
                        throw new SyncException("Error reading last timestamp", e);
                    }
                    Timestamp since = new Timestamp(last.start);

                    operations.put(uri, syncStrategy.prepareFastSync(sources, p, since));
                }

                //
                // After processed the db if status code was "Refresh required"
                // then change that into StatusCode.OK
                //
                if (db.getStatusCode() == StatusCode.REFRESH_REQUIRED) {
                    db.setStatusCode(StatusCode.OK);
                }

                //
                // Now synchronize the sources
                //
                status.addAll(
                    Arrays.asList(
                        syncStrategy.sync((SyncOperation[])operations.get(uri))
                    )
                );

                operationStatus = status.toArray(new SyncOperationStatus[0]);

                //
                // Call endSync()
                //
                serverSource.endSync(p);

                log.info("Ending synchronization ...");

                syncStrategy.endSync();
            } catch (SyncException e) {
               log.debug("sync", e);

                throw new Sync4jException(e.getMessage(), e);
            }
        }  // next i (client source)
    }

    /**
     * Returns the operations of the last synchronization
     *
     * @param uri the URI of the source the operations are applied to
     *
     * @return the operations of the last synchronization
     */
    public SyncOperation[] getSyncOperations(String uri) {
        return (SyncOperation[])operations.get(uri);
    }

    /**
     * Returns the operation status of the operations executed in the last
     * synchronization
     *
     * @param msgId the id of the SyncML message
     *
     * @return the operations status of the operations executed in the last
               synchronization
     */
    public Status[] getModificationsStatusCommands(String msgId) {

        if ((operationStatus == null) || (operationStatus.length == 0)) {
            return new Status[0];
        }

        return EngineHelper.generateStatusCommands(
            operationStatus,
            msgId,
            cmdIdGenerator
        );
    }

    /**
     * Adds a new client source to the list of the sources the engine deals with.
     *
     * @param source the source to be added
     */
    public void addClientSource(SyncSource source) {
        if(log.isEnabled(Level.TRACE)){
            log.trace("adding " + source);
        }

        clientSources.add(source);
    }

    /**
     * Returns the client sources
     *
     * @return the client sources
     */
    public List<SyncSource> getClientSources() {
        return clientSources;
    }

    /**
     * Returns the client source with the given name if there is any.
     *
     * @param name the source name
     *
     * @return the client source with the given name if there is any, null otherwise
     */
    public SyncSource getClientSource(String name) {
        Iterator<SyncSource> i = clientSources.iterator();

        SyncSource s = null;
        while (i.hasNext()) {
            s = i.next();

            if (s.getSourceURI().equals(name)) {
                return s;
            }
        }

        return null;
    }

    /**
     * Returns the server source with the given name if there is any.
     *
     * @param name the source name
     *
     * @return the server source with the given name if there is any, null otherwise
     */
    public SyncSource getServerSource(String name) {
        Iterator<Object> i = serverSources.iterator();

        SyncSource s = null;
        while (i.hasNext()) {
            s = (SyncSource)i.next();

            if (s.getSourceURI().equals(name)) {
                return s;
            }
        }

        return null;
    }

    /**
     * Return the <i>DataStore</i> object corresponding to the given
     * <i>Database</i> object.
     *
     * @param database the database to convert
     *
     * @return the <i>DataStore</i> object corresponding to the given
     *         <i>Database</i> object.
     *
     */
    public DataStore databaseToDataStore(Database db) {
        ContentTypeInfo contentTypeInfo
            = new ContentTypeInfo(db.getType(), "-" /* version */);

        return new DataStore(
            new SourceRef(db.getSource())                          ,
            null                                                   ,
            -1                                                     ,
            contentTypeInfo                                        ,
            new ContentTypeInfo[] { contentTypeInfo }              ,
            contentTypeInfo                                        ,
            new ContentTypeInfo[] { contentTypeInfo }              ,
            new DSMem(false, -1, -1)                     ,
            new SyncCap(new SyncType[] { SyncType.SLOW })
        );
    }

    /**
     * Returns the content type capabilities of this sync engine.
     *
     * @return the content type capabilities of this sync engine.
     */
    public CTCap[] getContentTypeCapabilities() {
        return new CTCap[0];
    }

    /**
     * Returns the extensions of this sync engine.
     *
     * @return the extensions of this sync engine.
     */
    public Ext[] getExtensions() {
        return new Ext[0];
    }

    /**
     * Returns the data store capabilities of the sync sources to synchronize.
     *
     * @return the data store capabilities of the sync sources to synchronize.
     */
    public DataStore[] getDatastores() {
        DataStore[] ds = null;
        ArrayList<DataStore> al = new ArrayList<DataStore>();

        Database db  = null;
        String   uri = null;
        Iterator<Database> i = dbs.values().iterator();
        while(i.hasNext()) {
            db = i.next();
            uri = db.getName();

            SyncSource ss = getServerSource(uri);

            if ( ss != null) {
                al.add(EngineHelper.toDataStore(uri, ss.getInfo()));
            }
        }

        int size = al.size();
        if (size == 0) {
            ds = new DataStore[0];
        } else {
        ds = al.toArray(new DataStore[size]);
        }


        return ds;
    }


    /**
     * Creates and returns a <i>DeviceInfo</i> object with the information
     * extracted from the configuration object.
     *
     * @param verDTD the version of the DTD to use to encode the capabilities
     *
     * @return the engine capabilities
     *
     * @throws ConfigurationException a runtime exception in case of misconfiguration
     */
    public DevInf getServerCapabilities(VerDTD verDTD) {
        DevInf devInf =
        new DevInf(
            verDTD,
            configuration.getStringValue(CFG_ENGINE_MANIFACTURER, "Funambol"),
            configuration.getStringValue(CFG_ENGINE_MODELNAME, "Funambol"),
            configuration.getStringValue(CFG_ENGINE_OEM,          null    ),
            configuration.getStringValue(CFG_ENGINE_FWVERSION,    null    ),
            configuration.getStringValue(CFG_ENGINE_SWVERSION,    null    ),
            configuration.getStringValue(CFG_ENGINE_HWVERSION,    null    ),
            configuration.getStringValue(CFG_ENGINE_DEVICEID, "Funambol"),
            configuration.getStringValue(CFG_ENGINE_DEVICETYPE,   "Server"),
            getDatastores(),
            getContentTypeCapabilities(),
            getExtensions(),
            false,
            false,
            false
        );
        return devInf;
    }

    /**
     * First of all, check the availablity and accessibility of the given
     * databases. The state of the given database will change as described below
     * (and in the same order):
     * <ul>
     *   <li>The database status code is set to <i>StatusCode.OK</i> if the
     *       database is accessible, <i>StatusCode.NOT_FOUND</i> if the database
     *       is not found and <i>StatusCode.FORBIDDEN</i> if the principal is
     *       not allowed to access the database.
     *   <li>If the currently set last anchor does not match the last tag
     *       retrieved from the database (DBMS) and the requested alert code is
     *       not a refresh, the synchronization method is set to
     *       <i>AlertCode.SLOW</i>
     *   <li>The database server sync anchor is set to the server-side sync anchor
     *
     * @param principal the principal the is requesting db preparation
     * @param dbs an array of <i>Database</i> objects - NULL
     * @param next the sync timestamp of the current synchronization
     *
     */
    public void prepareDatabases(Sync4jPrincipal principal,
                                 Database[]      dbs      ,
                                 SyncTimestamp   next     ) {
        for (int i=0; ((dbs != null) && (i < dbs.length)); ++i) {
            int statusCode = StatusCode.OK;

            if (!checkServerDatabase(dbs[i])) {
                statusCode = StatusCode.NOT_FOUND;
            } else if (!checkDatabasePermissions(dbs[i])) {
                statusCode = StatusCode.FORBIDDEN;
            }

            dbs[i].setStatusCode(statusCode);

            //
            // Now retrieve the last sync anchor
            //
            if (statusCode == StatusCode.OK) {
                LastTimestamp last = new LastTimestamp(
                    principal.getId(),
                    dbs[i].getName()
                );

                try {
                    store.read(last);
                    dbs[i].setServerAnchor(new Anchor(last.tagClient, next.tagClient));
                } catch (NotFoundException e) {
                    //
                    // No prev last sync! Create a new anchor that won't match
                    //
                    last.tagServer = next.tagClient;
                    dbs[i].setServerAnchor(new Anchor(last.tagServer, next.tagClient));
                } catch(PersistentStoreException e) {
                    if(log.isEnabled(Level.FATAL)){
                        log.fatal("Unable to retrieve timestamp from store");
                    }

                    log.debug("prepareDatabases", e);
                }

                if (  !(last.tagServer.equals(dbs[i].getAnchor().getLast()))
                   && (dbs[i].getMethod() != AlertCode.REFRESH_FROM_SERVER)) {
                    if (log.isEnabled(Level.TRACE)) {
                        log.trace( "Forcing slow sync for database "
                                + dbs[i].getName()
                                );

                        log.trace( "Server last: "
                                + last.tagServer
                                + "; client last: "
                                + dbs[i].getAnchor().getLast()
                                );
                    }
                    dbs[i].setMethod(AlertCode.SLOW);
                }
            }
        }
    }

    /**
     * Translates an array of <i>SyncOperation</i> objects to an array of
     * <i>(Add,Delete,Replace)Command</i> objects. Only client side operations
     * are translated.
     *
     * @param clientMapping the associated existing client mapping
     * @param operations the operations to be translated
     * @param sourceName the corresponding source name
     *
     * @return the commands corresponding to <i>operations</i>
     */
    public ItemizedCommand[] operationsToCommands(ClientMapping      clientMapping,
                                                  SyncOperation[]    operations   ,
                                                  String             sourceName   ) {
        return EngineHelper.operationsToCommands( clientMapping ,
                                                  operations    ,
                                                  sourceName    ,
                                                  cmdIdGenerator);
    }

    /**
     * Updates the mapping given an array of operations. This is used for
     * mappings sent by the client.
     *
     * @param slowSync is this method called during a slow sync
     * @param clientMappings the client mappings for current synchronization
     * @param operations the operation performed
     */
    public void updateClientMappings(java.util.Map<String, ClientMapping>   clientMappings,
                                     SyncOperation[] operations    ,
                                     boolean         slowSync      ) {
        try {
            EngineHelper.updateClientMappings(clientMappings, operations, slowSync);
        } catch (Exception e) {
          log.debug("updateClientMappings", e);
        }
    }


    /**
     * Updates the LUID-GUIDmapping for the client modifications (that is the
     * items directlry inserted or removed by the server).
     *
     * @param clientMappings the client mappings for current synchronization
     *
     */
    public void updateServerMappings(java.util.Map<String, ClientMapping> clientMappings, boolean slowSync) {
        try {
            EngineHelper.updateServerMappings(clientMappings, operationStatus, slowSync);
        } catch (Exception e) {
          log.debug("updateServerMappings", e);
        }
    }

    /**
     * Converts an array of <i>Item</i> objects belonging to the same
     * <i>SyncSource</i> into an array of <i>SyncItem</i> objects.
     * <p>
     * The <i>Item</i>s created are enriched with an additional property
     * called as in SyncItemHelper.PROPERTY_COMMAND, which is used to bound the
     * newly created object with the original command.
     *
     * @param syncSource the <i>SyncSource</i> items belong to - NOT NULL
     * @param items the <i>Item</i> objects
     * @param state the state of the item as one of the values defined in
     *              <i>SyncItemState</i>
     * @param the timestamp to assign to the last even on this item
     *
     *
     * @return an array of <i>SyncItem</i> objects
     */
    public static SyncItem[] itemsToSyncItems(ClientMapping       clientMapping,
                                              SyncSource          syncSource   ,
                                              ModificationCommand cmd          ,
                                              char                state        ,
                                              long                timestamp    ) {

        return EngineHelper.itemsToSyncItems(clientMapping, syncSource, cmd, state, timestamp);
    }

    // --------------------------------------------------------- Private methods

    /**

    /**
     * Checks if the given database is managed by this server.
     *
     * @param db the database to be checked
     *
     * @return true if the database is under control of this server, false otherwise
     *
     */
    private boolean checkServerDatabase(Database db) {
        if (log.isEnabled(Level.TRACE)) {
            log.trace( "Checking if the database "
                      + db
                      + " is in the server database list "
                      + serverSources
                      );
        }

        SyncSource source = null;
        Iterator<Object> i = serverSources.iterator();
        while(i.hasNext()) {
            source = (SyncSource)i.next();

            if (db.getName().equals(source.getSourceURI())) {
                if(log.isEnabled(Level.TRACE)){
                    log.trace("Yes sir!");
                }

                return true;
            }
        }

        if(log.isEnabled(Level.TRACE)){
            log.trace("Not found sir");
        }

        //
        // not found...
        //
        return false;
    }

    /**
     * Checks if the principal associated to the database is allowed to synchronize.<br>
     *
     * @param db the database to be checked
     *
     * @return true if the credential is allowed to access the database, false otherwise
     */
    private boolean checkDatabasePermissions(Database db) {
        return authorize(db.getPrincipal(), db.getName());
    }
}
