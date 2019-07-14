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
import com.it4logic.mindatory.model.ApplicationMetadataRepository
import com.it4logic.mindatory.model.security.SecurityGroup
import com.it4logic.mindatory.model.security.SecurityRole
import com.it4logic.mindatory.model.security.SecurityUser
import com.it4logic.mindatory.security.ApplicationSecurityPermissions
import com.it4logic.mindatory.security.JwtAuthenticationResponse
import com.it4logic.mindatory.security.LoginRequest
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
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class AuthenticationTests {

    @Autowired
    private lateinit var context: WebApplicationContext

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var applicationMetadataRepository: ApplicationMetadataRepository

    @Autowired
    private lateinit var securityRoleService: SecurityRoleService

    @Autowired
    private lateinit var securityGroupService: SecurityGroupService

    @Autowired
    private lateinit var securityUserService: SecurityUserService

    @Autowired
    private lateinit var languageService: LanguageService

    private lateinit var mvc: MockMvc

    private lateinit var roleUser: SecurityRole

    private lateinit var userGroup: SecurityGroup

    private lateinit var normalUser: SecurityUser

    private val _usersEntryPointEn = TestHelper.setLocaleForEntryPoint(ApplicationControllerEntryPoints.SecurityUsers, "en")
    private val _rolesEntryPointEn = TestHelper.setLocaleForEntryPoint(ApplicationControllerEntryPoints.SecurityRoles, "en")
    private val _groupsEntryPointEn = TestHelper.setLocaleForEntryPoint(ApplicationControllerEntryPoints.SecurityGroups, "en")
    
    @Before
    fun setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply<DefaultMockMvcBuilder>(springSecurity())
                .build()

        setupSecurityData()
    }

    fun setupSecurityData() {
        roleUser = securityRoleService.create(SecurityRole("ROLE_USER", "Users Role",
                permissions = arrayListOf(ApplicationSecurityPermissions.SecurityRoleAdminView)))

        userGroup = securityGroupService.create(SecurityGroup("Users Group", "Group for Users"))

        normalUser = securityUserService.create(SecurityUser("user", "password", fullName = "Manager User", email = "manager@it4logic.com",
                roles = mutableListOf(roleUser), group = userGroup))
    }

    @Test
    fun `Authentication and Authorization`() {
        // check login with wrong username
        mvc.perform(
                post(ApplicationControllerEntryPoints.Authentication + "login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(LoginRequest("admin1", "password")))
            )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.errorCode", equalTo(ApplicationErrorCodes.SecurityInvalidUsernameOrPassword)))

        // check login with wrong password
        mvc.perform(
                post(ApplicationControllerEntryPoints.Authentication + "login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(LoginRequest("admin", "password1")))
            )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.errorCode", equalTo(ApplicationErrorCodes.SecurityInvalidUsernameOrPassword)))

        // check login with correct username and password, then check for the returned response contents
        var contents = mvc.perform(
                post(ApplicationControllerEntryPoints.Authentication + "login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(LoginRequest("admin", "password")))
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken", not(isEmptyOrNullString())))
            .andReturn().response.contentAsString
        var loginResponse = objectMapper.readValue(contents, JwtAuthenticationResponse::class.java)

        // check using token with granted permission
        mvc.perform(get(_groupsEntryPointEn)
                .header("Authorization", loginResponse.tokenType + " " + loginResponse.accessToken)
            )
            .andExpect(status().isOk)

        // login to get the JWT token & check the user is OK
        contents = mvc.perform(
                post(ApplicationControllerEntryPoints.Authentication + "login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequest("user", "password")))
            )
            .andExpect(status().isOk)
            .andReturn().response.contentAsString
        loginResponse = objectMapper.readValue(contents, JwtAuthenticationResponse::class.java)

        // check using token with granted permission
        mvc.perform(get(_rolesEntryPointEn)
                .header("Authorization", loginResponse.tokenType + " " + loginResponse.accessToken)
            )
            .andExpect(status().isOk)

        // check using token with none-granted permission
        mvc.perform(get(_usersEntryPointEn)
            .header("Authorization", loginResponse.tokenType + " " + loginResponse.accessToken)
        )
            .andExpect(status().isForbidden)

        normalUser.accountEnabled = false
        normalUser.accountLocked = false
        normalUser.accountExpired = false
        normalUser = securityUserService.update(normalUser)

        // check to login with disabled user
        mvc.perform(
                post(ApplicationControllerEntryPoints.Authentication + "login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequest("user", "password")))
            )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.errorCode", equalTo(ApplicationErrorCodes.SecurityAccountDisabled)))

        mvc.perform(get(_rolesEntryPointEn)
                .header("Authorization", loginResponse.tokenType + " " + loginResponse.accessToken)
            )
            .andExpect(status().isUnauthorized)

        normalUser.accountEnabled = true
        normalUser.accountLocked = true
        normalUser.accountExpired = false
        normalUser = securityUserService.update(normalUser)

        // check to login with locked user
        mvc.perform(
                post(ApplicationControllerEntryPoints.Authentication + "login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequest("user", "password")))
            )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.errorCode", equalTo(ApplicationErrorCodes.SecurityAccountLocked)))

        mvc.perform(get(_rolesEntryPointEn)
                .header("Authorization", loginResponse.tokenType + " " + loginResponse.accessToken)
            )
            .andExpect(status().isUnauthorized)

        normalUser.accountEnabled = true
        normalUser.accountLocked = false
        normalUser.accountExpired = true
        normalUser = securityUserService.update(normalUser)

        // check to login with expired user
        mvc.perform(
                post(ApplicationControllerEntryPoints.Authentication + "login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequest("user", "password")))
            )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.errorCode", equalTo(ApplicationErrorCodes.SecurityAccountExpired)))

        mvc.perform(get(_rolesEntryPointEn)
                .header("Authorization", loginResponse.tokenType + " " + loginResponse.accessToken)
            )
            .andExpect(status().isUnauthorized)

        // clean up the account
        normalUser.accountEnabled = true
        normalUser.accountLocked = false
        normalUser.accountExpired = false
        normalUser = securityUserService.update(normalUser)


        // check the normal user again
        mvc.perform(
                post(ApplicationControllerEntryPoints.Authentication + "login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequest("user", "password")))
            )
            .andExpect(status().isOk)

        mvc.perform(get(_rolesEntryPointEn)
                .header("Authorization", loginResponse.tokenType + " " + loginResponse.accessToken)
            )
            .andExpect(status().isOk)
    }

}