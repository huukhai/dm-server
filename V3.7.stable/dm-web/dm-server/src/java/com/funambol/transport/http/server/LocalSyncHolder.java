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


import java.io.File;
import java.io.IOException;

import java.net.URL;
import java.net.MalformedURLException;

import javax.naming.InitialContext;

import com.funambol.framework.config.ConfigClassLoader;
import com.funambol.framework.config.Configuration;
import com.funambol.framework.config.ConfigurationException;
import com.funambol.framework.transport.http.SyncHolder;
import com.funambol.framework.core.Sync4jException;
import com.funambol.framework.server.error.ServerException;
import com.funambol.framework.server.SyncResponse;

import com.funambol.server.engine.SyncAdapter;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/**
 * Implementes a <i>SyncHolder</i>.
 *
 *
 * @version $Id: LocalSyncHolder.java,v 1.2 2006/08/07 21:09:26 nichele Exp $
 */
public class LocalSyncHolder implements SyncHolder {

    // --------------------------------------------------------------- Constants

    private static final Logger log = Logger.getLogger(com.funambol.transport.http.server.LocalSyncHolder.class.getName());
    private static final String CONFIG_PATH_PREFIX = File.separator
                                                   + "config"
                                                   ;
    private static final String PROPERTY_CONFIG_PATH = "funambol.dm.home";

    // ------------------------------------------------------------ Private data

    private SyncAdapter syncAdapter       = null;
    private long        creationTimestamp       ;

    // ------------------------------------------------------------ Constructors
    public LocalSyncHolder() throws ServerException {
        syncAdapter = new SyncAdapter(loadConfiguration());
        creationTimestamp = System.currentTimeMillis();
    }

    // ---------------------------------------------------------- Public methods

    /** Processes an incoming message.
     *
     * @param requestData the SyncML request as stream of bytes
     * @param contentType the content type associated with the request
     * @param hmacHeader the hmac value associated with the request
     *
     * @return the SyncML response as a <i>ISyncResponse</i> object
     *
     * @throws ServerException in case of a server error
     *
     */
    public SyncResponse processMessage(byte[] requestData, String contentType, String hmacHeader)
    throws ServerException {
        return syncAdapter.processMessage(requestData, contentType, hmacHeader);
     }

    public void setSessionId(String sessionId) throws Sync4jException {
        syncAdapter.setSessionId(sessionId);
    }

    public String getSessionId() {
        return syncAdapter.getSessionId();
    }

    /** Called when the SyncHolder is not required any more. It gives the holder
     * an opportunity to do clean up and releaseing of resources.
     *
     * @throws java.lang.Exception in case of error. The real exception is stored
     * in the cause.
     *
     */
    public void close() throws Exception {
        syncAdapter.endSync();
    }

    /**
     * Returns the creation timestamp (in milliseconds since midnight, January
     * 1, 1970 UTC).
     *
     * @see com.funambol.framework.transport.http.SyncHolder
     */
    public long getCreationTimestamp() {
        return creationTimestamp;
    }

    // --------------------------------------------------------- Private methods

    /**
     * Loads the configuration for the server. The URI from which the configuration
     * is must be set as the envirnoment property named as <i>ENV_SERVER_CONFIG_URI</i>.
     *
     * @throws ConfigurationException in case of errors.
     */
    private Configuration loadConfiguration()
    throws ConfigurationException {
        URL configURI = null, configPath = null;
        Configuration config = Configuration.getConfiguration();

        String propertiesFile = null;
        try {
            InitialContext ctx = new InitialContext();

            //
            // The following two entries should be two URIs, but in the case
            // they are not (they do not start with http://, https:// or file://,
            // they are considered simple files
            //
            configPath = fixURI((String)System.getProperty(PROPERTY_CONFIG_PATH, ".") + CONFIG_PATH_PREFIX);

            config.setClassLoader(
                new ConfigClassLoader(
                    new URL[] { configPath },
                    getClass().getClassLoader()
                )
            );

            propertiesFile =
                configPath.toString() + File.separatorChar + "Funambol.properties";
            config.load(propertiesFile);

            if (log.isEnabledFor(Level.TRACE)) {
                log.trace("Configuration: " + config);
            }

            return config;
        } catch (ConfigurationException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            throw new ConfigurationException("Error loading configuration from "
                + propertiesFile, e);
        }
    }

    /**
     * Checks if the given string is an accepted URL (starting with http://,
     * https:// or file://). If yes, a new URL object representing the given
     * url is returned; otherwise, the given string is considered a file name
     * and a new URL is obtained calling File.toURL().
     *
     * @param s the string to check
     *
     * @return the corresponding URL if the string represents a URL or the
     *         fixed URL if the string is a pathname/filename
     *
     * @throws MalformedURLException
     */
    private URL fixURI(final String s)
    throws MalformedURLException {
        String minS = s.toLowerCase();

        try {
            return new URL(s);
        } catch (MalformedURLException e) {
            //
            // This is not a URL, let's consider it just a file
        }

        try {
            return new File(new File(s).getCanonicalPath()).toURI().toURL();
        } catch (IOException e) {
            throw new MalformedURLException("Unable to convert" + s + " to a URL");
        }
    }

}
