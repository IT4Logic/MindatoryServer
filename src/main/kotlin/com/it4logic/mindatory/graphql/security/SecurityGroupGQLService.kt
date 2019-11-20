package com.it4logic.mindatory.graphql.security

import com.it4logic.mindatory.graphql.GQLBaseService
import com.it4logic.mindatory.model.security.SecurityGroup
import com.it4logic.mindatory.security.ApplicationSecurityPermissions
import com.it4logic.mindatory.services.common.ApplicationBaseService
import com.it4logic.mindatory.services.security.SecurityGroupService
import io.leangen.graphql.annotations.GraphQLMutation
import io.leangen.graphql.annotations.GraphQLQuery
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service


@Service
@GraphQLApi
class SecurityGroupGQLService : GQLBaseService<SecurityGroup>() {
	@Autowired
	lateinit var securityGroupService: SecurityGroupService

	override fun service(): ApplicationBaseService<SecurityGroup> = securityGroupService

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.SecurityGroupAdminView}', '${ApplicationSecurityPermissions.SecurityGroupAdminCreate}', '${ApplicationSecurityPermissions.SecurityGroupAdminModify}', '${ApplicationSecurityPermissions.SecurityGroupAdminDelete}')")
	@GraphQLQuery(name = "securityGroupsPageable")
	override fun findAll(locale: String?, page: Int, size: Int, sort: String?, filter: String?): Page<SecurityGroup> {
		return super.findAll(locale, page, size, sort, filter)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.SecurityGroupAdminView}', '${ApplicationSecurityPermissions.SecurityGroupAdminCreate}', '${ApplicationSecurityPermissions.SecurityGroupAdminModify}', '${ApplicationSecurityPermissions.SecurityGroupAdminDelete}')")
	@GraphQLQuery(name = "securityGroups")
	override fun findAll(locale: String?, sort: String?, filter: String?): List<SecurityGroup> {
		return super.findAll(locale, sort, filter)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.SecurityGroupAdminView}', '${ApplicationSecurityPermissions.SecurityGroupAdminCreate}', '${ApplicationSecurityPermissions.SecurityGroupAdminModify}', '${ApplicationSecurityPermissions.SecurityGroupAdminDelete}')")
	@GraphQLQuery(name = "securityGroup")
	override fun find(locale: String?, id: Long?, filter: String?): SecurityGroup? {
		return super.find(locale, id, filter)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.SecurityGroupAdminCreate}')")
	@GraphQLMutation(name = "createSecurityGroup")
	override fun create(locale: String?, target: SecurityGroup): SecurityGroup {
		return super.create(locale, target)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.SecurityGroupAdminModify}')")
	@GraphQLMutation(name = "updateSecurityGroup")
	override fun update(locale: String?, target: SecurityGroup): SecurityGroup {
		return super.update(locale, target)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.SecurityGroupAdminDelete}')")
	@GraphQLMutation(name = "deleteSecurityGroup")
	override fun delete(locale: String?, id: Long): Boolean {
		return super.delete(locale, id)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.SecurityGroupAdminDelete}')")
	@GraphQLMutation(name = "deleteSecurityGroup")
	override fun delete(locale: String?, target: SecurityGroup): Boolean {
		return super.delete(locale, target)
	}

}