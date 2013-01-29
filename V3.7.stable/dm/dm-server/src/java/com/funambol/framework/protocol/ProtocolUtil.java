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

package com.funambol.framework.protocol;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Arrays;
import java.util.List;

import com.funambol.framework.core.AbstractCommand;
import com.funambol.framework.core.Alert;
import com.funambol.framework.core.AlertCode;
import com.funambol.framework.core.Anchor;
import com.funambol.framework.core.Atomic;
import com.funambol.framework.core.Chal;
import com.funambol.framework.core.CmdID;
import com.funambol.framework.core.Cred;
import com.funambol.framework.core.Item;
import com.funambol.framework.core.ItemizedCommand;
import com.funambol.framework.core.Meta;
import com.funambol.framework.core.NextNonce;
import com.funambol.framework.core.Replace;
import com.funambol.framework.core.ResponseCommand;
import com.funambol.framework.core.Sequence;
import com.funambol.framework.core.Source;
import com.funambol.framework.core.SourceRef;
import com.funambol.framework.core.Status;
import com.funambol.framework.core.Sync;
import com.funambol.framework.core.SyncBody;
import com.funambol.framework.core.SyncML;
import com.funambol.framework.core.Target;
import com.funambol.framework.core.TargetRef;
import com.funambol.framework.core.dm.ddf.DevInfo;
import com.funambol.framework.database.Database;
import com.funambol.framework.tools.MD5;

/**
 *
 *
 *
 * @version $Id: ProtocolUtil.java,v 1.5 2007-06-19 08:16:12 luigiafassina Exp $
 *
 */
public class ProtocolUtil {

    /** Filters the given commands based on the given class
     *
     * @param commands commands to be filtered
     * @param filterClass selector
     *
     * @return a java.util.List containing the selected commands
     *
     */
    public static ArrayList<AbstractCommand> filterCommands(AbstractCommand[] commands, Class filterClass) {
        ArrayList<AbstractCommand> filteredCommands = new ArrayList<AbstractCommand>();

        for (int i=0; i<commands.length; ++i) {
            if (filterClass.isInstance(commands[i])) {
                filteredCommands.add(commands[i]);
            }
        }

        return filteredCommands;
    }

    /**
     * Filters the given commands based on the given command id. A command is
     * selected if it a ResponseCommand (only ResponseCommand subclasses have
     * got the reference command id) and its command id ref matches <i>cmdId</i>.
     *
     * @param commands commands to be filtered
     * @param cmdId selector
     *
     * @return a java.util.List containing the selected commands
     *
     */
    public static ArrayList<AbstractCommand> filterCommands(AbstractCommand[] commands,
                                           CmdID cmdId               ) {
        ArrayList<AbstractCommand> filteredCommands = new ArrayList<AbstractCommand>();

        for (int i=0; i<commands.length; ++i) {
            if (  (commands[i] instanceof ResponseCommand)
               && ((((ResponseCommand)commands[i]).getCmdID()).equals(cmdId))) {
                filteredCommands.add(commands[i]);
            }
        }

        return filteredCommands;
    }

    /**
     * Filters the given commands based on the given command type and command id.
     * It combines <i>filterCommands(commands, filterClass)</i> and
     * <i>filterCommands(commands, cmdId)</i> returning only the commands
     * that respect both requirements.
     *
     * @param commands commands to be filtered
     * @param filterClass class type selector
     * @param cmdId selector
     *
     * @return a java.util.List containing the selected commands
     *
     */
    public static ArrayList<AbstractCommand> filterCommands(AbstractCommand[] commands   ,
                                           Class             filterClass,
                                           CmdID cmdId                  ) {
        //
        // Since filtering on command identifier seems more selective,
        // filterCommands(commands, cmdId) is called first and than
        // filterCommands(..., filterClass) is called with the returned values.
        //
        ArrayList<AbstractCommand> list = filterCommands(commands, cmdId);
        int size = list.size();
        AbstractCommand [] aCommands = new AbstractCommand[size];
        for (int i=0; i < size; i++) {
            aCommands[i] = list.get(i);
        }
        return filterCommands(
                    /* not compatible with j2me
                    (AbstractCommand[])list.toArray(new AbstractCommand[0]),
                    */
                    aCommands,
                   filterClass
               );
    }

    /**
     * Filters the given commands based on the given command type and command name.
     * It combines <i>filterCommands(commands, filterClass)</i> and
     * <i>filterCommands(commands, cmdId)</i> returning only the commands
     * that respect both requirements.
     *
     * @param commands commands to be filtered
     * @param filterClass class type selector
     * @param cmd the command name selector
     *
     * @return a java.util.List containing the selected commands
     *
     */
    public static Status filterStatus(AbstractCommand[] commands,
                                      Class    filterClass      ,
                                      String   cmd              ) {

        ArrayList<AbstractCommand> allStatus = filterCommands(commands, filterClass);
        for (int i=0; allStatus != null && i<allStatus.size(); ++i) {
            if (((Status)allStatus.get(i)).getCmd().equals(cmd)) {
                return (Status)allStatus.get(i);
            }
        }
        return null;
    }

    /**
     * Search a alert command in the given <code>SyncBody</code> with the code
     * equals to the give code.
     *
     * @param syncBody the SyncBody
     * @param code int the alert code searched
     * @return the Alert command with the given code or <code>null</code> if the alert
     * command isn't found
     */
    public static Alert searchAlertCommand(SyncBody syncBody, int code) {
        AbstractCommand[] commands = (AbstractCommand[])syncBody.getCommands().toArray(new AbstractCommand[0]);
        ArrayList<AbstractCommand> alertList = filterCommands(commands, Alert.class);
        Iterator<AbstractCommand> i = alertList.iterator();
        Alert alertCommand = null;
        while (i.hasNext()) {
            alertCommand = (Alert)i.next();
            if (code == alertCommand.getData()) {
                return alertCommand;
            }
        }
        return null;
    }

    /**
     * Search all the alert commands in the given <code>SyncBody</code> with the code
     * equals to the give code.
     *
     * @param syncBody the SyncBody
     * @param code int the alert code searched
     * @return the Alert commands with the given code or <code>null</code> if no alert
     * command is found
     */
    public static Alert[] searchAlertCommands(SyncBody syncBody, int code) {
        AbstractCommand[] commands = (AbstractCommand[])syncBody.getCommands().toArray(new AbstractCommand[0]);
        ArrayList<AbstractCommand> alertList = filterCommands(commands, Alert.class);
        Iterator<AbstractCommand> i = alertList.iterator();
        
        List<Alert> alerts = new ArrayList<Alert>();
        Alert alertCommand = null;
        
        while (i.hasNext()) {
            alertCommand = (Alert)i.next();
            if (code == alertCommand.getData()) {
                alerts.add(alertCommand);
            }
        }
        return alerts.toArray(new Alert[0]);
    }

    /**
     * Search all the generic alert commands in the given <code>SyncBody</code>
     *
     * @param syncBody the SyncBody
     * 
     * @return the generic alert commands with the given code or <code>null</code> if no 
     * generic alert command is found
     */
    public static Alert[] searchGenericAlertCommands(SyncBody syncBody) {
        return searchAlertCommands(syncBody, AlertCode.GENERIC_ALERT);
    }
    
    
    /**
     * Creates and returns and AlertCommand for the synchronization of the
     * given database.
     *
     * @param id the command id - NULL
     * @param noResponse
     * @param credential - NULL
     * @param db the database to be synchronized - NOT NULL
     *
     * @return the AlertCommand object
     */
    public static Alert createAlertCommand(CmdID    id        ,
                                           boolean  noResponse,
                                           Cred     credential,
                                           Database db        ) {
        Item[] items = new Item[1];

        Anchor serverAnchor = db.getServerAnchor();

        Meta meta = new Meta();
        meta.setAnchor(serverAnchor);
        items[0] = new Item(db.getTarget(),
                            db.getSource(),
                            meta          ,
                            null          ,  //data
                            false         ); //MoreData

        return new Alert(
                   id            ,
                   noResponse    ,
                   credential    ,
                   db.getMethod(),
                   items
               );
    }

    /**
     * Translates a Target object to a Source object
     *
     * @param target the target object - NULL
     *
     * @return a Source object with the same URI and local name of <i>target</i>
     */
    public static Source target2Source(Target target) {
        if (target == null) return null;

        return new Source(target.getLocURI(), target.getLocName());
    }

    /**
     * Translates a Source object to a Target object
     *
     * @param source the source object - NULL
     *
     * @return a Target object with the same URI and local name of <i>source</i>
     */
    public static Target source2Target(Source source) {
        if (source == null) return null;

        return new Target(source.getLocURI(), source.getLocName());
    }

    /**
     * Extracts the target and source refs from an array of items
     *
     * @param items the items to inspect. If null targetRefs and sourceRefs
     *              remain unchanged
     * @param targetRefs a reference to an array that will contain the references
     *                   to the items' targets
     * @param sourceRefs a reference to an array that will contain the references
     *                   to the items' sources
     *
     */
    public static void extractRefs(Item[] items          ,
                                   TargetRef[] targetRefs,
                                   SourceRef[] sourceRefs) {
        if (items == null) {
            return;
        }

        Target t = null;
        Source s = null;
        for (int i=0; i<items.length; ++i) {
            t = items[i].getTarget();
            s = items[i].getSource();

            targetRefs[i] = (t != null) ? new TargetRef(t) : null;
            sourceRefs[i] = (s != null) ? new SourceRef(s) : null;
        }
    }

    /**
     * Filters a list of commands extracting the ones of the given types.
     *
     * @param commands the list of command to be filtered
     * @param types the command types to extract
     *
     * @return an array of the selected commmands
     */
    public static List<AbstractCommand> filterCommands(List<AbstractCommand> commands, String[] types) {
        StringBuffer sb = new StringBuffer(",");

        for (int i = 0; ((types != null) && (i < types.length)); ++i) {
            sb.append(types[i]).append(',');
        }

        ArrayList<AbstractCommand> selectedCommands = new ArrayList<AbstractCommand>();
        AbstractCommand command = null;
        Iterator<AbstractCommand> i = commands.iterator();
        while (i.hasNext()) {
            command = i.next();

            if (sb.indexOf(',' + command.getName() + ',') >= 0) {
                selectedCommands.add(command);
            }
        }

        return selectedCommands;
    }

    /**
     * Checks if a message require a response from the client.<p>
     * A message requires a response if its body contains commands other than
     * status commands.
     *
     * @param msg the message to check - NOT NULL and properly constructed
     *
     * @return true if the message requires a response, false otherwise
     *
     */
    public static boolean noMoreResponse(SyncML msg) {
        AbstractCommand[] commands =
        (AbstractCommand[])msg.getSyncBody().getCommands().toArray(
        new AbstractCommand[0]);

        for(int i=0; ((commands != null) && (i<commands.length)); ++i) {
            if (!Status.COMMAND_NAME.equals(commands[i].getName())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Creates and returns a DevInfo object from a Replace command of found in
     * SyncML DM PKG #1.
     *
     * @param cmd the Replace command
     *
     * @return the creted corresponding DevInfo object
     *
     * @throws ProtocolException in case cmd does not contain DevInfo data.
     */
    public static DevInfo devInfoFromReplace(Replace cmd)
    throws ProtocolException {
        Item item = null;
        ArrayList<Item> items = cmd.getItems();

        if (items.isEmpty()) {
            throw new ProtocolException("No Item found in Replace");
        }

        String devId = null,
               man   = null,
               mod   = null,
               dmV   = null,
               lang  = null;

        String uri = null;
        Iterator<Item> i = items.iterator();
        while(i.hasNext()) {
            item = i.next();
            try {
                uri = item.getSource().getLocURI();
                if (uri == null) {
                    throw new NullPointerException();
                }
            } catch (NullPointerException e) {
                // something in the calling chain is null
                throw new ProtocolException("Some of the items are not in the right format.");
            }
            if (uri.endsWith(DevInfo.DEVINFO_DEV_ID)) {
                devId = item.getData().getData();
            } else if (uri.endsWith(DevInfo.DEVINFO_MAN)) {
                man = item.getData().getData();
            } else if (uri.endsWith(DevInfo.DEVINFO_MOD)) {
                mod = item.getData().getData();
            } else if (uri.endsWith(DevInfo.DEVINFO_DMV)) {
                dmV = item.getData().getData();
            } else if (uri.endsWith(DevInfo.DEVINFO_LANG)) {
                lang = item.getData().getData();
            } else {
                // value not recognized or supported: just ignored
            }
        }

        return new DevInfo(devId, man, mod, dmV, lang);
    }

    /**
     * Generate the next nonce for MD5 authentication
     * The nextNonce for the session has a number format;
     * the nextNonce for the chal element is encoding b64
     *
     * @return NextNonce a new NextNonce object
     */
    public static NextNonce generateNextNonce() {
        return new NextNonce(MD5.getNextNonce());
    }

    /**
     * Returns the header status code of the given message, if specified.<br<
     * The header status code is the first status in the message body. The
     * first message of PCK1 has not any header status code. In this case -1 is
     * returned.
     *
     * @param msg the SyncML message object
     *
     * @return the header status code or -1 if the given message does not
     *         containno any header status command.
     */
    public static int getHeaderStatusCode(SyncML msg) {
        ArrayList<AbstractCommand> cmdList = msg.getSyncBody().getCommands();

        cmdList = filterCommands(
                      cmdList.toArray(new AbstractCommand[cmdList.size()]),
                      Status.class,
                      new CmdID("1")
                  );

        if (cmdList.isEmpty()) {
            return -1;
        }

        return ((Status)cmdList.get(0)).getStatusCode();
    }

    /**
     * Returns the Chal element included in the header status if there is any or
     * null if no chal is given.
     *
     * @param msg the SyncML message object
     *
     * @return Chal element included in the header status if there is any or
     *         null if no chal is given.
     */
    public static Chal getStatusChal(SyncML msg) {
        ArrayList<AbstractCommand> cmdList = msg.getSyncBody().getCommands();

        cmdList = filterCommands(
                      cmdList.toArray(new AbstractCommand[cmdList.size()]),
                      Status.class,
                      new CmdID("1")
                  );

        if (cmdList.isEmpty()) {
            return null;
        }

        return ((Status)cmdList.get(0)).getChal();
    }

    /**
     * Sort an array of Status object in according to their cmdRef
     *
     * @param statusToSort an array of Status object
     *
     * @return an array of sorted Status object
     */
    public static Object[] sortStatusCommand(Object[] statusToSort) {
        StatusComparator comparator = new StatusComparator();
        Arrays.sort(statusToSort, comparator);
        return statusToSort;
    }

    /**
     * Sort an array of AbstractCommand object in according to their cmdID
     *
     * @param cmdToSort an array of AbstractCommand object
     *
     * @return an array of sorted AbstractCommand object
     */
    public static Object[] sortAbstractCommand(Object[] cmdToSort) {
        AbstractCmdComparator comparator = new AbstractCmdComparator();
        Arrays.sort(cmdToSort, comparator);
        return cmdToSort;
    }

    /**
     * Returns item that contains large object (moreData == true).
     * If there is it,
     *
     * @param commands AbstractCommand[]
     * @return item that contains large object or null if
     *         there aren't items with moreData == true
     */
    public static Item getLargeObject(AbstractCommand[] commands) {
        int num = commands.length;
        AbstractCommand command = null;
        ArrayList<Item> items = null;
        Iterator<Item> iItems = null;
        Item item = null;
        for (int i=0; i<num; i++) {
            command = commands[i];
            if (!(command instanceof ItemizedCommand)) {
                continue;
            }
            items = ((ItemizedCommand)command).getItems();
            iItems = items.iterator();
            while (iItems.hasNext()) {
                item = iItems.next();
                if (item.isMoreData()) {
                    return item;
                }
            }
        }
        return null;
    }

    /**
     * Returns item that contains large object (moreData == true).
     * Only last command can have more data.
     *
     * @param commands AbstractCommand[]
     * @return item that contains large object or null if
     *         there aren't items with moreData == true
     */
    public static Item getLargeObject(List<AbstractCommand> commands) {
        AbstractCommand command = null;
        ArrayList<Item> items = null;
        Iterator<Item> iItems = null;
        Item item = null;

        command = (commands.get(commands.size() -1));

        if (! (command instanceof ItemizedCommand)) {
            return null;
        }
        items = ( (ItemizedCommand)command).getItems();
        iItems = items.iterator();
        while (iItems.hasNext()) {
            item = iItems.next();
            if (item.isMoreData()) {
                return item;
            }
        }

        return null;
    }

    /**
     * Checks if the given command contains a item with more data
     * @param command AbstractCommand
     * @return true if the given command contains a item with more data, false otherwise
     */
    public static boolean hasLargeObject(AbstractCommand command) {
        if (! (command instanceof ItemizedCommand)) {
            return false;
        }
        ArrayList<Item> items = ((ItemizedCommand)command).getItems();
        Iterator<Item> iItems = items.iterator();
        Item item = null;
        while (iItems.hasNext()) {
            item = iItems.next();
            if (item.isMoreData()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Reassign ordered cmdId to all commands in the given list.
     *
     * @param commandsList the list of commands
     */
    public static void updateCmdId(List<AbstractCommand> commandsList) {
        updateCmdId(commandsList, 1);
    }

    /**
     * Reassign ordered cmdId to all commands in the given list.
     * <br>To the first command is assigned cmdID=startId.
     *
     * @param commands List
     * @param startId the first id to use
     * @return the last id used + 1
     */
    public static int updateCmdId(List<AbstractCommand> commands, int startId) {
        Iterator<AbstractCommand> iCommand = commands.iterator();
        AbstractCommand command = null;
        int id = startId;
        while (iCommand.hasNext()) {
            command = iCommand.next();
            command.setCmdID(new CmdID(id++));
            if (command instanceof Sync) {
                id = updateCmdId( ( (Sync)command).getCommands(), id);
            } else if (command instanceof Atomic) {
                id = updateCmdId( ( (Atomic)command).getCommands(), id);
            } else if (command instanceof Sequence) {
                id = updateCmdId( ( (Sequence)command).getCommands(), id);
            }
        }
        return id;
    }



    // --------------------------------------------------------- Private methods

    /**
     * This class compares two Status object in according to their cmdRef
     */
    private static class StatusComparator implements java.util.Comparator {
        private StatusComparator() {
        }

        public int compare(Object o1, Object o2) {
            Object value1 = null;
            Object value2 = null;

            value1 = new Integer(((Status)o1).getCmdRef());
            value2 = new Integer(((Status)o2).getCmdRef());

            return ( (Integer)value1).compareTo((Integer)value2);
        }
    }

    /**
     * This class compares two AbstractCommand object in according to their cmdID
     */
    private static class AbstractCmdComparator implements java.util.Comparator {
        private AbstractCmdComparator() {
        }

        public int compare(Object o1, Object o2) {
            Object value1 = null;
            Object value2 = null;

            value1 = new Integer(((AbstractCommand)o1).getCmdID().getCmdID());
            value2 = new Integer(((AbstractCommand)o2).getCmdID().getCmdID());

            return ( (Integer)value1).compareTo((Integer)value2);
        }
    }
}
