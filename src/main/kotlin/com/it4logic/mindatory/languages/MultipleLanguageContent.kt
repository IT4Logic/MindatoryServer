package com.it4logic.mindatory.languages

import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class MultipleLanguageContentEntity(
    val mclEntityClass: KClass<*>,
    val mclRepositoryClass: KClass<*>
)


@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class MultipleLanguageContent