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

package com.it4logic.mindatory.controllers

import com.it4logic.mindatory.exceptions.*
import com.it4logic.mindatory.security.JwtAuthenticationResponse
import com.it4logic.mindatory.security.JwtTokenHelper
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.ResponseEntity
import com.it4logic.mindatory.security.LoginRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestBody
import javax.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.security.authentication.*

/**
 * Entry point controller responsible for authentication
 */
@CrossOrigin
@RestController
@RequestMapping(ApplicationControllerEntryPoints.Authentication)
class AuthenticationController {

    @Autowired
    lateinit var authenticationManager: AuthenticationManager

    @Autowired
    lateinit var jwtTokenHelper: JwtTokenHelper

    @PostMapping("/login")
    fun authenticateUser(@Valid @RequestBody loginRequest: LoginRequest): ResponseEntity<Any> {
        try {
            val authentication = authenticationManager.authenticate( UsernamePasswordAuthenticationToken( loginRequest.username, loginRequest.password ) )
            val jwtToken = jwtTokenHelper.generateToken(authentication)
            return ResponseEntity.ok<Any>(JwtAuthenticationResponse(jwtToken))
        } catch (e: Exception) {
            when(ExceptionHelper.getRootCause(e)) {
                is ApplicationObjectNotFoundException -> throw ApplicationAuthenticationException(ApplicationErrorCodes.SecurityInvalidUsernameOrPassword)
                is DisabledException -> throw ApplicationAuthenticationException(ApplicationErrorCodes.SecurityAccountDisabled)
                is BadCredentialsException -> throw ApplicationAuthenticationException(ApplicationErrorCodes.SecurityInvalidUsernameOrPassword)
                is CredentialsExpiredException -> throw ApplicationAuthenticationException(ApplicationErrorCodes.SecurityCredentialsExpired)
                is AccountExpiredException -> throw ApplicationAuthenticationException(ApplicationErrorCodes.SecurityAccountExpired)
                is LockedException -> throw ApplicationAuthenticationException(ApplicationErrorCodes.SecurityAccountLocked)
            }
            throw ApplicationAuthenticationException(ApplicationErrorCodes.AuthenticationError, e)
        }
    }
}
