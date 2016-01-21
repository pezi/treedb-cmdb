/*
 * (C) Copyright 2014-2016 Peter Sauer (http://treedb.at/).
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 */
package at.treedb.db;

/**
 * <p>
 * Container to extend a HQL/JPA query with an additional condition. <br>
 * Parameter tuple: {@code EntityFieldName} {@code codecOperator}
 * {@code ComparisonData}
 * </p>
 * 
 * @author Peter Sauer
 * 
 */
public class SearchCriteria {
    /**
     * HQL/JPA comparison operators
     * 
     */
    public enum ComparisonOperator {
        /** EQUAL **/
        EL {
            public String toString() {
                return "=";
            }
        },
        /** LIKE **/
        LIKE {
            public String toString() {
                return "LIKE";
            }
        },
        /** NOT EQUAL **/
        NE {
            public String toString() {
                return "<>";
            }
        },
        /** GREATER THAN **/
        GT {
            public String toString() {
                return ">";
            }
        },
        /** LESS THAN **/
        LT {
            public String toString() {
                return "<";
            }
        },
        /** GREATER THAN OR EQUAL TO **/
        GE {
            public String toString() {
                return ">=";
            }
        },
        /** LESS THAN OR EQUAL TO **/
        LE {
            public String toString() {
                return "<=";
            }
        },

    };

    private Enum<?> enumValue;
    private ComparisonOperator operator;
    private Object data;

    /**
     * Constructor for the criteria: entity field value is equal data value.
     * 
     * @param enumValue
     *            entity field name
     * @param data
     *            comparison data
     */
    public SearchCriteria(Enum<?> enumValue, Object data) {
        this.enumValue = enumValue;
        this.operator = ComparisonOperator.EL;
        this.data = data;
    }

    /**
     * Constructor
     * 
     * @param enumValue
     *            entity field name
     * @param operator
     *            comparison operator
     * @param data
     *            comparison data
     */
    public SearchCriteria(Enum<?> enumValue, ComparisonOperator operator, Object data) {
        this.enumValue = enumValue;
        this.operator = operator;
        this.data = data;
    }

    /**
     * Returns the entity field name.
     * 
     * @return entity field name
     */
    public Enum<?> getEnumValue() {
        return enumValue;
    }

    /**
     * Returns the comparison operator.
     * 
     * @return comparison operator
     */
    public ComparisonOperator getOperator() {
        return operator;
    }

    /**
     * Returns the comparison data.
     * 
     * @return comparison data
     */
    public Object getData() {
        return data;
    }

}
