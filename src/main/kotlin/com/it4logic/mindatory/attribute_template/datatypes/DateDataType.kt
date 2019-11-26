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
import org.joda.time.chrono.ISOChronology
import org.joda.time.chrono.IslamicChronology
import org.joda.time.format.DateTimeFormat
import org.springframework.http.HttpStatus
import java.util.*


class DateDataType : AttributeTemplateDataType {

	override val identifier: UUID
		get() = DataTypeUUID.Date.toUUID()

	override val name: String
		get() = DataTypeName.Date

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
				DataTypePropertyId.CALENDAR,
				DataTypePropertyName.CALENDAR,
				"",
				DataTypePropertyDataType.DropDown,
				"1:mindatory.attribute-template.date.calendar.gregorian," +
						"2:mindatory.attribute-template.date.calendar.hijri," +
						"3:mindatory.attribute-template.date.calendar.umm-al-qura"
			),
			AttributeTemplateDataTypeProperty(
				DataTypePropertyId.MIN_VALUE,
				DataTypePropertyName.MIN_VALUE,
				"",
				DataTypePropertyDataType.String,
				""
			),
			AttributeTemplateDataTypeProperty(
				DataTypePropertyId.MAX_VALUE,
				DataTypePropertyName.MAX_VALUE,
				"",
				DataTypePropertyDataType.String,
				""
			),
			AttributeTemplateDataTypeProperty(
				DataTypePropertyId.FORMAT,
				DataTypePropertyName.FORMAT,
				"",
				DataTypePropertyDataType.String,
				"dd/mm/yyyy"
			),
			AttributeTemplateDataTypeProperty(
				DataTypePropertyId.DEFAULT_VALUE,
				DataTypePropertyName.DEFAULT_VALUE,
				"",
				DataTypePropertyDataType.DropDown,
				"1:mindatory.attribute-template.date.none," +
						"2:mindatory.attribute-template.date.current-date"
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

		var property = properties.find { it.identifier == DataTypePropertyId.CALENDAR }
			?: return ApiError(
				HttpStatus.NOT_ACCEPTABLE,
				ApplicationErrorCodes.ValidationAttributeTemplateDataTypeMissingProperty,
				DataTypePropertyName.CALENDAR
			)

		if (required.value.toBoolean() && property.value.isBlank())
			return ApiError(
				HttpStatus.NOT_ACCEPTABLE,
				ApplicationErrorCodes.ValidationAttributeTemplateDataTypePropertyValueIsNotInRange,
				DataTypePropertyName.CALENDAR
			)

		val calendarType: Int
		try {
			calendarType = property.value.toInt()
			if (calendarType < 1 || calendarType > 3)
				return ApiError(
					HttpStatus.NOT_ACCEPTABLE,
					ApplicationErrorCodes.ValidationAttributeTemplateDataTypePropertyValueIsNotInRange,
					""
				)
		} catch (ex: NumberFormatException) {
			return ApiError(
				HttpStatus.NOT_ACCEPTABLE,
				ApplicationErrorCodes.ValidationAttributeTemplateDataTypePropertyValueIsNotMatchingPropertyType,
				DataTypePropertyName.CALENDAR,
				property.value
			)
		}

		val format = properties.find { it.identifier == DataTypePropertyId.FORMAT }
			?: return ApiError(
				HttpStatus.NOT_ACCEPTABLE,
				ApplicationErrorCodes.ValidationAttributeTemplateDataTypeMissingProperty,
				DataTypePropertyName.FORMAT
			)

		// we need to get the suitable calendar instance according to calendar type
		val calInstance = if (calendarType == 1) {
			ISOChronology.getInstance()
		} else {
			IslamicChronology.getInstance()
		}

		val dtFormat = if(format.value.isBlank()) {
			DateTimeFormat.fullDate().withChronology(calInstance)
		} else {
			DateTimeFormat.forPattern(format.value).withChronology(calInstance)
		}

		property = properties.find { it.identifier == DataTypePropertyId.MIN_VALUE }
			?: return ApiError(
				HttpStatus.NOT_ACCEPTABLE,
				ApplicationErrorCodes.ValidationAttributeTemplateDataTypeMissingProperty,
				DataTypePropertyName.MIN_VALUE
			)

		try {
			if (validateContent && contents != null) {
				val dtValue = dtFormat.parseDateTime(property.value)
				val dtContents = dtFormat.parseDateTime(contents)

				if (dtContents < dtValue)
					return ApiError(
						HttpStatus.NOT_ACCEPTABLE,
						ApplicationErrorCodes.ValidationAttributeTemplateDataTypeContentsIsLowerThanMinimum,
						""
					)
			}
		} catch (ex: Exception) {
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
			if (validateContent && contents != null) {
				val dtValue = dtFormat.parseDateTime(property.value)
				val dtContents = dtFormat.parseDateTime(contents)

				if (dtContents < dtValue)
					return ApiError(
						HttpStatus.NOT_ACCEPTABLE,
						ApplicationErrorCodes.ValidationAttributeTemplateDataTypeContentsIsHigherThanMaximum,
						""
					)
			}
		} catch (ex: Exception) {
			return ApiError(
				HttpStatus.NOT_ACCEPTABLE,
				ApplicationErrorCodes.ValidationAttributeTemplateDataTypePropertyValueIsNotMatchingPropertyType,
				DataTypePropertyName.MAX_VALUE,
				property.value
			)
		}

		property = properties.find { it.identifier == DataTypePropertyId.DEFAULT_VALUE }
			?: return ApiError(
				HttpStatus.NOT_ACCEPTABLE,
				ApplicationErrorCodes.ValidationAttributeTemplateDataTypeMissingProperty,
				DataTypePropertyName.DEFAULT_VALUE
			)

		try {
			val defaultValue = property.value.toInt()
			if (defaultValue < 1 || defaultValue > 2)
				return ApiError(
					HttpStatus.NOT_ACCEPTABLE,
					ApplicationErrorCodes.ValidationAttributeTemplateDataTypePropertyValueIsNotInRange,
					""
				)
		} catch (ex: NumberFormatException) {
			return ApiError(
				HttpStatus.NOT_ACCEPTABLE,
				ApplicationErrorCodes.ValidationAttributeTemplateDataTypePropertyValueIsNotMatchingPropertyType,
				DataTypePropertyName.DEFAULT_VALUE,
				property.value
			)
		}

		return null
	}
}