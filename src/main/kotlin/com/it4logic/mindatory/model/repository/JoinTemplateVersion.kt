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

package com.it4logic.mindatory.model.repository

import com.it4logic.mindatory.model.common.ApplicationConstraintCodes
import com.it4logic.mindatory.model.common.ApplicationRepositoryBaseRepository
import com.it4logic.mindatory.model.common.ApplicationRepositoryEntityBase
import com.it4logic.mindatory.model.common.DesignStatus
import org.hibernate.envers.Audited
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.Query
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull


@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "t_join_templates", uniqueConstraints = [
    (UniqueConstraint(name = ApplicationConstraintCodes.JoinTemplateNameUniqueIndex, columnNames = ["name"]))
])
data class JoinTemplateVersion (
    @get: NotNull
    @ManyToOne
    @JoinColumn(name = "join_template_id", nullable = false)
    var joinTemplate: JoinTemplate,

    @get: NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "source_stereotype_id", nullable = false)
    var sourceStereotype: Stereotype,

    @ManyToMany()
    @JoinTable(name = "t_artifact_join_templates", joinColumns = [JoinColumn(name = "join_id")], inverseJoinColumns = [JoinColumn(name = "artifact_id")])
    var sourceArtifacts: MutableList<ArtifactTemplate> = mutableListOf(),

    @get: NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "target_stereotype_id", nullable = false)
    var targetStereotype: Stereotype,

    @ManyToMany()
    @JoinTable(name = "t_artifact_join_templates", joinColumns = [JoinColumn(name = "join_id")], inverseJoinColumns = [JoinColumn(name = "artifact_id")])
    var targetArtifacts: MutableList<ArtifactTemplate> = mutableListOf(),

    var designStatus: DesignStatus = DesignStatus.InDesign,

    var designVersion: Int = 1

) : ApplicationRepositoryEntityBase()

/**
 * Repository
 */
@RepositoryRestResource(exported = false)
interface JoinTemplateVersionRepository : ApplicationRepositoryBaseRepository<JoinTemplateVersion> {
    fun countByRepositoryIdNotAndSourceArtifacts_RepositoryId(id1: Long, id2: Long): Long
    fun countByRepositoryIdNotAndTargetArtifacts_RepositoryId(id1: Long, id2: Long): Long

    fun countByRepositoryIdNotAndSourceStereotypeRepositoryId(id1: Long, id2: Long): Long
    fun countByRepositoryIdNotAndTargetStereotypeRepositoryId(id1: Long, id2: Long): Long

    fun countBySourceArtifacts_Id(id: Long): Long
    fun countByTargetArtifacts_Id(id: Long): Long

    fun countBySourceStereotypeId(id: Long): Long
    fun countByTargetStereotypeId(id: Long): Long

    fun findOneByJoinTemplateIdAndDesignStatus(id: Long, designStatus: DesignStatus): Optional<JoinTemplateVersion>

    @Query("select max(designVersion) from JoinTemplateVersion a where a.joinTemplate.id = ?1")
    fun maxDesignVersion(id: Long): Int
}