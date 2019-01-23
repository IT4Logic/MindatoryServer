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
import com.it4logic.mindatory.services.common.ApplicationBaseService
import com.it4logic.mindatory.controllers.common.ApplicationControllerEntryPoints
import com.it4logic.mindatory.model.security.SecurityRole
import com.it4logic.mindatory.model.security.SecurityUser
import com.it4logic.mindatory.security.ApplicationSecurityPermissions
import com.it4logic.mindatory.services.security.SecurityRoleService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.Errors
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.validation.Valid


@CrossOrigin
@RestController
@RequestMapping(ApplicationControllerEntryPoints.SecurityRoles)
class SecurityRoleController : ApplicationBaseController<SecurityRole>() {

    @Autowired
    lateinit var securityRoleService: SecurityRoleService

    override fun service(): ApplicationBaseService<SecurityRole> = securityRoleService

    override fun type(): Class<SecurityRole> =  SecurityRole::class.java

    @GetMapping
    @ResponseBody
    @PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SecurityRoleAdminView}', '${ApplicationSecurityPermissions.SecurityRoleAdminCreate}', '${ApplicationSecurityPermissions.SecurityRoleAdminModify}', '${ApplicationSecurityPermissions.SecurityRoleAdminDelete}')")
    override fun doGet(@RequestParam(required = false) filter: String?, pageable: Pageable, request: HttpServletRequest, response: HttpServletResponse): Any
            = doGetInternal(filter, pageable, request, response)

    @GetMapping("{id}")
    @PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SecurityRoleAdminView}', '${ApplicationSecurityPermissions.SecurityRoleAdminCreate}', '${ApplicationSecurityPermissions.SecurityRoleAdminModify}', '${ApplicationSecurityPermissions.SecurityRoleAdminDelete}')")
    override fun doGet(@PathVariable id: Long, request: HttpServletRequest, response: HttpServletResponse): SecurityRole
            = doGetInternal(id, request, response)

    @PostMapping
    @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SecurityRoleAdminCreate}')")
    override fun doCreate(@Valid @RequestBody target: SecurityRole, errors: Errors, request: HttpServletRequest, response: HttpServletResponse): SecurityRole
            = doCreateInternal(target, errors, request, response)

    @PutMapping
    @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SecurityRoleAdminModify}')")
    override fun doUpdate(@Valid @RequestBody target: SecurityRole, errors: Errors, request: HttpServletRequest, response: HttpServletResponse): SecurityRole
            = doUpdateInternal(target, errors, request, response)

    @DeleteMapping("{id}")
    @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SecurityRoleAdminDelete}')")
    override fun doDelete(@PathVariable id: Long, request: HttpServletRequest, response: HttpServletResponse) = doDeleteInternal(id, request, response)

    @GetMapping("{id}/users")
    @PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SecurityRoleAdminView}', '${ApplicationSecurityPermissions.SecurityRoleAdminCreate}', '${ApplicationSecurityPermissions.SecurityRoleAdminModify}', '${ApplicationSecurityPermissions.SecurityRoleAdminDelete}') " +
            " and hasAnyAuthority('${ApplicationSecurityPermissions.SecurityUserAdminView}', '${ApplicationSecurityPermissions.SecurityUserAdminCreate}', '${ApplicationSecurityPermissions.SecurityUserAdminModify}', '${ApplicationSecurityPermissions.SecurityUserAdminDelete}')")
    fun doGetRoleUsers(@PathVariable id: Long) : MutableList<SecurityUser> = securityRoleService.getRoleUsers(id)

    @PostMapping("{id}/users")
    fun doAddUsersToRole(@PathVariable id: Long, @Valid @RequestBody userIdsList: List<Long>) = securityRoleService.addUsersToRole(id, userIdsList)

    @DeleteMapping("{id}/users")
    fun doDeleteUsersFromRole(@PathVariable id: Long, @Valid @RequestBody userIdsList: List<Long>) = securityRoleService.removeUsersFromRole(id, userIdsList)
}