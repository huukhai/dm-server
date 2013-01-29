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

package com.funambol.framework.core;


/**
 * This class represents the &lt;SyncML&gt; tag as defined by the SyncML r
 * epresentation specifications.
 *
 *
 *
 * @version $Id: SyncML.java,v 1.3 2006/11/15 14:27:16 nichele Exp $
 */
public final class SyncML
implements java.io.Serializable {

    // ------------------------------------------------------------ Private data

    private SyncHdr  header;
    private SyncBody body;
    private boolean  lastMessage;

    // ------------------------------------------------------------ Constructors

    /**
     * For serialization purposes
     */
    protected SyncML(){}

    /**
     * Creates a new SyncML object from header and body.
     *
     * @param header the SyncML header - NOT NULL
     * @param body the SyncML body - NOT NULL
     * @param last is the last SyncML of a session ?
     *
     */
    public SyncML(final SyncHdr  header,
                  final SyncBody body,
                  final boolean  last)
    throws RepresentationException {

        setSyncHdr(header);
        setSyncBody(body);

        this.lastMessage = last;
    }

    /**
     * The same as SyncML(header, body, false).
     *
     * @param header the SyncML header - NOT NULL
     * @param body the SyncML body - NOT NULL
     *
     */
    public SyncML(final SyncHdr header, final SyncBody body)
    throws RepresentationException {
        this(header, body, false);
    }

    // ---------------------------------------------------------- Public methods

    /**
     * Returns the SyncML header
     *
     * @return the SyncML header
     *
     */
    public SyncHdr getSyncHdr() {
        return header;
    }

    /**
     * Sets the SyncML header
     *
     * @param header the SyncML header - NOT NULL
     *
     * @throws IllegalArgumentException if header is null
     */
    public void setSyncHdr(SyncHdr header) {
        if (header == null) {
            throw new IllegalArgumentException("header cannot be null");
        }
        this.header = header;
    }

    /**
     * Returns the SyncML body
     *
     * @return the SyncML body
     *
     */
    public SyncBody getSyncBody() {
        return body;
    }

    /**
     * Sets the SyncML body
     *
     * @param body the SyncML body - NOT NULL
     *
     * @throws IllegalArgumentException if body is null
     */
    public void setSyncBody(SyncBody body) {
        if (body == null) {
            throw new IllegalArgumentException("body cannot be null");
        }
        this.body = body;
    }

    /**
     * Returns lastMessage
     *
     * @return lastMessage
     */
    public boolean isLastMessage() {
        return lastMessage;
    }

    /**
     * Sets lastMessage
     *
     * @param lastMessage the new lastMessage value
     *
     */
    public void setLastMessage(boolean lastMessage) {
        this.lastMessage = lastMessage;
    }
}
