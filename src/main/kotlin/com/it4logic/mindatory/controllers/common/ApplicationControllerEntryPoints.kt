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
        const val COMPANY: String = "/api/company"
        const val REPOSITORIES: String = "/api/repositories"
        const val SOLUTIONS: String = "/api/solutions"
        const val ARTIFACT_TEMPLATES: String = "/api/repository/artifact-templates"
        const val ATTRIBUTE_TEMPLATES: String = "/api/repository/attribute-templates"
        const val ATTRIBUTE_TEMPLATE_DATA_TYPES: String = "$ATTRIBUTE_TEMPLATES/data-types"
        const val JOIN_TEMPLATES: String = "/api/repository/join-templates"
        const val STEREOTYPES: String = "/api/repository/stereotypes"
        const val ARTIFACT_STORES: String = "/api/store/artifacts"
        const val ATTRIBUTE_STORES: String = "/api/store/attributes"
        const val JOIN_STORES: String = "/api/store/joins"

        const val Authentication: String = "/auth/"
        const val Companies: String = "/api/companies"
        const val SecurityRoles: String = "/api/security/roles/"
        const val SecurityGroups: String = "/api/security/groups/"
        const val SecurityUsers: String = "/api/security/users/"
        const val SecurityUserProfile: String = "/api/security/profile/"
    }
}
