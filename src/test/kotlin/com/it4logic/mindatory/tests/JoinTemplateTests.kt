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
import com.it4logic.mindatory.services.model.ArtifactTemplateService
import com.it4logic.mindatory.services.model.AttributeTemplateService
import com.it4logic.mindatory.services.model.StereotypeService
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
class RelationTemplateTests {
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

	private lateinit var applicationRepository: ApplicationRepositoryTest
	private lateinit var project: ProjectTest

	private lateinit var firstSideArtifactTemplate: ArtifactTemplateTest
	private lateinit var firstSideArtifactTemplateVersion: ArtifactTemplateVersionTest
	private lateinit var secondSideArtifactTemplate: ArtifactTemplateTest
	private lateinit var secondSideArtifactTemplateVersion: ArtifactTemplateVersionTest

	private lateinit var firstSideStereotype: StereotypeTest
	private lateinit var secondSideStereotype: StereotypeTest


	private lateinit var attributeCodeTemplate: AttributeTemplateTest
	private lateinit var attributeCodeTemplateVersion: AttributeTemplateVersionTest
	private lateinit var attributeNameTemplate: AttributeTemplateTest
	private lateinit var attributeNameTemplateVersion: AttributeTemplateVersionTest
	private lateinit var attributeVersions: MutableList<AttributeTemplateVersionTest>

	private lateinit var relationTemplate: RelationTemplateTest

	private val testDataTypeUUID = "19bf955e-00c7-43d6-9b47-d286c20bd0da"

	private val _relationTemplatesEntryPointEn: String = ApplicationControllerEntryPoints.RelationTemplates + "en/"
	private val _repositoriesEntryPoint: String = ApplicationControllerEntryPoints.Models + "en/"
	private val _projectsEntryPointEn: String = ApplicationControllerEntryPoints.Projects + "en/"
	private val _attributeTemplatesEntryPointEn: String = ApplicationControllerEntryPoints.AttributeTemplates + "en/"
	private val _artifactTemplatesEntryPointEn: String = ApplicationControllerEntryPoints.ArtifactTemplates + "en/"
	private val _stereotypesEntryPointEn: String = ApplicationControllerEntryPoints.Stereotypes + "en/"

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
					ApplicationSecurityPermissions.RelationTemplateAdminView,
					ApplicationSecurityPermissions.RelationTemplateAdminCreate,
					ApplicationSecurityPermissions.RelationTemplateAdminModify,
					ApplicationSecurityPermissions.RelationTemplateAdminDelete,
					ApplicationSecurityPermissions.ModelAdminCreate,
					ApplicationSecurityPermissions.ProjectAdminCreate,
					ApplicationSecurityPermissions.AttributeTemplateAdminCreate,
					ApplicationSecurityPermissions.ArtifactTemplateAdminCreate,
					ApplicationSecurityPermissions.StereotypeAdminCreate
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

		// create attributes
		attributeCodeTemplate = AttributeTemplateTest(
			identifier = "mindatory.code",
			name = "Mindatory Code",
			repository = applicationRepository
		)
		attributeNameTemplate = AttributeTemplateTest(
			identifier = "mindatory.name",
			name = "Mindatory Name",
			repository = applicationRepository,
			project = project
		)

		contents = mvc.perform(
			post(_attributeTemplatesEntryPointEn)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(attributeCodeTemplate))
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
				.content(objectMapper.writeValueAsString(attributeNameTemplate))
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

		attributeVersions = mutableListOf(attributeCodeTemplateVersion, attributeNameTemplateVersion)

		// first side artifact
		// create artifact
		firstSideArtifactTemplate = ArtifactTemplateTest(
			identifier = "mindatory.first-side",
			name = "Mindatory First Side",
			repository = applicationRepository
		)
		contents = mvc.perform(
			post(_artifactTemplatesEntryPointEn)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(firstSideArtifactTemplate))
		)
			.andExpect(status().isCreated)
			.andReturn().response.contentAsString
		firstSideArtifactTemplate = objectMapper.readValue(contents, ArtifactTemplateTest::class.java)

		firstSideArtifactTemplateVersion = ArtifactTemplateVersionTest(
			firstSideArtifactTemplate,
			mutableListOf(attributeCodeTemplateVersion, attributeNameTemplateVersion)
		)
		contents = mvc.perform(
			post("$_artifactTemplatesEntryPointEn/${firstSideArtifactTemplate.id}/design-versions")
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(firstSideArtifactTemplateVersion))
		)
			.andExpect(status().isCreated)
			.andReturn().response.contentAsString
		firstSideArtifactTemplateVersion = objectMapper.readValue(contents, ArtifactTemplateVersionTest::class.java)

		contents = mvc.perform(
			post("$_artifactTemplatesEntryPointEn/${firstSideArtifactTemplate.id}/design-versions/${firstSideArtifactTemplateVersion.id}/release")
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
		)
			.andExpect(status().isOk)
			.andReturn().response.contentAsString
		firstSideArtifactTemplateVersion = objectMapper.readValue(contents, ArtifactTemplateVersionTest::class.java)


		// second side artifact
		// create artifact
		secondSideArtifactTemplate = ArtifactTemplateTest(
			identifier = "mindatory.second-side",
			name = "Mindatory Second Side",
			repository = applicationRepository
		)
		contents = mvc.perform(
			post(_artifactTemplatesEntryPointEn)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(secondSideArtifactTemplate))
		)
			.andExpect(status().isCreated)
			.andReturn().response.contentAsString
		secondSideArtifactTemplate = objectMapper.readValue(contents, ArtifactTemplateTest::class.java)

		secondSideArtifactTemplateVersion = ArtifactTemplateVersionTest(
			secondSideArtifactTemplate,
			mutableListOf(attributeCodeTemplateVersion, attributeNameTemplateVersion)
		)
		contents = mvc.perform(
			post("$_artifactTemplatesEntryPointEn/${secondSideArtifactTemplate.id}/design-versions")
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(secondSideArtifactTemplateVersion))
		)
			.andExpect(status().isCreated)
			.andReturn().response.contentAsString
		secondSideArtifactTemplateVersion = objectMapper.readValue(contents, ArtifactTemplateVersionTest::class.java)

		contents = mvc.perform(
			post("$_artifactTemplatesEntryPointEn/${secondSideArtifactTemplate.id}/design-versions/${secondSideArtifactTemplateVersion.id}/release")
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
		)
			.andExpect(status().isOk)
			.andReturn().response.contentAsString
		secondSideArtifactTemplateVersion = objectMapper.readValue(contents, ArtifactTemplateVersionTest::class.java)


		// Stereotypes
		contents = mvc.perform(
			post(_stereotypesEntryPointEn)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					objectMapper.writeValueAsString(
						StereotypeTest(
							name = "Bridge",
							repository = applicationRepository
						)
					)
				)
		)
			.andExpect(status().isCreated)
			.andReturn().response.contentAsString
		firstSideStereotype = objectMapper.readValue(contents, StereotypeTest::class.java)

		contents = mvc.perform(
			post(_stereotypesEntryPointEn)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					objectMapper.writeValueAsString(
						StereotypeTest(
							name = "Link",
							repository = applicationRepository
						)
					)
				)
		)
			.andExpect(status().isCreated)
			.andReturn().response.contentAsString
		secondSideStereotype = objectMapper.readValue(contents, StereotypeTest::class.java)
	}

	@Test
	fun `Artifact Templates Tests`() {
		basicOperations()
		designVersions()
	}

	fun basicOperations() {
		// create Join Templates
		mvc.perform(post(_relationTemplatesEntryPointEn).with(SecurityMockMvcRequestPostProcessors.anonymous()))
			.andExpect(status().isUnauthorized)

		var contents = mvc.perform(
			post(_relationTemplatesEntryPointEn)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					objectMapper.writeValueAsString(
						RelationTemplateTest(
							identifier = "mindatory.first-second",
							repository = applicationRepository
						)
					)
				)
		)
			.andExpect(status().isCreated)
			.andExpect(jsonPath("$.identifier", equalTo("mindatory.first-second")))
			.andReturn().response.contentAsString
		relationTemplate = objectMapper.readValue(contents, RelationTemplateTest::class.java)

		// duplicate check
		mvc.perform(
			post(_relationTemplatesEntryPointEn)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					objectMapper.writeValueAsString(
						RelationTemplateTest(
							identifier = "mindatory.first-second",
							repository = applicationRepository
						)
					)
				)
		)
			.andExpect(status().isNotAcceptable)
			.andExpect(jsonPath("$.errorCode", equalTo(ApplicationErrorCodes.DataIntegrityError)))
			.andExpect(
				jsonPath(
					"$.errorData",
					anyOf(equalTo(ApplicationErrorCodes.DuplicateRelationTemplateIdentifier))
				)
			)

		contents = mvc.perform(
			post(_relationTemplatesEntryPointEn)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					objectMapper.writeValueAsString(
						RelationTemplateTest(
							identifier = "mindatory.dummy",
							repository = applicationRepository
						)
					)
				)
		)
			.andExpect(status().isCreated)
			.andExpect(jsonPath("$.identifier", equalTo("mindatory.dummy")))
			.andReturn().response.contentAsString
		val relationTemplate2 = objectMapper.readValue(contents, RelationTemplateTest::class.java)

		// load
		mvc.perform(
			get(_relationTemplatesEntryPointEn + relationTemplate.id)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
		)
			.andExpect(status().isOk)

		mvc.perform(
			get(_relationTemplatesEntryPointEn + relationTemplate.id)
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
			post(_relationTemplatesEntryPointEn + relationTemplate.id + "/permissions/add")
				.header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(aclRequest))
		)
			.andExpect(status().isForbidden)

		mvc.perform(
			post(_relationTemplatesEntryPointEn + relationTemplate.id + "/permissions/add")
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(aclRequest))
		)
			.andExpect(status().isOk)

		mvc.perform(
			get(_relationTemplatesEntryPointEn + relationTemplate.id)
				.header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.identifier", equalTo("mindatory.first-second")))

		aclRequest = listOf(ApplicationAclPermissionRequest("user", listOf(ApplicationPermission.View)))
		mvc.perform(
			post(_relationTemplatesEntryPointEn + relationTemplate.id + "/permissions/remove")
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(aclRequest))
		)
			.andExpect(status().isOk)

		mvc.perform(
			get(_relationTemplatesEntryPointEn + relationTemplate.id)
				.header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
		)
			.andExpect(status().isOk)

		// update
		relationTemplate.description = "updated"

		contents = mvc.perform(
			put(_relationTemplatesEntryPointEn)
				.header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(relationTemplate))
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.description", equalTo("updated")))
			.andReturn().response.contentAsString
		relationTemplate = objectMapper.readValue(contents, RelationTemplateTest::class.java)

		// change the owner
		mvc.perform(
			get(_relationTemplatesEntryPointEn + relationTemplate2.id)
				.header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
		)
			.andExpect(status().isForbidden)

		mvc.perform(
			post(_relationTemplatesEntryPointEn + relationTemplate2.id + "/permissions/change-owner/user")
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
		)
			.andExpect(status().isOk)

		mvc.perform(
			get(_relationTemplatesEntryPointEn + relationTemplate2.id)
				.header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.identifier", equalTo("mindatory.dummy")))

		// delete
		mvc.perform(
			delete(_relationTemplatesEntryPointEn + relationTemplate.id)
				.header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
		)
			.andExpect(status().isForbidden)

		mvc.perform(
			delete(_relationTemplatesEntryPointEn + relationTemplate2.id)
				.header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
		)
			.andExpect(status().isOk)

		mvc.perform(
			get(_relationTemplatesEntryPointEn + relationTemplate2.id)
				.header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
		)
			.andExpect(status().isNotFound)
	}

	fun designVersions() {
		// get design versions
		val relationTemplateDesignVersions = "$_relationTemplatesEntryPointEn/${relationTemplate.id}/design-versions"

		mvc.perform(get(relationTemplateDesignVersions).with(SecurityMockMvcRequestPostProcessors.anonymous()))
			.andExpect(status().isUnauthorized)

		mvc.perform(
			get(relationTemplateDesignVersions)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$", hasSize<Any>(0)))

		// create design version
		var designVersion1 = RelationTemplateVersionTest(
			relationTemplate, firstSideStereotype, mutableListOf(firstSideArtifactTemplateVersion),
			secondSideStereotype, mutableListOf(secondSideArtifactTemplateVersion)
		)
		var contents = mvc.perform(
			post(relationTemplateDesignVersions)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(designVersion1))
		)
			.andExpect(status().isCreated)
			.andExpect(jsonPath("$.designVersion", equalTo(1)))
			.andReturn().response.contentAsString
		designVersion1 = objectMapper.readValue(contents, RelationTemplateVersionTest::class.java)

		// create another design version for the same attribute
		var designVersion2 = RelationTemplateVersionTest(
			relationTemplate, firstSideStereotype, mutableListOf(firstSideArtifactTemplateVersion),
			secondSideStereotype, mutableListOf(secondSideArtifactTemplateVersion)
		)
		mvc.perform(
			post(relationTemplateDesignVersions)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(designVersion2))
		)
			.andExpect(status().isNotAcceptable)
			.andExpect(
				jsonPath(
					"$.errorCode",
					equalTo(ApplicationErrorCodes.ValidationRelationTemplateHasInDesignVersion)
				)
			)

		// modify the created design version
		contents = mvc.perform(
			put(relationTemplateDesignVersions)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(designVersion1))
		)
			.andExpect(status().isOk)
			.andReturn().response.contentAsString
		designVersion1 = objectMapper.readValue(contents, RelationTemplateVersionTest::class.java)

		// release the design version
		contents = mvc.perform(
			post("$relationTemplateDesignVersions/${designVersion1.id}/release")
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
		)
			.andExpect(status().isOk)
			.andReturn().response.contentAsString
		designVersion1 = objectMapper.readValue(contents, RelationTemplateVersionTest::class.java)

		// modify the released version
		mvc.perform(
			put(relationTemplateDesignVersions)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(designVersion1))
		)
			.andExpect(status().isNotAcceptable)
			.andExpect(
				jsonPath(
					"$.errorCode",
					equalTo(ApplicationErrorCodes.ValidationCannotChangeReleasedRelationTemplateVersion)
				)
			)

		// add another design version
		contents = mvc.perform(
			post(relationTemplateDesignVersions)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(designVersion2))
		)
			.andExpect(status().isCreated)
			.andExpect(jsonPath("$.designVersion", equalTo(2)))
			.andReturn().response.contentAsString
		designVersion2 = objectMapper.readValue(contents, RelationTemplateVersionTest::class.java)

		// delete the released design version
		mvc.perform(
			delete("$relationTemplateDesignVersions/${designVersion1.id}")
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
		)
			.andExpect(status().isOk)

		// delete the non-released design version
		mvc.perform(
			delete("$relationTemplateDesignVersions/${designVersion2.id}")
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
		)
			.andExpect(status().isOk)
	}
}