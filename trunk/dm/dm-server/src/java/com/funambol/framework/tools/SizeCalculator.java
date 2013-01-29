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


import com.funambol.framework.core.AbstractCommand;
import com.funambol.framework.core.Add;
import com.funambol.framework.core.Alert;
import com.funambol.framework.core.Anchor;
import com.funambol.framework.core.Atomic;
import com.funambol.framework.core.Authentication;
import com.funambol.framework.core.CTCap;
import com.funambol.framework.core.CTPropParam;
import com.funambol.framework.core.CTType;
import com.funambol.framework.core.CTTypeSupported;
import com.funambol.framework.core.Chal;
import com.funambol.framework.core.CmdID;
import com.funambol.framework.core.ComplexData;
import com.funambol.framework.core.ContentTypeInfo;
import com.funambol.framework.core.ContentTypeParameter;
import com.funambol.framework.core.Copy;
import com.funambol.framework.core.Cred;
import com.funambol.framework.core.DSMem;
import com.funambol.framework.core.Data;
import com.funambol.framework.core.DataStore;
import com.funambol.framework.core.Delete;
import com.funambol.framework.core.DevInf;
import com.funambol.framework.core.DevInfData;
import com.funambol.framework.core.DevInfItem;
import com.funambol.framework.core.EMI;
import com.funambol.framework.core.Exec;
import com.funambol.framework.core.Ext;
import com.funambol.framework.core.Get;
import com.funambol.framework.core.Item;
import com.funambol.framework.core.ItemizedCommand;
import com.funambol.framework.core.Map;
import com.funambol.framework.core.MapItem;
import com.funambol.framework.core.Mem;
import com.funambol.framework.core.Meta;
import com.funambol.framework.core.ModificationCommand;
import com.funambol.framework.core.NextNonce;
import com.funambol.framework.core.Put;
import com.funambol.framework.core.Replace;
import com.funambol.framework.core.ResponseCommand;
import com.funambol.framework.core.Results;
import com.funambol.framework.core.Search;
import com.funambol.framework.core.Sequence;
import com.funambol.framework.core.SessionID;
import com.funambol.framework.core.Source;
import com.funambol.framework.core.SourceRef;
import com.funambol.framework.core.Status;
import com.funambol.framework.core.Sync;
import com.funambol.framework.core.SyncBody;
import com.funambol.framework.core.SyncCap;
import com.funambol.framework.core.SyncHdr;
import com.funambol.framework.core.SyncML;
import com.funambol.framework.core.SyncType;
import com.funambol.framework.core.Target;
import com.funambol.framework.core.TargetRef;
import com.funambol.framework.core.VerDTD;
import com.funambol.framework.core.VerProto;

import java.util.ArrayList;


/**
 *  Utility class for calculate the XML size and WBXML size of framework Objects
 *
 *
 * @version $Id: SizeCalculator.java,v 1.2 2006/08/07 21:09:23 nichele Exp $
 */
public class SizeCalculator {

    // ---------------------------------------------------------- Public methods

    // ------------------------------------------------------------------ SyncML
    /**
     * Returns the XML overhead for SyncML object
     *     sizeof(<SyncML xmlns='SYNCML:SYNCML1.1'>\n</SyncML>\n)
     *
     * @return overhead for SyncML object
     */
    public static long getXMLOverheadSyncML() {
        return 43;
    }

    /**
     * Returns the WBXML overhead for SyncML object
     *     sizeof(<SyncML xmlns='SYNCML:SYNCML1.1'>\n</SyncML>\n)
     *
     * @return overhead for SyncML object
     */
    public static long getWBXMLOverheadSyncML() {
        return 29;
    }

    /**
     * Returns the XML size of the SyncML object
     *    sizeof(<SyncML xmlns='SYNCML:SYNCML1.1'>\n) +
     *    sizeof(syncHdr)    +
     *    sizeof(syncBody)   +
     *    sizeof(</SyncML>)
     *
     * @param syncML the SyncML element
     *
     * @return size the XML size of the SyncML element
     */
    public static long getXMLSize(SyncML syncML) {
        SyncHdr  syncHdr  = syncML.getSyncHdr() ;
        SyncBody syncBody = syncML.getSyncBody();

        return 43
             + getXMLSize(syncHdr )
             + getXMLSize(syncBody)
             ;
    }

    /**
     * Returns the WBXML size of the SyncML object
     *
     * @param syncML the SyncML element
     *
     * @return size the WBXML size of the SyncML element
     */
    public static long getWBXMLSize(SyncML syncML) {
        SyncHdr  syncHdr  = syncML.getSyncHdr() ;
        SyncBody syncBody = syncML.getSyncBody();

        return 29
             + getWBXMLSize(syncHdr )
             + getWBXMLSize(syncBody)
             ;
    }

    // ----------------------------------------------------------------- SyncHdr
    /**
     * Returns the XML size of SyncHdr element as:
     *    sizeof(<SyncHdr>\n)      +
     *    if verDTD != null
     *        sizeof(verDTD)       +
     *    if verProto != null
     *        sizeof(verProto)     +
     *    if sessionID != null
     *        sizeof(<SessionID>)  +
     *    if msgId != null
     *        sizeof(<MsgID>)      +
     *        msgId.length         +
     *        sizeof(</MsgID>\n)   +
     *    if target != null
     *        sizeof(target)       +
     *    if source != null
     *        sizeof(source)       +
     *    if respURI
     *        sizeof(<RespURI>)    +
     *        sizeof(respURI)      +
     *        sizeof(</RespURI>\n) +
     *    if noResp
     *        sizeof(<NoResp></NoResp>\n)  +
     *    if cred != null
     *        sizeof(cred)         +
     *    if meta != null
     *        sizeof(meta)         +
     *    sizeof(</SyncHdr>\n)     +
     *
     * @return the XML size of this element
     */
    public static long getXMLSize(SyncHdr syncHdr) {

        VerDTD    verDTD    = syncHdr.getVerDTD()   ;
        VerProto  verProto  = syncHdr.getVerProto() ;
        SessionID sessionID = syncHdr.getSessionID();
        String    msgID     = syncHdr.getMsgID()    ;
        Target    target    = syncHdr.getTarget()   ;
        Source    source    = syncHdr.getSource()   ;
        String    respURI   = syncHdr.getRespURI()  ;
        boolean   noResp    = syncHdr.isNoResp()    ;
        Cred      cred      = syncHdr.getCred()     ;
        Meta      meta      = syncHdr.getMeta()     ;

        return 21
             + ((verDTD    != null) ? getXMLSize(verDTD)    : 0)
             + ((verProto  != null) ? getXMLSize(verProto)  : 0)
             + ((sessionID != null) ? getXMLSize(sessionID) : 0)
             + ((msgID     != null) ? 16 + msgID.length()   : 0)
             + ((target    != null) ? getXMLSize(target)    : 0)
             + ((source    != null) ? getXMLSize(source)    : 0)
             + ((respURI   != null) ? 19 + respURI.length() : 90)
             + (noResp              ? 18                    : 0)
             + ((cred      != null) ? getXMLSize(cred)      : 0)
             + ((meta      != null) ? getXMLSize(meta)      : 0)
             ;
    }

    /**
     * Returns the WBXML size of the SyncHdr object
     *
     * @param syncHdr the SyncHdr element
     *
     * @return size the WBXML size of the SyncHdr element
     */
    public static long getWBXMLSize(SyncHdr syncHdr) {
        VerDTD    verDTD    = syncHdr.getVerDTD()   ;
        VerProto  verProto  = syncHdr.getVerProto() ;
        SessionID sessionID = syncHdr.getSessionID();
        String    msgID     = syncHdr.getMsgID()    ;
        Target    target    = syncHdr.getTarget()   ;
        Source    source    = syncHdr.getSource()   ;
        String    respURI   = syncHdr.getRespURI()  ;
        boolean   noResp    = syncHdr.isNoResp()    ;
        Cred      cred      = syncHdr.getCred()     ;
        Meta      meta      = syncHdr.getMeta()     ;

        return 4
             + ((verDTD    != null) ? getWBXMLSize(verDTD)    : 0)
             + ((verProto  != null) ? getWBXMLSize(verProto)  : 0)
             + ((sessionID != null) ? getWBXMLSize(sessionID) : 0)
             + ((msgID     != null) ? 4 + msgID.length()      : 0)
             + ((target    != null) ? getWBXMLSize(target)    : 0)
             + ((source    != null) ? getWBXMLSize(source)    : 0)
             + ((respURI   != null) ? 4 + respURI.length()    : 80)
             + (noResp              ? 1                       : 0)
             + ((cred      != null) ? getWBXMLSize(cred)      : 0)
             + ((meta      != null) ? getWBXMLSize(meta)      : 0)
             ;
    }

    // ---------------------------------------------------------------- SyncBody
    /**
     * Returns the XML overhead for SyncBody object
     *     sizeof(<SyncBody>\n</SyncBody>\n)
     *
     * @return overhead for SyncBody object
     */
    public static long getXMLOverheadSyncBody() {
        return 23;
    }

    /**
     * Returns the WBXML overhead for SyncBody object
     *     sizeof(<SyncBody>\n</SyncBody>\n)
     *
     * @return overhead for SyncBody object
     */
    public static long getWBXMLOverheadSyncBody() {
        return 4;
    }

    /**
     * Returns the XML size of the SyncBody object
     *
     *    sizeof(<SyncBody>\n)              +
     *    for (i=0; i<commands.size(); i++)
     *        sizeof(commands[i])           +
     *    if final
     *        sizeof(<Final></Final>\n)     +
     *    sizeof(</SyncBody>\n)             +
     * @param syncBody the SyncBody element
     *
     * @return size the XML size of the SyncBody element
     */
    public static long getXMLSize(SyncBody syncBody) {
        ArrayList<AbstractCommand> commands = syncBody.getCommands();

        long size = 23
                  + ((syncBody.isFinalMsg()) ? 16 : 0)
                  ;

        for (int i=0; i<commands.size(); i++) {
            size += getCommandXMLSize((AbstractCommand)commands.get(i));
        }
        return size;
    }

    /**
     * Returns the WBXML size of the SyncBody object
     *
     * @param syncBody the SyncBody element
     *
     * @return size the WBXML size of the SyncBody element
     */
    public static long getWBXMLSize(SyncBody syncBody) {
        ArrayList<AbstractCommand> commands = syncBody.getCommands();

        long size = 4
                  + ((syncBody.isFinalMsg()) ? 1 : 0)
                  ;

       for (int i=0; i<commands.size(); ++i) {
           size += getCommandWBXMLSize((AbstractCommand)commands.get(i));
       }
       return size;
    }

    // ------------------------------------------------------------------ VerDTD
    /**
     * Returns the XML size of VerDTD object:
     *    sizeof(<VerDTD>)    +
     *    sizeof(value)       +
     *    sizeof(</VerDTD>\n)
     *
     * @return the XML size of VerDTD object
     */
    public static long getXMLSize(VerDTD verDTD) {
        return 18
             + verDTD.getValue().length()
             ;
    }

    /**
     * Returns the WBXML size of VerDTD object
     *
     * @return the WBXML size of VerDTD object
     */
    public static long getWBXMLSize(VerDTD verDTD) {
        return 4
             + verDTD.getValue().length()
             ;
    }

    // ---------------------------------------------------------------- VerProto
    /**
     * Returns the XML size of VerProto object:
     *    sizeof("<VerProto>") +
     *    sizeof(version)      +
     *    sizeof("</VerProto>\n")
     *
     * @return the XML size of this object
     */
    public static long getXMLSize(VerProto verProto) {
        return 22
             + verProto.getVersion().length()
             ;
    }

    /**
     * Returns the WBXML size of VerProto object
     *
     * @return the WBXML size of VerProto object
     */
    public static long getWBXMLSize(VerProto verProto) {
        return 4
             + verProto.getVersion().length()
             ;
    }

    // --------------------------------------------------------------- SessionID
    /**
     * Returns the XML size of SessionID element as:
     *    sizeof("<SessionID>") +
     *    sizeof(sessionID)     +
     *    sizeof("</SessionID>\n")
     *
     * @return the XML size of this element
     */
    public static long getXMLSize(SessionID sessionID) {
        return 24
             + sessionID.getSessionID().length()
             ;
    }

    /**
     * Returns the WBXML size of SessionID object
     *
     * @return the WBXML size of SessionID object
     */
    public static long getWBXMLSize(SessionID sessionID) {
        return 4
             + sessionID.getSessionID().length()
             ;
    }

    // ------------------------------------------------------------------ Target
    /**
     * Returns the XML size of Target element:
     *    sizeof(<Target>\n)      +
     *    if locURI != null
     *        sizeof(<LocURI>)    +
     *        sizeof(locURI)      +
     *        sizeof(</LocURI>\n) +
     *    if locName != null
     *      sizeof(<LocName>)     +
     *      sizeof(locName)       +
     *      sizeof(</LocName>\n)  +
     *    sizeof(</Target>\n)
     *
     * @return the XML size of this element
     */
    public static long getXMLSize(Target target) {
        String locURI  = target.getLocURI() ;
        String locName = target.getLocName();

        return 19
             + ((locURI != null)  ? (18 + locURI.length() ) : 0)
             + ((locName != null) ? (20 + locName.length()) : 0)
             ;
    }
    /**
     * Returns the WBXML size of this element.
     * @return the WBXML size of this element
     */
    public static long getWBXMLSize(Target target) {
        String locURI  = target.getLocURI() ;
        String locName = target.getLocName();

        return 4
             + ((locURI != null)  ? (4 + locURI.length() ) : 0)
             + ((locName != null) ? (4 + locName.length()) : 0)
             ;
    }

    // ------------------------------------------------------------------ Source
    /**
     * Returns the XML size of Source element:
     *    sizeof(<Source>\n)      +
     *    if locURI != null
     *        sizeof(<LocURI>)    +
     *        sizeof(locURI)      +
     *        sizeof(</LocURI>\n) +
     *    if locName != null
     *      sizeof(<LocName>)     +
     *      sizeof(locName)       +
     *      sizeof(</LocName>\n)  +
     *    sizeof(</Source>\n)
     *
     * @return the XML size of this element
     */
    public static long getXMLSize(Source source) {
        String locURI  = source.getLocURI() ;
        String locName = source.getLocName();

        return 19
             + ((locURI != null)  ? (18 + locURI.length() ) : 0)
             + ((locName != null) ? (20 + locName.length()) : 0)
             ;
    }
    /**
     * Returns the WBXML size of this element.
     * @return the WBXML size of this element
     */
    public static long getWBXMLSize(Source source) {
        String locURI  = source.getLocURI() ;
        String locName = source.getLocName();

        return 4
             + ((locURI != null)  ? (4 + locURI.length() ) : 0)
             + ((locName != null) ? (4 + locName.length()) : 0)
             ;
    }

    // -------------------------------------------------------------------- Cred
    /**
     * Returns the XML size of Cred element as:
     *    sizeof(<Cred>\n)      +
     *    if meta != null
     *      sizeof(meta)        +
     *    if data != null
     *        sizeof(<Data>)    +
     *        sizeof(data)      +
     *        sizeof(</Data>\n) +
     *    sizeof(</Cred>\n)
     *
     * @return the XML size of this element
     */
    public static long getXMLSize(Cred cred) {
        Authentication auth = cred.getAuthentication();
        Meta   meta = auth.getMeta();
        String data = cred.getData();

        return 15
             + ((meta != null) ? getXMLSize(meta)   : 0)
             + ((data != null) ? 14 + data.length() : 0)
             ;
    }

    /**
     * Returns the WBXML size of this element.
     * @return the WBXML size of this element
     */
    public static long getWBXMLSize(Cred cred) {
        Authentication auth = cred.getAuthentication();
        Meta   meta = auth.getMeta();
        String data = cred.getData();

        return 4
             + ((meta != null) ? getWBXMLSize(meta) : 0)
             + ((data != null) ? 4 + data.length()  : 0)
             ;
    }

    // -------------------------------------------------------------------- Meta
    /**
     * Returns the XML size of Meta object.
     *    sizeof(<Meta>\n)                               +
     *    if format != null
     *        sizeof(<Format>)                        +
     *        sizeof(format)                          +
     *        sizeof(</Format>\n)                     +
     *    if type != null
     *        sizeof(<Type>)                          +
     *        sizeof(type)                            +
     *        sizeof(</Type>\n)                       +
     *    if mark != null
     *        sizeof(mark)                            +
     *    if size != null
     *        sizeof(<Size>)                          +
     *        sizeof(size)                            +
     *        sizeof(</Size>\n)                       +
     *    if anchor != null
     *        sizeof(anchor)                          +
     *    if version != null
     *        sizeof(<Version>)                       +
     *        sizeof(version)                         +
     *        sizeof(</Version>\n)                    +
     *    if nextNonce != null
     *        sizeof(nextNonce)                       +
     *    if maxMsgSize != null
     *        sizeof(<MaxMsgSize>)                    +
     *        sizeof(maxMsgSize)                      +
     *        sizeof(</MaxMsgSize>\n)                 +
     *    if maxObjSize != null
     *        sizeof(<MaxObjSize>)                    +
     *        sizeof(maxObjSize)                      +
     *        sizeof(</MaxObjSize>\n)                 +
     *    for (i=0; emi != null && i<emi.size(); i++)
     *        sizeof(emi[i]                           +
     *    if mem != null
     *        sizeof(mem)                             +
     *    sizeof(</Meta>\n)
     * @return the XML size of Meta object
     */
    public static long getXMLSize(Meta meta) {
        long sizeMeta = 0;

        String format       = meta.getFormat()    ;
        String type         = meta.getType()      ;
        String mark         = meta.getMark()      ;
        Long size           = meta.getSize()      ;
        Anchor anchor       = meta.getAnchor()    ;
        String version      = meta.getVersion()   ;
        NextNonce nextNonce = meta.getNextNonce() ;
        Long maxMsgSize     = meta.getMaxMsgSize();
        Long maxObjSize     = meta.getMaxObjSize();
        ArrayList<EMI> emi       = meta.getEMI()       ;
        Mem mem             = meta.getMem()       ;

        sizeMeta = 37
                 + ((format    != null) ? 18 + format.length()              : 0)
                 + ((type      != null) ? 14 + type.length()                : 0)
                 + ((mark      != null) ? 14 + mark.length()                : 0)
                 + ((size      != null) ? 14 + String.valueOf(size).length(): 0)
                 + ((anchor    != null) ? getXMLSize(anchor)                : 0)
                 + ((version   != null) ? 20 + version.length()             : 0)
                 + ((nextNonce != null) ? getXMLSize(nextNonce)             : 0)
                 + ((maxMsgSize!= null) ? 26 + String.valueOf(maxMsgSize).length() : 0)
                 + ((maxObjSize!= null) ? 26 + String.valueOf(maxObjSize).length() : 0)
                 + ((mem       != null) ? getXMLSize(mem)                   : 0)
                 ;

	for (int i=0; emi != null && i < emi.size(); i++) {
            sizeMeta += getXMLSize(emi.get(i));
        }

       return sizeMeta;
    }



    /**
     * Returns the WBXML size of this object.
     *
     * @return the WBXML size of this object
     */
    public static long getWBXMLSize(Meta meta) {
        long sizeMeta = 0;

        String format       = meta.getFormat()    ;
        String type         = meta.getType()      ;
        String mark         = meta.getMark()      ;
        Long size           = meta.getSize()      ;
        Anchor anchor       = meta.getAnchor()    ;
        String version      = meta.getVersion()   ;
        NextNonce nextNonce = meta.getNextNonce() ;
        Long maxMsgSize     = meta.getMaxMsgSize();
        Long maxObjSize     = meta.getMaxObjSize();
        ArrayList<EMI> emi       = meta.getEMI()       ;
        Mem mem             = meta.getMem()       ;

        sizeMeta = 4
                 + ((format    != null) ? 4 + format.length()               : 0)
                 + ((type      != null) ? 4 + type.length()                 : 0)
                 + ((mark      != null) ? 4 + mark.length()                 : 0)
                 + ((size      != null) ? 4 + String.valueOf(size).length() : 0)
                 + ((anchor    != null) ? getWBXMLSize(anchor)              : 0)
                 + ((version   != null) ? 4 + version.length()              : 0)
                 + ((nextNonce != null) ? getWBXMLSize(nextNonce)           : 0)
                 + ((maxMsgSize!= null) ? 4 + String.valueOf(maxMsgSize).length() : 0)
                 + ((maxObjSize!= null) ? 4 + String.valueOf(maxObjSize).length() : 0)
                 + ((mem       != null) ? getWBXMLSize(mem)                 : 0)
                 ;

	for (int i=0; emi != null && i < emi.size(); i++) {
            sizeMeta += getWBXMLSize(emi.get(i));
        }

       return sizeMeta;
    }

    // ------------------------------------------------------------------ Anchor
    /**
     * Returns the XML size of Anchor element as:
     *    sizeof(<Anchor xmlns='syncml:metinf'>\n) +
     *    if last != null
     *        sizeof(<Last>)                       +
     *        sizeof(last)                         +
     *        sizeof(</Last>\n)                    +
     *    if next != null
     *        sizeof(<Next>)                       +
     *        sizeof(next)                         +
     *        sizeof(</Next>\n)                    +
     *    sizeof(</Anchor>\n)                      +
     *
     * @return the XML size of this element
     */
    public static long getXMLSize(Anchor anchor) {
        String last = anchor.getLast();
        String next = anchor.getNext();

        return 41
             + ((last != null) ? 14 + last.length() : 0)
             + ((next != null) ? 14 + next.length() : 0)
             ;
    }

    /**
     * Returns the WBXML size of this element.
     *
     * @return the WBXML size of this element
     */
    public static long getWBXMLSize(Anchor anchor) {
        String last = anchor.getLast();
        String next = anchor.getNext();

        return 25
             + ((last != null) ? 4 + last.length() : 0)
             + ((next != null) ? 4 + next.length() : 0)
             ;
    }

    // --------------------------------------------------------------------- EMI
    /**
     * Returns the XML size of EMI element as:
     *    sizeof(<EMI>)   +
     *    sizeof(value)   +
     *    sizeof(</EMI>\n)
     *
     * @return the XML size of this element
     */
    public static long getXMLSize(EMI emi) {
        return 12
             + emi.getValue().length()
             ;
    }

    /**
     * Returns the WBXML size of EMI element.
     *
     * @return the WBXML size of EMI element
     */
    public static long getWBXMLSize(EMI emi) {
        return 4
             + emi.getValue().length()
             ;
    }

    // --------------------------------------------------------------- NextNonce
    /**
     * Returns the XML size of NextNonce element as:
     *    sizeof(<NextNonce>)    +
     *    sizeof(value)          +
     *    sizeof(</NextNonce>\n)
     *
     * @return the XML size of NextNonce element
     */
    public static long getXMLSize(NextNonce nextNonce) {
        return 24
             + nextNonce.getValueAsBase64().length()
             ;
    }

    /**
     * Returns the WBXML size of NextNonce element.
     *
     * @return the WBXML size of NextNonce element
     */
    public static long getWBXMLSize(NextNonce nextNonce) {
        return 4
             + nextNonce.getValueAsBase64().length()
             ;
    }

    // --------------------------------------------------------------------- Mem
    /**
     * Returns the XML size of Mem element as:
     *    sizeof("<Mem>\n")                 +
     *    if sharedMem
     *        sizeof("<Shared></Shared>\n") +
     *    if freeMem
     *        sizeof("<FreeMem>")           +
     *        sizeof(freeMem)               +
     *        sizeof("</FreeMem>\n")        +
     *    if freeID
     *        sizeof("<FreeID>")            +
     *        sizeof(freeID)                +
     *        sizeof("</FreeID>\n")         +
     *    sizeof("</Mem>\n")
     *
     * @return the XML size of Mem element
     */
    public static long getXMLSize(Mem mem) {
        boolean sharedMem = mem.isSharedMem();
        long freeMem      = mem.getFreeMem() ;
        long freeID       = mem.getFreeID()  ;

        return 13
             + ((sharedMem)    ? 18                                    : 0)
             + ((freeMem != 0) ? 20 + String.valueOf(freeMem).length() : 0)
             + ((freeID  != 0) ? 18 + String.valueOf(freeID).length()  : 0)
             ;
    }

    /**
     * Returns the WBXML size of Mem object.
     *
     * @return the WBXML size of Mem object
     */
    public static long getWBXMLSize(Mem mem) {
        boolean sharedMem = mem.isSharedMem();
        long freeMem      = mem.getFreeMem() ;
        long freeID       = mem.getFreeID()  ;

        return 4
             + ((sharedMem)    ? 1                                    : 0)
             + ((freeMem != 0) ? 4 + String.valueOf(freeMem).length() : 0)
             + ((freeID  != 0) ? 4 + String.valueOf(freeID).length()  : 0)
             ;
    }

    // --------------------------------------------------------- AbstractCommand
    /**
     * Gets the XML size of all element of AbstractCommand
     *    if cmdID != null
     *        sizeof(cmdID)               +
     *    if noResp
     *        sizeof(<NoResp></NoResp>\n) +
     *    if cred != null
     *        sizeof(cred)                +
     * @return the XML size of all element of AbstractCommand
     */
    public static long getXMLSize(AbstractCommand command) {
        CmdID cmdID = command.getCmdID();
        Cred  cred  = command.getCred() ;

        return ((cmdID != null)      ? getXMLSize(cmdID) : 0)
             + ((command.isNoResp()) ? 18                : 0)
             + ((cred  != null)      ? getXMLSize(cred)  : 0)
             ;
    }

    /**
     * Returns the WBXML size of AbstractCommand object.
     * @return the WBXML size of AbstractCommand object
     */
    public static long getWBXMLSize(AbstractCommand command) {
        CmdID cmdID = command.getCmdID();
        Cred  cred  = command.getCred() ;

        return ((cmdID != null)      ? getWBXMLSize(cmdID) : 0)
             + ((command.isNoResp()) ? 1                   : 0)
             + ((cred  != null)      ? getWBXMLSize(cred)  : 0)
             ;
    }

    /**
     * Gets the XML size of the command
     *
     * @return the XML size of the command
     */
    public static long getCommandXMLSize(AbstractCommand command) {
        long size = 0;
        if (command instanceof Add) {
            size = getXMLSize((Add)command);
        } else if (command instanceof Alert) {
            size = getXMLSize((Alert)command);
        } else if (command instanceof Atomic) {
            size = getXMLSize((Atomic)command);
        } else if (command instanceof Copy) {
            size = getXMLSize((Copy)command);
        } else if (command instanceof Delete) {
            size = getXMLSize((Delete)command);
        } else if (command instanceof Exec) {
            size = getXMLSize((Exec)command);
        } else if (command instanceof Get) {
            size = getXMLSize((Get)command);
        } else if (command instanceof Map) {
            size = getXMLSize((Map)command);
        } else if (command instanceof Put) {
            size = getXMLSize((Put)command);
        } else if (command instanceof Replace) {
            size = getXMLSize((Replace)command);
        } else if (command instanceof Results) {
            size = getXMLSize((Results)command);
        } else if (command instanceof Search) {
            size = getXMLSize((Search)command);
        } else if (command instanceof Sequence) {
            size = getXMLSize((Sequence)command);
        } else if (command instanceof Status) {
            size = getXMLSize((Status)command);
        } else if (command instanceof Sync) {
            size = getXMLSize((Sync)command);
        }

        return size;
    }

    /**
     * Gets the XML size of the command
     *
     * @return the XML size of the command
     */
    public static long getCommandWBXMLSize(AbstractCommand command) {
        long size = 0;
        if (command instanceof Add) {
            size = getWBXMLSize((Add)command);
        } else if (command instanceof Alert) {
            size = getWBXMLSize((Alert)command);
        } else if (command instanceof Atomic) {
            size = getWBXMLSize((Atomic)command);
        } else if (command instanceof Copy) {
            size = getWBXMLSize((Copy)command);
        } else if (command instanceof Delete) {
            size = getWBXMLSize((Delete)command);
        } else if (command instanceof Exec) {
            size = getWBXMLSize((Exec)command);
        } else if (command instanceof Get) {
            size = getWBXMLSize((Get)command);
        } else if (command instanceof Map) {
            size = getWBXMLSize((Map)command);
        } else if (command instanceof Put) {
            size = getWBXMLSize((Put)command);
        } else if (command instanceof Replace) {
            size = getWBXMLSize((Replace)command);
        } else if (command instanceof Results) {
            size = getWBXMLSize((Results)command);
        } else if (command instanceof Search) {
            size = getWBXMLSize((Search)command);
        } else if (command instanceof Sequence) {
            size = getWBXMLSize((Sequence)command);
        } else if (command instanceof Status) {
            size = getWBXMLSize((Status)command);
        } else if (command instanceof Sync) {
            size = getWBXMLSize((Sync)command);
        }

        return size;
    }

    // ------------------------------------------------------------------- CmdID
    /**
     * Returns the XML size of CmdID element as:
     *    sizeof(<CmdID>)    +
     *    sizeof(cmdID)      +
     *    sizeof(</CmdID>\n)
     *
     * @return the XML size of CmdID element
     */
    public static long getXMLSize(CmdID cmdID) {
        return 16
             + cmdID.getCmdID().length();
    }

    /**
     * Returns the WBXML size of CmdID element.
     * @return the WBXML size of CmdID element
     */
    public static long getWBXMLSize(CmdID cmdID) {
        return 2
             + cmdID.getCmdID().length();
    }

    // --------------------------------------------------------------------- Add
    /**
     * Returns the XML size of Add element as:
     *    sizeof(<Add>\n)                                     +
     *    if cmdID != null
     *        sizeof(cmdID)                                   +
     *    if noResp
     *        sizeof(<NoResp></NoResp>\n)                     +
     *    if cred != null
     *        sizeof(cred)                                    +
     *    if meta != null
     *      sizeof(meta)                                      +
     *    for (int i=0; items != null && i<items.size(); i++)
     *      sizeof(items[i])                                  +
     *    sizeof(</Add>\n)
     *
     * @return the XML size of Add element
     */
    public static long getXMLSize(Add add) {
        Meta meta       = add.getMeta() ;
        ArrayList<Item> items = add.getItems();

        long size = 13
                  + getXMLSize((AbstractCommand)add)
                  + ((meta != null) ? getXMLSize(meta) : 0)
                  ;
        for (int i=0; items != null && i<items.size(); i++) {
            size += getXMLSize(items.get(i));
        }
        return size;
    }

    /**
     * Returns the WBXML size of Add element.
     *
     * @return the WBXML size of Add element
     */
    public static long getWBXMLSize(Add add) {
        Meta meta       = add.getMeta() ;
        ArrayList<Item> items = add.getItems();

        long size = 4
                  + getWBXMLSize((AbstractCommand)add)
                  + ((meta != null) ? getWBXMLSize(meta) : 0)
                  ;
        for (int i=0; items != null && i<items.size(); i++) {
            size += getWBXMLSize(items.get(i));
        }
        return size;
    }

    // -------------------------------------------------------------------- Item
    /**
     * Returns the XML size of Item element as:
     *    sizeof(<Item>\n)                    +
     *    if target != null
     *        sizeof(target)                  +
     *    if source != null
     *        sizeof(source)                  +
     *    if meta != null
     *        sizeof(meta)                    +
     *    if data != null
     *        sizeof(data)                    +
     *    if moreData
     *        sizeof(<MoreData></MoreData>\n) +
     *    sizeof(</Item>\n)
     *
     * @return the XML size of Item element
     */
    public static long getXMLSize(Item item) {
        Target      target = item.getTarget();
        Source      source = item.getSource();
        Meta        meta   = item.getMeta()  ;
        ComplexData data   = item.getData()  ;

        return 15
             + ((target != null)    ? getXMLSize(target) : 0)
             + ((source != null)    ? getXMLSize(source) : 0)
             + ((meta != null)      ? getXMLSize(meta)   : 0)
             + ((data != null)      ? getXMLSize(data)   : 0)
             + ((item.isMoreData()) ? 22                 : 0)
             ;
    }

    /**
     * Returns the WBXML size of Item element.
     * @return the WBXML size of Item element
     */
    public static long getWBXMLSize(Item item) {
        Target      target = item.getTarget();
        Source      source = item.getSource();
        Meta        meta   = item.getMeta()  ;
        ComplexData data   = item.getData()  ;

        return 4
             + ((target != null)    ? getWBXMLSize(target) : 0)
             + ((source != null)    ? getWBXMLSize(source) : 0)
             + ((meta != null)      ? getWBXMLSize(meta)   : 0)
             + ((data != null)      ? getWBXMLSize(data)   : 0)
             + ((item.isMoreData()) ? 1                    : 0)
             ;
    }

    // ------------------------------------------------------------- ComplexData
    /**
     * Returns the XML size of ComplexData element as:
     *    sizeof(<Data>\n)   +
     *    if data != null
     *        sizeof(data)   +
     *    if anchor != null
     *        sizeof(anchor) +
     *    if devinf != null
     *        sizeof(devinf) +
     *    sizeof(</Data>\n)
     * @return the XML size of ComplexData element
     */
    public static long getXMLSize(ComplexData complexData) {
        String data   = complexData.getData()  ;
        Anchor anchor = complexData.getAnchor();
        DevInf devInf = complexData.getDevInf();

        return 15
             + ((data   != null) ? data.length()      :0)
             + ((anchor != null) ? getXMLSize(anchor) :0)
             + ((devInf != null) ? getXMLSize(devInf) :0)
             ;
    }

    /**
     * Returns the WBXML size of ComplexData element.
     *
     * @return the WBXML size of ComplexData element
     */
    public static long getWBXMLSize(ComplexData complexData) {
        String data   = complexData.getData()  ;
        Anchor anchor = complexData.getAnchor();
        DevInf devInf = complexData.getDevInf();

        return 4
             + ((data   != null) ? data.length()        :0)
             + ((anchor != null) ? getWBXMLSize(anchor) :0)
             + ((devInf != null) ? getWBXMLSize(devInf) :0)
             ;
    }

    // -------------------------------------------------------------------- Data
    /**
     * Returns the XML size of Data element as:
     *    sizeof(<Data>)    +
     *    sizeof(data)       +
     *    sizeof(</Data>\n)
     *
     * @return the XML size of Data element
     */
    public static long getXMLSize(Data data) {
        return 14
             + data.getData().length();
    }

    /**
     * Returns the WBXML size of Data element.
     *
     * @return the WBXML size of Data element
     */
    public static long getWBXMLSize(Data data) {
        return 4
             + data.getData().length();
    }

    // -------------------------------------------------------------- DevInfData
    /**
     * Returns the XML size of DevInfData element as:
     *    sizeof(<Data>\n)   +
     *    id devInf != null
     *        sizeof(devInf) +
     *    sizeof(</Data>\n)
     *
     * @return the XML size of DevInfData element
     */
    public static long getXMLSize(DevInfData devInfData) {
        DevInf devInf = devInfData.getDevInf();
        return 15
             + ((devInf != null) ? getXMLSize(devInf) : 0)
             ;
    }

    /**
     * Returns the WBXML size of DevInfData element.
     *
     * @return the WBXML size of DevInfData element
     */
    public static long getWBXMLSize(DevInfData devInfData) {
        DevInf devInf = devInfData.getDevInf();
        return 4
             + ((devInf != null) ? getWBXMLSize(devInf) : 0)
             ;
    }

    // ------------------------------------------------------------------ DevInf
    /**
     * Returns the XML size of DevInf element as:
     *    sizeof(<DevInf xmlns='syncml:devinf'>\n)                        +
     *    if verDTD != null
     *        sizeof(verDTD)                                              +
     *    if man != null
     *        sizeof(<Man>)                                               +
     *        sizeof(man)                                                 +
     *        sizeof(</Man>\n)                                            +
     *    if mod != null
     *        sizeof(<Mod>)                                               +
     *        sizeof(mod)                                                 +
     *        sizeof(</Mod>\n)                                            +
     *    if oem != null
     *        sizeof(<OEM>)                                               +
     *        sizeof(oem)                                                 +
     *        sizeof(</OEM>\n)                                            +
     *    if fwV != null
     *        sizeof(<FwV>)                                               +
     *        sizeof(fwV)                                                 +
     *        sizeof(</FwV>\n)                                            +
     *    if swV != null
     *        sizeof(<SwV>)                                               +
     *        sizeof(swV)                                                 +
     *        sizeof(</SwV>\n)                                            +
     *    if hwV != null
     *        sizeof(<HwV>)                                               +
     *        sizeof(hwV)                                                 +
     *        sizeof(</HwV>\n)                                            +
     *    if devID != null
     *        sizeof(<DevID>)                                             +
     *        sizeof(devID)                                               +
     *        sizeof(</DevID>\n)                                          +
     *    if devTyp != null
     *        sizeof(<DevTyp>)                                            +
     *        sizeof(devTyp)                                              +
     *        sizeof(</DevTyp>\n)                                         +
     *    if utc
     *        sizeof(<UTC></UTC>\n)                                       +
     *    if supportLargeObjs
     *        sizeof(<SupportLargeObjs></SupportLargeObjs>\n)             +
     *    if supportNumberOfChanges
     *        sizeof(<SupportNumberOfChanges></SupportNumberOfChanges>\n) +
     *    for (int i=0; datastores != null && i<datastores.size(); i++)
     *        sizeof(datastores[i])                                       +
     *    for (int i=0; ctCaps != null && i<ctCaps.size(); i++)
     *        sizeof(ctCaps[i])                                            +
     *    for (int i=0; exts != null && i<exts.size(); i++)
     *        sizeof(exts[i])                                              +
     *    sizeof(</DevInf>\n)
     *
     * @return the XML size of DevInf element
     */
    public static long getXMLSize(DevInf devInf) {
        VerDTD verDTD        = devInf.getVerDTD()   ;
        String man           = devInf.getMan()      ;
        String mod           = devInf.getMod()      ;
        String oem           = devInf.getOEM()      ;
        String fwV           = devInf.getFwV()      ;
        String swV           = devInf.getSwV()      ;
        String hwV           = devInf.getHwV()      ;
        String devID         = devInf.getDevID()    ;
        String devTyp        = devInf.getDevTyp()   ;
        ArrayList<DataStore> dataStores = devInf.getDataStore();
        ArrayList<CTCap> ctCaps     = devInf.getCTCap()    ;
        ArrayList<Ext> exts       = devInf.getExt()      ;

        long size = 0;

        for (int i=0; dataStores != null && i<dataStores.size(); i++) {
            size += getXMLSize(dataStores.get(i));
        }
        for (int i=0; ctCaps != null && i<ctCaps.size(); i++) {
            size += getXMLSize(ctCaps.get(i));
        }
        for (int i=0; exts != null && i<exts.size(); i++) {
            size += getXMLSize(exts.get(i));
        }

        return 41
             + ((verDTD != null)                    ? getXMLSize(verDTD)   : 0)
             + ((man    != null)                    ? 12 + man.length()    : 0)
             + ((mod    != null)                    ? 12 + mod.length()    : 0)
             + ((oem    != null)                    ? 12 + oem.length()    : 0)
             + ((fwV    != null)                    ? 12 + fwV.length()    : 0)
             + ((swV    != null)                    ? 12 + swV.length()    : 0)
             + ((hwV    != null)                    ? 12 + hwV.length()    : 0)
             + ((devID  != null)                    ? 16 + devID.length()  : 0)
             + ((devTyp != null)                    ? 18 + devTyp.length() : 0)
             + ((devInf.isUTC())                    ? 12                   : 0)
             + ((devInf.isSupportLargeObjs())       ? 38                   : 0)
             + ((devInf.isSupportNumberOfChanges()) ? 50                   : 0)
             + size;
    }

    /**
     * Returns the WBXML size of DevInf element.
     *
     * @return the WBXML size of DevInf element
     */
    public static long getWBXMLSize(DevInf devInf) {
        VerDTD verDTD        = devInf.getVerDTD()   ;
        String man           = devInf.getMan()      ;
        String mod           = devInf.getMod()      ;
        String oem           = devInf.getOEM()      ;
        String fwV           = devInf.getFwV()      ;
        String swV           = devInf.getSwV()      ;
        String hwV           = devInf.getHwV()      ;
        String devID         = devInf.getDevID()    ;
        String devTyp        = devInf.getDevTyp()   ;
        ArrayList<DataStore> dataStores = devInf.getDataStore();
        ArrayList<CTCap> ctCaps     = devInf.getCTCap()    ;
        ArrayList<Ext> exts       = devInf.getExt()      ;

        long size = 0;

        for (int i=0; dataStores != null && i<dataStores.size(); i++) {
            size += getWBXMLSize(dataStores.get(i));
        }
        for (int i=0; ctCaps != null && i<ctCaps.size(); i++) {
            size += getWBXMLSize(ctCaps.get(i));
        }
        for (int i=0; exts != null && i<exts.size(); i++) {
            size += getWBXMLSize(exts.get(i));
        }

        return 4
             + ((verDTD != null)                    ? getWBXMLSize(verDTD) : 0)
             + ((man    != null)                    ? 4 + man.length()     : 0)
             + ((mod    != null)                    ? 4 + mod.length()     : 0)
             + ((oem    != null)                    ? 4 + oem.length()     : 0)
             + ((fwV    != null)                    ? 4 + fwV.length()     : 0)
             + ((swV    != null)                    ? 4 + swV.length()     : 0)
             + ((hwV    != null)                    ? 4 + hwV.length()     : 0)
             + ((devID  != null)                    ? 4 + devID.length()   : 0)
             + ((devTyp != null)                    ? 4 + devTyp.length()  : 0)
             + ((devInf.isUTC())                    ? 1                    : 0)
             + ((devInf.isSupportLargeObjs())       ? 1                    : 0)
             + ((devInf.isSupportNumberOfChanges()) ? 1                    : 0)
             + size;
    }

    // --------------------------------------------------------------- DataStore
    /**
     * Returns the XML size of DataStore element as:
     *    sizeof(<DataStore>\n)                               +
     *    if sourceRef != null
     *        sizeof(sourceRef)                               +
     *    if displayName != null && displayName.length() != 0
     *        sizeof(<DisplayName>)                           +
     *        sizeof(displayName)                             +
     *        sizeof(</DisplayName>\n)                        +
     *    if maxGUIDSize > 0
     *        sizeof(<MaxGUIDSize>)                           +
     *        sizeof(maxGUIDSize)                             +
     *        sizeof(</MaxGUIDSize>\n)                        +
     *    if rxPref != null
     *        sizeof(<Rx-Pref>\n)                             +
     *        sizeof(rxPref)                                  +
     *        sizeof(</Rx-Pref>\n)                            +
     *    for (int i=0; rxs != null && i<rxs.size(); i++)
     *      sizeof(<Rx>\n)                                    +
     *      sizeof(rxs[i])                                    +
     *      sizeof(</Rx>\n)                                   +
     *    if txPref != null
     *        sizeof(<Tx-Pref>\n)                             +
     *        sizeof(txPref)                                  +
     *        sizeof(</Tx-Pref>\n)                            +
     *    for (int i=0; txs != null && i<txs.size(); i++)
     *      sizeof(<Tx>\n)                                    +
     *      sizeof(txs[i])                                    +
     *      sizeof(</Tx>\n)                                   +
     *    if dsMem != null
     *      sizeof(dsMem)                                     +
     *    if syncCap != null
     *        sizeof(syncCap)                                 +
     *    sizeof(</DataStore>\n)
     *
     * @return the XML size of DataStore element
     */
    public static long getXMLSize(DataStore dataStore) {
        SourceRef       sourceRef   = dataStore.getSourceRef()  ;
        String          displayName = dataStore.getDisplayName();
        long            maxGUIDSize = dataStore.getMaxGUIDSize();
        ContentTypeInfo rxPref      = dataStore.getRxPref()     ;
        ArrayList<ContentTypeInfo>       rxs         = dataStore.getRx()         ;
        ContentTypeInfo txPref      = dataStore.getTxPref()     ;
        ArrayList<ContentTypeInfo>       txs         = dataStore.getTx()         ;
        DSMem           dsMem       = dataStore.getDSMem()      ;
        SyncCap         syncCap     = dataStore.getSyncCap()    ;

        long size = 0;

        for (int i=0; rxs != null && i<rxs.size(); i++) {
            size += 11 + getXMLSize(rxs.get(i));
        }
        for (int i=0; txs != null && i<txs.size(); i++) {
            size += 11 + getXMLSize(txs.get(i));
        }

        return 25
             + ((sourceRef   != null) ? getXMLSize(sourceRef)            : 0)
             + ((displayName != null && displayName.length() != 0)
                             ? 28 + displayName.length()                 : 0)
             + ((maxGUIDSize > 0)
                             ? 28 + String.valueOf(maxGUIDSize).length() : 0)
             + ((rxPref      != null) ? 21 + getXMLSize(rxPref)          : 0)
             + ((txPref      != null) ? 21 + getXMLSize(txPref)          : 0)
             + ((dsMem       != null) ? getXMLSize(dsMem)                : 0)
             + ((syncCap     != null) ? getXMLSize(syncCap)              : 0)
             + size
             ;
    }

    /**
     * Returns the WBXML size of DataStore element.
     *
     * @return the WBXML size of DataStore element
     */
    public static long getWBXMLSize(DataStore dataStore) {
        SourceRef       sourceRef   = dataStore.getSourceRef()  ;
        String          displayName = dataStore.getDisplayName();
        long            maxGUIDSize = dataStore.getMaxGUIDSize();
        ContentTypeInfo rxPref      = dataStore.getRxPref()     ;
        ArrayList<ContentTypeInfo>       rxs         = dataStore.getRx()         ;
        ContentTypeInfo txPref      = dataStore.getTxPref()     ;
        ArrayList<ContentTypeInfo>       txs         = dataStore.getTx()         ;
        DSMem           dsMem       = dataStore.getDSMem()      ;
        SyncCap         syncCap     = dataStore.getSyncCap()    ;

        long size = 0;

        for (int i=0; rxs != null && i<rxs.size(); i++) {
            size += 4 + getWBXMLSize(rxs.get(i));
        }
        for (int i=0; txs != null && i<txs.size(); i++) {
            size += 4 + getWBXMLSize(txs.get(i));
        }

        return 4
             + ((sourceRef   != null) ? getWBXMLSize(sourceRef)           : 0)
             + ((displayName != null && displayName.length() != 0)
                             ? 1 + displayName.length()                   : 0)
             + ((maxGUIDSize > 0)
                             ? 4 + String.valueOf(maxGUIDSize).length()   : 0)
             + ((rxPref      != null) ? 4 + getWBXMLSize(rxPref)          : 0)
             + ((txPref      != null) ? 4 + getWBXMLSize(txPref)          : 0)
             + ((dsMem       != null) ? getWBXMLSize(dsMem)               : 0)
             + ((syncCap     != null) ? getWBXMLSize(syncCap)             : 0)
             + size
             ;
    }

    // --------------------------------------------------------- ContentTypeInfo
    /**
     * Returns the XML size of ContentTypeInfo element as:
     *    if ctType != null
     *        sizeof(<CTType>)    +
     *        sizeof(ctType)      +
     *        sizeof(</CTType>\n) +
     *    if verCT != null
     *        sizeof(<VerCT>)     +
     *        sizeof(verCT)       +
     *        sizeof(</VerCT>\n)  +
     *
     * @return the XML size of ContentTypeInfo element
     */
    public static long getXMLSize(ContentTypeInfo contentTypeInfo) {
        String ctType = contentTypeInfo.getCTType();
        String verCT  = contentTypeInfo.getVerCT() ;

        return ((ctType != null) ? 18 + ctType.length() : 0)
             + ((verCT  != null) ? 16 + verCT.length()  : 0)
             ;
    }

    /**
     * Returns the WBXML size of ContentTypeInfo element.
     *
     * @return the WBXML size of ContentTypeInfo element
     */
    public static long getWBXMLSize(ContentTypeInfo contentTypeInfo) {
        String ctType = contentTypeInfo.getCTType();
        String verCT  = contentTypeInfo.getVerCT() ;

        return ((ctType != null) ? 4 + ctType.length() : 0)
             + ((verCT  != null) ? 4 + verCT.length()  : 0)
             ;
    }

    // --------------------------------------------------------------- SourceRef
    /**
     * Returns the XML size of SourceRef element as:
     *    sizeof(<SourceRef>)    +
     *    if value != null
     *        sizeof(value)      +
     *    if source != null
     *        sizeof(source)     +
     *    sizeof(</SourceRef>\n)
     *
     * @return the XML size of SourceRef element
     */
    public static long getXMLSize(SourceRef sourceRef) {
        String value  = sourceRef.getValue() ;
        Source source = sourceRef.getSource();

        return 24
             + ((value  != null) ? value.length()     : 0)
             + ((source != null) ? getXMLSize(source) : 0)
             ;
    }

    /**
     * Returns the WBXML size of SourceRef element.
     * @return the WBXML size of SourceRef element
     */
    public static long getWBXMLSize(SourceRef sourceRef) {
        String value  = sourceRef.getValue() ;
        Source source = sourceRef.getSource();

        return 4
             + ((value  != null) ? value.length()       : 0)
             + ((source != null) ? getWBXMLSize(source) : 0)
             ;
    }

    // ------------------------------------------------------------------- DSMem
    /**
     * Returns the XML size of DSMem element as:
     *    sizeof(<DSMem>\n)        +
     *    if sharedMem
     *      sizeof(<SharedMem></SharedMem>\n) +
     *    if maxMem >= 0
     *      sizeof(<MaxMem>)       +
     *      sizeof(maxMem)  +
     *      sizeof(</<MaxMem>\n)   +
     *    if maxID >= 0
     *      sizeof(<MaxID>)        +
     *      sizeof(maxID)          +
     *      sizeof(</MaxID>\n)    +
     *    sizeof(</DSMem>\n)
     *
     * @return the XML size of DSMem element
     */
    public static long getXMLSize(DSMem dsMem) {
        long maxMem = dsMem.getMaxMem();
        long maxID  = dsMem.getMaxID() ;

        return 17
             + ((dsMem.isSharedMem()) ? 24                                   :0)
             + ((maxMem>=0)           ? 18 + String.valueOf(maxMem).length() :0)
             + ((maxID >=0)           ? 16 + String.valueOf(maxID).length()  :0)
             ;
    }

    /**
     * Returns the WBXML size of DSMem element
     *
     * @return the WBXML size of DSMem element
     */
    public static long getWBXMLSize(DSMem dsMem) {
        long maxMem = dsMem.getMaxMem();
        long maxID  = dsMem.getMaxID() ;

        return 4
             + ((dsMem.isSharedMem())  ? 1                                   :0)
             + ((maxMem >=0)           ? 4 + String.valueOf(maxMem).length() :0)
             + ((maxID  >=0)           ? 4 + String.valueOf(maxID).length()  :0)
             ;
    }

    // ----------------------------------------------------------------- SyncCap
    /**
     * Returns the XML size of SyncCap element as:
     *    sizeof(<SyncCap>\n)                                       +
     *    for (i=0; syncTypes != null && i < syncTypes.size(); i++)
     *        sizeof(syncTypes[i])                                  +
     *    sizeof(</SyncCap>\n)
     *
     * @return the XML size of SyncCap element
     */
    public static long getXMLSize(SyncCap syncCap) {
        ArrayList<SyncType> syncTypes = syncCap.getSyncType();
        long size = 21;

        for (int i=0; i<syncTypes.size(); ++i) {
            size += getXMLSize(syncTypes.get(i));
        }
        return size;
    }

    /**
     * Returns the WBXML size of SyncCap object.
     *
     * @return the WBXML size of SyncCap object
     */
    public static long getWBXMLSize(SyncCap syncCap) {
        ArrayList<SyncType> syncTypes = syncCap.getSyncType();
        long size = 4;

        for (int i=0; i<syncTypes.size(); ++i) {
            size += getWBXMLSize(syncTypes.get(i));
        }
        return size;
    }

    // ---------------------------------------------------------------- SyncType
    /**
     * Returns the XML size of SyncType element as:
     *    sizeof(<SyncType>)    +
     *    sizeof(syncType)      +
     *    sizeof(</SyncType>\n)
     *
     * @return the XML size of SyncType element
     */
    public static long getXMLSize(SyncType syncType) {
        return 22
             + String.valueOf(syncType.getType()).length()
             ;
    }

    /**
     * Returns the WBXML size of SyncType object.
     *
     * @return the WBXML size of SyncType object
     */
    public static long getWBXMLSize(SyncType syncType) {
       return 4
            + String.valueOf(syncType.getType()).length()
            ;
    }

    // ------------------------------------------------------------------- CTCap
    /**
     * Returns the XML size of CTCap element as:
     *  sizeof(<CTCap>\n)                  +
     *  for (int i=0; ctTypeSupported != null && i<ctTypeSupported.size(); i++)
     *      sizeof(ctTypeSupported.get(i)) +
     *    sizeof(</CTCap>\n)
     *
     * @return the XML size of CTCap element
     */
    public static long getXMLSize(CTCap ctCap) {
        ArrayList<CTTypeSupported> ctTypeSup = ctCap.getCTTypeSupported();
        long size = 17;

        for (int i=0; ctTypeSup != null && i<ctTypeSup.size(); i++) {
            size += getXMLSize(ctTypeSup.get(i));
        }
        return size;
    }

    /**
     * Returns the WBXML size of CTCap element.
     *
     * @return the WBXML size of CTCap element
     */
    public static long getWBXMLSize(CTCap ctCap) {
        ArrayList<CTTypeSupported> ctTypeSup = ctCap.getCTTypeSupported();
        long size = 4;

        for (int i=0; ctTypeSup != null && i<ctTypeSup.size(); i++) {
            size += getWBXMLSize(ctTypeSup.get(i));
        }
        return size;
    }

    // --------------------------------------------------------- CTTypeSupported
    /**
     * Returns the XML size of CTTypeSupported element as:
     *    if ctType != null
     *        sizeof(ctType)           +
     *  for (int i=0; ctPropParams != null && i<ctPropParams.size(); i++)
     *      sizeof(ctPropParam.get(i))
     *
     * @return the XML size of CTTypeSupported element
     */
    public static long getXMLSize(CTTypeSupported ctTypeSupported) {
        CTType    ctType       = ctTypeSupported.getCTType()      ;
        ArrayList<CTPropParam> ctPropParams = ctTypeSupported.getCTPropParams();
        long size = 0;

        for (int i=0; ctPropParams != null && i<ctPropParams.size(); i++) {
            size += getXMLSize(ctPropParams.get(i));
        }
        return ((ctType != null) ? getXMLSize(ctType) : 0)
             + size
             ;
    }

    /**
     * Returns the WBXML size of this element.
     *
     * @return the WBXML size of this element
     */
    public static long getWBXMLSize(CTTypeSupported ctTypeSupported) {
        CTType    ctType       = ctTypeSupported.getCTType()      ;
        ArrayList<CTPropParam> ctPropParams = ctTypeSupported.getCTPropParams();
        long size = 0;

        for (int i=0; ctPropParams != null && i<ctPropParams.size(); i++) {
            size += getWBXMLSize(ctPropParams.get(i));
        }
        return ((ctType != null) ? getWBXMLSize(ctType) : 0)
             + size
             ;
    }

    // ------------------------------------------------------------------ CTType
    /**
     * Returns the XML size of CTType element as:
     *    sizeof(<CTType>)    +
     *    sizeof(ctType)      +
     *    sizeof(</CTType>\n)
     *
     * @return the XML size of CTType element
     */
    public static long getXMLSize(CTType ctType) {
        return 18
             + String.valueOf(ctType.getCTType()).length()
             ;
    }

    /**
     * Returns the WBXML size of CTType object.
     *
     * @return the WBXML size of CTType object
     */
    public static long getWBXMLSize(CTType ctType) {
       return 4
            + String.valueOf(ctType.getCTType()).length()
        ;
    }

    // ------------------------------------------------------------- CTPropParam
    /**
     * Returns the XML size of CTPropParam element as:
     *    sizeof(<PropName>)                     +
     *    sizeof(propName)                       +
     *    sizeof(</PropName>\n)                  +
     *    for (int i=0; valEnums != null && i<valEnums.size(); i++)
     *        sizeof(<ValEnum>)                  +
     *        sizeof(valEnums.get(i))            +
     *        sizeof(</ValEnum>\n)               +
     *    if dataType != null
     *        sizeof(<DataType>)                 +
     *        sizeof(dataType)                   +
     *        sizeof(</DataType>\n)              +
     *    if size != 0
     *        sizeof(<Size>)                     +
     *        sizeof(size)                       +
     *        sizeof(</Size>\n)                  +
     *    if displayName != null
     *        sizeof(<DisplayName>)              +
     *        sizeof(displayName)                +
     *        sizeof(</DisplayName>\n)           +
     *    for (int i=0; ctParameters != null && i<ctParameters.size(); i++)
     *        sizeof(ctParameters.get(i))
     * @return the XML size of CTPropParam element
     */
    public static long getXMLSize(CTPropParam ctPropParam) {
        String    propName     = ctPropParam.getPropName()             ;
        ArrayList<String> valEnums     = ctPropParam.getValEnum()              ;
        String    dataType     = ctPropParam.getDataType()             ;
        int       size         = ctPropParam.getSize()                 ;
        String    displayName  = ctPropParam.getDisplayName()          ;
        ArrayList<ContentTypeParameter> ctParameters = ctPropParam.getContentTypeParameters();

        long sizeCTPP = 0;
        for (int i=0; valEnums != null && i<valEnums.size(); i++) {
            sizeCTPP += 20 + valEnums.get(i).length();
        }

        for (int i=0; ctParameters!=null && i<ctParameters.size(); i++) {
            sizeCTPP += getXMLSize(ctParameters.get(i));
        }

        return ((propName    != null) ? 22 + propName.length()             : 0)
             + ((dataType    != null) ? 22 + dataType.length()             : 0)
             + ((size        != 0   ) ? 14 + String.valueOf(size).length() : 0)
             + ((displayName != null) ? 28 + displayName.length()          : 0)
             + sizeCTPP
             ;
    }

    /**
     * Returns the WBXML size of CTPropParam element.
     *
     * @return the WBXML size of CTPropParam element
     */
    public static long getWBXMLSize(CTPropParam ctPropParam) {
        String    propName     = ctPropParam.getPropName()             ;
        ArrayList<String> valEnums     = ctPropParam.getValEnum()              ;
        String    dataType     = ctPropParam.getDataType()             ;
        int       size         = ctPropParam.getSize()                 ;
        String    displayName  = ctPropParam.getDisplayName()          ;
        ArrayList<ContentTypeParameter> ctParameters = ctPropParam.getContentTypeParameters();

        long sizeCTPP = 0;
        for (int i=0; valEnums != null && i<valEnums.size(); i++) {
            sizeCTPP += 4 + valEnums.get(i).length();
        }

        for (int i=0; ctParameters!=null && i<ctParameters.size(); i++) {
            sizeCTPP += getWBXMLSize(ctParameters.get(i));
        }

        return ((propName    != null) ? 4 + propName.length()             : 0)
             + ((dataType    != null) ? 4 + dataType.length()             : 0)
             + ((size        != 0   ) ? 4 + String.valueOf(size).length() : 0)
             + ((displayName != null) ? 4 + displayName.length()          : 0)
             + sizeCTPP
             ;
    }

    // ---------------------------------------------------- ContentTypeParameter
    /**
     * Returns the XML size of ContentTypeParameter element as:
     *    sizeof(<ParamName>)                    +
     *    sizeof(paramName)                      +
     *    sizeof(</ParamName>\n)                 +
     *    for (int i=0; valEnums != null && i<valEnums.size(); i++)
     *        sizeof(<ValEnum>)                  +
     *        sizeof(valEnum.get(i))             +
     *        sizeof(</ValEnum>\n)               +
     *    if dataType != null
     *        sizeof(<DataType>)                 +
     *        sizeof(dataType)                   +
     *        sizeof(</DataType>\n)              +
     *    if size != 0
     *        sizeof(<Size>)                     +
     *        sizeof(size)                       +
     *        sizeof(</Size>\n)                  +
     *    if displayName != null
     *        sizeof(<DisplayName>)              +
     *        sizeof(displayName)                +
     *        sizeof(</DisplayName>\n)           +
     *
     * @return the XML size of CTPropParam element
     */
    public static long getXMLSize(ContentTypeParameter ctParameter) {
        String    paramName   = ctParameter.getParamName()  ;
        ArrayList<String> valEnums    = ctParameter.getValEnum()    ;
        String    dataType    = ctParameter.getDataType()   ;
        int       size        = ctParameter.getSize()       ;
        String    displayName = ctParameter.getDisplayName();
        long sizeCTP = 0;

        for (int i=0; valEnums != null && i<valEnums.size(); i++) {
            sizeCTP += 20 + valEnums.get(i).length();
        }

        return ((paramName   != null) ? 24 + paramName.length()            : 0)
             + ((dataType    != null) ? 22 + dataType.length()             : 0)
             + ((size        != 0   ) ? 14 + String.valueOf(size).length() : 0)
             + ((displayName != null) ? 28 + displayName.length()          : 0)
             + sizeCTP
             ;
    }

    /**
     * Returns the WBXML size of ContentTypeParameter element.
     *
     * @return the WBXML size of ContentTypeParameter element
     */
    public static long getWBXMLSize(ContentTypeParameter ctParameter) {
        String    paramName   = ctParameter.getParamName()  ;
        ArrayList<String> valEnums    = ctParameter.getValEnum()    ;
        String    dataType    = ctParameter.getDataType()   ;
        int       size        = ctParameter.getSize()       ;
        String    displayName = ctParameter.getDisplayName();
        long sizeCTP = 0;

        for (int i=0; valEnums != null && i<valEnums.size(); i++) {
            sizeCTP += 4 + valEnums.get(i).length();
        }

        return ((paramName   != null) ? 4 + paramName.length()            : 0)
             + ((dataType    != null) ? 4 + dataType.length()             : 0)
             + ((size        != 0   ) ? 4 + String.valueOf(size).length() : 0)
             + ((displayName != null) ? 4 + displayName.length()          : 0)
             + sizeCTP
             ;
    }

    // --------------------------------------------------------------------- Ext
    /**
     * Returns the XML size of Ext element as:
     *    sizeof(<Ext>\n)  +
     *    if name != null
     *        sizeof(<XNam>)    +
     *        sizeof(name) +
     *        sizeof(</XNam>\n) +
     *    for (int i=0; values != null && i<values.size(); i++)
     *        sizeof(<XVal>)  +
     *        sizeof(values[i])  +
     *        sizeof(</XVal>\n)  +
     *    sizeof(</Ext>\n)
     *
     * @return the XML size of Ext element
     */
    public static long getXMLSize(Ext ext) {
        String    name   = ext.getXNam();
        ArrayList<String> values = ext.getXVal();
        long size = 13;

        for (int i=0; values != null && i<values.size(); i++) {
            size += 14 + values.get(i).length();
        }

        return ((name != null) ? 14 + name.length() : 0)
             + size
             ;
    }

    /**
     * Returns the WBXML size of Ext element.
     *
     * @return the WBXML size of Ext element
     */
    public static long getWBXMLSize(Ext ext) {
        String    name   = ext.getXNam();
        ArrayList<String> values = ext.getXVal();
        long size = 4;

        for (int i=0; values != null && i<values.size(); i++) {
            size += 4 + values.get(i).length();
        }

        return ((name != null) ? 4 + name.length() : 0)
             + size
             ;
    }
    // ------------------------------------------------------------------- Alert
    /**
     * Returns the XML size of Alert element as:
     *    sizeof(<Alert>\n)                                   +
     *    if cmdID != null
     *        sizeof(cmdID)                                   +
     *    if noResp
     *        sizeof(<NoResp></NoResp>\n)                     +
     *    if cred != null
     *        sizeof(cred)                                    +
     *    if data != null
     *        sizeof(<Data>)                                  +
     *        sizeof(data)                                    +
     *        sizeof(</Data>\n)                               +
     *    for (int i=0; items != null && i<items.size(); i++)
     *        sizeof(items[i])                                +
     *    sizeof(</Alert>\n)
     *
     * @return the XML size of Alert element
     */
    public static long getXMLSize(Alert alert) {
        int       data  = alert.getData() ;
        ArrayList<Item> items = alert.getItems();

        long size = 17
                  + getXMLSize((AbstractCommand)alert)
                  + ((data != 0) ? 14 + String.valueOf(data).length() : 0)
                  ;
        for (int i=0; items != null && i<items.size(); i++) {
            size += getXMLSize(items.get(i));
        }
        return size;
    }

    /**
     * Returns the WBXML size of Alert element.
     *
     * @return the WBXML size of Alert element
     */
    public static long getWBXMLSize(Alert alert) {
        int       data  = alert.getData() ;
        ArrayList<Item> items = alert.getItems();

        long size = 4
                  + getWBXMLSize((AbstractCommand)alert)
                  + ((data != 0) ? 2 + String.valueOf(data).length() : 0)
                  ;
        for (int i=0; items != null && i<items.size(); i++) {
            size += getWBXMLSize(items.get(i));
        }
        return size;
    }

    // ------------------------------------------------------------------ Atomic
    /**
     * Returns the XML size of Atomic element as:
     *    sizeof(<Atomic>\n)                                        +
     *    if cmdID != null
     *        sizeof(cmdID)                                         +
     *    if noResp
     *        sizeof(<NoResp></NoResp>\n)                           +
     *    if cred != null
     *        sizeof(cred)                                          +
     *    if meta != null
     *        sizeof(meta)                                          +
     *    for (int i=0; commands != null && i<commands.size(); i++)
     *        sizeof(commands[i])                                   +
     *    sizeof(</Atomic>\n)
     *
     * @return the XML size of Atomic element
     */
    public static long getXMLSize(Atomic atomic) {
        Meta      meta     = atomic.getMeta()    ;
        ArrayList<AbstractCommand> commands = atomic.getCommands();

        long size = 19
                  + getXMLSize((AbstractCommand)atomic)
                  + ((meta != null) ? getXMLSize(meta) : 0)
                  ;
        for (int i=0; commands != null && i<commands.size(); i++) {
            size += getCommandXMLSize((AbstractCommand)commands.get(i));
        }
        return size;
    }

    /**
     * Returns the WBXML size of Atomic element.
     *
     * @return the WBXML size of Atomic element
     */
    public static long getWBXMLSize(Atomic atomic) {
        Meta      meta     = atomic.getMeta()    ;
        ArrayList<AbstractCommand> commands = atomic.getCommands();

        long size = 4
                  + getWBXMLSize((AbstractCommand)atomic)
                  + ((meta != null) ? getWBXMLSize(meta) : 0)
                  ;
        for (int i=0; commands != null && i<commands.size(); i++) {
            size += getCommandWBXMLSize((AbstractCommand)commands.get(i));
        }
        return size;
    }

    // -------------------------------------------------------------------- Copy
    /**
     * Returns the XML size of Copy element as:
     *    sizeof(<Copy>\n)                                    +
     *    if cmdID != null
     *        sizeof(cmdID)                                   +
     *    if noResp
     *        sizeof(<NoResp></NoResp>\n)                     +
     *    if cred != null
     *        sizeof(cred)                                    +
     *    if meta != null
     *        sizeof(meta)                                    +
     *    for (int i=0; items != null && i<items.size(); i++)
     *        sizeof(items[i])                                +
     *    sizeof(</Copy>\n)
     *
     * @return the XML size of Copy element
     */
    public static long getXMLSize(Copy copy) {
        Meta      meta  = copy.getMeta() ;
        ArrayList<Item> items = copy.getItems();

        long size = 13
                  + getXMLSize((AbstractCommand)copy)
                  + ((meta != null) ? getXMLSize(meta) : 0)
                  ;
        for (int i=0; items != null && i<items.size(); i++) {
            size += getXMLSize(items.get(i));
        }
        return size;
    }

    /**
     * Returns the WBXML size of Copy element.
     *
     * @return the WBXML size of Copy element
     */
    public static long getWBXMLSize(Copy copy) {
        Meta      meta  = copy.getMeta() ;
        ArrayList<Item> items = copy.getItems();

        long size = 4
                  + getWBXMLSize((AbstractCommand)copy)
                  + ((meta != null) ? getWBXMLSize(meta) : 0)
                  ;
        for (int i=0; items != null && i<items.size(); i++) {
            size += getWBXMLSize(items.get(i));
        }
        return size;
    }

    // ------------------------------------------------------------------ Delete
    /**
     * Returns the XML size of Delete element as:
     *    sizeof(<Delete>\n)                                  +
     *    if cmdID != null
     *        sizeof(cmdID)                                   +
     *    if noResp
     *        sizeof(<NoResp></NoResp>\n)                     +
     *    if cred != null
     *        sizeof(cred)                                    +
     *    if archive
     *        sizeof(<Archive></Archive>\n)                   +
     *    if sftDel
     *        sizeof(<SftDel></SftDel>\n)                     +
     *    if meta != null
     *        sizeof(meta)                                    +
     *    for (int i=0; items != null && i<items.size(); i++)
     *        sizeof(items[i])                                +
     *    sizeof(</Delete>\n)
     *
     * @return the XML size of Delete element
     */
    public static long getXMLSize(Delete delete) {
        Meta      meta  = delete.getMeta() ;
        ArrayList<Item> items = delete.getItems();

        long size = 20
                  + getXMLSize((AbstractCommand)delete)
                  + ((delete.isArchive()) ? 20               : 0)
                  + ((delete.isSftDel() ) ? 18               : 0)
                  + ((meta != null      ) ? getXMLSize(meta) : 0)
                  ;
        for (int i=0; items != null && i<items.size(); i++) {
            size += getXMLSize(items.get(i));
        }
        return size;
    }

    /**
     * Returns the WBXML size of Delete element.
     *
     * @return the WBXML size of Delete element
     */
    public static long getWBXMLSize(Delete delete) {
        Meta meta       = delete.getMeta() ;
        ArrayList<Item> items = delete.getItems();

        long size = 4
                  + getWBXMLSize((AbstractCommand)delete)
                  + ((delete.isArchive()) ? 1                  : 0)
                  + ((delete.isSftDel() ) ? 1                  : 0)
                  + ((meta != null      ) ? getWBXMLSize(meta) : 0)
                  ;
        for (int i=0; items != null && i<items.size(); i++) {
            size += getWBXMLSize(items.get(i));
        }
        return size;
    }

    // -------------------------------------------------------------------- Exec
    /**
     * Returns the XML size of Exec element as:
     *    sizeof(<Exec>\n)                                    +
     *    if cmdID != null
     *        sizeof(cmdID)                                   +
     *    if noResp
     *        sizeof(<NoResp></NoResp>\n)                     +
     *    if cred != null
     *        sizeof(cred)                                    +
     *    for (int i=0; items != null && i<items.size(); i++)
     *        sizeof(items[i])                                +
     *    sizeof(</Exec>\n)
     *
     * @return the XML size of Delete element
     */
    public static long getXMLSize(Exec exec) {
        ArrayList<Item> items = exec.getItems();

        long size = 14
                  + getXMLSize((AbstractCommand)exec)
                  ;
        for (int i=0; items != null && i<items.size(); i++) {
            size += getXMLSize(items.get(i));
        }
        return size;
    }

    /**
     * Returns the WBXML size of Exec element.
     *
     * @return the WBXML size of Exec element
     */
    public static long getWBXMLSize(Exec exec) {
        ArrayList<Item> items = exec.getItems();

        long size = 4
                  + getWBXMLSize((AbstractCommand)exec)
                  ;
        for (int i=0; items != null && i<items.size(); i++) {
            size += getWBXMLSize(items.get(i));
        }
        return size;
    }

    // --------------------------------------------------------------------- Get
    /**
     * Returns the XML size of Get element as:
     *    sizeof(<Get>\n)                                     +
     *    if cmdID != null
     *        sizeof(cmdID)                                   +
     *    if noResp
     *        sizeof(<NoResp></NoResp>\n)                     +
     *    if cred != null
     *        sizeof(cred)                                    +
     *    if lang != null
     *        sizeof(<Lang>)                                  +
     *        sizeof(lang)                                    +
     *        sizeof(</Lang>\n)                               +
     *    if meta != null
     *        sizeof(meta)                                    +
     *    for (int i=0; items != null && i<items.size(); i++)
     *        sizeof(items[i])                                +
     *    sizeof(</Get>\n)
     *
     * @return the XML size of Get element
     */
    public static long getXMLSize(Get get) {
        String    lang  = get.getLang();
        Meta      meta  = get.getMeta() ;
        ArrayList<Item> items = get.getItems();

        long size = 13
                  + getXMLSize((AbstractCommand)get)
                  + ((lang != null) ? 14 + lang.length() : 0)
                  + ((meta != null) ? getXMLSize(meta)   : 0)
                  ;
        for (int i=0; items != null && i<items.size(); i++) {
            size += getXMLSize(items.get(i));
        }
        return size;
    }

    /**
     * Returns the WBXML size of Get element.
     *
     * @return the WBXML size of Get element
     */
    public static long getWBXMLSize(Get get) {
        String    lang  = get.getLang();
        Meta      meta  = get.getMeta() ;
        ArrayList<Item> items = get.getItems();

        long size = 4
                  + getXMLSize((AbstractCommand)get)
                  + ((lang != null) ? 1 + lang.length() : 0)
                  + ((meta != null) ? getXMLSize(meta)  : 0)
                  ;
        for (int i=0; items != null && i<items.size(); i++) {
            size += getXMLSize(items.get(i));
        }
        return size;
    }

    // --------------------------------------------------------------------- Map
    /**
     * Returns the XML size of Map element as:
     *    sizeof(<Map>\n)                                     +
     *    if cmdID != null
     *        sizeof(cmdID)                                   +
     *    if noResp
     *        sizeof(<NoResp></NoResp>\n)                     +
     *    if cred != null
     *        sizeof(cred)                                    +
     *    if target != null
     *        sizeof(target)                                  +
     *    if source != null
     *        sizeof(source)                                  +
     *    if meta != null
     *        sizeof(meta)                                    +
     *    for (int i=0; mapItems != null && i<mapItems.size(); i++)
     *        sizeof(mapItems[i])                             +
     *    sizeof(</Map>\n)
     *
     * @return the XML size of Map element
     */
    public static long getXMLSize(Map map) {
        Target    target   = map.getTarget();
        Source    source   = map.getSource();
        Meta      meta     = map.getMeta() ;
        ArrayList<MapItem> mapItems = map.getMapItems();

        long size = 13
                  + getXMLSize((AbstractCommand)map)
                  + ((target != null) ? getXMLSize(target) : 0)
                  + ((source != null) ? getXMLSize(source) : 0)
                  + ((meta   != null) ? getXMLSize(meta)   : 0)
                  ;
        for (int i=0; mapItems != null && i<mapItems.size(); i++) {
            size += getXMLSize(mapItems.get(i));
        }
        return size;
    }

    /**
     * Returns the WBXML size of Map element.
     *
     * @return the WBXML size of Map element
     */
    public static long getWBXMLSize(Map map) {
        Target    target   = map.getTarget();
        Source    source   = map.getSource();
        Meta      meta     = map.getMeta() ;
        ArrayList<MapItem> mapItems = map.getMapItems();

        long size = 4
                  + getWBXMLSize((AbstractCommand)map)
                  + ((target != null) ? getWBXMLSize(target) : 0)
                  + ((source != null) ? getWBXMLSize(source) : 0)
                  + ((meta   != null) ? getWBXMLSize(meta)   : 0)
                  ;
        for (int i=0; mapItems != null && i<mapItems.size(); i++) {
            size += getWBXMLSize(mapItems.get(i));
        }
        return size;
    }

    // ----------------------------------------------------------------- MapItem
    /**
     * Returns the XML size of MapItem element as:
     *    sizeof(<MapItem>\n)  +
     *    if target != null
     *        sizeof(target)   +
     *    if source != null
     *        sizeof(source)   +
     *    sizeof(</MapItem>\n)
     *
     * @return the XML size of MapItem element
     */
    public static long getXMLSize(MapItem mapItem) {
        Target    target   = mapItem.getTarget();
        Source    source   = mapItem.getSource();

        return 21
             + ((target != null) ? getXMLSize(target) : 0)
             + ((source != null) ? getXMLSize(source) : 0)
             ;
    }

    /**
     * Returns the WBXML size of MapItem element.
     *
     * @return the WBXML size of MapItem element
     */
    public static long getWBXMLSize(MapItem mapItem) {
        Target    target   = mapItem.getTarget();
        Source    source   = mapItem.getSource();

        return 2
             + ((target != null) ? getWBXMLSize(target) : 0)
             + ((source != null) ? getWBXMLSize(source) : 0)
             ;
    }

    // --------------------------------------------------------------------- Put
    /**
     * Returns the XML size of Put element as:
     *    sizeof(<Put>\n)                                     +
     *    if cmdID != null
     *        sizeof(cmdID)                                   +
     *    if noResp
     *        sizeof(<NoResp></NoResp>\n)                     +
     *    if cred != null
     *        sizeof(cred)                                    +
     *    if lang != null
     *        sizeof(<Lang>)                                  +
     *        sizeof(lang)                                    +
     *        sizeof(</Lang>\n)                               +
     *    if meta != null
     *        sizeof(meta)                                    +
     *    for (int i=0; devInfItems != null && i<devInfItems.size(); i++)
     *        sizeof(devInfItems[i])                          +
     *    sizeof(</Put>\n)
     *
     * @return the XML size of Put element
     */
    public static long getXMLSize(Put put) {
        String    lang        = put.getLang();
        Meta      meta        = put.getMeta() ;
        ArrayList<Item> devInfItems = put.getItems();

        long size = 14
                  + getXMLSize((AbstractCommand)put)
                  + ((lang != null) ? 14 + lang.length() : 0)
                  + ((meta != null) ? getXMLSize(meta)   : 0)
                  ;
        for (int i=0; devInfItems != null && i<devInfItems.size(); i++) {
            size += getXMLSize((DevInfItem)devInfItems.get(i));
        }
        return size;
    }

    /**
     * Returns the WBXML size of Put element.
     *
     * @return the WBXML size of Put element
     */
    public static long getWBXMLSize(Put put) {
        String    lang        = put.getLang();
        Meta      meta        = put.getMeta() ;
        ArrayList<Item> devInfItems = put.getItems();

        long size = 4
                  + getWBXMLSize((AbstractCommand)put)
                  + ((lang != null) ? 1 + lang.length()  : 0)
                  + ((meta != null) ? getWBXMLSize(meta) : 0)
                  ;
        for (int i=0; devInfItems != null && i<devInfItems.size(); i++) {
            size += getWBXMLSize((DevInfItem)devInfItems.get(i));
        }
        return size;
    }

    // -------------------------------------------------------------- DevInfItem
     /**
     * Returns the XML size of Item element as:
     *    sizeof(<Item>\n)                    +
     *    if target != null
     *        sizeof(target)                  +
     *    if source != null
     *        sizeof(source)                  +
     *    if meta != null
     *        sizeof(meta)                    +
     *    if data != null
     *        sizeof(data)                    +
     *    sizeof(</Item>\n)
     *
     * @return the XML size of Item element
     */
    public static long getXMLSize(DevInfItem devInfItem) {
        Target     target     = devInfItem.getTarget()    ;
        Source     source     = devInfItem.getSource()    ;
        Meta       meta       = devInfItem.getMeta()      ;
        DevInfData devInfData = devInfItem.getDevInfData();

        return 15
             + ((target     != null) ? getXMLSize(target)     : 0)
             + ((source     != null) ? getXMLSize(source)     : 0)
             + ((meta       != null) ? getXMLSize(meta)       : 0)
             + ((devInfData != null) ? getXMLSize(devInfData) : 0)
             ;
    }

    /**
     * Returns the WBXML size of Item element.
     * @return the WBXML size of Item element
     */
    public static long getWBXMLSize(DevInfItem devInfItem) {
        Target     target     = devInfItem.getTarget()    ;
        Source     source     = devInfItem.getSource()    ;
        Meta       meta       = devInfItem.getMeta()      ;
        DevInfData devInfData = devInfItem.getDevInfData();

        return 4
             + ((target     != null) ? getWBXMLSize(target)     : 0)
             + ((source     != null) ? getWBXMLSize(source)     : 0)
             + ((meta       != null) ? getWBXMLSize(meta)       : 0)
             + ((devInfData != null) ? getWBXMLSize(devInfData) : 0)
             ;
    }

    // ----------------------------------------------------------------- Replace
    /**
     * Returns the XML size of Replace element as:
     *    sizeof(<Replace>\n)                                 +
     *    if cmdID != null
     *        sizeof(cmdID)                                   +
     *    if noResp
     *        sizeof(<NoResp></NoResp>\n)                     +
     *    if cred != null
     *        sizeof(cred)                                    +
     *    if meta != null
     *        sizeof(meta)                                    +
     *    for (int i=0; items != null && i<items.size(); i++)
     *        sizeof(items[i])                                +
     *    sizeof(</Replace>\n)
     *
     * @return the XML size of Replace element
     */
    public static long getXMLSize(Replace replace) {
        Meta      meta  = replace.getMeta() ;
        ArrayList<Item> items = replace.getItems();

        long size = 21
                  + getXMLSize((AbstractCommand)replace)
                  + ((meta != null) ? getXMLSize(meta) : 0)
                  ;
        for (int i=0; items != null && i<items.size(); i++) {
            size += getXMLSize(items.get(i));
        }
        return size;
    }

    /**
     * Returns the WBXML size of Replace element.
     *
     * @return the WBXML size of Replace element
     */
    public static long getWBXMLSize(Replace replace) {
        Meta      meta  = replace.getMeta() ;
        ArrayList<Item> items = replace.getItems();

        long size = 4
                  + getWBXMLSize((AbstractCommand)replace)
                  + ((meta != null) ? getWBXMLSize(meta) : 0)
                  ;
        for (int i=0; items != null && i<items.size(); i++) {
            size += getWBXMLSize(items.get(i));
        }
        return size;
    }

    // ----------------------------------------------------------------- Results
    /**
     * Returns the XML size of Results element as:
     *    sizeof(<Results>\n)                                 +
     *    if cmdID != null
     *        sizeof(cmdID)                                   +
     *    if noResp
     *        sizeof(<NoResp></NoResp>\n)                     +
     *    if cred != null
     *        sizeof(cred)                                    +
     *    if msgRef != null
     *        sizeof(<MsgRef>)                                +
     *        sizeof(msgRef)                                  +
     *        sizeof(</MsgRef>\n)                             +
     *    if cmdRef != null
     *        sizeof(<CmdRef>)                                +
     *        sizeof(cmdRef)                                  +
     *        sizeof(</CmdRef>\n)                             +
     *    if meta != null
     *        sizeof(meta)                                    +
     *    for (int i=0; targetRefs != null && i<targetRefs.size(); i++)
     *        sizeof(targetRefs[i])                           +
     *    for (int i=0; sourceRefs != null && i<sourceRefs.size(); i++)
     *        sizeof(sourceRefs[i])                           +
     *    for (int i=0; items != null && i<items.size(); i++)
     *        sizeof(items[i])                                +
     *    sizeof(</Results>\n)
     *
     * @return the XML size of Results element
     */
    public static long getXMLSize(Results results) {
        String    msgRef     = results.getMsgRef()   ;
        String    cmdRef     = results.getCmdRef()   ;
        Meta      meta       = results.getMeta()     ;
        ArrayList<TargetRef> targetRefs = results.getTargetRef();
        ArrayList<SourceRef> sourceRefs = results.getSourceRef();
        ArrayList<Item> items      = results.getItems()    ;

        long size = 21
                  + getXMLSize((AbstractCommand)results)
                  + ((msgRef != null) ? 18 + msgRef.length() : 0)
                  + ((cmdRef != null) ? 18 + cmdRef.length() : 0)
                  + ((meta   != null) ? getXMLSize(meta)     : 0)
                  ;
        for (int i=0; targetRefs != null && i<targetRefs.size(); i++) {
            size += getXMLSize(targetRefs.get(i));
        }
        for (int i=0; sourceRefs != null && i<sourceRefs.size(); i++) {
            size += getXMLSize(sourceRefs.get(i));
        }
        for (int i=0; items != null && i<items.size(); i++) {
            size += getXMLSize(items.get(i));
        }
        return size;
    }

    /**
     * Returns the WBXML size of Results element.
     *
     * @return the WBXML size of Results element
     */
    public static long getWBXMLSize(Results results) {
        String    msgRef     = results.getMsgRef()   ;
        String    cmdRef     = results.getCmdRef()   ;
        Meta      meta       = results.getMeta()     ;
        ArrayList<TargetRef> targetRefs = results.getTargetRef();
        ArrayList<SourceRef> sourceRefs = results.getSourceRef();
        ArrayList<Item> items      = results.getItems()    ;

        long size = 4
                  + getWBXMLSize((AbstractCommand)results)
                  + ((msgRef != null) ? 4 + msgRef.length() : 0)
                  + ((cmdRef != null) ? 4 + cmdRef.length() : 0)
                  + ((meta   != null) ? getWBXMLSize(meta)  : 0)
                  ;
        for (int i=0; targetRefs != null && i<targetRefs.size(); i++) {
            size += getWBXMLSize(targetRefs.get(i));
        }
        for (int i=0; sourceRefs != null && i<sourceRefs.size(); i++) {
            size += getWBXMLSize(sourceRefs.get(i));
        }
        for (int i=0; items != null && i<items.size(); i++) {
            size += getWBXMLSize(items.get(i));
        }
        return size;
    }

    // --------------------------------------------------------------- TargetRef
    /**
     * Returns the XML size of TargetRef element as:
     *    sizeof(<TargetRef>)    +
     *    if value != null
     *        sizeof(value)      +
     *    if target != null
     *        sizeof(targe)     +
     *    sizeof(</TargetRef>\n)
     *
     * @return the XML size of TargetRef element
     */
    public static long getXMLSize(TargetRef targetRef) {
        String value  = targetRef.getValue() ;
        Target target = targetRef.getTarget();

        return 24
             + ((value  != null) ? value.length()     : 0)
             + ((target != null) ? getXMLSize(target) : 0)
             ;
    }

    /**
     * Returns the WBXML size of TargetRef element.
     * @return the WBXML size of TargetRef element
     */
    public static long getWBXMLSize(TargetRef targetRef) {
        String value  = targetRef.getValue() ;
        Target target = targetRef.getTarget();

        return 4
             + ((value  != null) ? value.length()       : 0)
             + ((target != null) ? getWBXMLSize(target) : 0)
             ;
    }

    // ------------------------------------------------------------------ Search
    /**
     * Returns the XML size of Search element as:
     *    sizeof(<Search>\n)                                  +
     *    if cmdID != null
     *        sizeof(cmdID)                                   +
     *    if noResp
     *        sizeof(<NoResp></NoResp>\n)                     +
     *    if cred != null
     *        sizeof(cred)                                    +
     *    if noResults
     *        sizeof(<NoResults></NoResults>\n)               +
     *    if target != null
     *        sizeof(target)                                  +
     *    for (int i=0; sources != null && i<sources.size(); i++)
     *        sizeof(sources[i])                              +
     *    if lang != null
     *        sizeof(<Lang>)                                  +
     *        sizeof(lang)                                    +
     *        sizeof(</Lang)\n)                               +
     *    if meta != null
     *        sizeof(meta)                                    +
     *    if data != null
     *        sizeof(data)                                    +
     *    sizeof(</Search>\n)
     *
     * @return the XML size of Search element
     */
    public static long getXMLSize(Search search) {
        Target    target  = search.getTarget() ;
        ArrayList<Source> sources = search.getSources();
        String    lang    = search.getLang()   ;
        Meta      meta    = search.getMeta()   ;
        Data      data    = search.getData()   ;

        long size =
                  + getXMLSize((AbstractCommand)search)
                  + ((search.isNoResults()) ? 24                 : 0)
                  + ((target != null)       ? getXMLSize(target) : 0)
                  + ((lang   != null)       ? 14 + lang.length() : 0)
                  + ((meta   != null)       ? getXMLSize(meta)   : 0)
                  + ((data   != null)       ? getXMLSize(data)   : 0)
                  ;
        for (int i=0; sources != null && i<sources.size(); i++) {
            size += getXMLSize(sources.get(i));
        }
        return size;
    }

    /**
     * Returns the WBXML size of Search element.
     *
     * @return the WBXML size of Search element
     */
    public static long getWBXMLSize(Search search) {
        Target    target  = search.getTarget() ;
        ArrayList<Source> sources = search.getSources();
        String    lang    = search.getLang()   ;
        Meta      meta    = search.getMeta()   ;
        Data      data    = search.getData()   ;

        long size =
                  + getWBXMLSize((AbstractCommand)search)
                  + ((search.isNoResults()) ? 1                  : 0)
                  + ((target != null)       ? getXMLSize(target) : 0)
                  + ((lang   != null)       ? 1 + lang.length()  : 0)
                  + ((meta   != null)       ? getWBXMLSize(meta) : 0)
                  + ((data   != null)       ? getWBXMLSize(data) : 0)
                  ;
        for (int i=0; sources != null && i<sources.size(); i++) {
            size += getWBXMLSize(sources.get(i));
        }
        return size;
    }

    // ---------------------------------------------------------------- Sequence
    /**
     * Returns the XML size of Sequence element as:
     *    sizeof(<Sequence>\n)                                        +
     *    if cmdID != null
     *        sizeof(cmdID)                                         +
     *    if noResp
     *        sizeof(<NoResp></NoResp>\n)                           +
     *    if cred != null
     *        sizeof(cred)                                          +
     *    if meta != null
     *        sizeof(meta)                                          +
     *    for (int i=0; commands != null && i<commands.size(); i++)
     *        sizeof(commands[i])                                   +
     *    sizeof(</Sequence>\n)
     *
     * @return the XML size of Sequence element
     */
    public static long getXMLSize(Sequence sequence) {
        Meta      meta     = sequence.getMeta()    ;
        ArrayList<AbstractCommand> commands = sequence.getCommands();

        long size = 23
                  + getXMLSize((AbstractCommand)sequence)
                  + ((meta != null) ? getXMLSize(meta) : 0)
                  ;
        for (int i=0; commands != null && i<commands.size(); i++) {
            size += getCommandXMLSize((AbstractCommand)commands.get(i));
        }
        return size;
    }

    /**
     * Returns the WBXML size of Sequence element.
     *
     * @return the WBXML size of Sequence element
     */
    public static long getWBXMLSize(Sequence sequence) {
        Meta      meta     = sequence.getMeta()    ;
        ArrayList<AbstractCommand> commands = sequence.getCommands();

        long size = 4
                  + getWBXMLSize((AbstractCommand)sequence)
                  + ((meta != null) ? getWBXMLSize(meta) : 0)
                  ;
        for (int i=0; commands != null && i<commands.size(); i++) {
            size += getCommandWBXMLSize((AbstractCommand)commands.get(i));
        }
        return size;
    }

    // ------------------------------------------------------------------ Status
    /**
     * Returns the XML size of Status element as:
     *    sizeof(<Status>\n)                                  +
     *    if cmdID != null
     *        sizeof(cmdID)                                   +
     *    if msgRef != null
     *        sizeof(<MsgRef>)                                +
     *        sizeof(msgRef)                                  +
     *        sizeof(</MsgRef>\n)                             +
     *    if cmdRef != null
     *        sizeof(<CmdRef>)                                +
     *        sizeof(cmdRef)                                  +
     *        sizeof(</CmdRef>\n)                             +
     *    if cmd != null
     *        sizeof(<Cmd)                                    +
     *        sizeof(cmd)                                     +
     *        sizeof(</Cmd>\n)                                +
     *    for (int i=0; targetRefs != null && i<targetRefs.size(); i++)
     *        sizeof(targetRefs[i])                           +
     *    for (int i=0; sourceRefs != null && i<sourceRefs.size(); i++)
     *        sizeof(sourceRefs[i])                           +
     *    if cred != null
     *        sizeof(cred)                                    +
     *    if chal != null
     *        sizeof(chal)                                    +
     *    if data != null
     *        sizeof(data)                                    +
     *    for (int i=0; items != null && i<items.size(); i++)
     *        sizeof(items[i])                                +
     *    sizeof(</Status>\n)
     *
     * @return the XML size of Status element
     */
    public static long getXMLSize(Status status) {
        CmdID     cmdID      = status.getCmdID()    ;
        String    msgRef     = status.getMsgRef()   ;
        String    cmdRef     = status.getCmdRef()   ;
        String    cmd        = status.getCmd()      ;
        ArrayList<TargetRef> targetRefs = status.getTargetRef();
        ArrayList<SourceRef> sourceRefs = status.getSourceRef();
        Cred      cred       = status.getCred()     ;
        Chal      chal       = status.getChal()     ;
        Data      data       = status.getData()     ;
        ArrayList<Item> items      = status.getItems()    ;

        long size = 19
                  + ((cmdID  != null) ? getXMLSize(cmdID)    : 0)
                  + ((msgRef != null) ? 18 + msgRef.length() : 0)
                  + ((cmdRef != null) ? 18 + cmdRef.length() : 0)
                  + ((cmd    != null) ? 12 + cmd.length()    : 0)
                  + ((cred   != null) ? getXMLSize(cred)     : 0)
                  + ((chal   != null) ? getXMLSize(chal)     : 0)
                  + ((data   != null) ? getXMLSize(data)     : 0)
                  ;
        for (int i=0; targetRefs != null && i<targetRefs.size(); i++) {
            size += getXMLSize(targetRefs.get(i));
        }
        for (int i=0; sourceRefs != null && i<sourceRefs.size(); i++) {
            size += getXMLSize(sourceRefs.get(i));
        }
        for (int i=0; items != null && i<items.size(); i++) {
            size += getXMLSize(items.get(i));
        }
        return size;
    }

    /**
     * Returns the WBXML size of Status element.
     *
     * @return the WBXML size of Status element
     */
    public static long getWBXMLSize(Status status) {
        CmdID     cmdID      = status.getCmdID()    ;
        String    msgRef     = status.getMsgRef()   ;
        String    cmdRef     = status.getCmdRef()   ;
        String    cmd        = status.getCmd()      ;
        ArrayList<TargetRef> targetRefs = status.getTargetRef();
        ArrayList<SourceRef> sourceRefs = status.getSourceRef();
        Cred      cred       = status.getCred()     ;
        Chal      chal       = status.getChal()     ;
        Data      data       = status.getData()     ;
        ArrayList<Item> items      = status.getItems()    ;

        long size = 4
                  + ((cmdID  != null) ? getWBXMLSize(cmdID) : 0)
                  + ((msgRef != null) ? 4 + msgRef.length() : 0)
                  + ((cmdRef != null) ? 4 + cmdRef.length() : 0)
                  + ((cmd    != null) ? 4 + cmd.length()    : 0)
                  + ((cred   != null) ? getWBXMLSize(cred)  : 0)
                  + ((chal   != null) ? getWBXMLSize(chal)  : 0)
                  + ((data   != null) ? getWBXMLSize(data)  : 0)
                  ;
        for (int i=0; targetRefs != null && i<targetRefs.size(); i++) {
            size += getWBXMLSize(targetRefs.get(i));
        }
        for (int i=0; sourceRefs != null && i<sourceRefs.size(); i++) {
            size += getWBXMLSize(sourceRefs.get(i));
        }
        for (int i=0; items != null && i<items.size(); i++) {
            size += getWBXMLSize(items.get(i));
        }
        return size;
    }
    // -------------------------------------------------------------------- Chal
    /**
     * Returns the XML size of Chal element as:
     *    sizeof(<Chal>\n) +
     *    if meta != null
     *        sizeof(meta) +
     *    sizeof</Chal>\n)
     *
     * @return the XML size of Chal element
     */
    public static long getXMLSize(Chal chal) {
        Meta meta = chal.getMeta();

        return 15
             + ((meta != null) ? getXMLSize(meta) : 0)
             ;
    }

    /**
     * Returns the WBXML size of Chal element.
     *
     * @return the WBXML size of Chal element
     */
    public static long getWBXMLSize(Chal chal) {
        Meta meta = chal.getMeta();

        return 4
             + ((meta != null) ? getXMLSize(meta) : 0)
             ;
    }

    // -------------------------------------------------------------------- Sync
    /**
     * Returns the XML size of Sync element as:
     *    sizeof(<Sync>\n)                                          +
     *    if cmdID != null
     *        sizeof(cmdID)                                         +
     *    if noResp
     *        sizeof(<NoResp></NoResp>\n)                           +
     *    if cred != null
     *        sizeof(cred)                                          +
     *    if target != null
     *        sizeof(target)                                        +
     *    if source != null
     *        sizeof(source)                                        +
     *    if meta != null
     *        sizeof(meta)                                          +
     *    if numberOfChanges != 0
     *        sizeof(<NumberOfChanges>)                             +
     *        sizeof(numberOfChanges)                               +
     *        sizeof(</NumberOfChanges>\n)                          +
     *    for (int i=0; commands != null && i<commands.size(); i++)
     *        sizeof(commands[i])                                   +
     *    sizeof(</Sync>\n)
     *
     * @return the XML size of Sync element
     */
    public static long getXMLSize(Sync sync) {
        Target    target          = sync.getTarget()         ;
        Source    source          = sync.getSource()         ;
        Meta      meta            = sync.getMeta()           ;
        int       numberOfChanges = sync.getNumberOfChanges();
        ArrayList<AbstractCommand> commands        = sync.getCommands()       ;

        long size = 15
                  + getXMLSize((AbstractCommand)sync)
                  + ((target          != null) ? getXMLSize(target) : 0)
                  + ((source          != null) ? getXMLSize(source) : 0)
                  + ((meta            != null) ? getXMLSize(meta)   : 0)
                  + ((numberOfChanges != 0   ) ? 36                 : 0)
                  ;
        for (int i=0; commands != null && i<commands.size(); i++) {
            size += getCommandXMLSize((AbstractCommand)commands.get(i));
        }
        return size;
    }

    /**
     * Returns the WBXML size of Sync element.
     *
     * @return the WBXML size of Sync element
     */
    public static long getWBXMLSize(Sync sync) {
        Target    target          = sync.getTarget()         ;
        Source    source          = sync.getSource()         ;
        Meta      meta            = sync.getMeta()           ;
        int       numberOfChanges = sync.getNumberOfChanges();
        ArrayList<AbstractCommand> commands        = sync.getCommands()       ;

        long size = 4
                  + getWBXMLSize((AbstractCommand)sync)
                  + ((target          != null) ? getWBXMLSize(target) : 0)
                  + ((source          != null) ? getWBXMLSize(source) : 0)
                  + ((meta            != null) ? getWBXMLSize(meta)   : 0)
                  + ((numberOfChanges != 0   ) ? 4                    : 0)
                  ;
        for (int i=0; commands != null && i<commands.size(); i++) {
            size += getCommandWBXMLSize((AbstractCommand)commands.get(i));
        }
        return size;
    }

    // --------------------------------------------------------- ItemizedCommand
    /**
     * Returns the XML size of the ItemizedCommand element
     */
    public static long getXMLSize(ItemizedCommand itemCmd) {
        Meta      meta  = itemCmd.getMeta() ;
        ArrayList<Item> items = itemCmd.getItems();
        long size = 0;

        size = getXMLSize((AbstractCommand)itemCmd)
             + ((meta != null) ? getXMLSize(meta) : 0)
             ;

        for (int i=0; items != null && i<items.size(); i++) {
            size += getXMLSize(items.get(i));
        }
        return size;
    }

    /**
     * Returns the WBXML size of the ItemizedCommand element
     */
    public static long getWBXMLSize(ItemizedCommand itemCmd) {
        Meta      meta  = itemCmd.getMeta() ;
        ArrayList<Item> items = itemCmd.getItems();
        long size = 0;

        size = getWBXMLSize((AbstractCommand)itemCmd)
             + ((meta != null) ? getWBXMLSize(meta) : 0)
             ;

        for (int i=0; items != null && i<items.size(); i++) {
            size += getWBXMLSize(items.get(i));
        }
        return size;
    }

    // ----------------------------------------------------- ModificationCommand
    /**
     * Returns the XML size of ModificationCommand object.
     *
     * @return the XML size of ModificationCommand object
     */
    public static long getXMLSize(ModificationCommand modCmd) {
        return getXMLSize((ItemizedCommand)modCmd);
    }

    /**
     * Returns the WBXML size of ModificationCommand object.
     *
     * @return the WBXML size of ModificationCommand object
     */
    public static long getWBXMLSize(ModificationCommand modCmd) {
        return getWBXMLSize((ItemizedCommand)modCmd);
    }

    // --------------------------------------------------------- ResponseCommand
    /**
     * Returns the XML size of ResponseCommand object.
     *
     * @return the XML size of ResponseCommand object
     */
    public static long getXMLSize(ResponseCommand responseCmd) {
        String msgRef        = responseCmd.getMsgRef()   ;
        String cmdRef        = responseCmd.getCmdRef()   ;
        ArrayList<TargetRef> targetRefs = responseCmd.getTargetRef();
        ArrayList<SourceRef> sourceRefs = responseCmd.getSourceRef();
        long size = 0;

        size = getXMLSize((ItemizedCommand)responseCmd)
             + ((msgRef != null) ? 18 + msgRef.length() : 0)
             + ((cmdRef != null) ? 18 + cmdRef.length() : 0)
             ;

        for (int i=0; targetRefs != null && i<targetRefs.size(); ++i) {
            size += getXMLSize(targetRefs.get(i));
        }
        for (int i=0; sourceRefs != null && i<sourceRefs.size(); ++i) {
            size += getXMLSize(sourceRefs.get(i));
        }
        return size;
    }

    /**
     * Returns the WBXML size of ResponseCommand object.
     *
     * @return the WBXML size of ResponseCommand object
     */
    public static long getWBXMLSize(ResponseCommand responseCmd) {
        String msgRef        = responseCmd.getMsgRef()   ;
        String cmdRef        = responseCmd.getCmdRef()   ;
        ArrayList<TargetRef> targetRefs = responseCmd.getTargetRef();
        ArrayList<SourceRef> sourceRefs = responseCmd.getSourceRef();
        long size = 0;

        size = getWBXMLSize((ItemizedCommand)responseCmd)
             + ((msgRef != null) ? 4 + msgRef.length() : 0)
             + ((cmdRef != null) ? 4 + cmdRef.length() : 0)
             ;

        for (int i=0; targetRefs != null && i<targetRefs.size(); ++i) {
            size += getWBXMLSize(targetRefs.get(i));
        }
        for (int i=0; sourceRefs != null && i<sourceRefs.size(); ++i) {
            size += getWBXMLSize(sourceRefs.get(i));
        }
        return size;
    }
}
