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

package com.it4logic.mindatory.model

import com.it4logic.mindatory.languages.*
import com.it4logic.mindatory.model.common.*
import javax.validation.constraints.Size
import javax.validation.constraints.NotBlank
import org.hibernate.envers.Audited
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import javax.persistence.*


/**
 * Solution entity that will be used as container for the whole application.
 * One database can have one or more solutions. Solution will act like a database but without the need to change the database and that will be at runtime.
 */
@Audited
@Entity
@EntityListeners(AuditingEntityListener::class, MultipleLanguageContentEntityListener::class)
@MultipleLanguageContentEntity(SolutionMultipleLanguageContent::class, SolutionLanguageContentRepository::class)
@Table(name = "t_solutions", uniqueConstraints = [
//    (UniqueConstraint(name = ApplicationConstraintCodes.SolutionNameUniqueIndex, columnNames = ["name"]))
])
data class Solution (
        @get: NotBlank
        @get: Size(min = 2, max = 100)
//        @Column(nullable = false, length = 255)
        @Transient
        @MultipleLanguageContent
        var name: String,

        @get: Size(max = 255)
        @Column(length = 255)
        var description: String = ""

) : ApplicationCompanyEntityBase()

/**
 * Repository
 */
@RepositoryRestResource(exported = false)
interface SolutionRepository : ApplicationCompanyBaseRepository<Solution>

/**
 * Multiple Language Content support entity
  */

@Audited
@Entity
@EntityListeners(AuditingEntityListener::class, MultipleLanguageContentEntityListener::class)
@Table(name = "t_solution_mlcs", uniqueConstraints = [
        (UniqueConstraint(name = ApplicationConstraintCodes.SolutionNameUniqueIndex, columnNames = ["solution_id", "language_id", "fieldName"]))
])
class SolutionMultipleLanguageContent : MultipleLanguageContentBaseEntity()

/**
 * Multiple Language Content support Repository
 */
@RepositoryRestResource(exported = false)
interface SolutionLanguageContentRepository : MultipleLanguageContentBaseEntityRepository<SolutionMultipleLanguageContent> {

}