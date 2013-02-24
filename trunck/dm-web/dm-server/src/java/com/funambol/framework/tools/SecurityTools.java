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


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/**
 * This class groups utility methods for security.
 * <p>
 *
 * NOTE: when this class is first loaded, it creates and initializes a random
 * generator that can be used to create random keys.
 *
 *
 */
public class SecurityTools {

    // ------------------------------------------------------------ Private data
    private static SecureRandom random = null;

    // ------------------------------------------------------------ Constructors

    /** Creates a new instance of SecurityTools */
    protected SecurityTools() {
    }

    // ---------------------------------------------------------- Public methods

    /**
     * Returns a new random password
     *
     * @return a new password
     */
    public static String getRandomPassword() {
        byte[] nextNonce = new byte[16];
        random.nextBytes(nextNonce);

        int i;
        for (int j=0; j<nextNonce.length; ++j) {
            i = nextNonce[j] & 0x000000ff;
            if ((i<32) || (i>128)) {
                nextNonce[j] = (byte)(32 + (i % 64));
            }
        }

        return new String(Base64.encode(nextNonce));
    }


    /**
     * Calculates the HMAC of the given parameters as specified in
     * OMA-SyncML-DMSecurity-V1_1_2-20031209-A. Currently, only the MD5
     * algorithm is supported.
     *
     * @param algorithm "MD5" for now
     * @param msg the message on which calculate the mac
     * @param username the username to be used in B64(H(username:password))
     * @param password the password to be used in B64(H(username:password))
     * @param nonce the nonce to be used
     * @param log the <code>Logger</code> used to log the info
     *
     * @return the B64 encoding of the calculated MAC.
     *
     * @throws NoSuchAlgorithmException in the case the specified algorithm is
     *         not supported.
     */
    public static String getHMACValue( String algorithm,
                                       byte[] msg      ,
                                       String username ,
                                       String password ,
                                       byte[] nonce    ,
                                       Logger log)
        throws NoSuchAlgorithmException {

        if (nonce == null) {
            nonce = new byte[0];
        }
        if (log != null) {
            if (log.isEnabledFor(Level.TRACE)) {
                log.trace("Calculates mac with: " +
                           "\n\talgorith: " + algorithm +
                           "\n\tmsg: " + msg +
                           "\n\tusername: " + username +
                           "\n\tpassword: " + password +
                           "\n\tnonce: " + ((nonce == null) ? "is null" : new String(Base64.encode(nonce)))
                    );
            }
        }

        if (msg == null) {
            return null;
        }

        String cred = username + ":" + password;

        byte[] md5 = null;

        md5 = MD5.digest(cred.getBytes()); // md5(user:password)

        byte[] digest = Base64.encode(md5);

        cred = new String(digest);

        return getHMACValue(algorithm, msg, cred, nonce, log);
    }


    /**
     * Calculates the HMAC of the given parameters as specified in
     * OMA-SyncML-DMSecurity-V1_1_2-20031209-A. Currently, only the MD5
     * algorithm is supported.
     *
     * @param algorithm "MD5" for now
     * @param msg the message on which calculate the mac
     * @param credential the credential string ( B64(H(username:password)) )
     * @param nonce the nonce to be used
     * @param log the <code>Logger</code> used to log the info
     *
     * @return the B64 encoding of the calculated MAC.
     *
     * @throws NoSuchAlgorithmException in the case the specified algorithm is
     *         not supported.
     */
    public static String getHMACValue(String algorithm ,
                                      byte[] msg       ,
                                      String credential,
                                      byte[] nonce     ,
                                      Logger log)
    throws NoSuchAlgorithmException {
        //
        // Calculates mac
        //

        if (log != null) {
            if (log.isEnabledFor(Level.TRACE)) {
                log.trace("Calculates mac with: " +
                           "\n\talgorith: " + algorithm +
                           "\n\tmsg: " + msg +
                           "\n\tcredential: " + credential +
                           "\n\tnonce: " + ((nonce == null) ? "is null" : new String(Base64.encode(nonce)))
                    );
            }
        }

        if (msg == null) {
            return null;
        }

        MessageDigest md = null;

        md = MessageDigest.getInstance(algorithm);

        byte[] digestDataMessage = null;
        byte[] b64DigestDataMessage = null;

        byte[] digest = null;

        // H(B64(H(username:password)):nonce:B64(H(message)))

        // H(data)
        digestDataMessage = md.digest(msg);

        // B64(H(data))
        b64DigestDataMessage = Base64.encode(digestDataMessage);

        md.reset();

        byte[] credentialBytes = null;

        if (credential != null) {
            credentialBytes = credential.getBytes();
        } else {
            credentialBytes = new byte[0];
        }

        if (nonce == null) {
            nonce = new byte[0];
        }

        //
        // Creates a unique buffer containing the bytes to digest
        // B64(H(username:pw)):nonce:B64(H(trigger))
        //
        byte[] buf = new byte[credentialBytes.length + 2 + nonce.length + b64DigestDataMessage.length];

        System.arraycopy(credentialBytes, 0, buf, 0, credentialBytes.length);
        buf[credentialBytes.length] = (byte)':';
        System.arraycopy(nonce, 0, buf, credentialBytes.length+1, nonce.length);
        buf[credentialBytes.length + nonce.length + 1] = (byte)':';
        System.arraycopy(b64DigestDataMessage, 0, buf, credentialBytes.length + nonce.length + 2, b64DigestDataMessage.length);

        digest = md.digest(buf);

        String mac = new String(Base64.encode(digest));
        if (log != null) {
            if (log.isEnabledFor(Level.TRACE)) {
                log.trace("HMAC: " + mac);
            }
        }
        return mac;
    }


    /**
     * Compute digest using HMAC-Sha1 keyed-hashing algorithm; see RFC 2104
     * @param bKey the key
     * @param message the message
     * @throws GeneralSecurityException
     * @return byte[] the mac
     */
    public static byte[] computeHmacSha1(byte[] bKey, byte[] message)
        throws java.security.GeneralSecurityException {

        String algorithm = "HmacSha1";
        byte[] digest    = null;

        // Generate a key for the HMAC-MD5 keyed-hashing algorithm; see RFC 2104
        SecretKeySpec key = new SecretKeySpec(bKey, algorithm);

        // Create a MAC object using HMAC-MD5 and initialize with key
        Mac mac = Mac.getInstance(algorithm);
        mac.init(key);

        digest = mac.doFinal(message);

        return digest;
    }


    // ------------------------------------------------------------- Static code

    static {
        try {
        random = SecureRandom.getInstance("SHA1PRNG");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
