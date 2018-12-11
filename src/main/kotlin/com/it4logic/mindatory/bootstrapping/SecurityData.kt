package com.it4logic.mindatory.bootstrapping

import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class SecurityData : InitializingBean {

    @Transactional()
    override fun afterPropertiesSet() {

    }
}