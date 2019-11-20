package com.it4logic.mindatory.services.common

class ApplicationServiceRegistry {
	companion object {
		val registry: HashMap<String, String> = hashMapOf(
			"com.it4logic.mindatory.model.mlc.Language" to "languageService",

			"com.it4logic.mindatory.model.Company" to "companyService",

			"com.it4logic.mindatory.model.security.SecurityUser" to "securityUserService",
			"com.it4logic.mindatory.model.security.SecurityRole" to "securityRoleService",
			"com.it4logic.mindatory.model.security.SecurityGroup" to "securityGroupService",

			"com.it4logic.mindatory.model.mail.MailTemplate" to "mailTemplateService",

			"com.it4logic.mindatory.model.project.Project" to "projectService",
			"com.it4logic.mindatory.model.project.ArtifactStore" to "artifactStoreService",
			"com.it4logic.mindatory.model.project.AttributeStore" to "attributeStoreService",
			"com.it4logic.mindatory.model.project.RelationStore" to "relationStoreService",

			"com.it4logic.mindatory.model.model.Model" to "modelService",
			"com.it4logic.mindatory.model.model.ArtifactTemplate" to "artifactTemplateService",
			"com.it4logic.mindatory.model.model.AttributeTemplate" to "attributeTemplateService",
			"com.it4logic.mindatory.model.model.RelationTemplate" to "relationTemplateService",
			"com.it4logic.mindatory.model.model.Stereotype" to "stereotypeService"
		)
	}
}