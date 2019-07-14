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

import java.util.Arrays
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener
import org.springframework.validation.Validator

/**
 * Utility class to solve the current Spring Boot problem for automatic discovery for Entity Repository Listener
 * through Component annotation
 */
@Configuration
class ValidatorEventRegister : InitializingBean {

    @Autowired
    private lateinit var validatingRepositoryEventListener: ValidatingRepositoryEventListener

    @Autowired
    private lateinit var validators: Map<String, Validator>

    /**
     * Searches the system for any existing component that has registered for entity repository event listener
     */
    @Throws(Exception::class)
    override fun afterPropertiesSet() {
        val events = Arrays.asList( "beforeCreate", "afterCreate",
                                    "beforeSave", "afterSave",
                                    "beforeLinkSave", "afterLinkSave",
                                    "beforeDelete", "afterDelete")

        for ( (key, value) in validators) {
            events
                .stream()
                .filter { p -> key.startsWith(p) }
                .findFirst()
                .ifPresent { p -> validatingRepositoryEventListener.addValidator(p, value) }
        }
    }
}