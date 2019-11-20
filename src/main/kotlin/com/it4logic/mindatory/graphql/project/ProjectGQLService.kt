package com.it4logic.mindatory.graphql.project

import com.it4logic.mindatory.graphql.GQLBaseService
import com.it4logic.mindatory.model.model.ArtifactTemplate
import com.it4logic.mindatory.model.project.Project
import com.it4logic.mindatory.security.ApplicationSecurityPermissions
import com.it4logic.mindatory.services.common.ApplicationBaseService
import com.it4logic.mindatory.services.project.ProjectService
import io.leangen.graphql.annotations.GraphQLMutation
import io.leangen.graphql.annotations.GraphQLQuery
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service


@Service
@GraphQLApi
class ProjectGQLService : GQLBaseService<Project>() {
	@Autowired
	lateinit var projectService: ProjectService

	override fun service(): ApplicationBaseService<Project> = projectService

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ProjectAdminView}', '${ApplicationSecurityPermissions.ProjectAdminCreate}', '${ApplicationSecurityPermissions.ProjectAdminModify}', '${ApplicationSecurityPermissions.ProjectAdminDelete}')")
	@GraphQLQuery(name = "projectsPageable")
	override fun findAll(locale: String?, page: Int, size: Int, sort: String?, filter: String?): Page<Project> {
		return super.findAll(locale, page, size, sort, filter)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ProjectAdminView}', '${ApplicationSecurityPermissions.ProjectAdminCreate}', '${ApplicationSecurityPermissions.ProjectAdminModify}', '${ApplicationSecurityPermissions.ProjectAdminDelete}')")
	@GraphQLQuery(name = "projects")
	override fun findAll(locale: String?, sort: String?, filter: String?): List<Project> {
		return super.findAll(locale, sort, filter)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ProjectAdminView}', '${ApplicationSecurityPermissions.ProjectAdminCreate}', '${ApplicationSecurityPermissions.ProjectAdminModify}', '${ApplicationSecurityPermissions.ProjectAdminDelete}')")
	@GraphQLQuery(name = "project")
	override fun find(locale: String?, id: Long?, filter: String?): Project? {
		return super.find(locale, id, filter)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ProjectAdminView}', '${ApplicationSecurityPermissions.ProjectAdminCreate}', '${ApplicationSecurityPermissions.ProjectAdminModify}', '${ApplicationSecurityPermissions.ProjectAdminDelete}')")
	@GraphQLQuery(name = "availableArtifactTemplates")
	fun getAvailableArtifactTemplates(
		locale: String?,
		id: Long,
		filter: String?
	): List<ProjectService.ModelVersionArtifactTemplatesMap> {
		propagateLanguage(locale)
		return projectService.getAvailableArtifactsMap(id)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ProjectAdminView}', '${ApplicationSecurityPermissions.ProjectAdminCreate}', '${ApplicationSecurityPermissions.ProjectAdminModify}', '${ApplicationSecurityPermissions.ProjectAdminDelete}')")
	@GraphQLQuery(name = "usedArtifactTemplates")
	fun getUsedArtifactTemplates(
		locale: String?,
		id: Long
	): List<ArtifactTemplate> {
		propagateLanguage(locale)
		return projectService.getUsedArtifactTemplates(id)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ProjectAdminCreate}')")
	@GraphQLMutation(name = "createProject")
	override fun create(locale: String?, target: Project): Project {
		return super.create(locale, target)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ProjectAdminModify}')")
	@GraphQLMutation(name = "updateProject")
	override fun update(locale: String?, target: Project): Project {
		return super.update(locale, target)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ProjectAdminDelete}')")
	@GraphQLMutation(name = "deleteProject")
	override fun delete(locale: String?, id: Long): Boolean {
		return super.delete(locale, id)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ProjectAdminDelete}')")
	@GraphQLMutation(name = "deleteProject")
	override fun delete(locale: String?, target: Project): Boolean {
		return super.delete(locale, target)
	}

}