/*
    Copyright (c) 2018, IT4Logic.

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
package com.it4logic.mindatory.model.security

import javax.persistence.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

/**
 * ACL Object SID entity
 */
@Entity
@Table(name = "acl_sid", uniqueConstraints = [
    (UniqueConstraint(name = "acl_sid_uk_idx", columnNames = ["sid", "principal"]))
])
data class AclSid (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    @get: NotNull
    @Column(name = "principal", nullable = false)
    var principal: Boolean,

    @get: NotNull
    @get: Size(max = 100)
    @Column(name = "sid", nullable = false, length = 100)
    var sid: String
)

/**
 * ACL Object Class entity
 */
@Entity
@Table(name = "acl_class", uniqueConstraints = [
    (UniqueConstraint(name = "acl_class_uk_idx", columnNames = ["class"]))
])
data class AclClass (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    @get: NotNull
    @get: Size(max = 100)
    @Column(name = "class", nullable = false, length = 100)
    var className: String
)

/**
 * ACL Object Identity entity
 */
@Entity
@Table(name = "acl_object_identity", uniqueConstraints = [
    (UniqueConstraint(name = "acl_object_identity_uk_idx", columnNames = ["object_id_class", "object_id_identity"]))
])
data class AclObjectIdentity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    @get: NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "object_id_class", nullable = false)
    var objectIdClass: AclClass,

    @get: NotNull
    @Column(name = "object_id_identity", nullable = false)
    var objectIdIdentity: Long,

    @ManyToOne
    @JoinColumn(name = "parent_object")
    var parentObject: AclObjectIdentity,

    @ManyToOne
    @JoinColumn(name = "owner_sid")
    var ownerSid: AclSid,

    @get: NotNull
    @Column(name = "entries_inheriting", nullable = false)
    var entriesInheriting: Boolean
)

/**
 * ACL Entry entity
 */
@Entity
@Table(name = "acl_entry", uniqueConstraints = [
    (UniqueConstraint(name = "acl_entry_uk_idx", columnNames = ["acl_object_identity", "ace_order"]))
])
data class AclEntry (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    @get: NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "acl_object_identity", nullable = false)
    var aclObjectIdentity: AclObjectIdentity,

    @get: NotNull
    @Column(name = "ace_order", nullable = false)
    var aceOrder: Int,

    @get: NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "sid", nullable = false)
    var sid: AclSid,

    @get: NotNull
    @Column(name = "mask", nullable = false)
    var mask: Int,

    @get: NotNull
    @Column(name = "granting", nullable = false)
    var granting: Boolean,

    @get: NotNull
    @Column(name = "audit_success", nullable = false)
    var auditSuccess: Boolean,

    @get: NotNull
    @Column(name = "audit_failure", nullable = false)
    var auditFailure: Boolean
)