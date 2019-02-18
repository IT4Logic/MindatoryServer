package com.it4logic.mindatory.tests

import com.fasterxml.jackson.databind.ObjectMapper
import com.it4logic.mindatory.controllers.common.ApplicationControllerEntryPoints
import com.it4logic.mindatory.exceptions.ApplicationErrorCodes
import com.it4logic.mindatory.model.ApplicationRepository
import com.it4logic.mindatory.model.ApplicationRepositoryRepository
import com.it4logic.mindatory.model.Solution
import com.it4logic.mindatory.model.mlc.Language
import com.it4logic.mindatory.model.repository.*
import com.it4logic.mindatory.model.security.SecurityGroup
import com.it4logic.mindatory.model.security.SecurityRole
import com.it4logic.mindatory.model.security.SecurityUser
import com.it4logic.mindatory.security.*
import com.it4logic.mindatory.services.LanguageService
import com.it4logic.mindatory.services.repository.ArtifactTemplateService
import com.it4logic.mindatory.services.repository.AttributeTemplateService
import com.it4logic.mindatory.services.repository.StereotypeService
import com.it4logic.mindatory.services.security.SecurityGroupService
import com.it4logic.mindatory.services.security.SecurityRoleService
import com.it4logic.mindatory.services.security.SecurityUserService
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.hamcrest.Matchers.*
import org.junit.Test
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class JoinTemplateTests {
    @Autowired
    private lateinit var context: WebApplicationContext

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var applicationRepositoryRepository: ApplicationRepositoryRepository

    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var securityRoleService: SecurityRoleService

    @Autowired
    private lateinit var securityGroupService: SecurityGroupService

    @Autowired
    private lateinit var securityUserService: SecurityUserService

    @Autowired
    private lateinit var artifactTemplateService: ArtifactTemplateService

    @Autowired
    private lateinit var attributeTemplateService: AttributeTemplateService

    @Autowired
    private lateinit var stereotypeService: StereotypeService

    @Autowired
    private lateinit var languageService: LanguageService

    private lateinit var roleAdmin: SecurityRole
    private lateinit var roleUser: SecurityRole

    private lateinit var adminGroup: SecurityGroup
    private lateinit var userGroup: SecurityGroup

    private lateinit var adminUser: SecurityUser
    private lateinit var normalUser: SecurityUser

    private lateinit var adminLogin: JwtAuthenticationResponse
    private lateinit var userLogin: JwtAuthenticationResponse

    private lateinit var applicationRepository: ApplicationRepository
    private lateinit var solution: Solution

    private lateinit var firstSideArtifactTemplate: ArtifactTemplate
    private lateinit var firstSideArtifactTemplateVersion: ArtifactTemplateVersion
    private lateinit var secondSideArtifactTemplate: ArtifactTemplate
    private lateinit var secondSideArtifactTemplateVersion: ArtifactTemplateVersion

    private lateinit var firstSideStereotype: Stereotype
    private lateinit var secondSideStereotype: Stereotype


    private lateinit var attributeCodeTemplate: AttributeTemplate
    private lateinit var attributeCodeTemplateVersion: AttributeTemplateVersion
    private lateinit var attributeNameTemplate: AttributeTemplate
    private lateinit var attributeNameTemplateVersion: AttributeTemplateVersion
    private lateinit var attributeVersions: MutableList<AttributeTemplateVersion>

    private lateinit var joinTemplate: JoinTemplate

    private val testDataTypeUUID = "19bf955e-00c7-43d6-9b47-d286c20bd0da"

    private val _joinTemplatesEntryPointEn: String = ApplicationControllerEntryPoints.JoinTemplates + "en/"
    private val _repositoriesEntryPoint: String = ApplicationControllerEntryPoints.Repositories + "en/"
    private val _solutionsEntryPointEn: String = ApplicationControllerEntryPoints.Solutions + "en/"
    private val _attributeTemplatesEntryPointEn: String = ApplicationControllerEntryPoints.AttributeTemplates + "en/"
    private val _artifactTemplatesEntryPointEn: String = ApplicationControllerEntryPoints.ArtifactTemplates + "en/"

    @Before
    fun setup() {
        mvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(SecurityMockMvcConfigurers.springSecurity())
            .build()

        setupLanguageData()
        setupSecurityData()
        setupBasicData()
    }

    fun setupLanguageData() {
        languageService.create(Language("en", "English", true))
        languageService.create(Language("ar", "عربي", false))
    }

    fun setupSecurityData() {
        roleAdmin = securityRoleService.create(
            SecurityRole("ROLE_ADMIN", "Admins Role",
                permissions = arrayListOf(
                    ApplicationSecurityPermissions.JoinTemplateAdminView,
                    ApplicationSecurityPermissions.JoinTemplateAdminCreate,
                    ApplicationSecurityPermissions.JoinTemplateAdminModify,
                    ApplicationSecurityPermissions.JoinTemplateAdminDelete,
                    ApplicationSecurityPermissions.ApplicationRepositoryAdminCreate,
                    ApplicationSecurityPermissions.SolutionAdminCreate,
                    ApplicationSecurityPermissions.AttributeTemplateAdminCreate,
                    ApplicationSecurityPermissions.ArtifactTemplateAdminCreate
                ))
        )
        roleUser = securityRoleService.create(SecurityRole("ROLE_USER", "Users Role"))

        adminGroup = securityGroupService.create(SecurityGroup("Admins Group", "Group for Admins"))
        userGroup = securityGroupService.create(SecurityGroup("Users Group", "Group for Users"))

        adminUser = securityUserService.create(
            SecurityUser("admin", "password", fullName = "Admin User", email = "admin@it4logic.com",
                roles = mutableListOf(roleAdmin), group = adminGroup)
        )

        normalUser = securityUserService.create(
            SecurityUser("user", "password", fullName = "Manager User", email = "manager@it4logic.com",
                roles = mutableListOf(roleUser), group = userGroup)
        )

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


    fun setupBasicData() {
        var contents = mvc.perform(
            post(_repositoriesEntryPoint)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ApplicationRepository("ApplicationRepository A")))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name", equalTo("ApplicationRepository A")))
            .andReturn().response.contentAsString
        applicationRepository = objectMapper.readValue(contents, ApplicationRepository::class.java)

        contents = mvc.perform(
            post(_solutionsEntryPointEn)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Solution("Solution A")))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name", equalTo("Solution A")))
            .andReturn().response.contentAsString
        solution = objectMapper.readValue(contents, Solution::class.java)

        // create attributes
        attributeCodeTemplate = AttributeTemplate(identifier = "mindatory.code", name = "Mindatory Code", repository = applicationRepository)
        attributeNameTemplate = AttributeTemplate(identifier = "mindatory.name", name = "Mindatory Name", repository = applicationRepository, solution = solution)

        contents = mvc.perform(
            post(_attributeTemplatesEntryPointEn)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attributeCodeTemplate))
        )
            .andExpect(status().isCreated)
            .andReturn().response.contentAsString

        attributeCodeTemplate = objectMapper.readValue(contents, AttributeTemplate::class.java)
        attributeCodeTemplateVersion = attributeCodeTemplate.createDesignVersion(testDataTypeUUID, hashMapOf(Pair("length", 50), Pair("nullable",false)))
        attributeCodeTemplateVersion = attributeTemplateService.createVersion(attributeCodeTemplate,attributeCodeTemplateVersion)
        attributeCodeTemplateVersion = attributeTemplateService.releaseVersion(attributeCodeTemplate.id,attributeCodeTemplateVersion)

        contents = mvc.perform(
            post(_attributeTemplatesEntryPointEn)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attributeNameTemplate))
        )
            .andExpect(status().isCreated)
            .andReturn().response.contentAsString
        attributeNameTemplate = objectMapper.readValue(contents, AttributeTemplate::class.java)
        attributeNameTemplateVersion = attributeNameTemplate.createDesignVersion(testDataTypeUUID, hashMapOf(Pair("length", 55), Pair("nullable",true)))
        attributeNameTemplateVersion = attributeTemplateService.createVersion(attributeNameTemplate, attributeNameTemplateVersion)
        attributeNameTemplateVersion = attributeTemplateService.releaseVersion(attributeNameTemplate.id, attributeNameTemplateVersion)

        attributeVersions = mutableListOf(attributeCodeTemplateVersion, attributeNameTemplateVersion)

        // first side artifact
        // create artifact
        firstSideArtifactTemplate = ArtifactTemplate(identifier = "mindatory.first-side", name = "Mindatory First Side", repository = applicationRepository)
        contents = mvc.perform(
            post(_artifactTemplatesEntryPointEn)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstSideArtifactTemplate))
        )
            .andExpect(status().isCreated)
            .andReturn().response.contentAsString
        firstSideArtifactTemplate = objectMapper.readValue(contents, ArtifactTemplate::class.java)
        firstSideArtifactTemplateVersion = firstSideArtifactTemplate.createDesignVersion(attributeVersions)
        firstSideArtifactTemplateVersion = artifactTemplateService.createVersion(firstSideArtifactTemplate, firstSideArtifactTemplateVersion)

        // second side artifact
        // create artifact
        secondSideArtifactTemplate = ArtifactTemplate(identifier = "mindatory.second-side", name = "Mindatory Second Side", repository = applicationRepository)
        contents = mvc.perform(
            post(_artifactTemplatesEntryPointEn)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondSideArtifactTemplate))
        )
            .andExpect(status().isCreated)
            .andReturn().response.contentAsString
        secondSideArtifactTemplate = objectMapper.readValue(contents, ArtifactTemplate::class.java)
        secondSideArtifactTemplateVersion = secondSideArtifactTemplate.createDesignVersion(attributeVersions)
        secondSideArtifactTemplateVersion = artifactTemplateService.createVersion(secondSideArtifactTemplate, secondSideArtifactTemplateVersion)

        firstSideStereotype = stereotypeService.create(Stereotype(name = "Bridge", repository = applicationRepository))
        secondSideStereotype = stereotypeService.create(Stereotype(name = "Link", repository = applicationRepository))
    }

    @Test
    fun `Artifact Templates Tests`() {
        basicOperations()
        designVersions()
    }

    fun basicOperations() {
        // create Join Templates
        mvc.perform(post(_joinTemplatesEntryPointEn).with(SecurityMockMvcRequestPostProcessors.anonymous()))
            .andExpect(status().isUnauthorized)

        var contents = mvc.perform(
            post(_joinTemplatesEntryPointEn)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(JoinTemplate(identifier = "mindatory.first-second", repository = applicationRepository)))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.identifier", equalTo("mindatory.first-second")))
            .andReturn().response.contentAsString
        joinTemplate = objectMapper.readValue(contents, JoinTemplate::class.java)

        // duplicate check
        mvc.perform(
            post(_joinTemplatesEntryPointEn)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(JoinTemplate(identifier = "mindatory.first-second", repository = applicationRepository)))
        )
            .andExpect(status().isNotAcceptable)
            .andExpect(jsonPath("$.errorCode", equalTo(ApplicationErrorCodes.DataIntegrityError)))
            .andExpect(jsonPath("$.errorData", anyOf(equalTo(ApplicationErrorCodes.DuplicateJoinTemplateIdentifier))))

        contents = mvc.perform(
            post(_joinTemplatesEntryPointEn)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(JoinTemplate(identifier = "mindatory.dummy", repository = applicationRepository)))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.identifier", equalTo("mindatory.dummy")))
            .andReturn().response.contentAsString
        val joinTemplate2 = objectMapper.readValue(contents, JoinTemplate::class.java)

        // load
        mvc.perform(
            get(_joinTemplatesEntryPointEn + joinTemplate.id)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
        )
            .andExpect(status().isOk)

        mvc.perform(
            get(_joinTemplatesEntryPointEn + joinTemplate.id)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
        )
            .andExpect(status().isForbidden)

        var aclRequest = listOf(ApplicationAclPermissionRequest ("user", listOf(ApplicationPermission.View, ApplicationPermission.Modify)))

        mvc.perform(
            post(_joinTemplatesEntryPointEn + joinTemplate.id + "/permissions/add")
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(aclRequest))
        )
            .andExpect(status().isForbidden)

        mvc.perform(
            post(_joinTemplatesEntryPointEn + joinTemplate.id + "/permissions/add")
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(aclRequest))
        )
            .andExpect(status().isOk)

        mvc.perform(
            get(_joinTemplatesEntryPointEn + joinTemplate.id)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.identifier", equalTo("mindatory.first-second")))

        aclRequest = listOf(ApplicationAclPermissionRequest ("user", listOf(ApplicationPermission.View)))
        mvc.perform(
            post(_joinTemplatesEntryPointEn + joinTemplate.id + "/permissions/remove")
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(aclRequest))
        )
            .andExpect(status().isOk)

        mvc.perform(
            get(_joinTemplatesEntryPointEn + joinTemplate.id)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
        )
            .andExpect(status().isOk    )

        // update
        joinTemplate.description = "updated"

        contents = mvc.perform(
            put(_joinTemplatesEntryPointEn)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(joinTemplate))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.description", equalTo("updated")))
            .andReturn().response.contentAsString
        joinTemplate = objectMapper.readValue(contents, JoinTemplate::class.java)

        // change the owner
        mvc.perform(
            get(_joinTemplatesEntryPointEn + joinTemplate2.id)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
        )
            .andExpect(status().isForbidden)

        mvc.perform(
            post(_joinTemplatesEntryPointEn + joinTemplate2.id + "/permissions/change-owner/user")
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
        )
            .andExpect(status().isOk)

        mvc.perform(
            get(_joinTemplatesEntryPointEn + joinTemplate2.id)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.identifier", equalTo("mindatory.dummy")))

        // delete
        mvc.perform(
            delete(_joinTemplatesEntryPointEn + joinTemplate.id)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
        )
            .andExpect(status().isForbidden)

        mvc.perform(
            delete(_joinTemplatesEntryPointEn + joinTemplate2.id)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
        )
            .andExpect(status().isOk)

        mvc.perform(
            get(_joinTemplatesEntryPointEn + joinTemplate2.id)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
        )
            .andExpect(status().isNotFound)
    }

    fun designVersions() {
        // get design versions
        val joinTemplateDesignVersions = "${_joinTemplatesEntryPointEn}/${joinTemplate.id}/design-versions"

        mvc.perform(get(joinTemplateDesignVersions).with(SecurityMockMvcRequestPostProcessors.anonymous()))
            .andExpect(status().isUnauthorized)

        mvc.perform(
            get(joinTemplateDesignVersions)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", hasSize<Any>(0)))

        // create design version
        var designVersion1 = joinTemplate.createDesignVersion(firstSideStereotype, mutableListOf(firstSideArtifactTemplateVersion),
                                        secondSideStereotype, mutableListOf(secondSideArtifactTemplateVersion))
        var contents = mvc.perform(
            post(joinTemplateDesignVersions)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(designVersion1))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.designVersion", equalTo(1)))
            .andReturn().response.contentAsString
        designVersion1 = objectMapper.readValue(contents, JoinTemplateVersion::class.java)

        // create another design version for the same attribute
        var designVersion2 = joinTemplate.createDesignVersion(firstSideStereotype, mutableListOf(firstSideArtifactTemplateVersion),
                                        secondSideStereotype, mutableListOf(secondSideArtifactTemplateVersion))
        mvc.perform(
            post(joinTemplateDesignVersions)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(designVersion2))
        )
            .andExpect(status().isNotAcceptable)
            .andExpect(jsonPath("$.errorCode", equalTo(ApplicationErrorCodes.ValidationJoinTemplateHasInDesignVersion)))

        // modify the created design version
        contents = mvc.perform(
            put(joinTemplateDesignVersions)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(designVersion1))
        )
            .andExpect(status().isOk)
            .andReturn().response.contentAsString
        designVersion1 = objectMapper.readValue(contents, JoinTemplateVersion::class.java)

        // release the design version
        contents = mvc.perform(
            post("$joinTemplateDesignVersions/${designVersion1.id}/release")
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
        )
            .andExpect(status().isOk)
            .andReturn().response.contentAsString
        designVersion1 = objectMapper.readValue(contents, JoinTemplateVersion::class.java)

        // modify the released version
        mvc.perform(
            put(joinTemplateDesignVersions)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(designVersion1))
        )
            .andExpect(status().isNotAcceptable)
            .andExpect(jsonPath("$.errorCode", equalTo(ApplicationErrorCodes.ValidationCannotChangeReleasedJoinTemplateVersion)))

        // add another design version
        contents = mvc.perform(
            post(joinTemplateDesignVersions)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(designVersion2))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.designVersion", equalTo(2)))
            .andReturn().response.contentAsString
        designVersion2 = objectMapper.readValue(contents, JoinTemplateVersion::class.java)

        // delete the released design version
        mvc.perform(
            delete("$joinTemplateDesignVersions/${designVersion1.id}")
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
        )
            .andExpect(status().isOk)

        // delete the non-released design version
        mvc.perform(
            delete("$joinTemplateDesignVersions/${designVersion2.id}")
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
        )
            .andExpect(status().isOk)
    }
}