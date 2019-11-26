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

package com.it4logic.mindatory.exceptions

/**
 * Holder for Application Error Codes
 */
interface ApplicationErrorCodes {
	companion object {
		// Data Not Found
		const val DataNotFoundError = "mindatory.error.data_not_found"

		// Authorization
		const val AccessDeniedError = "mindatory.error.security.access_denied"

		// Data Integrity
		const val DataIntegrityError = "mindatory.error.data_integrity"

		// Validation
		const val ValidationError = "mindatory.error.validation"
		const val ValidationProjectHasModel = "mindatory.error.validation.project_has_model"
		const val ValidationModelHasVersionThatHasRelatedStoreData =
			"mindatory.error.validation.model_has_version_that_has_related_store_data"
		const val ValidationModelHasArtifactTemplatesRelatedStoreData =
			"mindatory.error.validation.model_has_artifact_templates_related_store_data"
		const val ValidationModelHasAttributeTemplatesRelatedStoreData =
			"mindatory.error.validation.model_has_attribute_templates_related_store_data"
		const val ValidationModelHasRelationTemplatesRelatedStoreData =
			"mindatory.error.validation.model_has_relation_templates_related_store_data"
		const val ValidationArtifactTemplateHasRelatedStoreData =
			"mindatory.error.validation.artifact_template_related_store_data"
		const val ValidationAttributeTemplateVersionHasRelatedStoreData =
			"mindatory.error.validation.attribute_template_version_related_store_data"
		const val ValidationArtifactTemplateVersionHasRelatedStoreData =
			"mindatory.error.validation.artifact_template_version_related_store_data"
		const val ValidationRelationTemplateVersionHasRelatedStoreData =
			"mindatory.error.validation.relation_template_version_related_store_data"
		const val ValidationRelationTemplateHasRelatedStoreData =
			"mindatory.error.validation.relation_template_related_store_data"
		const val ValidationModelHasArtifactTemplatesUsedInRelationTemplatesFromOtherModels =
			"mindatory.error.validation.model_has_artifact_templates_used_in_relation_templates_from_other_models"
		const val ValidationModelHasArtifactTemplatesUsedInArtifactTemplatesFromOtherModels =
			"mindatory.error.validation.model_has_attribute_templates_used_in_artifact_templates_from_other_models"
		const val ValidationModelHasStereotypesUsedInRelationTemplatesFromOtherModels =
			"mindatory.error.validation.model_has_stereotypes_used_in_relation_templates_from_other_models"
		const val ValidationArtifactTemplateUsedInRelationTemplates =
			"mindatory.error.validation.artifact_template_used_in_relation_templates"
		const val ValidationApplicationModelHasInDesignVersion =
			"mindatory.error.validation.model_has_in_design_version"
		const val ValidationArtifactTemplateHasNoInDesignVersion =
			"mindatory.error.validation.artifact_template_has_no_in_design_version"
		const val ValidationAttributeTemplateVersionUsedInArtifactTemplates =
			"mindatory.error.validation.attribute_template_version_used_in_artifact_templates"
		const val ValidationStereotypesUsedInRelationTemplates =
			"mindatory.error.validation.stereotype_used_in_relation_templates"
		const val ValidationAttributeTemplateHasInDesignVersion =
			"mindatory.error.validation.attribute_template_has_in_design_version"
		const val ValidationAttributeTemplateHasNoInDesignVersion =
			"mindatory.error.validation.attribute_template_has_no_in_design_version"
		const val ValidationCannotChangeStoreObjectsRelatedToNoneReleasedModelVersion =
			"mindatory.error.validation.cannot_change_store_objects_related_to_none_released_model_version"
		const val ValidationCannotChangeObjectsWithinNoneInDesignModelVersion =
			"mindatory.error.validation.cannot_change_objects_within_none_in_design_model_version"
		const val ValidationModelForVersionMismatch =
			"mindatory.error.validation.model_for_version__mismatch"
		const val ValidationStoreObjectVersionAndTemplateMismatch =
			"mindatory.error.validation.store_object_version_and_template_mismatch"
		const val ValidationCannotChangeAttributesInReleasedArtifactTemplateVersion =
			"mindatory.error.validation.cannot_change_attributes_in_released_artifact_template_version"
		const val ValidationAttributeAlreadyAddedToThisArtifactTemplateVersion =
			"mindatory.error.validation.attribute_already_added_to_this_artifact_template_version"
		const val ValidationCannotAddNoneReleasedAttributeToArtifactTemplateVersion =
			"mindatory.error.validation.cannot_add_none_released_attribute_to_artifact_template_version"
		const val ValidationCannotChangeNotInDesignApplicationModelVersion =
			"mindatory.error.validation.cannot_change_not_in_design_model_version"
		const val ValidationCannotChangeReleasedArtifactTemplateVersion =
			"mindatory.error.validation.cannot_change_released_artifact_template_version"
		const val ValidationCannotChangeReleasedRelationTemplateVersion =
			"mindatory.error.validation.cannot_change_released_relation_template_version"
		const val ValidationCannotDeleteArtifactStoreObjectThatUsedInRelationStoreObjects =
			"mindatory.error.validation.cannot_delete_artifact_store_object_that_used_in_relation_store_objects"
		const val ValidationCannotMigrateStoreObjectsToNoneReleasedVersion =
			"mindatory.error.validation.cannot_migrate_store_objects_to_none_released_version"
		const val ValidationCannotMigrateRelationStoresDueToRemovedSourceArtifactWithRelativeData =
			"mindatory.error.validation.cannot_migrate_relation_stores_due_to_removed_source_artifact_with_relative_data"
		const val ValidationCannotMigrateRelationStoresDueToRemovedTargetArtifactWithRelativeData =
			"mindatory.error.validation.cannot_migrate_relation_stores_due_to_removed_target_artifact_with_relative_data"
		const val ValidationGroupHasUsers = "mindatory.error.validation.group_has_users"
		const val ValidationPasswordsNotMatched = "mindatory.error.validation.passwords_not_matched"
		const val ValidationIncorrectUserPassword = "mindatory.error.validation.incorrect_user_password"
		const val ValidationChangeAnotherUserProfileNotAllowed =
			"mindatory.error.validation.change_another_user_profile_not_allowed"
		const val ValidationCannotCreateObjectWithExistingId =
			"mindatory.error.validation.cannot_create_object_with_existing_id"
		const val ValidationRelationTemplateHasInDesignVersion =
			"mindatory.error.validation.relation_template_has_in_design_version"
		const val ValidationAtLeastOneLanguageInSystem = "mindatory.error.validation.at_least_one_language_in_system"
		const val ValidationCannotDeleteDefaultLanguage = "mindatory.error.validation.cannot_delete_default_language"
		const val ValidationLanguageHasRelatedContents = "mindatory.error.validation.language_has_related_contents"
		const val ValidationMailTemplateUUIDNotFound = "mindatory.error.validation.mail_template_uuid_not_found"
		const val ValidationTokenUserIsNotMatching = "mindatory.error.validation.token_user_is_not_matching"
		const val ValidationTokenIsExpired = "mindatory.error.validation.token_is_expired"
		const val ValidationIdentifierNotMatched = "mindatory.error.validation.identifier_is_not_matched"
		const val ValidationRequestedModelVersionDoesNotBelongToModel =
			"mindatory.error.validation.requested_model_version_does_not_belong_to_model"
		const val ValidationRequestedObjectDoesNotBelongToModelVersion =
			"mindatory.error.validation.requested_object_does_not_belong_to_model_version"
		const val ValidationRequestedAttributeTemplateDoesNotBelongToArtifactTemplate =
			"mindatory.error.validation.requested_attribute_template_does_not_belong_to_artifact_template"
		const val ValidationRequestedObjectDoesNotBelongToProject =
			"mindatory.error.validation.requested_object_does_not_belong_to_project"
		const val ValidationRequestedObjectDoesNotBelongToArtifactTemplate =
			"mindatory.error.validation.requested_object_does_not_belong_to_artifact_template"
		const val ValidationRequestedObjectDoesNotBelongToRelationTemplate =
			"mindatory.error.validation.requested_object_does_not_belong_to_relation_template"
		const val ValidationCannotLinkModelVersionToItself =
			"mindatory.error.validation.cannot_link_model_version_to_itself"
		const val ValidationCannotLinkBetweenTwoVersionsInSameModel =
			"cannot_link_between_two_versions_in_same_model"
		const val ValidationCannotLinkProjectToNoneReleasedModelVersion =
			"mindatory.error.validation.cannot_link_project_to_none_released_model_version"
		const val ValidationModelDependencyDoesNotExistInModelVersion =
			"mindatory.error.validation.model_dependency_does_not_exist_in_model_version"
		const val ValidationModelDependencyIsAlreadyLatestRelease =
			"mindatory.error.validation.model_dependency_is_already_latest_release"
		const val ValidationModelHasNoReleasedVersion =
			"mindatory.error.validation.model_has_no_released_version"

		const val ValidationCannotUseArtifactInNoneReleasedVersionInsideProject =
			"mindatory.error.validation.cannot_use_artifact_in_none_released_version_inside_project"

		// Attribute Template Data Types
		const val ValidationAttributeTemplateDataTypeDoesNotExist = "mindatory.error.plugin.data_type_does_not_exit"
		const val ValidationAttributeTemplateDataTypeMissingProperty = "mindatory.error.plugin.missing_property"
		const val ValidationAttributeTemplateDataTypePropertyValueIsNotMatchingPropertyType =
			"mindatory.error.plugin.property_value_is_not_matching_property_type"
		const val ValidationAttributeTemplateDataTypePropertyValueIsNotInRange =
			"mindatory.error.plugin.validation.contents_is_not_in_range"
		const val ValidationAttributeTemplateDataTypeContentsIsNotMatchingDataType =
			"mindatory.error.plugin.contents_is_not_matching_data_type"

		const val ValidationAttributeTemplateDataTypeContentsIsRequired =
			"mindatory.error.plugin.validation.contents_is_required"
		const val ValidationAttributeTemplateDataTypeContentsIsLowerThanMinimum =
			"mindatory.error.plugin.validation.contents_is_lower_than_minimum"
		const val ValidationAttributeTemplateDataTypeContentsIsHigherThanMaximum =
			"mindatory.error.plugin.validation.contents_is_higher_than_minimum"
		const val ValidationAttributeTemplateDataTypeContentsLengthIsLowerThanMinimum =
			"mindatory.error.plugin.validation.contents_length_is_lower_than_minimum"
		const val ValidationAttributeTemplateDataTypeContentsLengthIsHigherThanMinimum =
			"mindatory.error.plugin.validation.contents_length_is_higher_than_minimum"
		const val ValidationAttributeTemplateDataTypeContentsIsNotMatchingPattern =
			"mindatory.error.plugin.validation.contents_is_not_matching_pattern"
		const val ValidationAttributeTemplateDataTypeContentsIsNotInRange =
			"mindatory.error.plugin.validation.contents_is_not_in_range"


		// Data Not Found
		const val NotFoundAttributeTemplateDataType = "mindatory.error.data.not_found_attribute_template_data_type"
		const val NotFoundDefaultLanguage = "mindatory.error.data.not_found_default_language"
		const val NotFoundPermission = "mindatory.error.data.not_found_permission"

//        const val NotFoundCompany                       = "mindatory.error.data.company_not_found"
//        const val NotFoundRole                          = "mindatory.error.data.role_not_found"
//        const val NotFoundGroup                         = "mindatory.error.data.group_not_found"
//        const val NotFoundUser                          = "mindatory.error.data.user_not_found"

		// JWT
		const val SecurityInvalidJwtSignature = "mindatory.error.security.invalid_jwt_signature"
		const val SecurityInvalidJwtToken = "mindatory.error.security.invalid_jwt_token"
		const val SecurityExpiredJwtToken = "mindatory.error.security.expired_jwt_token"
		const val SecurityUnsupportedJwtToken = "mindatory.error.security.unsupported_jwt_token"
		const val SecurityInvalidJwtContents = "mindatory.error.security.invalid_jwt_contents"

		// Authentication
		const val AuthenticationError = "mindatory.error.security.authentication_error"
		const val SecurityInvalidUsernameOrPassword = "mindatory.error.security.invalid_username_or_password"
		const val SecurityCredentialsExpired = "mindatory.error.security.credentials_expired"
		const val SecurityAccountDisabled = "mindatory.error.security.account_disabled"
		const val SecurityAccountExpired = "mindatory.error.security.account_expired"
		const val SecurityAccountLocked = "mindatory.error.security.account_locked"

		// Authorization
		const val AuthorizationError = "mindatory.error.security.authorization_error"

		// Mail
		const val SendMailError = "mindatory.error.mail.sending_error"

		// Database Duplications
		const val DuplicateLanguageLocale = "mindatory.error.duplicate.language_locale"
		const val DuplicateLanguageName = "mindatory.error.duplicate.language_name"
		const val DuplicateCompanyName = "mindatory.error.duplicate.company_name"
		const val DuplicateProjectName = "mindatory.error.duplicate.project_name"
		const val DuplicateModelName = "mindatory.error.duplicate.model_name"
		const val DuplicateModelProject = "mindatory.error.duplicate.model_project"
		const val DuplicateStereotypeName = "mindatory.error.duplicate.stereotype_name"
		const val DuplicateRelationTemplateIdentifier = "mindatory.error.duplicate.relation_template_name"
		const val DuplicateArtifactTemplateIdentification =
			"mindatory.error.duplicate.artifact_template_identification_version"
		const val DuplicateArtifactTemplateName = "mindatory.error.duplicate.artifact_template_name"
		const val DuplicateAttributeTemplateIdentification =
			"mindatory.error.duplicate.artifact_template_identification_version"
		const val DuplicateAttributeTemplateProperty = "mindatory.error.duplicate.artifact_template_property"
		const val DuplicateAttributeTemplateName = "mindatory.error.duplicate.attribute_template_name"
		const val DuplicateSecurityUserUsername = "mindatory.error.duplicate.security_user_username"
		const val DuplicateSecurityRoleName = "mindatory.error.duplicate.security_role_name"
		const val DuplicateSecurityGroupName = "mindatory.error.duplicate.security_group_name"
		const val DuplicateMailTemplateUUID = "mindatory.error.duplicate.mail_template_uuid"
		const val DuplicateStereotypeIdentifier = "mindatory.error.duplicate.stereotype_identifier"

		const val DuplicateCompanyMCL = "mindatory.error.duplicate.company_mcl"
		const val DuplicateProjectMCL = "mindatory.error.duplicate.project_mcl"
		const val DuplicateModelMCL = "mindatory.error.duplicate.model_mcl"
		const val DuplicateArtifactTemplateMCL = "mindatory.error.duplicate.artifact_template_mcl"
		const val DuplicateAttributeTemplateMCL = "mindatory.error.duplicate.attribute_template_mcl"
		const val DuplicateRelationTemplateMCL = "mindatory.error.duplicate.relation_template_mcl"
		const val DuplicateStereotypeMCL = "mindatory.error.duplicate.stereotype_mcl"
		const val DuplicateSecurityUserMCL = "mindatory.error.duplicate.security_user_mcl"
		const val DuplicateSecurityRoleMCL = "mindatory.error.duplicate.security_role_mcl"
		const val DuplicateSecurityGroupMCL = "mindatory.error.duplicate.security_group_mcl"
		const val DuplicateMailTemplateMCL = "mindatory.error.duplicate.mail_template_mcl"


	}
}