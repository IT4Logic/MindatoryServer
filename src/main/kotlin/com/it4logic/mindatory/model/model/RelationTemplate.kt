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
import org.hibernate.annotations.LazyCollection
import org.hibernate.annotations.LazyCollectionOption
import org.hibernate.envers.Audited
import org.hibernate.envers.NotAudited
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import javax.persistence.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

/**
 * Relation Template Entity
 */
@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(
	name = "t_relation_templates", uniqueConstraints = [
		(UniqueConstraint(
			name = ApplicationConstraintCodes.RelationTemplateIdentifierUniqueIndex,
			columnNames = ["f_identifier"]
		))
	]
)
data class RelationTemplate(
	@get: Size(max = 50)
	@Column(name = "f_identifier")
	var identifier: String,

	@get: Size(max = 255)
	@get: MultipleLanguageContent
	@Transient
	var description: String = "",

	@get: Size(max = 50)
	@Column(name = "f_g_identifier")
	var globalIdentifier: String = "",

	@get: NotNull
	@get: MultipleLanguageContent
	@ManyToOne(optional = false)
	@JoinColumn(name = "f_source_stereotype_id", nullable = false)
	var sourceStereotype: Stereotype,

	@get: NotNull
	@get: MultipleLanguageContent
	@ManyToOne(optional = false)
	@JoinColumn(name = "f_source_artifact_id", nullable = false)
	var sourceArtifact: ArtifactTemplate,

	@get: NotNull
	@get: MultipleLanguageContent
	@ManyToOne(optional = false)
	@JoinColumn(name = "f_target_stereotype_id", nullable = false)
	var targetStereotype: Stereotype,

	@get: NotNull
	@get: MultipleLanguageContent
	@ManyToOne(optional = false)
	@JoinColumn(name = "f_source_target_id", nullable = false)
	var targetArtifact: ArtifactTemplate,

	@ManyToOne(optional = false)
	@JoinColumn(name = "f_model_ver_id", nullable = false)
	var modelVersion: ModelVersion? = null,

	@NotAudited
	@OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, mappedBy = "parent")
	@LazyCollection(LazyCollectionOption.FALSE)
	@JsonIgnore
	@get: GraphQLIgnore
	var mlcs: MutableList<RelationTemplateMultipleLanguageContent> = mutableListOf()

) : ApplicationEntityBase() {
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
interface RelationTemplateRepository : ApplicationBaseRepository<RelationTemplate> {

	fun findAllBySourceArtifactId(id: Long): List<RelationTemplate>
	fun findAllByTargetArtifactId(id: Long): List<RelationTemplate>

	fun countBySourceStereotypeId(id: Long): Long
	fun countByTargetStereotypeId(id: Long): Long

	fun findAllByModelVersionIdAndSourceArtifactModelVersionIdNot(
		modelVerId: Long,
		attrModelVerId: Long
	): List<RelationTemplate>

	fun findAllByModelVersionIdAndTargetArtifactModelVersionIdNot(
		modelVerId: Long,
		attrModelVerId: Long
	): List<RelationTemplate>

	fun findAllByModelVersionIdAndSourceStereotypeModelVersionIdNot(
		modelVerId: Long,
		attrModelVerId: Long
	): List<RelationTemplate>

	fun findAllByModelVersionIdAndTargetStereotypeModelVersionIdNot(
		modelVerId: Long,
		attrModelVerId: Long
	): List<RelationTemplate>
}

/**
 * Multiple Language Content entity
 */
@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(
	name = "t_relation_template_mlcs", uniqueConstraints = [
		(UniqueConstraint(
			name = ApplicationConstraintCodes.RelationTemplateMCLUniqueIndex,
			columnNames = ["f_parent", "f_language_id", "f_field_name"]
		))
	]
)
class RelationTemplateMultipleLanguageContent(
	@get: NotNull
	@ManyToOne
	@JoinColumn(name = "f_parent", nullable = false)
	var parent: RelationTemplate? = null
) : MultipleLanguageContentBaseEntity() {
	override fun updatedParent(obj: ApplicationEntityBase?) { parent = obj as RelationTemplate? }
	override fun obtainParent(): ApplicationEntityBase? = parent
}

/**
 * Multiple Language Content Repository
 */
@RepositoryRestResource(exported = false)
interface RelationTemplateMLCRepository :
	MultipleLanguageContentBaseEntityRepository<RelationTemplateMultipleLanguageContent>