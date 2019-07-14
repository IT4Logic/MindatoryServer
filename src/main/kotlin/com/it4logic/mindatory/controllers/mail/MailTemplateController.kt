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

package com.it4logic.mindatory.controllers.mail

import com.it4logic.mindatory.controllers.common.ApplicationBaseController
import org.springframework.beans.factory.annotation.Autowired
import com.it4logic.mindatory.controllers.common.ApplicationControllerEntryPoints
import com.it4logic.mindatory.model.mail.MailTemplate
import com.it4logic.mindatory.security.*
import com.it4logic.mindatory.services.common.ApplicationBaseService
import com.it4logic.mindatory.services.mail.MailTemplateService
import org.springframework.data.domain.Pageable
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.Errors
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.validation.Valid

@CrossOrigin
@RestController
@RequestMapping(ApplicationControllerEntryPoints.MailTemplates)
class MailTemplateController : ApplicationBaseController<MailTemplate>() {

  @Autowired
  lateinit var mailTemplateService: MailTemplateService

  override fun service(): ApplicationBaseService<MailTemplate> = mailTemplateService

  override fun type(): Class<MailTemplate> =  MailTemplate::class.java

  @GetMapping
  @ResponseBody
  @PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.MailTemplateAdminView}', '${ApplicationSecurityPermissions.MailTemplateAdminCreate}', '${ApplicationSecurityPermissions.MailTemplateAdminModify}', '${ApplicationSecurityPermissions.MailTemplateAdminDelete}')" )
  fun doGet(@PathVariable locale: String, @RequestParam(required = false) filter: String?, pageable: Pageable, request: HttpServletRequest, response: HttpServletResponse): Any
          = doGetInternal(locale, filter, pageable, request, response)

  @GetMapping("{id}")
  @PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.MailTemplateAdminView}', '${ApplicationSecurityPermissions.MailTemplateAdminCreate}', '${ApplicationSecurityPermissions.MailTemplateAdminModify}', '${ApplicationSecurityPermissions.MailTemplateAdminDelete}')" )
  fun doGet(@PathVariable locale: String, @PathVariable id: Long, request: HttpServletRequest, response: HttpServletResponse): MailTemplate
          = doGetInternal(locale, id, request, response)

  @PostMapping
  @PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.MailTemplateAdminCreate}')")
  fun doCreate(@PathVariable locale: String, @Valid @RequestBody target: MailTemplate, errors: Errors, request: HttpServletRequest, response: HttpServletResponse): MailTemplate
          = doCreateInternal(locale, target, errors, request, response)

  @PutMapping
  @PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.MailTemplateAdminModify}')")
  fun doUpdate(@PathVariable locale: String, @Valid @RequestBody target: MailTemplate, errors: Errors, request: HttpServletRequest, response: HttpServletResponse): MailTemplate
          = doUpdateInternal(locale, target, errors, request, response)

  @DeleteMapping("{id}")
  @PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.MailTemplateAdminDelete}')")
  fun doDelete(@PathVariable locale: String, @PathVariable id: Long, request: HttpServletRequest, response: HttpServletResponse)
          = doDeleteInternal(locale, id, request, response)
}