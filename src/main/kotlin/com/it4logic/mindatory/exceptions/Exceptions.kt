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

package com.it4logic.mindatory.exceptions

import org.springframework.validation.Errors


class ApplicationAuthenticationException (var errorCode: String, override var cause: Throwable? = null) : RuntimeException()




class ApplicationGeneralException (val error: ApiError, override var cause: Throwable? = null) : RuntimeException()

class ApplicationObjectNotFoundException (var id: Any, var errorCode: String) : RuntimeException()

class ApplicationAuthorizationException (var errorCode: String, override var cause: Throwable? = null) : RuntimeException()

class ApplicationValidationException (var errorCode: String, var errors: Errors? = null, override var cause: Throwable? = null) : RuntimeException()

class ApplicationDataIntegrityViolationException (var errorCode: String, override var cause: Throwable? = null) : RuntimeException()