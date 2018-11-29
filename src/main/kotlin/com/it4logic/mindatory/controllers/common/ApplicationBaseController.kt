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
//@Transactional
abstract class ApplicationBaseController<T : ApplicationEntityBase>(
    protected val notAllowedMethods: MutableSet<HttpMethod> = mutableSetOf()
) {
    protected val logger: Log = LogFactory.getLog(javaClass)

    protected abstract fun service() : ApplicationBaseService<T>

    protected fun isMethodNotAllowed(method: HttpMethod): Boolean = notAllowedMethods.contains(method)

    //@PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SecurityRoleView}')")
    @GetMapping
    @ResponseBody
    fun doGet(@RequestParam(required = false) filter: String?, pageable: Pageable, request: HttpServletRequest): Any {
        if(isMethodNotAllowed(HttpMethod.GET))
            throw HttpRequestMethodNotSupportedException(HttpMethod.GET.name)

        return when {
            request.parameterMap.isEmpty() -> service().findAll(null, null, null)
            request.parameterMap.containsKey("page") -> service().findAll(pageable, null, filter)
            request.parameterMap.containsKey("sort") -> service().findAll(null, pageable.sort, filter)
            else -> service().findAll(null, null, filter)
        }
    }

    //  @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SecurityRoleView}')")
    @GetMapping("{id}")
    fun doGet(@PathVariable id: Long) : ResponseEntity<T> {
        if(isMethodNotAllowed(HttpMethod.GET))
            throw HttpRequestMethodNotSupportedException(HttpMethod.GET.name)

        val result = service().findById(id)
        service().refresh(result)
        return ResponseEntity.ok(result)
    }

    //  @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SecurityRoleAdd}')")
    @PostMapping
    fun doCreate(@Valid @RequestBody target: T, errors: Errors, response: HttpServletResponse) : ResponseEntity<T> {
        if(isMethodNotAllowed(HttpMethod.POST))
            throw HttpRequestMethodNotSupportedException(HttpMethod.POST.name)

        if(errors.hasErrors())
            throw RepositoryConstraintViolationException(errors)

//        beforeCreate(target)

        val result = service().create(target)

//        afterCreate(result)

        val location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{Id}").buildAndExpand(result.id).toUri()

        return ResponseEntity.created(location).body(result)
    }

    //@PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SecurityRoleModify}')")
    @PutMapping
    fun doUpdate(@Valid @RequestBody target: T, errors: Errors, request: HttpServletRequest): ResponseEntity<T> {
        if(isMethodNotAllowed(HttpMethod.PUT))
            throw HttpRequestMethodNotSupportedException(HttpMethod.PUT.name)

        if (errors.hasErrors())
            throw RepositoryConstraintViolationException(errors)

//        beforeUpdate(target)

        val result = service().update(target)
        service().refresh(result)

//        afterUpdate(result)

        return ResponseEntity.ok().body(result)
    }

    //@PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SecurityRoleDelete}') and hasAuthority('${ApplicationSecurityPermissions.SecurityUserModify}')")
    @DeleteMapping("{id}")
    fun doDelete(@PathVariable id: Long) : ResponseEntity<Any> {
        if(isMethodNotAllowed(HttpMethod.DELETE))
            throw HttpRequestMethodNotSupportedException(HttpMethod.DELETE.name)

        val target = service().findById(id)

//        beforeDelete(target)

        service().delete(target)

//        afterDelete(target)

        return ResponseEntity.ok("")
    }

//    fun beforeCreate(target: T) {}
//    fun afterCreate(target: T) {}
//
//    fun beforeUpdate(target: T) {}
//    fun afterUpdate(target: T) {}
//
//    fun beforeDelete(target: T) {}
//    fun afterDelete(target: T) {}
}
