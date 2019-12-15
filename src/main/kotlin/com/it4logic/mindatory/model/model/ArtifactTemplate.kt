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
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size
import javax.persistence.*
import javax.validation.constraints.NotNull

/**
 * Artifact Template Entity
 */
@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(
	name = "t_artifact_templates", uniqueConstraints = [
		(UniqueConstraint(
			name = ApplicationConstraintCodes.ArtifactTemplateIdentifierUniqueIndex,
			columnNames = ["f_identifier"]
		))
	]
)
data class ArtifactTemplate(
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

	@Lob
	@Column(name = "f_metadata")
	var metadata: String = "",

	@get: Size(max = 50)
	@Column(name = "f_g_identifier")
	var globalIdentifier: String = "",

	@ManyToOne
	@JoinColumn(name = "f_model_ver_id", nullable = false)
	var modelVersion: ModelVersion? = null,

	@get: MultipleLanguageContent
	@OneToMany(mappedBy = "artifact")
	var attributes: MutableList<AttributeTemplate> = mutableListOf(),

	@NotAudited
	@OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, mappedBy = "parent")
	@LazyCollection(LazyCollectionOption.FALSE)
	@JsonIgnore
	@get: GraphQLIgnore
	var mlcs: MutableList<ArtifactTemplateMultipleLanguageContent> = mutableListOf()

) : ApplicationEntityBase() {

	@Suppress("SENSELESS_COMPARISON", "UNCHECKED_CAST")
	override fun obtainMLCs(): MutableList<MultipleLanguageContentBaseEntity> {
		if (mlcs == null)
			mlcs = mutableListOf()
		return mlcs as MutableList<MultipleLanguageContentBaseEntity>
	}
}

/**
 * Repository
 */
@RepositoryRestResource(exported = false)
interface ArtifactTemplateRepository : ApplicationBaseRepository<ArtifactTemplate> {

	fun findAllByModelVersionIdAndAttributes_ModelVersionIdNot(
		modelVerId: Long,
		attrModelVerId: Long
	): List<ArtifactTemplate>

	fun findAllByModelVersionId(modelVerId: Long): List<ArtifactTemplate>
}


/**
 * Multiple Language Content entity
 */
@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(
	name = "t_artifact_template_mlcs", uniqueConstraints = [
		(UniqueConstraint(
			name = ApplicationConstraintCodes.ArtifactTemplateMCLUniqueIndex,
			columnNames = ["f_parent", "f_language_id", "f_field_name"]
		))
	]
)
class ArtifactTemplateMultipleLanguageContent(
	@get: NotNull
	@ManyToOne
	@JoinColumn(name = "f_parent", nullable = false)
	var parent: ArtifactTemplate? = null
) : MultipleLanguageContentBaseEntity() {
	override fun updatedParent(obj: ApplicationEntityBase?) { parent = obj as ArtifactTemplate? }
	override fun obtainParent(): ApplicationEntityBase? = parent
}

/**
 * Multiple Language Content Repository
 */
@RepositoryRestResource(exported = false)
interface ArtifactTemplateMLCRepository :
	MultipleLanguageContentBaseEntityRepository<ArtifactTemplateMultipleLanguageContent>