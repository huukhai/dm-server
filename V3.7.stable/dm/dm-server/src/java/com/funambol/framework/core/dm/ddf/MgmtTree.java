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

package com.funambol.framework.core.dm.ddf;

import com.funambol.framework.core.VerDTD;

import java.io.Serializable;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Represents a MgmtTree in DM Tree 1.2. The Management Tree is a hierarchical arrangement 
 * of managed objects in a device, which defines the management view of the device. 
 * 
 * @version $Id: MgmtTree.java,v 1.1 2006/11/15 14:29:17 nichele Exp $
 */
public class MgmtTree implements Serializable {

    // -------------------------------------------------------------- Properties
    private ArrayList<Node> treeNodes = new ArrayList<Node>();
    private VerDTD verDTD;
    private String man;
    private String mod;
    
    // ------------------------------------------------------------- Constructor
    public MgmtTree() {
    }

    /**
     * Creates a new MgmtTree
     * @param treeNodes     the list of children nodes
     * @param verDTD        the DTD version
     * @param man           the manufacter identifier
     * @param mod           the model identifier (manufacter specified string)
     */
    public MgmtTree(Node[] treeNodes,
                    VerDTD verDTD   ,
                    String man      ,
                    String mod      ){
        
        if (treeNodes == null) {
            treeNodes = new Node[0];
        }
        setTreeNodes(treeNodes);
        setVerDTD(verDTD);
        this.man = man;
        this.mod = mod;
    }
    
    // ---------------------------------------------------------- Public Methods

    /**
     * Returns the manufacter identifier
     *
     * @return the man property
     */
    public String getMan() {
        return man;
    }

    /**
     * Set the manufacter identifier
     * 
     * @param man the new manufacter identifier
     */
    public void setMan(String man) {
        this.man = man;
    }

    /**
     * Returns the model identifier
     *
     * @return the mod property
     */
    public String getMod() {
        return mod;
    }

    /**
     * Set the model identifier
     * 
     * @param mod the new model identifier
     */
    public void setMod(String mod) {
        this.mod = mod;
    }

    /**
     * Returns the list of nodes
     *
     * @return the treeNodes list
     */
    public ArrayList<Node> getTreeNodes() {
        return this.treeNodes;
    }
  
    /**
     * Appends all of the nodes in the array to the end of the nodes collection
     * 
     * @param nodes the new nodes
     */
    public void addTreeNodes(Node[] nodes) {
        if (nodes != null) {
            this.treeNodes.addAll(Arrays.asList(nodes));
        }
    }
    
    /**
     * Appends the new node to the end of the nodes collection
     *  
     * @param node the new node
     */
    public void addNode(Node node) {
        this.treeNodes.add(node);
    }

    /**
     * Appends all of the nodes in the arrayList to the end of the nodes collection
     * 
     * @param nodes the collection of new nodes
     * @throws IllegalArgumentException if the arraylist includes not valid objects 
     */
    public void addTreeNodes(ArrayList<Node> nodes) {
        if (nodes == null){
            return;
        }      
        Iterator<Node> i = nodes.iterator();
        while (i.hasNext()){
            if (!(i.next() instanceof Node)) {
                throw new IllegalArgumentException("The nodes in the array list are not valid");
            }
        }
        this.treeNodes.addAll(nodes);
    }

    /**
     * Replaces the nodes collection with the new nodes
     * 
     * @param nodes the collection of new nodes
     * @throws IllegalArgumentException if the arraylist includes not valid objects  
     */
    public void setTreeNodes(ArrayList<Node> nodes) {
        if (nodes != null) {                 
            Iterator<Node> i = nodes.iterator();
            while (i.hasNext()){
                if (!(i.next() instanceof Node)) {
                    throw new IllegalArgumentException("The nodes in the array list are not valid");
                }
            }
            this.treeNodes = nodes;
        } else {
            this.treeNodes.clear();
        }
    }   
    
    /**
     * Replaces the nodes in the collection with the new nodes
     * 
     * @param nodes the new nodes
     */
    public void setTreeNodes(Node[] nodes) {
        this.treeNodes.clear();
        if (nodes != null) {
            this.treeNodes.addAll(Arrays.asList(nodes));
        }
    }

    /**
     * Returns the DTD version
     *
     * @return the verDTD property
     */
    public VerDTD getVerDTD() {
        return verDTD;
    }

    /**
     * Set the DTD version. Specifies the major and minor version identifier of the OMA DM 
     * Description Framework specification used to represent the OMA DM description.
     *
     * @param verDTD the new DTD version
     */    
    public void setVerDTD(VerDTD verDTD) {
        this.verDTD = verDTD;
    }

    /**
     * Returns a String representation of this Management Tree 
     * 
     * @return String
     */
    public String toString() {
        ToStringBuilder sb = new ToStringBuilder(this);
    
        sb.append("treeNodes", treeNodes);
        sb.append("VerDTD", (verDTD == null) ? null : verDTD.getValue());        
        sb.append("man"   , man); 
        sb.append("mod"   , mod);

        return sb.toString();
    }
}
