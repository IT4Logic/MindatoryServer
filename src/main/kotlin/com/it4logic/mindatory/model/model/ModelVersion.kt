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

import com.it4logic.mindatory.mlc.MultipleLanguageContent
import com.it4logic.mindatory.model.common.*
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntity
import org.hibernate.envers.Audited
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.*
import javax.persistence.*
import javax.validation.constraints.Size

/**
 * Enumerator for Model Version Status
 */
enum class ModelVersionStatus(private val status: Int) {
	InDesign(0),
	Released(1),
	Obsoleted(2)
}

/**
 * Model Version entity
 */
@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(
	name = "t_model_versions", uniqueConstraints = [
		(UniqueConstraint(
			name = ApplicationConstraintCodes.ModelVersionUniqueIndex,
			columnNames = ["f_model_id", "f_design_version"]
		))
	]
)
data class ModelVersion(
	@get: Size(max = 50)
	var identifier: String,

	@get: MultipleLanguageContent
	@ManyToOne
	@JoinColumn(name = "f_model_id", nullable = false)
	var model: Model,

	@Column(name = "f_model_ver_status")
	var status: ModelVersionStatus = ModelVersionStatus.InDesign,

	@Column(name = "f_design_version")
	var designVersion: Int = 1,

	@Lob
	@Column(name = "f_metadata")
	var metadata: String = "",

	@ManyToMany(cascade = [CascadeType.ALL])
	@JoinTable(
		name = "t_m2m_model_ver_model_dependencies",
		joinColumns = [JoinColumn(name = "f_dependent_id")],
		inverseJoinColumns = [JoinColumn(name = "f_dependency_id")]
	)
	var modelDependencies: MutableList<ModelVersion> = mutableListOf()
) : ApplicationMLCEntityBase() {
	override fun obtainMLCs(): MutableList<MultipleLanguageContentBaseEntity> = mutableListOf()
}


/**
 * JPA Repository
 */
@RepositoryRestResource(exported = false)
interface ModelVersionRepository : ApplicationBaseRepository<ModelVersion> {
	fun countAllByModelDependencies_Id(id: Long): Long
	fun findOneByModelIdAndStatus(
		appRepoId: Long,
		modelVersionStatus: ModelVersionStatus
	): Optional<ModelVersion>

	fun findAllByStatusAndModelId(status: ModelVersionStatus, id: Long): List<ModelVersion>
	fun findOneByStatusAndModelId(
		status: ModelVersionStatus,
		id: Long
	): Optional<ModelVersion>

	fun findAllByStatus(status: ModelVersionStatus): List<ModelVersion>

	@Query("select coalesce(max(designVersion),0) from ModelVersion o where o.model.id = ?1")
	fun maxDesignVersion(id: Long): Int
}

