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

package com.funambol.framework.security.jaas;


import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.commons.lang.StringUtils;

import com.funambol.framework.core.Cred;
import com.funambol.framework.tools.Base64;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;


/**
 * This handler implements the JAAS <i>CallbackHandler</i> interface. It stores
 * a <i>Credential</i> object for later use as principal and credentials provider.
 * This simple implementation supports basic authentication stored bas64 encoded
 * in the form login:password.
 *
 * TO DO: supports MD5 authentication
 *
 *.com
 */
public class CredentialHandler implements CallbackHandler {

    // --------------------------------------------------------------- Constants

    public static final String TYPE_BASIC = "syncml:auth-basic";
    public static final String TYPE_CLEAR = "syncml:auth-clear";
    public static final String SUPPORTED_TYPES = TYPE_BASIC + ',' + TYPE_CLEAR;

    // ------------------------------------------------------------ Private data

    private String     login      = null;
    private char[]     password   = null;

    // ---------------------------------------------------------- Protected data

    protected static final Logger log = Logger.getLogger(com.funambol.framework.security.jaas.CredentialHandler.class.getName());

    // ------------------------------------------------------------ Constructors

    /**
     * Creates a new instance of CredentialHandler
     *
     * @param credential
     *
     * @throws IllegalArgumentException
     */
    public CredentialHandler(Cred credential) throws IllegalArgumentException {

        String type = credential.getType();

        if (log.isEnabled(Level.TRACE)) {
            log.trace("credential: " + credential);
        }

        if (SUPPORTED_TYPES.indexOf(type) < 0) {
            throw new IllegalArgumentException( "Authorization type '"
                                              + type
                                              + "' not supported"
                                              );
        }

        if (TYPE_BASIC.equals(type)) {
            String s = new String(Base64.decode(credential.getData()));

            int p = s.indexOf(':');

            if (p == -1) {
                login = s;
                password = null;
            } else {
                login = (p>0) ? s.substring(0, p) : "";
                password = toChars((p == (s.length()-1)) ? "" : s.substring(p+1));
            }
        } else if (TYPE_CLEAR.equals(type)) {
            String s = credential.getData();

            int p = s.indexOf(':');

            if (p == -1) {
                login = s;
                password = null;
            } else {
                login = (p>0) ? s.substring(0, p) : "";
                password = toChars((p == (s.length()-1)) ? "" : s.substring(p+1));
            }
        }
    }

    // ---------------------------------------------------------- Public methods

    /**
     * Returns the login of this credential.
     *
     * @return the login value
     */
    public String getLogin() {
        return login;
    }

    // ------------------------------------------------------------------ handle

    public void handle(Callback[] callbacks)
    throws IOException, UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {

            if(log.isEnabled(Level.TRACE)){
                log.trace("Handling " + callbacks[i]);
            }

            if (callbacks[i] instanceof TextOutputCallback) {

                // display the message according to the specified type
                TextOutputCallback toc = (TextOutputCallback)callbacks[i];
                switch (toc.getMessageType()) {
                   case TextOutputCallback.INFORMATION:
                       if(log.isEnabled(Level.INFO)){
                      log.info(toc.getMessage());
                       }
                      break;
                   case TextOutputCallback.ERROR:
                       if(log.isEnabled(Level.FATAL)){
                           log.fatal(toc.getMessage());
                       }
                      break;
                   case TextOutputCallback.WARNING:
                       if(log.isEnabled(Level.WARN)){
                           log.warn(toc.getMessage());
                       }
                     break;
                   default:
                      throw new IOException( "Unsupported message type: "
                                           + toc.getMessageType()
                                           );
                }

            } else if (callbacks[i] instanceof NameCallback) {
                NameCallback nc = (NameCallback)callbacks[i];
                nc.setName(login);
            } else if (callbacks[i] instanceof PasswordCallback) {
                PasswordCallback nc = (PasswordCallback)callbacks[i];
                nc.setPassword(password);
            } else {
                throw new UnsupportedCallbackException
                 (callbacks[i], "Unrecognized Callback");
            }
          }
    }

    // --------------------------------------------------------- Private methods

    private char[] toChars(String str) {
        if (StringUtils.isEmpty(str)) {
            return new char[0];
        }

        int l = str.length();
        char[] ret = new char[l];

        str.getChars(0, l, ret, 0);

        return ret;
    }

}
