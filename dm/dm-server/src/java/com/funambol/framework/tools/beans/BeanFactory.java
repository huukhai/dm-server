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

package com.funambol.framework.tools.beans;

import java.beans.XMLEncoder;
import java.beans.XMLDecoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * This is a JavaBeans factory. At the moment it has a very simple
 * implementation: it loads the class from a serialized file at runtime.
 *
 * @version $Id: BeanFactory.java,v 1.2 2006/08/07 21:09:23 nichele Exp $
 */
public class BeanFactory {

    protected BeanFactory() { }

    /**
     * Creates an instance of the given bean. The bean is first search as a
     * class. If not found as class, it is searched as a resource. There isn't a
     * lazy initialization.
     *
     * @param       classLoader the class loader to use for loading the bean
     * @param       beanName    name of the bean (for now is also the file name,
     *                          without .ser, into which the bean has been serialized)
     *                          NOT NULL
     *
     * @return      a new instance of the serialized bean
     *
     * @throws      BeanInstantiationException if the bean cannot be istantiated
     */
    public static Object getNoInitBeanInstance(ClassLoader classLoader, String beanName)
           throws BeanInstantiationException, BeanInitializationException {

        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }

        Object bean = null;

        //
        // Search beanName as a class
        //
        try {
            bean = Class.forName(beanName, true, classLoader).newInstance();
            if (bean instanceof LazyInitBean) {
                ((LazyInitBean)bean).init();
            }
            return bean;
        }  catch (ClassNotFoundException e) {
            //
            // beanName does not represent a class!
            //
        } catch (BeanInitializationException e) {
            throw e;
        } catch (Exception e) {
            //
            // Other exceptions are not good!
            //
            throw new BeanInstantiationException( "Error instantiating "
                                                + beanName
                                                , e
                                                );
        }
        InputStream is = classLoader.getResourceAsStream(beanName);

        if (is == null) {
            throw new BeanInstantiationException( "Resource "
                                                 + beanName
                                                 + " not found"
                                                 + " with "
                                                 + classLoader
                                                 );
        }

        BeanExceptionListener exceptionListener = new BeanExceptionListener();

        //
        // IMPORTANT NOTE
        // --------------
        //
        // This serialization is a very important flaw for scalability, but we
        // must do it for a bug in XMLDecoder (see bug #  4822050:
        // http://developer.java.sun.com/developer/bugParade/bugs/4822050.html )
        //
        synchronized (BeanFactory.class) {
            XMLDecoder d = new XMLDecoder(is, null, exceptionListener);
            bean = d.readObject();
            d.close();
        }

        if (exceptionListener.exceptionThrown()) {
            Throwable t = exceptionListener.getException();

            if (t.getCause() != null) {
                t = t.getCause();
            }
            throw new BeanInstantiationException( "Error instantiating "
                                                + beanName
                                                , t
                                                );
        }

        return bean;
    }

    /**
     * Creates an instance of the given bean. The bean is first search as a
     * class. If not found as class, it is searched as a resource.
     *
     * @param       classLoader the class loader to use for loading the bean
     * @param       beanName    name of the bean (for now is also the file name,
     *                          without .ser, into which the bean has been serialized)
     *                          NOT NULL
     *
     * @return      a new instance of the serialized bean
     *
     * @throws      BeanInstantiationException if the bean cannot be istantiated
     */
    public static Object getBeanInstance(ClassLoader classLoader, String beanName)
           throws BeanInstantiationException, BeanInitializationException {

        Object bean = null;

        bean = getNoInitBeanInstance(classLoader, beanName);

        if (bean instanceof LazyInitBean) {
            ((LazyInitBean)bean).init();
        }

        return bean;
    }


    /**
     * Creates an instance of the given bean using the system class loader.
     *
     * @param       beanName    name of the bean (for now is also the file name,
     *                          without .ser, into which the bean has been serialized)
     *
     * @return      a new instance of the serialized bean
     *
     * @throws      BeanInstantiationException if the bean cannot be istantiated
     */
    public static Object getBeanInstance(String beanName)
           throws BeanInstantiationException, BeanInitializationException {
           return getBeanInstance(null, beanName);
    }


    /**
     * Creates an instance of the given bean. This version deserializes the bean
     * from the given file.
     *
     * @param       beanFile    the filename of the serialized file
     *
     * @return      a new instance of the serialized bean
     *
     * @throws      BeanInstantiationException if the bean cannot be istantiated
     */
    public static Object getBeanInstance(File beanFile)
           throws BeanInstantiationException, BeanInitializationException {
        try {
            Object bean = null;

            XMLDecoder e = new XMLDecoder(new FileInputStream(beanFile));
            bean = e.readObject();
            e.close();

            if (bean instanceof LazyInitBean) {
                ((LazyInitBean)bean).init();
            }
            return bean;
        } catch (IOException e) {
            String msg = "Bean creation ("
                       +          beanFile
                       +       ") failed: "
                       +     e.getMessage()
                       ;
            throw new BeanInstantiationException(msg, e);
        } catch (Exception e) {
            String msg = "Bean creation (" + beanFile + ") failed: " + e.getMessage();
            throw new BeanInstantiationException(msg, e);
        }
    }

    /**
     * Serialize the given object in the give file using XMLEncoder.
     *
     * @param obj the object to be serialized
     * @param file the file into wich serialize the object
     *
     * @throws BeanException in case of errors
     */
    public static void saveBeanInstance(Object obj, File file)
    throws BeanException {
        XMLEncoder encoder = null;

        try {
            encoder = new XMLEncoder(new FileOutputStream(file));
            encoder.writeObject(obj);
        } catch (IOException e) {
            String msg = "Bean saving (" + file + ") failed: " + e.getMessage();
            throw new BeanException(msg, e);
        } finally {
            if (encoder != null) {
                encoder.close();
            }
        }
    }

    /**
     * Creates a bean instance given the class name and serialize it in the
     * specified file. The object is serialized in XML.
     * <p>
     * Syntax:<br>
     * com.funambol.framework.tools.beans.BeanFactory <i>class name</i> </i>file name</i>
     **/
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Syntax: com.funambol.framework.tools.beans.BeanFactory <class name> <file name>");
            return;
        }

        saveBeanInstance(getBeanInstance(args[0]), new File(args[1]));
    }
}
