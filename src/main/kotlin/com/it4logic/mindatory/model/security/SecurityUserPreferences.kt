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

import com.it4logic.mindatory.model.common.*
import com.it4logic.mindatory.model.mlc.Language
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntity
import javax.validation.constraints.Size
import org.hibernate.envers.Audited
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import javax.persistence.*
import javax.validation.constraints.NotNull


/**
 * User Preferences.
 */
@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "t_security_user_preferences", uniqueConstraints = [])
data class SecurityUserPreferences(
	@get: Size(max = 10)
	@Column(name = "f_ui_language", length = 10)
	var uiLanguage: String? = null,

	@ManyToOne
	@JoinColumn(name = "f_content_language")
	var contentLanguage: Language? = null
//	,
//
//	@get: NotNull
//	@OneToOne(mappedBy = "preferences")
//	var user: SecurityUser

) : ApplicationMLCEntityBase() {
	override fun obtainMLCs(): MutableList<MultipleLanguageContentBaseEntity> = mutableListOf()
}

/**
 * Repository
 */
@RepositoryRestResource(exported = false)
interface SecurityUserPreferencesRepository : ApplicationBaseRepository<SecurityUserPreferences>