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

package com.it4logic.mindatory.controllers.common

/**
 * Holder for Application Controller Entry Points
 */
interface ApplicationControllerEntryPoints {
    companion object {
        const val Locale: String = "{locale}"

        const val Security: String = "/security/$Locale/"

        const val Authentication: String = "/auth/"

        const val Actuator: String = "/actuator/"

        const val Languages : String = "/api/languages/$Locale/"

        const val Company: String = "/api/company/$Locale/"

        const val Preferences: String = "/api/preferences/$Locale/"

        const val MailTemplates: String = "/api/mail-templates/$Locale/"

        const val SecurityRoles: String = "/api/security/roles/$Locale/"
        const val SecurityGroups: String = "/api/security/groups/$Locale/"
        const val SecurityUsers: String = "/api/security/users/$Locale/"
        const val SecurityUserProfile: String = "/api/security/profile/$Locale/"

        const val Solutions: String = "/api/solutions/$Locale/"

        const val Repositories: String = "/api/repositories/$Locale/"
        const val Stereotypes: String = "/api/repository/stereotypes/$Locale/"
        const val AttributeTemplates: String = "/api/repository/attribute-templates/$Locale/"
        const val AttributeTemplateDataTypes: String = "$AttributeTemplates/data-types/"
        const val ArtifactTemplates: String = "/api/repository/artifact-templates/$Locale/"
        const val JoinTemplates: String = "/api/repository/join-templates/$Locale/"

        const val ArtifactStores: String = "/api/store/artifacts/$Locale/"
        const val JoinStores: String = "/api/store/joins/$Locale/"
    }
}
