package com.it4logic.mindatory.model.mail

import com.fasterxml.jackson.annotation.JsonIgnore
import com.it4logic.mindatory.mlc.MultipleLanguageContent
import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.model.common.ApplicationConstraintCodes
import com.it4logic.mindatory.model.common.ApplicationMLCEntityBase
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntity
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntityRepository
import org.hibernate.envers.Audited
import org.hibernate.envers.NotAudited
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size


/*
class MailTemplateTypeUUID {
	companion object {
		const val ResetPassword: UUID = UUID.fromString("0b0994ce-aca6-40d2-aecd-67b536e3ba09")

		fun fromString(uuid: String): MailTemplateTypeUUID? {
			when(uuid) {
				ResetPassword.toString() -> return ResetPassword
			}
			return null
		}


	}
}
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

@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(
	name = "t_mail_templates", uniqueConstraints = [
		(UniqueConstraint(name = ApplicationConstraintCodes.MailTemplateUUIDUniqueIndex, columnNames = ["uuid"]))
	]
)
data class MailTemplate(
	@get: NotBlank
	@get: Size(min = 2, max = 50)
	@Column(length = 50)
	var uuid: String,

	@get: NotBlank
	@get: Size(max = 1024)
	@Column(length = 1024)
	@get: MultipleLanguageContent
	@Transient
	var subject: String,

	@get: NotBlank
	@Lob
	@get: MultipleLanguageContent
	@Transient
	var template: String,

	@NotAudited
	@OneToMany
	@JoinColumn(name = "parent", referencedColumnName = "id")
	@JsonIgnore
	var mlcs: MutableList<MailTemplateMultipleLanguageContent> = mutableListOf()

) : ApplicationMLCEntityBase() {
	override fun obtainMLCs(): MutableList<MultipleLanguageContentBaseEntity> {
		if (mlcs == null)
			mlcs = mutableListOf()
		return mlcs as MutableList<MultipleLanguageContentBaseEntity>
	}
}

/**
 * Repository
 */
@RepositoryRestResource(exported = false)
interface MailTemplateRepository : ApplicationBaseRepository<MailTemplate> {
	fun findOneByUuid(uuid: String): Optional<MailTemplate>
}


/**
 * Multiple Language Content support entity
 */

@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(
	name = "t_mail_tmpl_mlcs", uniqueConstraints = [
		(UniqueConstraint(
			name = ApplicationConstraintCodes.MailTemplateMCLUniqueIndex,
			columnNames = ["parent", "languageId", "fieldName"]
		))
	]
)
class MailTemplateMultipleLanguageContent : MultipleLanguageContentBaseEntity()

/**
 * Multiple Language Content support Repository
 */
@RepositoryRestResource(exported = false)
interface MailTemplateMLCRepository : MultipleLanguageContentBaseEntityRepository<MailTemplateMultipleLanguageContent>