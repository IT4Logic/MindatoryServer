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

package com.it4logic.mindatory.services.store

import com.it4logic.mindatory.exceptions.ApplicationErrorCodes
import com.it4logic.mindatory.exceptions.ApplicationValidationException
import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.model.common.DesignStatus
import com.it4logic.mindatory.model.store.ArtifactStore
import com.it4logic.mindatory.model.store.ArtifactStoreRepository
import com.it4logic.mindatory.model.store.AttributeStore
import com.it4logic.mindatory.model.store.JoinStoreRepository
import com.it4logic.mindatory.services.common.ApplicationBaseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.transaction.Transactional


@Service
@Transactional
class ArtifactStoreService : ApplicationBaseService<ArtifactStore>() {
  @Autowired
  private lateinit var artifactStoreRepository: ArtifactStoreRepository

  @Autowired
  private lateinit var attributeStoreService: AttributeStoreService

  @Autowired
  private lateinit var joinStoreRepository: JoinStoreRepository

  override fun repository(): ApplicationBaseRepository<ArtifactStore> = artifactStoreRepository

  override fun type(): Class<ArtifactStore> = ArtifactStore::class.java

  override fun beforeCreate(target: ArtifactStore) {
//    if(target.artifactTemplate.id != target.artifactTemplateVersion.artifactTemplate.id)
//      throw ApplicationValidationException(ApplicationErrorCodes.ValidationStoreObjectVersionAndTemplateMismatch)

    if(target.artifactTemplateVersion.designStatus != DesignStatus.Released)
      throw ApplicationValidationException(ApplicationErrorCodes.ValidationStoreObjectCanOnlyBeAssociatedWithReleasedVersion)

    for(attribute in target.attributeStores) {
      attributeStoreService.validate(attribute)
    }
  }

  override fun beforeUpdate(target: ArtifactStore) {
//    if(target.artifactTemplate.id != target.artifactTemplateVersion.artifactTemplate.id)
//      throw ApplicationValidationException(ApplicationErrorCodes.ValidationStoreObjectVersionAndTemplateMismatch)

    if(target.artifactTemplateVersion.designStatus != DesignStatus.Released)
      throw ApplicationValidationException(ApplicationErrorCodes.ValidationStoreObjectCanOnlyBeAssociatedWithReleasedVersion)

    for(attribute in target.attributeStores) {
      attributeStoreService.validateStore(attribute)
    }
  }

  override fun beforeDelete(target: ArtifactStore) {
    if(joinStoreRepository.countBySourceArtifacts_Id(target.id) > 0)
      throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotDeleteArtifactStoreObjectThatUsedInJoinStoreObjects)

    if(joinStoreRepository.countByTargetArtifacts_Id(target.id) > 0)
      throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotDeleteArtifactStoreObjectThatUsedInJoinStoreObjects)

    super.beforeDelete(target)
  }

  override fun create(target: ArtifactStore): ArtifactStore {
    updateStores(target)
    return super.create(target)
  }

  override fun update(target: ArtifactStore): ArtifactStore {
    updateStores(target)
    return super.update(target)
  }

  private fun updateStores(target: ArtifactStore) {
    target.artifactTemplate = target.artifactTemplateVersion.artifactTemplate
    for(aStore in target.attributeStores) {
      aStore.attributeTemplate = aStore.attributeTemplateVersion.attributeTemplate
      aStore.solution = target.solution
    }
  }
}