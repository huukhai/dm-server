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


import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.funambol.server.engine.Sync4jSource;
import com.funambol.framework.tools.DBTools;
import com.funambol.framework.tools.Base64;
import com.funambol.framework.engine.dm.DeviceDMState;
import com.funambol.framework.server.Sync4jDevice;
import com.funambol.framework.security.Sync4jPrincipal;
import com.funambol.framework.server.store.BasePersistentStore;
import com.funambol.framework.server.store.Clause;
import com.funambol.framework.server.store.PersistentStore;
import com.funambol.framework.server.store.PersistentStoreException;
import com.funambol.framework.server.store.NotFoundException;
import com.funambol.framework.server.store.PreparedWhere;
import com.funambol.framework.server.store.WhereClause;


/**
 * This is the store for information regarding the Sync4j engine such as users,
 * principals, ecc. Currently it persistes the following classes:
 *
 * <ul>
 * <li>com.funambol.framework.security.Sync4jPrincipal</li>
 * </ul>
 *
 * @version $Id: EnginePersistentStore.java,v 1.5 2006/11/15 15:23:19 nichele Exp $
 *
 */
public class EnginePersistentStore
extends BasePersistentStore
implements PersistentStore, java.io.Serializable {

    // --------------------------------------------------------------- Constants

    private static final String OPT_INSERT     = "INSERT";

    public static final byte SQL_INSERT_PRINCIPAL             =  0;
    public static final byte SQL_GET_PRINCIPAL                =  1;
    public static final byte SQL_SELECT_PRINCIPAL             =  2;
    public static final byte SQL_UPDATE_PRINCIPAL             =  3;
    public static final byte SQL_SELECT_ALL_PRINCIPALS        =  4;
    public static final byte SQL_INSERT_SOURCE                =  5;
    public static final byte SQL_GET_SOURCE                   =  6;
    public static final byte SQL_UPDATE_SOURCE                =  7;
    public static final byte SQL_SELECT_ALL_SOURCES           =  8;
    public static final byte SQL_SELECT_ALL_DEVICES           =  9;
    public static final byte SQL_GET_DEVICE                   = 10;
    public static final byte SQL_INSERT_DEVICE                = 11;
    public static final byte SQL_UPDATE_DEVICE                = 12;
    public static final byte SQL_DELETE_DEVICE                = 13;
    public static final byte SQL_DELETE_DEVICE_PRINCIPAL      = 14;
    public static final byte SQL_DELETE_PRINCIPAL             = 15;
    public static final byte SQL_DELETE_CLIENT_MAPPING        = 16;
    public static final byte SQL_DELETE_LAST_SYNC             = 17;
    public static final byte SQL_GET_COUNTER                  = 18;
    public static final byte SQL_UPDATE_COUNTER               = 19;
    public static final byte SQL_DELETE_SOURCE                = 20;
    public static final byte SQL_INSERT_SYNCSOURCE            = 21;
    public static final byte SQL_UPDATE_SYNCSOURCE            = 22;
    public static final byte SQL_DELETE_SOURCE_CLIENT_MAPPING = 23;
    public static final byte SQL_DELETE_SOURCE_LAST_SYNC      = 24;
    public static final byte SQL_COUNT_DEVICES                = 25;
    public static final byte SQL_COUNT_PRINCIPALS             = 26;
    public static final byte SQL_GET_DEVICE_DM_STATE          = 27;
    public static final byte SQL_UPDATE_DEVICE_DM_STATE       = 28;
    public static final byte SQL_INSERT_DEVICE_DM_STATE       = 29;
    public static final byte SQL_DELETE_DEVICE_DM_STATE       = 30;
    public static final byte SQL_SELECT_ALL_DM_STATE          = 31;

    public static final String NS_PRINCIPAL = "principal";
    public static final String NS_DEVICE    = "device"   ;
    public static final String NS_DM_STATE  = "dmstate"  ;

    // -------------------------------------------------------------- Properties

    private String[] sql = null;

    public void setSql(String[] sql) {
        this.sql = sql;
    }

    public String[] getSql() {
        return this.sql;
    }

    // ------------------------------------------------------------ Private data
    private static final String SEARCH_COUNT_DEVICES    = "SCD";
    private static final String SEARCH_COUNT_PRINCIPALS = "SCP";

    // ------------------------------------------------------------ Constructors

    // ---------------------------------------------------------- Public methods

    public boolean delete(Object o)
    throws PersistentStoreException {
        if (o instanceof Sync4jDevice) {
            deleteDevice((Sync4jDevice) o);
            return true;
        } else if (o instanceof Sync4jPrincipal) {
            deletePrincipal((Sync4jPrincipal) o);
            return true;
        } else if (o instanceof Sync4jSource) {
            deleteSource((Sync4jSource) o);
            return true;
        } else if (o instanceof DeviceDMState) {
            deleteDeviceDMState((DeviceDMState) o);
            return true;
        }

        return false;
    }

    public boolean store(String id, Object o, String operation) throws PersistentStoreException {
        if (o instanceof Sync4jSource) {
            if (operation.equals(OPT_INSERT)) {
                insertSyncSource(id, (Sync4jSource)o);
            } else {
                updateSyncSource(id, (Sync4jSource)o);
            }
            return true;
        }

        return false;
    }

    public boolean store(Object o)
    throws PersistentStoreException {
        if (o instanceof Sync4jPrincipal) {
            storePrincipal((Sync4jPrincipal)o);
            return true;
        } else if (o instanceof Sync4jSource) {
            storeSource((Sync4jSource)o);
            return true;
        } else if (o instanceof Sync4jDevice) {
            storeDevice((Sync4jDevice)o);
            return true;
        } else if (o instanceof DeviceDMState) {
            storeDeviceDMState((DeviceDMState)o);
            return true;
        }

        return false;
    }

    public boolean read(Object o)
    throws PersistentStoreException {
        if (o instanceof Sync4jPrincipal) {
            readPrincipal((Sync4jPrincipal) o);
            return true;
        } else if (o instanceof Sync4jSource) {
            readSource((Sync4jSource) o);
            return true;
        } else if (o instanceof Sync4jDevice) {
            readDevice((Sync4jDevice) o);
            return true;
        } else if (o instanceof DeviceDMState) {
            readDeviceDMState((DeviceDMState)o);
            return true;
        }

        return false;
    }

    public Object[] read(Class objClass) throws PersistentStoreException {
        if (objClass.equals(Sync4jSource.class)) {
            return readAllSources();
        }

        return null;
    }

    /**
     * Read all informations
     * @param object whichever object Sync
     * @param clause condition where for select
     *
     * @return Object[] array of object
     */
    public Object[] read(Object o, Clause clause) throws PersistentStoreException {
        if (o instanceof Sync4jPrincipal) {
            return readAllPrincipals(clause);
        } else if (o instanceof Sync4jDevice) {
            return readAllDevices(clause);
        } else if (o instanceof String) {
            if (o.equals(SEARCH_COUNT_DEVICES)) {
                return readCountDevices(clause);
            } else if (o.equals(SEARCH_COUNT_PRINCIPALS)) {
                return readCountPrincipals(clause);
            }
        } else if (o instanceof Sync4jSource) {
            return readSource((Sync4jSource)o, clause);
        } else if (o instanceof DeviceDMState) {
            return readDeviceDMState(clause);
        }

        return null;
    }

    // --------------------------------------------------------- Private methods


    // -------------------------------------------------------------------------
    // Sync4jPricipal
    // -------------------------------------------------------------------------

    private void storePrincipal(Sync4jPrincipal p)
    throws PersistentStoreException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int n = 0;

        try {
            conn = dataSource.getConnection();

            if (p.getId() != null && !p.getId().equals("")) {

                stmt = conn.prepareStatement(sql[SQL_UPDATE_PRINCIPAL]);

                stmt.setString(1, p.getUsername());
                stmt.setString(2, p.getDeviceId());
                stmt.setString(3, p.getId()      );

                n = stmt.executeUpdate();

                stmt.close();
            } else {
                //check if the principal already exist: verify username-deviceid

                stmt = conn.prepareStatement(sql[SQL_SELECT_PRINCIPAL]);

                stmt.setString(1, p.getUsername());
                stmt.setString(2, p.getDeviceId());

                rs = stmt.executeQuery();

                if (rs.next() == false) {

                    n = 0;

                } else {

                    n = 1;

                    p.setId       (rs.getString(1));
                    p.setUsername (rs.getString(2));
                    p.setDeviceId (rs.getString(3));
                    p.setEmail    (rs.getString(4));
                    p.setFirstName(rs.getString(5));
                    p.setLastName (rs.getString(6));
                }

                stmt.close();
                rs.close();
            }

            if (n == 0) {
                //
                // The first time!!!
                //

                int principalId = this.readCounter(NS_PRINCIPAL);
                p.setId("" + principalId);

                stmt = conn.prepareStatement(sql[SQL_INSERT_PRINCIPAL]);

                stmt.setString(1, p.getId()      );
                stmt.setString(2, p.getUsername());
                stmt.setString(3, p.getDeviceId());

                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new PersistentStoreException("Error storing principal " + p, e);
        } finally {
            DBTools.close(conn, stmt, rs);
        }
    }

    /**
     * Read the principal from the data store. If <i>getId()</i> returns null,
     * it tries to read the principal from username/device, otherwise throws id
     * is used for the lookup.
     *
     * @throws PersistentException in case of error reading the data store
     */
    private void readPrincipal(Sync4jPrincipal p)
    throws PersistentStoreException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dataSource.getConnection();

            if (p.getId() == null) {

                stmt = conn.prepareStatement(sql[SQL_SELECT_PRINCIPAL]);

                stmt.setString(1, p.getUsername());
                stmt.setString(2, p.getDeviceId());
            } else {

                stmt = conn.prepareStatement(sql[SQL_GET_PRINCIPAL]);

                stmt.setString(1, p.getId());
            }

            rs = stmt.executeQuery();

            if (rs.next() == false) {
                throw new NotFoundException("Principal not found for "
                + p.toString()
                );
            }

            p.setId       (rs.getString(1));
            p.setUsername (rs.getString(2));
            p.setDeviceId (rs.getString(3));
            p.setEmail    (rs.getString(4));
            p.setFirstName(rs.getString(5));
            p.setLastName (rs.getString(6));
        } catch (SQLException e) {
            throw new PersistentStoreException("Error reading principal " + p, e);
        } finally {
            DBTools.close(conn, stmt, rs);
        }
    }

    /**
     * Read all principals
     *
     * @return an array with the principals (empty if no objects are found)
     *
     * @throws PersistentException in case of error reading the data store
     */
    private Object[] readAllPrincipals(Clause clause)
    throws PersistentStoreException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        ArrayList<Sync4jPrincipal> ret = new ArrayList<Sync4jPrincipal>();

        try {
            conn = dataSource.getConnection();

            PreparedWhere where = clause.getPreparedWhere();

            String query = sql[SQL_SELECT_ALL_PRINCIPALS];
            if (where.sql.length() > 0){
                query += " where " + where.sql;
            }

            stmt = conn.prepareStatement(query);

            for (int i=0; i<where.parameters.length; ++i) {
                stmt.setObject(i+1, where.parameters[i]);
            }

            rs = stmt.executeQuery();

            while (rs.next()) {
                ret.add(
                    new Sync4jPrincipal(
                        rs.getString(1),
                        rs.getString(2),
                        rs.getString(3)
                    )
                );
            }

            return ret.toArray(new Sync4jPrincipal[ret.size()]);
        } catch (SQLException e) {
            throw new PersistentStoreException("Error reading principals", e);
        } finally {
            DBTools.close(conn, stmt, rs);
        }
    }


    /**
     * Delete the principal from the data store.
     *
     * @param p the principal
     *
     * @throws PersistentException in case of error reading the data store
     */
    private void deletePrincipal(Sync4jPrincipal p)
    throws PersistentStoreException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = dataSource.getConnection();

            stmt = conn.prepareStatement(sql[SQL_DELETE_PRINCIPAL]);

            stmt.setString(1, p.getId());

            stmt.executeUpdate();


        } catch (SQLException e) {
            throw new PersistentStoreException("Error deleting the principal " + p, e);
        } finally {
            DBTools.close(conn, stmt, null);
        }
    }

    /**
     * Select the number of principals that satisfy the conditions specified in input
     *
     * @return int number of principals
     */
    public Object[] readCountPrincipals(Clause clause) throws PersistentStoreException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int n = 0;

        try {
            conn = dataSource.getConnection();

            PreparedWhere where = clause.getPreparedWhere();

            String query = sql[SQL_COUNT_PRINCIPALS];

            if (where.sql.length()>0) {
                query += " where " + where.sql;
            }

            stmt = conn.prepareStatement(query);

            for (int i=0; i<where.parameters.length; ++i) {
                stmt.setObject(i+1, where.parameters[i]);
            }

            rs = stmt.executeQuery();
            while (rs.next()) {
                n = rs.getInt(1);
            }

            String[] s = new String[1];
            s[0] = "" + n;
            return s;

        } catch (SQLException e) {
            throw new PersistentStoreException("Error reading count principals ", e);
        } finally {
            DBTools.close(conn, stmt, rs);
        }
    }

    // -------------------------------------------------------------------------
    // Sync4jDevice
    // -------------------------------------------------------------------------

    private Object[] readAllDevices(Clause clause)
    throws PersistentStoreException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        ArrayList<Sync4jDevice> ret = new ArrayList<Sync4jDevice>();

        try {
            conn = dataSource.getConnection();

            PreparedWhere where = clause.getPreparedWhere();

            String query = sql[SQL_SELECT_ALL_DEVICES];
            if (where.sql.length() > 0) {
                query += " where " + where.sql;
            }

            stmt = conn.prepareStatement(query);

            for (int i=0; i<where.parameters.length; ++i) {
                stmt.setObject(i+1, where.parameters[i]);
            }

            rs = stmt.executeQuery();

            while (rs.next()) {
                ret.add(
                    new Sync4jDevice(
                        rs.getString(1),
                        rs.getString(2),
                        rs.getString(3)
                    )
                );
            }

            return ret.toArray(new Sync4jDevice[ret.size()]);
        } catch (SQLException e) {
            throw new PersistentStoreException("Error reading devices", e);
        } finally {
            DBTools.close(conn, stmt, rs);
        }
    }

    /**
     * Read the device from the data store.
     *
     * @param d the device
     *
     * @throws PersistentException in case of error reading the data store
     */
    private void readDevice(Sync4jDevice d)
    throws PersistentStoreException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String value;
        try {
            conn = dataSource.getConnection();

            stmt = conn.prepareStatement(sql[SQL_GET_DEVICE]);
            stmt.setString(1, d.getDeviceId());

            rs = stmt.executeQuery();

            if (rs.next() == false) {
                throw new NotFoundException("Device not found for "
                + d.getDeviceId()
                );
            }

            d.setDescription(rs.getString(1));
            d.setType       (rs.getString(2));
            d.setDigest     (rs.getString(3));
            value = rs.getString(4);
            if (value == null) {
                d.setClientNonce(null);
            } else if (value.equals("")) {
                d.setClientNonce(new byte[0]);
            } else {
                d.setClientNonce(Base64.decode(value.getBytes()));
            }

            value = rs.getString(5);
            if (value == null) {
                d.setServerNonce(null);
            } else if (value.equals("")) {
                d.setServerNonce(new byte[0]);
            } else {
                d.setServerNonce(Base64.decode(value.getBytes()));
            }

            d.setServerPassword(rs.getString(6));

        } catch (SQLException e) {
            throw new PersistentStoreException("Error reading the device " + d, e);
        } finally {
            DBTools.close(conn, stmt, rs);
        }
    }

    /**
     * Insert or Update the device from the data store.
     *
     * @param d the device
     *
     * @throws PersistentException in case of error reading the data store
     */
    private void storeDevice(Sync4jDevice d)
    throws PersistentStoreException {
        Connection conn = null;
        PreparedStatement stmt = null;
        int n = 0;

        try {
            conn = dataSource.getConnection();

            if (d.getDeviceId() != null && !d.getDeviceId().equals("")) {
                stmt = conn.prepareStatement(sql[SQL_UPDATE_DEVICE]);

                stmt.setString(1, d.getDescription());
                stmt.setString(2, d.getType());
                stmt.setString(3, d.getDigest());
                byte[] nonce = d.getClientNonce();
                stmt.setString(4, ((nonce != null) ? new String(Base64.encode(nonce)) : null));
                nonce = d.getServerNonce();
                stmt.setString(5, ((nonce != null) ? new String(Base64.encode(nonce)) : null));
                stmt.setString(6, d.getServerPassword());

                stmt.setString(7, d.getDeviceId());


                n = stmt.executeUpdate();

                stmt.close();
            }

            if (n == 0) {
                //
                // The first time!!!
                //
                if (d.getDeviceId() == null || d.getDeviceId().equals("")) {
                    int deviceId = this.readCounter(NS_DEVICE);
                    d.setDeviceId("" + deviceId);
                }

                stmt = conn.prepareStatement(sql[SQL_INSERT_DEVICE]);

                stmt.setString(1, d.getDeviceId());
                stmt.setString(2, d.getDescription());
                stmt.setString(3, d.getType());
                stmt.setString(4, d.getDigest());
                byte[] nonce = d.getClientNonce();
                stmt.setString(5, ((nonce != null) ? new String(Base64.encode(nonce)) : null));
                nonce = d.getServerNonce();
                stmt.setString(6, ((nonce != null) ? new String(Base64.encode(nonce)) : null));
                stmt.setString(7, d.getServerPassword());


                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new PersistentStoreException("Error storing the device " + d, e);
        } finally {
            DBTools.close(conn, stmt, null);
        }
    }

    /**
     * Delete the device from the data store.
     *
     * @param d the device
     *
     * @throws PersistentException in case of error reading the data store
     */
    private void deleteDevice(Sync4jDevice d)
    throws PersistentStoreException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = dataSource.getConnection();

            //select and delete all principal for this device
            WhereClause c = new WhereClause("device", new String[] {d.getDeviceId()}, WhereClause.OPT_EQ, true);
            Sync4jPrincipal[] sp = (Sync4jPrincipal[])readAllPrincipals(c);

            for (int i=0; (sp != null) && i<sp.length; i++) {
                Sync4jPrincipal syncPrincipal = sp[i];
                this.deletePrincipal(syncPrincipal);
            }

            stmt = conn.prepareStatement(sql[SQL_DELETE_DEVICE]);

            stmt.setString(1, d.getDeviceId());

            stmt.executeUpdate();


        } catch (SQLException e) {
            throw new PersistentStoreException("Error deleting the device " + d, e);
        } finally {
            DBTools.close(conn, stmt, null);
        }
    }

    /**
     * Select the number of devices that satisfy the conditions specified in input
     *
     * @return int number of devices
     */
    public Object[] readCountDevices(Clause clause) throws PersistentStoreException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int n = 0;

        try {
            conn = dataSource.getConnection();

            PreparedWhere where = clause.getPreparedWhere();

            String query = sql[SQL_COUNT_DEVICES];
            if (where.sql.length()>0) {
                query += " where " + where.sql;
            }

            stmt = conn.prepareStatement(query);

            for (int i=0; i<where.parameters.length; ++i) {
                stmt.setObject(i+1, where.parameters[i]);
            }

            rs = stmt.executeQuery();
            while (rs.next()) {
                n = rs.getInt(1);
            }

            String[] s = new String[1];
            s[0] = "" + n;
            return s;

        } catch (SQLException e) {
            throw new PersistentStoreException("Error reading count devices ", e);
        } finally {
            DBTools.close(conn, stmt, rs);
        }
    }

    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // Sync4jSource
    // -------------------------------------------------------------------------

    /**
     * Read all sources
     *
     * @return an array with the sources (empty if no objects are found)
     *
     * @throws PersistentException in case of error reading the data store
     */
    private Object[] readAllSources()
    throws PersistentStoreException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        ArrayList<Sync4jSource> ret = new ArrayList<Sync4jSource>();

        try {
            conn = dataSource.getConnection();

            stmt = conn.prepareStatement(sql[SQL_SELECT_ALL_SOURCES]);

            rs = stmt.executeQuery();

            while (rs.next()) {
                ret.add(
                    new Sync4jSource(
                        rs.getString(1),
                        rs.getString(2)
                    )
                );
            }

            return ret.toArray(new Sync4jSource[ret.size()]);
        } catch (SQLException e) {
            throw new PersistentStoreException("Error reading sources", e);
        } finally {
            DBTools.close(conn, stmt, rs);
        }
    }

    private void storeSource(Sync4jSource s)
    throws PersistentStoreException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = dataSource.getConnection();

            stmt = conn.prepareStatement(sql[SQL_UPDATE_SOURCE]);

            stmt.setString(1, s.getConfig());
            stmt.setString(2, s.getUri()   );

            int n = stmt.executeUpdate();

            if (n == 0) {
                //
                // The first time!!!
                //
                stmt.close();

                stmt = conn.prepareStatement(sql[SQL_INSERT_SOURCE]);

                stmt.setString(1, s.getUri()   );
                stmt.setString(2, s.getConfig());

                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new PersistentStoreException("Error storing the source " + s, e);
        } finally {
            DBTools.close(conn, stmt, null);
        }
    }

    /**
     * Read the source from the data store.
     *
     * @param s the source
     *
     * @throws PersistentException in case of error reading the data store
     */
    private void readSource(Sync4jSource s)
    throws PersistentStoreException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dataSource.getConnection();

            stmt = conn.prepareStatement(sql[SQL_GET_SOURCE]);
            stmt.setString(1, s.getUri());

            rs = stmt.executeQuery();

            if (rs.next() == false) {
                throw new NotFoundException("Source not found for "
                + s.getUri()
                );
            }

            s.setUri   (rs.getString(1));
            s.setConfig(rs.getString(2));
        } catch (SQLException e) {
            throw new PersistentStoreException("Error reading the source " + s, e);
        } finally {
            DBTools.close(conn, stmt, rs);
        }
    }

    private Object[] readSource(Sync4jSource s,Clause clause)
    throws PersistentStoreException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        ArrayList<Sync4jSource> ret = new ArrayList<Sync4jSource>();

        try {
            conn = dataSource.getConnection();

            PreparedWhere where = clause.getPreparedWhere();

            String query = sql[SQL_SELECT_ALL_SOURCES];
            if (where.sql.length() > 0) {
                query += " where " + where.sql;
            }

            stmt = conn.prepareStatement(query);

            for (int i=0; i<where.parameters.length; ++i) {
                stmt.setObject(i+1, where.parameters[i]);
            }

            rs = stmt.executeQuery();

            while (rs.next()) {
                ret.add(
                    new Sync4jSource(
                        rs.getString(1),
                        rs.getString(2)
                    )
                );
            }

            return ret.toArray(new Sync4jSource[ret.size()]);
        } catch (SQLException e) {
            throw new PersistentStoreException("Error reading sources", e);
        } finally {
            DBTools.close(conn, stmt, rs);
        }
    }

    private void deleteSource(Sync4jSource s)
    throws PersistentStoreException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = dataSource.getConnection();

            stmt = conn.prepareStatement(sql[SQL_DELETE_SOURCE_CLIENT_MAPPING]);

            stmt.setString(1, s.getUri());

            stmt.executeUpdate();

            stmt.close();
            stmt = conn.prepareStatement(sql[SQL_DELETE_SOURCE_LAST_SYNC]);

            stmt.setString(1, s.getUri());

            stmt.executeUpdate();

            stmt.close();

            stmt = conn.prepareStatement(sql[SQL_DELETE_SOURCE]);

            stmt.setString(1, s.getUri());

            stmt.executeUpdate();

            stmt.close();

        } catch (SQLException e) {
            throw new PersistentStoreException("Error deleting the source " + s, e);
        } finally {
            DBTools.close(conn, stmt, null);
        }
    }

    /**
     * Insert a new SyncSource
     */
    private void insertSyncSource(String sourcetypeid, Sync4jSource ss)
    throws PersistentStoreException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = dataSource.getConnection();

            stmt = conn.prepareStatement(sql[SQL_INSERT_SYNCSOURCE]);

            stmt.setString(1, ss.getUri());
            stmt.setString(2, ss.getConfig());
            stmt.setString(3, ss.getSourceName());
            stmt.setString(4, sourcetypeid);

            stmt.executeUpdate();
            stmt.close();

        } catch (SQLException e) {
            throw new PersistentStoreException("Error storing the syncsource " + ss, e);
        } finally {
            DBTools.close(conn, stmt, null);
        }
    }

    /**
     * Update an existing SyncSource
     */
    private void updateSyncSource(String sourcetypeid, Sync4jSource ss)
    throws PersistentStoreException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = dataSource.getConnection();

            stmt = conn.prepareStatement(sql[SQL_UPDATE_SYNCSOURCE]);

            stmt.setString(1, ss.getConfig());
            stmt.setString(2, ss.getSourceName());
            stmt.setString(3, ss.getSourceTypeId());
            stmt.setString(4, ss.getUri()   );

            stmt.executeUpdate();
            stmt.close();

        } catch (SQLException e) {
            throw new PersistentStoreException("Error updating the syncsource " + ss, e);
        } finally {
            DBTools.close(conn, stmt, null);
        }
    }

    // -------------------------------------------------------------------------
    // DeviceDMState
    // -------------------------------------------------------------------------

    /**
     * Read a device DM state from the data store.
     *
     * @param d the device DM state
     *
     * @throws PersistentException in case of error reading the data store
     * @throws NotFoundException if no device DM state is found with the given
     *                           device id
     */
    private void readDeviceDMState(DeviceDMState d)
    throws PersistentStoreException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String value;
        try {
            conn = dataSource.getConnection();

            stmt = conn.prepareStatement(sql[SQL_GET_DEVICE_DM_STATE]);
            stmt.setString(1, d.deviceId);

            rs = stmt.executeQuery();

            if (rs.next() == false) {
                throw new NotFoundException("Device not found for "
                + d.deviceId
                );
            }

            d.mssid     = rs.getString(1)     ;
            value       = rs.getString(2)     ;
            d.state     = (value == null)
                        ? 0
                        : (byte)value.charAt(0);
            d.start     = rs.getTimestamp(3)   ;
            d.end       = rs.getTimestamp(4)   ;
            d.operation = rs.getString(5)      ;
            d.info      = rs.getString(6)      ;

        } catch (SQLException e) {
            throw new PersistentStoreException("Error reading the device " + d, e);
        } finally {
            DBTools.close(conn, stmt, rs);
        }
    }

    /**
     * Insert or update the given device DM state into the data store.
     *
     * @param d the device DM state
     *
     * @throws PersistentException in case of data store error
     */
    private void storeDeviceDMState(DeviceDMState d)
    throws PersistentStoreException {
        Connection conn = null;
        PreparedStatement stmt = null;
        int n = 0;

        try {
            conn = dataSource.getConnection();

            if (d.id != null) {
                stmt = conn.prepareStatement(sql[SQL_UPDATE_DEVICE_DM_STATE]);

                stmt.setString   (1, d.deviceId);
                stmt.setString   (2, d.mssid);
                stmt.setString   (3, String.valueOf((char)d.state));
                stmt.setTimestamp(4, (d.start == null)
                                   ? null
                                   : new Timestamp(d.start.getTime()));
                stmt.setTimestamp(5, (d.end == null)
                                   ? null
                                   : new Timestamp(d.end.getTime()));
                stmt.setString   (6, d.operation);
                stmt.setString   (7, d.info);
                stmt.setString   (8, d.id);

                n = stmt.executeUpdate();

                stmt.close(); stmt = null;
            }

            if (n == 0) {
                //
                // The first time!!!
                //

                if (d.id == null) {
                    d.id = String.valueOf(readCounter(NS_DM_STATE));
                }

                stmt = conn.prepareStatement(sql[SQL_INSERT_DEVICE_DM_STATE]);

                stmt.setString   (1, d.id);
                stmt.setString   (2, d.deviceId);
                stmt.setString   (3, d.mssid);
                stmt.setString   (4, String.valueOf((char)d.state));
                stmt.setTimestamp(5, (d.start == null)
                                   ? null
                                   : new Timestamp(d.start.getTime()));
                stmt.setTimestamp(6, (d.end == null)
                                   ? null
                                   : new Timestamp(d.end.getTime()));
                stmt.setString   (7, d.operation);
                stmt.setString   (8, d.info);

                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new PersistentStoreException("Error storing the device DM state " + d, e);
        } finally {
            DBTools.close(conn, stmt, null);
        }
    }

    /**
     * Read the DM states identified by the given clause
     *
     * @param clause the selecting clause
     *
     * @return an array with the principals (empty if no objects are found)
     *
     * @throws PersistentException in case of error reading the data store
     */
    private Object[] readDeviceDMState(Clause clause)
    throws PersistentStoreException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        ArrayList<DeviceDMState> ret = new ArrayList<DeviceDMState>();

        try {
            conn = dataSource.getConnection();

            PreparedWhere where = clause.getPreparedWhere();

            String query = sql[SQL_SELECT_ALL_DM_STATE];
            if (where.sql.length() > 0){
                query += " where " + where.sql;
            }

            stmt = conn.prepareStatement(query);

            for (int i=0; i<where.parameters.length; ++i) {
                stmt.setObject(i+1, where.parameters[i]);
            }

            rs = stmt.executeQuery();

            String value = null;
            DeviceDMState dms = null;
            while (rs.next()) {
                dms = new DeviceDMState();

                dms.id        = rs.getString   (1);
                dms.deviceId  = rs.getString   (2);
                dms.mssid     = rs.getString   (3);
                value         = rs.getString   (4);
                dms.state     = (value != null)
                              ? (byte)value.charAt(0)
                              : DeviceDMState.STATE_UNKNOWN
                              ;
                dms.start     = rs.getTimestamp(5);
                dms.end       = rs.getTimestamp(6);
                dms.operation = rs.getString   (7);
                dms.info      = rs.getString   (8);

                ret.add(dms);
            }

            return ret.toArray(new DeviceDMState[ret.size()]);
        } catch (SQLException e) {
            throw new PersistentStoreException("Error reading device managment state", e);
        } finally {
            DBTools.close(conn, stmt, rs);
        }
    }

    /**
     * Delete the given device DM state from the data store.
     *
     * @param d the device DM state
     *
     * @throws PersistentException in case of error reading the data store
     */
    private void deleteDeviceDMState(DeviceDMState d)
    throws PersistentStoreException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = dataSource.getConnection();

            stmt = conn.prepareStatement(sql[SQL_DELETE_DEVICE_DM_STATE]);

            stmt.setString(1, d.id);

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new PersistentStoreException("Error deleting the device " + d, e);
        } finally {
            DBTools.close(conn, stmt, null);
        }
    }

    /**
     * Read the counter of the specific id in order to use it like primary key
     * in case of inserting a new record into data store.
     *
     * @param idSpace the idSpace
     *
     * @throws PersistentException in case of error reading the data store
     */
    public int readCounter(String idSpace)
    throws PersistentStoreException {
        return readCounter(idSpace, 1);
    }

    /**
     * Read the counter of the specific id in order to use it like primary key
     * in case of inserting a new record into data store and increment it of the
     * given increment
     *
     * @param idSpace the idSpace
     * @param increment the increment
     *
     * @throws PersistentException in case of error reading the data store
     */
    public int readCounter(String idSpace, int increment) throws PersistentStoreException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int counter = 0;

        try {
            conn = dataSource.getConnection();

            java.sql.DatabaseMetaData dmd = conn.getMetaData();

            if (dmd.supportsSelectForUpdate()) {
                stmt = conn.prepareStatement(sql[SQL_GET_COUNTER]);
            } else {
                stmt = conn.prepareStatement("select counter from sync4j_id where idspace=?");
            }

            stmt.setString(1, idSpace);

            rs = stmt.executeQuery();

            if (rs.next() == false) {
                throw new NotFoundException("Counter not found for "
                                            + idSpace
                    );
            }

            counter = rs.getInt(1);

            stmt.close();

            stmt = conn.prepareStatement(sql[SQL_UPDATE_COUNTER]);
            stmt.setInt(1, counter + increment);
            stmt.setString(2, idSpace);

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new PersistentStoreException("Error reading the counter " + counter, e);
        } finally {
            DBTools.close(conn, stmt, rs);
        }
        return counter;
    }



}
