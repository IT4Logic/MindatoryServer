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

	private fun handleObjectNotFoundException(
		exception: ApplicationObjectNotFoundException
	): ApiGraphQLError {
		return ApiGraphQLError(
			listOf(ApplicationErrorCodes.DataNotFoundError, exception.errorCode, exception.id.toString()),
			ErrorType.DataFetchingException
		)
	}

	private fun handleAccessDeniedException(
		exception: org.springframework.security.access.AccessDeniedException
	): ApiGraphQLError {
		return ApiGraphQLError(
			listOf(ApplicationErrorCodes.AccessDeniedError),
			ErrorType.DataFetchingException
		)
	}

	private fun handleApplicationValidationException(exception: ApplicationValidationException): ApiGraphQLError {
		return ApiGraphQLError(
			listOf(ApplicationErrorCodes.ValidationError, exception.errorCode),
			ErrorType.ValidationError
		)
	}
}