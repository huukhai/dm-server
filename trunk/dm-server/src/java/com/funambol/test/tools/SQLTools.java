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


import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


/**
 *
 * @version $Id: SQLTools.java,v 1.3 2006/11/15 16:09:22 nichele Exp $
 */
public class SQLTools {

    // ---------------------------------------------------------- Public Methods

    /**
     * Executes all sql queries contained in the given script file.
     * <br/>Performs a simply parser of the file ignoring line that start with '--'
     *
     * @param conn the connection to use to execute the query
     * @param file the script file
     *
     * @throws IOException
     * @throws SQLException
     */
    public static void executeScript(Connection conn, File file) throws IOException, SQLException {

        StringBuffer query = new StringBuffer();

        BufferedReader br = new BufferedReader(new FileReader(file));

        String line = null;
        StringReader lineReader = null;
        int c = 0;

        while ( (line = br.readLine()) != null ) {
            if (line.startsWith("--")) {
                // ignore comment line
                continue;
            }

            lineReader = new StringReader(line);

            while ( (c = lineReader.read()) != -1) {

                if ( (char)c == ';' ) {
                    executeQuery(conn, query.toString().trim());
                    query.delete(0, query.length());
                } else {
                    query.append( (char)c);
                }

            }

            lineReader.close();
        }

        br.close();

    }

    /**
     * Executes a query on the given connection
     *
     * @param conn the connection to use to execute the query
     * @param query the query to execute
     *
     * @throws SQLException
     * @return int
     */
    public static int executeQuery(Connection conn, String query) throws SQLException {
      Statement stmt = conn.createStatement();
      int i = stmt.executeUpdate(query);
      stmt.close();
      return i;
     }



}
