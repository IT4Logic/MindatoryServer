package com.it4logic.mindatory.services.security

import com.it4logic.mindatory.exceptions.ApplicationObjectNotFoundException
import com.it4logic.mindatory.security.ApplicationAclOwnerRequest
import com.it4logic.mindatory.security.ApplicationPermission
import com.it4logic.mindatory.security.ApplicationAclPermissionRequest
import com.it4logic.mindatory.security.ApplicationAclRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.PermissionEvaluator
import org.springframework.security.acls.domain.BasePermission
import org.springframework.security.acls.domain.GrantedAuthoritySid
import org.springframework.security.acls.jdbc.JdbcMutableAclService
import org.springframework.stereotype.Service
import org.springframework.security.acls.domain.ObjectIdentityImpl
import org.springframework.security.acls.domain.PrincipalSid
import org.springframework.security.acls.model.*
import org.springframework.security.core.Authentication
import org.springframework.security.acls.model.MutableAcl


@Service
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
    private lateinit var securityUserService: SecurityUserService

    /**
     * Grant a permission. Used when you don't have the instance available.
     *
     * @param permissionRequest [ApplicationAclPermissionRequest] object
     */
    fun addPermission(permissionRequest: ApplicationAclPermissionRequest) {
        val domainClass = Class.forName(permissionRequest.domainClass)
            ?: throw ApplicationObjectNotFoundException(permissionRequest.domainClass, "Domain Object Class")
        addPermission(domainClass, permissionRequest.id, permissionRequest.recipient, getPermission(permissionRequest.permission))
    }

    /**
     * Grant a permission. Used when you don't have the instance available.
     *
     * @param domainClass  the domain class
     * @param id  the instance id
     * @param recipient  the grantee; can be a username, role name, Sid, or Authentication
     * @param permission  the permission to grant
     */
    fun addPermission(domainClass: Class<*>, id: Long, recipient: Any, permission: Permission) {
        val oid = ObjectIdentityImpl(domainClass.javaClass, id)
        addPermission(oid, recipient, permission)
    }

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

    /**
     * Grant a permission. Used when you don't have the instance available.
     *
     * @param oid  [ObjectIdentity] object
     * @param recipient  the grantee; can be a username, role name, Sid, or Authentication
     * @param permission  the permission to grant
     */
    fun addPermission(oid: ObjectIdentity, recipient: Any, permission: Permission) {
        val sid = createSid(recipient)
        // Create or update the relevant ACL
        val acl: MutableAcl = try {
            aclService.readAclById(oid) as MutableAcl
        } catch (nfe: NotFoundException) {
            aclService.createAcl(oid)
        }
        // Now grant some permissions via an access control entry (ACE)
        acl.insertAce(acl.entries.size, permission, sid, true)
        aclService.updateAcl(acl)
    }

    /**
     * Update the owner of the domain class instance.
     *
     * @param ownerRequest [ApplicationAclOwnerRequest] object
     */
    fun changeOwner(ownerRequest: ApplicationAclOwnerRequest) {
        val domainClass = Class.forName(ownerRequest.domainClass)
            ?: throw ApplicationObjectNotFoundException(ownerRequest.domainClass, "Domain Object Class")

        val acl = readAcl(domainClass, ownerRequest.id) as MutableAcl

        //check if owner username exists
        securityUserService.findByUsername(ownerRequest.owner)

        acl.owner = PrincipalSid(ownerRequest.owner)

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
     * Grant a permission. Used when you don't have the instance available.
     *
     * @param permissionRequest [ApplicationAclPermissionRequest] object
     */
    fun deletePermission(permissionRequest: ApplicationAclPermissionRequest) {
        val domainClass = Class.forName(permissionRequest.domainClass)
            ?: throw ApplicationObjectNotFoundException(permissionRequest.domainClass, "Domain Object Class")
        deletePermission(domainClass, permissionRequest.id, permissionRequest.recipient, getPermission(permissionRequest.permission))
    }

    /**
     * Removes a granted permission. Used when you don't have the instance available.
     *
     * @param domainClass  the domain class
     * @param id  the instance id
     * @param recipient  the grantee; can be a username, role name, Sid, or Authentication
     * @param permission  the permission to remove
     */
    fun deletePermission(domainClass: Class<*>, id: Long, recipient: Any, permission: Permission ) {
        val sid = createSid(recipient)
        val acl = readAcl(domainClass, id) as MutableAcl

        acl.entries.forEachIndexed { index, entry ->
            if (entry.sid == sid && entry.permission == permission) {
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
     * @return  <code>true</code> if granted
     */
    fun hasPermission(authentication: Authentication, id: Long, domainClass: String, permission: Permission): Boolean {
        return permissionEvaluator.hasPermission(authentication, id, domainClass, permission)
    }

    /**
     * Helper method to retrieve the ACL for a domain class instance.
     *
     * @param aclRequest [ApplicationAclRequest] object
     * @return the {@link Acl} (never <code>null</code>)
     */
    fun readAcl(aclRequest: ApplicationAclRequest): Acl {
        val domainClass = Class.forName(aclRequest.domainClass)
            ?: throw ApplicationObjectNotFoundException(aclRequest.domainClass, "Domain Object Class")
        return readAcl(domainClass, aclRequest.id)
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

    /**
     * Convert [ApplicationPermission] object to [Permission] object
     *
     * @param permission [ApplicationPermission] object
     * @return [Permission] object
     */
    private fun getPermission(permission: ApplicationPermission): Permission {
        return when (permission) {
            ApplicationPermission.Read -> BasePermission.READ
            ApplicationPermission.Write -> BasePermission.WRITE
            ApplicationPermission.Create -> BasePermission.CREATE
            ApplicationPermission.Delete -> BasePermission.DELETE
            ApplicationPermission.Administration -> BasePermission.ADMINISTRATION
        }
    }
}