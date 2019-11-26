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
 * Attribute Template Number Data Type management class
 */
class NumberDataType : AttributeTemplateDataType {

	override val identifier: UUID
		get() = DataTypeUUID.Number.toUUID()

	override val name: String
		get() = DataTypeName.Number

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
				DataTypePropertyId.MIN_VALUE,
				DataTypePropertyName.MIN_VALUE,
				"",
				DataTypePropertyDataType.Number,
				-1
			),
			AttributeTemplateDataTypeProperty(
				DataTypePropertyId.MAX_VALUE,
				DataTypePropertyName.MAX_VALUE,
				"",
				DataTypePropertyDataType.Number,
				-1
			),
			AttributeTemplateDataTypeProperty(
				DataTypePropertyId.SHOW_SPIN_BUTTONS,
				DataTypePropertyName.SHOW_SPIN_BUTTONS,
				"",
				DataTypePropertyDataType.Boolean,
				false
			),
			AttributeTemplateDataTypeProperty(
				DataTypePropertyId.SYMBOL_POSITION,
				DataTypePropertyName.SYMBOL_POSITION,
				"",
				DataTypePropertyDataType.String,
				"left"
			),
			AttributeTemplateDataTypeProperty(
				DataTypePropertyId.SYMBOL,
				DataTypePropertyName.SYMBOL,
				"",
				DataTypePropertyDataType.String,
				""
			),
			AttributeTemplateDataTypeProperty(
				DataTypePropertyId.NEGATIVE_SYMBOL,
				DataTypePropertyName.NEGATIVE_SYMBOL,
				"",
				DataTypePropertyDataType.String,
				"-"
			),
			AttributeTemplateDataTypeProperty(
				DataTypePropertyId.ALLOW_NULL,
				DataTypePropertyName.ALLOW_NULL,
				"",
				DataTypePropertyDataType.Boolean,
				true
			),
			AttributeTemplateDataTypeProperty(
				DataTypePropertyId.DECIMAL_DIGITS,
				DataTypePropertyName.DECIMAL_DIGITS,
				"",
				DataTypePropertyDataType.Number,
				2
			),
			AttributeTemplateDataTypeProperty(
				DataTypePropertyId.DECIMAL_SEPARATOR,
				DataTypePropertyName.DECIMAL_SEPARATOR,
				"",
				DataTypePropertyDataType.Number,
				"."
			),
			AttributeTemplateDataTypeProperty(
				DataTypePropertyId.DIGITS,
				DataTypePropertyName.DIGITS,
				"",
				DataTypePropertyDataType.Number,
				8
			),
			AttributeTemplateDataTypeProperty(
				DataTypePropertyId.GROUP_SEPARATOR,
				DataTypePropertyName.GROUP_SEPARATOR,
				"",
				DataTypePropertyDataType.String,
				","
			),
			AttributeTemplateDataTypeProperty(
				DataTypePropertyId.GROUP_SIZE,
				DataTypePropertyName.GROUP_SIZE,
				"",
				DataTypePropertyDataType.Number,
				3
			)

		)


	override fun validate(
		properties: MutableList<AttributeTemplateProperty>,
		contents: String?,
		validateContent: Boolean
	): ApiError? {
		val allowNull = properties.find { it.identifier == DataTypePropertyId.ALLOW_NULL }
			?: return ApiError(
				HttpStatus.NOT_ACCEPTABLE,
				ApplicationErrorCodes.ValidationAttributeTemplateDataTypeMissingProperty,
				DataTypePropertyName.ALLOW_NULL
			)

		val required = properties.find { it.identifier == DataTypePropertyId.REQUIRED }
			?: return ApiError(
				HttpStatus.NOT_ACCEPTABLE,
				ApplicationErrorCodes.ValidationAttributeTemplateDataTypeMissingProperty,
				DataTypePropertyName.REQUIRED
			)

		if (validateContent && !allowNull.value.toBoolean() && required.value.toBoolean()) {
			if (contents == null || contents.isBlank())
				return ApiError(
					HttpStatus.NOT_ACCEPTABLE,
					ApplicationErrorCodes.ValidationAttributeTemplateDataTypeContentsIsRequired,
					""
				)

			try {
				contents.toBigDecimal()
			} catch (ex: NumberFormatException) {
				return ApiError(
					HttpStatus.NOT_ACCEPTABLE,
					ApplicationErrorCodes.ValidationAttributeTemplateDataTypeContentsIsNotMatchingDataType,
					contents
				)
			}
		}

		var property = properties.find { it.identifier == DataTypePropertyId.MIN_VALUE }
			?: return ApiError(
				HttpStatus.NOT_ACCEPTABLE,
				ApplicationErrorCodes.ValidationAttributeTemplateDataTypeMissingProperty,
				DataTypePropertyName.MIN_VALUE
			)

		try {
			val value = property.value.toBigDecimal()
			if (validateContent && contents != null && contents.toBigDecimal() < value)
				return ApiError(
					HttpStatus.NOT_ACCEPTABLE,
					ApplicationErrorCodes.ValidationAttributeTemplateDataTypeContentsIsLowerThanMinimum,
					""
				)
		} catch (ex: NumberFormatException) {
			return ApiError(
				HttpStatus.NOT_ACCEPTABLE,
				ApplicationErrorCodes.ValidationAttributeTemplateDataTypePropertyValueIsNotMatchingPropertyType,
				DataTypePropertyName.MIN_VALUE,
				property.value
			)
		}

		property = properties.find { it.identifier == DataTypePropertyId.MAX_VALUE }
			?: return ApiError(
				HttpStatus.NOT_ACCEPTABLE,
				ApplicationErrorCodes.ValidationAttributeTemplateDataTypeMissingProperty,
				DataTypePropertyName.MAX_VALUE
			)

		try {
			val value = property.value.toBigDecimal()
			if (validateContent && contents != null && contents.toBigDecimal() > value)
				return ApiError(
					HttpStatus.NOT_ACCEPTABLE,
					ApplicationErrorCodes.ValidationAttributeTemplateDataTypeContentsIsHigherThanMaximum,
					""
				)
		} catch (ex: NumberFormatException) {
			return ApiError(
				HttpStatus.NOT_ACCEPTABLE,
				ApplicationErrorCodes.ValidationAttributeTemplateDataTypePropertyValueIsNotMatchingPropertyType,
				DataTypePropertyName.MAX_VALUE,
				property.value
			)
		}

		properties.find { it.identifier == DataTypePropertyId.SHOW_SPIN_BUTTONS }
			?: return ApiError(
				HttpStatus.NOT_ACCEPTABLE,
				ApplicationErrorCodes.ValidationAttributeTemplateDataTypeMissingProperty,
				DataTypePropertyName.SHOW_SPIN_BUTTONS
			)

		properties.find { it.identifier == DataTypePropertyId.SYMBOL_POSITION }
			?: return ApiError(
				HttpStatus.NOT_ACCEPTABLE,
				ApplicationErrorCodes.ValidationAttributeTemplateDataTypeMissingProperty,
				DataTypePropertyName.SYMBOL_POSITION
			)

		properties.find { it.identifier == DataTypePropertyId.SYMBOL }
			?: return ApiError(
				HttpStatus.NOT_ACCEPTABLE,
				ApplicationErrorCodes.ValidationAttributeTemplateDataTypeMissingProperty,
				DataTypePropertyName.SYMBOL
			)

		properties.find { it.identifier == DataTypePropertyId.NEGATIVE_SYMBOL }
			?: return ApiError(
				HttpStatus.NOT_ACCEPTABLE,
				ApplicationErrorCodes.ValidationAttributeTemplateDataTypeMissingProperty,
				DataTypePropertyName.NEGATIVE_SYMBOL
			)

		properties.find { it.identifier == DataTypePropertyId.DECIMAL_SEPARATOR }
			?: return ApiError(
				HttpStatus.NOT_ACCEPTABLE,
				ApplicationErrorCodes.ValidationAttributeTemplateDataTypeMissingProperty,
				DataTypePropertyName.DECIMAL_SEPARATOR
			)

		properties.find { it.identifier == DataTypePropertyId.DIGITS }
			?: return ApiError(
				HttpStatus.NOT_ACCEPTABLE,
				ApplicationErrorCodes.ValidationAttributeTemplateDataTypeMissingProperty,
				DataTypePropertyName.DIGITS
			)

		properties.find { it.identifier == DataTypePropertyId.GROUP_SEPARATOR }
			?: return ApiError(
				HttpStatus.NOT_ACCEPTABLE,
				ApplicationErrorCodes.ValidationAttributeTemplateDataTypeMissingProperty,
				DataTypePropertyName.GROUP_SEPARATOR
			)

		properties.find { it.identifier == DataTypePropertyId.GROUP_SIZE }
			?: return ApiError(
				HttpStatus.NOT_ACCEPTABLE,
				ApplicationErrorCodes.ValidationAttributeTemplateDataTypeMissingProperty,
				DataTypePropertyName.GROUP_SIZE
			)

		properties.find { it.identifier == DataTypePropertyId.DECIMAL_DIGITS }
			?: return ApiError(
				HttpStatus.NOT_ACCEPTABLE,
				ApplicationErrorCodes.ValidationAttributeTemplateDataTypeMissingProperty,
				DataTypePropertyName.DECIMAL_DIGITS
			)

		return null
	}
}