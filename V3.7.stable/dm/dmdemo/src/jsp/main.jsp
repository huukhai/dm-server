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

<%-- $Id: main.jsp,v 1.5 2007-06-18 16:38:45 luigiafassina Exp $ --%>

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

<%
    // Set the datasource in session
    session.setAttribute("dataSource", dmDemoBean.getDataSource());

    String deleteSession = request.getParameter("deleteSession");
    String sessionId     = request.getParameter("sessionId");
    String processor     = request.getParameter("processor");
    String stop          = request.getParameter("stop");

    if (sessionId == null) {
        sessionId = dmDemoBean.getSessionId();
    }
    String deviceId = dmDemoBean.getDeviceId();
%>

<c:set var="deleteSession"><%= deleteSession%></c:set>
<c:set var="sessionId"><%= sessionId%></c:set>
<c:set var="deviceId"><%= deviceId%></c:set>


<c:if test="${deleteSession == 'true'}">
    <c:if test="${sessionId != null && sessionId != ''}">
        <%
            if (processor != null && !processor.equals("")) {
                dmDemoBean.setOperation(processor);
            }
            if (sessionId != null && !sessionId.equals("")) {
                dmDemoBean.setSessionId(sessionId);
            }
            dmDemoBean.deleteSession();
        %>
    </c:if>
</c:if>

<%
    if (stop != null && stop.equalsIgnoreCase("true")) {
        dmDemoBean.deleteSessionToProcess();
    }

    pageContext.setAttribute("sessionsList", dmDemoBean.getSessionsList(deviceId));
%>

<html>
    <head>
        <title><fmt:message key="WINDOW_TITLE" /></title>
        <link rel="stylesheet" type="text/css" href="../css/style.css">

        <script type="text/javascript"  language="JavaScript">
            function checkDevice() {
                var device = document.getElementById("200").value;
                if (device == '') {
                    disableStartNewSession();
                } else {
                    enableStartNewSession();
                }
            }

            function startNewSession() {

                var processor = document.getElementById("101").value;
                if (processor == 'GenericProcessor') {
                    document.location.href = "genericSession.jsp?newSession=true";
                } else if (processor == 'TreeDiscoveryProcessor') {
                    document.location.href = "treeDiscoverySession.jsp?newSession=true";
                } else if (processor == 'GetDeviceDetails') {
                    document.location.href = "getDeviceDetailsSession.jsp?newSession=true&preconfiguredOperations=GetDeviceDetails";
                }

                //location.href = 'genericSession   .jsp?newSession=true&deviceId=' + deviceId;
            }

            function refreshList() {
                location.href = 'main.jsp';
            }

            function stop() {
                location.href = 'main.jsp?stop=true';
            }

            function disableStartNewSession() {
                document.getElementById("100").disabled=true;
                document.getElementById("101").disabled=true;
                document.getElementById("300").style.visibility = "visible";
                document.getElementById("301").style.visibility = "visible";
            }

            function enableStartNewSession() {
                document.getElementById("100").disabled=false;
                document.getElementById("101").disabled=false;
                document.getElementById("300").style.visibility = "hidden";
                document.getElementById("301").style.visibility = "hidden";
            }

            function deleteSession(id, operation) {

              if (confirm('<fmt:message key="ALERT_DELETE_SESSION" /> \'' + id + '\' ?')) {
                   parent.main.document.location="main.jsp?sessionId="+ id + "&deleteSession=true&processor=" + operation;
              }
                       
              return;
            }
            
        </script>
    </head>

    <body>
        <table width="100%" border="0" bordercolor="red">
            <tr>
                <td align="center">
                    <table>
                        <tr>
                            <td><fmt:message key="DEVICE_ID" />:&nbsp;</td>
                            <td><c:out value="${deviceId}"/></td>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr>
                <td align="center">
                    <table  cellpadding="0" cellspacing="0" border="1">
                        <tr>
                            <td>
                                <table  cellpadding="5" cellspacing="0" border="0">
                                    <tr>
                                        <td><fmt:message key="SELECT_PROCESSOR" />:</td>
                                        <td align="left">
                                            <select id="101" name="processor">
                                                <option value="GetDeviceDetails"><fmt:message key="GET_DEVICE_DETAILS_PROCESSOR" /></option>
                                                <option value="TreeDiscoveryProcessor"><fmt:message key="TREEDISCOVERY_PROCESSOR" /></option>
                                                <option value="GenericProcessor"><fmt:message key="CUSTOM_PROCESSOR" /></option>
                                            </select>
                                        </td>
                                    </tr>

                                    <tr>
                                        <td align="center" colspan="2">
                                            <input id="100" type="button" name="newsession" value="<fmt:message key="NEW_MANAGEMENT_SESSION" />" onclick="startNewSession()" />
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr><td><br/><br/></td></tr>
           <tr>
                <td align="center">
                    <table>
                        <tr>
                            <td><input id="300" type="button" name="refresh" value="<fmt:message key="REFRESH_LIST" />" onclick="refreshList()" /></td>
                            <td><input id="301" type="button" name="stop" value="<fmt:message key="STOP" />" onclick="stop()" /></td>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr>
                <td align="center">
                    <table cellpadding="5" cellspacing="0" border="1">
                        <tr>
                            <th><fmt:message key="DEVICE_ID" /></th>
                            <th><fmt:message key="SESSION_ID" /></th>
                            <th><fmt:message key="OPERATION" /></th>
                            <th><fmt:message key="STATE" /></th>
                            <th><fmt:message key="START_TIME" /></th>
                            <th><fmt:message key="END_TIME" /></th>
                            <th><fmt:message key="VIEW_LOG" /></th>
                            <th>&nbsp;</th>
                        </tr>
                        <c:forEach var="rowSessions" items="${sessionsList}">
                            <c:if test="${rowSessions.operation == 'GenericProcessor' ||
                                          rowSessions.operation == 'TreeDiscoveryProcessor' ||
                                          rowSessions.operation == 'GetDeviceDetails'}">

                            <tr>
                                <td align="center"><c:out value="${rowSessions.device}"/></td>
                                <td align="center"><c:out value="${rowSessions.id}"/></td>
                                <td align="center"><fmt:message key="${rowSessions.operation}" /></td>
                                <td align="center">
                                    <c:if test="${rowSessions.state == 'P'}">
                                        <fmt:message key="STATE_TO_PROCESS" />
                                    </c:if>
                                    <c:if test="${rowSessions.state == 'C'}">
                                        <fmt:message key="STATE_COMPLETED" />
                                    </c:if>
                                    <c:if test="${rowSessions.state == 'N'}">
                                        <fmt:message key="STATE_NOTIFIED" />
                                    </c:if>
                                    <c:if test="${rowSessions.state == 'E'}">
                                        <fmt:message key="ERROR" />
                                    </c:if>
                                    <c:if test="${rowSessions.state == 'A'}">
                                        <fmt:message key="ABORTED" />
                                    </c:if>
                                </td>

                                <c:if test="${rowSessions.state == 'P'}">
                                    <c:set var="disable">true</c:set>
                                    <td>&nbsp;</td>
                                    <td>&nbsp;</td>
                                </c:if>
                                <c:if test="${rowSessions.state != 'P'}">
                                    <td><fmt:formatDate pattern="MM-dd-yyyy HH:mm:ss" value="${rowSessions.start_ts}"/></td>
                                    <td><fmt:formatDate pattern="MM-dd-yyyy HH:mm:ss" value="${rowSessions.end_ts}"/></td>
                                </c:if>

                                <td align="center">
                                    <c:if test="${rowSessions.operation == 'GenericProcessor'}">
                                        <a href="genericSession.jsp?sessionId=<c:out value="${rowSessions.id}"/>&state=<c:out value="${rowSessions.state}"/>&refresh=true"><img border="0" src="../img/show.gif" alt="View"/></a>
                                    </c:if>
                                    <c:if test="${rowSessions.operation == 'TreeDiscoveryProcessor'}">
                                        <a href="treeDiscoverySession.jsp?sessionId=<c:out value="${rowSessions.id}"/>&state=<c:out value="${rowSessions.state}"/>&refresh=true"><img border="0" src="../img/show.gif" alt="View"/></a>
                                    </c:if>
                                    <c:if test="${rowSessions.operation == 'GetDeviceDetails'}">
                                        <a href="getDeviceDetailsSession.jsp?sessionId=<c:out value="${rowSessions.id}"/>&state=<c:out value="${rowSessions.state}"/>&refresh=true"><img border="0" src="../img/show.gif" alt="View"/></a>
                                    </c:if>
                                </td>
                                <td>
                                    <a href="javascript:deleteSession(<c:out value="${rowSessions.id}"/>, '<c:out value="${rowSessions.operation}"/>')"><img border="0" src="../img/delete.gif" alt="Delete"/></a>
                                </td>
                            </tr>
                            </c:if>
                        </c:forEach>
                    </table>
                </td>
            </tr>
        </table>
    
    <%@include file="bottom.jsp"%>
    
    </body>

    <c:choose>
        <c:when test="${disable == 'true'}">
            <head>
                <META HTTP-EQUIV="refresh" CONTENT="10;URL=main.jsp" />
                <script type="text/javascript" language="JavaScript">
                    disableStartNewSession();
                </script>
            </head>
        </c:when>
        <c:otherwise>
            <script type="text/javascript"  language="JavaScript">
                enableStartNewSession();
            </script>
        </c:otherwise>
    </c:choose>

    <script type="text/javascript" language="JavaScript">
        parent.top_bar.document.location="top.jsp";
    </script>

</html>


