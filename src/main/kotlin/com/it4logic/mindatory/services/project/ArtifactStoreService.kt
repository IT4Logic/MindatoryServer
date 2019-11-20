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
import com.it4logic.mindatory.model.project.Project
import com.it4logic.mindatory.services.common.ApplicationBaseService
import com.it4logic.mindatory.services.model.ArtifactTemplateService
import com.it4logic.mindatory.services.model.AttributeTemplateService
import com.it4logic.mindatory.services.model.RelationTemplateService
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
		if (target.artifactTemplate.modelVersion.status != ModelVersionStatus.Released)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeStoreObjectsRelatedToNoneReleasedModelVersion)

//		updateStores(target)

//    if(target.artifact.id != target.artifactTemplateVersion.artifact.id)
//      throw ApplicationValidationException(ApplicationErrorCodes.ValidationStoreObjectVersionAndTemplateMismatch)

//    if(target.artifact.status != ModelVersionStatus.Released)
//      throw ApplicationValidationException(ApplicationErrorCodes.ValidationStoreObjectCanOnlyBeAssociatedWithReleasedVersion)

//		for (attribute in target.attributes) {
//			attributeStoreService.validate(attribute)
//		}
	}

	override fun beforeUpdate(target: ArtifactStore) {
		if (target.artifactTemplate.modelVersion.status != ModelVersionStatus.Released)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeStoreObjectsRelatedToNoneReleasedModelVersion)

//    if(target.artifact.id != target.artifactTemplateVersion.artifact.id)
//      throw ApplicationValidationException(ApplicationErrorCodes.ValidationStoreObjectVersionAndTemplateMismatch)

//    if(target.artifact.status != ModelVersionStatus.Released)
//      throw ApplicationValidationException(ApplicationErrorCodes.ValidationStoreObjectCanOnlyBeAssociatedWithReleasedVersion)

//		for (attribute in target.attributes) {
//			attributeStoreService.validateStore(attribute)
//		}
	}

	override fun beforeDelete(target: ArtifactStore) {
		if (target.artifactTemplate.modelVersion.status != ModelVersionStatus.Released)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeStoreObjectsRelatedToNoneReleasedModelVersion)

		relationStoreService.deleteAnyRelatedJoinsUsedWithArtifact(target.id)
//
//		var result = relationStoreService.findAllBySourceArtifactId(target.id)
//		for (obj in result) {
//			relationStoreService.delete(obj)
//		}
//
//		result = relationStoreService.findAllByTargetArtifactId(target.id)
//		for (obj in result) {
//			relationStoreService.delete(obj)
//		}

//		if (relationStoreRepository.countBySourceArtifactId(target.id) > 0)
//			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotDeleteArtifactStoreObjectThatUsedInRelationStoreObjects)
//
//		if (relationStoreRepository.countByTargetArtifactId(target.id) > 0)
//			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotDeleteArtifactStoreObjectThatUsedInRelationStoreObjects)
	}

	override fun beforeFlush(
		savedTarget: ArtifactStore,
		refTarget: ArtifactStore,
		persistantFunctionType: PersistantFunctionType
	) {
		if (persistantFunctionType == PersistantFunctionType.DELETE)
			return
		
		for (aStore in refTarget.attributes) {
			aStore.attributeTemplate = attributeTemplateService.findById(aStore.attributeTemplate.id)
			aStore.artifact = savedTarget
			aStore.project = savedTarget.project
			attributeStoreService.validateStore(aStore)
			if (aStore.id > -1)
				attributeStoreService.update(aStore)
			else
				attributeStoreService.create(aStore)
		}
	}

	fun getRepositoryVersionDependencies(project: Project): List<ModelVersion> {
		val dependencies = mutableListOf<ModelVersion>()

		val artifactStores = artifactStoreRepository.findAllByProjectId(project.id)

		for (artifactStore in artifactStores) {
			if (dependencies.find { it.id == artifactStore.artifactTemplate.modelVersion.id } == null)
				dependencies.add(artifactStore.artifactTemplate.modelVersion)

			for (attribute in artifactStore.attributes) {
				if (dependencies.find { it.id == attribute.attributeTemplate.modelVersion.id } == null)
					dependencies.add(attribute.attributeTemplate.modelVersion)

			}
		}

		return dependencies
	}

	fun getUsedArtifactTemplatesForProject(projectId: Long): List<ArtifactTemplate> {
		val result = artifactStoreRepository.findAllUsedArtifactTemplates(projectId)
		for (artifact in result) {
			artifactTemplateService.loadMLC(artifact)
		}
		return result
	}

//	fun countByArtifactTemplateRepositoryVersionId(id: Long): Long {
//		return artifactStoreRepository.countByArtifactTemplateRepositoryVersionId(id)
//	}

	fun generateStoreTraceability(store: ArtifactStore): List<Any> {
		val artifactTemplateId : Long = store.artifactTemplate.id
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