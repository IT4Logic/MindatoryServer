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

package com.it4logic.mindatory.security

import javax.validation.constraints.*

data class LoginRequest (
        @get: NotBlank
        @get: Size(min = 4, max = 50)
        var username: String,

        @get: NotBlank
        @get: Size(min = 6)
        var password: String
)

data class JwtAuthenticationResponse (
        var accessToken: String = "",
        var tokenType: String = "Bearer"
)

data class ChangePasswordRequest (
        @get: NotBlank
        @get: Size(min = 6)
        var currentPassword: String,

        @get: NotBlank
        @get: Size(min = 6)
        var newPassword: String,

        @get: NotBlank
        @get: Size(min = 6)
        var confirmPassword: String
)

enum class ApplicationPermission(private val permission: Int) {
        Read(1),
        Write(2),
        Create(3),
        Delete(4),
        Administration(5)
}

data class ApplicationAclRequest (
        @get: NotBlank
        var domainClass: String,

        @get: NotNull
        var id: Long
)

data class ApplicationAclOwnerRequest (
        @get: NotBlank
        var domainClass: String,

        @get: NotNull
        var id: Long,

        @get: NotBlank
        var owner: String
)

data class ApplicationAclPermissionRequest (
        @get: NotBlank
        var domainClass: String,

        @get: NotNull
        var id: Long,

        @get: NotBlank
        var recipient: String,

        @get: NotNull
        var permission: ApplicationPermission
)