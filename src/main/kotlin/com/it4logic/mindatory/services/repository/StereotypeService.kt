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

import com.it4logic.mindatory.exceptions.ApplicationDataIntegrityViolationException
import com.it4logic.mindatory.exceptions.ApplicationErrorCodes
import com.it4logic.mindatory.exceptions.ApplicationValidationException
import com.it4logic.mindatory.mlc.LanguageManager
import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.model.repository.*
import com.it4logic.mindatory.services.common.ApplicationBaseService
import com.it4logic.mindatory.services.security.SecurityAclService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.transaction.Transactional
import kotlin.reflect.KClass


@Service
@Transactional
class StereotypeService : ApplicationBaseService<Stereotype>() {
	@Autowired
	private lateinit var stereotypeRepository: StereotypeRepository

	@Autowired
	private lateinit var joinTemplateRepository: JoinTemplateRepository

	@Autowired
	private lateinit var joinTemplateVersionRepository: JoinTemplateVersionRepository

	@Autowired
	protected lateinit var securityAclService: SecurityAclService

	@Autowired
	private lateinit var mlcRepository: StereotypeMLCRepository

	@Autowired
	protected lateinit var languageManager: LanguageManager

	override fun repository(): ApplicationBaseRepository<Stereotype> = stereotypeRepository

	override fun type(): Class<Stereotype> = Stereotype::class.java

	override fun useAcl(): Boolean = false

	override fun securityAclService(): SecurityAclService? = securityAclService

	override fun multipleLanguageContentRepository(): StereotypeMLCRepository = mlcRepository

	override fun multipleLanguageContentType(): KClass<*> = StereotypeMultipleLanguageContent::class

	override fun beforeCreate(target: Stereotype) {
		//    val result = mlcRepository.findAllByLanguageIdAndFieldNameAndContents(languageManager.currentLanguage.id, "name", target.name)
		val result = mlcRepository.findAllByLanguageIdAndFieldName(languageManager.currentLanguage.id, "name")
		val obj = result.find { it.contents == target.name }
		//if(result.isNotEmpty()) {
		if (obj != null) {
			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.DuplicateStereotypeName)
		}
	}

	override fun beforeUpdate(target: Stereotype) {
		//        val result = mlcRepository.findAllByLanguageIdAndFieldNameAndContentsAndParentNot(languageManager.currentLanguage.id, "name", target.name, target.id)
		val result = mlcRepository.findAllByLanguageIdAndFieldNameAndParentNot(
			languageManager.currentLanguage.id,
			"name",
			target.id
		)
		val obj = result.find { it.contents == target.name }
		//if(result.isNotEmpty()) {
		if (obj != null) {
			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.DuplicateStereotypeName)
		}
	}

	override fun beforeDelete(target: Stereotype) {
		var count = joinTemplateVersionRepository.countBySourceStereotypeId(target.id)
		if (count > 0)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationStereotypesUsedInJoinTemplates)

		count = joinTemplateVersionRepository.countByTargetStereotypeId(target.id)
		if (count > 0)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationStereotypesUsedInJoinTemplates)
	}
}