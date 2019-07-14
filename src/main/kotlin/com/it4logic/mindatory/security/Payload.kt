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

package com.it4logic.mindatory.security

import com.it4logic.mindatory.exceptions.ApplicationObjectNotFoundException
import org.springframework.security.acls.domain.BasePermission
import org.springframework.security.acls.model.Permission
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
        var currentPassword: String,

        @get: NotBlank
        @get: Size(min = 6)
        var newPassword: String,

        @get: NotBlank
        @get: Size(min = 6)
        var confirmPassword: String
)

data class ResetPasswordRequest  (
        @get: NotBlank
        var username: String,

        @get: NotBlank
        var requesterRestPasswordUrl: String
)

data class ProcessResetPasswordRequest  (
        @get: NotBlank
        var token: String,

        @get: NotBlank
        @get: Size(min = 6)
        var password: String,

        @get: NotBlank
        @get: Size(min = 6)
        var passwordConfirm: String
)

enum class ApplicationPermission(private val permission: Permission) {
        View(BasePermission.READ),
        Create(BasePermission.CREATE),
        Modify(BasePermission.WRITE),
        Delete(BasePermission.DELETE),
        Administration(BasePermission.ADMINISTRATION);

        companion object {
            fun fromPermission(permission: Permission): ApplicationPermission {
                    return when(permission) {
                            BasePermission.READ -> View
                            BasePermission.CREATE -> Create
                            BasePermission.WRITE -> Modify
                            BasePermission.DELETE -> Delete
                            BasePermission.ADMINISTRATION -> Administration
                            else -> throw ApplicationObjectNotFoundException(permission.mask, "ApplicationPermission")
                    }
            }
        }
        fun toPermission(): Permission = permission
}

data class ApplicationAclPermissionRequest (
        @get: NotBlank
        var recipient: String,

        @get: NotNull
        var permissions: List<ApplicationPermission>
)
