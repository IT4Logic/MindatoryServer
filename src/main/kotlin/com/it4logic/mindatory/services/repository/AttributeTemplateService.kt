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

package com.it4logic.mindatory.services.repository

import com.it4logic.mindatory.exceptions.ApplicationErrorCodes
import com.it4logic.mindatory.exceptions.ApplicationObjectNotFoundException
import com.it4logic.mindatory.exceptions.ApplicationValidationException
import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.model.repository.ArtifactTemplateRepository
import com.it4logic.mindatory.model.repository.AttributeTemplate
import com.it4logic.mindatory.model.repository.AttributeTemplateRepository
import com.it4logic.mindatory.model.store.AttributeStoreRepository
import com.it4logic.mindatory.services.RepositoryManagerService
import com.it4logic.mindatory.services.common.ApplicationBaseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.transaction.Transactional


@Service
@Transactional
class AttributeTemplateService : ApplicationBaseService<AttributeTemplate>() {
  @Autowired
  private lateinit var attributeTemplateRepository: AttributeTemplateRepository

  @Autowired
  lateinit var repositoryManagerService: RepositoryManagerService

  @Autowired
  private lateinit var attributeStoreRepository: AttributeStoreRepository

  @Autowired
  private lateinit var artifactTemplateRepository: ArtifactTemplateRepository

  override fun repository(): ApplicationBaseRepository<AttributeTemplate> = attributeTemplateRepository

  override fun type(): Class<AttributeTemplate> = AttributeTemplate::class.java

  override fun beforeCreate(target: AttributeTemplate) {
    if(!repositoryManagerService.hasAttributeTemplateDataType(target.typeUUID))
      throw ApplicationObjectNotFoundException(target.typeUUID, ApplicationErrorCodes.NotFoundAttributeTemplateDataType)
  }

  override fun beforeDelete(target: AttributeTemplate) {
    var count = attributeStoreRepository.countByAttributeTemplateId(target.id)
    if(count > 0)
      throw ApplicationValidationException(ApplicationErrorCodes.ValidationAttributeTemplateHasRelatedStoreData)

    // check if there are attribute templates from this repository used in artifact templates from other repositories
    count = artifactTemplateRepository.countByAttributes_Id(target.id)
    if(count > 0)
      throw ApplicationValidationException(ApplicationErrorCodes.ValidationAttributeTemplateUsedInArtifactTemplates)
  }
}