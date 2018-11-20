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

package com.it4logic.mindatory.controllers

import org.springframework.beans.factory.annotation.Autowired
import com.it4logic.mindatory.controllers.common.ApplicationControllerEntryPoints
import com.it4logic.mindatory.model.Company
import com.it4logic.mindatory.services.CompanyService
import org.springframework.data.rest.core.RepositoryConstraintViolationException
import org.springframework.http.ResponseEntity
import org.springframework.validation.Errors
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

@CrossOrigin
@RestController
@RequestMapping(ApplicationControllerEntryPoints.COMPANY)
class CompanyController {

  @Autowired
  lateinit var companyService: CompanyService

  @GetMapping
  fun doGet(@PathVariable id: Long) : ResponseEntity<Company> {
    return ResponseEntity.ok().body(companyService.findFirst())
  }

  @PutMapping
  fun doUpdate(@Valid @RequestBody target: Company, errors: Errors, request: HttpServletRequest): ResponseEntity<Company> {
    if (errors.hasErrors())
      throw RepositoryConstraintViolationException(errors)

    val result = companyService.update(target)
    companyService.refresh(result)

    return ResponseEntity.ok().body(result)
  }
}