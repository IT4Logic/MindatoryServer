/*
    Copyright (c) 2019, IT4Logic.

    This file is part of Mindatory project by IT4Logic.

    Mindatory is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Mindatory is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar.  If not, see <https://www.gnu.org/licenses/>.

 */
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

/**
 * ACL Security Service
 */
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
     * Delete Acl Object
     *
     * @param domainObject Object instance
     */
    fun deleteAcl(domainObject: ApplicationEntityBase) {
        val oid = objectIdentityRetrievalStrategy.getObjectIdentity(domainObject)
        aclService.deleteAcl(oid, true)
    }
}