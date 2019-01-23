/*
    Copyright (c) 2017, IT4Logic.

    This file is part of Mindatory Solution by IT4Logic.

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

//import com.it4logic.mindatory.exceptions.ApplicationErrorCodes
//import com.it4logic.mindatory.exceptions.ApplicationValidationException
import com.it4logic.mindatory.languages.Language
import com.it4logic.mindatory.languages.LanguageRepository
import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.services.common.ApplicationBaseService
import com.it4logic.mindatory.services.security.SecurityAclService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service


@Service
class LanguageService : ApplicationBaseService<Language>() {
  @Autowired
  private lateinit var languageRepository: LanguageRepository

  @Autowired
  protected lateinit var securityAclService: SecurityAclService

  override fun repository(): ApplicationBaseRepository<Language> = languageRepository

  override fun type(): Class<Language> = Language::class.java

  override fun useAcl() : Boolean = true

  override fun securityAclService() : SecurityAclService? = securityAclService

  override fun beforeDelete(target: Language) {
    // todo
    // check if the language is default
    // check if the language is used
//    val count = repoRepository.countByLanguageId(target.id)
//    if(count > 0)
//      throw ApplicationValidationException(ApplicationErrorCodes.ValidationLanguageHasRepository)
  }
}