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

	override fun beforeDelete(target: SecurityRole) {
		// Find all users that have this role and remove role from them
		val users = securityUserService.findAllByRoleId(target.id)
		for (user in users) {
			user.removeRole(target)
			securityUserService.update(user)
		}
	}

	/**
	 * Retrieves users associated with input role Id
	 * @param id Input role Id
	 * @return Users list
	 */
	fun getRoleUsers(id: Long): MutableList<SecurityUser> = securityUserService.findAllByRoleId(id)

	/**
	 * Associates the input user Ids list with the input role
	 * @param id Input role Id
	 * @param userIdsList User Ids list
	 */
	fun addUsersToRole(id: Long, userIdsList: List<Long>) {
		val role = findById(id)
		for (uid in userIdsList) {
			val user = securityUserService.findById(uid)
			user.addRole(role)
			securityUserService.update(user)
		}
	}

	/**
	 * Remove the input role from the input user Ids list
	 * @param id Input role Id
	 * @param userIdsList User Ids list
	 */
	fun removeUsersFromRole(id: Long, userIdsList: List<Long>) {
		val role = findById(id)
		for (uid in userIdsList) {
			val user = securityUserService.findById(uid)
			user.removeRole(role)
			securityUserService.update(user)
		}
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