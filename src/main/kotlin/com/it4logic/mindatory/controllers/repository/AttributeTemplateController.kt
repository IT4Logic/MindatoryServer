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

import com.it4logic.mindatory.api.plugins.AttributeTemplateDataType
import com.it4logic.mindatory.controllers.common.ApplicationBaseController
import org.springframework.beans.factory.annotation.Autowired
import com.it4logic.mindatory.controllers.common.ApplicationControllerEntryPoints
import com.it4logic.mindatory.exceptions.ApplicationErrorCodes
import com.it4logic.mindatory.exceptions.ObjectNotFoundException
import com.it4logic.mindatory.model.repository.AttributeTemplate
import com.it4logic.mindatory.services.repository.AttributeTemplateService
import com.it4logic.mindatory.services.RepositoryManagerService
import com.it4logic.mindatory.services.common.ApplicationBaseService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
@RequestMapping(ApplicationControllerEntryPoints.ATTRIBUTE_TEMPLATES)
class AttributeTemplateController : ApplicationBaseController<AttributeTemplate>() {

  @Autowired
  lateinit var repositoryManagerService: RepositoryManagerService

  @Autowired
  lateinit var attributeTemplatesService: AttributeTemplateService


  override fun service(): ApplicationBaseService<AttributeTemplate> {
    return attributeTemplatesService
  }

  //  @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SecurityRoleView}')")
  @GetMapping("/data-types")
  @ResponseBody
  fun doGetDataTypes(): List<AttributeTemplateDataType> {
      return repositoryManagerService.getAttributeTemplateDataTypes()
  }

  //  @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SecurityRoleView}')")
  @GetMapping("/data-types/{uuid}")
  fun doGetDataType(@PathVariable uuid: String) : ResponseEntity<AttributeTemplateDataType> {
    return ResponseEntity.ok(repositoryManagerService.getAttributeTemplateDataType(uuid))
  }

  override fun beforeCreate(target: AttributeTemplate) {
    if(!repositoryManagerService.hasAttributeTemplateDataType(target.typeUUID))
      throw ObjectNotFoundException(target.typeUUID, ApplicationErrorCodes.NotFoundAttributeTemplateDataType)
  }
}