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

import com.it4logic.mindatory.model.common.*
import javax.validation.constraints.Size
import javax.validation.constraints.NotBlank
import org.hibernate.envers.Audited
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import javax.persistence.*


/**
 * Application Repository entity that will be used as container for the whole application.
 */
@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "t_app_md", uniqueConstraints = [
    (UniqueConstraint(name = ApplicationConstraintCodes.ApplicationRepositorySolutionUniqueIndex, columnNames = ["majorVersion", "minorVersion", "buildVersion"]))
])
data class ApplicationMetadata (
    @get: NotBlank
    @get: Size(min = 1, max = 20)
    @Column(length = 20)
    var majorVersion: String,

    @get: NotBlank
    @get: Size(min = 1, max = 20)
    @Column(length = 20)
    var minorVersion: String,

    @get: NotBlank
    @get: Size(min = 1, max = 20)
    @Column(length = 20)
    var buildVersion: String,

    @get: Size(max = 255)
    var notes: String = ""

) : ApplicationEntityBase()

/**
 * ApplicationRepository Entity Rest Repository
 */
@RepositoryRestResource(exported = false)
interface ApplicationMetadataRepository : ApplicationBaseRepository<ApplicationMetadata>
