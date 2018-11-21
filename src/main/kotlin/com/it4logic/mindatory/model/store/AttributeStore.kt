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

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.it4logic.mindatory.model.common.ApplicationSolutionBaseRepository
import com.it4logic.mindatory.model.common.ApplicationSolutionEntityBase
import com.it4logic.mindatory.model.repository.AttributeTemplate
import org.hibernate.envers.Audited
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "t_attribute_stores")
data class AttributeStore (

    @get: NotBlank
    @Lob
    var contents: String,

    @Transient
    var jsonContents: JsonNode,

    @get: NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "attribute_template_id", nullable = false)
    var attributeTemplate: AttributeTemplate? = null

) : ApplicationSolutionEntityBase() {
    @PrePersist
    @PreUpdate
    fun preSave() {
        contents = ObjectMapper().writeValueAsString(jsonContents)
    }

    @PostLoad
    fun postLoad() {
        jsonContents = ObjectMapper().readTree(contents)
    }
}

/**
 * Repository
 */
@RepositoryRestResource(exported = false)
interface AttributeStoreRepository : ApplicationSolutionBaseRepository<AttributeStore> {
    fun countByAttributeTemplateRepositoryId(id: Long): Long
    fun countByAttributeTemplateId(id: Long): Long
}