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
import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.model.common.ApplicationMLCEntityBase
import com.it4logic.mindatory.model.common.ApplicationConstraintCodes
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

@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "t_security_groups", uniqueConstraints = [])
data class SecurityGroup(
	@get: NotNull
	@get: Size(min = 2, max = 100)
	@get: MultipleLanguageContent
	@Transient
	var name: String = "",

	@get: Size(max = 255)
	@get: MultipleLanguageContent
	@Transient
	var description: String = "",

	@NotAudited
	@OneToMany(fetch = FetchType.EAGER)
	@JoinColumn(name = "f_parent", referencedColumnName = "f_id")
	@JsonIgnore
	@get: GraphQLIgnore
	var mlcs: MutableList<SecurityGroupMultipleLanguageContent> = mutableListOf()

) : ApplicationMLCEntityBase() {
	@Suppress("SENSELESS_COMPARISON", "UNCHECKED_CAST")
	override fun obtainMLCs(): MutableList<MultipleLanguageContentBaseEntity> {
		if (mlcs == null)
			mlcs = mutableListOf()
		return mlcs as MutableList<MultipleLanguageContentBaseEntity>
	}
}


/**
 * SecurityGroup Entity Repository
 */
@RepositoryRestResource(exported = false)
interface SecurityGroupRepository : ApplicationBaseRepository<SecurityGroup>


/**
 * Multiple Language Content support entity
 */
@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(
	name = "t_security_group_mlcs", uniqueConstraints = [
		(UniqueConstraint(
			name = ApplicationConstraintCodes.SecurityGroupMCLUniqueIndex,
			columnNames = ["f_parent", "f_language_id", "f_field_name"]
		))
	]
)
class SecurityGroupMultipleLanguageContent : MultipleLanguageContentBaseEntity()

/**
 * Multiple Language Content support Repository
 */
@RepositoryRestResource(exported = false)
interface SecurityGroupMLCRepository : MultipleLanguageContentBaseEntityRepository<SecurityGroupMultipleLanguageContent>