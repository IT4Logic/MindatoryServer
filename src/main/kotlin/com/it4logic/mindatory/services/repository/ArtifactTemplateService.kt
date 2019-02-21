/*
    Copyright (c) 2017, IT4Logic.

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

import com.it4logic.mindatory.exceptions.ApplicationDataIntegrityViolationException
import com.it4logic.mindatory.exceptions.ApplicationErrorCodes
import com.it4logic.mindatory.exceptions.ApplicationObjectNotFoundException
import com.it4logic.mindatory.exceptions.ApplicationValidationException
import com.it4logic.mindatory.mlc.LanguageManager
import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.model.common.DesignStatus
import com.it4logic.mindatory.model.repository.*
import com.it4logic.mindatory.model.store.ArtifactStoreRepository
import com.it4logic.mindatory.services.RepositoryManagerService
import com.it4logic.mindatory.services.common.ApplicationBaseService
import com.it4logic.mindatory.services.security.SecurityAclService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.lang.Exception
import javax.transaction.Transactional
import kotlin.reflect.KClass


@Service
@Transactional
class ArtifactTemplateService : ApplicationBaseService<ArtifactTemplate>() {
    @Autowired
    private lateinit var artifactTemplateRepository: ArtifactTemplateRepository

    @Autowired
    private lateinit var artifactTemplateVersionRepository: ArtifactTemplateVersionRepository

    @Autowired
    private lateinit var artifactStoreRepository: ArtifactStoreRepository

    @Autowired
    private lateinit var joinTemplateVersionRepository: JoinTemplateVersionRepository

    @Autowired
    private lateinit var repositoryManagerService: RepositoryManagerService

    @Autowired
    private lateinit var attributeTemplateVersionRepository: AttributeTemplateVersionRepository

    @Autowired
    protected lateinit var securityAclService: SecurityAclService

    @Autowired
    private lateinit var mlcRepository: ArtifactTemplateMLCRepository

    @Autowired
    protected lateinit var languageManager: LanguageManager

    override fun repository(): ApplicationBaseRepository<ArtifactTemplate> = artifactTemplateRepository

    override fun type(): Class<ArtifactTemplate> = ArtifactTemplate::class.java

    override fun useAcl() : Boolean = true

    override fun securityAclService() : SecurityAclService? = securityAclService

    override fun multipleLanguageContentRepository() : ArtifactTemplateMLCRepository = mlcRepository

    override fun multipleLanguageContentType() : KClass<*> = ArtifactTemplateMultipleLanguageContent::class

    override fun beforeCreate(target: ArtifactTemplate) {
        val result = mlcRepository.findAllByLanguageIdAndFieldNameAndContents(languageManager.currentLanguage.id, "name", target.name)
        if(result.isNotEmpty()) {
            throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.DuplicateArtifactTemplateName)
        }
    }

    override fun beforeUpdate(target: ArtifactTemplate) {
        val result = mlcRepository.findAllByLanguageIdAndFieldNameAndContentsAndParentNot(languageManager.currentLanguage.id, "name", target.name, target.id)
        if(result.isNotEmpty()) {
            throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.DuplicateArtifactTemplateName)
        }
    }

    override fun beforeDelete(target: ArtifactTemplate) {
        //todo check if the store to be related to version of artifact
        var count = artifactStoreRepository.countByArtifactTemplateId(target.id)
        if (count > 0)
            throw ApplicationValidationException(ApplicationErrorCodes.ValidationArtifactTemplateHasRelatedStoreData)

        //todo check if the join to be related to version of artifact
        count = joinTemplateVersionRepository.countBySourceArtifacts_Id(target.id)
        if (count > 0)
            throw ApplicationValidationException(ApplicationErrorCodes.ValidationArtifactTemplateUsedInJoinTemplates)

        count = joinTemplateVersionRepository.countByTargetArtifacts_Id(target.id)
        if (count > 0)
            throw ApplicationValidationException(ApplicationErrorCodes.ValidationArtifactTemplateUsedInJoinTemplates)
    }

    // ================================================================================================================

    fun getAllDesignVersions(id: Long): List<ArtifactTemplateVersion> {
        val obj = findById(id)
        return obj.versions
    }

    fun getDesignVersion(id: Long, versionId: Long): ArtifactTemplateVersion {
        val obj = findById(id)
        for(version in obj.versions) {
            if(version.id == versionId)
                return version
        }
        throw ApplicationObjectNotFoundException(versionId, ArtifactTemplateVersion::class.java.simpleName.toLowerCase())
    }

    fun createVersion(artifactTemplateId: Long, artifactTemplateVersion: ArtifactTemplateVersion): ArtifactTemplateVersion {
        return createVersion(findById(artifactTemplateId), artifactTemplateVersion)
    }

    fun createVersion(target: ArtifactTemplate, artifactTemplateVersion: ArtifactTemplateVersion): ArtifactTemplateVersion {
        // check if we have a current in-design version
        val result = artifactTemplateVersionRepository.findOneByArtifactTemplateIdAndDesignStatus(
            target.id,
            DesignStatus.InDesign
        )

        if (result.isPresent)
            throw ApplicationValidationException(ApplicationErrorCodes.ValidationArtifactTemplateHasInDesignVersion)

        artifactTemplateVersion.artifactTemplate = target
        artifactTemplateVersion.repository = target.repository
        artifactTemplateVersion.solution = target.solution

        val max = artifactTemplateVersionRepository.maxDesignVersion(target.id)
        artifactTemplateVersion.designVersion = max + 1
        artifactTemplateVersion.designStatus = DesignStatus.InDesign
        val ver = artifactTemplateVersionRepository.save(artifactTemplateVersion)
        repository().flush()
        entityManager.refresh(ver)

        if(target.versions == null)
            target.versions = mutableListOf()

        target.versions.add(ver)
        update(target)

        multipleLanguageContentService.load(ver)

        return ver
    }

    fun updateVersion(artifactTemplateId: Long, artifactTemplateVersion: ArtifactTemplateVersion): ArtifactTemplateVersion {
        val result = artifactTemplateVersionRepository.findOneByIdAndArtifactTemplateId(artifactTemplateVersion.id, artifactTemplateId).orElseThrow {
            ApplicationObjectNotFoundException(artifactTemplateVersion.id, ArtifactTemplateVersion::class.java.simpleName.toLowerCase())
        }

        if(result.designStatus == DesignStatus.Released)
            throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeReleasedArtifactTemplateVersion)

        val target = findById(artifactTemplateId)
        artifactTemplateVersion.artifactTemplate = target
        artifactTemplateVersion.repository = target.repository
        artifactTemplateVersion.solution = target.solution

        val ver = artifactTemplateVersionRepository.save(artifactTemplateVersion)
        repository().flush()
        entityManager.refresh(ver)

        multipleLanguageContentService.load(ver)

        return ver
    }

    fun releaseVersion(artifactTemplateId: Long, artifactTemplateVersionId: Long): ArtifactTemplateVersion {
        val result = artifactTemplateVersionRepository.findOneByIdAndArtifactTemplateId(artifactTemplateVersionId, artifactTemplateId).orElseThrow {
            ApplicationObjectNotFoundException(artifactTemplateVersionId, ArtifactTemplateVersion::class.java.simpleName.toLowerCase())
        }
        return releaseVersion(artifactTemplateId, result)
    }

    fun releaseVersion(artifactTemplateId: Long, artifactTemplateVersion: ArtifactTemplateVersion): ArtifactTemplateVersion {
        val result = artifactTemplateVersionRepository.findOneByIdAndArtifactTemplateId(artifactTemplateVersion.id, artifactTemplateId).orElseThrow {
            ApplicationObjectNotFoundException(artifactTemplateVersion.id, ArtifactTemplateVersion::class.java.simpleName.toLowerCase())
        }

        if(result.designStatus == DesignStatus.Released)
            throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeReleasedArtifactTemplateVersion)

        artifactTemplateVersion.designStatus = DesignStatus.Released

        val ver = artifactTemplateVersionRepository.save(artifactTemplateVersion)
        repository().flush()
        entityManager.refresh(ver)

        multipleLanguageContentService.load(ver)

        return ver
    }

    fun deleteVersion(artifactTemplateId: Long, artifactTemplateVersionId: Long) {
        val result = artifactTemplateVersionRepository.findOneByIdAndArtifactTemplateId(artifactTemplateVersionId, artifactTemplateId).orElseThrow {
            ApplicationObjectNotFoundException(artifactTemplateVersionId, ArtifactTemplateVersion::class.java.simpleName.toLowerCase())
        }
        deleteVersion(artifactTemplateId, result)
    }

    fun deleteVersion(artifactTemplateId: Long, artifactTemplateVersion: ArtifactTemplateVersion) {
        val result = artifactTemplateVersionRepository.findOneByIdAndArtifactTemplateId(artifactTemplateVersion.id, artifactTemplateId).orElseThrow {
            ApplicationObjectNotFoundException(artifactTemplateVersion.id, ArtifactTemplateVersion::class.java.simpleName.toLowerCase())
        }

        var count = artifactStoreRepository.countByArtifactTemplateVersionId(artifactTemplateVersion.id)
        if (count > 0)
            throw ApplicationValidationException(ApplicationErrorCodes.ValidationArtifactTemplateVersionHasRelatedStoreData)

        //todo check if the join to be related to version of artifact
//        count = joinTemplateVersionRepository.countBySourceArtifacts_Id(artifactTemplateVersion.artifactTemplate.id)
//        if (count > 0)
//            throw ApplicationValidationException(ApplicationErrorCodes.ValidationArtifactTemplateUsedInJoinTemplates)
//
//        count = joinTemplateVersionRepository.countByTargetArtifacts_Id(artifactTemplateVersion.artifactTemplate.id)
//        if (count > 0)
//            throw ApplicationValidationException(ApplicationErrorCodes.ValidationArtifactTemplateUsedInJoinTemplates)

        artifactTemplateVersionRepository.delete(result)
    }

    // ================================================================================================================

    fun getAllAttributes(artifactId: Long, versionId: Long): MutableList<AttributeTemplateVersion> {
        val version = artifactTemplateVersionRepository.findOneByIdAndArtifactTemplateId(versionId,artifactId).orElseThrow {
            ApplicationObjectNotFoundException(versionId, ArtifactTemplateVersion::class.java.simpleName.toLowerCase())
        }
        return version.attributes
    }

    fun addAttributes(artifactId: Long, versionId: Long, attributesList : List<Long>) {
        val version = artifactTemplateVersionRepository.findOneByIdAndArtifactTemplateId(versionId,artifactId).orElseThrow {
            ApplicationObjectNotFoundException(versionId, ArtifactTemplateVersion::class.java.simpleName.toLowerCase())
        }

        if(version.designStatus == DesignStatus.Released)
            throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeAttributesInReleasedArtifactTemplateVersion)

        for(attributeId in attributesList) {
            val attribute = attributeTemplateVersionRepository.findById(attributeId).orElseThrow { ApplicationObjectNotFoundException(attributeId, AttributeTemplateVersion::class.java.simpleName.toLowerCase()) }
            if(attribute.designStatus != DesignStatus.Released)
                throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotAddNoneReleasedAttributeToArtifactTemplateVersion, Exception(attribute.attributeTemplate.identifier))

            val result = version.attributes.contains(attribute)
            if(result)
                throw ApplicationValidationException(ApplicationErrorCodes.ValidationAttributeAlreadyAddedToThisArtifactTemplateVersion)

            version.attributes.add(attribute)
        }

        artifactTemplateVersionRepository.save(version)
    }

    fun removeAttributes(artifactId: Long, versionId: Long, attributesList : List<Long>) {
        val version = artifactTemplateVersionRepository.findOneByIdAndArtifactTemplateId(versionId,artifactId).orElseThrow {
            ApplicationObjectNotFoundException(versionId, ArtifactTemplateVersion::class.java.simpleName.toLowerCase())
        }

        for(attributeId in attributesList) {
            val attribute = attributeTemplateVersionRepository.findById(attributeId).orElseThrow { ApplicationObjectNotFoundException(attributeId, AttributeTemplateVersion::class.java.simpleName.toLowerCase()) }

            val result = version.attributes.contains(attribute)
            if(!result)
                continue

            version.attributes.remove(attribute)
        }

        artifactTemplateVersionRepository.save(version)
    }

    // ================================================================================================================

//    fun migrateStores(source: ArtifactTemplateVersion, target: ArtifactTemplateVersion): MutableList<ArtifactStore> {
//        val versionsMap = mutableMapOf<Long, ArtifactTemplateVersion?>()
//        val managersMap = mutableMapOf<String, ArtifactTemplateDataTypeManager?>()
//        val sourceArtifactStores = artifactStoreRepository.findAllByArtifactTemplateVersionId(source.id)
//        val targetArtifactStores = mutableListOf<ArtifactStore>()
//
//        for(store in sourceArtifactStores) {
//            val targetArtifactStore = ArtifactStore(artifactTemplate = store.artifactTemplate, artifactTemplateVersion = target)
//            targetArtifactStore.solution = store.solution
//
//            for(attributeStore in store.attributeStores) {
//                var artifactTemplateVersion: ArtifactTemplateVersion?
//                if(versionsMap.containsKey(attributeStore.artifactTemplate.id)) {
//                    artifactTemplateVersion = versionsMap[attributeStore.artifactTemplate.id]
//                } else {
//                    artifactTemplateVersion = getArtifactTemplateVersion(attributeStore, target)
//                    versionsMap[attributeStore.artifactTemplate.id] = artifactTemplateVersion
//                }
//
//                if(artifactTemplateVersion != null) {
//                    var manager: ArtifactTemplateDataTypeManager?
//                    if(managersMap.containsKey(artifactTemplateVersion.typeUUID)) {
//                        manager = managersMap[artifactTemplateVersion.typeUUID]
//                    }
//                    else {
//                        manager = repositoryManagerService.getArtifactTemplateDataTypeManager(artifactTemplateVersion.typeUUID)
//                        managersMap[artifactTemplateVersion.typeUUID] = manager
//                    }
//
////                    val result = manager!!.migrateStoreContent(attributeStore.contentsJson, UUID.fromString(artifactTemplateVersion.typeUUID),
////                                                                    artifactTemplateVersion.propertiesJson)
////
////                    val targetAttributeStore = AttributeStore(contents = "", contentsJson = result,
////                                                    artifactTemplate = attributeStore.artifactTemplate,
////                                                    artifactTemplateVersion = artifactTemplateVersion)
////                    targetArtifactStore.attributeStores.add(targetAttributeStore)
//                }
//            }
//
//            store.storeStatus = StoreObjectStatus.Migrated
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
//        if(targetVersion.designStatus != DesignStatus.Released)
//            throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotMigrateStoreObjectsToNoneReleasedVersion)
//
//        val sourceVersion = artifactTemplateVersionRepository.findOneByIdAndArtifactTemplateId(versionId,artifactId).orElseThrow {
//            ApplicationObjectNotFoundException(versionId, ArtifactTemplateVersion::class.java.simpleName.toLowerCase())
//        }
//
//        return migrateStores(sourceVersion, targetVersion)
//    }
}