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

@RepositoryRestResource(exported = false)
interface SecurityUserTokenRepository : ApplicationBaseRepository<SecurityUserToken> {
	fun findOneByUserId(id: Long): Optional<SecurityUserToken>
	fun findOneByToken(token: String): Optional<SecurityUserToken>
}