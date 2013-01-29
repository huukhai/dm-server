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

package com.funambol.test.tools.ant;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Iterator;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.PatternSet;

import com.funambol.framework.tools.DBTools;
import com.funambol.framework.tools.IOTools;
import com.funambol.test.TestFailedException;
import com.funambol.test.tools.PostSyncML;

/**
 * This is an Ant task that executes the protocol level tests.
 * <p>
 * @see com.funambol.test.tools.PostSyncML
 * <p
 * It can be used in the following manner:
 *
 * <pre>
 * &lt;task name="test-syncml" ...&gt;
 *   &lt;testsyncml url="{initial URL}" test="{test name}"&gt;
 *     &lt;patternset&gt;
          &lt;exclude name="/SyncML/SyncHdr/SessionID/self::node()[1]"/&gt;
          &lt;exclude name="/SyncML/SyncHdr/RespURI/self::node()[1]"/&gt;
        &lt;/patternset&gt;
 *   &lt;/testsyncml&gt;
 * &lt;/task&gt;
 * </pre>
 *
 * 
 * @version $Id: TestSyncMLTask.java,v 1.3 2006/11/15 16:11:17 nichele Exp $
 */
public class TestSyncMLTask extends org.apache.tools.ant.Task {

    // ------------------------------------------------------------ Private data
    private AntClassLoader loader;


    // -------------------------------------------------------------- Properties

    protected String url = null;
    public void setUrl(String url) {
        this.url = url;
    }

    protected String test = null;
    public void setTest(String test) {
        this.test = test;
    }


    protected String driver = null;
    public void setDriver(String driver) {
        this.driver = driver;
    }

    protected String dburl = null;
    public void setDburl(String dburl) {
        this.dburl = dburl;
    }

    protected String userid = null;
    public void setUserid(String userid) {
        this.userid = userid;
    }

    protected String password = null;
    public void setPassword(String password) {
        this.password = password;
    }

    protected String classpath = null;
    public void setClasspath(String classpath) {
        this.classpath = classpath;
    }


    protected ArrayList<PatternSet> xpathPatterns = new ArrayList<PatternSet>();

    public void addPatternset(PatternSet set) {
        xpathPatterns.add(set);
    }


    // ----------------------------------------------------- Task Implementation

    public void execute() throws BuildException {
        Project p = getProject();

        //
        // make sure we don't have an illegal set of options
        //
        validateAttributes();


        Connection conn = null;

        try {
            log("Getting db connection", Project.MSG_VERBOSE);
            conn = getConnection();
        } catch (SQLException ex) {
            throw new BuildException("Error connecting to db", ex, getLocation());
        }


        //
        // Deal with the specified XPath to exclude from the comparison of
        // the XML messages. They are specified through one or more inner
        // <patternset> elements.
        //
        ArrayList<String> allExcludeXPaths = new ArrayList<String>();

        PatternSet ps = null;
        String[] xpaths = null;
        for (int i = 0; i < xpathPatterns.size(); i++) {
            ps = xpathPatterns.get(i);
            xpaths = ps.getExcludePatterns(p);
            for (int j = 0; j < xpaths.length; ++j) {
                allExcludeXPaths.add(xpaths[j]);
            }
        }  // next i

        log("Initial URL: "    + url             );
        log("Test: "           + test            );
        log("Ignored XPaths: " + allExcludeXPaths);

        xpaths = allExcludeXPaths.toArray(new String[0]);

        //
        // Execute the tests now
        //
        FilenameFilter filter = IOTools.getFileTypeFilter(".xml");

        String[] msgFiles = ordersAndFilterFiles(new File(test).list(filter));

        try {
            new PostSyncML(
                url                                 ,
                new File(p.getBaseDir(), test)      ,
                msgFiles                            ,
                xpaths                              ,
                conn
            ).syncAndTest();

            log("Test " + test + " passed!");
        } catch (IOException e) {
            log(e.getMessage(), Project.MSG_ERR);
            throw new BuildException("Error executing PostSyncMLTask", e);
        } catch (TestFailedException e) {
            log("Test " + test + " failed: " + e.getMessage(), Project.MSG_INFO);
        } finally {
            log("Closing db connection", Project.MSG_VERBOSE);
            DBTools.close(conn, null, null);
        }




    }

    // ------------------------------------------------------- Protected methods

    protected void validateAttributes() throws BuildException {
        try {
            new java.net.URL(url);
        } catch (Exception e) {
            new BuildException("Malformed url exception: " + url);
        }
    }

    /**
     * Gets an instance of the required driver.
     * Uses the ant class loader and the optionally the provided classpath.
     * @return Driver
     * @throws BuildException
     */
    protected Driver getDriver() throws BuildException {
        if (driver == null) {
            throw new BuildException("Driver attribute must be set", getLocation());
        }

        Driver driverInstance = null;
        try {
            Class dc;
            if (classpath != null) {

                    if (loader == null) {
                        loader = new AntClassLoader(getProject(), new Path(getProject(), classpath));
                    }

                    dc = loader.loadClass(driver);

            } else {
                dc = Class.forName(driver);
            }
            driverInstance = (Driver) dc.newInstance();
        } catch (Exception e) {
            throw new BuildException(
                    "Error loading JDBC driver", e, getLocation());
        }

        return driverInstance;
    }


    /**
     * Orders and filters list of files. Valid name file is msgNNNNN.xml where
     * NNNNN is a positive integer.
     *
     * @param files String[]
     * @return String[]
     */
    protected static String[] ordersAndFilterFiles(String[] files) {
        int numFiles = files.length;

        TreeMap<Integer, String> map = new TreeMap<Integer, String>();

        String fileName = null;
        int index = -1;

        int numValidFiles = 0;

        for (int i=0; i<numFiles; i++) {
            fileName = files[i];

            index = getIndexOfFile(fileName);

            if (index == -1) {
                continue;
            }
            numValidFiles++;
            map.put(Integer.valueOf(index), fileName);
        }


        String[] newFilesList = new String[numValidFiles];

        Iterator<Integer> it = map.keySet().iterator();
        int i = 0;

        while (it.hasNext()) {
            newFilesList[i++] = (map.get(it.next()));
        }

        return newFilesList;
    }

    /**
     * Given a fileName, if it is msgNNNNN.xml returns NNNNN, otherwise return -1
     * @param fileName String
     * @return int
     */
    protected static int getIndexOfFile(String fileName) {

        int indexMsg = fileName.indexOf("msg");

        if (indexMsg == -1) {
            return -1;
        }

        int indexExtension = fileName.lastIndexOf('.');

        if (indexExtension == -1) {
            return -1;
        }

        int indexOfFile = -1;

        try {
            indexOfFile = Integer.parseInt(fileName.substring(3, indexExtension));
        } catch (NumberFormatException ex) {
            return -1;
        }

        return indexOfFile;
    }


    // --------------------------------------------------------- Private Methods
    private Connection getConnection() throws SQLException {
        Connection conn = null;

        Driver driver = getDriver();

        Properties prop = new Properties();
        prop.setProperty("user", userid);
        prop.setProperty("password", password);

        conn = driver.connect(dburl, prop);

        return conn;
    }
}
