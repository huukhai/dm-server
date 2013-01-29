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

package com.funambol.framework.server.store;

import java.util.Map;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;

/**
 * This is a base class for <i>PersistenStore</i> objects. It does not persist
 * any object, but it provides services common to concrete implementations.
 *
 *
 *
 * @version $Id: BasePersistentStore.java,v 1.2 2006/08/07 21:09:23 nichele Exp $
 *
 */
public abstract class BasePersistentStore {

    // --------------------------------------------------------------- Constants

    public static final String
    CONFIG_JNDI_DATA_SOURCE_NAME = "jndi-data-source-name";

    /**
     * Logger
     */
    protected transient static final Logger log = Logger.getLogger(com.funambol.framework.server.store.BasePersistentStore.class.getName());

    // -------------------------------------------------------------- Properties

    /**
     * The JNDI name of the datasource to be used
     */
    protected String jndiDataSourceName = null;

    public String getJndiDataSourceName() {
        return this.jndiDataSourceName;
    }

    public void setJndiDataSourceName(String jndiDataSourceName) throws PersistentStoreException {
        this.jndiDataSourceName = jndiDataSourceName;

        if (jndiDataSourceName == null) {
            dataSource = null;
        }

        try {
            InitialContext ctx = new InitialContext();
            dataSource = (DataSource) ctx.lookup(jndiDataSourceName);
        } catch (NamingException e) {
            throw new PersistentStoreException("Data source "
            + jndiDataSourceName
            + " not found"
            , e
            );
        }
    }

    // ---------------------------------------------------------- Protected data

    protected transient DataSource dataSource = null;

    // ------------------------------------------------------------ Constructors

    // ---------------------------------------------------------- Public methods

    /** Configures the persistent store
     *
     * @param config an <i>Map</i> containing configuration parameters.
     *
     * @throws ConfigPersistentStoreException
     *
     */
    public void configure(Map config) throws ConfigPersistentStoreException {

        checkConfigParams(config);

        try {
            setJndiDataSourceName((String) config.get(CONFIG_JNDI_DATA_SOURCE_NAME));
        } catch (PersistentStoreException e) {
            throw new ConfigPersistentStoreException( "Error creating the datasource: "
                                                    + e.getMessage()
                                                    , e
                                                    );
        }
    }

    // ------------------------------------------------------- Protected methods

    // ------------------------------------------------------- Protected methods

    // --------------------------------------------------------- Private methods

    /**
     * Checks if the given configuration parameters contain all required
     * parameters. If not a <i>ConfigPersistentStoreException</i> is thrown.
     *
     * @param config the <i>Map</i> containing the configuration parameters
     *
     * @throws ConfigPersistentStoreException in case of missing parameters
     */
    private void checkConfigParams(Map config)
    throws ConfigPersistentStoreException {
        StringBuffer sb = new StringBuffer();

        if (StringUtils.isEmpty((String) config.get(CONFIG_JNDI_DATA_SOURCE_NAME))) {
            sb.append(CONFIG_JNDI_DATA_SOURCE_NAME);
        }
    }
}
