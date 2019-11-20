package com.it4logic.mindatory.graphql.model

import com.it4logic.mindatory.graphql.GQLBaseService
import com.it4logic.mindatory.model.model.RelationTemplate
import com.it4logic.mindatory.security.ApplicationSecurityPermissions
import com.it4logic.mindatory.services.common.ApplicationBaseService
import com.it4logic.mindatory.services.model.ModelVersionService
import com.it4logic.mindatory.services.model.RelationTemplateService
import io.leangen.graphql.annotations.GraphQLMutation
import io.leangen.graphql.annotations.GraphQLQuery
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service


@Service
@GraphQLApi
class RelationTemplateGQLService : GQLBaseService<RelationTemplate>() {
	@Autowired
	lateinit var relationTemplateService: RelationTemplateService

	@Autowired
	lateinit var modelVersionService: ModelVersionService
	
	override fun service(): ApplicationBaseService<RelationTemplate> = relationTemplateService

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.RelationTemplateAdminView}', '${ApplicationSecurityPermissions.RelationTemplateAdminCreate}', '${ApplicationSecurityPermissions.RelationTemplateAdminModify}', '${ApplicationSecurityPermissions.RelationTemplateAdminDelete}')")
	@GraphQLQuery(name = "relationTemplatesPageable")
	override fun findAll(
		locale: String?,
		page: Int,
		size: Int,
		sort: String?,
		filter: String?
	): Page<RelationTemplate> {
		return super.findAll(locale, page, size, sort, filter)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.RelationTemplateAdminView}', '${ApplicationSecurityPermissions.RelationTemplateAdminCreate}', '${ApplicationSecurityPermissions.RelationTemplateAdminModify}', '${ApplicationSecurityPermissions.RelationTemplateAdminDelete}')")
	@GraphQLQuery(name = "relationTemplates")
	override fun findAll(locale: String?, sort: String?, filter: String?): List<RelationTemplate> {
		return super.findAll(locale, sort, filter)
	}

//	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.RelationTemplateAdminView}', '${ApplicationSecurityPermissions.RelationTemplateAdminCreate}', '${ApplicationSecurityPermissions.RelationTemplateAdminModify}', '${ApplicationSecurityPermissions.RelationTemplateAdminDelete}')")
//	@GraphQLQuery
//	fun findRelationsForArtifactPageable(locale: String?, page: Int, size: Int, sort: String?, artifactTemplateId: Long): Page<RelationTemplate> {
//		return findAll(locale, page, size, sort, "sourceArtifact.id==${artifactTemplateId},targetArtifact.id==${artifactTemplateId}")
//	}
//
//	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.RelationTemplateAdminView}', '${ApplicationSecurityPermissions.RelationTemplateAdminCreate}', '${ApplicationSecurityPermissions.RelationTemplateAdminModify}', '${ApplicationSecurityPermissions.RelationTemplateAdminDelete}')")
//	@GraphQLQuery
//	fun findRelationsForArtifact(locale: String?, page: Int, size: Int, sort: String?, artifactTemplateId: Long): List<RelationTemplate> {
//		return findAll(locale, sort, "sourceArtifact.id==${artifactTemplateId},targetArtifact.id==${artifactTemplateId}")
//	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.RelationTemplateAdminView}', '${ApplicationSecurityPermissions.RelationTemplateAdminCreate}', '${ApplicationSecurityPermissions.RelationTemplateAdminModify}', '${ApplicationSecurityPermissions.RelationTemplateAdminDelete}')")
	@GraphQLQuery(name = "relationTemplate")
	override fun find(locale: String?, id: Long?, filter: String?): RelationTemplate? {
		return super.find(locale, id, filter)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.RelationTemplateAdminCreate}')")
	@GraphQLMutation(name = "createRelationTemplate")
	fun create(locale: String?, verId: Long, target: RelationTemplate): RelationTemplate {
		target.modelVersion = modelVersionService.findById(verId)
		return super.create(locale, target)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.RelationTemplateAdminModify}')")
	@GraphQLMutation(name = "updateRelationTemplate")
	override fun update(locale: String?, target: RelationTemplate): RelationTemplate {
		val ref = relationTemplateService.findById(target.id)
		target.modelVersion = ref.modelVersion
		return super.update(locale, target)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.RelationTemplateAdminDelete}')")
	@GraphQLMutation(name = "deleteRelationTemplate")
	override fun delete(locale: String?, id: Long): Boolean {
		return super.delete(locale, id)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.RelationTemplateAdminDelete}')")
	@GraphQLMutation(name = "deleteRelationTemplate")
	override fun delete(locale: String?, target: RelationTemplate): Boolean {
		return super.delete(locale, target)
	}

}