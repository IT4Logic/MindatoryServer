/*
    Copyright (c) 2018, IT4Logic.

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

package com.it4logic.mindatory.model.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.it4logic.mindatory.mlc.MultipleLanguageContent
import com.it4logic.mindatory.model.common.*
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntity
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntityRepository
import io.leangen.graphql.annotations.GraphQLIgnore
import org.hibernate.annotations.DynamicUpdate
import org.hibernate.envers.Audited
import org.hibernate.envers.NotAudited
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

/**
 * Attribute Template Entity
 */
@Audited
@Entity
@DynamicUpdate
@EntityListeners(AuditingEntityListener::class)
@Table(
	name = "t_attribute_templates", uniqueConstraints = [
		(UniqueConstraint(
			name = ApplicationConstraintCodes.AttributeTemplateIdentifierUniqueIndex,
			columnNames = ["f_identifier"]
		))
	]
)
data class AttributeTemplate(
	@get: Size(max = 50)
	@Column(name = "f_identifier")
	var identifier: String,

	@get: NotBlank
	@get: Size(min = 2, max = 100)
	@get: MultipleLanguageContent
	@Transient
	var name: String,

	@get: Size(max = 255)
	@get: MultipleLanguageContent
	@Transient
	var description: String = "",

	@get: NotNull
	@Column(name = "f_type_uuid")
	var typeUUID: String,

	@ManyToOne(optional = false)
	@JoinColumn(name = "f_artifact_id", nullable = false)
	var artifact: ArtifactTemplate,

	@get: MultipleLanguageContent
	@ManyToOne
	@JoinColumn(name = "f_model_ver_id", nullable = false)
	var modelVersion: ModelVersion,

	@OneToMany(mappedBy = "attribute", cascade = [CascadeType.ALL], orphanRemoval = true)
	var properties: MutableList<AttributeTemplateProperty> = mutableListOf(),

	@NotAudited
	@OneToMany
	@JoinColumn(name = "f_parent", referencedColumnName = "f_id")
	@JsonIgnore
	@get: GraphQLIgnore
	var mlcs: MutableList<AttributeTemplateMultipleLanguageContent> = mutableListOf()

) : ApplicationMLCEntityBase() {

	@Suppress("SENSELESS_COMPARISON", "UNCHECKED_CAST")
	override fun obtainMLCs(): MutableList<MultipleLanguageContentBaseEntity> {
		if (mlcs == null)
			mlcs = mutableListOf()
		return mlcs as MutableList<MultipleLanguageContentBaseEntity>
	}
}

/**
 * JPA Repository
 */
@RepositoryRestResource(exported = false)
interface AttributeTemplateRepository : ApplicationBaseRepository<AttributeTemplate>

/**
 * Multiple Language Content entity
 */
@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(
	name = "t_attribute_template_mlcs", uniqueConstraints = [
		(UniqueConstraint(
			name = ApplicationConstraintCodes.AttributeTemplateMCLUniqueIndex,
			columnNames = ["f_parent", "f_language_id", "f_field_name"]
		))
	]
)
class AttributeTemplateMultipleLanguageContent : MultipleLanguageContentBaseEntity()

/**
 * Multiple Language Content Repository
 */
@RepositoryRestResource(exported = false)
interface AttributeTemplateMLCRepository :
	MultipleLanguageContentBaseEntityRepository<AttributeTemplateMultipleLanguageContent>