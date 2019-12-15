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

import com.it4logic.mindatory.attribute_template.datatypes.AttributeTemplateDataType
import com.it4logic.mindatory.graphql.GQLBaseService
import com.it4logic.mindatory.model.model.AttributeTemplate
import com.it4logic.mindatory.security.ApplicationSecurityPermissions
import com.it4logic.mindatory.services.AttributeTemplateDataTypeManagerService
import com.it4logic.mindatory.services.common.ApplicationBaseService
import com.it4logic.mindatory.services.model.ArtifactTemplateService
import com.it4logic.mindatory.services.model.AttributeTemplateService
import com.it4logic.mindatory.services.model.ModelVersionService
import io.leangen.graphql.annotations.GraphQLMutation
import io.leangen.graphql.annotations.GraphQLQuery
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

/**
 * GraphQL service for Attribute Template
 */
@Service
@GraphQLApi
class AttributeTemplateGQLService : GQLBaseService<AttributeTemplate>() {
	@Autowired
	lateinit var attributeTemplateDataTypeManagerService: AttributeTemplateDataTypeManagerService

	@Autowired
	lateinit var attributeTemplateService: AttributeTemplateService

	@Autowired
	lateinit var modelVersionService: ModelVersionService

	@Autowired
	lateinit var artifactService: ArtifactTemplateService


	override fun service(): ApplicationBaseService<AttributeTemplate> = attributeTemplateService

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.AttributeTemplateAdminView}', '${ApplicationSecurityPermissions.AttributeTemplateAdminCreate}', '${ApplicationSecurityPermissions.AttributeTemplateAdminModify}', '${ApplicationSecurityPermissions.AttributeTemplateAdminDelete}')")
	@GraphQLQuery(name = "attributeTemplatesPageable")
	override fun findAll(
		locale: String?,
		page: Int,
		size: Int,
		sort: String?,
		filter: String?
	): Page<AttributeTemplate> {
		return super.findAll(locale, page, size, sort, filter)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.AttributeTemplateAdminView}', '${ApplicationSecurityPermissions.AttributeTemplateAdminCreate}', '${ApplicationSecurityPermissions.AttributeTemplateAdminModify}', '${ApplicationSecurityPermissions.AttributeTemplateAdminDelete}')")
	@GraphQLQuery(name = "attributeTemplates")
	override fun findAll(locale: String?, sort: String?, filter: String?): List<AttributeTemplate> {
		return super.findAll(locale, sort, filter)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.AttributeTemplateAdminView}', '${ApplicationSecurityPermissions.AttributeTemplateAdminCreate}', '${ApplicationSecurityPermissions.AttributeTemplateAdminModify}', '${ApplicationSecurityPermissions.AttributeTemplateAdminDelete}')")
	@GraphQLQuery(name = "attributeTemplate")
	override fun find(locale: String?, id: Long?, filter: String?): AttributeTemplate? {
		return super.find(locale, id, filter)
	}

	/**
	 * Custom implementation to provide the Model Version and Artifact Template information
	 * while creating Attribute Template object
	 * @param locale Input locale
	 * @param verId Model Version Id
	 * @param artifactId Artifact Template Id
	 * @param target Input object instance
	 * @return Created Attribute Template instance
	 */
	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.AttributeTemplateAdminCreate}')")
	@GraphQLMutation(name = "createAttributeTemplate")
	fun create(locale: String?, verId: Long, artifactId: Long, target: AttributeTemplate): AttributeTemplate {
		target.modelVersion = modelVersionService.findById(verId)
		target.artifact = artifactService.findById(artifactId)
		return super.create(locale, target)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.AttributeTemplateAdminModify}')")
	@GraphQLMutation(name = "updateAttributeTemplate")
	override fun update(locale: String?, target: AttributeTemplate): AttributeTemplate {
		val ref = attributeTemplateService.findById(target.id)
		target.modelVersion = ref.modelVersion
		target.artifact = ref.artifact
		target.globalIdentifier = ref.globalIdentifier
		return super.update(locale, target)
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.AttributeTemplateAdminDelete}')")
	@GraphQLMutation(name = "deleteAttributeTemplate")
	override fun delete(locale: String?, id: Long): Boolean {
		return super.delete(locale, id)
	}

	/**
	 * Retrieves Pre-defined Attributes data types list
	 * @param locale Input locale
	 * @return Attributes data types list
	 */
	@PreAuthorize("isFullyAuthenticated()")
	@GraphQLQuery(name = "dataTypes")
	fun doGetDataTypes(locale: String?): List<AttributeTemplateDataType> {
		propagateLanguage(locale)
		return attributeTemplateDataTypeManagerService.getAttributeTemplateDataTypes()
	}

	/**
	 * Retrieves Pre-defined Attributes data type
	 * @param locale Input locale
	 * @param uuid Data type UUID
	 * @return Attributes data type object
	 */
	@PreAuthorize("isFullyAuthenticated()")
	@GraphQLQuery(name = "dataType")
	fun doGetDataType(locale: String?, uuid: String): AttributeTemplateDataType {
		propagateLanguage(locale)
		return attributeTemplateDataTypeManagerService.getAttributeTemplateDataType(uuid)
	}
}