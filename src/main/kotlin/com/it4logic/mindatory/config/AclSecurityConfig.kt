/*
    Copyright (c) 2019, IT4Logic.

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

package com.it4logic.mindatory.config

import com.it4logic.mindatory.security.CustomDefaultPermissionGrantingStrategy
import com.it4logic.mindatory.security.SecurityUserDetailsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Bean
import javax.sql.DataSource
import org.springframework.security.acls.AclPermissionCacheOptimizer
import org.springframework.security.acls.AclPermissionEvaluator
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler
import org.springframework.security.acls.jdbc.JdbcMutableAclService
import org.springframework.security.acls.jdbc.BasicLookupStrategy
import org.springframework.security.acls.jdbc.LookupStrategy
import org.springframework.security.acls.model.PermissionGrantingStrategy
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean
import org.springframework.cache.ehcache.EhCacheFactoryBean
import org.springframework.context.ApplicationContext
import org.springframework.security.access.PermissionEvaluator
import org.springframework.security.acls.domain.*
import org.springframework.security.acls.model.ObjectIdentityRetrievalStrategy
import org.springframework.security.acls.model.ObjectIdentityGenerator
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.authentication.dao.DaoAuthenticationProvider

/**
 * Utility class to configure ACL Spring Security
 */
@Configuration
class MethodSecurityConfig {

    @Autowired
    private lateinit var userDetailsService: SecurityUserDetailsService

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Autowired
    private lateinit var dataSource: DataSource

    private var objectIdentityRetrievalStrategy = ObjectIdentityRetrievalStrategyImpl()

    @Bean
    fun authenticationProvider(): DaoAuthenticationProvider {
        val authenticationProvider = DaoAuthenticationProvider()
        authenticationProvider.setUserDetailsService(userDetailsService)
        authenticationProvider.setPasswordEncoder(passwordEncoder)
        return authenticationProvider
    }

    @Bean
    fun aclCache(): EhCacheBasedAclCache {
        return EhCacheBasedAclCache(
            aclEhCacheFactoryBean().getObject()!!,
            permissionGrantingStrategy(),
            aclAuthorizationStrategy()
        )
    }

    @Bean
    fun aclEhCacheFactoryBean(): EhCacheFactoryBean {
        val ehCacheFactoryBean = EhCacheFactoryBean()
        ehCacheFactoryBean.setCacheManager(aclCacheManager().getObject()!!)
        ehCacheFactoryBean.setCacheName("aclCache")
        return ehCacheFactoryBean
    }

    @Bean
    fun aclCacheManager(): EhCacheManagerFactoryBean {
        return EhCacheManagerFactoryBean()
    }


    @Bean
    fun lookupStrategy(): LookupStrategy {
        return BasicLookupStrategy(dataSource, aclCache(), aclAuthorizationStrategy(), permissionGrantingStrategy())
    }

    @Bean
    fun aclAuthorizationStrategy(): AclAuthorizationStrategy {
        return AclAuthorizationStrategyImpl(SimpleGrantedAuthority("ROLE_ADMIN"))
    }


    @Bean
    fun permissionGrantingStrategy(): PermissionGrantingStrategy {
        return CustomDefaultPermissionGrantingStrategy(ConsoleAuditLogger())
    }

    @Bean
    fun permissionEvaluator(): PermissionEvaluator {
        return AclPermissionEvaluator(aclService())
    }

    @Bean
    fun defaultMethodSecurityExpressionHandler(): MethodSecurityExpressionHandler {
        val expressionHandler = DefaultMethodSecurityExpressionHandler()
        expressionHandler.setPermissionEvaluator(permissionEvaluator())
        expressionHandler.setPermissionCacheOptimizer(AclPermissionCacheOptimizer(aclService()))
        expressionHandler.setApplicationContext(applicationContext)
        return expressionHandler
    }

    @Bean
    fun aclService(): JdbcMutableAclService {
        return JdbcMutableAclService(dataSource, lookupStrategy(), aclCache())
    }

    @Bean
    fun objectIdentityRetrievalStrategy(): ObjectIdentityRetrievalStrategy {
        return objectIdentityRetrievalStrategy
    }

    @Bean
    fun objectIdentityGenerator(): ObjectIdentityGenerator {
        return objectIdentityRetrievalStrategy
    }


}
