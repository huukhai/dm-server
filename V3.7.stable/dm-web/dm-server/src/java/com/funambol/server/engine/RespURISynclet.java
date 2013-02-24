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


import com.funambol.framework.core.SyncML;
import com.funambol.framework.core.Sync4jException;
import com.funambol.framework.config.Configuration;
import com.funambol.framework.config.ConfigurationConstants;
import com.funambol.framework.engine.pipeline.OutputMessageProcessor;
import com.funambol.framework.engine.pipeline.MessageProcessingContext;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/**
 * This class implements a synclet that adds the RespURI tag to the message.
 *
 *.com
 */
public class RespURISynclet
implements OutputMessageProcessor {
    // --------------------------------------------------------------- Constants

    public static final String PARAM_SESSION_ID = "sid";

    // ------------------------------------------------------------ Private data

    private static final Logger log = Logger.getLogger(com.funambol.server.engine.RespURISynclet.class.getName());

    // ------------------------------------------------------------ Constructors


    // -------------------------------------------------- OutputMessageProcessor

    public void postProcessMessage(MessageProcessingContext processingContext,
                                   SyncML                   message          )
    throws Sync4jException {
        Configuration config = Configuration.getConfiguration();

        String sessionId =
            (String)processingContext.getProperty(processingContext.PROPERTY_SESSIONID);

        if (sessionId == null) {
            if (log.isEnabledFor(Level.INFO)) {
                log.info(processingContext.PROPERTY_SESSIONID + " is null! Synclet ignored");
            }

            return;
        }

        String serverUri =
            config.getStringValue(ConfigurationConstants.CFG_SERVER_URI);

        message.getSyncHdr().setRespURI(
            serverUri                    +
            '?'                          +
            PARAM_SESSION_ID             +
            '='                          +
            sessionId
        );
    }
}
