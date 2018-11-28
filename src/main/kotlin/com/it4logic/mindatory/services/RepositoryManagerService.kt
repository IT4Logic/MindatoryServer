package com.it4logic.mindatory.services

import com.it4logic.mindatory.api.plugins.AttributeTemplateDataType
import com.it4logic.mindatory.api.plugins.AttributeTemplateDataTypeManager
import com.it4logic.mindatory.api.plugins.MindatoryPlugin
import com.it4logic.mindatory.exceptions.ApplicationErrorCodes
import com.it4logic.mindatory.exceptions.ApplicationObjectNotFoundException
import org.pf4j.spring.SpringPluginManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class RepositoryManagerService {
    @Autowired
    lateinit var pluginManager: SpringPluginManager

    fun getAttributeTemplateDataTypes() : List<AttributeTemplateDataType> {
        val attributeDataTypes = mutableListOf<AttributeTemplateDataType>()
        val plugins = pluginManager.getExtensions(MindatoryPlugin::class.java)
        for(plugin in plugins) {
            attributeDataTypes.addAll(plugin.attributeTemplateDataTypeManager().dataTypes())
        }
        return attributeDataTypes
    }

    fun getAttributeTemplateDataType(typeUUID: String): AttributeTemplateDataType {
        val plugins = pluginManager.getExtensions(MindatoryPlugin::class.java)
        for(plugin in plugins) {
            val dataType = plugin.attributeTemplateDataTypeManager().dataType(UUID.fromString(typeUUID))
            if(dataType != null)
                return dataType
        }
        throw ApplicationObjectNotFoundException(typeUUID, ApplicationErrorCodes.NotFoundAttributeTemplateDataType)
    }

    fun getAttributeTemplateDataTypeManager(typeUUID: String) : AttributeTemplateDataTypeManager {
        val plugins = pluginManager.getExtensions(MindatoryPlugin::class.java)
        for(plugin in plugins) {
            val manager = plugin.attributeTemplateDataTypeManager()
            if(manager.dataType(UUID.fromString(typeUUID)) != null)
                return manager
        }
        throw ApplicationObjectNotFoundException(typeUUID, ApplicationErrorCodes.NotFoundAttributeTemplateDataType)
    }

    fun hasAttributeTemplateDataType(typeUUID: String) : Boolean {
        val plugins = pluginManager.getExtensions(MindatoryPlugin::class.java)
        for(plugin in plugins) {
            if(plugin.attributeTemplateDataTypeManager().dataType(UUID.fromString(typeUUID)) != null)
                return true
        }
        return false
    }


}