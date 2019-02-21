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

import cz.jirutka.rsql.parser.ast.ComparisonNode
import cz.jirutka.rsql.parser.ast.OrNode
import cz.jirutka.rsql.parser.ast.AndNode
import cz.jirutka.rsql.parser.ast.RSQLVisitor
import org.springframework.data.jpa.domain.Specification


/**
 * Implementation of [RSQLVisitor] visitor interface to generate JPA Specification from RSQL Node
 */
class JpaRSQLVisitor<T> : RSQLVisitor<Specification<T>, Any> {

    private val builder: JpaRSQLSpecBuilder<T> = JpaRSQLSpecBuilder()

    override fun visit(node: AndNode, param: Any?): Specification<T>? {
        return builder.createSpecification(node, param)
    }

    override fun visit(node: OrNode, param: Any?): Specification<T>? {
        return builder.createSpecification(node, param)
    }

    override fun visit(node: ComparisonNode, param: Any?): Specification<T>? {
        return builder.createSpecification(node, param)
    }
}
