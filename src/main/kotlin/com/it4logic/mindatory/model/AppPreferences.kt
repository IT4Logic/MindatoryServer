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

package com.it4logic.mindatory.model

import com.it4logic.mindatory.model.common.*
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntity
import javax.validation.constraints.Size
import org.hibernate.envers.Audited
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.*
import javax.persistence.*


/**
 * System Preferences
 */
@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "t_app_preferences", uniqueConstraints = [])
data class AppPreferences(
	@get: Size(max = 255)
	@Column(name = "f_default_mail_from", length = 255)
	var defaultMailFrom: String = "",

	@get: Size(max = 512)
	@Column(name = "f_smtp_server_url", length = 512)
	var smtpServerUrl: String = "",

	@Column(name = "f_smtp_server_port")
	var smtpServerPort: Int = 0,

	@get: Size(max = 100)
	@Column(name = "f_smtp_server_username", length = 100)
	var smtpServerUsername: String = "",

	@get: Size(max = 100)
	@Column(name = "f_smtp_server_password", length = 100)
	var smtpServerPassword: String = "",

	@Lob
	@Column(name = "f_smtp_server_properties")
	var smtpServerProperties: String = ""

) : ApplicationEntityBase() {
	override fun obtainMLCs(): MutableList<MultipleLanguageContentBaseEntity> = mutableListOf()

	/**
	 * Convert SMTP Server properties from string form to [Properties] instance
	 * @return [Properties] instance for SMTP Server properties string form
	 */
	fun obtainSMTPServerProperties(): Properties {
		val out = Properties()
		if (smtpServerProperties.isBlank())
			return out
		val props = smtpServerProperties.split(',')
		props.forEach {
			val result = it.replace("\"".toRegex(), "").split(':')
			out[result[0]] = result[1]
		}
		return out
	}
}

/**
 * Repository
 */
@RepositoryRestResource(exported = false)
interface AppPreferencesRepository : ApplicationBaseRepository<AppPreferences>
