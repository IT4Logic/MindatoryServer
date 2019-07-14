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

package com.it4logic.mindatory.controllers.store

import com.it4logic.mindatory.controllers.common.ApplicationBaseController
import org.springframework.beans.factory.annotation.Autowired
import com.it4logic.mindatory.controllers.common.ApplicationControllerEntryPoints
import com.it4logic.mindatory.model.store.JoinStore
import com.it4logic.mindatory.security.ApplicationSecurityPermissions
import com.it4logic.mindatory.services.common.ApplicationBaseService
import com.it4logic.mindatory.services.store.JoinStoreService
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
@RequestMapping(ApplicationControllerEntryPoints.JoinStores)
class JoinStoreController : ApplicationBaseController<JoinStore>() {

	@Autowired
	lateinit var attributeStoreService: JoinStoreService

	override fun service(): ApplicationBaseService<JoinStore> = attributeStoreService

	override fun type(): Class<JoinStore> = JoinStore::class.java

// ====================================================== Basic operations ======================================================

	@GetMapping
	@ResponseBody
	@PostFilter("hasAnyAuthority('${ApplicationSecurityPermissions.JoinStoreAdminView}', '${ApplicationSecurityPermissions.JoinStoreAdminCreate}', '${ApplicationSecurityPermissions.JoinStoreAdminModify}', '${ApplicationSecurityPermissions.JoinStoreAdminDelete}')" +
			" or hasPermission(filterObject, ${ApplicationSecurityPermissions.PermissionView})" +
			" or hasPermission(filterObject, ${ApplicationSecurityPermissions.PermissionCreate})" +
			" or hasPermission(filterObject, ${ApplicationSecurityPermissions.PermissionModify})" +
			" or hasPermission(filterObject, ${ApplicationSecurityPermissions.PermissionDelete})" )
	fun doGet(@PathVariable locale: String, @RequestParam(required = false) filter: String?, pageable: Pageable, request: HttpServletRequest, response: HttpServletResponse): Any
			= doGetInternal(locale,filter, pageable, request, response)

	@GetMapping("{id}")
	@PostAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.JoinStoreAdminView}', '${ApplicationSecurityPermissions.JoinStoreAdminCreate}', '${ApplicationSecurityPermissions.JoinStoreAdminModify}', '${ApplicationSecurityPermissions.JoinStoreAdminDelete}')" +
			" or hasPermission(returnObject, ${ApplicationSecurityPermissions.PermissionView})" +
			" or hasPermission(returnObject, ${ApplicationSecurityPermissions.PermissionCreate})" +
			" or hasPermission(returnObject, ${ApplicationSecurityPermissions.PermissionModify})" +
			" or hasPermission(returnObject, ${ApplicationSecurityPermissions.PermissionDelete})" )
	fun doGet(@PathVariable locale: String, @PathVariable id: Long, request: HttpServletRequest, response: HttpServletResponse): JoinStore
			= doGetInternal(locale,id, request, response)

	@PostMapping
	@PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.JoinStoreAdminCreate}')")
	fun doCreate(@PathVariable locale: String, @Valid @RequestBody target: JoinStore, errors: Errors, request: HttpServletRequest, response: HttpServletResponse): JoinStore
			= doCreateInternal(locale,target, errors, request, response)

	@PutMapping
	@PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.JoinStoreAdminModify}')" +
			" or hasPermission(#target, ${ApplicationSecurityPermissions.PermissionModify})")
	fun doUpdate(@PathVariable locale: String, @Valid @RequestBody target: JoinStore, errors: Errors, request: HttpServletRequest, response: HttpServletResponse): JoinStore
			= doUpdateInternal(locale,target, errors, request, response)

	@DeleteMapping("{id}")
	@PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.JoinStoreAdminDelete}')" +
			" or hasPermission(#id, 'com.it4logic.mindatory.model.repository.JoinStore', ${ApplicationSecurityPermissions.PermissionDelete})")
	fun doDelete(@PathVariable locale: String, @PathVariable id: Long, request: HttpServletRequest, response: HttpServletResponse)
			= doDeleteInternal(locale,id, request, response)
}