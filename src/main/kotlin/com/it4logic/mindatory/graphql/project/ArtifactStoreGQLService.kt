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
package com.it4logic.mindatory.graphql.project

import com.it4logic.mindatory.graphql.GQLBaseService
import com.it4logic.mindatory.model.project.ArtifactStore
import com.it4logic.mindatory.security.ApplicationSecurityPermissions
import com.it4logic.mindatory.services.common.ApplicationBaseService
import com.it4logic.mindatory.services.model.ArtifactTemplateService
import com.it4logic.mindatory.services.project.ArtifactStoreService
import com.it4logic.mindatory.services.project.ProjectService
import io.leangen.graphql.annotations.GraphQLMutation
import io.leangen.graphql.annotations.GraphQLQuery
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

/**
 * GraphQL service for Artifact Store
 */
@Service
@GraphQLApi
class ArtifactStoreGQLService : GQLBaseService<ArtifactStore>() {
	@Autowired
	lateinit var artifactStoreService: ArtifactStoreService

	@Autowired
	lateinit var artifactTemplateService: ArtifactTemplateService

	@Autowired
	lateinit var projectService: ProjectService

	override fun service(): ApplicationBaseService<ArtifactStore> = artifactStoreService

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ArtifactStoreAdminView}', '${ApplicationSecurityPermissions.ArtifactStoreAdminCreate}', '${ApplicationSecurityPermissions.ArtifactStoreAdminModify}', '${ApplicationSecurityPermissions.ArtifactStoreAdminDelete}')")
	@GraphQLQuery(name = "artifactStoresPageable")
	override fun findAll(locale: String?, page: Int, size: Int, sort: String?, filter: String?): Page<ArtifactStore> {
		return super.findAll(locale, page, size, sort, filter)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ArtifactStoreAdminView}', '${ApplicationSecurityPermissions.ArtifactStoreAdminCreate}', '${ApplicationSecurityPermissions.ArtifactStoreAdminModify}', '${ApplicationSecurityPermissions.ArtifactStoreAdminDelete}')")
	@GraphQLQuery(name = "artifactStores")
	override fun findAll(locale: String?, sort: String?, filter: String?): List<ArtifactStore> {
		return super.findAll(locale, sort, filter)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ArtifactStoreAdminView}', '${ApplicationSecurityPermissions.ArtifactStoreAdminCreate}', '${ApplicationSecurityPermissions.ArtifactStoreAdminModify}', '${ApplicationSecurityPermissions.ArtifactStoreAdminDelete}')")
	@GraphQLQuery(name = "artifactStore")
	override fun find(locale: String?, id: Long?, filter: String?): ArtifactStore? {
		return super.find(locale, id, filter)
	}

	/**
	 * Generates Traceability Matrix for the given Artifact Store object
	 * @param locale Input locale
	 * @param id Artifact Store Id
	 * @param filter Input Filter in RSQL syntax
	 * @return Traceability Matrix
	 */
	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ArtifactStoreAdminView}', '${ApplicationSecurityPermissions.ArtifactStoreAdminCreate}', '${ApplicationSecurityPermissions.ArtifactStoreAdminModify}', '${ApplicationSecurityPermissions.ArtifactStoreAdminDelete}')")
	@GraphQLQuery
	fun generateArtifactStoreTraceabilityMatrix(locale: String?, id: Long?, filter: String?): List<Any> {
		val store = find(locale, id, filter)
		return artifactStoreService.generateStoreTraceabilityMatrix(store!!)
	}

	/**
	 * Custom implementation to provide the Project and Project and Artifact Template information while creating Artifact Store object
	 * @param locale Input locale
	 * @param projectId Project Id
	 * @param artifactTemplateId Artifact Template Id
	 * @param target Input object instance
	 * @return Created Artifact Store instance
	 */
	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ArtifactStoreAdminCreate}')")
	@GraphQLMutation(name = "createArtifactStore")
	fun create(locale: String?, projectId: Long, artifactTemplateId: Long, target: ArtifactStore): ArtifactStore {
		target.project = projectService.findById(projectId)
		target.artifactTemplate = artifactTemplateService.findById(artifactTemplateId)
		return super.create(locale, target)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ArtifactStoreAdminModify}')")
	@GraphQLMutation(name = "updateArtifactStore")
	override fun update(locale: String?, target: ArtifactStore): ArtifactStore {
		val ref = artifactStoreService.findById(target.id)
		target.project = ref.project
		target.artifactTemplate = ref.artifactTemplate
		return super.update(locale, target)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ArtifactStoreAdminDelete}')")
	@GraphQLMutation(name = "deleteArtifactStore")
	override fun delete(locale: String?, id: Long): Boolean {
		return super.delete(locale, id)
	}
}