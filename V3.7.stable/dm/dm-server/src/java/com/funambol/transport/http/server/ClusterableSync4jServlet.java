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

package com.funambol.transport.http.server;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.funambol.framework.core.Sync4jException;
import com.funambol.framework.tools.Base64;
import com.funambol.framework.transport.http.SyncHolder;
import com.funambol.framework.protocol.ProtocolException;
import com.funambol.framework.server.SyncResponse;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;


/**
 *  Receives the HTTP request and does session management.
 *
 *  <b>Session Management</b>
 *  <p>
 *  If the url contains a parameter {PARAM_SESSION_ID}={SESSION_ID}, the session
 *  id is used to lookup in the <i>handlerCache</i> if there is already a
 *  <i>SyncHolder</i> associated with it; the found handler is then used to process
 *  the incoming request. Otherwise, (either if the session parameter is not
 *  specified or no cached handler is found), a new session id is created and a
 *  new <i>SyncHolder</i> object is instantiated and stored in the cache.<br>
 *  The session id is created as followed:
 *  <p>
 *  <ol>
 *    <li> the first four bytes contains the IP address of the remote client
 *    <li> a dash ('-') is appended
 *    <li> the creation timestamp is appended
 *    <li> the resulting string is encoded base 64
 *  </ol>
 *  Note that no session expiration is handled by this class. That is delegated
 *  to the <i>SyncHandler</i> object.
 *
 *  @version $Id: ClusterableSync4jServlet.java,v 1.3 2006/08/07 21:09:26 nichele Exp $
 *
 */
public final class ClusterableSync4jServlet
extends HttpServlet
implements Constants {
    // ------------------------------------------------------------ Private data

    private String jndiAddress = DEFAULT_JNDI_ADDRESS;
    private RemoteHolderCache holderCache = null;

    private static final Logger log = Logger.getLogger(com.funambol.transport.http.server.ClusterableSync4jServlet.class.getName());


    // ---------------------------------------------------------- Public methods

    public void init() throws ServletException {
        String value;

        if (log.isEnabled(Level.INFO)) {
            System.getProperties().list(System.out);
        }

        ServletConfig config = getServletConfig();

        value = config.getInitParameter(PARAM_CHANNEL_PROPERTIES);
        if (StringUtils.isEmpty(value)) {
            String msg = "The servlet configuration parameter "
                       + PARAM_CHANNEL_PROPERTIES
                       + " cannot be empty ("
                       + value
                       + ")"
                       ;
            if(log.isEnabled(Level.FATAL)){
                log.fatal(msg);
            }
            throw new ServletException(msg);
        }

        value = config.getInitParameter(PARAM_SESSION_TTL);
        if (!StringUtils.isEmpty(value)) {
            try {
                Long.parseLong(value);
            } catch (NumberFormatException e) {
                String msg = "The servlet configuration parameter "
                           + PARAM_SESSION_TTL
                           + " is not an integer number ("
                           + value
                           + ")"
                           ;
                if(log.isEnabled(Level.FATAL)){
                    log.fatal(msg);
                }
                throw new ServletException(msg);
            }
        }

        value = config.getInitParameter(PARAM_JNDI_ADDRESS);
        if (StringUtils.isEmpty(value)) {
            String msg = "Missing optional parameter "
                       + PARAM_JNDI_ADDRESS
                       + ", default value ("
                       + jndiAddress
                       + ") used."
                       ;
            if(log.isEnabled(Level.WARN)){
                log.warn(msg);
            }
        } else {
            jndiAddress = value;
        }

        holderCache = new RemoteHolderCache(config);
    }


    public void doPost(
            final HttpServletRequest  httpRequest,
            final HttpServletResponse httpResponse)
            throws ServletException, IOException {

        final String contentType   = httpRequest.getContentType().split(";")[0];
        final int    contentLength = httpRequest.getContentLength();
        final String hmacHeader    = httpRequest.getHeader(Sync4jServlet.HEADER_HMAC);


        if (log.isEnabled(Level.TRACE)) {
            log.trace("contentType: " + contentType);
            log.trace("contentLength: " + contentLength);
            log.trace("hmacHeader: " + hmacHeader);
        }

        if (contentLength < 1) {
            throw new Error("Content length < 1 (" + contentLength + ")");
        }

        byte[] requestData = new byte[contentLength];

        InputStream in = httpRequest.getInputStream();
        int n = 0;
        int bytesRead = 0;

        try {
            do {
                n = in.read(requestData,
                            bytesRead,
                            requestData.length - bytesRead);
                if (n > 0) {
                    bytesRead += n;
                }
            } while (n != -1);
        } catch (IOException ex) {
            handleError(httpRequest, httpResponse, "Error reading the request", ex);
            return;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception ex) {
                handleError(httpRequest, httpResponse, ex.getClass().getName(), ex);
                return;
            }
            in = null;
        }

        if (bytesRead != contentLength) {
            handleError(httpRequest, httpResponse, "bytesRead != contentLength", null);
            return;
        }

        //
        // If the session id is not specified in the URL, a new remote object
        // will be created. Otherwise the session id specifies which remote
        // object shall handles the request.
        //
        RemoteEJBSyncHolder holder = null;
        try {
            holder = createHolder(httpRequest);
        } catch (Exception e) {
            handleError(httpRequest, httpResponse, "Error creating SyncBean", e);
            return;
        }

        SyncResponse resp = null;
        try {
            resp = holder.processMessage(requestData, contentType, hmacHeader);
        } catch (Exception e) {
            Throwable cause = e.getCause();

            if (  (cause != null)
               && (  (cause instanceof ProtocolException)
                  || (cause instanceof Sync4jException  )
                  )
               ) {
                handleError(httpRequest, httpResponse, "Protocol error", cause);
                return;
            } else {
                throw new ServletException(e);
            }
        } finally {
            //
            // Now we can store the handler into the distributed cache. Note
            // that when the holder is put in the cache a serialized copy is
            // actually stored, not the simple local reference.
            //
            if (holder.isNew()) {
                holderCache.put(holder);
            }
        }

        byte[] msgToReturn        = resp.getMessage();
        String hmacHeaderToReturn = resp.getHMACHeader();

        OutputStream out = null;
        try {
            out = httpResponse.getOutputStream();
            httpResponse.setContentType(resp.getMimeType());

            if (hmacHeaderToReturn != null) {
                httpResponse.setHeader(Sync4jServlet.HEADER_HMAC, hmacHeaderToReturn);
            }

            httpResponse.setContentLength(msgToReturn.length);
            out.write(msgToReturn);

            out.flush();
        } finally {
            log.trace("Finally");
            if (out != null) out.close();
        }

        //
        // If the message completed the SyncML communication, the holder
        // must be closed and discarded.
        //
        if (resp.isCompleted()) {
            releaseHolder(holder);
        }
    }

    public void doGet(
            final HttpServletRequest request,
            final HttpServletResponse response)
            throws ServletException, IOException {
        log.info("holderCache: " + holderCache);
        super.doGet(request, response);
    }

    // --------------------------------------------------------- Private methods


    /**
     * Factory method for <i>SyncHolder</i> objects. If the session id is
     * passed as a CGI parameter, it is transformed in a EJB handle. If the
     * handle is not valid, it is considered expired. If the session id is not
     * specified, a new EJB is created
     *
     * @param request the associated HTTP request object
     *
     * @return a new <i>SyncHolder</i>
     *
     */
    private RemoteEJBSyncHolder createHolder(HttpServletRequest request)
    throws Exception {
        String sessionId = request.getParameter(PARAM_SESSION_ID);

        RemoteEJBSyncHolder holder = null;

        if (log.isEnabled(Level.TRACE)) {
            log.trace("holderCache: " + holderCache);
        }

        if (!StringUtils.isEmpty(sessionId)) {
            holder = (RemoteEJBSyncHolder)holderCache.get(sessionId);
        }

        if (holder == null) {
            holder = new RemoteEJBSyncHolder(true);

            holder.setSessionId(createSessionId(request));
            holder.setJndiAddress(jndiAddress);
        }

        return holder;
    }

    /**
     * Closes the given <i>SyncHolder</i> and removes it from the cache.
     *
     * @param holder the holder to releases
     */
    private void releaseHolder(SyncHolder holder) {
        try {
            holder.close();
        } catch (Exception e) {
            if(log.isEnabled(Level.FATAL)){
                log.fatal(e.getMessage());
            }
            log.debug( "releaseHolder", e.getCause());
        }
        holderCache.remove(holder.getSessionId());
    }

    /**
     * Handles errors conditions returning an appropriate content to the client.
     *
     * @param request the request object
     * @param response the response object
     * @msg   a desctiptive message
     * @t     a throwable object
     *
     */
    private void handleError(final HttpServletRequest   request,
                             final HttpServletResponse response,
                                   String                   msg,
                             final Throwable                  t) {

        if (msg == null) {
            msg = "";
        }

        if (t == null) {
            if(log.isEnabled(Level.FATAL)){
                log.fatal(msg);
            }
        } else {
            if(log.isEnabled(Level.FATAL)){
                log.fatal(msg);
            }
            log.debug( "handleError", t);
        }
        try {
            response.sendError(response.SC_BAD_REQUEST, msg);
        } catch (IOException e) {
            if(log.isEnabled(Level.FATAL)){
                log.fatal(e.getMessage());
            }
            log.debug( "handleError", e);
        }
    }

    /**
     * Creates the session id (see the class description for details).
     *
     * @param request the HTTP request object
     *
     * @return a newly created session id
     */
    private String createSessionId(HttpServletRequest request) {
        String clientIP  = request.getRemoteAddr()   ;
        long   timestamp = System.currentTimeMillis();

        StringBuffer sb = new StringBuffer();

        byte[] ip = null;

        try {
            ip = InetAddress.getByName(clientIP).getAddress();
        } catch (UnknownHostException e) {
            ip = new byte[] {0, 0, 0, 0};
        }

        sb.append(ip).append('-').append(timestamp);

        String stringSessionId = new String(Base64.encode(sb.toString().getBytes()));

        //
        // strips out tailing '='
        //
        int i = stringSessionId.lastIndexOf('=');

        return ((i>0) ? stringSessionId.substring(0, i-1) : stringSessionId);
    }

    /**
     * The same as <i>handleError(reqest, response, msg, t, null)</i>.
     *
     * @param request the request object
     * @param response the response object
     * @msg   a desctiptive message
     * @t     a throwable object
     *
     */
    private void handleError(final HttpServletRequest   request,
                             final HttpServletResponse response,
                             final String                   msg) {
        this.handleError(request, response, msg, null);
    }

}
