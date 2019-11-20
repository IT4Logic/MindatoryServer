package com.it4logic.mindatory.graphql

import com.it4logic.mindatory.model.mlc.Language
import com.it4logic.mindatory.security.ApplicationSecurityPermissions
import com.it4logic.mindatory.services.common.ApplicationBaseService
import io.leangen.graphql.annotations.GraphQLMutation
import io.leangen.graphql.annotations.GraphQLQuery
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi
import org.springframework.data.domain.Page
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service


@Service
@GraphQLApi
class LanguageGQLService : GQLBaseService<Language>() {

	override fun service(): ApplicationBaseService<Language> = languageService

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.LanguageAdminView}', '${ApplicationSecurityPermissions.LanguageAdminCreate}', '${ApplicationSecurityPermissions.LanguageAdminModify}', '${ApplicationSecurityPermissions.LanguageAdminDelete}')")
	@GraphQLQuery(name = "languagesPageable")
	override fun findAll(locale: String?, page: Int, size: Int, sort: String?, filter: String?): Page<Language> {
		return super.findAll(locale, page, size, sort, filter)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.LanguageAdminView}', '${ApplicationSecurityPermissions.LanguageAdminCreate}', '${ApplicationSecurityPermissions.LanguageAdminModify}', '${ApplicationSecurityPermissions.LanguageAdminDelete}')")
	@GraphQLQuery(name = "languages")
	override fun findAll(locale: String?, sort: String?, filter: String?): List<Language> {
		return super.findAll(locale, sort, filter)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.LanguageAdminView}', '${ApplicationSecurityPermissions.LanguageAdminCreate}', '${ApplicationSecurityPermissions.LanguageAdminModify}', '${ApplicationSecurityPermissions.LanguageAdminDelete}')")
	@GraphQLQuery(name = "language")
	override fun find(locale: String?, id: Long?, filter: String?): Language? {
		return super.find(locale, id, filter)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.LanguageAdminCreate}')")
	@GraphQLMutation(name = "createLanguage")
	override fun create(locale: String?, target: Language): Language {
		return super.create(locale, target)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.LanguageAdminModify}')")
	@GraphQLMutation(name = "updateLanguage")
	override fun update(locale: String?, target: Language): Language {
		return super.update(locale, target)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.LanguageAdminModify}')")
	@GraphQLMutation
	fun makeLanguageDefault(locale: String?, id: Long): Boolean {
		propagateLanguage(locale)
		return languageService.makeLanguageDefault(id)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.LanguageAdminDelete}')")
	@GraphQLMutation(name = "deleteLanguage")
	override fun delete(locale: String?, id: Long): Boolean {
		return super.delete(locale, id)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.LanguageAdminDelete}')")
	@GraphQLMutation(name = "deleteLanguage")
	override fun delete(locale: String?, target: Language): Boolean {
		return super.delete(locale, target)
	}

}