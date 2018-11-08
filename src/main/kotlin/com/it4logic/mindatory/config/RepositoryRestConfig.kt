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

import com.it4logic.mindatory.model.Company
import org.springframework.data.rest.core.config.RepositoryRestConfiguration
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer
import org.springframework.stereotype.Component
import org.springframework.http.HttpMethod


@Component
class SpringDataRestCustomization : RepositoryRestConfigurer {

  override fun configureRepositoryRestConfiguration(config: RepositoryRestConfiguration) {
    config.exposureConfiguration.forDomainType(Company::class.java)
        .withItemExposure { _, httpMethods ->
          httpMethods.disable(HttpMethod.DELETE, HttpMethod.POST)
        }
  }
}

