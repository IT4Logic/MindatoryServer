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

import com.it4logic.mindatory.model.common.ApplicationConstraintCodes
import com.it4logic.mindatory.model.common.ApplicationRepositoryBaseRepository
import com.it4logic.mindatory.model.common.ApplicationRepositoryEntityBase
import org.hibernate.envers.Audited
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "t_stereotypes", uniqueConstraints = [
    (UniqueConstraint(name = ApplicationConstraintCodes.StereotypeNameUniqueIndex, columnNames = ["name"]))
])
data class Stereotype (
    @get: NotBlank
    @get: Size(min = 2, max = 50)
    @Column(nullable = false, length = 255)
    var name: String,

    @get: Size(max = 255)
    @Column(length = 255)
    var description: String = ""

) : ApplicationRepositoryEntityBase()

/**
 * Repository
 */
@RepositoryRestResource(exported = false)
interface StereotypeRepository : ApplicationRepositoryBaseRepository<Stereotype>