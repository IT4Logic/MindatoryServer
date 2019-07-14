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

package com.it4logic.mindatory.mlc

import com.it4logic.mindatory.exceptions.ApplicationErrorCodes
import com.it4logic.mindatory.exceptions.ApplicationObjectNotFoundException
import com.it4logic.mindatory.model.mlc.Language
import com.it4logic.mindatory.model.mlc.LanguageRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Component
import org.springframework.web.context.WebApplicationContext


/**
 * Language Manager
 */
@Component
//@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
class LanguageManager {
    @Autowired
    private lateinit var languageRepository: LanguageRepository

    private var _currentLanguage: Language? = null

    var currentLanguage: Language
        get() {
            if(_currentLanguage != null)
                return _currentLanguage as Language
            return defaultLanguage
        }
        set(value) {
            _currentLanguage = value
        }

    val defaultLanguage: Language
        get() {
            val result = languageRepository.findOneByDefault(true)
            if(result.isPresent)
                return result.get()
            throw ApplicationObjectNotFoundException(-1, ApplicationErrorCodes.NotFoundDefaultLanguage)
        }
}