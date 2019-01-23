package com.it4logic.mindatory.languages

import com.it4logic.mindatory.model.common.ApplicationBaseRepository
import com.it4logic.mindatory.model.common.ApplicationEntityBase
import org.hibernate.annotations.DynamicUpdate
import org.springframework.data.rest.core.annotation.RepositoryRestResource
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
    @ManyToOne(optional = false)
    @JoinColumn(name = "parent_id", nullable = false)
    open var parent: Any? = null
) : ApplicationEntityBase()

/**
 * Repository
 */
@RepositoryRestResource(exported = false)
interface MultipleLanguageContentBaseEntityRepository<T> : ApplicationBaseRepository<T> {
    fun findOneByLanguageIdAndFieldNameAndParentId(langId: Long, fieldName: String, parentId: Long): Optional<T>
}


