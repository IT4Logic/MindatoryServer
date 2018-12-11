package com.it4logic.mindatory.tests

import com.fasterxml.jackson.databind.ObjectMapper
import com.it4logic.mindatory.controllers.common.ApplicationControllerEntryPoints
import com.it4logic.mindatory.exceptions.ApplicationErrorCodes
import com.it4logic.mindatory.model.security.SecurityGroup
import com.it4logic.mindatory.model.security.SecurityRole
import com.it4logic.mindatory.model.security.SecurityUser
import com.it4logic.mindatory.security.ApplicationSecurityPermissions
import com.it4logic.mindatory.security.JwtAuthenticationResponse
import com.it4logic.mindatory.security.LoginRequest
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
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthenticationTests {

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

    private lateinit var mvc: MockMvc

    private lateinit var roleAdmin: SecurityRole
    private lateinit var roleUser: SecurityRole

    private lateinit var adminGroup: SecurityGroup
    private lateinit var userGroup: SecurityGroup

    private lateinit var adminUser: SecurityUser
    private lateinit var normalUser: SecurityUser

    @Before
    fun setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply<DefaultMockMvcBuilder>(springSecurity())
                .build()

        setupSecurityData()
    }

    fun setupSecurityData() {
        roleAdmin = securityRoleService.create(SecurityRole("ROLE_ADMIN", "Admins Role",
                permissions = arrayListOf(
                        ApplicationSecurityPermissions.SecurityRoleAdminView,
                        ApplicationSecurityPermissions.SecurityRoleAdminCreate,
                        ApplicationSecurityPermissions.SecurityGroupAdminView
                        )))
        roleUser = securityRoleService.create(SecurityRole("ROLE_USER", "Users Role",
                permissions = arrayListOf(ApplicationSecurityPermissions.SecurityRoleAdminView)))

        adminGroup = securityGroupService.create(SecurityGroup("Admins Group", "Group for Admins"))
        userGroup = securityGroupService.create(SecurityGroup("Users Group", "Group for Users"))

        adminUser = securityUserService.create(SecurityUser("admin", "password", fullName = "Admin User", email = "admin@it4logic.com",
                roles = mutableListOf(roleAdmin), group = adminGroup))

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
        mvc.perform(get(ApplicationControllerEntryPoints.SecurityGroups)
                .header("Authorization", loginResponse.tokenType + " " + loginResponse.accessToken)
            )
            .andExpect(status().isOk)

        // check using token with none-granted permission
        mvc.perform(get(ApplicationControllerEntryPoints.SecurityUsers)
                .header("Authorization", loginResponse.tokenType + " " + loginResponse.accessToken)
            )
            .andExpect(status().isForbidden)


        // login to get the JWT token & check the user is OK
        contents = mvc.perform(
                post(ApplicationControllerEntryPoints.Authentication + "login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequest("user", "password")))
            )
            .andExpect(status().isOk)
            .andReturn().response.contentAsString
        loginResponse = objectMapper.readValue(contents, JwtAuthenticationResponse::class.java)

        mvc.perform(get(ApplicationControllerEntryPoints.SecurityRoles)
                .header("Authorization", loginResponse.tokenType + " " + loginResponse.accessToken)
            )
            .andExpect(status().isOk)

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

        mvc.perform(get(ApplicationControllerEntryPoints.SecurityRoles)
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

        mvc.perform(get(ApplicationControllerEntryPoints.SecurityRoles)
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

        mvc.perform(get(ApplicationControllerEntryPoints.SecurityRoles)
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

        mvc.perform(get(ApplicationControllerEntryPoints.SecurityRoles)
                .header("Authorization", loginResponse.tokenType + " " + loginResponse.accessToken)
            )
            .andExpect(status().isOk)
    }

}