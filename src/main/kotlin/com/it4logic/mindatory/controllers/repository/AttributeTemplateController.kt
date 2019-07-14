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
import com.it4logic.mindatory.model.repository.AttributeTemplate
import com.it4logic.mindatory.model.repository.AttributeTemplateVersion
import com.it4logic.mindatory.security.ApplicationSecurityPermissions
import com.it4logic.mindatory.services.repository.AttributeTemplateService
import com.it4logic.mindatory.services.common.ApplicationBaseService
import org.springframework.data.domain.Pageable
import org.springframework.data.rest.core.RepositoryConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.security.access.prepost.PostFilter
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.Errors
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.validation.Valid

@CrossOrigin
@RestController
@RequestMapping(ApplicationControllerEntryPoints.AttributeTemplates)
class AttributeTemplateController : ApplicationBaseController<AttributeTemplate>() {

	@Autowired
	lateinit var attributeTemplatesService: AttributeTemplateService

	override fun service(): ApplicationBaseService<AttributeTemplate> = attributeTemplatesService

	override fun type(): Class<AttributeTemplate> = AttributeTemplate::class.java

	// ====================================================== Basic operations ======================================================

	@GetMapping
	@ResponseBody
	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.AttributeTemplateAdminView}') ")
	fun doGet(
		@PathVariable locale: String, @RequestParam(required = false) filter: String?, pageable: Pageable,
		request: HttpServletRequest,
		response: HttpServletResponse
	): Any = doGetInternal(locale, filter, pageable, request, response)

	@GetMapping("{id}")
	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.AttributeTemplateAdminView}') ")
	fun doGet(
		@PathVariable locale: String, @PathVariable id: Long, request: HttpServletRequest,
		response: HttpServletResponse
	): AttributeTemplate = doGetInternal(locale, id, request, response)

	@PostMapping
	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.AttributeTemplateAdminCreate}') ")
	fun doCreate(
		@PathVariable locale: String, @Valid @RequestBody target: AttributeTemplate, errors: Errors,
		request: HttpServletRequest,
		response: HttpServletResponse
	): AttributeTemplate = doCreateInternal(locale, target, errors, request, response)

	@PutMapping
	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.AttributeTemplateAdminModify}') ")
	fun doUpdate(
		@PathVariable locale: String, @Valid @RequestBody target: AttributeTemplate, errors: Errors,
		request: HttpServletRequest,
		response: HttpServletResponse
	): AttributeTemplate = doUpdateInternal(locale, target, errors, request, response)

	@DeleteMapping("{id}")
	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.AttributeTemplateAdminDelete}') ")
	fun doDelete(
		@PathVariable locale: String, @PathVariable id: Long, request: HttpServletRequest,
		response: HttpServletResponse
	) = doDeleteInternal(locale, id, request, response)

	// ====================================================== Design Versions ======================================================

	@GetMapping("{id}/design-versions")
	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.AttributeTemplateAdminView}') ")
	fun doGetDesignVersions(@PathVariable id: Long): List<AttributeTemplateVersion> =
		attributeTemplatesService.getAllDesignVersions(id)

	@GetMapping("{id}/design-versions/{verId}")
	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.AttributeTemplateAdminView}') ")
	fun doGetDesignVersion(@PathVariable id: Long, @PathVariable verId: Long): AttributeTemplateVersion =
		attributeTemplatesService.getDesignVersion(id, verId)

	@PostMapping("{id}/design-versions")
	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.AttributeTemplateAdminCreate}') ")
	fun doCreateDesignVersion(
		@PathVariable id: Long, @Valid @RequestBody target: AttributeTemplateVersion, errors: Errors,
		response: HttpServletResponse
	): AttributeTemplateVersion {
		if (errors.hasErrors())
			throw RepositoryConstraintViolationException(errors)

		val result = attributeTemplatesService.createVersion(id, target)
		val location =
			ServletUriComponentsBuilder.fromCurrentRequest().path("/design-versions/{id}").buildAndExpand(result.id)
				.toUri()
		response.status = HttpStatus.CREATED.value()
		response.addHeader("Location", location.path)
		return result
	}

	@PutMapping("{id}/design-versions")
	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.AttributeTemplateAdminModify}') ")
	fun doUpdateDesignVersion(@PathVariable id: Long, @Valid @RequestBody target: AttributeTemplateVersion, errors: Errors): AttributeTemplateVersion {
		if (errors.hasErrors())
			throw RepositoryConstraintViolationException(errors)

		return attributeTemplatesService.updateVersion(id, target)
	}

	@DeleteMapping("{id}/design-versions/{verId}")
	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.AttributeTemplateAdminDelete}') ")
	fun doDeleteDesignVersion(@PathVariable id: Long, @PathVariable verId: Long) =
		attributeTemplatesService.deleteVersion(id, verId)

	@PostMapping("{id}/design-versions/{verId}/release")
	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.AttributeTemplateAdminModify}') ")
	fun doReleaseDesignVersion(@PathVariable id: Long, @PathVariable verId: Long): AttributeTemplateVersion =
		attributeTemplatesService.releaseVersion(id, verId)
}