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

/**
 * This interface includes the costants relative to the SyncML.
 *
 *
 *
 * @version $Id: Constants.java,v 1.3 2006/11/15 14:23:52 nichele Exp $
 */
public interface Constants {
   public static final String MIMETYPE_SYNCMLDS_XML
                                        = "application/vnd.syncml+xml";
   public static final String MIMETYPE_SYNCMLDS_WBXML
                                        = "application/vnd.syncml+wbxml";
   public static final String MIMETYPE_SYNCMLDM_XML
                                        = "application/vnd.syncml.dm+xml";
   public static final String MIMETYPE_SYNCMLDM_WBXML
                                        = "application/vnd.syncml.dm+wbxml";
   public static final String MIMETYPE_SYNCML_DEVICEINFO_XML
                                        = "application/vnd.syncml-devinf+xml";
   public static final String MIMETYPE_SYNCML_DEVICEINFO_WBXML
                                        = "application/vnd.syncml-devinf+wbxml";

   public static final String NAMESPACE_METINF = "syncml:metinf";
   public static final String NAMESPACE_DEVINF = "syncml:devinf";


   public static final String AUTH_TYPE_NONE  = "none";
   public static final String AUTH_TYPE_MD5   = "syncml:auth-md5";
   public static final String AUTH_TYPE_HMAC  = "syncml:auth-MAC";
   public static final String AUTH_TYPE_BASIC = "syncml:auth-basic";
   public static final String AUTH_TYPE_CLEAR = "syncml:auth-clear";

   public static final String AUTH_SUPPORTED_TYPES = AUTH_TYPE_BASIC
                                                   + ','
                                                   + AUTH_TYPE_CLEAR
                                                   + ','
                                                   + AUTH_TYPE_MD5
                                                   + ','
                                                   + AUTH_TYPE_HMAC;

   public static final VerDTD DTD_1_0 = new VerDTD("1.0");
   public static final VerDTD DTD_1_1 = new VerDTD("1.1");
   public static final VerDTD DTD_1_2 = new VerDTD("1.2");

   public static final String TNDS_XML_TYPE   = "application/vnd.syncml.dmtnds+xml";
   public static final String TNDS_WBXML_TYPE = "application/vnd.syncml.dmtnds+wbxml";

   public static final String TNDS_XML_FORMAT   = "xml";
   public static final String TNDS_WBXML_FORMAT = "bin";

   public static final VerProto SYNCML_DS_1_0   = new VerProto("SyncML/1.0");
   public static final VerProto SYNCML_DS_1_1   = new VerProto("SyncML/1.1");
   public static final VerProto SYNCML_DS_1_1_1 = new VerProto("SyncML/1.1.1");
   public static final VerProto SYNCML_DM_1_1   = new VerProto("DM/1.1");
   public static final VerProto SYNCML_DM_1_2   = new VerProto("DM/1.2");

   public static final String FORMAT_B64 = "b64";
   public static final String FORMAT_CLEAR = "clear";
  }
