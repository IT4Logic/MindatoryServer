/*
    Copyright (c) 2019, IT4Logic.

    This file is part of Mindatory Project by IT4Logic.

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

package com.it4logic.mindatory.services

import com.it4logic.mindatory.exceptions.ApplicationErrorCodes
import com.it4logic.mindatory.exceptions.ApplicationObjectNotFoundException
import com.it4logic.mindatory.exceptions.ApplicationValidationException
import com.it4logic.mindatory.model.CompanyMLCRepository
import com.it4logic.mindatory.model.project.ProjectMLCRepository
import com.it4logic.mindatory.model.mlc.Language
import com.it4logic.mindatory.model.mlc.LanguageRepository
import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.model.mail.MailTemplateMLCRepository
import com.it4logic.mindatory.model.model.*
import com.it4logic.mindatory.model.project.AttributeStoreMLCRepository
import com.it4logic.mindatory.model.security.SecurityGroupMLCRepository
import com.it4logic.mindatory.model.security.SecurityRoleMLCRepository
import com.it4logic.mindatory.model.security.SecurityUserMLCRepository
import com.it4logic.mindatory.services.common.ApplicationBaseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.transaction.Transactional

/**
 * Language Data Service
 */
@Service
@Transactional
class LanguageService : ApplicationBaseService<Language>() {
	@Autowired
	private lateinit var languageRepository: LanguageRepository

	@Autowired
	protected lateinit var companyMLCRepository: CompanyMLCRepository
	@Autowired
	protected lateinit var mailTemplateMLCRepository: MailTemplateMLCRepository
	@Autowired
	protected lateinit var securityUserMLCRepository: SecurityUserMLCRepository
	@Autowired
	protected lateinit var securityRoleMLCRepository: SecurityRoleMLCRepository
	@Autowired
	protected lateinit var securityGroupMLCRepository: SecurityGroupMLCRepository
	@Autowired
	protected lateinit var modelMLCRepository: ModelMLCRepository
	@Autowired
	protected lateinit var relationTemplateMLCRepository: RelationTemplateMLCRepository
	@Autowired
	protected lateinit var stereotypeMLCRepository: StereotypeMLCRepository
	@Autowired
	protected lateinit var attributeTemplateMLCRepository: AttributeTemplateMLCRepository
	@Autowired
	protected lateinit var artifactTemplateMLCRepository: ArtifactTemplateMLCRepository
	@Autowired
	protected lateinit var projectMLCRepository: ProjectMLCRepository
	@Autowired
	protected lateinit var attributeStoreMLCRepository: AttributeStoreMLCRepository


	override fun repository(): ApplicationBaseRepository<Language> = languageRepository

	override fun type(): Class<Language> = Language::class.java

	override fun beforeCreate(target: Language) {
		changeDefault(target)
	}

	override fun beforeUpdate(target: Language) {
		changeDefault(target)
	}

	override fun beforeDelete(target: Language) {
		if (target.default)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotDeleteDefaultLanguage)

		val count = repository().count()
		if (count <= 1)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationAtLeastOneLanguageInSystem)

		deleteRelatedContents(target.id)
	}

	/**
	 * Change the default language
	 * @param target Language object
	 */
	fun changeDefault(target: Language) {
		if (!target.default)
			return
		val languages = languageRepository.findAllByDefault(true)
		languages.forEach { it.default = false }
		languageRepository.saveAll(languages)
	}

	/**
	 * Finds language according to the given locale, or load the default language, otherwise a suitable exception will be raised
	 * @param locale Language locale
	 * @return Language object according to the given locale, or the default language object
	 */
	fun findLanguageByLocaleOrDefault(locale: String?): Language {
		return if (locale == null)
			languageRepository.findOneByDefault(true)
				.orElseThrow { ApplicationObjectNotFoundException(-1, ApplicationErrorCodes.NotFoundDefaultLanguage) }
		else {
			languageRepository.findOneByLocale(locale).orElseGet {
				languageRepository.findOneByDefault(true).orElseThrow {
					ApplicationObjectNotFoundException(
						-1,
						ApplicationErrorCodes.NotFoundDefaultLanguage
					)
				}
			}
		}
	}

	/**
	 * Delete all related language contents (usage)
	 * @param id Language Id
	 */
	fun deleteRelatedContents(id: Long) {
		companyMLCRepository.deleteAllByLanguageId(id)
		mailTemplateMLCRepository.deleteAllByLanguageId(id)
		securityUserMLCRepository.deleteAllByLanguageId(id)
		securityRoleMLCRepository.deleteAllByLanguageId(id)
		securityGroupMLCRepository.deleteAllByLanguageId(id)
		modelMLCRepository.deleteAllByLanguageId(id)
		relationTemplateMLCRepository.deleteAllByLanguageId(id)
		stereotypeMLCRepository.deleteAllByLanguageId(id)
		attributeTemplateMLCRepository.deleteAllByLanguageId(id)
		artifactTemplateMLCRepository.deleteAllByLanguageId(id)
		projectMLCRepository.deleteAllByLanguageId(id)
		attributeStoreMLCRepository.deleteAllByLanguageId(id)
	}

	/**
	 * Makes the langage for the input Id the default language and clear the current default language mark
	 * @param id Input Language Id
	 */
	fun makeLanguageDefault(id: Long): Boolean {
		val lang = findById(id)
		if (lang.default)
			return true
		val languages = languageRepository.findAllByDefault(true)
		languages.forEach { it.default = false }
		languageRepository.saveAll(languages)
		lang.default = true
		languageRepository.save(lang)
		return true
	}
}