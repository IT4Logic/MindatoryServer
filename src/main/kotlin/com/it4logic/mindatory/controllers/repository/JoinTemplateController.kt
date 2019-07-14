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

package com.it4logic.mindatory.controllers.repository

import com.it4logic.mindatory.controllers.common.ApplicationBaseController
import org.springframework.beans.factory.annotation.Autowired
import com.it4logic.mindatory.controllers.common.ApplicationControllerEntryPoints
import com.it4logic.mindatory.model.repository.JoinTemplateVersion
import com.it4logic.mindatory.model.repository.JoinTemplate
import com.it4logic.mindatory.model.store.JoinStore
import com.it4logic.mindatory.security.ApplicationSecurityPermissions
import com.it4logic.mindatory.services.common.ApplicationBaseService
import com.it4logic.mindatory.services.repository.JoinTemplateService
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.Errors
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.validation.Valid

@CrossOrigin
@RestController
@RequestMapping(ApplicationControllerEntryPoints.JoinTemplates)
class JoinTemplateController : ApplicationBaseController<JoinTemplate>() {
	@Autowired
	lateinit var joinTemplateService: JoinTemplateService

	override fun service(): ApplicationBaseService<JoinTemplate> = joinTemplateService

	override fun type(): Class<JoinTemplate> = JoinTemplate::class.java

	@GetMapping
	@ResponseBody
	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.JoinTemplateAdminView}') ")
	fun doGet(
		@PathVariable locale: String, @RequestParam(required = false) filter: String?, pageable: Pageable,
		request: HttpServletRequest,
		response: HttpServletResponse
	): Any = doGetInternal(locale, filter, pageable, request, response)

	@GetMapping("{id}")
	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.JoinTemplateAdminView}') ")
	fun doGet(
		@PathVariable locale: String, @PathVariable id: Long, request: HttpServletRequest,
		response: HttpServletResponse
	): JoinTemplate = doGetInternal(locale, id, request, response)

	@PostMapping
	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.JoinTemplateAdminCreate}') ")
	fun doCreate(
		@PathVariable locale: String, @Valid @RequestBody target: JoinTemplate, errors: Errors,
		request: HttpServletRequest,
		response: HttpServletResponse
	): JoinTemplate = doCreateInternal(locale, target, errors, request, response)

	@PutMapping
	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.JoinTemplateAdminModify}') ")
	fun doUpdate(
		@PathVariable locale: String, @Valid @RequestBody target: JoinTemplate, errors: Errors,
		request: HttpServletRequest,
		response: HttpServletResponse
	): JoinTemplate = doUpdateInternal(locale, target, errors, request, response)

	@DeleteMapping("{id}")
	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.JoinTemplateAdminDelete}') ")
	fun doDelete(
		@PathVariable locale: String, @PathVariable id: Long, request: HttpServletRequest,
		response: HttpServletResponse
	) = doDeleteInternal(locale, id, request, response)

	// Design Versions

	@GetMapping("{id}/design-versions")
	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.JoinTemplateAdminView}') ")
	fun doGetDesignVersions(@PathVariable id: Long): List<JoinTemplateVersion> =
		joinTemplateService.getAllDesignVersions(id)

	@GetMapping("{id}/design-versions/{verId}")
	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.JoinTemplateAdminView}') ")
	fun doGetDesignVersion(@PathVariable id: Long, @PathVariable verId: Long): JoinTemplateVersion =
		joinTemplateService.getDesignVersion(id, verId)

	@PostMapping("{id}/design-versions")
	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.JoinTemplateAdminCreate}') ")
	fun doCreateDesignVersion(@PathVariable id: Long, @Valid @RequestBody target: JoinTemplateVersion, response: HttpServletResponse): JoinTemplateVersion {
		val result = joinTemplateService.createVersion(id, target)
		val location =
			ServletUriComponentsBuilder.fromCurrentRequest().path("/design-versions/{id}").buildAndExpand(result.id)
				.toUri()
		response.status = HttpStatus.CREATED.value()
		response.addHeader("Location", location.path)
		return result
	}

	@PutMapping("{id}/design-versions")
	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.JoinTemplateAdminModify}') ")
	fun doUpdateDesignVersion(@PathVariable id: Long, @Valid @RequestBody target: JoinTemplateVersion): JoinTemplateVersion =
		joinTemplateService.updateVersion(id, target)

	@DeleteMapping("{id}/design-versions/{verId}")
	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.JoinTemplateAdminDelete}') ")
	fun doDeleteDesignVersion(@PathVariable id: Long, @PathVariable verId: Long) =
		joinTemplateService.deleteVersion(id, verId)

	@PostMapping("{id}/design-versions/{verId}/release")
	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.JoinTemplateAdminModify}') ")
	fun doReleaseDesignVersion(@PathVariable id: Long, @PathVariable verId: Long): JoinTemplateVersion =
		joinTemplateService.releaseVersion(id, verId)

	@PostMapping("{id}/design-versions/{verId}/migrate-stores/{targetVerId}")
	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.JoinTemplateAdminModify}') ")
	fun doStoresMigrate(@PathVariable id: Long, @PathVariable verId: Long, @PathVariable targetVerId: Long): List<JoinStore> =
		joinTemplateService.migrateStores(id, verId, targetVerId)
}