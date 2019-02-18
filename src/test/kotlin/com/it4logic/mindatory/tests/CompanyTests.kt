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

package com.it4logic.mindatory.tests

import com.fasterxml.jackson.databind.ObjectMapper
import com.it4logic.mindatory.controllers.common.ApplicationControllerEntryPoints
import com.it4logic.mindatory.model.Company
import com.it4logic.mindatory.model.CompanyRepository
import com.it4logic.mindatory.model.mlc.Language
import com.it4logic.mindatory.model.security.SecurityGroup
import com.it4logic.mindatory.model.security.SecurityRole
import com.it4logic.mindatory.model.security.SecurityUser
import com.it4logic.mindatory.security.ApplicationSecurityPermissions
import com.it4logic.mindatory.security.JwtAuthenticationResponse
import com.it4logic.mindatory.security.LoginRequest
import com.it4logic.mindatory.services.CompanyService
import com.it4logic.mindatory.services.LanguageService
import com.it4logic.mindatory.services.security.SecurityGroupService
import com.it4logic.mindatory.services.security.SecurityRoleService
import com.it4logic.mindatory.services.security.SecurityUserService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.hamcrest.Matchers.*
import org.junit.FixMethodOrder
import org.junit.runners.MethodSorters
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*
import org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import kotlin.test.assertEquals

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class CompanyTests {

    @Autowired
    private lateinit var context: WebApplicationContext

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var securityRoleService: SecurityRoleService

    @Autowired
    private lateinit var securityGroupService: SecurityGroupService

    @Autowired
    private lateinit var securityUserService: SecurityUserService

    @Autowired
    lateinit var languageService: LanguageService

    @Autowired
    private lateinit var companyService: CompanyService

    private lateinit var mvc: MockMvc

    private lateinit var roleAdmin: SecurityRole
    private lateinit var roleUser: SecurityRole

    private lateinit var adminGroup: SecurityGroup
    private lateinit var userGroup: SecurityGroup

    private lateinit var adminUser: SecurityUser
    private lateinit var normalUser: SecurityUser

    private lateinit var adminLogin: JwtAuthenticationResponse
    private lateinit var userLogin: JwtAuthenticationResponse

    private val _companyEntryPoint: String = ApplicationControllerEntryPoints.Company + "en/"

    companion object {
        var companyId = -1L
    }

    @Before
    fun setup() {
        mvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .build()

        setupLanguageData()
        setupSecurityData()

        companyId = companyService.create(Company("IT4L", id = 1)).id
    }

    fun setupLanguageData() {
        languageService.create(Language("en", "English", true))
        languageService.create(Language("ar", "عربي", false))
    }

    fun setupSecurityData() {
        roleAdmin = securityRoleService.create(SecurityRole("ROLE_ADMIN", "Admins Role",
            permissions = arrayListOf(
                ApplicationSecurityPermissions.CompanyAdminView,
                ApplicationSecurityPermissions.CompanyAdminModify
            )))
        roleUser = securityRoleService.create(SecurityRole("ROLE_USER", "Users Role"))

        adminGroup = securityGroupService.create(SecurityGroup("Admins Group", "Group for Admins"))
        userGroup = securityGroupService.create(SecurityGroup("Users Group", "Group for Users"))

        adminUser = securityUserService.create(SecurityUser("admin", "password", fullName = "Admin User", email = "admin@it4logic.com",
            roles = mutableListOf(roleAdmin), group = adminGroup))

        normalUser = securityUserService.create(SecurityUser("user", "password", fullName = "Manager User", email = "manager@it4logic.com",
            roles = mutableListOf(roleUser), group = userGroup))

        var contents = mvc.perform(
            post(ApplicationControllerEntryPoints.Authentication + "login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequest("admin", "password")))
        )
            .andExpect(status().isOk)
            .andReturn().response.contentAsString
        adminLogin = objectMapper.readValue(contents, JwtAuthenticationResponse::class.java)

        contents = mvc.perform(
            post(ApplicationControllerEntryPoints.Authentication + "login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequest("user", "password")))
        )
            .andExpect(status().isOk)
            .andReturn().response.contentAsString
        userLogin = objectMapper.readValue(contents, JwtAuthenticationResponse::class.java)
    }

    @Test
    fun companies () {
        // we need the root company to be with id of value 1
        assertEquals(1, companyId)

        // Check for view company with anonymous access
        mvc.perform(get(_companyEntryPoint).with(anonymous())).andExpect(unauthenticated())

        // Check for view company with lack of permissions
        mvc.perform(
            get(_companyEntryPoint)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
            )
            .andExpect(status().isForbidden)

        // load company
        val contents = mvc.perform(
            get(_companyEntryPoint)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name", equalTo("IT4L")))
            .andReturn().response.contentAsString

        val company = objectMapper.readValue(contents, Company::class.java)
        company.name = "IT4Logic"
        company.country = "EGYPT"
        company.mobile = "0536014030"

        // Check company update
        mvc.perform(
            post(_companyEntryPoint)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(company))
            )
            .andExpect(status().isMethodNotAllowed)

        mvc.perform(
            put(_companyEntryPoint)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(company))
            )
            .andExpect(status().isForbidden)

        mvc.perform(
            put(_companyEntryPoint)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(company))
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name", equalTo("IT4Logic")))
            .andExpect(jsonPath("$.country", equalTo("EGYPT")))
            .andExpect(jsonPath("$.mobile", equalTo("0536014030")))
    }
}