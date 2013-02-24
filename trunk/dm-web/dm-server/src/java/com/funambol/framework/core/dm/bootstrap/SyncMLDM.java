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

import java.beans.XMLEncoder;
import java.util.Map;

import org.apache.commons.collections15.map.ListOrderedMap;


/**
 * Corresponds to the &lt;SyncML DM&gt; management object
 *
 * @version $Id: SyncMLDM.java,v 1.1 2006/11/15 13:43:15 nichele Exp $
 */

public class SyncMLDM implements java.io.Serializable {

    // --------------------------------------------------------------- Constants
   public static final String SYNCML_DM_BASE_URI    = "./SyncML";
   public static final String SYNCML_DM_DMACC       = "/DMAcc";
   public static final String SYNCML_DM_CON         = "/Con"  ;

   // ------------------------------------------------------------ Private data
   private DMAcc dmAcc;
   private ConNode con;

   // ------------------------------------------------------------ Constructors
   /** For serialization purposes */
   public SyncMLDM() {}

    /**
     * Creates a new SyncMLDM object with the given parameters
     *
     * @param dmAcc the dm account node
     * @param con the connection node
     *
     */
    public SyncMLDM(final DMAcc dmAcc,
                    final ConNode con) {
        this.dmAcc  = dmAcc;
        this.con    = con;
    }


    // ---------------------------------------------------------- Public methods

    /**
     * Gets the dmAcc
     *
     * @return  the dmAcc
     */
    public DMAcc getDmAcc() {
        return dmAcc;
    }

    /**
     * Sets the dmAcc
     *
     * @param  dmAcc the dmAcc
     */
    public void setDmAcc(DMAcc dmAcc) {
        this.dmAcc = dmAcc;
    }

    /**
     * Gets the con
     *
     * @return  the con
     */
    public ConNode getCon() {
        return con;
    }

    /**
     * Sets the con
     *
     * @param  con the con
     */
    public void setCon(ConNode con) {
        this.con = con;
    }


    /**
     * Used to test
     * @param args String[]
     */
    public static void main(String[] args) {
        SyncMLDM syncmlDm = new SyncMLDM();

        DMAcc acc = new DMAcc();
        Map<String, DMAccount> accounts = new ListOrderedMap<String, DMAccount>();

        DMAccount account = new DMAccount();
        accounts.put("AccountName", account);

        DMAccount account2 = new DMAccount();
        accounts.put("AccountName2", account2);
        account2.setAddressType(2);
        account2.setClientNonce(new byte[] {23,24,25,26});

        acc.setDMAccounts(accounts);
        syncmlDm.setDmAcc(acc);

        account.setConRef("ConnectionName");

        Ext ext = new Ext();
        ext.setNode("service", "5");

        XMLEncoder enc = new XMLEncoder(System.out);
        enc.writeObject(ext);
        enc.flush();
        enc.close();

        Map<String, Auth> auths = new ListOrderedMap<String, Auth>();
        Auth auth = new Auth("id", "secret");
        auths.put("auth-method", auth );
        NAP nap = new NAP("napBearer", "napAddress", "napAddressType");
        nap.setAuths(auths);

        Map<String, Auth> authsPx = new ListOrderedMap<String, Auth>();
        Auth authPx = new Auth("id", "secret");
        authsPx.put("auth-method", authPx );
        PX px = new PX("portNbr", "pxAddress", "pxAddressType");
        px.setAuths(authsPx);

        Connection con = new Connection(ext, nap, px);
        ConNode conNode = new ConNode(con, "ConnectionName");

        syncmlDm.setCon(conNode);

        enc = new XMLEncoder(System.out);
        enc.writeObject(syncmlDm);
        enc.flush();
        enc.close();
    }

}
