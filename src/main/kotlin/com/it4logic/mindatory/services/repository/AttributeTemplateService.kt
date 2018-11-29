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

import com.it4logic.mindatory.exceptions.ApplicationErrorCodes
import com.it4logic.mindatory.exceptions.ApplicationObjectNotFoundException
import com.it4logic.mindatory.exceptions.ApplicationValidationException
import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.model.common.DesignStatus
import com.it4logic.mindatory.model.repository.*
import com.it4logic.mindatory.model.store.AttributeStoreRepository
import com.it4logic.mindatory.services.RepositoryManagerService
import com.it4logic.mindatory.services.common.ApplicationBaseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional


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
    private lateinit var artifactTemplateRepository: ArtifactTemplateRepository

    @Autowired
    private lateinit var artifactTemplateVersionRepository: ArtifactTemplateVersionRepository

    override fun repository(): ApplicationBaseRepository<AttributeTemplate> = attributeTemplateRepository

    override fun type(): Class<AttributeTemplate> = AttributeTemplate::class.java

    // ================================================================================================================

    fun getAllDesignVersions(id: Long): List<AttributeTemplateVersion> {
        val obj = findById(id)
        return obj.versions
    }

    fun getDesignVersion(id: Long, versionId: Long): AttributeTemplateVersion {
        val obj = findById(id)
        for(version in obj.versions) {
            if(version.id == versionId)
                return version
        }
        throw ApplicationObjectNotFoundException(versionId, AttributeTemplateVersion::class.java.simpleName.toLowerCase())
    }

    fun createVersion(attributeTemplateId: Long, attributeTemplateVersion: AttributeTemplateVersion): AttributeTemplateVersion {
        return createVersion(findById(attributeTemplateId), attributeTemplateVersion)
    }

    fun createVersion(target: AttributeTemplate, attributeTemplateVersion: AttributeTemplateVersion): AttributeTemplateVersion {
        // check if we have a current in-design version
        val result = attributeTemplateVersionRepository.findOneByAttributeTemplateIdAndDesignStatus(
            target.id,
            DesignStatus.InDesign
        )

        if (result.isPresent)
            throw ApplicationValidationException(ApplicationErrorCodes.ValidationAttributeTemplateHasInDesignVersion)

        val dataTypeManager = repositoryManagerService.getAttributeTemplateDataTypeManager(attributeTemplateVersion.typeUUID)
        dataTypeManager.validateDataTypeProperties(UUID.fromString(attributeTemplateVersion.typeUUID), attributeTemplateVersion.propertiesJson)

        val max = attributeTemplateVersionRepository.maxDesignVersion(target.id)
        attributeTemplateVersion.designVersion = max + 1
        attributeTemplateVersion.designStatus = DesignStatus.InDesign
        target.versions.add(attributeTemplateVersion)

        update(target)

        return attributeTemplateVersion
    }

    fun updateVersion(attributeTemplateId: Long, attributeTemplateVersion: AttributeTemplateVersion): AttributeTemplateVersion {
        val result = attributeTemplateVersionRepository.findOneByIdAndAttributeTemplateId(attributeTemplateVersion.id, attributeTemplateId).orElseThrow {
            ApplicationObjectNotFoundException(attributeTemplateVersion.id, AttributeTemplateVersion::class.java.simpleName.toLowerCase())
        }

        if(result.designStatus == DesignStatus.Released)
            throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeReleasedAttributeTemplateVersion)

        val dataTypeManager = repositoryManagerService.getAttributeTemplateDataTypeManager(attributeTemplateVersion.typeUUID)
        dataTypeManager.validateDataTypeProperties(UUID.fromString(attributeTemplateVersion.typeUUID), attributeTemplateVersion.propertiesJson)

        return attributeTemplateVersionRepository.save(attributeTemplateVersion)
    }

    fun releaseVersion(attributeTemplateId: Long, attributeTemplateVersionId: Long): AttributeTemplateVersion {
        val result = attributeTemplateVersionRepository.findOneByIdAndAttributeTemplateId(attributeTemplateVersionId, attributeTemplateId).orElseThrow {
            ApplicationObjectNotFoundException(attributeTemplateVersionId, AttributeTemplateVersion::class.java.simpleName.toLowerCase())
        }
        return releaseVersion(attributeTemplateId, result)
    }

    fun releaseVersion(attributeTemplateId: Long, attributeTemplateVersion: AttributeTemplateVersion): AttributeTemplateVersion {
        val result = attributeTemplateVersionRepository.findOneByIdAndAttributeTemplateId(attributeTemplateVersion.id, attributeTemplateId).orElseThrow {
            ApplicationObjectNotFoundException(attributeTemplateVersion.id, AttributeTemplateVersion::class.java.simpleName.toLowerCase())
        }

        if(result.designStatus == DesignStatus.Released)
            throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeReleasedAttributeTemplateVersion)

        attributeTemplateVersion.designStatus = DesignStatus.Released

        return attributeTemplateVersionRepository.save(attributeTemplateVersion)
    }

    fun deleteVersion(attributeTemplateId: Long, attributeTemplateVersionId: Long) {
        val result = attributeTemplateVersionRepository.findOneByIdAndAttributeTemplateId(attributeTemplateVersionId, attributeTemplateId).orElseThrow {
            ApplicationObjectNotFoundException(attributeTemplateVersionId, AttributeTemplateVersion::class.java.simpleName.toLowerCase())
        }
        deleteVersion(attributeTemplateId, result)
    }

    fun deleteVersion(attributeTemplateId: Long, attributeTemplateVersion: AttributeTemplateVersion) {
        val result = attributeTemplateVersionRepository.findOneByIdAndAttributeTemplateId(attributeTemplateVersion.id, attributeTemplateId).orElseThrow {
            ApplicationObjectNotFoundException(attributeTemplateVersion.id, AttributeTemplateVersion::class.java.simpleName.toLowerCase())
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
}