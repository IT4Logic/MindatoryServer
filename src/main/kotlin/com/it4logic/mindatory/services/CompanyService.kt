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

package com.it4logic.mindatory.services

import com.it4logic.mindatory.exceptions.ApplicationDataIntegrityViolationException
import com.it4logic.mindatory.exceptions.ApplicationErrorCodes
import com.it4logic.mindatory.mlc.LanguageManager
import com.it4logic.mindatory.model.*
import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.services.common.ApplicationBaseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.transaction.Transactional
import kotlin.reflect.KClass

/**
 * Company Data Service
 */
@Service
@Transactional
class CompanyService : ApplicationBaseService<Company>() {
	@Autowired
	private lateinit var companyRepository: CompanyRepository

	@Autowired
	protected lateinit var languageManager: LanguageManager

	@Autowired
	private lateinit var mlcRepository: CompanyMLCRepository

	override fun repository(): ApplicationBaseRepository<Company> = companyRepository

	override fun type(): Class<Company> = Company::class.java

	override fun multipleLanguageContentRepository(): CompanyMLCRepository = mlcRepository

	override fun multipleLanguageContentType(): KClass<*> = CompanyMultipleLanguageContent::class

	/**
	 * Finds and loads the first instance of Companies, because only once instance is allowed
	 * @return Company instance
	 */
	fun findFirst(): Company {
		val obj = repository().findAll()[0]
		mlcService().load(obj)
		return obj
	}

	override fun beforeCreate(target: Company) {
		// validates if there are any duplicates, as this property should be unique and MLC in the same time
		val result = target.findAllByLanguageIdAndFieldName(languageManager.currentLanguage.id, "name")
		val obj = result.find { it.contents == target.name }
		if (obj != null) {
			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.DuplicateCompanyName)
		}
	}

	override fun beforeUpdate(target: Company) {
		// validates if there are any duplicates, as this property should be unique and MLC in the same time
		val result = mlcRepository.findAllByLanguageIdAndFieldNameAndParentIdNot(
			languageManager.currentLanguage.id,
			"name",
			target.id
		)
		val obj = result.find { it.contents == target.name }
		if (obj != null) {
			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.DuplicateCompanyName)
		}
	}
}