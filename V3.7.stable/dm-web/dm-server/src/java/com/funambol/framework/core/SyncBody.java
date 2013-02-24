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

package com.funambol.framework.core;


import java.util.ArrayList;
import java.util.Arrays;


/**
 * Corresponds to the &lt;SyncBody&gt; element in the SyncML represent DTD
 *
 * 
 *  @see    SyncHdr
 *
 *  @version $Id: SyncBody.java,v 1.2 2006/08/07 21:05:28 nichele Exp $
 */
public final class SyncBody
implements java.io.Serializable {
    // --------------------------------------------------------------- Constants
    private static final String ALLOWED_COMMANDS = ":Alert:Add:Atomic:Copy:Delete:Exec:Get:Map:Put:Replace:Results:Search:Sequence:Status:Sync:";

    // ------------------------------------------------------------ Private data
    private ArrayList<AbstractCommand> commands = new ArrayList<AbstractCommand>();
    private Boolean finalMsg;

    // ------------------------------------------------------------ Constructors

    /** For serialization purposes */
    protected SyncBody() {}

    /**
     * Create a new SyncBody object. The commands in <i>commands</i>
     * must be of the allowed types.
     *
     * @param commands The array elements must be an instance of one of these
     *                 classes: {@link Alert},  {@link Atomic}, {@link Copy},
     *                 {@link Exec}, {@link Get}, {@link Map}, {@link Put},
     *                 {@link Results}, {@link Search}, {@link Sequence},
     *                 {@link Status}, {@link Sync}
     * @param finalMsg is true if this is the final message that is being sent
     *
     */
    public SyncBody( final AbstractCommand[] commands, final boolean finalMsg) {

        setCommands(commands);
        this.finalMsg = (finalMsg) ? Boolean.valueOf(finalMsg) : null;
    }

    // ---------------------------------------------------------- Public methods

    /**
     *
     *  @return the return value is guaranteed to be non-null. Also,
     *          the elements of the array are guaranteed to be non-null.
     *
     */
    public ArrayList<AbstractCommand> getCommands() {
        return this.commands;
    }


    /**
     * Sets the sequenced commands. The given commands must be of the allowed
     * types.
     *
     * @param commands the commands - NOT NULL and o the allawed types
     *
     * @throws IllegalArgumentException if the constraints are not met
     */
    public void setCommands(AbstractCommand[] commands) {
        if (commands == null) {
            throw new IllegalArgumentException("commands cannot be null");
        }

        for (int i = 0; i < commands.length; i++) {
            if (commands[i] == null) {
                throw new IllegalArgumentException("commands[" + i +"] cannot be null");
            } else if (   (!(commands[i] instanceof Alert))
                       && (!(commands[i] instanceof Add))
                       && (!(commands[i] instanceof Atomic))
                       && (!(commands[i] instanceof Copy))
                       && (!(commands[i] instanceof Delete))
                       && (!(commands[i] instanceof Exec))
                       && (!(commands[i] instanceof Get))
                       && (!(commands[i] instanceof Map))
                       && (!(commands[i] instanceof Put))
                       && (!(commands[i] instanceof Replace))
                       && (!(commands[i] instanceof Results))
                       && (!(commands[i] instanceof Search))
                       && (!(commands[i] instanceof Sequence))
                       && (!(commands[i] instanceof Status))
                       && (!(commands[i] instanceof Sync))) {
                throw new IllegalArgumentException(
                    "commands[" + i + "] cannot be a " + commands[i].getName()
                );
            }
        }
        this.commands.clear();
        this.commands.addAll(Arrays.asList(commands));
    }

    /**
     * Sets the message as final
     *
     * @param finalMsg the Boolean value of finalMsg property
     */
    public void setFinalMsg(Boolean finalMsg) {
        this.finalMsg = (finalMsg.booleanValue()) ? finalMsg : null;
    }

    /**
     * Gets the value of finalMsg property
     *
     * @return true if this is the final message being sent, otherwise false
     *
     */
    public boolean isFinalMsg() {
        return (finalMsg != null);
    }

    /**
     * Gets the value of finalMsg property
     *
     * @return true if this is the final message being sent, otherwise null
     *
     */
    public Boolean getFinalMsg() {
        if (!finalMsg.booleanValue()) {
            return null;
        }
        return finalMsg;
    }
}
