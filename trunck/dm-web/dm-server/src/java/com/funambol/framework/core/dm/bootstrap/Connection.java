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


/**
 * Corresponds to the single connection node in the management tree
 *
 * @version $Id: Connection.java,v 1.1 2006/11/15 13:43:15 nichele Exp $
 */
public class Connection  implements java.io.Serializable {

    // --------------------------------------------------------------- Constants
    public static final String CON_NAP = "/NAP";
    public static final String CON_PX  = "/PX";
    public static final String CON_EXT = "/Ext";


    // ------------------------------------------------------------ Private data
    private NAP nap;
    private PX  px;
    private Ext ext;


    // ------------------------------------------------------------ Constructors
    /**
     * Creates a new Connection object
     *
     */
    public Connection() {}


     /**
      * Creates a new Connection object with the given parameters
      *
      * @param nap the nap node
      * @param px the px node
      */
     public Connection(final Ext ext,
                       final NAP nap,
                       final PX  px ) {
         this.nap    = nap;
         this.px     = px;
         this.ext    = ext;
     }


     // ---------------------------------------------------------- Public methods

     /**
      * Gets the nap
      *
      * @return  the nap
      */
     public NAP getNap() {
         return nap;
     }

     /**
      * Sets the nap
      *
      * @param  nap the nap
      */
     public void setNap(NAP nap) {
         this.nap = nap;
     }

     /**
      * Gets the px
      *
      * @return  the px
      */
     public PX getPX() {
         return px;
     }

     /**
      * Sets the px
      *
      * @param  px the px
      */
     public void setPX(PX px) {
         this.px = px;
     }

     /**
      * Gets the ext
      *
      * @return the ext
      */
     public Ext getExt() {
         return ext;
     }

     /**
      * Sets the ext
      *
      * @param ext the ext
      */
     public void setExt(Ext ext) {
         this.ext = ext;
     }

 }
