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
	private lateinit var relationTemplateService: RelationTemplateService

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
		val result = modelVerRepository.findOneByModelIdAndStatus(
			model.id,
			ModelVersionStatus.InDesign
		)

		if (result.isPresent)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationApplicationModelHasInDesignVersion)

		val modelVersion = ModelVersion(UUID.randomUUID().toString(), model)

		val max = modelVerRepository.maxDesignVersion(model.id)
		modelVersion.designVersion = max + 1
		modelVersion.status = ModelVersionStatus.InDesign

		if (model.versions == null)
			model.versions = mutableListOf()

		model.versions.add(modelVersion)
		modelService.update(model)

		return modelVersion
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
			ver.status = ModelVersionStatus.Obsoleted

		modelVerRepository.saveAll(releasedVersions)

		modelVersion.status = ModelVersionStatus.Released
		return update(modelVersion)
	}

	/**
	 * Deletes an existing model version Id
	 * @param modelVersionId Input Model Version Id
	 */
	fun delete(modelVersionId: Long) {
		val modelVersion = findById(modelVersionId)

		if (modelVersion.status != ModelVersionStatus.InDesign) {
			if (projectService.checkIfProjectsUseRepositoryVersion(modelVersion))
				throw ApplicationValidationException(ApplicationErrorCodes.ValidationModelHasVersionThatHasRelatedStoreData)
		}

		if (modelVerRepository.countAllByModelDependencies_Id(modelVersion.id) > 0)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationModelHasVersionThatHasRelatedStoreData)

		super.delete(modelVersion)
	}

	/**
	 * Updates Model Version Dependencies from other Model Versions
	 * @param modelVersion Input Model Version object
	 */
	fun updateModelVersionDependencies(modelVersion: ModelVersion) {
		val dependencies = mutableListOf<ModelVersion>()
		val listFromJoins = relationTemplateService.getRepositoryVersionDependencies(modelVersion)
		for (item in listFromJoins) {
			if (dependencies.find { it.id == item.id } == null)
				dependencies.add(item)
		}
		modelVersion.modelDependencies = dependencies
		updateVersion(modelVersion)
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
}