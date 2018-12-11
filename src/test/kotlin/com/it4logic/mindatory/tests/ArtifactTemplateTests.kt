package com.it4logic.mindatory.tests

import com.fasterxml.jackson.databind.ObjectMapper
import com.it4logic.mindatory.controllers.common.ApplicationControllerEntryPoints
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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder


//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
//@RunWith(SpringRunner::class)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//class ArtifactTemplateTests {
//    @Autowired
//    private lateinit var context: WebApplicationContext
//
//    @Autowired
//    private lateinit var objectMapper: ObjectMapper
//
//    private lateinit var mvc: MockMvc
//
//    @Before
//    fun setup() {
//        mvc = MockMvcBuilders
//            .webAppContextSetup(context)
//            .apply<DefaultMockMvcBuilder>(springSecurity())
//            .build()
//    }
//
//    @Test
//    fun `A- Attribute Template Data Types`() {
//        mvc.perform(get(ApplicationControllerEntryPoints.AttributeTemplateDataTypes).with(anonymous()))
//            .andExpect(status().isUnauthorized)
//
//        mvc.perform(get(ApplicationControllerEntryPoints.AttributeTemplateDataTypes).with(
//            SecurityMockMvcRequestPostProcessors.user("super_admin")))
//            .andExpect(status().isOk)
//            .andExpect(jsonPath("$.length()", greaterThan(0)))
//
//        val uuid = "dd4bf72f-8689-4653-b2b0-c8e59a592a80"
//        mvc.perform(get(ApplicationControllerEntryPoints.AttributeTemplateDataTypes + "/$uuid").with(anonymous()))
//            .andExpect(status().isUnauthorized)
//
//        mvc.perform(get(ApplicationControllerEntryPoints.AttributeTemplateDataTypes + "/$uuid").with(
//            SecurityMockMvcRequestPostProcessors.user("super_admin")))
//            .andExpect(status().isOk)
//            .andExpect(jsonPath("$.identifier", equalTo(uuid)))
//
//        val badUuid = "dd4bf72f-8689-4653-0000-c8e59a590000"
//        mvc.perform(get(ApplicationControllerEntryPoints.AttributeTemplateDataTypes + "/$badUuid").with(
//            SecurityMockMvcRequestPostProcessors.user("super_admin")))
//            .andExpect(status().isNotFound)
//    }
//}