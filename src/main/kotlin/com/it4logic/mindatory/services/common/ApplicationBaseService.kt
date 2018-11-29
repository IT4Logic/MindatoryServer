/*
    Copyright (c) 2017, IT4Logic.

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

package com.it4logic.mindatory.services.common

import com.it4logic.mindatory.exceptions.ApplicationObjectNotFoundException
import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.model.common.ApplicationEntityBase
import com.it4logic.mindatory.query.QueryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.util.HashSet
import javax.persistence.EntityManager
import javax.transaction.Transactional
import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException
import javax.validation.Validator

/**
 * Base class for application services
 */
@Service
@Transactional
abstract class ApplicationBaseService<T : ApplicationEntityBase> {
  @Autowired
  protected lateinit var validator: Validator

  @Autowired
  protected lateinit var entityManager: EntityManager

  protected abstract fun repository() : ApplicationBaseRepository<T>

  protected abstract fun type() : Class<T>

  fun validate(target: T) {
    val result = validator.validate(target)
    if(!result.isEmpty()) {
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
    val specs = QueryService.parse<T>(filter)
    return if (specs != null && pageable != null)
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
  }

  /**
   * Searches and loads object for the input Id
   *
   * @param id Objects Id
   * @return Objectobject, or ApplicationObjectNotFoundException in case if the object Id doesn't exist
   */
  fun findById(id: Long) : T {
    return repository().findById(id).orElseThrow { ApplicationObjectNotFoundException(id, type().simpleName.toLowerCase()) }
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
    validate(target)
    beforeCreate(target)
    val obj = repository().save(target)
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
    findById(target.id)
    beforeUpdate(target)
    val obj = repository().save(target)
    refresh(obj)
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
    beforeDelete(target)
    repository().delete(target)
    afterDelete(target)
  }

  /**
   * Deletes object for the input Id
   *
   * @param target Object instance
   */
  fun delete(target: T) {
    repository().delete(target)
  }

  fun beforeCreate(target: T) {}
  fun afterCreate(target: T) {}

  fun beforeUpdate(target: T) {}
  fun afterUpdate(target: T) {}

  fun beforeDelete(target: T) {}
  fun afterDelete(target: T) {}
}
