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

package com.it4logic.mindatory.controllers

/**
 * Holder for Application Controller Entry Points
 */
interface ApplicationControllerEntryPoints {
	companion object {
		const val Authentication: String = "/auth/"
		

		const val Locale: String = "{locale}"


		const val Actuator: String = "/actuator/"

		const val Security: String = "/security/$Locale/"

		const val Languages: String = "/api/$Locale/languages/"

		const val Company: String = "/api/$Locale/company/"

		const val Preferences: String = "/api/preferences/$Locale/"

		const val MailTemplates: String = "/api/mail-templates/$Locale/"

		const val SecurityRoles: String = "/api/security/roles/$Locale/"
		const val SecurityGroups: String = "/api/security/groups/$Locale/"
		const val SecurityUsers: String = "/api/security/users/$Locale/"
		const val SecurityUserProfile: String = "/api/security/profile/$Locale/"

		const val Models: String = "/api/$Locale/models/"

		const val AttributeTemplateDataTypes: String = Models + "data-types/"

		private const val ModelVersions: String = "$Models/{modelId}/versions/{modelVerId}/"
		const val ArtifactTemplates: String = ModelVersions + "artifact-templates/"
		const val AttributeTemplates: String = "$ArtifactTemplates/{artifactId}/attribute-templates/"
		const val Stereotypes: String = ModelVersions + "stereotypes/"
		const val RelationTemplates: String = ModelVersions + "relation-templates/"

//		private const val ModelVersions: String = "$Models/versions/"
//		const val ArtifactTemplates: String = "$Models/artifacts/"
//		const val AttributeTemplates: String = "$Models/attributes/"
//		const val Stereotypes: String = "$Models/stereotypes/"
//		const val RelationTemplates: String = ModelVersions + "relation-templates/"

		const val Projects: String = "/api/$Locale/projects/"
		const val ArtifactStores: String = "/api/$Locale/projects/{projectId}/artifact-stores/"
		const val RelationStores: String = "/api/$Locale/projects/{projectId}/relation-stores/"
	}
}
