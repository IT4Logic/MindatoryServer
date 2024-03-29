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

package com.it4logic.mindatory.model.common

import com.it4logic.mindatory.exceptions.ApplicationErrorCodes

/**
 * Application constraints codes that will be used to generate indexes and database error handling
 */
abstract class ApplicationConstraintCodes {

	companion object {
		const val CompanyMCLUniqueIndex = "company_mcl_unq_idx"

		const val MailTemplateUUIDUniqueIndex = "mail_tmpl_uuid_mcl_unq_idx"
		const val MailTemplateMCLUniqueIndex = "mail_tmpl_mcl_unq_idx"

		const val LanguageLocaleUniqueIndex = "language_locale_unq_idx"
		const val LanguageNameUniqueIndex = "language_name_unq_idx"

		const val ModelVersionUniqueIndex = "model_ver_unq_idx"
		const val ModelMCLUniqueIndex = "model_mcl_unq_idx"

		const val ArtifactTemplateIdentifierUniqueIndex = "artifact_template_identifier_unq_idx"
		const val ArtifactTemplateMCLUniqueIndex = "artifact_template_mcl_unq_idx"

		const val AttributeTemplateIdentifierUniqueIndex = "attribute_template_identifier_unq_idx"
		const val AttributeTemplateMCLUniqueIndex = "attribute_template_mcl_unq_idx"

		const val AttributeTemplatePropertyUniqueIndex = "attribute_template_property_unq_idx"

		const val RelationTemplateIdentifierUniqueIndex = "relation_template_identifier_unq_idx"
		const val RelationTemplateMCLUniqueIndex = "relation_template_mcl_unq_idx"

		const val StereotypeIdentifierUniqueIndex = "stereotype_identifier_unq_idx"
		const val StereotypeMCLUniqueIndex = "stereotype_mcl_unq_idx"

		const val ProjectMCLUniqueIndex = "project_mcl_unq_idx"
		const val AttributeStoreMCLUniqueIndex = "attribute_store_mcl_unq_idx"

		const val SecurityGroupMCLUniqueIndex = "sec_group_mcl_unq_idx"

		const val SecurityRoleMCLUniqueIndex = "sec_role_mcl_unq_idx"

		const val SecurityUserUsernameUniqueIndex = "security_user_username_unq_idx"
		const val SecurityUserMCLUniqueIndex = "sec_user_mcl_unq_idx"


//		const val ModelProjectUniqueIndex = "model_project_unq_idx"
//
//
//		const val ArtifactTemplateVersionUniqueIndex = "artifact_template_ver_unq_idx"
//
//		const val AttributeTemplateVersionUniqueIndex = "attribute_template_ver_unq_idx"


		//        const val CompanyNameUniqueIndex                        = "company_name_unq_idx"
//        const val ProjectNameUniqueIndex                       = "project_name_unq_idx"
//        const val ApplicationRepositoryNameUniqueIndex          = "app_model_name_unq_idx"
//        const val StereotypeNameUniqueIndex                     = "stereotype_name_unq_idx"
//        const val ArtifactTemplateNameUniqueIndex               = "artifact_template_name_unq_idx"
//        const val AttributeTemplateNameUniqueIndex              = "attribute_template_name_unq_idx"
//        const val SecurityRoleNameUniqueIndex                   = "security_role_name_unq_idx"
//        const val SecurityGroupNameUniqueIndex                  = "security_group_name_unq_idx"


		val map: HashMap<String, String> = hashMapOf(
//              CompanyNameUniqueIndex to ApplicationErrorCodes.DuplicateCompanyName,
//              ProjectNameUniqueIndex to ApplicationErrorCodes.DuplicateProjectName,
//              ApplicationRepositoryNameUniqueIndex to ApplicationErrorCodes.DuplicateModelName,
//              StereotypeNameUniqueIndex to ApplicationErrorCodes.DuplicateStereotypeName,
//              ArtifactTemplateNameUniqueIndex to ApplicationErrorCodes.DuplicateArtifactTemplateName,
//              AttributeTemplateNameUniqueIndex to ApplicationErrorCodes.DuplicateAttributeTemplateName,
//              SecurityRoleNameUniqueIndex to ApplicationErrorCodes.DuplicateSecurityRoleName,
//              SecurityGroupNameUniqueIndex to ApplicationErrorCodes.DuplicateSecurityGroupName,
			LanguageLocaleUniqueIndex to ApplicationErrorCodes.DuplicateLanguageLocale,
			LanguageNameUniqueIndex to ApplicationErrorCodes.DuplicateLanguageName,
//			ModelProjectUniqueIndex to ApplicationErrorCodes.DuplicateModelProject,
			RelationTemplateIdentifierUniqueIndex to ApplicationErrorCodes.DuplicateRelationTemplateIdentifier,
			ArtifactTemplateIdentifierUniqueIndex to ApplicationErrorCodes.DuplicateArtifactTemplateIdentification,
			AttributeTemplateIdentifierUniqueIndex to ApplicationErrorCodes.DuplicateAttributeTemplateIdentification,
			AttributeTemplatePropertyUniqueIndex to ApplicationErrorCodes.DuplicateAttributeTemplateProperty,
			SecurityUserUsernameUniqueIndex to ApplicationErrorCodes.DuplicateSecurityUserUsername,
			MailTemplateUUIDUniqueIndex to ApplicationErrorCodes.DuplicateMailTemplateUUID,
			StereotypeIdentifierUniqueIndex to ApplicationErrorCodes.DuplicateStereotypeIdentifier,

			CompanyMCLUniqueIndex to ApplicationErrorCodes.DuplicateCompanyMCL,
			ProjectMCLUniqueIndex to ApplicationErrorCodes.DuplicateProjectMCL,
			ModelMCLUniqueIndex to ApplicationErrorCodes.DuplicateModelMCL,
			ArtifactTemplateMCLUniqueIndex to ApplicationErrorCodes.DuplicateArtifactTemplateMCL,
			AttributeTemplateMCLUniqueIndex to ApplicationErrorCodes.DuplicateAttributeTemplateMCL,
			RelationTemplateMCLUniqueIndex to ApplicationErrorCodes.DuplicateRelationTemplateMCL,
			StereotypeMCLUniqueIndex to ApplicationErrorCodes.DuplicateStereotypeMCL,
			SecurityUserMCLUniqueIndex to ApplicationErrorCodes.DuplicateSecurityUserMCL,
			SecurityRoleMCLUniqueIndex to ApplicationErrorCodes.DuplicateSecurityRoleMCL,
			SecurityGroupMCLUniqueIndex to ApplicationErrorCodes.DuplicateSecurityGroupMCL,
			MailTemplateMCLUniqueIndex to ApplicationErrorCodes.DuplicateMailTemplateMCL

		)
	}
}