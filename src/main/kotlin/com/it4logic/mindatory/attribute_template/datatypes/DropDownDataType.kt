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
package com.it4logic.mindatory.attribute_template.datatypes

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.it4logic.mindatory.exceptions.ApiError
import com.it4logic.mindatory.exceptions.ApplicationErrorCodes
import com.it4logic.mindatory.model.model.AttributeTemplateProperty
import org.springframework.http.HttpStatus
import java.io.IOException
import java.util.*

/**
 * Attribute Template DropDown Data Type management class
 */
class DropDownDataType : AttributeTemplateDataType {
	data class DropDownItem (var id: Long, var value: String)

	override val identifier: UUID
		get() = DataTypeUUID.DropDown.toUUID()

	override val name: String
		get() = DataTypeName.DropDown

	override val properties: List<AttributeTemplateDataTypeProperty>
		get() = listOf(
			AttributeTemplateDataTypeProperty(
				DataTypePropertyId.REQUIRED,
				DataTypePropertyName.REQUIRED,
				"",
				DataTypePropertyDataType.Boolean,
				false
			),
			AttributeTemplateDataTypeProperty(
				DataTypePropertyId.DATA,
				DataTypePropertyName.DATA,
				"",
				DataTypePropertyDataType.Json,
				""
			)
		)

	override fun validate(
		properties: MutableList<AttributeTemplateProperty>,
		contents: String?,
		validateContent: Boolean
	): ApiError? {
		val required = properties.find { it.identifier == DataTypePropertyId.REQUIRED }
			?: return ApiError(
				HttpStatus.NOT_ACCEPTABLE,
				ApplicationErrorCodes.ValidationAttributeTemplateDataTypeMissingProperty,
				DataTypePropertyName.REQUIRED
			)

		if (validateContent && required.value.toBoolean()) {
			if (contents == null || contents.isBlank())
				return ApiError(
					HttpStatus.NOT_ACCEPTABLE,
					ApplicationErrorCodes.ValidationAttributeTemplateDataTypeContentsIsRequired,
					""
				)
		}

		val property = properties.find { it.identifier == DataTypePropertyId.DATA }
			?: return ApiError(
				HttpStatus.NOT_ACCEPTABLE,
				ApplicationErrorCodes.ValidationAttributeTemplateDataTypeMissingProperty,
				DataTypePropertyName.DATA
			)

		var data = listOf<DropDownItem>()
		try {
			if (property.value.isNotBlank()) {
				data = ObjectMapper().readValue(property.value)
			}
		} catch (ex: Exception) {
			when (ex) {
				is IOException,
				is JsonParseException,
				is JsonMappingException -> {
					return ApiError(
						HttpStatus.NOT_ACCEPTABLE,
						ApplicationErrorCodes.ValidationAttributeTemplateDataTypePropertyValueIsNotMatchingPropertyType,
						DataTypePropertyName.DATA,
						property.value
					)
				}
			}
		}

		try {
			if (validateContent && contents != null && contents.isNotBlank()) {
				data.find { it.id == contents.toLong() }
					?: return ApiError(
						HttpStatus.NOT_ACCEPTABLE,
						ApplicationErrorCodes.ValidationAttributeTemplateDataTypeContentsIsNotInRange,
						""
					)
			}
		} catch (ex: NumberFormatException) {
			return ApiError(
				HttpStatus.NOT_ACCEPTABLE,
				ApplicationErrorCodes.ValidationAttributeTemplateDataTypePropertyValueIsNotMatchingPropertyType,
				DataTypePropertyName.MAX_LENGTH,
				property.value
			)
		}


		return null
	}
}