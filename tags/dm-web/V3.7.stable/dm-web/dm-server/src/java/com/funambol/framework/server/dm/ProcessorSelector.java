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

package com.funambol.framework.server.dm;

import com.funambol.framework.engine.dm.ManagementProcessor;
import com.funambol.framework.engine.dm.DeviceDMState;
import com.funambol.framework.core.dm.ddf.DevInfo;


/**
 * This interface defines the structure of a Processor Selector. A Processor
 * selector is the component that implements the logic to select the specific
 * processor manager that should handle the management session.
 *
 *
 *
 * @version $Id: ProcessorSelector.java,v 1.4 2007-06-19 08:16:16 luigiafassina Exp $
 */
public interface ProcessorSelector {

    /**
     * Returns the processor manager that will handle the management session. <br>
     * The selection can be done accordingly to any logic and relevan data.
     * However, at least the device info obtained by the client are given.
     *
     * @param dms the device management state
     * @param devInfo the ./DevInfo standardized object provided by the client
     *
     * @return the processor manager that will handle the management session
     */
    public ManagementProcessor getProcessor(DeviceDMState dms, DevInfo devInfo);
}
