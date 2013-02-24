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


package com.funambol.dmdemo;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import javax.sql.DataSource;

import com.funambol.framework.engine.dm.ManagementException;
import com.funambol.framework.tools.Base64;
import com.funambol.framework.tools.DBTools;
import com.funambol.framework.tools.MD5;

import org.apache.log4j.Logger;


/**
 * Contains the methods to support the users registration.
 *
 * @version $Id: RegistrationTools.java,v 1.4 2007-06-18 16:38:44 luigiafassina Exp $
 */
public class RegistrationTools {

    // --------------------------------------------------------------- Constants

    private static final String DEFAULT_USER_NAME              = "funambol";
    private static final String DEFAULT_USER_PASSWORD          = "funambol";
    private static final String DEFAULT_DEVICE_DESCRIPTION     = "phone for DM demo";
    private static final String DEFAULT_DEVICE_SERVER_PASSWORD = "srvpwd";
    private static final String DEFAULT_DEVICE_TYPE            = "phone";

    private static final String SQL_INSERT_NEW_USER_DM_DEMO =
        "INSERT INTO fnbl_user_dm_demo " +
        " (name, company, job, address, city, zip, state, country, email, phone, device_id, password) " +
        " VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";

    private static final String SQL_SELECT_USER_DM_DEMO =
        "SELECT password FROM fnbl_user_dm_demo WHERE device_id=?";

    private static final String SQL_INSERT_USER =
        "INSERT INTO fnbl_user (username, password, internal_user) values (?,?, 'N')";

    private static final String SQL_INSERT_DEVICE =
        "INSERT INTO fnbl_device (id, description, type, digest, server_password) " +
        " values (?,?,?,?,?)";

    private static final String SQL_SELECT_USER =
        "SELECT * FROM fnbl_user WHERE username=?";

    private static final String SQL_SELECT_DEVICE = "SELECT * FROM fnbl_device WHERE id=?";

    private static final String SQL_SELECT_ID_PRINCIPAL =
        "SELECT counter FROM fnbl_id WHERE idspace=?";

    private static final String SQL_UPDATE_ID_PRINCIPAL =
        "UPDATE fnbl_id SET counter=? WHERE idspace=?";

    private static final String SQL_SELECT_PRINCIPAL =
        "SELECT * FROM fnbl_principal WHERE username=? AND device=?";

    private static final String SQL_INSERT_PRINCIPAL =
        "INSERT INTO fnbl_principal (id, username, device) values (?,?,?)";

    // ------------------------------------------------------------ Private data
    private static final Logger log = Logger.getLogger(com.funambol.dmdemo.RegistrationTools.class.getName());

    private DataSource datasource = null;

    // ------------------------------------------------------------ Constructors

    private RegistrationTools() {
    }

    public RegistrationTools(DataSource ds) {
        this.datasource = ds;
    }

    // ---------------------------------------------------------- Public Methods

    /**
     * Performe login with the given deviceid and password. In this implementation
     * is checked only if the device id exist
     * @param deviceId the device id
     * @param password the password
     * @return boolean true if the user is authenticated, false otherwise
     * @throws ManagementException
     */
    public boolean loginUser(String deviceId, String password) throws ManagementException {
        return checkIfUserAlreadyExists(deviceId);
    }

    /**
     * Register a new user
     * @param name String
     * @param company String
     * @param job String
     * @param address String
     * @param city String
     * @param zip String
     * @param state String
     * @param country String
     * @param email String
     * @param phone String
     * @param deviceId String
     * @param password String
     * @throws ManagementException
     */
    public void registerNewUser(String name,
                                String company,
                                String job,
                                String address,
                                String city,
                                String zip,
                                String state,
                                String country,
                                String email,
                                String phone,
                                String deviceId) throws ManagementException {

        if (name == null || name.equals("") ||
            deviceId == null || deviceId.equals("") ||
            email == null || email.equals("")) {

            throw new ManagementException("Name, Email, and Device Id are required");
        }

        insertNewUser(name,
                      company,
                      job,
                      address,
                      city,
                      zip,
                      state,
                      country,
                      email,
                      phone,
                      deviceId,
                      "");

        String digest = setSync4jUserDigest();
        String clearUsername = setSync4jUserClear();

        setSync4jDevice(deviceId, digest);

        setSync4jPrincipal(digest, deviceId);
        setSync4jPrincipal(clearUsername, deviceId);
    }

    /**
     * Insert a new dm demo user
     * @param name String
     * @param company String
     * @param job String
     * @param address String
     * @param city String
     * @param zip String
     * @param state String
     * @param country String
     * @param email String
     * @param phone String
     * @param deviceId String
     * @param password String
     * @throws ManagementException
     */
    public void insertNewUser(String name,
                              String company,
                              String job,
                              String address,
                              String city,
                              String zip,
                              String state,
                              String country,
                              String email,
                              String phone,
                              String deviceId,
                              String password) throws ManagementException {

        Connection con = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;


        try {
            //
            // insert new dm_state
            //
            con = datasource.getConnection();
            pStmt = con.prepareStatement(SQL_INSERT_NEW_USER_DM_DEMO);

            if (name != null) {
                pStmt.setObject(1, name);
            } else {
                pStmt.setNull(1, Types.VARCHAR);
            }

            if (company != null) {
                pStmt.setObject(2, company);
            } else {
                pStmt.setNull(2, Types.VARCHAR);
            }

            if (job != null) {
                pStmt.setObject(3, job);
            } else {
                pStmt.setNull(3, Types.VARCHAR);
            }

            if (address != null) {
                pStmt.setObject(4, address);
            } else {
                pStmt.setNull(4, Types.VARCHAR);
            }

            if (city != null) {
                pStmt.setObject(5, city);
            } else {
                pStmt.setNull(5, Types.VARCHAR);
            }

            if (zip != null) {
                pStmt.setObject(6, zip);
            } else {
                pStmt.setNull(6, Types.VARCHAR);
            }

            if (state != null) {
                pStmt.setObject(7, state);
            } else {
                pStmt.setNull(7, Types.VARCHAR);
            }

            if (country != null) {
                pStmt.setObject(8, country);
            } else {
                pStmt.setNull(8, Types.VARCHAR);
            }

            if (email != null) {
                pStmt.setObject(9, email);
            } else {
                pStmt.setNull(9, Types.VARCHAR);
            }

            if (phone != null) {
                pStmt.setObject(10, phone);
            } else {
                pStmt.setNull(10, Types.VARCHAR);
            }

            if (deviceId != null) {
                pStmt.setObject(11, deviceId);
            } else {
                pStmt.setNull(11, Types.VARCHAR);
            }

            if (password != null) {
                pStmt.setObject(12, password);
            } else {
                pStmt.setNull(12, Types.VARCHAR);
            }


            int temp = pStmt.executeUpdate();

        } catch (SQLException e) {
            throw new ManagementException("Error inserting the new user", e);
        } finally {
            DBTools.close(con, pStmt, rs);
        }
    }

    /**
     * Checks if the given deviceId is already in fnbl__user_dm_demo
     * @param deviceId String
     * @return boolean
     */
    public boolean checkIfUserAlreadyExists(String deviceId) throws ManagementException {


        Connection con          = null;
        PreparedStatement pStmt = null;
        ResultSet rs            = null;

        boolean deviceFound = false;
        try {
            con = datasource.getConnection();
            pStmt = con.prepareStatement(SQL_SELECT_USER_DM_DEMO);

            pStmt.setObject(1, deviceId);

            rs = pStmt.executeQuery();

            while (rs.next()) {
                deviceFound = true;
            }
        } catch (SQLException e) {
            throw new ManagementException("Error reading the user", e);
        } finally {
            DBTools.close(con, pStmt, rs);
        }
        return deviceFound;
    }

    // --------------------------------------------------------- Private Methods

    /**
     * Save the default fnbl_user used in the dm demo with digest credential
     * @return the username (in this case is the digest)
     * @throws ManagementException
     */
    private String setSync4jUserDigest() throws ManagementException {

        String name     = DEFAULT_USER_NAME;
        String password = DEFAULT_USER_PASSWORD;

        // b64(H(name:password)
        String digest = new String(Base64.encode(MD5.digest( (name + ":" + password).getBytes())));

        Connection con = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;

        boolean userFound = false;

        try {
            //
            // checks if the user already exist
            //
            con = datasource.getConnection();
            pStmt = con.prepareStatement(SQL_SELECT_USER);

            pStmt.setObject(1, digest);

            rs = pStmt.executeQuery();

            while (rs.next()) {
                userFound = true;
            }

            if (!userFound) {
                pStmt = con.prepareStatement(SQL_INSERT_USER);
                pStmt.setObject(1, digest);
                pStmt.setObject(2, "");

                pStmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw new ManagementException("Error inserting the user", e);
        } finally {
            DBTools.close(con, pStmt, rs);
        }
        return digest;
    }

    /**
     * Save the default user used in the dm demo with clear credential
     * @return the username
     * @throws ManagementException
     */
    private String setSync4jUserClear() throws ManagementException {

        String name     = DEFAULT_USER_NAME;
        String password = DEFAULT_USER_PASSWORD;

        Connection con = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;

        boolean userFound = false;

        try {
            //
            // checks if the user already exist
            //
            con = datasource.getConnection();
            pStmt = con.prepareStatement(SQL_SELECT_USER);

            pStmt.setObject(1, name);

            rs = pStmt.executeQuery();

            while (rs.next()) {
                userFound = true;
            }

            if (!userFound) {
                pStmt = con.prepareStatement(SQL_INSERT_USER);
                pStmt.setObject(1, name);
                pStmt.setObject(2, password);

                pStmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw new ManagementException("Error inserting the user", e);
        } finally {
            DBTools.close(con, pStmt, rs);
        }
        return name;
    }


    /**
     * Save the given device
     * @param deviceId String
     * @param digest String
     * @throws ManagementException
     */
    private void setSync4jDevice(String deviceId, String digest) throws ManagementException {

        Connection con = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;

        boolean deviceFound = false;

        try {
            //
            // checks if the device already exist
            //
            con = datasource.getConnection();
            pStmt = con.prepareStatement(SQL_SELECT_DEVICE);

            pStmt.setObject(1, deviceId);

            rs = pStmt.executeQuery();

            while (rs.next()) {
                deviceFound = true;
            }

            if (!deviceFound) {
                pStmt = con.prepareStatement(SQL_INSERT_DEVICE);
                pStmt.setObject(1, deviceId);
                pStmt.setObject(2, DEFAULT_DEVICE_DESCRIPTION);
                pStmt.setObject(3, DEFAULT_DEVICE_TYPE);
                pStmt.setObject(4, digest);
                pStmt.setObject(5, DEFAULT_DEVICE_SERVER_PASSWORD);

                pStmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw new ManagementException("Error inserting the device", e);
        } finally {
            DBTools.close(con, pStmt, rs);
        }
    }

    /**
     * Save the principal with the digest credential
     * @param digest String
     * @param deviceId String
     * @throws ManagementException
     */
    private void setSync4jPrincipalDigest(String digest, String deviceId) throws ManagementException {

        Connection con = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;

        boolean principalFound = false;

        try {
            //
            // checks if the principal already exist
            //
            con = datasource.getConnection();
            pStmt = con.prepareStatement(SQL_SELECT_PRINCIPAL);

            pStmt.setObject(1, digest);
            pStmt.setObject(2, deviceId);

            rs = pStmt.executeQuery();

            while (rs.next()) {
                principalFound = true;
            }

            if (!principalFound) {
                pStmt = con.prepareStatement(SQL_INSERT_PRINCIPAL);
                pStmt.setInt(1, getSync4jPrincipalId());
                pStmt.setObject(2, digest);
                pStmt.setObject(3, deviceId);

                pStmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw new ManagementException("Error inserting the principal", e);
        } finally {
            DBTools.close(con, pStmt, rs);
        }
    }

    /**
     * Save the principal with the digest credential
     * @param username String
     * @param deviceId String
     * @throws ManagementException
     */
    private void setSync4jPrincipal(String username, String deviceId) throws ManagementException {

        Connection con = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;

        boolean principalFound = false;

        try {
            //
            // checks if the principal already exist
            //
            con = datasource.getConnection();
            pStmt = con.prepareStatement(SQL_SELECT_PRINCIPAL);

            pStmt.setObject(1, username);
            pStmt.setObject(2, deviceId);

            rs = pStmt.executeQuery();

            while (rs.next()) {
                principalFound = true;
            }

            if (!principalFound) {
                pStmt = con.prepareStatement(SQL_INSERT_PRINCIPAL);
                pStmt.setInt(1, getSync4jPrincipalId());
                pStmt.setObject(2, username);
                pStmt.setObject(3, deviceId);

                pStmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw new ManagementException("Error inserting the principal", e);
        } finally {
            DBTools.close(con, pStmt, rs);
        }
    }

    /**
     * Returns the key (id) to use to create a new principal
     * @return String
     * @throws ManagementException
     */
    private int getSync4jPrincipalId() throws ManagementException {
        Connection con = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;

        int id = -1;

        try {

            con = datasource.getConnection();
            pStmt = con.prepareStatement(SQL_SELECT_ID_PRINCIPAL);

            pStmt.setObject(1, "principal");

            rs = pStmt.executeQuery();
            while (rs.next()) {
                id = rs.getInt(1);
            }


            pStmt = con.prepareStatement(SQL_UPDATE_ID_PRINCIPAL);
            pStmt.setInt(1, id + 1);
            pStmt.setObject(2, "principal");

            pStmt.executeUpdate();

        } catch (SQLException e) {
            throw new ManagementException("Error reading id for the principal", e);
        } finally {
            DBTools.close(con, pStmt, rs);
        }
        return id;
    }


    private void showMethods(Object obj) {
        System.out.println("Methods of: '" + obj.getClass().getName() + "'");
        Class<? extends Object> cObj = obj.getClass();
        Method[] methods = cObj.getMethods();
        for (int i = 0; i < methods.length; i++) {
            System.out.println(methods[i].toString());
        }
    }

}
