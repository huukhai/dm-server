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

package com.funambol.test.tools;

import java.util.List;
import java.util.Iterator;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Connection;

import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;

import org.vmguys.vmtools.ota.OtaUpdate;
import org.vmguys.vmtools.ota.UniqueId;
import org.vmguys.vmtools.utils.DomFactory;

import com.funambol.framework.core.RepresentationException;
import com.funambol.framework.core.Sync4jException;
import com.funambol.framework.core.SyncML;
import com.funambol.framework.tools.IOTools;
import com.funambol.framework.tools.CommandIdGenerator;
import com.funambol.test.TestFailedException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/**
 * This is a driver for running tests at the protocol level.<br>
 * It requires a directory structure like this:
 * <pre>
 *  {test code}
 *    <i>msg1.xml</i>
 *    <i>msg2.xml</i>
 *    <i>...</i>
 *    reference
 *      <i>msg1.xml</i>
 *      <i>msg2.xml</i>
 *      <i>...</i>
 *    response
 *      <i>msg1.xml</i>
 *      <i>msg2.xml</i>
 *      <i>...</i>
 * </pre>
 * <i>test code</i> is the code name of the test to be performed (i.e. WOSI0001);
 * msg{N}.xml are the messages that the client has to send to the server. For
 * each message sent, the response is stored in the <i>response</i> directory.
 * <p>
 * The directory <i>reference</i> contains the expected response messages used
 * by the comparison tool to identify the differences between the returned
 * messages and the expected values.
 * <p>
 * This class is designe to work as a standalone program. An Ant task is
 * developed as well; @see com.funambol.test.tools.ant.PostSyncMLTask .
 * <p>
 * <b>Syntax</b>
 * <pre>
 * com.funambol.test.tools.PostSyncML {initial URL} {file msg1} ... {file msgN}
 *
 * where:
 *
 * {initial URL}: the URL the first request has to be sent to (the others depend
 *                by the RespURI element in the response.
 * {file msg1} .. {file msgN}: the messages to send to server. They are sent in
 *                             the order they appear on the command line.
 * </pre>
 *
 *
 * @version $Id: PostSyncML.java,v 1.3 2006/08/07 21:09:26 nichele Exp $
 */
public class PostSyncML {

    // --------------------------------------------------------------- Constants

    public static String LOG_NAME = "funambol.test.tools.PostSyncML";

    public static String FILE_ERROR     = "error"    ;
    public static String FILE_RESPONSE  = "response" ;
    public static String FILE_REFERENCE = "reference";


    // ------------------------------------------------------------ Private data

    private CommandIdGenerator idGenerator  = null;
    private String             nextURL      = null;
    private String[]           msgs         = null;
    private String[]           msgFiles     = null;
    private String[]           ignoreXPaths = null;

    private File baseDir = null;
    private File responseDir = null, referenceDir = null, errorDir = null;

    private Connection conn = null;

    private static final Logger log = Logger.getLogger(com.funambol.test.tools.PostSyncML.class.getName());

    // ------------------------------------------------------------ Constructors

    public PostSyncML(String  initialURL   ,
                      File     baseDir     ,
                      String[] msgFiles    ,
                      String[] ignoreXPaths,
                      Connection conn
                      )
    throws IOException {
        idGenerator = new CommandIdGenerator();

        if ((msgFiles == null) || (msgFiles.length == 0)) {
            msgs = new String[0];
        }

        msgs = new String[msgFiles.length];

        this.baseDir  = baseDir;
        this.responseDir  = new File(baseDir, FILE_RESPONSE );
        this.referenceDir = new File(baseDir, FILE_REFERENCE);
        this.errorDir     = new File(baseDir, FILE_ERROR    );

        this.msgFiles = msgFiles;
        for (int i=0; i<msgFiles.length; ++i) {
            msgs[i] = IOTools.readFileString(new File(baseDir, msgFiles[i]));
        }

        this.ignoreXPaths = ignoreXPaths;

        nextURL = initialURL;

        this.conn = conn;
    }

    //----------------------------------------------------------- Public methods

    public void syncAndTest() throws IOException, TestFailedException {
        //
        // First of all clean up!
        //
        clean();

        SyncML response = null;
        String  respURI  = null;

        String  referenceXML = null;

        File responseFile = null;


        String sqlScriptFileName = null;



        for (int i=0; i<msgs.length; ++i) {

            if (conn != null) {

                // checks if there is a file with the same name but with .sql extension
                // If so, executes it

                sqlScriptFileName = checkForSQLScriptFile(msgFiles[i]);

                log.info("Executing file: " + sqlScriptFileName);

                if (sqlScriptFileName != null) {
                    // executes it
                    try {
                        SQLTools.executeScript(conn, new File(baseDir, sqlScriptFileName));
                    } catch (Exception ex) {
                        throw new TestFailedException("Error executing sql script file '" + sqlScriptFileName + "'", ex);
                    }
                }

            }

            log.info("Sending " + msgFiles[i]);

            try {
                response = postRequest(msgs[i]);
            } catch (RepresentationException e) {
                IOTools.writeFile(e.getMessage(), new File(errorDir, msgFiles[i]));
                throw new TestFailedException ("XML syntax error: " + e.getMessage(), e);
            } catch (Sync4jException e) {
                IOTools.writeFile(e.getMessage(), new File(errorDir, msgFiles[i]));
                throw new TestFailedException ("XML syntax error: " + e.getMessage(), e);
            }

            //
            // Write the messages responded by the server, than read the reference
            // and make the comparison (excluding the XPaths specified by
            // ignoreXPaths
            //
            responseFile = new File(responseDir, msgFiles[i]);
            log.info("Writing the response into " + responseFile);

            try {

                String xmlMsg = marshallSyncML(response);
                IOTools.writeFile(xmlMsg, responseFile);

            } catch(Exception e) {
                e.printStackTrace();
                throw new TestFailedException ("XML syntax error: " + e.getMessage(), e);
            }

            referenceXML = IOTools.readFileString(new File(referenceDir, msgFiles[i]));

            compare(msgFiles[i]);

            respURI = response.getSyncHdr().getRespURI();

            if (respURI != null) {
                nextURL = respURI;
            }
        }
    }

    // --------------------------------------------------------- Private methods

    private SyncML postRequest(String request)
    throws IOException, Sync4jException, RepresentationException {
        HttpClientConnection syncMLConnection = new HttpClientConnection(nextURL);
        return syncMLConnection.sendMessage(request);
    }

    private void compare(String msgFile)
    throws IOException, TestFailedException {
        File responseFile  = new File(responseDir , msgFile);
        File referenceFile = new File(referenceDir, msgFile);

        SAXBuilder sb = new SAXBuilder();
        sb.setFactory(new DomFactory());

        try {
            Document response  = sb.build(responseFile );
            Document reference = sb.build(referenceFile);

            OtaUpdate update = new OtaUpdate(false);

            UniqueId id = new UniqueId("SyncMLTest", msgFile);
            Element diffs = update.generateDiffs(response.getRootElement() ,
                                                 reference.getRootElement(),
                                                 id                        );

            if (log.isEnabledFor(Level.TRACE)) {
                saveDiffs(diffs, new File(errorDir, msgFile + ".dbg"));
            }

            if (checkDiffs(diffs)) {
                saveDiffs(diffs, new File(errorDir, msgFile));

                throw new TestFailedException( "Test failed on "
                                             + msgFile
                                             + ". Diff file saved in "
                                             + new File(errorDir, msgFile)
                                             );
            }
        } catch (JDOMException e) {
            IOTools.writeFile(e.getMessage(), new File(errorDir, msgFile));
            throw new TestFailedException("Test failed on "
                                             + msgFile
                                             + ": "
                                             + e.getMessage()
                                             + ". Error message saved in "
                                             + new File(errorDir, msgFile)
                                             );
        }
    }

    /**
     * Checks if the given diffs Element contains significant differences, so
     * that differences on any node excluding the ones specified by the
     * <i>ignoreXPaths</i> XPaths.
     *
     * @param diffs the differences to be inspected
     *
     * @return <i>true</i> if there is at least one difference in one of the not
     *         ignored XPaths, or <i>false</i> otherwise.
     */
    private boolean checkDiffs(Element diffs) {
        List positions = diffs.getChildren("Position", diffs.getNamespace());

        Element position = null;

        Iterator i = positions.iterator();
        while(i.hasNext()) {
            position = (Element)i.next();

            if (!ignore(position.getAttributeValue("XPath"))) {
                //
                // This means a difference!
                //
                return true;
            }
        }

        return false;
    }

    /**
     * Removes old files from the working directories
     */
    private void clean() {
        FilenameFilter filter = IOTools.getFileTypeFilter("xml");

        String[] files = responseDir.list(filter);

        for (int i=0; ((files != null) && (i<files.length)); ++i) {
            new File(files[i]).delete();
        }

        files = errorDir.list(filter);

        for (int i=0; ((files != null) && (i<files.length)); ++i) {
            new File(files[i]).delete();
        }
    }

    /**
     * Checks if the given XPath is one of the ignored XPath
     *
     * @param xPath the xPath to check
     *
     * @return <i>true</i> is the xPath in the list of the ignored XPaths,
     *         <i>false</i> otherwise.
     */
    private boolean ignore(String xPath) {

        for (int i=0;
             ((xPath != null) && (ignoreXPaths != null) && (i<ignoreXPaths.length));
             ++i) {
            if (xPath.equals(ignoreXPaths[i])) {
                return true;
            }
        }

        return false;
    }

    /**
     * Saves the given diff element to the given file
     *
     * @param diffs the diff element
     * @param file the file to save into
     */
    private void saveDiffs(Element diffs, File file) throws IOException {
        XMLOutputter xmlo = new XMLOutputter("  ", true);
        xmlo.setTextNormalize(true);

        FileOutputStream fos = new FileOutputStream(file);
        xmlo.output(diffs, fos);
        fos.close();
    }

    private static void syntax() {
        System.out.println("Syntax: " + (PostSyncML.class) + "<initial URL> <msg1> ... <msgN>");
    }

    private String marshallSyncML(SyncML syncML) throws Sync4jException {
        String msg = null;
        try {

            ByteArrayOutputStream bout = new ByteArrayOutputStream();

            IBindingFactory f = BindingDirectory.getFactory(SyncML.class);
            IMarshallingContext c = f.createMarshallingContext();
            c.setIndent(0);
            c.marshalDocument(syncML, "UTF-8", null, bout);

            msg = new String(bout.toByteArray());

        } catch(Exception e) {
            e.printStackTrace();
            throw new Sync4jException(e);
        }
        return msg;
    }


    private String checkForSQLScriptFile(String msg) {

        String sqlScriptFileName = null;

        int index = msg.lastIndexOf('.');

        if (index == -1) {
            sqlScriptFileName = msg + ".sql";
        } else {
            sqlScriptFileName = msg.substring(0, index) + ".sql";
        }

        File sqlFile = new File(baseDir, sqlScriptFileName);

        if (sqlFile.isFile()) {
            return sqlScriptFileName;
        }

        return null;
    }


    // -------------------------------------------------------------------- Main

    public static void main(String args[])
    throws Exception {
        if(args.length < 2) {
            syntax();
        }

        String[] msgFiles = new String[args.length-1];

        System.arraycopy(args, 1, msgFiles, 0, msgFiles.length);

        PostSyncML postsyncml = new PostSyncML(args[0], new File("."), msgFiles, new String[0], null);
        postsyncml.syncAndTest();
    }
}
