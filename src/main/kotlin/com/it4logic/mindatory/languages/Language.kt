/*
    Copyright (c) 2018, IT4Logic. All rights reserved.

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

package com.it4logic.mindatory.languages

import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.model.common.ApplicationConstraintCodes
import com.it4logic.mindatory.model.common.ApplicationEntityBase
import javax.validation.constraints.Size
import javax.validation.constraints.NotBlank
import org.hibernate.envers.Audited
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.*
import javax.persistence.*

@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "t_languages", uniqueConstraints = [
        (UniqueConstraint(name = ApplicationConstraintCodes.LanguageLocaleUniqueIndex, columnNames = ["locale"])),
        (UniqueConstraint(name = ApplicationConstraintCodes.LanguageNameUniqueIndex, columnNames = ["name"]))
])
data class Language (
        @get: NotBlank
        @get: Size(min = 2, max = 10)
        @Column(nullable = false, length = 10)
        var locale: String,

        @get: NotBlank
        @get: Size(min = 2, max = 255)
        @Column(nullable = false, length = 255)
        var name: String,

        var default: Boolean = false

) : ApplicationEntityBase()

/**
 * Repository
 */
@RepositoryRestResource(exported = false)
interface LanguageRepository : ApplicationBaseRepository<Language> {
        fun findOneByDefault(value: Boolean) : Optional<Language>
}
