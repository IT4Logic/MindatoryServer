package com.it4logic.mindatory.model.common

import com.it4logic.mindatory.mlc.MultipleLanguageContent
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.query.QueryUtils.toOrders
import java.io.Serializable
import org.springframework.data.repository.NoRepositoryBean
import javax.persistence.EntityManager
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.lang.Nullable
import org.springframework.util.Assert
import java.util.ArrayList
import javax.persistence.Query
import javax.persistence.TypedQuery
import javax.persistence.criteria.*
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties


@NoRepositoryBean
interface ExtendedJpaRepository<T, ID : Serializable> : JpaRepository<T, ID>


class ExtendedJpaRepositoryImpl<T, ID : Serializable> (
	val entityInformation: JpaEntityInformation<T, *>,
	val entityManager: EntityManager
) : SimpleJpaRepository<T, ID>(entityInformation, entityManager), ExtendedJpaRepository<T, ID> {

	fun <S> toMLCSort(sort: Sort, from: From<*, *>, cb: CriteriaBuilder, query: CriteriaQuery<S>): Sort {
		if (sort.isUnsorted)
			return sort

		Assert.notNull(from, "From must not be null!")
		Assert.notNull(cb, "CriteriaBuilder must not be null!")

		val orders = ArrayList<Sort.Order>()

		for (order in sort) {
			val memberProperty = from.javaType.kotlin.memberProperties.filter { it.name ==  order.property}
			if(!memberProperty.isEmpty()) {
				val result = memberProperty[0].getter.findAnnotation<MultipleLanguageContent>()
				if(result != null) {
					// This feature is not supported in PostgresSQL
					val sortOrder = Sort.Order(order.direction, "mlcs.contents", order.nullHandling)
					orders.add(sortOrder)
					query.distinct(true)
					continue
				}
			}

			orders.add(order)
		}

		return Sort.by(orders)
	}

	override fun <S : T> getQuery(spec: Specification<S>?, domainClass: Class<S>, sort: Sort): TypedQuery<S> {
		val builder = entityManager.criteriaBuilder
		val query = builder.createQuery(domainClass)

		val root = applySpecificationToCriteria(spec, domainClass, query)
		query.select(root)

		if (sort.isSorted) {
			val newSort = toMLCSort(sort, root, builder, query)
			query.orderBy(toOrders(newSort, root, builder))
		}

		return applyRepositoryMethodMetadata(entityManager.createQuery(query))
	}

	/**
	 * Applies the given [Specification] to the given [CriteriaQuery].
	 *
	 * @param spec can be null.
	 * @param domainClass must not be null.
	 * @param query must not be null.
	 * @return
	 */
	private fun <S, U : T> applySpecificationToCriteria( @Nullable spec: Specification<U>?, domainClass: Class<U>, query: CriteriaQuery<S>): Root<U> {
		Assert.notNull(domainClass, "Domain class must not be null!")
		Assert.notNull(query, "CriteriaQuery must not be null!")

		val root = query.from(domainClass)

		if (spec == null) {
			return root
		}

		val builder = entityManager.criteriaBuilder
		val predicate = spec.toPredicate(root, query, builder)

		if (predicate != null) {
			query.where(predicate)
		}

		return root
	}

	private fun <S> applyRepositoryMethodMetadata(query: TypedQuery<S>): TypedQuery<S> {

		if (repositoryMethodMetadata == null) {
			return query
		}

		val type = repositoryMethodMetadata!!.lockModeType
		val toReturn = if (type == null) query else query.setLockMode(type)

		applyQueryHints(toReturn)

		return toReturn
	}

	private fun applyQueryHints(query: Query) {
		for (hint in queryHints().withFetchGraphs(entityManager)) {
			query.setHint(hint.key, hint.value)
		}
	}

	private fun queryHints(): QueryHints {
		return if (repositoryMethodMetadata == null) QueryHints.NoHints.INSTANCE else DefaultQueryHints.of(entityInformation, repositoryMethodMetadata!!)
	}
}