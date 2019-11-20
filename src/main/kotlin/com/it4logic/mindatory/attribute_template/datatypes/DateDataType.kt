package com.it4logic.mindatory.attribute_template.datatypes

import com.it4logic.mindatory.exceptions.ApiError
import com.it4logic.mindatory.model.model.AttributeTemplateProperty
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