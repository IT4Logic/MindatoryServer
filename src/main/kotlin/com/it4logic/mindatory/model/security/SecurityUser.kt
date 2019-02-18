/*
    Copyright (c) 2018, IT4Logic.

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

package com.it4logic.mindatory.model.security

import com.it4logic.mindatory.mlc.MultipleLanguageContent
import com.it4logic.mindatory.model.common.*
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntity
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntityRepository
import org.hibernate.envers.Audited
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.*
import javax.validation.constraints.*
import javax.persistence.*

@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "t_security_users", uniqueConstraints = [
            UniqueConstraint(name = ApplicationConstraintCodes.SecurityUserUsernameUniqueIndex, columnNames = ["username"])
        ]
)
data class SecurityUser (
        @get: NotBlank
        @get: Size(min = 4, max = 50)
        @Column(nullable = false, length = 50)
        var username: String = "",

        @Column(nullable = false)
        @get: NotBlank
        @get: Size(min = 6)
        var password: String = "",

        var accountEnabled: Boolean = true,

        var accountLocked: Boolean = false,

        var accountExpired: Boolean = false,

        var passwordExpired: Boolean = false,

        var passwordNeverExpires: Boolean = true,

        var passwordChangeAtNextLogin: Boolean = false,

        @get: NotBlank
        @get: Size(min = 4, max = 100)
        @get: MultipleLanguageContent
        @Transient
        var fullName: String = "",

        @get: NotBlank
        @get: Email
        @get: Size(max = 100)
        @Column(nullable = false, length = 100)
        var email: String = "",

        @get: Size(max = 20)
        @Column(length = 20)
        var mobile: String = "",

        @get: Size(max = 255)
        @get: MultipleLanguageContent
        @Transient
        var notes: String = "",

        @get: NotNull
        @get: MultipleLanguageContent
        @ManyToOne(fetch = FetchType.EAGER, optional = false)
        @JoinColumn(name = "group_id", nullable = false)
        var group: SecurityGroup? = null,

        @get: MultipleLanguageContent
        @ManyToMany(fetch = FetchType.EAGER)
        @JoinTable(name = "t_security_users_roles", joinColumns = [JoinColumn(name = "user_id")], inverseJoinColumns = [JoinColumn(name = "role_id")])
        var roles: MutableList<SecurityRole> = mutableListOf()

) : ApplicationCompanyEntityBase() {
        private fun isRoleExists(role: SecurityRole) : Boolean {
                for(r in roles) {
                        if(r.id == role.id)
                                return true
                }
                return false
        }

        fun addRole(role: SecurityRole) {
                if(isRoleExists(role))
                        return
                roles.add(role)
        }

        fun removeRole(role: SecurityRole) {
                for(r in roles) {
                        if(r.id == role.id) {
                                roles.remove(r)
                                break
                        }
                }
        }
}

/**
 * SecurityUsers Entity Repository
 */
@RepositoryRestResource(exported = false)
interface SecurityUserRepository : ApplicationCompanyBaseRepository<SecurityUser> {
//        fun findAllByGroupId(id: Long, @Nullable spec: Specification<SecurityUser>) : MutableList<SecurityUser>
//        fun findAllByGroupId(id: Long, @Nullable spec: Specification<SecurityUser>, pageable: Pageable) : Page<SecurityUser>
//        fun findAllByGroupId(id: Long, @Nullable spec: Specification<SecurityUser>, sort: Sort) : MutableList<SecurityUser>
        fun findAllByGroupId(id: Long) : MutableList<SecurityUser>
        fun findAllByRolesId(id: Long) : MutableList<SecurityUser>
        fun findByUsername(username: String): Optional<SecurityUser>
}

/**
 * Multiple Language Content support entity
 */
@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "t_security_user_mlcs", uniqueConstraints = [
        (UniqueConstraint(name = ApplicationConstraintCodes.SecurityUserMCLUniqueIndex, columnNames = ["parentId", "languageId", "fieldName"]))
])
class SecurityUserMultipleLanguageContent : MultipleLanguageContentBaseEntity()

/**
 * Multiple Language Content support Repository
 */
@RepositoryRestResource(exported = false)
interface SecurityUserMLCRepository : MultipleLanguageContentBaseEntityRepository<SecurityUserMultipleLanguageContent>