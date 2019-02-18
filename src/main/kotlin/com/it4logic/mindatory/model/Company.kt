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

package com.it4logic.mindatory.model

import com.it4logic.mindatory.mlc.MultipleLanguageContent
import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.model.common.ApplicationConstraintCodes
import com.it4logic.mindatory.model.common.ApplicationEntityBase
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntity
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntityRepository
import javax.validation.constraints.Size
import javax.validation.constraints.NotBlank
import org.hibernate.envers.Audited
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import javax.persistence.*

@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "t_companies", uniqueConstraints = [])
data class Company (
        @get: NotBlank
        @get: Size(min = 2, max = 255)
        @get: MultipleLanguageContent
        @Transient
        var name: String,

        @get: Size(max = 255)
        @get: MultipleLanguageContent
        @Transient
        var street: String = "",

        @get: Size(max = 100)
        @get: MultipleLanguageContent
        @Transient
        var city: String = "",

        @get: Size(max = 100)
        @get: MultipleLanguageContent
        @Transient
        var state: String = "",

        @get: Size(max = 20)
        @Column(length = 20)
        var zipCode: String = "",

        @get: Size(max = 100)
        @get: MultipleLanguageContent
        @Transient
        var country: String = "",

        @get: Size(max = 20)
        @Column(length = 20)
        var mobile: String = "",

        @get: Size(max = 20)
        @Column(length = 20)
        var phone: String = "",

        @get: Size(max = 20)
        @Column(length = 20)
        var fax: String = "",

        override var id: Long = -1

) : ApplicationEntityBase()

/**
 * Repository
 */
@RepositoryRestResource(exported = false)
interface CompanyRepository : ApplicationBaseRepository<Company>


/**
 * Multiple Language Content support entity
 */

@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "t_company_mlcs", uniqueConstraints = [
        (UniqueConstraint(name = ApplicationConstraintCodes.CompanyMCLUniqueIndex, columnNames = ["parentId", "languageId", "fieldName"]))
])
class CompanyMultipleLanguageContent : MultipleLanguageContentBaseEntity()

/**
 * Multiple Language Content support Repository
 */
@RepositoryRestResource(exported = false)
interface CompanyMLCRepository : MultipleLanguageContentBaseEntityRepository<CompanyMultipleLanguageContent>