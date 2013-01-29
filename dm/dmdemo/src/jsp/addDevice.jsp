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

<%-- $Id: addDevice.jsp,v 1.6 2007-06-18 16:38:45 luigiafassina Exp $ --%>

<%@ taglib prefix="sql" uri="/WEB-INF/tlds/sql.tld" %>
<%@ taglib prefix="c" uri="/WEB-INF/tlds/c.tld" %>
<%@ taglib prefix="fmt" uri="/WEB-INF/tlds/fmt.tld" %>

<%@ page import="java.util.*" %>
<%@ page import="java.sql.*" %>
<%@ page import="javax.sql.*" %>
<%@ page import="com.funambol.framework.tools.*" %>
<%@ page import="com.funambol.dmdemo.*" %>

<%@ page errorPage = "ErrorPage.jsp" %>

<fmt:setLocale value="en_EN" />
<fmt:setBundle basename="DMDemo" />

<jsp:useBean id="dmDemoBean" scope="session" class="com.funambol.dmdemo.DMDemoBean"/>

<%  
   
   // Set the datasource in session
   session.setAttribute("dataSource", dmDemoBean.getDataSource());

   String loginAccess    = request.getParameter("login");
   String passwordAccess = request.getParameter("pwd");

   String name     = request.getParameter("name");
   String company  = request.getParameter("company");
   String job      = request.getParameter("job");
   String address  = request.getParameter("address");
   String city     = request.getParameter("city");
   String zip      = request.getParameter("zip");
   String state    = request.getParameter("state");
   String country  = request.getParameter("country");
   String email    = request.getParameter("email");
   String phone    = request.getParameter("phone");
   String deviceId = request.getParameter("deviceId");

   String reg      = request.getParameter("reg");

   boolean userAlreadyRegistered = false;

   if (reg != null && reg.equalsIgnoreCase("true")) {

       userAlreadyRegistered = dmDemoBean.checkIfUserAlreadyExists(deviceId);

       if (!userAlreadyRegistered) {
           dmDemoBean.registerNewUser(name,
           company,
           job,
           address,
           city,
           zip,
           state,
           country,
           email,
           phone,
           deviceId
           );
       }
   }

%>

<c:set var="reg"><%= reg %></c:set>
<c:set var="userAlreadyRegistered"><%= userAlreadyRegistered %></c:set>


<html>
    <head>
        <title><fmt:message key="WINDOW_TITLE"/></title>
        <link rel="stylesheet" type="text/css" href="../css/style.css">

        <script type="text/javascript"  language="JavaScript">
            function createNewUser() {
            //showProp(document.documentElement, 'document.documentElement');
              document.location.href = 'addDevice.jsp';
           }

           function checkValue() {
               var deviceId = document.forms.reg.deviceId.value;

               if (deviceId == null || deviceId == '') {

                   alert('<fmt:message key="REQUIRED_DEVICE_ID"/>');
                   return false;
               }

               return true;
           }
        </script>
    </head>

    <body>
            
        <c:choose>
            <c:when test="${userAlreadyRegistered=='true'}">
                <div align="center" class="loginError"><fmt:message key="ALERT_DEVICE_REGISTERED"/></div>
                <br />
                <div align="center"><input type="button" name="back" value="<fmt:message key="CREATE_NEW_DEVICE_BT"/>" onClick="createNewUser()"/></div>
            </c:when>
            <c:otherwise>
                <c:choose>
                    <c:when test="${reg=='true'}">
                        <div align="center" class="loginError"><fmt:message key="DEVICE_CREATED"/></div>
                        <br />
                        <div align="center"><input type="button" name="back" value="<fmt:message key="CREATE_NEW_DEVICE_BT"/>" onClick="createNewUser()"/></div>
                    </c:when>
                    <c:otherwise>
                        
                        <div align="center" >
                            <form action="addDevice.jsp" name="reg" onSubmit="return checkValue()" >
                                <input type="hidden" name="reg" value="true" />
                                <input type="hidden" name="login" value="<%= loginAccess %>" />
                                <input type="hidden" name="pwd" value="<%= passwordAccess %>" />
                                <input type="hidden" name="name" value=" " />
                                <input type="hidden" name="email" value=" " />
                                        <table width="1%" border="0" bordercolor="red">           
                                            <tr>
                                                <td nowrap="nowrap"><fmt:message key="DEVICE_ID" />:</td>
                                                <td><input type="text" name="deviceId"/></td>
                                            </tr>
                                            <tr>
                                                <td align="center" colspan="2"><input type="submit"  name="regUser" value="Add" /></td>
                                            </tr>          
                                        </table>
                            </form>
                        </div>
                    </c:otherwise>
                </c:choose>
            </c:otherwise>
        </c:choose>
        <%@include file="bottom.jsp"%>
    </body>
</html>


