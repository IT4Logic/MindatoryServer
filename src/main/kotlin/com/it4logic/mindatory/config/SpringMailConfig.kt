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

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.thymeleaf.ITemplateEngine
import org.thymeleaf.spring5.SpringTemplateEngine
import org.thymeleaf.templateresolver.StringTemplateResolver
import org.thymeleaf.templateresolver.ITemplateResolver

/**
 * Utility class to configure Spring Mail
 */
@Configuration
class SpringMailConfig {

	@Bean
	fun appTemplateEngine(): ITemplateEngine {
		val templateEngine = SpringTemplateEngine()
		templateEngine.addTemplateResolver(stringTemplateResolver())
		templateEngine.enableSpringELCompiler = true
		return templateEngine
	}

	private fun stringTemplateResolver(): ITemplateResolver {
		val templateResolver = StringTemplateResolver()
		templateResolver.order = Integer.valueOf(1)
		templateResolver.setTemplateMode("HTML5")
		templateResolver.isCacheable = false
		return templateResolver
	}
}