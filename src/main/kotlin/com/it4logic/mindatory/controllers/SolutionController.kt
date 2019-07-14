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

package com.it4logic.mindatory.controllers

import com.it4logic.mindatory.controllers.common.ApplicationBaseController
import org.springframework.beans.factory.annotation.Autowired
import com.it4logic.mindatory.controllers.common.ApplicationControllerEntryPoints
import com.it4logic.mindatory.model.Solution
import com.it4logic.mindatory.security.*
import com.it4logic.mindatory.services.SolutionService
import com.it4logic.mindatory.services.common.ApplicationBaseService
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
@RequestMapping(ApplicationControllerEntryPoints.Solutions)
class SolutionController : ApplicationBaseController<Solution>() {

	@Autowired
	lateinit var solutionService: SolutionService

	override fun service(): ApplicationBaseService<Solution> = solutionService

	override fun type(): Class<Solution> = Solution::class.java

	@GetMapping
	@ResponseBody
	@PostFilter(
		"hasAnyAuthority('${ApplicationSecurityPermissions.SolutionAdminView}', '${ApplicationSecurityPermissions.SolutionAdminCreate}', '${ApplicationSecurityPermissions.SolutionAdminModify}', '${ApplicationSecurityPermissions.SolutionAdminDelete}')" +
				" or hasPermission(filterObject, ${ApplicationSecurityPermissions.PermissionView})" +
				" or hasPermission(filterObject, ${ApplicationSecurityPermissions.PermissionCreate})" +
				" or hasPermission(filterObject, ${ApplicationSecurityPermissions.PermissionModify})" +
				" or hasPermission(filterObject, ${ApplicationSecurityPermissions.PermissionDelete})"
	)
	fun doGet(
		@PathVariable locale: String, @RequestParam(required = false) filter: String?, pageable: Pageable,
		request: HttpServletRequest,
		response: HttpServletResponse
	): Any = doGetInternal(locale, filter, pageable, request, response)

	@GetMapping("{id}")
	@PostAuthorize(
		"hasAnyAuthority('${ApplicationSecurityPermissions.SolutionAdminView}', '${ApplicationSecurityPermissions.SolutionAdminCreate}', '${ApplicationSecurityPermissions.SolutionAdminModify}', '${ApplicationSecurityPermissions.SolutionAdminDelete}')" +
				" or hasPermission(returnObject, ${ApplicationSecurityPermissions.PermissionView})" +
				" or hasPermission(returnObject, ${ApplicationSecurityPermissions.PermissionCreate})" +
				" or hasPermission(returnObject, ${ApplicationSecurityPermissions.PermissionModify})" +
				" or hasPermission(returnObject, ${ApplicationSecurityPermissions.PermissionDelete})"
	)
	fun doGet(
		@PathVariable locale: String, @PathVariable id: Long, request: HttpServletRequest,
		response: HttpServletResponse
	): Solution = doGetInternal(locale, id, request, response)

	@PostMapping
	@PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SolutionAdminCreate}')")
	fun doCreate(
		@PathVariable locale: String, @Valid @RequestBody target: Solution, errors: Errors,
		request: HttpServletRequest,
		response: HttpServletResponse
	): Solution = doCreateInternal(locale, target, errors, request, response)

	@PutMapping
	@PreAuthorize(
		"hasAuthority('${ApplicationSecurityPermissions.SolutionAdminModify}')" +
				" or hasPermission(#target, ${ApplicationSecurityPermissions.PermissionModify})"
	)
	fun doUpdate(
		@PathVariable locale: String, @Valid @RequestBody target: Solution, errors: Errors,
		request: HttpServletRequest,
		response: HttpServletResponse
	): Solution = doUpdateInternal(locale, target, errors, request, response)

	@DeleteMapping("{id}")
	@PreAuthorize(
		"hasAuthority('${ApplicationSecurityPermissions.SolutionAdminDelete}')" +
				" or hasPermission(#id, 'com.it4logic.mindatory.model.Solution', ${ApplicationSecurityPermissions.PermissionDelete})"
	)
	fun doDelete(
		@PathVariable locale: String, @PathVariable id: Long, request: HttpServletRequest,
		response: HttpServletResponse
	) = doDeleteInternal(locale, id, request, response)
}