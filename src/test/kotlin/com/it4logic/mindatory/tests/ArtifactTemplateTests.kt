package com.it4logic.mindatory.tests

import com.fasterxml.jackson.databind.ObjectMapper
import com.it4logic.mindatory.controllers.common.ApplicationControllerEntryPoints
import com.it4logic.mindatory.exceptions.ApplicationErrorCodes
import com.it4logic.mindatory.model.ApplicationRepository
import com.it4logic.mindatory.model.ApplicationRepositoryRepository
import com.it4logic.mindatory.model.Solution
import com.it4logic.mindatory.model.common.DesignStatus
import com.it4logic.mindatory.model.repository.ArtifactTemplate
import com.it4logic.mindatory.model.repository.ArtifactTemplateVersion
import com.it4logic.mindatory.model.repository.AttributeTemplate
import com.it4logic.mindatory.model.repository.AttributeTemplateVersion
import com.it4logic.mindatory.model.security.SecurityGroup
import com.it4logic.mindatory.model.security.SecurityRole
import com.it4logic.mindatory.model.security.SecurityUser
import com.it4logic.mindatory.security.*
import com.it4logic.mindatory.services.repository.AttributeTemplateService
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
class ArtifactTemplateTests {
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
    private lateinit var attributeTemplateService: AttributeTemplateService

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
    private lateinit var artifactTemplate: ArtifactTemplate
    private lateinit var artifactTemplateVersion: ArtifactTemplateVersion

    private lateinit var attributeCodeTemplate: AttributeTemplate
    private lateinit var attributeCodeTemplateVersion: AttributeTemplateVersion
    private lateinit var attributeNameTemplate: AttributeTemplate
    private lateinit var attributeNameTemplateVersion: AttributeTemplateVersion
    private lateinit var attributeDescTemplate: AttributeTemplate
    private lateinit var attributeDescTemplateVersion: AttributeTemplateVersion
    private lateinit var attributeVersions: MutableList<AttributeTemplateVersion>

    private val testDataTypeUUID = "19bf955e-00c7-43d6-9b47-d286c20bd0da"

    @Before
    fun setup() {
        mvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(SecurityMockMvcConfigurers.springSecurity())
            .build()

        setupSecurityData()
        setupBasicData()
    }

    fun setupSecurityData() {
        roleAdmin = securityRoleService.create(
            SecurityRole("ROLE_ADMIN", "Admins Role",
                permissions = arrayListOf(
                    ApplicationSecurityPermissions.ArtifactTemplateAdminView,
                    ApplicationSecurityPermissions.ArtifactTemplateAdminCreate,
                    ApplicationSecurityPermissions.ArtifactTemplateAdminModify,
                    ApplicationSecurityPermissions.ArtifactTemplateAdminDelete,
                    ApplicationSecurityPermissions.ApplicationRepositoryAdminCreate,
                    ApplicationSecurityPermissions.SolutionAdminCreate
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
            post(ApplicationControllerEntryPoints.Repositories)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ApplicationRepository("ApplicationRepository A")))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name", equalTo("ApplicationRepository A")))
            .andReturn().response.contentAsString
        applicationRepository = objectMapper.readValue(contents, ApplicationRepository::class.java)

        contents = mvc.perform(
            post(ApplicationControllerEntryPoints.Solutions)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Solution("Solution A")))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name", equalTo("Solution A")))
            .andReturn().response.contentAsString
        solution = objectMapper.readValue(contents, Solution::class.java)


        attributeCodeTemplate = attributeTemplateService.create(AttributeTemplate(identifier = "mindatory.code", name = "Mindatory Code", repository = applicationRepository))
        attributeCodeTemplateVersion = attributeCodeTemplate.createDesignVersion(testDataTypeUUID, hashMapOf(Pair("length", 50), Pair("nullable",false)))
        attributeCodeTemplateVersion.designStatus = DesignStatus.Released
        attributeCodeTemplateVersion = attributeTemplateService.createVersion(attributeCodeTemplate,attributeCodeTemplateVersion)
        attributeCodeTemplateVersion = attributeTemplateService.releaseVersion(attributeCodeTemplate.id,attributeCodeTemplateVersion)

        attributeNameTemplate = attributeTemplateService.create(AttributeTemplate(identifier = "mindatory.name", name = "Mindatory Name", repository = applicationRepository))
        attributeNameTemplateVersion = attributeNameTemplate.createDesignVersion(testDataTypeUUID, hashMapOf(Pair("length", 55), Pair("nullable",true)))
        attributeNameTemplateVersion.designStatus = DesignStatus.Released
        attributeNameTemplateVersion = attributeTemplateService.createVersion(attributeNameTemplate, attributeNameTemplateVersion)
        attributeNameTemplateVersion = attributeTemplateService.releaseVersion(attributeNameTemplate.id, attributeNameTemplateVersion)

        attributeDescTemplate = attributeTemplateService.create(AttributeTemplate(identifier = "mindatory.desc", name = "Mindatory Desc", repository = applicationRepository))
        attributeDescTemplateVersion = attributeDescTemplate.createDesignVersion(testDataTypeUUID,  hashMapOf(Pair("length", 255), Pair("nullable",true)))
        attributeDescTemplateVersion = attributeTemplateService.createVersion(attributeDescTemplate, attributeDescTemplateVersion)
        attributeDescTemplateVersion = attributeTemplateService.releaseVersion(attributeDescTemplate.id, attributeDescTemplateVersion)

        attributeVersions = mutableListOf(attributeCodeTemplateVersion, attributeNameTemplateVersion)
    }

    @Test
    fun `Artifact Templates Tests`() {
        basicOperations()
        designVersions()
        attributes()
    }

    fun basicOperations() {
        artifactTemplate = ArtifactTemplate(identifier = "mindatory.code", name = "Mindatory Code", repository = applicationRepository)
        var attributeTemplate2 = ArtifactTemplate(identifier = "mindatory.name", name = "Mindatory Name", repository = applicationRepository, solution = solution)
        val attributeTemplate3 = ArtifactTemplate(identifier = "mindatory.name", name = "Mindatory Name", repository = applicationRepository)

        // create Attribute Templates
        mvc.perform(post(ApplicationControllerEntryPoints.ArtifactTemplates).with(SecurityMockMvcRequestPostProcessors.anonymous()))
            .andExpect(status().isUnauthorized)

        var contents = mvc.perform(
            post(ApplicationControllerEntryPoints.ArtifactTemplates)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(artifactTemplate))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.identifier", equalTo("mindatory.code")))
            .andReturn().response.contentAsString
        artifactTemplate = objectMapper.readValue(contents, ArtifactTemplate::class.java)

        contents = mvc.perform(
            post(ApplicationControllerEntryPoints.ArtifactTemplates)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attributeTemplate2))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.identifier", equalTo("mindatory.name")))
            .andReturn().response.contentAsString
        attributeTemplate2 = objectMapper.readValue(contents, ArtifactTemplate::class.java)

        // duplicate check
        mvc.perform(
            post(ApplicationControllerEntryPoints.ArtifactTemplates)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attributeTemplate2))
        )
            .andExpect(status().isNotAcceptable)
            .andExpect(jsonPath("$.errorCode", equalTo(ApplicationErrorCodes.ValidationCannotCreateObjectWithExistingId)))

        mvc.perform(
            post(ApplicationControllerEntryPoints.ArtifactTemplates)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attributeTemplate3))
        )
            .andExpect(status().isNotAcceptable)
            .andExpect(jsonPath("$.errorCode", equalTo(ApplicationErrorCodes.DataIntegrityError)))
            .andExpect(jsonPath("$.errorData", anyOf(
                equalTo(ApplicationErrorCodes.DuplicateArtifactTemplateIdentification),
                equalTo(ApplicationErrorCodes.DuplicateArtifactTemplateName))))

        // load solution
        mvc.perform(
            get(ApplicationControllerEntryPoints.ArtifactTemplates + artifactTemplate.id)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
        )
            .andExpect(status().isOk)

        mvc.perform(
            post(ApplicationControllerEntryPoints.ArtifactTemplates)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attributeTemplate2))
        )
            .andExpect(status().isForbidden)

        mvc.perform(
            get(ApplicationControllerEntryPoints.ArtifactTemplates + artifactTemplate.id)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
        )
            .andExpect(status().isForbidden)

        var aclRequest = listOf(ApplicationAclPermissionRequest ("user", listOf(ApplicationPermission.View, ApplicationPermission.Modify)))

        mvc.perform(
            post(ApplicationControllerEntryPoints.ArtifactTemplates + artifactTemplate.id + "/permissions/add")
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(aclRequest))
        )
            .andExpect(status().isForbidden)

        mvc.perform(
            post(ApplicationControllerEntryPoints.ArtifactTemplates + artifactTemplate.id + "/permissions/add")
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(aclRequest))
        )
            .andExpect(status().isOk)

        mvc.perform(
            get(ApplicationControllerEntryPoints.ArtifactTemplates + artifactTemplate.id)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.identifier", equalTo("mindatory.code")))

        aclRequest = listOf(ApplicationAclPermissionRequest ("user", listOf(ApplicationPermission.View)))
        mvc.perform(
            post(ApplicationControllerEntryPoints.ArtifactTemplates + artifactTemplate.id + "/permissions/remove")
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(aclRequest))
        )
            .andExpect(status().isOk)

        mvc.perform(
            get(ApplicationControllerEntryPoints.ArtifactTemplates + artifactTemplate.id)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
        )
            .andExpect(status().isForbidden)

        // update
        artifactTemplate.description = "updated"

        contents = mvc.perform(
            put(ApplicationControllerEntryPoints.ArtifactTemplates)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(artifactTemplate))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.description", equalTo("updated")))
            .andReturn().response.contentAsString
        artifactTemplate = objectMapper.readValue(contents, ArtifactTemplate::class.java)

        // change the owner
        mvc.perform(
            get(ApplicationControllerEntryPoints.ArtifactTemplates + attributeTemplate2.id)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
        )
            .andExpect(status().isForbidden)

        mvc.perform(
            post(ApplicationControllerEntryPoints.ArtifactTemplates + attributeTemplate2.id + "/permissions/change-owner/user")
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
        )
            .andExpect(status().isOk)

        mvc.perform(
            get(ApplicationControllerEntryPoints.ArtifactTemplates + attributeTemplate2.id)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.identifier", equalTo("mindatory.name")))

        // delete
        mvc.perform(
            delete(ApplicationControllerEntryPoints.ArtifactTemplates + artifactTemplate.id)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
        )
            .andExpect(status().isForbidden)

        mvc.perform(
            delete(ApplicationControllerEntryPoints.ArtifactTemplates + attributeTemplate2.id)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
        )
            .andExpect(status().isOk)

        mvc.perform(
            get(ApplicationControllerEntryPoints.ArtifactTemplates + attributeTemplate2.id)
                .header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
        )
            .andExpect(status().isNotFound)
    }

    fun designVersions() {
        // get design versions
        val attributeTemplateDesignVersions = "${ApplicationControllerEntryPoints.ArtifactTemplates}/${artifactTemplate.id}/design-versions"

        mvc.perform(get(attributeTemplateDesignVersions).with(SecurityMockMvcRequestPostProcessors.anonymous()))
            .andExpect(status().isUnauthorized)

        mvc.perform(
            get(attributeTemplateDesignVersions)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", hasSize<Any>(0)))

        // create design version
        var designVersion1 = artifactTemplate.createDesignVersion(attributeVersions)
        var contents = mvc.perform(
            post(attributeTemplateDesignVersions)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(designVersion1))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.designVersion", equalTo(1)))
            .andReturn().response.contentAsString
        designVersion1 = objectMapper.readValue(contents, ArtifactTemplateVersion::class.java)

        // create another design version for the same attribute
        var designVersion2 = artifactTemplate.createDesignVersion(attributeVersions)
        mvc.perform(
            post(attributeTemplateDesignVersions)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(designVersion2))
        )
            .andExpect(status().isNotAcceptable)
            .andExpect(jsonPath("$.errorCode", equalTo(ApplicationErrorCodes.ValidationArtifactTemplateHasInDesignVersion)))

        // modify the created design version
        contents = mvc.perform(
            put(attributeTemplateDesignVersions)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(designVersion1))
        )
            .andExpect(status().isOk)
            .andReturn().response.contentAsString
        designVersion1 = objectMapper.readValue(contents, ArtifactTemplateVersion::class.java)

        // release the design version
        contents = mvc.perform(
            post("$attributeTemplateDesignVersions/${designVersion1.id}/release")
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
        )
            .andExpect(status().isOk)
            .andReturn().response.contentAsString
        designVersion1 = objectMapper.readValue(contents, ArtifactTemplateVersion::class.java)

        // modify the released version
        mvc.perform(
            put(attributeTemplateDesignVersions)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(designVersion1))
        )
            .andExpect(status().isNotAcceptable)
            .andExpect(jsonPath("$.errorCode", equalTo(ApplicationErrorCodes.ValidationCannotChangeReleasedArtifactTemplateVersion)))

        // add another design version
        contents = mvc.perform(
            post(attributeTemplateDesignVersions)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(designVersion2))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.designVersion", equalTo(2)))
            .andReturn().response.contentAsString
        designVersion2 = objectMapper.readValue(contents, ArtifactTemplateVersion::class.java)

        // delete the released design version
        mvc.perform(
            delete("$attributeTemplateDesignVersions/${designVersion1.id}")
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
        )
            .andExpect(status().isOk)

        // delete the non-released design version
        mvc.perform(
            delete("$attributeTemplateDesignVersions/${designVersion2.id}")
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
        )
            .andExpect(status().isOk)

        contents = mvc.perform(
            post(attributeTemplateDesignVersions)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(artifactTemplate.createDesignVersion(mutableListOf())))
        )
            .andExpect(status().isCreated)
            .andReturn().response.contentAsString
        artifactTemplateVersion = objectMapper.readValue(contents, ArtifactTemplateVersion::class.java)
    }

    fun attributes() {
        val attributeTemplateDesignVersion = "${ApplicationControllerEntryPoints.ArtifactTemplates}/${artifactTemplate.id}/design-versions/${artifactTemplateVersion.id}/attributes"

        // get attributes count
        mvc.perform(get(attributeTemplateDesignVersion).with(SecurityMockMvcRequestPostProcessors.anonymous()))
            .andExpect(status().isUnauthorized)

        mvc.perform(
            get(attributeTemplateDesignVersion)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", hasSize<Any>(0)))

        // add attributes
        mvc.perform(
            post("$attributeTemplateDesignVersion/add")
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(listOf(attributeCodeTemplateVersion.id, attributeNameTemplateVersion.id)))
        )
            .andExpect(status().isOk)

        // get attributes count after adding
        mvc.perform(
            get(attributeTemplateDesignVersion)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", hasSize<Any>(2)))

        // remove attribute
        mvc.perform(
            post("$attributeTemplateDesignVersion/remove")
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(listOf(attributeNameTemplateVersion.id)))
        )
            .andExpect(status().isOk)

        // get attributes count after removing
        mvc.perform(
            get(attributeTemplateDesignVersion)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", hasSize<Any>(1)))

        // add already added attribute
        mvc.perform(
            post("$attributeTemplateDesignVersion/add")
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(listOf(attributeCodeTemplateVersion.id)))
        )
            .andExpect(status().isNotAcceptable)
            .andExpect(jsonPath("$.errorCode", equalTo(ApplicationErrorCodes.ValidationAttributeAlreadyAddedToThisArtifactTemplateVersion)))

        // add another attribute
        mvc.perform(
            post("$attributeTemplateDesignVersion/add")
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(listOf(attributeDescTemplateVersion.id)))
        )
            .andExpect(status().isOk)

        // get attributes count after removing
        mvc.perform(
            get(attributeTemplateDesignVersion)
                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", hasSize<Any>(2)))
    }
}