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

package com.it4logic.mindatory.controllers.security

import com.it4logic.mindatory.controllers.common.ApplicationControllerEntryPoints
import com.it4logic.mindatory.mlc.LanguageManager
import com.it4logic.mindatory.security.*
import com.it4logic.mindatory.services.LanguageService
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import com.it4logic.mindatory.services.mail.MailService
import com.it4logic.mindatory.services.security.SecurityUserService
import com.it4logic.mindatory.services.security.SecurityUserTokenService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestBody
import javax.validation.Valid
import org.springframework.web.bind.annotation.PostMapping


@CrossOrigin
@RestController
@RequestMapping(ApplicationControllerEntryPoints.Authentication)
class PasswordResetController {
	@Autowired
	lateinit var securityUserService: SecurityUserService
	@Autowired
	lateinit var securityUserTokenService: SecurityUserTokenService
	@Autowired
	lateinit var mailService: MailService
	@Autowired
	lateinit var languageManager: LanguageManager
	@Autowired
	lateinit var languageService: LanguageService

	@PostMapping("/reset-password")
	fun resetPassword(@Valid @RequestBody request: ResetPasswordRequest) {
		val user = securityUserService.findByUsername(request.username)
		val language = user.preferences?.contentLanguage ?: languageService.findLanguageByLocaleOrDefault(null)
		languageManager.currentLanguage = language
		val token = securityUserTokenService.createUserTokenForUser(user)
		mailService.sendResetPasswordEmail(token, request.requesterRestPasswordUrl)
	}

	@PostMapping("/process-reset-password")
	fun processResetPassword(@Valid @RequestBody request: ProcessResetPasswordRequest) {
		val token = securityUserTokenService.findToken(request.token)
		securityUserTokenService.validateToken(token)
		securityUserService.changeUserPassword(
			token.user,
			ChangePasswordRequest("", request.password, request.passwordConfirm),
			false
		)
		securityUserTokenService.delete(token)
	}
}
