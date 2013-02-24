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

<%-- $Id: genericSession.jsp,v 1.6 2007-06-18 16:38:45 luigiafassina Exp $ --%>

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
    String clone       = request.getParameter("clone");

    String deviceId    = request.getParameter("deviceId");
    String sessionId   = request.getParameter("sessionId");
    String operationId = request.getParameter("operationId");
    String deleteOp    = request.getParameter("deleteOp");
    String save        = request.getParameter("save");

    String move    = request.getParameter("move");
    String refresh = request.getParameter("refresh");

    // Parameters to create new operation
    String addOp         = request.getParameter("addOp");
    String operationName = request.getParameter("operationName");
    String nodeUri       = request.getParameter("nodeUri");
    String objectValue   = request.getParameter("objectValue");
    String objectType    = request.getParameter("objectType");

    String newNode       = request.getParameter("newNode");

    String Option1    = request.getParameter("Option1");
    String Option2    = request.getParameter("Option2");
    String Option3    = request.getParameter("Option3");

    String preconfiguredOperations = request.getParameter("preconfiguredOperations");

    if (deviceId != null) {
        dmDemoBean.setDeviceId(deviceId);
    } else {
        deviceId = dmDemoBean.getDeviceId();
    }

    if (newSession != null && newSession.equalsIgnoreCase("true")) {
        dmDemoBean.startNewManagementSession(deviceId, com.funambol.dmdemo.DMDemoBean.PROCESSOR_GENERIC);
        deviceId  = dmDemoBean.getDeviceId();
        if (preconfiguredOperations != null && !preconfiguredOperations.equals("")) {
            dmDemoBean.genericProcessorTools.setPreconfiguredOperations(preconfiguredOperations);
            dmDemoBean.saveSession();

        %>
            <jsp:forward page="main.jsp" />
        <%
        }

    }

    if (save != null && save.equalsIgnoreCase("true")) {
        dmDemoBean.saveSession();
         %>
            <jsp:forward page="main.jsp" />
        <%
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

    if (move != null && !move.equals("")) {
        int index = Integer.parseInt(operationId);
        if (move.equalsIgnoreCase("up")) {
            dmDemoBean.genericProcessorTools.moveOperationUp(index);
        } else {
            dmDemoBean.genericProcessorTools.moveOperationDown(index);
        }
    }

    if (deleteOp != null && deleteOp.equals("true")) {
        int index = -1;
        if (operationId != null) {
            index = Integer.parseInt(operationId);
        }
        dmDemoBean.genericProcessorTools.removeOperation(index);
    }

    // checks for new operations
    if (addOp != null && addOp.equals("true")) {

        if (newNode != null && newNode.equalsIgnoreCase("true")) {
            dmDemoBean.genericProcessorTools.addManagementOperation(operationName, nodeUri, objectValue, objectType, Option1, Option2, Option3, true);
        } else {
            dmDemoBean.genericProcessorTools.addManagementOperation(operationName, nodeUri, objectValue, objectType, Option1, Option2, Option3, false);
        }
    }

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

        (document.getElementById('400')).checked = false;

        if (value == "Get" || value == "Delete" ) {

            document.getElementById("100").disabled = false;
            document.getElementById("101").disabled = false;
            document.getElementById("102").disabled = false;
            document.getElementById("200").disabled = true;
            document.getElementById("201").disabled = true;
            document.getElementById("202").disabled = true;
            document.getElementById("203").disabled = true;
            document.getElementById("400").disabled = true;
            document.getElementById("500").disabled = true;
            document.getElementById("501").disabled = true;
            document.getElementById("502").disabled = true;
            document.getElementById("503").disabled = true;

        } else if (value == "Add" || value == "Replace") {

            document.getElementById("100").disabled = false;
            document.getElementById("101").disabled = false;
            document.getElementById("102").disabled = false;
            document.getElementById("200").disabled = false;
            document.getElementById("201").disabled = false;
            document.getElementById("202").disabled = false;
            document.getElementById("203").disabled = false;
            document.getElementById("400").disabled = false;
            document.getElementById("500").disabled = true;
            document.getElementById("501").disabled = true;
            document.getElementById("502").disabled = true;
            document.getElementById("503").disabled = true;

        } else if (value == "Copy") {

            document.getElementById("100").disabled = false;
            document.getElementById("101").disabled = false;
            document.getElementById("102").disabled = false;
            document.getElementById("200").disabled = false;
            document.getElementById("201").disabled = false;
            document.getElementById("202").disabled = true;
            document.getElementById("203").disabled = true;
            document.getElementById("400").disabled = true;
            document.getElementById("500").disabled = true;
            document.getElementById("501").disabled = true;
            document.getElementById("502").disabled = true;
            document.getElementById("503").disabled = true;

        } else if (value == "Exec") {

            document.getElementById("100").disabled = false;
            document.getElementById("101").disabled = false;
            document.getElementById("102").disabled = false;
            document.getElementById("200").disabled = false;
            document.getElementById("201").disabled = false;
            document.getElementById("202").disabled = true;
            document.getElementById("203").disabled = true;
            document.getElementById("400").disabled = true;
            document.getElementById("500").disabled = false;
            document.getElementById("501").disabled = false;
            document.getElementById("502").disabled = true;
            document.getElementById("503").disabled = true;

        } else if (value == 'ShowAlertMessage' || value == 'ShowConfirmMessage'
            || value == 'ShowInputMessage') {

            document.getElementById("100").disabled = true;
            document.getElementById("101").disabled = true;
            document.getElementById("102").disabled = true;
            document.getElementById("200").disabled = false;
            document.getElementById("201").disabled = false;
            document.getElementById("202").disabled = true;
            document.getElementById("203").disabled = true;
            document.getElementById("400").disabled = true;
            document.getElementById("500").disabled = true;
            document.getElementById("501").disabled = true;
            document.getElementById("502").disabled = true;
            document.getElementById("503").disabled = true;

        } else if (value == 'ShowChoiceMessage') {

            document.getElementById("100").disabled = true;
            document.getElementById("101").disabled = true;
            document.getElementById("102").disabled = true;
            document.getElementById("200").disabled = false;
            document.getElementById("201").disabled = false;
            document.getElementById("202").disabled = true;
            document.getElementById("203").disabled = true;
            document.getElementById("400").disabled = true;
            document.getElementById("500").disabled = false;
            document.getElementById("501").disabled = false;
            document.getElementById("502").disabled = false;
            document.getElementById("503").disabled = false;

        } else {

            document.getElementById("100").disabled = true;
            document.getElementById("101").disabled = true;
            document.getElementById("102").disabled = true;
            document.getElementById("200").disabled = true;
            document.getElementById("201").disabled = true;
            document.getElementById("202").disabled = true;
            document.getElementById("203").disabled = true;
            document.getElementById("400").disabled = true;
            document.getElementById("500").disabled = true;
            document.getElementById("501").disabled = true;
            document.getElementById("502").disabled = true;
            document.getElementById("503").disabled = true;
        }
    }

    function onChangeObject(value) {
        if (value != 'other') {
            document.operations.nodeUri.value = value;
        } else {
            document.operations.nodeUri.value = '';
        }
    }

    function checkNewNode(value) {
        if (value.checked) {
            document.getElementById("200").disabled = true;
            document.getElementById("201").disabled = true;
            document.getElementById("202").disabled = true;
            document.getElementById("203").disabled = true;
        } else {
            document.getElementById("200").disabled = false;
            document.getElementById("201").disabled = false;
            document.getElementById("202").disabled = false;
            document.getElementById("203").disabled = false;
        }
    }

    function refreshList() {
        location.href = 'genericSession.jsp?deviceId=<%= deviceId%>&refresh=true';
    }

    function saveSession() {
        var currentDeviceId = -1;
        if (document.getElementById("300") != null) {
            currentDeviceId = document.getElementById("300").value;
        } else {
            currentDeviceId = '<%= deviceId %>';
        }
        location.href='genericSession.jsp?save=true&deviceId=' + currentDeviceId;
    }

    function addOperation() {

        var operation = document.operations.operationName.value;

        if (document.operations.nodeUri.value == '' &&
            (operation == 'Add' || operation == 'Get' || operation == 'Replace' || operation == 'Delete')
            ) {
            alert('<fmt:message key="INSERT_THE_NODE_URI" />');
            return ;
        }

        var valuesOk = checkValues();

        if (valuesOk != true) {
            return;
        }

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
        document.location.href = 'main.jsp';
    }

    function checkValues() {
        var value = "";
        var type  = "";

        value = document.getElementById("201").value;
        type  = document.getElementById("203").value;

        if (type == 'Bool') {
            var isOk = checkBoolean(value);
            if (isOk == false) {
                alert('<fmt:message key="WRONG_BOOLEAN_VALUE" />: ' + value);
                return false;
            }
        } else if (type == 'Int') {
            var isOk = checkInteger(value);
            if (isOk == false) {
                alert('<fmt:message key="WRONG_INTEGER_VALUE" />: ' + value);
                return false;
            }
        }

        return true;
    }

    function checkInteger(value) {
        var nums = "0123456789";

        for (i = 0;  i < value.length;  i++) {
            ch = value.charAt(i);
            for (j = 0;  j < nums.length;  j++) {
                if (ch == nums.charAt(j)) {
                    break;
                }

                if (j == (nums.length - 1)) {
                    return false;
                }

            }
        }
        return true;
    }


    function checkBoolean(value) {

        value = value.toUpperCase();

        if (value != 'TRUE' && value != 'FALSE') {
            return false;
        }
        return true;
    }

</script>
</head>

<body>
    <form name="operations" method="post" action="genericSession.jsp">
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
                        <td><fmt:message key="OPERATION" />:</td><td><fmt:message key="CUSTOM_PROCESSOR" /></td>
                    </tr>

                </table>
            </td>
        </tr>


        <c:if test="${state == 'P'}">
            <tr>
                <td align="center">
                    <table>
                    <tr>
                    <td align="center">
                    <div style="border: 1px solid gray;" align="center">
                    <table border="0" bordercolor="green" cellpadding="5" cellspacing="0">
                        <tr>
                            <td><fmt:message key="NEW_OPERATION" />:</td>
                            <td >
                                <select name="operationName" ONCHANGE="onChangeOperation(this.value)">
                                    <option value="Add">Add</option>
                                    <option value="Copy">Copy</option>
                                    <option value="Delete">Delete</option>
                                    <option value="Exec">Exec</option>
                                    <option value="Get">Get</option>
                                    <option value="Replace">Replace</option>
                                    <option value="ShowAlertMessage"><fmt:message key="ALERT_DISPLAY" /></option>
                                    <option value="ShowConfirmMessage"><fmt:message key="ALERT_CONFIRMATION" /></option>
                                    <option value="ShowInputMessage"><fmt:message key="ALERT_INPUT" /></option>
                                    <option value="ShowChoiceMessage"><fmt:message key="ALERT_CHOICE" /></option>
                                </select>
                            </td>
                            <td style="visibility:visible">
                                <input id="400" type="checkbox" onClick="checkNewNode(this)" name="newNode" value="true" />
                                <fmt:message key="NEW_NODE" />
                            </td>
                            <td colspan="2">&nbsp;</td>
                        </tr>
                        <tr>
                            <td id="100" style="visibility:visible"><fmt:message key="NODE_URI" />: </td>
                            <td colspan="2" style="visibility:visible" width="1">
                                <select id="101" name="objectName" ONCHANGE="onChangeObject(this.value)">
                                    <option value="other"><fmt:message key="OTHER_NODE_URI" /></option>
                                    <option value="./DevDetail">./DevDetail</option>
                                    <option value="./DevDetail/LrgObj">./DevDetail/LrgObj</option>
                                    <option value="./DevDetail/HwV">./DevDetail/HwV</option>
                                    <option value="./DevDetail/SwV">./DevDetail/SwV</option>
                                    <option value="./DevDetail/FwV">./DevDetail/FwV</option>
                                    <option value="./DevDetail/OEM">./DevDetail/OEM</option>
                                    <option value="./DevDetail/DevTyp">./DevDetail/DevTyp</option>
                                    <option value="./DevDetail/URI">./DevDetail/URI</option>
                                    <option value="./DevDetail/URI/MaxSegLen">./DevDetail/URI/MaxSegLen</option>
                                    <option value="./DevDetail/URI/MaxTotLen">./DevDetail/URI/MaxTotLen</option>
                                    <option value="./DevDetail/URI/MaxDepth">./DevDetail/URI/MaxDepth</option>
                                    <option value="./DevDetail/Bearer">./DevDetail/Bearer</option>
                                    <option value="./DevInfo">./DevInfo</option>
                                    <option value="./DevInfo/Lang">./DevInfo/Lang</option>
                                    <option value="./DevInfo/DmV">./DevInfo/DmV</option>
                                    <option value="./DevInfo/Mod">./DevInfo/Mod</option>
                                    <option value="./DevInfo/Man">./DevInfo/Man</option>
                                    <option value="./DevInfo/DevId">./DevInfo/DevId</option>
                                    <option value="./funambol">./funambol</option>
                                    <option value="./DMAcc">./DMAcc</option>
                                    <option value="./FirmwareUpdate">./FirmwareUpdate</option>
                                    <option value="./FirmwareUpdate/FWPkg1">./FirmwareUpdate/FWPkg1</option>
                                    <option value="./FirmwareUpdate/FWPkg1/PkgName">./FirmwareUpdate/FWPkg1/PkgName</option>
                                    <option value="./FirmwareUpdate/FWPkg1/PkgVersion">./FirmwareUpdate/FWPkg1/PkgVersion</option>
                                    <option value="./FirmwareUpdate/FWPkg1/Download">./FirmwareUpdate/FWPkg1/Download</option>
                                    <option value="./FirmwareUpdate/FWPkg1/Download/PkgURL">./FirmwareUpdate/FWPkg1/Download/PkgURL</option>
                                    <option value="./FirmwareUpdate/FWPkg1/Update">./FirmwareUpdate/FWPkg1/Update</option>
                                    <option value="./FirmwareUpdate/FWPkg1/Update/PkgData">./FirmwareUpdate/FWPkg1/Update/PkgDat</option>
                                    <option value="./FirmwareUpdate/FWPkg1/DownloadAndUpdate">./FirmwareUpdate/FWPkg1/DownloadAndUpdate</option>
                                    <option value="./FirmwareUpdate/FWPkg1/DownloadAndUpdate/PkgURL">./FirmwareUpdate/FWPkg1/DownloadAndUpdate/PkgURL</option>
                                    <option value="./FirmwareUpdate/FWPkg1/State">./FirmwareUpdate/FWPkg1/State</option>
                                    <option value="./FirmwareUpdate/FWPkg1/Ext">./FirmwareUpdate/FWPkg1/Ext</option>
                                </select>
                            </td>
                            <td colspan="2" style="visibility:visible">
                                <input id="102" type="textfield" size="30" name="nodeUri" />
                            </td>
                         </tr>
                         <tr>
                            <td id="200" style="visibility:visible"><fmt:message key="VALUE" />:</td>
                            <td colspan="2" style="visibility:visible"><input id="201" type="text" size="30" name="objectValue" /></td>
                            <td id="202" style="visibility:visible" width="1"><fmt:message key="TYPE" />:</td>
                            <td nowrap="nowrap" style="visibility:visible">
                                <select id="203" name="objectType">
                                    <option value="chr">String</option>
                                    <option value="int">Integer</option>
                                    <option value="bool">Boolean</option>
                                    <option value="b64">Base64</option>
                                </select>
                            </td>
                        </tr>
                         <tr>
                            <td id="500" style="visibility:visible" disabled><fmt:message key="OPTIONS" />:</td>
                            <td colspan="2" style="visibility:visible"><input id="501" type="text" size="20" name="Option1"  disabled/></td>
                            <td colspan="2" style="visibility:visible"><input id="502" type="text" size="20" name="Option2"  disabled /></td>
                            <td colspan="2" style="visibility:visible"><input id="503" type="text" size="20" name="Option3"  disabled/></td>
                         </tr>
                         <tr>
                            <td align="center" colspan="5"><input type="button" name="add" value="<fmt:message key="ADD_TO_CUSTOM_OPERATIONS" />" onclick="addOperation()"/></td>
                        </tr>
                    </table>
                    </div>
                    </td>
                    </tr>
                    </table>
                </td>
            </tr>
        </c:if>

        <tr><td><hr/></td></tr>
        <tr>
            <td align="center">
                <table cellpadding="10" cellspacing="0" border="0">
                    <tr>
                        <c:if test="${state == 'P'}">
                            <td><input type="button" name="save" value="<fmt:message key="ADD_TO_OPERATION_LIST" />" onclick="saveSession()" /></td>
                        </c:if>
                        <td><input type="button" name="back" value="<fmt:message key="CANCEL" />" onclick="goBack()" /></td>
                        <%--c:if test="${clone != 'true'}">
                           <td><input type="button" name="clone"  value="Clone"  onclick="location.href='genericSession.jsp?clone=true'" /></td>
                        </c:if--%>
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
                        <th><fmt:message key="VALUE" /></th>
                        <th><fmt:message key="OPTIONS" /></th>
                        <th>&nbsp;</th>
                        <th>&nbsp;</th>
                        <c:choose>
                            <c:when test="${state == 'P'}">
                                <th>&nbsp;</th>
                                <th>&nbsp;</th>
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
                        String commandDescription = null;

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
                        for (int i=0; i<(numOfOperations - 1); i++) {
                            operation = (ManagementOperation)operationsList.get(i);
                            if (!(operation instanceof TreeManagementOperation)) {
                              continue;
                            }
                            if ( dmDemoBean.getSessionState().equals("C") ) {
                                operationResult = (ManagementOperationResult)operationsResultsList.get(i);
                                nodes = operationResult.getNodes();
                                command = operationResult.getCommand();
                                commandDescription = command;
                            } else {
                                nodes = ((TreeManagementOperation)operation).getNodes();
                                command = operation.getDescription();

                                if (operation instanceof UserAlertManagementOperation) {
                                    int alertCode = ((UserAlertManagementOperation)operation).getAlertCode();
                                    if (alertCode == AlertCode.DISPLAY) {
                                        commandDescription = "Display";
                                    } else if (alertCode == AlertCode.CONFIRM_OR_REJECT) {
                                        commandDescription = "Confirmation";
                                    } else if (alertCode == AlertCode.INPUT) {
                                        commandDescription = "Input";
                                    } else if (alertCode == AlertCode.SINGLE_CHOICE) {
                                        commandDescription = "Choice";
                                    } else {
                                        commandDescription = "Alert";
                                    }
                                } else {
                                    commandDescription = command;
                                }
                            }

                            //
                            // If the operation is a Alert, create a dummy nodes
                            //  hashmap with the alerts values
                            //
                            if (command.equalsIgnoreCase("Alert") ||
                                command.equalsIgnoreCase("Display") ||
                                command.equalsIgnoreCase("Confirmation") ||
                                command.equalsIgnoreCase("Input") ||
                                command.equalsIgnoreCase("Choice")) {

                                String[] alerts = ((UserAlertManagementOperation)operation).getAlerts();
                                nodes = new HashMap();
                                for (int t=0; t<alerts.length; t++) {
                                    nodes.put(alerts[t], alerts[t]);
                                }
                            }
                            numNodes = nodes.size();
                            value    = null;

                            itNodes  = nodes.keySet().iterator();
                        %>
                            <tr>
                                <td><%= commandDescription %></td>
                                <td>
                                    <!-- Nodes URI-->
                                    <table>
                                        <%
                                            while (itNodes.hasNext()) {
                                        %>
                                            <tr>
                                                <td>
                                                    <%
                                                        if (command.equalsIgnoreCase("Alert") ||
                                                            command.equalsIgnoreCase("Display") ||
                                                            command.equalsIgnoreCase("Confirmation") ||
                                                            command.equalsIgnoreCase("Input") ||
                                                            command.equalsIgnoreCase("Choice")
                                                        ) {
                                                            itNodes.next();
                                                    %>
                                                            &nbsp;
                                                    <%
                                                        } else {
                                                    %>
                                                            <%= itNodes.next() %>
                                                    <%
                                                        }
                                                    %>

                                                </td>
                                            </tr>
                                        <%
                                        }
                                        %>
                                    </table>
                                </td>
                                <td>
                                    <%
                                        if (!command.equals("Get")) {
                                    %>
                                            <!-- Values -->
                                            <table>
                                                <%
                                                    itNodes  = nodes.keySet().iterator();
                                                    Object oValue = null;
                                                    while (itNodes.hasNext()) {
                                                        oValue = nodes.get(itNodes.next());

                                                        if (oValue instanceof TreeNode) {
                                                            oValue = ((TreeNode)oValue).getValue();
                                                            if (oValue == null) {
                                                                oValue = "";
                                                            }
                                                        }
                                                %>
                                                    <tr>
                                                        <td><%= oValue %></td>
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
                                <td>
                                    <%
                                        if (command.equals("Exec")) {
                                            String correlator = ((ExecManagementOperation)operation).getCorrelator();
                                    %>
                                            <%= correlator %>
                                    <%
                                        }  else {
                                    %>
                                             &nbsp;
                                    <%
                                        }
                                    %>
                                </td>
                                <td>&nbsp;</td>
                                <td>&nbsp;</td>
                                <c:if test="${state == 'NEW'}">
                                        <td><a href="genericSession.jsp?operationId=<%= i%>&deleteOp=true"><img border="0" src="../img/delete.gif" alt="Delete"/></a></td>
                                        <td>
                                          <a href="genericSession.jsp?operationId=<%= i%>&move=up"><img border="0" src="../img/up.gif" alt="Move up"/></a>
                                          <a href="genericSession.jsp?operationId=<%= i%>&move=down"><img border="0" src="../img/down.gif" alt="Move down"/></a>
                                        </td>
                                    </c:if>

                                    <c:if test="${state == 'P'}">
                                        <td><a href="genericSession.jsp?operationId=<%= i%>&deleteOp=true"><img border="0" src="../img/delete.gif" alt="Delete"/></a></td>
                                        <td>
                                          <a href="genericSession.jsp?operationId=<%= i%>&move=up"><img border="0" src="../img/up.gif" alt="Move up"/></a>
                                          <a href="genericSession.jsp?operationId=<%= i%>&move=down"><img border="0" src="../img/down.gif" alt="Move down"/></a>
                                        </td>
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
                                        <td align="center">
                                            <%
                                                if (command.equalsIgnoreCase("Confirmation")) {
                                                    if (statusCode == 200)  {
                                                        out.print("Yes");
                                                    } else if (statusCode == 214) {
                                                        out.print("Cancel");
                                                    } else if (statusCode == 304) {
                                                        out.print("No");
                                                    } else {
                                                        out.print(statusCode);
                                                    }
                                                } else {
                                            %>
                                                    <%= statusCode%>
                                            <%
                                                }
                                            %>
                                        </td>
                                    </c:if>
                            </tr>
                    <%
                        }
                    %>

                </table>
            </td>
        </tr>
    </table>
    </form>
    
    <%@include file="bottom.jsp"%>

</body>
</html>
