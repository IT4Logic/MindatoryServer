/*
    Copyright (c) 2018, IT4Logic.

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

package com.it4logic.mindatory.model.common

import com.fasterxml.jackson.annotation.JsonFormat
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntity
import org.hibernate.annotations.DynamicUpdate
import org.hibernate.annotations.GenericGenerator
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime
import javax.persistence.*


/**
 * Base class for application entities
 */
@MappedSuperclass
@DynamicUpdate
abstract class ApplicationEntityBase {
	@CreatedBy
	@Column(name = "f_created_by", length = 100, updatable = false, nullable = false)
	open var createdBy: String? = null

	@CreatedDate
	@get: JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
	@Column(name = "f_created_at", updatable = false, nullable = false, columnDefinition = "TIMESTAMP")
	open var createdAt: LocalDateTime? = null

	@LastModifiedBy
	@Column(name = "f_updated_by", length = 100)
	open var updatedBy: String? = null

	@LastModifiedDate
	@get: JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
	@Column(name = "f_updated_at", columnDefinition = "TIMESTAMP")
	open var updatedAt: LocalDateTime? = null

	@Version
	@Column(name = "f_version")
	open var version: Long = 1

	@Id
	@GenericGenerator(
		name = "UseExistingOrGenerateIdGenerator",
		strategy = "com.it4logic.mindatory.helpers.UseExistingOrGenerateIdGenerator"
	)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "UseExistingOrGenerateIdGenerator")
	@Column(name = "f_id")
	open var id: Long = -1


	/**
	 * Retrieves MLC objects
	 * @return MLC objects list
	 */
	open fun obtainMLCs(): MutableList<MultipleLanguageContentBaseEntity> {
		throw NotImplementedError()
	}

	/**
	 * Adds MLC object
	 * @param mlc MLC Object
	 */
	open fun addMLC(mlc: MultipleLanguageContentBaseEntity) {
		val result = obtainMLCs().filter { it.languageId == mlc.languageId && it.fieldName == mlc.fieldName }
		if (result.isNotEmpty())
			return
		obtainMLCs().add(mlc)
	}

	/**
	 * Removes MLC object
	 * @param mlc MLC Object
	 */
	open fun removeMLC(mlc: MultipleLanguageContentBaseEntity) {
		val result = obtainMLCs().filter { it.id == mlc.id }
		if (result.isEmpty())
			return
		obtainMLCs().remove(mlc)
	}

	/**
	 * Copy MLC Objects from the input object to current object
	 * @param target Input object
	 */
	open fun copyMLCs(target: ApplicationEntityBase) {
		for (mlc in target.obtainMLCs()) {
			addMLC(mlc)
		}
	}

	fun findAllByLanguageIdAndFieldName(languageId: Long, propertyName: String): List<MultipleLanguageContentBaseEntity> {
		return obtainMLCs().filter {
			it.languageId == languageId && it.fieldName == propertyName
		}
	}
}