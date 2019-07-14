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

import com.it4logic.mindatory.exceptions.ApplicationErrorCodes
import com.it4logic.mindatory.exceptions.ApplicationValidationException
import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.model.common.DesignStatus
import com.it4logic.mindatory.model.store.JoinStore
import com.it4logic.mindatory.model.store.JoinStoreRepository
import com.it4logic.mindatory.services.common.ApplicationBaseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.transaction.Transactional


@Service
@Transactional
class JoinStoreService : ApplicationBaseService<JoinStore>() {
  @Autowired
  private lateinit var joinStoreRepository: JoinStoreRepository

  override fun repository(): ApplicationBaseRepository<JoinStore> = joinStoreRepository

  override fun type(): Class<JoinStore> = JoinStore::class.java

  override fun beforeCreate(target: JoinStore) {
//    if(target.joinTemplate.id != target.joinTemplateVersion.joinTemplate.id)
//      throw ApplicationValidationException(ApplicationErrorCodes.ValidationStoreObjectVersionAndTemplateMismatch)

    if(target.joinTemplateVersion.designStatus != DesignStatus.Released)
      throw ApplicationValidationException(ApplicationErrorCodes.ValidationStoreObjectCanOnlyBeAssociatedWithReleasedVersion)
  }

  override fun beforeUpdate(target: JoinStore) {
//    if(target.joinTemplate.id != target.joinTemplateVersion.joinTemplate.id)
//      throw ApplicationValidationException(ApplicationErrorCodes.ValidationStoreObjectVersionAndTemplateMismatch)

    if(target.joinTemplateVersion.designStatus != DesignStatus.Released)
      throw ApplicationValidationException(ApplicationErrorCodes.ValidationStoreObjectCanOnlyBeAssociatedWithReleasedVersion)
  }

  override fun create(target: JoinStore): JoinStore {
    target.joinTemplate = target.joinTemplateVersion.joinTemplate
    return super.create(target)
  }

  override fun update(target: JoinStore): JoinStore {
    target.joinTemplate = target.joinTemplateVersion.joinTemplate
    return super.update(target)
  }
}