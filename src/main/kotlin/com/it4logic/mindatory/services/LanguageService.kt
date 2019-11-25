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
import com.it4logic.mindatory.model.model.ModelMLCRepository
import com.it4logic.mindatory.model.CompanyMLCRepository
import com.it4logic.mindatory.model.project.ProjectMLCRepository
import com.it4logic.mindatory.model.mlc.Language
import com.it4logic.mindatory.model.mlc.LanguageRepository
import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.services.common.ApplicationBaseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
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
	protected lateinit var projectMLCRepository: ProjectMLCRepository
	@Autowired
	protected lateinit var applicationRepositoryMLCRepository: ModelMLCRepository
	@Autowired
	protected lateinit var companyMLCRepository: CompanyMLCRepository

	override fun repository(): ApplicationBaseRepository<Language> = languageRepository

	override fun type(): Class<Language> = Language::class.java

	override fun beforeCreate(target: Language) {
		changeDefault(target)
	}

	override fun beforeUpdate(target: Language) {
		changeDefault(target)
	}

	override fun beforeDelete(target: Language) {
		checkForDelete(target.id)
		checkForUsages(target.id)
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
		if (locale == null)
			return languageRepository.findOneByDefault(true)
				.orElseThrow { ApplicationObjectNotFoundException(-1, ApplicationErrorCodes.NotFoundDefaultLanguage) }
		else {
			return languageRepository.findOneByLocale(locale).orElseGet {
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
	 * Deletes language and its all related translations from all objects
	 * @param target Object instance
	 */
	fun forceDelete(target: Language) {
		if (useAcl() && SecurityContextHolder.getContext().authentication != null) {
			securityAclService()?.deleteAcl(target)
		}
		checkForDelete(target.id)
		deleteRelatedContents(target.id)
		repository().delete(target)
		repository().flush()
	}

	/**
	 * Check if we can delete this language, if no a suitable exception will be raised
	 * @param id Language Id
	 */
	private fun checkForDelete(id: Long) {
		val obj = findById(id)
		if (obj.default)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotDeleteDefaultLanguage)

		val count = repository().count()
		if (count <= 1)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationAtLeastOneLanguageInSystem)
	}

	/**
	 * Check if the language has any usage (i.e. related contents), if yes a suitable exception will be raised
	 * @param id Language Id
	 */
	private fun checkForUsages(id: Long) {
		var count = projectMLCRepository.countByLanguageId(id)
		if (count > 0)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationLanguageHasRelatedContents)

		count = applicationRepositoryMLCRepository.countByLanguageId(id)
		if (count > 0)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationLanguageHasRelatedContents)
	}

	/**
	 * Delete all related language contents (usage)
	 * @param id Language Id
	 */
	fun deleteRelatedContents(id: Long) {
		applicationRepositoryMLCRepository.deleteAllByLanguageId(id)
		projectMLCRepository.deleteAllByLanguageId(id)
		companyMLCRepository.deleteAllByLanguageId(id)
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