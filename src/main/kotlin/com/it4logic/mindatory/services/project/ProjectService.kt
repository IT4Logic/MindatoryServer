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
import com.it4logic.mindatory.model.common.ApplicationBaseRepository
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

/**
 * Project Data Service
 */
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

		// validates if there are any duplicates, as this property should be unique and MLC in the same time
		val result = findAll(null, null, null) as List<Project>
		val obj = result.find { it.name == target.name }
		if (obj != null)
			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.DuplicateProjectName)
	}

	@Suppress("UNCHECKED_CAST")
	override fun beforeUpdate(target: Project) {
		val objTmp = findById(target.id)
		if (objTmp.identifier != target.identifier)
			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.ValidationIdentifierNotMatched)

		// validates if there are any duplicates, as this property should be unique and MLC in the same time
		val result = findAll(null, null, null) as List<Project>
		val obj = result.find { it.name == target.name }
		if (obj != null && obj.id != target.id)
			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.DuplicateProjectName)
	}

	override fun beforeDelete(target: Project) {
		// TODO you have to delete all related objects first
	}

	/**
	 * Checks if there are any projects use the input model version
	 * @param target Input Model Version object
	 * @return True if there are projects use the model version, False otherwise
	 */
	fun checkIfProjectsUseRepositoryVersion(target: ModelVersion): Boolean {
		return projectRepository.countAllByRepositoryDependencies_Id(target.id)
	}

	/**
	 * Updates Project dependencies from Model Versions
	 * @param project Project object to be updated
	 */
	fun updateRepositoryVersionDependencies(project: Project) {
		val dependencies = mutableListOf<ModelVersion>()
		val listFromArtifacts = artifactStoreService.getRepositoryVersionDependencies(project)
		dependencies.addAll(listFromArtifacts)

		val listFromJoins = relationStoreService.getRepositoryVersionDependencies(project)
		for (item in listFromJoins) {
			if (dependencies.find { it.id == item.id } == null)
				dependencies.add(item)
		}

		project.repositoryDependencies = dependencies
		update(project)
	}

	/**
	 * Builds a map that hold released model versions and its artifact templates that can be used with this project
	 * @param id Project Id
	 * @return released model versions and its artifact templates map
	 */
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

	/**
	 * Builds the map entry for model version and its artifact
	 * @param repoVersion Model Version object
	 * @param artifacts Artifact Template list
	 * @return Map entry
	 */
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

	/**
	 * Retrieves all the used Artifact Templates for Project Id
	 * @param id Project Id
	 * @return Artifact Templates list
	 */
	fun getUsedArtifactTemplates(id: Long): List<ArtifactTemplate> {
		return artifactStoreService.getUsedArtifactTemplatesForProject(id)
	}
}