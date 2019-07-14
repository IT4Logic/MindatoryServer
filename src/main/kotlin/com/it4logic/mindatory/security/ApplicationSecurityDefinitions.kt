/*
    Copyright (c) 2019, IT4Logic.

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
        const val PermissionView                         = "read"
        const val PermissionCreate                       = "create"
        const val PermissionModify                       = "write"
        const val PermissionDelete                       = "delete"

        const val SystemWideAdmin                         = "PERM_SYSTEM_WIDE_ADMIN"

        const val SystemAdmin                             = "PERM_SYSTEM_ADMIN"

        // Languages
        const val LanguageAdminView                       = "PERM_LANGUAGES_ADMIN_VIEW"
        const val LanguageAdminCreate                     = "PERM_LANGUAGES_ADMIN_CREATE"
        const val LanguageAdminModify                     = "PERM_LANGUAGES_ADMIN_MODIFY"
        const val LanguageAdminDelete                     = "PERM_LANGUAGES_ADMIN_DELETE"

        // Mail Templates
        const val MailTemplateAdminView                   = "PERM_MAIL_TEMPLATES_ADMIN_VIEW"
        const val MailTemplateAdminCreate                 = "PERM_MAIL_TEMPLATES_ADMIN_CREATE"
        const val MailTemplateAdminModify                 = "PERM_MAIL_TEMPLATES_ADMIN_MODIFY"
        const val MailTemplateAdminDelete                 = "PERM_MAIL_TEMPLATES_ADMIN_DELETE"
        
        // Companies
        const val CompanyAdminView                       = "PERM_COMPANIES_ADMIN_VIEW"
        const val CompanyAdminCreate                     = "PERM_COMPANIES_ADMIN_CREATE"
        const val CompanyAdminModify                     = "PERM_COMPANIES_ADMIN_MODIFY"
        const val CompanyAdminDelete                     = "PERM_COMPANIES_ADMIN_DELETE"

        // Preferences
        const val AppPreferencesAdminView                = "PERM_APP_PREFERENCES_ADMIN_VIEW"
        const val AppPreferencesAdminCreate              = "PERM_APP_PREFERENCES_ADMIN_CREATE"
        const val AppPreferencesAdminModify              = "PERM_APP_PREFERENCES_ADMIN_MODIFY"
        const val AppPreferencesAdminDelete              = "PERM_APP_PREFERENCES_ADMIN_DELETE"
        
        // Security Roles
        const val SecurityRoleAdminView                  = "PERM_SECURITY_ROLES_ADMIN_VIEW"
        const val SecurityRoleAdminCreate                = "PERM_SECURITY_ROLES_ADMIN_CREATE"
        const val SecurityRoleAdminModify                = "PERM_SECURITY_ROLES_ADMIN_MODIFY"
        const val SecurityRoleAdminDelete                = "PERM_SECURITY_ROLES_ADMIN_DELETE"

        // Security Group
        const val SecurityGroupAdminView                 = "PERM_SECURITY_GROUPS_ADMIN_VIEW"
        const val SecurityGroupAdminCreate               = "PERM_SECURITY_GROUPS_ADMIN_CREATE"
        const val SecurityGroupAdminModify               = "PERM_SECURITY_GROUPS_ADMIN_MODIFY"
        const val SecurityGroupAdminDelete               = "PERM_SECURITY_GROUPS_ADMIN_DELETE"

        // Security Users
        const val SecurityUserAdminView                  = "PERM_SECURITY_USERS_ADMIN_VIEW"
        const val SecurityUserAdminCreate                = "PERM_SECURITY_USERS_ADMIN_CREATE"
        const val SecurityUserAdminModify                = "PERM_SECURITY_USERS_ADMIN_MODIFY"
        const val SecurityUserAdminDelete                = "PERM_SECURITY_USERS_ADMIN_DELETE"

        // Security ACL
        const val SecurityAclAdminView                   = "PERM_SECURITY_ACL_ADMIN_VIEW"
        const val SecurityAclAdminAdd                    = "PERM_SECURITY_ACL_ADMIN_CREATE"
        const val SecurityAclAdminRemove                 = "PERM_SECURITY_ACL_ADMIN_MODIFY"
        const val SecurityAclAdminChangeOwner            = "PERM_SECURITY_ACL_ADMIN_DELETE"

        // Application Repositories
        const val ApplicationRepositoryAdminView        = "PERM_APPLICATION_REPOSITORIES_ADMIN_VIEW"
        const val ApplicationRepositoryAdminCreate      = "PERM_APPLICATION_REPOSITORIES_ADMIN_CREATE"
        const val ApplicationRepositoryAdminModify      = "PERM_APPLICATION_REPOSITORIES_ADMIN_MODIFY"
        const val ApplicationRepositoryAdminDelete      = "PERM_APPLICATION_REPOSITORIES_ADMIN_DELETE"

        // Solutions
        const val SolutionAdminView                     = "PERM_SOLUTIONS_ADMIN_VIEW"
        const val SolutionAdminCreate                   = "PERM_SOLUTIONS_ADMIN_CREATE"
        const val SolutionAdminModify                   = "PERM_SOLUTIONS_ADMIN_MODIFY"
        const val SolutionAdminDelete                   = "PERM_SOLUTIONS_ADMIN_DELETE"

        // Stereotypes
        const val StereotypeAdminView                   = "PERM_STEREOTYPES_ADMIN_VIEW"
        const val StereotypeAdminCreate                 = "PERM_STEREOTYPES_ADMIN_CREATE"
        const val StereotypeAdminModify                 = "PERM_STEREOTYPES_ADMIN_MODIFY"
        const val StereotypeAdminDelete                 = "PERM_STEREOTYPES_ADMIN_DELETE"

        // Join Templates
        const val JoinTemplateAdminView                 = "PERM_JOIN_TEMPLATES_ADMIN_VIEW"
        const val JoinTemplateAdminCreate               = "PERM_JOIN_TEMPLATES_ADMIN_CREATE"
        const val JoinTemplateAdminModify               = "PERM_JOIN_TEMPLATES_ADMIN_MODIFY"
        const val JoinTemplateAdminDelete               = "PERM_JOIN_TEMPLATES_ADMIN_DELETE"

        const val JoinTemplateStoreAdminModify          = "PERM_JOIN_TEMPLATE_STORES_ADMIN_MODIFY"

        // Attribute Templates
        const val AttributeTemplateAdminView            = "PERM_ATTRIBUTE_TEMPLATES_ADMIN_VIEW"
        const val AttributeTemplateAdminCreate          = "PERM_ATTRIBUTE_TEMPLATES_ADMIN_CREATE"
        const val AttributeTemplateAdminModify          = "PERM_ATTRIBUTE_TEMPLATES_ADMIN_MODIFY"
        const val AttributeTemplateAdminDelete          = "PERM_ATTRIBUTE_TEMPLATES_ADMIN_DELETE"

        // Artifact Templates
        const val ArtifactTemplateAdminView             = "PERM_ARTIFACT_TEMPLATES_ADMIN_VIEW"
        const val ArtifactTemplateAdminCreate           = "PERM_ARTIFACT_TEMPLATES_ADMIN_CREATE"
        const val ArtifactTemplateAdminModify           = "PERM_ARTIFACT_TEMPLATES_ADMIN_MODIFY"
        const val ArtifactTemplateAdminDelete           = "PERM_ARTIFACT_TEMPLATES_ADMIN_DELETE"

        // Artifact Stores
        const val ArtifactStoreAdminView                = "PERM_ARTIFACT_STORES_ADMIN_VIEW"
        const val ArtifactStoreAdminCreate              = "PERM_ARTIFACT_STORES_ADMIN_CREATE"
        const val ArtifactStoreAdminModify              = "PERM_ARTIFACT_STORES_ADMIN_MODIFY"
        const val ArtifactStoreAdminDelete              = "PERM_ARTIFACT_STORES_ADMIN_DELETE"

        // Join Stores
        const val JoinStoreAdminView                    = "PERM_JOIN_STORES_ADMIN_VIEW"
        const val JoinStoreAdminCreate                  = "PERM_JOIN_STORES_ADMIN_CREATE"
        const val JoinStoreAdminModify                  = "PERM_JOIN_STORES_ADMIN_MODIFY"
        const val JoinStoreAdminDelete                  = "PERM_JOIN_STORES_ADMIN_DELETE"
        
        // Attribute Stores
//        const val AttributeStoreAdminView                     = "PERM_ATTRIBUTE_STORES_ADMIN_VIEW"
//        const val AttributeStoreAdminCreate                   = "PERM_ATTRIBUTE_STORES_ADMIN_CREATE"
//        const val AttributeStoreAdminModify                   = "PERM_ATTRIBUTE_STORES_ADMIN_MODIFY"
//        const val AttributeStoreAdminDelete                   = "PERM_ATTRIBUTE_STORES_ADMIN_DELETE"
        
        
        val Permissions = listOf(
            SystemWideAdmin,
            LanguageAdminView,
            LanguageAdminCreate,
            LanguageAdminModify,
            LanguageAdminDelete,
            CompanyAdminView,
            CompanyAdminCreate,
            CompanyAdminModify,
            CompanyAdminDelete,
            SecurityRoleAdminView,
            SecurityRoleAdminCreate,
            SecurityRoleAdminModify,
            SecurityRoleAdminDelete,
            SecurityGroupAdminView,
            SecurityGroupAdminCreate,
            SecurityGroupAdminModify,
            SecurityGroupAdminDelete,
            SecurityUserAdminView,
            SecurityUserAdminCreate,
            SecurityUserAdminModify,
            SecurityUserAdminDelete,
            SecurityAclAdminView,
            SecurityAclAdminAdd,
            SecurityAclAdminRemove,
            SecurityAclAdminChangeOwner,
            ApplicationRepositoryAdminView,
            ApplicationRepositoryAdminCreate,
            ApplicationRepositoryAdminModify,
            ApplicationRepositoryAdminDelete,
            SolutionAdminView,
            SolutionAdminCreate,
            SolutionAdminModify,
            SolutionAdminDelete,
            StereotypeAdminView,
            StereotypeAdminCreate,
            StereotypeAdminModify,
            StereotypeAdminDelete,
            JoinTemplateAdminView,
            JoinTemplateAdminCreate,
            JoinTemplateAdminModify,
            JoinTemplateAdminDelete,
            JoinTemplateStoreAdminModify,
            AttributeTemplateAdminView,
            AttributeTemplateAdminCreate,
            AttributeTemplateAdminModify,
            AttributeTemplateAdminDelete,
            ArtifactTemplateAdminView,
            ArtifactTemplateAdminCreate,
            ArtifactTemplateAdminModify,
            ArtifactTemplateAdminDelete,
            ArtifactStoreAdminView,
            ArtifactStoreAdminCreate,
            ArtifactStoreAdminModify,
            ArtifactStoreAdminDelete,
            JoinStoreAdminView,
            JoinStoreAdminCreate,
            JoinStoreAdminModify,
            JoinStoreAdminDelete
        )

    }
}