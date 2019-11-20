/*
    Copyright (c) 2019, IT4Logic.

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

package com.it4logic.mindatory.services.mail

import com.it4logic.mindatory.exceptions.ApplicationErrorCodes
import com.it4logic.mindatory.exceptions.ApplicationObjectNotFoundException
import com.it4logic.mindatory.exceptions.ApplicationValidationException
import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.model.mail.*
import com.it4logic.mindatory.services.common.ApplicationBaseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.transaction.Transactional
import kotlin.reflect.KClass


@Service
@Transactional
class MailTemplateService : ApplicationBaseService<MailTemplate>() {
	@Autowired
	private lateinit var mailTemplateRepository: MailTemplateRepository

	@Autowired
	private lateinit var mlcRepository: MailTemplateMLCRepository

	override fun repository(): ApplicationBaseRepository<MailTemplate> = mailTemplateRepository

	override fun type(): Class<MailTemplate> = MailTemplate::class.java

	override fun multipleLanguageContentRepository(): MailTemplateMLCRepository = mlcRepository

	override fun multipleLanguageContentType(): KClass<*> = MailTemplateMultipleLanguageContent::class

	override fun beforeCreate(target: MailTemplate) {
		if (!isTemplateUUIDValid(target))
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationMailTemplateUUIDNotFound)
	}

	override fun beforeUpdate(target: MailTemplate) {
		if (!isTemplateUUIDValid(target))
			throw ApplicationValidationException(ApplicationErrorCodes.ValidationMailTemplateUUIDNotFound)
	}

	private fun isTemplateUUIDValid(target: MailTemplate): Boolean {
		return MailTemplateTypeUUID.fromValueString(target.uuid) != null
	}

	fun getMailTemplate(mailTemplateTypeUUID: MailTemplateTypeUUID): MailTemplate {
		val uuid = mailTemplateTypeUUID.toUUID().toString()
		val template = mailTemplateRepository.findOneByUuid(uuid)
			.orElseThrow { ApplicationObjectNotFoundException(uuid, type().simpleName.toLowerCase()) }
		loadMLC(template)
		return template
	}
}