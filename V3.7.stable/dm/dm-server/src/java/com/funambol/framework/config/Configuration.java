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
 
package com.funambol.framework.config;

import java.io.InputStream;
import java.io.IOException;

import java.net.URL;

import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.Collections;

import com.funambol.framework.tools.beans.BeanFactory;

import org.apache.commons.lang.StringUtils;

/**
 * Incapsulates all the configuration information about the Sync4j Engine.
 * Configuration parameters are stored as properties in a Map structure. The
 * key is the name of the parameter and it is strucured in dotted separated
 * namespaces. The value of the parameter is the value of the key and can be of
 * any type. Accessor methods are provided to get the value of the parameter in
 * a particular type (such as String, int, double, boolean, ...).<br>
 * Access to getXXX() and setXXX() methods are synchronized.
 * <p>
 * This class follows the Singleton pattern, therefore it cannot be directly
 * instantiated; an internal instance is created just once and returned at any
 * getConfiguration() call.
 *
 * @version $Id: Configuration.java,v 1.2 2006/08/07 21:05:28 nichele Exp $
 */
public class Configuration
implements java.io.Serializable {

    // --------------------------------------------------------------- Constants

    // ------------------------------------------------------------ Private data

    private final Map internalMap = Collections.synchronizedMap(new HashMap());

    private static Configuration singleton;
    private transient ClassLoader classLoader = null;

    // -------------------------------------------------------------- properties

    /** Getter for property classLoader.
     * @return Value of property classLoader.
     *
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /** Setter for property classLoader.
     * @param classLoader New value of property classLoader.
     *
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    // ------------------------------------------------------------ Constructors

    protected Configuration() {
    }

    public static synchronized Configuration getConfiguration() {
        if (singleton == null) {
            singleton = new Configuration();
        }

        return singleton;
    }

    // ---------------------------------------------------------- Public methods

    public String getStringValue(String name, String def)
    throws ConfigurationException {
        String value = (String)internalMap.get(name);

        return (value == null) ? def : value;
    }

    public String getStringValue(String name)
    throws ConfigurationException {
        String value = (String)internalMap.get(name);

        if (value == null) {
            throw new ConfigurationException("No " + name + " property defined");
        }

        return value;
    }

    /**
     * Turns a character separated values property into an array of String
     * objects given the separator. For example:
     * <pre>
     *      one;two;trhee
     * </pre>
     * will result in
     * <pre
     *      {"one", "two", "trhee"}
     * </pre>
     *
     * @param name the name of the property
     * @param separator the separator character
     *
     * @return an array of Strings
     */
    public String[] getStringArray(String name, char separator)
    throws ConfigurationException {
        String value = (String)internalMap.get(name);

        if (value == null) {
            throw new ConfigurationException("No " + name + " property defined");
        }

        //
        // NOTE: String.split() is not used for performance reason. We do not
        //       need a regular expression here.
        //
        return StringUtils.split(value, String.valueOf(separator));
    }

    public int getIntValue(String name, int def)
    throws ConfigurationException {
        Integer value = (Integer)internalMap.get(name);

        return (value == null) ? def : value.intValue();
    }

    public int getIntValue(String name)
    throws ConfigurationException {
        Integer value = (Integer)internalMap.get(name);

        if (value == null) {
            throw new ConfigurationException("No " + name + " property defined");
        }

        return value.intValue();
    }

    public boolean getBoolValue(String name, boolean def)
    throws ConfigurationException {
        Boolean value = (Boolean)internalMap.get(name);

        return (value == null) ? def : value.booleanValue();
    }

    public boolean getBoolValue(String name)
    throws ConfigurationException {
        Boolean value = (Boolean)internalMap.get(name);

        if (value == null) {
            throw new ConfigurationException("No " + name + " property defined");
        }

        return value.booleanValue();
    }

    public Object getValue(String name, Object def)
    throws ConfigurationException {
        Object value = internalMap.get(name);

        return (value == null) ? def : value;
    }

    public Object getValue(String name)
    throws ConfigurationException {
        Object value = internalMap.get(name);

        if (value == null) {
            throw new ConfigurationException("No " + name + " property defined");
        }

        return value;
    }

    /**
     * It is supposed that the value of the parameter is a class name. It returns
     * an instance of that class, created with <i>newInstance()</i>.
     *
     * @param name the name of the parameter
     *
     * @return an instance of the class specified by the parameter
     *
     * @throws ConfigurationException in case of errors
     */
    public Object getClassInstance(String name)
    throws ConfigurationException {
       String value = (String)internalMap.get(name);

       if (value == null) {
           throw new ConfigurationException("No " + name + " property defined");
       }

       try {
           return Class.forName(value).newInstance();
       } catch (Exception e) {
           throw new ConfigurationException( "Error in creating an instance of "
                                           + value
                                           , e                                );
       }
    }

    /**
     * It is supposed that the value of the parameter is a bean name. It returns
     * that bean, created with <i>BeanFactory.getBeanInstance()</i>.
     *
     * @param name the name of the parameter
     *
     * @return an instance of the class specified by the parameter
     *
     * @throws ConfigurationException in case of errors
     */
    public Object getBeanInstance(String name)
    throws ConfigurationException {
       String value = (String)internalMap.get(name);

       if (value == null) {
           throw new ConfigurationException("No " + name + " property defined");
       }

       try {
           return BeanFactory.getBeanInstance(classLoader, value);
       } catch (Exception e) {
           throw new ConfigurationException( "Error in creating an instance of "
                                           + value
                                           , e                                );
       }
    }

    /**
     * Create and return an array of bean as specified by a list of bean names.
     *
     * @param name the name of the paramenter
     * @param separator the separator character
     *
     */
    public Object[] getBeanArray(String name, char separator)
    throws ConfigurationException {
        String[] values = getStringArray(name, separator);

        Object[] ret = new Object[values.length];

        int i = 0;
        try {
            for (; i<ret.length; ++i) {
                ret[i] = BeanFactory.getBeanInstance(classLoader, values[i]);
            }
        } catch (Exception e) {
            throw new ConfigurationException ( "Error in creating the bean "
                                             + values[i]
                                             , e                           );
        }

        return ret;
    }

    /**
     * Loads and instantiate a bean by its config name. In this case the bean
     * is not looked up in the internal configuration map, but is created
     * directly by the means of the BeanFactory. This call is a shortcut for:
     * <code>
     *
     *  ClassLoader cl = Configuration.getConfiguration().getClassLoader();
     *  Object o = BeanFactory.getBeanInstance(cl, beanName);
     *
     * </code>
     *
     * @param beanName the complete beanName
     *
     * @return the bean instance
     *
     * @throws ConfigurationException in case of instantiation error
     */
    public Object getBeanInstanceByName(String beanName) {
        try {
             return BeanFactory.getBeanInstance(classLoader, beanName);
        } catch (Exception e) {
             throw new ConfigurationException( "Error in creating an instance of "
                                             + beanName
                                             , e                                );
        }
    }

    public synchronized void load(InputStream is) throws IOException {
        Properties properties = new Properties();

        properties.load(is);

        internalMap.putAll(properties);
    }

    public synchronized void load(String uri)
    throws IOException {
        load(new URL(uri).openStream());
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append(getClass().getName()).append(" - ");
        sb.append(internalMap.toString()).append(" - ");
        sb.append("classLoader: " + classLoader);

        return sb.toString();
    }

}
