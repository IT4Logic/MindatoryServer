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

package com.it4logic.mindatory.security

import com.it4logic.mindatory.model.security.SecurityRole
import com.it4logic.mindatory.model.security.SecurityUser
import org.springframework.context.annotation.Bean
import org.springframework.security.access.expression.SecurityExpressionOperations
import org.springframework.security.authentication.AuthenticationTrustResolverImpl
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.FilterInvocation
import org.springframework.security.web.access.expression.WebSecurityExpressionRoot
import org.springframework.stereotype.Component
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Security Helper to generate security specific objects
 */
@Component
class SecurityFactory {

    companion object {
        /**
         * Creates a [SecurityUser] object from [UserDetails] object
         * @param user SecurityUsers object
         * @return UserDetails object
         */
        fun createUserDetails(user: SecurityUser) : UserDetails = User( user.username, user.password, user.accountEnabled,
                                                                        !user.accountExpired, !user.passwordExpired,
                                                                        !user.accountLocked, createAuthorities(user.roles))

        /**
         * Creates an authorities string list from SecurityRoles list
         * @param roles SecurityRoles list
         * @return Authorities string list
         */
        private fun createAuthorities(roles: Collection<SecurityRole>) : Collection<GrantedAuthority> {
            return roles.map { role ->
                role.permissions.map {
                    GrantedAuthority { it }
                }
            }.flatten()
        }

        /**
         * Get the current logged user
         *
         * @return Current logged username
         */
        fun getCurrentUsername(): Optional<String> {
            val authentication = SecurityContextHolder.getContext().authentication
            if(authentication == null || !authentication.isAuthenticated)
                return Optional.empty()

            val principal = SecurityContextHolder.getContext().authentication.principal

            return if (principal is UserDetails) { Optional.of(principal.username) } else { Optional.of(principal.toString()) }
        }

        fun createSecurityExpressionRoot(request: HttpServletRequest, response: HttpServletResponse): SecurityExpressionOperations {
            val filterInvocation = FilterInvocation(request, response, FilterChain { _, _ -> throw UnsupportedOperationException() })
            val sec = WebSecurityExpressionRoot(SecurityContextHolder.getContext().authentication, filterInvocation);
            sec.setTrustResolver(AuthenticationTrustResolverImpl())
            return sec
        }

        fun getCurrentAuthentication(): Authentication {
            return SecurityContextHolder.getContext().authentication
        }
    }

    /**
     * This a bean producer for password encoder
     */
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }



}