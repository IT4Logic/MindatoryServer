package com.it4logic.mindatory.graphql.security

import com.it4logic.mindatory.graphql.GQLBaseService
import com.it4logic.mindatory.model.security.SecurityUser
import com.it4logic.mindatory.security.ApplicationSecurityPermissions
import com.it4logic.mindatory.security.ChangePasswordRequest
import com.it4logic.mindatory.services.common.ApplicationBaseService
import com.it4logic.mindatory.services.security.SecurityUserService
import io.leangen.graphql.annotations.GraphQLMutation
import io.leangen.graphql.annotations.GraphQLQuery
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service


@Service
@GraphQLApi
class SecurityUserGQLService : GQLBaseService<SecurityUser>() {
	@Autowired
	lateinit var securityUserService: SecurityUserService

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

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.SecurityUserAdminModify}')")
	@GraphQLMutation
	fun changeUserPassword(
		locale: String, id: Long, request: ChangePasswordRequest
	): Boolean? {
		propagateLanguage(locale)
		securityUserService.changeCurrentUserPassword(request, false)
		return true
	}

	@PreAuthorize("isFullyAuthenticated()")
	@GraphQLQuery(name = "currentUserProfile")
	fun getCurrentUserProfile(locale: String): SecurityUser {
		propagateLanguage(locale)
		return securityUserService.getCurrentSecurityUser()
	}

	@PreAuthorize("isFullyAuthenticated()")
	@GraphQLMutation
	fun updateCurrentUserProfile(
		locale: String, target: SecurityUser
	): SecurityUser {
		propagateLanguage(locale)
		return securityUserService.updateCurrentSecurityUser(target)
	}

	@PreAuthorize("isFullyAuthenticated()")
	@GraphQLMutation
	fun changeCurrentUserPassword(
		locale: String, request: ChangePasswordRequest
	): Boolean? {
		propagateLanguage(locale)
		securityUserService.changeCurrentUserPassword(request, false)
		return true
	}
}