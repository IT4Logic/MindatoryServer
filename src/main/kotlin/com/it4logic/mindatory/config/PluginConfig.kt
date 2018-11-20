package com.it4logic.mindatory.config

import org.pf4j.spring.SpringPluginManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class PluginConfig {

  @Bean
  fun pluginManager(): SpringPluginManager {
    return SpringPluginManager()
  }
}