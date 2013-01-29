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

package com.funambol.framework.engine.pipeline;

import java.io.Serializable;

import com.funambol.framework.core.SyncML;
import com.funambol.framework.core.Sync4jException;
import com.funambol.framework.core.Util;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;


/**
 * This class represents the manager of the pipeline. It supplies a hook for adding
 * additional processing and manipulation of the message between the server and the
 * client.
 */
public class PipelineManager implements InputMessageProcessor, OutputMessageProcessor, Serializable {

    // --------------------------------------------------------------- Constants

    // ------------------------------------------------------------ Private data

    private static final Logger log = Logger.getLogger(com.funambol.framework.engine.pipeline.PipelineManager.class.getName());

    // ------------------------------------------------------------ Constructors

    // -------------------------------------------------------------- Properties

    private InputMessageProcessor  inputProcessors[]  = null;
    private OutputMessageProcessor outputProcessors[] = null;

    /** Getter for property inputProcessors.
     * @return Value of property inputProcessors.
     */
    public InputMessageProcessor[] getInputProcessors() {
        return this.inputProcessors;
    }

    /** Setter for property inputProcessors.
     * @param inputProcessors New value of property inputProcessors.
     */
    public void setInputProcessors(InputMessageProcessor[] inputProcessors) {
        this.inputProcessors = inputProcessors;
    }

    /** Getter for property outputProcessors.
     * @return Value of property outputProcessors.
     */
    public OutputMessageProcessor[] getOutputProcessors() {
        return this.outputProcessors;
    }

    /** Setter for property outputProcessors
     * @param outputProcessors New value of property outputProcessors.
     */
    public void setOutputProcessors(OutputMessageProcessor[] outputProcessors) {
        this.outputProcessors = outputProcessors;
    }

    // ---------------------------------------------------------- Public methods

	public PipelineManager() {
    }

    /** Process the message with the input processors.
     * @param processingContext message processing context
     * @param message the message to be processed
     */
    public void preProcessMessage(MessageProcessingContext processingContext, SyncML message) {

        if(log.isEnabled(Level.TRACE)){
            log.trace("Starting preprocessing");
        }

        //cycle on inputMessageProcessor[] elements
        int size = inputProcessors.length;
        for (int i=0; i<size; i++) {
            try {
                inputProcessors[i].preProcessMessage(processingContext, message);
            } catch(Sync4jException e) {
                log.info("preProcessMessage error: " + e);
            }
        }

        if(log.isEnabled(Level.TRACE)){
            log.trace("Preprocessed message: " + Util.toXML(message));
        }
    }

    /** Process the message with the output processors.
     * @param processingContext message processing context
     * @param message the message to be processed
     */
    public void postProcessMessage(MessageProcessingContext processingContext, SyncML message) {

        if(log.isEnabled(Level.TRACE)){
            log.trace("Starting postprocessing");
            log.trace("Returning message to process: " + Util.toXML(message));
        }

        //cycle on outputMessageProcessor[] elements
        int size = outputProcessors.length;
        for (int i = 0; i < size; i++) {
            try {
                outputProcessors[i].postProcessMessage(processingContext, message);
            } catch (Sync4jException e) {
                log.info("postProcessMessage error: " + e.getMessage(), e);
            }
        }
    }
}
