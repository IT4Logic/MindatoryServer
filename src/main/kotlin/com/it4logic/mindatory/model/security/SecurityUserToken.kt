package com.it4logic.mindatory.model.security

import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.model.common.ApplicationMLCEntityBase
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntity
import org.hibernate.envers.Audited
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.*
import javax.persistence.*
import javax.swing.text.html.Option
import javax.validation.constraints.Max
import javax.validation.constraints.NotNull

@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "t_sec_user_tokens")
data class SecurityUserToken (
	@get: NotNull
	@Column(nullable = false, length = 255)
	var token: String,

	@get: NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	val user: SecurityUser,

	@get: NotNull
	val tokenDate: Date = Date(),

	@get: NotNull
	val expireDate: Date

) : ApplicationMLCEntityBase() {
	override fun obtainMLCs(): MutableList<MultipleLanguageContentBaseEntity> = mutableListOf()
}

@RepositoryRestResource(exported = false)
interface SecurityUserTokenRepository : ApplicationBaseRepository<SecurityUserToken> {
	fun findOneByUserId(id: Long): Optional<SecurityUserToken>
	fun findOneByToken(token: String): Optional<SecurityUserToken>
}