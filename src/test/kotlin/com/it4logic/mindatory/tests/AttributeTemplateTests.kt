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
class AttributeTemplateTests {
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
	//	private lateinit var textDataType: AttributeTemplateDataType
	private lateinit var attributeCodeTemplate: AttributeTemplateTest

	private val testDataTypeUUID = "19bf955e-00c7-43d6-9b47-d286c20bd0da"

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
					ApplicationSecurityPermissions.AttributeTemplateAdminView,
					ApplicationSecurityPermissions.AttributeTemplateAdminCreate,
					ApplicationSecurityPermissions.AttributeTemplateAdminModify,
					ApplicationSecurityPermissions.AttributeTemplateAdminDelete,
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
	}

	@Test
	fun `Attribute Templates Tests`() {
		dataTypes()
		basicOperations()
		designVersions()
	}

	fun dataTypes() {
		mvc.perform(
			get(ApplicationControllerEntryPoints.AttributeTemplateDataTypes)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$", not<Any>(empty<Any>())))

//		val contents = mvc.perform(
//			get(ApplicationControllerEntryPoints.AttributeTemplateDataTypes + "/" + testDataTypeUUID)
//				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
//		)
//			.andExpect(status().isOk)
//			.andReturn().response.contentAsString
//		textDataType = objectMapper.readValue(contents, AttributeTemplateDataType::class.java)
	}

	fun basicOperations() {
		attributeCodeTemplate = AttributeTemplateTest(
			identifier = "mindatory.code",
			name = "Mindatory Code",
			repository = applicationRepository
		)
		var attributeTemplate2 = AttributeTemplateTest(
			identifier = "mindatory.name",
			name = "Mindatory Name",
			repository = applicationRepository,
			project = project
		)
		val attributeTemplate3 = AttributeTemplateTest(
			identifier = "mindatory.name",
			name = "Mindatory Name",
			repository = applicationRepository
		)

		// create Attribute Templates
		mvc.perform(post(_attributeTemplatesEntryPointEn).with(SecurityMockMvcRequestPostProcessors.anonymous()))
			.andExpect(status().isUnauthorized)

		var contents = mvc.perform(
			post(_attributeTemplatesEntryPointEn)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(attributeCodeTemplate))
		)
			.andExpect(status().isCreated)
			.andExpect(jsonPath("$.identifier", equalTo("mindatory.code")))
			.andReturn().response.contentAsString
		attributeCodeTemplate = objectMapper.readValue(contents, AttributeTemplateTest::class.java)

		contents = mvc.perform(
			post(_attributeTemplatesEntryPointEn)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(attributeTemplate2))
		)
			.andExpect(status().isCreated)
			.andExpect(jsonPath("$.identifier", equalTo("mindatory.name")))
			.andReturn().response.contentAsString
		attributeTemplate2 = objectMapper.readValue(contents, AttributeTemplateTest::class.java)

		// duplicate check
		mvc.perform(
			post(_attributeTemplatesEntryPointEn)
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
			post(_attributeTemplatesEntryPointEn)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(attributeTemplate3))
		)
			.andExpect(status().isNotAcceptable)
			.andExpect(jsonPath("$.errorCode", equalTo(ApplicationErrorCodes.DataIntegrityError)))
			.andExpect(
				jsonPath(
					"$.errorData", anyOf(
						equalTo(ApplicationErrorCodes.DuplicateAttributeTemplateIdentification),
						equalTo(ApplicationErrorCodes.DuplicateAttributeTemplateName)
					)
				)
			)

		// load project
		mvc.perform(
			get(_attributeTemplatesEntryPointEn + attributeCodeTemplate.id)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
		)
			.andExpect(status().isOk)

		mvc.perform(
			post(_attributeTemplatesEntryPointEn)
				.header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(attributeTemplate2))
		)
			.andExpect(status().isForbidden)

		mvc.perform(
			get(_attributeTemplatesEntryPointEn + attributeCodeTemplate.id)
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
			post(_attributeTemplatesEntryPointEn + attributeCodeTemplate.id + "/permissions/add")
				.header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(aclRequest))
		)
			.andExpect(status().isForbidden)

		mvc.perform(
			post(_attributeTemplatesEntryPointEn + attributeCodeTemplate.id + "/permissions/add")
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(aclRequest))
		)
			.andExpect(status().isOk)

		mvc.perform(
			get(_attributeTemplatesEntryPointEn + attributeCodeTemplate.id)
				.header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.identifier", equalTo("mindatory.code")))

		aclRequest = listOf(ApplicationAclPermissionRequest("user", listOf(ApplicationPermission.View)))
		mvc.perform(
			post(_attributeTemplatesEntryPointEn + attributeCodeTemplate.id + "/permissions/remove")
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(aclRequest))
		)
			.andExpect(status().isOk)

		mvc.perform(
			get(_attributeTemplatesEntryPointEn + attributeCodeTemplate.id)
				.header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
		)
			.andExpect(status().isOk)

		// update
		attributeCodeTemplate.description = "updated"

		contents = mvc.perform(
			put(_attributeTemplatesEntryPointEn)
				.header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(attributeCodeTemplate))
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.description", equalTo("updated")))
			.andReturn().response.contentAsString
		attributeCodeTemplate = objectMapper.readValue(contents, AttributeTemplateTest::class.java)

		// change the owner
		mvc.perform(
			get(_attributeTemplatesEntryPointEn + attributeTemplate2.id)
				.header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
		)
			.andExpect(status().isForbidden)

		mvc.perform(
			post(_attributeTemplatesEntryPointEn + attributeTemplate2.id + "/permissions/change-owner/user")
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
		)
			.andExpect(status().isOk)

		mvc.perform(
			get(_attributeTemplatesEntryPointEn + attributeTemplate2.id)
				.header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.identifier", equalTo("mindatory.name")))

		// delete
		mvc.perform(
			delete(_attributeTemplatesEntryPointEn + attributeCodeTemplate.id)
				.header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
		)
			.andExpect(status().isForbidden)

		mvc.perform(
			delete(_attributeTemplatesEntryPointEn + attributeTemplate2.id)
				.header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
		)
			.andExpect(status().isOk)

		mvc.perform(
			get(_attributeTemplatesEntryPointEn + attributeTemplate2.id)
				.header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
		)
			.andExpect(status().isNotFound)
	}

	fun designVersions() {
		// get design versions
		val attributeTemplateDesignVersions =
			"${_attributeTemplatesEntryPointEn}/${attributeCodeTemplate.id}/design-versions"

		mvc.perform(get(attributeTemplateDesignVersions).with(SecurityMockMvcRequestPostProcessors.anonymous()))
			.andExpect(status().isUnauthorized)

		mvc.perform(
			get(attributeTemplateDesignVersions)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$", hasSize<Any>(0)))

		// create design version
		var designVersion1 = AttributeTemplateVersionTest(
			attributeCodeTemplate,
			testDataTypeUUID,
			hashMapOf(Pair("length", 50), Pair("nullable", false))
		)
		var contents = mvc.perform(
			post(attributeTemplateDesignVersions)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(designVersion1))
		)
			.andExpect(status().isCreated)
			.andExpect(jsonPath("$.designVersion", equalTo(1)))
			.andReturn().response.contentAsString
		designVersion1 = objectMapper.readValue(contents, AttributeTemplateVersionTest::class.java)

		// create another design version for the same attribute
		var designVersion2 = AttributeTemplateVersionTest(
			attributeCodeTemplate,
			testDataTypeUUID,
			hashMapOf(Pair("length", 55), Pair("nullable", false))
		)
		mvc.perform(
			post(attributeTemplateDesignVersions)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(designVersion2))
		)
			.andExpect(status().isNotAcceptable)
			.andExpect(
				jsonPath(
					"$.errorCode",
					equalTo(ApplicationErrorCodes.ValidationAttributeTemplateHasInDesignVersion)
				)
			)

		mvc.perform(
			get(attributeTemplateDesignVersions)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$", hasSize<Any>(1)))

		// modify the created design version
		designVersion1.properties["pattern"] = "999999.99"
		contents = mvc.perform(
			put(attributeTemplateDesignVersions)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(designVersion1))
		)
			.andExpect(status().isOk)
			.andReturn().response.contentAsString
		designVersion1 = objectMapper.readValue(contents, AttributeTemplateVersionTest::class.java)

		// release the design version
		contents = mvc.perform(
			post("$attributeTemplateDesignVersions/${designVersion1.id}/release")
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
		)
			.andExpect(status().isOk)
			.andReturn().response.contentAsString
		designVersion1 = objectMapper.readValue(contents, AttributeTemplateVersionTest::class.java)

		// modify the released version
//        mvc.perform(
//            put(attributeTemplateDesignVersions)
//                .header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(designVersion1))
//            )
//            .andExpect(status().isNotAcceptable)
//            .andExpect(jsonPath("$.errorCode", equalTo(ApplicationErrorCodes.ValidationCannotChangeReleasedAttributeTemplateVersion)))

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
		designVersion2 = objectMapper.readValue(contents, AttributeTemplateVersionTest::class.java)

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
	}
}