package com.it4logic.mindatory.model.data

import com.it4logic.mindatory.model.common.ApplicationSolutionBaseRepository
import com.it4logic.mindatory.model.common.ApplicationSolutionEntityBase
import org.hibernate.envers.Audited
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "t_joins", uniqueConstraints = [
//    (UniqueConstraint(name = ApplicationConstraintCodes.ProductCodeUniqueIndex, columnNames = ["code"]))
])
data class Join (
        @get: NotBlank
        @get: Size(min = 4, max = 100)
        @Column(nullable = false, length = 100)
        var code: String = ""
) : ApplicationSolutionEntityBase()

/**
 * Repository
 */
interface JoinRepository : ApplicationSolutionBaseRepository<Join>