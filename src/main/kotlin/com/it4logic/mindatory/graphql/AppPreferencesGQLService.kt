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

/**
 * GraphQL service for Application Preferences
 */
@Service
@GraphQLApi
class AppPreferencesGQLService : GQLBaseService<AppPreferences>() {
	@Autowired
	lateinit var appPreferencesService: AppPreferencesService

	override fun service(): ApplicationBaseService<AppPreferences> = appPreferencesService

	/**
	 * Custom implementation to provide the only one instance of Application Preferences
	 * @param locale Input locale
	 * @return Application Preferences instance
	 */
	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.AppPreferencesAdminView}', '${ApplicationSecurityPermissions.AppPreferencesAdminCreate}', '${ApplicationSecurityPermissions.AppPreferencesAdminModify}', '${ApplicationSecurityPermissions.AppPreferencesAdminDelete}')")
	@GraphQLQuery(name = "appPreferences")
	fun find(locale: String?): AppPreferences? {
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