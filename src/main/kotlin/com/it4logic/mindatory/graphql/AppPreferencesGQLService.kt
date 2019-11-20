package com.it4logic.mindatory.graphql

import com.it4logic.mindatory.model.AppPreferences
import com.it4logic.mindatory.security.ApplicationSecurityPermissions
import com.it4logic.mindatory.services.AppPreferencesService
import com.it4logic.mindatory.services.common.ApplicationBaseService
import io.leangen.graphql.annotations.GraphQLMutation
import io.leangen.graphql.annotations.GraphQLQuery
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service


@Service
@GraphQLApi
class AppPreferencesGQLService : GQLBaseService<AppPreferences>() {
	@Autowired
	lateinit var appPreferencesService: AppPreferencesService

	override fun service(): ApplicationBaseService<AppPreferences> = appPreferencesService

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.AppPreferencesAdminView}', '${ApplicationSecurityPermissions.AppPreferencesAdminCreate}', '${ApplicationSecurityPermissions.AppPreferencesAdminModify}', '${ApplicationSecurityPermissions.AppPreferencesAdminDelete}')")
	@GraphQLQuery(name = "appPreferences")
	override fun find(locale: String?, id: Long?, filter: String?): AppPreferences? {
		propagateLanguage(locale)
		val result = appPreferencesService.findFirst()
		service().refresh(result)
		return result
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.AppPreferencesAdminModify}')")
	@GraphQLMutation(name = "updateAppPreferences")
	override fun update(locale: String?, target: AppPreferences): AppPreferences {
		return super.update(locale, target)
	}
}