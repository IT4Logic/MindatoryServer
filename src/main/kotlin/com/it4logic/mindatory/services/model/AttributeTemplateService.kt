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
		if (target.modelVersion.status != ModelVersionStatus.InDesign)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeObjectsWithinNoneInDesignModelVersion)

		val error = dataTypeManagerService.getAttributeTemplateDataType(target.typeUUID).validateDataTypeProperties(
			target.properties
		)

		if (error != null) {
			throw ApplicationGeneralException(error)
		}

//		val max = attributeTemplateRepository.maxByGuiOrderByArtifact(target.artifact.id)
//		target.guiOrder = max + 1
		target.identifier = UUID.randomUUID().toString()
		val result =
			findAll(null, null, "artifact.id==" + target.artifact.id) as List<AttributeTemplate>
		val obj = result.find { it.name == target.name }
		if (obj != null)
			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.DuplicateAttributeTemplateName)

		target.properties.forEach {
			it.attribute = target
		}
//		//    val result = mlcRepository.findAllByLanguageIdAndFieldNameAndContents(languageManager.currentLanguage.id, "name", target.name)
//		val result = mlcRepository.findAllByLanguageIdAndFieldName(languageManager.currentLanguage.id, "name")
//		val obj = result.find { it.contents == target.name }
//		//if(result.isNotEmpty()) {
//		if (obj != null) {
//			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.DuplicateAttributeTemplateName)
//		}
	}

	@Suppress("UNCHECKED_CAST")
	override fun beforeUpdate(target: AttributeTemplate) {
		if (target.modelVersion.status != ModelVersionStatus.InDesign)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeObjectsWithinNoneInDesignModelVersion)

		val error = dataTypeManagerService.getAttributeTemplateDataType(target.typeUUID).validateDataTypeProperties(
			target.properties
		)

		if (error != null)
			throw ApplicationGeneralException(error)

		val objTmp = findById(target.id)
		if (objTmp.identifier != target.identifier)
			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.ValidationIdentifierNotMatched)

		val result =
			findAll(null, null, "artifact.id==" + target.artifact.id) as List<AttributeTemplate>
		val obj = result.find { it.name == target.name && it.identifier != target.identifier }
		if (obj != null)
			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.DuplicateAttributeTemplateName)

		attributeTemplatePropertyRepository.deleteByAttributeId(target.id)
		attributeTemplatePropertyRepository.flush()

		target.properties.forEach {
			it.attribute = target
		}

//		//        val result = mlcRepository.findAllByLanguageIdAndFieldNameAndContentsAndParentNot(languageManager.currentLanguage.id, "name", target.name, target.id)
//		val result = mlcRepository.findAllByLanguageIdAndFieldNameAndParentNot(
//			languageManager.currentLanguage.id,
//			"name",
//			target.id
//		)
//		val obj = result.find { it.contents == target.name }
//		//if(result.isNotEmpty()) {
//		if (obj != null) {
//			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.DuplicateAttributeTemplateName)
//		}
	}

	override fun beforeDelete(target: AttributeTemplate) {
		if (target.modelVersion.status != ModelVersionStatus.InDesign)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeObjectsWithinNoneInDesignModelVersion)
//
//		artifactTemplateService.deleteAttributeFromArtifacts(target)


//
//		if (target.modelVersion.status == ModelVersionStatus.InDesign)
//			return
//
//		val count = attributeStoreRepository.countByAttributeTemplateId(target.id)
//		if (count > 0)
//			throw ApplicationValidationException(ApplicationErrorCodes.ValidationModelHasAttributeTemplatesRelatedStoreData)
	}

//	override fun afterCreate(target: AttributeTemplate) {
//		modelService.updateDataTypePluginDependencies(target)
//	}
//
//	override fun afterUpdate(target: AttributeTemplate) {
//		modelService.updateDataTypePluginDependencies(target)
//	}
//
//	override fun afterDelete(target: AttributeTemplate) {
//		modelService.updateDataTypePluginDependencies(target)
//	}

//	fun getAttributeTemplateDataTypePluginDependencies(repoVersion: ModelVersion): MutableList<String> {
//		val dependencies = mutableListOf<String>()
//		val attributes = attributeTemplateRepository.findAllByModelVersionId(repoVersion.id)
//
//		for (attribute in attributes) {
//			val identifier = dataTypeManagerService.getAttributeTemplateDataTypePlugin(
//				attribute.typeUUID
//			).identifier()
//			if (!dependencies.contains(identifier))
//				dependencies.add(identifier)
//		}
//		return dependencies
//	}
}