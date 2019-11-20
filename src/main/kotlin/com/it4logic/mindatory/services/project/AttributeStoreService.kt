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

package com.it4logic.mindatory.services.project

import com.it4logic.mindatory.exceptions.ApplicationGeneralException
import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.model.project.AttributeStore
import com.it4logic.mindatory.model.project.AttributeStoreMLCRepository
import com.it4logic.mindatory.model.project.AttributeStoreMultipleLanguageContent
import com.it4logic.mindatory.model.project.AttributeStoreRepository
import com.it4logic.mindatory.services.AttributeTemplateDataTypeManagerService
import com.it4logic.mindatory.services.common.ApplicationBaseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.transaction.Transactional
import kotlin.reflect.KClass


@Service
@Transactional
class AttributeStoreService : ApplicationBaseService<AttributeStore>() {
	@Autowired
	private lateinit var attributeStoreRepository: AttributeStoreRepository

	@Autowired
	private lateinit var attributeStoreService: AttributeStoreService

	@Autowired
	private lateinit var attributeTemplateDataTypeManagerService: AttributeTemplateDataTypeManagerService

	@Autowired
	private lateinit var mlcRepository: AttributeStoreMLCRepository

	override fun repository(): ApplicationBaseRepository<AttributeStore> = attributeStoreRepository

	override fun type(): Class<AttributeStore> = AttributeStore::class.java

	override fun multipleLanguageContentRepository(): AttributeStoreMLCRepository = mlcRepository

	override fun multipleLanguageContentType(): KClass<*> = AttributeStoreMultipleLanguageContent::class

	fun validateStore(target: AttributeStore) {
		val error =
			attributeTemplateDataTypeManagerService.getAttributeTemplateDataType(target.attributeTemplate.typeUUID).validateDataTypeContents(
				target.contents,
				target.attributeTemplate.properties
			) ?: return
		throw ApplicationGeneralException(error)
	}

//	fun countByAttributeTemplateRepositoryVersionId(id: Long): Long {
//		return attributeStoreRepository.countByAttributeTemplateRepositoryVersionId(id)
//	}
}