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
package com.it4logic.mindatory.services.mail

import com.it4logic.mindatory.exceptions.ApiError
import com.it4logic.mindatory.exceptions.ApplicationErrorCodes
import com.it4logic.mindatory.exceptions.ApplicationGeneralException
import com.it4logic.mindatory.exceptions.ExceptionHelper
import com.it4logic.mindatory.mlc.LanguageManager
import com.it4logic.mindatory.model.AppPreferences
import com.it4logic.mindatory.model.mail.MailTemplateTypeUUID
import com.it4logic.mindatory.model.security.SecurityUserToken
import com.it4logic.mindatory.services.AppPreferencesService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.mail.MailException
import org.springframework.stereotype.Service
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.web.context.WebApplicationContext
import org.thymeleaf.ITemplateEngine
import java.util.*
import javax.servlet.http.HttpServletRequest

/**
 * This class is responsible for handling mailing functionality
 */
@Service
class MailService {
	@Autowired
	private lateinit var appPreferencesService: AppPreferencesService

	@Autowired
	private lateinit var mailTemplateService: MailTemplateService

	@Autowired
	@Qualifier("appTemplateEngine")
	private lateinit var templateEngine: ITemplateEngine

	@Autowired
	private lateinit var applicationContext: WebApplicationContext

	@Autowired
	private lateinit var currentRequest: HttpServletRequest

	var mailSender: JavaMailSenderImpl = JavaMailSenderImpl()

	/**
	 * Sends a password reset mail
	 * @param securityUserToken The securityUserToken that will be used for resetting the password
	 * @param requesterRestPasswordUrl Rest Password Url
	 * @return The sent mail, or null in case of failure
	 */
	fun sendResetPasswordEmail(securityUserToken: SecurityUserToken, requesterRestPasswordUrl: String) {
		sendEmail(MailTemplateTypeUUID.ResetPassword, securityUserToken, requesterRestPasswordUrl)
	}

	/**
	 * Sends a welcome mail
	 * @param securityUserToken The securityUserToken that will be used for resetting the password
	 * @param requesterRestPasswordUrl Rest Password Url
	 * @return The sent mail, or null in case of failure
	 */
	fun sendWelcomeEmail(securityUserToken: SecurityUserToken, requesterRestPasswordUrl: String) {
		sendEmail(MailTemplateTypeUUID.Welcome, securityUserToken, requesterRestPasswordUrl)
	}

	/**
	 * Sends an email
	 * @param mailTemplateTypeUUID Email Template UUID
	 * @param securityUserToken The securityUserToken that will be used for resetting the password
	 * @param requesterRestPasswordUrl Rest Password Url
	 * @return The sent mail, or null in case of failure
	 */
	private fun sendEmail(mailTemplateTypeUUID: MailTemplateTypeUUID, securityUserToken: SecurityUserToken, requesterRestPasswordUrl: String) {
		val preferences = configMailSender(null)

		val template = mailTemplateService.getMailTemplate(mailTemplateTypeUUID)

		val resetPasswordLink = requesterRestPasswordUrl + securityUserToken.token

		val htmlTemplate = template.template.replace(MailTemplateVariables.ResetPasswordLink, resetPasswordLink)
			.replace(MailTemplateVariables.Username, securityUserToken.user.username)
			.replace(MailTemplateVariables.UserFullName, securityUserToken.user.fullName)

		try {
			val message = mailSender.createMimeMessage()
			val helper = MimeMessageHelper(message, false, "UTF-8")
			helper.setTo(securityUserToken.user.email)
			helper.setSubject(template.subject)
			helper.setFrom(preferences.defaultMailFrom)
			helper.setText(htmlTemplate, true)
			mailSender.send(message)
		} catch (ex: MailException) {
			val message = if (ex.cause == null) "" else ExceptionHelper.getRootCause(ex.cause!!)?.message
			val error = ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ApplicationErrorCodes.SendMailError, message ?: "")
			throw ApplicationGeneralException(error)
		}
	}

	/**
	 * Configures the mail service from Application Preferences
	 * @param appPreferences Application Preferences object, in case of Null the AppPreferences will be loaded
	 * @return AppPreferences instance
	 */
	protected fun configMailSender(appPreferences: AppPreferences?): AppPreferences {

		val preferences = appPreferences ?: appPreferencesService.findFirst()

		mailSender = JavaMailSenderImpl()
		mailSender.javaMailProperties = preferences.obtainSMTPServerProperties()
		mailSender.host = preferences.smtpServerUrl
		mailSender.port = preferences.smtpServerPort
		mailSender.username = preferences.smtpServerUsername
		mailSender.password = preferences.smtpServerPassword

		return preferences
	}
}