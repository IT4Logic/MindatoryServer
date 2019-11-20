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
package com.it4logic.mindatory.security

/**
 * Helper class for applying the view permission when a higher permission is already granted
 */
class SecurityPermissionsHelper {
	companion object {
		fun verifyViewPermission(permissions: ArrayList<String>) {

			if (permissions.contains(ApplicationSecurityPermissions.LanguageAdminCreate) ||
				permissions.contains(ApplicationSecurityPermissions.LanguageAdminModify) ||
				permissions.contains(ApplicationSecurityPermissions.LanguageAdminDelete)
			) {
				if (!permissions.contains(ApplicationSecurityPermissions.LanguageAdminView))
					permissions.add(ApplicationSecurityPermissions.LanguageAdminView)
			}

			if (permissions.contains(ApplicationSecurityPermissions.MailTemplateAdminCreate) ||
				permissions.contains(ApplicationSecurityPermissions.MailTemplateAdminModify) ||
				permissions.contains(ApplicationSecurityPermissions.MailTemplateAdminDelete)
			) {
				if (!permissions.contains(ApplicationSecurityPermissions.MailTemplateAdminView))
					permissions.add(ApplicationSecurityPermissions.MailTemplateAdminView)
			}

			if (permissions.contains(ApplicationSecurityPermissions.CompanyAdminCreate) ||
				permissions.contains(ApplicationSecurityPermissions.CompanyAdminModify) ||
				permissions.contains(ApplicationSecurityPermissions.CompanyAdminDelete)
			) {
				if (!permissions.contains(ApplicationSecurityPermissions.CompanyAdminView))
					permissions.add(ApplicationSecurityPermissions.CompanyAdminView)
			}

			if (permissions.contains(ApplicationSecurityPermissions.AppPreferencesAdminCreate) ||
				permissions.contains(ApplicationSecurityPermissions.AppPreferencesAdminModify) ||
				permissions.contains(ApplicationSecurityPermissions.AppPreferencesAdminDelete)
			) {
				if (!permissions.contains(ApplicationSecurityPermissions.AppPreferencesAdminView))
					permissions.add(ApplicationSecurityPermissions.AppPreferencesAdminView)
			}

			if (permissions.contains(ApplicationSecurityPermissions.SecurityRoleAdminCreate) ||
				permissions.contains(ApplicationSecurityPermissions.SecurityRoleAdminModify) ||
				permissions.contains(ApplicationSecurityPermissions.SecurityRoleAdminDelete)
			) {
				if (!permissions.contains(ApplicationSecurityPermissions.SecurityRoleAdminView))
					permissions.add(ApplicationSecurityPermissions.SecurityRoleAdminView)
			}

			if (permissions.contains(ApplicationSecurityPermissions.SecurityGroupAdminCreate) ||
				permissions.contains(ApplicationSecurityPermissions.SecurityGroupAdminModify) ||
				permissions.contains(ApplicationSecurityPermissions.SecurityGroupAdminDelete)
			) {
				if (!permissions.contains(ApplicationSecurityPermissions.SecurityGroupAdminView))
					permissions.add(ApplicationSecurityPermissions.SecurityGroupAdminView)
			}

			if (permissions.contains(ApplicationSecurityPermissions.SecurityUserAdminCreate) ||
				permissions.contains(ApplicationSecurityPermissions.SecurityUserAdminModify) ||
				permissions.contains(ApplicationSecurityPermissions.SecurityUserAdminDelete)
			) {
				if (!permissions.contains(ApplicationSecurityPermissions.SecurityUserAdminView))
					permissions.add(ApplicationSecurityPermissions.SecurityUserAdminView)
			}

			if (permissions.contains(ApplicationSecurityPermissions.SecurityAclAdminAdd) ||
				permissions.contains(ApplicationSecurityPermissions.SecurityAclAdminRemove) ||
				permissions.contains(ApplicationSecurityPermissions.SecurityAclAdminChangeOwner)
			) {
				if (!permissions.contains(ApplicationSecurityPermissions.SecurityAclAdminView))
					permissions.add(ApplicationSecurityPermissions.SecurityAclAdminView)
			}

			if (permissions.contains(ApplicationSecurityPermissions.ModelAdminCreate) ||
				permissions.contains(ApplicationSecurityPermissions.ModelAdminModify) ||
				permissions.contains(ApplicationSecurityPermissions.ModelAdminDelete)
			) {
				if (!permissions.contains(ApplicationSecurityPermissions.ModelAdminView))
					permissions.add(ApplicationSecurityPermissions.ModelAdminView)
			}

			if (permissions.contains(ApplicationSecurityPermissions.ProjectAdminCreate) ||
				permissions.contains(ApplicationSecurityPermissions.ProjectAdminModify) ||
				permissions.contains(ApplicationSecurityPermissions.ProjectAdminDelete)
			) {
				if (!permissions.contains(ApplicationSecurityPermissions.ProjectAdminView))
					permissions.add(ApplicationSecurityPermissions.ProjectAdminView)
			}

			if (permissions.contains(ApplicationSecurityPermissions.StereotypeAdminCreate) ||
				permissions.contains(ApplicationSecurityPermissions.StereotypeAdminModify) ||
				permissions.contains(ApplicationSecurityPermissions.StereotypeAdminDelete)
			) {
				if (!permissions.contains(ApplicationSecurityPermissions.StereotypeAdminView))
					permissions.add(ApplicationSecurityPermissions.StereotypeAdminView)
			}

			if (permissions.contains(ApplicationSecurityPermissions.RelationTemplateAdminCreate) ||
				permissions.contains(ApplicationSecurityPermissions.RelationTemplateAdminModify) ||
				permissions.contains(ApplicationSecurityPermissions.RelationTemplateAdminDelete)
			) {
				if (!permissions.contains(ApplicationSecurityPermissions.RelationTemplateAdminView))
					permissions.add(ApplicationSecurityPermissions.RelationTemplateAdminView)
			}

			if (permissions.contains(ApplicationSecurityPermissions.AttributeTemplateAdminCreate) ||
				permissions.contains(ApplicationSecurityPermissions.AttributeTemplateAdminModify) ||
				permissions.contains(ApplicationSecurityPermissions.AttributeTemplateAdminDelete)
			) {
				if (!permissions.contains(ApplicationSecurityPermissions.AttributeTemplateAdminView))
					permissions.add(ApplicationSecurityPermissions.AttributeTemplateAdminView)
			}

			if (permissions.contains(ApplicationSecurityPermissions.ArtifactTemplateAdminCreate) ||
				permissions.contains(ApplicationSecurityPermissions.ArtifactTemplateAdminModify) ||
				permissions.contains(ApplicationSecurityPermissions.ArtifactTemplateAdminDelete)
			) {
				if (!permissions.contains(ApplicationSecurityPermissions.ArtifactTemplateAdminView))
					permissions.add(ApplicationSecurityPermissions.ArtifactTemplateAdminView)
			}

			if (permissions.contains(ApplicationSecurityPermissions.ArtifactStoreAdminCreate) ||
				permissions.contains(ApplicationSecurityPermissions.ArtifactStoreAdminModify) ||
				permissions.contains(ApplicationSecurityPermissions.ArtifactStoreAdminDelete)
			) {
				if (!permissions.contains(ApplicationSecurityPermissions.ArtifactStoreAdminView))
					permissions.add(ApplicationSecurityPermissions.ArtifactStoreAdminView)
			}

			if (permissions.contains(ApplicationSecurityPermissions.RelationStoreAdminCreate) ||
				permissions.contains(ApplicationSecurityPermissions.RelationStoreAdminModify) ||
				permissions.contains(ApplicationSecurityPermissions.RelationStoreAdminDelete)
			) {
				if (!permissions.contains(ApplicationSecurityPermissions.RelationStoreAdminView))
					permissions.add(ApplicationSecurityPermissions.RelationStoreAdminView)
			}
		}
	}
}
