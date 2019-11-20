package com.it4logic.mindatory.graphql.error

import graphql.ErrorType
import graphql.GraphQLError
import graphql.language.SourceLocation

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