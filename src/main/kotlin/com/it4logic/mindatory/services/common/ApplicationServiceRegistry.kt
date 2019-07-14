package com.it4logic.mindatory.services.common

class ApplicationServiceRegistry {
    companion object {
        val registry: HashMap<String, String> = hashMapOf(
            "com.it4logic.mindatory.model.Solution" to "mailTemplateService",
            "com.it4logic.mindatory.model.ApplicationRepository" to "applicationRepositoryService",
            "com.it4logic.mindatory.model.Company" to "companyService",
            "com.it4logic.mindatory.model.mlc.Language" to "languageService",
            "com.it4logic.mindatory.model.repository.ArtifactTemplate"  to "artifactTemplateService",
            "com.it4logic.mindatory.model.repository.AttributeTemplate"  to "attributeTemplateService",
            "com.it4logic.mindatory.model.repository.JoinTemplate"  to "joinTemplateService",
            "com.it4logic.mindatory.model.repository.Stereotype"  to "stereotypeService",
            "com.it4logic.mindatory.model.security.SecurityUser" to "securityUserService",
            "com.it4logic.mindatory.model.security.SecurityRole" to "securityRoleService",
            "com.it4logic.mindatory.model.security.SecurityGroup" to "securityGroupService"
        )
    }
}