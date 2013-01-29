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

package com.funambol.framework.tools;


/**
 * Container of utility methods to handle xml
 *
 *
 * @version $Id: XMLTools.java,v 1.1 2006/11/15 14:02:34 nichele Exp $
 */
public class XMLTools {


    /**
     * Replace into message the char '&' with '&amp;amp;'
     * (if the character isn't the fisrt char of &amp;amp; or &amp;lt; or &amp;gt; or &amp;quot;)
     * @param message the original message xml
     *
     * @return message the message updated
     */
    public static String replaceAmp(String message) {
        StringBuffer sb = new StringBuffer();
        int numCh = message.length();

        String temp = null;

        // char[] of the message
        char[] chString = null;
        chString = message.toCharArray();

        for (int i = 0; i < numCh; i++) {
            sb.append(chString[i]);
            if (chString[i] == '&') {
                int t = 0;
                if ((i + 6) > numCh) {
                    t = numCh;
                } else {
                    t = i + 6;
                }
                temp = message.substring(i + 1, t);
                if (!temp.startsWith("amp;") &&
                    !temp.startsWith("lt;") &&
                    !temp.startsWith("gt;") &&
                    !temp.startsWith("quot;")) {
                    // convert '&' in '&amp;' adding 'amp;' in the StringBuffer
                    sb.append("amp;");
                } else {
                    // '&' is already in '&amp;' form
                }
            }
        }
        return sb.toString();
    }

}
