/*
    Copyright (c) 2017, IT4Logic.

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

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.JsonIdentityReference
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import com.it4logic.mindatory.model.common.ApplicationCompanyBaseRepository
import com.it4logic.mindatory.model.common.ApplicationCompanyEntityBase
import com.it4logic.mindatory.model.common.ApplicationConstraintCodes
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
@EntityListeners(AuditingEntityListener::class)
@Table(name = "t_repositories", uniqueConstraints = [
//    (UniqueConstraint(name = ApplicationConstraintCodes.SolutionNameUniqueIndex, columnNames = ["name"]))
])
data class ApplicationRepository (
    @get: NotBlank
    @get: Size(min = 2, max = 255)
    @Column(nullable = false, length = 255)
    var name: String = "",

        @ManyToOne(optional=false)
        @JoinColumn(name = "solution_id")
        var solution: Solution? = null

) : ApplicationCompanyEntityBase()

/**
 * Solution Entity Rest Repository
 */
@RepositoryRestResource(path = "repositories", collectionResourceRel = "repositories")
interface RepoRepository : ApplicationCompanyBaseRepository<ApplicationRepository>
