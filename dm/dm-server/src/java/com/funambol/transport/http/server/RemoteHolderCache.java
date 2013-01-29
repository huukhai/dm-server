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
package com.funambol.transport.http.server;

import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.ServletConfig;

import org.apache.commons.lang.StringUtils;
import org.jgroups.JChannelFactory;
import org.jgroups.blocks.DistributedHashtable;
import org.jgroups.log.Trace;

import com.funambol.framework.transport.http.SyncHolder;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;


/**
 * This class implements the remote reference cache. <br>
 * It holds the DistributedHashtable in its instance parameter cache.
 * <p>
 * Another optimization performed in this object is about avoiding memory eating
 * due to missing SyncHolders removing. This problem is solved with the
 * following actions:
 * <ul>
 *   <li> each SyncHolder stores a creation timestamp that is set in the
 *        constructor to the current system time millis;
 *   <li> HolderCache stores in the lastCleaningTimestamp static field the last
 *        time the cache was cleared;
 *   <li> If the the difference between the current time millis and the
 *        lastCleaningTimestamp is greater then the configured sessionTimeToLive,
 *        the cleaner process is executed;
 *   <li> the cleaner process iterates the sync holders in the cache and closes and removes the expired ones;
 * </ul>
 * <p>
 * NOTES:
 * <p>
 * 1. The cleaning process could be moved to a separate thread and executed
 *    asynchronously respect to the execution of servlet execution.
 *
 *
 * @version $Id: RemoteHolderCache.java,v 1.5 2007-06-19 08:16:26 luigiafassina Exp $
 */
public class RemoteHolderCache
implements Constants {

    // --------------------------------------------------------------- Constants

    public static final long CLEANING_PERIOD = 300000; // 5 mins

    private static final Logger log = Logger.getLogger(com.funambol.transport.http.server.RemoteHolderCache.class
.getName());

    // ------------------------------------------------------------ Private data

    private long holderTimeToLive = DEFAULT_TTL;

    private long lastCleaningTimestamp = 0;

    private String channelProps;


    private DistributedHashtable cache;

    // ------------------------------------------------------------ Constructors

    public RemoteHolderCache(ServletConfig config) {
        try {
            String group = config.getInitParameter(PARAM_GROUP);
            String props = config.getInitParameter(PARAM_CHANNEL_PROPERTIES);

            if (StringUtils.isEmpty(group)) {
                group = DEFAULT_GROUP;
            }

            //
            // Consitionally enable tracing (if the logging level includes
            // Level.FINE, enable it, otherwise not)
            //
            if (log.isEnabled(Level.TRACE)) {
                Trace.init();
            }

            if (log.isEnabled(Level.TRACE)) {
                log.trace("Multicast group: " + group);
                log.trace("Multicast properties: " + props);
            }

            JChannelFactory cf = new JChannelFactory();
            cache = new DistributedHashtable(group, cf, props, DEFAULT_TIMEOUT);

        } catch (Exception e) {
            if(log.isEnabled(Level.FATAL)){
                log.fatal(e.getMessage());
            }
            log.debug( "configure", e);
        }

        lastCleaningTimestamp = System.currentTimeMillis();

        holderTimeToLive =
            Long.parseLong(config.getInitParameter(PARAM_SESSION_TTL));

    }

    // ------------------------------------------- Implementation of HolderCache

    public void put(SyncHolder holder) {
        clean();
        if (log.isEnabled(Level.TRACE)) {
            log.trace("Caching " + holder.getSessionId() + '(' + holder + ')');
        }
        synchronized (cache) {
            cache.put(holder.getSessionId(), holder);
        }
    }

    public SyncHolder get(String sessionId) {
        return (SyncHolder)cache.get(sessionId);
    }

    public void remove(String sessionId) {
        if (log.isEnabled(Level.TRACE)) {
            log.trace("Removing holder for " + sessionId);
        }
        synchronized (cache) {
            cache.remove(sessionId);
        }
    }

    // ---------------------------------------------------- Other public methods

    public String toString() {
        String s = null;
        synchronized (cache) {
            s = String.valueOf(cache);
        }
        return s;
    }


    // --------------------------------------------------------- Private methods

    /**
     * If System.currentTimeMillis() - lastCleaningTimestamp is greater than
     * holderTimeToLive, purge old holders.
     *
     * @returns true if cleaning was performed, false otherwise
     */
    private boolean clean() {
        long now = System.currentTimeMillis();

        if (log.isEnabled(Level.TRACE)) {
            log.trace("Cleaning procedure..."                          );
            log.trace("now: " + now                                    );
            log.trace("CLEANING_PERIOD: " + CLEANING_PERIOD            );
            log.trace("lastCleaningTimestamp: " + lastCleaningTimestamp);
            log.trace("holderTimeToLive: " + holderTimeToLive          );
        }

        ArrayList<Object> toBeRemoved = null;

        RemoteEJBSyncHolder h;
        Object key, value;
        Iterator<Object> i = null;

        synchronized (cache) {

            if ((now - lastCleaningTimestamp) <= CLEANING_PERIOD) {
                if(log.isEnabled(Level.TRACE)){
                   log.trace("No purging required");
                }
                return false;
            }

            if(log.isEnabled(Level.TRACE)){
                log.trace("Performing purging...");
            }

            toBeRemoved = new ArrayList<Object>();

            i = cache.keySet().iterator();
            while (i.hasNext()) {
                key = i.next();
                value = cache.get(key);

                if (! (value instanceof RemoteEJBSyncHolder)) {
                    // You shouldn't be here matey!!!
                    if(log.isEnabled(Level.TRACE)){
                        log.trace("Found unexpected object:" + key + " will be removed!");
                    }
                    toBeRemoved.add(key);
                    continue;
                }
                h = (RemoteEJBSyncHolder)value;
                if ( (now - h.getCreationTimestamp()) > holderTimeToLive) {
                    if(log.isEnabled(Level.TRACE)){
                        log.trace("Purging holder for session " + key);
                    }
                    try {
                        h.close();
                    } catch (Exception e) {
                        if(log.isEnabled(Level.FATAL)){
                            log.fatal(e.getMessage());
                        }
                        log.debug( "clean", e);
                    }
                    toBeRemoved.add(key);
                }
            }


            //
            // Now remove the purged objects
            //
            i = toBeRemoved.iterator();
            while (i.hasNext()) {
                cache.remove(i.next());
            }
        }

        lastCleaningTimestamp = System.currentTimeMillis();

        return true;
    }
}
