/*
    Copyright (c) 2019, IT4Logic.

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
package com.it4logic.mindatory.graphql

import com.it4logic.mindatory.mlc.LanguageManager
import com.it4logic.mindatory.model.common.ApplicationEntityBase
import com.it4logic.mindatory.services.LanguageService
import com.it4logic.mindatory.services.common.ApplicationBaseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

/**
 * Base GraphQL service that implements the common RAID functionality
 * The derived classes should provide the security permissions and GraphQL method names.
 */
@Service
abstract class GQLBaseService<T : ApplicationEntityBase> {
	@Autowired
	lateinit var languageService: LanguageService

	@Autowired
	lateinit var languageManager: LanguageManager

	/**
	 * Abstract function that has to be implemented by the derived classes
	 * to retrieve the instance of the implemented service
	 * @return Instance of the implemented service
	 */
	abstract fun service(): ApplicationBaseService<T>

	/**
	 * Propagates the input locale across the system
	 * @param locale Input locale
	 */
	protected fun propagateLanguage(locale: String?) {
		val language = languageService.findLanguageByLocaleOrDefault(locale)
		languageManager.currentLanguage = language
	}

	fun prepareForFind(locale: String?,sort: String?): Sort {
		propagateLanguage(locale)

		var sortRequest: Sort = Sort.unsorted()

		if (sort != null && sort.isNotBlank()) {
			val property = sort.split(",")[0]
			val dir = sort.split(",")[1]
			sortRequest = Sort.by(Sort.Direction.fromString(dir), property)
		}

		return sortRequest
	}
	/**
	 * Finds and loads a pageable list of objects
	 * @param locale Input locale
	 * @param page Page index. 0 is the first page index
	 * @param size Page size. How many objects in one page
	 * @param sort Attributes to be used in sort (should be in "attributeA,ASC or DESC" format)
	 * @param filter Input filter in RSQL format
	 * @return Pageable list of objects
	 */
	@Suppress("UNCHECKED_CAST")
	fun findAll(
		locale: String?,
		page: Int,
		size: Int,
		sort: String?,
		filter: String?
	): Page<T> {
		val sortRequest = prepareForFind(locale, sort)
		val pageRequest = PageRequest.of(page, size, sortRequest)

		return service().findAll(pageRequest, sortRequest, filter) as Page<T>

	}

	/**
	 * Finds and loads a list of objects
	 * @param locale Input locale
	 * @param sort Attributes to be used in sort (should be in "attributeA,ASC or DESC" format)
	 * @param filter Input filter in RSQL format
	 * @return List of objects
	 */
	@Suppress("UNCHECKED_CAST")
	fun findAll(
		locale: String?,
		sort: String?,
		filter: String?
	): List<T> {
		val sortRequest = prepareForFind(locale, sort)
		return service().findAll(null, sortRequest, filter) as List<T>
	}

	/**
	 * Finds and loads an object
	 * @param locale Input locale
	 * @param id Input Object ID
	 * @param filter Input filter in RSQL format
	 * @return Loaded object or null
	 */
	fun find(
		locale: String?,
		id: Long?,
		filter: String?
	): T? {

		propagateLanguage(locale)

		if (id != null)
			return service().findById(id)

		if (filter != null && filter.isNotBlank()) {
			val result = findAll(locale, null, filter)
			if (result.isNotEmpty())
				return result[0]
		}

		return null
	}

	/**
	 * Creates a new object
	 * @param locale Input locale
	 * @param target Input object
	 * @return Created object
	 */
	fun create(
		locale: String?,
		target: T
	): T {
		propagateLanguage(locale)
		return service().create(target)
	}

	/**
	 * Updates an existing object
	 * @param locale Input locale
	 * @param target Input object
	 * @return Updated object
	 */
	fun update(
		locale: String?,
		target: T
	): T {
		propagateLanguage(locale)
		return service().update(target)
	}

	/**
	 * Deletes an existing object
	 * @param locale Input locale
	 * @param id Input Object ID
	 * @return True if object has been deleted successfully or Exception will be raised
	 */
	fun delete(
		locale: String?,
		id: Long
	): Boolean {
		propagateLanguage(locale)
		service().deleteById(id)
		return true
	}

	/**
	 * Deletes an existing object
	 * @param locale Input locale
	 * @param target Input object instance
	 * @return True if object has been deleted successfully or Exception will be raised
	 */
	fun delete(
		locale: String?,
		target: T
	): Boolean {
		propagateLanguage(locale)
		service().delete(target)
		return true
	}
}