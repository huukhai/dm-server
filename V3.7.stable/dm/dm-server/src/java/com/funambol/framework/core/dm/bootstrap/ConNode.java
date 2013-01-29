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

package com.funambol.framework.core.dm.bootstrap;

import java.util.Map;

import org.apache.commons.collections15.map.ListOrderedMap;


/**
 * Corresponds to the &lt;Con&gt; interior node in the management tree
 *
 * @version $Id: ConNode.java,v 1.1 2006/11/15 13:43:15 nichele Exp $
 */
public class ConNode implements java.io.Serializable {

    // --------------------------------------------------------------- Constants


    // ------------------------------------------------------------ Private data

    private Map<String, Connection> connections;

    // ------------------------------------------------------------ Constructors
    /** For serialization purposes */
    public ConNode() {
        this.connections = new ListOrderedMap<String, Connection>();
    }

     /**
      * Creates a new ConNode object with the given <code>Connection</code> and the given name
      *
      * @param con the connection node
      * @param connectionName the connection name
      */
     public ConNode(final Connection con, final String connectionName) {
         this();
         this.connections.put(connectionName, con);
     }

     // ---------------------------------------------------------- Public methods

     /**
      * Add new connection
      * @param connection the connection to add
      * @param connectionName the name of the connection to add
      */
     public void addConnection(final Connection connection, final String connectionName) {
         this.connections.put(connectionName, connection);
     }

     /**
      * Gets the connectiont with the given name
      *
      * @param connectionName the name of the connection
      * @return the connection with the given name.<br/><code>null</code> if not exists.
      */
     public Connection getConnection(final String connectionName) {
         return this.connections.get(connectionName);
     }

     /**
      * Gets the connections
      *
      * @return the connections
      */
     public Map<String, Connection> getConnections() {
         return connections;
     }

     /**
      * Sets the connections
      *
      * @param connections the connections
      */

     public void setConnections(Map<String, Connection> connections) {
         this.connections = connections;
     }

 }
