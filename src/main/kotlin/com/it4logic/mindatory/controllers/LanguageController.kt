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

package com.it4logic.mindatory.controllers

import com.it4logic.mindatory.controllers.common.ApplicationBaseController
import org.springframework.beans.factory.annotation.Autowired
import com.it4logic.mindatory.controllers.common.ApplicationControllerEntryPoints
import com.it4logic.mindatory.model.mlc.Language
import com.it4logic.mindatory.security.*
import com.it4logic.mindatory.services.LanguageService
import com.it4logic.mindatory.services.common.ApplicationBaseService
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.security.access.prepost.PostFilter
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.Errors
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.validation.Valid

@CrossOrigin
@RestController
@RequestMapping(ApplicationControllerEntryPoints.Languages)
class LanguageController : ApplicationBaseController<Language>() {

  override fun service(): ApplicationBaseService<Language> = languageService

  override fun type(): Class<Language> =  Language::class.java

  @GetMapping
  @ResponseBody
  @PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.LanguageAdminView}', '${ApplicationSecurityPermissions.LanguageAdminCreate}', '${ApplicationSecurityPermissions.LanguageAdminModify}', '${ApplicationSecurityPermissions.LanguageAdminDelete}')" )
  fun doGet(@PathVariable(required = false) locale: String, @RequestParam(required = false) filter: String?, pageable: Pageable, request: HttpServletRequest, response: HttpServletResponse): Any
          = doGetInternal(locale,filter, pageable, request, response)

  @GetMapping("{id}")
  @PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.LanguageAdminView}', '${ApplicationSecurityPermissions.LanguageAdminCreate}', '${ApplicationSecurityPermissions.LanguageAdminModify}', '${ApplicationSecurityPermissions.LanguageAdminDelete}')" )
  fun doGet(@PathVariable(required = false) locale: String, @PathVariable id: Long, request: HttpServletRequest, response: HttpServletResponse): Language
          = doGetInternal(locale,id, request, response)

  @PostMapping
  @PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.LanguageAdminCreate}')")
  fun doCreate(@PathVariable(required = false) locale: String, @Valid @RequestBody target: Language, errors: Errors, request: HttpServletRequest, response: HttpServletResponse): Language
          = doCreateInternal(locale,target, errors, request, response)

  @PutMapping
  @PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.LanguageAdminModify}')")
  fun doUpdate(@PathVariable(required = false) locale: String, @Valid @RequestBody target: Language, errors: Errors, request: HttpServletRequest, response: HttpServletResponse): Language
          = doUpdateInternal(locale,target, errors, request, response)

  @DeleteMapping("{id}")
  @PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.LanguageAdminDelete}')")
  fun doDelete(@PathVariable(required = false) locale: String, @PathVariable id: Long, request: HttpServletRequest, response: HttpServletResponse)
          = doDeleteInternal(locale,id, request, response)

  @DeleteMapping("{id}/force")
  @PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.LanguageAdminDelete}')")
  fun doForceDelete(@PathVariable(required = false) locale: String, @PathVariable id: Long, request: HttpServletRequest, response: HttpServletResponse) {
    val target = service().findById(id)
    languageService.forceDelete(target)
    response.status = HttpStatus.OK.value()
  }
}