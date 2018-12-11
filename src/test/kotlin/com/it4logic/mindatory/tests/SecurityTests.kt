package com.it4logic.mindatory.tests

import com.fasterxml.jackson.databind.ObjectMapper
import com.it4logic.mindatory.controllers.common.ApplicationControllerEntryPoints
import com.it4logic.mindatory.exceptions.ApplicationErrorCodes
import com.it4logic.mindatory.model.security.SecurityGroup
import com.it4logic.mindatory.model.security.SecurityRole
import com.it4logic.mindatory.model.security.SecurityUser
import com.it4logic.mindatory.security.ApplicationSecurityPermissions
import com.it4logic.mindatory.security.ChangePasswordRequest
import com.it4logic.mindatory.security.JwtAuthenticationResponse
import com.it4logic.mindatory.security.LoginRequest
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
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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

    private lateinit var mvc: MockMvc


    @Before
    fun setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply<DefaultMockMvcBuilder>(springSecurity())
                .build()
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
        mvc.perform(get(ApplicationControllerEntryPoints.SecurityRoles).with(anonymous())).andExpect(unauthenticated())

        // Check for view role with lack of permissions
        mvc.perform(get(ApplicationControllerEntryPoints.SecurityRoles).with(user("super_admin"))).andExpect(status().isForbidden)

        // Check for retrieving all roles
        mvc.perform(get(ApplicationControllerEntryPoints.SecurityRoles).with(
                user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityRoleAdminView})))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", hasSize<Any>(0)))

        // Check for add role with anonymous access
        mvc.perform(post(ApplicationControllerEntryPoints.SecurityRoles).with(anonymous())).andExpect(unauthenticated())

        // Check for add with lack of permissions
        mvc.perform(post(ApplicationControllerEntryPoints.SecurityRoles).with(
                user("super_admin")).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(roleAdmin)))
            .andExpect(status().isForbidden)

        // Check for add role
        var contents = mvc.perform(
                post(ApplicationControllerEntryPoints.SecurityRoles)
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
                post(ApplicationControllerEntryPoints.SecurityRoles)
                .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityRoleAdminCreate}))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleAdmin))
            )
            .andExpect(status().isNotAcceptable)
            .andExpect(jsonPath("$.errorCode", equalTo(ApplicationErrorCodes.DataIntegrityError)))
            .andExpect(jsonPath("$.errorData", equalTo(ApplicationErrorCodes.DuplicateSecurityRoleName)))

        // Check for add another role
        val roleUrl = mvc.perform(
                post(ApplicationControllerEntryPoints.SecurityRoles)
                .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityRoleAdminCreate}))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleUser))
            )
            .andExpect(status().isCreated)
            .andReturn().response.getHeaderValue("Location")

        // Check for new roles count
        mvc.perform(get(ApplicationControllerEntryPoints.SecurityRoles).with(
                user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityRoleAdminView})))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$", hasSize<Any>(2)))

        // Check for non exists role
        mvc.perform(get(ApplicationControllerEntryPoints.SecurityRoles + "99999").with(
                user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityRoleAdminView})))
                .andExpect(status().isNotFound)

        // check retrieving specific role
        contents = mvc.perform(get("$roleUrl").with(
                user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityRoleAdminView})))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.name", equalTo("ROLE_USER")))
                .andReturn().response.contentAsString

        roleUser = objectMapper.readValue(contents, SecurityRole::class.java)
        roleUser.addPermission(ApplicationSecurityPermissions.SecurityRoleAdminUpdate)
        roleUser.removePermission(ApplicationSecurityPermissions.SecurityRoleAdminView)
        roleUser.addPermission(ApplicationSecurityPermissions.SecurityRoleAdminCreate)
        roleUser.description = "updated"

        // Check update specific role
        mvc.perform(
                put(ApplicationControllerEntryPoints.SecurityRoles)
                .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityRoleAdminUpdate}))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleUser))
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.description", equalTo("updated")))
            .andExpect(jsonPath("$.permissions", hasSize<Any>(2)))
            .andExpect(jsonPath("$.permissions", contains(ApplicationSecurityPermissions.SecurityRoleAdminUpdate,ApplicationSecurityPermissions.SecurityRoleAdminCreate)))

        // Check delete specific role
        mvc.perform(
                delete("$roleUrl")
                .with(user("super_admin").authorities(
                        GrantedAuthority {ApplicationSecurityPermissions.SecurityRoleAdminDelete},
                        GrantedAuthority {ApplicationSecurityPermissions.SecurityRoleAdminUpdate}))
            )
            .andExpect(status().isOk)

        // Check for the roles count after delete
        mvc.perform(get(ApplicationControllerEntryPoints.SecurityRoles).with(
            user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityRoleAdminView})))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", hasSize<Any>(1)))

        // Adding second role for users test
        roleUser = SecurityRole("ROLE_USER", "Users Role", permissions = arrayListOf(ApplicationSecurityPermissions.SecurityRoleAdminView))

        contents = mvc.perform(
                post(ApplicationControllerEntryPoints.SecurityRoles)
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
                    post(ApplicationControllerEntryPoints.SecurityRoles)
                            .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityRoleAdminCreate}))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(SecurityRole("ROLE_$idx")))
            )
                    .andExpect(status().isCreated)
        }

        mvc.perform(
                get(ApplicationControllerEntryPoints.SecurityRoles)
                        .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityRoleAdminView}))
                        .param("filter","name=='*9'")
        )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$", hasSize<Any>(1)))

        mvc.perform(
                get(ApplicationControllerEntryPoints.SecurityRoles)
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
                get(ApplicationControllerEntryPoints.SecurityRoles)
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
        mvc.perform(get(ApplicationControllerEntryPoints.SecurityGroups).with(anonymous())).andExpect(unauthenticated())

        // Check for view group with lack of permissions
        mvc.perform(get(ApplicationControllerEntryPoints.SecurityGroups).with(user("super_admin"))).andExpect(status().isForbidden)

        // Check for retrieving all groups
        mvc.perform(get(ApplicationControllerEntryPoints.SecurityGroups).with(
                user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityGroupAdminView})))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$", hasSize<Any>(0)))

        // Check for add group with anonymous access
        mvc.perform(post(ApplicationControllerEntryPoints.SecurityGroups).with(anonymous())).andExpect(unauthenticated())

        // Check for add group with lack of permissions
        mvc.perform(post(ApplicationControllerEntryPoints.SecurityGroups).with(
                user("super_admin")).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(adminGroup)))
                .andExpect(status().isForbidden)

        // Check for add group
        var contents = mvc.perform(
                post(ApplicationControllerEntryPoints.SecurityGroups)
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
                post(ApplicationControllerEntryPoints.SecurityGroups)
                        .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityGroupAdminCreate}))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminGroup))
        )
                .andExpect(status().isNotAcceptable)
                .andExpect(jsonPath("$.errorCode", equalTo(ApplicationErrorCodes.DataIntegrityError)))
                .andExpect(jsonPath("$.errorData", equalTo(ApplicationErrorCodes.DuplicateSecurityGroupName)))

        // Check for add another group
        val groupUrl = mvc.perform(
                post(ApplicationControllerEntryPoints.SecurityGroups)
                        .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityGroupAdminCreate}))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userGroup))
        )
                .andExpect(status().isCreated)
                .andReturn().response.getHeaderValue("Location")

        // Check for new groups count
        mvc.perform(get(ApplicationControllerEntryPoints.SecurityGroups).with(
                user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityGroupAdminView})))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$", hasSize<Any>(2)))

        // Check for none exists group
        mvc.perform(get(ApplicationControllerEntryPoints.SecurityGroups + "99999").with(
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
                put(ApplicationControllerEntryPoints.SecurityGroups)
                        .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityGroupAdminUpdate}))
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
        mvc.perform(get(ApplicationControllerEntryPoints.SecurityGroups).with(
                user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityGroupAdminView})))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$", hasSize<Any>(1)))

        // Adding second group for users test
        userGroup = SecurityGroup("Users Group", "Group for Users")

        contents = mvc.perform(
                post(ApplicationControllerEntryPoints.SecurityGroups)
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
                    post(ApplicationControllerEntryPoints.SecurityGroups)
                            .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityGroupAdminCreate}))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(SecurityGroup("Group $idx")))
                    )
                    .andExpect(status().isCreated)
        }

        mvc.perform(
                get(ApplicationControllerEntryPoints.SecurityGroups)
                .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityGroupAdminView}))
                .param("filter","name=='*9'")
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", hasSize<Any>(1)))

        mvc.perform(
                get(ApplicationControllerEntryPoints.SecurityGroups)
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
                get(ApplicationControllerEntryPoints.SecurityGroups)
                .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityGroupAdminView}))
                .param("filter","name=='Group*'")
                .param("page","1")
                .param("size","9")
                .param("sort","name,desc")
                .param("sort","createdBy,asc")
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Any>(1)))

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
        mvc.perform(get(ApplicationControllerEntryPoints.SecurityUsers).with(anonymous())).andExpect(unauthenticated())

        // Check for view user with lack of permissions
        mvc.perform(get(ApplicationControllerEntryPoints.SecurityUsers).with(user("super_admin"))).andExpect(status().isForbidden)

        // Check for retrieving all users
        mvc.perform(get(ApplicationControllerEntryPoints.SecurityUsers).with(
                user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityUserAdminView})))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$", hasSize<Any>(0)))

        // Check for add user with anonymous access
        mvc.perform(post(ApplicationControllerEntryPoints.SecurityUsers).with(anonymous())).andExpect(unauthenticated())

        // Check for add user with lack of permissions
        mvc.perform(post(ApplicationControllerEntryPoints.SecurityUsers).with(
                user("super_admin")).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(adminUser)))
                .andExpect(status().isForbidden)

        // Check for add user
        mvc.perform(
                post(ApplicationControllerEntryPoints.SecurityUsers)
                .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityUserAdminCreate}))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminUser))
            )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.username", equalTo("admin")))
            .andReturn().response.contentAsString

        // Check for add user with duplication
        mvc.perform(
                post(ApplicationControllerEntryPoints.SecurityUsers)
                .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityUserAdminCreate}))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminUser))
            )
            .andExpect(status().isNotAcceptable)
            .andExpect(jsonPath("$.errorCode", equalTo(ApplicationErrorCodes.DataIntegrityError)))
            .andExpect(jsonPath("$.errorData", equalTo(ApplicationErrorCodes.DuplicateSecurityUserUsername)))

        // Check for add another user
        var response = mvc.perform(
                post(ApplicationControllerEntryPoints.SecurityUsers)
                .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityUserAdminCreate}))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(anotherUser))
            )
            .andExpect(status().isCreated)
            .andReturn().response
        anotherUser = objectMapper.readValue(response.contentAsString, SecurityUser::class.java)

        // Check for add another user
        response = mvc.perform(
            post(ApplicationControllerEntryPoints.SecurityUsers)
                .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityUserAdminCreate}))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(normalUser))
        )
            .andExpect(status().isCreated)
            .andReturn().response

        val userUrl = response.getHeaderValue("Location")
        normalUserId = objectMapper.readValue(response.contentAsString, SecurityGroup::class.java).id

        // Check for new users count
        mvc.perform(get(ApplicationControllerEntryPoints.SecurityUsers).with(
                user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityUserAdminView})))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$", hasSize<Any>(3)))

        // Check for non exists user
        mvc.perform(get(ApplicationControllerEntryPoints.SecurityUsers + "99999").with(
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
                put(ApplicationControllerEntryPoints.SecurityUsers)
                        .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityUserAdminUpdate}))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(normalUser))
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.notes", equalTo("updated")))
            .andExpect(jsonPath("$.roles", hasSize<Any>(1)))
            .andExpect(jsonPath("$.roles[0].name", equalTo("ROLE_ADMIN")))


        // Check delete specific user
        mvc.perform(
                delete(ApplicationControllerEntryPoints.SecurityUsers + anotherUser.id.toString())
                        .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityUserAdminDelete}))
            )
            .andExpect(status().isOk)

        // Check for the users count after delete
        mvc.perform(get(ApplicationControllerEntryPoints.SecurityUsers).with(
                user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityUserAdminView})))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$", hasSize<Any>(2)))

        // testing paging & searching
        for(idx in 1..10) {
            mvc.perform(
                    post(ApplicationControllerEntryPoints.SecurityUsers)
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
                get(ApplicationControllerEntryPoints.SecurityUsers)
                        .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityUserAdminView}))
                        .param("filter","username=='*9'")
        )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$", hasSize<Any>(1)))

        mvc.perform(
                get(ApplicationControllerEntryPoints.SecurityUsers)
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
                get(ApplicationControllerEntryPoints.SecurityUsers)
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
        mvc.perform(get(ApplicationControllerEntryPoints.SecurityGroups + "$groupAdminId/users").with(
                user("super_admin").authorities(
                        GrantedAuthority {ApplicationSecurityPermissions.SecurityGroupAdminView},
                        GrantedAuthority {ApplicationSecurityPermissions.SecurityUserAdminView})))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$", hasSize<Any>(11)))

        // delete group without users
        val contents = mvc.perform(
                get(ApplicationControllerEntryPoints.SecurityGroups)
                        .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityGroupAdminView}))
                        .param("filter","name=='Group 9'")
            )
            .andExpect(status().isOk)
            .andReturn().response.contentAsString
        val group = objectMapper.treeToValue(objectMapper.readTree(contents)[0], SecurityGroup::class.java)

        mvc.perform(
                delete(ApplicationControllerEntryPoints.SecurityGroups + group.id)
                .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityGroupAdminDelete}))
            )
            .andExpect(status().isOk)

        // delete group with users
        mvc.perform(
                delete(ApplicationControllerEntryPoints.SecurityGroups + groupAdminId)
                .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityGroupAdminDelete}))
            )
            .andExpect(status().isNotAcceptable)
            .andExpect(jsonPath("$.errorCode", equalTo(ApplicationErrorCodes.ValidationGroupHasUsers)))

        // assign users to specific group
        val userIdsList = listOf(normalUserId)
        mvc.perform(
                post(ApplicationControllerEntryPoints.SecurityGroups + groupAdminId + "/users")
                        .with(user("super_admin").authorities(
                                GrantedAuthority {ApplicationSecurityPermissions.SecurityGroupAdminUpdate},
                                GrantedAuthority {ApplicationSecurityPermissions.SecurityUserAdminUpdate}))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userIdsList))
            )
            .andExpect(status().isOk)
    }

    @Test
    fun `E- Security Role Users`() {
        // get the users associated with role
        mvc.perform(get(ApplicationControllerEntryPoints.SecurityRoles + "$roleAdminId/users").with(
                user("super_admin").authorities(
                        GrantedAuthority {ApplicationSecurityPermissions.SecurityRoleAdminView},
                        GrantedAuthority {ApplicationSecurityPermissions.SecurityUserAdminView})))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$", hasSize<Any>(12)))

        // delete role without users
        val contents = mvc.perform(
                get(ApplicationControllerEntryPoints.SecurityRoles)
                        .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityRoleAdminView}))
                        .param("filter","name=='ROLE_9'")
                )
                .andExpect(status().isOk)
                .andReturn().response.contentAsString
        val role = objectMapper.treeToValue(objectMapper.readTree(contents)[0], SecurityRole::class.java)


        // assign users to specific group
        val userIdsList = listOf(normalUserId)
        mvc.perform(
                post(ApplicationControllerEntryPoints.SecurityRoles + role.id + "/users")
                        .with(user("super_admin").authorities(
                            GrantedAuthority {ApplicationSecurityPermissions.SecurityGroupAdminUpdate},
                            GrantedAuthority {ApplicationSecurityPermissions.SecurityUserAdminUpdate}))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userIdsList))
                )
                .andExpect(status().isOk)

        // delete role with users
        mvc.perform(
                delete(ApplicationControllerEntryPoints.SecurityRoles + role.id)
                        .with(user("super_admin").authorities(
                                GrantedAuthority {ApplicationSecurityPermissions.SecurityRoleAdminDelete},
                                GrantedAuthority {ApplicationSecurityPermissions.SecurityUserAdminUpdate}))
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
        contents = mvc.perform(get(ApplicationControllerEntryPoints.SecurityUserProfile)
                .header("Authorization", loginResponse.tokenType + " " + loginResponse.accessToken)
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.username", equalTo("admin")))
            .andReturn().response.contentAsString
        val user = objectMapper.readValue(contents, SecurityUser::class.java)
        user.notes = "updated through profile"

        // check updating user profile
        mvc.perform(put(ApplicationControllerEntryPoints.SecurityUserProfile)
                .header("Authorization", loginResponse.tokenType + " " + loginResponse.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user))
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.notes", equalTo("updated through profile")))

        var pwdReq = ChangePasswordRequest("password", "P@ssw0rd", "P@ssw0rd")
        // check updating user password
        mvc.perform(post(ApplicationControllerEntryPoints.SecurityUserProfile + "change-password")
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
        mvc.perform(get(ApplicationControllerEntryPoints.SecurityUserProfile)
                .header("Authorization", loginResponse.tokenType + " " + loginResponse.accessToken)
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.username", equalTo("admin")))

        // change user password by id
        pwdReq = ChangePasswordRequest("password", "P@ssw0rd", "P@ssw0rd")
        mvc.perform(post(ApplicationControllerEntryPoints.SecurityUsers + "$normalUserId/change-password")
                .with(user("super_admin").authorities(GrantedAuthority {ApplicationSecurityPermissions.SecurityUserAdminUpdate}))
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