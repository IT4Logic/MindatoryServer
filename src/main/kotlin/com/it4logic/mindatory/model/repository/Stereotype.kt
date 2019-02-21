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

import com.it4logic.mindatory.mlc.MultipleLanguageContent
import com.it4logic.mindatory.model.ApplicationRepository
import com.it4logic.mindatory.model.Solution
import com.it4logic.mindatory.model.common.*
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
@Table(name = "t_stereotypes", uniqueConstraints = [])
data class Stereotype (
    @get: NotBlank
    @get: Size(min = 2, max = 100)
    @get: MultipleLanguageContent
    @Transient
    var name: String,

    @get: Size(max = 255)
    @get: MultipleLanguageContent
    @Transient
    var description: String = "",

    @get: NotNull
    @get: MultipleLanguageContent
    @ManyToOne(optional = false)
    @JoinColumn(name = "repository_id", nullable = false)
    var repository: ApplicationRepository? = null,

    @get: MultipleLanguageContent
    @ManyToOne(optional=true)
    @JoinColumn(name = "solution_id")
    var solution: Solution? = null

) : ApplicationEntityBase()

/**
 * Repository
 */
@RepositoryRestResource(exported = false)
interface StereotypeRepository : ApplicationRepositoryBaseRepository<Stereotype>


/**
 * Multiple Language Content support entity
 */
@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "t_stereotype_mlcs", uniqueConstraints = [
    (UniqueConstraint(name = ApplicationConstraintCodes.StereotypeMCLUniqueIndex, columnNames = ["parent", "languageId", "fieldName"]))
])
class StereotypeMultipleLanguageContent : MultipleLanguageContentBaseEntity()

/**
 * Multiple Language Content support Repository
 */
@RepositoryRestResource(exported = false)
interface StereotypeMLCRepository : MultipleLanguageContentBaseEntityRepository<StereotypeMultipleLanguageContent>