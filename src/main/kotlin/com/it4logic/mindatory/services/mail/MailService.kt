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
	 * @return The sent mail, or null in case of failure
	 */
	fun sendResetPasswordEmail(securityUserToken: SecurityUserToken, requesterRestPasswordUrl: String) {
		val preferences = configMailSender(null)

		val template = mailTemplateService.getMailTemplate(MailTemplateTypeUUID.ResetPassword)

//		val resetPasswordLink = getServerBaseUrl() + ApplicationControllerEntryPoints.Security + languageManager.currentLanguage.locale + "/process-reset-password"
		val resetPasswordLink = requesterRestPasswordUrl + securityUserToken.token

//		val ctx = Context(Locale(languageManager.currentLanguage.locale))
		val htmlTemplate = template.template.replace(MailTemplateVariables.ResetPasswordLink, resetPasswordLink)
			.replace(MailTemplateVariables.Username, securityUserToken.user.username)
			.replace(MailTemplateVariables.UserFullName, securityUserToken.user.fullName)

//		ctx.setVariable(MailTemplateVariables.ResetPasswordLink, resetPasswordLink)
//		ctx.setVariable(MailTemplateVariables.Username, securityUserToken.user.username)
//		ctx.setVariable(MailTemplateVariables.UserFullName, securityUserToken.user.fullName)
//		val htmlTemplate = templateEngine.process(stringTemplate, ctx)
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

//	fun getServerBaseUrl(): String {
//		val scheme = "http"//connector.getScheme()
//		val ip = InetAddress.getLocalHost().hostName
//		val port = currentRequest.serverPort
//		val contextPath = applicationContext.servletContext?.contextPath
//		return "$scheme://$ip:$port$contextPath"
//	}
}