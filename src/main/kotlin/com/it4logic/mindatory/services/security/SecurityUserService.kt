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

import com.it4logic.mindatory.exceptions.ApplicationErrorCodes
import com.it4logic.mindatory.exceptions.ApplicationObjectNotFoundException
import com.it4logic.mindatory.exceptions.ApplicationValidationException
import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.model.security.SecurityUser
import com.it4logic.mindatory.model.security.SecurityUserRepository
import com.it4logic.mindatory.security.ChangePasswordRequest
import com.it4logic.mindatory.security.SecurityFactory
import com.it4logic.mindatory.services.common.ApplicationBaseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.security.crypto.password.PasswordEncoder
import javax.transaction.Transactional


@Service
@Transactional
class SecurityUserService : ApplicationBaseService<SecurityUser>() {
    @Autowired
    private lateinit var userRepository: SecurityUserRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    override fun repository(): ApplicationBaseRepository<SecurityUser> = userRepository

    override fun type(): Class<SecurityUser> = SecurityUser::class.java

    override fun beforeCreate(target: SecurityUser) {
        target.password = passwordEncoder.encode(target.password)
    }

    /**
     * Updates SecurityUser password
     *
     * @param id SecurityUser Id
     * @param changePasswordRequest ChangePasswordRequest object
     */
    fun changeUserPassword(id: Long, changePasswordRequest: ChangePasswordRequest) {
        val user = findById(id)
        changeUserPassword(user, changePasswordRequest)
    }

    /**
     * Updates SecurityUser password
     *
     * @param target SecurityUser object
     * @param changePasswordRequest ChangePasswordRequest object
     */
    fun changeUserPassword(target: SecurityUser, changePasswordRequest: ChangePasswordRequest) {
        if(changePasswordRequest.newPassword.compareTo(changePasswordRequest.confirmPassword) != 0)
            throw ApplicationValidationException(ApplicationErrorCodes.ValidationPasswordsNotMatched)

        // check the current user password
        if(!passwordEncoder.matches(changePasswordRequest.currentPassword, target.password))
            throw ApplicationValidationException(ApplicationErrorCodes.ValidationIncorrectUserPassword)

        target.password = passwordEncoder.encode(changePasswordRequest.newPassword)

        update(target)
    }

    /**
     * Searches and loads user for the input username
     *
     * @param username SecurityUser username
     * @return SecurityUser object, or ApplicationObjectNotFoundException in case if the user username doesn't exist
     */
    fun findByUsername(username: String) : SecurityUser {
        return userRepository.findByUsername(username).orElseThrow { ApplicationObjectNotFoundException(username, SecurityUser::class.java.simpleName.toLowerCase()) }
    }

    /**
     * Searches and loads users for the input group id
     *
     * @param id SecurityGroup Id
     * @return SecurityUser objects list
     */
    fun findAllByGroupId(id: Long) : MutableList<SecurityUser> {
        return userRepository.findAllByGroupId(id)
    }

    /**
     * Searches and loads user for the input role id
     *
     * @param id SecurityRole Id
     * @return SecurityUser objects list
     */
    fun findAllByRoleId(id: Long) : MutableList<SecurityUser> {
        return userRepository.findAllByRolesId(id)
    }

    /**
     * Cet the current user object according to the current signed-in user
     *
     * @return Current [SecurityUser] object or [ApplicationObjectNotFoundException] will be thrown
     */
    fun getCurrentSecurityUser(): SecurityUser {
        val username = if(SecurityFactory.getCurrentUsername().isPresent) SecurityFactory.getCurrentUsername().get() else ""
        return userRepository.findByUsername(username).orElseThrow { ApplicationObjectNotFoundException(username, SecurityUser::class.java.simpleName.toLowerCase()) }
    }

    /**
     * Update current user information
     *
     * @param target SecurityUser object
     * @return Updated SecurityUser object
     */
    fun updateCurrentSecurityUser(target: SecurityUser): SecurityUser {
        val currentUser = getCurrentSecurityUser()
        if(target.id != currentUser.id && target.username != currentUser.username)
            throw throw ApplicationValidationException(ApplicationErrorCodes.ValidationChangeAnotherUserProfileNotAllowed)
        return update(target)
    }

    /**
     * Updates current SecurityUser password
     *
     * @param changePasswordRequest ChangePasswordRequest object
     */
    fun changeCurrentUserPassword(changePasswordRequest: ChangePasswordRequest) {
        val currentUser = getCurrentSecurityUser()
        changeUserPassword(currentUser, changePasswordRequest)
    }
}