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
 
package com.funambol.server.engine.dm;


import bsh.BshClassManager;
import bsh.Interpreter;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.Principal;

import com.funambol.framework.core.Alert;
import com.funambol.framework.core.dm.ddf.DevInfo;
import com.funambol.framework.config.Configuration;
import com.funambol.framework.config.ConfigClassLoader;
import com.funambol.framework.engine.dm.DeviceDMState;
import com.funambol.framework.engine.dm.ManagementProcessor;
import com.funambol.framework.engine.dm.ManagementException;
import com.funambol.framework.engine.dm.ManagementOperation;
import com.funambol.framework.engine.dm.ManagementOperationResult;
import com.funambol.framework.engine.dm.SessionContext;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;


/**
 * This is a concrete implementation of the ManagementProcessor interface.
 * It uses a scripting language to carry on the required management logic.
 * <p>
 * Currently supported scripting language is BeanShell. More scripting languages
 * can be added in the future.
 * <p>
 * The interpreter is created once in the ManagementProcessor's beginSession()
 * method and is initialized with the scripting variable listed below.
 * In addition, the script specified in the scriptFile property is called.
 * Scripts are located under the Funambol config path. Details for the BeanShell
 * engine are given later.
 * <p>
 * The script specified in scriptFile must have three entry points:
 * init(), getNextOperations() and setOperationResults(). In order to keep the
 * interaction between ScriptManagementProcessor and the the underlying
 * scripting engine, input and output values are passed by variables and not as
 * input parameters and return velues.
 * <p>
 * <g>Scripting Variables</g>
 * The following scripting variables are set just after scripting engine
 * creation:
 * <ul>
 * <li>processor - the ManagementProcessor instance reference</li>
 * <li>principal - user principal who is going to be managed</li>
 * <li>devInfo - device info of the device which is going to be managed</li>
 * <li>managementType - value given by the device when starting the management
 *     session (such as server or client initiated management session)</li>
 * <li>config - the Configuration object used to get server side beans</li>
 * <li>sessionId - The current session identifier</li>
 * <li>log - The FunambolLogger to use for logging</li>
 * <li>dmstate - The DeviceDMState object associated to the session</li>
 *
 * </ul>
 * The following scripting variables are input/output variables that the
 * management script and the management processor exchange:
 * <ul>
 * <li>operations (OUT) - ManagementOperation[] to be sent to the device
 *                       management engine
 * <li>results (IN) - ManagementResult[] returned by the device management
 *                     engine</li>
 * </ul>
 *
 * @version $Id: ScriptManagementProcessor.java,v 1.4 2006/11/15 15:27:39 nichele Exp $
 */
public class ScriptManagementProcessor
implements ManagementProcessor, java.io.Serializable {

    // --------------------------------------------------------------- Constants

    public static final String VAR_PROCESSOR       = "processor"     ;
    public static final String VAR_SESSIONID       = "sessionId"     ;
    public static final String VAR_PRINCIPAL       = "principal"     ;
    public static final String VAR_MANAGEMENT_TYPE = "managementType";
    public static final String VAR_DEVINFO         = "devInfo"       ;
    public static final String VAR_CONFIG          = "config"        ;
    public static final String VAR_OPERATIONS      = "operations"    ;
    public static final String VAR_RESULTS         = "results"       ;
    public static final String VAR_LOG             = "log"           ;
    public static final String VAR_DM_STATE        = "dmstate"       ;
    public static final String VAR_GENERIC_ALERTS  = "genericAlerts" ;
    public static final String VAR_SESSION_CONTEXT = "context"       ;

    public static final String SCRIPT_INIT           = "init()"               ;
    public static final String SCRIPT_NEXTOPERATIONS = "getNextOperations()"  ;
    public static final String SCRIPT_SETRESULTS     = "setOperationResults()";
    public static final String SCRIPT_SETGENERICALERTS = "setGenericAlerts()"   ;
    // endSession not endSession(): (int i) will be added in the method definition
    public static final String SCRIPT_ENDSESSION     = "endSession";

    // ------------------------------------------------------------ Private data

    private Interpreter interpreter;

    private static final Logger log = Logger.getLogger(com.funambol.server.engine.dm.ScriptManagementProcessor.class.getName());

    // -------------------------------------------------------------- Properties

    /**
     * Management processor name
     */
    private String name;

    /**
     * Sets management processor name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @see ManagementProcessor
     */
    public String getName() {
        return name;
    }

    /**
     * Script file. This path is relative to the config path
     */
    private String scriptFile;

    /**
     * Sets scriptFile
     *
     * @param scriptFile the new value
     */
    public void setScriptFile(String scriptFile) {
        this.scriptFile = scriptFile;
    }

    /**
     * Returns scriptFile
     *
     * @returns scriptFile property value
     */
    public String getScriptFile() {
        return scriptFile;
    }

    // ------------------------------------------------------------ Constructors

    /** Creates a new instance of ScriptMAnagementProcessor */
    public ScriptManagementProcessor() {
    }

    // ----------------------------------------------------- ManagementProcessor
    /**
     * Creates and initializes a new BeanShell interpreter.
     *
     * @see ManagementProcessor
     */
    public void beginSession(SessionContext context)
    throws ManagementException {
        
        String        sessionId = context.getSessionId();
        Principal     p         = context.getPrincipal();
        int           type      = context.getType();
        DevInfo       devInfo   = context.getDevInfo();
        DeviceDMState dmstate   = context.getDmstate();
        
        if (log.isEnabled(Level.TRACE)) {
            log.trace("Starting a new management session");
            log.trace("sessionContext: " + context         );
        }

        Configuration c = Configuration.getConfiguration();

        ConfigClassLoader cl = (ConfigClassLoader)c.getClassLoader();

        URL[] urls = cl.getURLs();

        interpreter = new Interpreter();

        //
        // 1. We add all the configuration paths to the BeanShell path
        //
        if (urls != null) {
            BshClassManager cm = interpreter.getClassManager();
            for (int i=0; i<urls.length; ++i) {
                //
                // NOTE: wrong path will be ignored
                //
                try {
                    cm.addClassPath(urls[i]);
                } catch (Exception e) {
                    if (log.isEnabled(Level.INFO)) {
                        log.info( "Wrong path "
                                + urls[i]
                                + " will not be added to the interpreter classpath"
                                );
                    }
                }
            }
        }

        //
        // 2. We set global objects and variables
        //
        try {
            interpreter.set(VAR_PROCESSOR,       this     );
            interpreter.set(VAR_SESSIONID,       sessionId);
            interpreter.set(VAR_PRINCIPAL,       p        );
            interpreter.set(VAR_MANAGEMENT_TYPE, type     );
            interpreter.set(VAR_DEVINFO,         devInfo  );
            interpreter.set(VAR_CONFIG,          c        );
            interpreter.set(VAR_LOG,             log      );
            interpreter.set(VAR_DM_STATE,        dmstate  );
            interpreter.set(VAR_OPERATIONS,      null     );
            interpreter.set(VAR_RESULTS,         null     );
            interpreter.set(VAR_GENERIC_ALERTS,  null     );
            interpreter.set(VAR_SESSION_CONTEXT, context  );
        } catch (Exception e) {
            throw new ManagementException(e.getMessage(), e);
        }

        //
        // 3. We evaluate the script file if any and run the init() function
        //
        if ((scriptFile != null) && (scriptFile.trim().length()>0)) {
            InputStream is = cl.getResourceAsStream(scriptFile);

            if (is == null) {
                if (log.isEnabled(Level.INFO)) {
                    log.info( "Initialization script file "
                            + scriptFile
                            + " not found in config path"
                            );
                }
            } else {
                try {
                    interpreter.eval(new InputStreamReader(is));
                    interpreter.eval(SCRIPT_INIT);
                } catch (Exception e) {
                    throw new ManagementException(e.getMessage(), e);
                } finally {
                    try {
                        is.close();
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    /**
     * @see ManagementProcessor
     */
    public void endSession(int completionCode) throws ManagementException {
        String script = SCRIPT_ENDSESSION + "(" + completionCode + ")";
        try {
            interpreter.eval(script);
        } catch (Exception e) {
            throw new ManagementException(e.getMessage(), e);
        }

        interpreter = null;
    }

    /**
     * @see ManagementProcessor
     */
    public ManagementOperation[] getNextOperations()
    throws ManagementException {
        try {
            interpreter.eval(SCRIPT_NEXTOPERATIONS);

            return (ManagementOperation[])interpreter.get(VAR_OPERATIONS);
        } catch (Exception e) {
            throw new ManagementException(e.getMessage(), e);
        }
    }

    /**
     * @see ManagementProcessor#setOperationResults(ManagementOperationResult[])
     */
    public void setOperationResults(ManagementOperationResult[] results)
    throws ManagementException {
        try {
            
            interpreter.set(VAR_RESULTS, results);
            interpreter.eval(SCRIPT_SETRESULTS);
        } catch (Exception e) {
            throw new ManagementException(e.getMessage(), e);
        }
    }


    /**
     * @see ManagementProcessor#setGenericAlert(com.funambol.framework.core.Alert[])
     */
    public void setGenericAlert(Alert[] genericAlerts) 
    throws ManagementException {
        
        try {
            interpreter.set(VAR_GENERIC_ALERTS, genericAlerts);
            interpreter.eval(SCRIPT_SETGENERICALERTS);
        } catch (Exception e) {
            throw new ManagementException(e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------ Private data

}
