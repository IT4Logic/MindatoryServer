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

import com.it4logic.mindatory.exceptions.*
import java.util.*

/**
 * Attribute Template Data Types Manager
 */
class DataTypeManager {
	/**
	 * Contains available data types
	 */
	private var mindatoryAttributeTemplateDataTypeManagers: List<AttributeTemplateDataType> = listOf(
		TextDataType(),
		NumberDataType(),
		ColorDataType(),
		DateDataType(),
		DropDownDataType(),
		ImageDataType(),
		RichTextDataType(),
		StatusDataType()
	)

	/**
	 * Retrieves available data types list
	 * @return Available data types list
	 */
	fun dataTypes(): List<AttributeTemplateDataType> {

		val attributeTemplateDataTypes: ArrayList<AttributeTemplateDataType> = arrayListOf()

		for (mindatoryDataType in mindatoryAttributeTemplateDataTypeManagers) {
			attributeTemplateDataTypes.add(mindatoryDataType)
		}

		return attributeTemplateDataTypes
	}

	/**
	 * Retrieves data type according to its UUID
	 * @param dataTypeUUID Data type UUID
	 * @return Data type object, or [ApplicationObjectNotFoundException] will be thrown
	 */
	fun dataType(dataTypeUUID: UUID): AttributeTemplateDataType {
		for (mindatoryDataType in mindatoryAttributeTemplateDataTypeManagers) {
			if (mindatoryDataType.identifier == dataTypeUUID) {
				return mindatoryDataType
			}
		}

		throw ApplicationObjectNotFoundException(dataTypeUUID, ApplicationErrorCodes.ValidationAttributeTemplateDataTypeDoesNotExist)
	}
}