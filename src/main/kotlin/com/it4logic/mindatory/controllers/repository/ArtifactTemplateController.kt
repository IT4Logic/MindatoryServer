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
import com.it4logic.mindatory.services.common.ApplicationBaseService
import com.it4logic.mindatory.services.repository.ArtifactTemplateService
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
@RequestMapping(ApplicationControllerEntryPoints.ARTIFACT_TEMPLATES)
class ArtifactTemplateController : ApplicationBaseController<ArtifactTemplate>() {

  @Autowired
  lateinit var artifactTemplateService: ArtifactTemplateService

  override fun service(): ApplicationBaseService<ArtifactTemplate> {
    return artifactTemplateService
  }
}