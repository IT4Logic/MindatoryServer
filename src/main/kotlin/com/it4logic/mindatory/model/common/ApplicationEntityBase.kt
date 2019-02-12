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

package com.it4logic.mindatory.model.common

import com.fasterxml.jackson.annotation.JsonFormat
import com.it4logic.mindatory.model.ApplicationRepository
import com.it4logic.mindatory.model.mlc.Language
import com.it4logic.mindatory.model.Solution
import org.hibernate.annotations.DynamicUpdate
import org.hibernate.annotations.GenericGenerator
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull


/**
 * Base entity class for any common entity functionaries
 */
@MappedSuperclass
@DynamicUpdate
open class ApplicationEntityBase {

    @CreatedBy
    @Column(length = 100, updatable = false, nullable = false)
    open var createdBy: String? = null

    @CreatedDate
    @get: JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    @Column(updatable = false, nullable = false)
    open var createdAt: Date? = null

    @LastModifiedBy
    @Column(length = 100)
    open var updatedBy: String? = null

    @LastModifiedDate
    @get: JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    open var updatedAt: Date? = null

    @Version
    open var version: Long = 1

    @Id
    @GenericGenerator(name = "UseExistingOrGenerateIdGenerator", strategy = "com.it4logic.mindatory.helpers.UseExistingOrGenerateIdGenerator")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "UseExistingOrGenerateIdGenerator")
    open var id: Long = -1
}

/**
 * Base entity class for company common entity functionaries
 */
@MappedSuperclass
@DynamicUpdate
open class ApplicationCompanyEntityBase (

        @Column(name = "company_id")
        open var companyId: Long = 1

) : ApplicationEntityBase()

/**
 * Base entity class for solution common entity functionaries
 */
@MappedSuperclass
@DynamicUpdate
open class ApplicationSolutionEntityBase (

    @get: NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "solution_id", nullable = false)
    open var solution: Solution? = null

) : ApplicationCompanyEntityBase()


/**
 * Base entity class for solution common entity functionaries
 */
@MappedSuperclass
@DynamicUpdate
open class ApplicationRepositoryEntityBase (
    @get: NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "repository_id", nullable = false)
    open var repository: ApplicationRepository? = null

) : ApplicationSolutionEntityBase()

/**
 * Base entity class for solution common entity functionaries
 */
@MappedSuperclass
@DynamicUpdate
open class LanguageContentEntityBase (
    @get: NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "language_id", nullable = false)
    open var language: Language? = null,

    @get: NotBlank
    @Column(nullable = false, length = 255)
    open var fieldName: String = "",

    @get: NotNull
    @Lob
    open var contents: String = ""

) : ApplicationEntityBase()


enum class DesignStatus(private val status: Int) {
    InDesign(1),
    Released(2)
}

enum class StoreObjectStatus(private val status: Int) {
    Active(1),
    Migrated(2)
}