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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;

import org.kxml.Attribute;
import org.kxml.Xml;
import org.kxml.parser.ParseEvent;
import org.kxml.parser.Tag;
import org.kxml.parser.XmlParser;
import org.kxml.wap.SyncMLParser;
import org.kxml.wap.SyncMLWriter;
import org.kxml.wap.WapExtensionEvent;
import org.kxml.wap.Wbxml;

import com.funambol.framework.core.Sync4jException;
import com.funambol.framework.core.SyncML;


/**
 *  Utility class for WBXML stuff
 *
 *
 * @version $Id: WBXMLTools.java,v 1.6 2007-06-19 08:16:16 luigiafassina Exp $
 */
public class WBXMLTools {

    // --------------------------------------------------------------- Constants
    public static final String WELL_KNOWN_NS = ",DevInf,SyncML,MgmtTree,";

    // ------------------------------------------------------------ Private data

    // ---------------------------------------------------------- Public methods

    /**
     * Converts a string to a WBXML message.
     *
     * @param s the String to convert - NOT NULL
     * @param verDTD the version of the dtd to use
     *
     * @return the WBXML message
     *
     * @throws Sync4jException in case of parser errors
     */
    public static byte[] toWBXML(final String s, final String verDTD)
    throws Sync4jException {
        SyncMLWriter writer = null;
        try {

            XmlParser xml = new XmlParser(new StringReader(s));

            boolean[] inTag = new boolean[5];

            writer = traverseXML(xml, null, inTag, verDTD);
        } catch (IOException e) {
            throw new Sync4jException(e.getMessage(), e);
        } finally {
            if (writer != null) try {writer.close();} catch (Exception e) {}
        }

        return writer.getBytes();
    }

    /**
     * Converts a string to a WBXML message.
     *
     * @param s the String to convert - NOT NULL
     *
     * @return the WBXML message
     *
     * @throws Sync4jException in case of parser errors
     */
    public static byte[] toWBXML(final String s)
    throws Sync4jException {
        SyncMLWriter writer = null;
        try {

            XmlParser xml = new XmlParser(new StringReader(s));

            boolean[] inTag = new boolean[5];

            writer = traverseXML(xml, null, inTag, null);
        } catch (IOException e) {
            throw new Sync4jException(e.getMessage(), e);
        } finally {
            if (writer != null) try {writer.close();} catch (Exception e) {}
        }

        return writer.getBytes();
    }

    /**
     * Encodes a <i>Message</i> to WBXML
     * <p>
     * The message is fixed before encoding in order to get a converted message
     * that makes sense. For instance, the Meta type of a Results element (if
     * there is any), must be changed from <code>application/vnd.syncml-devinf+xml</code>
     * to <code>application/vnd.syncml-devinf+wbxml</code>
     *
     * @param msg the message to encode
     *
     * @return the encoded stream of bytes (as a byte[] buffer).
     *
     * @throws Sync4jException in case of errors
     */
    public static byte[] toWBXML(SyncML msg)
    throws Sync4jException {

        String verDTD = msg.getSyncHdr().getVerDTD().getValue();

        try {

            ByteArrayOutputStream bout = new ByteArrayOutputStream();

            IBindingFactory f = BindingDirectory.getFactory(SyncML.class);
            IMarshallingContext c = f.createMarshallingContext();
            c.setIndent(0);
            c.marshalDocument(msg, "UTF-8", null, bout);

            String inputXml = new String(bout.toByteArray());

            return toWBXML(inputXml, verDTD);

        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
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
    public static String wbxmlToXml(final byte[] msg)
    throws Sync4jException {

        ByteArrayInputStream in = null;
        try {
            in = new ByteArrayInputStream(msg);

            SyncMLParser parser = new SyncMLParser(in);

            return parseWBXML(parser);
        } catch (Throwable t) {
            throw new Sync4jException(t.getMessage(), t);
        }
    }

    public static boolean isWellKnownNamespace(String ns) {
        return (WELL_KNOWN_NS.indexOf(',' + ns + ',') >= 0);
    }

    // --------------------------------------------------------- Private methods
    /**
     *
     * @param parser XmlParser
     * @param writer SyncMLWriter
     * @param inTag boolean[]
     * @throws IOException
     */
    private static SyncMLWriter traverseXML(XmlParser    parser,
                                            SyncMLWriter writer,
                                            boolean[]    inTag,
                                            String       verDTD) throws IOException {
        //
        // NOTE: when the namespace changes in one of the namespaces listed
        //       in WELL_KNOWN_NS, a well known document must be inserted;
        //       therefore a new inner writer is created when the tag is opened
        //       and its content flushed in the original writer when the tag is
        //       closed
        //

        boolean leave = false;

        /**
         * inTag[0]: flag for tag <Item>
         * inTag[1]: flag for tag <Meta> (inside a Item)
         * inTag[2]: flag for tag <Format> (inside a Item/Meta)
         * inTag[3]: flag for tag <Data> (inside a Item)
         * inTag[4]: flag for item with data in b64
         */
        do {
            ParseEvent event = parser.read();
            String tagName = event.getName();
            String nameSpace = null;
            switch (event.getType()) {

                case Xml.START_TAG:
                    SyncMLWriter tagWriter = null;

                    // go in Item
                    if (!inTag[0]) {
                        inTag[0] = "Item".equals(tagName);
                        inTag[3] = false; // is b64 ?
                    }

                    if (inTag[0]) {
                        // go in Item/Meta
                        if (!inTag[1]) {
                            inTag[1] = "Meta".equals(tagName);
                        }
                    }

                    if (inTag[0]) {
                        if (inTag[1]) {
                            // go in Item/Meta/Format
                            if (!inTag[2]) {
                                inTag[2] = "Format".equals(tagName);
                            }
                        }
                    }

                    if (inTag[0]) {
                        // go in Item/Data
                        if (!inTag[3]) {
                            inTag[3] = "Data".equals(tagName);
                        }
                    }
                    nameSpace = ((Tag)event).getNamespace();
                    if (isWellKnownNamespace(tagName)) {
                        tagWriter = new SyncMLWriter(nameSpace, verDTD);
                        if (writer == null) {
                            writer = tagWriter;
                        }
                    } else {
                        tagWriter = writer;
                    }

                    // see API doc of StartTag for more access methods
                    tagWriter.startTag(tagName);
                    traverseXML(parser, tagWriter, inTag, verDTD); // recursion

                    if (tagWriter != writer) {
                        tagWriter.close();
                        writer.writeOpaque(new String(tagWriter.getBytes()));
                        tagWriter = null;
                    }
                    break;

                case Xml.END_TAG:

                    // go out from the Item
                    if ("Item".equals(tagName)) {
                        if (inTag[0]) {
                            inTag[0] = false;
                        }
                    }


                    // go out from the Meta
                    if ("Meta".equals(tagName)) {
                        if (inTag[1]) {
                            inTag[1] = false;
                        }
                    }

                    // go out from the Format
                    if ("Format".equals(tagName)) {
                        if (inTag[2]) {
                            inTag[2] = false;
                        }
                    }

                    // go out from the Item/Data
                    if ("Data".equals(tagName)) {
                        if (inTag[3]) {
                            inTag[3] = false;
                            inTag[4] = false;
                        }
                    }

                    writer.endTag();
                    leave = true;
                    break;

                case Xml.END_DOCUMENT:
                    leave = true;
                    break;

                case Xml.TEXT:
                    String text = event.getText();
                    if (inTag[2]) {
                        if ("b64".equalsIgnoreCase(text)) {
                            text = "bin";
                            inTag[4] = true;
                        }
                    }

                    if (inTag[3] && inTag[4]) {
                        byte[] bytes = Base64.decode(text.getBytes());
                        writer.write(bytes);
                    } else {
                        writer.write(text);
                    }

                    break;

                case Xml.WHITESPACE:
                    break;

                default:
            }
        } while (!leave);

        return writer;
    }

    private static String parseWBXML(SyncMLParser parser) throws IOException {
        boolean[] inTag = new boolean[9];
        return parseWBXML(parser, inTag);
    }

    private static String parseWBXML(SyncMLParser parser, boolean[] inTag) throws IOException{

        /**
         * inTag[0]: flag for tag <Put> or <Results>
         * inTag[1]: flag for tag <Item>
         * inTag[2]: flag for tag <Data> (inside a Item)
         * inTag[3]: flag for tag <Cred>
         * inTag[4]: set if tag Meta inside Cred contains "b64"
         * inTag[5]: set if tag Meta inside Cred contains "auth-md5"
         * inTag[6]: flag for tag <Iten>
         * inTag[7]: flag for tag <Data> (inside a Item)
         * inTag[8]: set if tag Meta inside Item contains "bin"
         */

        StringBuffer buf=new StringBuffer();
        boolean leave = false;

        String tagName = null;
        String text    = null;

        do {
            ParseEvent event = parser.read();
            tagName = event.getName();
            switch (event.getType()) {

                case Xml.START_TAG:
                    tagName = event.getName();

                    buf.append("<");
                    buf.append(tagName);
                    List attrs=event.getAttributes();
                    if(attrs!=null){
                        for(int i=0;i<attrs.size();i++){
                            Attribute attr=(Attribute)attrs.get(i);
                            buf.append(" ");
                            buf.append(attr.getName());
                            buf.append("='");
                            buf.append(attr.getValue());
                            buf.append("'");
                        }
                    }
                    buf.append(">");

                    //
                    //This is util for replace the Data content if contains
                    //illegal character
                    //
                    if (!inTag[0]) {
                        inTag[0] = ("Put".equals(tagName) || "Results".equals(tagName));
                    }


                    if (!inTag[1]) {
                        inTag[1] = "Item".equals(tagName);
                    } else if (inTag[1]) {
                        inTag[2] = "Data".equals(tagName);
                    }

                    // go in Item
                    if (!inTag[6]) {
                        inTag[6] = "Item".equals(tagName);
                    }

                    if (inTag[6]) {
                        // go in Item/Data
                        if (!inTag[7]) {
                            inTag[7] = "Data".equals(tagName);
                        }
                    }

                    //
                    //This is util to establish if the auth-md5 credential are
                    //encoded in Base64
                    //
                    if (!inTag[3]) {
                        inTag[3] = "Cred".equals(tagName);
                    }

                    text = parseWBXML(parser, inTag);

                    if (inTag[6]) {
                        // go in Item/Meta
                        if ("Meta".equals(tagName)) {
                            inTag[8] = (text.indexOf("bin") >= 0);
                            text = text.replaceAll("bin", "b64");
                        }
                    }


                    if (inTag[3]) {
                        if ("Meta".equals(tagName)) {
                            inTag[4] = (text.indexOf("b64") >= 0);
                            inTag[5] = (text.indexOf("auth-md5") >= 0);
                            buf.append(text);
                            text = parseWBXML(parser, inTag);
                        }
                    }

                    buf.append(text);
                    break;

                case Xml.END_TAG:

                    if (tagName != null) {
                        if (tagName.equals("Put")) {
                            if (inTag[0]) {
                                inTag[0] = false;
                            }
                        } else if (tagName.equals("Results")) {
                            if (inTag[0]) {
                                inTag[0] = false;
                            }
                        } else if (tagName.equals("Cred")) {
                            if (inTag[3]) {
                                inTag[3] = false;
                            }
                        } else if (tagName.equals("Item")) {
                            if (inTag[6]) {
                                inTag[6] = false;
                            }
                            if (inTag[1]) {
                                inTag[1] = false;
                            }
                        } else if (tagName.equals("Data")) {
                            if (inTag[7]) {
                                inTag[7] = false;
                                inTag[8] = false;
                            }
                            if (inTag[2]) {
                                inTag[2] = false;
                            }
                        }
                    }

                    buf.append("</");
                    buf.append(event.getName());
                    buf.append(">");
                    leave = true;
                    break;

                case Xml.END_DOCUMENT:
                    leave = true;
                    break;

                case Xml.TEXT:
                    text = event.getText();

                    text = replaceDataContent(text);

                    buf.append(text);
                    break;

                case Xml.WAP_EXTENSION:
                    text = event.getText();

                    if (!inTag[0] && inTag[1] && inTag[2]) {
                        text = replaceDataContent(text);
                    }

                    if (event instanceof WapExtensionEvent) {
                        WapExtensionEvent e = (WapExtensionEvent)event;
                        Object content = e.getContent();

                        if (inTag[5] && !inTag[4] && content != null) {
                            if (content instanceof byte[]) {
                                text = new String(Base64.encode((byte[])content));
                            }
                        }
                        if (inTag[8]) {
                            if (content instanceof byte[]) {
                                text = new String(
                                           Base64.encode((byte[])content));
                            }
                        }


                        if ( ( (WapExtensionEvent)event).getId() == Wbxml.OPAQUE) {
                            text = replaceDataContent(text);
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

    /**
     * Replace not permitted characters with their HTML codification.
     *
     * @param text the data content
     *
     * @return text the data content modified
     */
    private static String replaceDataContent(String text) {
        text = StringUtils.replace(text, "&", "&amp;");
        text = StringUtils.replace(text, "<", "&lt;");
        text = StringUtils.replace(text, ">", "&gt;");
        text = StringUtils.replace(text, "\"", "&quot;");
        return text;
    }
}
