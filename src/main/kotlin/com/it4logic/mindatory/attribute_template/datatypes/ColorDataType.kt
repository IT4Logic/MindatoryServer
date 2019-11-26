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
 * Attribute Template Color Data Type management class
 */
class ColorDataType : AttributeTemplateDataType {

	override val identifier: UUID
		get() = DataTypeUUID.Color.toUUID()

	override val name: String
		get() = DataTypeName.Color

	override val properties: List<AttributeTemplateDataTypeProperty>
		get() = listOf(
			AttributeTemplateDataTypeProperty(
				DataTypePropertyId.REQUIRED,
				DataTypePropertyName.REQUIRED,
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

		return null
	}
}