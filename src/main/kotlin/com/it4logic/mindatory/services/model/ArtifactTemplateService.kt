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
import com.it4logic.mindatory.services.common.ApplicationBaseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional
import kotlin.reflect.KClass


@Service
@Transactional
class ArtifactTemplateService : ApplicationBaseService<ArtifactTemplate>() {

	class UIGroupAction {
		companion object {
			val JoinAbove = 1
			val JoinBelow = 2
			val Separate = 3
		}
	}

	@Autowired
	private lateinit var artifactTemplateRepository: ArtifactTemplateRepository

	//	@Autowired
//	private lateinit var artifactStoreRepository: ArtifactStoreRepository
//
	@Autowired
	private lateinit var attributeTemplateService: AttributeTemplateService

	@Autowired
	private lateinit var modelService: ModelService

	@Autowired
	private lateinit var modelVersionService: ModelVersionService

	@Autowired
	private lateinit var relationTemplateService: RelationTemplateService

//	@Autowired
//	private lateinit var dataTypeManagerService: AttributeTemplateDataTypeManagerService

	@Autowired
	private lateinit var mlcRepository: ArtifactTemplateMLCRepository

	override fun repository(): ApplicationBaseRepository<ArtifactTemplate> = artifactTemplateRepository

	override fun type(): Class<ArtifactTemplate> = ArtifactTemplate::class.java

	override fun multipleLanguageContentRepository(): ArtifactTemplateMLCRepository = mlcRepository

	override fun multipleLanguageContentType(): KClass<*> = ArtifactTemplateMultipleLanguageContent::class

	@Suppress("UNCHECKED_CAST")
	override fun beforeCreate(target: ArtifactTemplate) {
		if (target.modelVersion.status != ModelVersionStatus.InDesign)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeObjectsWithinNoneInDesignModelVersion)

		target.identifier = UUID.randomUUID().toString()
		val result =
			findAll(null, null, "modelVersion.id==" + target.modelVersion.id) as List<ArtifactTemplate>
		val obj = result.find { it.name == target.name }
		if (obj != null)
			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.DuplicateArtifactTemplateName)

//		//    val result = mlcRepository.findAllByLanguageIdAndFieldNameAndContents(languageManager.currentLanguage.id, "name", target.name)
//		val result = mlcRepository.findAllByLanguageIdAndFieldName(languageManager.currentLanguage.id, "name")
//		val obj = result.find { it.contents == target.name }
//		//if(result.isNotEmpty()) {
//		if (obj != null) {
//			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.DuplicateArtifactTemplateName)
	}


	@Suppress("UNCHECKED_CAST")
	override fun beforeUpdate(target: ArtifactTemplate) {
		if (target.modelVersion.status != ModelVersionStatus.InDesign)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeObjectsWithinNoneInDesignModelVersion)

		val objTmp = findById(target.id)
		if (objTmp.identifier != target.identifier)
			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.ValidationIdentifierNotMatched)

		val result =
			findAll(null, null, "modelVersion.id==" + target.modelVersion.id) as List<ArtifactTemplate>
		val obj = result.find { it.name == target.name && it.identifier != target.identifier }
		if (obj != null)
			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.DuplicateArtifactTemplateName)

//	//        val result = mlcRepository.findAllByLanguageIdAndFieldNameAndContentsAndParentNot(languageManager.currentLanguage.id, "name", target.name, target.id)
//	val result = mlcRepository.findAllByLanguageIdAndFieldNameAndParentNot(
//		languageManager.currentLanguage.id,
//		"name",
//		target.id
//	)
//	val obj = result.find { it.contents == target.name }
//	//if(result.isNotEmpty()) {
//	if (obj != null) {
//		throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.DuplicateArtifactTemplateName)
	}


	override fun beforeDelete(target: ArtifactTemplate) {
		if (target.modelVersion.status != ModelVersionStatus.InDesign)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeObjectsWithinNoneInDesignModelVersion)

		for (attribute in target.attributes) {
			attributeTemplateService.delete(attribute)
		}

//		var count = artifactStoreRepository.countByArtifactTemplateId(target.id)
//		if (count > 0)
//			throw ApplicationValidationException(ApplicationErrorCodes.ValidationArtifactTemplateHasRelatedStoreData)

		relationTemplateService.deleteAnyRelatedJoinsUsedWithArtifact(target.id)

//		var result = relationTemplateRepository.findAllBySourceArtifactId(target.id)
//		for (obj in result) {
//			relationTemplateService.delete(obj)
//		}
//		if (count > 0)
//			throw ApplicationValidationException(ApplicationErrorCodes.ValidationArtifactTemplateUsedInRelationTemplates)

//		result = relationTemplateRepository.findAllByTargetArtifactId(target.id)
//		for (obj in result) {
//			relationTemplateService.delete(obj)
//		}
//		if (count > 0)
//			throw ApplicationValidationException(ApplicationErrorCodes.ValidationArtifactTemplateUsedInRelationTemplates)
	}

	override fun afterCreate(target: ArtifactTemplate) {
		modelVersionService.updateModelVersionDependencies(target.modelVersion)
	}

	override fun afterUpdate(target: ArtifactTemplate) {
		modelVersionService.updateModelVersionDependencies(target.modelVersion)
	}

	override fun afterDelete(target: ArtifactTemplate) {
		modelVersionService.updateModelVersionDependencies(target.modelVersion)
	}

	fun getRepositoryVersionDependencies(modelVersion: ModelVersion): List<ModelVersion> {
		val result = mutableListOf<ModelVersion>()

		val artifactTemplates =
			artifactTemplateRepository.findAllByModelVersionIdAndAttributes_ModelVersionIdNot(
				modelVersion.id,
				modelVersion.id
			)

		for (artifactTemplate in artifactTemplates) {
			for (attribute in artifactTemplate.attributes) {
				if (attribute.modelVersion.model.id != modelVersion.model.id) {
					if (result.find { it.id == attribute.modelVersion.id } == null)
						result.add(attribute.modelVersion)
				}
			}
		}

		return result
	}

	fun getAllArtifactsForVersion(modelVerId: Long): List<ArtifactTemplate> {
		val result = artifactTemplateRepository.findAllByModelVersionId(modelVerId)
		result.forEach {
			loadMLC(it)
		}
		return result
	}

	fun findByIdentifierForProject(identifier: String): ArtifactTemplate {
		val target = artifactTemplateRepository.findByIdentifier(identifier)
			.orElseThrow { ApplicationObjectNotFoundException(identifier, type().simpleName.toLowerCase()) }
		if (target.modelVersion.status != ModelVersionStatus.Released)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotUseArtifactInNoneReleasedVersionInsideProject)
		loadMLC(target)
		return target
	}

	fun findByIdentifierForProject(identifier: Long): ArtifactTemplate {
		val target = findById(identifier)
		if (target.modelVersion.status != ModelVersionStatus.Released)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotUseArtifactInNoneReleasedVersionInsideProject)
		return target
	}

	fun updateMetadata(id: Long, metadata: String): ArtifactTemplate {
		var artifact = findById(id)
		artifact.metadata = metadata
		artifact = repository().save(artifact)
		repository().flush()
		refresh(artifact)
		loadMLC(artifact)
		return artifact
	}


//	fun changeAttributeUIOrder(artifact: ArtifactTemplate, attributeId: Long, dir: String) {
//		var prevAttribute: AttributeTemplate? = null
//		var attributeToChange: AttributeTemplate? = null
//		for (attribute in artifact.attributes) {
//			if (attributeId == attribute.id) {
//				if (dir == "up") {
//					if (attribute.guiOrder == 1L)
//						return
//					if (prevAttribute == null)
//						return
//					val order = prevAttribute.guiOrder
//					prevAttribute.guiOrder = attribute.guiOrder
//					attribute.guiOrder = order
//					break
//				} else if (dir == "down") {
//					attributeToChange = attribute
//				} else
//					return
//
//			} else if (attributeToChange != null) {
//				val uiOrder = attribute.guiOrder
//				attribute.guiOrder = attributeToChange.guiOrder
//				attributeToChange.guiOrder = uiOrder
//				break
//			} else
//				prevAttribute = attribute
//
//		}
//
//		update(artifact)
//	}

//	fun changeAttributeUIGroup(artifact: ArtifactTemplate, attributeId: Long, action: Int) {
//		var separate = false
//		var prevAttribute: AttributeTemplate? = null
//		var attributeToChange: AttributeTemplate? = null
//		for (idx in artifact.attributes.indices) {
//			val attribute = artifact.attributes[idx]
//			if (attributeId == attribute.id) {
//				if (action == UIGroupAction.JoinAbove) {
//					if (attribute.guiGroup == 0)
//						return
//					if (prevAttribute == null)
//						return
//					attribute.guiGroup = prevAttribute.guiGroup
//					break
//				} else if (action == UIGroupAction.JoinBelow) {
//					if (idx == artifact.attributes.size - 1)
//						return
//					attributeToChange = attribute
//				} else if (action == UIGroupAction.Separate) {
//					if (idx == artifact.attributes.size - 1) {
//						if (prevAttribute != null) {
//							attribute.guiGroup = prevAttribute.guiGroup + 1
//							return
//						}
//					}
//
//					attributeToChange = attribute
//					separate = true
//				} else
//					return
//			} else {
//				if (attributeToChange != null) {
//					if (separate) {
//						if (idx == artifact.attributes.size - 1) {
//							attributeToChange.guiGroup = attribute.guiGroup + 1
//							break
//						}
//					} else {
//						attributeToChange.guiGroup = attribute.guiGroup
//						break
//					}
//				}
//				prevAttribute = attribute
//			}
//
//		}
//
//		update(artifact)
//	}

//	fun countByRepositoryVersionIdNotAndAttributesRepositoryVersionId(id: Long): Long {
//		return artifactTemplateRepository.countByRepositoryVersionIdNotAndAttributes_RepositoryVersionId(
//			id, id
//		)
//	}

// ================================================================================================================

//    fun getAllAttributes(artifactId: Long, versionId: Long): MutableList<AttributeTemplateVersion> {
//        val version = artifactTemplateVersionRepository.findOneByIdAndArtifactTemplateId(versionId,artifactId).orElseThrow {
//            ApplicationObjectNotFoundException(versionId, ArtifactTemplateVersion::class.java.simpleName.toLowerCase())
//        }
//        loadMLC(version)
//        return version.attributes
//    }
//
//    fun addAttributes(artifactId: Long, versionId: Long, attributesList : List<Long>) {
//        val version = artifactTemplateVersionRepository.findOneByIdAndArtifactTemplateId(versionId,artifactId).orElseThrow {
//            ApplicationObjectNotFoundException(versionId, ArtifactTemplateVersion::class.java.simpleName.toLowerCase())
//        }
//
//        if(version.status == ModelVersionStatus.Released)
//            throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeAttributesInReleasedArtifactTemplateVersion)
//
//        for(attributeId in attributesList) {
//            val attribute = attributeTemplateVersionRepository.findById(attributeId).orElseThrow { ApplicationObjectNotFoundException(attributeId, AttributeTemplateVersion::class.java.simpleName.toLowerCase()) }
//            if(attribute.status != ModelVersionStatus.Released)
//                throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotAddNoneReleasedAttributeToArtifactTemplateVersion, Exception(attribute.attributeTemplate.identifier))
//
//            val result = version.attributes.contains(attribute)
//            if(result)
//                throw ApplicationValidationException(ApplicationErrorCodes.ValidationAttributeAlreadyAddedToThisArtifactTemplateVersion)
//
//            version.attributes.add(attribute)
//        }
//
//        artifactTemplateVersionRepository.save(version)
//    }
//
//    fun removeAttributes(artifactId: Long, versionId: Long, attributesList : List<Long>) {
//        val version = artifactTemplateVersionRepository.findOneByIdAndArtifactTemplateId(versionId,artifactId).orElseThrow {
//            ApplicationObjectNotFoundException(versionId, ArtifactTemplateVersion::class.java.simpleName.toLowerCase())
//        }
//
//        for(attributeId in attributesList) {
//            val attribute = attributeTemplateVersionRepository.findById(attributeId).orElseThrow { ApplicationObjectNotFoundException(attributeId, AttributeTemplateVersion::class.java.simpleName.toLowerCase()) }
//
//            val result = version.attributes.contains(attribute)
//            if(!result)
//                continue
//
//            version.attributes.remove(attribute)
//        }
//
//        artifactTemplateVersionRepository.save(version)
//    }

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