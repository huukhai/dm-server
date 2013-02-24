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

package com.funambol.framework.transport.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/**
 * This class implements a <i>SyncHolder</i> cache. <br>
 * It holds an HashMap in its instance parameter <i>cache</i>.
 * <p>
 * An optimization performed in this object is about avoiding memory eating
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
 * @version $Id: SyncHolderCache.java,v 1.5 2007-06-19 08:16:17 luigiafassina Exp $
 */
public class SyncHolderCache {

    // --------------------------------------------------------------- Constants

    public static final long   CLEANING_PERIOD = 300000; // 5 mins
    public static final long   DEFAULT_TTL     = 300000; // 5 mins
    public static final String LOG_NAME        = "funambol.transport.http";

    private static final Logger log = Logger.getLogger(com.funambol.framework.transport.http.SyncHolderCache.class.getName());

    // ------------------------------------------------------------ Private data

    private long holderTimeToLive = DEFAULT_TTL;

    private long lastCleaningTimestamp = 0;

    private Map<Object, SyncHolder> cache;

    // ------------------------------------------------------------ Constructors


    public SyncHolderCache(long timeToLive) {
        cache = new HashMap<Object, SyncHolder>(97);

        lastCleaningTimestamp = System.currentTimeMillis();

        holderTimeToLive = timeToLive;
    }

    // ------------------------------------------- Implementation of HolderCache

    public void put(SyncHolder holder) {
        clean();

        if (log.isEnabledFor(Level.TRACE)) {
            log.trace("Caching " + holder.getSessionId() + '(' + holder + ')');
        }

        synchronized (cache) {
            cache.put(holder.getSessionId(), holder);
        }
    }

    public SyncHolder get(String sessionId) {
        return cache.get(sessionId);
    }

    public void remove(String sessionId) {
        if (log.isEnabledFor(Level.TRACE)) {
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

        if (log.isEnabledFor(Level.TRACE)) {
            log.trace("Cleaning procedure..."                          );
            log.trace("now: "                   + now                  );
            log.trace("CLEANING_PERIOD: "       + CLEANING_PERIOD      );
            log.trace("lastCleaningTimestamp: " + lastCleaningTimestamp);
            log.trace("holderTimeToLive: "      + holderTimeToLive     );
        }

        ArrayList<Object> toBeRemoved = null;
        SyncHolder h;
        Object key, value;
        Iterator<Object> i = null;

        synchronized (cache) {

            if ((now - lastCleaningTimestamp) <= CLEANING_PERIOD) {
                if(log.isEnabledFor(Level.TRACE)){
                    log.trace("No purging required");
                }

                return false;
            }

            if(log.isEnabledFor(Level.TRACE)){
                log.trace("Performing purging...");
            }

            toBeRemoved = new ArrayList<Object>();

            i = cache.keySet().iterator();
            while (i.hasNext()) {
                key = i.next();
                value = cache.get(key);

                if (! (value instanceof SyncHolder)) {
                    // You shouldn't be here matey!!!
                    if (log.isEnabledFor(Level.TRACE)) {
                        log.trace("Found unexpected object: " + key + " will be removed!");
                    }
                    toBeRemoved.add(key);
                    continue;
                }
                h = (SyncHolder)value;
                if ( (now - h.getCreationTimestamp()) > holderTimeToLive) {
                    if(log.isEnabledFor(Level.TRACE)){
                        log.trace("Purging holder for session " + key);
                    }

                    try {
                        h.close();
                    } catch (Exception e) {
                        log.fatal(e.getMessage());
                        log.debug("clean", e);
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
