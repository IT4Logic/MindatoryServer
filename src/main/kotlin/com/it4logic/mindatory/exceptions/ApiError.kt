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

import org.springframework.http.HttpStatus
import java.util.*


/**
 * Class that is used for sending details about errors that occur while executing APIs
 */
data class ApiError (
        /**
         * Http status that results because of the error
         */
        val status: HttpStatus,
        /**
         * Error message
         */
        val errorCode: String,
        /**
         * Error message
         */
        val errorData: String,
        /**
         * Detailed error message if exists
         */
        val debugMessage: String = "",
        /**
         * Error date and time
         */
        val timestamp: Date = Date(),
        /**
         * Any sub errors that related to the main error
         */
        val subErrors: ArrayList<ApiSubError> = ArrayList()
    )

/**
 * Base interface for any API Sub-Error
 */
interface ApiSubError

/**
 * Validation Violation Sub Error
 */
data class ApiValidationError(
        /**
         * Object name that has validation errors
         */
        val objectName: String,
        /**
         * Field Name that has the validation errors
         */
        val fieldId: String,
        /**
         * The value that has been rejected
         */
        val rejectedValue: Any?,
        /**
         * Any related message to the validation
         */
        val message: String?) : ApiSubError