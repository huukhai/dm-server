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

package com.funambol.framework.tools;


/**
 * This class implements an ID generator, that increments the counter each time
 * the next method is called. When the maximum 16 bit integer si reached, it
 * starts again from 1. The first retuned number is 1.
 * <p>
 * SimpleIdGenerator is thread-safe
 *
 * @version $Id: Int16IdGenerator.java,v 1.2 2006/08/07 21:09:23 nichele Exp $
 */
public final class Int16IdGenerator implements IdGenerator, java.io.Serializable {

    // -------------------------------------------------------------- Properties

    /**
     * The counter
     */
    private int counter;

    public int getCounter() {
        return counter;
    }

    // ------------------------------------------------------------ Constructors

    /**
     * Creates a new instance of SimpleIdGenerator
     *
     * @param counter the starting value
     * @param increment the increment
     */
    public Int16IdGenerator() {
        counter = 0;
    }

    /**
     * Reset the generator to 0.
     */
    public void reset() {
        counter = 0;
    }

    /**
     * Returns the next value of the counter (incrementing the counter by the
     * increment)
     *
     * @return the next generated value
     */
    public synchronized String next() {
        if (counter == 0x0000ffff) {
            reset();
        }

        return String.valueOf(++counter);
    }

    /**
     * Returns the last generated id (which is the current id).
     *
     * @return the last generated id
     */
    public synchronized String current() {
        return String.valueOf(counter);
    }
}
