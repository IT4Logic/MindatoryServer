/*
    Copyright (c) 2018, IT4Logic.

    This file is part of Mindatory solution by IT4Logic.

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

package com.it4logic.mindatory.model.store

import com.it4logic.mindatory.model.common.ApplicationSolutionBaseRepository
import com.it4logic.mindatory.model.common.ApplicationSolutionEntityBase
import com.it4logic.mindatory.model.common.StoreObjectStatus
import com.it4logic.mindatory.model.repository.JoinTemplate
import com.it4logic.mindatory.model.repository.JoinTemplateVersion
import org.hibernate.envers.Audited
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import javax.persistence.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull


@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "t_join_stores")
data class JoinStore (
    @get: NotEmpty
    @ManyToMany()
    @JoinTable(name = "t_source_artifact_join_stores", joinColumns = [JoinColumn(name = "join_id")], inverseJoinColumns = [JoinColumn(name = "artifact_id")])
    var sourceArtifacts: MutableList<ArtifactStore> = mutableListOf(),

    @get: NotEmpty
    @ManyToMany()
    @JoinTable(name = "t_target_artifact_join_stores", joinColumns = [JoinColumn(name = "join_id")], inverseJoinColumns = [JoinColumn(name = "artifact_id")])
    var targetArtifacts: MutableList<ArtifactStore> = mutableListOf(),

    @get: NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "join_template_id", nullable = false)
    var joinTemplate: JoinTemplate,

    @get: NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "join_template_ver_id", nullable = false)
    var joinTemplateVersion: JoinTemplateVersion,

    var storeStatus: StoreObjectStatus = StoreObjectStatus.Active

) : ApplicationSolutionEntityBase()

/**
 * Repository
 */
@RepositoryRestResource(exported = false)
interface JoinStoreRepository : ApplicationSolutionBaseRepository<JoinStore> {
    fun countByJoinTemplateRepositoryId(id: Long): Long
    fun countByJoinTemplateVersionId(id: Long): Long

    fun findAllByJoinTemplateVersionId(id: Long): List<JoinStore>

    fun countByJoinTemplateVersionIdAndSourceArtifacts_Id(id1: Long, id2: Long): Long
    fun countByJoinTemplateVersionIdAndTargetArtifacts_Id(id1: Long, id2: Long): Long

}