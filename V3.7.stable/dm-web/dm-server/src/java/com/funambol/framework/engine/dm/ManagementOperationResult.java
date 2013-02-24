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


package com.funambol.framework.engine.dm;

import java.util.Map;
import java.util.HashMap;

/**
 * This class represents the result of a single management operation.<br>
 * Note that differently by the Results comand, which can hold multiple results,
 * this object brings only one result.
 *
 *
 *
 * @version $Id: ManagementOperationResult.java,v 1.2 2006/08/07 21:09:20 nichele Exp $
 */
public class ManagementOperationResult {
    // -------------------------------------------------------------- Properties

    /**
     * The status code of the requested operation
     */
    private int statusCode;

    /**
     * Sets statusCode
     *
     * @param statusCode the new statusCode
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Gets statusCode
     *
     * @return the current statusCode value
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * The the requested command. One of "Add", "Copy", "Delete", "Exec",
     * "Get", "Replace", "Sequence", "Atomic"
     */
    private String command;

    /**
     * Sets command
     *
     * @param command the new command
     */
    public void setCommand(String command) {
        this.command = command;
    }

    /**
     * Gets command
     *
     * @return the current command value
     */
    public String getCommand() {
        return command;
    }

    /**
     * The nodes property. Note that it may contain results if the
     * ManagementOperationStatus regards a Get or a set of nodes if it relates
     * to a status of any command who specified items in it.
     */
    private Map<String, Object> nodes;

    /**
     * Returns the nodes property
     *
     * @return the nodes property
     */
    public Map<String, Object> getNodes() {
        return nodes;
    }

    /**
     * Set the nodes property
     *
     * @param newNodes the new values
     */
    public void setNodes(Map<String, Object> newNodes) {
        this.nodes = new HashMap<String, Object>();
        if (newNodes == null) {
            return;
        }

        nodes.putAll(newNodes);
    }

    /**
     * Addes the given nodes to the existing nodes. If no nodes were present,
     * a new hash map is created to store them.
     *
     * @param newNodes the new values
     */
    public void addNodes(Map<String, Object> newNodes) {
        if (nodes == null) {
            nodes = new HashMap<String, Object>();
        }

        nodes.putAll(newNodes);
    }

    // ------------------------------------------------------------ Constructors

    /** Creates a new instance of ManagementOperationResult */
    public ManagementOperationResult() {
        statusCode = -1;
        command = null;
        nodes = new HashMap<String, Object>();
    }

    // ---------------------------------------------------------- Public methods

}
