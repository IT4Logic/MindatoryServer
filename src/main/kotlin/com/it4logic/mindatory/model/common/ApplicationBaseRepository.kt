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

package com.it4logic.mindatory.model.common

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.history.RevisionRepository
import org.springframework.lang.Nullable

/**
 * Base interface for error functionality for entity repositories
 */
@NoRepositoryBean
interface ApplicationBaseRepository<T> : ExtendedJpaRepository<T, Long>, JpaRepository<T, Long>,
	RevisionRepository<T, Long, Long>, JpaSpecificationExecutor<T>

/**
 * Base interface for company error functionality for entity repositories
 */
//@NoRepositoryBean
//interface ApplicationCompanyRepository<T> : ApplicationBaseRepository<T>

/**
 * Base interface for project error functionality for entity repositories
 */
//interface ApplicationProjectBaseRepository<T> {
//	fun findAllByProjectId(id: Long, @Nullable spec: Specification<T>?, pageable: Pageable): Page<T>
//	fun findAllByProjectId(id: Long, @Nullable spec: Specification<T>?, sort: Sort): List<T>
//	fun findAllByProjectId(id: Long, sort: Sort): List<T>
//	fun findAllByProjectId(id: Long): List<T>
//	fun countByProjectId(id: Long, @Nullable spec: Specification<T>?, sort: Sort): Long
//	fun countByProjectId(id: Long): Long
//}

/**
 * Base interface for model error functionality for entity repositories
 */
//interface ApplicationRepositoryBaseRepository<T> {
//	fun findAllByModelVersionId(id: Long, @Nullable spec: Specification<T>?, pageable: Pageable): Page<T>
//	fun findAllByModelVersionId(id: Long, @Nullable spec: Specification<T>?, sort: Sort): List<T>
//	fun findAllByModelVersionId(id: Long, sort: Sort): List<T>
//	fun findAllByModelVersionId(id: Long): List<T>
//	fun countByRepositoryVersionId(id: Long, @Nullable spec: Specification<T>?, sort: Sort): Long
//	fun countByRepositoryVersionId(id: Long): Long
//}


@NoRepositoryBean
interface LanguageContentBaseRepository<T> : ApplicationBaseRepository<T>