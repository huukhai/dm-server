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


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.funambol.framework.config.Configuration;
import com.funambol.framework.tools.PatternPair;
import com.funambol.framework.tools.beans.LazyInitBean;
import com.funambol.framework.tools.beans.BeanInitializationException;
import com.funambol.framework.core.dm.ddf.DevInfo;
import com.funambol.framework.engine.dm.DeviceDMState;
import com.funambol.framework.engine.dm.ManagementProcessor;
import com.funambol.framework.server.dm.ProcessorSelector;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/**
 * This is a simple ProcessorSelector implementation that associates management
 * processors to sets of device identifiers. It is configured with an array of
 * associations {regexp}-{management_processor}, where {regexp} is a regular
 * expression interpreted by the JDK package java.util.regex used to match the
 * device id and {management_processor} is a server side bean configuration
 * path. If no device id matches any of the given regexp, a default processor
 * will be returned; otherwise the first match is returned.
 *
 *
 *
 * @version $Id: DeviceIdProcessorSelector.java,v 1.2 2006/08/07 21:09:24 nichele Exp $
 */
public class DeviceIdProcessorSelector
implements ProcessorSelector, LazyInitBean, java.io.Serializable {
    // ---------------------------------------------------------- Protected data

    /**
     * Logger
     */
    private static final Logger log = Logger.getLogger(com.funambol.server.engine.dm.DeviceIdProcessorSelector.class.getName());

    // ------------------------------------------------------------ Private data

    private Pattern[] regexps;

    // -------------------------------------------------------------- Properties

    /**
     * The pattern-pairs used to metch device ids
     */
    private PatternPair[] patterns;

    /**
     * Sets patterns
     *
     * @param patterns the new patterns
     */
    public void setPatterns(PatternPair[] patterns) {
        this.patterns = patterns;
    }

    /**
     * Gets patterns
     *
     * @return the patterns property
     */
    public PatternPair[] getPatterns() {
        return patterns;
    }

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

    // ------------------------------------------------------------ Constructors

    // ------------------------------------------------------- ProcessorSelector

    /**
     * @param sessionId the management session id: ignored
     * @param devInfo the device info
     *
     * @see ProcessorSelector
     */
    public ManagementProcessor getProcessor(DeviceDMState dms, DevInfo devInfo) {
        String beanName = defaultProcessor;

        String device = devInfo.getDevId();

        Matcher m;
        for (int i=0; i<regexps.length; ++i) {
            m = regexps[i].matcher(device);

            if (m.matches()) {
                beanName = patterns[i].processor;
                break;
            }
        }

        ManagementProcessor processor = null;
        try {
            processor = (ManagementProcessor)
                        Configuration.getConfiguration().getBeanInstanceByName(beanName);
        } catch (Exception e) {
            if (log.isEnabledFor(Level.TRACE)) {
                log.fatal( "Error instantiating the management processor"
                          + beanName
                          + ": "
                          + e.getMessage()
                          );
            }
            log.debug("getProcessor", e);

        }

        return processor;
    }

    // ------------------------------------------------------------ LazyInitBean

    /**
     * During bean initialization all the given regular expressions are compiled.
     * If there are errors, a BeanInitializationException is thrown.
     *
     * @throws BeanInitializationException if one of the patterns cannot be compiled
     */
    public void init() throws BeanInitializationException {
        if ((patterns == null) || (patterns.length == 0)) {
            regexps = new Pattern[0];
            return;
        }

        regexps = new Pattern[patterns.length];
        for (int i=0; i<patterns.length; ++i) {
            try {
                regexps[i] = Pattern.compile(patterns[i].pattern);
            } catch (Exception e) {
                if (log.isEnabledFor(Level.FATAL)) {
                    log.fatal( "Error compiling pattern '"
                              + patterns[i].pattern
                              + "': "
                              + e.getMessage()
                              );
                }
                throw new BeanInitializationException(
                    "Error compiling pattern '" + patterns[i].pattern + "'", e
                );
            }
        }
    }
}
