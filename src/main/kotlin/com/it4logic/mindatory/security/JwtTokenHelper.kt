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

package com.it4logic.mindatory.security

import com.it4logic.mindatory.exceptions.ApplicationErrorCodes
import com.it4logic.mindatory.exceptions.ApplicationAuthenticationException
import io.jsonwebtoken.*
import org.springframework.stereotype.Component
import io.jsonwebtoken.impl.DefaultClock
import org.springframework.beans.factory.annotation.Value
import java.util.*
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.Claims
import org.slf4j.LoggerFactory

/**
 * Utility class that contains JWT helper methods
 */
@Component
class JwtTokenHelper {

    private val logger = LoggerFactory.getLogger(JwtTokenHelper::class.java)

    @Value("\${app.jwt.key}")
    private val jwtKey: String? = null

    @Value("\${app.jwt.expiration}")
    private val jwtExpiration: Long? = null

    var claims: Claims? = null

    /**
     * Generates a JWT token from the given user name
     *
     * @param username Input user name
     * @return Generated JWT token
     */
    fun generateToken(username: String) : String {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(DefaultClock.INSTANCE.now())
                .setExpiration(Date(DefaultClock.INSTANCE.now().time + jwtExpiration!! ))
                .signWith(SignatureAlgorithm.HS512, jwtKey)
                .compact()
    }

    /**
     * Validates a JWT token, and throws exception in case off token validation errors
     *
     * @param authToken Input JWT token
     * @return True if the token is valid, otherwise False
     */
    fun validateToken(authToken: String): Boolean {
        try {
            claims = Jwts.parser().setSigningKey(jwtKey).parseClaimsJws(authToken).body
            return true
        } catch (ex: SignatureException) {
            throw ApplicationAuthenticationException(ApplicationErrorCodes.SecurityInvalidJwtSignature)
        } catch (ex: MalformedJwtException) {
            throw ApplicationAuthenticationException(ApplicationErrorCodes.SecurityInvalidJwtToken)
        } catch (ex: ExpiredJwtException) {
            throw ApplicationAuthenticationException(ApplicationErrorCodes.SecurityExpiredJwtToken)
        } catch (ex: UnsupportedJwtException) {
            throw ApplicationAuthenticationException(ApplicationErrorCodes.SecurityUnsupportedJwtToken)
        } catch (ex: IllegalArgumentException) {
            throw ApplicationAuthenticationException(ApplicationErrorCodes.SecurityInvalidJwtContents)
        }
    }
}