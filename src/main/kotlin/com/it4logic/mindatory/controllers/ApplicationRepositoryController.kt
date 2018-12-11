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
import com.it4logic.mindatory.model.ApplicationRepository
import com.it4logic.mindatory.security.ApplicationSecurityPermissions
import com.it4logic.mindatory.services.ApplicationRepositoryService
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
@RequestMapping(ApplicationControllerEntryPoints.Repositories)
class ApplicationRepositoryController : ApplicationBaseController<ApplicationRepository>() {

  @Autowired
  lateinit var applicationRepositoryService: ApplicationRepositoryService

  override fun service(): ApplicationBaseService<ApplicationRepository> = applicationRepositoryService

  @GetMapping
  @ResponseBody
  @PostAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.ApplicationRepositoryAdminView}', '${ApplicationSecurityPermissions.ApplicationRepositoryAdminCreate}', '${ApplicationSecurityPermissions.ApplicationRepositoryAdminUpdate}', '${ApplicationSecurityPermissions.ApplicationRepositoryAdminDelete}')" +
          " or hasPermission(filterObject, ${ApplicationSecurityPermissions.PermissionRead})")
  override fun doGet(filter: String?, pageable: Pageable, request: HttpServletRequest): Any
          = doGetInternal(filter, pageable, request)

  @GetMapping("{id}")
  @PostAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.ApplicationRepositoryAdminView}', '${ApplicationSecurityPermissions.ApplicationRepositoryAdminCreate}', '${ApplicationSecurityPermissions.ApplicationRepositoryAdminUpdate}', '${ApplicationSecurityPermissions.ApplicationRepositoryAdminDelete}')" +
          " or hasPermission(filterObject, ${ApplicationSecurityPermissions.PermissionRead})")
  override fun doGet(id: Long): ResponseEntity<ApplicationRepository> = doGetInternal(id)

  @PostMapping
  @PreAuthorize("(hasAuthority('${ApplicationSecurityPermissions.SolutionAdminUpdate}') and hasAuthority('${ApplicationSecurityPermissions.ApplicationRepositoryAdminCreate}'))" +
          " or hasPermission(filterObject.solution, ${ApplicationSecurityPermissions.PermissionUpdate})")
  override fun doCreate(target: ApplicationRepository, errors: Errors, response: HttpServletResponse): ResponseEntity<ApplicationRepository>
          = doCreateInternal(target, errors, response)

  @PutMapping
  @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.ApplicationRepositoryAdminUpdate}')" +
          " or hasPermission(filterObject, ${ApplicationSecurityPermissions.PermissionUpdate})")
  override fun doUpdate(target: ApplicationRepository, errors: Errors, request: HttpServletRequest): ResponseEntity<ApplicationRepository>
          = doUpdateInternal(target, errors, request)

  @DeleteMapping("{id}")
  @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.ApplicationRepositoryAdminDelete}')" +
          " or hasPermission(filterObject, ${ApplicationSecurityPermissions.PermissionDelete})")
  override fun doDelete(id: Long): ResponseEntity<Any> = doDeleteInternal(id)
}