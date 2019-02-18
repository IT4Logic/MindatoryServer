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

package com.it4logic.mindatory.controllers.common

/**
 * Holder for Application Controller Entry Points
 */
interface ApplicationControllerEntryPoints {
    companion object {
        const val Authentication: String = "/auth/"

        const val Languages : String = "/api/mlc/"

        const val Company: String = "/api/company/"

        const val SecurityRoles: String = "/api/security/roles/"
        const val SecurityGroups: String = "/api/security/groups/"
        const val SecurityUsers: String = "/api/security/users/"
        const val SecurityUserProfile: String = "/api/security/profile/"

        const val Solutions: String = "/api/solutions/"

        const val Repositories: String = "/api/repositories/"
        const val Stereotypes: String = "/api/repository/stereotypes/"
        const val AttributeTemplates: String = "/api/repository/attribute-templates/"
        const val AttributeTemplateDataTypes: String = "$AttributeTemplates/data-types/"
        const val ArtifactTemplates: String = "/api/repository/artifact-templates/"
        const val JoinTemplates: String = "/api/repository/join-templates/"

        const val AttributeStores: String = "/api/store/attributes/"
        const val ArtifactStores: String = "/api/store/artifacts/"
        const val JoinStores: String = "/api/store/joins/"
    }
}
