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

package com.it4logic.mindatory.model.project

import com.fasterxml.jackson.annotation.JsonIgnore
import com.it4logic.mindatory.mlc.MultipleLanguageContent
import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.model.common.ApplicationConstraintCodes
import com.it4logic.mindatory.model.common.ApplicationEntityBase
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntity
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntityRepository
import com.it4logic.mindatory.model.model.AttributeTemplate
import io.leangen.graphql.annotations.GraphQLIgnore
import org.hibernate.annotations.LazyCollection
import org.hibernate.annotations.LazyCollectionOption
import org.hibernate.envers.Audited
import org.hibernate.envers.NotAudited
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import javax.persistence.*
import javax.validation.constraints.NotNull

/**
 * Attribute Store entity
 */
@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "t_attribute_stores")
data class AttributeStore(
	@get: MultipleLanguageContent
	@Transient
	var contents: String = "",

	@Column(name = "f_store_status")
	var storeStatus: StoreObjectStatus = StoreObjectStatus.Active,

	@get: NotNull
	@get: MultipleLanguageContent
	@ManyToOne(optional = false)
	@JoinColumn(name = "f_attribute_template_id", nullable = false)
	var attributeTemplate: AttributeTemplate,

	@ManyToOne(optional = false)
	@JoinColumn(name = "f_artifact_id", nullable = false)
	var artifact: ArtifactStore,

	@get: NotNull
	@ManyToOne
	@JoinColumn(name = "f_project_id", nullable = false)
	var project: Project,

	@NotAudited
	@OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, mappedBy = "parent")
	@LazyCollection(LazyCollectionOption.FALSE)
	@JsonIgnore
	@get: GraphQLIgnore
	var mlcs: MutableList<AttributeStoreMultipleLanguageContent> = mutableListOf()

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
interface AttributeStoreRepository : ApplicationBaseRepository<AttributeStore>

/**
 * Multiple Language Content entity
 */
@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(
	name = "t_attribute_store_mlcs", uniqueConstraints = [
		(UniqueConstraint(
			name = ApplicationConstraintCodes.AttributeStoreMCLUniqueIndex,
			columnNames = ["f_parent", "f_language_id", "f_field_name"]
		))
	]
)
class AttributeStoreMultipleLanguageContent(
	@get: NotNull
	@ManyToOne
	@JoinColumn(name = "f_parent", nullable = false)
	var parent: AttributeStore? = null
) : MultipleLanguageContentBaseEntity() {
	override fun updatedParent(obj: ApplicationEntityBase?) { parent = obj as AttributeStore? }
	override fun obtainParent(): ApplicationEntityBase? = parent
}

/**
 * Multiple Language Content Repository
 */
@RepositoryRestResource(exported = false)
interface AttributeStoreMLCRepository :
	MultipleLanguageContentBaseEntityRepository<AttributeStoreMultipleLanguageContent>