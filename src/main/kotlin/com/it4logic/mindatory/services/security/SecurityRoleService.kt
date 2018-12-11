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

import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.model.security.SecurityRole
import com.it4logic.mindatory.model.security.SecurityRoleRepository
import com.it4logic.mindatory.model.security.SecurityUser
import com.it4logic.mindatory.services.common.ApplicationBaseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.transaction.Transactional


@Service
@Transactional
class SecurityRoleService : ApplicationBaseService<SecurityRole>() {
    @Autowired
    private lateinit var roleRepository: SecurityRoleRepository

    @Autowired
    private lateinit var securityUserService: SecurityUserService

    override fun repository(): ApplicationBaseRepository<SecurityRole> = roleRepository

    override fun type(): Class<SecurityRole> = SecurityRole::class.java

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

    override fun beforeDelete(target: SecurityRole) {
        val users = securityUserService.findAllByRoleId(target.id)
        for(user in users) {
            user.removeRole(target)
            securityUserService.update(user)
        }
    }
}