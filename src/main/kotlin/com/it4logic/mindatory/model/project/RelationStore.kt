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

import com.it4logic.mindatory.mlc.MultipleLanguageContent
import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.model.common.ApplicationEntityBase
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntity
import com.it4logic.mindatory.model.model.RelationTemplate
import org.hibernate.envers.Audited
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.Query
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import javax.persistence.*
import javax.validation.constraints.NotNull

/**
 * Relation Store entity
 */
@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "t_relation_stores")
data class RelationStore(
	@get: NotNull
	@get: MultipleLanguageContent
	@ManyToOne(optional = false)
	@JoinColumn(name = "f_source_artifact_id", nullable = false)
	var sourceArtifact: ArtifactStore,

	@get: NotNull
	@get: MultipleLanguageContent
	@ManyToOne(optional = false)
	@JoinColumn(name = "f_target_artifact_id", nullable = false)
	var targetArtifact: ArtifactStore,

	@get: MultipleLanguageContent
	@get: NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "f_relation_template_id", nullable = false)
	var relationTemplate: RelationTemplate,

	@Column(name = "f_store_status")
	var storeStatus: StoreObjectStatus = StoreObjectStatus.Active,

	@get: NotNull
	@ManyToOne
	@JoinColumn(name = "f_project_id", nullable = false)
	var project: Project

) : ApplicationEntityBase() {
	override fun obtainMLCs(): MutableList<MultipleLanguageContentBaseEntity> = mutableListOf()
}

/**
 * JPA Repository
 */
@RepositoryRestResource(exported = false)
interface RelationStoreRepository : ApplicationBaseRepository<RelationStore> {
	@Query("select coalesce(count(r.id),0) from RelationStore r where (r.sourceArtifact.id = ?1 or r.targetArtifact.id = ?1) and r.relationTemplate.id=?2")
	fun countAllByArtifactStoreAndRelationTemplate(storeId: Long, tmpId: Long): Long

	fun findAllBySourceArtifactId(id: Long): List<RelationStore>
	fun findAllByTargetArtifactId(id: Long): List<RelationStore>
	fun findAllByProjectId(id: Long): List<RelationStore>
}