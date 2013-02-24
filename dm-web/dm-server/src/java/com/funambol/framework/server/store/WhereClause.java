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

package com.funambol.framework.server.store;

import org.apache.commons.lang.builder.ToStringBuilder;
import java.io.Serializable;

/**
 * This class represents selection clause of a query.
 *
 *
 * @version $Id: WhereClause.java,v 1.4 2007-06-19 08:16:16 luigiafassina Exp $
 *
 */
public class WhereClause
extends Clause
implements Serializable {

    // -------------------------------------------------------- Public constants
	public static final String OPT_START_WITH = "START_WITH";
	public static final String OPT_END_WITH   = "END_WITH"  ;
	public static final String OPT_CONTAINS   = "CONTAINS"  ;
	public static final String OPT_EQ         = "EQ"        ;
	public static final String OPT_GT         = "GT"        ;
	public static final String OPT_LT 		  = "LT"        ;
	public static final String OPT_BETWEEN    = "BETWEEN"   ;
	public static final String OPT_GE         = "GE"        ;
	public static final String OPT_LE         = "LE"        ;

    // ------------------------------------------------------------ Private data
    private static final String OPT_UPPER      = "upper("    ;

    private String property;
    private String[] value;
    private String operator;
    private boolean caseSensitive;

    /**
     * The default constructor is not intended to be used.
     */
    protected WhereClause() {
        this(null, null, null, false);
    }

    public WhereClause(String property, String[] value, String operator, boolean caseSensitive) {
        this.property      = property;
        this.value         = value;
        this.operator      = operator;
        this.caseSensitive = caseSensitive;
    }

    /** Getter for property property.
     * @return Value of property property.
     *
     */
    public String getProperty() {
        return property;
    }

    /** Setter for property property.
     * @param name New value of property property.
     *
     */
    public void setProperty(String property) {
        this.property = property;
    }

    /** Getter for property value.
     * @return Value of property value.
     *
     */
    public String[] getvalue() {
        return value;
    }

    /** Setter for property value.
     * @param name New value of property value.
     *
     */
    public void setValue(String[] value) {
        this.value = value;
    }

    /** Setter for property parameter.
     * @param name New value of property value.
     *
     */
    public void setParameter(String[] value) {
        this.value = value;
    }

    /** Getter for property operator.
     * @return Value of property operator.
     *
     */
    public String getOperator() {
        return operator;
    }

    /** Setter for property operator.
     * @param name New value of property operator.
     *
     */
    public void setOperator(String operator) {
        this.operator = operator;
    }

    /** Getter for property caseSensitive.
     * @return Value of property caseSensitive.
     *
     */
    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    /** Setter for property caseSensitive.
     * @param name New value of property caseSensitive.
     *
     */
    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    // ------------------------------------------------ Implementation of Clause

    public PreparedWhere getPreparedWhere() {
        String property = getProperty();
        String operator = getOperator();
        String[] values = getvalue();
        boolean caseSensitive = isCaseSensitive();

        StringBuffer query = new StringBuffer();

        assert (values != null);

        PreparedWhere where = new PreparedWhere();
        where.parameters = new Object[values.length];

        String uprOpen     = ""      ;
        String uprClose    = ""      ;
        String uprProperty = property;

        if (!caseSensitive) {
            uprOpen  = OPT_UPPER;
            uprClose = ")";
            uprProperty = " UPPER("+property+")";
        }

        if (OPT_START_WITH.equalsIgnoreCase(operator)) {
            query.append(uprProperty).append(" LIKE ").append(uprOpen).append('?').append(uprClose);
            where.sql = query.toString();
            where.parameters[0] = values[0] + '%';
        } else if (OPT_END_WITH.equalsIgnoreCase(operator)) {
            query.append(uprProperty).append(" LIKE ").append(uprOpen).append('?').append(uprClose);
            where.sql = query.toString();
            where.parameters[0] = '%' + values[0];
        } else if (OPT_CONTAINS.equalsIgnoreCase(operator)) {
            query.append(uprProperty).append(" LIKE ").append(uprOpen).append('?').append(uprClose);
            where.sql = query.toString();
            where.parameters[0] = '%' + values[0] + '%';
        } else if (OPT_EQ.equalsIgnoreCase(operator)) {
            query.append(uprProperty).append(" = ").append(uprOpen).append('?').append(uprClose);
            where.sql = query.toString();
            where.parameters[0] = values[0];
        } else if (OPT_GT.equalsIgnoreCase(operator)) {
            query.append(uprProperty).append(" > ").append(uprOpen).append('?').append(uprClose);
            where.sql = query.toString();
            where.parameters[0] = values[0];
        } else if (OPT_LT.equalsIgnoreCase(operator)) {
            query.append(uprProperty).append(" < ").append(uprOpen).append('?').append(uprClose);
            where.sql = query.toString();
            where.parameters[0] = values[0];
        } else if (OPT_BETWEEN.equalsIgnoreCase(operator)) {
            query.append(uprProperty).append(' ').append(OPT_BETWEEN).append(' ').append(uprOpen).append('?').append(uprClose)
                .append(" AND ").append(uprOpen).append('?').append(uprClose);
            where.sql = query.toString();
            where.parameters[0] = values[0];
            where.parameters[1] = values[1];
        } else if (OPT_GE.equalsIgnoreCase(operator)) {
            query.append(uprProperty).append(" >= ").append(uprOpen).append('?').append(uprClose);
            where.sql = query.toString();
            where.parameters[0] = values[0];
        } else if (OPT_LE.equalsIgnoreCase(operator)) {
            query.append(uprProperty).append(" <= ").append(uprOpen).append('?').append(uprClose);
            where.sql = query.toString();
            where.parameters[0] = values[0];
        }

        where.sql = '(' + where.sql + ')';
        return where;
    }

    // -------------------------------------------------------------------------

    public String toString() {
        ToStringBuilder sb = new ToStringBuilder(this);
        sb.append("operator:",  operator);
        sb.append("property:",  property);
        for (int i=0; i<value.length; i++) {
            sb.append("value:",  value[i]);
        }
        sb.append("caseSensitive:",  caseSensitive);

        return sb.toString();
    }
}
