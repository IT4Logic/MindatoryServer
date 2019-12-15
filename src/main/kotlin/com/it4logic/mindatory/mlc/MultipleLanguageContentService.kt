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
import com.it4logic.mindatory.model.common.ApplicationEntityBase
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

	private val _applicationEntityBaseType = ApplicationEntityBase::class.createType()

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
	fun load(target: ApplicationEntityBase) {
		processTarget(target, ProcessingType.LOAD)
	}

	/**
	 * Saves language content for the given object
	 * @param target Input object instance
	 */
	fun save(target: ApplicationEntityBase, ref: ApplicationEntityBase?) {
		processTarget(target, ProcessingType.SAVE, ref)
	}

	/**
	 * Deletes language content for the given object
	 * @param target Input object instance
	 */
	fun delete(target: ApplicationEntityBase) {
		target.obtainMLCs().clear()
	}

	/**
	 * Process object properties and save or delete language content accordingly
	 * @param target Input object instance
	 * @param processingType Processing type (either save or delete)
	 */
	private fun processTarget(
		target: ApplicationEntityBase,
		processingType: ProcessingType,
		ref: ApplicationEntityBase? = null
	) {
		val memberProperties = target::class.memberProperties.filterIsInstance<KMutableProperty<*>>()
		for (property in memberProperties) {
			property.getter.findAnnotation<MultipleLanguageContent>() ?: continue

			if (loadIfObjectsList(target, property))
				continue
			if (loadIfEntityObject(target, property))
				continue

			when (processingType) {
				ProcessingType.LOAD -> loadFieldContent(target, property)
				ProcessingType.SAVE -> saveFieldContent(target, ref, property)
			}
		}
	}

	/**
	 * Loads the language content if the property of type list
	 * @param target Input object instance
	 * @param property Input property
	 * @return True if the property is list and has been loaded
	 */
	fun loadIfObjectsList(target: ApplicationEntityBase, property: KMutableProperty<*>): Boolean {
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
			val obj = it as ApplicationEntityBase
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
	fun loadIfEntityObject(target: ApplicationEntityBase, property: KMutableProperty<*>): Boolean {
		val propertyType = property.returnType.classifier?.createType(property.returnType.arguments)
		val isEntityObject = propertyType?.isSubtypeOf(_applicationEntityBaseType)
		if (isEntityObject != null && isEntityObject) {
			var obj: Any? = property.getter.call(target) ?: return true
			obj = obj as ApplicationEntityBase
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
	private fun loadFieldContent(target: ApplicationEntityBase, property: KMutableProperty<*>) {
		val mlcs = target.obtainMLCs()
		var mlc = mlcs.find {
							it.languageId == languageManager.currentLanguage.id && it.fieldName == property.name
						}
		if(mlc == null) {
			mlc = mlcs.find {
				it.languageId == languageManager.defaultLanguage.id && it.fieldName == property.name
			}
		}
		if(mlc != null)
			property.setter.call(target, mlc.contents)
	}

	/**
	 * Saves the language content for the input property
	 * @param target Reference object instance
	 * @param property Input property
	 */
	private fun saveFieldContent(
		target: ApplicationEntityBase,
		ref: ApplicationEntityBase?,
		property: KMutableProperty<*>
	) {
		val mlcs = ref?.obtainMLCs()
		val mlc = mlcs?.find {
			it.languageId == languageManager.currentLanguage.id && it.fieldName == property.name
		} ?: type!!.createInstance() as MultipleLanguageContentBaseEntity

		mlc.updatedParent(target)
		mlc.languageId = languageManager.currentLanguage.id
		mlc.fieldName = property.name
		val propValue = property.getter.call(target) ?: ""
		mlc.contents = propValue.toString()

		target.addMLC(mlc)
	}
}

