/*
    Copyright (c) 2017, IT4Logic.

    This file is part of Mindatory solution by IT4Logic.

    Mindatory is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Mindatory is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar.  If not, see <https://www.gnu.org/licenses/>.

 */

package com.it4logic.mindatory.query

import cz.jirutka.rsql.parser.ast.RSQLOperators
import cz.jirutka.rsql.parser.ast.ComparisonOperator

/**
 * Enumrator to map the RSQL opetator types to internal implementation types and extended types as well
 */
enum class RSQLSearchOperator (private val operator: ComparisonOperator) {
    EQUAL(RSQLOperators.EQUAL),
    NOT_EQUAL(RSQLOperators.NOT_EQUAL),
    GREATER_THAN(RSQLOperators.GREATER_THAN),
    GREATER_THAN_OR_EQUAL(RSQLOperators.GREATER_THAN_OR_EQUAL),
    LESS_THAN(RSQLOperators.LESS_THAN),
    LESS_THAN_OR_EQUAL(RSQLOperators.LESS_THAN_OR_EQUAL),
    IN(RSQLOperators.IN),
    NOT_IN(RSQLOperators.NOT_IN),
    NULL(ComparisonOperator("=null="));

    companion object {
        /**
         * Returns the extended non RSQL default operators
         * @return Extended operators list
         */
        fun extendedOperators(): List<ComparisonOperator> {
            return listOf(NULL.operator)
        }

        /**
         * Gets internal implemented operator from RSQL operator
         *
         * @param operator RSQL operator
         * @return Internal operation
         */
        fun getSimpleOperator(operator: ComparisonOperator): RSQLSearchOperator? {
            for (operation in values()) {
                if (operation.operator === operator) {
                    return operation
                }
            }
            return null
        }
    }
}