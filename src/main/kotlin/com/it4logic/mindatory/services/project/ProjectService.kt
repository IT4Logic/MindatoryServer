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

import com.it4logic.mindatory.exceptions.ApplicationDataIntegrityViolationException
import com.it4logic.mindatory.exceptions.ApplicationErrorCodes
import com.it4logic.mindatory.exceptions.ApplicationValidationException
import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.model.model.ModelVersionStatus
import com.it4logic.mindatory.model.project.Project
import com.it4logic.mindatory.model.project.ProjectMLCRepository
import com.it4logic.mindatory.model.project.ProjectMultipleLanguageContent
import com.it4logic.mindatory.model.project.ProjectRepository
import com.it4logic.mindatory.model.model.Model
import com.it4logic.mindatory.model.model.ModelVersion
import com.it4logic.mindatory.model.model.ArtifactTemplate
import com.it4logic.mindatory.services.common.ApplicationBaseService
import com.it4logic.mindatory.services.model.ModelService
import com.it4logic.mindatory.services.model.ArtifactTemplateService
import com.it4logic.mindatory.services.model.ModelVersionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional
import kotlin.reflect.KClass


@Service
@Transactional
class ProjectService : ApplicationBaseService<Project>() {

	open class ObjectSummary(
		open var id: Long,
		open var identifier: String,
		open var name: String
	)

	data class ModelVersionArtifactTemplatesMap(
		override var id: Long,
		override var identifier: String,
		override var name: String,
		var artifacts: List<ObjectSummary>
	) : ObjectSummary(id, identifier, name)

	@Autowired
	private lateinit var projectRepository: ProjectRepository

	@Autowired
	private lateinit var artifactStoreService: ArtifactStoreService

	@Autowired
	private lateinit var relationStoreService: RelationStoreService

	@Autowired
	private lateinit var modelService: ModelService

	@Autowired
	private lateinit var modelVersionService: ModelVersionService

	@Autowired
	private lateinit var artifactTemplateService: ArtifactTemplateService

	@Autowired
	private lateinit var mlcRepository: ProjectMLCRepository

	override fun repository(): ApplicationBaseRepository<Project> = projectRepository

	override fun type(): Class<Project> = Project::class.java

	override fun multipleLanguageContentRepository(): ProjectMLCRepository = mlcRepository

	override fun multipleLanguageContentType(): KClass<*> = ProjectMultipleLanguageContent::class

	@Suppress("UNCHECKED_CAST")
	override fun beforeCreate(target: Project) {
		target.identifier = UUID.randomUUID().toString()

		val result = findAll(null, null, null) as List<Project>
		val obj = result.find { it.name == target.name }
		if (obj != null)
			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.DuplicateProjectName)

//		//    val result = mlcRepository.findAllByLanguageIdAndFieldNameAndContents(languageManager.currentLanguage.id, "name", target.name)
//		val result = mlcRepository.findAllByLanguageIdAndFieldName(languageManager.currentLanguage.id, "name")
//		val obj = result.find { it.contents == target.name }
//		//if(result.isNotEmpty()) {
//		if (obj != null) {
//			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.DuplicateProjectName)
//		}
	}

	@Suppress("UNCHECKED_CAST")
	override fun beforeUpdate(target: Project) {
		val objTmp = findById(target.id)
		if (objTmp.identifier != target.identifier)
			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.ValidationIdentifierNotMatched)

		val result = findAll(null, null, null) as List<Project>
		val obj = result.find { it.name == target.name }
		if (obj != null && obj.id != target.id)
			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.DuplicateProjectName)

//		//        val result = mlcRepository.findAllByLanguageIdAndFieldNameAndContentsAndParentNot(languageManager.currentLanguage.id, "name", target.name, target.id)
//		val result = mlcRepository.findAllByLanguageIdAndFieldNameAndParentNot(
//			languageManager.currentLanguage.id,
//			"name",
//			target.id
//		)
//		val obj = result.find { it.contents == target.name }
//		//if(result.isNotEmpty()) {
//		if (obj != null) {
//			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.DuplicateProjectName)
//		}
	}

	override fun beforeDelete(target: Project) {
//		val count = repoRepositoryVersion.countByProjectId(target.id)
//		if (count > 0)
//			throw ApplicationValidationException(ApplicationErrorCodes.ValidationProjectHasModel)
	}

	fun checkIfProjectsUseRepositoryVersion(target: ModelVersion): Boolean {
		return projectRepository.countAllByRepositoryDependencies_Id(target.id)
	}

	fun addToRepositoryDependenciesList(
		project: Project,
		dependencyRepoVer: ModelVersion
	) {

		if (dependencyRepoVer.status == ModelVersionStatus.Released)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotLinkProjectToNoneReleasedModelVersion)

		val result = project.repositoryDependencies.find { it.identifier == dependencyRepoVer.identifier }
		if (result != null)
			return

		project.repositoryDependencies.add(dependencyRepoVer)
		update(project)
	}

	fun removeFromRepositoryDependenciesList(
		project: Project,
		dependencyRepoVer: ModelVersion
	) {
		if (dependencyRepoVer.status == ModelVersionStatus.Released)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeNotInDesignApplicationModelVersion)

		for ((index, repo) in project.repositoryDependencies.withIndex()) {
			if (repo.identifier == dependencyRepoVer.identifier) {
				project.repositoryDependencies.removeAt(index)
				break
			}
		}

		update(project)
	}

	fun updateRepositoryVersionDependencies(project: Project) {
		val dependencies = calculateRepositoryVersionDependencies(project)
		project.repositoryDependencies = dependencies
		update(project)
	}

	fun calculateRepositoryVersionDependencies(project: Project): MutableList<ModelVersion> {
		val dependencies = mutableListOf<ModelVersion>()

		val listFromArtifacts = artifactStoreService.getRepositoryVersionDependencies(project)
		dependencies.addAll(listFromArtifacts)

		val listFromJoins = relationStoreService.getRepositoryVersionDependencies(project)
		for (item in listFromJoins) {
			if (dependencies.find { it.id == item.id } == null)
				dependencies.add(item)
		}

		return dependencies
	}

	@Suppress("UNCHECKED_CAST")
	fun getAvailableArtifactsMap(id: Long): List<ModelVersionArtifactTemplatesMap> {
		val list = mutableListOf<ModelVersionArtifactTemplatesMap>()
		val project = findById(id)

		val repos = modelService.findAll(null, null, null) as List<Model>
		for (repo in repos) {
			val result = project.repositoryDependencies.find { it.model.id == repo.id }
			if (result != null) {
				val artifacts = artifactTemplateService.getAllArtifactsForVersion(result.id)
				list.add(buildRepoInfo(result, artifacts))
				continue
			}

			val version = modelVersionService.getReleasedVersion(repo.id)
			if (version.isEmpty)
				continue
			val artifacts = artifactTemplateService.getAllArtifactsForVersion(version.get().id)
			list.add(buildRepoInfo(version.get(), artifacts))
		}

		return list
	}

	private fun buildRepoInfo(
		repoVersion: ModelVersion,
		artifacts: List<ArtifactTemplate>
	): ModelVersionArtifactTemplatesMap {
		val artifactsInfo = mutableListOf<ObjectSummary>()
		for (artifact in artifacts) {
			artifactsInfo.add(
				ObjectSummary(
					artifact.id,
					artifact.identifier,
					artifact.name
				)
			)
		}

		return ModelVersionArtifactTemplatesMap(
			repoVersion.id,
			repoVersion.identifier,
			repoVersion.model.name,
			artifactsInfo
		)
	}

	fun getArtifactByIdentifier(identifier: Long): ArtifactTemplate =
		artifactTemplateService.findByIdentifierForProject(identifier)

	fun getUsedArtifactTemplates(id: Long): List<ArtifactTemplate> {
		return artifactStoreService.getUsedArtifactTemplatesForProject(id)
	}
}