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

import com.it4logic.mindatory.controllers.common.ApplicationBaseController
import com.it4logic.mindatory.controllers.common.ApplicationControllerEntryPoints
import com.it4logic.mindatory.model.security.SecurityUser
import com.it4logic.mindatory.security.ApplicationSecurityPermissions
import com.it4logic.mindatory.security.ChangePasswordRequest
import com.it4logic.mindatory.services.common.ApplicationBaseService
import com.it4logic.mindatory.services.security.SecurityUserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.rest.core.RepositoryConstraintViolationException
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.Errors
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.validation.Valid


@CrossOrigin
@RestController
@RequestMapping(ApplicationControllerEntryPoints.SecurityUsers)
class SecurityUserController : ApplicationBaseController<SecurityUser>() {

    @Autowired
    lateinit var securityUserService: SecurityUserService

    override fun service(): ApplicationBaseService<SecurityUser> = securityUserService

    @GetMapping
    @ResponseBody
    @PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SecurityUserAdminView}', '${ApplicationSecurityPermissions.SecurityUserAdminCreate}', '${ApplicationSecurityPermissions.SecurityUserAdminUpdate}', '${ApplicationSecurityPermissions.SecurityUserAdminDelete}')")
    override fun doGet(@RequestParam(required = false) filter: String?, pageable: Pageable, request: HttpServletRequest): Any
            = doGetInternal(filter, pageable, request)

    @GetMapping("{id}")
    @PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SecurityUserAdminView}', '${ApplicationSecurityPermissions.SecurityUserAdminCreate}', '${ApplicationSecurityPermissions.SecurityUserAdminUpdate}', '${ApplicationSecurityPermissions.SecurityUserAdminDelete}')")
    override fun doGet(@PathVariable id: Long): ResponseEntity<SecurityUser> = doGetInternal(id)

    @PostMapping
    @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SecurityUserAdminCreate}')")
    override fun doCreate(@Valid @RequestBody target: SecurityUser, errors: Errors, response: HttpServletResponse): ResponseEntity<SecurityUser>
            = doCreateInternal(target, errors, response)

    @PutMapping
    @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SecurityUserAdminUpdate}')")
    override fun doUpdate(@Valid @RequestBody target: SecurityUser, errors: Errors, request: HttpServletRequest): ResponseEntity<SecurityUser>
            = doUpdateInternal(target, errors, request)

    @DeleteMapping("{id}")
    @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SecurityUserAdminDelete}')")
    override fun doDelete(@PathVariable id: Long): ResponseEntity<Any> = doDeleteInternal(id)
    
    @PostMapping("{id}/change-password")
    @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SecurityUserAdminUpdate}')")
    fun doChangeUserPassword(@PathVariable id: Long, @Valid @RequestBody changePasswordRequest: ChangePasswordRequest, errors: Errors) {
        if (errors.hasErrors())
            throw RepositoryConstraintViolationException(errors)
        securityUserService.changeUserPassword(id, changePasswordRequest)
    }
}