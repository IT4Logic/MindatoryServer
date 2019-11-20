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
class ModelService : ApplicationBaseService<Model>() {
	@Autowired
	private lateinit var modelRepository: ModelRepository

	@Autowired
	private lateinit var modelVerRepository: ModelVersionRepository

	@Autowired
	private lateinit var projectService: ProjectService

	@Autowired
	private lateinit var artifactTemplateService: ArtifactTemplateService

	@Autowired
	private lateinit var attributeTemplateService: AttributeTemplateService

	@Autowired
	private lateinit var relationTemplateService: RelationTemplateService

//	@Autowired
//	private lateinit var artifactStoreService: ArtifactStoreService
//
//	@Autowired
//	private lateinit var attributeStoreService: AttributeStoreService
//
//	@Autowired
//	private lateinit var relationStoreService: RelationStoreService
//
//	@Autowired
//	private lateinit var artifactTemplateService: ArtifactTemplateService
//
//	@Autowired
//	private lateinit var relationTemplateService: RelationTemplateService

	@Autowired
	private lateinit var mlcRepository: ModelMLCRepository

	override fun repository(): ApplicationBaseRepository<Model> = modelRepository

	override fun type(): Class<Model> = Model::class.java

	override fun multipleLanguageContentRepository(): ModelMLCRepository = mlcRepository

	override fun multipleLanguageContentType(): KClass<*> = ModelMultipleLanguageContent::class

	@Suppress("UNCHECKED_CAST")
	override fun beforeCreate(target: Model) {
		target.identifier = UUID.randomUUID().toString()
		val result = findAll(null, null, null) as List<Model>
		val obj = result.find { it.name == target.name }
		if (obj != null)
			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.DuplicateModelName)
	}

	@Suppress("UNCHECKED_CAST")
	override fun beforeUpdate(target: Model) {
		val objTmp = findById(target.id)
		if (objTmp.identifier != target.identifier)
			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.ValidationIdentifierNotMatched)

		val result = findAll(null, null, null) as List<Model>
		val obj = result.find { it.name == target.name }
		if (obj != null && obj.id != target.id)
			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.DuplicateModelName)
	}

	override fun beforeDelete(target: Model) {
		for (ver in target.versions) {
			if (ver.status == ModelVersionStatus.InDesign)
				continue
			if (checkIfVersionHasRelatedStoreObjects(ver))
				throw ApplicationValidationException(ApplicationErrorCodes.ValidationModelHasVersionThatHasRelatedStoreData)
		}
	}

	// ================================================================================================================
//
//	fun findAllVersions(modelId: Long, pageable: Pageable?, sort: Sort?, filter: String?): Any {
//		var newFilter = "model.id==$modelId"
//		if (filter != null && !filter.isBlank())
//			newFilter = "$filter;$newFilter"
//
//		val specs = QueryService.parseFilter<ModelVersion>(newFilter, null)
//		val result = if (specs != null && pageable != null)
//			modelVerRepository.findAll(specs, pageable)
//		else if (specs != null && sort != null)
//			modelVerRepository.findAll(specs, sort)
//		else if (specs != null)
//			modelVerRepository.findAll(specs)
//		else if (pageable != null)
//			modelVerRepository.findAll(pageable)
//		else if (sort != null)
//			modelVerRepository.findAll(sort)
//		else
//			modelVerRepository.findAll()
//
//		result.forEach {
//			loadMLC(it)
//		}
//
//		return result
//	}
//
//	fun findVersion(modelId: Long, versionId: Long): ModelVersion {
//		val result = modelVerRepository.findOneByIdAndModelId(versionId, modelId).orElseThrow {
//			throw ApplicationObjectNotFoundException(
//				versionId,
//				ModelVersion::class.java.simpleName.toLowerCase()
//			)
//		}
//		mlcService().load(result)
//		return result
//	}
//
//	fun findVersion(versionId: Long): ModelVersion {
//		val result = modelVerRepository.findById(versionId).orElseThrow {
//			throw ApplicationObjectNotFoundException(
//				versionId,
//				ModelVersion::class.java.simpleName.toLowerCase()
//			)
//		}
//		mlcService().load(result)
//		return result
//	}
//
//	fun createVersion(
//		modelId: Long
//	): ModelVersion {
//		return createVersion(findById(modelId))
//	}
//
//	@Suppress("SENSELESS_COMPARISON")
//	fun createVersion(
//		model: Model
//	): ModelVersion {
//		// check if we have a current in-design version
//		val result = modelVerRepository.findOneByModelIdAndStatus(
//			model.id,
//			ModelVersionStatus.InDesign
//		)
//
//		if (result.isPresent)
//			throw ApplicationValidationException(ApplicationErrorCodes.ValidationApplicationModelHasInDesignVersion)
//
//		val repoVersion = ModelVersion(UUID.randomUUID().toString(), model)
//
//		val max = modelVerRepository.maxDesignVersion(model.id)
//		repoVersion.designVersion = max + 1
//		repoVersion.status = ModelVersionStatus.InDesign
//
////		model.versions.add(repoVersion)
//
////		val ver = modelVerRepository.save(repoVersion)
////		model().flush()
////		entityManager.refresh(ver)
//
//		if (model.versions == null)
//			model.versions = mutableListOf()
//
//		model.versions.add(repoVersion)
//		update(model)
//
////		mlcService().load(ver)
//
//		return repoVersion
//	}
//
//	fun updateVersion(
//		modelId: Long,
//		repoVersion: ModelVersion
//	): ModelVersion {
//		val result = modelVerRepository.findOneByIdAndModelId(
//			repoVersion.id,
//			modelId
//		).orElseThrow {
//			ApplicationObjectNotFoundException(
//				repoVersion.id,
//				ModelVersion::class.java.simpleName.toLowerCase()
//			)
//		}
//
//		if (result.model.id != modelId)
//			throw ApplicationValidationException(ApplicationErrorCodes.ValidationModelForVersionMismatch)
//
//		result.metadata = repoVersion.metadata
//
//		return updateVersion(result)
//	}
//
//	fun updateVersion(
//		repoVersion: ModelVersion
//	): ModelVersion {
//		if (repoVersion.status != ModelVersionStatus.InDesign)
//			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeNotInDesignApplicationModelVersion)
//
//		if (repoVersion.identifier != repoVersion.identifier)
//			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.ValidationIdentifierNotMatched)
//
//		val ver = modelVerRepository.save(repoVersion)
//		repository().flush()
//		entityManager.refresh(ver)
//
//		mlcService().load(ver)
//
//		return ver
//	}
//
//	fun releaseVersion(modelId: Long, repoVersionId: Long): ModelVersion {
//		val repoVersion = modelVerRepository.findOneByIdAndModelId(
//			repoVersionId,
//			modelId
//		).orElseThrow {
//			ApplicationObjectNotFoundException(
//				repoVersionId,
//				ModelVersion::class.java.simpleName.toLowerCase()
//			)
//		}
//
//		if (repoVersion.status != ModelVersionStatus.InDesign)
//			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeNotInDesignApplicationModelVersion)
//
//		val releasedVersions =
//			modelVerRepository.findAllByStatusAndModelId(ModelVersionStatus.Released, modelId)
//
//		for (ver in releasedVersions)
//			ver.status = ModelVersionStatus.Obsoleted
//
//		modelVerRepository.saveAll(releasedVersions)
//
//		repoVersion.status = ModelVersionStatus.Released
//		val ver = modelVerRepository.save(repoVersion)
//		repository().flush()
//		entityManager.refresh(ver)
//
//		mlcService().load(ver)
//
//		return ver
//	}
//
//	fun deleteVersion(modelId: Long, repoVersionId: Long) {
//		val repoVersion = modelVerRepository.findOneByIdAndModelId(
//			repoVersionId,
//			modelId
//		).orElseThrow {
//			ApplicationObjectNotFoundException(
//				repoVersionId,
//				ModelVersion::class.java.simpleName.toLowerCase()
//			)
//		}
//
//		if (repoVersion.status != ModelVersionStatus.InDesign) {
//			if (checkIfVersionHasRelatedStoreObjects(repoVersion))
//				throw ApplicationValidationException(ApplicationErrorCodes.ValidationModelHasVersionThatHasRelatedStoreData)
//		}
//
//		if (checkIfVersionHasRelatedOutsideTemplateObjects(repoVersion))
//			throw ApplicationValidationException(ApplicationErrorCodes.ValidationModelHasVersionThatHasRelatedStoreData)
//
//		modelVerRepository.delete(repoVersion)
//	}
//
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
//
//	fun checkIfVersionHasRelatedOutsideTemplateObjects(target: ModelVersion): Boolean {
//		return modelVerRepository.countAllByModelDependencies_Id(target.id) > 0
//		/*
//		* 1- i need to know all the templates in this version
//		* 2- i need to know which of them used in another version
//		* 3- i need to know the version that are not related to version model
//		* */
///*
//
//		// check if there are attribute templates from this model used in artifact templates from other repositories
//		var count = artifactTemplateService.countByRepositoryVersionIdNotAndAttributesRepositoryVersionId(
//			target.id
//		)
//		if (count > 0)
//			return true
//
//		// check if there are artifact templates from this model used in joins templates from other repositories
//		count =
//			relationTemplateService.countByRepositoryVersionIdNotAndSourceArtifactRepositoryVersionId(
//				target.id
//			)
//		if (count > 0)
//			return true
//
//		count =
//			relationTemplateService.countByRepositoryVersionIdNotAndTargetArtifactRepositoryVersionId(
//				target.id
//			)
//		if (count > 0)
//			return true
//
//
//		// check if there are stereotypes from this model used in join templates from other repositories
//		count =
//			relationTemplateService.countByRepositoryVersionIdNotAndSourceStereotypeRepositoryVersionId(
//				target.id
//			)
//		if (count > 0)
//			return true
//
//		count =
//			relationTemplateService.countByRepositoryVersionIdNotAndTargetStereotypeRepositoryVersionId(
//				target.id
//			)
//		if (count > 0)
//			return true
//*/
////		return false
//	}
//
////	fun addToRepositoryDependenciesList(
////		repoVersion: ModelVersion,
////		dependencyRepoVer: ModelVersion
////	) {
////
////		if (repoVersion.identifier == dependencyRepoVer.identifier)
////			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotLinkModelVersionToItself)
////
////		if (repoVersion.model.id == dependencyRepoVer.model.id)
////			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotLinkBetweenTwoVersionsInSameModel)
////
////		if (repoVersion.status == ModelVersionStatus.Released)
////			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeNotInDesignApplicationModelVersion)
////
////		val result = repoVersion.modelDependencies.find { it.identifier == dependencyRepoVer.identifier }
////		if (result != null)
////			return
////
////		repoVersion.modelDependencies.add(dependencyRepoVer)
////		updateVersion(repoVersion)
////	}
////
////	fun removeFromRepositoryDependenciesList(
////		repoVersion: ModelVersion,
////		dependencyRepoVer: ModelVersion
////	) {
////		if (repoVersion.status == ModelVersionStatus.Released)
////			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeNotInDesignApplicationModelVersion)
////
////		for ((index, repo) in repoVersion.modelDependencies.withIndex()) {
////			if (repo.identifier == dependencyRepoVer.identifier) {
////				repoVersion.modelDependencies.removeAt(index)
////				break
////			}
////		}
////
////		updateVersion(repoVersion)
////	}
//
//	fun updateModelVersionDependencies(modelVersion: ModelVersion) {
//		val dependencies = calculateModelVersionDependencies(modelVersion)
//		modelVersion.modelDependencies = dependencies
//		updateVersion(modelVersion)
//	}
//
//	fun calculateModelVersionDependencies(modelVersion: ModelVersion): MutableList<ModelVersion> {
//		val dependencies = mutableListOf<ModelVersion>()
//
//		val listFromArtifacts = artifactTemplateService.getRepositoryVersionDependencies(modelVersion)
//		dependencies.addAll(listFromArtifacts)
//
//		val listFromJoins = relationTemplateService.getRepositoryVersionDependencies(modelVersion)
//		for (item in listFromJoins) {
//			if (dependencies.find { it.id == item.id } == null)
//				dependencies.add(item)
//		}
//
//		return dependencies
//	}
//
////	fun updateDataTypePluginDependencies(target: AttributeTemplate) {
////		val dependencies =
////			attributeTemplateService.getAttributeTemplateDataTypePluginDependencies(target.modelVersion)
////		target.modelVersion.dataTypePluginDependencies = dependencies
////		updateVersion(target.modelVersion)
////	}
//
//	fun upgradeVersionModelDependency(modelId: Long, versionId: Long, depVersionId: Long) {
//		val version = findVersion(modelId, versionId)
//		val depVersion =
//			version.modelDependencies.find { it.id == depVersionId } ?: throw ApplicationValidationException(
//				ApplicationErrorCodes.ValidationModelDependencyDoesNotExistInModelVersion
//			)
//
//		if (depVersion.status == ModelVersionStatus.Released)
//			throw ApplicationValidationException(
//				ApplicationErrorCodes.ValidationModelDependencyIsAlreadyLatestRelease
//			)
//
//		val releasedVersion = modelVerRepository.findOneByStatusAndModelId(
//			ModelVersionStatus.Released,
//			depVersion.model.id
//		).orElseThrow {
//			ApplicationValidationException(
//				ApplicationErrorCodes.ValidationModelHasNoReleasedVersion
//			)
//		}
//
//		version.modelDependencies.removeIf { it.identifier == depVersion.identifier }
//
//		version.modelDependencies.add(releasedVersion)
//		updateVersion(version)
//	}
//
//	fun upgradeVersionModelDependencies(modelId: Long, versionId: Long): List<ModelVersion> {
//		var version = findVersion(modelId, versionId)
//		val upgradedDependencies = mutableListOf<ModelVersion>()
//
//		for (depVersion in version.modelDependencies) {
//			if (depVersion.status == ModelVersionStatus.Released) {
//				upgradedDependencies.add(depVersion)
//				continue
//			}
//
//			val releasedVersion = modelVerRepository.findOneByStatusAndModelId(
//				ModelVersionStatus.Released,
//				depVersion.model.id
//			).orElseThrow {
//				ApplicationValidationException(
//					ApplicationErrorCodes.ValidationModelHasNoReleasedVersion
//				)
//			}
//
//			upgradedDependencies.add(releasedVersion)
//
//		}
//
//		version.modelDependencies = upgradedDependencies
//		version = updateVersion(version)
//
//		return version.modelDependencies
//	}
//
//	fun getReleasedVersion(modelId: Long): Optional<ModelVersion> {
//		val result = modelVerRepository.findOneByStatusAndModelId(ModelVersionStatus.Released, modelId)
//		if (result.isPresent) {
//			loadMLC(result.get())
//		}
//		return result
//	}
//
//	fun findAllReleasedVersions(): List<ModelVersion> {
//		val result = modelVerRepository.findAllByStatus(ModelVersionStatus.Released)
//		result.forEach { loadMLC(it) }
//		return result
//	}
}