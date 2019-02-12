package com.it4logic.mindatory.helpers

import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

/**
 * Helper class which is able to autowire a specified class. It holds a static reference to the [ .springframework.context.ApplicationContext][org].
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
//                if (bean == null) {
                    applicationContext!!.autowireCapableBeanFactory.autowireBean(classToAutowire)
//                }
            }
        }

        fun autowire(beansToAutowireInClass: Class<*>) : Any {
            return applicationContext!!.autowireCapableBeanFactory.autowire(beansToAutowireInClass, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true)
        }
    }

}