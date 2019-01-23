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

import com.it4logic.mindatory.exceptions.ApplicationAuthorizationException
import com.it4logic.mindatory.exceptions.ApplicationErrorCodes
import com.it4logic.mindatory.model.ApplicationRepository
import com.it4logic.mindatory.model.common.ApplicationEntityBase
import com.it4logic.mindatory.security.*
import com.it4logic.mindatory.services.common.ApplicationBaseService
import com.it4logic.mindatory.services.security.SecurityAclService
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.rest.core.RepositoryConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.acls.domain.BasePermission
import org.springframework.security.acls.model.Acl
import org.springframework.validation.Errors
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.lang.Exception
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.validation.Valid

/**
 * Base class for application controllers contains all the common functionalities
 */
@CrossOrigin
@RestController
abstract class ApplicationBaseController<T : ApplicationEntityBase> {
    protected val logger: Log = LogFactory.getLog(javaClass)

    @Autowired
    lateinit var securityAclService: SecurityAclService

    protected abstract fun service() : ApplicationBaseService<T>

    protected abstract fun type() : Class<T>

    protected fun typeName() : String = type().typeName

    // ====================================================== Basic Operations ======================================================

    protected fun doGetInternal(filter: String?, pageable: Pageable, request: HttpServletRequest, response: HttpServletResponse): Any {
        val result = when {
            request.parameterMap.isEmpty() -> service().findAll(null, null, null)
            request.parameterMap.containsKey("page") -> service().findAll(pageable, null, filter)
            request.parameterMap.containsKey("sort") -> service().findAll(null, pageable.sort, filter)
            else -> service().findAll(null, null, filter)
        }
        response.status = HttpStatus.OK.value()
        return result
    }

    protected fun doGetInternal(id: Long, request: HttpServletRequest, response: HttpServletResponse) : T {
        val result = service().findById(id)
        service().refresh(result)
        response.status = HttpStatus.OK.value()
        return result
    }

    protected fun doCreateInternal(target: T, errors: Errors, request: HttpServletRequest, response: HttpServletResponse) : T {
        if(errors.hasErrors())
            throw RepositoryConstraintViolationException(errors)

        val result = service().create(target)
        val location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{Id}").buildAndExpand(result.id).toUri()
        response.status = HttpStatus.CREATED.value()
        response.addHeader("Location", location.path)
        return result
    }

    protected fun doUpdateInternal(target: T, errors: Errors, request: HttpServletRequest, response: HttpServletResponse): T {
        if (errors.hasErrors())
            throw RepositoryConstraintViolationException(errors)

        val result = service().update(target)
        service().refresh(result)
        response.status = HttpStatus.OK.value()
        return result
    }

    protected fun doDeleteInternal(id: Long, request: HttpServletRequest, response: HttpServletResponse) {
        val target = service().findById(id)
        service().delete(target)
        response.status = HttpStatus.OK.value()
    }

    // ====================================================== ACL Permissions ======================================================

    fun doGetPermissionsInternal(id: Long, request: HttpServletRequest, response: HttpServletResponse): Acl {
        val authentication = SecurityFactory.getCurrentAuthentication()
        val securityExpressionRoot = SecurityFactory.createSecurityExpressionRoot(request, response)
        if( securityExpressionRoot.hasAnyAuthority(
                ApplicationSecurityPermissions.SecurityAclAdminView,
                ApplicationSecurityPermissions.SecurityAclAdminAdd,
                ApplicationSecurityPermissions.SecurityAclAdminRemove,
                ApplicationSecurityPermissions.SecurityAclAdminChangeOwner)
            or securityAclService.hasPermission(authentication, id, typeName(), BasePermission.READ)
            or securityAclService.hasPermission(authentication, id, typeName(), BasePermission.CREATE)
            or securityAclService.hasPermission(authentication, id, typeName(), BasePermission.WRITE)
            or securityAclService.hasPermission(authentication, id, typeName(), BasePermission.DELETE)) {
            return securityAclService.readAcl(type(), id)
        }
        throw ApplicationAuthorizationException(ApplicationErrorCodes.AuthorizationError)
    }

    protected fun doAddPermissionInternal(id: Long, permissionRequests: List<ApplicationAclPermissionRequest>, request: HttpServletRequest, response: HttpServletResponse) {
        val securityExpressionRoot = SecurityFactory.createSecurityExpressionRoot(request, response)
        if( securityExpressionRoot.hasAnyAuthority(
                ApplicationSecurityPermissions.SecurityAclAdminView,
                ApplicationSecurityPermissions.SecurityAclAdminAdd,
                ApplicationSecurityPermissions.SecurityAclAdminRemove,
                ApplicationSecurityPermissions.SecurityAclAdminChangeOwner)
                or securityAclService.hasPermission(SecurityFactory.getCurrentAuthentication(), id, typeName(), BasePermission.WRITE)) {
            for(permissionRequest in permissionRequests)
                securityAclService.addPermission(type(), id, permissionRequest.recipient, permissionRequest.permissions)
        } else
            throw ApplicationAuthorizationException(ApplicationErrorCodes.AuthorizationError)
    }

    fun doRemovePermissionInternal(id: Long, permissionRequests: List<ApplicationAclPermissionRequest>, request: HttpServletRequest, response: HttpServletResponse) {
        val securityExpressionRoot = SecurityFactory.createSecurityExpressionRoot(request, response)
        if( securityExpressionRoot.hasAnyAuthority(
                ApplicationSecurityPermissions.SecurityAclAdminView,
                ApplicationSecurityPermissions.SecurityAclAdminAdd,
                ApplicationSecurityPermissions.SecurityAclAdminRemove,
                ApplicationSecurityPermissions.SecurityAclAdminChangeOwner)
                or securityAclService.hasPermission(SecurityFactory.getCurrentAuthentication(), id, typeName(), BasePermission.WRITE)) {
            for(permissionRequest in permissionRequests)
                securityAclService.deletePermission(type(), id, permissionRequest.recipient, permissionRequest.permissions)
        } else
            throw ApplicationAuthorizationException(ApplicationErrorCodes.AuthorizationError)
    }

    fun doChangeOwnerInternal(id: Long, owner: String, request: HttpServletRequest, response: HttpServletResponse) {
        val securityExpressionRoot = SecurityFactory.createSecurityExpressionRoot(request, response)
        if( securityExpressionRoot.hasAnyAuthority(
                ApplicationSecurityPermissions.SecurityAclAdminView,
                ApplicationSecurityPermissions.SecurityAclAdminAdd,
                ApplicationSecurityPermissions.SecurityAclAdminRemove,
                ApplicationSecurityPermissions.SecurityAclAdminChangeOwner)
                or securityAclService.hasPermission(SecurityFactory.getCurrentAuthentication(), id, typeName(), BasePermission.WRITE)) {
            securityAclService.changeOwner(type(), id, owner)
        } else
            throw ApplicationAuthorizationException(ApplicationErrorCodes.AuthorizationError)
    }

    // ====================================================== ACL Operations exposed points ======================================================

    @PostMapping("{id}/permissions")
    @ResponseBody
    fun doGetPermissions(@PathVariable id: Long, request: HttpServletRequest, response: HttpServletResponse): Acl = doGetPermissionsInternal(id, request, response)

    @PostMapping("{id}/permissions/add")
    fun doAddPermission(@PathVariable id: Long, @Valid @RequestBody permissionRequests: List<ApplicationAclPermissionRequest>, request: HttpServletRequest, response: HttpServletResponse)
            = doAddPermissionInternal(id, permissionRequests, request, response)

    @PostMapping("{id}/permissions/remove")
    fun doRemovePermission(@PathVariable id: Long, @Valid @RequestBody permissionRequests: List<ApplicationAclPermissionRequest>, request: HttpServletRequest, response: HttpServletResponse)
            = doRemovePermissionInternal(id, permissionRequests, request, response)

    @PostMapping("{id}/permissions/change-owner/{owner}")
    fun doChangeOwner(@PathVariable id: Long, @PathVariable owner: String, request: HttpServletRequest, response: HttpServletResponse)
            = doChangeOwnerInternal(id, owner, request, response)

    // ====================================================== Basic Operations abstract methods ======================================================

    @GetMapping
    @ResponseBody
    abstract fun doGet(@RequestParam(required = false) filter: String?, pageable: Pageable, request: HttpServletRequest, response: HttpServletResponse): Any

    @GetMapping("{id}")
    abstract fun doGet(@PathVariable id: Long, request: HttpServletRequest, response: HttpServletResponse) : T

    @PostMapping
    abstract fun doCreate(@Valid @RequestBody target: T, errors: Errors, request: HttpServletRequest, response: HttpServletResponse) : T

    @PutMapping
    abstract fun doUpdate(@Valid @RequestBody target: T, errors: Errors, request: HttpServletRequest, response: HttpServletResponse) : T

    @DeleteMapping("{id}")
    abstract fun doDelete(@PathVariable id: Long, request: HttpServletRequest, response: HttpServletResponse)


}
