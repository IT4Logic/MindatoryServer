/*
    Copyright (c) 2019, IT4Logic.

    This file is part of Mindatory Project by IT4Logic.

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

import com.fasterxml.jackson.databind.ObjectMapper
import com.it4logic.mindatory.exceptions.ApplicationErrorCodes
import com.it4logic.mindatory.exceptions.ApplicationObjectNotFoundException
import com.it4logic.mindatory.exceptions.ApplicationValidationException
import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.model.security.SecurityUser
import com.it4logic.mindatory.model.security.SecurityUserToken
import com.it4logic.mindatory.model.security.SecurityUserTokenRepository
import com.it4logic.mindatory.services.common.ApplicationBaseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.encrypt.TextEncryptor
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*


@Service
class SecurityUserTokenService : ApplicationBaseService<SecurityUserToken>() {
	class TokenObject(var userId: Long, var uuid: String)

	@Autowired
	private lateinit var userTokenRepository: SecurityUserTokenRepository
	@Autowired
	private lateinit var textEncryptor: TextEncryptor

	override fun repository(): ApplicationBaseRepository<SecurityUserToken> = userTokenRepository

	override fun type(): Class<SecurityUserToken> = SecurityUserToken::class.java

	private fun createToke(user: SecurityUser): SecurityUserToken {
		val tokenObject = TokenObject(user.id, UUID.randomUUID().toString())
		val tokenDate = LocalDateTime.now()
		tokenDate.plusDays(1)
		val token = SecurityUserToken(
			textEncryptor.encrypt(ObjectMapper().writeValueAsString(tokenObject)),
			user,
			tokenDate,
			tokenDate.plusDays(1)
		)
		return create(token)
	}

	fun createUserTokenForUser(user: SecurityUser): SecurityUserToken {
		val result = userTokenRepository.findOneByUserId(user.id)
		if (result.isPresent)
			userTokenRepository.delete(result.get())
		return createToke(user)
	}

	fun findToken(token: String): SecurityUserToken {
		return userTokenRepository.findOneByToken(token)
			.orElseThrow { ApplicationObjectNotFoundException(token, type().simpleName.toLowerCase()) }
	}

	fun validateToken(secToken: SecurityUserToken) {
		val token = textEncryptor.decrypt(secToken.token)

		val tokenObject = ObjectMapper().readValue<TokenObject>(token, TokenObject::class.java)

		if (tokenObject.userId != secToken.user.id)
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationTokenUserIsNotMatching)

		if (secToken.expireDate > LocalDateTime.now())
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationTokenIsExpired)
	}
}