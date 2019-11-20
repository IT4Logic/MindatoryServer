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
import com.it4logic.mindatory.model.common.ApplicationMLCEntityBase
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntity
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntityRepository
import com.it4logic.mindatory.model.model.AttributeTemplate
import org.hibernate.envers.Audited
import org.hibernate.envers.NotAudited
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import javax.persistence.*
import javax.validation.constraints.NotNull


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
//	@JsonIgnore
	var attributeTemplate: AttributeTemplate,

	@ManyToOne(optional = false)
	@JoinColumn(name = "f_artifact_id", nullable = false)
	@JsonIgnore
	var artifact: ArtifactStore,

	@get: NotNull
	@get: MultipleLanguageContent
	@ManyToOne
	@JoinColumn(name = "f_project_id", nullable = false)
	var project: Project,

	@NotAudited
	@OneToMany
	@JoinColumn(name = "f_parent", referencedColumnName = "f_id")
	@JsonIgnore
	var mlcs: MutableList<AttributeStoreMultipleLanguageContent> = mutableListOf()

) : ApplicationMLCEntityBase() {
	@PrePersist
	@PreUpdate
	fun preSave() {
//        contentsRaw = ObjectMapper().writeValueAsString(contents)
	}

	@PostLoad
	fun postLoad() {
//        contents = ObjectMapper().readTree(contentsRaw)
	}

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
interface AttributeStoreRepository : //ApplicationProjectBaseRepository<AttributeStore>,
	ApplicationBaseRepository<AttributeStore> {
	//	fun countByAttributeTemplateId(id: Long): Long
//	fun countByAttributeTemplateRepositoryVersionId(id: Long): Long
//	fun countByAttributeTemplateRepositoryVersionId(id: Long): Long
//	fun countByAttributeTemplateVersionId(id: Long): Long
}

/**
 * Multiple Language Content support entity
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
class AttributeStoreMultipleLanguageContent : MultipleLanguageContentBaseEntity()

/**
 * Multiple Language Content support Repository
 */
@RepositoryRestResource(exported = false)
interface AttributeStoreMLCRepository :
	MultipleLanguageContentBaseEntityRepository<AttributeStoreMultipleLanguageContent>