package com.it4logic.mindatory.tests

import com.fasterxml.jackson.databind.ObjectMapper
import com.it4logic.mindatory.controllers.ApplicationControllerEntryPoints
import com.it4logic.mindatory.exceptions.ApplicationErrorCodes
import com.it4logic.mindatory.model.mlc.Language
import com.it4logic.mindatory.model.security.SecurityGroup
import com.it4logic.mindatory.model.security.SecurityRole
import com.it4logic.mindatory.model.security.SecurityUser
import com.it4logic.mindatory.security.*
import com.it4logic.mindatory.services.LanguageService
import com.it4logic.mindatory.services.model.AttributeTemplateService
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

	private lateinit var mvc: MockMvc

	@Autowired
	private lateinit var securityRoleService: SecurityRoleService

	@Autowired
	private lateinit var securityGroupService: SecurityGroupService

	@Autowired
	private lateinit var securityUserService: SecurityUserService

	@Autowired
	private lateinit var attributeTemplateService: AttributeTemplateService

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

	private lateinit var applicationRepository: ApplicationRepositoryTest
	private lateinit var project: ProjectTest
	private lateinit var artifactTemplate: ArtifactTemplateTest
	private lateinit var artifactTemplateVersion: ArtifactTemplateVersionTest

	private lateinit var attributeCodeTemplate: AttributeTemplateTest
	private lateinit var attributeCodeTemplateVersion: AttributeTemplateVersionTest
	private lateinit var attributeNameTemplate: AttributeTemplateTest
	private lateinit var attributeNameTemplateVersion: AttributeTemplateVersionTest
	private lateinit var attributeDescTemplate: AttributeTemplateTest
	private lateinit var attributeDescTemplateVersion: AttributeTemplateVersionTest
	private lateinit var attributeVersions: MutableList<AttributeTemplateVersionTest>

	private val testDataTypeUUID = "19bf955e-00c7-43d6-9b47-d286c20bd0da"

	private val _artifactTemplatesEntryPointEn: String = ApplicationControllerEntryPoints.ArtifactTemplates + "en/"
	private val _attributeTemplatesEntryPointEn: String = ApplicationControllerEntryPoints.AttributeTemplates + "en/"
	private val _repositoriesEntryPoint: String = ApplicationControllerEntryPoints.Models + "en/"
	private val _projectsEntryPointEn: String = ApplicationControllerEntryPoints.Projects + "en/"

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
			SecurityRole(
				"ROLE_ADMIN", "Admins Role",
				permissions = arrayListOf(
					ApplicationSecurityPermissions.ArtifactTemplateAdminView,
					ApplicationSecurityPermissions.ArtifactTemplateAdminCreate,
					ApplicationSecurityPermissions.ArtifactTemplateAdminModify,
					ApplicationSecurityPermissions.ArtifactTemplateAdminDelete,
					ApplicationSecurityPermissions.AttributeTemplateAdminCreate,
					ApplicationSecurityPermissions.ModelAdminCreate,
					ApplicationSecurityPermissions.ProjectAdminCreate
				)
			)
		)
		roleUser = securityRoleService.create(SecurityRole("ROLE_USER", "Users Role"))

		adminGroup = securityGroupService.create(SecurityGroup("Admins Group", "Group for Admins"))
		userGroup = securityGroupService.create(SecurityGroup("Users Group", "Group for Users"))

		adminUser = securityUserService.create(
			SecurityUser(
				"admin", "password", fullName = "Admin User", email = "admin@it4logic.com",
				roles = mutableListOf(roleAdmin), group = adminGroup
			)
		)

		normalUser = securityUserService.create(
			SecurityUser(
				"user", "password", fullName = "Manager User", email = "manager@it4logic.com",
				roles = mutableListOf(roleUser), group = userGroup
			)
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
				.content(objectMapper.writeValueAsString(ApplicationRepositoryTest("Model A")))
		)
			.andExpect(status().isCreated)
			.andExpect(jsonPath("$.name", equalTo("Model A")))
			.andReturn().response.contentAsString
		applicationRepository = objectMapper.readValue(contents, ApplicationRepositoryTest::class.java)

		contents = mvc.perform(
			post(_projectsEntryPointEn)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(ProjectTest("Project A")))
		)
			.andExpect(status().isCreated)
			.andExpect(jsonPath("$.name", equalTo("Project A")))
			.andReturn().response.contentAsString
		project = objectMapper.readValue(contents, ProjectTest::class.java)


		contents = mvc.perform(
			post(_attributeTemplatesEntryPointEn)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					objectMapper.writeValueAsString(
						AttributeTemplateTest(
							identifier = "mindatory.code",
							name = "Mindatory Code",
							repository = applicationRepository
						)
					)
				)
		)
			.andExpect(status().isCreated)
			.andExpect(jsonPath("$.identifier", equalTo("mindatory.code")))
			.andReturn().response.contentAsString
		attributeCodeTemplate = objectMapper.readValue(contents, AttributeTemplateTest::class.java)

		attributeCodeTemplateVersion = AttributeTemplateVersionTest(
			attributeCodeTemplate,
			testDataTypeUUID,
			hashMapOf(Pair("length", 50), Pair("nullable", false))
		)
		contents = mvc.perform(
			post("$_attributeTemplatesEntryPointEn/${attributeCodeTemplate.id}/design-versions")
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(attributeCodeTemplateVersion))
		)
			.andExpect(status().isCreated)
			.andExpect(jsonPath("$.designVersion", equalTo(1)))
			.andReturn().response.contentAsString
		attributeCodeTemplateVersion = objectMapper.readValue(contents, AttributeTemplateVersionTest::class.java)

		contents = mvc.perform(
			post("$_attributeTemplatesEntryPointEn/${attributeCodeTemplate.id}/design-versions/${attributeCodeTemplateVersion.id}/release")
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
		)
			.andExpect(status().isOk)
			.andReturn().response.contentAsString
		attributeCodeTemplateVersion = objectMapper.readValue(contents, AttributeTemplateVersionTest::class.java)


		contents = mvc.perform(
			post(_attributeTemplatesEntryPointEn)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					objectMapper.writeValueAsString(
						AttributeTemplateTest(
							identifier = "mindatory.name",
							name = "Mindatory Name",
							repository = applicationRepository
						)
					)
				)
		)
			.andExpect(status().isCreated)
			.andExpect(jsonPath("$.identifier", equalTo("mindatory.name")))
			.andReturn().response.contentAsString
		attributeNameTemplate = objectMapper.readValue(contents, AttributeTemplateTest::class.java)

		attributeNameTemplateVersion = AttributeTemplateVersionTest(
			attributeNameTemplate,
			testDataTypeUUID,
			hashMapOf(Pair("length", 55), Pair("nullable", true))
		)
		contents = mvc.perform(
			post("$_attributeTemplatesEntryPointEn/${attributeNameTemplate.id}/design-versions")
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(attributeNameTemplateVersion))
		)
			.andExpect(status().isCreated)
			.andExpect(jsonPath("$.designVersion", equalTo(1)))
			.andReturn().response.contentAsString
		attributeNameTemplateVersion = objectMapper.readValue(contents, AttributeTemplateVersionTest::class.java)

		contents = mvc.perform(
			post("$_attributeTemplatesEntryPointEn/${attributeNameTemplate.id}/design-versions/${attributeNameTemplateVersion.id}/release")
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
		)
			.andExpect(status().isOk)
			.andReturn().response.contentAsString
		attributeNameTemplateVersion = objectMapper.readValue(contents, AttributeTemplateVersionTest::class.java)


		contents = mvc.perform(
			post(_attributeTemplatesEntryPointEn)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					objectMapper.writeValueAsString(
						AttributeTemplateTest(
							identifier = "mindatory.desc",
							name = "Mindatory Desc",
							repository = applicationRepository
						)
					)
				)
		)
			.andExpect(status().isCreated)
			.andExpect(jsonPath("$.identifier", equalTo("mindatory.desc")))
			.andReturn().response.contentAsString
		attributeDescTemplate = objectMapper.readValue(contents, AttributeTemplateTest::class.java)

		attributeDescTemplateVersion = AttributeTemplateVersionTest(
			attributeDescTemplate,
			testDataTypeUUID,
			hashMapOf(Pair("length", 255), Pair("nullable", true))
		)
		contents = mvc.perform(
			post("$_attributeTemplatesEntryPointEn/${attributeDescTemplate.id}/design-versions")
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(attributeDescTemplateVersion))
		)
			.andExpect(status().isCreated)
			.andExpect(jsonPath("$.designVersion", equalTo(1)))
			.andReturn().response.contentAsString
		attributeDescTemplateVersion = objectMapper.readValue(contents, AttributeTemplateVersionTest::class.java)

		contents = mvc.perform(
			post("$_attributeTemplatesEntryPointEn/${attributeDescTemplate.id}/design-versions/${attributeDescTemplateVersion.id}/release")
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
		)
			.andExpect(status().isOk)
			.andReturn().response.contentAsString
		attributeDescTemplateVersion = objectMapper.readValue(contents, AttributeTemplateVersionTest::class.java)

		attributeVersions = mutableListOf(attributeCodeTemplateVersion, attributeNameTemplateVersion)
	}

	@Test
	fun `Artifact Templates Tests`() {
		basicOperations()
		designVersions()
//        attributes()
	}

	fun basicOperations() {
		artifactTemplate = ArtifactTemplateTest(
			identifier = "mindatory.code",
			name = "Mindatory Code",
			repository = applicationRepository
		)
		var attributeTemplate2 = ArtifactTemplateTest(
			identifier = "mindatory.name",
			name = "Mindatory Name",
			repository = applicationRepository,
			project = project
		)
		val attributeTemplate3 = ArtifactTemplateTest(
			identifier = "mindatory.name",
			name = "Mindatory Name",
			repository = applicationRepository
		)

		// create Attribute Templates
		mvc.perform(post(_artifactTemplatesEntryPointEn).with(SecurityMockMvcRequestPostProcessors.anonymous()))
			.andExpect(status().isUnauthorized)

		var contents = mvc.perform(
			post(_artifactTemplatesEntryPointEn)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(artifactTemplate))
		)
			.andExpect(status().isCreated)
			.andExpect(jsonPath("$.identifier", equalTo("mindatory.code")))
			.andReturn().response.contentAsString
		artifactTemplate = objectMapper.readValue(contents, ArtifactTemplateTest::class.java)

		contents = mvc.perform(
			post(_artifactTemplatesEntryPointEn)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(attributeTemplate2))
		)
			.andExpect(status().isCreated)
			.andExpect(jsonPath("$.identifier", equalTo("mindatory.name")))
			.andReturn().response.contentAsString
		attributeTemplate2 = objectMapper.readValue(contents, ArtifactTemplateTest::class.java)

		// duplicate check
		mvc.perform(
			post(_artifactTemplatesEntryPointEn)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(attributeTemplate2))
		)
			.andExpect(status().isNotAcceptable)
			.andExpect(
				jsonPath(
					"$.errorCode",
					equalTo(ApplicationErrorCodes.ValidationCannotCreateObjectWithExistingId)
				)
			)

		mvc.perform(
			post(_artifactTemplatesEntryPointEn)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(attributeTemplate3))
		)
			.andExpect(status().isNotAcceptable)
			.andExpect(jsonPath("$.errorCode", equalTo(ApplicationErrorCodes.DataIntegrityError)))
			.andExpect(
				jsonPath(
					"$.errorData", anyOf(
						equalTo(ApplicationErrorCodes.DuplicateArtifactTemplateIdentification),
						equalTo(ApplicationErrorCodes.DuplicateArtifactTemplateName)
					)
				)
			)

		// load project
		mvc.perform(
			get(_artifactTemplatesEntryPointEn + artifactTemplate.id)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
		)
			.andExpect(status().isOk)

		mvc.perform(
			post(_artifactTemplatesEntryPointEn)
				.header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(attributeTemplate2))
		)
			.andExpect(status().isForbidden)

		mvc.perform(
			get(_artifactTemplatesEntryPointEn + artifactTemplate.id)
				.header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
		)
			.andExpect(status().isForbidden)

		var aclRequest = listOf(
			ApplicationAclPermissionRequest(
				"user",
				listOf(ApplicationPermission.View, ApplicationPermission.Modify)
			)
		)

		mvc.perform(
			post(_artifactTemplatesEntryPointEn + artifactTemplate.id + "/permissions/add")
				.header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(aclRequest))
		)
			.andExpect(status().isForbidden)

		mvc.perform(
			post(_artifactTemplatesEntryPointEn + artifactTemplate.id + "/permissions/add")
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(aclRequest))
		)
			.andExpect(status().isOk)

		mvc.perform(
			get(_artifactTemplatesEntryPointEn + artifactTemplate.id)
				.header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.identifier", equalTo("mindatory.code")))

		aclRequest = listOf(ApplicationAclPermissionRequest("user", listOf(ApplicationPermission.View)))
		mvc.perform(
			post(_artifactTemplatesEntryPointEn + artifactTemplate.id + "/permissions/remove")
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(aclRequest))
		)
			.andExpect(status().isOk)

		mvc.perform(
			get(_artifactTemplatesEntryPointEn + artifactTemplate.id)
				.header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
		)
			.andExpect(status().isOk)

		// update
		artifactTemplate.description = "updated"

		contents = mvc.perform(
			put(_artifactTemplatesEntryPointEn)
				.header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(artifactTemplate))
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.description", equalTo("updated")))
			.andReturn().response.contentAsString
		artifactTemplate = objectMapper.readValue(contents, ArtifactTemplateTest::class.java)

		// change the owner
		mvc.perform(
			get(_artifactTemplatesEntryPointEn + attributeTemplate2.id)
				.header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
		)
			.andExpect(status().isForbidden)

		mvc.perform(
			post(_artifactTemplatesEntryPointEn + attributeTemplate2.id + "/permissions/change-owner/user")
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
		)
			.andExpect(status().isOk)

		mvc.perform(
			get(_artifactTemplatesEntryPointEn + attributeTemplate2.id)
				.header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.identifier", equalTo("mindatory.name")))

		// delete
		mvc.perform(
			delete(_artifactTemplatesEntryPointEn + artifactTemplate.id)
				.header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
		)
			.andExpect(status().isForbidden)

		mvc.perform(
			delete(_artifactTemplatesEntryPointEn + attributeTemplate2.id)
				.header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
		)
			.andExpect(status().isOk)

		mvc.perform(
			get(_artifactTemplatesEntryPointEn + attributeTemplate2.id)
				.header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
		)
			.andExpect(status().isNotFound)
	}

	fun designVersions() {
		// get design versions
		val attributeTemplateDesignVersions = "$_artifactTemplatesEntryPointEn/${artifactTemplate.id}/design-versions"

		mvc.perform(get(attributeTemplateDesignVersions).with(SecurityMockMvcRequestPostProcessors.anonymous()))
			.andExpect(status().isUnauthorized)

		mvc.perform(
			get(attributeTemplateDesignVersions)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$", hasSize<Any>(0)))

		// create design version
		var designVersion1 = ArtifactTemplateVersionTest(
			artifactTemplate,
			mutableListOf(attributeCodeTemplateVersion, attributeNameTemplateVersion)
		)
		var contents = mvc.perform(
			post(attributeTemplateDesignVersions)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(designVersion1))
		)
			.andExpect(status().isCreated)
			.andExpect(jsonPath("$.designVersion", equalTo(1)))
			.andExpect(jsonPath("$.attributes", hasSize<Any>(2)))
			.andReturn().response.contentAsString
		designVersion1 = objectMapper.readValue(contents, ArtifactTemplateVersionTest::class.java)

		// create another design version for the same attribute
//        var designVersion2 = ArtifactTemplateVersionTest(artifact)
//        mvc.perform(
//            post(attributeTemplateDesignVersions)
//                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(designVersion2))
//        )
//            .andExpect(status().isNotAcceptable)
//            .andExpect(jsonPath("$.errorCode", equalTo(ApplicationErrorCodes.ValidationArtifactTemplateHasInDesignVersion)))

		designVersion1.attributes = mutableListOf(attributeCodeTemplateVersion, attributeDescTemplateVersion)
		// modify the created design version
		contents = mvc.perform(
			put(attributeTemplateDesignVersions)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(designVersion1))
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.attributes", hasSize<Any>(2)))
			.andReturn().response.contentAsString
		designVersion1 = objectMapper.readValue(contents, ArtifactTemplateVersionTest::class.java)

		// release the design version
		contents = mvc.perform(
			post("$attributeTemplateDesignVersions/${designVersion1.id}/release")
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
		)
			.andExpect(status().isOk)
			.andReturn().response.contentAsString
		designVersion1 = objectMapper.readValue(contents, ArtifactTemplateVersionTest::class.java)

		// modify the released version
		mvc.perform(
			put(attributeTemplateDesignVersions)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(designVersion1))
		)
			.andExpect(status().isNotAcceptable)
			.andExpect(
				jsonPath(
					"$.errorCode",
					equalTo(ApplicationErrorCodes.ValidationCannotChangeReleasedArtifactTemplateVersion)
				)
			)

		// add another design version
//		contents = mvc.perform(
//			post(attributeTemplateDesignVersions)
//				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
//				.contentType(MediaType.APPLICATION_JSON)
//				.content(objectMapper.writeValueAsString(designVersion2))
//		)
//			.andExpect(status().isCreated)
//			.andExpect(jsonPath("$.designVersion", equalTo(2)))
//			.andReturn().response.contentAsString
//		designVersion2 = objectMapper.readValue(contents, ArtifactTemplateVersionTest::class.java)

		// delete the released design version
		mvc.perform(
			delete("$attributeTemplateDesignVersions/${designVersion1.id}")
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
		)
			.andExpect(status().isOk)

		// delete the non-released design version
//		mvc.perform(
//			delete("$attributeTemplateDesignVersions/${designVersion2.id}")
//				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
//		)
//			.andExpect(status().isOk)

		contents = mvc.perform(
			post(attributeTemplateDesignVersions)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(ArtifactTemplateVersionTest(artifactTemplate)))
		)
			.andExpect(status().isCreated)
			.andReturn().response.contentAsString
		artifactTemplateVersion = objectMapper.readValue(contents, ArtifactTemplateVersionTest::class.java)
	}

//    fun attributes() {
//        val attributeTemplateDesignVersion = "${_artifactTemplatesEntryPointEn}/${artifact.id}/design-versions/${artifactTemplateVersion.id}/attributes"
//
//        // get attributes count
//        mvc.perform(get(attributeTemplateDesignVersion).with(SecurityMockMvcRequestPostProcessors.anonymous()))
//            .andExpect(status().isUnauthorized)
//
//        mvc.perform(
//            get(attributeTemplateDesignVersion)
//                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
//        )
//            .andExpect(status().isOk)
//            .andExpect(jsonPath("$", hasSize<Any>(0)))
//
//        // add attributes
//        mvc.perform(
//            post("$attributeTemplateDesignVersion/add")
//                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(listOf(attributeCodeTemplateVersion.id, attributeNameTemplateVersion.id)))
//        )
//            .andExpect(status().isOk)
//
//        // get attributes count after adding
//        mvc.perform(
//            get(attributeTemplateDesignVersion)
//                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
//        )
//            .andExpect(status().isOk)
//            .andExpect(jsonPath("$", hasSize<Any>(2)))
//
//        // remove attribute
//        mvc.perform(
//            post("$attributeTemplateDesignVersion/remove")
//                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(listOf(attributeNameTemplateVersion.id)))
//        )
//            .andExpect(status().isOk)
//
//        // get attributes count after removing
//        mvc.perform(
//            get(attributeTemplateDesignVersion)
//                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
//        )
//            .andExpect(status().isOk)
//            .andExpect(jsonPath("$", hasSize<Any>(1)))
//
//        // add already added attribute
//        mvc.perform(
//            post("$attributeTemplateDesignVersion/add")
//                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(listOf(attributeCodeTemplateVersion.id)))
//        )
//            .andExpect(status().isNotAcceptable)
//            .andExpect(jsonPath("$.errorCode", equalTo(ApplicationErrorCodes.ValidationAttributeAlreadyAddedToThisArtifactTemplateVersion)))
//
//        // add another attribute
//        mvc.perform(
//            post("$attributeTemplateDesignVersion/add")
//                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(listOf(attributeDescTemplateVersion.id)))
//        )
//            .andExpect(status().isOk)
//
//        // get attributes count after removing
//        mvc.perform(
//            get(attributeTemplateDesignVersion)
//                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
//        )
//            .andExpect(status().isOk)
//            .andExpect(jsonPath("$", hasSize<Any>(2)))
//    }
}