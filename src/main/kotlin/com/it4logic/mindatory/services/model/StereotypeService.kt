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
 * Stereotype Data Service
 */
@Service
@Transactional
class StereotypeService : ApplicationBaseService<Stereotype>() {
	@Autowired
	private lateinit var stereotypeRepository: StereotypeRepository

	@Autowired
	private lateinit var relationTemplateService: RelationTemplateService

	@Autowired
	private lateinit var mlcRepository: StereotypeMLCRepository

	override fun repository(): ApplicationBaseRepository<Stereotype> = stereotypeRepository

	override fun type(): Class<Stereotype> = Stereotype::class.java

	override fun multipleLanguageContentRepository(): StereotypeMLCRepository = mlcRepository

	override fun multipleLanguageContentType(): KClass<*> = StereotypeMultipleLanguageContent::class

	@Suppress("UNCHECKED_CAST")
	override fun beforeCreate(target: Stereotype) {
		// Check if the if Model Version is released or not, as released version cannot be modified
		if (target.modelVersion.status != ModelVersionStatus.InDesign)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeObjectsWithinNoneInDesignModelVersion)

		target.identifier = UUID.randomUUID().toString()

		// validates if there are any duplicates, as this property should be unique and MLC in the same time
		val result = findAll(null, null, "modelVersion.id==" + target.modelVersion.id) as List<Stereotype>
		val obj = result.find { it.name == target.name }
		if (obj != null)
			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.DuplicateStereotypeName)
	}

	@Suppress("UNCHECKED_CAST")
	override fun beforeUpdate(target: Stereotype) {
		// Check if the if Model Version is released or not, as released version cannot be modified
		if (target.modelVersion.status != ModelVersionStatus.InDesign)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeObjectsWithinNoneInDesignModelVersion)

		val objTmp = findById(target.id)
		if (objTmp.identifier != target.identifier)
			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.ValidationIdentifierNotMatched)

		// validates if there are any duplicates, as this property should be unique and MLC in the same time
		val result = findAll(null, null, "modelVersion.id==" + target.modelVersion.id) as List<Stereotype>
		val obj = result.find { it.name == target.name && it.identifier != target.identifier }
		if (obj != null)
			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.DuplicateStereotypeName)
	}

	override fun beforeDelete(target: Stereotype) {
		// Check if the if Model Version is released or not, as released version cannot be modified
		if (target.modelVersion.status != ModelVersionStatus.InDesign)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotChangeObjectsWithinNoneInDesignModelVersion)

		if (relationTemplateService.isStereotypeUsedInRelationTemplates(target))
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationStereotypesUsedInRelationTemplates)
	}
}