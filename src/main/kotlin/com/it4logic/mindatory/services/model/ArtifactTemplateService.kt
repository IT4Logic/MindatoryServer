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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional
import kotlin.reflect.KClass

/**
 * Artifact Template Data Service
 */
@Service
@Transactional
class ArtifactTemplateService : ApplicationBaseService<ArtifactTemplate>() {

	@Autowired
	private lateinit var artifactTemplateRepository: ArtifactTemplateRepository

	@Autowired
	private lateinit var attributeTemplateService: AttributeTemplateService

	@Autowired
	private lateinit var modelService: ModelService

	@Autowired
	private lateinit var modelVersionService: ModelVersionService

	@Autowired
	private lateinit var relationTemplateService: RelationTemplateService

	@Autowired
	private lateinit var mlcRepository: ArtifactTemplateMLCRepository

	override fun repository(): ApplicationBaseRepository<ArtifactTemplate> = artifactTemplateRepository

	override fun type(): Class<ArtifactTemplate> = ArtifactTemplate::class.java

	override fun multipleLanguageContentRepository(): ArtifactTemplateMLCRepository = mlcRepository

	override fun multipleLanguageContentType(): KClass<*> = ArtifactTemplateMultipleLanguageContent::class

	@Suppress("UNCHECKED_CAST")
	override fun beforeCreate(target: ArtifactTemplate) {
		// Check if the if Model Version is released or not, as released version cannot be modified
		if (target.modelVersion.status != ModelVersionStatus.InDesign)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeObjectsWithinNoneInDesignModelVersion)

		target.identifier = UUID.randomUUID().toString()

		// validates if there are any duplicates, as this property should be unique and MLC in the same time
		val result =
			findAll(null, null, "modelVersion.id==" + target.modelVersion.id) as List<ArtifactTemplate>
		val obj = result.find { it.name == target.name }
		if (obj != null)
			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.DuplicateArtifactTemplateName)
	}


	@Suppress("UNCHECKED_CAST")
	override fun beforeUpdate(target: ArtifactTemplate) {
		// Check if the if Model Version is released or not, as released version cannot be modified
		if (target.modelVersion.status != ModelVersionStatus.InDesign)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeObjectsWithinNoneInDesignModelVersion)

		val objTmp = findById(target.id)
		if (objTmp.identifier != target.identifier)
			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.ValidationIdentifierNotMatched)

		// validates if there are any duplicates, as this property should be unique and MLC in the same time
		val result =
			findAll(null, null, "modelVersion.id==" + target.modelVersion.id) as List<ArtifactTemplate>
		val obj = result.find { it.name == target.name && it.identifier != target.identifier }
		if (obj != null)
			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.DuplicateArtifactTemplateName)
	}


	override fun beforeDelete(target: ArtifactTemplate) {
		// Check if the if Model Version is released or not, as released version cannot be modified
		if (target.modelVersion.status != ModelVersionStatus.InDesign)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeObjectsWithinNoneInDesignModelVersion)

		for (attribute in target.attributes) {
			attributeTemplateService.delete(attribute)
		}

		relationTemplateService.deleteAnyRelatedRelationsUsedWithArtifact(target.id)
	}

	/**
	 * Searches and loads Artifact Templates related to input Model Version Id
	 * @param modelVerId Model Version Id
	 * @return Artifact Templates list
	 */
	fun getAllArtifactsForVersion(modelVerId: Long): List<ArtifactTemplate> {
		val result = artifactTemplateRepository.findAllByModelVersionId(modelVerId)
		result.forEach {
			loadMLC(it)
		}
		return result
	}

	/**
	 * Updates Artifact Template Metadata
	 * @param id Artifact Template Id
	 * @param metadata Metadata in string representation
	 * @return Updated Artifact Template object
	 */
	fun updateMetadata(id: Long, metadata: String): ArtifactTemplate {
		var artifact = findById(id)
		artifact.metadata = metadata
		artifact = repository().save(artifact)
		repository().flush()
		refresh(artifact)
		loadMLC(artifact)
		return artifact
	}

// ================================================================================================================

//    fun migrateStores(source: ArtifactTemplateVersion, target: ArtifactTemplateVersion): MutableList<ArtifactStore> {
//        val versionsMap = mutableMapOf<Long, ArtifactTemplateVersion?>()
//        val managersMap = mutableMapOf<String, ArtifactTemplateDataTypeManager?>()
//        val sourceArtifactStores = artifactStoreRepository.findAllByArtifactTemplateVersionId(source.id)
//        val targetArtifactStores = mutableListOf<ArtifactStore>()
//
//        for(project in sourceArtifactStores) {
//            val targetArtifactStore = ArtifactStore(artifact = project.artifact, artifactTemplateVersion = target)
//            targetArtifactStore.project = project.project
//
//            for(attributeStore in project.attributeStores) {
//                var artifactTemplateVersion: ArtifactTemplateVersion?
//                if(versionsMap.containsKey(attributeStore.artifact.id)) {
//                    artifactTemplateVersion = versionsMap[attributeStore.artifact.id]
//                } else {
//                    artifactTemplateVersion = getArtifactTemplateVersion(attributeStore, target)
//                    versionsMap[attributeStore.artifact.id] = artifactTemplateVersion
//                }
//
//                if(artifactTemplateVersion != null) {
//                    var manager: ArtifactTemplateDataTypeManager?
//                    if(managersMap.containsKey(artifactTemplateVersion.typeUUID)) {
//                        manager = managersMap[artifactTemplateVersion.typeUUID]
//                    }
//                    else {
//                        manager = dataTypeManagerService.getArtifactTemplateDataTypeManager(artifactTemplateVersion.typeUUID)
//                        managersMap[artifactTemplateVersion.typeUUID] = manager
//                    }
//
////                    val result = manager!!.migrateStoreContent(attributeStore.contentsJson, UUID.fromString(artifactTemplateVersion.typeUUID),
////                                                                    artifactTemplateVersion.propertiesJson)
////
////                    val targetAttributeStore = AttributeStore(contents = "", contentsJson = result,
////                                                    artifact = attributeStore.artifact,
////                                                    artifactTemplateVersion = artifactTemplateVersion)
////                    targetArtifactStore.attributeStores.add(targetAttributeStore)
//                }
//            }
//
//            project.storeStatus = StoreObjectStatus.Migrated
//            targetArtifactStores.add(targetArtifactStore)
//        }
//
//        artifactStoreRepository.saveAll(sourceArtifactStores)
//        return artifactStoreRepository.saveAll(targetArtifactStores)
//    }

//    fun migrateStores(artifactId: Long, versionId: Long, targetVersionId: Long): MutableList<ArtifactStore> {
//        val targetVersion = artifactTemplateVersionRepository.findOneByIdAndArtifactTemplateId(targetVersionId,artifactId).orElseThrow {
//            ApplicationObjectNotFoundException(versionId, ArtifactTemplateVersion::class.java.simpleName.toLowerCase())
//        }
//
//        if(targetVersion.status != ModelVersionStatus.Released)
//            throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotMigrateStoreObjectsToNoneReleasedVersion)
//
//        val sourceVersion = artifactTemplateVersionRepository.findOneByIdAndArtifactTemplateId(versionId,artifactId).orElseThrow {
//            ApplicationObjectNotFoundException(versionId, ArtifactTemplateVersion::class.java.simpleName.toLowerCase())
//        }
//
//        return migrateStores(sourceVersion, targetVersion)
//    }
}