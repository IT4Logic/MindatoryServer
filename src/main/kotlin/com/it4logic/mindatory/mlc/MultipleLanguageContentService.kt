package com.it4logic.mindatory.mlc

import com.it4logic.mindatory.model.SolutionLanguageContentRepository
import com.it4logic.mindatory.model.common.ApplicationEntityBase
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntity
import com.it4logic.mindatory.model.mlc.MultipleLanguageContentBaseEntityRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import javax.transaction.Transactional
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

@Service
@Transactional
class MultipleLanguageContentService {
    enum class ProcessingType {
        LOAD, SAVE
    }

    var repository: MultipleLanguageContentBaseEntityRepository<MultipleLanguageContentBaseEntity>? = null
    var type: KClass<*>? = null

    @Autowired
    private lateinit var beanFactory: AutowireCapableBeanFactory

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Autowired
    private lateinit var languageManager: LanguageManager

    @Autowired
    private lateinit var solutionLanguageContentRepository: SolutionLanguageContentRepository

    fun load(target: ApplicationEntityBase) {
        if(repository == null || type == null)
            return
        processTarget(target, ProcessingType.LOAD)
    }

    fun save(parentId: Long, target: ApplicationEntityBase) {
        if(repository == null || type == null)
            return
        processTarget(target, ProcessingType.SAVE, parentId)
    }

    fun delete(target: ApplicationEntityBase) {
        if(repository == null || type == null)
            return
        deleteObjectContent(target.id)
    }

    private fun processTarget(target: ApplicationEntityBase, processingType: ProcessingType, parentId: Long = -1) {
        for (annotation in target::class.java.annotations) {
            when(annotation) {
                is MultipleLanguageContentEntity -> {
                    processFields(target, processingType, parentId)
                }
            }
        }
    }

    private fun processFields(target: ApplicationEntityBase, processingType: ProcessingType, parentId: Long = -1) {
        val memberProperties = target::class.memberProperties.filterIsInstance<KMutableProperty<*>>()
        for (property in memberProperties) {
            val result = property.getter.findAnnotation<MultipleLanguageContent>()
            if(result != null) {
                when (processingType) {
                    ProcessingType.LOAD -> loadFieldContent(target, property)
                    ProcessingType.SAVE -> saveFieldContent(parentId, target, property)
                }
            }
        }
    }

    private fun loadFieldContent(target: ApplicationEntityBase, property: KMutableProperty<*>) {
        val entity = repository!!.findOneByLanguageIdAndFieldNameAndParentId(languageManager.currentLanguage.id, property.name, target.id)
        val value = if(entity.isPresent) entity.get().contents else null
        property.setter.call(target, value)
    }

    private fun saveFieldContent(parentId: Long, target: ApplicationEntityBase, property: KMutableProperty<*>) {
        val entity = repository!!.findOneByLanguageIdAndFieldNameAndParentId(languageManager.currentLanguage.id, property.name, target.id)
        val value = (if(entity.isPresent) entity.get() else type!!.createInstance()) as MultipleLanguageContentBaseEntity
        value.parentId = parentId
        value.language = languageManager.currentLanguage
        value.fieldName = property.name
        value.contents = property.getter.call(target).toString()
        repository!!.save(value)
    }

    private fun deleteObjectContent(parentId: Long) {
        val entities = repository!!.findAllByParentId(parentId)
        repository!!.deleteAll(entities)
    }


}

