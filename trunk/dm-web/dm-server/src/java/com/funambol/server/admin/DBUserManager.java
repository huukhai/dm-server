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

package com.funambol.server.admin;


import java.util.ArrayList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import com.funambol.framework.tools.DBTools;
import com.funambol.framework.server.SyncUser;
import com.funambol.framework.server.store.Clause;
import com.funambol.framework.server.store.PreparedWhere;
import com.funambol.framework.server.store.PersistentStoreException;

/**
 * This class implements the UserManager: store and read users and roles from datastore.
 *
 * @version $Id: DBUserManager.java,v 1.2 2006/08/07 21:09:24 nichele Exp $
 *
 */
public class DBUserManager implements UserManager, java.io.Serializable {

    // --------------------------------------------------------------- Constants
    public final static int SQL_SELECT_ROLES           = 0;
    public final static int SQL_SELECT_USERS           = 1;
    public final static int SQL_SELECT_USER_ROLES      = 2;
    public final static int SQL_UPDATE_USER            = 3;
    public final static int SQL_INSERT_USER            = 4;
    public final static int SQL_DELETE_USER            = 5;
    public final static int SQL_DELETE_USER_ROLES      = 6;
    public final static int SQL_INSERT_USER_ROLES      = 7;
    public final static int SQL_DELETE_PRINCIPAL       = 8;
    public final static int SQL_SELECT_USER_PRINCIPALS = 9;
    public final static int SQL_DELETE_CLIENT_MAPPING  = 10;
    public final static int SQL_DELETE_LAST_SYNC       = 11;

    public final static String SQL_COUNT_USERS =
        "select count(*) as users from fnbl_user ";

    public static final String LOG_NAME = "server.admin";

    // -------------------------------------------------------------- Properties
    private String[] sql = null;

    public void setSql(String[] sql) {
        this.sql = sql;
    }

    public String[] getSql() {
        return this.sql;
    }

    /**
     * The JNDI name of the datasource to be used
     */
    private String jndiDataSourceName = null;

    public String getJndiDataSourceName() {
        return this.jndiDataSourceName;
    }

    public void setJndiDataSourceName(String jndiDataSourceName) throws PersistentStoreException {
        this.jndiDataSourceName = jndiDataSourceName;

        if (jndiDataSourceName == null) {
            ds = null;
        }

        try {
            InitialContext ctx = new InitialContext();
            ds = (DataSource) ctx.lookup(jndiDataSourceName);
        } catch (NamingException e) {
            throw new PersistentStoreException("Data source "
            + jndiDataSourceName
            + " not found"
            , e
            );
        }
    }

    // ------------------------------------------------------------ Private data
    protected transient DataSource ds = null;

    // ------------------------------------------------------------ Constructors

    // ---------------------------------------------------------- Public methods
    /*
     * Search all roles available.
     *
     * @return array of roles
     */
    public String[] getRoles() throws PersistentStoreException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        ArrayList<String> roles = new ArrayList<String>();
        try {
            conn = ds.getConnection();

            stmt = conn.prepareStatement(sql[SQL_SELECT_ROLES]);

            rs = stmt.executeQuery();
            while (rs.next()) {
                //role then space then description
                roles.add(rs.getString(1) + ' ' + rs.getString(2));
            }
            return (String[])roles.toArray(new String[roles.size()]);

        } catch (SQLException e) {
            throw new PersistentStoreException("Error reading roles ", e);
        } finally {
            DBTools.close(conn, stmt, rs);
        }
    }

    public SyncUser[] getUsers(Clause clause) throws PersistentStoreException {
        PreparedWhere pw = clause.getPreparedWhere();

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        ArrayList<SyncUser> users = new ArrayList<SyncUser>();
        try {
            conn = ds.getConnection();

            String query = sql[SQL_SELECT_USERS] + " where upper(internal_user)='N'" ;
            if (pw.sql.length() > 0) {
                query += " and " + pw.sql;
            }

            stmt = conn.prepareStatement(query);

            for(int i=0; i<pw.parameters.length; ++i) {
                stmt.setObject(i+1, pw.parameters[i]);
            }

            rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(
                 new SyncUser(
                    rs.getString(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4),
                    rs.getString(5),
                    null
                 )
                );
            }
            return users.toArray(new SyncUser[users.size()]);

        } catch (SQLException e) {
            throw new PersistentStoreException("Error reading roles ", e);
        } finally {
            DBTools.close(conn, stmt, rs);
        }
    }

    public void getUserRoles(SyncUser syncUser) throws PersistentStoreException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        ArrayList<String> ret = new ArrayList<String>();
        try {
            conn = ds.getConnection();

            stmt = conn.prepareStatement(sql[SQL_SELECT_USER_ROLES]);
            stmt.setString(1, syncUser.getUsername());

            rs = stmt.executeQuery();

            while (rs.next()) {
                ret.add(rs.getString(1));
            }

            syncUser.setRoles(ret.toArray(new String[ret.size()]));

        } catch (SQLException e) {
            throw new PersistentStoreException("Error reading roles ", e);
        } finally {
            DBTools.close(conn, stmt, rs);
        }
    }

    public void setUser(SyncUser user) throws PersistentStoreException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = ds.getConnection();

            stmt = conn.prepareStatement(sql[SQL_UPDATE_USER]);
            stmt.setString(1, user.getPassword());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getFirstname());
            stmt.setString(4, user.getLastname());
            stmt.setString(5, user.getUsername());

            stmt.executeUpdate();

            stmt.close();

            //for updating roles is necessary delete old roles and insert new roles
            stmt = conn.prepareStatement(sql[SQL_DELETE_USER_ROLES]);

            stmt.setString(1, user.getUsername());

            stmt.executeUpdate();

            stmt.close();

            String[] roles = user.getRoles();
            for (int i=0; i<roles.length; i++) {
                stmt = conn.prepareStatement(sql[SQL_INSERT_USER_ROLES]);
                stmt.setString(1, roles[i]);
                stmt.setString(2, user.getUsername());

                stmt.executeUpdate();
            }


        } catch (SQLException e) {
            throw new PersistentStoreException("Error updating user " + user, e);
        } finally {
            DBTools.close(conn, stmt, null);
        }
    }

    public void insertUser(SyncUser user) throws PersistentStoreException {
        Connection conn = null;
        PreparedStatement stmt = null;
        int n = 0;

        try {
            conn = ds.getConnection();

            stmt = conn.prepareStatement(sql[SQL_UPDATE_USER]);
            stmt.setString(1, user.getPassword() );
            stmt.setString(2, user.getEmail()    );
            stmt.setString(3, user.getFirstname());
            stmt.setString(4, user.getLastname() );
            stmt.setString(5, user.getUsername() );

            n = stmt.executeUpdate();

            if (n == 0) {

                stmt = conn.prepareStatement(sql[SQL_INSERT_USER]);
                stmt.setString(1, user.getUsername() );
                stmt.setString(2, user.getPassword() );
                stmt.setString(3, user.getEmail()    );
                stmt.setString(4, user.getFirstname());
                stmt.setString(5, user.getLastname() );
                stmt.setString(6, "N"                );

                stmt.executeUpdate();

                stmt.close();

                String[] roles = user.getRoles();
                for (int i=0; i<roles.length; i++) {
                    stmt = conn.prepareStatement(sql[SQL_INSERT_USER_ROLES]);
                    stmt.setString(1, roles[i]);
                    stmt.setString(2, user.getUsername());

                    stmt.executeUpdate();
                }
            } else {
                stmt.close();

                //for updating roles is necessary delete old roles and insert new roles
                stmt = conn.prepareStatement(sql[SQL_DELETE_USER_ROLES]);

                stmt.setString(1, user.getUsername());

                stmt.executeUpdate();

                stmt.close();

                String[] roles = user.getRoles();
                for (int i=0; i<roles.length; i++) {
                    stmt = conn.prepareStatement(sql[SQL_INSERT_USER_ROLES]);
                    stmt.setString(1, roles[i]);
                    stmt.setString(2, user.getUsername());

                    stmt.executeUpdate();
                }
            }

        } catch (SQLException e) {
            throw new PersistentStoreException("Error inserting user " + user, e);
        } finally {
            DBTools.close(conn, stmt, null);
        }
    }

    public void deleteUser(SyncUser user) throws PersistentStoreException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = ds.getConnection();

            stmt = conn.prepareStatement(sql[SQL_DELETE_USER]);

            stmt.setString(1, user.getUsername());

            stmt.executeUpdate();

            stmt.close();

            stmt = conn.prepareStatement(sql[SQL_DELETE_USER_ROLES]);

            stmt.setString(1, user.getUsername());

            stmt.executeUpdate();

            stmt.close();

            //delete pricipal associated
            deletePrincipal(user);

        } catch (SQLException e) {
            throw new PersistentStoreException("Error deleting user " + user, e);
        } finally {
            DBTools.close(conn, stmt, null);
        }
    }

    private void deletePrincipal(SyncUser user) throws PersistentStoreException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {

            conn = ds.getConnection();

            stmt = conn.prepareStatement(sql[SQL_SELECT_USER_PRINCIPALS]);

            stmt.setString(1, user.getUsername());

            rs = stmt.executeQuery();

            while (rs.next()) {
                String principal = rs.getString(1);

                PreparedStatement stmtCM = conn.prepareStatement(sql[SQL_DELETE_CLIENT_MAPPING]);

                stmtCM.setString(1, principal);

                stmtCM.executeUpdate();
                stmtCM.close();

                PreparedStatement stmtLS = conn.prepareStatement(sql[SQL_DELETE_LAST_SYNC]);

                stmtLS.setString(1, principal);

                stmtLS.executeUpdate();
                stmtLS.close();

            }
            stmt.close();

            stmt = conn.prepareStatement(sql[SQL_DELETE_PRINCIPAL]);

            stmt.setString(1, user.getUsername());

            stmt.executeUpdate();


        } catch (SQLException e) {
            throw new PersistentStoreException("Error deleting principal ", e);
        } finally {
            DBTools.close(conn, stmt, rs);
        }
    }

    /**
     * Select the number of users that satisfy the conditions specified in input
     *
     * @return int number of user
     */
    public int countUsers(Clause clause) throws PersistentStoreException {
        PreparedWhere where = clause.getPreparedWhere();

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int n = 0;

        try {
            conn = ds.getConnection();

            String query = SQL_COUNT_USERS + " where upper(internal_user)='N'";
            if (where.sql.length() > 0) {
                query += " and " + where.sql;
            }
            stmt = conn.prepareStatement(query);

            for(int i=0; i<where.parameters.length; ++i) {
                stmt.setObject(i+1, where.parameters[i]);
            }

            rs = stmt.executeQuery();
            while (rs.next()) {
                n = rs.getInt(1);
            }
            return n;

        } catch (SQLException e) {
            throw new PersistentStoreException("Error reading count users ", e);
        } finally {
            DBTools.close(conn, stmt, rs);
        }
    }

}
