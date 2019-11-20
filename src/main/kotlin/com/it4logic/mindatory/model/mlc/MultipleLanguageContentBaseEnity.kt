/*
    Copyright (c) 2018, IT4Logic. All rights reserved.

    This file is part of Mindatory project by IT4Logic.

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
 * Multiple Language Content entity
 */
@MappedSuperclass
@DynamicUpdate
open class MultipleLanguageContentBaseEntity(
	@get: NotNull
	@Column(name = "f_language_id", nullable = false)
	open var languageId: Long? = 0,

	@get: NotBlank
	@Column(name = "f_field_name", nullable = false, length = 100)
	open var fieldName: String = "",

	@get: NotNull
	@Lob
	@Column(name = "f_contents")
	open var contents: String = "",

	@get: NotNull
	@Column(name = "f_parent", nullable = false)
	open var parent: Long = 0

) : ApplicationEntityBase()

/**
 * JPA Repository
 */
@NoRepositoryBean
interface MultipleLanguageContentBaseEntityRepository<T : MultipleLanguageContentBaseEntity> :
	ApplicationBaseRepository<T> {
	fun findOneByLanguageIdAndFieldNameAndParent(langId: Long, fieldName: String, parentId: Long): Optional<T>

	fun findAllByLanguageIdAndFieldName(langId: Long, fieldName: String): List<T>
	fun findAllByLanguageIdAndFieldNameAndParentNot(langId: Long, fieldName: String, parentId: Long): List<T>

	fun findAllByParent(parentId: Long): List<T>
	fun countByLanguageId(langId: Long): Long
	fun deleteAllByLanguageId(langId: Long)
}


