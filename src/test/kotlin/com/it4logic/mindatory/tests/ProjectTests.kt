/*
    Copyright (c) 2019, IT4Logic.

    This file is part of Mindatory project by IT4Logic.

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
class ProjectTests {

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

	private val _projectsEntryPointEn: String = ApplicationControllerEntryPoints.Projects + "en/"
	private val _projectsEntryPointAr: String = ApplicationControllerEntryPoints.Projects + "ar/"
	private val _repositoriesEntryPoint: String = ApplicationControllerEntryPoints.Models + "en/"

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
		roleAdmin = securityRoleService.create(
			SecurityRole(
				"ROLE_ADMIN", "Admins Role",
				permissions = arrayListOf(
					ApplicationSecurityPermissions.ProjectAdminView,
					ApplicationSecurityPermissions.ProjectAdminCreate,
					ApplicationSecurityPermissions.ProjectAdminModify,
					ApplicationSecurityPermissions.ProjectAdminDelete,
					ApplicationSecurityPermissions.ModelAdminCreate
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

	@Test
	fun `Projects Management`() {
		var project1 = ProjectTest("Project A")
		var project2 = ProjectTest("Project B")
		val project3 = ProjectTest("Project B")

		// create projects
		mvc.perform(post(_projectsEntryPointEn).with(anonymous()))
			.andExpect(status().isUnauthorized)

		var contents = mvc.perform(
			post(_projectsEntryPointEn)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(project1))
		)
			.andExpect(status().isCreated)
			.andExpect(jsonPath("$.name", equalTo("Project A")))
			.andReturn().response.contentAsString
		project1 = objectMapper.readValue(contents, ProjectTest::class.java)

		mvc.perform(
			post(_repositoriesEntryPoint)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					objectMapper.writeValueAsString(
						ApplicationRepositoryTest(
							"Model A",
							project = project1
						)
					)
				)
		)
			.andExpect(status().isCreated)
			.andExpect(jsonPath("$.name", equalTo("Model A")))

		contents = mvc.perform(
			post(_projectsEntryPointEn)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(project2))
		)
			.andExpect(status().isCreated)
			.andExpect(jsonPath("$.name", equalTo("Project B")))
			.andReturn().response.contentAsString
		project2 = objectMapper.readValue(contents, ProjectTest::class.java)

		// duplicate check
		mvc.perform(
			post(_projectsEntryPointEn)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(project2))
		)
			.andExpect(status().isNotAcceptable)
			.andExpect(
				jsonPath(
					"$.errorCode",
					equalTo(ApplicationErrorCodes.ValidationCannotCreateObjectWithExistingId)
				)
			)

		mvc.perform(
			post(_projectsEntryPointEn)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(project3))
		)
			.andExpect(status().isNotAcceptable)
			.andExpect(jsonPath("$.errorCode", equalTo(ApplicationErrorCodes.DataIntegrityError)))
			.andExpect(jsonPath("$.errorData", equalTo(ApplicationErrorCodes.DuplicateProjectName)))

		// load project
		mvc.perform(
			get(_projectsEntryPointEn + project1.id)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
		)
			.andExpect(status().isOk)

		mvc.perform(
			post(_projectsEntryPointEn)
				.header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(project2))
		)
			.andExpect(status().isForbidden)

		mvc.perform(
			get(_projectsEntryPointEn + project1.id)
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
			post(_projectsEntryPointEn + project1.id + "/permissions/add")
				.header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(aclRequest))
		)
			.andExpect(status().isForbidden)

		mvc.perform(
			post(_projectsEntryPointEn + project1.id + "/permissions/add")
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(aclRequest))
		)
			.andExpect(status().isOk)

		mvc.perform(
			get(_projectsEntryPointEn + project1.id)
				.header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.name", equalTo("Project A")))

		aclRequest = listOf(ApplicationAclPermissionRequest("user", listOf(ApplicationPermission.View)))
		mvc.perform(
			post(_projectsEntryPointEn + project1.id + "/permissions/remove")
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(aclRequest))
		)
			.andExpect(status().isOk)

		mvc.perform(
			get(_projectsEntryPointEn + project1.id)
				.header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
		)
			.andExpect(status().isOk)

		// update
		project1.description = "updated"

		contents = mvc.perform(
			put(_projectsEntryPointEn)
				.header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(project1))
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.description", equalTo("updated")))
			.andReturn().response.contentAsString
		project1 = objectMapper.readValue(contents, ProjectTest::class.java)

		// getting in Arabic
		mvc.perform(
			get(_projectsEntryPointAr + project1.id)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.name", equalTo("Project A")))

		// updating in Arabic
		project1.description = "محدث"

		contents = mvc.perform(
			put(_projectsEntryPointAr)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(project1))
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.description", equalTo("محدث")))
			.andReturn().response.contentAsString
		project1 = objectMapper.readValue(contents, ProjectTest::class.java)

		// change the owner
		mvc.perform(
			get(_projectsEntryPointEn + project2.id)
				.header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
		)
			.andExpect(status().isForbidden)

		mvc.perform(
			post(_projectsEntryPointEn + project2.id + "/permissions/change-owner/user")
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
		)
			.andExpect(status().isOk)

		mvc.perform(
			get(_projectsEntryPointEn + project2.id)
				.header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.name", equalTo("Project B")))

		// delete
		mvc.perform(
			delete(_projectsEntryPointEn + project1.id)
				.header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
		)
			.andExpect(status().isForbidden)

		mvc.perform(
			delete(_projectsEntryPointEn + project1.id)
				.header("Authorization", adminLogin.tokenType + " " + adminLogin.accessToken)
		)
			.andExpect(status().isNotAcceptable)
			.andExpect(jsonPath("$.errorCode", equalTo(ApplicationErrorCodes.ValidationProjectHasModel)))

		mvc.perform(
			delete(_projectsEntryPointEn + project2.id)
				.header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
		)
			.andExpect(status().isOk)

		mvc.perform(
			get(_projectsEntryPointEn + project2.id)
				.header("Authorization", userLogin.tokenType + " " + userLogin.accessToken)
		)
			.andExpect(status().isNotFound)
	}

}