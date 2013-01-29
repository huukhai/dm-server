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
 
package com.funambol.server.store;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.funambol.framework.tools.DBTools;
import com.funambol.framework.server.LastTimestamp;
import com.funambol.framework.server.ClientMapping;
import com.funambol.framework.server.store.Clause;
import com.funambol.framework.server.store.PersistentStore;
import com.funambol.framework.server.store.BasePersistentStore;
import com.funambol.framework.server.store.NotFoundException;
import com.funambol.framework.server.store.PersistentStoreException;

/**
 * This is the store for information regarding the synchronization process and
 * status. It persists the following classes:
 *
 * <ul>
 * <li>com.funambol.framework.server.LastTimestamp</li>
 * <li>com.funambol.framework.server.ClientMapping</li>
 * </ul>
 *
 * This <i>PersistentStore</i> is configured with the following map key:
 * <ul>
 *  <li>jndiDataSourceName</li>
 * </ul>
 *
 * @version $Id: SyncPersistentStore.java,v 1.4 2007-06-19 08:16:25 luigiafassina Exp $
 */
public class SyncPersistentStore
extends    BasePersistentStore
implements PersistentStore, java.io.Serializable {

    // --------------------------------------------------------------- Constants

    // -------------------------------------------------------------- Properties

    // ------------------------------------------------------------ Private data

    // ------------------------------------------------------------ Constructors

    // ---------------------------------------------------------- Public methods

    public boolean store(Object o)
    throws PersistentStoreException {
        if (o instanceof LastTimestamp) {
            storeLastTimestamp((LastTimestamp) o);
            return true;
        } else if (o instanceof ClientMapping) {
            storeClientMapping((ClientMapping) o);
            return true;
        }

        return false;
    }

    public boolean read(Object o)
    throws PersistentStoreException {
        if (o instanceof LastTimestamp) {
            readLastTimestamp((LastTimestamp) o);
            return true;
        } else if (o instanceof ClientMapping) {
            readClientMapping((ClientMapping) o);
            return true;
        }

        return false;
    }

    /** Read all objects stored the persistent media.
     *
     * @param objClass the object class handled by the persistent store
     *
     * @return an array containing the objects read. If no objects are found an
     *         empty array is returned. If the persistent store has not
     *         processed the quest, null is returned.
     *
     * @throws PersistentStoreException
     *
     */
    public Object[] read(Class objClass) throws PersistentStoreException {
        //
        // TO DO (not used yet)
        //
        return null;
    }


    // ------------------------------------------------------- Protected methods
    // --------------------------------------------------------- Private methods

    /**
     * Store the last timestamp.
     * Into tagServer there is the Next anchor sent by client: now this become
     * a Last anchor of the server.
     * Into tagClient there is the Next anchor sent by server: now this become
     * a Last anchor of the client.
     *
     * @param l the LastTimestamp object with all information about start and
     *          end sync timestamp
     *
     */
    private void storeLastTimestamp(LastTimestamp l)
    throws PersistentStoreException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = dataSource.getConnection();

            stmt = conn.prepareStatement(sqlUpdateLastTimestamp);

            stmt.setString   (1, l.tagServer           );
            stmt.setString   (2, l.tagClient           );
            stmt.setTimestamp(3, new Timestamp(l.start));
            stmt.setTimestamp(4, new Timestamp(l.end)  );
            stmt.setString   (5, l.principal           );
            stmt.setString   (6, l.database            );

            int n = stmt.executeUpdate();

            if (n == 0) {
                //
                // The first time!!!
                //
                stmt.close();

                stmt = conn.prepareStatement(sqlInsertLastTimestamp);

                stmt.setString   (1, l.principal           );
                stmt.setString   (2, l.database            );
                stmt.setString   (3, l.tagServer           );
                stmt.setString   (4, l.tagClient           );
                stmt.setTimestamp(5, new Timestamp(l.start));
                stmt.setTimestamp(6, new Timestamp(l.end)  );

                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new PersistentStoreException("Error storing last timestamp", e);
        } finally {
            DBTools.close(conn, stmt, null);
        }
    }

    protected void readLastTimestamp(LastTimestamp l)
    throws PersistentStoreException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dataSource.getConnection();

            stmt = conn.prepareStatement(sqlSelectLastTimestamp);

            stmt.setString(1, l.principal);
            stmt.setString(2, l.database);

            rs = stmt.executeQuery();

            if (rs.next() == false) {
                throw new NotFoundException("Last timestamp not found for "
                + l.toString()
                );
            }

            l.tagServer = rs.getString   (1)          ;
            l.tagClient = rs.getString   (2)          ;
            l.start     = rs.getTimestamp(3).getTime();
            l.end       = rs.getTimestamp(4).getTime();
        } catch (SQLException e) {
            throw new PersistentStoreException("Error reading last timestamp", e);
        } finally {
            DBTools.close(conn, stmt, rs);
        }
    }

    private void readClientMapping(ClientMapping clientMapping)
    throws PersistentStoreException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dataSource.getConnection();

            stmt = conn.prepareStatement(sqlSelectClientMapping);
            stmt.setString(1, clientMapping.getPrincipal().getId());
            stmt.setString(2, clientMapping.getDbURI()            );
            rs = stmt.executeQuery();
            HashMap<String, String> mapping = new HashMap<String, String>();
            while (rs.next()) {
                mapping.put(rs.getString("luid"), rs.getString("guid"));
            }
            clientMapping.initializeFromMapping(mapping);
        } catch (SQLException e) {
            throw new PersistentStoreException("Error reading mapping", e);
        } finally {
            DBTools.close(conn, stmt, rs);
        }
    }

    private void storeClientMapping(ClientMapping clientMapping)
    throws PersistentStoreException {
        Connection conn = null;
        PreparedStatement stmt = null, stmtIns = null;

        String principal = clientMapping.getPrincipal().getId();
        String dbURI     = clientMapping.getDbURI()            ;

        assert ((principal != null) && (dbURI != null));

        try {
            conn = dataSource.getConnection();

            if (clientMapping.isDeleted()) {
                stmt = conn.prepareStatement(sqlDeleteClientMapping);
                stmt.setString(1, principal);
                stmt.setString(2, dbURI    );
                Map<String, String> clientMap = clientMapping.getDeletedEntries();
                Iterator<String> i = clientMap.keySet().iterator();
                while (i.hasNext()) {
                    String key = i.next();
                    stmt.setString(3, key);
                    stmt.executeUpdate();
                }
            }

            if (clientMapping.isModified()) {
                stmt = conn.prepareStatement(sqlUpdateClientMapping);
                stmt.setString(2, principal);
                stmt.setString(3, dbURI    );
                Map<String, String> clientMap = clientMapping.getModifiedEntries();
                Iterator<String> i = clientMap.keySet().iterator();
                while (i.hasNext()) {
                    int n = -1;

                    String key = i.next();
                    stmt.setString(1, key);
                    stmt.setString(4, clientMap.get(key));
                    n = stmt.executeUpdate();

                    if (n == 0) {
                        //
                        // insert new mapping
                        //
                        stmtIns = conn.prepareStatement(sqlInsertClientMapping);
                        stmtIns.setString(1, principal);
                        stmtIns.setString(2, dbURI    );

                        stmtIns.setString(3, key);
                        stmtIns.setString(4, clientMap.get(key));
                        stmtIns.executeUpdate();
                        stmtIns.close();
                    }

                }
            }

        } catch (SQLException e) {
            throw new PersistentStoreException("Error storing client mapping", e);
        } finally {
            DBTools.close(conn, stmt, null);
        }

    }

    // ---------------------------------------------------------- SQL properties

    /**
     * The SQL query to insert the last timestamp
     */
    private String sqlInsertLastTimestamp =
    "insert into fnbl_last_sync (principal, sync_source, last_anchor_server, last_anchor_client, start_sync, end_sync) values(?, ?, ?, ?, ?, ?)";

    /** Getter for property sqlInsertLastTimestamp.
     * @return Value of property sqlInsertLastTimestamp.
     *
     */
    public String getSqlInsertLastTimestamp() {
        return sqlInsertLastTimestamp;
    }

    /** Setter for property sqlInsertLastTimestamp.
     * @param sqlInsertLastTimestamp New value of property sqlInsertLastTimestamp.
     *
     */
    public void setSqlInsertLastTimestamp(String sqlInsertLastTimestamp) {
        this.sqlInsertLastTimestamp = sqlInsertLastTimestamp;
    }

    /**
     * The SQL query to update the last timestamp
     */
    private String sqlUpdateLastTimestamp =
    "update fnbl_last_sync set last_anchor_server=?,last_anchor_client=?,start_sync=?,end_sync=? where principal=? and sync_source=?";

    /** Getter for property sqlUpdateLastTimestamp.
     * @return Value of property sqlUpdateLastTimestamp.
     *
     */
    public String getSqlUpdateLastTimestamp() {
        return sqlUpdateLastTimestamp;
    }

    /** Setter for property sqlUpdateLastTimestamp.
     * @param sqlUpdateLastTimestamp New value of property sqlUpdateLastTimestamp.
     *
     */
    public void setSqlUpdateLastTimestamp(String sqlUpdateLastTimestamp) {
        this.sqlUpdateLastTimestamp = sqlUpdateLastTimestamp;
    }

    /**
     * The SQL query to select the last timestamp
     */
    private String sqlSelectLastTimestamp =
    "select last_anchor_server, last_anchor_client,start_sync,end_sync from fnbl_last_sync where principal=? and sync_source=?";

    /** Getter for property sqlUpdateLastTimestamp.
     * @return Value of property sqlUpdateLastTimestamp.
     *
     */
    public String getSqlSelectLastTimestamp() {
        return sqlSelectLastTimestamp;
    }

    /** Setter for property sqlUpdateLastTimestamp.
     * @param sqlSelectLastTimestamp New value of property sqlUpdateLastTimestamp.
     *
     */
    public void setSqlSelectLastTimestamp(String sqlSelectLastTimestamp) {
        this.sqlSelectLastTimestamp = sqlSelectLastTimestamp;
    }

    // Configurable SQL queries for Client Mapping persistence
    private String sqlInsertClientMapping =
    "insert into fnbl_client_mapping (principal, sync_source, luid, guid) values(?, ?, ?, ?)";
    private String sqlDeleteClientMapping =
    "delete from fnbl_client_mapping where principal=? and sync_source=? and luid=?";
    private String sqlUpdateClientMapping =
    "update fnbl_client_mapping set luid=? where principal=? and sync_source=? and guid=?";
    private String sqlSelectClientMapping =
    "select luid,guid from fnbl_client_mapping where principal=? and sync_source=?";

    /** Getter for property sqlInsertClientMapping.
     * @return Value of property sqlInsertClientMapping.
     *
     */
    public String getSqlInsertClientMapping() {
        return sqlInsertClientMapping;
    }

    /** Setter for property sqlInsertClientMapping.
     * @param sqlInsertClientMapping New value of property sqlInsertClientMapping.
     *
     */
    public void setSqlInsertClientMapping(String sqlInsertClientMapping) {
        this.sqlInsertClientMapping = sqlInsertClientMapping;
    }

    /** Getter for property sqlDeleteClientMapping.
     * @return Value of property sqlDeleteClientMapping.
     *
     */
    public String getSqlDeleteClientMapping() {
        return sqlDeleteClientMapping;
    }

    /** Setter for property sqlDeleteClientMapping.
     * @param sqlDeleteClientMapping New value of property sqlDeleteClientMapping.
     *
     */
    public void setSqlDeleteClientMapping(String sqlDeleteClientMapping) {
        this.sqlDeleteClientMapping = sqlDeleteClientMapping;
    }

    /** Getter for property sqlUpdateClientMapping.
     * @return Value of property sqlUpdateClientMapping.
     *
     */
    public String getSqlUpdateClientMapping() {
        return sqlUpdateClientMapping;
    }

    /** Setter for property sqlUpdateClientMapping.
     * @param sqlUpdateClientMapping New value of property sqlUpdateClientMapping.
     *
     */
    public void setSqlUpdateClientMapping(String sqlUpdateClientMapping) {
        this.sqlUpdateClientMapping = sqlUpdateClientMapping;
    }

    /** Getter for property sqlSelectClientMapping.
     * @return Value of property sqlSelectClientMapping.
     *
     */
    public String getSqlSelectClientMapping() {
        return sqlSelectClientMapping;
    }

    /** Setter for property sqlSelectClientMapping.
     * @param sqlSelectClientMapping New value of property sqlSelectClientMapping.
     *
     */
    public void setSqlSelectClientMapping(String sqlSelectClientMapping) {
        this.sqlSelectClientMapping = sqlSelectClientMapping;
    }

    public boolean delete(Object o) throws PersistentStoreException
    {
        return false;
    }

    public Object[] read(Object o, Clause clause) throws PersistentStoreException
    {
        return null;
    }

    public boolean store(String id, Object o, String operation) throws PersistentStoreException
    {
        return false;
    }

    public int readCounter(String idSpace, int increment)
        throws PersistentStoreException {
        throw new PersistentStoreException(ERROR_MSG_NOT_SUPPORTED);
    }
}
