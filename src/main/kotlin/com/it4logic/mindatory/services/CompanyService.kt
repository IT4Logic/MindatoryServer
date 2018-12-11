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

package com.it4logic.mindatory.services

import com.it4logic.mindatory.model.Company
import com.it4logic.mindatory.model.CompanyRepository
import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.security.ApplicationSecurityPermissions
import com.it4logic.mindatory.services.common.ApplicationBaseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import javax.transaction.NotSupportedException
import javax.transaction.Transactional


@Service
@Transactional
class CompanyService : ApplicationBaseService<Company>() {
  @Autowired
  private lateinit var companyRepository: CompanyRepository

  override fun repository(): ApplicationBaseRepository<Company> = companyRepository

  override fun type(): Class<Company> = Company::class.java

  fun findFirst(): Company = repository().findAll()[0]
}