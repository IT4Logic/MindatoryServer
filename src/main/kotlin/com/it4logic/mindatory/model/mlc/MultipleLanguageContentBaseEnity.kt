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

package com.it4logic.mindatory.model.mlc

import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.model.common.ApplicationEntityBase
import org.hibernate.annotations.DynamicUpdate
import org.springframework.data.repository.NoRepositoryBean
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

/**
 * Multiple language content root entity
 */
@MappedSuperclass
@DynamicUpdate
open class MultipleLanguageContentBaseEntity (
    @get: NotNull
    @Column(nullable = false)
    open var languageId: Long? = 0,

    @get: NotBlank
    @Column(nullable = false, length = 255)
    open var fieldName: String = "",

    @get: NotNull
    @Lob
    open var contents: String = "",

    @get: NotNull
    @Column(nullable = false)
    open var parentId: Long = 0

) : ApplicationEntityBase()

/**
 * Repository
 */
@NoRepositoryBean
interface MultipleLanguageContentBaseEntityRepository<T : MultipleLanguageContentBaseEntity> : ApplicationBaseRepository<T> {
    fun findOneByLanguageIdAndFieldNameAndParentId(langId: Long, fieldName: String, parentId: Long): Optional<T>
    fun findAllByLanguageIdAndFieldNameAndContents(langId: Long, fieldName: String, contents: String): List<T>
    fun findAllByLanguageIdAndFieldNameAndContentsAndParentIdNot(langId: Long, fieldName: String, contents: String, parentId: Long): List<T>
    fun findAllByParentId(parentId: Long): List<T>
    fun countByLanguageId(langId: Long): Long
    fun deleteAllByLanguageId(langId: Long)
}


