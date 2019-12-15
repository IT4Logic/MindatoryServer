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
import com.it4logic.mindatory.model.model.*
import com.it4logic.mindatory.model.project.ArtifactStore
import com.it4logic.mindatory.model.project.ArtifactStoreRepository
import com.it4logic.mindatory.services.common.ApplicationBaseService
import com.it4logic.mindatory.services.model.ArtifactTemplateService
import com.it4logic.mindatory.services.model.AttributeTemplateService
import com.it4logic.mindatory.services.model.RelationTemplateService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.transaction.Transactional

/**
 * Artifact Store Data Service
 */
@Service
@Transactional
class ArtifactStoreService : ApplicationBaseService<ArtifactStore>() {
	@Autowired
	private lateinit var artifactStoreRepository: ArtifactStoreRepository

	@Autowired
	private lateinit var attributeStoreService: AttributeStoreService

	@Autowired
	private lateinit var relationStoreService: RelationStoreService

	@Autowired
	private lateinit var relationTemplateService: RelationTemplateService

	@Autowired
	private lateinit var artifactTemplateService: ArtifactTemplateService

	@Autowired
	private lateinit var attributeTemplateService: AttributeTemplateService

	override fun repository(): ApplicationBaseRepository<ArtifactStore> = artifactStoreRepository

	override fun type(): Class<ArtifactStore> = ArtifactStore::class.java

	override fun beforeCreate(target: ArtifactStore) {
		// Make sure that the model version is not in design mode
		if (target.artifactTemplate?.modelVersion?.status == ModelVersionStatus.InDesign)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeStoreObjectsRelatedToNoneReleasedModelVersion)
	}

	override fun afterCreate(target: ArtifactStore, ref: ArtifactStore) {
		updateAttributeStores(target, ref)
	}

	override fun beforeUpdate(target: ArtifactStore) {
		// Make sure that the model version is not in design mode
		if (target.artifactTemplate?.modelVersion?.status == ModelVersionStatus.InDesign)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeStoreObjectsRelatedToNoneReleasedModelVersion)
	}

	override fun afterUpdate(target: ArtifactStore, ref: ArtifactStore) {
		updateAttributeStores(target, ref)
	}

	override fun beforeDelete(target: ArtifactStore) {
		// Make sure that the model version is not in design mode
		if (target.artifactTemplate?.modelVersion?.status == ModelVersionStatus.InDesign)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeStoreObjectsRelatedToNoneReleasedModelVersion)

		for (attribute in target.attributes) {
			attributeStoreService.delete(attribute)
		}

		relationStoreService.deleteAnyRelationsUsedWithArtifact(target.id)
	}

	fun updateAttributeStores(target: ArtifactStore, ref: ArtifactStore) {
		// Updating and validating attribute stores
		for (aStore in ref.attributes) {
			aStore.attributeTemplate = attributeTemplateService.findById(aStore.attributeTemplate.id)
			aStore.artifact = target
			aStore.project = target.project!!
			attributeStoreService.validateStore(aStore)
			if (aStore.id > -1)
				attributeStoreService.update(aStore)
			else
				attributeStoreService.create(aStore)
		}
	}

	/**
	 * Retrieves Artifact Templates that have been used inside a project
	 * @param projectId Project Id
	 * @return Used Artifact Template list
	 */
	fun getUsedArtifactTemplatesForProject(projectId: Long): List<ArtifactTemplate> {
		val result = artifactStoreRepository.findAllUsedArtifactTemplates(projectId)
		for (artifact in result) {
			artifactTemplateService.loadMLC(artifact)
		}
		return result
	}

	/**
	 * Generate Artifact Store Traceability Matrix
	 * @param store Artifact Store object
	 * @return Traceability Matrix
	 */
	fun generateStoreTraceabilityMatrix(store: ArtifactStore): List<Any> {
		val artifactTemplateId : Long = store.artifactTemplate!!.id
		val output = mutableMapOf<RelationTemplate, Long>()
		val relationTemplates = relationTemplateService.findAllRelationsRelatedToArtifact(artifactTemplateId)
		for (relation in relationTemplates) {
			output[relation] = relationStoreService.countStoresForRelationTemplateAndArtifactStore(relation.id, store.id)
		}

		return output.map {
			val refArtifact: ArtifactTemplate
			val relArtifact: ArtifactTemplate
			val refStereotype: Stereotype
			val relStereotype: Stereotype
			val count = it.value
			if(it.key.sourceArtifact.id == artifactTemplateId) {
				refArtifact = it.key.sourceArtifact
				relArtifact = it.key.targetArtifact
				refStereotype = it.key.sourceStereotype
				relStereotype = it.key.targetStereotype
			} else {
				relArtifact = it.key.sourceArtifact
				refArtifact = it.key.targetArtifact
				relStereotype = it.key.sourceStereotype
				refStereotype = it.key.targetStereotype
			}

			object {
				val referenceId: Long = refArtifact.id
				val referenceName: String = refArtifact.name
				val referenceRelation: String = refStereotype.name
				val relatedId: Long = relArtifact.id
				val relatedName: String = relArtifact.name
				val relatedRelation: String = relStereotype.name
				val count: Long = count
			}
		}
	}
}