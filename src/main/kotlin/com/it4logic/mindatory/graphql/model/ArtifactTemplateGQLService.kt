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

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ArtifactTemplateAdminModify}')")
	@GraphQLMutation
	fun updateArtifactTemplateMetadata(locale: String?, id: Long, metadata: String): ArtifactTemplate {
		propagateLanguage(locale)
		return artifactTemplateService.updateMetadata(id, metadata)
	}
}