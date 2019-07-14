package com.it4logic.mindatory.bootstrapping

import com.it4logic.mindatory.mlc.LanguageManager
import com.it4logic.mindatory.model.AppPreferences
import com.it4logic.mindatory.model.ApplicationMetadata
import com.it4logic.mindatory.model.ApplicationMetadataRepository
import com.it4logic.mindatory.model.Company
import com.it4logic.mindatory.model.mlc.Language
import com.it4logic.mindatory.model.security.SecurityGroup
import com.it4logic.mindatory.model.security.SecurityRole
import com.it4logic.mindatory.model.security.SecurityUser
import com.it4logic.mindatory.security.ApplicationSecurityPermissions
import com.it4logic.mindatory.services.AppPreferencesService
import com.it4logic.mindatory.services.CompanyService
import com.it4logic.mindatory.services.LanguageService
import com.it4logic.mindatory.services.security.SecurityGroupService
import com.it4logic.mindatory.services.security.SecurityRoleService
import com.it4logic.mindatory.services.security.SecurityUserService
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Service
import javax.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

@Service
class SystemData100 : InitializingBean {

    private val logger = LoggerFactory.getLogger(SystemData100::class.java)

    @Autowired
    private lateinit var languageService: LanguageService

    @Autowired
    private lateinit var securityRoleService: SecurityRoleService

    @Autowired
    private lateinit var securityGroupService: SecurityGroupService

    @Autowired
    private lateinit var securityUserService: SecurityUserService

    @Autowired
    private lateinit var companyService: CompanyService

    @Autowired
    private lateinit var appPreferencesService: AppPreferencesService

    @Autowired
    private lateinit var applicationMetadataRepository: ApplicationMetadataRepository

    @Autowired
    lateinit var languageManager: LanguageManager

    lateinit var enLanguage: Language
    lateinit var arLanguage: Language

    @Transactional
    override fun afterPropertiesSet() {
        if(applicationMetadataRepository.count() > 0)
            return

        logger.debug("Creating system data started")

        createLanguageData()
        createSecurityData()
        createBaseData()

        logger.debug("Creating system data finished")
    }

    fun createLanguageData() {
        logger.debug("Creating language data started")

        val result = languageService.findAll(null, null, null) as List<*>
        if(result.isNotEmpty())
            return
        enLanguage = languageService.create(Language("en", "English", true))
        arLanguage = languageService.create(Language("ar", "عربي", false))

        logger.debug("Creating language data finished")
    }

    fun createSecurityData() {
        logger.debug("Creating security data started")

        val roleAdmin = securityRoleService.create(
            SecurityRole("Admins Role", "Role for system wide administrator",
                permissions = arrayListOf( ApplicationSecurityPermissions.SystemWideAdmin ))
            )
        
        val adminGroup = securityGroupService.create(SecurityGroup("Admins Group", "Group for Admins"))

        val adminUser = securityUserService.create(
            SecurityUser("admin", "password", fullName = "System Admin", email = "admin@local",
                roles = mutableListOf(roleAdmin), group = adminGroup)
            )

        languageManager.currentLanguage = arLanguage

        roleAdmin.name = "دور مدير النظام"
        roleAdmin.description = "دور لمدير النظام بالكامل"
        securityRoleService.update(roleAdmin)

        adminGroup.name = "مجموعة المديرين"
        adminGroup.description = "مجموعة مديري النظام"
        securityGroupService.update(adminGroup)

        adminUser.fullName = "مدير النظام"
        securityUserService.update(adminUser)

        languageManager.currentLanguage = enLanguage

        logger.debug("Creating security data finished")
    }

    fun createBaseData() {
        companyService.create(Company("Mindatory"))
        appPreferencesService.create(AppPreferences())
        applicationMetadataRepository.save(ApplicationMetadata("1", "0", "0"))
    }
}