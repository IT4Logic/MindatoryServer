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

package com.it4logic.mindatory.services

import com.it4logic.mindatory.exceptions.ApplicationErrorCodes
import com.it4logic.mindatory.exceptions.ApplicationValidationException
import com.it4logic.mindatory.model.ApplicationRepository
import com.it4logic.mindatory.model.ApplicationRepositoryRepository
import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.model.repository.*
import com.it4logic.mindatory.model.store.ArtifactStoreRepository
import com.it4logic.mindatory.model.store.AttributeStoreRepository
import com.it4logic.mindatory.model.store.JoinStoreRepository
import com.it4logic.mindatory.services.common.ApplicationBaseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.transaction.Transactional


@Service
@Transactional
class ApplicationRepositoryService : ApplicationBaseService<ApplicationRepository>() {
  @Autowired
  private lateinit var applicationRepositoryRepository: ApplicationRepositoryRepository

  @Autowired
  private lateinit var artifactTemplateVersionRepository: ArtifactTemplateVersionRepository

  @Autowired
  private lateinit var joinTemplateVersionRepository: JoinTemplateVersionRepository

  @Autowired
  private lateinit var artifactStoreRepository: ArtifactStoreRepository

  @Autowired
  private lateinit var attributeStoreRepository: AttributeStoreRepository

  @Autowired
  private lateinit var joinStoreRepository: JoinStoreRepository

  override fun repository(): ApplicationBaseRepository<ApplicationRepository> = applicationRepositoryRepository

  override fun type(): Class<ApplicationRepository> = ApplicationRepository::class.java

  override fun beforeDelete(target: ApplicationRepository) {
    // check if there are artifacts stores based on artifact templates from this repository
    var count = artifactStoreRepository.countByArtifactTemplateRepositoryId(target.id)
    if(count > 0)
      throw ApplicationValidationException(ApplicationErrorCodes.ValidationRepositoryHasArtifactTemplatesRelatedStoreData)

    // check if there are attribute stores based on attribute templates from this repository
    count = attributeStoreRepository.countByAttributeTemplateRepositoryId(target.id)
    if(count > 0)
      throw ApplicationValidationException(ApplicationErrorCodes.ValidationRepositoryHasAttributeTemplatesRelatedStoreData)

    // check if there are join stores based on join templates from this repository
    count = joinStoreRepository.countByJoinTemplateRepositoryId(target.id)
    if(count > 0)
      throw ApplicationValidationException(ApplicationErrorCodes.ValidationRepositoryHasJoinTemplatesRelatedStoreData)

    // check if there are artifact templates from this repository used in joins templates from other repositories
    count = joinTemplateVersionRepository.countByRepositoryIdNotAndSourceArtifacts_RepositoryId(target.id, target.id)
    if(count > 0)
      throw ApplicationValidationException(ApplicationErrorCodes.ValidationRepositoryHasArtifactTemplatesUsedInJoinTemplatesFromOtherRepositories)

    count = joinTemplateVersionRepository.countByRepositoryIdNotAndTargetArtifacts_RepositoryId(target.id, target.id)
    if(count > 0)
      throw ApplicationValidationException(ApplicationErrorCodes.ValidationRepositoryHasArtifactTemplatesUsedInJoinTemplatesFromOtherRepositories)

    // check if there are attribute templates from this repository used in artifact templates from other repositories
    count = artifactTemplateVersionRepository.countByRepositoryIdNotAndAttributes_RepositoryId(target.id, target.id)
    if(count > 0)
      throw ApplicationValidationException(ApplicationErrorCodes.ValidationRepositoryHasArtifactTemplatesUsedInArtifactTemplatesFromOtherRepositories)

    // check if there are stereotypes from this repository used in join templates from other repositories
    count = joinTemplateVersionRepository.countByRepositoryIdNotAndSourceStereotypeRepositoryId(target.id, target.id)
    if(count > 0)
      throw ApplicationValidationException(ApplicationErrorCodes.ValidationRepositoryHasStereotypesUsedInJoinTemplatesFromOtherRepositories)

    count = joinTemplateVersionRepository.countByRepositoryIdNotAndTargetStereotypeRepositoryId(target.id, target.id)
    if(count > 0)
      throw ApplicationValidationException(ApplicationErrorCodes.ValidationRepositoryHasStereotypesUsedInJoinTemplatesFromOtherRepositories)
  }
}