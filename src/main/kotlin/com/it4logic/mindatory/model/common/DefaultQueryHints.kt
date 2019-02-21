package com.it4logic.mindatory.model.common

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.query.Jpa21Utils
import org.springframework.data.jpa.repository.query.JpaEntityGraph
import org.springframework.data.jpa.repository.support.CrudMethodMetadata
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.util.Optionals
import org.springframework.util.Assert
import java.util.*
import javax.persistence.EntityManager

/**
 * Default implementation of [QueryHints].
 *
 * @author Christoph Strobl
 * @author Oliver Gierke
 * @since 2.0
 */
internal class DefaultQueryHints
/**
 * Creates a new [DefaultQueryHints] instance for the given [JpaEntityInformation],
 * [CrudMethodMetadata], [EntityManager] and whether to include fetch graphs.
 *
 * @param information must not be null.
 * @param metadata must not be null.
 * @param entityManager must not be null.
 */
private constructor(
	private val information: JpaEntityInformation<*, *>, private val metadata: CrudMethodMetadata,
	private val entityManager: Optional<EntityManager>
) : QueryHints {

	private val fetchGraphs: Map<String, Any>
		get() = Optionals
			.mapIfAllPresent(
				entityManager, metadata.entityGraph
			) { em, graph -> Jpa21Utils.tryGetFetchGraphHints(em, getEntityGraph(graph), information.javaType) }
			.orElse(emptyMap())

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.jpa.repository.support.QueryHints#withFetchGraphs()
	 */
	override fun withFetchGraphs(em: EntityManager): QueryHints {
		return DefaultQueryHints(this.information, this.metadata, Optional.of(em))
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	override operator fun iterator(): Iterator<Map.Entry<String, Any>> {
		return asMap().entries.iterator()
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.jpa.repository.support.QueryHints#asMap()
	 */
	override fun asMap(): Map<String, Any> {

		val hints = HashMap<String, Any>()

		hints.putAll(metadata.queryHints)
		hints.putAll(fetchGraphs)

		return hints
	}

	private fun getEntityGraph(entityGraph: EntityGraph): JpaEntityGraph {

		val fallbackName = information.entityName + "." + metadata.method.name
		return JpaEntityGraph(entityGraph, fallbackName)
	}

	companion object {

		/**
		 * Creates a new [QueryHints] instance for the given [JpaEntityInformation], [CrudMethodMetadata]
		 * and [EntityManager].
		 *
		 * @param information must not be null.
		 * @param metadata must not be null.
		 * @return
		 */
		fun of(information: JpaEntityInformation<*, *>, metadata: CrudMethodMetadata): QueryHints {

			Assert.notNull(information, "JpaEntityInformation must not be null!")
			Assert.notNull(metadata, "CrudMethodMetadata must not be null!")

			return DefaultQueryHints(information, metadata, Optional.empty())
		}
	}
}
