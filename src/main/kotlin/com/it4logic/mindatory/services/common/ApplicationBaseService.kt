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
 * Base class for entity data services
 */
@Service
@Transactional
abstract class ApplicationBaseService<T : ApplicationMLCEntityBase> {

	/**
	 * Enumeration for Persistent Function Type
	 */
	enum class PersistentFunctionType {
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

	/**
	 * Retrieves Entity repository. This method has to be implemented by the derived class
	 * @return Entity JPA Repository
	 */
	protected abstract fun repository(): ApplicationBaseRepository<T>

	/**
	 * Retrieves Entity class instance. This method has to be implemented by the derived class
	 * @return Entity Class instance
	 */
	protected abstract fun type(): Class<T>

	/**
	 * Retrieves if the service will use ACL Security or not. ACL Security is disabled by default
	 * @return True, if ACL security will be used, False otherwise
	 */
	protected fun useAcl(): Boolean = false

	/**
	 * Retrieves ACL Service instance in case of using ACL Security
	 * @return ACL Service instance
	 */
	protected fun securityAclService(): SecurityAclService? = null

	/**
	 * Retrieves MLC JPA Repository instance in case of using MCL functionality
	 * @return MLC JPA Repository instance
	 */
	protected fun multipleLanguageContentRepository(): MultipleLanguageContentBaseEntityRepository<*>? = null

	/**
	 * Retrieves Entity MLC class instance in case of using MCL functionality
	 * @return Entity MLC Class instance
	 */
	protected fun multipleLanguageContentType(): KClass<*>? = null

	/**
	 * Retrieves MLC Service instance in case of using MCL functionality
	 * @return MLC Service Instance
	 */
	@Suppress("UNCHECKED_CAST")
	protected fun mlcService(): MultipleLanguageContentService {
		multipleLanguageContentService.repository =
			multipleLanguageContentRepository() as MultipleLanguageContentBaseEntityRepository<MultipleLanguageContentBaseEntity>?
		multipleLanguageContentService.type = multipleLanguageContentType()
		return multipleLanguageContentService
	}

	/**
	 * Validate entity instance properties, and raises [ConstraintViolationException] exception
	 * in case of there are validation errors
	 * @param target Input entity instance
	 */
	fun validate(target: T) {
		val result = validator.validate(target)
		if (result.isNotEmpty()) {
			val violations: java.util.HashSet<ConstraintViolation<T>> = java.util.HashSet()
			result.forEach { violations.add(it) }
			throw ConstraintViolationException(violations)
		}
	}

	/**
	 * Searches and loads all objects according to input filter and soring
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

		result.forEach {
			loadMLC(it)
		}

		return result
	}

	/**
	 * Searches and loads object for the input Id
	 *
	 * @param id Objects Id
	 * @param loadMLC Whether to load MLC content related to object or not
	 * @return Object instance, or ApplicationObjectNotFoundException in case if the object Id doesn't exist
	 */
	fun findById(id: Long, loadMLC: Boolean = true): T {
		val target = repository().findById(id)
			.orElseThrow { ApplicationObjectNotFoundException(id, type().simpleName.toLowerCase()) }
		if (loadMLC)
			loadMLC(target)
		return target
	}

	/**
	 * Reloads the objects from the database and updates the cache
	 * @param target Input object
	 */
	fun refresh(target: T) {
		entityManager.refresh(target)
	}

	/**
	 * Creates a new object
	 * @param target Input object to be created
	 * @return Created object
	 */
	fun create(target: T): T {
		val result = repository().findById(target.id)
		if (result.isPresent)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationCannotCreateObjectWithExistingId)

		validate(target)
		beforeCreate(target)
		val obj = repository().save(target)
		beforeFlush(obj, target, PersistentFunctionType.CREATE)
		repository().flush()
		afterFlush(obj, PersistentFunctionType.CREATE)
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
	 * Updates an existing object
	 *
	 * @param target Input object to be updated
	 * @return Updated  object
	 */
	fun update(target: T): T {
		validate(target)
		// to make sure that the object exists
		var obj = findById(target.id, false)
		target.copyMLCs(obj)
		beforeUpdate(target)
		obj = repository().save(target)
		beforeFlush(obj, target, PersistentFunctionType.UPDATE)
		repository().flush()
		afterFlush(obj, PersistentFunctionType.UPDATE)
		saveMLC(obj, target)
		refresh(obj)
		loadMLC(obj)
		afterUpdate(obj)
		return obj
	}

	/**
	 * Deletes object by its Id
	 * @param id Object Id
	 */
	fun deleteById(id: Long) {
		val target = findById(id)
		delete(target)
	}

	/**
	 * Deletes object by its instnace
	 * @param target Object instance
	 */
	fun delete(target: T) {
		if (useAcl() && SecurityContextHolder.getContext().authentication != null) {
			securityAclService()?.deleteAcl(target)
		}
		val obj = findById(target.id, false)
		beforeDelete(obj)
		beforeFlush(obj, target, PersistentFunctionType.DELETE)
		repository().flush()
		afterFlush(obj, PersistentFunctionType.DELETE)
		deleteMLC(obj)
		repository().flush()
		repository().delete(obj)
		afterDelete(obj)
	}

	/**
	 * Loads MLC content for the input object
	 * @param target Input object
	 */
	fun loadMLC(target: ApplicationMLCEntityBase) {
		mlcService().load(target)
	}

	/**
	 * LoadSaves MLC content for the input object
	 * This method uses two isntance for the same object Id, because after saving the object all MLC enabled
	 * properties will be cleared, hence, the updated content as well, this why we need the object instance
	 * before saving to have reference to properties content to be saved or updated.
	 * @param savedObj Saved object instance
	 * @param target Reference object
	 */
	fun saveMLC(savedObj: ApplicationMLCEntityBase, target: ApplicationMLCEntityBase) {
		mlcService().save(savedObj, target)
	}

	/**
	 * Deleted MLC content for the input object
	 * @param target Input object
	 */
	fun deleteMLC(target: ApplicationMLCEntityBase) {
		mlcService().delete(target)
	}

	/**
	 * Method to be called before saving to database
	 * @param target Input object
	 */
	fun beforeCreate(target: T) {}
	/**
	 * Method to be called after saving to database
	 * @param target Input object
	 */
	fun afterCreate(target: T) {}

	/**
	 * Method to be called before updating to database
	 * @param target Input object
	 */
	fun beforeUpdate(target: T) {}
	/**
	 * Method to be called after updating to database
	 * @param target Input object
	 */
	fun afterUpdate(target: T) {}

	/**
	 * Method to be called before deleting from database
	 * @param target Input object
	 */
	fun beforeDelete(target: T) {}
	/**
	 * Method to be called after deleting from database
	 * @param target Input object
	 */
	fun afterDelete(target: T) {}

	/**
	 * Method to be called before flushing the changes to database
	 * @param target Input object
	 */
	fun beforeFlush(savedTarget: T, refTarget: T, persistentFunctionType: PersistentFunctionType) {}
	/**
	 * Method to be called after flushing the changes to database
	 * @param target Input object
	 */
	fun afterFlush(target: T, persistentFunctionType: PersistentFunctionType) {}
}
