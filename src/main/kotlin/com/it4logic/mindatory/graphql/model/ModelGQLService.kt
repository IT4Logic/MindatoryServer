package com.it4logic.mindatory.graphql.model

import com.it4logic.mindatory.graphql.GQLBaseService
import com.it4logic.mindatory.model.model.Model
import com.it4logic.mindatory.security.ApplicationSecurityPermissions
import com.it4logic.mindatory.services.common.ApplicationBaseService
import com.it4logic.mindatory.services.model.ModelService
import io.leangen.graphql.annotations.GraphQLMutation
import io.leangen.graphql.annotations.GraphQLQuery
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service


@Service
@GraphQLApi
class ModelGQLService : GQLBaseService<Model>() {
	@Autowired
	lateinit var modelService: ModelService

	override fun service(): ApplicationBaseService<Model> = modelService

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ModelAdminView}', '${ApplicationSecurityPermissions.ModelAdminCreate}', '${ApplicationSecurityPermissions.ModelAdminModify}', '${ApplicationSecurityPermissions.ModelAdminDelete}')")
	@GraphQLQuery(name = "modelsPageable")
	override fun findAll(locale: String?, page: Int, size: Int, sort: String?, filter: String?): Page<Model> {
		return super.findAll(locale, page, size, sort, filter)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ModelAdminView}', '${ApplicationSecurityPermissions.ModelAdminCreate}', '${ApplicationSecurityPermissions.ModelAdminModify}', '${ApplicationSecurityPermissions.ModelAdminDelete}')")
	@GraphQLQuery(name = "models")
	override fun findAll(locale: String?, sort: String?, filter: String?): List<Model> {
		return super.findAll(locale, sort, filter)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ModelAdminView}', '${ApplicationSecurityPermissions.ModelAdminCreate}', '${ApplicationSecurityPermissions.ModelAdminModify}', '${ApplicationSecurityPermissions.ModelAdminDelete}')")
	@GraphQLQuery(name = "model")
	override fun find(locale: String?, id: Long?, filter: String?): Model? {
		return super.find(locale, id, filter)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ModelAdminCreate}')")
	@GraphQLMutation(name = "createModel")
	override fun create(locale: String?, target: Model): Model {
		return super.create(locale, target)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ModelAdminModify}')")
	@GraphQLMutation(name = "updateModel")
	override fun update(locale: String?, target: Model): Model {
		return super.update(locale, target)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ModelAdminDelete}')")
	@GraphQLMutation(name = "deleteModel")
	override fun delete(locale: String?, id: Long): Boolean {
		return super.delete(locale, id)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ModelAdminDelete}')")
	@GraphQLMutation(name = "deleteModel")
	override fun delete(locale: String?, target: Model): Boolean {
		return super.delete(locale, target)
	}

}