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
import com.it4logic.mindatory.model.security.SecurityRole
import com.it4logic.mindatory.security.ApplicationSecurityPermissions
import com.it4logic.mindatory.services.common.ApplicationBaseService
import com.it4logic.mindatory.services.security.SecurityRoleService
import io.leangen.graphql.annotations.GraphQLMutation
import io.leangen.graphql.annotations.GraphQLQuery
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

/**
 * GraphQL service for Security Role
 */
@Service
@GraphQLApi
class SecurityRoleGQLService : GQLBaseService<SecurityRole>() {
	@Autowired
	lateinit var securityRoleService: SecurityRoleService

	override fun service(): ApplicationBaseService<SecurityRole> = securityRoleService

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.SecurityRoleAdminView}', '${ApplicationSecurityPermissions.SecurityRoleAdminCreate}', '${ApplicationSecurityPermissions.SecurityRoleAdminModify}', '${ApplicationSecurityPermissions.SecurityRoleAdminDelete}')")
	@GraphQLQuery(name = "securityRolesPageable")
	override fun findAll(locale: String?, page: Int, size: Int, sort: String?, filter: String?): Page<SecurityRole> {
		return super.findAll(locale, page, size, sort, filter)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.SecurityRoleAdminView}', '${ApplicationSecurityPermissions.SecurityRoleAdminCreate}', '${ApplicationSecurityPermissions.SecurityRoleAdminModify}', '${ApplicationSecurityPermissions.SecurityRoleAdminDelete}')")
	@GraphQLQuery(name = "securityRoles")
	override fun findAll(locale: String?, sort: String?, filter: String?): List<SecurityRole> {
		return super.findAll(locale, sort, filter)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.SecurityRoleAdminView}', '${ApplicationSecurityPermissions.SecurityRoleAdminCreate}', '${ApplicationSecurityPermissions.SecurityRoleAdminModify}', '${ApplicationSecurityPermissions.SecurityRoleAdminDelete}')")
	@GraphQLQuery(name = "securityRole")
	override fun find(locale: String?, id: Long?, filter: String?): SecurityRole? {
		return super.find(locale, id, filter)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.SecurityRoleAdminCreate}')")
	@GraphQLMutation(name = "createSecurityRole")
	override fun create(locale: String?, target: SecurityRole): SecurityRole {
		return super.create(locale, target)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.SecurityRoleAdminModify}')")
	@GraphQLMutation(name = "updateSecurityRole")
	override fun update(locale: String?, target: SecurityRole): SecurityRole {
		return super.update(locale, target)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.SecurityRoleAdminDelete}')")
	@GraphQLMutation(name = "deleteSecurityRole")
	override fun delete(locale: String?, id: Long): Boolean {
		return super.delete(locale, id)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.SecurityRoleAdminDelete}')")
	@GraphQLMutation(name = "deleteSecurityRole")
	override fun delete(locale: String?, target: SecurityRole): Boolean {
		return super.delete(locale, target)
	}

}