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

import com.fasterxml.jackson.annotation.JsonIgnore
import com.it4logic.mindatory.helpers.ZipManager
import com.it4logic.mindatory.mlc.MultipleLanguageContent
import com.it4logic.mindatory.model.common.*
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntity
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntityRepository
import org.hibernate.envers.Audited
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import javax.persistence.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size
import kotlin.collections.ArrayList

@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "t_security_roles", uniqueConstraints = [])
data class SecurityRole (

        @get: NotNull
        @get: Size(min = 2, max = 50)
        @get: MultipleLanguageContent
        @Transient
        var name: String = "",

        @get: Size(max = 255)
        @get: MultipleLanguageContent
        @Transient
        var description: String = "",

        @Column
        var company: Long = 1,

        @Column(insertable=false, updatable = false, length = 1)
        var permissions: ArrayList<String> = ArrayList(),

        @Lob
        @JsonIgnore
        var authorities: ByteArray? = null

) : ApplicationCompanyEntityBase() {

        /**
         * Converts permissions from encoded format into string array
         */
        @PrePersist
        @PreUpdate
        fun prePersistOrUpdate() {
                var perms = ""
                permissions.forEach { perms += "$it;" }
                perms = perms.removeSuffix(";")
                authorities = ZipManager.gzip(perms)
        }

        /**
         * Converts permissions from string array to encoded format
         */
        @PostLoad
        fun postLoad() {
                permissions = ArrayList()
                if(authorities == null)
                        return
                val perms = ZipManager.ungzip(authorities)
                permissions.addAll(perms.split(";"))
        }

        // implementing equals method to avoid the byte array variable
        override fun equals(other: Any?): Boolean {
                if (this === other) return true
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

        private fun isPermissionExists(perm: String) : Boolean {
                return permissions.contains(perm)
        }

        fun addPermission(perm: String) {
                if(permissions.contains(perm))
                        return
                permissions.add(perm)
        }

        fun removePermission(perm: String) {
                if(!permissions.contains(perm))
                        return
                permissions.remove(perm)
        }
}

/**
 * SecurityRoles Entity Repository
 */
@RepositoryRestResource(exported = false)
interface SecurityRoleRepository : ApplicationCompanyBaseRepository<SecurityRole>

/**
 * Multiple Language Content support entity
 */
@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "t_security_role_mlcs", uniqueConstraints = [
        (UniqueConstraint(name = ApplicationConstraintCodes.SecurityRoleMCLUniqueIndex, columnNames = ["parentId", "languageId", "fieldName"]))
])
class SecurityRoleMultipleLanguageContent : MultipleLanguageContentBaseEntity()

/**
 * Multiple Language Content support Repository
 */
@RepositoryRestResource(exported = false)
interface SecurityRoleMLCRepository : MultipleLanguageContentBaseEntityRepository<SecurityRoleMultipleLanguageContent>