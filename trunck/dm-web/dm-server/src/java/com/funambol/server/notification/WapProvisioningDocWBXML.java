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


/**
 *
 *
 * @version $Id: WapProvisioningDocWBXML.java,v 1.2 2006/08/07 21:09:25 nichele Exp $
 */
public class WapProvisioningDocWBXML  {


    /**
     * Defines the element token code page 0 and 1
     *
     */
    public static final String[][] tagTable = {
    {
        "wap-provisioningdoc", // 05
        "characteristic", // 06
        "parm" // 07
    },
    {
        null, // 05
        "characteristic", // 06
        "parm" // 07
    }
    };

    /**
     * Attribute start tokens code page 0 and 1
     */
    public static final String[][] attributeStartTable = {
    {
        "name", // 05
        "value", // 06
        "name=NAME", // 07
        "name=NAP-ADDRESS", // 08
        "name=NAP-ADDRTYPE", // 09
        "name=CALLTYPE", // 0A
        "name=VALIDUNTIL", // 0B
        "name=AUTHTYPE", // 0C
        "name=AUTHNAME", // 0D
        "name=AUTHSECRET", // 0E
        "name=LINGER", // 0F
        "name=BEARER", // 10
        "name=NAPID", // 11
        "name=COUNTRY", // 12
        "name=NETWORK", // 13
        "name=INTERNET", // 14
        "name=PROXY-ID", // 15
        "name=PROXY-PROVIDER-ID", // 16
        "name=DOMAIN", // 17
        "name=PROVURL", // 18
        "name=PXAUTH-TYPE", // 19
        "name=PXAUTH-ID", // 1A
        "name=PXAUTH-PW", // 1B
        "name=STARTPAGE", // 1C
        "name=BASAUTH-ID", // 1D
        "name=BASAUTH-PW", // 1E
        "name=PUSHENABLED", // 1F
        "name=PXADDR", // 20
        "name=PXADDRTYPE", // 21
        "name=TO-NAPID", // 22
        "name=PORTNBR", // 23
        "name=SERVICE", // 24
        "name=LINKSPEED", // 25
        "name=DNLINKSPEED", // 26
        "name=LOCAL-ADDR", // 27
        "name=LOCAL-ADDRTYPE", // 28
        "name=CONTEXT-ALLOW", // 29
        "name=TRUST", // 2A
        "name=MASTER", // 2B
        "name=SID", // 2C
        "name=SOC", // 2D
        "name=WSP-VERSION", // 2E
        "name=PHYSICAL-PROXY-ID", // 2F
        "name=CLIENT-ID", // 30
        "name=DELIVERY-ERR-SDU", // 31
        "name=DELIVERY-ORDER", // 32
        "name=TRAFFIC-CLASS", // 33
        "name=MAX-SDU-SIZE", // 34
        "name=MAX-BITRATE-UPLINK", // 35
        "name=MAX-BITRATE-DNLINK", // 36
        "name=RESIDUAL-BER", // 37
        "name=SDU-ERROR-RATIO", // 38
        "name=TRAFFIC-HANDL-PRIO", // 39
        "name=TRANSFER-DELAY", // 3A
        "name=GUARANTEED-BITRATE-UPLINK", // 3B
        "name=GUARANTEED-BITRATE-DNLINK", // 3C
        "name=PXADDR-FQDN", // 3D
        "name=PROXY-PW", // 3E
        "name=PPGAUTH-TYPE", // 3F
        null, // 40
        null, // 41
        null, // 42
        null, // 43
        null, // 44
        "version", // 45
        "version=1.0", // 46
        "name=PULLENABLED", // 47
        "name=DNS-ADDR", // 48
        "name=MAX-NUM-RETRY", // 49
        "name=FIRST-RETRY-TIMEOUT", // 4A
        "name=REREG-THRESHOLD", // 4B
        "name=T-BIT", // 4C
        null, // 4D
        "name=AUTH-ENTITY", // 4E
        "name=SPI", // 4F
        "type", // 50
        "type=PXLOGICAL", // 51
        "type=PXPHYSICAL", // 52
        "type=PORT", // 53
        "type=VALIDITY", // 54
        "type=NAPDEF", // 55
        "type=BOOTSTRAP", // 56
        "type=VENDORCONFIG", // 57
        "type=CLIENTIDENTITY", // 58
        "type=PXAUTHINFO", // 59
        "type=NAPAUTHINFO", // 5A
        "type=ACCESS" // 5B
    },
    {
        "name", // 05
        "value", // 06
        "name=NAME", // 07
        null, // 08
        null, // 09
        null, // 0A
        null, // 0B
        null, // 0C
        null, // 0D
        null, // 0E
        null, // 0F
        null, // 10
        null, // 11
        null, // 12
        null, // 13
        "name=INTERNET", // 14
        null, // 15
        null, // 16
        null, // 17
        null, // 18
        null, // 19
        null, // 1A
        null, // 1B
        "name=STARTPAGE", // 1C
        null, // 1D
        null, // 1E
        null, // 1F
        null, // 20
        null, // 21
        "name=TO-NAPID", // 22
        "name=PORTNBR", // 23
        "name=SERVICE", // 24
        null, // 25
        null, // 26
        null, // 27
        null, // 28
        null, // 29
        null, // 2A
        null, // 2B
        null, // 2C
        null, // 2D
        "name=AACCEPT", // 2E
        "name=AAUTHDATA", // 2F
        "name=AAUTHLEVEL", // 30
        "name=AAUTHNAME", // 31
        "name=AAUTHSECRET", // 32
        "name=AAUTHTYPE", // 33
        "name=ADDR", // 34
        "name=ADDRTYPE", // 35
        "name=APPID", // 36
        "name=APROTOCOL", // 37
        "name=PROVIDER-ID", // 38
        "name=TO-PROXY", // 39
        "name=URI", // 3A
        "name=RULE", // 3B
        null, // 3C
        null, // 3D
        null, // 3E
        null, // 3F
        null, // 40
        null, // 41
        null, // 42
        null, // 43
        null, // 44
        null, // 45
        null, // 46
        null, // 47
        null, // 48
        null, // 49
        null, // 4A
        null, // 4B
        null, // 4C
        null, // 4D
        null, // 4E
        null, // 4F
        "type", // 50
        null, // 51
        null, // 52
        "type=PORT", // 53
        null, // 54
        "type=APPLICATION", // 55
        "type=APPADDR", // 56
        "type=APPAUTH", // 57
        "type=CLIENTIDENTITY", // 58
        "type=RESOURCE", // 59
    }
    };


    /**
     * Parameter token value code page 0 and 1
     */
    public static final String[][] paramValueTable = {
    {
        null, // 80
        null, // 81
        null, // 82
        null, // 83
        null, // 84
        "IPV4", // 85
        "IPV6", // 86
        "E164", // 87
        "ALPHA", // 88
        "APN", // 89
        "SCODE", // 8A
        "TETRA-ITSI", // 8B
        "MAN", // 8C
        null, // 8D
        null, // 8E
        null, // 8F
        "ANALOG-MODEM", // 90
        "V.120", // 91
        "V.110", // 92
        "X.31", // 93
        "BIT-TRANSPARENT", // 94
        "DIRECT-ASYNCHRONOUS-DATA-SERVICE", // 95
        null, // 96
        null, // 97
        null, // 98
        null, // 99
        "PAP", // 9A
        "CHAP", // 9B
        "HTTP-BASIC", // 9C
        "HTTP-DIGEST", // 9D
        "WTLS-SS", // 9E
        "MD5", // 9F
        null, // A0
        null, // A1
        "GSM-USSD", // A2
        "GSM-SMS", // A3
        "ANSI-136-GUTS", // A4
        "IS-95-CDMA-SMS", // A5
        "IS-95-CDMA-CSD", // A6
        "IS-95-CDMA-PACKET", // A7
        "ANSI-136-CSD", // A8
        "ANSI-136-GPRS", // A9
        "GSM-CSD", // AA
        "GSM-GPRS", // AB
        "AMPS-CDPD", // AC
        "PDC-CSD", // AD
        "PDC-PACKET", // AE
        "IDEN-SMS", // AF
        "IDEN-CSD", // B0
        "IDEN-PACKET", // B1
        "FLEX/REFLEX", // B2
        "PHS-SMS", // B3
        "PHS-CSD", // B4
        "TETRA-SDS", // B5
        "TETRA-PACKET", // B6
        "ANSI-136-GHOST", // B7
        "MOBITEX-MPAK", // B8
        "CDMA2000-1X-SIMPLE-IP", // B9
        "CDMA2000-1X-MOBILE-IP", // BA
        null, // BB
        null, // BC
        null, // BD
        null, // BE
        null, // BF
        null, // C0
        null, // C1
        null, // C2
        null, // C3
        null, // C4
        "AUTOBAUDING", // C5
        null, // C6
        null, // C7
        null, // C8
        null, // C9
        "CL-WSP", // CA
        "CO-WSP", // CB
        "CL-SEC-WSP", // CC
        "CO-SEC-WSP", // CD
        "CL-SEC-WTA", // CE
        "CO-SEC-WTA", // CF
        "OTA-HTTP-TO", // D0
        "OTA-HTTP-TLS-TO", // D1
        "OTA-HTTP-PO", // D2
        "OTA-HTTP-TLS-PO", // D3
        null, // D4
        null, // D5
        null, // D6
        null, // D7
        null, // D8
        null, // D9
        null, // DA
        null, // DB
        null, // DC
        null, // DD
        null, // DE
        null, // DF
        "AAA", // E0
        "HA"  // E1
    },
    {
        ",", // 80
        "HTTP-", // 81
        "BASIC", // 82
        "DIGEST", // 83
        null, // 84
        null, // 85
        "IPV6", // 86
        "E164", // 87
        "ALPHA", // 88
        null, // 89
        null, // 8A
        null, // 8B
        null, // 8C
        "APPSRV", // 8D
        "OBEX"
    }
    };

}
