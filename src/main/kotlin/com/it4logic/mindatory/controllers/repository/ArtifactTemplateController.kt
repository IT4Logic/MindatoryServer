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
import com.it4logic.mindatory.model.store.ArtifactStore
import com.it4logic.mindatory.services.common.ApplicationBaseService
import com.it4logic.mindatory.services.repository.ArtifactTemplateService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import javax.validation.Valid


//@CrossOrigin
//@RestController
//@RequestMapping(ApplicationControllerEntryPoints.ArtifactTemplates)
//class ArtifactTemplateController : ApplicationBaseController<ArtifactTemplate>() {
//
//  @Autowired
//  lateinit var artifactTemplateService: ArtifactTemplateService
//
//  override fun service(): ApplicationBaseService<ArtifactTemplate> {
//    return artifactTemplateService
//  }
//
//  //  @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SecurityRoleAdd}')")
//  @GetMapping("{id}/design-versions")
//  fun doGetDesignVersions(@PathVariable id: Long) : List<ArtifactTemplateVersion> {
//    return artifactTemplateService.getAllDesignVersions(id)
//  }
//
//  //  @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SecurityRoleAdd}')")
//  @GetMapping("{id}/design-versions/{verId}")
//  fun doGetDesignVersion(@PathVariable id: Long, @PathVariable verId: Long) : ArtifactTemplateVersion {
//    return artifactTemplateService.getDesignVersion(id, verId)
//  }
//
//  //  @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SecurityRoleAdd}')")
//  @PostMapping("{id}/design-versions/start")
//  fun doStartDesignVersion(@PathVariable id: Long) : ResponseEntity<ArtifactTemplateVersion> {
//    val result = artifactTemplateService.startNewDesignVersion(id)
//    val location = ServletUriComponentsBuilder.fromCurrentRequest().path("/design-versions/{id}").buildAndExpand(result.id).toUri()
//    return ResponseEntity.created(location).body(result)
//  }
//
//  //  @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SecurityRoleAdd}')")
//  @PostMapping("{id}/design-versions/release")
//  fun doReleaseDesignVersion(@PathVariable id: Long) : ResponseEntity<ArtifactTemplateVersion> {
//    val result = artifactTemplateService.releaseDesignVersion(id)
//    return ResponseEntity.ok(result)
//  }
//
//  //  @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SecurityRoleAdd}')")
//  @GetMapping("{id}/design-versions/attributes/{verId}")
//  fun doGetAttributes(@PathVariable id: Long, @PathVariable verId: Long) : List<AttributeTemplateVersion> {
//    return artifactTemplateService.getAllAttributes(id, verId)
//  }
//
//  //  @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SecurityRoleAdd}')")
//  @PostMapping("{id}/design-versions/{verId}/attributes/add")
//  fun doAddAttribute(@PathVariable id: Long, @PathVariable verId: Long, @Valid @RequestBody target: AttributeTemplateVersion)  {
//    artifactTemplateService.addAttribute(id, verId, target)
//  }
//
//  //  @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SecurityRoleAdd}')")
//  @PostMapping("{id}/design-versions/{verId}/attributes/{attributeId}/remove")
//  fun doRemoveAttribute(@PathVariable id: Long, @PathVariable verId: Long, @PathVariable attributeId: Long)  {
//    artifactTemplateService.removeAttribute(id, verId, attributeId)
//  }
//
//  //  @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SecurityRoleAdd}')")
//  @PostMapping("{id}/design-versions/{verId}/migrate-stores/{targetVerId}")
//  fun doStoresMigrate(@PathVariable id: Long, @PathVariable verId: Long, @PathVariable targetVerId: Long): List<ArtifactStore> {
//    return artifactTemplateService.migrateStores(id, verId, targetVerId)
//  }
//}