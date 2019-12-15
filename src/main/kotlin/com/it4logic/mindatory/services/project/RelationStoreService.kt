/*
    Copyright (c) 2019, IT4Logic.

    This file is part of Mindatory project by IT4Logic.

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

package com.it4logic.mindatory.services.project

import com.it4logic.mindatory.exceptions.ApplicationErrorCodes
import com.it4logic.mindatory.exceptions.ApplicationValidationException
import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.model.model.ModelVersionStatus
import com.it4logic.mindatory.model.project.RelationStore
import com.it4logic.mindatory.model.project.RelationStoreRepository
import com.it4logic.mindatory.services.common.ApplicationBaseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.transaction.Transactional

/**
 * Relation Store Data Service
 */
@Service
@Transactional
class RelationStoreService : ApplicationBaseService<RelationStore>() {
	@Autowired
	private lateinit var relationStoreRepository: RelationStoreRepository

	@Autowired
	private lateinit var projectService: ProjectService

	override fun repository(): ApplicationBaseRepository<RelationStore> = relationStoreRepository

	override fun type(): Class<RelationStore> = RelationStore::class.java

	override fun beforeCreate(target: RelationStore) {
		// Make sure that Model Version is not in design mode
		if (target.relationTemplate?.modelVersion?.status == ModelVersionStatus.InDesign)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeStoreObjectsRelatedToNoneReleasedModelVersion)
	}

	override fun beforeUpdate(target: RelationStore) {
		// Make sure that Model Version is not in design mode
		if (target.relationTemplate?.modelVersion?.status == ModelVersionStatus.InDesign)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeStoreObjectsRelatedToNoneReleasedModelVersion)
	}

	/**
	 * Delete all relation stores that artifact store used with
	 * @param artifactId Artifact Store Id
	 */
	fun deleteAnyRelationsUsedWithArtifact(artifactId: Long) {
		var result = relationStoreRepository.findAllBySourceArtifactId(artifactId)
		for (obj in result) {
			delete(obj)
		}

		result = relationStoreRepository.findAllByTargetArtifactId(artifactId)
		for (obj in result) {
			delete(obj)
		}
	}

	/**
	 * Counts how many relation stores that use relation template and artifact store either as source or target
	 * @param relationTemplateId Relation Template Id
	 * @param storeId Artifact Store Id
	 * @return Relation Stores count
	 */
	fun countStoresForRelationTemplateAndArtifactStore(relationTemplateId: Long, storeId: Long): Long {
		return relationStoreRepository.countAllByArtifactStoreAndRelationTemplate(storeId, relationTemplateId)
	}
}