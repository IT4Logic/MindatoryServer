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
import com.it4logic.mindatory.model.model.ModelVersion
import com.it4logic.mindatory.security.ApplicationSecurityPermissions
import com.it4logic.mindatory.services.common.ApplicationBaseService
import com.it4logic.mindatory.services.model.ModelVersionService
import io.leangen.graphql.annotations.GraphQLMutation
import io.leangen.graphql.annotations.GraphQLQuery
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

/**
 * GraphQL service for Model Version
 */
@Service
@GraphQLApi
class ModelVersionGQLService : GQLBaseService<ModelVersion>() {
	@Autowired
	lateinit var modelVersionService: ModelVersionService

	override fun service(): ApplicationBaseService<ModelVersion> = modelVersionService

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ModelAdminView}', '${ApplicationSecurityPermissions.ModelAdminCreate}', '${ApplicationSecurityPermissions.ModelAdminModify}', '${ApplicationSecurityPermissions.ModelAdminDelete}')")
	@GraphQLQuery(name = "modelVersionsPageable")
	override fun findAll(locale: String?, page: Int, size: Int, sort: String?, filter: String?): Page<ModelVersion> {
		return super.findAll(locale, page, size, sort, filter)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ModelAdminView}', '${ApplicationSecurityPermissions.ModelAdminCreate}', '${ApplicationSecurityPermissions.ModelAdminModify}', '${ApplicationSecurityPermissions.ModelAdminDelete}')")
	@GraphQLQuery(name = "modelVersions")
	override fun findAll(locale: String?, sort: String?, filter: String?): List<ModelVersion> {
		return super.findAll(locale, sort, filter)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ModelAdminView}', '${ApplicationSecurityPermissions.ModelAdminCreate}', '${ApplicationSecurityPermissions.ModelAdminModify}', '${ApplicationSecurityPermissions.ModelAdminDelete}')")
	@GraphQLQuery(name = "modelVersion")
	override fun find(locale: String?, id: Long?, filter: String?): ModelVersion? {
		return super.find(locale, id, filter)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ModelAdminCreate}')")
	override fun create(locale: String?, target: ModelVersion): ModelVersion {
		throw NotImplementedError()
	}

	/**
	 * Custom implementation to provide the Model information while creating Model Version object
	 * @param locale Input locale
	 * @param modelId Model Id
	 * @return Created model Version instance
	 */
	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ModelAdminCreate}')")
	@GraphQLMutation
	fun createModelVersion(locale: String?, modelId: Long): ModelVersion {
		propagateLanguage(locale)
		return modelVersionService.createVersion(modelId)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ModelAdminModify}')")
	override fun update(locale: String?, target: ModelVersion): ModelVersion {
		throw NotImplementedError()
	}

	/**
	 * Updated Model Version Metadata
	 * @param locale Input locale
	 * @param id Model Version Id
	 * @param metadata Input metadata in String format
	 * @return Updated Model Version instance
	 */
	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ModelAdminModify}')")
	@GraphQLMutation
	fun updateModelVersionMetadata(locale: String?, id: Long, metadata: String): ModelVersion {
		propagateLanguage(locale)
		return modelVersionService.updateMetadata(id, metadata)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ModelAdminDelete}')")
	@GraphQLMutation(name = "deleteModelVersion")
	override fun delete(locale: String?, id: Long): Boolean {
		propagateLanguage(locale)
		modelVersionService.deleteVersion(id)
		return true
	}

	/**
	 * Releases the Model Version object to be used within projects
	 * @param locale Input locale
	 * @param id Model Version Id
	 * @return Released Model Version object
	 */
	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ModelAdminModify}')")
	@GraphQLMutation
	fun releaseModelVersion(locale: String?, id: Long): ModelVersion {
		propagateLanguage(locale)
		return modelVersionService.releaseVersion(id)
	}

}