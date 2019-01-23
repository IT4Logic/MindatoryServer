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
import com.it4logic.mindatory.model.repository.ArtifactTemplate
import com.it4logic.mindatory.model.repository.ArtifactTemplateVersion
import org.hibernate.envers.Audited
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import javax.persistence.*
import javax.validation.constraints.NotNull


@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "t_artifact_stores")
data class ArtifactStore (
    @get: NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "artifact_template_id", nullable = false)
    var artifactTemplate: ArtifactTemplate,

    @get: NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "artifact_template_ver_id", nullable = false)
    var artifactTemplateVersion: ArtifactTemplateVersion,

    @ManyToMany()
    @JoinTable(name = "t_artifact_attribute_stores", joinColumns = [JoinColumn(name = "artifact_id")], inverseJoinColumns = [JoinColumn(name = "attribute_id")])
    var attributeStores: MutableList<AttributeStore> = mutableListOf(),

    var storeStatus: StoreObjectStatus = StoreObjectStatus.Active

) : ApplicationSolutionEntityBase()

/**
 * Repository
 */
@RepositoryRestResource(exported = false)
interface ArtifactStoreRepository : ApplicationSolutionBaseRepository<ArtifactStore> {
    fun countByArtifactTemplateRepositoryId(id: Long): Long
    fun countByArtifactTemplateVersionId(id: Long): Long
    fun countByArtifactTemplateId(id: Long): Long

    fun findAllByArtifactTemplateVersionId(id: Long): List<ArtifactStore>

}