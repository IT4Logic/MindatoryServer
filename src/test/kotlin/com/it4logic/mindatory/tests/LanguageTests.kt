/*
    Copyright (c) 2019, IT4Logic.

    This file is part of Mindatory language by IT4Logic.

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
import com.it4logic.mindatory.exceptions.ApplicationErrorCodes
import com.it4logic.mindatory.model.Solution
import com.it4logic.mindatory.model.mlc.Language
import com.it4logic.mindatory.model.security.SecurityGroup
import com.it4logic.mindatory.model.security.SecurityRole
import com.it4logic.mindatory.model.security.SecurityUser
import com.it4logic.mindatory.security.*
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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class LanguageTests {

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
    private lateinit var languageService: LanguageService

    private lateinit var mvc: MockMvc

    private lateinit var roleAdmin: SecurityRole
    private lateinit var roleUser: SecurityRole

    private lateinit var adminGroup: SecurityGroup
    private lateinit var userGroup: SecurityGroup

    private lateinit var adminUser: SecurityUser
    private lateinit var normalUser: SecurityUser

    private lateinit var adminLogin: JwtAuthenticationResponse
    private lateinit var userLogin: JwtAuthenticationResponse

    private lateinit var enLanguage: Language
    private val _languagesEntryPointEn: String = ApplicationControllerEntryPoints.Languages + "en/"
    private val _languagesEntryPointAr: String = ApplicationControllerEntryPoints.Languages + "ar/"
    private val _solutionsEntryPointEn: String = ApplicationControllerEntryPoints.Solutions + "en/"

    @Before
    fun setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply<DefaultMockMvcBuilder>(springSecurity())
                .build()

        setupLanguageData()
        setupSecurityData()
    }

    fun setupLanguageData() {
        enLanguage = languageService.create(Language("en", "English", true))
    }

    fun setupSecurityData() {
        roleAdmin = securityRoleService.create(SecurityRole("ROLE_ADMIN", "Admins Role",
                permissions = arrayListOf(
                        ApplicationSecurityPermissions.LanguageAdminView,
                        ApplicationSecurityPermissions.LanguageAdminCreate,
                        ApplicationSecurityPermissions.LanguageAdminModify,
                        ApplicationSecurityPermissions.LanguageAdminDelete,
                        ApplicationSecurityPermissions.SolutionAdminView,
                        ApplicationSecurityPermissions.SolutionAdminCreate,
                        ApplicationSecurityPermissions.SolutionAdminModify,
                        ApplicationSecurityPermissions.SolutionAdminDelete,
                        ApplicationSecurityPermissions.ApplicationRepositoryAdminCreate
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
    fun `Languages Management`() {
        var language1 = LanguageTest("ar", "عربي", false)
        var language2 = LanguageTest("fr", "France", false)
        val language3 = LanguageTest("fr", "France", false)

        // create languages
        mvc.perform(post(_languagesEntryPointEn).with(anonymous()))
            .andExpect(status().isUnauthorized)

        var contents = mvc.perform(
            post(_languagesEntryPointEn)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(language1))
            )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name", equalTo("عربي")))
            .andReturn().response.contentAsString
        language1 = objectMapper.readValue(contents, LanguageTest::class.java)

        contents = mvc.perform(
            post(_languagesEntryPointEn)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(language2))
            )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name", equalTo("France")))
            .andReturn().response.contentAsString
        language2 = objectMapper.readValue(contents, LanguageTest::class.java)

        // duplicate check
        mvc.perform(
            post(_languagesEntryPointEn)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(language2))
            )
            .andExpect(status().isNotAcceptable)
            .andExpect(jsonPath("$.errorCode", equalTo(ApplicationErrorCodes.ValidationCannotCreateObjectWithExistingId)))

        language3.locale = "frr"
        mvc.perform(
            post(_languagesEntryPointEn)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(language3))
            )
            .andExpect(status().isNotAcceptable)
            .andExpect(jsonPath("$.errorCode", equalTo(ApplicationErrorCodes.DataIntegrityError)))
            .andExpect(jsonPath("$.errorData", equalTo(ApplicationErrorCodes.DuplicateLanguageName)))

        language3.locale = "fr"
        language3.name = "French"
        mvc.perform(
            post(_languagesEntryPointEn)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(language3))
        )
            .andExpect(status().isNotAcceptable)
            .andExpect(jsonPath("$.errorCode", equalTo(ApplicationErrorCodes.DataIntegrityError)))
            .andExpect(jsonPath("$.errorData", equalTo(ApplicationErrorCodes.DuplicateLanguageLocale)))

        // load language
        mvc.perform(
            get(_languagesEntryPointEn + language1.id)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
            )
            .andExpect(status().isOk)

        mvc.perform(
            post(_languagesEntryPointEn)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(language2))
            )
            .andExpect(status().isForbidden)

        mvc.perform(
            get(_languagesEntryPointEn + language1.id)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
            )
            .andExpect(status().isForbidden)

        // update
        language1.name = "اللغة العربية"

        contents = mvc.perform(
            put(_languagesEntryPointEn)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(language1))
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name", equalTo("اللغة العربية")))
            .andReturn().response.contentAsString
        language1 = objectMapper.readValue(contents, LanguageTest::class.java)

        // getting in Arabic
        mvc.perform(
            get(_languagesEntryPointAr + language1.id)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name", equalTo("اللغة العربية")))

        // change default language
        language2.default = true

        mvc.perform(
            put(_languagesEntryPointEn)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(language2))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.default", equalTo(true)))

        mvc.perform(
            get(_languagesEntryPointAr + language1.id)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.default", equalTo(false)))

        // delete
        mvc.perform(
            delete(_languagesEntryPointEn + language1.id)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
            )
            .andExpect(status().isForbidden)

        mvc.perform(
            delete(_languagesEntryPointEn + language2.id)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
            )
            .andExpect(status().isNotAcceptable)
            .andExpect(jsonPath("$.errorCode", equalTo(ApplicationErrorCodes.ValidationCannotDeleteDefaultLanguage)))

        mvc.perform(
            delete(_languagesEntryPointEn + language1.id)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
        )
            .andExpect(status().isOk)


        // Create MLC
        mvc.perform(
            post(_solutionsEntryPointEn)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Solution("Solution A")))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name", equalTo("Solution A")))
            .andReturn().response.contentAsString

        // delete language with MLCs
        mvc.perform(
            delete(_languagesEntryPointEn + enLanguage.id)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
        )
            .andExpect(status().isNotAcceptable)
            .andExpect(jsonPath("$.errorCode", equalTo(ApplicationErrorCodes.ValidationLanguageHasRelatedContents)))

        // delete language with MLCs by force
        mvc.perform(
            delete(_languagesEntryPointEn + enLanguage.id + "/force")
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
        )
            .andExpect(status().isOk)
    }

}