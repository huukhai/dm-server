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

package com.funambol.dmdemo;


import java.io.IOException;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/**
 *
 * @version $Id: LoginFilter.java,v 1.5 2007-06-18 16:38:44 luigiafassina Exp $
 */
public class LoginFilter implements Filter {

    // ------------------------------------------------------------ Private Data

    private static final Logger log = Logger.getLogger(com.funambol.dmdemo.LoginFilter.class.getName());
    private static String DELIMITER             = ",";
    private static final String PAGE_TO_FORWARD = "jsp/login.jsp";
    private String skipPages                    = null;

    // ---------------------------------------------------------- Public Methods
    
    public void doFilter(ServletRequest req,
                         ServletResponse res,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse)res;
        HttpServletRequest request = (HttpServletRequest)req;
        HttpSession session = request.getSession();

        DMDemoBean dmDemoBean = null;

        if (log.isEnabledFor(Level.TRACE)) {
            log.trace("Request page: " + request.getRequestURL());
        }

        if (skipPages != null && !skipPages.isEmpty()){
            StringTokenizer sp = new StringTokenizer(skipPages, DELIMITER);
            String uri = request.getRequestURI();
            while (sp.hasMoreTokens()){
                if (uri.indexOf(sp.nextToken().trim()) > 0){
                    chain.doFilter(req, res);
                    if (log.isEnabledFor(Level.TRACE)) {
                        log.trace("Skip session for URI: " + uri);
                    }
                    return;
                }
            }
        }

        try {
            dmDemoBean = (DMDemoBean)session.getAttribute("dmDemoBean");
        } catch (Exception e) {
            if (log.isEnabledFor(Level.TRACE)) {
                log.trace("Error getting dmDemoBean from the session: " + e);
                log.trace("Forward to " + PAGE_TO_FORWARD);
            }
            redirect(PAGE_TO_FORWARD, request, response);
            return;
        }

        if (dmDemoBean == null) {
            if (log.isEnabledFor(Level.TRACE)) {
                log.trace("DmDemoBean not in session");
                log.trace("Forward to " + PAGE_TO_FORWARD);
            }
            redirect(PAGE_TO_FORWARD, request, response);
            return;
        }

        boolean userIsLogged = dmDemoBean.isUserLogged();

        if (!userIsLogged) {
            if (log.isEnabledFor(Level.TRACE)) {
                log.trace("User not logged. Forward to " + PAGE_TO_FORWARD);
            }
            redirect(PAGE_TO_FORWARD, request, response);
            return;
        } else {
            if (log.isEnabledFor(Level.TRACE)) {
                log.trace("User logged.");
            }
        }
        chain.doFilter(req, res);
    }

    public void init(FilterConfig filterConfig) {

        skipPages = filterConfig.getInitParameter("skip-pages");

    }

    public void destroy() {
    }


    // --------------------------------------------------------- Private Methods

    private void redirect(String page,
                          HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {

        response.sendRedirect(request.getContextPath() + "/" + page);
    }
}
