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

<%-- $Id: registrationForm.jsp,v 1.4 2007-06-18 16:38:45 luigiafassina Exp $ --%>

<%@ taglib prefix="sql" uri="/WEB-INF/tlds/sql.tld" %>
<%@ taglib prefix="c" uri="/WEB-INF/tlds/c.tld" %>
<%@ taglib prefix="fmt" uri="/WEB-INF/tlds/fmt.tld" %>

<%@ page import="java.util.*" %>
<%@ page import="java.sql.*" %>
<%@ page import="javax.sql.*" %>
<%@ page import="com.funambol.framework.tools.*" %>
<%@ page import="com.funambol.dmdemo.*" %>

<%@ page errorPage = "ErrorPage.jsp" %>

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

   if (loginAccess == null || !loginAccess.equals("admindm") ||
       passwordAccess == null || !passwordAccess.equals("fundm")) {
       response.sendError(response.SC_UNAUTHORIZED);
       return;
   }


%>
<c:set var="reg"><%= reg %></c:set>
<c:set var="userAlreadyRegistered"><%= userAlreadyRegistered %></c:set>


<html>
    <head>
        <title>Funambol Device Management Server Demo</title>
        <link rel="stylesheet" type="text/css" href="../css/style.css">

        <script type="text/javascript"  language="JavaScript">
            function createNewUser() {
            //showProp(document.documentElement, 'document.documentElement');
              document.location.href = 'registrationForm.jsp?login=<%=loginAccess%>&pwd=<%= passwordAccess%>';
           }

           function checkValue() {
               var name     = document.forms.reg.name.value;
               var deviceId = document.forms.reg.deviceId.value;
               var email    = document.forms.reg.email.value;

               if (name == null || name == '' ||
                   deviceId == null || deviceId == '' ||
                   email == null || email == '') {

                   alert("Name, Email, and Device Id are required");
                   return false;
               }

               return true;
           }
        </script>
    </head>

    <body>

        <div align="center"><a target="_top" href="http://www.funambol.com" border="0"><img src="../img/funambol.gif" alt="Funambol Device Management Server" border="0"></a><br></div>
        <div align="center" style=" font-size:14pt;font-family:arial;font-weight:bold">Device Management Server</div>

        <c:choose>
            <c:when test="${userAlreadyRegistered=='true'}">
                <br />
                <br />
                <div align="center" class="loginError">User already registered.</div>
                <br />
                <div align="center"><input type="button" name="back" value="Create New User" onClick="createNewUser()"/></div>
            </c:when>

            <c:otherwise>

                <c:choose>
                    <c:when test="${reg=='true'}">
                        <br />
                        <br />
                        <div align="center" class="loginError">User created.</div>
                        <br />
                        <div align="center"><input type="button" name="back" value="Create New User" onClick="createNewUser()"/></div>
                    </c:when>

                    <c:otherwise>
                        <div align="center" >
                            <form action="registrationForm.jsp" name="reg" onSubmit="return checkValue()" >
                                <input type="hidden" name="reg" value="true" />
                                <input type="hidden" name="login" value="<%= loginAccess %>" />
                                    <input type="hidden" name="pwd" value="<%= passwordAccess %>" />


                                        <table width="1%" border="0" bordercolor="red">
                                            <tr>
                                                <td colspan="2">&nbsp;</td>
                                            </tr>
                                            <tr>
                                                <td>Name* :</td>
                                                <td><input type="text" name="name"/></td>
                                            </tr>
                                            <tr>
                                                <td>Device Id* :</td>
                                                <td><input type="text" name="deviceId"/></td>
                                            </tr>
                                            <tr>
                                                <td>Email* :</td>
                                                <td><input type="text" name="email"/></td>
                                            </tr>
                                            <tr>
                                                <td>Company/Organization:</td>
                                                <td><input type="text" name="company"/></td>
                                            </tr>
                                            <tr>
                                                <td>Job Function:</td>
                                                <td><input type="text" name="job"/></td>
                                            </tr>
                                            <tr>
                                                <td>Address:</td>
                                                <td><input type="text" name="address"/></td>
                                            </tr>
                                            <tr>
                                                <td>City:</td>
                                                <td><input type="text" name="city"/></td>
                                            </tr>
                                            <tr>
                                                <td>ZIP code:</td>
                                                <td><input type="text" name="zip"/></td>
                                            </tr>
                                            <tr>
                                                <td>State/Province:</td>
                                                <td><input type="text" name="state"/></td>
                                            </tr>
                                            <tr>
                                                <td>Country:</td>
                                                <td><input type="text" name="country"/></td>
                                            </tr>
                                            <tr>
                                                <td>Phone:</td>
                                                <td><input type="text" name="phone"/></td>
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


    </body>

</html>


