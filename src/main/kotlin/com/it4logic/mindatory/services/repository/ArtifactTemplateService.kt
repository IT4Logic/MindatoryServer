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

import com.it4logic.mindatory.api.plugins.AttributeTemplateDataTypeManager
import com.it4logic.mindatory.exceptions.ApplicationErrorCodes
import com.it4logic.mindatory.exceptions.ApplicationObjectNotFoundException
import com.it4logic.mindatory.exceptions.ApplicationValidationException
import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.model.common.DesignStatus
import com.it4logic.mindatory.model.common.StoreObjectStatus
import com.it4logic.mindatory.model.repository.*
import com.it4logic.mindatory.model.store.ArtifactStore
import com.it4logic.mindatory.model.store.ArtifactStoreRepository
import com.it4logic.mindatory.model.store.AttributeStore
import com.it4logic.mindatory.services.RepositoryManagerService
import com.it4logic.mindatory.services.common.ApplicationBaseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional


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

    override fun repository(): ApplicationBaseRepository<ArtifactTemplate> = artifactTemplateRepository

    override fun type(): Class<ArtifactTemplate> = ArtifactTemplate::class.java

    override fun beforeDelete(target: ArtifactTemplate) {
        var count = artifactStoreRepository.countByArtifactTemplateId(target.id)
        if (count > 0)
            throw ApplicationValidationException(ApplicationErrorCodes.ValidationArtifactTemplateHasRelatedStoreData)

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

    fun startNewDesignVersion(id: Long): ArtifactTemplateVersion {
        return startNewDesignVersion(findById(id))
    }

    fun startNewDesignVersion(target: ArtifactTemplate): ArtifactTemplateVersion {
        // check if we have a current in-design version
        val result = artifactTemplateVersionRepository.findOneByArtifactTemplateIdAndDesignStatus(
            target.id,
            DesignStatus.InDesign
        )
        if (result.isPresent)
            throw ApplicationValidationException(ApplicationErrorCodes.ValidationArtifactTemplateHasInDesignVersion)

        val max = artifactTemplateVersionRepository.maxDesignVersion(target.id)
        val newVersion = ArtifactTemplateVersion(artifactTemplate = target, designVersion = max + 1)
        target.versions.add(newVersion)

        update(target)

        return newVersion
    }

    fun releaseDesignVersion(id: Long): ArtifactTemplateVersion {
        return releaseDesignVersion(findById(id))
    }

    fun releaseDesignVersion(target: ArtifactTemplate): ArtifactTemplateVersion {
        val result = artifactTemplateVersionRepository.findOneByArtifactTemplateIdAndDesignStatus(
            target.id,
            DesignStatus.InDesign
        )
        if (!result.isPresent)
            throw ApplicationValidationException(ApplicationErrorCodes.ValidationArtifactTemplateHasNoInDesignVersion)
        val obj = result.get()
        obj.designStatus = DesignStatus.Released
        artifactTemplateVersionRepository.save(obj)
        refresh(target)
        return obj
    }

    // ================================================================================================================

    fun getAllAttributes(artifactId: Long, versionId: Long): List<AttributeTemplateVersion> {
        val version = artifactTemplateVersionRepository.findOneByIdAndArtifactTemplateId(versionId,artifactId).orElseThrow {
            ApplicationObjectNotFoundException(versionId, ArtifactTemplateVersion::class.java.simpleName.toLowerCase())
        }
        return version.attributes
    }

    fun addAttribute(artifactId: Long, versionId: Long, attributeTemplateVersion: AttributeTemplateVersion) {
        val version = artifactTemplateVersionRepository.findOneByIdAndArtifactTemplateId(versionId,artifactId).orElseThrow {
            ApplicationObjectNotFoundException(versionId, ArtifactTemplateVersion::class.java.simpleName.toLowerCase())
        }
        if(version.designStatus == DesignStatus.Released)
            throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeAttributesInReleasedArtifactTemplateVersion)

        if(attributeTemplateVersion.designStatus != DesignStatus.Released)
            throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotAddNoneReleasedAttributeToArtifactTemplateVersion)

        for(attribute in version.attributes) {
            if(attribute.id == attributeTemplateVersion.id)
                throw ApplicationValidationException(ApplicationErrorCodes.ValidationAttributeAlreadyAddedToThisArtifactTemplateVersion)
        }

        version.attributes.add(attributeTemplateVersion)
        artifactTemplateVersionRepository.save(version)
    }

    fun removeAttribute(artifactId: Long, versionId: Long, attributeVersionId: Long) {
        val version = artifactTemplateVersionRepository.findOneByIdAndArtifactTemplateId(versionId,artifactId).orElseThrow {
            ApplicationObjectNotFoundException(versionId, ArtifactTemplateVersion::class.java.simpleName.toLowerCase())
        }

        if(version.designStatus == DesignStatus.Released)
            throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeAttributesInReleasedArtifactTemplateVersion)

        for(attribute in version.attributes) {
            if(attribute.id == attributeVersionId)
                version.attributes.remove(attribute)
        }
        artifactTemplateVersionRepository.save(version)
    }

    // ================================================================================================================

    private fun getAttributeTemplateVersion(source: AttributeStore, target: ArtifactTemplateVersion): AttributeTemplateVersion? {
        for(attributeTemplateVersion in target.attributes) {
            if(attributeTemplateVersion.attributeTemplate.id == source.attributeTemplate.id)
                return attributeTemplateVersion
        }
        return null
    }

    fun migrateStores(source: ArtifactTemplateVersion, target: ArtifactTemplateVersion): MutableList<ArtifactStore> {
        val versionsMap = mutableMapOf<Long, AttributeTemplateVersion?>()
        val managersMap = mutableMapOf<String, AttributeTemplateDataTypeManager?>()
        val sourceArtifactStores = artifactStoreRepository.findAllByArtifactTemplateVersionId(source.id)
        val targetArtifactStores = mutableListOf<ArtifactStore>()

        for(store in sourceArtifactStores) {
            val targetArtifactStore = ArtifactStore(artifactTemplate = store.artifactTemplate, artifactTemplateVersion = target)
            targetArtifactStore.solution = store.solution

            for(attributeStore in store.attributeStores) {
                var attributeTemplateVersion: AttributeTemplateVersion?
                if(versionsMap.containsKey(attributeStore.attributeTemplate.id)) {
                    attributeTemplateVersion = versionsMap[attributeStore.attributeTemplate.id]
                } else {
                    attributeTemplateVersion = getAttributeTemplateVersion(attributeStore, target)
                    versionsMap[attributeStore.attributeTemplate.id] = attributeTemplateVersion
                }

                if(attributeTemplateVersion != null) {
                    var manager: AttributeTemplateDataTypeManager?
                    if(managersMap.containsKey(attributeTemplateVersion.typeUUID)) {
                        manager = managersMap[attributeTemplateVersion.typeUUID]
                    }
                    else {
                        manager = repositoryManagerService.getAttributeTemplateDataTypeManager(attributeTemplateVersion.typeUUID)
                        managersMap[attributeTemplateVersion.typeUUID] = manager
                    }

                    val result = manager!!.migrateStoreContent(attributeStore.contentsJson, UUID.fromString(attributeTemplateVersion.typeUUID),
                                                                    attributeTemplateVersion.propertiesJson)

                    val targetAttributeStore = AttributeStore(contents = "", contentsJson = result,
                                                    attributeTemplate = attributeStore.attributeTemplate,
                                                    attributeTemplateVersion = attributeTemplateVersion)
                    targetArtifactStore.attributeStores.add(targetAttributeStore)
                }
            }

            store.storeStatus = StoreObjectStatus.Migrated
            targetArtifactStores.add(targetArtifactStore)
        }

        artifactStoreRepository.saveAll(sourceArtifactStores)
        return artifactStoreRepository.saveAll(targetArtifactStores)
    }

    fun migrateStores(artifactId: Long, versionId: Long, targetVersionId: Long): MutableList<ArtifactStore> {
        val targetVersion = artifactTemplateVersionRepository.findOneByIdAndArtifactTemplateId(targetVersionId,artifactId).orElseThrow {
            ApplicationObjectNotFoundException(versionId, ArtifactTemplateVersion::class.java.simpleName.toLowerCase())
        }

        if(targetVersion.designStatus != DesignStatus.Released)
            throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotMigrateStoreObjectsToNoneReleasedVersion)

        val sourceVersion = artifactTemplateVersionRepository.findOneByIdAndArtifactTemplateId(versionId,artifactId).orElseThrow {
            ApplicationObjectNotFoundException(versionId, ArtifactTemplateVersion::class.java.simpleName.toLowerCase())
        }

        return migrateStores(sourceVersion, targetVersion)
    }
}