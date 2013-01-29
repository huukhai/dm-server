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


import com.funambol.framework.core.Add;
import com.funambol.framework.core.ContentTypeInfo;
import com.funambol.framework.core.Data;
import com.funambol.framework.core.DataStore;
import com.funambol.framework.core.Delete;
import com.funambol.framework.core.Item;
import com.funambol.framework.core.ItemizedCommand;
import com.funambol.framework.core.Meta;
import com.funambol.framework.core.ModificationCommand;
import com.funambol.framework.core.Replace;
import com.funambol.framework.core.Source;
import com.funambol.framework.core.SourceRef;
import com.funambol.framework.core.Status;
import com.funambol.framework.core.SyncCap;
import com.funambol.framework.core.SyncType;
import com.funambol.framework.tools.CommandIdGenerator;
import com.funambol.framework.engine.SyncOperationStatus;
import com.funambol.framework.engine.SyncOperation;
import com.funambol.framework.engine.SyncItemKey;
import com.funambol.framework.engine.SyncItem;
import com.funambol.framework.engine.SyncItemImpl;
import com.funambol.framework.engine.SyncProperty;
import com.funambol.framework.engine.SyncItemMapping;
import com.funambol.framework.engine.SyncItemState;
import com.funambol.framework.engine.SyncConflict;
import com.funambol.framework.engine.source.ContentType;
import com.funambol.framework.engine.source.SyncSource;
import com.funambol.framework.engine.source.SyncSourceException;
import com.funambol.framework.engine.source.SyncSourceInfo;
import com.funambol.framework.server.ClientMapping;
import com.funambol.framework.server.error.MappingException;

import java.util.Map;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.security.Principal;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;


/**
 * This class collects helper methods used by the engine.
 *
 */
public class EngineHelper {

    public static Logger log = Logger.getLogger(com.funambol.server.engine.EngineHelper.class.getName());

     /**
      * Status are grouped as per their original commands and status code so
      * that the number of returned status will be minimal. This is done by
      * generating an hash map where the key is a couple
      * <i>(original command id, status code)</i> and the value is the
      * <i>StatusCommand</i> object. Finally the hash map is iterated over to
      * return the status commands array.<br>
      * Note that Sync4jOperationStatus's command is meaningful only for items
      * that derive from a SyncML command. If the associated command is null,
      * there is no need to generate a status. Of course, also if the command was
      * flagged as "noResponse" no status is required.
      *
      * @param operationStatus the status object returned as the result of
      *        commands execution
      * @param msgId the message id
      * @param idGenerator the command id generator
      *
      * @return a <i>StatusCommand[]</i> array containing the status to return
      *         to the client
      */
    public static Status[]
    generateStatusCommands(SyncOperationStatus[] operationStatus,
                           String                msgId          ,
                           CommandIdGenerator    idGenerator    ) {
        HashMap<StatusKey, ArrayList<SyncItemKey>>   statusMap = new HashMap<StatusKey, ArrayList<SyncItemKey>>();

        ArrayList<SyncItemKey> itemList  = null;
        StatusKey key       = null;

        ModificationCommand cmd = null;
        for (int i=0; i < operationStatus.length ; ++i) {

            cmd = ((Sync4jOperationStatus)operationStatus[i]).getCmd();

            if ((cmd == null) || (cmd.isNoResp())) {
                continue; // skip the operation status
            }

            //
            // Check if a status command has been already created for the given
            // original command and status code. If it is found, than a new
            // item is added to the existing ones, otherwise a new item list
            // is created
            //
            key = new StatusKey(
                      cmd,
                      operationStatus[i].getStatusCode()
                  );

            itemList = statusMap.get(key);

            if (itemList == null){
                itemList = new ArrayList<SyncItemKey>();
                statusMap.put(key, itemList);
            }
            //
            // If the item is mapped then used the mapped key like SourceRef.
            //
            SyncItemKey keyA    =
                operationStatus[i].getOperation().getSyncItemA().getKey();
            SyncItemKey mapKeyA =
                operationStatus[i].getOperation().getSyncItemA().getMappedKey();
            itemList.add(mapKeyA == null ? keyA : mapKeyA);
        }

        //
        // Now we can loop over the map and create a list of status commands.
        //
        ArrayList<Status> ret = new ArrayList<Status>();

        Item[] items = null;

        Iterator<StatusKey> i = statusMap.keySet().iterator();
        while(i.hasNext()) {
            key = i.next();

            itemList = statusMap.get(key);

            items = new Item[itemList.size()];

            for (int j = 0; j<items.length; ++j) {
                items[j] = new Item(
                               null,
                               new Source(itemList.get(j).getKeyAsString()),
                               null,
                               null,
                               false
                           );

            }
            ret.add(
                new Status(
                    idGenerator.next()                       ,
                    msgId                                    ,
                    key.cmd.getCmdID().getCmdID()            ,
                    key.cmd.getName()                        ,
                    null                                     ,
                    (items.length == 1)                      ?
                    new SourceRef(items[0].getSource())      :
                    null                                     ,
                    null /* credential */                    ,
                    null /* challenge  */                    ,
                    new Data(key.statusCode)                 ,
                    (items.length > 1)                       ?
                    items                                    :
                    new Item[0]
                )
           );
        }
        return ret.toArray(new Status[ret.size()]);
    }

    /**
     * Translates an array of <i>SyncOperation</i> objects to an array of
     * <i>(Add,Delete,Replace)Command</i> objects. Only client side operations
     * are translated.
     *
     * @param clientMapping the associated existing client mapping
     * @param operations the operations to be translated
     * @param sourceName the corresponding source name
     * @param idGenerator the ID generator for command ids
     *
     * @return the commands corresponding to <i>operations</i>
     */
    public static
    ItemizedCommand[] operationsToCommands(ClientMapping      clientMapping,
                                           SyncOperation[]    operations   ,
                                           String             sourceName   ,
                                           CommandIdGenerator idGenerator  ) {
        ArrayList<ItemizedCommand> commands = new ArrayList<ItemizedCommand>();

        SyncItem item = null;
        for (int i=0; ((operations != null) && (i<operations.length)); ++i) {

            if (log.isEnabled(Level.TRACE)) {
                log.trace( "Converting the operation\n"
                          + operations[i]
                          + "\nfor the source "
                          + sourceName
                          );
            }

            item = operations[i].getSyncItemB(); // this is the server-side item

            if (operations[i].isAOperation() && (item != null)) {

                char op = operations[i].getOperation();

                //
                // Should not translates a NOP operation
                //
                if (op == SyncOperation.NOP) {
                    continue;
                }
                commands.add(operationToCommand(clientMapping, operations[i], idGenerator));
            }
        }

        return commands.toArray(new ItemizedCommand[0]);
    }

    /**
     * Translates a <i>SyncOperation</i> object to a <i>(Add,Delete,Replace)Command</i>
     * object.
     *
     * @param clientMapping the item ids mapping
     * @param operation the operation to be translated
     * @param idGenerator the id generator to use to create command ids
     *
     * @return the command corresponding to <i>operation</i>
     */
    public static
    ItemizedCommand operationToCommand(ClientMapping      clientMapping,
                                       SyncOperation      operation    ,
                                       CommandIdGenerator idGenerator  ) {
        ItemizedCommand cmd = null;

        if (idGenerator == null) {
            if(log.isEnabled(Level.FATAL)){
                log.fatal("idGenerator is null. Cannot continue");
            }

            throw new NullPointerException("idGenerator cannot be null!");
        }

        char op = operation.getOperation();

        //
        // The item key must reflect the value known by the client agent. It
        // thus must be adjusted using the client mapping, but if the
        // operation is an addition. In this case the client key is generated
        // by the engine (the client will provide the right key with a
        // subsequent Map command).
        //
        String itemKey = operation.getSyncItemB().getKey().getKeyAsString();
        if (op != SyncOperation.NEW && op != SyncOperation.CONFLICT) {
            itemKey = clientMapping.getMappedValueForGuid(itemKey);
        }
        assert (itemKey != null);

        //
        // The following rules apply:
        // - If the operation is an addition, Targer MUST NOT be included.
        // - If the operation is not an addition, Source MUST NOT be included.
        // - If the operation is not a deletion, Data element MUST be used to carry data ifself
        //
        // TO DO: noResponse, credential
        //
        Meta m = null;
        if (op == SyncOperation.NEW) {
            m = new Meta();
            m.setType(operation.getSyncItemB().getSyncSource().getType());

            cmd = new Add(
                      idGenerator.next(),
                      false             ,
                      null              ,
                      m                 , // meta
                      new Item[] {
                          SyncItemHelper.toItem(
                              itemKey                 ,
                              operation.getSyncItemA(),
                              false                   ,
                              true                    ,
                              true
                          )
                      }
                  );
        } else  if (op == SyncOperation.DELETE) {
            cmd = new Delete(
                      idGenerator.next(),
                      false             ,
                      false             ,
                      false             ,
                      null              ,
                      null              , // meta
                      new Item[] {
                          SyncItemHelper.toItem(
                              itemKey,
                              operation.getSyncItemA(),
                              true,
                              false,
                              false
                          )
                      }
                  );
        } else  if (op == SyncOperation.UPDATE) {

            m = new Meta();
            m.setType(operation.getSyncItemB().getSyncSource().getType());
            cmd = new Replace(
                      idGenerator.next(),
                      false             ,
                      null              ,
                      m                 ,
                      new Item[] {
                          SyncItemHelper.toItem(
                              itemKey,
                              operation.getSyncItemA(),
                              true,
                              false,
                              true
                          )
                      }
                  );
        } else if (op == SyncOperation.CONFLICT) {
            //
            // Server wins!
            //
            // TO DO: implement other conflict resolution policies
            //
            if (operation.getSyncItemB().getSyncSource() != null) {
                m = new Meta();
                m.setType(operation.getSyncItemB().getSyncSource().getType());
            }

            if (operation.getSyncItemB().getState() == SyncItemState.NOT_EXISTING) {
                itemKey = operation.getSyncItemA().getKey().getKeyAsString();
            }

            //View the state of syncItemB and then say to client which the operation
            //to do in order to resolve the conflict
            char s = operation.getSyncItemB().getState();

            switch (s) {
                case SyncItemState.UPDATED:
                case SyncItemState.SYNCHRONIZED:
                    cmd = new Replace(
                              idGenerator.next(),
                              false             ,
                              null              ,
                              m                 ,
                              new Item[] {
                                  SyncItemHelper.toItem(
                                      itemKey,
                                      operation.getSyncItemA(),
                                      true,
                                      false,
                                      true
                                  )
                              }
                          );
                    break;
                case SyncItemState.DELETED:
                case SyncItemState.NOT_EXISTING:
                    cmd = new Delete(
                              idGenerator.next(),
                              false             ,
                              false             ,
                              false             ,
                              null              ,
                              null              , // meta
                              new Item[] {
                                  SyncItemHelper.toItem(
                                      itemKey,
                                      operation.getSyncItemA(),
                                      true,
                                      false,
                                      false
                                  )
                              }
                          );
                    break;
                case SyncItemState.NEW:
                    cmd = new Add(
                              idGenerator.next(),
                              false             ,
                              null              ,
                              m                 , // meta
                              new Item[] {
                                  SyncItemHelper.toItem(
                                      itemKey                 ,
                                      operation.getSyncItemA(),
                                      false                   ,
                                      true                    ,
                                      true
                                  )
                              }
                          );
                    break;
            }
        }

        return cmd;
    }

    /**
     * Converts an array of <i>Item</i> objects belonging to the same
     * <i>SyncSource</i> into an array of <i>SyncItem</i> objects.
     * <p>
     * The <i>Item</i>s created are enriched with an additional property
     * called as in SyncItemHelper.PROPERTY_COMMAND, which is used to bound the
     * newly created object with the original command.
     *
     * @param clientMapping the client mapping
     * @param syncSource the <i>SyncSource</i> items belong to - NOT NULL
     * @param cmd the modification command
     * @param state the state of the item as one of the values defined in
     *              <i>SyncItemState</i>
     * @param timestamp the timestamp to assign to the last even on this item
     *
     *
     * @return an array of <i>SyncItem</i> objects
     */
    public static SyncItem[] itemsToSyncItems(ClientMapping       clientMapping,
                                              SyncSource          syncSource   ,
                                              ModificationCommand cmd          ,
                                              char                state        ,
                                              long                timestamp    ) {

        Item[] items = cmd.getItems().toArray(new Item[0]);

        if ((items == null) || (items.length == 0)) {
            return new SyncItem[0];
        }

        SyncItem[] syncItems = new SyncItem[items.length];

        Data   d       = null;
        String content = null, key = null, mappedKey = null;
        for (int i=0; i<items.length; ++i) {
            d = items[i].getData();
            content = (d != null) ? d.getData() : "";

            key       = items[i].getSource().getLocURI();
            mappedKey = clientMapping.getMappedValueForLuid(key);

            //
            // NOTE: for the purpose of sync items comparison, the mappedKey,
            // when not null, becomes the real item key, whilst the old key
            // becomes the mapped key.
            //
            if (mappedKey != null) {
                String k = mappedKey;
                mappedKey = key;
                key = k;
            }
            syncItems[i] =
                new SyncItemImpl(syncSource, key, mappedKey, state);
            syncItems[i].setProperty(
                new SyncProperty(SyncItemHelper.PROPERTY_COMMAND, cmd)
            );
            syncItems[i].setProperty(
                new SyncProperty(SyncItem.PROPERTY_BINARY_CONTENT, content.getBytes())
            );
            syncItems[i].setProperty(
                new SyncProperty(SyncItem.PROPERTY_TIMESTAMP, new Timestamp(timestamp))
            );
        }

        return syncItems;
    }

    /**
     * Calculate the intersection between the given lists, returning a new list
     * of <i>SyncItemMapping</i>s each containing the corresponding item couple.
     * Items are matched using the <i>get()</i> method of the <i>List</i> object,
     * therefore the rules for matching items are the ones specified by tha
     * <i>List</i> interface.
     *
     * @param a the first list - NOT NULL
     * @param b the seconf list - NOT NULL
     *
     * @return the intersection mapping list.
     */
    public static List<Object> intersect(final List<Object> a, final List<Object> b) {
        int       n   = 0;
        SyncItem  itemA = null;
        ArrayList<Object> ret = new ArrayList<Object>();
        SyncItemMapping mapping = null;

        Iterator<Object> i = a.iterator();
        while(i.hasNext()) {
            itemA = (SyncItem)i.next();
            n = b.indexOf(itemA);
            if (n >= 0) {
                mapping = new SyncItemMapping(itemA.getKey());
                mapping.setMapping(itemA, (SyncItem)b.get(n));
                ret.add(mapping);
            }
        }

        return ret;
    }

    /**
     * Calculate and build the cross set Am(B-Bm) that is the set of synchronized
     * items of the source B interested by the modifications of source A.
     *
     * @param items source A items
     * @param source the B source
     * @param principal the pricipal data are related to
     *
     * @return a list of <i>SyncItemMapping</i> representing the item set
     */
    public static List<Object> buildAmBBm(List<Object>       items    ,
                                  SyncSource source   ,
                                  Principal  principal) {
        SyncItem itemA, itemB;
        SyncItemMapping mapping = null;

        ArrayList<Object> ret = new ArrayList<Object>();

        Iterator<Object> i = items.iterator();
        while (i.hasNext()) {
            itemA = (SyncItem)i.next();

            try {

                itemB = source.getSyncItemFromId(principal, itemA.getKey());

                if (itemB != null) {
                    itemB.setState(SyncItemState.SYNCHRONIZED);
                    mapping = new SyncItemMapping(itemA.getKey());
                    mapping.setMapping(itemA, itemB);
                    ret.add(mapping);
                }

            } catch (SyncSourceException e) {
                String msg = "Error retrieving an item by its key "
                           + itemA.getKey()
                           + " from source "
                           + source
                           + ": "
                           + e.getMessage();

                if(log.isEnabled(Level.FATAL)){
                    log.fatal(msg);
                }

                log.debug("buildAmBBm", e);
            }
        }

        return ret;
    }

     /**
     * Calculate and build the cross set (A-Am)Bm that is the set of synchronized
     * items of the source A interested by the modifications of source B.
     *
     * @param items source B items
     * @param source the A source
     * @param principal the pricipal data are related to
     *
     * @return a list of <i>SyncItemMapping</i> representing the item set
     */
    public static List<Object> buildAAmBm(List<Object>       items    ,
                                  SyncSource source   ,
                                  Principal  principal) {
        SyncItem itemA, itemB;
        SyncItemMapping mapping = null;

        ArrayList<Object> ret = new ArrayList<Object>();

        Iterator<Object> i = items.iterator();
        while (i.hasNext()) {
            itemB = (SyncItem)i.next();

            try {

                itemA = source.getSyncItemFromId(principal, itemB.getKey());

                if (itemA != null) {
                    itemA.setState(SyncItemState.SYNCHRONIZED);
                    mapping = new SyncItemMapping(itemB.getKey());
                    mapping.setMapping(itemA, itemB);
                    ret.add(mapping);
                }
            } catch (SyncSourceException e) {
                String msg = "Error retrieving an item by its key "
                           + itemB.getKey()
                           + " from source "
                           + source
                           + ": "
                           + e.getMessage();

                if(log.isEnabled(Level.FATAL)){
                    log.fatal(msg);
                }

                log.debug("buildAmBBm", e);
            }
        }

        return ret;
    }

    /**
     * Update the mapping given an array of operations. This is used for
     * mappings sent by the client.
     *
     * @param clientMappings the client mappings for current synchronization
     * @param operations the operation performed
     * @param slowSync is true if slow synchronization
     *
     */
    public static void updateClientMappings(Map<String, ClientMapping>             clientMappings,
                                            SyncOperation[] operations    ,
                                            boolean         slowSync      )
    throws MappingException {

        ClientMapping clientMapping = null;
        String guid = null, luid = null;
        SyncSource clientSource = null;

        for (int i=0; ((operations != null) && (i<operations.length)); ++i) {
            //
            // Ignore conflicts
            //
            if (operations[i] instanceof SyncConflict) {
                continue;
            }

            clientSource = operations[i].getSyncItemA().getSyncSource();
            if (clientSource == null) {
                //
                // clientSource is null for unexsisting items
                //
                continue;
            }

            clientMapping = clientMappings.get(clientSource.getSourceURI());

            //
            //this is the case of slow sync and without mappings into db
            //
            if (slowSync && operations[i].getOperation() != SyncOperation.DELETE) {
                if (operations[i].getSyncItemA() != null &&
                    operations[i].getSyncItemB() != null) {

                    guid = operations[i].getSyncItemB().getKey().getKeyAsString();

                    SyncItemKey sikA = operations[i].getSyncItemA().getMappedKey();
                    if (sikA != null) {
                        luid = sikA.getKeyAsString();
                    } else {
                        luid = operations[i].getSyncItemA().getKey().getKeyAsString();
                    }
                    clientMapping.updateMapping(luid , guid);
                    continue;
                }
            } else if (operations[i].getOperation() == SyncOperation.DELETE) {
                guid = operations[i].getSyncItemB().getKey().getKeyAsString();
                clientMapping.removeMappedValuesForGuid(guid);
            } else if (operations[i].getSyncItemA().getState() == SyncItemState.DELETED &&
                       operations[i].getSyncItemB().getState() == SyncItemState.DELETED ) {
                guid = operations[i].getSyncItemB().getKey().getKeyAsString();
                clientMapping.removeMappedValuesForGuid(guid);
            }
        }
    }

    /**
     * Updates the LUID-GUIDmapping for the client modifications (that is the
     * items directlry inserted or removed by the server).
     *
     * @param clientMappings the client mappings for current synchronization
     * @param status the status of the performed operations
     * @param slowSync is true if slow synchronization
     *
     */
    public static void updateServerMappings(Map<String, ClientMapping>                  clientMappings,
                                            SyncOperationStatus[] status       ,
                                            boolean slowSync                   )

    throws MappingException {
        char op;
        ClientMapping clientMapping = null;
        String luid = null, guid = null;
        SyncSource serverSource = null;
        SyncOperation operation;
        for (int i=0; ((status != null) && (i<status.length)); ++i) {
            operation = status[i].getOperation();
            op = operation.getOperation();

            //
            // Ignore conflicts
            //
            if (operation instanceof SyncConflict) {
                continue;
            }

            serverSource = status[i].getSyncSource();
            if (serverSource == null) {
                //
                // clientSource is null for unexsisting items
                //
                continue;
            }

            clientMapping = clientMappings.get(serverSource.getSourceURI());

            //
            //this is the case of slow sync and without mappings into db
            //
            if (slowSync &&
                operation.getOperation() != SyncOperation.DELETE &&
                operation.getOperation() != SyncOperation.NEW) {

                if (operation.getSyncItemA() != null &&
                    operation.getSyncItemB() != null) {

                    guid = operation.getSyncItemB().getKey().getKeyAsString();

                    SyncItemKey sikA = operation.getSyncItemA().getMappedKey();
                    if (sikA != null) {
                        luid = sikA.getKeyAsString();
                    } else {
                        luid = operation.getSyncItemA().getKey().getKeyAsString();
                    }
                    clientMapping.updateMapping(luid , guid);
                    continue;
                }
            } else if(op == SyncOperation.DELETE) {
                    guid = operation.getSyncItemB().getKey().getKeyAsString();
                    clientMapping.removeMappedValuesForGuid(guid);
            } else if (op == SyncOperation.NEW) {
                if (operation.isBOperation()) {
                    luid = operation.getSyncItemA().getKey().getKeyAsString();
                    guid = operation.getSyncItemB().getKey().getKeyAsString();
                    clientMapping.updateMapping(luid, guid);
                }
            } else if (operation.getSyncItemA().getState() == SyncItemState.DELETED &&
                       operation.getSyncItemB().getState() == SyncItemState.DELETED ) {
                guid = operation.getSyncItemB().getKey().getKeyAsString();
                clientMapping.removeMappedValuesForGuid(guid);
            }
        }
    }

    /**
     * Creates a <i>DataStore</i> from a <i>SyncSourceInfo</i>
     *
     * @param uri the source URI
     * @param info the <i>SyncSourceInfo</i>
     *
     * @return the corresponding <i>DataStore</i>
     */
    public static DataStore toDataStore(String uri, SyncSourceInfo info) {
        ContentTypeInfo[] supportedContents = null;
        ContentTypeInfo   preferredContent  = null;

        if (info != null) {
            ContentType[] supportedTypes = info.getSupportedTypes();
            ContentType   preferredType  = info.getPreferredType() ;

            supportedContents =
                new ContentTypeInfo[supportedTypes.length];

            for (int i=0; (i<supportedTypes.length); ++i) {
                supportedContents[i] = new ContentTypeInfo (
                    supportedTypes[i].type,
                    supportedTypes[i].version
                );
            }

            preferredContent =  new ContentTypeInfo(
                preferredType.type,
                preferredType.version
            );
        }

        return new DataStore(
            new SourceRef(uri),
            null,
            32,
            preferredContent ,
            supportedContents,
            preferredContent ,
            supportedContents,
            null,
            new SyncCap(SyncType.ALL_SYNC_TYPES)
        );
    }

    /**
     * Resets the item state of the items passed in the given collection to
     * the <i>synchronized</i> state.
     *
     * @param items items collection
     */
    public static void resetState(Collection<SyncItem> items) {
        if (items == null) {
            return;
        }

        Iterator<SyncItem> i = items.iterator();
        while (i.hasNext()) {
            Object obj = i.next();
            if (obj != null) {
                ((SyncItem)obj).setState(SyncItemState.SYNCHRONIZED);
            }
        }
    }
}

class StatusKey {
    public ModificationCommand  cmd = null;
    public int statusCode = 0;

    public StatusKey(ModificationCommand cmd, int statusCode) {
        this.cmd = cmd;
        this.statusCode = statusCode;
    }

    public boolean equals(Object o){
        if ((o != null) && (o instanceof StatusKey)) {
            return (((StatusKey)o).cmd.getCmdID().equals(this.cmd.getCmdID()))
                && (((StatusKey)o).statusCode == this.statusCode);
        }

        return false;
    }

    public int hashCode() {
        return ( String.valueOf(cmd.getCmdID().getCmdID())
               + String.valueOf(statusCode)
               ).hashCode();
    }
}
