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

import com.it4logic.mindatory.exceptions.ApiError
import com.it4logic.mindatory.exceptions.ApplicationErrorCodes
import com.it4logic.mindatory.model.model.AttributeTemplateProperty
import org.springframework.http.HttpStatus
import java.util.*

/**
 * Attribute Template Text Data Type management class
 */
class TextDataType : AttributeTemplateDataType {

	override val identifier: UUID
		get() = DataTypeUUID.Text.toUUID()

	override val name: String
		get() = DataTypeName.Text

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
				DataTypePropertyId.MIN_LENGTH,
				DataTypePropertyName.MIN_LENGTH,
				"",
				DataTypePropertyDataType.Number,
				-1
			),
			AttributeTemplateDataTypeProperty(
				DataTypePropertyId.MAX_LENGTH,
				DataTypePropertyName.MAX_LENGTH,
				"",
				DataTypePropertyDataType.Number,
				-1
			),
			AttributeTemplateDataTypeProperty(
				DataTypePropertyId.PATTERN,
				DataTypePropertyName.PATTERN,
				"",
				DataTypePropertyDataType.String,
				""
			),
			AttributeTemplateDataTypeProperty(
				DataTypePropertyId.MULTI_LINE,
				DataTypePropertyName.MULTI_LINE,
				"",
				DataTypePropertyDataType.Boolean,
				false
			)
		)

	override fun validate(
		properties: MutableList<AttributeTemplateProperty>,
		contents: String?,
		validateContent: Boolean
	): ApiError? {
		var property = properties.find { it.identifier == DataTypePropertyId.REQUIRED }
			?: return ApiError(
				HttpStatus.NOT_ACCEPTABLE,
				ApplicationErrorCodes.ValidationAttributeTemplateDataTypeMissingProperty,
				DataTypePropertyName.REQUIRED
			)

		if (validateContent && property.value.toBoolean()) {
			if (contents == null || contents.isBlank())
				return ApiError(
					HttpStatus.NOT_ACCEPTABLE,
					ApplicationErrorCodes.ValidationAttributeTemplateDataTypeContentsIsRequired,
					""
				)
		}

		property = properties.find { it.identifier == DataTypePropertyId.MIN_LENGTH }
			?: return ApiError(
				HttpStatus.NOT_ACCEPTABLE,
				ApplicationErrorCodes.ValidationAttributeTemplateDataTypeMissingProperty,
				DataTypePropertyName.MIN_LENGTH
			)

		try {
			val value = property.value.toLong()
			if (validateContent && contents != null && value != -1L && contents.length < value)
				return ApiError(
					HttpStatus.NOT_ACCEPTABLE,
					ApplicationErrorCodes.ValidationAttributeTemplateDataTypeContentsLengthIsLowerThanMinimum,
					""
				)
		} catch (ex: NumberFormatException) {
			return ApiError(
				HttpStatus.NOT_ACCEPTABLE,
				ApplicationErrorCodes.ValidationAttributeTemplateDataTypePropertyValueIsNotMatchingPropertyType,
				DataTypePropertyName.MIN_LENGTH,
				property.value
			)
		}

		property = properties.find { it.identifier == DataTypePropertyId.MAX_LENGTH }
			?: return ApiError(
				HttpStatus.NOT_ACCEPTABLE,
				ApplicationErrorCodes.ValidationAttributeTemplateDataTypeMissingProperty,
				DataTypePropertyName.MAX_LENGTH
			)

		try {
			val value = property.value.toLong()
			if (validateContent && contents != null && value != -1L && contents.length > value)
				return ApiError(
					HttpStatus.NOT_ACCEPTABLE,
					ApplicationErrorCodes.ValidationAttributeTemplateDataTypeContentsLengthIsHigherThanMinimum,
					""
				)
		} catch (ex: NumberFormatException) {
			return ApiError(
				HttpStatus.NOT_ACCEPTABLE,
				ApplicationErrorCodes.ValidationAttributeTemplateDataTypePropertyValueIsNotMatchingPropertyType,
				DataTypePropertyName.MAX_LENGTH,
				property.value
			)
		}

		property = properties.find { it.identifier == DataTypePropertyId.PATTERN }
			?: return ApiError(
				HttpStatus.NOT_ACCEPTABLE,
				ApplicationErrorCodes.ValidationAttributeTemplateDataTypeMissingProperty,
				DataTypePropertyName.PATTERN
			)

		val pattern = property.value.toRegex()
		if (validateContent && contents != null && !pattern.matches(contents))
			return ApiError(
				HttpStatus.NOT_ACCEPTABLE,
				ApplicationErrorCodes.ValidationAttributeTemplateDataTypeContentsIsNotMatchingPattern,
				""
			)

		properties.find { it.identifier == DataTypePropertyId.MULTI_LINE }
			?: return ApiError(
				HttpStatus.NOT_ACCEPTABLE,
				ApplicationErrorCodes.ValidationAttributeTemplateDataTypeMissingProperty,
				DataTypePropertyName.MULTI_LINE
			)

		return null
	}
}