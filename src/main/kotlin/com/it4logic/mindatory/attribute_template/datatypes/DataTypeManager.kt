package com.it4logic.mindatory.attribute_template.datatypes

import com.it4logic.mindatory.exceptions.*
import java.util.*

class DataTypeManager {
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

	fun dataTypes(): List<AttributeTemplateDataType> {

		val attributeTemplateDataTypes: ArrayList<AttributeTemplateDataType> = arrayListOf()

		for (mindatoryDataType in mindatoryAttributeTemplateDataTypeManagers) {
			attributeTemplateDataTypes.add(mindatoryDataType)
		}

		return attributeTemplateDataTypes
	}

	fun dataType(dataTypeUUID: UUID): AttributeTemplateDataType {
		for (mindatoryDataType in mindatoryAttributeTemplateDataTypeManagers) {
			if (mindatoryDataType.identifier == dataTypeUUID) {
				return mindatoryDataType
			}
		}

		throw ApplicationObjectNotFoundException(dataTypeUUID, ApplicationErrorCodes.DataTypeDoesNotExist)
	}
}