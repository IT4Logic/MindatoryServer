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
 * Relation Template Data Service
 */
@Service
@Transactional
class RelationTemplateService : ApplicationBaseService<RelationTemplate>() {
	@Autowired
	private lateinit var relationTemplateRepository: RelationTemplateRepository

	@Autowired
	private lateinit var modelService: ModelService

	@Autowired
	private lateinit var modelVersionService: ModelVersionService

	@Autowired
	private lateinit var mlcRepository: RelationTemplateMLCRepository

	override fun repository(): ApplicationBaseRepository<RelationTemplate> = relationTemplateRepository

	override fun type(): Class<RelationTemplate> = RelationTemplate::class.java

	override fun multipleLanguageContentRepository(): RelationTemplateMLCRepository = mlcRepository

	override fun multipleLanguageContentType(): KClass<*> = RelationTemplateMultipleLanguageContent::class

	override fun beforeCreate(target: RelationTemplate) {
		// Check if the if Model Version is released or not, as released version cannot be modified
		if (target.modelVersion?.status != ModelVersionStatus.InDesign)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeObjectsWithinNoneInDesignModelVersion)

		target.identifier = UUID.randomUUID().toString()

		if(target.globalIdentifier.isBlank())
			target.globalIdentifier = UUID.randomUUID().toString()

	}

	override fun beforeUpdate(target: RelationTemplate) {
		// Check if the if Model Version is released or not, as released version cannot be modified
		if (target.modelVersion?.status != ModelVersionStatus.InDesign)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeObjectsWithinNoneInDesignModelVersion)

		val objTmp = findById(target.id)
		if (objTmp.identifier != target.identifier)
			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.ValidationIdentifierNotMatched)
	}

	override fun beforeDelete(target: RelationTemplate) {
		// Check if the if Model Version is released or not, as released version cannot be modified
		if (target.modelVersion?.status != ModelVersionStatus.InDesign)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeObjectsWithinNoneInDesignModelVersion)
	}

	/**
	 * Retrieves all the relation templates that the input artifact template Id has been used as a source or target
	 * @param artifactTemplateId Input Artifact Template Id
	 * @return Relation Templates list
	 */
	fun findAllRelationsRelatedToArtifact(artifactTemplateId: Long) : List<RelationTemplate> {
		val output = mutableListOf<RelationTemplate>()
		output.addAll(relationTemplateRepository.findAllBySourceArtifactId(artifactTemplateId))
		output.addAll(relationTemplateRepository.findAllByTargetArtifactId(artifactTemplateId))
		output.forEach {
			loadMLC(it)
		}
		return output
	}

	/**
	 * Deletes any relation templates that has the input artifact template Id as a source or target
	 * @param artifactId Input Artifact Template Id
	 */
	fun deleteAnyRelatedRelationsUsedWithArtifact(artifactId: Long) {
		var result = relationTemplateRepository.findAllBySourceArtifactId(artifactId)
		for (obj in result) {
			delete(obj)
		}

		result = relationTemplateRepository.findAllByTargetArtifactId(artifactId)
		for (obj in result) {
			delete(obj)
		}
	}

	/**
	 * Checks if the input Stereotype object is used with any relation templates
	 * @param target Input Stereotype object
	 * @return True if the Stereotype object is used, False otherwise
	 */
	fun isStereotypeUsedInRelationTemplates(target: Stereotype): Boolean {
		return relationTemplateRepository.countBySourceStereotypeId(target.id) > 0 ||
				relationTemplateRepository.countByTargetStereotypeId(target.id) > 0
	}
}