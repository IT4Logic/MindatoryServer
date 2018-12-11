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

package com.it4logic.mindatory.security

/**
 * Holder for Application Security Permissions
 */
interface ApplicationSecurityPermissions {
    companion object {
        const val PermissionRead                         = "read"
        const val PermissionCreate                       = "create"
        const val PermissionUpdate                       = "write"
        const val PermissionDelete                       = "delete"

        // Companies
        const val CompanyAdminView                       = "PERM_COMPANIES_ADMIN_VIEW"
        const val CompanyAdminCreate                     = "PERM_COMPANIES_ADMIN_UPDATE"
        const val CompanyAdminUpdate                     = "PERM_COMPANIES_ADMIN_UPDATE"
        const val CompanyAdminDelete                     = "PERM_COMPANIES_ADMIN_UPDATE"

        // Security Roles
        const val SecurityRoleAdminView                  = "PERM_SECURITY_ROLES_ADMIN_VIEW"
        const val SecurityRoleAdminCreate                = "PERM_SECURITY_ROLES_ADMIN_CREATE"
        const val SecurityRoleAdminUpdate                = "PERM_SECURITY_ROLES_ADMIN_UPDATE"
        const val SecurityRoleAdminDelete                = "PERM_SECURITY_ROLES_ADMIN_DELETE"

        // Security Group
        const val SecurityGroupAdminView                 = "PERM_SECURITY_GROUPS_ADMIN_VIEW"
        const val SecurityGroupAdminCreate               = "PERM_SECURITY_GROUPS_ADMIN_CREATE"
        const val SecurityGroupAdminUpdate               = "PERM_SECURITY_GROUPS_ADMIN_UPDATE"
        const val SecurityGroupAdminDelete               = "PERM_SECURITY_GROUPS_ADMIN_DELETE"

        // Security Users
        const val SecurityUserAdminView                  = "PERM_SECURITY_USERS_ADMIN_VIEW"
        const val SecurityUserAdminCreate                = "PERM_SECURITY_USERS_ADMIN_CREATE"
        const val SecurityUserAdminUpdate                = "PERM_SECURITY_USERS_ADMIN_UPDATE"
        const val SecurityUserAdminDelete                = "PERM_SECURITY_USERS_ADMIN_DELETE"

        // Security ACL
        const val SecurityAclAdminView                   = "PERM_SECURITY_ACL_ADMIN_VIEW"
        const val SecurityAclAdminAdd                    = "PERM_SECURITY_ACL_ADMIN_CREATE"
        const val SecurityAclAdminRemove                 = "PERM_SECURITY_ACL_ADMIN_UPDATE"
        const val SecurityAclAdminChangeOwner            = "PERM_SECURITY_ACL_ADMIN_DELETE"

        // Application Repositories
        const val ApplicationRepositoryAdminView        = "PERM_APPLICATION_REPOSITORIES_ADMIN_VIEW"
        const val ApplicationRepositoryAdminCreate      = "PERM_APPLICATION_REPOSITORIES_ADMIN_CREATE"
        const val ApplicationRepositoryAdminUpdate      = "PERM_APPLICATION_REPOSITORIES_ADMIN_UPDATE"
        const val ApplicationRepositoryAdminDelete      = "PERM_APPLICATION_REPOSITORIES_ADMIN_DELETE"

        // Solutions
        const val SolutionAdminView                     = "PERM_SOLUTIONS_ADMIN_VIEW"
        const val SolutionAdminCreate                   = "PERM_SOLUTIONS_ADMIN_CREATE"
        const val SolutionAdminUpdate                   = "PERM_SOLUTIONS_ADMIN_UPDATE"
        const val SolutionAdminDelete                   = "PERM_SOLUTIONS_ADMIN_DELETE"
    }
}