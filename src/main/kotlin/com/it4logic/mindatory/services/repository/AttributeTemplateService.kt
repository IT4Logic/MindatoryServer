/*
    Copyright (c) 2019, IT4Logic.

    This file is part of Mindatory solution by IT4Logic.

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

package com.it4logic.mindatory.services.repository

import com.it4logic.mindatory.exceptions.*
import com.it4logic.mindatory.mlc.LanguageManager
import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.model.common.DesignStatus
import com.it4logic.mindatory.model.repository.*
import com.it4logic.mindatory.model.store.AttributeStoreRepository
import com.it4logic.mindatory.services.RepositoryManagerService
import com.it4logic.mindatory.services.common.ApplicationBaseService
import com.it4logic.mindatory.services.security.SecurityAclService
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
	private lateinit var attributeTemplateVersionRepository: AttributeTemplateVersionRepository

	@Autowired
	lateinit var repositoryManagerService: RepositoryManagerService

	@Autowired
	private lateinit var attributeStoreRepository: AttributeStoreRepository

	@Autowired
	private lateinit var artifactTemplateVersionRepository: ArtifactTemplateVersionRepository

	@Autowired
	protected lateinit var securityAclService: SecurityAclService

	@Autowired
	private lateinit var mlcRepository: AttributeTemplateMLCRepository

	@Autowired
	protected lateinit var languageManager: LanguageManager

	override fun repository(): ApplicationBaseRepository<AttributeTemplate> = attributeTemplateRepository

	override fun type(): Class<AttributeTemplate> = AttributeTemplate::class.java

	override fun useAcl(): Boolean = false

	override fun securityAclService(): SecurityAclService? = securityAclService

	override fun multipleLanguageContentRepository(): AttributeTemplateMLCRepository = mlcRepository

	override fun multipleLanguageContentType(): KClass<*> = AttributeTemplateMultipleLanguageContent::class

	// ================================================================================================================

	fun getAllDesignVersions(id: Long): List<AttributeTemplateVersion> {
		val obj = findById(id)
		for (ver in obj.versions)
			multipleLanguageContentService.load(ver)
		return obj.versions
	}

	fun getDesignVersion(id: Long, versionId: Long): AttributeTemplateVersion {
		val obj = findById(id)
		for (version in obj.versions) {
			if (version.id == versionId) {
				multipleLanguageContentService.load(version)
				return version
			}
		}
		throw ApplicationObjectNotFoundException(
			versionId,
			AttributeTemplateVersion::class.java.simpleName.toLowerCase()
		)
	}

	fun createVersion(
		attributeTemplateId: Long,
		attributeTemplateVersion: AttributeTemplateVersion
	): AttributeTemplateVersion {
		return createVersion(findById(attributeTemplateId), attributeTemplateVersion)
	}

	fun createVersion(
		target: AttributeTemplate,
		attributeTemplateVersion: AttributeTemplateVersion
	): AttributeTemplateVersion {
		// check if we have a current in-design version
		val result = attributeTemplateVersionRepository.findOneByAttributeTemplateIdAndDesignStatus(
			target.id,
			DesignStatus.InDesign
		)

		if (result.isPresent)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationAttributeTemplateHasInDesignVersion)

		attributeTemplateVersion.attributeTemplate = target
		attributeTemplateVersion.repository = target.repository
		attributeTemplateVersion.solution = target.solution

		val dataTypeManager =
			repositoryManagerService.getAttributeTemplateDataTypeManager(attributeTemplateVersion.typeUUID)
		val error = dataTypeManager.validateDataTypeProperties(
			UUID.fromString(attributeTemplateVersion.typeUUID),
			attributeTemplateVersion.properties
		)

		if (error != null)
			throw ApplicationGeneralException(error as ApiError)

		val max = attributeTemplateVersionRepository.maxDesignVersion(target.id)
		attributeTemplateVersion.designVersion = max + 1
		attributeTemplateVersion.designStatus = DesignStatus.InDesign
		val ver = attributeTemplateVersionRepository.save(attributeTemplateVersion)
		repository().flush()
		entityManager.refresh(ver)

		if (target.versions == null)
			target.versions = mutableListOf()

		target.versions.add(ver)
		update(target)

		multipleLanguageContentService.load(ver)

		return ver
	}

	fun updateVersion(
		attributeTemplateId: Long,
		attributeTemplateVersion: AttributeTemplateVersion
	): AttributeTemplateVersion {
		val result = attributeTemplateVersionRepository.findOneByIdAndAttributeTemplateId(
			attributeTemplateVersion.id,
			attributeTemplateId
		).orElseThrow {
			ApplicationObjectNotFoundException(
				attributeTemplateVersion.id,
				AttributeTemplateVersion::class.java.simpleName.toLowerCase()
			)
		}

		if (result.designStatus == DesignStatus.Released)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeReleasedAttributeTemplateVersion)

		val target = findById(attributeTemplateId)
		attributeTemplateVersion.attributeTemplate = target
		attributeTemplateVersion.repository = target.repository
		attributeTemplateVersion.solution = target.solution

		val dataTypeManager =
			repositoryManagerService.getAttributeTemplateDataTypeManager(attributeTemplateVersion.typeUUID)
		val error = dataTypeManager.validateDataTypeProperties(
			UUID.fromString(attributeTemplateVersion.typeUUID),
			attributeTemplateVersion.properties
		)

		if (error != null)
			throw ApplicationGeneralException(error as ApiError)

		val ver = attributeTemplateVersionRepository.save(attributeTemplateVersion)
		repository().flush()
		entityManager.refresh(ver)

		multipleLanguageContentService.load(ver)

		return ver
	}

	fun releaseVersion(attributeTemplateId: Long, attributeTemplateVersionId: Long): AttributeTemplateVersion {
		val result = attributeTemplateVersionRepository.findOneByIdAndAttributeTemplateId(
			attributeTemplateVersionId,
			attributeTemplateId
		).orElseThrow {
			ApplicationObjectNotFoundException(
				attributeTemplateVersionId,
				AttributeTemplateVersion::class.java.simpleName.toLowerCase()
			)
		}
		return releaseVersion(attributeTemplateId, result)
	}

	fun releaseVersion(
		attributeTemplateId: Long,
		attributeTemplateVersion: AttributeTemplateVersion
	): AttributeTemplateVersion {
		val result = attributeTemplateVersionRepository.findOneByIdAndAttributeTemplateId(
			attributeTemplateVersion.id,
			attributeTemplateId
		).orElseThrow {
			ApplicationObjectNotFoundException(
				attributeTemplateVersion.id,
				AttributeTemplateVersion::class.java.simpleName.toLowerCase()
			)
		}

		if (result.designStatus == DesignStatus.Released)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeReleasedAttributeTemplateVersion)

		attributeTemplateVersion.designStatus = DesignStatus.Released

		val ver = attributeTemplateVersionRepository.save(attributeTemplateVersion)
		repository().flush()
		entityManager.refresh(ver)

		multipleLanguageContentService.load(ver)

		return ver
	}

	fun deleteVersion(attributeTemplateId: Long, attributeTemplateVersionId: Long) {
		val result = attributeTemplateVersionRepository.findOneByIdAndAttributeTemplateId(
			attributeTemplateVersionId,
			attributeTemplateId
		).orElseThrow {
			ApplicationObjectNotFoundException(
				attributeTemplateVersionId,
				AttributeTemplateVersion::class.java.simpleName.toLowerCase()
			)
		}
		deleteVersion(attributeTemplateId, result)
	}

	fun deleteVersion(attributeTemplateId: Long, attributeTemplateVersion: AttributeTemplateVersion) {
		val result = attributeTemplateVersionRepository.findOneByIdAndAttributeTemplateId(
			attributeTemplateVersion.id,
			attributeTemplateId
		).orElseThrow {
			ApplicationObjectNotFoundException(
				attributeTemplateVersion.id,
				AttributeTemplateVersion::class.java.simpleName.toLowerCase()
			)
		}

		var count = attributeStoreRepository.countByAttributeTemplateVersionId(attributeTemplateVersion.id)
		if (count > 0)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationAttributeTemplateVersionHasRelatedStoreData)

		// check if there are attribute templates from this repository used in artifact templates from other repositories
		count = artifactTemplateVersionRepository.countByAttributes_Id(attributeTemplateVersion.id)
		if (count > 0)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationAttributeTemplateVersionUsedInArtifactTemplates)

		attributeTemplateVersionRepository.delete(result)
	}

	// ================================================================================================================

	override fun beforeCreate(target: AttributeTemplate) {
		//    val result = mlcRepository.findAllByLanguageIdAndFieldNameAndContents(languageManager.currentLanguage.id, "name", target.name)
		val result = mlcRepository.findAllByLanguageIdAndFieldName(languageManager.currentLanguage.id, "name")
		val obj = result.find { it.contents == target.name }
		//if(result.isNotEmpty()) {
		if (obj != null) {
			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.DuplicateAttributeTemplateName)
		}
	}

	override fun beforeUpdate(target: AttributeTemplate) {
		//        val result = mlcRepository.findAllByLanguageIdAndFieldNameAndContentsAndParentNot(languageManager.currentLanguage.id, "name", target.name, target.id)
		val result = mlcRepository.findAllByLanguageIdAndFieldNameAndParentNot(
			languageManager.currentLanguage.id,
			"name",
			target.id
		)
		val obj = result.find { it.contents == target.name }
		//if(result.isNotEmpty()) {
		if (obj != null) {
			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.DuplicateAttributeTemplateName)
		}
	}

	override fun beforeDelete(target: AttributeTemplate) {
		// check if there are attribute stores based on attribute templates from this repository
		val count = attributeStoreRepository.countByAttributeTemplateRepositoryId(target.id)
		if (count > 0)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationRepositoryHasAttributeTemplatesRelatedStoreData)
	}
}