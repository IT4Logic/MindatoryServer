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

package com.it4logic.mindatory.controllers.security

import com.it4logic.mindatory.controllers.common.ApplicationControllerEntryPoints
import com.it4logic.mindatory.security.*
import com.it4logic.mindatory.services.security.SecurityAclService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.acls.domain.BasePermission
import javax.validation.Valid
import org.springframework.web.bind.annotation.*
import java.lang.Exception
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@CrossOrigin
@RestController
@RequestMapping(ApplicationControllerEntryPoints.SecurityAcl)
class SecurityAclController {

    @Autowired
    lateinit var securityAclService: SecurityAclService

    @PostMapping
    @ResponseBody
    fun doGetPermissions(@Valid @RequestBody aclRequest: ApplicationAclRequest, request: HttpServletRequest, response: HttpServletResponse) {
        val authentication = SecurityFactory.getCurrentAuthentication()
        val securityExpressionRoot = SecurityFactory.createSecurityExpressionRoot(request, response)
        if( securityExpressionRoot.hasAnyAuthority(ApplicationSecurityPermissions.SecurityAclAdminView,
            ApplicationSecurityPermissions.SecurityAclAdminAdd,
            ApplicationSecurityPermissions.SecurityAclAdminRemove,
            ApplicationSecurityPermissions.SecurityAclAdminChangeOwner)
            or securityAclService.hasPermission(authentication, aclRequest.id, aclRequest.domainClass, BasePermission.READ)
            or securityAclService.hasPermission(authentication, aclRequest.id, aclRequest.domainClass, BasePermission.CREATE)
            or securityAclService.hasPermission(authentication, aclRequest.id, aclRequest.domainClass, BasePermission.WRITE)
            or securityAclService.hasPermission(authentication, aclRequest.id, aclRequest.domainClass, BasePermission.DELETE)) {
            securityAclService.readAcl(aclRequest)
        }
        throw Exception() // todo not allowed exception
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SecurityAclAdminAdd}')")
    fun doAddPermission(@Valid @RequestBody permissionRequest: ApplicationAclPermissionRequest, request: HttpServletRequest, response: HttpServletResponse) {
        val securityExpressionRoot = SecurityFactory.createSecurityExpressionRoot(request, response)
        if( securityExpressionRoot.hasAnyAuthority(ApplicationSecurityPermissions.SecurityAclAdminView,
                ApplicationSecurityPermissions.SecurityAclAdminAdd,
                ApplicationSecurityPermissions.SecurityAclAdminRemove,
                ApplicationSecurityPermissions.SecurityAclAdminChangeOwner)
            or securityAclService.hasPermission(SecurityFactory.getCurrentAuthentication(), permissionRequest.id, permissionRequest.domainClass, BasePermission.WRITE)) {
            securityAclService.addPermission(permissionRequest)
        }
        throw Exception()
    }

    @PostMapping("/remove")
    @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SecurityAclAdminRemove}')")
    fun doRemovePermission(@Valid @RequestBody permissionRequest: ApplicationAclPermissionRequest, request: HttpServletRequest, response: HttpServletResponse) {
        val securityExpressionRoot = SecurityFactory.createSecurityExpressionRoot(request, response)
        if( securityExpressionRoot.hasAnyAuthority(ApplicationSecurityPermissions.SecurityAclAdminView,
                ApplicationSecurityPermissions.SecurityAclAdminAdd,
                ApplicationSecurityPermissions.SecurityAclAdminRemove,
                ApplicationSecurityPermissions.SecurityAclAdminChangeOwner)
            or securityAclService.hasPermission(SecurityFactory.getCurrentAuthentication(), permissionRequest.id, permissionRequest.domainClass, BasePermission.WRITE)) {
            securityAclService.deletePermission(permissionRequest)
        }
        throw Exception()
    }

    @PostMapping("/change-owner")
    @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.SecurityAclAdminChangeOwner}')")
    fun doChangeOwner(@Valid @RequestBody ownerRequest: ApplicationAclOwnerRequest, request: HttpServletRequest, response: HttpServletResponse) {
        val securityExpressionRoot = SecurityFactory.createSecurityExpressionRoot(request, response)
        if( securityExpressionRoot.hasAnyAuthority(ApplicationSecurityPermissions.SecurityAclAdminView,
                ApplicationSecurityPermissions.SecurityAclAdminAdd,
                ApplicationSecurityPermissions.SecurityAclAdminRemove,
                ApplicationSecurityPermissions.SecurityAclAdminChangeOwner)
            or securityAclService.hasPermission(SecurityFactory.getCurrentAuthentication(), ownerRequest.id, ownerRequest.domainClass, BasePermission.WRITE)) {
            securityAclService.changeOwner(ownerRequest)
        }
        throw Exception()
    }
}
