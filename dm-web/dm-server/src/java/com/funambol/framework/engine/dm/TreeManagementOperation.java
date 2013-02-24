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


import java.util.LinkedHashMap;
import java.util.Map;

import com.funambol.framework.core.dm.ddf.MgmtTree;

/**
 * Base class for tree manipulation management commands such as Add, Replace,
 * Delete and so on.
 * <p>
 * These operations affect on or mode management nodes that in this simple
 * implementation are represented in a Map {key-value}. The key is the path
 * in the configuration tree.
 *
 *
 *
 * @version $Id: TreeManagementOperation.java,v 1.4 2006/11/15 13:46:49 nichele Exp $
 */
public abstract class TreeManagementOperation
extends ManagementOperation
implements java.io.Serializable {

    // ------------------------------------------------------------ Private data

    private Map<String, Object> nodes;

    // ------------------------------------------------------------ constructors

    protected TreeManagementOperation() {
        nodes = new LinkedHashMap<String, Object>();
    }

    // ---------------------------------------------------------- Public methods

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
    public void setNodes(Map newNodes) {
        nodes = new LinkedHashMap<String, Object>();
        if (newNodes == null) {
            return;
        }

        nodes.putAll(newNodes);
    }

    /**
     * Add the given node
     *
     * @param node the node to add
     */
    public void addNode(TreeNode node) {
        if (node == null) {
            return;
        }

        if (this.nodes == null) {
            this.nodes = new LinkedHashMap<String, Object>();
        }
        this.nodes.put(node.getName(), node);
    }
    
    /**
     * Add the given Management Tree
     *
     * @param path the mgmtTree path
     * @param mgmtTree the mgmtTree to add
     */
    public void addMgmtTree(String path, MgmtTree mgmtTree) {
        if (mgmtTree == null) {
            return;
        }

        if (this.nodes == null) {
            this.nodes = new LinkedHashMap<String, Object>();
        }
        this.nodes.put(path, mgmtTree);
    }    
}