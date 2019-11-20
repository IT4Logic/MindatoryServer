package com.it4logic.mindatory.attribute_template.datatypes

import com.it4logic.mindatory.exceptions.ApiError
import com.it4logic.mindatory.model.model.AttributeTemplateProperty
import java.util.*

class DropDownDataType : AttributeTemplateDataType {

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


//	override fun buildControl(properties: Map<String, Any>, contents: JsonNode): String {
//		return ""
//	}

	override fun validateDataTypeProperties(properties: MutableList<AttributeTemplateProperty>): ApiError? {
		/*
		if (!properties.containsKey(DataTypeProperty.REQUIRED))
			return MindatoryApiError(PluginErrorCodes.MissingProperty, DataTypeProperty.REQUIRED)
		if (!properties.containsKey(DataTypeProperty.MIN_LENGTH))
			return MindatoryApiError(PluginErrorCodes.MissingProperty, DataTypeProperty.MIN_LENGTH)
		if (!properties.containsKey(DataTypeProperty.MAX_LENGTH))
			return MindatoryApiError(PluginErrorCodes.MissingProperty, DataTypeProperty.MAX_LENGTH)
		if (!properties.containsKey(DataTypeProperty.PATTERN))
			return MindatoryApiError(PluginErrorCodes.MissingProperty, DataTypeProperty.PATTERN)

		if (properties[DataTypeProperty.REQUIRED] !is Boolean)
			return MindatoryApiError(
				PluginErrorCodes.PropertyValueIsNotMatchingPropertyType, DataTypeProperty.REQUIRED,
				subErrors = arrayListOf(MindatoryApiSubError(properties[DataTypeProperty.REQUIRED].toString()))
			)

		if (properties[DataTypeProperty.MIN_LENGTH] !is Long)
			return MindatoryApiError(
				PluginErrorCodes.PropertyValueIsNotMatchingPropertyType, DataTypeProperty.MIN_LENGTH,
				subErrors = arrayListOf(MindatoryApiSubError(properties[DataTypeProperty.MIN_LENGTH].toString()))
			)

		if (properties[DataTypeProperty.MAX_LENGTH] !is Long)
			return MindatoryApiError(
				PluginErrorCodes.PropertyValueIsNotMatchingPropertyType, DataTypeProperty.MAX_LENGTH,
				subErrors = arrayListOf(MindatoryApiSubError(properties[DataTypeProperty.MAX_LENGTH].toString()))
			)

		if (properties[DataTypeProperty.PATTERN] !is String)
			return MindatoryApiError(
				PluginErrorCodes.PropertyValueIsNotMatchingPropertyType, DataTypeProperty.PATTERN,
				subErrors = arrayListOf(MindatoryApiSubError(properties[DataTypeProperty.PATTERN].toString()))
			)
	*/
		return null
	}

	override fun validateDataTypeContents(
		contents: Any,
		properties: MutableList<AttributeTemplateProperty>
	): ApiError? {
		/*
		val error = validateDataTypeProperties(properties)
		if (error != null)
			return error

		val required = properties[DataTypeProperty.REQUIRED].toString().toBoolean()
		if (required && contents.isNull)
			return MindatoryApiError(PluginErrorCodes.ValidationContentsIsRequired, "")

		if (!contents.isTextual)
			return MindatoryApiError(PluginErrorCodes.ContentsIsNotMatchingDataType, "")

		val value = contents.asText()

		val minLength = properties[DataTypeProperty.MIN_LENGTH].toString().toLong()
		if (minLength != -1L && value.length < minLength)
			return MindatoryApiError(PluginErrorCodes.ValidationContentsLengthIsLowerThanMinimum, "")

		val maxLength = properties[DataTypeProperty.MAX_LENGTH].toString().toLong()
		if (maxLength != -1L && value.length < maxLength)
			return MindatoryApiError(PluginErrorCodes.ValidationContentsLengthIsHigherThanMinimum, "")

		val pattern = properties[DataTypeProperty.PATTERN].toString().toRegex()
		if (!pattern.matches(value))
			return MindatoryApiError(PluginErrorCodes.ValidationContentsIsNotMatchingPattern, "")
	*/
		return null
	}

	override fun migrateStoreContent(contents: Any, properties: MutableList<AttributeTemplateProperty>): Any {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

//private fun getResourceBundle(name: String = "AttributeDataTypes"): ResourceBundle {
//	var locale = Locale("en")
//
//	if (localeString != null)
//		locale = Locale(localeString)
//
//	return ResourceBundle.getBundle(name, locale)
//}
}