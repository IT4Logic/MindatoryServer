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

package com.it4logic.mindatory.services.common

import com.it4logic.mindatory.exceptions.ApplicationErrorCodes
import com.it4logic.mindatory.exceptions.ApplicationObjectNotFoundException
import com.it4logic.mindatory.exceptions.ApplicationValidationException
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntity
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntityRepository
import com.it4logic.mindatory.mlc.MultipleLanguageContentService
import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.model.common.ApplicationMLCEntityBase
import com.it4logic.mindatory.query.QueryService
import com.it4logic.mindatory.services.security.SecurityAclService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import javax.persistence.EntityManager
import javax.transaction.Transactional
import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException
import javax.validation.Validator
import kotlin.reflect.KClass

/**
 * Base class for application services
 */
@Service
@Transactional
abstract class ApplicationBaseService<T : ApplicationMLCEntityBase> {
	enum class PersistantFunctionType {
		CREATE,
		UPDATE,
		DELETE
	}

	@Autowired
	protected lateinit var validator: Validator

	@Autowired
	protected lateinit var entityManager: EntityManager

	@Autowired
	protected lateinit var multipleLanguageContentService: MultipleLanguageContentService

	protected abstract fun repository(): ApplicationBaseRepository<T>

	protected abstract fun type(): Class<T>

	protected fun useAcl(): Boolean = false

	protected fun securityAclService(): SecurityAclService? = null

	protected fun multipleLanguageContentRepository(): MultipleLanguageContentBaseEntityRepository<*>? = null

	protected fun multipleLanguageContentType(): KClass<*>? = null

	@Suppress("UNCHECKED_CAST")
	protected fun mlcService(): MultipleLanguageContentService {
//    if(multipleLanguageContentRepository() == null || multipleLanguageContentType() == null)
//      return null
		multipleLanguageContentService.repository =
			multipleLanguageContentRepository() as MultipleLanguageContentBaseEntityRepository<MultipleLanguageContentBaseEntity>?
		multipleLanguageContentService.type = multipleLanguageContentType()
		return multipleLanguageContentService
	}

	fun validate(target: T) {
		val result = validator.validate(target)
		if (result.isNotEmpty()) {
			val violations: java.util.HashSet<ConstraintViolation<T>> = java.util.HashSet()
			result.forEach { violations.add(it) }
			throw ConstraintViolationException(violations)
		}
	}

	/**
	 * Searches and loads all objects
	 *
	 * @param pageable Paging information
	 * @param sort Sort information
	 * @param filter Search criteria
	 * @return Objects list
	 */
	fun findAll(pageable: Pageable?, sort: Sort?, filter: String?): Any {
		val specs = QueryService.parseFilter<T>(filter, null)
		val result = if (specs != null && pageable != null)
			repository().findAll(specs, pageable)
		else if (specs != null && sort != null)
			repository().findAll(specs, sort)
		else if (specs != null)
			repository().findAll(specs)
		else if (pageable != null)
			repository().findAll(pageable)
		else if (sort != null)
			repository().findAll(sort)
		else
			repository().findAll()

//    val filtered = result.distinctBy { it.id }
//    if( (pageable?.sort != null && pageable.sort.isSorted) || (sort != null && sort.isSorted) ) {
//      filtered.sortedBy { "${it.id}" }
//    }

		result.forEach {
			loadMLC(it)
		}

		return result
	}

	/**
	 * Searches and loads object for the input Id
	 *
	 * @param id Objects Id
	 * @return Objectobject, or ApplicationObjectNotFoundException in case if the object Id doesn't exist
	 */
	fun findById(id: Long, loadMLC: Boolean = true): T {
		val target = repository().findById(id)
			.orElseThrow { ApplicationObjectNotFoundException(id, type().simpleName.toLowerCase()) }
		if (loadMLC)
			loadMLC(target)
		return target
	}

	fun refresh(target: T) {
		entityManager.refresh(target)
	}

	/**
	 * Creates Objects object
	 *
	 * @param target Objects user
	 * @return Created Objects object
	 */
	fun create(target: T): T {
		val result = repository().findById(target.id)
		if (result.isPresent)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotCreateObjectWithExistingId)

		validate(target)
		beforeCreate(target)
		val obj = repository().save(target)
		beforeFlush(obj, target, PersistantFunctionType.CREATE)
		repository().flush()
		afterFlush(obj, PersistantFunctionType.CREATE)
		saveMLC(obj, target)
		refresh(obj)
		loadMLC(obj)
		if (useAcl() && SecurityContextHolder.getContext().authentication != null) {
			securityAclService()?.createAcl(obj, SecurityContextHolder.getContext().authentication)
		}
		afterCreate(obj)
		return obj
	}

	/**
	 * Updates Objects object
	 *
	 * @param target Objects object
	 * @return Updated Objects object
	 */
	fun update(target: T): T {
		validate(target)
		// to make sure that the object exists
		var obj = findById(target.id, false)
		target.copyMLCs(obj)
		beforeUpdate(target)
		obj = repository().save(target)
		beforeFlush(obj, target, PersistantFunctionType.UPDATE)
		repository().flush()
		afterFlush(obj, PersistantFunctionType.UPDATE)
		saveMLC(obj, target)
		refresh(obj)
		loadMLC(obj)
		afterUpdate(obj)
		return obj
	}

	/**
	 * Deletes object for the input Id
	 *
	 * @param id Object Id
	 */
	fun deleteById(id: Long) {
		val target = findById(id)
		delete(target)
	}

	/**
	 * Deletes object for the input Id
	 *
	 * @param target Object instance
	 */
	fun delete(target: T) {
		if (useAcl() && SecurityContextHolder.getContext().authentication != null) {
			securityAclService()?.deleteAcl(target)
		}
		val obj = findById(target.id, false)
		beforeDelete(obj)
		beforeFlush(obj, target, PersistantFunctionType.DELETE)
		repository().flush()
		afterFlush(obj, PersistantFunctionType.DELETE)
		deleteMLC(obj)
		repository().flush()
		repository().delete(obj)
		afterDelete(obj)
	}

	fun loadMLC(target: ApplicationMLCEntityBase) {
		mlcService().load(target)
	}

	fun saveMLC(savedObj: ApplicationMLCEntityBase, target: ApplicationMLCEntityBase) {
		mlcService().save(savedObj, target)
	}

	fun deleteMLC(target: ApplicationMLCEntityBase) {
		mlcService().delete(target)
	}

	fun beforeCreate(target: T) {}
	fun afterCreate(target: T) {}

	fun beforeUpdate(target: T) {}
	fun afterUpdate(target: T) {}

	fun beforeDelete(target: T) {}
	fun afterDelete(target: T) {}

	fun beforeFlush(savedTarget: T, refTarget: T, persistantFunctionType: PersistantFunctionType) {}
	fun afterFlush(target: T, persistantFunctionType: PersistantFunctionType) {}
}
