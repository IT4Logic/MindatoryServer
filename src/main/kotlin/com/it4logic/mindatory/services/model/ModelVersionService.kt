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
import com.it4logic.mindatory.exceptions.ApplicationObjectNotFoundException
import com.it4logic.mindatory.exceptions.ApplicationValidationException
import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.model.model.ModelVersionStatus
import com.it4logic.mindatory.model.model.*
import com.it4logic.mindatory.query.QueryService
import com.it4logic.mindatory.services.common.ApplicationBaseService
import com.it4logic.mindatory.services.project.ProjectService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional
import kotlin.reflect.KClass


@Service
@Transactional
class ModelVersionService : ApplicationBaseService<ModelVersion>() {
//	@Autowired
//	private lateinit var modelRepository: ModelRepository

	@Autowired
	private lateinit var modelVerRepository: ModelVersionRepository

	@Autowired
	private lateinit var modelService: ModelService

	@Autowired
	private lateinit var projectService: ProjectService

	@Autowired
	private lateinit var artifactTemplateService: ArtifactTemplateService
	//
//	@Autowired
//	private lateinit var attributeTemplateService: AttributeTemplateService
//
	@Autowired
	private lateinit var relationTemplateService: RelationTemplateService

	override fun repository(): ApplicationBaseRepository<ModelVersion> = modelVerRepository

	override fun type(): Class<ModelVersion> = ModelVersion::class.java

	fun findAllVersions(modelId: Long, pageable: Pageable?, sort: Sort?, filter: String?): Any {
		var newFilter = "model.id==$modelId"
		if (filter != null && !filter.isBlank())
			newFilter = "$filter;$newFilter"

		return super.findAll(pageable, sort, newFilter)
	}

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

//		model.versions.add(modelVersion)

//		val ver = modelVerRepository.save(modelVersion)
//		model().flush()
//		entityManager.refresh(ver)

		if (model.versions == null)
			model.versions = mutableListOf()

		model.versions.add(modelVersion)
		modelService.update(model)

//		mlcService().load(ver)

		return modelVersion
	}

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

	fun delete(modelVersionId: Long) {
		val modelVersion = findById(modelVersionId)

		if (modelVersion.status != ModelVersionStatus.InDesign) {
			if (checkIfVersionHasRelatedStoreObjects(modelVersion))
				throw ApplicationValidationException(ApplicationErrorCodes.ValidationModelHasVersionThatHasRelatedStoreData)
		}

		if (checkIfVersionHasRelatedOutsideTemplateObjects(modelVersion))
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationModelHasVersionThatHasRelatedStoreData)

		super.delete(modelVersion)
	}

	fun checkIfVersionHasRelatedStoreObjects(target: ModelVersion): Boolean {
		return projectService.checkIfProjectsUseRepositoryVersion(target)

//		// check if there are artifacts stores based on artifact templates from this model
//		var count = artifactStoreService.countByArtifactTemplateRepositoryVersionId(target.id)
//		if (count > 0)
//			return true
//
//		// check if there are attribute stores based on attribute templates from this model
//		count = attributeStoreService.countByAttributeTemplateRepositoryVersionId(target.id)
//		if (count > 0)
//			return true
//
//		// check if there are join stores based on join templates from this model
//		count = relationStoreService.countByRelationTemplateRepositoryVersionId(target.id)
//		if (count > 0)
//			return true

//		return false
	}

	fun checkIfVersionHasRelatedOutsideTemplateObjects(target: ModelVersion): Boolean {
		return modelVerRepository.countAllByModelDependencies_Id(target.id) > 0
		/*
		* 1- i need to know all the templates in this version
		* 2- i need to know which of them used in another version
		* 3- i need to know the version that are not related to version model
		* */
/*

		// check if there are attribute templates from this model used in artifact templates from other repositories
		var count = artifactTemplateService.countByRepositoryVersionIdNotAndAttributesRepositoryVersionId(
			target.id
		)
		if (count > 0)
			return true

		// check if there are artifact templates from this model used in joins templates from other repositories
		count =
			relationTemplateService.countByRepositoryVersionIdNotAndSourceArtifactRepositoryVersionId(
				target.id
			)
		if (count > 0)
			return true

		count =
			relationTemplateService.countByRepositoryVersionIdNotAndTargetArtifactRepositoryVersionId(
				target.id
			)
		if (count > 0)
			return true


		// check if there are stereotypes from this model used in join templates from other repositories
		count =
			relationTemplateService.countByRepositoryVersionIdNotAndSourceStereotypeRepositoryVersionId(
				target.id
			)
		if (count > 0)
			return true

		count =
			relationTemplateService.countByRepositoryVersionIdNotAndTargetStereotypeRepositoryVersionId(
				target.id
			)
		if (count > 0)
			return true
*/
//		return false
	}

//	fun addToRepositoryDependenciesList(
//		modelVersion: ModelVersion,
//		dependencyRepoVer: ModelVersion
//	) {
//
//		if (modelVersion.identifier == dependencyRepoVer.identifier)
//			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotLinkModelVersionToItself)
//
//		if (modelVersion.model.id == dependencyRepoVer.model.id)
//			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotLinkBetweenTwoVersionsInSameModel)
//
//		if (modelVersion.status == ModelVersionStatus.Released)
//			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeNotInDesignApplicationModelVersion)
//
//		val result = modelVersion.modelDependencies.find { it.identifier == dependencyRepoVer.identifier }
//		if (result != null)
//			return
//
//		modelVersion.modelDependencies.add(dependencyRepoVer)
//		updateVersion(modelVersion)
//	}
//
//	fun removeFromRepositoryDependenciesList(
//		modelVersion: ModelVersion,
//		dependencyRepoVer: ModelVersion
//	) {
//		if (modelVersion.status == ModelVersionStatus.Released)
//			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeNotInDesignApplicationModelVersion)
//
//		for ((index, repo) in modelVersion.modelDependencies.withIndex()) {
//			if (repo.identifier == dependencyRepoVer.identifier) {
//				modelVersion.modelDependencies.removeAt(index)
//				break
//			}
//		}
//
//		updateVersion(modelVersion)
//	}

	fun updateModelVersionDependencies(modelVersion: ModelVersion) {
		val dependencies = calculateModelVersionDependencies(modelVersion)
		modelVersion.modelDependencies = dependencies
		updateVersion(modelVersion)
	}

	fun calculateModelVersionDependencies(modelVersion: ModelVersion): MutableList<ModelVersion> {
		val dependencies = mutableListOf<ModelVersion>()

		val listFromArtifacts = artifactTemplateService.getRepositoryVersionDependencies(modelVersion)
		dependencies.addAll(listFromArtifacts)

		val listFromJoins = relationTemplateService.getRepositoryVersionDependencies(modelVersion)
		for (item in listFromJoins) {
			if (dependencies.find { it.id == item.id } == null)
				dependencies.add(item)
		}

		return dependencies
	}

//	fun updateDataTypePluginDependencies(target: AttributeTemplate) {
//		val dependencies =
//			attributeTemplateService.getAttributeTemplateDataTypePluginDependencies(target.modelVersion)
//		target.modelVersion.dataTypePluginDependencies = dependencies
//		updateVersion(target.modelVersion)
//	}

	fun upgradeVersionModelDependency(versionId: Long, depVersionId: Long) {
		val version = findById(versionId)
		val depVersion =
			version.modelDependencies.find { it.id == depVersionId } ?: throw ApplicationValidationException(
				ApplicationErrorCodes.ValidationModelDependencyDoesNotExistInModelVersion
			)

		if (depVersion.status == ModelVersionStatus.Released)
			throw ApplicationValidationException(
				ApplicationErrorCodes.ValidationModelDependencyIsAlreadyLatestRelease
			)

		val releasedVersion = modelVerRepository.findOneByStatusAndModelId(
			ModelVersionStatus.Released,
			depVersion.model.id
		).orElseThrow {
			ApplicationValidationException(
				ApplicationErrorCodes.ValidationModelHasNoReleasedVersion
			)
		}

		version.modelDependencies.removeIf { it.identifier == depVersion.identifier }
		version.modelDependencies.add(releasedVersion)

		super.update(version)
	}

	fun upgradeVersionModelDependencies(versionId: Long): List<ModelVersion> {
		var version = findById(versionId)
		val upgradedDependencies = mutableListOf<ModelVersion>()

		for (depVersion in version.modelDependencies) {
			if (depVersion.status == ModelVersionStatus.Released) {
				upgradedDependencies.add(depVersion)
				continue
			}

			val releasedVersion = modelVerRepository.findOneByStatusAndModelId(
				ModelVersionStatus.Released,
				depVersion.model.id
			).orElseThrow {
				ApplicationValidationException(
					ApplicationErrorCodes.ValidationModelHasNoReleasedVersion
				)
			}

			upgradedDependencies.add(releasedVersion)

		}

		version.modelDependencies = upgradedDependencies
		version = updateVersion(version)

		return version.modelDependencies
	}

	fun getReleasedVersion(modelId: Long): Optional<ModelVersion> {
		val result = modelVerRepository.findOneByStatusAndModelId(ModelVersionStatus.Released, modelId)
		if (result.isPresent) {
			loadMLC(result.get())
		}
		return result
	}

	fun findAllReleasedVersions(): List<ModelVersion> {
		val result = modelVerRepository.findAllByStatus(ModelVersionStatus.Released)
		result.forEach { loadMLC(it) }
		return result
	}

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