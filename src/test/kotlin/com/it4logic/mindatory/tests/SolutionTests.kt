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

package com.it4logic.mindatory.tests

import com.fasterxml.jackson.databind.ObjectMapper
import com.it4logic.mindatory.controllers.common.ApplicationControllerEntryPoints
import com.it4logic.mindatory.exceptions.ApplicationErrorCodes
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
class SolutionTests {

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

    private val _solutionsEntryPointEn: String = ApplicationControllerEntryPoints.Solutions + "en/"
    private val _solutionsEntryPointAr: String = ApplicationControllerEntryPoints.Solutions + "ar/"
    private val _repositoriesEntryPoint: String = ApplicationControllerEntryPoints.Repositories + "en/"

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
        languageService.create(Language("en", "English", true))
        languageService.create(Language("ar", "عربي", false))
    }

    fun setupSecurityData() {
        roleAdmin = securityRoleService.create(SecurityRole("ROLE_ADMIN", "Admins Role",
                permissions = arrayListOf(
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
    fun `Solutions Management`() {
        var solution1 = SolutionTest("Solution A")
        var solution2 = SolutionTest("Solution B")
        val solution3 = SolutionTest("Solution B")

        // create solutions
        mvc.perform(post(_solutionsEntryPointEn).with(anonymous()))
            .andExpect(status().isUnauthorized)

        var contents = mvc.perform(
            post(_solutionsEntryPointEn)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(solution1))
            )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name", equalTo("Solution A")))
            .andReturn().response.contentAsString
        solution1 = objectMapper.readValue(contents, SolutionTest::class.java)

        mvc.perform(
            post(_repositoriesEntryPoint)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    ApplicationRepositoryTest(
                        "ApplicationRepository A",
                        solution = solution1
                    )
                ))
            )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name", equalTo("ApplicationRepository A")))

        contents = mvc.perform(
            post(_solutionsEntryPointEn)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(solution2))
            )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name", equalTo("Solution B")))
            .andReturn().response.contentAsString
        solution2 = objectMapper.readValue(contents, SolutionTest::class.java)

        // duplicate check
        mvc.perform(
            post(_solutionsEntryPointEn)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(solution2))
            )
            .andExpect(status().isNotAcceptable)
            .andExpect(jsonPath("$.errorCode", equalTo(ApplicationErrorCodes.ValidationCannotCreateObjectWithExistingId)))

        mvc.perform(
            post(_solutionsEntryPointEn)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(solution3))
            )
            .andExpect(status().isNotAcceptable)
            .andExpect(jsonPath("$.errorCode", equalTo(ApplicationErrorCodes.DataIntegrityError)))
            .andExpect(jsonPath("$.errorData", equalTo(ApplicationErrorCodes.DuplicateSolutionName)))

        // load solution
        mvc.perform(
            get(_solutionsEntryPointEn + solution1.id)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
            )
            .andExpect(status().isOk)

        mvc.perform(
            post(_solutionsEntryPointEn)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(solution2))
            )
            .andExpect(status().isForbidden)

        mvc.perform(
            get(_solutionsEntryPointEn + solution1.id)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
            )
            .andExpect(status().isForbidden)

        var aclRequest = listOf(ApplicationAclPermissionRequest ("user", listOf(ApplicationPermission.View, ApplicationPermission.Modify)))
        mvc.perform(
            post(_solutionsEntryPointEn + solution1.id + "/permissions/add")
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(aclRequest))
            )
            .andExpect(status().isForbidden)

        mvc.perform(
            post(_solutionsEntryPointEn + solution1.id + "/permissions/add")
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(aclRequest))
            )
            .andExpect(status().isOk)

        mvc.perform(
            get(_solutionsEntryPointEn + solution1.id)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name", equalTo("Solution A")))

        aclRequest = listOf(ApplicationAclPermissionRequest ("user", listOf(ApplicationPermission.View)))
        mvc.perform(
            post(_solutionsEntryPointEn + solution1.id + "/permissions/remove")
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(aclRequest))
            )
            .andExpect(status().isOk)

        mvc.perform(
            get(_solutionsEntryPointEn + solution1.id)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
            )
            .andExpect(status().isOk)

        // update
        solution1.description = "updated"

        contents = mvc.perform(
            put(_solutionsEntryPointEn)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(solution1))
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.description", equalTo("updated")))
            .andReturn().response.contentAsString
        solution1 = objectMapper.readValue(contents, SolutionTest::class.java)

        // getting in Arabic
        mvc.perform(
            get(_solutionsEntryPointAr + solution1.id)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name", equalTo("Solution A")))

        // updating in Arabic
        solution1.description = "محدث"

        contents = mvc.perform(
            put(_solutionsEntryPointAr)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(solution1))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.description", equalTo("محدث")))
            .andReturn().response.contentAsString
        solution1 = objectMapper.readValue(contents, SolutionTest::class.java)

        // change the owner
        mvc.perform(
            get(_solutionsEntryPointEn + solution2.id)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
            )
            .andExpect(status().isForbidden)

        mvc.perform(
            post(_solutionsEntryPointEn + solution2.id + "/permissions/change-owner/user")
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
            )
            .andExpect(status().isOk)

        mvc.perform(
            get(_solutionsEntryPointEn + solution2.id)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name", equalTo("Solution B")))

        // delete
        mvc.perform(
            delete(_solutionsEntryPointEn + solution1.id)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
            )
            .andExpect(status().isForbidden)

        mvc.perform(
            delete(_solutionsEntryPointEn + solution1.id)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
            )
            .andExpect(status().isNotAcceptable)
            .andExpect(jsonPath("$.errorCode", equalTo(ApplicationErrorCodes.ValidationSolutionHasRepository)))

        mvc.perform(
            delete(_solutionsEntryPointEn + solution2.id)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
            )
            .andExpect(status().isOk)

        mvc.perform(
            get(_solutionsEntryPointEn + solution2.id)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
            )
            .andExpect(status().isNotFound)
    }

}