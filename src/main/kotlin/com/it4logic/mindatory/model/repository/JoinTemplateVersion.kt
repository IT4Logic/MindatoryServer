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

import com.fasterxml.jackson.annotation.JsonIgnore
import com.it4logic.mindatory.mlc.MultipleLanguageContent
import com.it4logic.mindatory.model.ApplicationRepository
import com.it4logic.mindatory.model.Solution
import com.it4logic.mindatory.model.common.*
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
@Table(name = "t_join_template_versions")
data class JoinTemplateVersion (
    @ManyToOne
    @JoinColumn(name = "join_template_id", nullable = false)
    @JsonIgnore
    var joinTemplate: JoinTemplate,

    @get: NotNull
    @get: MultipleLanguageContent
    @ManyToOne(optional = false)
    @JoinColumn(name = "source_stereotype_id", nullable = false)
    var sourceStereotype: Stereotype,

    @ManyToMany
    @JoinTable(name = "t_source_artifact_join_templates", joinColumns = [JoinColumn(name = "join_id")], inverseJoinColumns = [JoinColumn(name = "artifact_id")])
    var sourceArtifacts: MutableList<ArtifactTemplateVersion> = mutableListOf(),

    @get: NotNull
    @get: MultipleLanguageContent
    @ManyToOne(optional = false)
    @JoinColumn(name = "target_stereotype_id", nullable = false)
    var targetStereotype: Stereotype,

    @ManyToMany
    @JoinTable(name = "t_target_artifact_join_templates", joinColumns = [JoinColumn(name = "join_id")], inverseJoinColumns = [JoinColumn(name = "artifact_id")])
    var targetArtifacts: MutableList<ArtifactTemplateVersion> = mutableListOf(),

    var designStatus: DesignStatus = DesignStatus.InDesign,

    var designVersion: Int = 1,

    @ManyToOne(optional = false)
    @JoinColumn(name = "repository_id", nullable = false)
    @JsonIgnore
    var repository: ApplicationRepository? = null,

    @ManyToOne(optional=true)
    @JoinColumn(name = "solution_id")
    @JsonIgnore
    var solution: Solution? = null

) : ApplicationEntityBase()

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
    fun findOneByIdAndJoinTemplateId(id1: Long, id2: Long): Optional<JoinTemplateVersion>

    @Query("select coalesce(max(designVersion),0) from JoinTemplateVersion a where a.joinTemplate.id = ?1")
    fun maxDesignVersion(id: Long): Int
}