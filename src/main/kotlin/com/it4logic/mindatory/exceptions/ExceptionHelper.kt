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

package com.it4logic.mindatory.exceptions

/**
 * Utility class that contains a helper methods for exception management
 */
abstract class ExceptionHelper {
    companion object {
        /**
         * Gets the root cause exception
         *
         * @param throwable Start exception
         * @return Root cause exception
         */
        fun getRootCause(throwable: Throwable): Throwable? {
            var cause: Throwable? = throwable

            while (cause?.cause != null && cause != cause.cause) {
                cause = cause.cause
            }

            return cause
        }
    }
}