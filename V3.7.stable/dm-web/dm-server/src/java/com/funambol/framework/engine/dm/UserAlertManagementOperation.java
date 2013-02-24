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

package com.funambol.framework.engine.dm;

import com.funambol.framework.core.AlertCode;

/**
 * ManagementOperation for Alerts.
 *
 * @version $Id: UserAlertManagementOperation.java,v 1.5 2006/11/15 13:46:05 nichele Exp $
 */
public class UserAlertManagementOperation extends TreeManagementOperation {
    // --------------------------------------------------------------- Constants

    protected final String INPUT_TYPES = "ANDTPI";
    protected final String ECHO_TYPES  = "TP"    ;

    // ------------------------------------------------------------ Private Data
    private static final String DESCRIPTION = "Alert";

    // ---------------------------------------------------------- Public Methods
    public String getDescription() {
      return DESCRIPTION;
    }


    /**
     * The messages that must be displayed to the user
     */
    protected String[] alerts;

    /**
     * Minimum display time (-1 for not specified)
     */
    protected int minDisplayTime;

    /**
     * Maximum display time (-1 for not specified)
     */
    protected int maxDisplayTime;

    /**
     * Default response (null for not specified)
     */
    protected String defaultResponse;

    /**
     * Max length (-1 for not specified)
     */
    protected int maxLength;

    /**
     * Input type (' ' for not specified). See protocol for content meaning.
     * Possible values are here summarized:
     * <ul>
     *  <li>A - alphanumeric</li>
     *  <li>N - numeric</li>
     *  <li>D - date in the form <i>DDMMYYYY</i></li>
     *  <li>T - time in the form <i>hhmmss</i></li>
     *  <li>P - phone number (the following chars included: "+", "p", "w", "s")</li>
     *  <li>I - IP address in the form xxx.yyy.www.zzz
     * </ul>
     */
    protected char inputType;

    /**
     * Echo type (' ' for not specified). One of the following values:
     * <ul>
     *  <li>T - text</li>
     *  <li>P - password</li>
     * </ul>
     */
    protected char echoType;

    /**
     * The alert code
     */
    protected int alertCode;

    // ------------------------------------------------------------ Constructors
    public UserAlertManagementOperation() {
    }

    /**
     * Creates a new UserAlertManagementOperation with minimal information.
     * It is the same as:
     * <code>
     *   UserAlertManagementOperation(alertCode, alerts, -1, -1, null, -1, ' ', ' ')
     * </code>
     *
     * @param alertCode the alert code to use
     * @param alerts
     *
     * @throws IllegalArgumentException if any of the given parameter is
     *         not correct
     */
    public UserAlertManagementOperation(final int alertCode, final String[] alerts)
    throws IllegalArgumentException {
        this(alertCode, alerts, -1, -1, null, -1, ' ', ' ');
    }

    /**
     * Creates a new UserAlertManagementOperation with the complete parameter
     * list.
     *
     * @param alertCode the alert code
     * @param alerts alert texts
     * @param mindt minimum display time
     * @param maxdt maximum display time
     * @param dr default response
     * @param maxlen maximum length
     * @param it input type
     * @param et echo type
     *
     * @throws IllegalArgumentException if any of the given parameter is
     *         not correct
     */
    public UserAlertManagementOperation(
        int      alertCode,
        String[] alerts   ,
        int      mindt    ,
        int      maxdt    ,
        String   dr       ,
        int      maxlen   ,
        char     it       ,
        char     et
    ) {
        if (!AlertCode.isUserAlertCode(alertCode)) {
            throw new IllegalArgumentException(alertCode + " is not a user alert code");
        }

        if ((it != ' ') && (INPUT_TYPES.indexOf(it) < 0)) {
            throw new IllegalArgumentException( "Input type '"
                                              + it
                                              + "' is not one of "
                                              + INPUT_TYPES
                                              );
        }

        if ((et != ' ') && (ECHO_TYPES.indexOf(et) < 0)) {
            throw new IllegalArgumentException( "Echo type '"
                                              + et
                                              + "' is not one of "
                                              + ECHO_TYPES
                                              );
        }

        this.alertCode = alertCode;

        if (alerts == null) {
            this.alerts = new String[0];
        } else {
            this.alerts = alerts;
        }

        minDisplayTime  = mindt ;
        maxDisplayTime  = maxdt ;
        defaultResponse = dr    ;
        maxLength       = maxlen;
        inputType       = it    ;
        echoType        = et    ;
    }


    // ---------------------------------------------------------- Public methods

    /**
     * Getter for property minimumDisplayTime.
     * @return Value of property minimumDisplayTime.
     */
    public int getMinDisplayTime() {
        return minDisplayTime;
    }

    /**
     * Setter for property minimumDisplayTime.
     * @param minDisplayTime the new value of property minimumDisplayTime
     */
    public void setMinDisplayTime(int minDisplayTime) {
        this.minDisplayTime = minDisplayTime;
    }

    /**
     * Getter for property alerts.
     * @return Value of property alerts.
     */
    public String[] getAlerts() {
        return this.alerts;
    }

    /**
     * Setter for property alerts.
     * @param alerts New value of property alerts.
     */
    public void setAlerts(String[] alerts) {
        this.alerts = alerts;
    }

    /**
     * Getter for property maximumDisplayTime.
     * @return Value of property maximumDisplayTime.
     */
    public int getMaxDisplayTime() {
        return maxDisplayTime;
    }

    /**
     * Setter for property maxDisplayTime.
     * @param maxDisplayTime the new maximumDisplayTime.
     */
    public void setMaxDisplayTime(int maxDisplayTime) {
        this.maxDisplayTime = maxDisplayTime;
    }

    /**
     * Getter for property defaultResponse.
     * @return Value of property defaultResponse.
     */
    public String getDefaultResponse() {
        return defaultResponse;
    }

    /**
     * Setter for property defaultResponse.
     * @param defaultResponse New value of property defaultResponse.
     */
    public void setDefaultResponse(String defaultResponse) {
        this.defaultResponse = defaultResponse;
    }

    /**
     * Getter for property maxLength.
     * @return Value of property maxLength.
     */
    public int getMaxLength() {
        return maxLength;
    }

    /**
     * Setter for property maxLength.
     * @param maxLength New value of property maxLength.
     */
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    /**
     * Getter for property inputType.
     * @return Value of property inputType.
     */
    public char getInputType() {
        return inputType;
    }

    /**
     * Setter for property inputType.
     * @param inputType New value of property inputType.
     */
    public void setInputType(char inputType) {
        this.inputType = inputType;
    }

    /**
     * Getter for property echoType.
     * @return Value of property echoType.
     */
    public char getEchoType() {
        return echoType;
    }

    /**
     * Setter for property echoType.
     * @param echoType New value of property echoType.
     */
    public void setEchoType(char echoType) {
        this.echoType = echoType;
    }

        /**
     * Getter for property alertCode.
     * @return Value of property alertCode.
     */
    public int getAlertCode() {
        return alertCode;
    }

    /**
     * Setter for property alertCode.
     * @param alertCode New value of property alertCode.
     */
    public void setAlertCode(int alertCode) {
        this.alertCode = alertCode;
    }

    // --------------------------------------------------- Public static methods

    /**
     * Creates a UserAlertManagementOperation for a DISPLAY alert. No options
     * are specified
     *
     * @param text the text to display
     *
     * @return the created UserAlertManagementOperation object
     *
     */
    public static UserAlertManagementOperation getDisplay(final String text) {
        return getDisplay(text, -1, -1);
    }

    /**
     * Creates a UserAlertManagementOperation for a DISPLAY alert with options.
     *
     * @param text the text to display
     * @param mindt minimum display time
     * @param maxdt maximum display time
     *
     * @return the created UserAlertManagementOperation object
     *
     */
    public static UserAlertManagementOperation getDisplay(
        final String text ,
        final int    mindt,
        final int    maxdt) {

        return getUserAlert(
            AlertCode.DISPLAY    ,
            new String[] { text },
            mindt                , /* MINDT  */
            maxdt                , /* MAXDT  */
            null                 , /* DR     */
            -1                   , /* MAXLEN */
            ' '                  , /* IT     */
            ' '                    /* ET     */
        );
    }

    /**
     * Creates a UserAlertManagementOperation for a DISPLAY alert. No options
     * are specified
     *
     * @param text the text to display
     *
     * @return the created UserAlertManagementOperation object
     *
     */
    public static UserAlertManagementOperation getConfirm(final String text) {
        return getConfirm(text, -1, -1, null);
    }

    /**
     * Creates a UserAlertManagementOperation for a DISPLAY alert with options.
     *
     * @param text the text to display
     * @param mindt minimum display time
     * @param maxdt maximum display time
     *
     * @return the created UserAlertManagementOperation object
     *
     */
    public static UserAlertManagementOperation getConfirm(
        final String text ,
        final int    mindt,
        final int    maxdt,
        final String dr   ) {

        return getUserAlert(
            AlertCode.CONFIRM_OR_REJECT,
            new String[] { text }      ,
            mindt                      , /* MINDT  */
            maxdt                      , /* MAXDT  */
            dr                         , /* DR     */
            -1                         , /* MAXLEN */
            ' '                        , /* IT     */
            ' '                          /* ET     */
        );
    }


    /**
     * Creates a UserAlertManagementOperation for an INPUT alert. No options
     * are specified
     *
     * @param text the text to display
     *
     * @return the created UserAlertManagementOperation object
     *
     */
    public static UserAlertManagementOperation getInput(final String text) {
        return getInput(text, -1, -1, null, -1, ' ', ' ');
    }

    /**
     * Creates a UserAlertManagementOperation for an INPUT alert with options.
     *
     * @param text the text to display
     * @param mindt minimum display time
     * @param maxdt maximum display time
     * @param dr default response
     * @param maxlen maximum length
     * @param it input type
     * @param et echo type
     *
     * @return the created UserAlertManagementOperation object
     *
     */
    public static UserAlertManagementOperation getInput(
        final String text  ,
        final int    mindt ,
        final int    maxdt ,
        final String dr    ,
        final int    maxlen,
        final char   it    ,
        final char   et    ) {

        return getUserAlert(
            AlertCode.INPUT      ,
            new String[] { text },
            mindt                , /* MINDT  */
            maxdt                , /* MAXDT  */
            dr                   , /* DR     */
            maxlen               , /* MAXLEN */
            it                   , /* IT     */
            et                     /* ET     */
        );
    }

    /**
     * Creates a UserAlertManagementOperation for a CHOICE alert. No options
     * are specified
     *
     * @param text the text to display
     * @param options the available options
     *
     * @return the created UserAlertManagementOperation object
     *
     */
    public static UserAlertManagementOperation getChoice(
        final String   text   ,
        final String[] options
    ) {
        return getChoice(text, options, -1, -1, null);
    }

    /**
     * Creates a UserAlertManagementOperation for a CHOICE alert with options.
     *
     * @param text the text to display
     * @param mindt minimum display time
     * @param maxdt maximum display time
     * @param dr default response
     *
     * @return the created UserAlertManagementOperation object
     *
     */
    public static UserAlertManagementOperation getChoice(
        final String   text   ,
        final String[] options,
        final int      mindt  ,
        final int      maxdt  ,
        final String   dr     ) {

        if ((options == null) || (options.length == 0)) {
            throw new IllegalArgumentException("options cannot be null or zero length");
        }

        String[] alerts = new String[options.length+1];

        alerts[0] = text;
        System.arraycopy(options, 0, alerts, 1, options.length);

        return getUserAlert(
            AlertCode.SINGLE_CHOICE,
            alerts          ,
            mindt           , /* MINDT  */
            maxdt           , /* MAXDT  */
            dr              , /* DR     */
            -1              , /* MAXLEN */
            ' '             , /* TI     */
            ' '               /* TE     */
        );
    }

    /**
     * Creates a UserAlertManagementOperation for a CHOICE alert with options.
     *
     * @param text the text to display
     * @param mindt minimum display time
     * @param maxdt maximum display time
     * @param dr default response
     *
     * @return the created UserAlertManagementOperation object
     *
     */
    public static UserAlertManagementOperation getMultiChoice(
        final String   text   ,
        final String[] options,
        final int      mindt  ,
        final int      maxdt  ,
        final String   dr     ) {

        if ((options == null) || (options.length == 0)) {
            throw new IllegalArgumentException("options cannot be null or zero length");
        }

        String[] alerts = new String[options.length+1];

        alerts[0] = text;
        System.arraycopy(options, 0, alerts, 1, options.length);

        return getUserAlert(
            AlertCode.MULTIPLE_CHOICE,
            alerts          ,
            mindt           , /* MINDT  */
            maxdt           , /* MAXDT  */
            dr              , /* DR     */
            -1              , /* MAXLEN */
            ' '             , /* TI     */
            ' '               /* TE     */
        );
    }

    // ------------------------------------------------ Protected static methods

    /**
     * Creates a UserAlertManagementOperation from the given parameters.
     *
     * @param alertCode the alert code
     * @param alerts alert texts
     * @param mindt minimum display time
     * @param maxdt maximum display time
     * @param dr default response
     * @param maxlen maximum length
     * @param it input type
     * @param et echo type
     *
     * @return the created UserAlertManagementOperation object
     */
    protected static UserAlertManagementOperation getUserAlert(
        final int      alertCode,
        final String[] alerts   ,
        final int      mindt    ,
        final int      maxdt    ,
        final String   dr       ,
        final int      maxlen   ,
        final char     it       ,
        final char     et
    ) {
        return new UserAlertManagementOperation(
                   alertCode,
                   alerts   ,
                   mindt    ,
                   maxdt    ,
                   dr       ,
                   maxlen   ,
                   it       ,
                   et
        );
    }

}
