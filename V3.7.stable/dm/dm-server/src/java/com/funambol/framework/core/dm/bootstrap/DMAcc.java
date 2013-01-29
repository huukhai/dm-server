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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.map.ListOrderedMap;

/**
 * Corresponds to the &lt;DMAcc&gt; in the management tree
 *
 * @version $Id: DMAcc.java,v 1.1 2006/11/15 13:43:15 nichele Exp $
 */
public class DMAcc  implements java.io.Serializable {

    // --------------------------------------------------------------- Constants

   // ------------------------------------------------------------ Private data

   private Map<String, DMAccount> dmAccounts;



   // ------------------------------------------------------------ Constructors
   /** For serialization purposes */
   public DMAcc() {}

    /**
     * Creates a new DMAcc object with the given <code>DMAccount</code> and the given name
     *
     * @param dmAccount the dm account
     * @param accountName the name of the dm account
     */
    public DMAcc(final DMAccount dmAccount, final String accountName) {
        this.dmAccounts = new ListOrderedMap<String, DMAccount>();
        this.dmAccounts.put(accountName, dmAccount);
    }


    // ---------------------------------------------------------- Public methods

    /**
     * Add new dm account
     * @param dmAccount the dm account to add
     * @param accountName the name of the dm account to add
     */
    public void addDMAccount(final DMAccount dmAccount, final String accountName) {
        this.dmAccounts.put(accountName, dmAccount);
    }

    /**
     * Gets the dm account with the given name
     *
     * @param accountName the name of the dm account
     * @return the dm account with the given name.<br/><code>null</code> if not exists.
     */
    public DMAccount getDMAccount(final String accountName) {
        return this.dmAccounts.get(accountName);
    }


    /**
     * Rename, if exists, a <code>DMAccount</code> contained in this <code>DMAcc</code>
     *
     * @param oldAccountName the name of the dm account to rename
     * @param newAccountName the new name of the dm account to rename
     */
    public void renameDMAccount(String oldAccountName, String newAccountName) {

        if (dmAccounts == null) {
            // no accounts
            return ;
        }

        //
        // In order to rename a dmAccount maintaining the order, we must create
        // a new ListOrderedMap
        //
        Set<String> keys = dmAccounts.keySet();
        Iterator<String> itKeys = keys.iterator();
        String accountName = null;
        Map<String, DMAccount> newDmAccounts = new ListOrderedMap<String, DMAccount>();
        while (itKeys.hasNext()) {
            accountName = itKeys.next();
            if (accountName.equalsIgnoreCase(oldAccountName)) {
                newDmAccounts.put(newAccountName, dmAccounts.get(oldAccountName));
            } else {
                newDmAccounts.put(accountName, dmAccounts.get(accountName));
            }
        }
        this.dmAccounts = newDmAccounts;
    }


    /**
     * Gets the dm accounts
     *
     * @return the dm accounts
     */
    public Map<String, DMAccount> getDMAccounts() {
        return dmAccounts;
    }

    /**
     * Sets the dm accounts
     *
     * @param dmAccounts the dmAccount to set
     */
    public void setDMAccounts(Map<String, DMAccount> dmAccounts) {
        this.dmAccounts = dmAccounts;
    }


    /**
     * Returns the number of dm accounts
     *
     * @return the number of dm accounts
     */
    public int numberOfDMAccounts() {
        return dmAccounts.size();
    }

}
