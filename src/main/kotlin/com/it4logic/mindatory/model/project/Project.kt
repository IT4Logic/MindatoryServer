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
import com.it4logic.mindatory.mlc.*
import com.it4logic.mindatory.model.common.*
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntity
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntityRepository
import com.it4logic.mindatory.model.model.ModelVersion
import io.leangen.graphql.annotations.GraphQLIgnore
import javax.validation.constraints.Size
import javax.validation.constraints.NotBlank
import org.hibernate.envers.Audited
import org.hibernate.envers.NotAudited
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import javax.persistence.*


/**
 * Project entity that will be used as container for the whole application.
 * One database can have one or more projects. Project will act like a database
 * but without the need to change the database and that will be at runtime.
 */
@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "t_projects", uniqueConstraints = [])
data class Project(
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

	@ManyToMany(cascade = [CascadeType.ALL])
	@JoinTable(
		name = "t_m2m_project_model_ver_depends",
		joinColumns = [JoinColumn(name = "f_dependent_id")],
		inverseJoinColumns = [JoinColumn(name = "f_dependency_id")]
	)
	var repositoryDependencies: MutableList<ModelVersion> = mutableListOf(),

	@NotAudited
	@OneToMany
	@JoinColumn(name = "f_parent", referencedColumnName = "f_id")
	@JsonIgnore
	@get: GraphQLIgnore
	var mlcs: MutableList<ProjectMultipleLanguageContent> = mutableListOf()

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
interface ProjectRepository : ApplicationBaseRepository<Project> {
	fun countAllByRepositoryDependencies_Id(id: Long): Boolean
}

/**
 * Multiple Language Content entity
 */
@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(
	name = "t_project_mlcs", uniqueConstraints = [
		(UniqueConstraint(
			name = ApplicationConstraintCodes.ProjectMCLUniqueIndex,
			columnNames = ["f_parent", "f_language_id", "f_field_name"]
		))
	]
)
class ProjectMultipleLanguageContent : MultipleLanguageContentBaseEntity()

/**
 * Multiple Language Content Repository
 */
@RepositoryRestResource(exported = false)
interface ProjectMLCRepository : MultipleLanguageContentBaseEntityRepository<ProjectMultipleLanguageContent>