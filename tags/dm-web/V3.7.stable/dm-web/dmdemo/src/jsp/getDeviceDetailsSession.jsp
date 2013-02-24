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

<%-- $Id: getDeviceDetailsSession.jsp,v 1.5 2007-06-18 16:38:45 luigiafassina Exp $ --%>

<%@ taglib prefix="sql" uri="/WEB-INF/tlds/sql.tld" %>
<%@ taglib prefix="c" uri="/WEB-INF/tlds/c.tld" %>
<%@ taglib prefix="fmt" uri="/WEB-INF/tlds/fmt.tld" %>

<%@ page import="java.io.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.beans.*" %>

<%@ page import="org.apache.taglibs.standard.tag.common.sql.ResultImpl" %>

<%@ page import="com.funambol.framework.engine.dm.*" %>
<%@ page import="com.funambol.framework.core.*" %>

<%@ page errorPage = "ErrorPage.jsp" %>

<fmt:setLocale value="en_EN" />
<fmt:setBundle basename="DMDemo" />

<jsp:useBean id="dmDemoBean" scope="session" class="com.funambol.dmdemo.DMDemoBean"/>

<%
    // Set the datasource in session
    session.setAttribute("dataSource", dmDemoBean.getDataSource());

    String newSession  = request.getParameter("newSession");

    String deviceId    = request.getParameter("deviceId");
    String sessionId   = request.getParameter("sessionId");

    String save        = request.getParameter("save");

    String refresh = request.getParameter("refresh");

    if (deviceId != null) {
        dmDemoBean.setDeviceId(deviceId);
    } else {
        deviceId = dmDemoBean.getDeviceId();
    }

    if (newSession != null && newSession.equalsIgnoreCase("true")) {
        dmDemoBean.startNewManagementSession(deviceId, com.funambol.dmdemo.DMDemoBean.PROCESSOR_GET_DEVICE_DETAILS);
        deviceId  = dmDemoBean.getDeviceId();
        dmDemoBean.saveSession();

        %>
            <jsp:forward page="main.jsp" />
        <%

    }

    if (save != null && save.equalsIgnoreCase("true")) {
        dmDemoBean.saveSession();
    }

    if (refresh != null && refresh.equalsIgnoreCase("true")) {
        if (sessionId != null && !sessionId.equals("")) {
            dmDemoBean.setSessionId(sessionId);
        } else {
            sessionId = dmDemoBean.getSessionId();
        }
        dmDemoBean.loadSession(sessionId);
    }

    sessionId = dmDemoBean.getSessionId();

    String stateDescription = dmDemoBean.getStateDescription();

%>

<c:set var="state"><%= dmDemoBean.getSessionState() %></c:set>

<html>
<head>
<title><fmt:message key="WINDOW_TITLE" /></title>
<link rel="stylesheet" type="text/css" href="../css/style.css" />
</head>

<script type="text/javascript"  language="JavaScript">

    function refreshList() {
        location.href = 'getDeviceDetailsSession.jsp?deviceId=<%= deviceId%>&refresh=true';
    }


    function goBack() {
        //showProp(document.documentElement, 'document.documentElement');
        document.location.href = 'main.jsp';
    }
</script>
</head>

<body>
    <form name="operations" method="get" action="getDeviceDetailsSession.jsp">
        <input type="hidden" name="addOp" value="true" />
        <input type="hidden" name="deviceId" value="<%=deviceId%>" />
        <input type="hidden" name="sessionId" value="<%= sessionId%>" />
        <input type="hidden" name="state" value='<c:out value="${state}"/>' />

    <table width="100%" border="0" bordercolor="blue">
        <tr>
            <td align="center">
                <table cellpadding="2" cellspacing="0">
                    <tr>
                        <td><fmt:message key="DEVICE_ID" />:</td>
                        <td><%= deviceId%></td>
                    </tr>
                     <tr>
                        <td><fmt:message key="SESSION_ID" />:</td><td><%= sessionId%></td>
                    </tr>
                    <tr>
                        <td><fmt:message key="STATE" />:</td>
                        <td>
                            <c:if test="${state == 'P'}">
                                <fmt:message key="STATE_TO_PROCESS" />
                            </c:if>
                            <c:if test="${state == 'C'}">
                                <fmt:message key="STATE_COMPLETED" />
                            </c:if>
                            <c:if test="${state == 'N'}">
                                <fmt:message key="STATE_NOTIFIED" />
                            </c:if>
                            <c:if test="${state == 'E'}">
                                <fmt:message key="ERROR" />
                            </c:if>
                            <c:if test="${state == 'A'}">
                                <fmt:message key="ABORTED" />
                            </c:if>
                        </td>
                    </tr>
                    <tr>
                        <td><fmt:message key="OPERATION" />:</td><td><fmt:message key="GET_DEVICE_DETAILS_PROCESSOR" /></td>
                    </tr>

                </table>
            </td>
        </tr>


        <c:if test="${state == 'P'}">
        </c:if>

        <tr><td><hr/></td></tr>
        <tr>
            <td align="center">
                <table cellpadding="10" cellspacing="0" border="0">
                    <tr>
                        <td><input type="button" name="back" value="<fmt:message key="CANCEL" />" onclick="goBack()" /></td>
                    </tr>
                </table>
            </td>
        </tr>

        <tr>
            <td align="center">
                <table>
                    <tr>
                        <td><fmt:message key="OPERATIONS_LIST" /></td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td align="center">
                <table cellpadding="5" cellspacing="0" border="1">
                    <tr>
                        <th><fmt:message key="OPERATION" /></th>
                        <th><fmt:message key="NODE_URI" /></th>
                        <c:choose>
                            <c:when test="${state == 'P'}">
                            </c:when>
                            <c:otherwise>
                                <th><fmt:message key="RESULTS" /></th>
                                <th><fmt:message key="STATUS_CODE" /></th>
                            </c:otherwise>
                        </c:choose>
                    </tr>
                    <%
                        ManagementOperation operation = null;
                        ManagementOperationResult operationResult = null;
                        String command = null;

                        String value = null;
                        int numNodes = 0;
                        java.util.Map nodes = null;
                        java.util.Iterator itNodes = null;

                        List operationsList        = dmDemoBean.genericProcessorTools.getOperationsList();
                        List operationsResultsList = null;
                        int numOfOperations        = operationsList.size();

                        if (dmDemoBean.getSessionState().equals("C") ) {
                            operationsResultsList = dmDemoBean.genericProcessorTools.getOperationsResultsWithValue();
                        }

                        //
                        // The last operation isn't showed beacause is the dummy alert
                        //
                        for (int i=0; i< (numOfOperations - 1); i++) {
                            operation = (ManagementOperation)operationsList.get(i);
                            if (!(operation instanceof TreeManagementOperation)) {
                              continue;
                            }
                            if ( dmDemoBean.getSessionState().equals("C") ) {
                                operationResult = (ManagementOperationResult)operationsResultsList.get(i);
                                nodes = operationResult.getNodes();
                                command = operationResult.getCommand();
                            } else {
                                nodes = ((TreeManagementOperation)operation).getNodes();
                                command = operation.getDescription();
                            }
                            numNodes = nodes.size();
                            value    = null;

                            itNodes  = nodes.keySet().iterator();
                        %>
                            <tr>
                                <td><%= command %></td>
                                <td>
                                    <!-- Nodes URI-->
                                    <table>
                                        <%
                                            while (itNodes.hasNext()) {
                                        %>
                                            <tr>
                                                <td><%= itNodes.next() %></td>
                                            </tr>
                                        <%
                                        }
                                        %>
                                    </table>
                                </td>

                                    <c:if test="${state == 'P'}">
                                    </c:if>
                                    <c:if test="${state == 'C'}">
                                        <%
                                            int statusCode = operationResult.getStatusCode();
                                        %>
                                        <td>
                                            <%
                                                if (command.equals("Get")) {
                                            %>
                                                    <table>
                                                        <%
                                                            itNodes  = nodes.keySet().iterator();
                                                            Object result = null;
                                                            while (itNodes.hasNext()) {
                                                                result = nodes.get(itNodes.next());
                                                                if (result instanceof TreeNode) {
                                                                    result = ((TreeNode)result).getValue();
                                                                }
                                                        %>
                                                                <tr>
                                                                    <td><%= result %></td>
                                                                </tr>

                                                        <%
                                                            }
                                                        %>
                                                    </table>
                                           <%
                                                }  else {
                                           %>
                                                     &nbsp;
                                           <%
                                                }
                                            %>
                                        </td>
                                        <td align="center"><%= statusCode%></td>
                                    </c:if>
                            </tr>
                    <%
                        }
                    %>

                </table>
            </td>
        </tr>
        <c:if test="${state == 'P'}">
            <tr>
                <td align="center">
                    <table>
                        <tr>

                        </tr>
                    </table>
                </td>
            </tr>
        </c:if>
        <tr><td><br/><br/></td></tr>
    </table>
    </form>
    
    <%@include file="bottom.jsp"%>    

</body>
</html>
