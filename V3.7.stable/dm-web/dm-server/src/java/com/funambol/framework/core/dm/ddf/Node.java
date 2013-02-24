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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Represents a Node in DM Tree 1.2.
 *
 * @version $Id: Node.java,v 1.1 2006/11/15 14:29:17 nichele Exp $
 */
public class Node implements Serializable {

    // -------------------------------------------------------------- Properties
    private String name;
    private String path;
    private ArrayList<Node> subNodes = new ArrayList<Node>();
    private String value;
    private RTProperties rtProperties;

    // ------------------------------------------------------------- Constructor

    public Node(String name              ,
                String path              ,
                Node[] subNodes          ,
                RTProperties rtProperties,
                String value             ) {

        //
        // It's important to call the setRtProperties before the setName because
        // the method setName checks if the rtProperties is null
        //
        setRtProperties(rtProperties);
        setName(name);
        this.path = path;
        if (subNodes == null) {
            subNodes = new Node[0];
        }
        setValue(value);
        setSubNodes(subNodes);
    }

    public Node(String name              ,
                String path              ,
                Node[] subNodes          ,
                String format            ,
                String value             ) {

        //
        // It's important to set the setRtProperties before to call setName because
        // the method setName checks if the rtProperties is null
        //
        setName(name);
        this.path = path;
        if (subNodes == null) {
            subNodes = new Node[0];
        }
        setValue(value);
        setSubNodes(subNodes);

        setFormat(format);
    }

    /**
     *
     */
    public Node() {
    }

    // ---------------------------------------------------------- Public Methods

    /**
     * Returns the name property
     *
     * @return the name property
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the name property
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the URI up to, but not including, the described node.
     *
     * @return the path property
     */
    public String getPath() {
        return this.path;
    }

    /**
     * Set the path property. Specifies the URI up to, but not including, the described node.
     *
     * @param path the new path
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Returns the arrayList of children nodes
     *
     * @return the subNodes list
     */
    public ArrayList<Node> getSubNodes() {
        return this.subNodes;
    }

    /**
     * Appends the new nodes to the end of the children nodes collection
     *
     * @param subNodes the new children nodes
     * @throws IllegalArgumentException if the arraylist includes not valid objects
     */
    public void addSubNodes(ArrayList<Node> subNodes) {
        if (subNodes == null){
            return;
        }
        checkValue();
        Iterator<Node> i = subNodes.iterator();
        while (i.hasNext()){
            if (!(i.next() instanceof Node)) {
                throw new IllegalArgumentException("The nodes in the array list are not valid");
            }
        }
        this.subNodes.addAll(subNodes);
    }

    /**
     * Appends all of the nodes in the array to the end of the children nodes collection
     *
     * @param subNodes the new children nodes
     */
    public void addSubNodes(Node[] subNodes) {
        if (subNodes != null) {
            checkValue();
            this.subNodes.addAll(Arrays.asList(subNodes));
        }
    }

    /**
     * Appends the new node to the end of the children nodes collection
     *
     * @param node the new node
     */
    public void addNode(Node node) {
        checkValue();
        this.subNodes.add(node);
    }

    /**
     * Replaces the children nodes collection with the new children nodes
     *
     * @param subNodes the collection of new nodes
     * @throws IllegalArgumentException if the arraylist includes not valid objects
     */
    public void setSubNodes(ArrayList<Node> subNodes) {
        if (subNodes != null) {
            checkValue();
            Iterator<Node> i = subNodes.iterator();
            while (i.hasNext()){
                if (!(i.next() instanceof Node)) {
                    throw new IllegalArgumentException("The nodes in the array list are not valid");
                }
            }
            this.subNodes = subNodes;
        } else {
            this.subNodes.clear();
        }
    }

    /**
     * Replaces the children nodes in the collection with the new nodes
     *
     * @param subNodes the new nodes
     */
    public void setSubNodes(Node[] subNodes) {
        this.subNodes.clear();
        if (subNodes != null) {
            if (subNodes.length > 0)
                checkValue();
            this.subNodes.addAll(Arrays.asList(subNodes));
        }
    }

    /**
     * Returns the value property
     *
     * A Node with a not null value must always terminate the recursion:
     * the ArrayList subNodes must be empty
     *
     * @return the value proper
     */
    public String getValue() {
            return this.value;
    }

    /**
     * Set the value property
     *
     * @param value the new value
     * @throws IllegalArgumentException if the value is not null and the node has children.
     * A Node with a not-null value must always terminate the recursion.
     */
    public void setValue(String value) {
        if (value != null && !subNodes.isEmpty()){
            throw new IllegalArgumentException("A Node with a value must always terminate the recursion");
        }
        this.value = value;
    }

    /**
     * Returns the node type
     *
     * @return the value
     */
    public String getType() {
        if (rtProperties == null) {
            return null;
        }
        return rtProperties.getType();
    }

    /**
     * Set the type
     *
     * @param type the new value
     */
    public void setType(String type) {
        if (rtProperties == null) {
            rtProperties = new RTProperties();
        }
        rtProperties.setType(type);
    }

    /**
     * Returns the node size
     *
     * @return the size
     */
    public String getSize() {
        if (rtProperties == null) {
            return null;
        }
        return rtProperties.getSize();
    }

    /**
     * Set the size
     *
     * @param size the new value
     */
    public void setSize(String size) {
        if (rtProperties == null) {
            rtProperties = new RTProperties();
        }
        rtProperties.setSize(size);
    }

    /**
     * Returns the node verNO
     *
     * @return the verNo
     */
    public String getVerNO() {
        if (rtProperties == null) {
            return null;
        }
        return rtProperties.getVerNo();
    }

    /**
     * Set the verNO
     *
     * @param verNO the new value
     */
    public void setVerNO(String verNO) {
        if (rtProperties == null) {
            rtProperties = new RTProperties();
        }
        rtProperties.setVerNo(verNO);
    }

    /**
     * Returns the format of the node
     *
     * @return the format
     */
    public String getFormat() {
        if (rtProperties == null) {
            return null;
        }
        return rtProperties.getFormat();
    }

    /**
     * Set the format
     *
     * @param format the new value
     */
    public void setFormat(String format) {
        if (rtProperties == null) {
            rtProperties = new RTProperties();
        }
        rtProperties.setFormat(format);
    }

    /**
     * Returns the timestamp
     *
     * @return String the timestamp
     */
    public String getTStamp() {
        if (rtProperties == null){
            return null;
        }
        return rtProperties.getTStamp();
    }

    /**
     * Set the timestamp
     *
     * @param tStamp the new timestamp
     */
    public void setTStamp(String tStamp) {
        if (rtProperties == null){
            rtProperties = new RTProperties();
        }
        rtProperties.setTStamp(tStamp);
    }

    /**
     * Returns the acl
     *
     * @return String the acl
     */
    public String getAcl() {
        if (rtProperties == null){
            return null;
        }
        return rtProperties.getAcl();
    }

    /**
     * Set the acl
     *
     * @param acl the new acl
     */
    public void setAcl(String acl) {
        if (rtProperties == null){
            rtProperties = new RTProperties();
        }
        rtProperties.setAcl(acl);
    }




    /**
     * Returns the Run Time Properties that exists at run-time in a device
     *
     * @return the rtProperties property
     */
    public RTProperties getRtProperties() {
        return this.rtProperties;
    }

    /**
     * Set the rtProperties property
     *
     * @param rtProperties the new value
     */
    public void setRtProperties(RTProperties rtProperties) {
        this.rtProperties = rtProperties;
    }

    /**
     * Returns a String representation of this Node
     *
     * @return String
     */
    public String toString() {
        ToStringBuilder sb = new ToStringBuilder(this).append("\r\n");

        sb.append("name"        , name);
        sb.append("value"       , value);
        sb.append("path"        , path);
        sb.append("rtProperties", rtProperties);
        sb.append("subNodes"    , subNodes);

        return sb.toString();
    }

    /**
     * A Node with a value must always terminate the recursion.
     *
     * @throws IllegalArgumentException if the value is not null and the node has children.
     * A Node with a not-null value must always terminate the recursion.
     */
    private void checkValue(){
        if (value != null){
            throw new IllegalArgumentException("A Node with a value must always terminate the recursion");
        }

    }

}
