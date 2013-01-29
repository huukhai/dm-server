<%--
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
 --%>

<%-- $Id: login.jsp,v 1.6 2007-06-18 16:38:45 luigiafassina Exp $ --%>

<%@ taglib prefix="sql" uri="/WEB-INF/tlds/sql.tld" %>
<%@ taglib prefix="c" uri="/WEB-INF/tlds/c.tld" %>
<%@ taglib prefix="fmt" uri="/WEB-INF/tlds/fmt.tld" %>

<%@ page errorPage = "ErrorPage.jsp" %>

<%@ page import="java.util.*" %>
<%@ page import="java.sql.*" %>
<%@ page import="javax.sql.*" %>
<%@ page import="com.funambol.framework.tools.*" %>
<%@ page import="com.funambol.dmdemo.*" %>

<fmt:setLocale value="en_EN" />
<fmt:setBundle basename="DMDemo" />

<jsp:useBean id="dmDemoBean" scope="session" class="com.funambol.dmdemo.DMDemoBean"/>

<%
   // Set the datasource in session
   session.setAttribute("dataSource", dmDemoBean.getDataSource());

   String deviceId = request.getParameter("deviceId");
   String password = request.getParameter("password");
   String login    = request.getParameter("login");

   boolean logged  = false;
   if (login != null && login.equalsIgnoreCase("true")) {
       logged = dmDemoBean.loginUser(deviceId, password);
   } else {
       if (dmDemoBean.isUserLogged()) {
           %>
               <jsp:forward page="main.jsp" />
           <%
       }
   }
%>
<c:set var="logged"><%= logged %></c:set>
<c:set var="login"><%= login %></c:set>

<c:if test="${login=='true' && logged=='true'}">
    <jsp:forward page="main.jsp" />
</c:if>

<html>
    <head>
        <title><fmt:message key="WINDOW_TITLE" /></title>
        <link rel="stylesheet" type="text/css" href="../css/style.css">

    </head>

    <body>

        <c:choose>
            <c:when test="${logged == 'false' && login == 'true'}">
                <div align="center" class="loginError"><fmt:message key="LOGIN_FAILED" /></div>
            </c:when>
        </c:choose>

        <div align="center" >
            <form action="login.jsp" name="loginForm">
                <input type="hidden" name="login" value="true" />

                <table width="1%" border="0" bordercolor="red">

                    <tr>
                        <td nowrap="nowrap"><fmt:message key="DEVICE_ID" />:</td>
                        <td><input type="text" name="deviceId"/></td>
                    </tr>
                    <tr>
                        <td align="center" colspan="2"><input type="submit" name="loginButton" value="<fmt:message key="LOG_IN" />" /></td>
                    </tr>
                </table>
            </form>
        </div>
        <%@include file="bottom.jsp"%>    
    </body>

    <script type="text/javascript" language="javascript" >
         document.loginForm.deviceId.focus();
    </script>
</html>


