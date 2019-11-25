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
package com.it4logic.mindatory.services.common

/**
 * Utility class that acts like Data Services Registry Database
 */
class ApplicationServiceRegistry {
	companion object {
		val registry: HashMap<String, String> = hashMapOf(
			"com.it4logic.mindatory.model.mlc.Language" to "languageService",

			"com.it4logic.mindatory.model.Company" to "companyService",

			"com.it4logic.mindatory.model.AppPreferences" to "appPreferencesService",

			"com.it4logic.mindatory.model.mail.MailTemplate" to "mailTemplateService",

			"com.it4logic.mindatory.model.security.SecurityUserToken" to "securityUserTokenService",
			"com.it4logic.mindatory.model.security.SecurityUser" to "securityUserService",
			"com.it4logic.mindatory.model.security.SecurityRole" to "securityRoleService",
			"com.it4logic.mindatory.model.security.SecurityGroup" to "securityGroupService",

			"com.it4logic.mindatory.model.model.Model" to "modelService",
			"com.it4logic.mindatory.model.model.ModelVersion" to "modelVersionService",
			"com.it4logic.mindatory.model.model.ArtifactTemplate" to "artifactTemplateService",
			"com.it4logic.mindatory.model.model.AttributeTemplate" to "attributeTemplateService",
			"com.it4logic.mindatory.model.model.RelationTemplate" to "relationTemplateService",
			"com.it4logic.mindatory.model.model.Stereotype" to "stereotypeService",

			"com.it4logic.mindatory.model.project.Project" to "projectService",
			"com.it4logic.mindatory.model.project.ArtifactStore" to "artifactStoreService",
			"com.it4logic.mindatory.model.project.AttributeStore" to "attributeStoreService",
			"com.it4logic.mindatory.model.project.RelationStore" to "relationStoreService"
		)
	}
}