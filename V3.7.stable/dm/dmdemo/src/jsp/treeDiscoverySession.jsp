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

<%-- $Id: treeDiscoverySession.jsp,v 1.5 2007-06-18 16:38:45 luigiafassina Exp $ --%>

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

    String newSession       = request.getParameter("newSession");
    String clone            = request.getParameter("clone");

    String sessionId   = request.getParameter("sessionId");
    String operationId = request.getParameter("operationId");
    String save        = request.getParameter("save");
    String refresh = request.getParameter("refresh");

    // Parameters to create new operation
    String addOp         = request.getParameter("addOp");

    String rootNode = request.getParameter("nodeUri");

    String deviceId = dmDemoBean.getDeviceId();


    if (refresh != null && refresh.equalsIgnoreCase("true")) {
        if (sessionId != null && !sessionId.equals("")) {
            dmDemoBean.setSessionId(sessionId);
        } else {
            sessionId = dmDemoBean.getSessionId();
        }
        dmDemoBean.loadSession(sessionId);
    }

    if (newSession != null && newSession.equalsIgnoreCase("true")) {
        dmDemoBean.startNewManagementSession(deviceId, "TreeDiscoveryProcessor");
        deviceId  = dmDemoBean.getDeviceId();
    }

    if (rootNode == null) {
        rootNode = dmDemoBean.treeDiscoveryProcessorTools.getRootNode();
        if (rootNode == null) {
            rootNode = "";
        }
    }

    if (save != null && save.equalsIgnoreCase("true")) {
        dmDemoBean.treeDiscoveryProcessorTools.setRootNode(rootNode);
        dmDemoBean.saveSession();
         %>
            <jsp:forward page="main.jsp" />
        <%
    }
    sessionId = dmDemoBean.getSessionId();

    String stateDescription = dmDemoBean.getStateDescription();
%>


<%-- Checks "Clone" request --%>
<c:set var="clone"><%= clone %></c:set>

<c:if test="${clone == 'true'}">
    <%
        //dmDemoBean.cloneSession();
        //sessionId = dmDemoBean.getSessionId();
    %>
</c:if>
<c:set var="state"><%= dmDemoBean.getSessionState() %></c:set>

<html>
<head>
<title><fmt:message key="WINDOW_TITLE" /></title>
<link rel="stylesheet" type="text/css" href="../css/style.css" />
</head>

<script type="text/javascript"  language="JavaScript">
    function onChangeOperation(value) {
        if (value == "Get" || value == "Delete") {
            document.getElementById("100").style.visibility = "visible";
            document.getElementById("101").style.visibility = "visible";
            document.getElementById("200").style.visibility = "hidden";
            document.getElementById("201").style.visibility = "hidden";
            document.getElementById("400").style.visibility = "hidden";
        } else if (value == "Add" || value == "Replace") {
            document.getElementById("100").style.visibility = "visible";
            document.getElementById("101").style.visibility = "visible";
            document.getElementById("200").style.visibility = "visible";
            document.getElementById("201").style.visibility = "visible";
            document.getElementById("400").style.visibility = "visible";
        } else {
            document.getElementById("100").style.visibility = "hidden";
            document.getElementById("101").style.visibility = "hidden";
            document.getElementById("200").style.visibility = "hidden";
            document.getElementById("201").style.visibility = "hidden";
            document.getElementById("400").style.visibility = "hidden";
        }
    }

    function onChangeObject(value) {
        if (value == "other") {
             document.getElementById("102").style.visibility = "visible";
        } else {
             document.getElementById("102").style.visibility = "hidden";
        }
    }

    function saveSession() {
        var currentDeviceId = -1;
        if (document.getElementById("300") != null) {
            currentDeviceId = document.getElementById("300").value;
        } else {
            currentDeviceId = '<%= deviceId %>';
        }
        var nodeUri = document.forms.tree.nodeUri.value;

        if (nodeUri == '') {
            alert('<fmt:message key="INSERT_THE_NODE_URI" />');
            return;
        }
        location.href='treeDiscoverySession.jsp?save=true&deviceId=' + currentDeviceId + '&nodeUri=' + nodeUri; ;
    }

    function addOperation() {
        var currentDeviceId = -1;
        if (document.getElementById("300") != null) {
            currentDeviceId = document.getElementById("300").value;
        } else {
            currentDeviceId = '<%= deviceId %>';
        }
        document.forms.operations.deviceId.value = currentDeviceId;
        document.forms.operations.submit();
    }

    function goBack() {
        //showProp(document.documentElement, 'document.documentElement');
        document.location.href = 'main.jsp';
    }

    function onChangeRootNode(value) {
        if (value == 'other') {
            document.forms.tree.nodeUri.value = "";
        } else {
            document.forms.tree.nodeUri.value = value;
        }
    }

    function setRootNode(rootNode) {
        if (rootNode != '.' &&
            rootNode != './SyncML/DSAcc' &&
            rootNode != './SyncML/DMAcc' &&
            rootNode != './MMS') {

            document.forms.tree.rootNode.value = 'other';
            document.forms.tree.nodeUri.value = rootNode;
        } else {
            document.forms.tree.rootNode.value = rootNode;
        }
    }

</script>
</head>

<body>
    <form name="tree" method="get" action="">
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
                        <td><fmt:message key="SESSION_ID" />:</td>
                        <td><%= sessionId%></td>
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
                        <td><fmt:message key="OPERATION" />:</td><td><fmt:message key="TREEDISCOVERY_PROCESSOR" /></td>
                    </tr>

                </table>
            </td>
        </tr>
        <tr><td><hr/></td></tr>

        <tr>
            <td align="center">
                <table cellpadding="2" cellspacing="0" border="0" bordercolor="red">
                    <c:choose>
                        <c:when test="${state != 'P'}">
                            <tr>
                                <td colspan="2" align="center"><fmt:message key="ROOT_NODE" />:&nbsp;<%= rootNode %></td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td width="1%" align="center" colspan="2">
                                    <table width="1" cellpadding="0" cellspacing="5" border="0" bordercolor="green">
                                        <tr>
                                            <td width="1%" nowrap="nowrap" align="center"><fmt:message key="ROOT_NODE" />:</td>
                                            <td>
                                                <select name="rootNode" ONCHANGE="onChangeRootNode(this.value)">
                                                    <option value=".">Entire Tree</option>
                                                    <option value="./SyncML/DSAcc">Data Synchronization Info</option>
                                                    <option value="./SyncML/DMAcc">Device Management Info</option>
                                                    <option value="./MMS">MMS Info</option>
                                                    <option value="other">Other</option>
                                                </select>
                                        </td>
                                    </tr>
                                    <tr id="500" style="visibility:visible">
                                        <td width="1%" nowrap="nowrap">Node URI:</td>
                                        <td><input size="25" type="text" name="nodeUri" value="<%= rootNode %>"></td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
                <tr>
                    <td width="1%" colspan="2" align="center">
                        <table cellpadding="10" cellspacing="0" border="0" bordercolor="green">
                            <tr>
                                <c:if test="${state == 'P'}">
                                    <td>
                                        <input type="button" name="ok" value='<fmt:message key="ADD_TO_OPERATION_LIST" />' onclick="saveSession()"/>
                                    </td>
                                </c:if>
                                <td>
                                    <input type="button" name="back" value='<fmt:message key="CANCEL" />' onclick="goBack()" />
                                </td>

                            </tr>
                        </table>
                    </td>
                </tr>

                <c:if test="${state == 'C'}">
                    <tr>
                        <td align="center" width="1%" colspan="2">
                            <table width="100%" cellpadding="2" cellspacing="0" border="1">
                                <% java.util.Map results = dmDemoBean.treeDiscoveryProcessorTools.getTreeDiscoveryResults();
                                Iterator itResults    = results.keySet().iterator();
                                       String key            = null;
                                       String value          = null;
                                       while (itResults.hasNext()) {
                                           key   = (String)itResults.next();
                                           value = String.valueOf(results.get(key));
                                           if (value == null || value.equals("")) {
                                               value = "&nbsp;";
                                           }
                                    %>
                                           <tr>
                                               <td><%= key %></td>
                                               <td><%= value %></td>
                                           </tr>
                                    <%
                                       }
                                    %>
                                </table>
                            </td>
                        </tr>
                    </c:if>

                </table>
            </td>
        </tr>
    </table>
    </form>
    
    <%@include file="bottom.jsp"%>    

    <c:if test="${state == 'P'}">
        <script type="text/javascript"  language="JavaScript">
            setRootNode('<%=rootNode %>');
        </script>
    </c:if>

</body>
</html>
