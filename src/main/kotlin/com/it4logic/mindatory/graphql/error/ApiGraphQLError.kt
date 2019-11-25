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
package com.it4logic.mindatory.graphql.error

import graphql.ErrorType
import graphql.GraphQLError
import graphql.language.SourceLocation

/**
 * Class that holds the error that could happen while executing GraphQL APIs
 */
class ApiGraphQLError(private val errors: List<String>, private val errorType: ErrorType) : GraphQLError {

	override fun getMessage(): String {
		return errors.joinToString(";")
	}

	override fun getErrorType(): ErrorType {
		return errorType
	}

	override fun getLocations(): MutableList<SourceLocation> {
		return mutableListOf()
	}
}