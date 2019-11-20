package com.it4logic.mindatory.services

import com.it4logic.mindatory.attribute_template.datatypes.AttributeTemplateDataType
import com.it4logic.mindatory.attribute_template.datatypes.DataTypeManager
import com.it4logic.mindatory.exceptions.ApplicationErrorCodes
import com.it4logic.mindatory.exceptions.ApplicationObjectNotFoundException
import com.it4logic.mindatory.mlc.LanguageManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional

@Service
@Transactional
class AttributeTemplateDataTypeManagerService {
	val dataTypeManager = DataTypeManager()

	@Autowired
	private lateinit var languageManager: LanguageManager

	@PreAuthorize("isFullyAuthenticated()")
	fun getAttributeTemplateDataTypes(): List<AttributeTemplateDataType> {
		return dataTypeManager.dataTypes()
	}

	@PreAuthorize("isFullyAuthenticated()")
	@Suppress("SENSELESS_COMPARISON")
	fun getAttributeTemplateDataType(typeUUID: String): AttributeTemplateDataType {
		val dataType = dataTypeManager.dataType(UUID.fromString(typeUUID))
		if (dataType != null)
			return dataType
		throw ApplicationObjectNotFoundException(typeUUID, ApplicationErrorCodes.NotFoundAttributeTemplateDataType)
	}

//	fun getAttributeTemplateDataTypePlugin(typeUUID: String): MindatoryPlugin {
//		val plugins = pluginManager.getExtensions(MindatoryPlugin::class.java)
//		for (plugin in plugins) {
//			val manager = plugin.getManager(MindatoryPluginManagerCodes.AttributeTemplateDataTypeManager)
//			if (!manager.isPresent)
//				continue
//			if ((manager.get() as AttributeTemplateDataTypeManager).dataType(
//					UUID.fromString(typeUUID),
//					languageManager.currentLanguage.locale
//				) != null
//			) {
//				return plugin
//			}
//		}
//		throw ApplicationObjectNotFoundException(typeUUID, ApplicationErrorCodes.NotFoundAttributeTemplateDataType)
//	}
/*
	fun getAttributeTemplateDataTypeManager(typeUUID: String): AttributeTemplateDataTypeManager {
		val plugins = pluginManager.getExtensions(MindatoryPlugin::class.java)
		for (plugin in plugins) {
			val manager = plugin.getManager(MindatoryPluginManagerCodes.AttributeTemplateDataTypeManager)
			if (!manager.isPresent)
				continue
			if ((manager.get() as AttributeTemplateDataTypeManager).dataType(
					UUID.fromString(typeUUID),
					languageManager.currentLanguage.locale
				) != null
			)
				return (manager.get() as AttributeTemplateDataTypeManager)
		}
		throw ApplicationObjectNotFoundException(typeUUID, ApplicationErrorCodes.NotFoundAttributeTemplateDataType)
	}




	fun validateDataTypeProperties(
		typeUUID: String,
		properties: HashMap<String, Any>
	): MindatoryApiError? {
		return getAttributeTemplateDataTypeManager(
			typeUUID
		).validateDataTypeProperties(
			UUID.fromString(typeUUID),
			properties,
			languageManager.currentLanguage.locale
		)
	}

//    fun hasAttributeTemplateDataType(typeUUID: String) : Boolean {
//        val plugins = pluginManager.getExtensions(MindatoryPlugin::class.java)
//        for(plugin in plugins) {
//            val manager = plugin.getManager(MindatoryPluginManagerCodes.AttributeTemplateDataTypeManager)
//            if(!manager.isPresent)
//                continue
//            if((manager.get() as AttributeTemplateDataTypeManager).dataType(UUID.fromString(typeUUID)) != null)
//                return true
//        }
//        return false
//    }
*/

}