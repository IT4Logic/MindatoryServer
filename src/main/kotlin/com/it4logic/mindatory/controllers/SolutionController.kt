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

package com.it4logic.mindatory.controllers

import com.it4logic.mindatory.controllers.common.ApplicationBaseController
import org.springframework.beans.factory.annotation.Autowired
import com.it4logic.mindatory.controllers.common.ApplicationControllerEntryPoints
import com.it4logic.mindatory.model.Solution
import com.it4logic.mindatory.security.ApplicationSecurityPermissions
import com.it4logic.mindatory.services.SolutionService
import com.it4logic.mindatory.services.common.ApplicationBaseService
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.Errors
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@CrossOrigin
@RestController
@RequestMapping(ApplicationControllerEntryPoints.Solutions)
class SolutionController : ApplicationBaseController<Solution>() {

  @Autowired
  lateinit var solutionService: SolutionService

  override fun service(): ApplicationBaseService<Solution> = solutionService

  @GetMapping
  @ResponseBody
  @PostAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SolutionAdminView}', '${ApplicationSecurityPermissions.SolutionAdminCreate}', '${ApplicationSecurityPermissions.SolutionAdminUpdate}', '${ApplicationSecurityPermissions.SolutionAdminDelete}')" +
          " or hasPermission(filterObject, ${ApplicationSecurityPermissions.PermissionRead})")
  override fun doGet(filter: String?, pageable: Pageable, request: HttpServletRequest): Any
          = doGetInternal(filter, pageable, request)

  @GetMapping("{id}")
  @PostAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SolutionAdminView}', '${ApplicationSecurityPermissions.SolutionAdminCreate}', '${ApplicationSecurityPermissions.SolutionAdminUpdate}', '${ApplicationSecurityPermissions.SolutionAdminDelete}')" +
          " or hasPermission(filterObject, ${ApplicationSecurityPermissions.PermissionRead})")
  override fun doGet(id: Long): ResponseEntity<Solution> = doGetInternal(id)

  @PostMapping
  @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SolutionAdminCreate}')")
  override fun doCreate(target: Solution, errors: Errors, response: HttpServletResponse): ResponseEntity<Solution>
          = doCreateInternal(target, errors, response)

  @PutMapping
  @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SolutionAdminUpdate}')" +
          " or hasPermission(filterObject, ${ApplicationSecurityPermissions.PermissionUpdate})")
  override fun doUpdate(target: Solution, errors: Errors, request: HttpServletRequest): ResponseEntity<Solution>
          = doUpdateInternal(target, errors, request)

  @DeleteMapping("{id}")
  @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SolutionAdminDelete}')" +
          " or hasPermission(filterObject, ${ApplicationSecurityPermissions.PermissionDelete})")
  override fun doDelete(id: Long): ResponseEntity<Any> = doDeleteInternal(id)
}