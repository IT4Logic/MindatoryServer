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

import com.it4logic.mindatory.mlc.MultipleLanguageContent
import org.springframework.data.jpa.domain.Specification
import cz.jirutka.rsql.parser.ast.ComparisonOperator
import java.text.SimpleDateFormat
import java.util.*
import javax.persistence.criteria.*
import javax.persistence.metamodel.ManagedType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties


/**
 * Implementation of JPA Specification interface to build Criteria Predicate
 */
class JpaRSQLSpecification<T> (
        private val qProperty: String,
        private val operator: ComparisonOperator,
        private val arguments: List<String>
) : Specification<T> {


    override fun toPredicate(root: Root<T>, query: CriteriaQuery<*>, builder: CriteriaBuilder): Predicate? {
        return createPredicate(root, root as Path<Any>, root.model as ManagedType<Any>, query, builder, qProperty)
    }


    /**
     * Creates a predicate for the input attribute (property), and from the given path and managed type.
     * If the attribute is nested like (group.id), the function will go deep until the last property and build the condition accordingly
     *
     * @param startRoot The root entity that will be used to create the join relation as the join relation will be created from the root entity only and no need to created from the nested entities
     * @param path The attribute path that will be used to fetch the property information
     * @param managedType The attribute entity type
     * @param query [CriteriaQuery] object
     * @param builder [CriteriaBuilder] object
     * @param property Attribute name, for example, could be (group.id) or (group.address.zipCode)
     *
     * @return [Predicate] object equivalent to the input attribute condition
     */
    private fun createPredicate(startRoot: Root<T>, path: Path<Any>, managedType: ManagedType<Any>, query: CriteriaQuery<*>, builder: CriteriaBuilder, property: String): Predicate? {
        val graph = property.split(".")
        if(graph.size == 1) {
            val memberProperty = path.javaType.kotlin.memberProperties.filter { it.name ==  property}
            if(!memberProperty.isEmpty()) {
                val result = memberProperty[0].getter.findAnnotation<MultipleLanguageContent>()
                if(result != null) {
                    val pAttr = managedType.getDeclaredList("mlcs")
                    val join = startRoot.join(pAttr, JoinType.LEFT)
                    query.distinct(true);
                    return createPredicate(join, builder, "contents")
                }
            }
            return createPredicate(path, builder, property)
        }

        val attr = managedType.getAttribute(graph[0])
        when {
            attr.isAssociation -> {
                val sAttr = managedType.getSingularAttribute(graph[0])
                val attrPath = path.get(sAttr)
                startRoot.join(sAttr, JoinType.LEFT)
                val prop = graph.asSequence().drop(1).joinToString(".")
                return createPredicate(startRoot, attrPath as Path<Any>, sAttr.type as ManagedType<Any>, query, builder, prop)
            }
            attr.isCollection -> {
                //TODO to be implemented later on when needed

//                when(pAttr?.collectionType) {
//                    PluralAttribute.CollectionType.COLLECTION -> startRoot.join(managedType.getDeclaredCollection("mlcs"), JoinType.LEFT)
//                    PluralAttribute.CollectionType.LIST -> startRoot.join(managedType.getDeclaredList("mlcs"), JoinType.LEFT)
//                    PluralAttribute.CollectionType.MAP -> startRoot.join(managedType.getDeclaredMap("mlcs"), JoinType.LEFT)
//                    PluralAttribute.CollectionType.SET -> startRoot.join(managedType.getDeclaredSet("mlcs"), JoinType.LEFT)
//                }

            }
        }

        return null
    }

    /**
     * Creates a predicate for the input attribute (property), and from the given path and managed type.
     *
     * @param rootPath The rootPath path that will be used to fetch the property information
     * @param builder [CriteriaBuilder] object
     * @param property Attribute name
     *
     * @return [Predicate] object equivalent to the input attribute condition
     */
    private fun createPredicate(rootPath: Path<*>, builder: CriteriaBuilder, property: String): Predicate? {
        val args = castArguments(rootPath, property)
        val argument = args[0]
        when (RSQLSearchOperator.getSimpleOperator(operator)) {
            RSQLSearchOperator.EQUAL -> {
                return when (argument) {
                    is String -> builder.like(rootPath.get(property), argument.toString().replace('*', '%'))
                    else -> builder.equal(rootPath.get<Any>(property), argument)
                }
            }
            RSQLSearchOperator.NOT_EQUAL -> {
                return when (argument) {
                    is String -> builder.notLike(rootPath.get(property), argument.toString().replace('*', '%'))
                    else -> builder.notEqual(rootPath.get<Any>(property), argument)
                }
            }
            RSQLSearchOperator.GREATER_THAN -> {
                return when (argument) {
                    is Date -> builder.greaterThan(rootPath.get(property), argument)
                    else -> builder.greaterThan(rootPath.get(property), argument.toString())
                }
            }
            RSQLSearchOperator.GREATER_THAN_OR_EQUAL -> {
                return when (argument) {
                    is Date -> builder.greaterThanOrEqualTo(rootPath.get(property), argument)
                    else -> builder.greaterThanOrEqualTo(rootPath.get(property), argument.toString())
                }
            }
            RSQLSearchOperator.LESS_THAN -> {
                return when (argument) {
                    is Date -> builder.lessThan(rootPath.get(property), argument)
                    else -> builder.lessThan(rootPath.get(property), argument.toString())
                }
            }
            RSQLSearchOperator.LESS_THAN_OR_EQUAL -> {
                return when (argument) {
                    is Date -> builder.lessThanOrEqualTo(rootPath.get(property), argument)
                    else -> builder.lessThanOrEqualTo(rootPath.get(property), argument.toString())
                }
            }
            RSQLSearchOperator.IN -> {
                return rootPath.get<Any>(property).`in`(args)
            }
            RSQLSearchOperator.NOT_IN -> {
                return builder.not(rootPath.get<Any>(property).`in`(args))
            }
            RSQLSearchOperator.NULL -> {
                return when (argument.toString().toBoolean()) {
                    true -> builder.isNull(rootPath.get<Any>(property))
                    false -> builder.isNotNull(rootPath.get<Any>(property))
                }
            }
        }

        return null
    }

    /**
     * Casts the arguments in the root object to suitable java types
     *
     * @param root [Root] object that contains the arguments
     * @return A list of suitable java types
     */
    private fun castArguments(root: Path<*>, property: String): List<Any> {
        val args = ArrayList<Any>()
        val type = root.get<Any>(property).javaType

        for (argument in arguments) {
            when (type) {
                Int::class.java -> args.add(Integer.parseInt(argument))
                Long::class.java -> args.add(java.lang.Long.parseLong(argument))
                Boolean::class.java -> args.add(argument.toBoolean())
                Date::class.java -> args.add(SimpleDateFormat("dd-MM-yyyy_HH:mm:ss").parse(argument))
                else -> args.add(argument)
            }
        }

        return args
    }
}