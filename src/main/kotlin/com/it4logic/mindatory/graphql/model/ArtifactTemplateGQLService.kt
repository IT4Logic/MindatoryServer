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
import com.it4logic.mindatory.model.model.ArtifactTemplate
import com.it4logic.mindatory.security.ApplicationSecurityPermissions
import com.it4logic.mindatory.services.common.ApplicationBaseService
import com.it4logic.mindatory.services.model.ArtifactTemplateService
import com.it4logic.mindatory.services.model.ModelVersionService
import io.leangen.graphql.annotations.GraphQLMutation
import io.leangen.graphql.annotations.GraphQLQuery
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

/**
 * GraphQL service for Artifact Template
 */
@Service
@GraphQLApi
class ArtifactTemplateGQLService : GQLBaseService<ArtifactTemplate>() {
	@Autowired
	lateinit var artifactTemplateService: ArtifactTemplateService

	@Autowired
	lateinit var modelVersionService: ModelVersionService

	override fun service(): ApplicationBaseService<ArtifactTemplate> = artifactTemplateService

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ArtifactTemplateAdminView}', '${ApplicationSecurityPermissions.ArtifactTemplateAdminCreate}', '${ApplicationSecurityPermissions.ArtifactTemplateAdminModify}', '${ApplicationSecurityPermissions.ArtifactTemplateAdminDelete}')")
	@GraphQLQuery(name = "artifactTemplatesPageable")
	override fun findAll(
		locale: String?,
		page: Int,
		size: Int,
		sort: String?,
		filter: String?
	): Page<ArtifactTemplate> {
		return super.findAll(locale, page, size, sort, filter)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ArtifactTemplateAdminView}', '${ApplicationSecurityPermissions.ArtifactTemplateAdminCreate}', '${ApplicationSecurityPermissions.ArtifactTemplateAdminModify}', '${ApplicationSecurityPermissions.ArtifactTemplateAdminDelete}')")
	@GraphQLQuery(name = "artifactTemplates")
	override fun findAll(locale: String?, sort: String?, filter: String?): List<ArtifactTemplate> {
		return super.findAll(locale, sort, filter)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ArtifactTemplateAdminView}', '${ApplicationSecurityPermissions.ArtifactTemplateAdminCreate}', '${ApplicationSecurityPermissions.ArtifactTemplateAdminModify}', '${ApplicationSecurityPermissions.ArtifactTemplateAdminDelete}')")
	@GraphQLQuery(name = "artifactTemplate")
	override fun find(locale: String?, id: Long?, filter: String?): ArtifactTemplate? {
		return super.find(locale, id, filter)
	}

	/**
	 * Custom implementation to provide the Model Version information while creating Artifact Template object
	 * @param locale Input locale
	 * @param verId Model Version Id
	 * @param target Input object instance
	 * @return Created Artifact Template instance
	 */
	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ArtifactTemplateAdminCreate}')")
	@GraphQLMutation(name = "createArtifactTemplate")
	fun create(locale: String?, verId: Long, target: ArtifactTemplate): ArtifactTemplate {
		target.modelVersion = modelVersionService.findById(verId)
		return super.create(locale, target)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ArtifactTemplateAdminModify}')")
	@GraphQLMutation(name = "updateArtifactTemplate")
	override fun update(locale: String?, target: ArtifactTemplate): ArtifactTemplate {
		val ref = artifactTemplateService.findById(target.id)
		target.modelVersion = ref.modelVersion
		return super.update(locale, target)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ArtifactTemplateAdminDelete}')")
	@GraphQLMutation(name = "deleteArtifactTemplate")
	override fun delete(locale: String?, id: Long): Boolean {
		return super.delete(locale, id)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ArtifactTemplateAdminDelete}')")
	@GraphQLMutation(name = "deleteArtifactTemplate")
	override fun delete(locale: String?, target: ArtifactTemplate): Boolean {
		return super.delete(locale, target)
	}

	/**
	 * Updated Artifact Template Metadata
	 * @param locale Input locale
	 * @param id Artifact Template Id
	 * @param metadata Input metadata in String format
	 * @return Updated Artifact Template instance
	 */
	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ArtifactTemplateAdminModify}')")
	@GraphQLMutation
	fun updateArtifactTemplateMetadata(locale: String?, id: Long, metadata: String): ArtifactTemplate {
		propagateLanguage(locale)
		return artifactTemplateService.updateMetadata(id, metadata)
	}
}