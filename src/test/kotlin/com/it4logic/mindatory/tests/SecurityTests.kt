/*
    Copyright (c) 2017, IT4Logic.

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
import com.it4logic.mindatory.model.mlc.Language
import com.it4logic.mindatory.model.security.SecurityGroup
import com.it4logic.mindatory.model.security.SecurityRole
import com.it4logic.mindatory.model.security.SecurityUser
import com.it4logic.mindatory.security.ApplicationSecurityPermissions
import com.it4logic.mindatory.security.ChangePasswordRequest
import com.it4logic.mindatory.security.JwtAuthenticationResponse
import com.it4logic.mindatory.security.LoginRequest
import com.it4logic.mindatory.services.LanguageService
import org.hamcrest.Matchers.hasSize
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.hamcrest.Matchers.*
import org.junit.FixMethodOrder
import org.junit.runners.MethodSorters
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*
import org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class SecurityTests {

    companion object {
        var roleAdminId = 0L
        var roleUserId = 0L
        var groupAdminId = 0L
        var groupUserId = 0L
        var normalUserId = 0L
    }

    @Autowired
    private lateinit var context: WebApplicationContext

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var languageService: LanguageService

    private lateinit var mvc: MockMvc

    private val _usersEntryPointEn: String = ApplicationControllerEntryPoints.SecurityUsers + "en/"
    private val _userProfileEntryPointEn: String = ApplicationControllerEntryPoints.SecurityUserProfile + "en/"
    private val _rolesEntryPointEn: String = ApplicationControllerEntryPoints.SecurityRoles + "en/"
    private val _groupsEntryPointEn: String = ApplicationControllerEntryPoints.SecurityGroups + "en/"

    @Before
    fun setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply<DefaultMockMvcBuilder>(springSecurity())
                .build()

        setupLanguageData()
    }

    fun setupLanguageData() {
        val result = languageService.findAll(null, null, null) as List<*>
        if(!result.isEmpty())
            return
        languageService.create(Language("en", "English", true))
        languageService.create(Language("ar", "عربي", false))
    }

    @Test
    fun `A- Security Roles`() {
        val roleAdmin = SecurityRole("ROLE_ADMIN", "Admins Role",
                permissions = arrayListOf(
                        ApplicationSecurityPermissions.SecurityRoleAdminView,
                        ApplicationSecurityPermissions.SecurityRoleAdminCreate,
                        ApplicationSecurityPermissions.SecurityGroupAdminView
                        ))
        var roleUser = SecurityRole("ROLE_USER", "Users Role",
                permissions = arrayListOf(ApplicationSecurityPermissions.SecurityRoleAdminView))

        // Check for view role with anonymous access
        mvc.perform(get(_rolesEntryPointEn).with(anonymous())).andExpect(unauthenticated())

        // Check for view role with lack of permissions
        mvc.perform(get(_rolesEntryPointEn).with(user("super_admin"))).andExpect(status().isForbidden)

        // Check for retrieving all roles
        mvc.perform(get(_rolesEntryPointEn).with(
                user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityRoleAdminView})))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", hasSize<Any>(0)))

        // Check for add role with anonymous access
        mvc.perform(post(_rolesEntryPointEn).with(anonymous())).andExpect(unauthenticated())

        // Check for add with lack of permissions
        mvc.perform(post(_rolesEntryPointEn).with(
                user("super_admin")).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(roleAdmin)))
            .andExpect(status().isForbidden)

        // Check for add role
        var contents = mvc.perform(
                post(_rolesEntryPointEn)
                .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityRoleAdminCreate}))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleAdmin))
            )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name", equalTo("ROLE_ADMIN")))
            .andReturn().response.contentAsString
        roleAdminId = objectMapper.readValue(contents, SecurityRole::class.java).id

        // Check for add role with duplication
        mvc.perform(
                post(_rolesEntryPointEn)
                .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityRoleAdminCreate}))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleAdmin))
            )
            .andExpect(status().isNotAcceptable)
            .andExpect(jsonPath("$.errorCode", equalTo(ApplicationErrorCodes.DataIntegrityError)))
            .andExpect(jsonPath("$.errorData", equalTo(ApplicationErrorCodes.DuplicateSecurityRoleName)))

        // Check for add another role
        val roleUrl = mvc.perform(
                post(_rolesEntryPointEn)
                .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityRoleAdminCreate}))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleUser))
            )
            .andExpect(status().isCreated)
            .andReturn().response.getHeaderValue("Location")

        // Check for new roles count
        mvc.perform(get(_rolesEntryPointEn).with(
                user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityRoleAdminView})))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$", hasSize<Any>(2)))

        // Check for non exists role
        mvc.perform(get(_rolesEntryPointEn + "99999").with(
                user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityRoleAdminView})))
                .andExpect(status().isNotFound)

        // check retrieving specific role
        contents = mvc.perform(get("$roleUrl").with(
                user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityRoleAdminView})))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.name", equalTo("ROLE_USER")))
                .andReturn().response.contentAsString

        roleUser = objectMapper.readValue(contents, SecurityRole::class.java)
        roleUser.addPermission(ApplicationSecurityPermissions.SecurityRoleAdminModify)
        roleUser.removePermission(ApplicationSecurityPermissions.SecurityRoleAdminView)
        roleUser.addPermission(ApplicationSecurityPermissions.SecurityRoleAdminCreate)
        roleUser.description = "updated"

        // Check update specific role
        mvc.perform(
                put(_rolesEntryPointEn)
                .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityRoleAdminModify}))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleUser))
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.description", equalTo("updated")))
            .andExpect(jsonPath("$.permissions", hasSize<Any>(2)))
            .andExpect(jsonPath("$.permissions", contains(ApplicationSecurityPermissions.SecurityRoleAdminModify,ApplicationSecurityPermissions.SecurityRoleAdminCreate)))

        // Check delete specific role
        mvc.perform(
                delete("$roleUrl")
                .with(user("super_admin").authorities(
                        GrantedAuthority {ApplicationSecurityPermissions.SecurityRoleAdminDelete},
                        GrantedAuthority {ApplicationSecurityPermissions.SecurityRoleAdminModify}))
            )
            .andExpect(status().isOk)

        // Check for the roles count after delete
        mvc.perform(get(_rolesEntryPointEn).with(
            user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityRoleAdminView})))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", hasSize<Any>(1)))

        // Adding second role for users test
        roleUser = SecurityRole("ROLE_USER", "Users Role", permissions = arrayListOf(ApplicationSecurityPermissions.SecurityRoleAdminView))

        contents = mvc.perform(
                post(_rolesEntryPointEn)
                .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityRoleAdminCreate}))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleUser))
            )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name", equalTo("ROLE_USER")))
            .andReturn().response.contentAsString
        roleUserId = objectMapper.readValue(contents, SecurityRole::class.java).id


        // testing paging & searching
        for(idx in 1..10) {
            mvc.perform(
                    post(_rolesEntryPointEn)
                            .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityRoleAdminCreate}))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(SecurityRole("ROLE_$idx")))
            )
                    .andExpect(status().isCreated)
        }

        mvc.perform(
                get(_rolesEntryPointEn)
                        .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityRoleAdminView}))
                        .param("filter","name=='*9'")
        )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$", hasSize<Any>(1)))

        mvc.perform(
                get(_rolesEntryPointEn)
                        .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityRoleAdminView}))
                        .param("filter","name=='ROLE*'")
                        .param("page","0")
                        .param("size","9")
                        .param("sort","name,desc")
                        .param("sort","createdBy,asc")
        )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content", hasSize<Any>(9)))

        mvc.perform(
                get(_rolesEntryPointEn)
                        .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityRoleAdminView}))
                        .param("filter","name=='ROLE*'")
                        .param("page","1")
                        .param("size","9")
                        .param("sort","name,desc")
                        .param("sort","createdBy,asc")
        )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content", hasSize<Any>(3)))
    }

    @Test
    fun `B- Security Groups`() {
        val adminGroup = SecurityGroup("Admins Group", "Group for Admins")
        var userGroup = SecurityGroup("Users Group", "Group for Users")

        // Check for view group with anonymous access
        mvc.perform(get(_groupsEntryPointEn).with(anonymous())).andExpect(unauthenticated())

        // Check for view group with lack of permissions
        mvc.perform(get(_groupsEntryPointEn).with(user("super_admin"))).andExpect(status().isForbidden)

        // Check for retrieving all groups
        mvc.perform(get(_groupsEntryPointEn).with(
                user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityGroupAdminView})))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$", hasSize<Any>(0)))

        // Check for add group with anonymous access
        mvc.perform(post(_groupsEntryPointEn).with(anonymous())).andExpect(unauthenticated())

        // Check for add group with lack of permissions
        mvc.perform(post(_groupsEntryPointEn).with(
                user("super_admin")).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(adminGroup)))
                .andExpect(status().isForbidden)

        // Check for add group
        var contents = mvc.perform(
                post(_groupsEntryPointEn)
                        .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityGroupAdminCreate}))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminGroup))
                )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.name", equalTo("Admins Group")))
                .andReturn().response.contentAsString

        groupAdminId = objectMapper.readValue(contents, SecurityGroup::class.java).id

        // Check for add group with duplication
        mvc.perform(
                post(_groupsEntryPointEn)
                        .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityGroupAdminCreate}))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminGroup))
        )
                .andExpect(status().isNotAcceptable)
                .andExpect(jsonPath("$.errorCode", equalTo(ApplicationErrorCodes.DataIntegrityError)))
                .andExpect(jsonPath("$.errorData", equalTo(ApplicationErrorCodes.DuplicateSecurityGroupName)))

        // Check for add another group
        val groupUrl = mvc.perform(
                post(_groupsEntryPointEn)
                        .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityGroupAdminCreate}))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userGroup))
        )
                .andExpect(status().isCreated)
                .andReturn().response.getHeaderValue("Location")

        // Check for new groups count
        mvc.perform(get(_groupsEntryPointEn).with(
                user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityGroupAdminView})))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$", hasSize<Any>(2)))

        // Check for none exists group
        mvc.perform(get(_groupsEntryPointEn + "99999").with(
                user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityGroupAdminView})))
                .andExpect(status().isNotFound)

        // check retrieving specific group
        contents = mvc.perform(get("$groupUrl").with(
                user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityGroupAdminView})))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.name", equalTo("Users Group")))
                .andReturn().response.contentAsString

        userGroup = objectMapper.readValue(contents, SecurityGroup::class.java)
        userGroup.description = "updated"

        // Check update specific group
        mvc.perform(
                put(_groupsEntryPointEn)
                        .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityGroupAdminModify}))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userGroup))
        )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.description", equalTo("updated")))

        // Check delete specific group
        mvc.perform(
                delete("$groupUrl")
                        .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityGroupAdminDelete}))
        )
                .andExpect(status().isOk)

        // Check for the groups count after delete
        mvc.perform(get(_groupsEntryPointEn).with(
                user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityGroupAdminView})))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$", hasSize<Any>(1)))

        // Adding second group for users test
        userGroup = SecurityGroup("Users Group", "Group for Users")

        contents = mvc.perform(
                post(_groupsEntryPointEn)
                        .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityGroupAdminCreate}))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userGroup))
        )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.name", equalTo("Users Group")))
                .andReturn().response.contentAsString

        groupUserId = objectMapper.readValue(contents, SecurityGroup::class.java).id


        // testing paging & searching
        for(idx in 1..10) {
            mvc.perform(
                    post(_groupsEntryPointEn)
                            .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityGroupAdminCreate}))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(SecurityGroup("Group $idx")))
                    )
                    .andExpect(status().isCreated)
        }

        mvc.perform(
                get(_groupsEntryPointEn)
                .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityGroupAdminView}))
                .param("filter","name=='*9'")
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", hasSize<Any>(1)))

        mvc.perform(
                get(_groupsEntryPointEn)
                .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityGroupAdminView}))
                .param("filter","name=='Group*'")
                .param("page","0")
                .param("size","9")
                .param("sort","name,desc")
                .param("sort","createdBy,asc")
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Any>(9)))

        mvc.perform(
                get(_groupsEntryPointEn)
                .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityGroupAdminView}))
                .param("filter","name=='Group*'")
                .param("page","1")
                .param("size","9")
                .param("sort","name,desc")
                .param("sort","createdBy,asc")
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Any>(3)))

    }

    @Test
    fun `C- Security Users`() {
        val roleAdmin = SecurityRole()
        roleAdmin.id = roleAdminId
        val roleUser = SecurityRole()
        roleUser.id = roleUserId

        val adminGroup = SecurityGroup()
        adminGroup.id = groupAdminId
        val userGroup = SecurityGroup()
        userGroup.id = groupUserId

        val adminUser = SecurityUser("admin", "password", fullName = "Admin User", email = "admin@it4logic.com",
                roles = mutableListOf(roleAdmin), group = adminGroup)

        var normalUser = SecurityUser("user", "password", fullName = "Manager User", email = "manager@it4logic.com",
                roles = mutableListOf(roleUser), group = userGroup)

        var anotherUser = SecurityUser("another-user", "password", fullName = "Manager User", email = "manager@it4logic.com",
            roles = mutableListOf(roleUser), group = userGroup)

        // Check for view user with anonymous access
        mvc.perform(get(_usersEntryPointEn).with(anonymous())).andExpect(unauthenticated())

        // Check for view user with lack of permissions
        mvc.perform(get(_usersEntryPointEn).with(user("super_admin"))).andExpect(status().isForbidden)

        // Check for retrieving all users
        mvc.perform(get(_usersEntryPointEn).with(
                user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityUserAdminView})))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$", hasSize<Any>(0)))

        // Check for add user with anonymous access
        mvc.perform(post(_usersEntryPointEn).with(anonymous())).andExpect(unauthenticated())

        // Check for add user with lack of permissions
        mvc.perform(post(_usersEntryPointEn).with(
                user("super_admin")).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(adminUser)))
                .andExpect(status().isForbidden)

        // Check for add user
        mvc.perform(
                post(_usersEntryPointEn)
                .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityUserAdminCreate}))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminUser))
            )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.username", equalTo("admin")))
            .andReturn().response

        // Check for add user with duplication
        mvc.perform(
                post(_usersEntryPointEn)
                .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityUserAdminCreate}))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminUser))
            )
            .andExpect(status().isNotAcceptable)
            .andExpect(jsonPath("$.errorCode", equalTo(ApplicationErrorCodes.DataIntegrityError)))
            .andExpect(jsonPath("$.errorData", equalTo(ApplicationErrorCodes.DuplicateSecurityUserUsername)))

        // Check for add another user
        var response = mvc.perform(
                post(_usersEntryPointEn)
                .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityUserAdminCreate}))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(anotherUser))
            )
            .andExpect(status().isCreated)
            .andReturn().response
        anotherUser = objectMapper.readValue(response.contentAsString, SecurityUser::class.java)

        // Check for add another user
        response = mvc.perform(
            post(_usersEntryPointEn)
                .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityUserAdminCreate}))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(normalUser))
        )
            .andExpect(status().isCreated)
            .andReturn().response

        val userUrl = response.getHeaderValue("Location")
        normalUserId = objectMapper.readValue(response.contentAsString, SecurityGroup::class.java).id

        // Check for new users count
        mvc.perform(get(_usersEntryPointEn).with(
                user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityUserAdminView})))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$", hasSize<Any>(3)))

        // Check for non exists user
        mvc.perform(get(_usersEntryPointEn + "99999").with(
                user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityUserAdminView})))
                .andExpect(status().isNotFound)

        // check retrieving specific user
        response = mvc.perform(get("$userUrl").with(
                user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityUserAdminView})))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.username", equalTo("user")))
                .andReturn().response

        normalUser = objectMapper.readValue(response.contentAsString, SecurityUser::class.java)
        normalUser.addRole(roleAdmin)
        normalUser.removeRole(roleUser)
        normalUser.notes = "updated"

        // Check update specific user
        mvc.perform(
                put(_usersEntryPointEn)
                        .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityUserAdminModify}))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(normalUser))
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.notes", equalTo("updated")))
            .andExpect(jsonPath("$.roles", hasSize<Any>(1)))
            .andExpect(jsonPath("$.roles[0].name", equalTo("ROLE_ADMIN")))


        // Check delete specific user
        mvc.perform(
                delete(_usersEntryPointEn + anotherUser.id.toString())
                        .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityUserAdminDelete}))
            )
            .andExpect(status().isOk)

        // Check for the users count after delete
        mvc.perform(get(_usersEntryPointEn).with(
                user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityUserAdminView})))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$", hasSize<Any>(2)))

        // testing paging & searching
        for(idx in 1..10) {
            mvc.perform(
                    post(_usersEntryPointEn)
                            .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityUserAdminCreate}))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    SecurityUser("user$idx", "password", fullName = "User $idx", email = "user$idx@it4logic.com",
                                            roles = mutableListOf(roleAdmin), group = adminGroup)
                                    ))
            )
                    .andExpect(status().isCreated)
        }

        mvc.perform(
                get(_usersEntryPointEn)
                        .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityUserAdminView}))
                        .param("filter","username=='*9'")
        )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$", hasSize<Any>(1)))

        mvc.perform(
                get(_usersEntryPointEn)
                        .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityUserAdminView}))
                        .param("filter","fullName=='User*'")
                        .param("page","0")
                        .param("size","9")
                        .param("sort","fullName,desc")
                        .param("sort","createdBy,asc")
        )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content", hasSize<Any>(9)))

        mvc.perform(
                get(_usersEntryPointEn)
                        .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityUserAdminView}))
                        .param("filter","fullName=='User*'")
                        .param("page","1")
                        .param("size","9")
                        .param("sort","fullName,desc")
                        .param("sort","createdBy,asc")
        )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content", hasSize<Any>(1)))
    }

    @Test
    fun `D- Security Group Users`() {
        mvc.perform(get("$_groupsEntryPointEn$groupAdminId/users").with(
                user("super_admin").authorities(
                        GrantedAuthority {ApplicationSecurityPermissions.SecurityGroupAdminView},
                        GrantedAuthority {ApplicationSecurityPermissions.SecurityUserAdminView})))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$", hasSize<Any>(11)))

        // delete group without users
        val contents = mvc.perform(
                get(_groupsEntryPointEn)
                        .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityGroupAdminView}))
                        .param("filter","name=='Group 9'")
            )
            .andExpect(status().isOk)
            .andReturn().response.contentAsString
        val group = objectMapper.treeToValue(objectMapper.readTree(contents)[0], SecurityGroup::class.java)

        mvc.perform(
                delete(_groupsEntryPointEn + group.id)
                .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityGroupAdminDelete}))
            )
            .andExpect(status().isOk)

        // delete group with users
        mvc.perform(
                delete(_groupsEntryPointEn + groupAdminId)
                .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityGroupAdminDelete}))
            )
            .andExpect(status().isNotAcceptable)
            .andExpect(jsonPath("$.errorCode", equalTo(ApplicationErrorCodes.ValidationGroupHasUsers)))

        // assign users to specific group
        val userIdsList = listOf(normalUserId)
        mvc.perform(
                post("$_groupsEntryPointEn$groupAdminId/users")
                        .with(user("super_admin").authorities(
                                GrantedAuthority {ApplicationSecurityPermissions.SecurityGroupAdminModify},
                                GrantedAuthority {ApplicationSecurityPermissions.SecurityUserAdminModify}))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userIdsList))
            )
            .andExpect(status().isOk)
    }

    @Test
    fun `E- Security Role Users`() {
        // get the users associated with role
        mvc.perform(get("$_rolesEntryPointEn$roleAdminId/users").with(
                user("super_admin").authorities(
                        GrantedAuthority {ApplicationSecurityPermissions.SecurityRoleAdminView},
                        GrantedAuthority {ApplicationSecurityPermissions.SecurityUserAdminView})))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$", hasSize<Any>(12)))

        // delete role without users
        val contents = mvc.perform(
                get(_rolesEntryPointEn)
                        .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityRoleAdminView}))
                        .param("filter","name=='ROLE_9'")
                )
                .andExpect(status().isOk)
                .andReturn().response.contentAsString
        val role = objectMapper.treeToValue(objectMapper.readTree(contents)[0], SecurityRole::class.java)


        // assign users to specific group
        val userIdsList = listOf(normalUserId)
        mvc.perform(
                post(_rolesEntryPointEn + role.id + "/users")
                        .with(user("super_admin").authorities(
                            GrantedAuthority {ApplicationSecurityPermissions.SecurityGroupAdminModify},
                            GrantedAuthority {ApplicationSecurityPermissions.SecurityUserAdminModify}))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userIdsList))
                )
                .andExpect(status().isOk)

        // delete role with users
        mvc.perform(
                delete(_rolesEntryPointEn + role.id)
                        .with(user("super_admin").authorities(
                                GrantedAuthority {ApplicationSecurityPermissions.SecurityRoleAdminDelete},
                                GrantedAuthority {ApplicationSecurityPermissions.SecurityUserAdminModify}))
                )
                .andExpect(status().isOk)


    }

    @Test
    fun `F- User Profile and Password Change`() {
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

        // check getting the user profile
        contents = mvc.perform(get(_userProfileEntryPointEn)
                .header("Authorization", loginResponse.tokenType + " " + loginResponse.accessToken)
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.username", equalTo("admin")))
            .andReturn().response.contentAsString
        val user = objectMapper.readValue(contents, SecurityUser::class.java)
        user.notes = "updated through profile"

        // check updating user profile
        mvc.perform(put(_userProfileEntryPointEn)
                .header("Authorization", loginResponse.tokenType + " " + loginResponse.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user))
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.notes", equalTo("updated through profile")))

        var pwdReq = ChangePasswordRequest("password", "P@ssw0rd", "P@ssw0rd")
        // check updating user password
        mvc.perform(post(_userProfileEntryPointEn + "change-password")
                .header("Authorization", loginResponse.tokenType + " " + loginResponse.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pwdReq))
            )
            .andExpect(status().isOk)

        // login after password change
        contents = mvc.perform(
                post(ApplicationControllerEntryPoints.Authentication + "login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(LoginRequest("admin", "P@ssw0rd")))
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken", not(isEmptyOrNullString())))
            .andReturn().response.contentAsString
        loginResponse = objectMapper.readValue(contents, JwtAuthenticationResponse::class.java)

        // check getting the user profile after password change
        mvc.perform(get(_userProfileEntryPointEn)
                .header("Authorization", loginResponse.tokenType + " " + loginResponse.accessToken)
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.username", equalTo("admin")))

        // change user password by id
        pwdReq = ChangePasswordRequest("password", "P@ssw0rd", "P@ssw0rd")
        mvc.perform(post("$_usersEntryPointEn$normalUserId/change-password")
                .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityUserAdminModify}))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pwdReq))
            )
            .andExpect(status().isOk)

        // login after password change
        mvc.perform(
                post(ApplicationControllerEntryPoints.Authentication + "login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(LoginRequest("user", "P@ssw0rd")))
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken", not(isEmptyOrNullString())))
    }
}