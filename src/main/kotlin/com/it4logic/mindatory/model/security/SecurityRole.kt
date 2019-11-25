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
import com.it4logic.mindatory.helpers.ZipManager
import com.it4logic.mindatory.mlc.MultipleLanguageContent
import com.it4logic.mindatory.model.common.*
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntity
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntityRepository
import io.leangen.graphql.annotations.GraphQLIgnore
import org.hibernate.envers.Audited
import org.hibernate.envers.NotAudited
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import javax.persistence.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size
import kotlin.collections.ArrayList

/**
 * Security Role entity
 */
@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "t_security_roles", uniqueConstraints = [])
data class SecurityRole(

	@get: NotNull
	@get: Size(min = 2, max = 50)
	@get: MultipleLanguageContent
	@Transient
	var name: String = "",

	@get: Size(max = 255)
	@get: MultipleLanguageContent
	@Transient
	var description: String = "",

	@Column(name = "f_permissions", insertable = false, updatable = false, length = 1)
	var permissions: ArrayList<String> = ArrayList(),

	@Lob
	@JsonIgnore
	@Column(name = "f_authorities")
	@get: GraphQLIgnore
	var authorities: ByteArray? = null,

	@NotAudited
	@OneToMany(fetch = FetchType.EAGER)
	@JoinColumn(name = "f_parent", referencedColumnName = "f_id")
	@JsonIgnore
	@get: GraphQLIgnore
	var mlcs: MutableList<SecurityRoleMultipleLanguageContent> = mutableListOf()

) : ApplicationMLCEntityBase() {
	@Suppress("SENSELESS_COMPARISON", "UNCHECKED_CAST")
	override fun obtainMLCs(): MutableList<MultipleLanguageContentBaseEntity> {
		if (mlcs == null)
			mlcs = mutableListOf()
		return mlcs as MutableList<MultipleLanguageContentBaseEntity>
	}

	/**
	 * Converts permissions from encoded format into string array
	 */
	@PrePersist
	@PreUpdate
	fun prePersistOrUpdate() {
		var perms = ""
		permissions.forEach { perms += "$it;" }
		perms = perms.removeSuffix(";")
		authorities = ZipManager.compress(perms)
	}

	/**
	 * Converts permissions from string array to encoded format
	 */
	@PostLoad
	fun postLoad() {
		permissions = ArrayList()
		if (authorities == null)
			return
		val perms = ZipManager.decompress(authorities)
		if (!perms.isBlank())
			permissions.addAll(perms.split(";"))
	}

	// implementing equals method to avoid the byte array variable
	override fun equals(other: Any?): Boolean {
		if (this == other) return true
		if (javaClass != other?.javaClass) return false

		other as SecurityRole

		if (name != other.name) return false
		if (description != other.description) return false
		if (permissions != other.permissions) return false

		return true
	}

	// implementing hashCode method to avoid the byte array variable
	override fun hashCode(): Int {
		var result = name.hashCode()
		result = 31 * result + description.hashCode()
		result = 31 * result + permissions.hashCode()
		return result
	}

	private fun isPermissionExists(perm: String): Boolean = permissions.contains(perm)

	/**
	 * Adds permission to user object
	 * @param perm Permission to be added
	 */
	fun addPermission(perm: String) {
		if (isPermissionExists(perm))
			return
		permissions.add(perm)
	}

	/**
	 * Removes permission to user object
	 * @param perm Permission to be removed
	 */
	fun removePermission(perm: String) {
		if (isPermissionExists(perm))
			return
		permissions.remove(perm)
	}
}

/**
 * JPA Repository
 */
@RepositoryRestResource(exported = false)
interface SecurityRoleRepository : ApplicationBaseRepository<SecurityRole>

/**
 * Multiple Language Content entity
 */
@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(
	name = "t_security_role_mlcs", uniqueConstraints = [
		(UniqueConstraint(
			name = ApplicationConstraintCodes.SecurityRoleMCLUniqueIndex,
			columnNames = ["f_parent", "f_language_id", "f_field_name"]
		))
	]
)
class SecurityRoleMultipleLanguageContent : MultipleLanguageContentBaseEntity()

/**
 * Multiple Language Content Repository
 */
@RepositoryRestResource(exported = false)
interface SecurityRoleMLCRepository : MultipleLanguageContentBaseEntityRepository<SecurityRoleMultipleLanguageContent>