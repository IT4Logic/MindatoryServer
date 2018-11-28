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

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.it4logic.mindatory.api.plugins.AttributeTemplateDataType
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
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull


@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "t_attribute_template_versions", uniqueConstraints = [
    (UniqueConstraint(name = ApplicationConstraintCodes.AttributeTemplateVersionUniqueIndex, columnNames = ["attribute_template_id", "designVersion"]))
])
data class AttributeTemplateVersion (
    @get: NotNull
    @ManyToOne
    @JoinColumn(name = "attribute_template_id", nullable = false)
    var attributeTemplate: AttributeTemplate,

    @get: NotNull
    var typeUUID: String,

//    @Transient
//    var type: AttributeTemplateDataType? = null,

    @get: NotBlank
    @Lob
    var properties: String,

    @Transient
    var propertiesJson: JsonNode,

    var designStatus: DesignStatus = DesignStatus.InDesign,

    var designVersion: Int = 1

) : ApplicationRepositoryEntityBase() {
    @PrePersist
    @PreUpdate
    fun preSave() {
        properties = ObjectMapper().writeValueAsString(propertiesJson)
    }

    @PostLoad
    fun postLoad() {
        propertiesJson = ObjectMapper().readTree(properties)
    }
}

@RepositoryRestResource(exported = false)
interface AttributeTemplateVersionRepository : ApplicationRepositoryBaseRepository<AttributeTemplateVersion> {
    fun findOneByIdAndAttributeTemplateId(id: Long, id2: Long): Optional<AttributeTemplateVersion>
    fun findOneByAttributeTemplateIdAndDesignStatus(id: Long, designStatus: DesignStatus): Optional<AttributeTemplateVersion>

    @Query("select max(designVersion) from AttributeTemplateVersion a where a.attributeTemplate.id = ?1")
    fun maxDesignVersion(id: Long): Int

}