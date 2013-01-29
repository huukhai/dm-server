/**
 * Copyright (C) 2003-2006 Funambol
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the Honest Public License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Honest Public License for more details.
 *
 * You should have received a copy of the Honest Public License
 * along with this program; if not, write to Funambol,
 * 643 Bair Island Road, Suite 305 - Redwood City, CA 94063, USA
 */


package com.funambol.framework.server.session;

/**
 *
 *
 */
public class SessionExpiredException extends java.lang.Exception {

    /**
     * Creates a new instance of <code>SessionExpiredException</code> without detail message.
     */
    public SessionExpiredException() {
        super();
    }


    /**
     * Constructs an instance of <code>SessionExpiredException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public SessionExpiredException(String msg) {
        super(msg);
    }
}
