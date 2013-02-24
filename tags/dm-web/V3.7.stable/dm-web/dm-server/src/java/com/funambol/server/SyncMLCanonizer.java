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

package com.funambol.server;


import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

import com.funambol.framework.tools.XMLTools;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/**
 * This class is used for transformer the message in the canonical form.
 *
 */
public class SyncMLCanonizer implements Serializable {

    // ------------------------------------------------------------ Private data

    private static final Logger log = Logger.getLogger(com.funambol.server.SyncMLCanonizer.class.getName());

    // ------------------------------------------------------------ Constructors
    public SyncMLCanonizer() {
    }

    // ---------------------------------------------------------- Public methods

    /**
     * The message must be canonized every time before call the XML-Java
     * mapping tool (JiBX) that it parsers and generates the SyncML object.
     *
     * @param message the input XML message
     *
     * @return message the input XML message canonized
     **/
    public String canonizeOutputMessage(String message) {
        if (log.isEnabledFor(Level.TRACE)) {
            log.trace("Starting process of canonization on output message");
        }

        String response = metInfNamespaceHandler(message).replaceAll("<MoreData></MoreData>", "<MoreData/>");

        return mgmtTreeNamespaceHandler(response);
    }


    /**
     * The message input must be canonized every time before call the XML-Java
     * mapping tool (JiBX) that it parsers and generates the SyncML object.
     *
     * @param message the input XML message
     *
     * @return message the input XML message canonized
     **/
    public String canonizeInputMessage(String message) {
        if (log.isEnabledFor(Level.TRACE)) {
            log.trace("Starting process of canonization on input message");
        }

        message = replaceEntity(message);

        return message;
    }


    /**
     * Replace into message, into the tag <Data> amd if it isn't a CDATA:
     * <ui>
     * <li>'&' with '&amp;amp;'
     *      (if the character isn't the fisrt char of &amp;amp; or &amp;lt; or &amp;gt; or &amp;quot;)
     * <li>'<' with '&amp;lt;'
     * <li>'>' with '&amp;gt;'
     * <li>'"' with '&amp;quot;'
     * </ui>
     * @param message the original message xml
     *
     * @return message the message updated
     */
    private String replaceEntity(String msg) {

        int s = 0;
        int e = 0;

        StringBuffer response = new StringBuffer();
        while ( (e = msg.indexOf("<Data>", s)) >= 0) {

            // 6 = length of <Data>
            response = response.append(msg.substring(s, e + 6));

            int a = msg.indexOf("</Data>", e);
            String data = msg.substring(e + 6, a);

            if (data.startsWith("<![CDATA[")) {
                // not replace nothing
            } else if (data.trim().startsWith("<SyncML xmlns=\"syncml:dmddf1.2\">")) {
                // The data contains a MgmtTree
            } else {
                data = XMLTools.replaceAmp(data);
                data = StringUtils.replace(data, "<", "&lt;");
                data = StringUtils.replace(data, ">", "&gt;");
                data = StringUtils.replace(data, "\"", "&quot;");
            }
            s = a + 7; // length of </Data>
            response.append(data).append("</Data>");
        }
        response.append(msg.substring(s, msg.length()));

        return response.toString();
    }

    /**
     * This is a temporary solution in order to obviate to a JiBX bug: it does
     * not allow to declare the namespace to level of structure.
     *
     * @param msg the server response
     *
     * @return the response with namespace correctly added into MetInf element
     */
    private String metInfNamespaceHandler(String msg) {
        int s = 0;
        int e = 0;

        StringBuffer response = new StringBuffer();
        while (( e = msg.indexOf("<Meta", s)) >= 0) {

            response = response.append(msg.substring(s, e));

            int a = msg.indexOf("</Meta>", e);
            String meta = msg.substring(e, a);

            meta = meta.replaceAll("<Type>"   , "<Type xmlns='syncml:metinf'>");
            meta = meta.replaceAll("<Format>" , "<Format xmlns='syncml:metinf'>");
            meta = meta.replaceAll("<Mark>"   , "<Mark xmlns='syncml:metinf'>");
            meta = meta.replaceAll("<Size>"   , "<Size xmlns='syncml:metinf'>");
            meta = meta.replaceAll("<Anchor>" , "<Anchor xmlns='syncml:metinf'>");
            meta = meta.replaceAll("<Version>", "<Version xmlns='syncml:metinf'>");
            meta = meta.replaceAll("<NextNonce>" , "<NextNonce xmlns='syncml:metinf'>");
            meta = meta.replaceAll("<MaxMsgSize>", "<MaxMsgSize xmlns='syncml:metinf'>");
            meta = meta.replaceAll("<MaxObjSize>", "<MaxObjSize xmlns='syncml:metinf'>");
            meta = meta.replaceAll("<EMI>"    , "<EMI xmlns='syncml:metinf'>");
            meta = meta.replaceAll("<Mem>"    , "<Mem xmlns='syncml:metinf'>");

            s = a + 7;
            response.append(meta).append("</Meta>");
        }
        String sResponse = response.append(msg.substring(s, msg.length())).toString();

        return sResponse;
    }


    /**
     * This is a temporary solution in order to obviate to a JiBX bug: it does
     * not allow to declare the namespace to level of structure.
     *
     * @param msg the server response
     *
     * @return the response with namespace correctly added into MetInf element
     */
    private String mgmtTreeNamespaceHandler(String msg) {
        int s = 0;
        int e = 0;

        int cont = 0;
        StringBuffer response = new StringBuffer();
        while (( e = msg.indexOf("<SyncML>", s)) >= 0) {

            response = response.append(msg.substring(s, e));

            int a = msg.indexOf("</SyncML>", e);

            String mgtmTree = msg.substring(e + 8, a);

            if (cont > 0) {
                if (mgtmTree.trim().startsWith("<MgmtTree>")) {
                    response.append("<SyncML xmlns='syncml:dmddf1.2'>");
                }
                response.append(mgtmTree);
                response.append("</SyncML>");
                s = a + 9;

                } else {
                    response.append("<SyncML>");
                    s = e + 8;
                }

                cont ++;
            }
            String sResponse = response.append(msg.substring(s, msg.length())).toString();

            return sResponse;
        }


}
