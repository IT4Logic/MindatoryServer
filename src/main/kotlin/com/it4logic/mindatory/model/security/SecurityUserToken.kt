/*
    Copyright (c) 2018, IT4Logic.

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
package com.it4logic.mindatory.model.security

import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.model.common.ApplicationMLCEntityBase
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntity
import org.hibernate.envers.Audited
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.swing.text.html.Option
import javax.validation.constraints.Max
import javax.validation.constraints.NotNull

/**
 * Security User Token entity
 */
@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "t_security_user_tokens")
data class SecurityUserToken(
	@get: NotNull
	@Column(name = "f_token", nullable = false, length = 255)
	var token: String,

	@get: NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_user_id", nullable = false)
	val user: SecurityUser,

	@get: NotNull
	@Column(name = "f_token_date")
	val tokenDate: LocalDateTime = LocalDateTime.now(),

	@get: NotNull
	@Column(name = "f_expire_date")
	val expireDate: LocalDateTime

) : ApplicationMLCEntityBase() {
	override fun obtainMLCs(): MutableList<MultipleLanguageContentBaseEntity> = mutableListOf()
}

/**
 * JPA Repository
 */
@RepositoryRestResource(exported = false)
interface SecurityUserTokenRepository : ApplicationBaseRepository<SecurityUserToken> {
	fun findOneByUserId(id: Long): Optional<SecurityUserToken>
	fun findOneByToken(token: String): Optional<SecurityUserToken>
}