package com.it4logic.mindatory.services.security

import com.it4logic.mindatory.exceptions.ApplicationObjectNotFoundException
import com.it4logic.mindatory.model.common.ApplicationEntityBase
import com.it4logic.mindatory.model.security.SecurityUser
import com.it4logic.mindatory.model.security.SecurityUserRepository
import com.it4logic.mindatory.security.ApplicationPermission
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.PermissionEvaluator
import org.springframework.security.acls.domain.GrantedAuthoritySid
import org.springframework.security.acls.jdbc.JdbcMutableAclService
import org.springframework.stereotype.Service
import org.springframework.security.acls.domain.ObjectIdentityImpl
import org.springframework.security.acls.domain.PrincipalSid
import org.springframework.security.acls.model.*
import org.springframework.security.core.Authentication
import org.springframework.security.acls.model.MutableAcl
import javax.transaction.Transactional


@Service
@Transactional
class SecurityAclService {
    @Autowired
    private lateinit var aclService: JdbcMutableAclService

    @Autowired
    private lateinit var objectIdentityRetrievalStrategy: ObjectIdentityRetrievalStrategy

    @Autowired
    private lateinit var objectIdentityGenerator: ObjectIdentityGenerator

    @Autowired
    private lateinit var permissionEvaluator: PermissionEvaluator

    @Autowired
    private lateinit var userRepository: SecurityUserRepository


//    /**
//     * Grant a permission. Used when you don't have the instance available.
//     *
//     * @param domainClass  the domain class
//     * @param id  the instance id
//     * @param recipient  the grantee; can be a username, role name, Sid, or Authentication
//     * @param permission  the permission to grant
//     */
//    fun addPermission(domainClass: Class<*>, id: Long, recipient: Any, permission: Permission) {
//        val oid = ObjectIdentityImpl(domainClass, id)
//        addPermission(oid, recipient, permission)
//    }

//    /**
//     * Grant a permission. Used when you have the instance available.
//     *
//     * @param domainObject  the domain class instance
//     * @param recipient  the grantee; can be a username, role name, Sid, or Authentication
//     * @param permission  the permission to grant
//     */
//    fun addPermission(domainObject: ApplicationEntityBase, recipient: Any, permission: Permission) {
//        val oid = objectIdentityRetrievalStrategy.getObjectIdentity(domainObject)
//        addPermission(oid, recipient, permission)
//    }

//    /**
//     * Grant a permission. Used when you don't have the instance available.
//     *
//     * @param oid  [ObjectIdentity] object
//     * @param recipient  the grantee; can be a username, role name, Sid, or Authentication
//     * @param permission  the permission to grant
//     */
//    fun addPermission(oid: ObjectIdentity, recipient: Any, permission: Permission) {
//        val sid = createSid(recipient)
//        // Create or update the relevant ACL
//        val acl: MutableAcl = try {
//            aclService.readAclById(oid) as MutableAcl
//        } catch (nfe: NotFoundException) {
//            aclService.createAcl(oid)
//        }
//        // Now grant some permissions via an access control entry (ACE)
//        acl.insertAce(acl.entries.size, permission, sid, true)
//        aclService.updateAcl(acl)
//    }

    /**
     * Grant a permission. Used when you don't have the instance available.
     *
     * @param domainClass  [ObjectIdentity] object
     * @param id  Object id
     * @param recipient  the grantee; can be a username, role name, Sid, or Authentication
     * @param permissions  the permissions to grant
     */
    fun addPermission(domainClass: Class<*>, id: Long, recipient: Any, permissions: List<ApplicationPermission>) {
        addPermission(ObjectIdentityImpl(domainClass, id), recipient, permissions)
    }

    /**
     * Grant a permission. Used when you don't have the instance available.
     *
     * @param oid  [ObjectIdentity] object
     * @param recipient  the grantee; can be a username, role name, Sid, or Authentication
     * @param permissions  the permissions to grant
     */
    fun addPermission(oid: ObjectIdentity, recipient: Any, permissions: List<ApplicationPermission>) {
        val sid = createSid(recipient)
        // Create or update the relevant ACL
        val acl: MutableAcl = try {
            aclService.readAclById(oid) as MutableAcl
        } catch (nfe: NotFoundException) {
            aclService.createAcl(oid)
        }
        // Now grant some permissions via an access control entry (ACE)
        for(permission in permissions)
            acl.insertAce(acl.entries.size, permission.toPermission(), sid, true)
        aclService.updateAcl(acl)
    }

    /**
     * change the owner of the domain class instance.
     *
     * @param domainClass  [ObjectIdentity] object
     * @param id  Object id
     * @param owner  the new owner
     */

    fun changeOwner(domainClass: Class<*>, id: Long, owner: String) {
        val acl = readAcl(domainClass, id) as MutableAcl

        //check if owner username exists
        userRepository.findByUsername(owner).orElseThrow { ApplicationObjectNotFoundException(owner, SecurityUser::class.java.simpleName.toLowerCase()) }

        acl.owner = PrincipalSid(owner)

        aclService.updateAcl(acl)
    }

//    /**
//     * Update the owner of the domain class instance.
//     *
//     * @param domainObject  the domain class instance
//     * @param newUsername  the new username
//     */
//    fun changeOwner(domainObject: ApplicationEntityBase, newUsername: String) {
//        val acl = readAcl(domainObject) as MutableAcl
//        acl.owner = PrincipalSid(newUsername)
//        aclService.updateAcl(acl)
//    }


    /**
     * Removes a granted permission. Used when you don't have the instance available.
     *
     * @param domainClass  the domain class
     * @param id  Object id
     * @param recipient  the grantee; can be a username, role name, Sid, or Authentication
     * @param permissions  the permissions to remove
     */
    fun deletePermission(domainClass: Class<*>, id: Long, recipient: Any, permissions: List<ApplicationPermission> ) {
        val sid = createSid(recipient)
        val acl = readAcl(domainClass, id) as MutableAcl

        acl.entries.forEachIndexed { index, entry ->
            if (entry.sid == sid && permissions.contains(ApplicationPermission.fromPermission(entry.permission)) ) {
                acl.deleteAce(index)
            }
        }
        aclService.updateAcl(acl)
    }



//    /**
//     * Check if the authentication has grants for the specified permission(s) on the domain class instance.
//     *
//     * @param authentication  an authentication representing a user and roles
//     * @param domainObject  the domain class instance
//     * @param permissions  one or more permissions to check
//     * @return  <code>true</code> if granted
//     */
//    fun hasPermission(authentication: Authentication, domainObject: Any, permission: Permission): Boolean {
//        return permissionEvaluator.hasPermission(authentication, domainObject, permission)
//    }

    /**
     * Check if the authentication has grants for the specified permission(s) on the domain class instance.
     *
     * @param authentication  an authentication representing a user and roles
     * @param id the domain object id
     * @param domainClass  the domain class name
     * @param permission  permission to check
     *
     * @return  <code>true</code> if granted
     */
    fun hasPermission(authentication: Authentication, id: Long, domainClass: String, permission: Permission): Boolean {
        return permissionEvaluator.hasPermission(authentication, id, domainClass, permission)
    }

//    fun hasPermission(domainObject: ApplicationEntityBase, authentication: Authentication, permission: Permission): Boolean {
//        return permissionEvaluator.hasPermission(authentication, domainObject, permission)
//    }


    /**
     * Create Acl object and save it, or load it if exists
     *
     * @param domainObject Object instance
     * @param recipient the grantee; can be a username, role name, Sid, or Authentication
     *
     * @return Saved or loaded Acl object
     */
    fun createAcl(domainObject: ApplicationEntityBase, recipient: Any): MutableAcl {
        val oid = objectIdentityRetrievalStrategy.getObjectIdentity(domainObject)
        // Create or update the relevant ACL
        return try {
            aclService.readAclById(oid) as MutableAcl
        } catch (nfe: NotFoundException) {
            aclService.createAcl(oid)
        }
    }



//    /**
//     * Helper method to retrieve the ACL for a domain class instance.
//     *
//     * @param domainObject  the domain class instance
//     * @return the {@link Acl} (never <code>null</code>)
//     */
//    fun readAcl(domainObject: ApplicationEntityBase): Acl {
//        return aclService.readAclById(objectIdentityRetrievalStrategy.getObjectIdentity(domainObject))
//    }

    /**
     * Helper method to retrieve the ACL for a domain class instance.
     *
     * @param domainClass  the domain class
     * @param id  the instance id
     * @return the {@link Acl} (never <code>null</code>)
     */
    fun readAcl(domainClass: Class<*>, id: Long): Acl {
        return aclService.readAclById(objectIdentityGenerator.createObjectIdentity(id, domainClass.name))
    }

//    /**
//     * Helper method to delete an ACL for a domain class.
//     *
//     * @param domainObject  the domain class instance
//     */
//    fun deleteAcl(domainObject: Any) {
//        aclService.deleteAcl(objectIdentityRetrievalStrategy.getObjectIdentity(domainObject), false)
//    }

    /**
     * Create [Sid] object from recipient username or role name
     *
     * @param recipient username or role name
     * @return [Sid] object
     */
    private fun createSid(recipient: Any): Sid {
        if (recipient is String) {
            return if(recipient.startsWith("ROLE_")) GrantedAuthoritySid(recipient) else PrincipalSid(recipient)
        }

        if (recipient is Sid) {
            return recipient
        }

        if (recipient is Authentication) {
            return PrincipalSid(recipient)
        }

        throw IllegalArgumentException("recipient must be a String, Sid, or Authentication")
    }

//    /**
//     * Convert [ApplicationPermission] object to [Permission] object
//     *
//     * @param permission [ApplicationPermission] object
//     * @return [Permission] object
//     */
//    private fun getPermission(permission: ApplicationPermission): Permission {
//        return when (permission) {
//            ApplicationPermission.View -> BasePermission.READ
//            ApplicationPermission.Modify -> BasePermission.WRITE
//            ApplicationPermission.Create -> BasePermission.CREATE
//            ApplicationPermission.Delete -> BasePermission.DELETE
//            ApplicationPermission.Administration -> BasePermission.ADMINISTRATION
//        }
//    }

    /**
     * Delete Acl Object
     *
     * @param domainObject Object instance
     */
    fun deleteAcl(domainObject: ApplicationEntityBase) {
        val oid = objectIdentityRetrievalStrategy.getObjectIdentity(domainObject)
        aclService.deleteAcl(oid, true)
    }
}