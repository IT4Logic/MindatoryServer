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
import com.it4logic.mindatory.model.repository.ArtifactTemplate
import com.it4logic.mindatory.model.repository.ArtifactTemplateVersion
import com.it4logic.mindatory.model.repository.AttributeTemplateVersion
import com.it4logic.mindatory.security.ApplicationSecurityPermissions
import com.it4logic.mindatory.services.RepositoryManagerService
import com.it4logic.mindatory.services.common.ApplicationBaseService
import com.it4logic.mindatory.services.repository.ArtifactTemplateService
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
@RequestMapping(ApplicationControllerEntryPoints.ArtifactTemplates)
class ArtifactTemplateController : ApplicationBaseController<ArtifactTemplate>() {

    @Autowired
    lateinit var repositoryManagerService: RepositoryManagerService

    @Autowired
    lateinit var attributeTemplatesService: ArtifactTemplateService

    override fun service(): ApplicationBaseService<ArtifactTemplate> = attributeTemplatesService

    override fun type(): Class<ArtifactTemplate> = ArtifactTemplate::class.java

    // ====================================================== Basic operations ======================================================

    @GetMapping
    @ResponseBody
    @PostFilter("hasAnyAuthority('${ApplicationSecurityPermissions.ArtifactTemplateAdminView}', '${ApplicationSecurityPermissions.ArtifactTemplateAdminCreate}', '${ApplicationSecurityPermissions.ArtifactTemplateAdminModify}', '${ApplicationSecurityPermissions.ArtifactTemplateAdminDelete}')" +
            " or hasPermission(filterObject, ${ApplicationSecurityPermissions.PermissionView})")
    override fun doGet(@RequestParam(required = false) filter: String?, pageable: Pageable, request: HttpServletRequest, response: HttpServletResponse): Any
            = doGetInternal(filter, pageable, request, response)

    @GetMapping("{id}")
    @PostAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.ArtifactTemplateAdminView}', '${ApplicationSecurityPermissions.ArtifactTemplateAdminCreate}', '${ApplicationSecurityPermissions.ArtifactTemplateAdminModify}', '${ApplicationSecurityPermissions.ArtifactTemplateAdminDelete}')" +
            " or hasPermission(returnObject, ${ApplicationSecurityPermissions.PermissionView})")
    override fun doGet(@PathVariable id: Long, request: HttpServletRequest, response: HttpServletResponse): ArtifactTemplate
            = doGetInternal(id, request, response)

    @PostMapping
    @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.ArtifactTemplateAdminCreate}')")
    override fun doCreate(@Valid @RequestBody target: ArtifactTemplate, errors: Errors, request: HttpServletRequest, response: HttpServletResponse): ArtifactTemplate
            = doCreateInternal(target, errors, request, response)

    @PutMapping
    @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.ArtifactTemplateAdminModify}')" +
            " or hasPermission(#target, ${ApplicationSecurityPermissions.PermissionModify})")
    override fun doUpdate(@Valid @RequestBody target: ArtifactTemplate, errors: Errors, request: HttpServletRequest, response: HttpServletResponse): ArtifactTemplate
            = doUpdateInternal(target, errors, request, response)

    @DeleteMapping("{id}")
    @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.ArtifactTemplateAdminDelete}')" +
            " or hasPermission(#id, 'com.it4logic.mindatory.model.repository.ArtifactTemplate', ${ApplicationSecurityPermissions.PermissionDelete})")
    override fun doDelete(@PathVariable id: Long, request: HttpServletRequest, response: HttpServletResponse) = doDeleteInternal(id, request, response)


    // ====================================================== Attributes ======================================================

    @GetMapping("{id}/design-versions/{verId}/attributes")
    @PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.ArtifactTemplateAdminView}', '${ApplicationSecurityPermissions.ArtifactTemplateAdminCreate}', '${ApplicationSecurityPermissions.ArtifactTemplateAdminModify}', '${ApplicationSecurityPermissions.ArtifactTemplateAdminDelete}')" +
            " or hasPermission(#id, 'com.it4logic.mindatory.model.repository.ArtifactTemplate', ${ApplicationSecurityPermissions.PermissionView})")
    fun doGetAttributes(@PathVariable id: Long, @PathVariable verId: Long): List<AttributeTemplateVersion> = attributeTemplatesService.getAllAttributes(id, verId)

    @PostMapping("{id}/design-versions/{verId}/attributes/add")
    @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.ArtifactTemplateAdminCreate}')" +
            " or hasPermission(#id, 'com.it4logic.mindatory.model.repository.ArtifactTemplate', ${ApplicationSecurityPermissions.PermissionCreate})")
    fun doAddAttributes(@PathVariable id: Long, @PathVariable verId: Long, @RequestBody attributesList : List<Long>, response: HttpServletResponse) = attributeTemplatesService.addAttributes(id, verId, attributesList)

    @PostMapping("{id}/design-versions/{verId}/attributes/remove")
    @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.ArtifactTemplateAdminCreate}')" +
            " or hasPermission(#id, 'com.it4logic.mindatory.model.repository.ArtifactTemplate', ${ApplicationSecurityPermissions.PermissionCreate})")
    fun doRemoveAttributes(@PathVariable id: Long, @PathVariable verId: Long, @RequestBody attributesList : List<Long>, response: HttpServletResponse) = attributeTemplatesService.removeAttributes(id, verId, attributesList)


    // ====================================================== Design Versions ======================================================

    @GetMapping("{id}/design-versions")
    @PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.ArtifactTemplateAdminView}', '${ApplicationSecurityPermissions.ArtifactTemplateAdminCreate}', '${ApplicationSecurityPermissions.ArtifactTemplateAdminModify}', '${ApplicationSecurityPermissions.ArtifactTemplateAdminDelete}')" +
            " or hasPermission(#id, 'com.it4logic.mindatory.model.repository.ArtifactTemplate', ${ApplicationSecurityPermissions.PermissionView})")
    fun doGetDesignVersions(@PathVariable id: Long): List<ArtifactTemplateVersion> = attributeTemplatesService.getAllDesignVersions(id)

    @GetMapping("{id}/design-versions/{verId}")
    @PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.ArtifactTemplateAdminView}', '${ApplicationSecurityPermissions.ArtifactTemplateAdminCreate}', '${ApplicationSecurityPermissions.ArtifactTemplateAdminModify}', '${ApplicationSecurityPermissions.ArtifactTemplateAdminDelete}')" +
            " or hasPermission(#id, 'com.it4logic.mindatory.model.repository.ArtifactTemplate', ${ApplicationSecurityPermissions.PermissionView})")
    fun doGetDesignVersion(@PathVariable id: Long, @PathVariable verId: Long): ArtifactTemplateVersion = attributeTemplatesService.getDesignVersion(id, verId)

    @PostMapping("{id}/design-versions")
    @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.ArtifactTemplateAdminCreate}')" +
            " or hasPermission(#id, 'com.it4logic.mindatory.model.repository.ArtifactTemplate', ${ApplicationSecurityPermissions.PermissionCreate})")
    fun doCreateDesignVersion(@PathVariable id: Long, @Valid @RequestBody target: ArtifactTemplateVersion, errors: Errors, response: HttpServletResponse): ArtifactTemplateVersion {
        if(errors.hasErrors())
            throw RepositoryConstraintViolationException(errors)

        val result = attributeTemplatesService.createVersion(id, target)
        val location = ServletUriComponentsBuilder.fromCurrentRequest().path("/design-versions/{id}").buildAndExpand(result.id).toUri()
        response.status = HttpStatus.CREATED.value()
        response.addHeader("Location", location.path)
        return result
    }

    @PutMapping("{id}/design-versions")
    @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.ArtifactTemplateAdminModify}')" +
            " or hasPermission(#id, 'com.it4logic.mindatory.model.repository.ArtifactTemplate', ${ApplicationSecurityPermissions.PermissionModify})")
    fun doUpdateDesignVersion(@PathVariable id: Long, @Valid @RequestBody target: ArtifactTemplateVersion, errors: Errors): ArtifactTemplateVersion {
        if(errors.hasErrors())
            throw RepositoryConstraintViolationException(errors)

        return attributeTemplatesService.updateVersion(id, target)
    }

    @DeleteMapping("{id}/design-versions/{verId}")
    @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.ArtifactTemplateAdminDelete}')" +
            " or hasPermission(#id, 'com.it4logic.mindatory.model.repository.ArtifactTemplate', ${ApplicationSecurityPermissions.PermissionDelete})")
    fun doDeleteDesignVersion(@PathVariable id: Long, @PathVariable verId: Long) = attributeTemplatesService.deleteVersion(id, verId)

    @PostMapping("{id}/design-versions/{verId}/release")
    @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.ArtifactTemplateAdminModify}')" +
            " or hasPermission(#id, 'com.it4logic.mindatory.model.repository.ArtifactTemplate', ${ApplicationSecurityPermissions.PermissionModify})")
    fun doReleaseDesignVersion(@PathVariable id: Long, @PathVariable verId: Long): ArtifactTemplateVersion = attributeTemplatesService.releaseVersion(id, verId)
}