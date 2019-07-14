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
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntity
import org.hibernate.envers.Audited
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import org.springframework.data.jpa.repository.Query
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull


@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "t_artf_templ_vers", uniqueConstraints = [
    (UniqueConstraint(name = ApplicationConstraintCodes.ArtifactTemplateVersionUniqueIndex, columnNames = ["artifact_template_id", "designVersion"]))
])
data class ArtifactTemplateVersion (
    @get: MultipleLanguageContent
    @ManyToOne
    @JoinColumn(name = "artifact_template_id", nullable = false)
    var artifactTemplate: ArtifactTemplate,

    @get: MultipleLanguageContent
    @ManyToMany
    @JoinTable(name = "t_artf_vers_attr_verss_templs", joinColumns = [JoinColumn(name = "artifact_version_id")], inverseJoinColumns = [JoinColumn(name = "attribute_version_id")])
    var attributes: MutableList<AttributeTemplateVersion> = mutableListOf(),

    var designStatus: DesignStatus = DesignStatus.InDesign,

    var designVersion: Int = 1,

    @ManyToOne
    @JoinColumn(name = "repository_id", nullable = false)
    @JsonIgnore
    var repository: ApplicationRepository? = null,

    @ManyToOne
    @JoinColumn(name = "solution_id")
    @JsonIgnore
    var solution: Solution? = null

) : ApplicationMLCEntityBase() {
    override fun obtainMLCs(): MutableList<MultipleLanguageContentBaseEntity> = mutableListOf()
}

@RepositoryRestResource(exported = false)
interface ArtifactTemplateVersionRepository : ApplicationRepositoryBaseRepository<ArtifactTemplateVersion> {
    fun countByRepositoryIdNotAndAttributes_RepositoryId(id1: Long, id2: Long): Long
    fun countByAttributes_Id(id: Long): Long

    fun findOneByArtifactTemplateIdAndDesignStatus(id: Long, designStatus: DesignStatus): Optional<ArtifactTemplateVersion>

    @Query("select coalesce(max(designVersion),0) from ArtifactTemplateVersion a where a.artifactTemplate.id = ?1")
    fun maxDesignVersion(id: Long): Int

    fun findOneByIdAndArtifactTemplateId(versionId: Long, artifactId: Long): Optional<ArtifactTemplateVersion>
}