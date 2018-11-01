/*
    Copyright (c) 2017, IT4Logic.

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
import springfox.documentation.swagger2.annotations.EnableSwagger2
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.service.ApiInfo
import springfox.documentation.service.Contact
import java.time.OffsetDateTime
import java.time.LocalDate

/**
 * Class that includes swagger library configuration
 */
@Configuration
@EnableSwagger2
class SwaggerConfig {

    /**
     * [ApiInfo] builder, that will be used to provide information about the sever in the swagger ui page
     *
     * @return ApiInfo object
     */
    fun apiInfo(): ApiInfo {
        return ApiInfoBuilder()
                .title("Mindatory API Server")
                .description("This is the Mindatory API server.  You can find out more about Mindatory at [http://it4logic.com](http://it4logic.com).")
                .license("GNU GPL v3.0")
                .licenseUrl("https://www.gnu.org/licenses/gpl-3.0.html")
                .version("0.1.0")
                .contact(Contact("IT4Logic", "http://it4logic.com", "h.zakaria@it4loigc.com"))
                .build()
    }

    /**
     * Bean provider for [Docket] to be used within swagger library. This method will provide the configuration for swagger
     *
     * @return Docket object
     */
    @Bean
    fun api(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any()) // should only include the data packages
                .build()
//                .directModelSubstitute(org.threeten.bp.LocalDate::class.java, java.sql.Date::class.java) // to be checked
//                .directModelSubstitute(org.threeten.bp.OffsetDateTime::class.java, java.util.Date::class.java) // to be checked
//                .directModelSubstitute(org.threeten.bp.OffsetDateTime::class.java, java.util.Date::class.java) // to be checked
                .apiInfo(apiInfo())
    }
}