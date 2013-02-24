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

package com.funambol.framework.engine.source;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.security.Principal;
import java.sql.Timestamp;

import com.funambol.framework.engine.SyncProperty;
import com.funambol.framework.engine.SyncItemKey;
import com.funambol.framework.engine.SyncItem;
import com.funambol.framework.engine.SyncItemImpl;
import com.funambol.framework.engine.SyncItemState;


/*
 * This source represents a <i>SyncSource</i> where all items are kept in memory.
 * A <i>MemorySyncSource</i> can be created passing in the constructor the name
 * of the source and the items or creating an anonymous source with the default
 * constructor and then initialize the source with <i>initialize(...)</i>.
 *
 *
 *
 * @version $Id: MemorySyncSource.java,v 1.2 2006/08/07 21:09:20 nichele Exp $
 */
public class MemorySyncSource
extends AbstractSyncSource
implements SyncSource, java.io.Serializable {

    // --------------------------------------------------------------- Constants

    public static final String NAME = "MemorySyncSource";

    // ----------------------------------------------------- Private data member

    // the content of the SyncML client
    private ArrayList<SyncItem> unchangedSyncItems = new ArrayList<SyncItem>();
    private ArrayList<SyncItem> newSyncItems      = new ArrayList<SyncItem>();
    private ArrayList<SyncItem> updatedSyncItems  = new ArrayList<SyncItem>();
    private ArrayList<SyncItem> deletedSyncItems  = new ArrayList<SyncItem>();

    // ------------------------------------------------------------ Constructors

    public MemorySyncSource() {}

    public MemorySyncSource(String name, String type, String sourceURI) {
        super(name, type, sourceURI);
    }

    public MemorySyncSource(String name           ,
                            List<SyncItem> unchangedSyncItems,
                            List<SyncItem> deletedSyncItems ,
                            List<SyncItem> newSyncItems     ,
                            List<SyncItem> updatedSyncItems ) {
      //
      // The items are added so no need to clone the lists
      //
      super(name);
      initialize(unchangedSyncItems, deletedSyncItems, newSyncItems, updatedSyncItems);
    }

    // ---------------------------------------------------------- Public Methods

    /**
     * Sets the name of the source.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /*
    * @see SyncSource
    */

    public SyncItem setSyncItem(Principal principal, SyncItem syncItem)
    throws SyncSourceException {
        unchangedSyncItems.remove(syncItem);
        newSyncItems.remove      (syncItem);
        deletedSyncItems.add     (syncItem);

        updatedSyncItems.add(syncItem);

        SyncItem newSyncItem =
            new SyncItemImpl(this, syncItem.getKey().getKeyAsString(), SyncItemState.NEW);

        newSyncItem.setProperties(syncItem.getProperties());

        return newSyncItem;
    }

    /*
    * @see SyncSource
    */

    public SyncItem[] setSyncItems(Principal principal, SyncItem[] syncItems)
    throws SyncSourceException {
        SyncItem[] ret = new SyncItem[syncItems.length];
        for (int i=0; (i<syncItems.length); ++i) {
            ret[i] = setSyncItem(principal,  syncItems[i]);
        }  // next i

        return ret;
    }

    /*
     * @see SyncSource
     *
     * This implementation cycles through all SyncItems looking for the
     * specified key.
     */
    public SyncItem getSyncItemFromId(Principal principal, SyncItemKey syncItemKey)
    throws SyncSourceException {
        SyncItem[] all = this.getAllSyncItems(principal);

        for (int i=0; ((all != null) && (i<all.length)); ++i) {
            if (syncItemKey.equals(all[i].getKey())) {
                return all[i];
            }
        }

        return null;
    }

    /*
     * @see SyncSource
     *
     */
    public SyncItem[] getSyncItemsFromIds(Principal principal, SyncItemKey[] syncItemKeys)
    throws SyncSourceException {
        ArrayList<SyncItem> ret = new ArrayList<SyncItem>();
        SyncItem  syncItem = null;

        for (int i=0; ((syncItemKeys != null) && (i<syncItemKeys.length)); ++i) {
            syncItem = getSyncItemFromId(principal, syncItemKeys[i]);

            if (syncItem != null) {
                ret.add(syncItem);
            }
        }

        return ret.toArray(new SyncItem[ret.size()]);
    }

    /*
    * @see SyncSource
    */
    public SyncItem[] getUnchangedSyncItems(Principal principal, Timestamp since) throws SyncSourceException {
        return unchangedSyncItems.toArray(new SyncItem[0]);
    }

    /*
    * @see SyncSource
    */
    public SyncItemKey[] getUnchangedSyncItemKeys(Principal principal, Timestamp since) throws SyncSourceException {
        return extractKeys(unchangedSyncItems);
    }

    /*
    * @see SyncSource
    */
    public SyncItemKey[] getNewSyncItemKeys(Principal principal,
                                            Timestamp since    )
    throws SyncSourceException {
        return extractKeys(newSyncItems);
    }

    /*
    * @see SyncSource
    */
    public SyncItem[] getNewSyncItems(Principal principal,
                                      Timestamp since    )
    throws SyncSourceException {
        return newSyncItems.toArray(new SyncItem[0]);
    }

    /*
    * @see SyncSource
    */
    public SyncItemKey[] getDeletedSyncItemKeys(Principal principal,
                                                Timestamp since    )
    throws SyncSourceException {
        return extractKeys(deletedSyncItems);
    }

    /*
    * @see SyncSource
    */
    public SyncItem[] getDeletedSyncItems(Principal principal,
                                          Timestamp since    )
    throws SyncSourceException {
        return deletedSyncItems.toArray(new SyncItem[0]);
    }

    /**
     * @return an array of keys containing the <i>SyncItem</i>'s key of the updated
     *        items after the last synchronizazion.
     */
    public SyncItemKey[] getUpdatedSyncItemKeys(Principal principal,
                                                Timestamp since    )
    throws SyncSourceException {
        return extractKeys(updatedSyncItems);
    }

    /**
    * @see SyncSource
    */
    public SyncItem[] getUpdatedSyncItems(Principal principal,
                                          Timestamp since    )
    throws SyncSourceException {
        return updatedSyncItems.toArray(new SyncItem[0]);
    }

    /*
    * @see SyncSource
    */
    public void removeSyncItem(Principal principal, SyncItem syncItem)
    throws SyncSourceException {
        unchangedSyncItems.remove(syncItem);
        newSyncItems.remove      (syncItem);
        updatedSyncItems.remove  (syncItem);

        deletedSyncItems.add(syncItem);
    }

    /*
    * @see SyncSource
    */
    public void removeSyncItems(Principal principal, SyncItem[] syncItems)
    throws SyncSourceException {
        for (int i=0; i<syncItems.length; ++i) {
            removeSyncItem(principal, syncItems[i]);
        } // next i
    }

    /**
    * @see SyncSource
    */
    public SyncItem[] getAllSyncItems(Principal principal)
    throws SyncSourceException {
        SyncItem[] unchangedItems = getUnchangedSyncItems(principal, null);
        SyncItem[] newItems = getNewSyncItems(principal, null);
        SyncItem[] deletedItems = getDeletedSyncItems(principal, null);
        SyncItem[] updatedItems = getUpdatedSyncItems(principal, null);

        SyncItem[] allItems = new SyncItem[ unchangedItems.length
                                          + newItems.length
                                          + deletedItems.length
                                          + updatedItems.length
                                          ];
        int c = 0;
        for(int i=0; (i<unchangedItems.length); ++i) {
            allItems[c++] = unchangedItems[i];
        }
        for(int i=0; (i<newItems.length); ++i) {
            allItems[c++] = newItems[i];
        }
        for(int i=0; (i<deletedItems.length); ++i) {
            allItems[c++] = deletedItems[i];
        }
        for(int i=0; (i<updatedItems.length); ++i) {
            allItems[c++] = updatedItems[i];
        }

        return allItems;
    }

    /**
     * @see SyncSource
     */
    public boolean isModified(Principal principal, Timestamp since) {
        try {
            return (getDeletedSyncItems(principal, since).length > 0)
                || (getNewSyncItems(principal, since).length     > 0)
                || (getUpdatedSyncItems(principal, since).length > 0);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Initializes this MemorySyncSource with items.
     *
     * @param existingItems the existing items
     * @param deletedItems the deleted items
     * @param newItems the created items
     * @param updatedItems the updated items
     */
    public void initialize(List<SyncItem> existingItems,
                           List<SyncItem> deletedItems ,
                           List<SyncItem> newItems     ,
                           List<SyncItem> updatedItems ) {

       unchangedSyncItems.clear();
       deletedSyncItems.clear() ;
       newSyncItems.clear()     ;
       updatedSyncItems.clear() ;

       unchangedSyncItems.addAll(existingItems);
       deletedSyncItems.addAll(deletedItems)  ;
       newSyncItems.addAll(newItems)          ;
       updatedSyncItems.addAll(updatedItems ) ;

       //
       // We have to make sure that the complete view is consistent so that
       // new, modified or deleted items are not in the unchanged list; that
       // deleted items are not in new, update and unchanged lists, and so on.
       //
       SyncItem syncItem = null;
       Iterator<SyncItem> i = deletedSyncItems.iterator();
       while (i.hasNext()) {
           syncItem = i.next();

           unchangedSyncItems.remove(syncItem);
           newSyncItems.remove(syncItem)      ;
           updatedSyncItems.remove(syncItem)  ;
       }

       i = updatedSyncItems.iterator();
       while (i.hasNext()) {
           syncItem = i.next();

           unchangedSyncItems.remove(syncItem);
           newSyncItems.remove(syncItem)      ;
       }

       i = newSyncItems.iterator();
       while (i.hasNext()) {
           syncItem = i.next();

           unchangedSyncItems.remove(syncItem);
       }
    }


    /**
     * Search the items for an item with exactly the same binary content.
     *
     * @return an item from a twin item. Each source implementation is free to
     *         interpret this as it likes (i.e.: comparing all fields).
     *
     * @param principal
     * @param syncItemTwin the twin item
     *
     * @throws SyncSourceException in case of errors (for instance if the
     *         underlying data store runs into problems)
     *
     */
    public SyncItem getSyncItemFromTwin(Principal principal, SyncItem syncItemTwin)
    throws SyncSourceException {
        SyncItem[] syncItems =
            getSyncItemsFromTwins(principal, new SyncItem[] {syncItemTwin});

        if ((syncItems == null) || (syncItems.length == 0)) {
            return null; // not found
        }

        return syncItems[0];
    }

    /**
     * Two items are considered twin if their binary content, converted to
     * String are equal.
     *
     * @param principal
     * @param syncItemTwins the twin items
     *
     * @return an array of items from corresponding twin items.
     *
     * @throws SyncSourceException in case of errors (for instance if the
     *         underlying data store runs into problems)
     *
     */
    public SyncItem[] getSyncItemsFromTwins(Principal principal, SyncItem[] syncItemTwins)
    throws SyncSourceException {
        ArrayList<SyncItem> ret = new ArrayList<SyncItem>();

        String[] contents = new String[syncItemTwins.length];

        for (int i=0; i<syncItemTwins.length; ++i) {
            contents[i] = new String((byte[])syncItemTwins[i].getProperty(SyncItem.PROPERTY_BINARY_CONTENT).getValue());
        }

        SyncItem[] all = getAllSyncItems(principal);

        String content = null;
        SyncProperty prop = null;
        for (int i=0; i<all.length; ++i) {
            prop = all[i].getProperty(SyncItem.PROPERTY_BINARY_CONTENT);
            if (prop != null) {
                content = new String((byte[])prop.getValue());
            }

            for (int j=0; ((content != null) && (j<contents.length)); ++j) {

                if (content.equals(contents[j])) {
                    ret.add(all[i]);
                    break;
                }
            }
        }

        return ret.toArray(new SyncItem[ret.size()]);
    }

    // --------------------------------------------------------- Private methods

    private SyncItemKey[] extractKeys(Collection<SyncItem> syncItems) {
        SyncItemKey[] keys = new SyncItemKey[syncItems.size()];

        SyncItem syncItem = null;
        int j = 0;
        for(Iterator<SyncItem> i = syncItems.iterator(); i.hasNext(); ++j) {
           syncItem = i.next();

           keys[j] = syncItem.getKey();
        } // next i, j

        return keys;
    }

}
