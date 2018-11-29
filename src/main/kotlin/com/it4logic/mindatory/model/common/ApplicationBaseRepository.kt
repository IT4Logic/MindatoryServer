/*
    Copyright (c) 2018, IT4Logic.

    This file is part of Mindatory solution by IT4Logic.

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
 * Base interface for common functionality for entity repositories
 */
@NoRepositoryBean
interface ApplicationBaseRepository<T> : JpaRepository<T, Long>, RevisionRepository<T, Long, Long>, JpaSpecificationExecutor<T>

/**
 * Base interface for company common functionality for entity repositories
 */
@NoRepositoryBean
interface ApplicationCompanyBaseRepository<T> : ApplicationBaseRepository<T>

/**
 * Base interface for solution common functionality for entity repositories
 */
@NoRepositoryBean
interface ApplicationSolutionBaseRepository<T> : ApplicationCompanyBaseRepository<T> {
    fun findAllBySolutionId(id: Long, @Nullable spec: Specification<T>?, pageable: Pageable): Page<T>
    fun findAllBySolutionId(id: Long, @Nullable spec: Specification<T>?, sort: Sort): List<T>
    fun findAllBySolutionId(id: Long, sort: Sort): List<T>
    fun findAllBySolutionId(id: Long): List<T>
    fun countBySolutionId(id: Long, @Nullable spec: Specification<T>?, sort: Sort): Long
    fun countBySolutionId(id: Long): Long
}

/**
 * Base interface for repository common functionality for entity repositories
 */
@NoRepositoryBean
interface ApplicationRepositoryBaseRepository<T> : ApplicationSolutionBaseRepository<T> {
    fun findAllByRepositoryId(id: Long, @Nullable spec: Specification<T>?, pageable: Pageable): Page<T>
    fun findAllByRepositoryId(id: Long, @Nullable spec: Specification<T>?, sort: Sort): List<T>
    fun findAllByRepositoryId(id: Long, sort: Sort): List<T>
    fun findAllByRepositoryId(id: Long): List<T>
    fun countByRepositoryId(id: Long, @Nullable spec: Specification<T>?, sort: Sort): Long
    fun countByRepositoryId(id: Long): Long
}
