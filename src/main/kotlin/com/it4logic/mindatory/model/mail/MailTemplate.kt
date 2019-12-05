/*
    Copyright (c) 2018, IT4Logic. All rights reserved.

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
package com.it4logic.mindatory.model.mail

import com.fasterxml.jackson.annotation.JsonIgnore
import com.it4logic.mindatory.mlc.MultipleLanguageContent
import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.model.common.ApplicationConstraintCodes
import com.it4logic.mindatory.model.common.ApplicationEntityBase
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntity
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntityRepository
import io.leangen.graphql.annotations.GraphQLIgnore
import org.hibernate.annotations.LazyCollection
import org.hibernate.annotations.LazyCollectionOption
import org.hibernate.envers.Audited
import org.hibernate.envers.NotAudited
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

/**
 * Enumerator for Mail Template Types
 */
enum class MailTemplateTypeUUID(private val uuid: UUID) {
	Welcome(UUID.fromString("efad9493-df35-4ba1-8169-ce19590acb73")),
	ResetPassword(UUID.fromString("0b0994ce-aca6-40d2-aecd-67b536e3ba09"));

	companion object {
		fun fromValueString(uuid: String): MailTemplateTypeUUID? {
			when (uuid) {
				Welcome.uuid.toString() -> return Welcome
				ResetPassword.uuid.toString() -> return ResetPassword
			}
			return null
		}
	}

	fun toUUID(): UUID = this.uuid
}

/**
 * Mail Template Entity
 */
@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(
	name = "t_mail_templates", uniqueConstraints = [
		(UniqueConstraint(name = ApplicationConstraintCodes.MailTemplateUUIDUniqueIndex, columnNames = ["f_uuid"]))
	]
)
data class MailTemplate(
	@get: NotBlank
	@get: Size(min = 2, max = 50)
	@Column(name = "f_uuid", length = 50)
	var uuid: String,

	@get: NotBlank
	@get: Size(max = 1024)
	@get: MultipleLanguageContent
	@Transient
	var subject: String,

	@get: NotBlank
	@Lob
	@get: MultipleLanguageContent
	@Transient
	var template: String,

	@NotAudited
	@OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, mappedBy = "parent")
	@LazyCollection(LazyCollectionOption.FALSE)
	@JsonIgnore
	@get: GraphQLIgnore
	var mlcs: MutableList<MailTemplateMultipleLanguageContent> = mutableListOf()

) : ApplicationEntityBase() {
	@Suppress("SENSELESS_COMPARISON", "UNCHECKED_CAST")
	override fun obtainMLCs(): MutableList<MultipleLanguageContentBaseEntity> {
		if (mlcs == null)
			mlcs = mutableListOf()
		return mlcs as MutableList<MultipleLanguageContentBaseEntity>
	}
}

/**
 * JPA Repository
 */
@RepositoryRestResource(exported = false)
interface MailTemplateRepository : ApplicationBaseRepository<MailTemplate> {
	fun findOneByUuid(uuid: String): Optional<MailTemplate>
}


/**
 * Multiple Language Content entity
 */

@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(
	name = "t_mail_template_mlcs", uniqueConstraints = [
		(UniqueConstraint(
			name = ApplicationConstraintCodes.MailTemplateMCLUniqueIndex,
			columnNames = ["f_parent", "f_language_id", "f_field_name"]
		))
	]
)
class MailTemplateMultipleLanguageContent(
	@get: NotNull
	@ManyToOne
	@JoinColumn(name = "f_parent", nullable = false)
	var parent: MailTemplate? = null
) : MultipleLanguageContentBaseEntity() {
	override fun updatedParent(obj: ApplicationEntityBase?) { parent = obj as MailTemplate? }
	override fun obtainParent(): ApplicationEntityBase? = parent
}

/**
 * Multiple Language Content JPA Repository
 */
@RepositoryRestResource(exported = false)
interface MailTemplateMLCRepository : MultipleLanguageContentBaseEntityRepository<MailTemplateMultipleLanguageContent>