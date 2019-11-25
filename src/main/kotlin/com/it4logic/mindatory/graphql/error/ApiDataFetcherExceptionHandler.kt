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

import com.it4logic.mindatory.exceptions.ApplicationErrorCodes
import com.it4logic.mindatory.exceptions.ApplicationObjectNotFoundException
import com.it4logic.mindatory.exceptions.ApplicationValidationException
import com.it4logic.mindatory.exceptions.ExceptionHelper
import com.it4logic.mindatory.model.common.ApplicationConstraintCodes
import graphql.ErrorType
import graphql.execution.DataFetcherExceptionHandlerParameters
import graphql.execution.SimpleDataFetcherExceptionHandler
import org.springframework.dao.DataIntegrityViolationException
import java.util.*

/**
 * Implantation of [DataFetcherExceptionHandler] interface through extending [SimpleDataFetcherExceptionHandler] class,
 * to handle errors during execution of GraphQL APIs
 */
class ApiDataFetcherExceptionHandler : SimpleDataFetcherExceptionHandler() {

	override fun accept(handlerParameters: DataFetcherExceptionHandlerParameters?) {
		val exception = handlerParameters?.exception ?: return super.accept(handlerParameters)

		val error = when (exception::class.java) {
			DataIntegrityViolationException::class.java -> {
				handleDataIntegrityViolationException(exception as DataIntegrityViolationException)
			}
			ApplicationObjectNotFoundException::class.java -> {
				handleObjectNotFoundException(exception as ApplicationObjectNotFoundException)
			}
			org.springframework.security.access.AccessDeniedException::class.java -> {
				handleAccessDeniedException(exception as org.springframework.security.access.AccessDeniedException)
			}
			ApplicationValidationException::class.java -> {
				handleApplicationValidationException(exception as ApplicationValidationException)
			}
			else -> return super.accept(handlerParameters)
		}

		handlerParameters.executionContext.addError(error)
	}

	/**
	 * Handle exceptions of type [DataIntegrityViolationException]
	 * @param exception Raised exception
	 * @return [ApiGraphQLError] instance for the input exception
	 */
	private fun handleDataIntegrityViolationException(
		exception: DataIntegrityViolationException
	): ApiGraphQLError {
		val rootMsg = ExceptionHelper.getRootCause(exception)?.message?.toLowerCase()
		if (rootMsg != null) {
			val entry: Optional<MutableMap.MutableEntry<String, String>>? =
				ApplicationConstraintCodes.map.entries.stream()
					.filter { rootMsg.contains(it.key) }.findAny()

			if (entry?.isPresent!!) {
				return ApiGraphQLError(
					listOf(ApplicationErrorCodes.DataIntegrityError, entry.get().value),
					ErrorType.ValidationError
				)
			}
		}

		return ApiGraphQLError(
			listOf(ApplicationErrorCodes.DataIntegrityError, exception.message!!),
			ErrorType.ValidationError
		)
	}

	/**
	 * Handle exceptions of type [ObjectNotFoundException]
	 * @param exception Raised exception
	 * @return [ApiGraphQLError] instance for the input exception
	 */
	private fun handleObjectNotFoundException(
		exception: ApplicationObjectNotFoundException
	): ApiGraphQLError {
		return ApiGraphQLError(
			listOf(ApplicationErrorCodes.DataNotFoundError, exception.errorCode, exception.id.toString()),
			ErrorType.DataFetchingException
		)
	}

	/**
	 * Handle exceptions of type [AccessDeniedException]
	 * @param exception Raised exception
	 * @return [ApiGraphQLError] instance for the input exception
	 */
	private fun handleAccessDeniedException(
		exception: org.springframework.security.access.AccessDeniedException
	): ApiGraphQLError {
		return ApiGraphQLError(
			listOf(ApplicationErrorCodes.AccessDeniedError),
			ErrorType.DataFetchingException
		)
	}

	/**
	 * Handle exceptions of type [ApplicationValidationException]
	 * @param exception Raised exception
	 * @return [ApiGraphQLError] instance for the input exception
	 */
	private fun handleApplicationValidationException(exception: ApplicationValidationException): ApiGraphQLError {
		return ApiGraphQLError(
			listOf(ApplicationErrorCodes.ValidationError, exception.errorCode),
			ErrorType.ValidationError
		)
	}
}