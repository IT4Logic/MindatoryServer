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

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.it4logic.mindatory.mlc.MultipleLanguageContent
import com.it4logic.mindatory.model.ApplicationRepository
import com.it4logic.mindatory.model.Solution
import com.it4logic.mindatory.model.common.ApplicationMLCEntityBase
import com.it4logic.mindatory.model.common.ApplicationSolutionBaseRepository
import com.it4logic.mindatory.model.common.StoreObjectStatus
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntity
import com.it4logic.mindatory.model.repository.AttributeTemplate
import com.it4logic.mindatory.model.repository.AttributeTemplateVersion
import org.hibernate.envers.Audited
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "t_attr_stores")
data class AttributeStore (
    @Lob
    var contents: String = "",

//    @Transient
//    var contents: JsonNode,

//    @get: NotNull
    @get: MultipleLanguageContent
    @ManyToOne(optional = false)
    @JoinColumn(name = "attribute_template_id", nullable = false)
    @JsonIgnore
    var attributeTemplate: AttributeTemplate?= null,

    @get: NotNull
    @get: MultipleLanguageContent
    @ManyToOne(optional = false)
    @JoinColumn(name = "attribute_template_ver_id", nullable = false)
    var attributeTemplateVersion: AttributeTemplateVersion,

    var storeStatus: StoreObjectStatus = StoreObjectStatus.Active,

//    @JsonIgnore
//    @ManyToMany(mappedBy = "attributeStores")
//    var artifactStores: MutableList<ArtifactStore> = mutableListOf(),

//    @get: NotNull
    @get: MultipleLanguageContent
    @ManyToOne
    @JoinColumn(name = "solution_id", nullable = false)
    var solution: Solution? = null

) : ApplicationMLCEntityBase() {
    @PrePersist
    @PreUpdate
    fun preSave() {
//        contentsRaw = ObjectMapper().writeValueAsString(contents)
    }

    @PostLoad
    fun postLoad() {
//        contents = ObjectMapper().readTree(contentsRaw)
    }

    override fun obtainMLCs(): MutableList<MultipleLanguageContentBaseEntity> = mutableListOf()
}

/**
 * Repository
 */
@RepositoryRestResource(exported = false)
interface AttributeStoreRepository : ApplicationSolutionBaseRepository<AttributeStore> {
    fun countByAttributeTemplateRepositoryId(id: Long): Long
    fun countByAttributeTemplateVersionId(id: Long): Long
}