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


import java.security.Principal;
import java.sql.Timestamp;

import com.funambol.framework.engine.SyncItem;
import com.funambol.framework.engine.SyncItemKey;


/**
 * A <i>SyncSource</i> is responsible for the storing and retrieving of
 * <i>SyncItem</i> objects. It is used also for getting newly created or removed
 * <i>SyncItem</i>s.<br>
 * Note that the <i>SyncSource</i> inteface doesn't make any assumption about the
 * underlying data source. Each concrete implementation will use the storage its
 * specific database.
 *
 *
 *
 * @version $Id: SyncSource.java,v 1.2 2006/08/07 21:09:20 nichele Exp $
 */
public interface SyncSource {

    // ---------------------------------------------------------- Public Methods

    /**
     * Returns the name of the source
     *
     * @return the name of the source
     */
    public String getName();

    /**
     * Returns the source URI
     *
     * @return the absolute URI of the source
     */
    public String getSourceURI();

    /**
     * Returns the type of the source (i.e. text/x-vcard)
     *
     * @return the type of the source
     */
    public String getType();

    /**
     * Returns the type info of the content handled by this source
     *
     * @return the type info of the content handled by this source
     */
    public SyncSourceInfo getInfo();

    /**
     * Called before any other synchronization method. To interrupt the sync
     * process, throw a SyncSourceException.
     *
     * @param principal the principal for which the data has to be considered.
     *                  Null means all principals
     * @param syncMode the synchronization mode being performed
     *
     * @throws SyncSourceException to interrupt the process with an error
     */
    void beginSync(Principal principal, int syncMode) throws SyncSourceException;

    /**
     * Called after the modifications have been applied and just before commit
     * the source synchronization. To interrupt the committing, throw a
     * SyncSourceException.
     *
     * @param principal the principal for which the data has to be considered.
     *                  Null means all principals
     *
     * @throws SyncSourceException to interrupt the process with an error
     */
    void endSync(Principal principal) throws SyncSourceException;

    /**
     * @param principal the principal for which the data has to be considered.
     *                  Null means all principals
     * @param since consider the changes since this point in time. Null means
     *              all items regardless when they was changed.
     *
     * @return an array of keys containing the <i>SyncItem</i>'s key of the updated
     *         items after the last synchronizazion. It MUST NOT return null for
     *         no keys, but instad an empty array.
     */
    public SyncItemKey[] getUpdatedSyncItemKeys(Principal principal,
                                                Timestamp since    )
    throws SyncSourceException ;

    /**
     * @param principal the principal for which the data has to be considered
     *                  Null means all principals
     * @param since consider the changes since this point in time. Null means
     *              all items regardless when they was changed.
     *
     * @return an array of keys containing the <i>SyncItem</I>'s key of the updated
     *         items after the last synchronizazion. It MUST NOT return null for
     *         no keys, but instad an empty array.
     */
    public SyncItem[] getUpdatedSyncItems(Principal principal,
                                          Timestamp since    )
    throws SyncSourceException ;

    /**
     * Replaces an existing <i>SyncItem</i> or adds a new <i>SyncItem</i> if it
     * does not exist. The item is also returned giving the opportunity to the
     * source to modify its content and return the updated item (i.e. updating
     * the id to the GUID).
     *
     * @param principal the entity that wants to do the operation
     * @param syncInstance  the bean to replace/add
     *
     * @return the inserted/updated item
     *
     * @throws SyncSourceException in case of error (for instance if the
     *         underlying data store runs into problems)
     */
    public SyncItem setSyncItem(Principal principal, SyncItem syncInstance)
    throws SyncSourceException ;

    /**
     * Replaces existing SyncItems or adds new SyncItems if they do not
     * exist.
     *
     * @param principal the entity that wants to do the operation
     * @param syncItems  the beans to be replaced/added
     *
     * @return the inserted/updated
     *
     * @throws SyncSourceException in case of error (for instance if the
     *         underlying data store runs into problems)
     */
    public SyncItem[] setSyncItems(Principal principal, SyncItem[] syncItems)
    throws SyncSourceException ;

    /**
     * Removes a SyncItem given its key.
     *
     * @param principal the entity that wants to do the operation
     * @param syncItem  the item to remove
     *
     * @throws SyncSourceException in case of error (for instance if the
     *         underlying data store runs into problems)
     */
    public void removeSyncItem(Principal principal, SyncItem syncItem)
    throws SyncSourceException ;

    /**
     * Removes the given <i>SyncItem</i>s.
     *
     * @param principal the entity that wants to do the operation
     * @param syncItems  the items to remove
     *
     * @throws SyncSourceException in case of error (for instance if the
     *         underlying data store runs into problems)
     */
    public void removeSyncItems(Principal principal, SyncItem[] syncItems)
    throws SyncSourceException;

    /**
     * @param principal the principal for which the data has to be considered
     *                  Null means all principals
     * @param since consider the changes since this point in . Null means
     *              all items regardless when they was changed.
     *
     * @return an array of keys containing the <i>SyncItem</i>'s key of the newly
     *         created items after the last synchronizazion. If there are no new
     *         items an empty array MUST BE returned.
     *
     * @throws SyncSourceException in case of error (for instance if the
     *         underlying data store runs into problems)
     */
    public SyncItemKey[] getNewSyncItemKeys(Principal principal,
                                            Timestamp since    )
    throws SyncSourceException ;

    /**
     * @param principal the principal for which the data has to be considered
     *                  Null means all principals
     * @param since consider the changes since this point in time. Null means
     *              all items regardless when they was changed.
     *
     * @return an array of keys containing the <i>SyncItem</i>'s key of the newly
     *         created items after the last synchronizazion. If there are no new
     *         items an empty array is returned.
     *
     * @throws SyncSourceException in case of error (for instance if the
     *         underlying data store runs into problems)
     */
    public SyncItem[] getNewSyncItems(Principal principal,
                                      Timestamp since    )
    throws SyncSourceException ;

    /**
     * @param principal the principal for which the data has to be considered
     *                  Null means all principals
     * @param since consider the changes since this point in time. Null means
     *              all items regardless when they was changed.
     *
     * @return an array of keys containing the <i>SyncItem</i>'s key of the deleted
     *         items after the last synchronizazion. If there are no deleted
     *         items an empty array is returned.
     *
     * @throws SyncSourceException in case of error (for instance if the
     *         underlying data store runs into problems)
     */
    public SyncItemKey[] getDeletedSyncItemKeys(Principal principal,
                                                Timestamp since    )
    throws SyncSourceException ;

    /**
     * @param principal the principal for which the data has to be considered
     *                  Null means all principals
     * @param since consider the changes since this point in time. Null means
     *              all items regardless when they was changed.
     *
     * @return an array of keys containing the <i>SyncItem</i>'s key of the deleted
     *         items after the last synchronizazion. If there are no deleted
     *         items an empty array is returned.
     *
     * @throws SyncSourceException in case of error (for instance if the
     *         underlying data store runs into problems)
     */
    public SyncItem[] getDeletedSyncItems(Principal principal,
                                          Timestamp since    )
    throws SyncSourceException ;

    /**
     * @param principal the principal for which the data has to be considered
     *                  Null means all principals
     *
     * @return an array of all <i>SyncItem</i>s stored in this source. If there
     *         are no items an empty array is returned.
     *
     * @throws SyncSourceException in case of error (for instance if the
     *         underlying data store runs into problems)
     */
    public SyncItem[] getAllSyncItems(Principal principal)
    throws SyncSourceException;

    /**
     * @return an array containing the <i>SyncItem</i>s corresponding to the given
     *         array of keys. Unexisting keys must be silently ignored.
     *
     * @param principal look for data belonging to this principal, or null for
     *        not user-related searching
     * @param syncItemKeys  the keys of the SyncItems to return
     *
     * @throws SyncSourceException in case of error (for instance if the
     *         underlying data store runs into problems)
     */
    public SyncItem[] getSyncItemsFromIds(Principal principal, SyncItemKey[] syncItemKeys) throws SyncSourceException;

    /**
     * @return return the <i>SyncItem</i>s corresponding to the given
     *         key. If no item is found, null is returned
     *
     * @param principal look for data belonging to this principal, or null for
     *        not user-related searching
     * @param syncItemKey  the key of the SyncItem to return
     *
     * @throws SyncSourceException in case of errors (for instance if the
     *         underlying data store runs into problems)
     */
    public SyncItem getSyncItemFromId(Principal principal, SyncItemKey syncItemKey) throws SyncSourceException;

    /**
     * @return an item from a twin item. Each source implementation is free to
     *         interpret this as it likes (i.e.: comparing all fields).
     *
     * @param principal look for data belonging to this principal, or null for
     *        not user-related searching
     * @param syncItem the twin item
     *
     * @throws SyncSourceException in case of errors (for instance if the
     *         underlying data store runs into problems)
     */
    public SyncItem getSyncItemFromTwin(Principal principal, SyncItem syncItem) throws SyncSourceException;

    /**
     * @return an array of items from corresponding twin items. Each source
     *         implementation is free to interpret this as it likes (i.e.:
     *         comparing all fields).
     * @param principal look for data belonging to this principal, or null for
     *        not user-related searching
     * @param syncItems the twin items
     *
     * @throws SyncSourceException in case of errors (for instance if the
     *         underlying data store runs into problems)
     */
    public SyncItem[] getSyncItemsFromTwins(Principal principal, SyncItem[] syncItems) throws SyncSourceException;
}
