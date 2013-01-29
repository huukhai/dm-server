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
package com.funambol.server.notification;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import com.funambol.framework.core.Sync4jException;
import com.funambol.framework.core.dm.bootstrap.WapProvisioningDoc;
import com.funambol.framework.tools.Base64;

import org.kxml.Attribute;
import org.kxml.Xml;
import org.kxml.parser.ParseEvent;
import org.kxml.parser.XmlParser;
import org.kxml.wap.WapExtensionEvent;

/**
 *  Utility class for WBXML
 *
 *
 * @version $Id: WBXMLTools.java,v 1.5 2007-06-19 08:16:25 luigiafassina Exp $
 */
public class WBXMLTools {

    // --------------------------------------------------------------- Constants

    // ------------------------------------------------------------ Private data

    // ---------------------------------------------------------- Public methods

    /**
     * Converts a string to a WBXML message.
     *
     * @param s the String to convert - NOT NULL
     *
     * @return the WBXML message
     *
     * @throws Sync4jException in case of parser errors
     */
    public static byte[] toWBXML(final String s) throws Sync4jException {
        WapProvisioningDocWriter writer = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            writer = new WapProvisioningDocWriter(out);
            XmlParser xml = new XmlParser(new StringReader(s));
            traverseXML(xml, writer);
        } catch (IOException e) {
            throw new Sync4jException(e.getMessage(), e);
        } finally {
            if (writer != null)try {writer.close();
            } catch (Exception e) {}
        }

        return out.toByteArray();
    }

    /**
     * Encodes a <i>WapProvisioningDoc</i> to WBXML
     *
     * @param msg the message to encode
     *
     * @return the encoded stream of bytes (as a byte[] buffer).
     *
     * @throws Exception in case of errors
     */
    public static byte[] toWBXML(WapProvisioningDoc msg) throws Exception {
        return toWBXML(msg.toXml());
    }

    // --------------------------------------------------------- Private methods
    private static void traverseXML(XmlParser parser, WapProvisioningDocWriter writer) throws
        IOException {

        boolean leave = false;

        do {
            ParseEvent event = parser.read();

            switch (event.getType()) {
                case Xml.START_TAG:
                    WapProvisioningDocWriter tagWriter = null;

                    String name = event.getName();

                    tagWriter = writer;
                    tagWriter.startTag(name);

                    // see API doc of StartTag for more access methods

                    List attributes = event.getAttributes();
                    if (attributes != null) {
                        int numAttr = attributes.size();
                        Attribute attr = null;
                        for (int i = 0; i < numAttr; i++) {
                            attr = event.getAttribute(i);
                            tagWriter.attribute(attr.getName(), attr.getValue());
                        }
                    }

                    traverseXML(parser, tagWriter); // recursion

                    break;

                case Xml.END_TAG:
                    writer.endTag();
                    leave = true;
                    break;

                case Xml.END_DOCUMENT:
                    leave = true;
                    break;

                case Xml.TEXT:
                    writer.write(event.getText());
                    break;

                case Xml.WHITESPACE:
                    break;

                default:

            }
        } while (!leave);
    }

    /**
     * Converts a WBXML message into the corresponding XML message.
     *
     * @param msg the message to convert - NOT NULL
     *
     * @return the XML message or NULL if an error occurred
     *
     * @throws Sync4jException in case of parser errors
     */
    public static String wbxmlToXml(final byte[] msg) throws Sync4jException {

        ByteArrayInputStream in = null;
        try {
            in = new ByteArrayInputStream(msg);

            WapProvisioningDocParser parser = new WapProvisioningDocParser(in);

            return parseWBXML(parser);

        } catch (Throwable t) {
            t.printStackTrace();
            throw new Sync4jException(t.getMessage(), t);
        }
    }

    private static String parseWBXML(WapProvisioningDocParser parser) throws IOException {
        return parseWBXML(parser, false, false, false);
    }

    private static String parseWBXML(WapProvisioningDocParser parser, boolean inCred, boolean b64,
                                     boolean auth_md5) throws IOException {

        StringBuffer buf = new StringBuffer();
        boolean leave = false;

        String tagName = null;
        String text = null;

        do {
            ParseEvent event = parser.read();
            switch (event.getType()) {
                case Xml.START_TAG:

                    tagName = event.getName();

                    buf.append("<");
                    buf.append(tagName);
                    List attrs = event.getAttributes();
                    if (attrs != null) {
                        for (int i = 0; i < attrs.size(); i++) {
                            Attribute attr = (Attribute)attrs.get(i);
                            buf.append(" ");
                            buf.append(attr.getName());
                            buf.append("='");
                            buf.append(attr.getValue());
                            buf.append("'");
                        }
                    }
                    buf.append(">");

                    if (inCred == false) {
                        text = parseWBXML(parser, "Cred".equals(tagName), false, false);
                    } else {
                        text = parseWBXML(parser, true, b64, auth_md5);
                    }

                    if (inCred) {
                        if ("Meta".equals(tagName)) {
                            b64 = (text.indexOf("b64") >= 0);
                            auth_md5 = (text.indexOf("auth-md5") >= 0);
                            buf.append(text);
                            text = parseWBXML(parser, true, b64, auth_md5);
                        }
                    }

                    buf.append(text);
                    break;

                case Xml.END_TAG:
                    buf.append("</");
                    buf.append(event.getName());
                    buf.append(">");
                    leave = true;
                    break;

                case Xml.END_DOCUMENT:
                    leave = true;
                    break;

                case Xml.TEXT:
                    buf.append(event.getText());
                    break;

                case Xml.WAP_EXTENSION:
                    text = event.getText();

                    if (event instanceof WapExtensionEvent) {
                        WapExtensionEvent e = (WapExtensionEvent)event;
                        Object content = e.getContent();

                        if (auth_md5 && !b64 && content != null) {

                            if (content instanceof byte[]) {
                                text = new String(Base64.encode( (byte[])content));
                            }
                        }
                    }

                    buf.append(text);
                    break;

                case Xml.WHITESPACE:
                    break;

                default:
            }
        } while (!leave);

        return buf.toString();
    }
}
