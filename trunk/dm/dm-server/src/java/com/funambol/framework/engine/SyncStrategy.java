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

package com.funambol.framework.engine;

import java.security.Principal;
import java.sql.Timestamp;

import com.funambol.framework.engine.source.SyncSource;


/**
 * It defines the interface of a Synchronization Strategy object. <br>
 * It implements the <i>Strategy</i> partecipant of the Strategy Pattern.
 * <p>
 * It is usually called by the synchronization engine when a syncronization
 * action has to be performed.
 * </p>
 * There are two types of synchronization process: slow and fast.
 * <h2>Slow synchronization</h2>
 * A slow synchronization is when the sources to be synchronized must be fully
 * compared in order to reconstruct the right images of the data on both
 * sources. The way the sets of items are compared is implementation specific
 * and can vary from comparing just the key or the entire content of a SyncItem.
 * In fact, in order to decide if two sync items are exactly the same or some
 * filed has changed, all fields might riquire a comparison.<br>
 * A slow sync is prepared calling <i>prepareSlowSync(...)</i>
 *
 * <h2>Fast synchronization</h2>
 * In the case of fast synchronization, the sources are queried only for new,
 * deleted or updated items since a given point in time. In this case the status
 * of the items can be checked in order to decide when a deeper comparison is
 * required.<br>
 * A fast sync is prepared calling <i>prepareFastSync(...)</i>
 *
 * <h2>Synchronization principal</h2>
 * <i>prepareXXXSync()</i> requires an additional <i>java.security.Principal</i>
 * parameter in input. The meaning of this parameter is implementation specific,
 * but as general rule, it is used to operate on the data specific for a given
 * entity such as a user, an application, a device, ecc.
 *
 * @see com.funambol.framework.engine.source.SyncSource
 * @see com.funambol.framework.engine.SyncEngine
 *
 *
 *
 * @version $Id: SyncStrategy.java,v 1.2 2006/08/07 21:09:19 nichele Exp $
 */
public interface SyncStrategy {
    /**
     * Fired when a synchornization action must be performed
     *
     * @param syncOperations the synchronization operations
     */
    SyncOperationStatus[] sync(SyncOperation[] syncOperations) throws SyncException ;

    /**
     * Fired when a slow synchronization action must be prepared.
     *
     * @param sources the sources to be synchronized
     * @param principal the entity for which the synchronization is required
     *
     * @return an array of SyncOperation, one for each SyncItem that must be
     *         created/updated/deleted or in conflict.
     */
    SyncOperation[] prepareSlowSync(SyncSource[] sources, Principal principal)
    throws SyncException;

    /**
     * Fired when a fast synchronization action must be prepared.
     *
     * @param sources the sources to be synchronized
     * @param principal the entity for which the synchronization is required
     * @param since look for data earlier than this timestamp
     *
     * @return an array of SyncOperation, one for each SyncItem that must be
     *         created/updated/deleted or in conflict.
     */
    SyncOperation[] prepareFastSync(SyncSource[] sources  ,
                                    Principal    principal,
                                    Timestamp    since    )
    throws SyncException;

    /**
     * Fired when a synchronization action must be finished.
     */
    void endSync() throws SyncException ;
}
