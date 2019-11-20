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
import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.model.common.ApplicationMLCEntityBase
import com.it4logic.mindatory.model.common.ApplicationConstraintCodes
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntity
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntityRepository
import javax.validation.constraints.Size
import javax.validation.constraints.NotBlank
import org.hibernate.envers.Audited
import org.hibernate.envers.NotAudited
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import javax.persistence.*


/**
 * Model entity
 */
@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(
	name = "t_models", uniqueConstraints = []
)
data class Model(
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

	@OneToMany(mappedBy = "model", cascade = [CascadeType.ALL])
	var versions: MutableList<ModelVersion> = mutableListOf(),

	@NotAudited
	@OneToMany
	@JoinColumn(name = "f_parent", referencedColumnName = "f_id")
	@JsonIgnore
	var mlcs: MutableList<ModelMultipleLanguageContent> = mutableListOf()

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
interface ModelRepository : ApplicationBaseRepository<Model>

/**
 * Multiple Language Content entity
 */
@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(
	name = "t_model_mlcs", uniqueConstraints = [
		(UniqueConstraint(
			name = ApplicationConstraintCodes.ModelMCLUniqueIndex,
			columnNames = ["f_parent", "f_language_id", "f_field_name"]
		))
	]
)
class ModelMultipleLanguageContent : MultipleLanguageContentBaseEntity()

/**
 * Multiple Language Content Repository
 */
@RepositoryRestResource(exported = false)
interface ModelMLCRepository :
	MultipleLanguageContentBaseEntityRepository<ModelMultipleLanguageContent>