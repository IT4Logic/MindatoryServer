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
import com.it4logic.mindatory.model.repository.JoinTemplate
import com.it4logic.mindatory.model.repository.JoinTemplateVersion
import com.it4logic.mindatory.model.store.JoinStore
import com.it4logic.mindatory.services.common.ApplicationBaseService
import com.it4logic.mindatory.services.repository.JoinTemplateService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import javax.validation.Valid

//@CrossOrigin
//@RestController
//@RequestMapping(ApplicationControllerEntryPoints.JoinTemplates)
//class JoinTemplateController : ApplicationBaseController<JoinTemplate>() {
//
//  @Autowired
//  lateinit var joinTemplateService: JoinTemplateService
//
//  override fun service(): ApplicationBaseService<JoinTemplate> {
//    return joinTemplateService
//  }
//
//  //  @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SecurityRoleAdd}')")
//  @GetMapping("{id}/design-versions")
//  fun doGetDesignVersions(@PathVariable id: Long) : List<JoinTemplateVersion> {
//    return joinTemplateService.getAllDesignVersions(id)
//  }
//
//  //  @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SecurityRoleAdd}')")
//  @GetMapping("{id}/design-versions/{verId}")
//  fun doGetDesignVersion(@PathVariable id: Long, @PathVariable verId: Long) : JoinTemplateVersion {
//    return joinTemplateService.getDesignVersion(id, verId)
//  }
//
//  //  @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SecurityRoleAdd}')")
//  @PostMapping("{id}/design-versions")
//  fun doCreateDesignVersion(@PathVariable id: Long, @Valid @RequestBody target: JoinTemplateVersion) : ResponseEntity<JoinTemplateVersion> {
//    val result = joinTemplateService.createVersion(id, target)
//    val location = ServletUriComponentsBuilder.fromCurrentRequest().path("/design-versions/{id}").buildAndExpand(result.id).toUri()
//    return ResponseEntity.created(location).body(result)
//  }
//
//  //  @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SecurityRoleAdd}')")
//  @PutMapping("{id}/design-versions")
//  fun doUpdateDesignVersion(@PathVariable id: Long, @Valid @RequestBody target: JoinTemplateVersion) : ResponseEntity<JoinTemplateVersion> {
//    val result = joinTemplateService.updateVersion(id, target)
//    return ResponseEntity.ok(result)
//  }
//
//  //  @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SecurityRoleAdd}')")
//  @DeleteMapping("{id}/design-versions/{verId}")
//  fun doUpdateDesignVersion(@PathVariable id: Long, @PathVariable verId: Long) {
//    joinTemplateService.deleteVersion(id, verId)
//  }
//
//  //  @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SecurityRoleAdd}')")
//  @PostMapping("{id}/design-versions/{verId}")
//  fun doReleaseDesignVersion(@PathVariable id: Long, @PathVariable verId: Long) : ResponseEntity<JoinTemplateVersion> {
//    val result = joinTemplateService.releaseVersion(id, verId)
//    return ResponseEntity.ok(result)
//  }
//
//  //  @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SecurityRoleAdd}')")
//  @PostMapping("{id}/design-versions/{verId}/migrate-stores/{targetVerId}")
//  fun doStoresMigrate(@PathVariable id: Long, @PathVariable verId: Long, @PathVariable targetVerId: Long): List<JoinStore> {
//    return joinTemplateService.migrateStores(id, verId, targetVerId)
//  }
//}