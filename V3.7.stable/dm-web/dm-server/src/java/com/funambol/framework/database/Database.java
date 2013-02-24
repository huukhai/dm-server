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

package com.funambol.framework.database;

import com.funambol.framework.core.Alert;
import com.funambol.framework.core.AlertCode;
import com.funambol.framework.core.Anchor;
import com.funambol.framework.core.Item;
import com.funambol.framework.core.Source;
import com.funambol.framework.core.StatusCode;
import com.funambol.framework.core.Target;

import java.security.Principal;

import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * This class represents a synchronizable database.
 *
 *
 * @version $Id: Database.java,v 1.3 2006/11/15 14:55:54 nichele Exp $
 */
public class Database {

    // -------------------------------------------------------------- Properties

    /**
     * The database name
     */
    private String name = null;

    public String getName() {
        return this.name;
    }

    /**
     * The database type (e.g. text/x-vcard)
     */
    private String type = null;

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * The target of the database
     */
    private Target target = null;

    public Target getTarget() {
        return this.target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    /**
     * The source of the database
     */
    private Source source = null;

    public Source getSource() {
        return this.source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    /**
     * Client synchronization anchor
     */
    private Anchor anchor = null;

    public Anchor getAnchor() {
        return anchor;
    }

    public void setAnchor(Anchor anchor) {
        this.anchor = anchor;
    }

    /**
     * Server synchronization anchor
     */
    private Anchor serverAnchor = null;

    public Anchor getServerAnchor() {
        return serverAnchor;
    }

    public void setServerAnchor(Anchor serverAnchor) {
        this.serverAnchor = serverAnchor;
    }

    /**
     * Synchronization method
     */
    private int method = AlertCode.SLOW;

    public int getMethod() {
        return this.method;
    }

    public void setMethod(int method) {
        this.method = method;
    }

    /**
     * Database status
     */
    private int statusCode = StatusCode.OK;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * The alert command used to request the sychronization of this database
     */
    private Alert alertCommand = null;

    public Alert getAlertCommand() {
        return this.alertCommand;
    }

    public void setAlertCommand(Alert alertCommand) {
        this.alertCommand = alertCommand;
    }

    /**
     * The associated principal
     */
    private Principal principal = null;

    /** Getter for property principal.
     * @return Value of property principal.
     *
     */
    public Principal getPrincipal() {
        return principal;
    }

    /** Setter for property principal.
     * @param principal New value of property principal.
     *
     */
    public void setPrincipal(Principal principal) {
        this.principal = principal;
    }

    // -------------------------------------------------------------------------

    /**
     * The items of the database to be added, copied, deleted, executed, replaced
     */
    private Item[] addItems       = null;
    private Item[] copyItems      = null;
    private Item[] deleteItems    = null;
    private Item[] execItems      = null;
    private Item[] replaceItems   = null;
    private Item[] existingItems  = null;

    public void setAddItems(Item[] items) {
        this.addItems = items;
    }

    public Item[] getAddItems() {
        return this.addItems;
    }

    public void setCopyItems(Item[] items) {
        this.copyItems = items;
    }

    public Item[] getCopyItems() {
        return this.copyItems;
    }

    public void setDeleteItems(Item[] items) {
        this.deleteItems = items;
    }

    public Item[] getDeleteItems() {
        return this.deleteItems;
    }

    public void setExecItems(Item[] items) {
        this.execItems = items;
    }

    public Item[] getExecItems() {
        return this.execItems;
    }

    public void setReplaceItems(Item[] items) {
        this.replaceItems = items;
    }

    public Item[] getReplaceItems() {
        return this.replaceItems;
    }

    public void setExistingItems(Item[] items) {
        this.existingItems = items;
    }

    public Item[] getExistingItems() {
        return this.existingItems;
    }

    // ------------------------------------------------------------ Constructors

    /**
     * Creates a new instance of Database
     *
     * @param name database idenfier - NOT NULL
     * @param type database type (e.g. text/x-vcard) - NULL
     * @param target database target - NULL
     * @param source database source - NULL
     * @param anchor database last/next anchor tags NULL
     * @param principal the associated Principal. NULL means no associated principal
     */
    public Database(String     name     ,
                    String     type     ,
                    Target     target   ,
                    Source     source   ,
                    Anchor     anchor   ,
                    Principal  principal) {
        if (name == null) {
            throw new NullPointerException("name cannot be null");
        }

        this.name      = name  ;
        this.type      = type  ;
        this.target    = target;
        this.source    = source;
        this.anchor    = anchor;
        this.principal = principal;
    }

    /**
     * Creates a new instance of Database
     *
     * @param name database idenfier - NOT NULL
     */
    public Database(String name) {
        this(name, null, null, null, null, null);
    }

    // ---------------------------------------------------------- Public methods

    public String getLast() {
        return (anchor != null) ? anchor.getLast() : null;
    }

    public String getNext() {
        return (anchor != null) ? anchor.getNext() : null;
    }

    public String toString() {
        ToStringBuilder sb = new ToStringBuilder(this);

        sb.append("name",       name      ).
           append("type",       type      ).
           append("statusCode", statusCode).
           append("target",     target    ).
           append("source",     source    ).
           append("anchor",     anchor    ).
           append("principal",  principal );

        return sb.toString();
    }
}
