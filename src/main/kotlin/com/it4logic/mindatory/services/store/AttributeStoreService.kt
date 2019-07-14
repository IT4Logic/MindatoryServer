/*
    Copyright (c) 2019, IT4Logic.

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

package com.it4logic.mindatory.services.store

import com.fasterxml.jackson.databind.ObjectMapper
import com.it4logic.mindatory.exceptions.ApiError
import com.it4logic.mindatory.exceptions.ApplicationGeneralException
import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.model.store.AttributeStore
import com.it4logic.mindatory.model.store.AttributeStoreRepository
import com.it4logic.mindatory.services.RepositoryManagerService
import com.it4logic.mindatory.services.common.ApplicationBaseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional


@Service
@Transactional
class AttributeStoreService : ApplicationBaseService<AttributeStore>() {
    @Autowired
    private lateinit var attributeStoreRepository: AttributeStoreRepository

    @Autowired
    private lateinit var attributeStoreService: AttributeStoreService

    @Autowired
    private lateinit var repositoryManagerService: RepositoryManagerService

    override fun repository(): ApplicationBaseRepository<AttributeStore> = attributeStoreRepository

    override fun type(): Class<AttributeStore> = AttributeStore::class.java


    fun validateStore(target: AttributeStore) {
        val dataTypeManager = repositoryManagerService.getAttributeTemplateDataTypeManager(target.attributeTemplateVersion.typeUUID)
        val error = dataTypeManager.validateDataTypeContents(
                                UUID.fromString(target.attributeTemplateVersion.typeUUID),
                                target.attributeTemplateVersion.properties,
                                ObjectMapper().readTree(target.contents)) ?: return
        throw ApplicationGeneralException(error as ApiError)
    }
}