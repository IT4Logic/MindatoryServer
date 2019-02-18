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
import com.it4logic.mindatory.exceptions.ApplicationValidationException
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
class SecurityGroupService : ApplicationBaseService<SecurityGroup>() {
    @Autowired
    private lateinit var groupRepository: SecurityGroupRepository

    @Autowired
    lateinit var securityUserService: SecurityUserService

    @Autowired
    private lateinit var mlcRepository: SecurityGroupMLCRepository

    @Autowired
    protected lateinit var languageManager: LanguageManager

    override fun repository(): ApplicationBaseRepository<SecurityGroup> = groupRepository

    override fun type(): Class<SecurityGroup> = SecurityGroup::class.java

    override fun multipleLanguageContentRepository() : SecurityGroupMLCRepository = mlcRepository

    override fun multipleLanguageContentType() : KClass<*> = SecurityGroupMultipleLanguageContent::class

    fun getGroupUsers(id: Long): MutableList<SecurityUser> = securityUserService.findAllByGroupId(id)

    override fun beforeCreate(target: SecurityGroup) {
        val result = mlcRepository.findAllByLanguageIdAndFieldNameAndContents(languageManager.currentLanguage.id, "name", target.name)
        if(result.isNotEmpty()) {
            throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.DuplicateSecurityGroupName)
        }
    }

    override fun beforeUpdate(target: SecurityGroup) {
        val result = mlcRepository.findAllByLanguageIdAndFieldNameAndContentsAndParentIdNot(languageManager.currentLanguage.id, "name", target.name, target.id)
        if(result.isNotEmpty()) {
            throw ApplicationDataIntegrityViolationException(ApplicationErrorCodes.DuplicateSecurityGroupName)
        }
    }

    override fun beforeDelete(target: SecurityGroup) {
        if( getGroupUsers(target.id).size > 0)
            throw ApplicationValidationException(ApplicationErrorCodes.ValidationGroupHasUsers)
    }

    fun assignUsersToGroup(id: Long, userIdsList: List<Long>) {
        val group = findById(id)
        for(uid in userIdsList) {
            val user = securityUserService.findById(uid)
            user.group = group
            securityUserService.update(user)
        }
    }
}