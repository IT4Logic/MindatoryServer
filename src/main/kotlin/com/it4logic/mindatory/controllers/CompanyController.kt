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
import com.it4logic.mindatory.mlc.LanguageManager
import com.it4logic.mindatory.model.Company
import com.it4logic.mindatory.security.ApplicationSecurityPermissions
import com.it4logic.mindatory.services.CompanyService
import com.it4logic.mindatory.services.LanguageService
import org.springframework.data.rest.core.RepositoryConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.Errors
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

@CrossOrigin
@RestController
@RequestMapping(ApplicationControllerEntryPoints.Company + "{locale}/")
class CompanyController {

  @Autowired
  lateinit var companyService: CompanyService

  @Autowired
  lateinit var languageService: LanguageService

  @Autowired
  lateinit var languageManager: LanguageManager

  protected fun propagateLanguage(locale: String?) {
    val language = languageService.findLanguageByLocaleOrDefault(locale)
    languageManager.currentLanguage = language
  }

  @GetMapping
  @ResponseBody
  @PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.CompanyAdminView}', '${ApplicationSecurityPermissions.CompanyAdminCreate}', '${ApplicationSecurityPermissions.CompanyAdminModify}', '${ApplicationSecurityPermissions.CompanyAdminDelete}')")
  fun doGet(@PathVariable locale: String) : ResponseEntity<Company> {
    propagateLanguage(locale)
    return ResponseEntity.ok().body(companyService.findFirst())
  }

  @PutMapping
  @PreAuthorize("hasAuthority('${ApplicationSecurityPermissions.CompanyAdminModify}')")
  fun doUpdate(@PathVariable locale: String, @Valid @RequestBody target: Company, errors: Errors, request: HttpServletRequest): ResponseEntity<Company> {
    propagateLanguage(locale)
    if (errors.hasErrors())
      throw RepositoryConstraintViolationException(errors)

    val company = companyService.findFirst()
    if(company.id != target.id)
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null)

    val result = companyService.update(target)
    companyService.refresh(result)

    return ResponseEntity.ok().body(result)
  }
}