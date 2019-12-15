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

import com.fasterxml.jackson.annotation.JsonIgnore
import com.it4logic.mindatory.mlc.MultipleLanguageContent
import com.it4logic.mindatory.model.common.*
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntity
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntityRepository
import io.leangen.graphql.annotations.GraphQLIgnore
import org.hibernate.annotations.LazyCollection
import org.hibernate.annotations.LazyCollectionOption
import org.hibernate.envers.Audited
import org.hibernate.envers.NotAudited
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.Query
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.*
import javax.validation.constraints.*
import javax.persistence.*

/**
 * Security User entity
 */
@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(
	name = "t_security_users", uniqueConstraints = [
		UniqueConstraint(
			name = ApplicationConstraintCodes.SecurityUserUsernameUniqueIndex,
			columnNames = ["f_username"]
		)
	]
)
data class SecurityUser(
	@get: NotBlank
	@get: Size(min = 4, max = 50)
	@Column(name = "f_username", nullable = false, length = 50)
	var username: String = "",

	@get: NotBlank
	@get: Size(min = 6)
	@Column(name = "f_password", nullable = false)
	var password: String = "",

	@Column(name = "f_account_enabled")
	var accountEnabled: Boolean = true,

	@Column(name = "f_account_locked")
	var accountLocked: Boolean = false,

	@Column(name = "f_account_expired")
	var accountExpired: Boolean = false,

	@Column(name = "f_password_expired")
	var passwordExpired: Boolean = false,

	@Column(name = "f_password_never_expires")
	var passwordNeverExpires: Boolean = true,

	@Column(name = "f_change_pwd_next_login")
	var passwordChangeAtNextLogin: Boolean = false,

	@get: NotBlank
	@get: Size(min = 4, max = 100)
	@get: MultipleLanguageContent
	@Transient
	var fullName: String = "",

	@get: NotBlank
	@get: Email
	@get: Size(max = 100)
	@Column(name = "f_email", nullable = false, length = 100)
	var email: String = "",

	@get: Size(max = 20)
	@Column(name = "f_mobile", length = 20)
	var mobile: String = "",

	@get: Size(max = 255)
	@get: MultipleLanguageContent
	@Transient
	var notes: String = "",

	@get: NotNull
	@get: MultipleLanguageContent
	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "f_group_id", nullable = false)
	var group: SecurityGroup? = null,

	@OneToOne(cascade = [CascadeType.ALL])
	@JoinColumn(name = "f_preference_id", referencedColumnName = "f_id")
	var preferences: SecurityUserPreferences? = null,


	@get: MultipleLanguageContent
	@ManyToMany(cascade = [CascadeType.ALL])
	@JoinTable(
		name = "t_m2m_security_users_roles",
		joinColumns = [JoinColumn(name = "f_user_id")],
		inverseJoinColumns = [JoinColumn(name = "f_role_id")]
	)
	@LazyCollection(LazyCollectionOption.FALSE)
	var roles: MutableList<SecurityRole> = mutableListOf(),

	@NotAudited
	@OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, mappedBy = "parent")
	@LazyCollection(LazyCollectionOption.FALSE)
	@JsonIgnore
	@get: GraphQLIgnore
	var mlcs: MutableList<SecurityUserMultipleLanguageContent> = mutableListOf()

) : ApplicationEntityBase() {
	@Suppress("SENSELESS_COMPARISON", "UNCHECKED_CAST")
	override fun obtainMLCs(): MutableList<MultipleLanguageContentBaseEntity> {
		if (mlcs == null)
			mlcs = mutableListOf()
		return mlcs as MutableList<MultipleLanguageContentBaseEntity>
	}

	private fun isRoleExists(role: SecurityRole): Boolean {
		val result = roles.filter { it.id == role.id }
		if (result.isEmpty())
			return false
		return true
	}

	fun addRole(role: SecurityRole) {
		if (isRoleExists(role))
			return
		roles.add(role)
	}

	fun removeRole(role: SecurityRole) {
		for (r in roles) {
			if (r.id == role.id) {
				roles.remove(r)
				break
			}
		}
	}
}

/**
 * JPA Repository
 */
@RepositoryRestResource(exported = false)
interface SecurityUserRepository : ApplicationBaseRepository<SecurityUser> {
	fun findAllByGroupId(id: Long): MutableList<SecurityUser>
	fun findAllByRolesId(id: Long): MutableList<SecurityUser>
	fun findByUsername(username: String): Optional<SecurityUser>

	@Query("select u from SecurityUser u where u.username=?1 or u.email=?1")
	fun findByUsernameOrEmail(usernameOrPassword: String): Optional<SecurityUser>
}

/**
 * Multiple Language Content entity
 */
@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(
	name = "t_security_user_mlcs", uniqueConstraints = [
		(UniqueConstraint(
			name = ApplicationConstraintCodes.SecurityUserMCLUniqueIndex,
			columnNames = ["f_parent", "f_language_id", "f_field_name"]
		))
	]
)
class SecurityUserMultipleLanguageContent(
	@get: NotNull
	@ManyToOne
	@JoinColumn(name = "f_parent", nullable = false)
	var parent: SecurityUser? = null
) : MultipleLanguageContentBaseEntity() {
	override fun updatedParent(obj: ApplicationEntityBase?) { parent = obj as SecurityUser? }
	override fun obtainParent(): ApplicationEntityBase? = parent
}

/**
 * Multiple Language Content Repository
 */
@RepositoryRestResource(exported = false)
interface SecurityUserMLCRepository : MultipleLanguageContentBaseEntityRepository<SecurityUserMultipleLanguageContent>