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
import com.it4logic.mindatory.model.Company
import com.it4logic.mindatory.security.ApplicationSecurityPermissions
import com.it4logic.mindatory.services.CompanyService
import com.it4logic.mindatory.services.common.ApplicationBaseService
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.Errors
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.validation.Valid

@CrossOrigin
@RestController
@RequestMapping(ApplicationControllerEntryPoints.Company)
class CompanyController
	: ApplicationBaseController<Company>() {

	@Autowired
	lateinit var companyService: CompanyService

	override fun service(): ApplicationBaseService<Company> = companyService

	override fun type(): Class<Company> = Company::class.java

	@GetMapping
	@ResponseBody
	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.CompanyAdminView}', '${ApplicationSecurityPermissions.CompanyAdminCreate}', '${ApplicationSecurityPermissions.CompanyAdminModify}', '${ApplicationSecurityPermissions.CompanyAdminDelete}')")
	fun doGet(
		@PathVariable locale: String, request: HttpServletRequest,
		response: HttpServletResponse
	): Company {
		propagateLanguage(locale)
		val result = companyService.findFirst()
		service().refresh(result)
		response.status = HttpStatus.OK.value()
		return result
	}

	@PutMapping
	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.CompanyAdminModify}')")
	fun doUpdate(
		@PathVariable locale: String, @Valid @RequestBody target: Company, errors: Errors,
		request: HttpServletRequest,
		response: HttpServletResponse
	): Company = doUpdateInternal(locale, target, errors, request, response)
}