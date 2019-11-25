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

package com.it4logic.mindatory.services.model

import com.it4logic.mindatory.exceptions.ApplicationDataIntegrityViolationException
import com.it4logic.mindatory.exceptions.ApplicationErrorCodes
import com.it4logic.mindatory.exceptions.ApplicationValidationException
import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.model.model.ModelVersionStatus
import com.it4logic.mindatory.model.model.*
import com.it4logic.mindatory.services.common.ApplicationBaseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional
import kotlin.reflect.KClass

/**
 * Relation Template Data Service
 */
@Service
@Transactional
class RelationTemplateService : ApplicationBaseService<RelationTemplate>() {
	@Autowired
	private lateinit var relationTemplateRepository: RelationTemplateRepository

	@Autowired
	private lateinit var modelService: ModelService

	@Autowired
	private lateinit var modelVersionService: ModelVersionService

	@Autowired
	private lateinit var mlcRepository: RelationTemplateMLCRepository

	override fun repository(): ApplicationBaseRepository<RelationTemplate> = relationTemplateRepository

	override fun type(): Class<RelationTemplate> = RelationTemplate::class.java

	override fun multipleLanguageContentRepository(): RelationTemplateMLCRepository = mlcRepository

	override fun multipleLanguageContentType(): KClass<*> = RelationTemplateMultipleLanguageContent::class

	override fun beforeCreate(target: RelationTemplate) {
		// Check if the if Model Version is released or not, as released version cannot be modified
		if (target.modelVersion.status != ModelVersionStatus.InDesign)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeObjectsWithinNoneInDesignModelVersion)

		target.identifier = UUID.randomUUID().toString()
	}

	override fun beforeUpdate(target: RelationTemplate) {
		// Check if the if Model Version is released or not, as released version cannot be modified
		if (target.modelVersion.status != ModelVersionStatus.InDesign)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeObjectsWithinNoneInDesignModelVersion)

		val objTmp = findById(target.id)
		if (objTmp.identifier != target.identifier)
			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.ValidationIdentifierNotMatched)
	}

	override fun beforeDelete(target: RelationTemplate) {
		// Check if the if Model Version is released or not, as released version cannot be modified
		if (target.modelVersion.status != ModelVersionStatus.InDesign)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeObjectsWithinNoneInDesignModelVersion)
	}

	override fun afterCreate(target: RelationTemplate) {
		modelVersionService.updateModelVersionDependencies(target.modelVersion)
	}

	override fun afterUpdate(target: RelationTemplate) {
		modelVersionService.updateModelVersionDependencies(target.modelVersion)
	}

	override fun afterDelete(target: RelationTemplate) {
		modelVersionService.updateModelVersionDependencies(target.modelVersion)
	}

	/**
	 * Retrieves all the relation templates that the input artifact template Id has been used as a source or target
	 * @param artifactTemplateId Input Artifact Template Id
	 * @return Relation Templates list
	 */
	fun findAllRelationsRelatedToArtifact(artifactTemplateId: Long) : List<RelationTemplate> {
		val output = mutableListOf<RelationTemplate>()
		output.addAll(relationTemplateRepository.findAllBySourceArtifactId(artifactTemplateId))
		output.addAll(relationTemplateRepository.findAllByTargetArtifactId(artifactTemplateId))
		output.forEach {
			loadMLC(it)
		}
		return output
	}

	/**
	 * Deletes any relation templates that has the input artifact template Id as a source or target
	 * @param artifactId Input Artifact Template Id
	 */
	fun deleteAnyRelatedRelationsUsedWithArtifact(artifactId: Long) {
		var result = relationTemplateRepository.findAllBySourceArtifactId(artifactId)
		for (obj in result) {
			delete(obj)
		}

		result = relationTemplateRepository.findAllByTargetArtifactId(artifactId)
		for (obj in result) {
			delete(obj)
		}
	}

	/**
	 * Checks if the input Stereotype object is used with any relation templates
	 * @param target Input Stereotype object
	 * @return True if the Stereotype object is used, False otherwise
	 */
	fun isStereotypeUsedInRelationTemplates(target: Stereotype): Boolean {
		return relationTemplateRepository.countBySourceStereotypeId(target.id) > 0 ||
				relationTemplateRepository.countByTargetStereotypeId(target.id) > 0
	}

	/**
	 * Retrieves Model Versions list that have been used in Relation Templates through Artifact Templates as source
	 * or target.
	 * @param modelVersion Input model Version object
	 * @return Model Versions list
	 */
	fun getRepositoryVersionDependencies(modelVersion: ModelVersion): List<ModelVersion> {
		val result = mutableListOf<ModelVersion>()

		// Get the artifacts that not belong to relation model version and used with relations
		// as a source
		var artifactTemplates =
			relationTemplateRepository.findAllByModelVersionIdAndSourceArtifactModelVersionIdNot(
				modelVersion.id,
				modelVersion.id
			)

		for (artifactTemplate in artifactTemplates) {
			if (artifactTemplate.modelVersion.model.id != modelVersion.model.id) {
				if (result.find { it.id == artifactTemplate.modelVersion.id } == null)
					result.add(artifactTemplate.modelVersion)
			}
		}

		// Get the artifacts that not belong to relation model version and used with relations
		// as a target
		artifactTemplates =
			relationTemplateRepository.findAllByModelVersionIdAndTargetArtifactModelVersionIdNot(
				modelVersion.id,
				modelVersion.id
			)

		for (artifactTemplate in artifactTemplates) {
			if (artifactTemplate.modelVersion.model.id != modelVersion.model.id) {
				if (result.find { it.id == artifactTemplate.modelVersion.id } == null)
					result.add(artifactTemplate.modelVersion)
			}
		}

		// Get the stereotypes that not belong to relation model version and used with relations
		// as a source
		var stereotypes =
			relationTemplateRepository.findAllByModelVersionIdAndSourceStereotypeModelVersionIdNot(
				modelVersion.id,
				modelVersion.id
			)

		for (stereotype in stereotypes) {
			if (stereotype.modelVersion.model.id != modelVersion.model.id) {
				if (result.find { it.id == stereotype.modelVersion.id } == null)
					result.add(stereotype.modelVersion)
			}
		}

		// Get the stereotypes that not belong to relation model version and used with relations
		// as a target
		stereotypes =
			relationTemplateRepository.findAllByModelVersionIdAndTargetStereotypeModelVersionIdNot(
				modelVersion.id,
				modelVersion.id
			)

		for (stereotype in stereotypes) {
			if (stereotype.modelVersion.model.id != modelVersion.model.id) {
				if (result.find { it.id == stereotype.modelVersion.id } == null)
					result.add(stereotype.modelVersion)
			}
		}

		return result
	}



	// ================================================================================================================
/*

	fun migrateStores(sourceVersion: RelationTemplate, targetVersion: RelationTemplate): MutableList<RelationStore> {
		for (sourceArtifact in sourceVersion.sourceArtifacts) {
			if (!isArtifactExists(sourceArtifact, targetVersion.sourceArtifacts)) {
				val count = relationStoreRepository.countByRelationTemplateIdAndSourceArtifacts_Id(
					sourceVersion.id,
					sourceArtifact.id
				)
				if (count > 0)
					throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotMigrateRelationStoresDueToRemovedSourceArtifactWithRelativeData)
			}
		}

		for (targetArtifact in sourceVersion.targetArtifacts) {
			if (!isArtifactExists(targetArtifact, targetVersion.targetArtifacts)) {
				val count = relationStoreRepository.countByRelationTemplateIdAndTargetArtifacts_Id(
					sourceVersion.id,
					targetArtifact.id
				)
				if (count > 0)
					throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotMigrateRelationStoresDueToRemovedTargetArtifactWithRelativeData)
			}
		}

		val targetStores = mutableListOf<RelationStore>()
		val sourceStores = relationStoreRepository.findAllByRelationTemplateId(sourceVersion.id)
		for (sourceStore in sourceStores) {
			val targetStore = RelationStore(
				sourceStore.sourceArtifacts,
				sourceStore.targetArtifacts,
				sourceStore.relationTemplate,
				targetVersion,
				project = sourceStore.project
			)
			targetStores.add(targetStore)
			sourceStore.storeStatus = StoreObjectStatus.Migrated
		}

		relationStoreRepository.saveAll(sourceStores)
		relationStoreRepository.saveAll(targetStores)

		return targetStores
	}

	fun migrateStores(relationTemplateId: Long, sourceVersionId: Long, targetVersionId: Long): MutableList<RelationStore> {
		val targetVersion =
			relationTemplateRepository.findOneByIdAndRelationTemplateId(targetVersionId, relationTemplateId).orElseThrow {
				ApplicationObjectNotFoundException(
					targetVersionId,
					RelationTemplate::class.java.simpleName.toLowerCase()
				)
			}

		if (targetVersion.status != ModelVersionStatus.Released)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotMigrateStoreObjectsToNoneReleasedVersion)

		val sourceVersion =
			relationTemplateRepository.findOneByIdAndRelationTemplateId(sourceVersionId, relationTemplateId).orElseThrow {
				ApplicationObjectNotFoundException(
					sourceVersionId,
					RelationTemplate::class.java.simpleName.toLowerCase()
				)
			}

		return migrateStores(sourceVersion, targetVersion)
	}
 */
}