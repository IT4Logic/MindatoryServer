package com.it4logic.mindatory.languages

import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Component
import org.springframework.web.context.WebApplicationContext
import java.lang.Exception

@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
class LanguageManager {
//    companion object {
        private lateinit var languageRepository: LanguageRepository

        private var _currentLanguage: Language? = null

        var currentLanguage: Language
            get() {
                if(_currentLanguage != null)
                    return _currentLanguage as Language
                val result = languageRepository.findOneByDefault(true)
                if(result.isPresent)
                    return result.get()

                // todo
                throw Exception()
            }
            set(value) {
                _currentLanguage = value
            }

//        fun getCurrentLanguage() : Language {
//            if(_currentLanguage != null)
//                return _currentLanguage as Language
//            val result = languageRepository.findOneByDefault(true)
//            if(result.isPresent)
//                return result.get()
//
//            // todo
//            throw Exception()
//        }
//
//        fun setCurrentLanguage(lang: Language) {
//            _currentLanguage = lang
//        }
//    }


}