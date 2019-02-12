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

package com.it4logic.mindatory.model.common

import com.it4logic.mindatory.exceptions.ApplicationErrorCodes

/**
 * Application constraints codes that will be used to generate indexes and database error handling
 */
abstract class ApplicationConstraintCodes {

    companion object {
        const val LanguageLocaleUniqueIndex                     = "language_locale_unq_idx"
        const val LanguageNameUniqueIndex                       = "language_name_unq_idx"
        const val CompanyNameUniqueIndex                        = "company_name_unq_idx"
        //const val SolutionNameUniqueIndex                       = "solution_name_unq_idx"
        const val ApplicationRepositoryNameUniqueIndex          = "app_repo_name_unq_idx"
        const val ApplicationRepositorySolutionUniqueIndex      = "app_repo_solution_unq_idx"
        const val StereotypeNameUniqueIndex                     = "stereotype_name_unq_idx"
        const val JoinTemplateIdentifierUniqueIndex             = "join_template_identifier_unq_idx"
        const val ArtifactTemplateIdentifierUniqueIndex         = "artifact_template_identifier_unq_idx"
        const val ArtifactTemplateNameUniqueIndex               = "artifact_template_name_unq_idx"
        const val ArtifactTemplateVersionUniqueIndex            = "artifact_template_ver_unq_idx"
        const val AttributeTemplateIdentifierUniqueIndex        = "attribute_template_identifier_unq_idx"
        const val AttributeTemplateNameUniqueIndex              = "attribute_template_name_unq_idx"
        const val AttributeTemplateVersionUniqueIndex           = "attribute_template_ver_unq_idx"
        const val SecurityUserUsernameUniqueIndex               = "security_user_username_unq_idx"
        const val SecurityRoleNameUniqueIndex                   = "security_role_name_unq_idx"
        const val SecurityGroupNameUniqueIndex                  = "security_group_name_unq_idx"

        const val SolutionMCLUniqueIndex                        = "solution_mcl_unq_idx"



        val map: HashMap<String, String> = hashMapOf(
                LanguageLocaleUniqueIndex to ApplicationErrorCodes.DuplicateLanguageLocale,
                LanguageNameUniqueIndex to ApplicationErrorCodes.DuplicateLanguageName,
                CompanyNameUniqueIndex to ApplicationErrorCodes.DuplicateCompanyName,
                //SolutionNameUniqueIndex to ApplicationErrorCodes.DuplicateSolutionName,
                ApplicationRepositoryNameUniqueIndex to ApplicationErrorCodes.DuplicateApplicationRepositoryName,
                ApplicationRepositorySolutionUniqueIndex to ApplicationErrorCodes.DuplicateApplicationRepositorySolution,
                StereotypeNameUniqueIndex to ApplicationErrorCodes.DuplicateStereotypeName,
                JoinTemplateIdentifierUniqueIndex to ApplicationErrorCodes.DuplicateJoinTemplateIdentifier,
                ArtifactTemplateIdentifierUniqueIndex to ApplicationErrorCodes.DuplicateArtifactTemplateIdentification,
                ArtifactTemplateNameUniqueIndex to ApplicationErrorCodes.DuplicateArtifactTemplateName,
                AttributeTemplateIdentifierUniqueIndex to ApplicationErrorCodes.DuplicateAttributeTemplateIdentification,
                AttributeTemplateNameUniqueIndex to ApplicationErrorCodes.DuplicateAttributeTemplateName,
                SecurityUserUsernameUniqueIndex to ApplicationErrorCodes.DuplicateSecurityUserUsername,
                SecurityRoleNameUniqueIndex to ApplicationErrorCodes.DuplicateSecurityRoleName,
                SecurityGroupNameUniqueIndex to ApplicationErrorCodes.DuplicateSecurityGroupName,

                SolutionMCLUniqueIndex to ApplicationErrorCodes.DuplicateSolutionMCL
        )
    }
}