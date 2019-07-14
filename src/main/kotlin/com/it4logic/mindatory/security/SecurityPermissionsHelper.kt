package com.it4logic.mindatory.security

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

			if (permissions.contains(ApplicationSecurityPermissions.SecurityUserAdminCreate) ||
				permissions.contains(ApplicationSecurityPermissions.SecurityUserAdminModify) ||
				permissions.contains(ApplicationSecurityPermissions.SecurityUserAdminDelete)
			) {
				if (!permissions.contains(ApplicationSecurityPermissions.SecurityUserAdminView))
					permissions.add(ApplicationSecurityPermissions.SecurityUserAdminView)
			}
/*
		// Security ACL
		const val SecurityAclAdminView = "PERM_SECURITY_ACL_ADMIN_VIEW"
		const val SecurityAclAdminAdd = "PERM_SECURITY_ACL_ADMIN_CREATE"
		const val SecurityAclAdminRemove = "PERM_SECURITY_ACL_ADMIN_MODIFY"
		const val SecurityAclAdminChangeOwner = "PERM_SECURITY_ACL_ADMIN_DELETE"

		// Application Repositories
		const val ApplicationRepositoryAdminView = "PERM_APPLICATION_REPOSITORIES_ADMIN_VIEW"
		const val ApplicationRepositoryAdminCreate = "PERM_APPLICATION_REPOSITORIES_ADMIN_CREATE"
		const val ApplicationRepositoryAdminModify = "PERM_APPLICATION_REPOSITORIES_ADMIN_MODIFY"
		const val ApplicationRepositoryAdminDelete = "PERM_APPLICATION_REPOSITORIES_ADMIN_DELETE"

		// Solutions
		const val SolutionAdminView = "PERM_SOLUTIONS_ADMIN_VIEW"
		const val SolutionAdminCreate = "PERM_SOLUTIONS_ADMIN_CREATE"
		const val SolutionAdminModify = "PERM_SOLUTIONS_ADMIN_MODIFY"
		const val SolutionAdminDelete = "PERM_SOLUTIONS_ADMIN_DELETE"

		// Stereotypes
		const val StereotypeAdminView = "PERM_STEREOTYPES_ADMIN_VIEW"
		const val StereotypeAdminCreate = "PERM_STEREOTYPES_ADMIN_CREATE"
		const val StereotypeAdminModify = "PERM_STEREOTYPES_ADMIN_MODIFY"
		const val StereotypeAdminDelete = "PERM_STEREOTYPES_ADMIN_DELETE"

		// Join Templates
		const val JoinTemplateAdminView = "PERM_JOIN_TEMPLATES_ADMIN_VIEW"
		const val JoinTemplateAdminCreate = "PERM_JOIN_TEMPLATES_ADMIN_CREATE"
		const val JoinTemplateAdminModify = "PERM_JOIN_TEMPLATES_ADMIN_MODIFY"
		const val JoinTemplateAdminDelete = "PERM_JOIN_TEMPLATES_ADMIN_DELETE"

		const val JoinTemplateStoreAdminModify = "PERM_JOIN_TEMPLATE_STORES_ADMIN_MODIFY"

		// Attribute Templates
		const val AttributeTemplateAdminView = "PERM_ATTRIBUTE_TEMPLATES_ADMIN_VIEW"
		const val AttributeTemplateAdminCreate = "PERM_ATTRIBUTE_TEMPLATES_ADMIN_CREATE"
		const val AttributeTemplateAdminModify = "PERM_ATTRIBUTE_TEMPLATES_ADMIN_MODIFY"
		const val AttributeTemplateAdminDelete = "PERM_ATTRIBUTE_TEMPLATES_ADMIN_DELETE"

		// Artifact Templates
		const val ArtifactTemplateAdminView = "PERM_ARTIFACT_TEMPLATES_ADMIN_VIEW"
		const val ArtifactTemplateAdminCreate = "PERM_ARTIFACT_TEMPLATES_ADMIN_CREATE"
		const val ArtifactTemplateAdminModify = "PERM_ARTIFACT_TEMPLATES_ADMIN_MODIFY"
		const val ArtifactTemplateAdminDelete = "PERM_ARTIFACT_TEMPLATES_ADMIN_DELETE"

		// Artifact Stores
		const val ArtifactStoreAdminView = "PERM_ARTIFACT_STORES_ADMIN_VIEW"
		const val ArtifactStoreAdminCreate = "PERM_ARTIFACT_STORES_ADMIN_CREATE"
		const val ArtifactStoreAdminModify = "PERM_ARTIFACT_STORES_ADMIN_MODIFY"
		const val ArtifactStoreAdminDelete = "PERM_ARTIFACT_STORES_ADMIN_DELETE"

		// Join Stores
		const val JoinStoreAdminView = "PERM_JOIN_STORES_ADMIN_VIEW"
		const val JoinStoreAdminCreate = "PERM_JOIN_STORES_ADMIN_CREATE"
		const val JoinStoreAdminModify = "PERM_JOIN_STORES_ADMIN_MODIFY"
		const val JoinStoreAdminDelete = "PERM_JOIN_STORES_ADMIN_DELETE"
		*/
		}
	}
}