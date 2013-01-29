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


import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.util.Map;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

import com.funambol.framework.core.AbstractCommand;
import com.funambol.framework.core.Add;
import com.funambol.framework.core.Alert;
import com.funambol.framework.core.Atomic;
import com.funambol.framework.core.CmdID;
import com.funambol.framework.core.ComplexData;
import com.funambol.framework.core.Constants;
import com.funambol.framework.core.Copy;
import com.funambol.framework.core.Data;
import com.funambol.framework.core.Delete;
import com.funambol.framework.core.Exec;
import com.funambol.framework.core.Get;
import com.funambol.framework.core.Item;
import com.funambol.framework.core.ItemizedCommand;
import com.funambol.framework.core.Meta;
import com.funambol.framework.core.Replace;
import com.funambol.framework.core.ResponseCommand;
import com.funambol.framework.core.Results;
import com.funambol.framework.core.Sequence;
import com.funambol.framework.core.Source;
import com.funambol.framework.core.Status;
import com.funambol.framework.core.Sync4jException;
import com.funambol.framework.core.Target;
import com.funambol.framework.core.TargetRef;
import com.funambol.framework.core.dm.ddf.MgmtTree;
import com.funambol.framework.core.dm.ddf.Node;
import com.funambol.framework.core.dm.ddf.RTProperties;
import com.funambol.framework.protocol.ProtocolException;
import com.funambol.framework.tools.Base64;
import com.funambol.framework.tools.CommandIdGenerator;
import com.funambol.framework.tools.WBXMLTools;
import com.funambol.framework.tools.XMLTools;


/**
 *
 * @version $Id: Util.java,v 1.4 2006/11/15 13:49:31 nichele Exp $
 */
public final class Util {

    // --------------------------------------------------------------- Constants

    private static final String MANAGEMENT_COMMANDS
        = ",Add,Alert,Copy,Delete,Exec,Get,Replace,Atomic,Sequence,";

    private static final String ALERT_OPTION_MINDT  = "MINDT" ;
    private static final String ALERT_OPTION_MAXDT  = "MAXDT" ;
    private static final String ALERT_OPTION_DR     = "DR"    ;
    private static final String ALERT_OPTION_MAXLEN = "MAXLEN";
    private static final String ALERT_OPTION_IT     = "IT"    ;
    private static final String ALERT_OPTION_ET     = "ET"    ;


    // --------------------------------------------------- Public static methods

    /**
     * Converts the given array of ManagementOperations in AbstractCommands.
     *
     * @param operations the ManagementOperations to convert
     * @param idGenerator generator for command ids
     *
     * @return the corresponding AbstractCommand array.
     */
    public static AbstractCommand[]
        managementOperations2commands(ManagementOperation[] operations ,
                                      CommandIdGenerator    idGenerator,
                                      String mimeType) {

            if ((operations == null) || (operations.length == 0)) {
                return new AbstractCommand[0];
            }

            AbstractCommand[] ret = new AbstractCommand[operations.length];
            for (int i=0; i<operations.length; ++i) {
                ret[i] = managementOperation2command(
                            operations[i], idGenerator.next(), idGenerator, mimeType
                         );
            }

            return ret;
    }

    /**
     * Converts the given array of ManagementOperations in AbstractCommands.
     *
     * @param o the ManagementOperations to convert
     * @param cmdId id for the new command
     * @param idGenerator the generator for command ids
     *
     * @return the corresponding AbstractCommand array.
     */
    public static AbstractCommand
        managementOperation2command(ManagementOperation o,
                                    CmdID cmdId,
                                    CommandIdGenerator idGenerator,
                                    String mimeType) {

        if (o instanceof AtomicManagementOperation) {
            return new Atomic(
                cmdId,
                false,  // noResp
                null ,
                managementOperations2commands(((AtomicManagementOperation)o).getOperations(),
                                              idGenerator,
                                              mimeType)
                );
        } else if (o instanceof SequenceManagementOperation) {
            return new Sequence(
                cmdId,
                false,  // noResp
                null ,
                managementOperations2commands(((SequenceManagementOperation)o).getOperations(),
                                              idGenerator,
                                              mimeType)
                );
        } else if (o instanceof AddManagementOperation) {
            return new Add(
                cmdId,
                false,  // noResp
                null ,
                null ,
                nodes2Items(((AddManagementOperation)o).getNodes(),
                            true, // useValue
                            false, // includeSourceURI
                            mimeType)
            );
        } else if (o instanceof CopyManagementOperation) {
            return new Copy(
                cmdId,
                false,  // noResp
                null ,
                null ,
                nodes2Items(((CopyManagementOperation)o).getNodes(),
                            false,  // useValue
                            true,   // includeSourceURI
                            mimeType)
            );
        } else if (o instanceof DeleteManagementOperation) {
            return new Delete(
                cmdId,
                false,  // noResp
                false,  // archive
                false,  // soft delete
                null ,
                null ,
                nodes2Items(((DeleteManagementOperation)o).getNodes(),
                            false,  // useValue
                            false,  // includeSourceURI
                            mimeType)
            );
        } else if (o instanceof ExecManagementOperation) {
            return new Exec(
                cmdId,
                false,  // noResp
                null ,
                ((ExecManagementOperation)o).getCorrelator(),
                nodes2Items(((ExecManagementOperation)o).getNodes(),
                            false, // useValue
                            false, // includeSourceURI
                            mimeType)[0]
            );
        } else if (o instanceof GetManagementOperation) {
            return new Get(
                cmdId,
                false,  // noResp
                null ,
                null ,
                null ,
                nodes2Items(((GetManagementOperation)o).getNodes(),
                            false, // useValue
                            false,  // includeSourceUURI
                            mimeType)
            );
        } else if (o instanceof ReplaceManagementOperation) {
            return new Replace(
                cmdId,
                false,  // noResp
                null ,
                null ,
                nodes2Items(((ReplaceManagementOperation)o).getNodes(),
                            true,  // useValue
                            false, // includeSourceURI
                            mimeType)
            );
        } else if (o instanceof UserAlertManagementOperation) {
            UserAlertManagementOperation a = (UserAlertManagementOperation)o;

            return new Alert(
                cmdId,
                false,
                null,
                a.getAlertCode(),
                alert2Items(a)
            );
        }

        return null;
    }

    /**
     * Converts a management nodes map into an array of Item objects.
     * <p/>
     *
     * @param nodes the nodes map to convert
     * @param useValue true for exec and replace management operations; false for get and delete
     *
     * @return the Item[]
     */
    public static Item[] nodes2Items(Map<String, Object> nodes,
                                     boolean useValue,
                                     boolean includeSourceURI,
                                     String mimeType) {
        if ((nodes == null) || nodes.isEmpty()) {
            return new Item[0];
        }

        Item[] items = new Item[nodes.size()];
        Object key = null, value = null;
        int j = 0;
        Iterator<String> i = nodes.keySet().iterator();
        String   sourceURI = null;
        while(i.hasNext()) {
            key    = i.next();
            value  = nodes.get(key);

            if (includeSourceURI) {
                sourceURI = (String)key;
            } else {
                sourceURI = null;
            }

            if (!(value instanceof TreeNode) &&
                !(value instanceof MgmtTree)) {
                // create a dummy TreeNode
                if (includeSourceURI) {
                    //
                    // This happens in a Copy operation
                    //
                    value = new TreeNode((String) value, value);
                } else {
                    value = new TreeNode((String) key, value);
                }
            }

            if (value instanceof TreeNode) {
                items[j] = treeNode2item(sourceURI,
                                         (TreeNode)value,
                                         useValue);
                j++;
            } else if (value instanceof MgmtTree) {
                items[j++] = Util.mgmtTree2item(key.toString(),
                                                (MgmtTree)value,
                                                useValue,
                                                mimeType);
            }
        }
        return items;
    }

    /**
     * Converts a TreeNode in a object Item
     *
     * @param sourceURI the sourceURI to set in the item
     * @param treeNode the tree node
     * @param useValue true for exec and replace management operations; false for get and delete
     *
     * @return the item
     */
    public static Item treeNode2item(String sourceURI, TreeNode treeNode, boolean useValue) {
        Item item               = null;
        ComplexData complexData = null;

        String format = null;
        String key    = null;
        Object value  = null;
        String type   = null;

            format        = treeNode.getFormat();
            key           = treeNode.getName();
            value         = treeNode.getValue();
            type          = treeNode.getType();

            if (format == null) {
                format = TreeNode.FORMAT_DEFAULT_VALUE;
            }

            if (format.equalsIgnoreCase(TreeNode.FORMAT_BINARY)) {
            if (value instanceof byte[]) {
                complexData = new ComplexData(new String(Base64.encode( (byte[])value)));
                } else {
                    // get the byte[] of the string representation of the dataValue
                complexData = new ComplexData(new String(Base64.encode(value.toString().
                        getBytes())));
                }
            } else if (format.equalsIgnoreCase(TreeNode.FORMAT_NODE)) {
            value = "";
            }

            if (useValue && (complexData == null)) {
            complexData = new ComplexData(String.valueOf(value));
            }

        Meta meta = new Meta();
            meta.setFormat(format);
            meta.setType(type);

        item = new Item(
                            new Target(String.valueOf(key)),
                            (sourceURI != null) ? (new Source(sourceURI)) : null,
                            (useValue) ? meta : null       ,
                            (useValue) ? complexData : null,
                            false   // moreData
                        );
        return item;
    }

    /**
     * Converts an array of Status and Results commands into an array of
     * ManagementOperationResult objects.<br>
     * Only the status to the following commands are taken into account:
     *
     * <ul>
     *   <li>Add</li>
     *   <li>Copy</li>
     *   <li>Delete</li>
     *   <li>Exec</li>
     *   <li>Get</li>
     *   <li>Replace</li>
     *   <li>Atomic</li>
     *   <li>Sequence</li>
     * </ul>
     *
     * only if the their status code isn't contained in the given dischargedStatus.
     * <br>The <code>dischargedStatus</code> must be in the following format:<br>
     * <code>status1,status2,status3</code>. <br>This is used because for example the status
     * code 213 isn't a result code (or final code. The processor not must be notified
     * of this status).
     *
     * <p>
     * Note that in the case of a Status for a Get command, as per the specs
     * (OMA-SyncML-DMRepPro-V1_1_2-20030613-A) the following command is the
     * Results. Its content will be stored into the property nodes.
     * <p>
     * In all other cases, nodes will contain an entry for each Status' Item
     * (if there is any).
     *
     * @param commands the array of AbstractCommand to process
     * @param dischargedStatus comma separated list of status code to ignore
     *        (see method description)
     *
     * @return the corresponding ManagementOperationResult[]
     *
     * @throws ProtocolException in case of something wrong with the protocol
     */
    public static ManagementOperationResult[]
    operationResults(AbstractCommand[] commands, String dischargedStatus)
    throws ProtocolException {
        if ((commands == null) || (commands.length==0)) {
            return new ManagementOperationResult[0];
        }

        TreeMap<String, ManagementOperationResult> results = new TreeMap<String, ManagementOperationResult>(new CmdComparator());

        //
        // We first pass throgh all statuses and fill the results map.
        // Then we look for Results
        //

        //
        // NOTE: accordingly with the specs "The optional MsgRef element type
        // specifies the MsgID of the associated SyncML request. If the MsgRef
        // is not present in a Results element type, then the MsgRef value of
        // "1" MUST be assumed."
        //

        //
        // 1. Status processing
        //
        String key = null;
        Status status = null;
        ManagementOperationResult s = null;
        String statusCode = null;
        for(int i=0; i<commands.length; ++i) {
            if (!(commands[i] instanceof Status)) {
                continue;
            }

            status = (Status)commands[i];

            if (MANAGEMENT_COMMANDS.indexOf(status.getCmd()) < 0) {
                continue;
            }

            statusCode = status.getData().getData();
            if (dischargedStatus.indexOf(statusCode) != -1) {
                continue;
            }

            key = status.getMsgRef();
            key = ((key == null) ? "1" : key)
                + '-'
                + status.getCmdRef()
                + '-'
                + status.getStatusCode()
                ;

            s = results.get(key);
            if (s == null) {
                String cmd = status.getCmd();
                s = new ManagementOperationResult();
                s.setStatusCode(status.getStatusCode());
                s.setCommand(cmd);

                if (Alert.COMMAND_NAME.equals(cmd)) {
                    s.setNodes(nodesFromItems(status));
                } else {
                    s.setNodes(nodesFromTargetRefs(status));
                }

                results.put(key, s);
            }
        }

        //
        // 2. Results processing
        //
        Results res = null;
        for(int i=0; i<commands.length; ++i) {
            if (!(commands[i] instanceof Results)) {
                continue;
            }

            res = (Results)commands[i];

            key = res.getMsgRef();
            key = ((key == null) ? "1" : key)
                + '-'
                + res.getCmdRef()
                + "-200"  // we suppose there was a 200 status, otherwise no
                          // Results should be here
                ;

            s = results.get(key);

            if (s == null) {
                //
                // This should not happen!
                //
                throw new ProtocolException( "Results "
                                           + key
                                           + " is without corresponding Status"
                                           );
            }

            s.addNodes(nodesFromItems(res));
        }

        //
        // Converts results into a ManagementOperationResult[]
        //
        ManagementOperationResult[] ret
            = new ManagementOperationResult[results.size()];

        int i = 0;
        Iterator<ManagementOperationResult> iter = results.values().iterator();
        while (iter.hasNext()) {
            ret[i++] = iter.next();
        }

        return ret;
    }

    /**
     * Returns a Map from the Items of a ItemizedCommand. If the given command
     * is not a ItemizedCommand, an empty map is returned.
     *
     * @param cmd the status command
     *
     * @return a Map from the Items of a ItemizedCommand command
     */
    public static Map<String, Object> nodesFromItems(AbstractCommand cmd) {
        HashMap<String, Object> ret = new HashMap<String, Object>();

        if (!(cmd instanceof ItemizedCommand)) {
            return ret;
        }

        String path  = null;
        Object value = null;

        if (((ItemizedCommand)cmd).getItems() == null) {
            return ret;
        }

        int c = 0;
        Item item = null;
        Iterator<Item> i = ((ItemizedCommand)cmd).getItems().iterator();
        String format = null;
        String type   = null;
        Data itemData = null;

        while (i.hasNext()) {

            item  = i.next();
            format = getItemFormat(item);
            type   = item.getMeta() != null ? item.getMeta().getType() : null;

            path = (item.getSource() == null)
                 ? String.valueOf(++c)
                 : item.getSource().getLocURI();

            if (Constants.TNDS_XML_FORMAT.equalsIgnoreCase(format) &&
                Constants.TNDS_XML_TYPE.equalsIgnoreCase(type)) {
                //
                // The client sends a MgmtTree (in xml). We convert it in
                //

                value = convertXMLInMgmtTree(item.getData().getData());

            } else if (Constants.TNDS_WBXML_TYPE.equalsIgnoreCase(type)) {
                //
                // The client sends a MgmtTree in wxml
                //
                value = convertB64WBXMLInMgmtTree(item.getData().getData());

            } else {

            itemData = item.getData();
            if (itemData != null) {
                value = itemData.getData();

                if (format.equalsIgnoreCase(TreeNode.FORMAT_DEFAULT_VALUE)) {
                    value = String.valueOf(value);
                } else if (format.equalsIgnoreCase(TreeNode.FORMAT_BINARY)) {
                    if (value != null && (((String)value).length() > 0)) {
                        value = Base64.decode(((String)value).getBytes());
                    } else {
                        value = new byte[0];
                    }
                } else if (format.equalsIgnoreCase(TreeNode.FORMAT_BOOL)) {
                    value = Boolean.valueOf((String)value);
                } else if (format.equalsIgnoreCase(TreeNode.FORMAT_INT)) {
                    try {
                        value = new Integer( (String)value);
                    } catch (NumberFormatException e) {
                        // if the node not contains a valid integer, adds an exception
                        value = new ManagementException(
                                    "Node with format int not contains a valid integer value (" +
                                    value +
                            ")");
                    }
                }

                if (format.equalsIgnoreCase("node")) {
                    value = new TreeNode(item.getSource().getLocURI(), value, format);
                }
            }
            }

            ret.put(path, (value == null) ? "" : value);
        }

        return ret;
    }

    /**
     * Returns a Map from the TargetRefs of a ResponseCommand. If the given
     * command is not a ResponseCommand, an empty map is returned.
     *
     * @param cmd the status command
     *
     * @return a Map from the TargetRefs of a ResponseCommand command
     */
    public static Map<String, Object> nodesFromTargetRefs(AbstractCommand cmd) {
        HashMap<String, Object> ret = new HashMap<String, Object>();

        if (!(cmd instanceof ResponseCommand)) {
            return ret;
        }

        String node = null;

        if (((ResponseCommand)cmd).getTargetRef() == null) {
            return ret;
        }

        TargetRef ref = null;
        Iterator<TargetRef> i = ((ResponseCommand)cmd).getTargetRef().iterator();
        while (i.hasNext()) {
            ref  = i.next();
            node  = ref.getValue();

            ret.put(node, "");
        }

        return ret;
    }

    /**
     * Returns an array of Item objects to be set into an Alert. The array is
     * at least two length long: the first Item is the option string, the others
     * are the real alerts.
     *
     * @param alert the UserAlertManagementObject instance
     *
     * @return the corresponding Item[] array
     */
    public static Item[] alert2Items(final UserAlertManagementOperation alert) {

        String[] alerts = alert.getAlerts();

        //
        // We have to create one item for the options string, plus one item for
        // each alert messages
        //
        Item[] items = new Item[(alerts == null) ? 1 : alerts.length+1];

        //
        // First of all, creates the option string.
        //
        StringBuffer options = new StringBuffer();

        int i = alert.getMinDisplayTime();
        if (i > 0) {
            options.append(ALERT_OPTION_MINDT).append('=').append(i).append('&');
        }
        i = alert.getMaxDisplayTime();
        if (i > 0) {
            options.append(ALERT_OPTION_MAXDT).append('=').append(i).append('&');
        }

        String s = alert.getDefaultResponse();
        if (s != null) {
            try {
                options.append(ALERT_OPTION_DR).append('=').append(URLEncoder.encode(s, "UTF-8")).append('&');
            } catch (java.io.UnsupportedEncodingException e) {
                options.append(e.getMessage());
            }
        }

        i = alert.getMaxLength();
        if (i > 0) {
            options.append(ALERT_OPTION_MAXLEN).append('=').append(i).append('&');
        }

        char c = alert.getInputType();
        if (c != ' ') {
            options.append(ALERT_OPTION_IT).append('=').append(c).append('&');
        }

        c = alert.getEchoType();
        if (c != ' ') {
            options.append(ALERT_OPTION_ET).append('=').append(c).append('&');
        }
        if (options.length() > 0) {
            options.deleteCharAt(options.length() - 1);
        }

        //
        // Note the here alert options will always terminate with an additional
        // '&' that must be removed.
        //
        items[0] = new Item(null, null, null, new ComplexData(options.toString()), false);

        //
        // Now we create a new Item for each alert message
        //
        for (i=1; i < items.length; ++i) {
            items[i] = new Item(null, null, null, new ComplexData(alerts[i-1]), false);
        }

        return items;
    }

    // --------------------------------------------------------- Private methods

    /**
     * Returns the format of the item. If the format is not specified, returns
     * the <code>ITEM_FORMAT_DEFAULT</code>
     *
     * @param item the item
     * @return the format of the given item
     */
    private static String getItemFormat(Item item) {
        Meta meta = item.getMeta();
        if (meta == null) {
            return TreeNode.FORMAT_DEFAULT_VALUE;
        }
        String format = meta.getFormat();
        if (format == null) {
            format = TreeNode.FORMAT_DEFAULT_VALUE;
        }

        return format;
    }

    // --------------------------------------------------------- Private classes

    /**
     * This class compares two strings in the form of keys used by the
     * <i>operationResults</i> methods. They are formatted as follows:<br>
     * <i>{msgref}-{cmdref}-{status}</i><br>
     * The comparison must be done on the <i>{msgref}</i> and <i>{cmdref}</i>,
     * considering them a number.
     */
    private static class CmdComparator
    implements Comparator {
        /**
         * @see java.util.Comparator
         *
         * @param o1 first argument
         * @param o2 second argument
         *
         * @return a negative integer, zero, or a positive integer as the first
         * argument is less than, equal to, or greater than the second.
         */
        public int compare(Object o1, Object o2) {
            String value1 = null;
            String value2 = null;

            if (!((o1 instanceof String) && (o2 instanceof String))) {
                throw new IllegalArgumentException( "o1 ("
                                                  + o1
                                                  + ") and o2 ("
                                                  + o2
                                                  + ") must be string!"
                                                  );
            }

            value1 = (String)o1;
            value2 = (String)o2;

            int msgId1 = extractMsgId(value1);
            int msgId2 = extractMsgId(value2);

            if (msgId1 == msgId2) {
                int cmdId1 = extractCmdId(value1);
                int cmdId2 = extractCmdId(value2);
                return cmdId1 - cmdId2;
            }
            return msgId1 - msgId2;
        }

        /**
         * Given a string in the form of <i>{msgref}-{cmdref}-{status}</i>,
         * this method extracts the {msgref} and returns it as an int.
         * If the string is unparsable, IllegalArgumentException is thrown.
         *
         * @param s the string to be processed
         *
         * @return the msgref as int
         *
         * @throws IllegalArgumentException if key is not in the form <i>{msgref}-{cmdref}-{status}</i>
         */
        private int extractMsgId(final String key)
        throws IllegalArgumentException {

            if (key == null) {
                throw new IllegalArgumentException("key cannot be null");
            }

            int p1 = key.indexOf('-');
            if ((p1 <= 0) || (p1 == key.length()-1)) {
                throw new IllegalArgumentException("key is not in the form {msgref}-{cmdref}-{status}");
            }

            int p2 = key.indexOf('-', p1+1);

            if (p2 <= 0) {
                throw new IllegalArgumentException("key is not in the form {msgref}-{cmdref}-{status}");
            }

            try {
                return Integer.parseInt(key.substring(0, p1));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("key is not in the form {msgref}-{cmdref}-{status}");
            }
        }

        /**
         * Given a string in the form of <i>{msgref}-{cmdref}-{status}</i>,
         * this method extracts the {cmdref} and returns it as an int.
         * If the string is unparsable, IllegalArgumentException is thrown.
         *
         * @param key the string to be processed
         *
         * @return the cmdref as int
         *
         * @throws IllegalArgumentException if key is not in the form <i>{msgref}-{cmdref}-{status}</i>
         */
        private int extractCmdId(final String key)
        throws IllegalArgumentException {

            if (key == null) {
                throw new IllegalArgumentException("key cannot be null");
            }

            int p1 = key.indexOf('-');
            if ((p1 <= 0) || (p1 == key.length()-1)) {
                throw new IllegalArgumentException("key is not in the form {msgref}-{cmdref}-{status}");
            }

            int p2 = key.indexOf('-', p1+1);

            if (p2 <= 0) {
                throw new IllegalArgumentException("key is not in the form {msgref}-{cmdref}-{status}");
            }

            try {
                return Integer.parseInt(key.substring(p1+1, p2));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("key is not in the form {msgref}-{cmdref}-{status}");
            }
        }
    }

    /**
     * Converts a management tree MgmtTree in an object Item
     *
     * @param path the path
     * @param mgmtTree the management tree
     * @param useValue true for exec and replace management operations; false for get and delete
     *
     * @return the item
     */
    public static Item mgmtTree2item(String   path,
                                     MgmtTree mgmtTree,
                                     boolean  useValue,
                                     String   mimeType) {

        Item item               = null;
        ComplexData complexData = null;

        if (Constants.MIMETYPE_SYNCMLDM_XML.equalsIgnoreCase(mimeType)) {
            StringBuffer data = new StringBuffer();
            data.append("<![CDATA[");

            String xml = com.funambol.framework.core.Util.toXML(mgmtTree);
            xml = xml.replaceAll("<MgmtTree>", "<MgmtTree xmlns='syncml:dmddf1.2'>");

            byte[] wbxml = null;

            try {
                wbxml = WBXMLTools.toWBXML(xml, Constants.DTD_1_2.getValue());
            } catch (Sync4jException ex) {
                ex.printStackTrace();
            }
            String b64 = new String(Base64.encode(wbxml));

            xml = replaceTagsInMgmtTree(xml);

            data.append(xml);

            /*
             String xml = null;

                     try {
                xml = com.funambol.framework.tools.IOTools.readFileString(
                        "mgmtnode.xml");
                     } catch (IOException ex) {
                     }
                     data.append(xml);
             */

            data.append("]]>");

        if (useValue && (complexData == null)) {
            complexData = new ComplexData();
                complexData.setData(data.toString());
            }
        } else if (Constants.MIMETYPE_SYNCMLDM_WBXML.equalsIgnoreCase(mimeType)) {

            complexData = new ComplexData();
            complexData.setMgmtTree(mgmtTree);
        }

        Meta meta = new Meta();

        if (Constants.MIMETYPE_SYNCMLDM_XML.equalsIgnoreCase(mimeType)) {
            meta.setFormat(Constants.TNDS_XML_FORMAT);
            meta.setType(Constants.TNDS_XML_TYPE);
        } else if (Constants.MIMETYPE_SYNCMLDM_WBXML.equalsIgnoreCase(mimeType)) {
            meta.setFormat(Constants.TNDS_WBXML_FORMAT);
            meta.setType(Constants.TNDS_WBXML_TYPE);
        }

/*
        if (Constants.MIMETYPE_SYNCMLDM_XML.equalsIgnoreCase(mimeType)) {
            meta.setFormat("b64");
            meta.setType(Constants.TNDS_WBXML_TYPE);
        } else if (Constants.MIMETYPE_SYNCMLDM_WBXML.equalsIgnoreCase(mimeType)) {
            //meta.setFormat("bin");
            //meta.setType(Constants.TNDS_WBXML_TYPE);
            meta.setFormat(Constants.TNDS_XML_FORMAT);
            meta.setType(Constants.TNDS_XML_TYPE);

        }
*/

        item = new Item(
                   new Target(String.valueOf(path)),
                   null                           ,
                   (useValue) ? meta : null       ,
                   (useValue) ? complexData : null,
                   false   // moreData
               );
        return item;
    }

    /**
     * Merge two arrayList of Nodes and return a new arrayList. Nodes from the second arrayList update
     * the correspondent nodes in the first arrayList. New nodes from the second arrayList are added to
     * the first arrayList. Note that mergeSubNodes(a,b) != mergeSubNodes(b,a).
     *
     * @param firstNodes the first arrayList of Nodes
     * @param secondNodes the second arrayList of Nodes
     *
     * @return List the merged arrayList of Nodes
     */
    public static List<Node> mergeSubNodes(ArrayList<Node> firstNodes, ArrayList<Node> secondNodes) {

        if (firstNodes == null)
            return secondNodes;

        if (secondNodes == null)
            return firstNodes;

        List<Node> newNodes = new ArrayList<Node>();
        Iterator<Node> i = firstNodes.iterator();
        Iterator<Node> j = secondNodes.iterator();

        while (j.hasNext()){

            Node secondNode = j.next();
            boolean nodeNotFound = true;
            String secondName = secondNode.getName();
            String secondType = secondNode.getRtProperties().getType();
            //
            // Check if a node with the same name is already there.
            //
            while (i.hasNext() && nodeNotFound){

                Node firstNode = i.next();
                String firstName = firstNode.getName();
                String firstType = firstNode.getRtProperties().getType();

                if (firstType != null && firstType.equals(secondType)){

                    if ((firstName == null && secondName == null) || firstName.equals(secondName)){
                        //
                        // Update the properties of the node from the FIRST arrayList and merge the subnodes.
                        //
                        nodeNotFound = false;
                        firstNode.setSubNodes((ArrayList<Node>)mergeSubNodes(firstNode.getSubNodes(), secondNode.getSubNodes()));
                        firstNode.setValue(secondNode.getValue());
                        firstNode.setPath(secondNode.getPath());
                        firstNode.setRtProperties(secondNode.getRtProperties());

                    }
                }

            }
            //
            // The node from the SECOND arrayList has to be added.
            //
            if (nodeNotFound){
                newNodes.add(secondNode);
            }
        }
        //
        // Add the new nodes from the second arrayList to the first one and return it
        //
        firstNodes.addAll(newNodes);

        return firstNodes;
    }

    /**
     * Converts a com.funambol.framework.engine.dm.TreeNode to
     * a com.funambol.framework.engine.dm.Node
     *
     * @param tn the TreeNode to convert
     *
     * @return Node the new Node
     */
    public static Node convertTreeNodeToNode(TreeNode tn) {

        if (tn == null){
            return null;
        }

        Node node = new Node();
        RTProperties rtp = new RTProperties();

        // old name: the URI
        String tnName = tn.getName();
        // new name: the name of the described node
        String name;
        // new path: the URI up to, but not including, the described node
        String path;

        if (!tnName.startsWith("./")){
            throw new IllegalArgumentException("Node name '" + tnName + "' is not valid");
        }

        int i = tnName.lastIndexOf("/");

        if (tnName.length() > i + 1){
            name = tnName.substring(i + 1);
            path = tnName.substring(0, i);

            // If path is the root, path is ./ not .
            if (path.endsWith(".")){
                path += "/";
            }
        }
        else{
            // If URI is root directory path is null
            name = tnName;
            path = null;
        }

        node.setName(name);
        node.setPath(path);

        if (tn.getValue() != null){
            node.setValue(tn.getValue().toString());
        }

        rtp.setFormat(tn.getFormat());
        node.setRtProperties(rtp);

        return node;
    }

    private static MgmtTree convertXMLInMgmtTree(String xml) {
        MgmtTree tree = null;
        if (xml == null) {
            return null;
        }
        int indexOfSyncML = xml.indexOf("<SyncML");
        StringBuffer newData = new StringBuffer(xml.substring(0, indexOfSyncML));
        newData.append("<Data>");
        newData.append(xml.substring(indexOfSyncML));
        newData.append("</Data>");
        String xmlMgmtTree = XMLTools.replaceAmp(newData.toString());

        try {
            IBindingFactory f = BindingDirectory.getFactory(MgmtTree.class);
            IUnmarshallingContext unm = f.createUnmarshallingContext();

            Object value = unm.unmarshalDocument(
                new ByteArrayInputStream(xmlMgmtTree.getBytes()), "UTF-8"
            );

            tree = ((ComplexData)value).getMgmtTree();
        } catch (JiBXException ex) {
            ex.printStackTrace();
        }
        return tree;
    }


    private static MgmtTree convertB64WBXMLInMgmtTree(String data) {
        MgmtTree tree = null;
        if (data == null) {
            return null;
        }
        byte[] wbxml = Base64.decode(data);
        String xml = null;

        try {
            xml = WBXMLTools.wbxmlToXml(wbxml);
            IBindingFactory f = BindingDirectory.getFactory(MgmtTree.class);
            IUnmarshallingContext unm = f.createUnmarshallingContext();

            Object value = unm.unmarshalDocument(
                new ByteArrayInputStream(xml.getBytes()), "UTF-8"
            );

            tree = ((MgmtTree)value);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return tree;
    }


    /**
     * Replaces some tags in a tnds
     * @param data String
     * @return String
     */
    private static String replaceTagsInMgmtTree(String data) {
        if (data == null) {
            return null;
        }
        data = data.replaceAll("<node></node>", "<node/>");
        data = data.replaceAll("<chr></chr>", "<chr/>");
        data = data.replaceAll("<b64></b64>", "<b64/>");
        data = data.replaceAll("<bool></bool>", "<bool/>");
        data = data.replaceAll("<float></float>", "<float/>");
        data = data.replaceAll("<int></int>", "<int/>");
        data = data.replaceAll("<null></null>", "<null/>");
        data = data.replaceAll("<xml></xml>", "<xml/>");
        data = data.replaceAll("<date></date>", "<date/>");
        data = data.replaceAll("<time></time>", "<time/>");

        return data;
    }

    public static void main(String[] args) throws Exception {
        String b64 = "AgAAahotLy9PTUEvL0RURC1ETS1EREYgMS4yLy9FTkQAYGRmA0Z1bmFtYm9sQWNjAAFsAy4AAW9cJQF1A29yZy5vcGVubW9iaWxlYWxsaWFuY2UvMS4wL3c3AAEBZGYDQXBwSUQAAXYDdzcAAW9cCwF1A3RleHQvcGxhaW4AAQEBZGYDU2VydmVySUQAAXYDRnVuYW1ib2xJRAABb1wLAXUDdGV4dC9wbGFpbgABAQFkZgNBcHBBZGRyAAFvXCUBdQN0ZXh0L3BsYWluAAEBZGYDRnVuYW1ib2xBZGRyAAFvXCUBdQN0ZXh0L3BsYWluAAEBZGYDQWRkclR5cGUAAXYDVVJJAAFvXAsBdQN0ZXh0L3BsYWluAAEBAWRmA1BvcnQAAW9cJQF1A3RleHQvcGxhaW4AAQFkZgNGdW5hbWJvbFBvcnQAAW9cJQF1A3RleHQvcGxhaW4AAQFkZgNQb3J0TmJyAAF2AzgwODAAAW9cCwF1A3RleHQvcGxhaW4AAQEBAQEBAWRmA0FwcEF1dGgAAW9cJQF1A3RleHQvcGxhaW4AAQFkZgNmdW5hbWJvbFNlcnZlcgABb1wlAXUDdGV4dC9wbGFpbgABAWRmA0FBdXRoVHlwZQABdgNCQVNJQwABb1wLAXUDdGV4dC9wbGFpbgABAQFkZgNBQXV0aFNlY3JldAABdgNzcnZwd2QAAW9cCwF1A3RleHQvcGxhaW4AAQEBZGYDQUF1dGhMZXZlbAABdgNTUlZDUkVEAAFvXAsBdQN0ZXh0L3BsYWluAAEBAWRmA0FBdXRoTmFtZQABdgNmdW5hbWJvbAABb1wLAXUDdGV4dC9wbGFpbgABAQEBZGYDZnVuYW1ib2xDbGllbnQAAW9cJQF1A3RleHQvcGxhaW4AAQFkZgNBQXV0aFR5cGUAAXYDQkFTSUMAAW9cCwF1A3RleHQvcGxhaW4AAQEBZGYDQUF1dGhTZWNyZXQAAXYDZnVuYW1ib2wAAW9cCwF1A3RleHQvcGxhaW4AAQEBZGYDQUF1dGhMZXZlbAABdgNDTENSRUQAAW9cCwF1A3RleHQvcGxhaW4AAQEBZGYDQUF1dGhOYW1lAAF2A2Z1bmFtYm9sAAFvXAsBdQN0ZXh0L3BsYWluAAEBAQEBAXcDMS4yAAEBAQ==";
        byte[] wbxml = Base64.decode(b64);
        com.funambol.framework.tools.IOTools.writeFile(wbxml, "c:\\mgmtTree.bin");
    }
}
