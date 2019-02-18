/*
    Copyright (c) 2017, IT4Logic.

    This file is part of Mindatory solution by IT4Logic.

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

package com.it4logic.mindatory.mlc

import com.it4logic.mindatory.model.SolutionMLCRepository
import com.it4logic.mindatory.model.common.ApplicationEntityBase
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntity
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntityRepository
import com.it4logic.mindatory.services.common.ApplicationBaseService
import com.it4logic.mindatory.services.common.ApplicationServiceRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import javax.transaction.Transactional
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.*

/**
 * Service responsible for managing Multiple Language Content functionalities
 */
@Service
@Transactional
class MultipleLanguageContentService {
    enum class ProcessingType {
        LOAD, SAVE
    }

    var repository: MultipleLanguageContentBaseEntityRepository<MultipleLanguageContentBaseEntity>? = null
    var type: KClass<*>? = null

    private val _applicationEntityBaseType = ApplicationEntityBase::class.createType()

    @Autowired
    private lateinit var beanFactory: AutowireCapableBeanFactory

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Autowired
    private lateinit var languageManager: LanguageManager

    @Autowired
    private lateinit var solutionMLCRepository: SolutionMLCRepository

    fun load(target: ApplicationEntityBase) {
        processTarget(target, ProcessingType.LOAD)
    }

    fun save(savedObj: ApplicationEntityBase, target: ApplicationEntityBase) {
        processTarget(target, ProcessingType.SAVE, savedObj)
    }

    fun delete(target: ApplicationEntityBase) {
        deleteObjectContent(target.id)
    }

    private fun processTarget(target: ApplicationEntityBase, processingType: ProcessingType, savedObj: ApplicationEntityBase? = null) {
//        for (annotation in target::class.java.annotations) {
//            when(annotation) {
//                is MultipleLanguageContentEntity -> {
                    processFields(target, processingType, savedObj)
//                }
//            }
//        }
    }

    private fun processFields(target: ApplicationEntityBase, processingType: ProcessingType, savedObj: ApplicationEntityBase? = null) {
        val memberProperties = target::class.memberProperties.filterIsInstance<KMutableProperty<*>>()
        val obj = savedObj ?: target
        for (property in memberProperties) {
            property.getter.findAnnotation<MultipleLanguageContent>() ?: continue

            if(loadIfObjectsList(target, property))
                continue
            if(loadIfEntityObject(obj, property))
                continue

            when (processingType) {
                ProcessingType.LOAD -> loadFieldContent(target, property)
                ProcessingType.SAVE -> saveFieldContent(savedObj!!, target, property)
            }
        }
    }

    fun loadIfObjectsList(target: ApplicationEntityBase, property: KMutableProperty<*>): Boolean {
        if(property.returnType.arguments.isEmpty())
            return false
        val propertyType = property.returnType.classifier?.createType(property.returnType.arguments)
        val listType = List::class.createType(property.returnType.arguments)
        val isList = propertyType?.isSubtypeOf(listType)
        if(isList == null || !isList)
            return false
        val isEntityObject = property.returnType.arguments[0].type?.isSubtypeOf(_applicationEntityBaseType)
        if(isEntityObject == null || !isEntityObject)
            return false
//        property.returnType.arguments[0].type?.findAnnotation<MultipleLanguageContentEntity>() ?: return true
        val objList: List<*>? = property.getter.call(target) as List<*>? ?: return true
        if(objList!!.isEmpty())
            return true
        val propertyFullClassName = property.returnType.arguments[0].type?.toString()
        val serviceName = ApplicationServiceRegistry.registry[propertyFullClassName]
        //todo raise exception if null
        var serviceBean: ApplicationBaseService<*>? = null
        if(serviceName != null)
            serviceBean =  beanFactory.getBean(serviceName) as ApplicationBaseService<*>

        objList.forEach {
            val obj = it as ApplicationEntityBase
            if (serviceName != null)
                serviceBean?.loadMLC(obj)
            else
                load(obj)
        }
        return true
    }

    fun loadIfEntityObject(target: ApplicationEntityBase, property: KMutableProperty<*>): Boolean {
        val propertyType = property.returnType.classifier?.createType()
        val isEntityObject = propertyType?.isSubtypeOf(_applicationEntityBaseType)
        if(isEntityObject != null && isEntityObject) {
            var obj: Any? = property.getter.call(target) ?: return true
            obj = obj as ApplicationEntityBase
            val propertyFullClassName = propertyType.toString()
            val serviceName = ApplicationServiceRegistry.registry[propertyFullClassName]
            //todo raise exception if null
            var serviceBean: ApplicationBaseService<*>? = null
            if(serviceName != null)
                serviceBean =  beanFactory.getBean(serviceName) as ApplicationBaseService<*>
//            val serviceBean = beanFactory.getBean(serviceName!!) as ApplicationBaseService<*>
            if (serviceName != null)
                serviceBean?.loadMLC(obj)
            else
                load(obj)
//            serviceBean.loadMLC(obj)
            property.setter.call(target, obj)
            return true
        }
        return false
    }

    private fun loadFieldContent(target: ApplicationEntityBase, property: KMutableProperty<*>) {
        var entity = repository!!.findOneByLanguageIdAndFieldNameAndParentId(languageManager.currentLanguage.id, property.name, target.id)
        if(!entity.isPresent)
            entity = repository!!.findOneByLanguageIdAndFieldNameAndParentId(languageManager.defaultLanguage.id, property.name, target.id)
        val value = if(entity.isPresent) entity.get().contents else ""
        property.setter.call(target, value)
    }

    private fun saveFieldContent(savedObj: ApplicationEntityBase, target: ApplicationEntityBase, property: KMutableProperty<*>) {
        val entity = repository!!.findOneByLanguageIdAndFieldNameAndParentId(languageManager.currentLanguage.id, property.name, target.id)
        val mlcObj = (if(entity.isPresent) entity.get() else type!!.createInstance()) as MultipleLanguageContentBaseEntity
        mlcObj.parentId = savedObj.id
        mlcObj.languageId = languageManager.currentLanguage.id
        mlcObj.fieldName = property.name
        val propValue = property.getter.call(target)
        val obj: Any? = propValue ?: ""
        mlcObj.contents = obj.toString()
        repository!!.save(mlcObj)
        property.setter.call(savedObj, obj)
    }

    private fun deleteObjectContent(parentId: Long) {
        val entities = repository!!.findAllByParentId(parentId)
        repository!!.deleteAll(entities)
    }
}

