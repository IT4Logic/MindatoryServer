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
import kotlin.reflect.KClass

/**
 * Model Data Service
 */
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

	@Autowired
	private lateinit var mlcRepository: ModelMLCRepository

	override fun repository(): ApplicationBaseRepository<Model> = modelRepository

	override fun type(): Class<Model> = Model::class.java

	override fun multipleLanguageContentRepository(): ModelMLCRepository = mlcRepository

	override fun multipleLanguageContentType(): KClass<*> = ModelMultipleLanguageContent::class

	@Suppress("UNCHECKED_CAST")
	override fun beforeCreate(target: Model) {
		target.identifier = UUID.randomUUID().toString()

		// validates if there are any duplicates, as this property should be unique and MLC in the same time
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

		// validates if there are any duplicates, as this property should be unique and MLC in the same time
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

	/**
	 * Checks if the input model version object has any store objects related to it
	 * @param target Input Model Version object
	 * @return True if there are store objects related exist, False otherwise
	 */
	fun checkIfVersionHasRelatedStoreObjects(target: ModelVersion): Boolean {
		return projectService.checkIfProjectsUseRepositoryVersion(target)
	}
}