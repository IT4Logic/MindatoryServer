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

package com.it4logic.mindatory.services.security

import com.it4logic.mindatory.exceptions.ApplicationDataIntegrityViolationException
import com.it4logic.mindatory.exceptions.ApplicationErrorCodes
import com.it4logic.mindatory.mlc.LanguageManager
import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.model.security.*
import com.it4logic.mindatory.services.common.ApplicationBaseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.transaction.Transactional
import kotlin.reflect.KClass


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

    override fun multipleLanguageContentRepository() : SecurityRoleMLCRepository = mlcRepository

    override fun multipleLanguageContentType() : KClass<*> = SecurityRoleMultipleLanguageContent::class

    override fun beforeCreate(target: SecurityRole) {
        val result = mlcRepository.findAllByLanguageIdAndFieldNameAndContents(languageManager.currentLanguage.id, "name", target.name)
        if(result.isNotEmpty()) {
            throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.DuplicateSecurityRoleName)
        }
    }

    override fun beforeUpdate(target: SecurityRole) {
        val result = mlcRepository.findAllByLanguageIdAndFieldNameAndContentsAndParentIdNot(languageManager.currentLanguage.id, "name", target.name, target.id)
        if(result.isNotEmpty()) {
            throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.DuplicateSecurityRoleName)
        }
    }

    override fun beforeDelete(target: SecurityRole) {
        val users = securityUserService.findAllByRoleId(target.id)
        for(user in users) {
            user.removeRole(target)
            securityUserService.update(user)
        }
    }

    fun getRoleUsers(id: Long): MutableList<SecurityUser> = securityUserService.findAllByRoleId(id)

    fun addUsersToRole(id: Long, userIdsList: List<Long>) {
        val role = findById(id)
        for(uid in userIdsList) {
            val user = securityUserService.findById(uid)
            user.addRole(role)
            securityUserService.update(user)
        }
    }

    fun removeUsersFromRole(id: Long, userIdsList: List<Long>) {
        val role = findById(id)
        for(uid in userIdsList) {
            val user = securityUserService.findById(uid)
            user.removeRole(role)
            securityUserService.update(user)
        }
    }
}