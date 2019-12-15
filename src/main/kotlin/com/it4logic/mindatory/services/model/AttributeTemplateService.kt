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

import com.it4logic.mindatory.exceptions.*
import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.model.model.ModelVersionStatus
import com.it4logic.mindatory.model.model.*
import com.it4logic.mindatory.services.AttributeTemplateDataTypeManagerService
import com.it4logic.mindatory.services.common.ApplicationBaseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional
import kotlin.reflect.KClass

/**
 * Attribute Template Data Service
 */
@Service
@Transactional
class AttributeTemplateService : ApplicationBaseService<AttributeTemplate>() {
	@Autowired
	private lateinit var attributeTemplateRepository: AttributeTemplateRepository

	@Autowired
	private lateinit var attributeTemplatePropertyRepository: AttributeTemplatePropertyRepository

	@Autowired
	lateinit var dataTypeManagerService: AttributeTemplateDataTypeManagerService

	@Autowired
	private lateinit var ModelService: ModelService

	@Autowired
	private lateinit var artifactTemplateService: ArtifactTemplateService

	@Autowired
	private lateinit var mlcRepository: AttributeTemplateMLCRepository

	override fun repository(): ApplicationBaseRepository<AttributeTemplate> = attributeTemplateRepository

	override fun type(): Class<AttributeTemplate> = AttributeTemplate::class.java

	override fun multipleLanguageContentRepository(): AttributeTemplateMLCRepository = mlcRepository

	override fun multipleLanguageContentType(): KClass<*> = AttributeTemplateMultipleLanguageContent::class

	// ================================================================================================================

	@Suppress("UNCHECKED_CAST")
	override fun beforeCreate(target: AttributeTemplate) {
		// Check if the if Model Version is released or not, as released version cannot be modified
		if (target.modelVersion?.status != ModelVersionStatus.InDesign)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeObjectsWithinNoneInDesignModelVersion)

		val error = dataTypeManagerService.getAttributeTemplateDataType(target.typeUUID).validate(
			target.properties
		)

		if (error != null) {
			throw ApplicationGeneralException(error)
		}

		target.identifier = UUID.randomUUID().toString()

		if(target.globalIdentifier.isBlank())
			target.globalIdentifier = UUID.randomUUID().toString()

		// validates if there are any duplicates, as this property should be unique and MLC in the same time
		val result =
			findAll(null, null, "artifact.id==" + target.artifact?.id) as List<AttributeTemplate>
		val obj = result.find { it.name == target.name }
		if (obj != null)
			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.DuplicateAttributeTemplateName)

		target.properties.forEach {
			it.attribute = target
		}
	}

	@Suppress("UNCHECKED_CAST")
	override fun beforeUpdate(target: AttributeTemplate) {
		// Check if the if Model Version is released or not, as released version cannot be modified
		if (target.modelVersion?.status != ModelVersionStatus.InDesign)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeObjectsWithinNoneInDesignModelVersion)

		val error = dataTypeManagerService.getAttributeTemplateDataType(target.typeUUID).validate(
			target.properties
		)

		if (error != null)
			throw ApplicationGeneralException(error)

		val objTmp = findById(target.id)
		if (objTmp.identifier != target.identifier)
			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.ValidationIdentifierNotMatched)

		// validates if there are any duplicates, as this property should be unique and MLC in the same time
		val result =
			findAll(null, null, "artifact.id==" + target.artifact?.id) as List<AttributeTemplate>
		val obj = result.find { it.name == target.name && it.identifier != target.identifier }
		if (obj != null)
			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.DuplicateAttributeTemplateName)

		// Delete and create the attributes to avoid hibernate flushing mechanism conflict
		attributeTemplatePropertyRepository.deleteByAttributeId(target.id)
		attributeTemplatePropertyRepository.flush()

		target.properties.forEach {
			it.attribute = target
		}
	}

	override fun beforeDelete(target: AttributeTemplate) {
		// Check if the if Model Version is released or not, as released version cannot be modified
		if (target.modelVersion?.status != ModelVersionStatus.InDesign)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeObjectsWithinNoneInDesignModelVersion)
	}
}