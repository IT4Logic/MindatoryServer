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

import com.it4logic.mindatory.mlc.LanguageManager
import com.it4logic.mindatory.model.*
import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.services.common.ApplicationBaseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.transaction.Transactional


@Service
@Transactional
class AppPreferencesService : ApplicationBaseService<AppPreferences>() {
	@Autowired
	private lateinit var companyRepository: AppPreferencesRepository

	@Autowired
	protected lateinit var languageManager: LanguageManager

	override fun repository(): ApplicationBaseRepository<AppPreferences> = companyRepository

	override fun type(): Class<AppPreferences> = AppPreferences::class.java

	fun findFirst(): AppPreferences {
		val obj = repository().findAll()[0]
		mlcService().load(obj)
		return obj
	}
}