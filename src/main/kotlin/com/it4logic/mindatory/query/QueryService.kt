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

import com.it4logic.mindatory.mlc.MultipleLanguageContent
import com.it4logic.mindatory.query.RSQLSearchOperator
import cz.jirutka.rsql.parser.RSQLParser
import org.springframework.data.jpa.domain.Specification
import cz.jirutka.rsql.parser.ast.RSQLOperators
import org.springframework.data.domain.Sort
import javax.persistence.criteria.JoinType
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties


/**
 * Utility class to hide the internal implementation of RSQL and contains the required configuration
 */
abstract class QueryService {
    companion object {
        /**
         * Parse the input query string to JPA Specification(s)
         *
         * @param query Query string
         * @return JPA Specification list
         */
        fun <T>parseFilter(query: String?, param: Any?): Specification<T>? {
            if(query == null || query.isBlank())
                return null
            val operators = RSQLOperators.defaultOperators()
            operators.addAll(RSQLSearchOperator.extendedOperators())
            val rootNode = RSQLParser(operators).parse(query)
            return rootNode.accept(JpaRSQLVisitor(), param)
        }

        fun parseSort(klass: Class<*>, sort: Sort?): Sort {
            val orders = mutableListOf<Sort.Order>()
            val iterator = sort?.iterator()
            if (iterator != null) {
                for(e in iterator)  {
                    val memberProperty = klass.kotlin.memberProperties.filter { it.name ==  e.property}
                    if(!memberProperty.isEmpty()) {
                        val result = memberProperty[0].getter.findAnnotation<MultipleLanguageContent>()
                        if(result != null) {
//                            val pAttr = managedType.getDeclaredList("mlcs")
//                            val join = startRoot.join(pAttr, JoinType.LEFT)
//                            return createPredicate(join, builder, "contents")
                        }
                    } else
                        orders.add(orders.size, e)
                }
            }
            return Sort.by(orders)
        }
    }
}