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
package com.it4logic.mindatory.graphql.model

import com.it4logic.mindatory.graphql.GQLBaseService
import com.it4logic.mindatory.model.model.Stereotype
import com.it4logic.mindatory.security.ApplicationSecurityPermissions
import com.it4logic.mindatory.services.common.ApplicationBaseService
import com.it4logic.mindatory.services.model.ModelVersionService
import com.it4logic.mindatory.services.model.StereotypeService
import io.leangen.graphql.annotations.GraphQLMutation
import io.leangen.graphql.annotations.GraphQLQuery
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

/**
 * GraphQL service for Stereotype
 */
@Service
@GraphQLApi
class StereotypeGQLService : GQLBaseService<Stereotype>() {
	@Autowired
	lateinit var stereotypeService: StereotypeService

	@Autowired
	lateinit var modelVersionService: ModelVersionService

	override fun service(): ApplicationBaseService<Stereotype> = stereotypeService

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.StereotypeAdminView}', '${ApplicationSecurityPermissions.StereotypeAdminCreate}', '${ApplicationSecurityPermissions.StereotypeAdminModify}', '${ApplicationSecurityPermissions.StereotypeAdminDelete}')")
	@GraphQLQuery(name = "stereotypesPageable")
	override fun findAll(locale: String?, page: Int, size: Int, sort: String?, filter: String?): Page<Stereotype> {
		return super.findAll(locale, page, size, sort, filter)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.StereotypeAdminView}', '${ApplicationSecurityPermissions.StereotypeAdminCreate}', '${ApplicationSecurityPermissions.StereotypeAdminModify}', '${ApplicationSecurityPermissions.StereotypeAdminDelete}')")
	@GraphQLQuery(name = "stereotypes")
	override fun findAll(locale: String?, sort: String?, filter: String?): List<Stereotype> {
		return super.findAll(locale, sort, filter)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.StereotypeAdminView}', '${ApplicationSecurityPermissions.StereotypeAdminCreate}', '${ApplicationSecurityPermissions.StereotypeAdminModify}', '${ApplicationSecurityPermissions.StereotypeAdminDelete}')")
	@GraphQLQuery(name = "stereotype")
	override fun find(locale: String?, id: Long?, filter: String?): Stereotype? {
		return super.find(locale, id, filter)
	}

	/**
	 * Custom implementation to provide the Model Version information while creating Stereotype object
	 * @param locale Input locale
	 * @param verId Model Version Id
	 * @param target Input object instance
	 * @return Created Stereotype instance
	 */
	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.StereotypeAdminCreate}')")
	@GraphQLMutation(name = "createStereotype")
	fun create(locale: String?, verId: Long, target: Stereotype): Stereotype {
		target.modelVersion = modelVersionService.findById(verId)
		return super.create(locale, target)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.StereotypeAdminModify}')")
	@GraphQLMutation(name = "updateStereotype")
	override fun update(locale: String?, target: Stereotype): Stereotype {
		val ref = stereotypeService.findById(target.id)
		target.modelVersion = ref.modelVersion
		return super.update(locale, target)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.StereotypeAdminDelete}')")
	@GraphQLMutation(name = "deleteStereotype")
	override fun delete(locale: String?, id: Long): Boolean {
		return super.delete(locale, id)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.StereotypeAdminDelete}')")
	@GraphQLMutation(name = "deleteStereotype")
	override fun delete(locale: String?, target: Stereotype): Boolean {
		return super.delete(locale, target)
	}

}