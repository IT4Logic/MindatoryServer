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

import com.it4logic.mindatory.exceptions.ApplicationErrorCodes
import com.it4logic.mindatory.exceptions.ApplicationObjectNotFoundException
import com.it4logic.mindatory.exceptions.ApplicationValidationException
import com.it4logic.mindatory.mlc.LanguageManager
import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.model.common.DesignStatus
import com.it4logic.mindatory.model.common.StoreObjectStatus
import com.it4logic.mindatory.model.repository.*
import com.it4logic.mindatory.model.store.JoinStore
import com.it4logic.mindatory.model.store.JoinStoreRepository
import com.it4logic.mindatory.services.common.ApplicationBaseService
import com.it4logic.mindatory.services.security.SecurityAclService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.transaction.Transactional
import kotlin.reflect.KClass

@Service
@Transactional
class JoinTemplateService : ApplicationBaseService<JoinTemplate>() {
	@Autowired
	private lateinit var joinTemplateRepository: JoinTemplateRepository

	@Autowired
	private lateinit var joinTemplateVersionRepository: JoinTemplateVersionRepository

	@Autowired
	private lateinit var joinStoreRepository: JoinStoreRepository

	@Autowired
	protected lateinit var securityAclService: SecurityAclService

	@Autowired
	private lateinit var mlcRepository: JoinTemplateMLCRepository

	@Autowired
	protected lateinit var languageManager: LanguageManager

	override fun repository(): ApplicationBaseRepository<JoinTemplate> = joinTemplateRepository

	override fun type(): Class<JoinTemplate> = JoinTemplate::class.java

	override fun useAcl(): Boolean = false

	override fun securityAclService(): SecurityAclService? = securityAclService

	override fun multipleLanguageContentRepository(): JoinTemplateMLCRepository = mlcRepository

	override fun multipleLanguageContentType(): KClass<*> = JoinTemplateMultipleLanguageContent::class

	override fun beforeDelete(target: JoinTemplate) {
		val count = joinStoreRepository.countByJoinTemplateVersionId(target.id)
		if (count > 0)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationJoinTemplateHasRelatedStoreData)
	}

	// ================================================================================================================

	fun getAllDesignVersions(id: Long): List<JoinTemplateVersion> {
		val obj = findById(id)
		for (ver in obj.versions)
			multipleLanguageContentService.load(ver)
		return obj.versions
	}

	fun getDesignVersion(id: Long, versionId: Long): JoinTemplateVersion {
		val obj = findById(id)
		for (version in obj.versions) {
			if (version.id == versionId) {
				multipleLanguageContentService.load(version)
				return version
			}
		}
		throw ApplicationObjectNotFoundException(versionId, JoinTemplateVersion::class.java.simpleName.toLowerCase())
	}

	fun createVersion(joinTemplateId: Long, joinTemplateVersion: JoinTemplateVersion): JoinTemplateVersion {
		return createVersion(findById(joinTemplateId), joinTemplateVersion)
	}

	fun createVersion(target: JoinTemplate, joinTemplateVersion: JoinTemplateVersion): JoinTemplateVersion {
		// check if we have a current in-design version
		val result = joinTemplateVersionRepository.findOneByJoinTemplateIdAndDesignStatus(
			target.id,
			DesignStatus.InDesign
		)

		if (result.isPresent)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationJoinTemplateHasInDesignVersion)

		joinTemplateVersion.joinTemplate = target
		joinTemplateVersion.repository = target.repository
		joinTemplateVersion.solution = target.solution

		val max = joinTemplateVersionRepository.maxDesignVersion(target.id)
		joinTemplateVersion.designVersion = max + 1
		joinTemplateVersion.designStatus = DesignStatus.InDesign
		val ver = joinTemplateVersionRepository.save(joinTemplateVersion)
		repository().flush()
		entityManager.refresh(ver)

		if (target.versions == null)
			target.versions = mutableListOf()

		target.versions.add(ver)
		update(target)

		multipleLanguageContentService.load(ver)

		return ver
	}

	fun updateVersion(joinTemplateId: Long, joinTemplateVersion: JoinTemplateVersion): JoinTemplateVersion {
		val result = joinTemplateVersionRepository.findOneByIdAndJoinTemplateId(joinTemplateVersion.id, joinTemplateId)
			.orElseThrow {
				ApplicationObjectNotFoundException(
					joinTemplateVersion.id,
					JoinTemplateVersion::class.java.simpleName.toLowerCase()
				)
			}

		if (result.designStatus == DesignStatus.Released)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeReleasedJoinTemplateVersion)

		val target = findById(joinTemplateId)
		joinTemplateVersion.joinTemplate = target
		joinTemplateVersion.repository = target.repository
		joinTemplateVersion.solution = target.solution
		val ver = joinTemplateVersionRepository.save(joinTemplateVersion)
		repository().flush()
		entityManager.refresh(ver)

		multipleLanguageContentService.load(ver)

		return ver
	}

	fun releaseVersion(joinTemplateId: Long, joinTemplateVersionId: Long): JoinTemplateVersion {
		val result = joinTemplateVersionRepository.findOneByIdAndJoinTemplateId(joinTemplateVersionId, joinTemplateId)
			.orElseThrow {
				ApplicationObjectNotFoundException(
					joinTemplateVersionId,
					JoinTemplateVersion::class.java.simpleName.toLowerCase()
				)
			}
		return releaseVersion(joinTemplateId, result)
	}

	fun releaseVersion(joinTemplateId: Long, joinTemplateVersion: JoinTemplateVersion): JoinTemplateVersion {
		val result = joinTemplateVersionRepository.findOneByIdAndJoinTemplateId(joinTemplateVersion.id, joinTemplateId)
			.orElseThrow {
				ApplicationObjectNotFoundException(
					joinTemplateVersion.id,
					JoinTemplateVersion::class.java.simpleName.toLowerCase()
				)
			}

		if (result.designStatus == DesignStatus.Released)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeReleasedJoinTemplateVersion)

		joinTemplateVersion.designStatus = DesignStatus.Released

		val ver = joinTemplateVersionRepository.save(joinTemplateVersion)
		repository().flush()
		entityManager.refresh(ver)

		multipleLanguageContentService.load(ver)

		return ver
	}

	fun deleteVersion(joinTemplateId: Long, joinTemplateVersionId: Long) {
		val result = joinTemplateVersionRepository.findOneByIdAndJoinTemplateId(joinTemplateVersionId, joinTemplateId)
			.orElseThrow {
				ApplicationObjectNotFoundException(
					joinTemplateVersionId,
					JoinTemplateVersion::class.java.simpleName.toLowerCase()
				)
			}
		deleteVersion(joinTemplateId, result)
	}

	fun deleteVersion(joinTemplateId: Long, joinTemplateVersion: JoinTemplateVersion) {
		val result = joinTemplateVersionRepository.findOneByIdAndJoinTemplateId(joinTemplateVersion.id, joinTemplateId)
			.orElseThrow {
				ApplicationObjectNotFoundException(
					joinTemplateVersion.id,
					JoinTemplateVersion::class.java.simpleName.toLowerCase()
				)
			}

		val count = joinStoreRepository.countByJoinTemplateVersionId(joinTemplateVersion.id)
		if (count > 0)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationJoinTemplateVersionHasRelatedStoreData)

		joinTemplateVersionRepository.delete(result)
	}

	// ================================================================================================================

	fun isArtifactExists(
		targetArtifactVersion: ArtifactTemplateVersion,
		lookupList: List<ArtifactTemplateVersion>
	): Boolean {
		for (obj in lookupList) {
			if (targetArtifactVersion.id == obj.id)
				return true
		}
		return false
	}

	fun migrateStores(sourceVersion: JoinTemplateVersion, targetVersion: JoinTemplateVersion): MutableList<JoinStore> {
		for (sourceArtifact in sourceVersion.sourceArtifacts) {
			if (!isArtifactExists(sourceArtifact, targetVersion.sourceArtifacts)) {
				val count = joinStoreRepository.countByJoinTemplateVersionIdAndSourceArtifacts_Id(
					sourceVersion.id,
					sourceArtifact.id
				)
				if (count > 0)
					throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotMigrateJoinStoresDueToRemovedSourceArtifactWithRelativeData)
			}
		}

		for (targetArtifact in sourceVersion.targetArtifacts) {
			if (!isArtifactExists(targetArtifact, targetVersion.targetArtifacts)) {
				val count = joinStoreRepository.countByJoinTemplateVersionIdAndTargetArtifacts_Id(
					sourceVersion.id,
					targetArtifact.id
				)
				if (count > 0)
					throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotMigrateJoinStoresDueToRemovedTargetArtifactWithRelativeData)
			}
		}

		val targetStores = mutableListOf<JoinStore>()
		val sourceStores = joinStoreRepository.findAllByJoinTemplateVersionId(sourceVersion.id)
		for (sourceStore in sourceStores) {
			val targetStore = JoinStore(
				sourceStore.sourceArtifacts,
				sourceStore.targetArtifacts,
				sourceStore.joinTemplate,
				targetVersion,
				solution = sourceStore.solution
			)
			targetStores.add(targetStore)
			sourceStore.storeStatus = StoreObjectStatus.Migrated
		}

		joinStoreRepository.saveAll(sourceStores)
		joinStoreRepository.saveAll(targetStores)

		return targetStores
	}

	fun migrateStores(joinTemplateId: Long, sourceVersionId: Long, targetVersionId: Long): MutableList<JoinStore> {
		val targetVersion =
			joinTemplateVersionRepository.findOneByIdAndJoinTemplateId(targetVersionId, joinTemplateId).orElseThrow {
				ApplicationObjectNotFoundException(
					targetVersionId,
					JoinTemplateVersion::class.java.simpleName.toLowerCase()
				)
			}

		if (targetVersion.designStatus != DesignStatus.Released)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotMigrateStoreObjectsToNoneReleasedVersion)

		val sourceVersion =
			joinTemplateVersionRepository.findOneByIdAndJoinTemplateId(sourceVersionId, joinTemplateId).orElseThrow {
				ApplicationObjectNotFoundException(
					sourceVersionId,
					JoinTemplateVersion::class.java.simpleName.toLowerCase()
				)
			}

		return migrateStores(sourceVersion, targetVersion)
	}
}