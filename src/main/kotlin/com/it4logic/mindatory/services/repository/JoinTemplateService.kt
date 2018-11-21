/*
    Copyright (c) 2017, IT4Logic.

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

package com.it4logic.mindatory.services.repository

import com.it4logic.mindatory.exceptions.ApplicationErrorCodes
import com.it4logic.mindatory.exceptions.ApplicationObjectNotFoundException
import com.it4logic.mindatory.exceptions.ApplicationValidationException
import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.model.repository.JoinTemplate
import com.it4logic.mindatory.model.repository.JoinTemplateRepository
import com.it4logic.mindatory.model.store.JoinStoreRepository
import com.it4logic.mindatory.services.common.ApplicationBaseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
@Transactional
class JoinTemplateService : ApplicationBaseService<JoinTemplate>() {
  @Autowired
  private lateinit var joinTemplateRepository: JoinTemplateRepository

  @Autowired
  private lateinit var joinStoreRepository: JoinStoreRepository

  override fun repository(): ApplicationBaseRepository<JoinTemplate> = joinTemplateRepository

  override fun type(): Class<JoinTemplate> = JoinTemplate::class.java

  override fun beforeDelete(target: JoinTemplate) {
    val count = joinStoreRepository.countByJoinTemplateId(target.id)
    if(count > 0)
      throw ApplicationValidationException(ApplicationErrorCodes.ValidationJoinTemplateHasRelatedStoreData)
  }
}