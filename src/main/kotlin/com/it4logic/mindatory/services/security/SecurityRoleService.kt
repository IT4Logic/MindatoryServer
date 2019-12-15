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

package com.it4logic.mindatory.services.security

import com.it4logic.mindatory.exceptions.*
import com.it4logic.mindatory.mlc.LanguageManager
import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.model.security.*
import com.it4logic.mindatory.security.ApplicationSecurityPermissions
import com.it4logic.mindatory.security.SecurityPermissionsHelper
import com.it4logic.mindatory.services.common.ApplicationBaseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.transaction.Transactional
import kotlin.reflect.KClass

/**
 * Security Role Data Service
 */
@Service
@Transactional
class SecurityRoleService : ApplicationBaseService<SecurityRole>() {
	@Autowired
	private lateinit var roleRepository: SecurityRoleRepository

	@Autowired
	private lateinit var securityUserService: SecurityUserService

	@Autowired
	private lateinit var mlcRepository: SecurityRoleMLCRepository

	@Autowired
	protected lateinit var languageManager: LanguageManager

	override fun repository(): ApplicationBaseRepository<SecurityRole> = roleRepository

	override fun type(): Class<SecurityRole> = SecurityRole::class.java

	override fun multipleLanguageContentRepository(): SecurityRoleMLCRepository = mlcRepository

	override fun multipleLanguageContentType(): KClass<*> = SecurityRoleMultipleLanguageContent::class

	override fun beforeCreate(target: SecurityRole) {
		// validates if there are any duplicates, as this property should be unique and MLC in the same time
		val result = target.findAllByLanguageIdAndFieldName(languageManager.currentLanguage.id, "name")
		val obj = result.find { it.contents == target.name }
		if (obj != null) {
			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.DuplicateSecurityRoleName)
		}
		validatePermissions(target)
	}

	override fun beforeUpdate(target: SecurityRole) {
		// validates if there are any duplicates, as this property should be unique and MLC in the same time
		val result = mlcRepository.findAllByLanguageIdAndFieldNameAndParentIdNot(
			languageManager.currentLanguage.id,
			"name",
			target.id
		)
		val obj = result.find { it.contents == target.name }
		if (obj != null) {
			throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.DuplicateSecurityRoleName)
		}

		validatePermissions(target)
	}

	/**
	 * Validates that the input role contains only the predefined permissions
	 * @param target Input role object
	 */
	fun validatePermissions(target: SecurityRole) {
		for (perm in target.permissions) {
			if (ApplicationSecurityPermissions.Permissions.indexOf(perm) < 0)
				throw ApplicationObjectNotFoundException(perm, ApplicationErrorCodes.NotFoundPermission)
		}

		SecurityPermissionsHelper.verifyViewPermission(target.permissions)
	}
}