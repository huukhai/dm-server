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

import com.funambol.framework.core.Sync4jException;
import com.funambol.framework.security.Sync4jPrincipal;


/**
 * A synchronization engine represents a mechanism used to drive the syncrhonization
 * process.<br>
 * <i>SyncEngine</i> represents the Context partecipant of the Strategy pattern.<br>
 * It is a sort of factory and manager for the starategy object referenced by
 * the property <i>strategy</i> (that implementing classes must provide).<p>
 * <i>SyncEngine</i> concentrate all the implementation specific information
 * regarding strategies, sources, databases, services, etc. It is the point of
 * contact between the synchronization, protocol and server services.
 * <p>
 * When a synchronization process must take place, the <i>SyncEngine</i> will
 * pass the control to the configured strategy, which has the responsability of
 * query item sources in order to define which synchronization operations are
 * required. Then the synchronization operations are applied to the sources.
 *
 * @see SyncStrategy
 *
 *
 *
 * @version $Id: SyncEngine.java,v 1.2 2006/08/07 21:09:19 nichele Exp $
 */
public interface SyncEngine {

    /**
     * Get the underlying strategy
     *
     * @return the underlying strategy
     */
    public SyncStrategy getStrategy();

    /**
     * Set the synchronization strategy to be used
     */
    public void setStrategy(SyncStrategy strategy);

    /**
     * Fires and manages the synchronization process.
     *
     * @throws Sync4jException in case of error
     */
    public void sync(Sync4jPrincipal principal) throws Sync4jException;
}
