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
import com.it4logic.mindatory.model.common.ApplicationConstraintCodes
import com.it4logic.mindatory.model.common.ApplicationEntityBase
import com.it4logic.mindatory.model.common.ApplicationRepositoryBaseRepository
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntity
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntityRepository
import org.hibernate.envers.Audited
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "t_join_templates", uniqueConstraints = [
    (UniqueConstraint(name = ApplicationConstraintCodes.JoinTemplateIdentifierUniqueIndex, columnNames = ["identifier"]))
])
data class JoinTemplate (
    @get: NotBlank
    @get: Size(min = 2, max = 255)
    @Column(nullable = false, length = 255)
    var identifier: String,

    @get: Size(max = 255)
    @get: MultipleLanguageContent
    @Transient
    var description: String = "",

    @JsonIgnore
    @OneToMany(mappedBy = "joinTemplate", cascade = [CascadeType.ALL])
    var versions: MutableList<JoinTemplateVersion> = mutableListOf(),

    @get: NotNull
    @get: MultipleLanguageContent
    @ManyToOne(optional = false)
    @JoinColumn(name = "repository_id", nullable = false)
    var repository: ApplicationRepository? = null,

    @get: MultipleLanguageContent
    @ManyToOne(optional=true)
    @JoinColumn(name = "solution_id")
    var solution: Solution? = null

) : ApplicationEntityBase() {
    fun createDesignVersion(sourceStereotype: Stereotype, sourceArtifacts: MutableList<ArtifactTemplateVersion>,
                            targetStereotype: Stereotype, targetArtifacts: MutableList<ArtifactTemplateVersion>): JoinTemplateVersion {
        return JoinTemplateVersion (
            sourceStereotype = sourceStereotype,
            sourceArtifacts = sourceArtifacts,
            targetStereotype = targetStereotype,
            targetArtifacts = targetArtifacts,
            joinTemplate = this,
            repository = repository,
            solution = solution
        )
    }
}

/**
 * Repository
 */
@RepositoryRestResource(exported = false)
interface JoinTemplateRepository : ApplicationRepositoryBaseRepository<JoinTemplate>


/**
 * Multiple Language Content support entity
 */
@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "t_join_template_mlcs", uniqueConstraints = [
    (UniqueConstraint(name = ApplicationConstraintCodes.JoinTemplateMCLUniqueIndex, columnNames = ["parentId", "languageId", "fieldName"]))
])
class JoinTemplateMultipleLanguageContent : MultipleLanguageContentBaseEntity()

/**
 * Multiple Language Content support Repository
 */
@RepositoryRestResource(exported = false)
interface JoinTemplateMLCRepository : MultipleLanguageContentBaseEntityRepository<JoinTemplateMultipleLanguageContent>