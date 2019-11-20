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

package com.it4logic.mindatory.tests

import com.it4logic.mindatory.model.model.ModelVersionStatus
import com.it4logic.mindatory.model.project.StoreObjectStatus
import java.util.*

open class ApplicationEntityBaseTest(
	var createdBy: String? = null,
	var createdAt: Date? = null,
	var updatedBy: String? = null,
	var updatedAt: Date? = null,
	var version: Long = 1,
	var id: Long = -1
)

open class ApplicationMLCEntityBaseTest(
//	var companyId: Long = 1
) : ApplicationEntityBaseTest()

open class ApplicationProjectEntityBaseTest(
	var project: ProjectTest? = null
) : ApplicationMLCEntityBaseTest()

data class CompanyTest(
	var name: String,
	var street: String = "",
	var city: String = "",
	var state: String = "",
	var zipCode: String = "",
	var country: String = "",
	var mobile: String = "",
	var phone: String = "",
	var fax: String = ""
) : ApplicationEntityBaseTest()

data class LanguageTest(
	var locale: String,
	var name: String,
	var default: Boolean = false
) : ApplicationEntityBaseTest()

data class ProjectTest(
	var name: String = "",
	var description: String = ""
) : ApplicationMLCEntityBaseTest()

data class ApplicationRepositoryTest(
	var name: String,
	var description: String = "",
	var shared: Boolean = true,
	var project: ProjectTest? = null
) : ApplicationMLCEntityBaseTest()

data class SecurityGroupTest(
	var name: String = "",
	var description: String = ""
) : ApplicationMLCEntityBaseTest()

data class SecurityRoleTest(
	var name: String = "",
	var description: String = "",
	var permissions: ArrayList<String> = ArrayList()
) : ApplicationMLCEntityBaseTest() {
	private fun isPermissionExists(perm: String): Boolean = permissions.contains(perm)
	fun addPermission(perm: String) {
		if (isPermissionExists(perm))
			return
		permissions.add(perm)
	}

	fun removePermission(perm: String) {
		if (!isPermissionExists(perm))
			return
		permissions.remove(perm)
	}
}

data class SecurityUserTest(
	var username: String = "",
	var password: String = "",
	var accountEnabled: Boolean = true,
	var accountLocked: Boolean = false,
	var accountExpired: Boolean = false,
	var passwordExpired: Boolean = false,
	var passwordNeverExpires: Boolean = true,
	var passwordChangeAtNextLogin: Boolean = false,
	var fullName: String = "",
	var email: String = "",
	var mobile: String = "",
	var notes: String = "",
	var group: SecurityGroupTest? = null,
	var roles: MutableList<SecurityRoleTest> = mutableListOf()
) : ApplicationMLCEntityBaseTest() {
	private fun isRoleExists(role: SecurityRoleTest): Boolean {
		val result = roles.filter { it.id == role.id }
		if (result.isEmpty())
			return false
		return true
	}

	fun addRole(role: SecurityRoleTest) {
		if (isRoleExists(role))
			return
		roles.add(role)
	}

	fun removeRole(role: SecurityRoleTest) {
		for (r in roles) {
			if (r.id == role.id) {
				roles.remove(r)
				break
			}
		}
	}
}

data class AttributeTemplateTest(
	var identifier: String,
	var name: String,
	var description: String = "",
	var repository: ApplicationRepositoryTest,
	var project: ProjectTest? = null
) : ApplicationMLCEntityBaseTest()

data class AttributeTemplateVersionTest(
	var attributeTemplate: AttributeTemplateTest,
	var typeUUID: String,
	var properties: HashMap<String, Any> = hashMapOf(),
	var modelVersionStatus: ModelVersionStatus = ModelVersionStatus.InDesign,
	var designVersion: Int = 1
) : ApplicationMLCEntityBaseTest()

data class ArtifactTemplateTest(
	var identifier: String,
	var name: String,
	var description: String = "",
	var repository: ApplicationRepositoryTest,
	var project: ProjectTest? = null
) : ApplicationMLCEntityBaseTest()

data class ArtifactTemplateVersionTest(
	var artifactTemplate: ArtifactTemplateTest,
	var attributes: MutableList<AttributeTemplateVersionTest> = mutableListOf(),
	var modelVersionStatus: ModelVersionStatus = ModelVersionStatus.InDesign,
	var designVersion: Int = 1
) : ApplicationMLCEntityBaseTest()

data class StereotypeTest(
	var name: String,
	var description: String = "",
	var repository: ApplicationRepositoryTest? = null,
	var project: ProjectTest? = null
) : ApplicationMLCEntityBaseTest()

data class RelationTemplateTest(
	var identifier: String,
	var description: String = "",
	var repository: ApplicationRepositoryTest,
	var project: ProjectTest? = null
) : ApplicationMLCEntityBaseTest()

data class RelationTemplateVersionTest(
	var relationTemplate: RelationTemplateTest,
	var sourceStereotype: StereotypeTest,
	var sourceArtifacts: MutableList<ArtifactTemplateVersionTest> = mutableListOf(),
	var targetStereotype: StereotypeTest,
	var targetArtifacts: MutableList<ArtifactTemplateVersionTest> = mutableListOf(),
	var modelVersionStatus: ModelVersionStatus = ModelVersionStatus.InDesign,
	var designVersion: Int = 1
) : ApplicationMLCEntityBaseTest()

data class AttributeStoreTest(
	var contents: String,
//	var attributeTemplate: AttributeTemplateTest,
	var attributeTemplateVersion: AttributeTemplateVersionTest,
	var storeStatus: StoreObjectStatus = StoreObjectStatus.Active//,
//	var project: ProjectTest

) : ApplicationMLCEntityBaseTest()

data class ArtifactStoreTest(
//	var artifact: ArtifactTemplateTest,
	var artifactTemplateVersion: ArtifactTemplateVersionTest,
	var attributeStores: MutableList<AttributeStoreTest> = mutableListOf(),
	var storeStatus: StoreObjectStatus = StoreObjectStatus.Active,
	var project: ProjectTest

) : ApplicationMLCEntityBaseTest()

data class RelationStoreTest(
	var sourceArtifacts: MutableList<ArtifactStoreTest> = mutableListOf(),
	var targetArtifacts: MutableList<ArtifactStoreTest> = mutableListOf(),
	var relationTemplateVersion: RelationTemplateVersionTest,
	var storeStatus: StoreObjectStatus = StoreObjectStatus.Active,
	var project: ProjectTest

) : ApplicationMLCEntityBaseTest()