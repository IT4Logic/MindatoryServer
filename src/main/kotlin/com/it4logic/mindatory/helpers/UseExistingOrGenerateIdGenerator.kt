/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.it4logic.mindatory.helpers

import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.id.IdentityGenerator

/**
 * Identity Generator for entity classes. Either uses the existing generator or creates a new one
 */
class UseExistingOrGenerateIdGenerator : IdentityGenerator() {
    override fun generate(session: SharedSessionContractImplementor?, obj: Any?): java.io.Serializable {
        val id = session?.getEntityPersister(null,obj)?.classMetadata?.getIdentifier(obj, session)
        if(id != null && id.toString().toLong() > 0)
            return id
        return super.generate(session, obj)
    }
}