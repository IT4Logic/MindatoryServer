package com.it4logic.mindatory.attribute_template.datatypes

import com.it4logic.mindatory.exceptions.ApiError
import com.it4logic.mindatory.model.model.AttributeTemplateProperty
import java.util.*

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


//	override fun buildControl(properties: Map<String, Any>, contents: JsonNode): String {
//		return ""
//	}

	override fun validateDataTypeProperties(properties: MutableList<AttributeTemplateProperty>): ApiError? {
		/*
		if (!properties.containsKey(DataTypeProperty.REQUIRED))
			return MindatoryApiError(PluginErrorCodes.MissingProperty, DataTypeProperty.REQUIRED)
		if (!properties.containsKey(DataTypeProperty.MIN_VALUE))
			return MindatoryApiError(PluginErrorCodes.MissingProperty, DataTypeProperty.MIN_VALUE)
		if (!properties.containsKey(DataTypeProperty.MAX_VALUE))
			return MindatoryApiError(PluginErrorCodes.MissingProperty, DataTypeProperty.MAX_VALUE)

		if (properties[DataTypeProperty.REQUIRED] !is Boolean)
			return MindatoryApiError(
				PluginErrorCodes.PropertyValueIsNotMatchingPropertyType, DataTypeProperty.REQUIRED,
				subErrors = arrayListOf(MindatoryApiSubError(properties[DataTypeProperty.REQUIRED].toString()))
			)

		if (properties[DataTypeProperty.MIN_VALUE] !is Long)
			return MindatoryApiError(
				PluginErrorCodes.PropertyValueIsNotMatchingPropertyType, DataTypeProperty.MIN_VALUE,
				subErrors = arrayListOf(MindatoryApiSubError(properties[DataTypeProperty.MIN_VALUE].toString()))
			)

		if (properties[DataTypeProperty.MAX_VALUE] !is Long)
			return MindatoryApiError(
				PluginErrorCodes.PropertyValueIsNotMatchingPropertyType, DataTypeProperty.MAX_VALUE,
				subErrors = arrayListOf(MindatoryApiSubError(properties[DataTypeProperty.MAX_VALUE].toString()))
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

		if (!contents.isIntegralNumber)
			return MindatoryApiError(PluginErrorCodes.ContentsIsNotMatchingDataType, "")

		val value = contents.asLong()

		val minValue = properties[DataTypeProperty.MIN_VALUE].toString().toLong()
		if (minValue != -1L && value < minValue)
			return MindatoryApiError(PluginErrorCodes.ValidationContentsIsLowerThanMinimum, "")

		val maxValue = properties[DataTypeProperty.MAX_VALUE].toString().toLong()
		if (maxValue != -1L && value < maxValue)
			return MindatoryApiError(PluginErrorCodes.ValidationContentsIsHigherThanMinimum, "")
	*/
		return null
	}

	override fun migrateStoreContent(contents: Any, properties: MutableList<AttributeTemplateProperty>): Any {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
//
//	private fun getResourceBundle(name: String = "AttributeDataTypes"): ResourceBundle {
//		var locale = Locale("en")
//
//		if (localeString != null)
//			locale = Locale(localeString)
//
//		return ResourceBundle.getBundle(name, locale)
//	}
}