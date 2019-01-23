package com.it4logic.mindatory.helpers

import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.id.IdentityGenerator

class UseExistingOrGenerateIdGenerator : IdentityGenerator() {
    override fun generate(session: SharedSessionContractImplementor?, obj: Any?): java.io.Serializable {
        val id = session?.getEntityPersister(null,obj)?.classMetadata?.getIdentifier(obj, session)
        if(id != null && id.toString().toLong() > 0)
            return id
        return super.generate(session, obj)
    }
}