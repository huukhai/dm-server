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


import org.apache.commons.lang.StringUtils;

import com.funambol.framework.config.Configuration;
import com.funambol.framework.config.ConfigurationConstants;
import com.funambol.framework.server.dm.ProcessorSelector;
import com.funambol.framework.engine.dm.ManagementProcessor;
import com.funambol.framework.engine.dm.DeviceDMState;
import com.funambol.framework.core.dm.ddf.DevInfo;
import com.funambol.framework.tools.beans.BeanInitializationException;
import com.funambol.framework.tools.beans.LazyInitBean;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/**
 * This is an implementation of ProcessorSelector that selects a management
 * processor looking up the management operation the management session was
 * meant for.
 * <p>
 * If no processor is found for the given session id, the processor specified by
 * <i>defaultProcessor</i> is used.
 * <p>
 * If a session is found, the processor name is computed as follows:
 * <br>
 * <i>namePrefix</i> + <i>operation</i> + <i>namePostfix</i>
 * <br>
 * Where <i>namePrefix</i> and <i>namePostfix</i> are the two homonymous properties.
 *
 *
 *
 * @version $Id: OperationProcessorSelector.java,v 1.3 2006/08/07 21:09:24 nichele Exp $
 */
public class OperationProcessorSelector
implements ProcessorSelector, LazyInitBean, ConfigurationConstants {

    // ------------------------------------------------------------ Private data

    private transient static final Logger log = Logger.getLogger(com.funambol.server.engine.dm.OperationProcessorSelector.class.getName());

    // -------------------------------------------------------------- Properties

    /**
     * The default processor server bean name
     */
    private String defaultProcessor;

    /**
     * Sets defaultProcessor
     *
     * @param defaultProcessor the new default processor name
     */
    public void setDefaultProcessor(String defaultProcessor) {
        this.defaultProcessor = defaultProcessor;
    }

    /**
     * Returns defaultProcessor
     *
     * @return defaultProcessor property value
     */
    public String getDefaultProcessor() {
        return this.defaultProcessor;
    }

    /**
     * The error processor server bean name
     */
    private String errorProcessor;

    /**
     * Sets errorProcessor
     *
     * @param errorProcessor the new error processor name
     */
    public void setErrorProcessor(String errorProcessor) {
        this.errorProcessor = errorProcessor;
    }

    /**
     * Returns errorProcessor
     *
     * @return errorProcessor property value
     */
    public String getErrorProcessor() {
        return this.errorProcessor;
    }

    /**
     * Processor name prefix (to be prepended to the operation name).
     */
    private String namePrefix;

    /**
     * Sets namePrefix
     *
     * @param namePrefix the new processor name prefix
     */
    public void setNamePrefix(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    /**
     * Returns namePrefix
     *
     * @return namePrefix property value
     */
    public String getNamePrefix() {
        return namePrefix;
    }

    /**
     * Processor name postfix (to be appended to the operation name).
     */
    private String namePostfix;

    /**
     * Sets namePostfix
     *
     * @param namePostfix the new processor name postfix
     */
    public void setNamePostfix(String namePostfix) {
        this.namePostfix = namePostfix;
    }

    /**
     * Returns namePostfix
     *
     * @return namePostfix property value
     */
    public String getNamePostfix() {
        return namePostfix;
    }


    // ------------------------------------------------------------ Constructors

    /**
     * Creates a new instance of OperationProcessorSelector
     */
    public OperationProcessorSelector() {

    }

    /**
     *
     * @see com.funambol.framework.server.dm.ProcessorSelector
     */
    public ManagementProcessor getProcessor(DeviceDMState dms, DevInfo devInfo) {
        String processorName = null;

        if (log.isEnabledFor(Level.TRACE)) {
            log.trace("dms: " + dms);
        }

        if (dms.state == DeviceDMState.STATE_ERROR) {
            processorName = errorProcessor;
        } else {
            if (StringUtils.isEmpty(dms.operation)) {
                processorName = defaultProcessor;
            } else {
                processorName = namePrefix + dms.operation + namePostfix;
            }
        }

        if (log.isEnabledFor(Level.TRACE)) {
            log.trace("Selected processor: " + processorName);
        }

        try {
            Object o = Configuration.getConfiguration().getBeanInstanceByName(processorName);
            return (ManagementProcessor)o
                   ;
        } catch (Exception e){
            if (log.isEnabledFor(Level.FATAL)) {
                log.fatal("Error creating the management processor: " + e.getMessage());
            }
            
            log.debug("getProcessor", e);

        }

        return null;
    }

    /**
     * Checks if the parameters are ok. If not, it throws an exception. <br>
     * If namePrefix and/or namePostfix are null, an empty string is assumed.
     * If defaultProcessor is not given, an exception is thrown.
     *
     * @throws BeanInitializationException in case of initialization errors
     */
    public void init() throws BeanInitializationException {
        if (StringUtils.isEmpty(defaultProcessor)) {
            throw new BeanInitializationException("Missing mandatory parameter defaultProcessor");
        }

        if (StringUtils.isEmpty(namePrefix)) {
            namePrefix = "";
        }

        if (StringUtils.isEmpty(namePostfix)) {
            namePostfix = "";
        }
    }

    // --------------------------------------------------------- Private methods

}
