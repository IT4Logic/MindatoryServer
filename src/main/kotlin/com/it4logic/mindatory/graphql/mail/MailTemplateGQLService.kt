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
package com.it4logic.mindatory.graphql.mail

import com.it4logic.mindatory.graphql.GQLBaseService
import com.it4logic.mindatory.model.mail.MailTemplate
import com.it4logic.mindatory.security.ApplicationSecurityPermissions
import com.it4logic.mindatory.services.common.ApplicationBaseService
import com.it4logic.mindatory.services.mail.MailTemplateService
import io.leangen.graphql.annotations.GraphQLMutation
import io.leangen.graphql.annotations.GraphQLQuery
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

/**
 * GraphQL service for MailTemplate
 */
@Service
@GraphQLApi
class MailTemplateGQLService : GQLBaseService<MailTemplate>() {
	@Autowired
	lateinit var mailTemplateService: MailTemplateService

	override fun service(): ApplicationBaseService<MailTemplate> = mailTemplateService

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.MailTemplateAdminView}', '${ApplicationSecurityPermissions.MailTemplateAdminCreate}', '${ApplicationSecurityPermissions.MailTemplateAdminModify}', '${ApplicationSecurityPermissions.MailTemplateAdminDelete}')")
	@GraphQLQuery(name = "mailTemplatesPageable")
	override fun findAll(locale: String?, page: Int, size: Int, sort: String?, filter: String?): Page<MailTemplate> {
		return super.findAll(locale, page, size, sort, filter)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.MailTemplateAdminView}', '${ApplicationSecurityPermissions.MailTemplateAdminCreate}', '${ApplicationSecurityPermissions.MailTemplateAdminModify}', '${ApplicationSecurityPermissions.MailTemplateAdminDelete}')")
	@GraphQLQuery(name = "mailTemplates")
	override fun findAll(locale: String?, sort: String?, filter: String?): List<MailTemplate> {
		return super.findAll(locale, sort, filter)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.MailTemplateAdminView}', '${ApplicationSecurityPermissions.MailTemplateAdminCreate}', '${ApplicationSecurityPermissions.MailTemplateAdminModify}', '${ApplicationSecurityPermissions.MailTemplateAdminDelete}')")
	@GraphQLQuery(name = "mailTemplate")
	override fun find(locale: String?, id: Long?, filter: String?): MailTemplate? {
		return super.find(locale, id, filter)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.MailTemplateAdminCreate}')")
	@GraphQLMutation(name = "createMailTemplate")
	override fun create(locale: String?, target: MailTemplate): MailTemplate {
		return super.create(locale, target)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.MailTemplateAdminModify}')")
	@GraphQLMutation(name = "updateMailTemplate")
	override fun update(locale: String?, target: MailTemplate): MailTemplate {
		return super.update(locale, target)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.MailTemplateAdminDelete}')")
	@GraphQLMutation(name = "deleteMailTemplate")
	override fun delete(locale: String?, id: Long): Boolean {
		return super.delete(locale, id)
	}
}