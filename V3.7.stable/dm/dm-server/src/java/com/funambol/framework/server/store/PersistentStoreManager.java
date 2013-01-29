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
import java.util.HashMap;
import java.io.Serializable;

import com.funambol.framework.config.Configuration;
import com.funambol.framework.tools.beans.BeanFactory;
import com.funambol.framework.tools.beans.LazyInitBean;
import com.funambol.framework.tools.beans.BeanInitializationException;

/**
 * This class represents the main persistent store of the Funambol server. It
 * handles everything related to saving and reading information to and from the
 * database, delegating the work to other <i>PersistentStore</i>s if necessary.
 * <p>
 * <i>PersistentStoreManager</i> can be configured with a list of <i>PersistetStore</i>s
 * that are called in sequence until one of them can process the given object.<br>
 * This list is expressed in the form of the string array <i>stores</i>; each
 * string is the name of a bean (or a class) and is loaded by
 * <i>com.funambol.framework.tools.beans.BeanFramework</i>.
 * <p>
 *
 * @version $Id: PersistentStoreManager.java,v 1.2 2006/08/07 21:09:23 nichele Exp $
 */
public class PersistentStoreManager
implements PersistentStore, LazyInitBean, Serializable {

    // --------------------------------------------------------------- Constants

    public static final String
    CONFIG_CLASS_LOADER          = "class-loader";

    public static final String
    CONFIG_JNDI_DATA_SOURCE_NAME = "jndi-data-source-name";

    public static final String
    CONFIG_USERNAME              = "username";

    public static final String
    CONFIG_PASSWORD              = "password";

    // ------------------------------------------------------------ Private data

    private PersistentStore persistentStores[] = null;

    // ------------------------------------------------------------ Constructors

    // -------------------------------------------------------------- Properties

    /**
     * The persistent stores handled by this manager
     */
    private String[] stores = null;

    /** Getter for property stores.
     * @return Value of property stores.
     *
     */
    public String[] getStores() {
        return this.stores;
    }

    /** Setter for property stores.
     * @param stores New value of property stores.
     *
     */
    public void setStores(String[] stores) {
        this.stores = stores;
    }

    /**
     * The JNDI name of the datasource to be used
     */
    private String jndiDataSourceName = null;

    public String getJndiDataSourceName() {
        return this.jndiDataSourceName;
    }

    public void setJndiDataSourceName(String jndiDataSourceName) {
        this.jndiDataSourceName = jndiDataSourceName;
    }

    /**
     * The database user
     */
    private String username = null;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * The database password
     */
    private String password = null;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // ---------------------------------------------------------- Public methods

    public boolean store(Object o)
    throws PersistentStoreException {
        for (int i=0; ((persistentStores != null) && (i<persistentStores.length)); ++i) {
            if (persistentStores[i].store(o)) {
                return true;
            }
        }

        return false;
    }

    public boolean read(Object o)
    throws PersistentStoreException {
        for (int i=0; ((persistentStores != null) && (i<persistentStores.length)); ++i) {
            if (persistentStores[i].read(o)) {
                return true;
            }
        }

        return false;
    }

    /** Read all objects stored the persistent media.
     *
     * @return an array containing the objects read. If no objects are found an
     *         empty array is returned. If the persistent store has not
     *         processed the quest, null is returned.
     *
     * @throws PersistentStoreException
     *
     */
    public Object[] read(Class objClass) throws PersistentStoreException {
        Object[] objs = null;
        for (int i=0; ((persistentStores != null) && (i<persistentStores.length)); ++i) {
            if ((objs = persistentStores[i].read(objClass)) != null) {
                return objs;
            }
        }

        return null;
    }

    /** Configures the persistent store. The following configuration parameters
     * are required:
     * <ul>
     *  <li>class-loader - the class loader to be used to load persistent sotres
     * </ul>
     *
     * @param config an <i>Map</i> containing configuration parameters.
     *
     * @throws ConfigPersistentStoreException
     *
     */
    public void configure(Map<String, Object> config) throws ConfigPersistentStoreException {
        //
        // If a particular class loader is specified, then it is used. Otherwise
        // the configpath classloader is used.
        //
        ClassLoader classLoader = (ClassLoader)config.get(CONFIG_CLASS_LOADER);
        if (classLoader == null) {
            classLoader = Configuration.getConfiguration().getClassLoader();
        }

        //
        // Instantiates the persistent stores
        //
        if ((stores == null) || (stores.length == 0)) {
            return;
        }

        persistentStores = new PersistentStore[stores.length];

        //
        // Prepares the configuration map for the persistent stores
        //
        config = new HashMap<String, Object>(3);
        config.put(CONFIG_JNDI_DATA_SOURCE_NAME, jndiDataSourceName);
        config.put(CONFIG_USERNAME, username);
        config.put(CONFIG_PASSWORD, password);

        //
        // Creates and configures the managed persistent stores
        //
        int i = 0;
        try {
            for (; ((stores != null) && (i<stores.length)); ++i) {
                persistentStores[i] =
                    (PersistentStore)BeanFactory.getBeanInstance(classLoader, stores[i]);
                persistentStores[i].configure(config);
            }
        } catch (Exception e) {
            throw new ConfigPersistentStoreException( "Error in loading the persistent store "
                                                    + stores[i]
                                                    + ": "
                                                    + e.getMessage()
                                                    , e
                                                    );
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append(getClass().getName()).append(" - {");
        sb.append("jndiDataSourceName: ").append(jndiDataSourceName);
        sb.append("; stores: ");
        for (int i=0; ((stores != null) && (i<stores.length)); ++i) {
            if (i>0) {
                sb.append(",");
            }
            sb.append(stores[i]);
        }

        return sb.toString();
    }

    public boolean delete(Object o) throws PersistentStoreException
    {
        for (int i=0; ((persistentStores != null) && (i<persistentStores.length)); ++i) {
            if (persistentStores[i].delete(o)) {
                return true;
            }
        }

        return false;
    }

    public Object[] read(Object o, Clause clause) throws PersistentStoreException
    {
        Object[] objs = null;
        for (int i=0; ((persistentStores != null) && (i<persistentStores.length)); ++i) {
            if ((objs = persistentStores[i].read(o, clause)) != null) {
                return objs;
            }
        }

        return null;
    }

    public boolean store(String id, Object o, String operation)
    throws PersistentStoreException {
        for (int i=0; ((persistentStores != null) && (i<persistentStores.length)); ++i) {
            if (persistentStores[i].store(id, o, operation)) {
                return true;
            }
        }

        return false;
    }

    public int readCounter(String idSpace, int increment)
        throws PersistentStoreException {
        int counter = -1;
        for (int i=0; ((persistentStores != null) && (i<persistentStores.length)); ++i) {
            try {
                counter = persistentStores[i].readCounter(idSpace, increment);
                return counter;
            } catch (PersistentStoreException e) {
                if (ERROR_MSG_NOT_SUPPORTED.equals(e.getMessage())) {
                 // ignore and try with next persistentStore
                } else {
                    throw e;
                }
            }
        }

        throw new PersistentStoreException(ERROR_MSG_NOT_SUPPORTED);
    }

    // ------------------------------------------------------------ LazyInitBean

    /**
     * Bean initialization at bean loading.
     *
     * @throws BeanInitializationException in the case of initialization errors.
     */
    public void init() throws BeanInitializationException {
        try {
            configure(new HashMap<String, Object>());
        } catch (Exception e) {
            throw new BeanInitializationException(e.getMessage(), e.getCause());
        }
    }

}
