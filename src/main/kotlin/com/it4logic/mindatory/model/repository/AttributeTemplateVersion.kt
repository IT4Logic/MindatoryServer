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
import com.it4logic.mindatory.api.plugins.AttributeTemplateDataType
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
import kotlin.collections.HashMap


@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "t_attribute_template_versions", uniqueConstraints = [
    (UniqueConstraint(name = ApplicationConstraintCodes.AttributeTemplateVersionUniqueIndex, columnNames = ["attribute_template_id", "designVersion"]))
])
data class AttributeTemplateVersion (
    @ManyToOne
    @JoinColumn(name = "attribute_template_id", nullable = false)
    @JsonIgnore
    var attributeTemplate: AttributeTemplate,

    @get: NotNull
    var typeUUID: String,

    @Transient
    @JsonIgnore
    var type: AttributeTemplateDataType? = null,

    @Lob
    var properties: HashMap<String, Any> = hashMapOf(),

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

) : ApplicationEntityBase()


@RepositoryRestResource(exported = false)
interface AttributeTemplateVersionRepository : ApplicationRepositoryBaseRepository<AttributeTemplateVersion> {
    fun findOneByIdAndAttributeTemplateId(id: Long, id2: Long): Optional<AttributeTemplateVersion>
    fun findOneByAttributeTemplateIdAndDesignStatus(id: Long, designStatus: DesignStatus): Optional<AttributeTemplateVersion>

    @Query("select coalesce(max(designVersion),0) from AttributeTemplateVersion a where a.attributeTemplate.id = ?1")
    fun maxDesignVersion(id: Long): Int

}