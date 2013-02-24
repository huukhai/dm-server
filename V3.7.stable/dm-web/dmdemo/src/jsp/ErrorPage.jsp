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


<%-- $Id: ErrorPage.jsp,v 1.4 2007-06-18 16:38:45 luigiafassina Exp $ --%>


<%@ page isErrorPage="true" %>
<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html;CHARSET=iso-8859-1">

<title>Funambol Device Manangement Server Demo - Error Page</title>

</head>

<body bgcolor="#ffffff" link="#3366cc" vlink="#9999cc" alink="#0000cc">


<table border=0 cellspacing="18" cellpadding="0">
<tr>
<td valign="top">

 <h3>Error Page</h3>

 </td>
 </tr>
</table>

<table border=0 cellspacing="18" cellpadding="0">
<tr>
<td valign="top">
<hr>

<!-- Test that there is indeed an exception to report.
     Otherwise, the ErrorPage throws an NPE. -->

<% if (exception != null) { %>
<p> An exception was thrown: <b> <%= exception %></b>

<p> With the following stack trace:
<pre>

<%
    ByteArrayOutputStream ostr = new ByteArrayOutputStream();
    exception.printStackTrace(new PrintStream(ostr));
    out.print(ostr);
%>
</pre>

<!-- This next block executes if this page is loaded without
     an exception. -->
<%
} else  { %>
<p> <font color=#FF0000><b>Error Page Error:</b></font>
    Error Page was called with a null exception.
<% } %>

<p>
<hr>
 </td>
 </tr>
</table>
<br>


</body>
</html>


