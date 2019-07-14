/*
    Copyright (c) 2019, IT4Logic.

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

import cz.jirutka.rsql.parser.ast.ComparisonNode
import cz.jirutka.rsql.parser.ast.LogicalNode
import cz.jirutka.rsql.parser.ast.LogicalOperator
import cz.jirutka.rsql.parser.ast.Node
import org.springframework.data.jpa.domain.Specification


/**
 * This class is responsible for building JPA Specification from RSQL Node
 */
class JpaRSQLSpecBuilder<T> {

    /**
     * Creates a JPA Specification from a RSQL Node
     *
     * @param node RSQL Node
     * @return JPA Specification
     */
    fun createSpecification(node: Node, param: Any?): Specification<T>? {
        return when (node) {
            is LogicalNode -> createSpecificationFromLogicalNode(node, param)
            is ComparisonNode -> createSpecificationFromComparisonNode(node)
            else -> null
        }
    }

    /**
     * Creates a JPA Specification from a RSQL [LogicalNode]
     *
     * @param logicalNode RSQL LogicalNode
     * @return JPA Specification
     */
    private fun createSpecificationFromLogicalNode(logicalNode: LogicalNode, param: Any?): Specification<T> {
        val specs = ArrayList<Specification<T>>()
        for (node in logicalNode.children) {
            val temp = createSpecification(node, param)
            if (temp != null)
                specs.add(temp)
        }

        var result = specs[0]
        if (logicalNode.operator == LogicalOperator.AND) {
            for (i in 1 until specs.size) {
                result = Specification.where(result).and(specs[i])
            }
        } else if (logicalNode.operator == LogicalOperator.OR) {
            for (i in 1 until specs.size) {
                result = Specification.where(result).or(specs[i])
            }
        }

        return result
    }

    /**
     * Creates a JPA Specification from a RSQL [ComparisonNode]
     *
     * @param comparisonNode RSQL ComparisonNode
     * @return JPA Specification
     */
    private fun createSpecificationFromComparisonNode(comparisonNode: ComparisonNode): Specification<T> {
        return Specification.where(
                JpaRSQLSpecification<T>(comparisonNode.selector, comparisonNode.operator, comparisonNode.arguments)
        )
    }
}