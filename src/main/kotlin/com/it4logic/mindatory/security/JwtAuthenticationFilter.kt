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

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.*
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component

@Component
class JwtAuthenticationFilter : OncePerRequestFilter() {
    @Autowired
    private lateinit var userDetailsService: SecurityUserDetailsService

    @Autowired
    private lateinit var tokenHelper: JwtTokenHelper

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        try {
            val jwt = getJwtFromRequest(request)

            if (StringUtils.hasText(jwt) && tokenHelper.validateToken(jwt)) {

                val userDetails = userDetailsService.loadUserByUsername(tokenHelper.claims?.subject!!)

                when {
                    !userDetails.isEnabled -> logger.error("User ${userDetails.username} is disabled")
                    !userDetails.isAccountNonExpired -> logger.error("User ${userDetails.username}' account is expired")
                    !userDetails.isAccountNonLocked -> logger.error("User ${userDetails.username}' account is locked")
                    !userDetails.isCredentialsNonExpired -> logger.error("User ${userDetails.username}' password is expired")
                    else -> {
                        val authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
                        authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                        SecurityContextHolder.getContext().authentication = authentication
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Authentication Exception", e)
        }

        filterChain.doFilter(request, response)
    }

    /**
     * Retrieves the JWT Token from request header
     * @param request Request object
     * @return JWT Token in string format, otherwise null
     */
    private fun getJwtFromRequest(request: HttpServletRequest): String {
        val bearerToken = request.getHeader("Authorization")
        return if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7, bearerToken.length)
        } else ""
    }
}