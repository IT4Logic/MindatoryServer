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

package com.it4logic.mindatory.exceptions

import com.it4logic.mindatory.model.common.ApplicationConstraintCodes
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.util.*
import javax.validation.ConstraintViolationException


/**
 * Class responsible for handling specific exception raised within controllers
 */
@ControllerAdvice
class ApplicationResponseEntityExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(ApplicationObjectNotFoundException::class)
    fun handleObjectNotFoundException(exception: ApplicationObjectNotFoundException, request: WebRequest): ResponseEntity<Any> {
        return ResponseEntity (
                ApiError(HttpStatus.NOT_FOUND, exception.errorCode, exception.id.toString()),
                HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(exception: ConstraintViolationException, request: WebRequest): ResponseEntity<Any> {
        val error = ApiError(HttpStatus.NOT_ACCEPTABLE, ApplicationErrorCodes.ValidationError, "")

        exception.constraintViolations?.forEach {
            error.subErrors.add(ApiValidationError(it.rootBeanClass.simpleName, it.propertyPath.toString(), it.invalidValue, it.message))
        }

        return ResponseEntity(error, HttpStatus.NOT_ACCEPTABLE)
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolationException(exception: DataIntegrityViolationException, request: WebRequest): ResponseEntity<Any> {
        val rootMsg = ExceptionHelper.getRootCause(exception)?.message?.toLowerCase()
        if (rootMsg != null) {
            val entry : Optional<MutableMap.MutableEntry<String, String>>? = ApplicationConstraintCodes.map.entries.stream()
                    .filter{ rootMsg.contains(it.key) }.findAny()

            if (entry?.isPresent!!) {
                val error = ApiError(HttpStatus.NOT_ACCEPTABLE, ApplicationErrorCodes.DataIntegrityError, entry.get().value)
                return ResponseEntity(error, HttpStatus.NOT_ACCEPTABLE)
            }
        }

        val error = ApiError(HttpStatus.NOT_ACCEPTABLE, ApplicationErrorCodes.DataIntegrityError, exception.message!!)
        return ResponseEntity(error, HttpStatus.NOT_ACCEPTABLE)
    }

    @ExceptionHandler(ApplicationAuthenticationException::class)
    fun handleAuthenticationException(exception: ApplicationAuthenticationException, request: WebRequest): ResponseEntity<Any> {
        val message = if(exception.cause == null) "" else ExceptionHelper.getRootCause(exception.cause!!)?.message
        val error = ApiError(HttpStatus.UNAUTHORIZED, exception.errorCode, message?:"")
        return ResponseEntity(error, HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(ApplicationAuthorizationException::class)
    fun handleAuthorizationException(exception: ApplicationAuthorizationException, request: WebRequest): ResponseEntity<Any> {
        val message = if(exception.cause == null) "" else ExceptionHelper.getRootCause(exception.cause!!)?.message
        val error = ApiError(HttpStatus.FORBIDDEN, exception.errorCode, message?:"")
        return ResponseEntity(error, HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(ApplicationValidationException::class)
    fun handleValidationException(exception: ApplicationValidationException, request: WebRequest): ResponseEntity<Any> {
        val message = if(exception.cause == null) "" else ExceptionHelper.getRootCause(exception.cause!!)?.message
        val error = ApiError(HttpStatus.NOT_ACCEPTABLE,  exception.errorCode, message?:"")
        return ResponseEntity(error, HttpStatus.NOT_ACCEPTABLE)
    }

    @ExceptionHandler(ApplicationDataIntegrityViolationException::class)
    fun handleApplicationDataIntegrityViolationException(exception: ApplicationDataIntegrityViolationException, request: WebRequest): ResponseEntity<Any> {
        val error = ApiError(HttpStatus.NOT_ACCEPTABLE, ApplicationErrorCodes.DataIntegrityError, exception.errorCode)
        return ResponseEntity(error, HttpStatus.NOT_ACCEPTABLE)
    }
}