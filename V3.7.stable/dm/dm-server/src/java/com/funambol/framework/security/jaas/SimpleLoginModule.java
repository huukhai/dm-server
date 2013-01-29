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


import java.util.Map;

import java.security.Principal;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import com.funambol.framework.security.Sync4jPrincipal;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;


/**
 * This is a simple implementation of a JAAS login module. It always authenticates
 * and uthorizes.
 *
 *.com
 */
public class SimpleLoginModule implements LoginModule {

    // ------------------------------------------------------------ Private data

    private Subject         subject        = null ;
    private CallbackHandler handler        = null ;
    private boolean         loginSucceded  = false;
    private boolean         loginCommitted = false;
    private Principal       principal      = null ;
    private String          username       = null ;
    private String          password       = null ;

    // ---------------------------------------------------------- Protected data

    protected static final Logger log = Logger.getLogger(com.funambol.framework.security.jaas.SimpleLoginModule.class.getName());

    // ---------------------------------------------------------- Public methods

    public void initialize(Subject         subject            ,
                           CallbackHandler handler            ,
                           Map             sharedState        ,
                           Map             options            ) {

        this.subject = subject;
        this.handler = handler;
        this.loginSucceded = this.loginCommitted = false;
        this.username = null;
        this.password = null;

        if (log.isEnabled(Level.TRACE)) {
            log.trace("sharedState: " + sharedState);
            log.trace("options: " + options);
        }
    }

    public boolean login() throws LoginException {
        if (handler == null) {
            throw new LoginException("No CallbackHandler defined");
        }

        //
        // Creates two callbacks: one for user, one for password. They are for
        // demonstration purpose only, because they will never be used by this
        // implementation of <i>LoginModule</i>
        //
        Callback[] callbacks = new Callback[2];

        NameCallback nameCallback = new NameCallback("Username");
        PasswordCallback passwordCallback = new PasswordCallback("Password", false);

        callbacks[0] = nameCallback;
        callbacks[1] = passwordCallback;

        try {
            handler.handle(callbacks);

            username = nameCallback.getName();
            password = (passwordCallback.getPassword() == null) ? null
                                                                : new String(passwordCallback.getPassword());

            passwordCallback.clearPassword();
        } catch (Exception e) {
            throw new LoginException(e.toString());
        }

        loginSucceded = true;
        return true;
    }

    public boolean commit() throws LoginException {
        if (loginSucceded == false) {
            return false;
        }

        //
        // Login succeded, so create a Principal and add it to the subject
        //
        principal = new Sync4jPrincipal(null, username, null);

        if (!(subject.getPrincipals().contains(principal))) {
            subject.getPrincipals().add(principal);
        }

        username = null;
        password = null;
        loginCommitted = true;

        return true;
    }

    public boolean abort() throws LoginException {
        if (loginSucceded == false) {
            return false;
        } else if (loginCommitted == false) {
            loginSucceded = false;
            username  = password  = null;
            principal = null;
        } else {
            logout();
        }

        return true;
    }

    public boolean logout() throws LoginException {
        subject.getPrincipals().remove(principal);

        loginSucceded = loginCommitted = false;
        username = password = null;
        principal = null;

        return true;
    }

}
