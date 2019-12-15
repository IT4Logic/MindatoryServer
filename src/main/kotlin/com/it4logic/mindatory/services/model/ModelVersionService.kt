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
import com.it4logic.mindatory.services.project.ProjectService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*
import javax.transaction.Transactional


/**
 * Model Version Data Service
 */
@Service
@Transactional
class ModelVersionService : ApplicationBaseService<ModelVersion>() {
	@Autowired
	private lateinit var modelVerRepository: ModelVersionRepository

	@Autowired
	private lateinit var modelService: ModelService

	@Autowired
	private lateinit var projectService: ProjectService

	@Autowired
	private lateinit var artifactTemplateService: ArtifactTemplateService

	@Autowired
	private lateinit var attributeTemplateService: AttributeTemplateService

	@Autowired
	private lateinit var relationTemplateService: RelationTemplateService

	@Autowired
	private lateinit var stereotypeService: StereotypeService

	override fun repository(): ApplicationBaseRepository<ModelVersion> = modelVerRepository

	override fun type(): Class<ModelVersion> = ModelVersion::class.java

	/**
	 * Creates a new version for the input Model Id
	 * @param modelId Input Model Id
	 * @return Created Model Version object
	 */
	@Suppress("SENSELESS_COMPARISON")
	fun createVersion(
		modelId: Long
	): ModelVersion {
		val model = modelService.findById(modelId)

		// check if we have a current in-design version
		var result: ModelVersion? = model.versions.find { it.status ==  ModelVersionStatus.InDesign }

		if (result != null)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationApplicationModelHasInDesignVersion)

		result = model.versions.find { it.status ==  ModelVersionStatus.Released }

		val designVersion = modelVerRepository.maxDesignVersion(model.id) + 1

		return if (result != null)
			createDesignVersionFromReleasedVersion(result, UUID.randomUUID().toString(), designVersion, ModelVersionStatus.InDesign)
		else {
			val modelVersion = ModelVersion(
				UUID.randomUUID().toString(),
				model,
				designVersion = designVersion,
				status = ModelVersionStatus.InDesign
			)

			if (model.versions == null)
				model.versions = mutableListOf()

			model.versions.add(modelVersion)
			modelService.update(model)
			modelVersion
		}
	}

	/**
	 * Updates an existing model version
	 * @param modelVersion Model Version object
	 * @return Updated Model Version object
	 */
	fun updateVersion(
		modelVersion: ModelVersion
	): ModelVersion {
		val result = findById(modelVersion.id)

		if (modelVersion.status != ModelVersionStatus.InDesign)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeNotInDesignApplicationModelVersion)

		if (modelVersion.identifier != modelVersion.identifier)
			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.ValidationIdentifierNotMatched)

		result.metadata = modelVersion.metadata

		return update(result)
	}

	/**
	 * Release the Model Version and make it read only and ready to be used inside projects
	 * @param modelVersionId Input Model Version Id
	 * @return Released Model Version object
	 */
	fun releaseVersion(modelVersionId: Long): ModelVersion {
		val modelVersion = findById(modelVersionId)

		if (modelVersion.status != ModelVersionStatus.InDesign)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeNotInDesignApplicationModelVersion)

		val releasedVersions =
			modelVerRepository.findAllByStatusAndModelId(ModelVersionStatus.Released, modelVersion.model.id)

		for (ver in releasedVersions)
			ver.status = ModelVersionStatus.Obsolete

		modelVerRepository.saveAll(releasedVersions)

		modelVersion.status = ModelVersionStatus.Released
		return update(modelVersion)
	}

	/**
	 * Deletes an existing model version Id
	 * @param modelVersionId Input Model Version Id
	 */
	fun deleteVersion(modelVersionId: Long) {
		val modelVersion = findById(modelVersionId)

		if (modelVersion.status != ModelVersionStatus.InDesign) {
			if (projectService.checkIfProjectsUseModelVersion(modelVersion))
				throw ApplicationValidationException(ApplicationErrorCodes.ValidationModelVersionIsUsedInsideProjects)
		}

		for(relation in modelVersion.relations) {
			relationTemplateService.delete(relation)
		}

		for(stereotype in modelVersion.stereotypes) {
			stereotypeService.delete(stereotype)
		}

		for(artifact in modelVersion.artifacts) {
			artifactTemplateService.delete(artifact)
		}

		super.delete(modelVersion)
	}

	/**
	 * Get the released Model Version for a given Model Id, as there is only one released version per model
	 * @param modelId Model Id
	 * @return [Optional] isntance of Model Version object
	 */
	fun getReleasedVersion(modelId: Long): Optional<ModelVersion> {
		val result = modelVerRepository.findOneByStatusAndModelId(ModelVersionStatus.Released, modelId)
		if (result.isPresent) {
			loadMLC(result.get())
		}
		return result
	}

	/**
	 * Updates Model Version Metadata
	 * @param id Model Version Id
	 * @param metadata Metadata in string representation
	 * @return Updated Model Version object
	 */
	fun updateMetadata(id: Long, metadata: String): ModelVersion {
		val modelVersion = findById(id)

		if (modelVersion.status != ModelVersionStatus.InDesign)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeNotInDesignApplicationModelVersion)

		if (modelVersion.identifier != modelVersion.identifier)
			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.ValidationIdentifierNotMatched)

		modelVersion.metadata = metadata

		return update(modelVersion)
	}

	fun createDesignVersionFromReleasedVersion(releasedVersion: ModelVersion, newIdentifier: String, newDesignVersion: Int, newStatus: ModelVersionStatus): ModelVersion {

		var modelVersionClone = ModelVersion(
			identifier = newIdentifier,
			model = releasedVersion.model,
			designVersion = newDesignVersion,
			status = newStatus,
			metadata = releasedVersion.metadata
		)

		modelVersionClone = repository().save(modelVersionClone)

		val artifactsUUIDMap = mutableMapOf<String, String>()
		for(artifact in releasedVersion.artifacts) {
			val uuid = UUID.randomUUID().toString()
			artifactsUUIDMap[artifact.identifier] = uuid

			var artifactClone =  ArtifactTemplate( identifier =  uuid,
				name = "temp",
				metadata = artifact.metadata,
				globalIdentifier = artifact.globalIdentifier,
				modelVersion = modelVersionClone)

			for(mlc in artifact.mlcs) {
				val mlcClone = ArtifactTemplateMultipleLanguageContent()
				mlcClone.parent = artifactClone
				mlcClone.languageId = mlc.languageId
				mlcClone.fieldName = mlc.fieldName
				mlcClone.contents = mlc.contents
				artifactClone.mlcs.add(mlcClone)
			}

			artifactClone = artifactTemplateService.repository().save(artifactClone)

			val attributesUUIDMap = mutableMapOf<String, String>()
			for (attribute in artifact.attributes) {
				val attrUUID = UUID.randomUUID().toString()
				attributesUUIDMap[attribute.identifier] = attrUUID

				var attributeClone = AttributeTemplate(
					identifier = attrUUID,
					name = "temp",
					typeUUID = attribute.typeUUID,
					globalIdentifier = attribute.globalIdentifier,
					artifact = artifactClone,
					modelVersion = modelVersionClone)

				for(mlc in attribute.mlcs) {
					val mlcClone = AttributeTemplateMultipleLanguageContent()
					mlcClone.parent = attributeClone
					mlcClone.languageId = mlc.languageId
					mlcClone.fieldName = mlc.fieldName
					mlcClone.contents = mlc.contents
					attributeClone.mlcs.add(mlcClone)
				}

				for (prop in attribute.properties)
					attributeClone.properties.add(
						AttributeTemplateProperty(
							identifier = UUID.randomUUID().toString(),
							value = prop.value,
							attribute = attributeClone
						)
					)

				attributeClone = attributeTemplateService.repository().save(attributeClone)
				artifactClone.attributes.add(attributeClone)
			}

			for(attributesUUIDMapEntry in attributesUUIDMap) {
				var metadata = URLDecoder.decode(artifactClone.metadata, StandardCharsets.UTF_8.toString());
				metadata = metadata.replace(attributesUUIDMapEntry.key, attributesUUIDMapEntry.value)
				artifactClone.metadata = URLEncoder.encode(metadata, StandardCharsets.UTF_8.toString());
			}

			artifactClone = artifactTemplateService.repository().save(artifactClone)
			modelVersionClone.artifacts.add(artifactClone)
		}

		val stereotypesUUIDMap = mutableMapOf<String, String>()
		for(stereotype in releasedVersion.stereotypes) {
			val uuid = UUID.randomUUID().toString()
			stereotypesUUIDMap[stereotype.identifier] = uuid

			var stereotypeClone = Stereotype(
				identifier = uuid,
				name = "temp",
				modelVersion = modelVersionClone
			)

			for(mlc in stereotype.mlcs) {
				val mlcClone = StereotypeMultipleLanguageContent()
				mlcClone.parent = stereotypeClone
				mlcClone.languageId = mlc.languageId
				mlcClone.fieldName = mlc.fieldName
				mlcClone.contents = mlc.contents
				stereotypeClone.mlcs.add(mlcClone)
			}

			stereotypeClone = stereotypeService.repository().save(stereotypeClone)
			modelVersionClone.stereotypes.add(stereotypeClone)
		}

		for(relation in releasedVersion.relations) {
			var relationClone = RelationTemplate(
				identifier = UUID.randomUUID().toString(),
				globalIdentifier = relation.globalIdentifier,
				modelVersion = modelVersionClone,
				sourceArtifact = relation.sourceArtifact,
				sourceStereotype = relation.sourceStereotype,
				targetArtifact = relation.targetArtifact,
				targetStereotype = relation.targetStereotype
			)

			// find source artifact clone
			var identifier = artifactsUUIDMap[relation.sourceArtifact.identifier]
			val sourceArtifact = modelVersionClone.artifacts.find { it.identifier == identifier }
			relationClone.sourceArtifact = sourceArtifact!!

			// find source stereotype clone
			identifier = stereotypesUUIDMap[relation.sourceStereotype.identifier]
			val sourceStereotype = modelVersionClone.stereotypes.find { it.identifier == identifier }
			relationClone.sourceStereotype = sourceStereotype!!

			// find target artifact clone
			identifier = artifactsUUIDMap[relation.targetArtifact.identifier]
			val targetArtifact = modelVersionClone.artifacts.find { it.identifier == identifier }
			relationClone.targetArtifact = targetArtifact!!

			// find target stereotype clone
			identifier = stereotypesUUIDMap[relation.targetStereotype.identifier]
			val targetStereotype = modelVersionClone.stereotypes.find { it.identifier == identifier }
			relationClone.targetStereotype = targetStereotype!!

			for(mlc in relation.mlcs) {
				val mlcClone = RelationTemplateMultipleLanguageContent()
				mlcClone.parent = relationClone
				mlcClone.languageId = mlc.languageId
				mlcClone.fieldName = mlc.fieldName
				mlcClone.contents = mlc.contents
				relationClone.mlcs.add(mlcClone)
			}

			relationClone = relationTemplateService.repository().save(relationClone)
			modelVersionClone.relations.add(relationClone)
		}

		refresh(modelVersionClone)

		return modelVersionClone
	}
}