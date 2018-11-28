package com.it4logic.mindatory.tests

import com.fasterxml.jackson.databind.ObjectMapper
import com.it4logic.mindatory.api.plugins.AttributeTemplateDataType
import com.it4logic.mindatory.controllers.common.ApplicationControllerEntryPoints
import com.it4logic.mindatory.exceptions.ApplicationErrorCodes
import com.it4logic.mindatory.model.ApplicationRepository
import com.it4logic.mindatory.model.ApplicationRepositoryRepository
import com.it4logic.mindatory.model.repository.AttributeTemplate
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


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AttributeTemplateTests {
    @Autowired
    private lateinit var context: WebApplicationContext

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var applicationRepositoryRepository: ApplicationRepositoryRepository

    private lateinit var mvc: MockMvc

    companion object {
        private var applicationRepository: ApplicationRepository? = null
    }


    @Before
    fun setup() {
        mvc = MockMvcBuilders
            .webAppContextSetup(context)
            .build()

        if(applicationRepository == null)
            applicationRepository = applicationRepositoryRepository.save(ApplicationRepository(name = "Default", description = "Default Repository"))
    }


    @Test
    fun `A- Request Attribute Template Data Types`() {
        mvc.perform(get(ApplicationControllerEntryPoints.ATTRIBUTE_TEMPLATE_DATA_TYPES))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", hasSize<Any>(2)))
    }
/*
    @Test
    fun `B- Manage Attribute Templates`() {
        val textDataTypeUUID = "cd59f8d2-02d5-4175-97d8-be1bdec3c2a5"
        var contents =
            mvc.perform(get(ApplicationControllerEntryPoints.ATTRIBUTE_TEMPLATE_DATA_TYPES + "/" + textDataTypeUUID))
                .andExpect(status().isOk)
                .andReturn().response.contentAsString
        val textDataType = objectMapper.readValue(contents, AttributeTemplateDataType::class.java)

        var attributeTemplate = AttributeTemplate(
            identifier = "mindatory.attributes.code",
            name = "code",
            description = "description for code attribute",
            typeUUID = "cd59f8d2-02d5-4175-0000-be1bdec3c2a5",
            properties = "{length: 50,nullable: false}"
        )
        attributeTemplate.repository = applicationRepository

        // check for non exists data type
        mvc.perform(
            post(ApplicationControllerEntryPoints.ATTRIBUTE_TEMPLATES)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attributeTemplate))
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.errorCode", equalTo(ApplicationErrorCodes.NotFoundAttributeTemplateDataType)))

        attributeTemplate.typeUUID = textDataType.identifier.toString()

        // creating new attribute template
        val objectUrl = mvc.perform(
            post(ApplicationControllerEntryPoints.ATTRIBUTE_TEMPLATES)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attributeTemplate))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.identifier", equalTo("mindatory.attributes.code")))
            .andReturn().response.getHeaderValue("Location")

        // creating new attribute template
        contents = mvc.perform(
            get("$objectUrl")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.identifier", equalTo("mindatory.attributes.code")))
            .andReturn().response.contentAsString
        attributeTemplate = objectMapper.readValue(contents, AttributeTemplate::class.java)

        attributeTemplate.createdBy = null
        attributeTemplate.updatedBy = null
        attributeTemplate.version = 1
        attributeTemplate.description = "updated"

        // updating new attribute template
        contents = mvc.perform(
            put("$objectUrl")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attributeTemplate))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.description", equalTo("updated")))
            .andReturn().response.contentAsString
        attributeTemplate = objectMapper.readValue(contents, AttributeTemplate::class.java)

        // updating new attribute template
        mvc.perform(
            delete("$objectUrl")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attributeTemplate))
        )
            .andExpect(status().isOk)

        // check if the objecy is still exists
        mvc.perform(
            get("$objectUrl")
        )
            .andExpect(status().isNotFound)
    }
    */
}