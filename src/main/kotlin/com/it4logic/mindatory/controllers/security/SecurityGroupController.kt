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
import com.it4logic.mindatory.model.security.SecurityGroup
import com.it4logic.mindatory.model.security.SecurityUser
import com.it4logic.mindatory.security.ApplicationSecurityPermissions
import com.it4logic.mindatory.services.common.ApplicationBaseService
import com.it4logic.mindatory.services.security.SecurityGroupService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.Errors
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.validation.Valid


@CrossOrigin
@RestController
@RequestMapping(ApplicationControllerEntryPoints.SecurityGroups)
class SecurityGroupController : ApplicationBaseController<SecurityGroup>() {
    @Autowired
    lateinit var securityGroupService: SecurityGroupService

    override fun service(): ApplicationBaseService<SecurityGroup> = securityGroupService

    @GetMapping
    @ResponseBody
    @PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SecurityGroupAdminView}', '${ApplicationSecurityPermissions.SecurityGroupAdminCreate}', '${ApplicationSecurityPermissions.SecurityGroupAdminUpdate}', '${ApplicationSecurityPermissions.SecurityGroupAdminDelete}')")
    override fun doGet(@RequestParam(required = false) filter: String?, pageable: Pageable, request: HttpServletRequest): Any
            = doGetInternal(filter, pageable, request)

    @GetMapping("{id}")
    @PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SecurityGroupAdminView}', '${ApplicationSecurityPermissions.SecurityGroupAdminCreate}', '${ApplicationSecurityPermissions.SecurityGroupAdminUpdate}', '${ApplicationSecurityPermissions.SecurityGroupAdminDelete}')")
    override fun doGet(@PathVariable id: Long): ResponseEntity<SecurityGroup> = doGetInternal(id)

    @PostMapping
    @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SecurityGroupAdminCreate}')")
    override fun doCreate(@Valid @RequestBody target: SecurityGroup, errors: Errors, response: HttpServletResponse): ResponseEntity<SecurityGroup>
            = doCreateInternal(target, errors, response)

    @PutMapping
    @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SecurityGroupAdminUpdate}')")
    override fun doUpdate(@Valid @RequestBody target: SecurityGroup, errors: Errors, request: HttpServletRequest): ResponseEntity<SecurityGroup>
            = doUpdateInternal(target, errors, request)

    @DeleteMapping("{id}")
    @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SecurityGroupAdminDelete}')")
    override fun doDelete(@PathVariable id: Long): ResponseEntity<Any> = doDeleteInternal(id)

    @GetMapping("{id}/users")
    @PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SecurityGroupAdminView}', '${ApplicationSecurityPermissions.SecurityGroupAdminCreate}', '${ApplicationSecurityPermissions.SecurityGroupAdminUpdate}', '${ApplicationSecurityPermissions.SecurityGroupAdminDelete}')" +
            " and hasAnyAuthority('${ApplicationSecurityPermissions.SecurityUserAdminView}', '${ApplicationSecurityPermissions.SecurityUserAdminCreate}', '${ApplicationSecurityPermissions.SecurityUserAdminUpdate}', '${ApplicationSecurityPermissions.SecurityUserAdminDelete}')")
    fun doGetGroupUsers(@PathVariable id: Long) : MutableList<SecurityUser> = securityGroupService.getGroupUsers(id)

    @PostMapping("{id}/users")
    fun doAssignUsersToGroup(@PathVariable id: Long, @Valid @RequestBody userIdsList: List<Long>) = securityGroupService.assignUsersToGroup(id, userIdsList)
}