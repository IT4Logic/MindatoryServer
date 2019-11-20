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

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ModelAdminModify}')")
	@GraphQLMutation
	fun updateModelVersionMetadata(locale: String?, id: Long, metadata: String): ModelVersion {
		propagateLanguage(locale)
		return modelVersionService.updateMetadata(id, metadata)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ModelAdminDelete}')")
	@GraphQLMutation(name = "deleteModelVersion")
	override fun delete(locale: String?, id: Long): Boolean {
		return super.delete(locale, id)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ModelAdminDelete}')")
	@GraphQLMutation(name = "deleteModelVersion")
	override fun delete(locale: String?, target: ModelVersion): Boolean {
		return super.delete(locale, target)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.ModelAdminModify}')")
	@GraphQLMutation
	fun releaseModelVersion(locale: String?, id: Long): ModelVersion {
		propagateLanguage(locale)
		return modelVersionService.releaseVersion(id)
	}

}