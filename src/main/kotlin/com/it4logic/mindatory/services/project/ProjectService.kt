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

import com.it4logic.mindatory.exceptions.*
import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.model.model.*
import com.it4logic.mindatory.model.project.Project
import com.it4logic.mindatory.model.project.ProjectMLCRepository
import com.it4logic.mindatory.model.project.ProjectMultipleLanguageContent
import com.it4logic.mindatory.model.project.ProjectRepository
import com.it4logic.mindatory.services.common.ApplicationBaseService
import com.it4logic.mindatory.services.model.ModelService
import com.it4logic.mindatory.services.model.ArtifactTemplateService
import com.it4logic.mindatory.services.model.ModelVersionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
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
	@Autowired
	private lateinit var projectRepository: ProjectRepository

	@Autowired
	private lateinit var artifactStoreService: ArtifactStoreService

	@Autowired
	private lateinit var attributeStoreService: AttributeStoreService

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

		if (target.modelVersion.status != ModelVersionStatus.Released)
			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.ValidationCannotLinkProjectToNoneReleasedModelVersion)

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

		// Check if there are any stores to allow change model and model version
		if (objTmp.modelVersion.id != target.modelVersion.id) {
			if (objTmp.artifacts.size > 0)
				throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.ValidationCannotChangeModelWhileStoresExist)

			if (target.modelVersion.status != ModelVersionStatus.Released)
				throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.ValidationCannotLinkProjectToNoneReleasedModelVersion)
		}

		// validates if there are any duplicates, as this property should be unique and MLC in the same time
		val result = findAll(null, null, null) as List<Project>
		val obj = result.find { it.name == target.name }
		if (obj != null && obj.id != target.id)
			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.DuplicateProjectName)
	}

	override fun beforeDelete(target: Project) {
		for (relation in target.relations) {
			relationStoreService.delete(relation)
		}

		for (artifact in target.artifacts) {
			artifactStoreService.delete(artifact)
		}
	}

	/**
	 * Checks if there are any projects use the input model version
	 * @param target Input Model Version object
	 * @return True if there are projects use the model version, False otherwise
	 */
	fun checkIfProjectsUseModelVersion(target: ModelVersion): Boolean {
		return projectRepository.countAllByModelVersionId(target.id) > 0
	}

	/**
	 * Builds a map that hold released model versions and its artifact templates that can be used with this project
	 * @param id Project Id
	 * @return released model versions and its artifact templates map
	 */
	fun getAvailableArtifactTemplates(id: Long): List<ArtifactTemplate> {
		val project = findById(id)
		return artifactTemplateService.getAllArtifactsForVersion(project.modelVersion.id)
	}

	/**
	 * Retrieves all the used Artifact Templates for Project Id
	 * @param id Project Id
	 * @return Artifact Templates list
	 */
	fun getUsedArtifactTemplates(id: Long): List<ArtifactTemplate> {
		return artifactStoreService.getUsedArtifactTemplatesForProject(id)
	}

	fun migrateStores(projectId: Long) {
		var project = findById(projectId)
		if(project.modelVersion.status == ModelVersionStatus.Released)
			return

		val modelVersion = project.modelVersion.model.versions.find { it.status == ModelVersionStatus.Released } ?:
				throw ApplicationGeneralException(
					ApiError(HttpStatus.NOT_ACCEPTABLE,
						ApplicationErrorCodes.ValidationModelHasNoReleasedVersion,
						project.modelVersion.model.id.toString())
				)

		project.modelVersion = modelVersion
		project = update(project)

		for(artifact in project.artifacts) {
			// get the new template version equivalent to current template
			val artifactTemplate = getEquivalentTemplate(modelVersion, artifact.artifactTemplate!!) ?:
					throw ApplicationGeneralException(
						ApiError(HttpStatus.NOT_ACCEPTABLE,
							ApplicationErrorCodes.ValidationNoEquivalentArtifactTemplateExists,
							artifact.id.toString())
					)

			artifact.artifactTemplate = artifactTemplate
			artifactStoreService.update(artifact)

			for(attribute in artifact.attributes) {
				// get the new template version equivalent to current template
				val attributeTemplate = getEquivalentTemplate(artifactTemplate, attribute.attributeTemplate) ?:
							throw ApplicationGeneralException(
								ApiError(HttpStatus.NOT_ACCEPTABLE,
									ApplicationErrorCodes.ValidationNoEquivalentAttributeTemplateExists,
									attribute.id.toString())
							)

				attribute.attributeTemplate = attributeTemplate
				attributeStoreService.update(attribute)
			}

		}

		for(relation in project.relations) {
			// get the new template version equivalent to current template
			val relationTemplate = getEquivalentTemplate(modelVersion, relation.relationTemplate!!) ?:
							throw ApplicationGeneralException(
								ApiError(HttpStatus.NOT_ACCEPTABLE,
									ApplicationErrorCodes.ValidationNoEquivalentRelationTemplateExists,
									relation.id.toString())
							)

			relation.relationTemplate = relationTemplate
			relationStoreService.update(relation)
		}
	}

	private fun getEquivalentTemplate(modelVersion: ModelVersion, artifactTemplate: ArtifactTemplate): ArtifactTemplate? {
		return modelVersion.artifacts.find {
			it.globalIdentifier == artifactTemplate.globalIdentifier
		}
	}

	private fun getEquivalentTemplate(artifactTemplate: ArtifactTemplate, attributeTemplate: AttributeTemplate): AttributeTemplate? {
		return artifactTemplate.attributes.find {
			it.globalIdentifier == attributeTemplate.globalIdentifier
		}
	}

	private fun getEquivalentTemplate(modelVersion: ModelVersion, relationTemplate: RelationTemplate): RelationTemplate? {
		return modelVersion.relations.find {
			it.globalIdentifier == relationTemplate.globalIdentifier
		}
	}
}