package com.it4logic.mindatory.model.mlc

import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.model.common.ApplicationEntityBase
import org.hibernate.annotations.DynamicUpdate
import org.springframework.data.repository.NoRepositoryBean
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

/**
 * Multiple language content root entity
 */
@MappedSuperclass
@DynamicUpdate
open class MultipleLanguageContentBaseEntity (
    @get: NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "language_id", nullable = false)
    open var language: Language? = null,

    @get: NotBlank
    @Column(nullable = false, length = 255)
    open var fieldName: String = "",

    @get: NotNull
    @Lob
    open var contents: String = "",

    @get: NotNull
    @Column(nullable = false)
    open var parentId: Long = 0

) : ApplicationEntityBase()

/**
 * Repository
 */
@NoRepositoryBean
interface MultipleLanguageContentBaseEntityRepository<T : MultipleLanguageContentBaseEntity> : ApplicationBaseRepository<T> {
    fun findOneByLanguageIdAndFieldNameAndParentId(langId: Long, fieldName: String, parentId: Long): Optional<T>
    fun findAllByLanguageIdAndFieldNameAndContents(langId: Long, fieldName: String, contents: String): List<T>
    fun findAllByLanguageIdAndFieldNameAndContentsAndParentIdNot(langId: Long, fieldName: String, contents: String, parentId: Long): List<T>
    fun findAllByParentId(parentId: Long): List<T>
}


