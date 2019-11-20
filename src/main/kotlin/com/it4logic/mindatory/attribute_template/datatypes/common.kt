package com.it4logic.mindatory.attribute_template.datatypes

import com.it4logic.mindatory.exceptions.ApiError
import com.it4logic.mindatory.model.model.AttributeTemplateProperty
import java.util.*

abstract class DataTypePropertyDataType {
	companion object {
		const val String = "25fe3cf6-a0dd-444e-bcaa-ccc09f5a29f1"
		const val Number = "c39a5421-9221-4849-8bf7-87d8732ed30e"
		const val Decimal = "38d26e11-c9eb-46c6-a637-ac7839275007"
		const val Boolean = "eda23e77-16f1-431f-b5b8-61f1738497b0"
		const val Date = "db5dcc26-364d-45eb-ba2c-c7b38d9a07f6"
		const val DropDown = "0d1ced6b-ff94-4e65-8c9a-98e672dc47a2"
		const val Json = "1fb482f3-e81a-463f-9bd6-c71c5cbf7139"
	}
}

class DataTypePropertyId {
	companion object {
		const val MIN_LENGTH = "0bbfc4c3-d471-4961-954b-88a0af89f028"
		const val MAX_LENGTH = "db226cc7-7c95-4dd4-898c-513a5224da50"
		const val REQUIRED = "54bdac67-c190-4647-98c3-e9c296a46e40"
		const val PATTERN = "7ba9e549-cca9-46b9-86c3-fd4b7500d3b6"
		const val MIN_VALUE = "210dea1b-2e39-459f-ba10-10d1d2f9ec55"
		const val MAX_VALUE = "66ea0577-30c3-4669-a696-af6e4ae6f14f"
		//		const val USE_DECIMAL_POINT = "53d32fda-8236-41f6-894f-531dace244fa"
		const val TYPE = "1f8b9701-ca81-4826-a485-3a2ac8099fd0"
		const val FORMAT = "477f4d2e-62ac-4c57-8ed0-bfdd13596d27"
		const val DATA = "e59c95e0-7efc-4e26-af67-26786ebc4be8"
		const val SHOW_SPIN_BUTTONS = "74dd8f01-db1c-494a-a95a-635a5fd2e115"
		const val SYMBOL_POSITION = "367362f9-5a3c-4ec8-8438-c7543ff3defe"
		const val SYMBOL = "dce0b76a-8e4f-44dc-8d83-daeffea1f7f1"
		const val NEGATIVE_SYMBOL = "df510842-145e-4c12-b79d-5ff2fd1a3c9a"
		const val ALLOW_NULL = "aec53a6b-3c76-4938-ad63-d40f4de1e128"
		const val DECIMAL_DIGITS = "232ea48f-496b-46b0-84b4-cf65d2364681"
		const val DECIMAL_SEPARATOR = "e70d784b-671b-4d6a-8185-3d71555c7faa"
		const val DIGITS = "d1e5ec11-ae71-476f-aea9-483d54e66676"
		const val GROUP_SEPARATOR = "d7b7f328-adaa-4633-ae15-cf394c2f43b2"
		const val GROUP_SIZE = "06940be8-bf26-467a-b85c-8b0ab88365ab"
		const val CALENDAR = "b35d4e64-a2ad-453d-9750-4f9d5531c11d"
		const val DEFAULT_VALUE = "ac078a85-e3e4-41be-a8f8-cc6ce35078c9"
		const val MULTI_LINE = "d3c41d9f-1a2c-4fc9-af9e-fd951f70820d"
	}
}

class DataTypePropertyName {
	companion object {
		const val MIN_LENGTH = "mindatory.attribute-template.property-name.min_length"
		const val MAX_LENGTH = "mindatory.attribute-template.property-name.max_length"
		const val REQUIRED = "mindatory.attribute-template.property-name.required"
		const val PATTERN = "mindatory.attribute-template.property-name.pattern"
		const val MIN_VALUE = "mindatory.attribute-template.property-name.min_value"
		const val MAX_VALUE = "mindatory.attribute-template.property-name.max_value"
		//		const val USE_DECIMAL_POINT = "mindatory.attribute-template.property-name.use_decimal_point"
		const val TYPE = "mindatory.attribute-template.property-name.type"
		const val FORMAT = "mindatory.attribute-template.property-name.format"
		const val DATA = "mindatory.attribute-template.property-name.data"
		const val SHOW_SPIN_BUTTONS = "mindatory.attribute-template.property-name.show_spin_buttons"
		const val SYMBOL_POSITION = "mindatory.attribute-template.property-name.symbol_position"
		const val SYMBOL = "mindatory.attribute-template.property-name.symbol"
		const val NEGATIVE_SYMBOL = "mindatory.attribute-template.property-name.negative_symbol"
		const val ALLOW_NULL = "mindatory.attribute-template.property-name.allow_null"
		const val DECIMAL_DIGITS = "mindatory.attribute-template.property-name.decimal_digits"
		const val DECIMAL_SEPARATOR = "mindatory.attribute-template.property-name.decimal_separator"
		const val DIGITS = "mindatory.attribute-template.property-name.digits"
		const val GROUP_SEPARATOR = "mindatory.attribute-template.property-name.group_separator"
		const val GROUP_SIZE = "mindatory.attribute-template.property-name.group_size"
		const val CALENDAR = "mindatory.attribute-template.property-name.calendar"
		const val DEFAULT_VALUE = "mindatory.attribute-template.property-name.default-value"
		const val MULTI_LINE = "mindatory.attribute-template.property-name.multi-line"
	}
}

class DataTypeName {
	companion object {
		const val Text = "mindatory.attribute-template.name.text"
		const val Number = "mindatory.attribute-template.name.number"
		const val Color = "mindatory.attribute-template.name.color"
		const val Date = "mindatory.attribute-template.name.date"
		const val Image = "mindatory.attribute-template.name.image"
		const val DropDown = "mindatory.attribute-template.name.drop-down"
		const val Status = "mindatory.attribute-template.name.status"
		const val RichText = "mindatory.attribute-template.name.rich-text"
	}
}

enum class DataTypeUUID(private val uuid: UUID) {
	Text(UUID.fromString("cd59f8d2-02d5-4175-97d8-be1bdec3c2a5")),
	Number(UUID.fromString("dd4bf72f-8689-4653-b2b0-c8e59a592a80")),
	Color(UUID.fromString("e8020525-d1e1-4f64-9d38-7b863aa320aa")),
	Date(UUID.fromString("791abac4-99ac-4ee2-8eeb-874dd3a34b4b")),
	Image(UUID.fromString("6b9c0883-d90b-405e-9d33-46526ec18e23")),
	DropDown(UUID.fromString("06e6fd05-4bfa-4750-b4b3-97aa00cbecc2")),
	Status(UUID.fromString("f2e8c309-cda0-4aa6-8f40-fa4552cedc43")),
	RichText(UUID.fromString("1403ad50-d974-4a22-aa86-efcff79f8f31"));

	fun toUUID(): UUID = this.uuid
}

data class AttributeTemplateDataTypeProperty(
	var identifier: String,
	var name: String,
	var description: String,
	var type: String,
	var value: Any
)

interface AttributeTemplateDataType {
	val identifier: UUID

	val name: String

//	var description: String

	val properties: List<AttributeTemplateDataTypeProperty>

//	fun buildControl(
//		properties: Map<String, Any>,
//		contents: JsonNode
//	): String

	fun validateDataTypeProperties(
		properties: MutableList<AttributeTemplateProperty>
	): ApiError?

	fun validateDataTypeContents(
		contents: Any,
		properties: MutableList<AttributeTemplateProperty>
	): ApiError?

	fun migrateStoreContent(
		contents: Any,
		properties: MutableList<AttributeTemplateProperty>
	): Any

//	fun updateLocale(localeString: String?)
}
