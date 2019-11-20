package com.it4logic.mindatory.graphql.project

import com.it4logic.mindatory.graphql.GQLBaseService
import com.it4logic.mindatory.model.project.RelationStore
import com.it4logic.mindatory.security.ApplicationSecurityPermissions
import com.it4logic.mindatory.services.common.ApplicationBaseService
import com.it4logic.mindatory.services.model.RelationTemplateService
import com.it4logic.mindatory.services.project.ProjectService
import com.it4logic.mindatory.services.project.RelationStoreService
import io.leangen.graphql.annotations.GraphQLMutation
import io.leangen.graphql.annotations.GraphQLQuery
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service


@Service
@GraphQLApi
class RelationStoreGQLService : GQLBaseService<RelationStore>() {
	@Autowired
	lateinit var relationStoreService: RelationStoreService

	@Autowired
	lateinit var relationTemplateService: RelationTemplateService

	@Autowired
	lateinit var projectService: ProjectService

	override fun service(): ApplicationBaseService<RelationStore> = relationStoreService

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.RelationStoreAdminView}', '${ApplicationSecurityPermissions.RelationStoreAdminCreate}', '${ApplicationSecurityPermissions.RelationStoreAdminModify}', '${ApplicationSecurityPermissions.RelationStoreAdminDelete}')")
	@GraphQLQuery(name = "relationStoresPageable")
	override fun findAll(locale: String?, page: Int, size: Int, sort: String?, filter: String?): Page<RelationStore> {
		return super.findAll(locale, page, size, sort, filter)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.RelationStoreAdminView}', '${ApplicationSecurityPermissions.RelationStoreAdminCreate}', '${ApplicationSecurityPermissions.RelationStoreAdminModify}', '${ApplicationSecurityPermissions.RelationStoreAdminDelete}')")
	@GraphQLQuery(name = "relationStores")
	override fun findAll(locale: String?, sort: String?, filter: String?): List<RelationStore> {
		return super.findAll(locale, sort, filter)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.RelationStoreAdminView}', '${ApplicationSecurityPermissions.RelationStoreAdminCreate}', '${ApplicationSecurityPermissions.RelationStoreAdminModify}', '${ApplicationSecurityPermissions.RelationStoreAdminDelete}')")
	@GraphQLQuery(name = "relationStore")
	override fun find(locale: String?, id: Long?, filter: String?): RelationStore? {
		return super.find(locale, id, filter)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.RelationStoreAdminCreate}')")
	@GraphQLMutation(name = "createRelationStore")
	fun create(locale: String?, projectId: Long, relationTemplateId: Long, target: RelationStore): RelationStore {
		target.project = projectService.findById(projectId)
		target.relationTemplate = relationTemplateService.findById(relationTemplateId)
		return super.create(locale, target)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.RelationStoreAdminModify}')")
	@GraphQLMutation(name = "updateRelationStore")
	override fun update(locale: String?, target: RelationStore): RelationStore {
		val ref = relationStoreService.findById(target.id)
		target.project = ref.project
		target.relationTemplate = ref.relationTemplate
		return super.update(locale, target)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.RelationStoreAdminDelete}')")
	@GraphQLMutation(name = "deleteRelationStore")
	override fun delete(locale: String?, id: Long): Boolean {
		return super.delete(locale, id)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.RelationStoreAdminDelete}')")
	@GraphQLMutation(name = "deleteRelationStore")
	override fun delete(locale: String?, target: RelationStore): Boolean {
		return super.delete(locale, target)
	}

}