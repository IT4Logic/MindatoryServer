/*
    Copyright (c) 2019, IT4Logic.

    This file is part of Mindatory project by IT4Logic.

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
package com.it4logic.mindatory.config

import com.it4logic.mindatory.graphql.error.ApiDataFetcherExceptionHandler
import org.springframework.context.annotation.Configuration
import graphql.execution.AsyncSerialExecutionStrategy
import graphql.execution.AsyncExecutionStrategy
import graphql.GraphQL
import graphql.schema.GraphQLSchema
import org.springframework.context.annotation.Bean

/**
 * Utility class to configure GraphQL
 */
@Configuration
class GraphQLConfig {

	/**
	 * GraphQL producer that will be used in GraphQL initialization
	 */
	@Bean
	fun graphQL(schema: GraphQLSchema): GraphQL {
		return GraphQL.newGraphQL(schema)
			.queryExecutionStrategy(AsyncExecutionStrategy(ApiDataFetcherExceptionHandler()))
			.mutationExecutionStrategy(AsyncSerialExecutionStrategy(ApiDataFetcherExceptionHandler()))
			.build()
	}
}