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

<%-- $Id: top.jsp,v 1.4 2007-06-18 16:38:45 luigiafassina Exp $ --%> 
 
<%@ taglib prefix="sql" uri="/WEB-INF/tlds/sql.tld" %>
<%@ taglib prefix="c" uri="/WEB-INF/tlds/c.tld" %>
<%@ taglib prefix="fmt" uri="/WEB-INF/tlds/fmt.tld" %>

<%@ page errorPage = "ErrorPage.jsp" %>

<%@ page import="java.util.*" %>
<%@ page import="java.sql.*" %>
<%@ page import="javax.sql.*" %>
<%@ page import="com.funambol.framework.tools.*" %>
<%@ page import="com.funambol.dmdemo.*" %>


<jsp:useBean id="dmDemoBean" scope="session" class="com.funambol.dmdemo.DMDemoBean"/>


<fmt:setLocale value="en_EN" />
<fmt:setBundle basename="DMDemo" />

<HTML>
<HEAD>
<TITLE>Funambol DM Demo</TITLE>
<link rel="stylesheet" type="text/css" href="../css/style.css">
</HEAD>

<body>
<div align="center"><a target="_top" href="http://www.funambol.com" border="0"><img src="/dmdemo/img/funambol.gif" alt="Device Management Server" border="0"></a><br></div>
<div align="center" style=" font-size:14pt;font-family:arial;font-weight:bold">Device Management Server</div>


<script type="text/javascript" >

function LogOut(){
    parent.document.location="logOut.jsp";
}

function LogIn(){
    parent.main.document.location="login.jsp";
}

function AddDevice(){
    parent.main.document.location="addDevice.jsp";
}

</script>

<%  
boolean isLogged = dmDemoBean.isUserLogged();
%>

<br>      
<table width="100%" border="0" bordercolor="red" bgcolor="#D5D5D5">
<tr>
<td>
<CENTER>
<a href="javascript:AddDevice();"><fmt:message key="CREATE_NEW_DEVICE_BT" /></a>
&nbsp;-&nbsp;
<% if(isLogged) { %>
    <fmt:message key="LOGIN" />
<% } %>
<% if(!isLogged) { %>
    <a href="javascript:LogIn();"><fmt:message key="LOGIN" /></a>
<% } %>        
&nbsp;-&nbsp;
<% if(isLogged) { %>
    <a href="javascript:LogOut();"><fmt:message key="LOGOUT" /></a>
<% } %>
<% if(!isLogged) { %>
    <fmt:message key="LOGOUT" />
<% } %>
</CENTER>
</td>
</tr>
</table>

</body>

</HTML>
