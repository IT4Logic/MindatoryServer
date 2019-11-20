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
package com.it4logic.mindatory.helpers

import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

/**
 * Helper class which is able to autowire a specified class.
 * It holds a static reference to the [ .springframework.context.ApplicationContext][org].
 */
@Component
class AutowireHelper private constructor() : ApplicationContextAware {

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        AutowireHelper.applicationContext = applicationContext
    }

    companion object {
        /**
         * @return the singleton instance.
         */
        val instance = AutowireHelper()
        var applicationContext: ApplicationContext? = null

        /**
         * Tries to autowire the specified instance of the class if one of the specified beans which need to be autowired
         * are null.
         *
         * @param classToAutowire the instance of the class which holds @Autowire annotations
         * @param beansToAutowireInClass the beans which have the @Autowire annotation in the specified {#classToAutowire}
         */
        fun autowire(classToAutowire: Any, vararg beansToAutowireInClass: Any) {
            for (bean in beansToAutowireInClass) {
                    applicationContext!!.autowireCapableBeanFactory.autowireBean(classToAutowire)
            }
        }

        /**
         * Tries to autowire the specified instance of the class if one of the specified beans which need to be autowired
         * are null.
         *
         * @param beansToAutowireInClass the beans which have the @Autowire annotation in the specified {#classToAutowire}
         */
        fun autowire(beansToAutowireInClass: Class<*>) : Any {
            return applicationContext!!.autowireCapableBeanFactory.autowire(beansToAutowireInClass, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true)
        }
    }

}