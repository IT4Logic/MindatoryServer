/*
    Copyright (c) 2019, IT4Logic.

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

package com.it4logic.mindatory.config

import com.it4logic.mindatory.controllers.ApplicationControllerEntryPoints
import com.it4logic.mindatory.security.ApplicationSecurityPermissions
import com.it4logic.mindatory.security.JwtAuthenticationEntryPoint
import com.it4logic.mindatory.security.JwtAuthenticationFilter
import com.it4logic.mindatory.security.SecurityUserDetailsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.encrypt.Encryptors
import org.springframework.security.crypto.encrypt.TextEncryptor
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import java.util.*
import javax.sql.DataSource

/**
 * Utility class to configure Spring Web Security
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, jsr250Enabled = true, prePostEnabled = true)
class WebSecurityConfig : WebSecurityConfigurerAdapter () {

    @Autowired
    private lateinit var appProperties: AppProperties

    @Autowired
    private lateinit var userDetailsService: SecurityUserDetailsService

    @Autowired
    private lateinit var unauthorizedHandler: JwtAuthenticationEntryPoint

    @Autowired
    private lateinit var authenticationFilter: JwtAuthenticationFilter

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var dataSource: DataSource

    /**
     * TextEncryptor producer that will be used in security user token creation
     */
    @Bean
    fun textEncryptorBean(): TextEncryptor {
        return Encryptors.text(appProperties.key, appProperties.keySalt)
    }

    /**
     * AuthenticationManager producer that will be used in security initialization
     */
    @Bean
    override fun authenticationManagerBean(): AuthenticationManager {
        return super.authenticationManagerBean()
    }

    /**
     * CorsConfigurationSource producer that will be used in security initialization for CORS
     */
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val source = UrlBasedCorsConfigurationSource()
        val config = CorsConfiguration().applyPermitDefaultValues()
        config.allowedMethods = Arrays.asList(
                HttpMethod.GET.name,
                HttpMethod.PUT.name,
                HttpMethod.POST.name,
                HttpMethod.DELETE.name)
        source.registerCorsConfiguration("/**", config)
        return source
    }

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth
            .userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder)
    }

    override fun configure(http: HttpSecurity?) {
        http
            ?.cors()?.and()
            ?.csrf()?.disable()
            ?.exceptionHandling()
            ?.authenticationEntryPoint(unauthorizedHandler)?.and()
            ?.sessionManagement()?.sessionCreationPolicy(SessionCreationPolicy.STATELESS)?.and()
            ?.authorizeRequests()
            ?.antMatchers(
                ApplicationControllerEntryPoints.Authentication + "**",
                ApplicationControllerEntryPoints.Security + "**"
            )?.permitAll()
            ?.antMatchers(ApplicationControllerEntryPoints.Actuator + "**")?.hasAnyAuthority(
                ApplicationSecurityPermissions.SystemWideAdmin, ApplicationSecurityPermissions.SystemAdmin
            )
            ?.anyRequest()?.fullyAuthenticated()?.and()
            ?.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            ?.headers()
            ?.cacheControl()
    }
}
