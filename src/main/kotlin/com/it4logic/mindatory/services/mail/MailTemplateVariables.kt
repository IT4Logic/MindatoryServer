package com.it4logic.mindatory.services.mail

class MailTemplateVariables {
	companion object {
		const val ResetPasswordLink = "$[resetPasswordLink]"
		const val Username = "$[username]"
		const val UserFullName = "$[userFullName]"
	}
}