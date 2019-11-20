package com.it4logic.mindatory.tests

import com.fasterxml.jackson.databind.ObjectMapper
import com.it4logic.mindatory.controllers.ApplicationControllerEntryPoints
import com.it4logic.mindatory.exceptions.ApplicationErrorCodes
import com.it4logic.mindatory.model.mlc.Language
import com.it4logic.mindatory.model.security.SecurityGroup
import com.it4logic.mindatory.model.security.SecurityRole
import com.it4logic.mindatory.model.security.SecurityUser
import com.it4logic.mindatory.security.ApplicationSecurityPermissions
import com.it4logic.mindatory.security.JwtAuthenticationResponse
import com.it4logic.mindatory.security.LoginRequest
import com.it4logic.mindatory.services.LanguageService
import com.it4logic.mindatory.services.model.ArtifactTemplateService
import com.it4logic.mindatory.services.model.AttributeTemplateService
import com.it4logic.mindatory.services.model.StereotypeService
import com.it4logic.mindatory.services.security.SecurityGroupService
import com.it4logic.mindatory.services.security.SecurityRoleService
import com.it4logic.mindatory.services.security.SecurityUserService
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class StoresTests {
	// Test Environment
	@Autowired
	private lateinit var context: WebApplicationContext
	@Autowired
	private lateinit var objectMapper: ObjectMapper
	private lateinit var mvc: MockMvc

	// Language
	@Autowired
	private lateinit var languageService: LanguageService

	// Security
	@Autowired
	private lateinit var securityRoleService: SecurityRoleService
	@Autowired
	private lateinit var securityGroupService: SecurityGroupService
	@Autowired
	private lateinit var securityUserService: SecurityUserService
	private lateinit var roleAdmin: SecurityRole
	private lateinit var roleUser: SecurityRole
	private lateinit var adminGroup: SecurityGroup
	private lateinit var userGroup: SecurityGroup
	private lateinit var adminUser: SecurityUser
	private lateinit var normalUser: SecurityUser
	private lateinit var adminLogin: JwtAuthenticationResponse
	private lateinit var userLogin: JwtAuthenticationResponse

	// Basic data
	@Autowired
	private lateinit var attributeTemplateService: AttributeTemplateService
	@Autowired
	private lateinit var artifactTemplateService: ArtifactTemplateService
	@Autowired
	private lateinit var stereotypeService: StereotypeService
	private lateinit var applicationRepository: ApplicationRepositoryTest
	private lateinit var project: ProjectTest
	private lateinit var attributeCodeTemplate: AttributeTemplateTest
	private lateinit var attributeCodeTemplateVersion: AttributeTemplateVersionTest
	private lateinit var attributeNameTemplate: AttributeTemplateTest
	private lateinit var attributeNameTemplateVersion: AttributeTemplateVersionTest
	private lateinit var attributeVersions: MutableList<AttributeTemplateVersionTest>
	private lateinit var firstSideArtifactTemplate: ArtifactTemplateTest
	private lateinit var firstSideArtifactTemplateVersion: ArtifactTemplateVersionTest
	//	private lateinit var firstSideArtifactTemplateVersionAttributes: MutableList<AttributeTemplateVersionTest>
	private lateinit var secondSideArtifactTemplate: ArtifactTemplateTest
	private lateinit var secondSideArtifactTemplateVersion: ArtifactTemplateVersionTest
	//	private lateinit var secondSideArtifactTemplateVersionAttributes: MutableList<AttributeTemplateVersionTest>
	private lateinit var firstSideStereotype: StereotypeTest
	private lateinit var secondSideStereotype: StereotypeTest
	private lateinit var relationTemplate: RelationTemplateTest
	private lateinit var relationTemplateVersion: RelationTemplateVersionTest

	// Testing Datatype UUID
	private val testDataTypeUUID = "19bf955e-00c7-43d6-9b47-d286c20bd0da"

	// Entry points
	private val _repositoriesEntryPoint: String = ApplicationControllerEntryPoints.Models + "en/"
	private val _projectsEntryPointEn: String = ApplicationControllerEntryPoints.Projects + "en/"
	private val _attributeTemplatesEntryPointEn: String = ApplicationControllerEntryPoints.AttributeTemplates + "en/"
	private val _artifactTemplatesEntryPointEn: String = ApplicationControllerEntryPoints.ArtifactTemplates + "en/"
	private val _relationTemplatesEntryPointEn: String = ApplicationControllerEntryPoints.RelationTemplates + "en/"
	private val _artifactStoresEntryPointEn: String = ApplicationControllerEntryPoints.ArtifactStores + "en/"
	private val _relationStoresEntryPointEn: String = ApplicationControllerEntryPoints.RelationStores + "en/"
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
		setupAttributesData()
		setupArtifactsData()
		setupJoinsData()
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
					ApplicationSecurityPermissions.ModelAdminCreate,
					ApplicationSecurityPermissions.ProjectAdminCreate,
					ApplicationSecurityPermissions.AttributeTemplateAdminCreate,
					ApplicationSecurityPermissions.ArtifactTemplateAdminCreate,
					ApplicationSecurityPermissions.StereotypeAdminCreate,
					ApplicationSecurityPermissions.RelationTemplateAdminCreate,
					ApplicationSecurityPermissions.ArtifactStoreAdminView,
					ApplicationSecurityPermissions.ArtifactStoreAdminCreate,
					ApplicationSecurityPermissions.ArtifactStoreAdminModify,
					ApplicationSecurityPermissions.ArtifactStoreAdminDelete,
					ApplicationSecurityPermissions.RelationStoreAdminView,
					ApplicationSecurityPermissions.RelationStoreAdminCreate,
					ApplicationSecurityPermissions.RelationStoreAdminModify,
					ApplicationSecurityPermissions.RelationStoreAdminDelete
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
			MockMvcRequestBuilders.post(ApplicationControllerEntryPoints.Authentication + "login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(LoginRequest("admin", "password")))
		)
			.andExpect(MockMvcResultMatchers.status().isOk)
			.andReturn().response.contentAsString
		adminLogin = objectMapper.readValue(contents, JwtAuthenticationResponse::class.java)

		contents = mvc.perform(
			MockMvcRequestBuilders.post(ApplicationControllerEntryPoints.Authentication + "login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(LoginRequest("user", "password")))
		)
			.andExpect(MockMvcResultMatchers.status().isOk)
			.andReturn().response.contentAsString
		userLogin = objectMapper.readValue(contents, JwtAuthenticationResponse::class.java)
	}

	fun setupBasicData() {
		var contents = mvc.perform(
			MockMvcRequestBuilders.post(_repositoriesEntryPoint)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(ApplicationRepositoryTest("Application Repository")))
		)
			.andExpect(MockMvcResultMatchers.status().isCreated)
			.andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.equalTo("Application Repository")))
			.andReturn().response.contentAsString
		applicationRepository = objectMapper.readValue(contents, ApplicationRepositoryTest::class.java)

		contents = mvc.perform(
			MockMvcRequestBuilders.post(_projectsEntryPointEn)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(ProjectTest("Application Project")))
		)
			.andExpect(MockMvcResultMatchers.status().isCreated)
			.andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.equalTo("Application Project")))
			.andReturn().response.contentAsString
		project = objectMapper.readValue(contents, ProjectTest::class.java)
	}

	fun setupAttributesData() {
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

		var contents = mvc.perform(
			MockMvcRequestBuilders.post(_attributeTemplatesEntryPointEn)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(attributeCodeTemplate))
		)
			.andExpect(MockMvcResultMatchers.status().isCreated)
			.andExpect(MockMvcResultMatchers.jsonPath("$.identifier", Matchers.equalTo("mindatory.code")))
			.andReturn().response.contentAsString
		attributeCodeTemplate = objectMapper.readValue(contents, AttributeTemplateTest::class.java)

		attributeCodeTemplateVersion = AttributeTemplateVersionTest(
			attributeCodeTemplate,
			testDataTypeUUID,
			hashMapOf(Pair("length", 50), Pair("nullable", false))
		)
		contents = mvc.perform(
			MockMvcRequestBuilders.post("$_attributeTemplatesEntryPointEn/${attributeCodeTemplate.id}/design-versions")
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(attributeCodeTemplateVersion))
		)
			.andExpect(MockMvcResultMatchers.status().isCreated)
			.andExpect(MockMvcResultMatchers.jsonPath("$.designVersion", Matchers.equalTo(1)))
			.andReturn().response.contentAsString
		attributeCodeTemplateVersion = objectMapper.readValue(contents, AttributeTemplateVersionTest::class.java)

		contents = mvc.perform(
			MockMvcRequestBuilders.post("$_attributeTemplatesEntryPointEn/${attributeCodeTemplate.id}/design-versions/${attributeCodeTemplateVersion.id}/release")
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
		)
			.andExpect(MockMvcResultMatchers.status().isOk)
			.andReturn().response.contentAsString
		attributeCodeTemplateVersion = objectMapper.readValue(contents, AttributeTemplateVersionTest::class.java)


		contents = mvc.perform(
			MockMvcRequestBuilders.post(_attributeTemplatesEntryPointEn)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(attributeNameTemplate))
		)
			.andExpect(MockMvcResultMatchers.status().isCreated)
			.andExpect(MockMvcResultMatchers.jsonPath("$.identifier", Matchers.equalTo("mindatory.name")))
			.andReturn().response.contentAsString
		attributeNameTemplate = objectMapper.readValue(contents, AttributeTemplateTest::class.java)

		attributeNameTemplateVersion = AttributeTemplateVersionTest(
			attributeNameTemplate,
			testDataTypeUUID,
			hashMapOf(Pair("length", 55), Pair("nullable", true))
		)
		contents = mvc.perform(
			MockMvcRequestBuilders.post("$_attributeTemplatesEntryPointEn/${attributeNameTemplate.id}/design-versions")
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(attributeNameTemplateVersion))
		)
			.andExpect(MockMvcResultMatchers.status().isCreated)
			.andExpect(MockMvcResultMatchers.jsonPath("$.designVersion", Matchers.equalTo(1)))
			.andReturn().response.contentAsString
		attributeNameTemplateVersion = objectMapper.readValue(contents, AttributeTemplateVersionTest::class.java)

		contents = mvc.perform(
			MockMvcRequestBuilders.post("$_attributeTemplatesEntryPointEn/${attributeNameTemplate.id}/design-versions/${attributeNameTemplateVersion.id}/release")
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
		)
			.andExpect(MockMvcResultMatchers.status().isOk)
			.andReturn().response.contentAsString
		attributeNameTemplateVersion = objectMapper.readValue(contents, AttributeTemplateVersionTest::class.java)

		attributeVersions = mutableListOf(attributeCodeTemplateVersion, attributeNameTemplateVersion)
	}

	fun setupArtifactsData() {
		// first side artifact
		// create artifact
		firstSideArtifactTemplate = ArtifactTemplateTest(
			identifier = "mindatory.first-side",
			name = "Mindatory First Side",
			repository = applicationRepository
		)
		var contents = mvc.perform(
			MockMvcRequestBuilders.post(_artifactTemplatesEntryPointEn)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(firstSideArtifactTemplate))
		)
			.andExpect(MockMvcResultMatchers.status().isCreated)
			.andReturn().response.contentAsString
		firstSideArtifactTemplate = objectMapper.readValue(contents, ArtifactTemplateTest::class.java)

		firstSideArtifactTemplateVersion = ArtifactTemplateVersionTest(
			firstSideArtifactTemplate,
			mutableListOf(attributeCodeTemplateVersion, attributeNameTemplateVersion)
		)
		contents = mvc.perform(
			MockMvcRequestBuilders.post("$_artifactTemplatesEntryPointEn/${firstSideArtifactTemplate.id}/design-versions")
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(firstSideArtifactTemplateVersion))
		)
			.andExpect(MockMvcResultMatchers.status().isCreated)
			.andReturn().response.contentAsString
		firstSideArtifactTemplateVersion = objectMapper.readValue(contents, ArtifactTemplateVersionTest::class.java)

		contents = mvc.perform(
			MockMvcRequestBuilders.post("$_artifactTemplatesEntryPointEn/${firstSideArtifactTemplate.id}/design-versions/${firstSideArtifactTemplateVersion.id}/release")
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
		)
			.andExpect(MockMvcResultMatchers.status().isOk)
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
			MockMvcRequestBuilders.post(_artifactTemplatesEntryPointEn)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(secondSideArtifactTemplate))
		)
			.andExpect(MockMvcResultMatchers.status().isCreated)
			.andReturn().response.contentAsString
		secondSideArtifactTemplate = objectMapper.readValue(contents, ArtifactTemplateTest::class.java)

		secondSideArtifactTemplateVersion = ArtifactTemplateVersionTest(
			secondSideArtifactTemplate,
			mutableListOf(attributeCodeTemplateVersion, attributeNameTemplateVersion)
		)
		contents = mvc.perform(
			MockMvcRequestBuilders.post("$_artifactTemplatesEntryPointEn/${secondSideArtifactTemplate.id}/design-versions")
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(secondSideArtifactTemplateVersion))
		)
			.andExpect(MockMvcResultMatchers.status().isCreated)
			.andReturn().response.contentAsString
		secondSideArtifactTemplateVersion = objectMapper.readValue(contents, ArtifactTemplateVersionTest::class.java)

		contents = mvc.perform(
			MockMvcRequestBuilders.post("$_artifactTemplatesEntryPointEn/${secondSideArtifactTemplate.id}/design-versions/${secondSideArtifactTemplateVersion.id}/release")
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
		)
			.andExpect(MockMvcResultMatchers.status().isOk)
			.andReturn().response.contentAsString
		secondSideArtifactTemplateVersion = objectMapper.readValue(contents, ArtifactTemplateVersionTest::class.java)
//
//		contents = mvc.perform(
//			MockMvcRequestBuilders.get(
//				_artifactTemplatesEntryPointEn + secondSideArtifactTemplate.id + "/design-versions/" + secondSideArtifactTemplateVersion.id + "/attributes")
//				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
//		)
//			.andExpect(MockMvcResultMatchers.status().isOk)
//			.andReturn().response.contentAsString
//		firstSideArtifactTemplateVersionAttributes = objectMapper.readValue(contents,
//			objectMapper.typeFactory.constructCollectionType(List::class.java, AttributeTemplateVersion::class.java))
	}

	fun setupJoinsData() {
		// Stereotypes
		var contents = mvc.perform(
			MockMvcRequestBuilders.post(_stereotypesEntryPointEn)
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
			.andExpect(MockMvcResultMatchers.status().isCreated)
			.andReturn().response.contentAsString
		firstSideStereotype = objectMapper.readValue(contents, StereotypeTest::class.java)

		contents = mvc.perform(
			MockMvcRequestBuilders.post(_stereotypesEntryPointEn)
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
			.andExpect(MockMvcResultMatchers.status().isCreated)
			.andReturn().response.contentAsString
		secondSideStereotype = objectMapper.readValue(contents, StereotypeTest::class.java)

		contents = mvc.perform(
			MockMvcRequestBuilders.post(_relationTemplatesEntryPointEn)
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
			.andExpect(MockMvcResultMatchers.status().isCreated)
			.andExpect(MockMvcResultMatchers.jsonPath("$.identifier", Matchers.equalTo("mindatory.first-second")))
			.andReturn().response.contentAsString
		relationTemplate = objectMapper.readValue(contents, RelationTemplateTest::class.java)

		val relationTemplateDesignVersions = "$_relationTemplatesEntryPointEn/${relationTemplate.id}/design-versions"
		relationTemplateVersion = RelationTemplateVersionTest(
			relationTemplate, firstSideStereotype, mutableListOf(firstSideArtifactTemplateVersion),
			secondSideStereotype, mutableListOf(secondSideArtifactTemplateVersion)
		)

		contents = mvc.perform(
			MockMvcRequestBuilders.post(relationTemplateDesignVersions)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(relationTemplateVersion))
		)
			.andExpect(MockMvcResultMatchers.status().isCreated)
			.andExpect(MockMvcResultMatchers.jsonPath("$.designVersion", Matchers.equalTo(1)))
			.andReturn().response.contentAsString
		relationTemplateVersion = objectMapper.readValue(contents, RelationTemplateVersionTest::class.java)

		// release the design version
		contents = mvc.perform(
			MockMvcRequestBuilders.post("$relationTemplateDesignVersions/${relationTemplateVersion.id}/release")
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
		)
			.andExpect(MockMvcResultMatchers.status().isOk)
			.andReturn().response.contentAsString
		relationTemplateVersion = objectMapper.readValue(contents, RelationTemplateVersionTest::class.java)
	}

	@Test
	fun `Data Stores Tests`() {
		testArtifactStores()
	}

	fun testArtifactStores() {

		mvc.perform(
			MockMvcRequestBuilders.post(_artifactStoresEntryPointEn)
				.with(SecurityMockMvcRequestPostProcessors.anonymous())
		)
			.andExpect(MockMvcResultMatchers.status().isUnauthorized)

		// First Artifact Store
		var firstArtifactStore =
			ArtifactStoreTest(artifactTemplateVersion = firstSideArtifactTemplateVersion, project = project)
		firstArtifactStore.project = project
		for (attribute in firstSideArtifactTemplateVersion.attributes) {
			val content = "{\"content\":\"${attribute.attributeTemplate.name} value\"}"
			val aStore = AttributeStoreTest(contents = content, attributeTemplateVersion = attribute)
			firstArtifactStore.attributeStores.add(aStore)
		}

		var contents = mvc.perform(
			MockMvcRequestBuilders.post(_artifactStoresEntryPointEn)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(firstArtifactStore))
		)
			.andExpect(MockMvcResultMatchers.status().isCreated)
			.andReturn().response.contentAsString
		firstArtifactStore = objectMapper.readValue(contents, ArtifactStoreTest::class.java)

		// First Artifact Store update
		firstArtifactStore.attributeStores.clear()
		for (attribute in firstSideArtifactTemplateVersion.attributes) {
			val content = "{\"content\":\"${attribute.attributeTemplate.name} value\", \"x\": \"y\"}"
			val aStore = AttributeStoreTest(
				contents = content,
				attributeTemplateVersion = attribute
			)
			firstArtifactStore.attributeStores.add(aStore)
		}

		contents = mvc.perform(
			MockMvcRequestBuilders.put(_artifactStoresEntryPointEn)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(firstArtifactStore))
		)
			.andExpect(MockMvcResultMatchers.status().isOk)
			.andReturn().response.contentAsString
		firstArtifactStore = objectMapper.readValue(contents, ArtifactStoreTest::class.java)

		// Second Artifact Store
		var secondArtifactStore =
			ArtifactStoreTest(artifactTemplateVersion = firstSideArtifactTemplateVersion, project = project)
		for (attribute in secondSideArtifactTemplateVersion.attributes) {
			val content = "{\"content\":\"${attribute.attributeTemplate.name} value\"}"
			val aStore = AttributeStoreTest(contents = content, attributeTemplateVersion = attribute)
			secondArtifactStore.attributeStores.add(aStore)
		}

		contents = mvc.perform(
			MockMvcRequestBuilders.post(_artifactStoresEntryPointEn)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(secondArtifactStore))
		)
			.andExpect(MockMvcResultMatchers.status().isCreated)
			.andReturn().response.contentAsString
		secondArtifactStore = objectMapper.readValue(contents, ArtifactStoreTest::class.java)

		// Create Join
		var relationStore = RelationStoreTest(
			mutableListOf(firstArtifactStore),
			mutableListOf(secondArtifactStore),
			relationTemplateVersion,
			project = project
		)
		contents = mvc.perform(
			MockMvcRequestBuilders.post(_relationStoresEntryPointEn)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(relationStore))
		)
			.andExpect(MockMvcResultMatchers.status().isCreated)
			.andReturn().response.contentAsString
		relationStore = objectMapper.readValue(contents, RelationStoreTest::class.java)

		mvc.perform(
			MockMvcRequestBuilders.delete(_artifactStoresEntryPointEn + secondArtifactStore.id)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
		)
			.andExpect(MockMvcResultMatchers.status().isNotAcceptable)
			.andExpect(
				MockMvcResultMatchers.jsonPath(
					"$.errorCode",
					Matchers.equalTo(ApplicationErrorCodes.ValidationCannotDeleteArtifactStoreObjectThatUsedInRelationStoreObjects)
				)
			)

		mvc.perform(
			MockMvcRequestBuilders.delete(_relationStoresEntryPointEn + relationStore.id)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
		)
			.andExpect(MockMvcResultMatchers.status().isOk)

		mvc.perform(
			MockMvcRequestBuilders.delete(_artifactStoresEntryPointEn + secondArtifactStore.id)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
		)
			.andExpect(MockMvcResultMatchers.status().isOk)
	}
}