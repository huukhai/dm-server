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


import java.util.List;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;
import java.security.Principal;
import java.sql.Timestamp;

import com.funambol.framework.core.ModificationCommand;
import com.funambol.framework.core.StatusCode;
import com.funambol.framework.core.Add;
import com.funambol.framework.engine.SyncConflict;
import com.funambol.framework.engine.SyncException;
import com.funambol.framework.engine.SyncItem;
import com.funambol.framework.engine.SyncItemImpl;
import com.funambol.framework.engine.SyncItemMapping;
import com.funambol.framework.engine.SyncItemState;
import com.funambol.framework.engine.SyncOperation;
import com.funambol.framework.engine.SyncOperationImpl;
import com.funambol.framework.engine.SyncOperationStatus;
import com.funambol.framework.engine.SyncStrategy;
import com.funambol.framework.engine.Util;
import com.funambol.framework.engine.source.SyncSource;
import com.funambol.framework.engine.source.SyncSourceException;

import org.apache.commons.collections15.ListUtils;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/**
 * This class represents a synchronization process.
 *
 * The base concrete implementation of a synchronization strategy. It implements
 * the <i>ConcreteStrategy</i> partecipant of the Strategy Pattern.
 * <p>
 * The synchronization process is implemented in this class as follows:
 * <p>
 * Given a set of sources A, B, C, D, etc, the synchronization process takes place
 * between two sources at a time: A is first synchronzed with B, then AB with
 * C, then ABC with D and so on. <br>
 * The synchronization process is divided in three phases: preparation, synchronization,
 * finalization. These phases correspond to the methdos prepareSync, sync and
 * endSync; they are call-back methods called sequentially by a driver object,
 * usually a SyncMethod object. In this way the SyncMethod object has a chance
 * to communicate with the external world before carry on the process.
 * For example a SyncMethod object can show to the user the items that are going
 * to be synchronized, so that the user can let the process carry on or stop it. <br>
 * prepareSync returns an array of SyncOperation, in which each element represents
 * a particular synchronization action, ie. create an item in the source A,
 * delete the item X from the source B, etc. Sometime it is not possible decide
 * what to do, thus a SyncConflict operation is used. A conflict must be solved
 * by something external the synchronization process, for instance by a user
 * action. Below is a table of all possible situations.
 * <pre>
 *
 * | -------- | --- | --- | --- | --- | --- |
 * | Source A |     |     |     |     |     |
 * |    /     |  N  |  D  |  U  |  S  |  X  |     N : item new
 * | Source B |     |     |     |     |     |     D : item deleted
 * | -------- | --- | --- | --- | --- | --- |     U : item updated
 * |       N  |  O  |  O  |  O  |  O  |  B  |     S : item synchronized/unchanged
 * | -------- | --- | --- | --- | --- | --- |     X : item not existing
 * |       D  |  O  |  X  |  O  |  X  |  X  |     O : conflict
 * | -------- | --- | --- | --- | --- | --- |     A : item A replaces item B
 * |       U  |  O  |  O  |  O  |  B  |  B  |     B : item B replaces item A
 * | -------- | --- | --- | --- | --- | --- |
 * |       S  |  O  |  X  |  A  |  =  |  B  |
 * | -------- | --- | --- | --- | --- | --- |
 * |       X  |  A  |  X  |  A  |  A  |  X  |
 * | -------- | --- | --- | --- | --- | --- |
 *
 * </pre>
 *
 * @version  $Id: Sync4jStrategy.java,v 1.3 2006/08/07 21:09:24 nichele Exp $
 */
public class Sync4jStrategy implements SyncStrategy, java.io.Serializable {

    // --------------------------------------------------------------- Constants

    public static String LOG_NAME = "funambol.framework.engine";

    private transient static final Logger log = Logger.getLogger(com.funambol.server.engine.Sync4jStrategy.class.getName());

    // -------------------------------------------------------------- Properties

    /**
     * The synchronization source
     */
    private transient SyncSource[] sources = null;
    public SyncSource[] getSources(){ return sources; }

    public void setSources(SyncSource[] sources){ this.sources = sources; }

    /**
     * The process' name
     */
    public String getName(){ return name; }

    private String name;
    public void setName(String name){ this.name = name; }

    // ------------------------------------------------------------ Constructors

    public Sync4jStrategy() {
    }

    public Sync4jStrategy(SyncSource[] syncSources) {
        sources = syncSources;
    }

    // ---------------------------------------------------------- Public methods

    /**
     * Preparation for the synchronization. If <i>sources</i> is not null,
     * the preparation works on the given sources. Otherwise it works on the
     * sources as they were set in the constructor.
     *
     * @param sources the sources to be synchronized
     * @param principal the entity for which the synchronization is required
     *
     * @return an array of SyncOperation, one for each SyncItem that must be
     *         created/updated/deleted or in conflict.
     *
     * @see com.funambol.framework.engine.SyncStrategy
     */
    public SyncOperation[] prepareSlowSync(SyncSource[] sources  ,
                                           Principal    principal)
    throws SyncException {

        SyncItem[] allA = null, allB = null;

        if (sources != null) {
            this.sources = sources;
        }

        List<Object> syncOperations = null           ,
             Am             = new ArrayList<Object>(),
             Bm             = new ArrayList<Object>(),
             AmBm           = null           ;

        if (log.isEnabledFor(Level.INFO)) {
            log.info("Preparing slow synchronization for " + principal + " ...");
        }

        allA = sources[1].getAllSyncItems(principal);
        allB = sources[0].getAllSyncItems(principal);

        //
        // Because it is a slow sync, items state must be reset to S
        //
        EngineHelper.resetState(Arrays.asList(allA));
        EngineHelper.resetState(Arrays.asList(allB));

        //
        // If any item from the client has not a corresponding mapping, the
        // server source must be queried for the item, since the client item
        // could be the same of an existing server item. In this case,  the old
        // unmapped item is replaced in Map by the newly mapped item.
        //
        ArrayList<SyncItemMapping> newlyMappedItems = new ArrayList<SyncItemMapping>();

        fixMappedItems(newlyMappedItems, allA, sources[0], principal, true);

        Am.addAll(Arrays.asList(allA));
        Bm.addAll(Arrays.asList(allB));

        AmBm = EngineHelper.intersect(Am, Bm);

        if (log.isEnabledFor(Level.TRACE)) {
            log.trace("Am: "   + Am  );
            log.trace("Bm: "   + Bm  );
            log.trace("AmBm: " + AmBm);
        }

        syncOperations =
            checkSyncOperations(principal, Am, Bm, AmBm, new ArrayList<Object>(), new ArrayList<Object>());

        //
        // In the case of slow sync, delete operations are not needed
        //
        SyncOperation o = null;

        ArrayList<SyncOperation> ret = new ArrayList<SyncOperation>();

        Iterator<Object> i = syncOperations.iterator();
        while (i.hasNext()) {
            o = (SyncOperation)i.next();

            if (o.getOperation() != SyncOperation.DELETE) {
                ret.add(o);
            }
        }

        if (log.isEnabledFor(Level.TRACE)) {
            log.trace("operations: " + ret);
        }

        log.info("Preparation completed.");

        return ret.toArray(new SyncOperationImpl[] {});
    }

    /**
     * Preparation for faset synchronization. If <i>sources</i> is not null,
     * the preparation operates on the given sources. Otherwise it works on the
     * sources as they were set in the constructor.
     * <p>
     * Refer to the architecture document</a> for details about the algoritm applied.
     *
     * @param sources the sources to be synchronized
     * @param principal the entity for which the synchronization is required
     *
     * @return an array of SyncOperation, one for each SyncItem that must be
     *         created/updated/deleted or in conflict.
     *
     * @see com.funambol.framework.engine.SyncStrategy
     */
    public SyncOperation[] prepareFastSync(SyncSource[] sources  ,
                                           Principal    principal,
                                           Timestamp    since    )
    throws SyncException {
        if (sources != null) {
            this.sources = sources;
        }

        // ---------------------------------------------------------------------

        List<Object> Am    ,  // items modified in A
             Bm    ,  // items modified in B
             AmBm  ,  // Am intersect Bm
             AAmBm ,  // items unmodified in A, but modified in B
             AmBBm ;  // items unmodified in B, but modified in A

        ArrayList<Object> syncOperations = null;

        SyncItem[] newA       = null, newB       = null,
                   updatedA   = null, updatedB   = null,
                   deletedA   = null, deletedB   = null;

        if (log.isEnabledFor(Level.INFO)) {
            log.info( "Preparing fast synchronization for "
                    + principal
                    + " since "
                    + since
                    );
        }

        // ---------------------------------------------------------------------

        //
        // NOTE: simplified version - only two sources, the first one of which
        // is the client
        //
        newA       = sources[1].getNewSyncItems      (principal, since);
        updatedA   = sources[1].getUpdatedSyncItems  (principal, since);
        deletedA   = sources[1].getDeletedSyncItems  (principal, since);

        newB       = sources[0].getNewSyncItems      (principal, since);
        updatedB   = sources[0].getUpdatedSyncItems  (principal, since);
        deletedB   = sources[0].getDeletedSyncItems  (principal, since);

        if (log.isEnabledFor(Level.TRACE)) {
            log.trace("newA: " + Util.arrayToString(newA));
            log.trace("newB: " + Util.arrayToString(newB));

            log.trace("updatedA: " + Util.arrayToString(updatedA));
            log.trace("updatedB: " + Util.arrayToString(updatedB));

            log.trace("deletedA: " + Util.arrayToString(deletedA));
            log.trace("deletedB: " + Util.arrayToString(deletedB));
        }

        //
        // If any item from the client has not a corresponding mapping, the
        // server source must be queried for the item, since the client item
        // could be the same of an existing server item. In this case,  the old
        // unmapped item is replaced in Ma by the newly mapped item.
        //
        ArrayList<SyncItemMapping> newlyMappedItems = new ArrayList<SyncItemMapping>();

        fixMappedItems(newlyMappedItems, newA,     sources[0], principal, false);
        fixMappedItems(newlyMappedItems, updatedA, sources[0], principal, false);
        fixMappedItems(newlyMappedItems, deletedA, sources[0], principal, false);

        if (log.isEnabledFor(Level.TRACE)) {
            log.trace("Newly mapped items: " + newlyMappedItems);
        }

        Am = new ArrayList<Object>(); Bm = new ArrayList<Object>();
        Am.addAll(Arrays.asList(newA      ));
        Am.addAll(Arrays.asList(updatedA  ));
        Am.addAll(Arrays.asList(deletedA  ));
        Bm.addAll(Arrays.asList(newB      ));
        Bm.addAll(Arrays.asList(updatedB  ));
        Bm.addAll(Arrays.asList(deletedB  ));

        if (log.isEnabledFor(Level.TRACE)) {
            log.trace("Am: " + Am);
            log.trace("Bm: " + Bm);
            log.trace("Am-Bm: " + ListUtils.subtract(Am, Bm));
            log.trace("Bm-Am: " + ListUtils.subtract(Bm, Am));
        }
        //
        // Now calculate subsets: AmBm, AmBBm, AAmBm
        //
        AmBm  = EngineHelper.intersect(Am, Bm);
        AmBBm = EngineHelper.buildAmBBm(ListUtils.subtract(Am, Bm), sources[0], principal);
        AAmBm = EngineHelper.buildAAmBm(ListUtils.subtract(Bm, Am), sources[1], principal);

        if (log.isEnabledFor(Level.TRACE)) {
            log.trace("AmBm:  " + AmBm );
            log.trace("AmBBm: " + AmBBm);
            log.trace("AAmBm: " + AAmBm);
        }

        //
        // Ready for conflict detection!
        //
        syncOperations =
            checkSyncOperations(principal, Am, Bm, AmBm, AmBBm, AAmBm);

        if (log.isEnabledFor(Level.TRACE)) {
            log.trace("operations: " + syncOperations);
        }

        log.info("Preparation completed.");

        return syncOperations.toArray(new SyncOperationImpl[] {});
    }

    /**
     * Implements Synchronizable.sync
     *
     *
     */
    public SyncOperationStatus[] sync(SyncOperation[] syncOperations) {

        log.info("Synchronizing...");

        if ((syncOperations == null) || (syncOperations.length == 0)) {
            return new SyncOperationStatus[0];
        }

        ArrayList<SyncOperationStatus> status = new ArrayList<SyncOperationStatus>();

        SyncOperationStatus[] operationStatus = null;
        for(int i=0; i<syncOperations.length; ++i) {
            if (log.isEnabledFor(Level.TRACE)) {
                log.trace("Executing " + syncOperations[i]);
            }

            //
            // execSyncOperation can return more than one status for one
            // operation when more than one source are involved
            //
            operationStatus = execSyncOperation((SyncOperationImpl)syncOperations[i]);

            for (int j=0; j<operationStatus.length; ++j) {
                status.add(operationStatus[j]);
            }  // next j
        } // next i

        log.info("Synchronization completed.");

        if (log.isEnabledFor(Level.TRACE)) {
            log.trace("status: " + status);
        }

        return status.toArray(new SyncOperationStatus[0]);
    }

    /**
     * Implements Synchronizable.endSync
     *
     * @see beanblue.sync.event.Synchronizable
     */
    public void endSync() throws SyncException {
        log.info("Ending synchronization...");
        log.info("Synchronization ended.");
    }

    // -------------------------------------------------------- Protected methds

    /**
     * Checks the given SyncItem lists and creates the needed SyncOperations
     * following the rules described in the class descriprio and in the .
     *
     * @param principal who has requested the synchronization
     * @param AmBm
     * @param AmBBm
     * @param AAmBm
     *
     * @return an ArrayList containing all the collected sync operations
     */
    protected ArrayList<Object> checkSyncOperations(Principal principal,
                                            List<Object>      Am       ,
                                            List<Object>      Bm       ,
                                            List<Object>      AmBm     ,
                                            List<Object>      AmBBm    ,
                                            List<Object>      AAmBm    ) {
        SyncItemMapping mapping = null;
        SyncItem syncItemA = null, syncItemB = null;

        ArrayList<Object> all        = new ArrayList<Object>();
        ArrayList<Object> operations = new ArrayList<Object>();

        // ---------------------------------------------------------------------

        all.addAll(AmBm );
        all.addAll(AmBBm);
        all.addAll(AAmBm);

        //
        // 1st check: items in both sources
        //
        Iterator<Object> i = all.iterator();
        while (i.hasNext()) {
            mapping = (SyncItemMapping)i.next();

            syncItemA = mapping.getSyncItemA();
            syncItemB = mapping.getSyncItemB();

            operations.add(
                checkSyncOperation(
                    principal,
                    syncItemA,
                    syncItemB
                )
            );
            Am.remove(syncItemA);
            Bm.remove(syncItemB);
        }

        //
        // 2nd check: items in source A and not in source B
        //
        i = Am.iterator();
        while (i.hasNext()) {
           syncItemA = (SyncItem)i.next();

           operations.add(checkSyncOperation(principal, syncItemA, null));
        }  // next i

        //
        // 3rd check: items in source B and not in source A
        //
        i = Bm.iterator();
        while (i.hasNext()) {
           syncItemB = (SyncItem)i.next();

           operations.add(checkSyncOperation(principal, null, syncItemB));
        }  // next i

        return operations;
    }

    /**
     * Create a SyncOperation based on the state of the given SyncItem couple.
     *
     * @param principal     the entity that wnats to do the operation
     * @param syncItemA     the SyncItem of the source A - NULL means <i>not existing</i/
     * @param syncItemB     the SyncItem of the source B - NULL means <i>not existing</i/
     *
     * @return the SyncOperation object
     */
    protected SyncOperation checkSyncOperation(Principal principal,
                                               SyncItem  syncItemA,
                                               SyncItem  syncItemB) {
        if (log.isEnabledFor(Level.TRACE)) {
            log.trace( "check: syncItemA: "
                    + syncItemA
                    + " syncItemB: "
                    + syncItemB
                    );
        }

        if (syncItemA == null) {
            syncItemA = SyncItemImpl.getNotExistingSyncItem(null);
        }
        if (syncItemB == null) {
            syncItemB = SyncItemImpl.getNotExistingSyncItem(null);
        }

        switch (syncItemA.getState()) {
            //
            // NEW
            //
            case SyncItemState.NEW:
                switch (syncItemB.getState()) {
                    case SyncItemState.NEW:
                        return new SyncConflict(syncItemA, syncItemB,
                                                String.valueOf(SyncItemState.NEW) +
                                                String.valueOf(SyncItemState.NEW) );
                    case SyncItemState.UPDATED:
                        return new SyncConflict(syncItemA, syncItemB,
                                                String.valueOf(SyncItemState.NEW)     +
                                                String.valueOf(SyncItemState.UPDATED) );
                   case SyncItemState.DELETED:
                        return new SyncConflict(syncItemA, syncItemB,
                                                String.valueOf(SyncItemState.NEW)     +
                                                String.valueOf(SyncItemState.DELETED) );
                   case SyncItemState.SYNCHRONIZED:
                        return new SyncConflict(syncItemA, syncItemB,
                                                String.valueOf(SyncItemState.NEW)       +
                                                String.valueOf(SyncItemState.SYNCHRONIZED) );
                   case SyncItemState.NOT_EXISTING:
                        return new SyncOperationImpl(principal, syncItemA, syncItemB, SyncOperation.NEW, false, true);
                }  // end inner switch

            //
            // DELETED
            //
            case SyncItemState.DELETED:
                switch (syncItemB.getState()) {
                    case SyncItemState.NEW:
                        return new SyncConflict(syncItemA, syncItemB,
                                                String.valueOf(SyncItemState.DELETED) +
                                                String.valueOf(SyncItemState.NEW)     );
                    case SyncItemState.UPDATED:
                        return new SyncConflict(syncItemA, syncItemB,
                                                String.valueOf(SyncItemState.DELETED) +
                                                String.valueOf(SyncItemState.UPDATED) );

                   case SyncItemState.SYNCHRONIZED:
                        return new SyncOperationImpl(principal, syncItemA, syncItemB, SyncOperation.DELETE, false, true);

                   case SyncItemState.DELETED:
                   case SyncItemState.NOT_EXISTING:
                        return new SyncOperationImpl(principal, syncItemB, syncItemA, SyncOperation.NOP, false, false);
                }  // end inner switch

            //
            // UPDATED
            //
            case SyncItemState.UPDATED:
                switch (syncItemB.getState()) {
                    case SyncItemState.NEW:
                        return new SyncConflict(syncItemA, syncItemB,
                                                String.valueOf(SyncItemState.UPDATED) +
                                                String.valueOf(SyncItemState.NEW)     );
                    case SyncItemState.UPDATED:
                        return new SyncConflict(syncItemA, syncItemB,
                                                String.valueOf(SyncItemState.UPDATED) +
                                                String.valueOf(SyncItemState.UPDATED) );
                    case SyncItemState.DELETED:
                        return new SyncConflict(syncItemA, syncItemB,
                                                String.valueOf(SyncItemState.UPDATED) +
                                                String.valueOf(SyncItemState.DELETED) );
                    case SyncItemState.SYNCHRONIZED:
                        return new SyncOperationImpl(principal, syncItemA, syncItemB, SyncOperation.UPDATE, false, true);
                    case SyncItemState.NOT_EXISTING:
                        return new SyncOperationImpl(principal, syncItemA, syncItemB, SyncOperation.NEW, false, true);
                }  // end inner switch

           //
           // SYNCHRONIZED
           //
           case SyncItemState.SYNCHRONIZED:
                switch (syncItemB.getState()) {
                    case SyncItemState.NEW:
                        return new SyncConflict(syncItemA, syncItemB,
                                                String.valueOf(SyncItemState.SYNCHRONIZED) +
                                                String.valueOf(SyncItemState.NEW)       );
                    case SyncItemState.UPDATED:
                        return new SyncOperationImpl(principal, syncItemA, syncItemB, SyncOperation.UPDATE, true, false);
                   case SyncItemState.DELETED:
                        return new SyncOperationImpl(principal, syncItemA, syncItemB, SyncOperation.DELETE, true, false);
                   case SyncItemState.SYNCHRONIZED:
                        return new SyncOperationImpl(principal, syncItemA, syncItemB, SyncOperation.NOP, false, false);
                   case SyncItemState.NOT_EXISTING:
                        return new SyncOperationImpl(principal, syncItemA, syncItemB, SyncOperation.NEW, false, true);
                }  // end inner switch

           //
           // NOTEXISITNG
           //
           case SyncItemState.NOT_EXISTING:
                switch (syncItemB.getState()) {
                    case SyncItemState.NEW:
                    case SyncItemState.UPDATED:
                        return new SyncOperationImpl(principal, syncItemA, syncItemB, SyncOperation.NEW, true, false);
                    case SyncItemState.SYNCHRONIZED:
                        return new SyncOperationImpl(principal, syncItemA, syncItemB, SyncOperation.NEW, true, false);
                    case SyncItemState.NOT_EXISTING:
                    case SyncItemState.DELETED:
                        return new SyncOperationImpl(principal, syncItemA, syncItemB, SyncOperation.NOP, false, false);
                }  // end inner switch

            //
            // CONFLICT
            // In this case itemA has a mapped twin and itemB not existing
            //
            case SyncItemState.CONFLICT:
                return new SyncConflict(syncItemA, syncItemB, "CX");
        }  // end switch

        return new SyncOperationImpl(principal, syncItemA, syncItemB, SyncOperation.NOP, false, false);
    }

    /**
     * Executes the given SyncOperation. Note that conflicts are ignored!
     * <p>
     * Note also that the number of status returned is equal to the number of
     * sources affected by the operation (0 for NOP, 1 for NEW and UPDATE and
     * 1 or 2 for DELETE).
     *
     * @param operation     the SyncOperation to execute
     *
     * @return an array of <i>SyncOperationStatus</i> objects representing the
     *         status of the executed operation. For instance, in case of error,
     *         <i>SyncOperation.error</i> will be set to the catched exception.
     */
    protected SyncOperationStatus[] execSyncOperation(SyncOperationImpl operation) {
                SyncItem syncItemA = operation.getSyncItemA(),
                 syncItemB = operation.getSyncItemB();

        Principal owner = operation.getOwner();
        SyncOperationStatus[] status = null;
        ModificationCommand cmd = null;

        int size = 0, s = 0;

        switch (operation.getOperation()) {
            case SyncOperation.NEW:
                status = new SyncOperationStatus[1];
                if (operation.isAOperation()) {
                    cmd = (ModificationCommand)syncItemA.getPropertyValue(SyncItemHelper.PROPERTY_COMMAND);
                    try {
                        syncItemA = sources[1].setSyncItem(owner, syncItemB);
                        operation.setSyncItemA(syncItemA);
                        status[0] = new Sync4jOperationStatusOK(operation, sources[1], cmd, StatusCode.ITEM_ADDED);

                    } catch (SyncException e) {
                        if(log.isEnabledFor(Level.FATAL)){
                            log.fatal("Error executing sync operation: " + e.getMessage());
                        }

                        log.debug("execSyncOperation", e);

                        status[0] = new Sync4jOperationStatusError(operation, sources[1], cmd, e);
                    }
                } else if (operation.isBOperation()) {
                    cmd = (ModificationCommand)syncItemA.getPropertyValue(SyncItemHelper.PROPERTY_COMMAND);
                    syncItemB.setProperty(syncItemA.getProperty(SyncItem.PROPERTY_TIMESTAMP)); // this contains the
                                                                                               // current sync
                    try {
                        syncItemB = sources[0].setSyncItem(owner, syncItemA);
                        operation.setSyncItemB(syncItemB);
                        status[0] = new Sync4jOperationStatusOK(operation, sources[1], cmd, StatusCode.ITEM_ADDED);

                    } catch (SyncException e) {
                        if(log.isEnabledFor(Level.FATAL)){
                            log.fatal("Error executing sync operation: " + e.getMessage());
                        }

                        log.debug("execSyncOperation", e);

                        status[0] = new Sync4jOperationStatusError(operation, sources[0], cmd, e);
                    }
                }
                break;

            case SyncOperation.UPDATE:
                status = new SyncOperationStatus[1];
                if (operation.isAOperation()) {
                    cmd = (ModificationCommand)syncItemA.getPropertyValue(SyncItemHelper.PROPERTY_COMMAND);
                    try {
                        syncItemA = sources[1].setSyncItem(owner, syncItemB);
                        operation.setSyncItemA(syncItemA);
                        status[0] = new Sync4jOperationStatusOK(operation, sources[1], cmd);
                    } catch (SyncException e) {
                        if(log.isEnabledFor(Level.FATAL)){
                            log.fatal("Error executing sync operation: " + e.getMessage());
                        }

                        log.debug("execSyncOperation", e);

                        status[0] = new Sync4jOperationStatusError(operation, sources[1], cmd, e);
                    }
                } else if (operation.isBOperation()) {
                    cmd = (ModificationCommand)syncItemA.getPropertyValue(SyncItemHelper.PROPERTY_COMMAND);
                    syncItemB.setProperty(syncItemA.getProperty(SyncItem.PROPERTY_TIMESTAMP)); // this contains the
                                                                                               // current sync
                    try {
                        syncItemB = sources[0].setSyncItem(owner, syncItemA);
                        operation.setSyncItemB(syncItemB);
                        status[0] = new Sync4jOperationStatusOK(operation, sources[0], cmd);
                    } catch (SyncException e) {
                        if(log.isEnabledFor(Level.FATAL)){
                            log.fatal("Error executing sync operation: " + e.getMessage());
                        }

                        log.debug("execSyncOperation", e);

                        status[0] = new Sync4jOperationStatusError(operation, sources[0], cmd, e);
                    }
                }
                break;

            case SyncOperation.DELETE:
                //
                // How many status we need? One if we have to delete the item
                // from only one source, two if we have to delete it from both
                // sources
                //
                size = 1;
                if (operation.isAOperation() && operation.isBOperation()) {
                    size = 2;
                }
                status = new SyncOperationStatus[size];

                s = 0;
                if (operation.isBOperation()) {
                    cmd = (ModificationCommand)syncItemA.getPropertyValue(SyncItemHelper.PROPERTY_COMMAND);
                    syncItemB.setProperty(syncItemA.getProperty(SyncItem.PROPERTY_TIMESTAMP)); // this contains the
                                                                                               // timestamp of the
                                                                                               // current sync
                    try {
                        sources[0].removeSyncItem(owner, syncItemB);
                        status[s++] = new Sync4jOperationStatusOK(operation, sources[0], cmd);
                    } catch (SyncException e) {
                        if(log.isEnabledFor(Level.FATAL)){
                            log.fatal("Error executing sync operation: " + e.getMessage());
                        }

                        log.debug("execSyncOperation", e);

                        status[s++] = new Sync4jOperationStatusError(operation, sources[0], cmd, e);
                    }
                }
                if (operation.isAOperation()) {
                    cmd = (ModificationCommand)syncItemA.getPropertyValue(SyncItemHelper.PROPERTY_COMMAND);

                    try {
                        sources[1].removeSyncItem(owner, syncItemA);
                        status[s++] = new Sync4jOperationStatusOK(operation, sources[1], cmd);
                    } catch (SyncException e) {
                        if(log.isEnabledFor(Level.FATAL)){
                             log.fatal("Error executing sync operation: " + e.getMessage());
                        }

                        log.debug("execSyncOperation", e);

                        status[s++] = new Sync4jOperationStatusError(operation, sources[1], cmd, e);
                    }
                }
                break;

            case SyncOperation.NOP:
                //
                // A NOP operation can be due by one of the following conditions
                // (see checkOperation(...)):
                //
                // 1. both items are flagged as "Deleted"
                // 2. both items are flagged "Synchronized"
                // 3. itemA is "Not existing" and itemB is "Not existing" or "Deleted"
                //
                // In case 1. we want a status is returned, thought no real
                // action is performed.
                //
                s = 0;
                if (operation.isAOperation()) ++s;
                if (operation.isBOperation()) ++s;

                status = new SyncOperationStatus[size];

                if (operation.isBOperation()) {
                    cmd = (ModificationCommand)syncItemA.getPropertyValue(SyncItemHelper.PROPERTY_COMMAND);
                    status[s++] = new Sync4jOperationStatusOK(operation, sources[0], cmd);
                }

                if (operation.isAOperation()) {
                    cmd = (ModificationCommand)syncItemA.getPropertyValue(SyncItemHelper.PROPERTY_COMMAND);
                    status[s++] = new Sync4jOperationStatusOK(operation, sources[1], cmd);
                }

                if (!operation.isAOperation() && !operation.isBOperation()) {
                    char stateItemA = syncItemA.getState();
                    char stateItemB = syncItemB.getState();

                    switch (stateItemA) {
                        case SyncItemState.SYNCHRONIZED:
                            if (stateItemB == SyncItemState.SYNCHRONIZED) {
                                status = new SyncOperationStatus[1];
                                cmd = (ModificationCommand)syncItemA.getPropertyValue(SyncItemHelper.PROPERTY_COMMAND);

                                if (cmd instanceof Add) {
                                    status[0] = new Sync4jOperationStatusOK(operation, sources[1], cmd, StatusCode.ITEM_ADDED);
                                } else {
                                    status[0] = new Sync4jOperationStatusOK(operation, sources[1], cmd);
                                }
                                break;
                            }
                        case SyncItemState.NOT_EXISTING:
                            if (stateItemB == SyncItemState.DELETED) {
                                //
                                //client sent Delete item that not exist on server
                                //
                                try {
                                    cmd = (ModificationCommand)syncItemB.getPropertyValue(SyncItemHelper.PROPERTY_COMMAND);
                                    syncItemA = sources[1].setSyncItem(owner, syncItemB);
                                    operation.setSyncItemA(syncItemA);

                                    status = new SyncOperationStatus[1];
                                    status[0] = new Sync4jOperationStatusOK(
                                                operation,
                                                sources[1],
                                                cmd,
                                                StatusCode.ITEM_NOT_DELETED
                                                );
                                    break;
                                } catch (SyncException e) {
                                    if(log.isEnabledFor(Level.FATAL)){
                                        log.fatal("Error executing sync operation: " + e.getMessage());
                                    }

                                    log.debug("execSyncOperation", e);

                                    status[0] = new Sync4jOperationStatusError(operation, sources[1], cmd, e);
                                    break;
                                }
                            }
                        case SyncItemState.DELETED:
                            if (stateItemB == SyncItemState.DELETED) {
                                status = new SyncOperationStatus[1];
                                cmd = (ModificationCommand)syncItemA.getPropertyValue(SyncItemHelper.PROPERTY_COMMAND);
                                status[0] = new Sync4jOperationStatusOK(operation, sources[1], cmd);
                                break;
                            }
                    }
                }

                break;

            case SyncOperation.CONFLICT:
                //
                // The conflic is solved with data from server
                //
                try {
                    SyncConflict sc = (SyncConflict)operation;
                    cmd = (ModificationCommand)syncItemA.getPropertyValue(SyncItemHelper.PROPERTY_COMMAND);

                    //
                    //Type CX means that itemA is in state CONFLICT and itemB is in state NOT_EXISTING
                    //This situation happen when itemA has already a twin mapped
                    //
                    if (sc.getType().equals("CX")) {
                        sources[1].removeSyncItem(owner, syncItemA);
                    } else {
                        syncItemA = sources[1].setSyncItem(owner, syncItemB);
                        operation.setSyncItemA(syncItemA);
                    }
                    operation.setAOperation(true);

                    status = new SyncOperationStatus[1];
                    status[0] = new Sync4jOperationStatusConflict(
                                    operation,
                                    sources[1],
                                    cmd,
                                    StatusCode.CONFLICT_RESOLVED_WITH_SERVER_DATA
                                );
                } catch (SyncException e) {
                    if(log.isEnabledFor(Level.FATAL)){
                        log.fatal("Error executing sync operation: " + e.getMessage());
                    }

                    log.debug("execSyncOperation", e);

                    status[0] = new Sync4jOperationStatusError(operation, sources[1], cmd, e);
                }

                break;
        }  // end switch

        return status;
    }

    // --------------------------------------------------------- Private methods

    /**
     * If any item from the client has not a corresponding mapping, the
     * server source must be queried for the item, since the client item
     * could be the same of an existing server item. In this case,  the old
     * unmapped item is replaced in Ma by the newly mapped item.
     *
     * @param newlyMappedItems the collection that contains all the checked items
     * @param source the source that has to be queried for twins
     * @param syncItems the items to be searched if not mapped
     * @param principal the principal items are belonging to
     *
     */
    private void fixMappedItems(Collection<SyncItemMapping> newlyMappedItems,
                                SyncItem[] syncItems       ,
                                SyncSource source          ,
                                Principal  principal       ,
                                boolean    isSlowSync      ) {
        SyncItem itemB = null;

        for (int i = 0; ((syncItems != null) && (i<syncItems.length)); ++i) {
            itemB = null;
            if (!syncItems[i].isMapped()) {
                try {
                    itemB = source.getSyncItemFromTwin(principal, syncItems[i]);
                } catch (SyncSourceException e) {
                      String msg = "Error retrieving the twin item of "
                                 + syncItems[i].getKey()
                                 + " from source "
                                 + source
                                 + ": "
                                 + e.getMessage();

                      if(log.isEnabledFor(Level.FATAL)){
                          log.fatal(msg);
                      }

                      log.debug("fixSyncMapping", e);

                }
                if (itemB != null) {
                   if (isSlowSync) {
                        //
                        //if slow sync and if there is a SyncItem twin of the input SyncItem
                        //verify if the mapping already exist into list of array mapped
                        //if mapping exists then there is a conflict
                        //
                        if(!isContained(newlyMappedItems, itemB)) {
                            SyncItemMapping mapping = new SyncItemMapping(itemB.getKey());
                            mapping.setMapping(syncItems[i], itemB);
                            newlyMappedItems.add(mapping);
                            syncItems[i] = SyncItemHelper.newMappedSyncItem(itemB.getKey(), syncItems[i]);
                        } else {
                            itemB = syncItems[i];
                            itemB.setState(SyncItemState.CONFLICT);

                            SyncItemMapping mapping = new SyncItemMapping(itemB.getKey());
                            mapping.setMapping(syncItems[i], itemB);
                            newlyMappedItems.add(mapping);
                            syncItems[i] = SyncItemHelper.newMappedSyncItem(itemB.getKey(), syncItems[i]);
                        }
                    } else {
                        SyncItemMapping mapping = new SyncItemMapping(itemB.getKey());
                        mapping.setMapping(syncItems[i], itemB);
                        newlyMappedItems.add(mapping);
                        syncItems[i] = SyncItemHelper.newMappedSyncItem(itemB.getKey(), syncItems[i]);
                    }
                }
            }
        }  // next i
    }

    /**
     * Verify if input item is already present into list of mapped items
     *
     * @param newlyMappedItems list updated of items
     * @param itemTwin item to research
     *
     * @return true if item presents into list else false
     */
    private boolean isContained(Collection<SyncItemMapping> newlyMappedItems, SyncItem itemTwin) {
        Iterator<SyncItemMapping> it2 = newlyMappedItems.iterator();
        while (it2.hasNext()) {
            SyncItemMapping map = it2.next();
            SyncItem itemB = map.getSyncItemB();

            if (itemB.equals(itemTwin)) {
                return true;
            }
        }
        return false;
    }
}
