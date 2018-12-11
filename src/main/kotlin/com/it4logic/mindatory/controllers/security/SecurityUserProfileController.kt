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

package com.it4logic.mindatory.controllers.security

import com.it4logic.mindatory.controllers.common.ApplicationControllerEntryPoints
import com.it4logic.mindatory.model.security.SecurityUser
import com.it4logic.mindatory.security.ChangePasswordRequest
import com.it4logic.mindatory.services.security.SecurityUserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.rest.core.RepositoryConstraintViolationException
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.Errors
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid


@CrossOrigin
@RestController
@RequestMapping(ApplicationControllerEntryPoints.SecurityUserProfile)
class SecurityUserProfileController {

    @Autowired
    lateinit var securityUserService: SecurityUserService

    @PreAuthorize("isFullyAuthenticated()")
    @GetMapping
    fun doGetCurrentUserProfile() : ResponseEntity<SecurityUser> = ResponseEntity.ok(securityUserService.getCurrentSecurityUser())

    @PutMapping
    @PreAuthorize("isFullyAuthenticated()")
    fun doUpdateUserProfile(@Valid @RequestBody user: SecurityUser, errors: Errors, request: HttpServletRequest): ResponseEntity<SecurityUser> {
        if (errors.hasErrors())
            throw RepositoryConstraintViolationException(errors)
        return ResponseEntity.ok().body(securityUserService.updateCurrentSecurityUser(user))
    }

    @PostMapping("change-password")
    @PreAuthorize("isFullyAuthenticated()")
    fun doChangeCurrentUserPassword(@Valid @RequestBody changePasswordRequest: ChangePasswordRequest, errors: Errors, request: HttpServletRequest) {
        if (errors.hasErrors())
            throw RepositoryConstraintViolationException(errors)
        securityUserService.changeCurrentUserPassword(changePasswordRequest)
    }
}