package com.it4logic.mindatory.tests

import com.fasterxml.jackson.databind.ObjectMapper
import com.it4logic.mindatory.controllers.common.ApplicationControllerEntryPoints
import com.it4logic.mindatory.exceptions.ApplicationErrorCodes
import com.it4logic.mindatory.model.ApplicationRepository
import com.it4logic.mindatory.model.Solution
import com.it4logic.mindatory.model.mlc.Language
import com.it4logic.mindatory.model.security.SecurityGroup
import com.it4logic.mindatory.model.security.SecurityRole
import com.it4logic.mindatory.model.security.SecurityUser
import com.it4logic.mindatory.security.*
import com.it4logic.mindatory.services.LanguageService
import com.it4logic.mindatory.services.SolutionService
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
class ApplicationRepositoryTests {

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
    private lateinit var solutionService: SolutionService

    private lateinit var mvc: MockMvc

    private lateinit var roleAdmin: SecurityRole
    private lateinit var roleUser: SecurityRole

    private lateinit var adminGroup: SecurityGroup
    private lateinit var userGroup: SecurityGroup

    private lateinit var adminUser: SecurityUser
    private lateinit var normalUser: SecurityUser

    private lateinit var adminLogin: JwtAuthenticationResponse
    private lateinit var userLogin: JwtAuthenticationResponse

    private lateinit var solution1: Solution

    @Autowired
    private lateinit var languageService: LanguageService

    private val _solutionsEntryPoint: String = ApplicationControllerEntryPoints.Solutions + "en/"
    private val _repositoriesEntryPoint: String = ApplicationControllerEntryPoints.Repositories + "en/"

    @Before
    fun setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply<DefaultMockMvcBuilder>(springSecurity())
                .build()

        setupLanguageData()
        setupSecurityData()
        setupSolutionData()
    }

    fun setupLanguageData() {
        languageService.create(Language("en", "English", true))
        languageService.create(Language("ar", "عربي", false))
    }

    fun setupSecurityData() {
        roleAdmin = securityRoleService.create(SecurityRole("ROLE_ADMIN", "Admins Role",
                permissions = arrayListOf(
                        ApplicationSecurityPermissions.SolutionAdminCreate,
                        ApplicationSecurityPermissions.ApplicationRepositoryAdminView,
                        ApplicationSecurityPermissions.ApplicationRepositoryAdminCreate,
                        ApplicationSecurityPermissions.ApplicationRepositoryAdminModify,
                        ApplicationSecurityPermissions.ApplicationRepositoryAdminDelete
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

    fun setupSolutionData() {
        val contents = mvc.perform(
            post(_solutionsEntryPoint)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Solution("Solution A")))
            )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name", equalTo("Solution A")))
            .andReturn().response.contentAsString
        solution1 = objectMapper.readValue(contents, Solution::class.java)
    }

    @Test
    fun `ApplicationRepositories Management`() {
        var applicationRepository1 = ApplicationRepository("ApplicationRepository A", solution = solution1)
        var applicationRepository2 = ApplicationRepository("ApplicationRepository B")
        val applicationRepository3 = ApplicationRepository("ApplicationRepository B")

        // create applicationRepositories
        mvc.perform(post(_repositoriesEntryPoint).with(anonymous()))
            .andExpect(status().isUnauthorized)

        var contents = mvc.perform(
            post(_repositoriesEntryPoint)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(applicationRepository1))
            )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name", equalTo("ApplicationRepository A")))
            .andReturn().response.contentAsString
        applicationRepository1 = objectMapper.readValue(contents, ApplicationRepository::class.java)

        contents = mvc.perform(
            post(_repositoriesEntryPoint)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(applicationRepository2))
            )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name", equalTo("ApplicationRepository B")))
            .andReturn().response.contentAsString
        applicationRepository2 = objectMapper.readValue(contents, ApplicationRepository::class.java)

        // duplicate check
        mvc.perform(
            post(_repositoriesEntryPoint)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(applicationRepository2))
            )
            .andExpect(status().isNotAcceptable)
            .andExpect(jsonPath("$.errorCode", equalTo(ApplicationErrorCodes.ValidationCannotCreateObjectWithExistingId)))

        mvc.perform(
            post(_repositoriesEntryPoint)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(applicationRepository3))
            )
            .andExpect(status().isNotAcceptable)
            .andExpect(jsonPath("$.errorCode", equalTo(ApplicationErrorCodes.DataIntegrityError)))
            .andExpect(jsonPath("$.errorData", equalTo(ApplicationErrorCodes.DuplicateApplicationRepositoryName)))

        // load applicationRepository
        mvc.perform(
            get(_repositoriesEntryPoint + applicationRepository1.id)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
            )
            .andExpect(status().isOk)

        mvc.perform(
            post(_repositoriesEntryPoint)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(applicationRepository2))
            )
            .andExpect(status().isForbidden)

        mvc.perform(
            get(_repositoriesEntryPoint + applicationRepository1.id)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
            )
            .andExpect(status().isForbidden)

        var aclRequest = listOf(ApplicationAclPermissionRequest ("user", listOf(ApplicationPermission.View, ApplicationPermission.Modify)))
        mvc.perform(
            post(_repositoriesEntryPoint + applicationRepository1.id + "/permissions/add")
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(aclRequest))
            )
            .andExpect(status().isForbidden)

        mvc.perform(
            post(_repositoriesEntryPoint + applicationRepository1.id + "/permissions/add")
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(aclRequest))
            )
            .andExpect(status().isOk)

        mvc.perform(
            get(_repositoriesEntryPoint + applicationRepository1.id)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name", equalTo("ApplicationRepository A")))

        aclRequest = listOf(ApplicationAclPermissionRequest ("user", listOf(ApplicationPermission.View)))
        mvc.perform(
            post(_repositoriesEntryPoint + applicationRepository1.id + "/permissions/remove")
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(aclRequest))
            )
            .andExpect(status().isOk)

        mvc.perform(
            get(_repositoriesEntryPoint + applicationRepository1.id)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
            )
            .andExpect(status().isOk)

        // update
        applicationRepository1.description = "updated"

        contents = mvc.perform(
            put(_repositoriesEntryPoint)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(applicationRepository1))
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.description", equalTo("updated")))
            .andReturn().response.contentAsString
        applicationRepository1 = objectMapper.readValue(contents, ApplicationRepository::class.java)

        // change the owner
        mvc.perform(
            get(_repositoriesEntryPoint + applicationRepository2.id)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
            )
            .andExpect(status().isForbidden)

        mvc.perform(
            post(_repositoriesEntryPoint + applicationRepository2.id + "/permissions/change-owner/user")
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
            )
            .andExpect(status().isOk)

        mvc.perform(
            get(_repositoriesEntryPoint + applicationRepository2.id)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name", equalTo("ApplicationRepository B")))

        // delete
        mvc.perform(
            delete(_repositoriesEntryPoint + applicationRepository1.id)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
            )
            .andExpect(status().isForbidden)

        mvc.perform(
            delete(_repositoriesEntryPoint + applicationRepository2.id)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
            )
            .andExpect(status().isOk)

        mvc.perform(
            get(_repositoriesEntryPoint + applicationRepository2.id)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
            )
            .andExpect(status().isNotFound)

    }

}