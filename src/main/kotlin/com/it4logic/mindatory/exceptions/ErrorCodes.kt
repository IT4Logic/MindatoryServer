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

package com.it4logic.mindatory.exceptions

/**
 * Holder for Application Error Codes
 */
interface ApplicationErrorCodes {
    companion object {
        // Data Integrity
        const val DataIntegrityError                    = "mindatory.error.data_integrity"
        
        // Validation
        const val ValidationError
                = "mindatory.error.validation"
        const val ValidationSolutionHasRepository
                = "mindatory.error.validation.solution_has_repository"
        const val ValidationRepositoryHasArtifactTemplatesRelatedStoreData
                = "mindatory.error.validation.repository_has_artifact_templates_related_store_data"
        const val ValidationRepositoryHasAttributeTemplatesRelatedStoreData
                = "mindatory.error.validation.repository_has_attribute_templates_related_store_data"
        const val ValidationRepositoryHasJoinTemplatesRelatedStoreData
                = "mindatory.error.validation.repository_has_join_templates_related_store_data"
        const val ValidationArtifactTemplateHasRelatedStoreData
                = "mindatory.error.validation.artifact_template_related_store_data"
        const val ValidationAttributeTemplateVersionHasRelatedStoreData
                = "mindatory.error.validation.attribute_template_version_related_store_data"
        const val ValidationArtifactTemplateVersionHasRelatedStoreData
                = "mindatory.error.validation.artifact_template_version_related_store_data"
        const val ValidationJoinTemplateVersionHasRelatedStoreData
                = "mindatory.error.validation.join_template_version_related_store_data"
        const val ValidationJoinTemplateHasRelatedStoreData
                = "mindatory.error.validation.join_template_related_store_data"
        const val ValidationRepositoryHasArtifactTemplatesUsedInJoinTemplatesFromOtherRepositories
                = "mindatory.error.validation.repository_has_artifact_templates_used_in_join_templates_from_other_repositories"
        const val ValidationRepositoryHasArtifactTemplatesUsedInArtifactTemplatesFromOtherRepositories
                = "mindatory.error.validation.repository_has_attribute_templates_used_in_artifact_templates_from_other_repositories"
        const val ValidationRepositoryHasStereotypesUsedInJoinTemplatesFromOtherRepositories
                = "mindatory.error.validation.repository_has_stereotypes_used_in_join_templates_from_other_repositories"
        const val ValidationArtifactTemplateUsedInJoinTemplates
                = "mindatory.error.validation.artifact_template_used_in_join_templates"
        const val ValidationArtifactTemplateHasInDesignVersion
                = "mindatory.error.validation.artifact_template_has_in_design_version"
        const val ValidationArtifactTemplateHasNoInDesignVersion
                = "mindatory.error.validation.artifact_template_has_no_in_design_version"
        const val ValidationAttributeTemplateVersionUsedInArtifactTemplates
                = "mindatory.error.validation.attribute_template_version_used_in_artifact_templates"
        const val ValidationStereotypesUsedInJoinTemplates
                = "mindatory.error.validation.stereotype_used_in_join_templates"
        const val ValidationAttributeTemplateHasInDesignVersion
                = "mindatory.error.validation.attribute_template_has_in_design_version"
        const val ValidationAttributeTemplateHasNoInDesignVersion
                = "mindatory.error.validation.attribute_template_has_no_in_design_version"
        const val ValidationStoreObjectCanOnlyBeAssociatedWithReleasedVersion
                = "mindatory.error.validation.store_object_can_only_be_associated_with_released_version"
        const val ValidationStoreObjectVersionAndTemplateMismatch
                = "mindatory.error.validation.store_object_version_and_template_mismatch"
        const val ValidationCannotChangeAttributesInReleasedArtifactTemplateVersion
                = "mindatory.error.validation.cannot_change_attributes_in_released_artifact_template_version"
        const val ValidationAttributeAlreadyAddedToThisArtifactTemplateVersion
                = "mindatory.error.validation.attribute_already_added_to_this_artifact_template_version"
        const val ValidationCannotAddNoneReleasedAttributeToArtifactTemplateVersion
                = "mindatory.error.validation.cannot_add_none_released_attribute_to_artifact_template_version"
        const val ValidationCannotChangeReleasedAttributeTemplateVersion
                = "mindatory.error.validation.cannot_change_released_attribute_template_version"
        const val ValidationCannotChangeReleasedArtifactTemplateVersion
                = "mindatory.error.validation.cannot_change_released_artifact_template_version"
        const val ValidationCannotChangeReleasedJoinTemplateVersion
                = "mindatory.error.validation.cannot_change_released_join_template_version"
        const val ValidationCannotDeleteArtifactStoreObjectThatUsedInJoinStoreObjects
                = "mindatory.error.validation.cannot_delete_artifact_store_object_that_used_in_join_store_objects"
        const val ValidationCannotMigrateStoreObjectsToNoneReleasedVersion
                = "mindatory.error.validation.cannot_migrate_store_objects_to_none_released_version"
        const val ValidationCannotMigrateJoinStoresDueToRemovedSourceArtifactWithRelativeData
                = "mindatory.error.validation.cannot_migrate_join_stores_due_to_removed_source_artifact_with_relative_data"
        const val ValidationCannotMigrateJoinStoresDueToRemovedTargetArtifactWithRelativeData
                = "mindatory.error.validation.cannot_migrate_join_stores_due_to_removed_target_artifact_with_relative_data"
        const val ValidationGroupHasUsers
                = "mindatory.error.validation.group_has_users"
        const val ValidationPasswordsNotMatched
                = "mindatory.error.validation.passwords_not_matched"
        const val ValidationIncorrectUserPassword
                = "mindatory.error.validation.incorrect_user_password"
        const val ValidationChangeAnotherUserProfileNotAllowed
                = "mindatory.error.validation.change_another_user_profile_not_allowed"
        const val ValidationCannotCreateObjectWithExistingId
                = "mindatory.error.validation.cannot_create_object_with_existing_id"
        const val ValidationJoinTemplateHasInDesignVersion
                = "mindatory.error.validation.join_template_has_in_design_version"
        const val ValidationAtLeastOneLanguageInSystem
                = "mindatory.error.validation.at_least_one_language_in_system"
        const val ValidationCannotDeleteDefaultLanguage
                = "mindatory.error.validation.cannot_delete_default_language"
        const val ValidationLanguageHasRelatedContents
                = "mindatory.error.validation.language_has_related_contents"
        const val ValidationMailTemplateUUIDNotFound
                = "mindatory.error.validation.mail_template_uuid_not_found"
        const val ValidationTokenUserIsNotMatching
                = "mindatory.error.validation.token_user_is_not_matching"
        const val ValidationTokenIsExpired
                = "mindatory.error.validation.token_is_expired"

        // Data Not Found
        const val NotFoundAttributeTemplateDataType     = "mindatory.error.data.not_found_attribute_template_data_type"
        const val NotFoundDefaultLanguage               = "mindatory.error.data.not_found_default_language"
        const val NotFoundPermission                    = "mindatory.error.data.not_found_permission"

//        const val NotFoundCompany                       = "mindatory.error.data.company_not_found"
//        const val NotFoundRole                          = "mindatory.error.data.role_not_found"
//        const val NotFoundGroup                         = "mindatory.error.data.group_not_found"
//        const val NotFoundUser                          = "mindatory.error.data.user_not_found"

        // JWT
        const val SecurityInvalidJwtSignature           = "mindatory.error.security.invalid_jwt_signature"
        const val SecurityInvalidJwtToken               = "mindatory.error.security.invalid_jwt_token"
        const val SecurityExpiredJwtToken               = "mindatory.error.security.expired_jwt_token"
        const val SecurityUnsupportedJwtToken           = "mindatory.error.security.unsupported_jwt_token"
        const val SecurityInvalidJwtContents            = "mindatory.error.security.invalid_jwt_contents"

        // Authentication
        const val AuthenticationError                   = "mindatory.error.security.authentication_error"
        const val SecurityInvalidUsernameOrPassword     = "mindatory.error.security.invalid_username_or_password"
        const val SecurityCredentialsExpired            = "mindatory.error.security.credentials_expired"
        const val SecurityAccountDisabled               = "mindatory.error.security.account_disabled"
        const val SecurityAccountExpired                = "mindatory.error.security.account_expired"
        const val SecurityAccountLocked                 = "mindatory.error.security.account_locked"

        // Authorization
        const val AuthorizationError                            = "mindatory.error.security.authorization_error"

        // Mail
        const val SendMailError                                 = "mindatory.error.mail.sending_error"

        // Database Duplications
        const val DuplicateLanguageLocale                       = "mindatory.error.duplicate.language_locale"
        const val DuplicateLanguageName                         = "mindatory.error.duplicate.language_name"
        const val DuplicateCompanyName                          = "mindatory.error.duplicate.company_name"
        const val DuplicateSolutionName                         = "mindatory.error.duplicate.solution_name"
        const val DuplicateApplicationRepositoryName            = "mindatory.error.duplicate.application_repository_name"
        const val DuplicateApplicationRepositorySolution        = "mindatory.error.duplicate.application_repository_solution"
        const val DuplicateStereotypeName                       = "mindatory.error.duplicate.stereotype_name"
        const val DuplicateJoinTemplateIdentifier               = "mindatory.error.duplicate.join_template_name"
        const val DuplicateArtifactTemplateIdentification       = "mindatory.error.duplicate.artifact_template_identification_version"
        const val DuplicateArtifactTemplateName                 = "mindatory.error.duplicate.artifact_template_name"
        const val DuplicateAttributeTemplateIdentification      = "mindatory.error.duplicate.artifact_template_identification_version"
        const val DuplicateAttributeTemplateName                = "mindatory.error.duplicate.attribute_template_name"
        const val DuplicateSecurityUserUsername                 = "mindatory.error.duplicate.security_user_username"
        const val DuplicateSecurityRoleName                     = "mindatory.error.duplicate.security_role_name"
        const val DuplicateSecurityGroupName                    = "mindatory.error.duplicate.security_group_name"
        const val DuplicateMailTemplateUUID                     = "mindatory.error.duplicate.mail_template_uuid"

        const val DuplicateCompanyMCL                           = "mindatory.error.duplicate.company_mcl"
        const val DuplicateSolutionMCL                          = "mindatory.error.duplicate.solution_mcl"
        const val DuplicateApplicationRepositoryMCL             = "mindatory.error.duplicate.app_repo_mcl"
        const val DuplicateArtifactTemplateMCL                  = "mindatory.error.duplicate.artifact_template_mcl"
        const val DuplicateAttributeTemplateMCL                 = "mindatory.error.duplicate.attribute_template_mcl"
        const val DuplicateJoinTemplateMCL                      = "mindatory.error.duplicate.join_template_mcl"
        const val DuplicateStereotypeMCL                        = "mindatory.error.duplicate.stereotype_mcl"
        const val DuplicateSecurityUserMCL                      = "mindatory.error.duplicate.security_user_mcl"
        const val DuplicateSecurityRoleMCL                      = "mindatory.error.duplicate.security_role_mcl"
        const val DuplicateSecurityGroupMCL                     = "mindatory.error.duplicate.security_group_mcl"
        const val DuplicateMailTemplateMCL                      = "mindatory.error.duplicate.mail_template_mcl"



    }
}