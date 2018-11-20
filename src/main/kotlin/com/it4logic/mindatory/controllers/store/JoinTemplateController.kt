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

package com.it4logic.mindatory.controllers.store

import com.it4logic.mindatory.controllers.common.ApplicationBaseController
import org.springframework.beans.factory.annotation.Autowired
import com.it4logic.mindatory.controllers.common.ApplicationControllerEntryPoints
import com.it4logic.mindatory.model.store.JoinStore
import com.it4logic.mindatory.services.common.ApplicationBaseService
import com.it4logic.mindatory.services.store.JoinStoreService
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
@RequestMapping(ApplicationControllerEntryPoints.JOIN_STORES)
class JoinStoreController : ApplicationBaseController<JoinStore>() {

  @Autowired
  lateinit var joinStoreService: JoinStoreService

  override fun service(): ApplicationBaseService<JoinStore> {
    return joinStoreService
  }
}