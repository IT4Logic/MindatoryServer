package com.it4logic.mindatory.services

import com.it4logic.mindatory.api.plugins.AttributeTemplateDataType
import com.it4logic.mindatory.api.plugins.AttributeTemplateDataTypeManager
import com.it4logic.mindatory.api.plugins.MindatoryPlugin
import com.it4logic.mindatory.api.plugins.MindatoryPluginManagerCodes
import com.it4logic.mindatory.exceptions.ApplicationErrorCodes
import com.it4logic.mindatory.exceptions.ApplicationObjectNotFoundException
import org.pf4j.spring.SpringPluginManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional

@Service
@Transactional
class RepositoryManagerService {
    @Autowired
    lateinit var pluginManager: SpringPluginManager

    @PreAuthorize("isFullyAuthenticated()")
    fun getAttributeTemplateDataTypes() : List<AttributeTemplateDataType> {
        val attributeDataTypes = mutableListOf<AttributeTemplateDataType>()
        val plugins = pluginManager.getExtensions(MindatoryPlugin::class.java)
        for(plugin in plugins) {
            val manager = plugin.getManager(MindatoryPluginManagerCodes.AttributeTemplateDataTypeManager)
            if(!manager.isPresent)
                continue
            attributeDataTypes.addAll((manager.get() as AttributeTemplateDataTypeManager).dataTypes())
        }
        return attributeDataTypes
    }

    @PreAuthorize("isFullyAuthenticated()")
    fun getAttributeTemplateDataType(typeUUID: String): AttributeTemplateDataType {
        val plugins = pluginManager.getExtensions(MindatoryPlugin::class.java)
        for(plugin in plugins) {
            val manager = plugin.getManager(MindatoryPluginManagerCodes.AttributeTemplateDataTypeManager)
            if(!manager.isPresent)
                continue
            val dataType = (manager.get() as AttributeTemplateDataTypeManager).dataType(UUID.fromString(typeUUID))
            if(dataType != null)
                return dataType
        }
        throw ApplicationObjectNotFoundException(typeUUID, ApplicationErrorCodes.NotFoundAttributeTemplateDataType)
    }

    fun getAttributeTemplateDataTypeManager(typeUUID: String) : AttributeTemplateDataTypeManager {
        val plugins = pluginManager.getExtensions(MindatoryPlugin::class.java)
        for(plugin in plugins) {
            val manager = plugin.getManager(MindatoryPluginManagerCodes.AttributeTemplateDataTypeManager)
            if(!manager.isPresent)
                continue
            if((manager.get() as AttributeTemplateDataTypeManager).dataType(UUID.fromString(typeUUID)) != null)
                return (manager.get() as AttributeTemplateDataTypeManager)
        }
        throw ApplicationObjectNotFoundException(typeUUID, ApplicationErrorCodes.NotFoundAttributeTemplateDataType)
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


}