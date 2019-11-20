package com.it4logic.mindatory.graphql

import com.it4logic.mindatory.mlc.LanguageManager
import com.it4logic.mindatory.model.common.ApplicationMLCEntityBase
import com.it4logic.mindatory.services.LanguageService
import com.it4logic.mindatory.services.common.ApplicationBaseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
abstract class GQLBaseService<T : ApplicationMLCEntityBase> {
	@Autowired
	lateinit var languageService: LanguageService

	@Autowired
	lateinit var languageManager: LanguageManager

	abstract fun service(): ApplicationBaseService<T>

	protected fun propagateLanguage(locale: String?) {
		val language = languageService.findLanguageByLocaleOrDefault(locale)
		languageManager.currentLanguage = language
	}

	@Suppress("UNCHECKED_CAST")
	fun findAll(
		locale: String?,
		page: Int,
		size: Int,
		sort: String?,
		filter: String?
	): Page<T> {
		propagateLanguage(locale)

		var sortRequest: Sort = Sort.unsorted()

		if (sort != null && sort.isNotBlank()) {
			val property = sort.split(",")[0]
			val dir = sort.split(",")[1]
			sortRequest = Sort.by(Sort.Direction.fromString(dir), property)
		}

		val pageRequest = PageRequest.of(page, size, sortRequest)

		return service().findAll(pageRequest, sortRequest, filter) as Page<T>

	}

	@Suppress("UNCHECKED_CAST")
	fun findAll(
		locale: String?,
		sort: String?,
		filter: String?
	): List<T> {
		propagateLanguage(locale)

		var sortRequest: Sort = Sort.unsorted()

		if (sort != null && sort.isNotBlank()) {
			val dir = sort.split(",")[0]
			val property = sort.split(",")[1]
			sortRequest = Sort.by(Sort.Direction.fromString(dir), property)
		}

		return service().findAll(null, sortRequest, filter) as List<T>

	}

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

	fun create(
		locale: String?,
		target: T
	): T {
		propagateLanguage(locale)

		return service().create(target)
	}

	fun update(
		locale: String?,
		target: T
	): T {
		propagateLanguage(locale)

		return service().update(target)
	}

	fun delete(
		locale: String?,
		id: Long
	): Boolean {
		propagateLanguage(locale)
		service().deleteById(id)
		return true
	}

	fun delete(
		locale: String?,
		target: T
	): Boolean {
		propagateLanguage(locale)

		service().delete(target)

		return true
	}
}