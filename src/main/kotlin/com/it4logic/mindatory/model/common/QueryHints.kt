package com.it4logic.mindatory.model.common

import org.springframework.data.jpa.repository.support.CrudMethodMetadata
import javax.persistence.EntityManager
import java.util.Collections
import kotlin.collections.Map.Entry

/**
 * QueryHints provides access to query hints defined via [CrudMethodMetadata.getQueryHints] by default excluding
 * JPA [javax.persistence.EntityGraph].
 *
 * @author Christoph Strobl
 * @author Oliver Gierke
 * @since 2.0
 */
internal interface QueryHints : Iterable<Entry<String, Any>> {

	/**
	 * Creates and returns a new [QueryHints] instance including [javax.persistence.EntityGraph].
	 *
	 * @param em must not be null.
	 * @return new instance of [QueryHints].
	 */
	fun withFetchGraphs(em: EntityManager): QueryHints

	/**
	 * Get the query hints as a [Map].
	 *
	 * @return never null.
	 */
	fun asMap(): Map<String, Any>

	/**
	 * Null object implementation of [QueryHints].
	 *
	 * @author Oliver Gierke
	 * @since 2.0
	 */
	enum class NoHints : QueryHints {

		INSTANCE;

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.jpa.repository.support.QueryHints#asMap()
		 */
		override fun asMap(): Map<String, Any> {
			return emptyMap()
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Iterable#iterator()
		 */
		override fun iterator(): Iterator<Entry<String, Any>> {
			return Collections.emptyIterator<Entry<String, Any>>()
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.jpa.repository.support.QueryHints#withFetchGraphs(javax.persistence.EntityManager)
		 */
		override fun withFetchGraphs(em: EntityManager): QueryHints {
			return this
		}
	}
}
