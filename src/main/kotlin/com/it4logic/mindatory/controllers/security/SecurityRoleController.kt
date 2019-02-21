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
@RequestMapping(ApplicationControllerEntryPoints.SecurityRoles + "{locale}/")
class SecurityRoleController : ApplicationBaseController<SecurityRole>() {

    @Autowired
    lateinit var securityRoleService: SecurityRoleService

    override fun service(): ApplicationBaseService<SecurityRole> = securityRoleService

    override fun type(): Class<SecurityRole> =  SecurityRole::class.java

    @GetMapping
    @ResponseBody
    @PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SecurityRoleAdminView}', '${ApplicationSecurityPermissions.SecurityRoleAdminCreate}', '${ApplicationSecurityPermissions.SecurityRoleAdminModify}', '${ApplicationSecurityPermissions.SecurityRoleAdminDelete}')")
    override fun doGet(@PathVariable locale: String, @RequestParam(required = false) filter: String?, pageable: Pageable, request: HttpServletRequest, response: HttpServletResponse): Any
            = doGetInternal(locale,filter, pageable, request, response)

    @GetMapping("{id}")
    @PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SecurityRoleAdminView}', '${ApplicationSecurityPermissions.SecurityRoleAdminCreate}', '${ApplicationSecurityPermissions.SecurityRoleAdminModify}', '${ApplicationSecurityPermissions.SecurityRoleAdminDelete}')")
    override fun doGet(@PathVariable locale: String, @PathVariable id: Long, request: HttpServletRequest, response: HttpServletResponse): SecurityRole
            = doGetInternal(locale,id, request, response)

    @PostMapping
    @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SecurityRoleAdminCreate}')")
    override fun doCreate(@PathVariable locale: String, @Valid @RequestBody target: SecurityRole, errors: Errors, request: HttpServletRequest, response: HttpServletResponse): SecurityRole
            = doCreateInternal(locale,target, errors, request, response)

    @PutMapping
    @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SecurityRoleAdminModify}')")
    override fun doUpdate(@PathVariable locale: String, @Valid @RequestBody target: SecurityRole, errors: Errors, request: HttpServletRequest, response: HttpServletResponse): SecurityRole
            = doUpdateInternal(locale,target, errors, request, response)

    @DeleteMapping("{id}")
    @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SecurityRoleAdminDelete}')")
    override fun doDelete(@PathVariable locale: String, @PathVariable id: Long, request: HttpServletRequest, response: HttpServletResponse)
            = doDeleteInternal(locale,id, request, response)

    @GetMapping("{id}/users")
    @PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SecurityRoleAdminView}', '${ApplicationSecurityPermissions.SecurityRoleAdminCreate}', '${ApplicationSecurityPermissions.SecurityRoleAdminModify}', '${ApplicationSecurityPermissions.SecurityRoleAdminDelete}') " +
            " and hasAnyAuthority('${ApplicationSecurityPermissions.SecurityUserAdminView}', '${ApplicationSecurityPermissions.SecurityUserAdminCreate}', '${ApplicationSecurityPermissions.SecurityUserAdminModify}', '${ApplicationSecurityPermissions.SecurityUserAdminDelete}')")
    fun doGetRoleUsers(@PathVariable locale: String, @PathVariable id: Long) : MutableList<SecurityUser> {
        propagateLanguage(locale)
        return securityRoleService.getRoleUsers(id)
    }

    @PostMapping("{id}/users")
    fun doAddUsersToRole(@PathVariable locale: String, @PathVariable id: Long, @Valid @RequestBody userIdsList: List<Long>) {
        propagateLanguage(locale)
        securityRoleService.addUsersToRole(id, userIdsList)
    }

    @DeleteMapping("{id}/users")
    fun doDeleteUsersFromRole(@PathVariable locale: String, @PathVariable id: Long, @Valid @RequestBody userIdsList: List<Long>) {
        propagateLanguage(locale)
        securityRoleService.removeUsersFromRole(id, userIdsList)
    }
}