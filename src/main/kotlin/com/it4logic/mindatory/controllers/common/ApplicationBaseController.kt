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

package com.it4logic.mindatory.controllers.common

import com.it4logic.mindatory.model.common.ApplicationEntityBase
import com.it4logic.mindatory.services.common.ApplicationBaseService
import com.sun.org.apache.xpath.internal.operations.Bool
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.rest.core.RepositoryConstraintViolationException
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.validation.Errors
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.transaction.Transactional
import javax.validation.Valid

/**
 * Base class for application controllers contains all the common functionalities
 */
@CrossOrigin
@RestController
abstract class ApplicationBaseController<T : ApplicationEntityBase> {
    protected val logger: Log = LogFactory.getLog(javaClass)

    protected abstract fun service() : ApplicationBaseService<T>

    protected fun doGetInternal(@RequestParam(required = false) filter: String?, pageable: Pageable, request: HttpServletRequest): Any {
        return when {
            request.parameterMap.isEmpty() -> service().findAll(null, null, null)
            request.parameterMap.containsKey("page") -> service().findAll(pageable, null, filter)
            request.parameterMap.containsKey("sort") -> service().findAll(null, pageable.sort, filter)
            else -> service().findAll(null, null, filter)
        }
    }

    protected fun doGetInternal(@PathVariable id: Long) : ResponseEntity<T> {
        val result = service().findById(id)
        service().refresh(result)
        return ResponseEntity.ok(result)
    }

    protected fun doCreateInternal(@Valid @RequestBody target: T, errors: Errors, response: HttpServletResponse) : ResponseEntity<T> {
        if(errors.hasErrors())
            throw RepositoryConstraintViolationException(errors)

        val result = service().create(target)
        val location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{Id}").buildAndExpand(result.id).toUri()
        return ResponseEntity.created(location).body(result)
    }

    protected fun doUpdateInternal(@Valid @RequestBody target: T, errors: Errors, request: HttpServletRequest): ResponseEntity<T> {
        if (errors.hasErrors())
            throw RepositoryConstraintViolationException(errors)

        val result = service().update(target)
        service().refresh(result)

        return ResponseEntity.ok().body(result)
    }

    protected fun doDeleteInternal(@PathVariable id: Long) : ResponseEntity<Any> {
        val target = service().findById(id)
        service().delete(target)
        return ResponseEntity.ok("")
    }

    @GetMapping
    @ResponseBody
    abstract fun doGet(@RequestParam(required = false) filter: String?, pageable: Pageable, request: HttpServletRequest): Any

    @GetMapping("{id}")
    abstract fun doGet(@PathVariable id: Long) : ResponseEntity<T>

    @PostMapping
    abstract fun doCreate(@Valid @RequestBody target: T, errors: Errors, response: HttpServletResponse) : ResponseEntity<T>

    @PutMapping
    abstract fun doUpdate(@Valid @RequestBody target: T, errors: Errors, request: HttpServletRequest): ResponseEntity<T>

    @DeleteMapping("{id}")
    abstract fun doDelete(@PathVariable id: Long) : ResponseEntity<Any>
}
