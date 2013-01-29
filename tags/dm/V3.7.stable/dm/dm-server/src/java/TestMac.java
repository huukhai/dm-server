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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.funambol.framework.notification.NotificationException;

public class TestMac {


    public static void main(String[] args) throws Exception {
        TestMac testmac = new TestMac();

        // your data
        String hex = "0200006A1D2D2F2F53594E434D4C2F2F4454442053796E634D4C20312E312F2F454E6D6C7103312E3100017203444D2F312E31000165033000015B033000016E5703494D45493A303030303030303131323334353634000101675703687474703A2F2F36352E3234312E35352E3130303A383038302F73796E63346A2F73796E63000101016B454B03310001546E57032E2F53796E634D4C2F444D4163632F313130390001015A000147036E6F6465000101010000546E57032E2F53796E634D4C2F444D4163632F313130392F416464720001014F03687474703A2F2F36352E3234312E35352E3130302F73796E63346A2F73796E63000101546E57032E2F53796E634D4C2F444D4163632F313130392F41646472547970650001014F0331000101546E57032E2F53796E634D4C2F444D4163632F313130392F506F72744E62720001014F0338303830000101546E57032E2F53796E634D4C2F444D4163632F313130392F436F6E5265660001010F01546E57032E2F53796E634D4C2F444D4163632F313130392F53657276657249640001014F0373796E63346A000101546E57032E2F53796E634D4C2F444D4163632F313130392F53657276657250570001014F034933303150436432527945746354412F4A6B6C6F57513D3D000101546E57032E2F53796E634D4C2F444D4163632F313130392F5365727665724E6F6E63650001015A0001470362696E00010100004FC310294A57252775592F762B5221465F72640101546E57032E2F53796E634D4C2F444D4163632F313130392F557365724E616D650001014F0331313039000101546E57032E2F53796E634D4C2F444D4163632F313130392F436C69656E7450570001014F03666F7461000101546E57032E2F53796E634D4C2F444D4163632F313130392F436C69656E744E6F6E63650001015A0001470362696E00010100004FC3106C5F40517727444B495B332E7E7057420101546E57032E2F53796E634D4C2F444D4163632F313130392F41757468507265660001014F0373796E636D6C3A617574682D4D4143000101546E57032E2F53796E634D4C2F444D4163632F313130392F4E616D650001014F033131303900010101120101";
        //  String hex = "0200006A1D2D2F2F53594E434D4C2F2F4454442053796E634D4C20312E312F2F454E6D6C7103312E3100017203444D2F312E31000165033000015B033000016E5703494D45493A303030303030303131323334353634000101675703687474703A2F2F36352E3234312E35352E3130303A383038302F73796E63346A2F73796E63000101016B454B03310001546E57032E2F53796E634D4C2F444D4163632F313130390001015A000147036E6F6465000101010000546E57032E2F53796E634D4C2F444D4163632F313130392F416464720001014F03687474703A2F2F36352E3234312E35352E3130302F73796E63346A2F73796E63000101546E57032E2F53796E634D4C2F444D4163632F313130392F41646472547970650001014F0331000101546E57032E2F53796E634D4C2F444D4163632F313130392F506F72744E62720001014F0338303830000101546E57032E2F53796E634D4C2F444D4163632F313130392F436F6E5265660001010F01546E57032E2F53796E634D4C2F444D4163";

        byte[] data = convertHexString(hex);
        System.out.println("Hex length: " + hex.length());
        System.out.println("Hex: " + hex);

        if (!hex.equals(bytesToHex(data))) {
            System.out.println("Error converting the string in bytes array");
        }

        // the imsi
        String imsi = "310260801674539";

        // your used semi-octet
        String semioctet = "3901628010765493";

        byte[] mac = computeHmacSha1(semioctet.getBytes(), data);

        // your expected mac
        System.out.println("Your expected Mac using semi-octet as string: " + bytesToHex(mac));


        byte[] key = getKeyFromIMSI(imsi);

        mac = computeHmacSha1(key, data);

        // our mac
        System.out.println("Our Mac using semi-octet as byte[]: " + bytesToHex(mac));
    }


    private static byte[] convertHexString(String hex) {
        int length = hex.length();
        byte[] bHex = new byte[length/2];
        String temp = null;
        int t = 0;
        for (int i=0; i<length; i++) {
            temp = "" + hex.charAt(i) + hex.charAt(++i);
            bHex[t++] = (byte)Integer.parseInt(temp, 16);
        }
        return bHex;
    }


    /**
     * Compute digest using HMAC-Sha1 keyed-hashing algorithm; see RFC 2104
     * @param bKey the key
     * @param message the message
     * @throws GeneralSecurityException
     * @return byte[] the mac
     */
    public static byte[] computeHmacSha1(byte[] bKey, byte[] message) throws java.security.
        GeneralSecurityException {

        String algorithm = "HmacSha1";
        byte[] digest = null;

        // Generate a key for the HMAC-MD5 keyed-hashing algorithm; see RFC 2104
        SecretKeySpec key = new SecretKeySpec(bKey, algorithm);

        // Create a MAC object using HMAC-MD5 and initialize with key
        Mac mac = Mac.getInstance(algorithm);
        mac.init(key);

        digest = mac.doFinal(message);

        return digest;
    }

    public static String bytesToHex(byte[] b) {
        StringBuffer buf = new StringBuffer("");
        for (int i = 0; i < b.length; i++)
            buf.append(byteToHex(b[i]));
        return buf.toString();
    }

    public static String byteToHex(byte b) {
        // Returns hex String representation of byte b
        char hexDigit[] = {
                          '0', '1', '2', '3', '4', '5', '6', '7',
                          '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
        };
        char[] array = {hexDigit[ (b >> 4) & 0x0f], hexDigit[b & 0x0f]};
        return new String(array);
    }


    /**
     * Computes the bytes array to use as key in the mac calculation
     * based on imsi. The IMSI MUST be on semi-octet representation as defined
     * in Digital cellular Telecommunications system (Phase 2+); Specification of the Subscriber
     * Identity Module - Mobile Equipment (SIM - ME) interface
     * (GSM 11.11 version 7.2.0 Release 1998)
     *
     * @param imsi String
     * @return byte[]
     */
    private static byte[] getKeyFromIMSI(String imsi) throws NotificationException {

        imsi = imsi.trim();

        if ( (imsi.length() % 2) == 1 ) {
            imsi = "9" + imsi;
        } else {
            imsi = "1" + imsi;
            imsi = imsi + "F";

        }

        int numDigit = imsi.length();
        String temp = null;
        char c1 = 0;
        char c2 = 0;
        byte b  = 0;
        byte[] key = new byte[numDigit / 2]; // always even
        int t = 0;
        for (int i = 0; i < numDigit; i++) {
            c1 = imsi.charAt(i);
            c2 = imsi.charAt(++i);
            temp = "" + c2 + c1;
            try {
                key[t] = (byte) (Integer.parseInt(temp, 16));
            } catch (NumberFormatException ex) {
                throw new NotificationException("IMSI isn't valid (only numbers are permitted)");
            }
            t++;
        }


        return key;
    }
}
