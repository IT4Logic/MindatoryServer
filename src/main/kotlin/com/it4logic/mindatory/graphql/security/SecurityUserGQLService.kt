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
package com.it4logic.mindatory.graphql.security

import com.it4logic.mindatory.graphql.GQLBaseService
import com.it4logic.mindatory.model.security.SecurityUser
import com.it4logic.mindatory.security.ApplicationSecurityPermissions
import com.it4logic.mindatory.security.ChangePasswordRequest
import com.it4logic.mindatory.security.ProcessResetPasswordRequest
import com.it4logic.mindatory.security.ResetPasswordRequest
import com.it4logic.mindatory.services.common.ApplicationBaseService
import com.it4logic.mindatory.services.mail.MailService
import com.it4logic.mindatory.services.security.SecurityUserService
import com.it4logic.mindatory.services.security.SecurityUserTokenService
import io.leangen.graphql.annotations.GraphQLMutation
import io.leangen.graphql.annotations.GraphQLQuery
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

/**
 * GraphQL service for Security User
 */
@Service
@GraphQLApi
class SecurityUserGQLService : GQLBaseService<SecurityUser>() {
	@Autowired
	lateinit var securityUserService: SecurityUserService

	@Autowired
	private lateinit var securityUserTokenService: SecurityUserTokenService

	@Autowired
	private lateinit var mailService: MailService

	override fun service(): ApplicationBaseService<SecurityUser> = securityUserService

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.SecurityUserAdminView}', '${ApplicationSecurityPermissions.SecurityUserAdminCreate}', '${ApplicationSecurityPermissions.SecurityUserAdminModify}', '${ApplicationSecurityPermissions.SecurityUserAdminDelete}')")
	@GraphQLQuery(name = "securityUsersPageable")
	override fun findAll(locale: String?, page: Int, size: Int, sort: String?, filter: String?): Page<SecurityUser> {
		return super.findAll(locale, page, size, sort, filter)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.SecurityUserAdminView}', '${ApplicationSecurityPermissions.SecurityUserAdminCreate}', '${ApplicationSecurityPermissions.SecurityUserAdminModify}', '${ApplicationSecurityPermissions.SecurityUserAdminDelete}')")
	@GraphQLQuery(name = "securityUsers")
	override fun findAll(locale: String?, sort: String?, filter: String?): List<SecurityUser> {
		return super.findAll(locale, sort, filter)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.SecurityUserAdminView}', '${ApplicationSecurityPermissions.SecurityUserAdminCreate}', '${ApplicationSecurityPermissions.SecurityUserAdminModify}', '${ApplicationSecurityPermissions.SecurityUserAdminDelete}')")
	@GraphQLQuery(name = "securityUser")
	override fun find(locale: String?, id: Long?, filter: String?): SecurityUser? {
		return super.find(locale, id, filter)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.SecurityUserAdminCreate}')")
	@GraphQLMutation(name = "createSecurityUser")
	override fun create(locale: String?, target: SecurityUser): SecurityUser {
		return super.create(locale, target)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.SecurityUserAdminModify}')")
	@GraphQLMutation(name = "updateSecurityUser")
	override fun update(locale: String?, target: SecurityUser): SecurityUser {
		return super.update(locale, target)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.SecurityUserAdminDelete}')")
	@GraphQLMutation(name = "deleteSecurityUser")
	override fun delete(locale: String?, id: Long): Boolean {
		return super.delete(locale, id)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.SecurityUserAdminDelete}')")
	@GraphQLMutation(name = "deleteSecurityUser")
	override fun delete(locale: String?, target: SecurityUser): Boolean {
		return super.delete(locale, target)
	}

	/**
	 * Changes user password
	 * @param locale Input locale
	 * @param id Input User Id
	 * @param request Change Password Request object
	 * @return True if the password successfully changed, otherwise an excption will be thrown
	 */
	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.SecurityUserAdminModify}')")
	@GraphQLMutation
	fun changeUserPassword(
		locale: String, id: Long, request: ChangePasswordRequest
	): Boolean? {
		propagateLanguage(locale)
		securityUserService.changeCurrentUserPassword(request, false)
		return true
	}

	/**
	 * Retrieve current user object (profile)
	 * @param locale Input Locale
	 * @return Current user object (profile)
	 */
	@PreAuthorize("isFullyAuthenticated()")
	@GraphQLQuery(name = "currentUserProfile")
	fun getCurrentUserProfile(locale: String): SecurityUser {
		propagateLanguage(locale)
		return securityUserService.getCurrentSecurityUser()
	}

	/**
	 * Updates current user object (profile)
	 * @param locale Input Locale
	 * @param target Input user object
	 * @return Current user object (profile)
	 */
	@PreAuthorize("isFullyAuthenticated()")
	@GraphQLMutation
	fun updateCurrentUserProfile(
		locale: String, target: SecurityUser
	): SecurityUser {
		propagateLanguage(locale)
		return securityUserService.updateCurrentSecurityUser(target)
	}

	/**
	 * Changes current logged-in user password
	 * @param locale Input locale
	 * @param request Change Password Request object
	 * @return True if the password successfully changed, otherwise an exception will be thrown
	 */
	@PreAuthorize("isFullyAuthenticated()")
	@GraphQLMutation
	fun changeCurrentUserPassword(
		locale: String, request: ChangePasswordRequest
	): Boolean? {
		propagateLanguage(locale)
		securityUserService.changeCurrentUserPassword(request, false)
		return true
	}

	/**
	 * Request Password Reset
	 * @param locale Input locale
	 * @param usernameOrEmail Username or user email
	 * @return True if the request has been successfully sent, otherwise an exception will be thrown
	 */
	@GraphQLMutation
	fun requestPasswordReset(
		locale: String, request: ResetPasswordRequest
	): Boolean? {
		propagateLanguage(locale)
		val user = securityUserService.findByUsernameOrEmail(request.usernameOrEmail)
		val token = securityUserTokenService.createToke(user)
		mailService.sendResetPasswordEmail(token, request.requesterRestPasswordUrl)
		return true
	}

	/**
	 * Process Password Reset Request
	 * @param locale Input locale
	 * @param request Password Reset Request
	 * @return True if the request has been successfully sent, otherwise an exception will be thrown
	 */
	@GraphQLMutation
	fun processResetPasswordRequest(locale: String, request: ProcessResetPasswordRequest): Boolean? {
		propagateLanguage(locale)
		val token = securityUserTokenService.findToken(request.token)
		securityUserTokenService.validateToken(token)
		securityUserService.changeUserPassword(
			token.user,
			ChangePasswordRequest("", request.password, request.passwordConfirm),
			false
		)
		securityUserTokenService.delete(token)
		return true
	}
}