package com.it4logic.mindatory.graphql

import com.it4logic.mindatory.model.Company
import com.it4logic.mindatory.security.ApplicationSecurityPermissions
import com.it4logic.mindatory.services.CompanyService
import com.it4logic.mindatory.services.common.ApplicationBaseService
import io.leangen.graphql.annotations.GraphQLMutation
import io.leangen.graphql.annotations.GraphQLQuery
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service


@Service
@GraphQLApi
class CompanyGQLService : GQLBaseService<Company>() {
	@Autowired
	lateinit var companyService: CompanyService

	override fun service(): ApplicationBaseService<Company> = companyService

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.CompanyAdminView}', '${ApplicationSecurityPermissions.CompanyAdminCreate}', '${ApplicationSecurityPermissions.CompanyAdminModify}', '${ApplicationSecurityPermissions.CompanyAdminDelete}')")
	@GraphQLQuery(name = "company")
	fun find(locale: String?): Company? {
		propagateLanguage(locale)
		val result = companyService.findFirst()
		service().refresh(result)
		return result
	}

	@PreAuthorize("hasAnyAuthority('${ApplicationSecurityPermissions.SystemWideAdmin}', '${ApplicationSecurityPermissions.CompanyAdminModify}')")
	@GraphQLMutation(name = "updateCompany")
	override fun update(locale: String?, target: Company): Company {
		return super.update(locale, target)
	}
}