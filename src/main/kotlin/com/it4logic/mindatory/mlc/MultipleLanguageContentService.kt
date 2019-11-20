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

package com.it4logic.mindatory.mlc

import com.it4logic.mindatory.model.project.ProjectMLCRepository
import com.it4logic.mindatory.model.common.ApplicationMLCEntityBase
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntity
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntityRepository
import com.it4logic.mindatory.services.common.ApplicationBaseService
import com.it4logic.mindatory.services.common.ApplicationServiceRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import javax.transaction.Transactional
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.*

/**
 * Service responsible for managing Multiple Language Content functionality
 */
@Service
@Scope("prototype")
@Transactional
class MultipleLanguageContentService {
	enum class ProcessingType {
		LOAD, SAVE
	}

	var repository: MultipleLanguageContentBaseEntityRepository<MultipleLanguageContentBaseEntity>? = null
	var type: KClass<*>? = null

	private val _applicationEntityBaseType = ApplicationMLCEntityBase::class.createType()

	@Autowired
	private lateinit var beanFactory: AutowireCapableBeanFactory

	@Autowired
	private lateinit var applicationContext: ApplicationContext

	@Autowired
	private lateinit var languageManager: LanguageManager

	@Autowired
	private lateinit var projectMLCRepository: ProjectMLCRepository

	/**
	 * Loads language content for the given object
	 * @param target Input object instance
	 */
	fun load(target: ApplicationMLCEntityBase) {
		processTarget(target, ProcessingType.LOAD)
	}

	/**
	 * Saves language content for the given object
	 * @param savedObj Saved instance of the object
	 * @param target Input object instance
	 */
	fun save(savedObj: ApplicationMLCEntityBase, target: ApplicationMLCEntityBase) {
		processTarget(target, ProcessingType.SAVE, savedObj)
	}

	/**
	 * Deletes language content for the given object
	 * @param target Input object instance
	 */
	fun delete(target: ApplicationMLCEntityBase) {
		deleteObjectContent(target.id)
	}

	/**
	 * Process object properties and save or delete language content accordingly
	 * @param target Input object instance
	 * @param processingType Processing type (either save or delete)
	 * @param savedObj Saved object isntance
	 */
	private fun processTarget(
		target: ApplicationMLCEntityBase,
		processingType: ProcessingType,
		savedObj: ApplicationMLCEntityBase? = null
	) {
		val memberProperties = target::class.memberProperties.filterIsInstance<KMutableProperty<*>>()
		val obj = savedObj ?: target
		for (property in memberProperties) {
			property.getter.findAnnotation<MultipleLanguageContent>() ?: continue

			if (loadIfObjectsList(target, property))
				continue
			if (loadIfEntityObject(obj, property))
				continue

			when (processingType) {
				ProcessingType.LOAD -> loadFieldContent(target, property)
				ProcessingType.SAVE -> saveFieldContent(savedObj!!, target, property)
			}
		}
	}

	/**
	 * Loads the language content if the property of type list
	 * @param target Input object instance
	 * @param property Input property
	 * @return True if the property is list and has been loaded
	 */
	fun loadIfObjectsList(target: ApplicationMLCEntityBase, property: KMutableProperty<*>): Boolean {
		if (property.returnType.arguments.isEmpty())
			return false
		val propertyType = property.returnType.classifier?.createType(property.returnType.arguments)
		val listType = List::class.createType(property.returnType.arguments)
		val isList = propertyType?.isSubtypeOf(listType)
		if (isList == null || !isList)
			return false
		val isEntityObject = property.returnType.arguments[0].type?.isSubtypeOf(_applicationEntityBaseType)
		if (isEntityObject == null || !isEntityObject)
			return false
		val objList: List<*>? = property.getter.call(target) as List<*>? ?: return true
		if (objList!!.isEmpty())
			return true
		val propertyFullClassName = property.returnType.arguments[0].type?.toString()
		val serviceName = ApplicationServiceRegistry.registry[propertyFullClassName]
		var serviceBean: ApplicationBaseService<*>? = null
		if (serviceName != null)
			serviceBean = beanFactory.getBean(serviceName) as ApplicationBaseService<*>

		objList.forEach {
			val obj = it as ApplicationMLCEntityBase
			if (serviceName != null)
				serviceBean?.loadMLC(obj)
			else
				load(obj)
		}
		return true
	}

	/**
	 * Loads the language content if the property of type Object
	 * @param target Input object instance
	 * @param property Input property
	 * @return True if the property is Object and has been loaded
	 */
	fun loadIfEntityObject(target: ApplicationMLCEntityBase, property: KMutableProperty<*>): Boolean {
		val propertyType = property.returnType.classifier?.createType(property.returnType.arguments)
		val isEntityObject = propertyType?.isSubtypeOf(_applicationEntityBaseType)
		if (isEntityObject != null && isEntityObject) {
			var obj: Any? = property.getter.call(target) ?: return true
			obj = obj as ApplicationMLCEntityBase
			val propertyFullClassName = propertyType.toString()
			val serviceName = ApplicationServiceRegistry.registry[propertyFullClassName]
			var serviceBean: ApplicationBaseService<*>? = null
			if (serviceName != null)
				serviceBean = beanFactory.getBean(serviceName) as ApplicationBaseService<*>
			if (serviceName != null)
				serviceBean?.loadMLC(obj)
			else
				load(obj)
			property.setter.call(target, obj)
			return true
		}
		return false
	}

	/**
	 * Loads the language content for the input property
	 * @param target Input object instance
	 * @param property Input property
	 */
	private fun loadFieldContent(target: ApplicationMLCEntityBase, property: KMutableProperty<*>) {
		if (repository == null)
			return
		var entity = repository!!.findOneByLanguageIdAndFieldNameAndParent(
			languageManager.currentLanguage.id,
			property.name,
			target.id
		)
		if (!entity.isPresent)
			entity = repository!!.findOneByLanguageIdAndFieldNameAndParent(
				languageManager.defaultLanguage.id,
				property.name,
				target.id
			)
		val value = if (entity.isPresent) entity.get().contents else ""
		property.setter.call(target, value)
	}

	/**
	 * Saves the language content for the input property
	 * @param savedObj Saved object instance
	 * @param refObj Reference object instance
	 * @param property Input property
	 */
	private fun saveFieldContent(
		savedObj: ApplicationMLCEntityBase,
		refObj: ApplicationMLCEntityBase,
		property: KMutableProperty<*>
	) {
		if (repository == null)
			return
		val entity = repository!!.findOneByLanguageIdAndFieldNameAndParent(
			languageManager.currentLanguage.id,
			property.name,
			savedObj.id
		)
		var mlcObj =
			(if (entity.isPresent) entity.get() else type!!.createInstance()) as MultipleLanguageContentBaseEntity
		mlcObj.parent = savedObj.id
		mlcObj.languageId = languageManager.currentLanguage.id
		mlcObj.fieldName = property.name
		val propValue = property.getter.call(refObj)
		val obj: Any? = propValue ?: ""
		mlcObj.contents = obj.toString()
		mlcObj = repository!!.save(mlcObj)
		savedObj.addMLC(mlcObj)
	}

	/**
	 * Deletes the language content for the input object identifier
	 * @param parentId Object identifier
	 */
	private fun deleteObjectContent(parentId: Long) {
		if (repository == null)
			return
		val entities = repository!!.findAllByParent(parentId)
		repository!!.deleteAll(entities)
	}
}

