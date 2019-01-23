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

    override fun type(): Class<SecurityUser> =  SecurityUser::class.java

    @GetMapping
    @ResponseBody
    @PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SecurityUserAdminView}', '${ApplicationSecurityPermissions.SecurityUserAdminCreate}', '${ApplicationSecurityPermissions.SecurityUserAdminModify}', '${ApplicationSecurityPermissions.SecurityUserAdminDelete}')")
    override fun doGet(@RequestParam(required = false) filter: String?, pageable: Pageable, request: HttpServletRequest, response: HttpServletResponse): Any
            = doGetInternal(filter, pageable, request, response)

    @GetMapping("{id}")
    @PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SecurityUserAdminView}', '${ApplicationSecurityPermissions.SecurityUserAdminCreate}', '${ApplicationSecurityPermissions.SecurityUserAdminModify}', '${ApplicationSecurityPermissions.SecurityUserAdminDelete}')")
    override fun doGet(@PathVariable id: Long, request: HttpServletRequest, response: HttpServletResponse): SecurityUser
            = doGetInternal(id, request, response)

    @PostMapping
    @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SecurityUserAdminCreate}')")
    override fun doCreate(@Valid @RequestBody target: SecurityUser, errors: Errors, request: HttpServletRequest, response: HttpServletResponse): SecurityUser
            = doCreateInternal(target, errors, request, response)

    @PutMapping
    @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SecurityUserAdminModify}')")
    override fun doUpdate(@Valid @RequestBody target: SecurityUser, errors: Errors, request: HttpServletRequest, response: HttpServletResponse): SecurityUser
            = doUpdateInternal(target, errors, request, response)

    @DeleteMapping("{id}")
    @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SecurityUserAdminDelete}')")
    override fun doDelete(@PathVariable id: Long, request: HttpServletRequest, response: HttpServletResponse) = doDeleteInternal(id, request, response)
    
    @PostMapping("{id}/change-password")
    @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SecurityUserAdminModify}')")
    fun doChangeUserPassword(@PathVariable id: Long, @Valid @RequestBody changePasswordRequest: ChangePasswordRequest, errors: Errors) {
        if (errors.hasErrors())
            throw RepositoryConstraintViolationException(errors)
        securityUserService.changeUserPassword(id, changePasswordRequest)
    }
}