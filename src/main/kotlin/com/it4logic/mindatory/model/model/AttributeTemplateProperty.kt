/*
    Copyright (c) 2018, IT4Logic.

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

package com.it4logic.mindatory.model.model

import com.it4logic.mindatory.model.common.*
import org.hibernate.annotations.DynamicUpdate
import org.hibernate.envers.Audited
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import javax.persistence.*
import javax.validation.constraints.Size

/**
 * Attribute Template Property Entity
 */
@Audited
@Entity
@DynamicUpdate
@EntityListeners(AuditingEntityListener::class)
@Table(
	name = "t_attribute_template_properties", uniqueConstraints = [
		(UniqueConstraint(
			name = ApplicationConstraintCodes.AttributeTemplateIdentifierUniqueIndex,
			columnNames = ["f_identifier", "f_attribute_id"]
		))
	]
)
data class AttributeTemplateProperty(
	@get: Size(max = 50)
	@Column(name = "f_identifier", length = 50)
	var identifier: String,

	@Lob
	@Column(name = "f_value")
	var value: String,

	@ManyToOne(optional = false)
	@JoinColumn(name = "f_attribute_id", nullable = false)
	var attribute: AttributeTemplate

) : ApplicationEntityBase()

/**
 * JPA Repository
 */
@RepositoryRestResource(exported = false)
interface AttributeTemplatePropertyRepository : //ApplicationRepositoryBaseRepository<AttributeTemplate>,
	ApplicationBaseRepository<AttributeTemplateProperty> {
	fun deleteByAttributeId(id: Long)
}