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

package com.it4logic.mindatory.controllers.repository

import com.it4logic.mindatory.controllers.common.ApplicationBaseController
import org.springframework.beans.factory.annotation.Autowired
import com.it4logic.mindatory.controllers.common.ApplicationControllerEntryPoints
import com.it4logic.mindatory.model.repository.Stereotype
import com.it4logic.mindatory.security.ApplicationSecurityPermissions
import com.it4logic.mindatory.services.common.ApplicationBaseService
import com.it4logic.mindatory.services.repository.StereotypeService
import org.springframework.data.domain.Pageable
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.security.access.prepost.PostFilter
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.Errors
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.validation.Valid

@CrossOrigin
@RestController
@RequestMapping(ApplicationControllerEntryPoints.Stereotypes + "{locale}/")
class StereotypeController : ApplicationBaseController<Stereotype>() {

    @Autowired
    lateinit var stereotypeService: StereotypeService

    override fun service(): ApplicationBaseService<Stereotype> = stereotypeService

    override fun type(): Class<Stereotype> =  Stereotype::class.java

    @GetMapping
    @ResponseBody
    @PostFilter("hasAnyAuthority('${ApplicationSecurityPermissions.StereotypeAdminView}', '${ApplicationSecurityPermissions.StereotypeAdminCreate}', '${ApplicationSecurityPermissions.StereotypeAdminModify}', '${ApplicationSecurityPermissions.StereotypeAdminDelete}')" +
            " or hasPermission(filterObject, ${ApplicationSecurityPermissions.PermissionView})" +
            " or hasPermission(filterObject, ${ApplicationSecurityPermissions.PermissionCreate})" +
            " or hasPermission(filterObject, ${ApplicationSecurityPermissions.PermissionModify})" +
            " or hasPermission(filterObject, ${ApplicationSecurityPermissions.PermissionDelete})" )
    override fun doGet(@PathVariable locale: String, @RequestParam(required = false) filter: String?, pageable: Pageable, request: HttpServletRequest, response: HttpServletResponse): Any
            = doGetInternal(locale,filter, pageable, request, response)

    @GetMapping("{id}")
    @PostAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.StereotypeAdminView}', '${ApplicationSecurityPermissions.StereotypeAdminCreate}', '${ApplicationSecurityPermissions.StereotypeAdminModify}', '${ApplicationSecurityPermissions.StereotypeAdminDelete}')" +
            " or hasPermission(returnObject, ${ApplicationSecurityPermissions.PermissionView})" +
            " or hasPermission(returnObject, ${ApplicationSecurityPermissions.PermissionCreate})" +
            " or hasPermission(returnObject, ${ApplicationSecurityPermissions.PermissionModify})" +
            " or hasPermission(returnObject, ${ApplicationSecurityPermissions.PermissionDelete})" )
    override fun doGet(@PathVariable locale: String, @PathVariable id: Long, request: HttpServletRequest, response: HttpServletResponse): Stereotype
            = doGetInternal(locale,id, request, response)

    @PostMapping
    @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.StereotypeAdminCreate}')")
    override fun doCreate(@PathVariable locale: String, @Valid @RequestBody target: Stereotype, errors: Errors, request: HttpServletRequest, response: HttpServletResponse): Stereotype
            = doCreateInternal(locale,target, errors, request, response)

    @PutMapping
    @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.StereotypeAdminModify}')" +
            " or hasPermission(#target, ${ApplicationSecurityPermissions.PermissionModify})")
    override fun doUpdate(@PathVariable locale: String, @Valid @RequestBody target: Stereotype, errors: Errors, request: HttpServletRequest, response: HttpServletResponse): Stereotype
            = doUpdateInternal(locale,target, errors, request, response)

    @DeleteMapping("{id}")
    @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.StereotypeAdminDelete}')" +
            " or hasPermission(#id, 'com.it4logic.mindatory.model.repository.Stereotype', ${ApplicationSecurityPermissions.PermissionDelete})")
    override fun doDelete(@PathVariable locale: String, @PathVariable id: Long, request: HttpServletRequest, response: HttpServletResponse)
            = doDeleteInternal(locale,id, request, response)
}