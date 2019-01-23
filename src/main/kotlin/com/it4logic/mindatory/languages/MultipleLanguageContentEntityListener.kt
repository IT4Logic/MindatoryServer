package com.it4logic.mindatory.languages

import com.it4logic.mindatory.model.common.ApplicationEntityBase
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Configurable
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import java.lang.reflect.Field
import javax.persistence.PostLoad
import javax.persistence.PrePersist
import javax.persistence.PreUpdate
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance


@Configurable
class MultipleLanguageContentEntityListener {

    enum class ProcessingType {
        SAVE, LOAD
    }

    private lateinit var mclRepositoryClass: KClass<*>
    private lateinit var mclEntityClass: KClass<*>

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Autowired
    private lateinit var languageManager: LanguageManager

    @PrePersist
    fun prePersist(target: ApplicationEntityBase) {
        processTarget(target, ProcessingType.SAVE)
    }

    @PreUpdate
    fun preUpdate(target: ApplicationEntityBase) {
        processTarget(target, ProcessingType.SAVE)
    }

    @PostLoad
    fun postLoad(target: ApplicationEntityBase) {
        processTarget(target, ProcessingType.LOAD)
    }

    private fun processTarget(target: ApplicationEntityBase, processingType: ProcessingType) {
        for (annotation in target::class.java.annotations) {
            when(annotation) {
                is MultipleLanguageContentEntity -> {
                    mclRepositoryClass = annotation.mclRepositoryClass
                    mclEntityClass = annotation.mclEntityClass
                    processFields(target, processingType)
                }
            }
        }
    }

    private fun processFields(target: ApplicationEntityBase, processingType: ProcessingType) {
        val declaredFields = target::class.java.declaredFields
        for (field in declaredFields) {
            for (annotation in field.annotations) {
                when(annotation) {
                    is MultipleLanguageContent -> {
                        when (processingType) {
                            ProcessingType.LOAD -> loadFieldContent(target, field)
                            ProcessingType.SAVE -> saveFieldContent(target, field)
                        }
                    }
                }
            }
        }
    }

    private fun loadFieldContent(target: ApplicationEntityBase, field: Field) {
        val repo = applicationContext.getBean<Any>(mclRepositoryClass) as MultipleLanguageContentBaseEntityRepository<*>
        val entity = repo.findOneByLanguageIdAndFieldNameAndParentId(languageManager.currentLanguage.id, field.name, target.id)
        val value = if(entity.isPresent) entity.get() else null
        field.set(target, value)
    }

    private fun saveFieldContent(target: ApplicationEntityBase, field: Field) {
        val repo = applicationContext.getBean<Any>(mclRepositoryClass) as MultipleLanguageContentBaseEntityRepository<Any>
        val entity = repo.findOneByLanguageIdAndFieldNameAndParentId(languageManager.currentLanguage.id, field.name, target.id)
        val value = (if(entity.isPresent) entity.get() else mclEntityClass.createInstance()) as MultipleLanguageContentBaseEntity
        value.language = languageManager.currentLanguage
        value.fieldName = field.name
        value.contents = field.get(target).toString()
        repo.save(value)
    }
}

